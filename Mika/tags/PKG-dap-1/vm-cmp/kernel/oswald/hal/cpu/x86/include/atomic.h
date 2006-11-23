#ifndef _ATOMIC_H
#define _ATOMIC_H

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
