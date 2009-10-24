#ifndef _ATOMIC_H
#define _ATOMIC_H

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

#include <types.h>

inline static void x_atomic_swap(void * address_1, void * address_2) {

  asm volatile (
    "movl (%%eax), %%eax\n\t"    // Move *address_1 to %eax
    "xchgl %%eax, (%%edx)\n\t"   // Atomically swap contents between %eax and *address_2
    "movl %%eax, (%%ecx)\n\t"    // Move swapped contents from %eax to *address_1
    : /* outputs */
    : /* inputs */
      "a" (address_1),
      "d" (address_2),
      "c" (address_1)
  );

}

inline static int xi_enter_atomic(void * address, void * contents) {

  int result;

  asm volatile (
    "movl $1, %%eax\n\t"                    // Load the 'looking value' in %eax
    "xchgl %%eax, (%%edx)\n\t"              // Atomically exchange the contents and lock others out
    "cmpl $1, %%eax\n\t"                    // Is anyone else trying to look ?
    "je 2f\n\t"                             // Yes, jump to return false
    "cmpl $0, %%eax\n\t"                    // No one was looking, is the content 0 ?
    "jne 1f\n\t"                            // No, jump to replace old contents
    "movl %2, (%1)\n\t"                     // Yes, so we put our new contents in
    "movl $1, %0\n\t"                       // Prepare true return value ...
    "jmp 3f\n\t"                            // ... and return
    "1:\n\t"
    "movl %%eax, (%%edx)\n\t"               // Restore old contents
    "2:\n\t"
    "movl $0, %0\n\t"                       // Prepare false return
    "3:\n\t"
    : /* outputs */
      "=m" (result)                         // %0
    : /* inputs */
      "d" (address),                        // %1 == %edx
      "c" (contents)                        // %2 
    : /* clobbered */
      "eax"
  );

  return result;

}

inline static int xi_exit_atomic(void * address) {

  int result = 0;

  asm volatile (
    "movl $1, %%eax\n\t"                    // Load the 'looking value' in %eax
    "xchgl %%eax, (%%edx)\n\t"              // Atomically exchange the contents and lock others out
    "cmpl $1, %%eax\n\t"                    // Is anyone else trying to look ?
    "je 1f\n\t"                             // Yes, jump to return false
    "movl $0, (%1)\n\t"                     // No, so we put NULL in
    "movl $1, %0\n\t"                       // Prepare true return value
    "1:\n\t"
    : /* outputs */
      "=m" (result)                         // %0
    : /* inputs */
      "d" (address)                         // %1 == %edx
    : /* clobbered */
      "eax"
  );

  return result;

}

#endif /* _ATOMIC_H */
