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
@ $Id: strlen.s,v 1.1 2004/07/18 17:54:33 cvs Exp $
@

			.text
			.align 0
			.global strlen

strlen:
			mov	r1, r0
loop:
			ldrb	r2, [r1],#1
			cmp	r2, #0
			bne	loop
			sub	r0, r1, r0
			sub	r0, r0, #1
			mov	pc, lr
