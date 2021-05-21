@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
@ Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 @
@                                                                         @
@ This software is copyrighted by and is the sole property of Acunia N.V. @
@ and its licensors, if any. All rights, title, ownership, or other       @
@ interests in the software remain the property of Acunia N.V. and its    @
@ licensors, if any.                                                      @
@                                                                         @
@ This software may only be used in accordance with the corresponding     @
@ license agreement. Any unauthorized use, duplication, transmission,     @
@  distribution or disclosure of this software is expressly forbidden.    @
@                                                                         @
@ This Copyright notice may not be removed or modified without prior      @
@ written consent of Acunia N.V.                                          @
@                                                                         @
@ Acunia N.V. reserves the right to modify this software without notice.  @
@                                                                         @
@   Acunia N.V.                                                           @
@   Vanden Tymplestraat 35      info@acunia.com                           @
@   3000 Leuven                 http://www.acunia.com                     @
@   Belgium - EUROPE                                                      @
@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@

@
@ $Id: context.s,v 1.1 2004/12/13 22:53:19 cvs Exp $
@

.file "context.s"

.align 4
.text
.global x_context_save

x_context_save:
    str lr, [r0, #0]        @ Store return address in exception structure as first field
    str sp, [r0, #4]        @ Store current stack pointer as second field in exception
    add r0, r0, #8          @ Skip to the register array in the exception structure
    stmia r0!, { r4 - r12 } @ Store callee saved registers r4 to r12
    mov r0, #0              @ Prepare return register to contain 0 as result
    mov pc, lr

.align 4
.text
.global x_context_restore

x_context_restore:
    mov r2, r0              @ Get exception pointer in r2
    mov r0, r1              @ Move return argument into the result register
    ldr lr, [r2, #0]        @ Load lr with return address
    ldr sp, [r2, #4]        @ Restore stack pointer from context save
    add r2, r2, #8          @ Skip to register array in exception structure
    ldmia r2!, { r4 - r12}  @ Restore saved registers
    mov pc, lr              @ Return back to saved return address
