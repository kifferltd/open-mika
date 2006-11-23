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
@ $Id: memcpy.s,v 1.1 2004/07/18 17:54:33 cvs Exp $
@

.text
.align 0
.global memcpy

@ r0 = destination address
@ r1 = source address
@ r2 = number of bytes to copy

memcpy:
  teq       r2, #0                  @ first do a reality check
  moveq     pc, lr                  @ return when nothing to do
  stmdb     sp!, {r4 - r5, lr}      @ save what we clobber
  mov       r5, r0                  @ save r0, keep it as a return value
  mov       r3, r2, lsr #3          @ divide number of bytes by 8
  mov       r4, r3, lsl #3          @ multiply r3 by 8
  subs      r2, r2, r4              @ find the remaining number of single byte copies
  teq       r3, #0
  beq       byte_copy

long_copy:
  ldrb      r4, [r1], #1
  strb      r4, [r5], #1
  ldrb      r4, [r1], #1
  strb      r4, [r5], #1
  ldrb      r4, [r1], #1
  strb      r4, [r5], #1
  ldrb      r4, [r1], #1
  strb      r4, [r5], #1
  ldrb      r4, [r1], #1
  strb      r4, [r5], #1
  ldrb      r4, [r1], #1
  strb      r4, [r5], #1
  ldrb      r4, [r1], #1
  strb      r4, [r5], #1
  ldrb      r4, [r1], #1
  strb      r4, [r5], #1
  subs      r3, r3, #1
  bne       long_copy
  teq       r2, #0
  beq       memcpy_end

byte_copy:
  ldrb      r4, [r1], #1
  strb      r4, [r5], #1
  subs      r2, r2, #1
  bne       byte_copy

memcpy_end:
  ldmia     sp!, {r4 - r5, pc}      @ return, r0 still contains destination address as return value
