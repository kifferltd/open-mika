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
** $Id: cpu.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <oswald.h>

static unsigned char * proceed_address;

void x_thread_switch(x_thread t_current, x_thread t_next) {

  asm volatile (
    "push %%ebp\n\t"
    "push %%edi\n\t"
    "push %%esi\n\t"
    "push %%ebx\n\t"
    "pushfl\n\t"                      /* save EFLAGS                                    */
    "movl %%esp, %0\n\t"              /* save ESP                                       */
    "movl $proceed_pc, %1\n\t"        /* save EIP we have to return to next time we run */
    "movl %2, %%esp\n\t"              /* restore ESP                                 |  */
    "pushl %3\n\t"                    /* restore EIP, ret in callback will pop       |  */
    "jmp x_thread_switching\n\t"      /* call C switch as callback...                |  */
    "proceed_pc:\n\t"                 /* EIP to return to next time prev runs <------+  */
    "popfl\n\t"                       /* restore EFLAGS                                 */
    "call x_host_post\n\t"            /* call x_host_post() */
    "pop %%ebx\n\t"
    "pop %%esi\n\t"
    "pop %%edi\n\t"
    "pop %%ebp\n\t"
    : /* outputs */
      "=m" (t_current->cpu.sp),       /* %0 */
      "=m" (t_current->cpu.pc)        /* %1 */
    : /* inputs */
      "m" (t_next->cpu.sp),           /* %2 */
      "m" (t_next->cpu.pc),           /* %3 */
      "a" (t_current),                /* %4 */
      "d" (t_next)                    /* %5 */
  );

}

/*
** Start the initial thread
*/

void x_init_start(void * i) {

  x_thread init = i;

  asm volatile(
    "movl %0, %%esp\n\t"              /* setup %esp to that of init thread                     */
    "pushl %1\n\t"                    /* push the %eip on the stack, ret in callback will pop  */
    "jmp x_thread_switching\n"        /* call C switch as callback...                          */
    : /* outputs */
    : /* inputs */
      "m" (init->cpu.sp),
      "m" (init->cpu.pc)
  );

}

/*
** Setup the stack such that it can 'survive' a normal task switch and starts
** running the x_thread_start function, on it's own stack. We do it in C since
** we have to read and write structure fields and keeping struct offsets synchronised
** in assembly with C header files is a pain.
*/

void x_stack_init(x_thread thread) {

  x_word *stack = (x_word *)thread->e_stack - 7;   /* reserve room on stacki; thread->e_stack is in %eax */
 
  *stack = 0;                                      /* initial eflags */
  *(stack + 1) = 0;                                /* initial ebx */
  *(stack + 2) = 0;                                /* initial esi */
  *(stack + 3) = 0;                                /* initial edi */
  *(stack + 4) = 0;                                /* initial ebp */
  *(stack + 5) = (x_word)x_thread_start;           /* get starting function in %edx */         
  *(stack + 7) = (x_word)thread;                   /* get thread in %edx */ 
                                                   /* x_thread argument, after this %edx is scratch register */
  
  thread->cpu.sp = (x_word)stack;                  /* save as stack pointer in thread->cpu.sp */
                                                   /* prepare %edx for setting up cpu.pc, the label refers to x_thread_switch */
  thread->cpu.pc = (x_word)proceed_address;        /* set up thread->cpu.pc so that we pop off this fake stack */

  /*
  ** Set the status to begin with. The begin status is 'non critical'.
  */
    
  thread->cpu.status = 0;

}

x_ubyte * x_cpu_setup(x_ubyte *memory) {

  asm volatile (
    "movl $proceed_pc, %%edx\n\t"        // This label refers to and address in x_thread_switch.
    "movl %%edx, %0\n\t"                 // Save the label address in 'proceed_address' for use in x_stack_init.
    : "=m" (proceed_address) : : "edx"   // outputs, inputs (none) and clobbered registers.
  );

  return memory;
  
}

inline static x_boolean xslurp_exit_atomic(void * addr) {

  x_boolean result = false;

  asm volatile (
    "movl $1, %%eax\n\t"                    // Load the 'looking value' in %eax
    "xchgl %%eax, (%%edx)\n\t"              // Atomically exchange the contents and lock others out
    "cmpl $1, %%eax\n\t"                    // Is anyone else trying to look ?
    "je 1f\n\t"                            // Yes, jump to return false
    "movl $0, (%1)\n\t"                     // No, so we put NULL in
    "movl $1, %0\n\t"                       // Prepare true return value
    "1:\n\t"
    : /* outputs */
      "=m" (result)                         // %0
    : /* inputs */
      "d" (addr)                            // %1 == %edx
    : /* clobbered */
      "eax"
  );

  return result;

}

inline static int xslurp_enter_atomic(void * addr, int contents) {

  int result = 0;

  asm volatile (
    "movl $1, %%eax\n\t"                    // Load the 'looking value' in %eax
    "xchgl %%eax, (%%edx)\n\t"              // Atomically exchange the contents and lock others out
    "cmpl $1, %%eax\n\t"                    // Is anyone else trying to look ?
    "je 1f\n\t"                             // Yes, jump to return false
    "cmpl $0, %%eax\n\t"                    // No one was looking, is the content 0 ?
    "jne 2f\n\t"                            // No, jump to replace old contents
    "movl %2, (%1)\n\t"                     // Yes, so we put our new contents in
    "movl $1, %0\n\t"                       // Prepare true return value
    "jmp 1f\n\t"                            // Return
    "2:\n\t"
    "movl %%eax, (%%edx)\n\t" 
    "1:\n\t"
    : /* outputs */
      "=m" (result)                         // %0
    : /* inputs */
      "d" (addr),                           // %1 == %edx
      "c" (contents)                        // %2 
    : /* clobbered */
      "eax"
  );

  return result;

}

#define R_NONE 0
#define R_32   1
#define R_PC32 2

x_int x_cpu_relocate(x_int type, x_address S, x_address A, x_address P) {

  x_int status = 0;
  
  switch (type) {
    case R_NONE:
      break;
        
    case  R_PC32:
      *(x_address *)P = S + A - P;
//      loempa(9, "PC32 : Patched value 0x%08x into location 0x%08x\n", S + A - P, P);
      break;
        
    case R_32:
      *(x_address *)P = S + A;
//      loempa(9, "  32 : Patched value 0x%08x into location 0x%08x\n", S + A, P);
      break;
        
    default: 
      loempa(9, "Unknown relocation type %d.\n", type);
      status = -1;
  }
  
  return status;
  
}
