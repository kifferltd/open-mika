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
@ $Id: calls.s,v 1.1 2004/07/18 17:54:33 cvs Exp $
@

.text
.align 4

@
@ unsigned long long call_static(JNIEnv * env, w_instance Class, x_slot top, w_method method)
@
@                                      r0            r1                r2            r3
@
@ the returned result is in r1:r0 and is not touched by us upon return
@

.global _call_static

_call_static:
    stmfd sp!, {r4-r7, lr}  @ Save working registers and link register
    ldr r4, [r3, #4]        @ Get argument count in r4
    ldr r5, [r3, #8]        @ Get function pointer in r5
    mov r6, sp              @ Save stack pointer for resetting after the call
    cmp r4, #0              @ Are there any further arguments?
    beq cs_do_call          @ No further arguments; do the call

    mov lr, r2              @ Get the stack pointer in lr
    sub lr, lr, r4, lsl #3  @ Make it point to first argument; note size of a stack slot is 8 bytes
    ldr r2, [lr], #8        @ Get next argument in r2 and increment java stack pointer
    subs r4, r4, #1         @ Reduce argument count
    beq cs_do_call          @ When 0, do the call

    ldr r3, [lr], #8        @ Get next argument in r3 and increment java stack pointer
    subs r4, r4, #1         @ Reduce argument count
    beq cs_do_call          @ When 0, do the call

    add lr, lr, r4, lsl #3  @ Make java stack point to last argument
  cs_do_push:
    ldr r7, [lr, #-8] !     @ Get argument from Java stack and decrement java stack pointer (r7 = [lr - 8]; lr -= 8)
    str r7, [sp, #-4] !     @ Push it on the native stack, decrement stack pointer before storing
    subs r4, r4, #1         @ Decrement argument count
    bne cs_do_push          @ When not done, push some more

  cs_do_call:
    mov lr, pc              @ Prepare new link register ---- pc points to --------+
    mov pc, r5              @ Do the call                                         |
    mov sp, r6              @ Restore the saved stack pointer <-------------------+
    ldmfd sp!, {r4-r7, pc}  @ Go back to our own caller...

@
@ unsigned long long call_instance(JNIEnv * env, x_slot stacktop, w_method method)
@
@                                      r0            r1                r2
@
@ the returned result is in r1:r0 and is not touched by us upon return
@

.global _call_instance

_call_instance:
    stmfd sp!, {r4-r7, lr}  @ Save working registers and link register
    ldr r4, [r2, #4]        @ Get argument count in r4
    ldr r5, [r2, #8]        @ Get function pointer in r5
    mov r6, sp              @ Save stack pointer for resetting after the call

    mov lr, r1              @ Get the stack pointer in lr
    sub lr, lr, r4, lsl #3  @ Make it point to first argument; note size of a stack slot is 8 bytes
    ldr r1, [lr], #8        @ Get next argument in r1 and increment java stack pointer
    subs r4, r4, #1         @ Reduce argument count
    beq ci_do_call          @ When 0, do the call

    ldr r2, [lr], #8        @ Get next argument in r2 and increment java stack pointer
    subs r4, r4, #1         @ Reduce argument count
    beq ci_do_call          @ When 0, do the call

    ldr r3, [lr], #8        @ Get next argument in r3 and increment java stack pointer
    subs r4, r4, #1         @ Reduce argument count
    beq ci_do_call          @ When 0, do the call

    add lr, lr, r4, lsl #3  @ Make java stack point to last argument
  ci_do_push:
    ldr r7, [lr, #-8] !     @ Get argument from Java stack and decrement java stack pointer (r7 = [lr - 8]; lr -= 8)
    str r7, [sp, #-4] !     @ Push it on the native stack, decrement stack pointer before storing
    subs r4, r4, #1         @ Decrement argument count
    bne ci_do_push          @ When not done, push some more

  ci_do_call:
    mov lr, pc              @ Prepare new link register ---- pc points to --------+
    mov pc, r5              @ Do the call                                         |
    mov sp, r6              @ Restore the saved stack pointer <-------------------+
    ldmfd sp!, {r4-r7, pc}  @ Go back to our own caller...
