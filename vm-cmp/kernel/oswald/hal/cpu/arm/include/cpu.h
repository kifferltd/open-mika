#ifndef _CPU_H
#define _CPU_H

/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
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
