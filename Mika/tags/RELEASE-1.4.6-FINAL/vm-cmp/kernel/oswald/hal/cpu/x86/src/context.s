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
# $Id: context.s,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
#

.file "context.s"

.text
.align 4
.global x_context_save

#
# int x_context_save(x_exception exception);
#
# eax                        4(%esp)
#
# Note that we don't use a frame pointer %ebp. The caller saves 3
# registers %eax, %ecx and %edx, and we need to save %ebp, %ebx, %edi
# and %esi. Since we don't use a frame pointer, the return address is
# on the top of the stack.
#

x_context_save:
    movl 4(%esp), %edx      # Load the exception pointer pointer in edx
    movl (%esp), %eax       # Load return address which is on top of stack, in %eax
    movl %eax,  0(%edx)     # And save as return address
    movl %esp,  4(%edx)     # Save current stack pointer
    movl %ebx,  8(%edx)     # First callee saved register
    movl %ebp, 12(%edx)     # Second callee saved register
    movl %esi, 16(%edx)     # Third callee saved register
    movl %edi, 20(%edx)     # Fourth callee saved register
    xorl %eax, %eax         # Make return value 0
    ret                     # Do a 'normal' return

.align 4
.global x_context_restore

#
# void x_context_restore(x_exception exception, int r);
#
# eax                                4(%esp)   8(%esp)
#

x_context_restore:
    movl  4(%esp), %edx     # Load the exception pointer pointer in edx
    movl  8(%esp), %eax     # Load %eax with return value
    movl  4(%edx), %esp     # Restore stack pointer to point of calling x_context_save
    movl  0(%edx), %ecx     # Get saved return address in %ecx
    movl  %ecx, 0(%esp)     # Replace old return address with saved return address
    movl  8(%edx), %ebx     # Restore callee saved register %ebx
    movl 12(%edx), %ebp     # Restore callee saved register %ebp
    movl 16(%edx), %esi     # Restore callee saved register %esi
    movl 20(%edx), %edi     # Restore callee saved register %edi
    ret                     # And return back to our saved point
