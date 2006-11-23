#ifndef _CPU_H
#define _CPU_H

/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
*  distribution or disclosure of this software is expressly forbidden.    *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: cpu.h,v 1.1 2004/07/18 17:12:48 cvs Exp $
*/

#include <types.h>
#include <stdio.h>   /* temporary, printf() */

#ifndef ARM
#define ARM
#endif

#ifndef arm
#define arm
#endif

/*
** The number of registers (beside PC and SP) that need to be
** saved in a context for x_context_save and x_context_restore.
*/

#define NUM_CALLEE_SAVED 9

/*
** This structure contains the cpu state of the ARM processor.
*/

typedef struct x_cpu {
  x_word sp;
  x_word pc;  
  x_word lr;
  x_word status;
} x_cpu;

void x_init_start(void * init);
x_int x_cpu_relocate(x_int type, x_address S, x_address A, x_address P);

inline static unsigned long long x_sample(void) {
  return 0LL;        
}
        
x_ubyte *x_cpu_setup(x_ubyte *memory);

#endif /* _CPU_H */
