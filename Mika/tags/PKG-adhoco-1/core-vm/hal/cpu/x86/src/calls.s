###########################################################################
# Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 #
#                                                                         #
# This software is copyrighted by and is the sole property of Acunia N.V. #
# and its licensors, if any. All rights, title, ownership, or other       #
# interests in the software remain the property of Acunia N.V. and its    #
# licensors, if any.                                                      #
#                                                                         #
# This software may only be used in accordance with the corresponding     #
# license agreement. Any unauthorized use, duplication, transmission,     #
#  distribution or disclosure of this software is expressly forbidden.    #
#                                                                         #
# This Copyright notice may not be removed or modified without prior      #
# written consent of Acunia N.V.                                          #
#                                                                         #
# Acunia N.V. reserves the right to modify this software without notice.  #
#                                                                         #
#   Acunia N.V.                                                           #
#   Vanden Tymplestraat 35      info@acunia.com                           #
#   3000 Leuven                 http://www.acunia.com                     #
#   Belgium - EUROPE                                                      #
###########################################################################

#
# $Id: calls.s,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
#

.file "calls.s"

.text
.align 4
.global _call_static

#
# MSW:LSW _call_static(w_word * env, w_instance Class, x_slot top, w_method method);
#
# edx:eax                8(ebp)         12(ebp)          16(ebp)         20(ebp)

_call_static:
   pushl %ebp                     # save %ebp
   movl %esp, %ebp                # and overwrite it with the new stack pointer
   subl $4, %esp                  # make room for 1 local argument
   movl 20(%ebp), %ecx            # get w_method pointer
   movl 4(%ecx), %edx             # push argument count into %edx
   incl %edx                      # increment with 1 for JNIEnv * argument
   incl %edx                      # increment with 1 for Class instance argument
   movl %edx, -4(%ebp)            # save new argument count in local variable

   movl 16(%ebp), %eax            # get argument pointer in %eax
   decl %edx
   decl %edx                      # restore correct argument count
   jz cs_no_args                  # when argument count is 0, bypass argument pushing (only with static methods)

cs_push:
   subl $8, %eax                  # decrement item pointer (at entry, this is OK since the stack is 1 off)
   pushl (%eax)                   # push data contents on stack
   decl %edx                      # decrement argument count
   jnz cs_push                    # if not 0, push more arguments
cs_no_args:
   pushl 12(%ebp)                 # push Class instance on the call stack
   pushl 8(%ebp)                  # push JNIEnv * on the call stack
   call *8(%ecx)                  # call our function
   addl -4(%ebp), %esp            # reset our call stack pointer with saved local variable
      
cs_epilogue:
   movl %ebp, %esp                # restore previous stack pointer
   popl %ebp                      # restore %ebp saved at the entry
   ret                            # and return to our caller

.text
.align 4
.global _call_instance

#
# MSW:LSW _call_instance(w_word * env, x_slot top, w_method method);
#
# edx:eax                 8(ebp)         12(ebp)         16(ebp) 
#

_call_instance:
   pushl %ebp                     # save %ebp
   movl %esp, %ebp                # and overwrite it with the new stack pointer
   subl $8, %esp                  # make room for 1 local argument
   movl 16(%ebp), %ecx            # get w_method pointer
   movl 4(%ecx), %edx             # push argument count into %edx
   incl %edx                      # increment with 1 for JNIEnv * argument
   movl %edx, -4(%ebp)            # save new argument count in local variable

   movl 12(%ebp), %eax            # get argument pointer in %eax
   decl %edx                      # restore correct argument count

ci_push:                          # there always is a 'this' argument, so no need to check if argument count == 0
   subl $8, %eax                  # decrement item pointer (at entry, this is OK since the stack is 1 off)
   pushl (%eax)                   # push data contents on stack
   decl %edx                      # decrement argument count
   jnz ci_push                    # if not 0, push more arguments
   pushl 8(%ebp)                  # push JNIEnv * on the call stack
   call *8(%ecx)                  # call our function 
   addl -4(%ebp), %esp            # reset our call stack pointer with saved local variable
      
ci_epilogue:
   movl %ebp, %esp                # restore previous stack pointer
   popl %ebp                      # restore %ebp saved at the entry
   ret                            # and return to our caller
