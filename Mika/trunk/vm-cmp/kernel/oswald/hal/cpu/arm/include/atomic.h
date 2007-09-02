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

/*
** $Id: atomic.h,v 1.1 2004/07/18 17:12:48 cvs Exp $
*/

inline static void x_atomic_swap(void * address_1, void * address_2) {

  asm volatile (
    "mov %2, %0\n\t"          // Load r2 with address_1
    "ldr %2, [%2]\n\t"        // Load r2 with contents of address_1
    "swp %2, %2, [%1]\n\t"    // Do the atomic swap with the contents at address_2
    "str %2, [%0]\n\t"        // Store the old contents of address_2 at address_1
    : /* outputs */
    : /* inputs */
      "r" (address_1),
      "r" (address_2),
      "r" (0)                 // Hack to get a non specific scratch register
  );

}

inline static int xi_enter_atomic(void * address, void * contents) {

  int result;
  
  asm volatile (
    "mov %0, #1\n\t"        // Use result register as scratch and load with 'looking' value
    "swp %0, %0, [%1]\n\t"  // Do the swap
    "cmp %0, #1\n\t"        // Is somebody else looking ?
    "moveq %0, #0\n\t"      // Yes, prepare 'false' result ...
    "beq 1f\n\t"            // ... and return
    "cmp %0, #0\n\t"        // Is the content NULL ?
    "streq %2, [%1]\n\t"    //   Yes, it's NULL, store new contents in
    "moveq %0, #1\n\t"      //   and make return result 'true'
    "strne %0, [%1]\n\t"    //   No, not NULL, put old contents back
    "movne %0, #0\n\t"      //   and prepare 'false' return result 
    "1:\n\t"
    : /* outputs */
      "=r" (result)
    : /* inputs */
      "r" (address),
      "r" (contents)
  );

  return result;

}

inline static int xi_exit_atomic(void * address) {

  int result;
  
  asm volatile (
    "mov %0, #1\n\t"        // Use result register as scratch and load with 'looking' value
    "swp %0, %0, [%1]\n\t"  // Do the swap
    "cmp %0, #1\n\t"        // Is somebody else looking ?
    "mov %0, #0\n\t"        // Clear scratch register, is also 'false' result when somebody else was looking
    "strne %0, [%1]\n\t"    // If nobody was looking, store 0 at address ...
    "movne %0, #1\n\t"      // ... and prepare 'true' result
    : /* outputs */
      "=r" (result)
    : /* inputs */
      "r" (address)
  );

  return result;

}

#endif /* _ATOMIC_H */
