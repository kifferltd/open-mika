/**************************************************************************
* Copyright (c) 2023 by KIFFER Ltd. All rights reserved.                  *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       *
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    *
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         *
* OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  *
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                              *
**************************************************************************/

#ifndef _PROCESSOR_H
#define _PROCESSOR_H

/*
** Machine dependent stuff for the MIPS Architecture
*/

#include <sys/types.h>

#ifndef __BIT_TYPES_DEFINED__
#error You have work to do here! See wonka/include/wonka.h.
#endif

/*
** A double word quantity is represented as an array of two 32 bit words. 
** LSW(l) and MSW(l) return the lower 32 bits and the upper 32 bits of a 64 bit quantity according
** to the settings of the MS_WORD and LS_WORD parameters. Our Wonka machine always uses these
** macros to get at double word values (be it floating point doubles or long integers).
**
*/

#ifndef __BIG_ENDIAN
#define __BIG_ENDIAN 4321
#endif

#ifndef __LITTLE_ENDIAN
#define __LITTLE_ENDIAN 1234
#endif

#ifndef __BYTE_ORDER
#define __BYTE_ORDER __LITTLE_ENDIAN
#endif

#define WORDS2LONG(m, l)             (w_long)(((w_long)(m) << 32) | (l))

#define WORD_MSW                       1
#define WORD_LSW                       0

#endif /* _PROCESSOR_H */
