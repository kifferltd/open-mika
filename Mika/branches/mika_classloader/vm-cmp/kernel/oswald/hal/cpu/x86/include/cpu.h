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
** $Id: cpu.h,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
**
** The CPU specific bits for an Intel X86 architecture.
*/

#include <types.h>
#include <host.h>

#ifndef X86
#define X86
#endif

#ifndef x86
#define x86
#endif

/*
** This defines the number of 32 bit words we need to save in a
** context environment.
*/

#define NUM_CALLEE_SAVED 4

typedef struct x_cpu {
  x_word sp;
  x_word pc;
  x_word status;
} x_cpu;

x_ubyte *x_cpu_setup(x_ubyte *memory);

void x_init_start(void * init);

x_int x_cpu_relocate(x_int type, x_address S, x_address A, x_address P);

#endif /* _CPU_H */
