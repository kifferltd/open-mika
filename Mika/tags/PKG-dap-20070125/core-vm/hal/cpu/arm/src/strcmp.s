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
@ $Id: strcmp.s,v 1.1 2004/07/18 17:54:33 cvs Exp $
@

.text
.align 0
.global strcmp

@ 
@ int strcmp(const unsigned char *s1, const unsigned char *s2);
@
@ r0 = pointer to string 1
@ r1 = pointer to string 2
@
@ return value:
@
@ r0 < 0 when string 1 is smaller than string 2
@ r0 > 0 when string 1 is greather than string 2
@ r0 = 0 when string 1 equals string 2
@

strcmp:
  stmdb     sp!, {r4, lr}           @ save what we clobber
  mov       r4, r0                  @ use r4 as base register for string 1

compare:
  ldrb      r0, [r4], #1            @ get byte from first string
  ldrb      r3, [r1], #1            @ get byte from second string
  cmp       r0, r3                  @ are both bytes equal
  bne       strcmp_end              @ when "no" return

strcmp_end:
  cmp       r0, #0
  moveq     r0, #1
  ldmia     sp!, {r4, pc}           @ return








