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
** $Id: cpu.c,v 1.1 2004/07/18 17:12:48 cvs Exp $
*/

#include <stddef.h>

#include <cpu.h>
#include <oswald.h>

x_ubyte * x_cpu_setup(x_ubyte *memory) {

  return memory;

}

/*
** Start the initial function. We patch up the stack frame as it has been prepared by x_stack_init.
** This means we change the start function from x_thread_start to x_init_entry and set the argument
** to the current memory pointer. Then we start of to x_init_entry...
*/

void x_init_start(void * t) {

  x_thread init = t;
  
  init->cpu.status = 0;
  
  asm volatile (
    "mov r1, #0xd3\n\t"                  /* Create cpsr value, SVC mode, interrupts disabled.     */
    "msr cpsr, r1\n\t"                   /* and make it the current mode...                       */
    "ldr sp, %0\n\t"                     /* Load stack pointer of structure in sp register.       */
    "ldr r1, 1f\n\t"                     /* Load x_init_entry as start function.                  */
    "str r1, [sp, #64]\n\t"              /* Store it as the pc to start at when we exit here ---+ */
    "ldr r1, %1\n\t"                     /* Load start argument (memory) set in wax.c           | */
    "str r1, [sp, #8]\n\t"               /* Set start argument in stack.                        | */
    "ldmfd sp!, {r0, r1}\n\t"            /* Pop of frame type and initial cpsr.                 | */
    "msr spsr, r1\n\t"                   /* Set cpsr to popped value.                           | */
    "ldmfd sp!, {r0 - r12, lr, pc}^\n\t" /* here we start the x_init_entry <--------------------+ */
    "1: .word x_init_entry\n\t"          /* The special start function for the init thread.       */
    : /* outputs */
    : /* inputs */
      "m" (((x_thread)t)->cpu.sp),       /* The stack pointer saved in the thread structure.      */
      "m" (((x_thread)t)->argument)      /* The initial argument, set in wax.c                    */
    : /* clobbered */
      "r1"
  );
  
}

void x_stack_init(x_thread thread) {

  thread->cpu.status = 0;

  asm volatile (
    "ldr r1, %1\n\t"                     /* load r1 with stack pointer of thread structure.       */
    "bic r1, r1, #3\n\t"                 /* Clear lower 2 bits to word align.                     */
    "sub r1, r1, #(18 * 4)\n\t"          /* Create space on the stack for 18 words.               */
    "str r1, %0\n\t"                     /* Store current stack pointer in thread structure.      */
    "ldr r2, 1f\n\t"                     /* Pickup pointer to thread start function.              */
    "str r2, [r1, #64]\n\t"              /* Set initial pc to thread start function.              */
    "mov r2, #0\n\t"                     /* Build initial register value.                         */
    "str r2, [r1, #68]\n\t"              /* For backtracing (not yet)                             */
    "str r2, [r1, #60]\n\t"              /* lr                                                    */
    "str r2, [r1, #56]\n\t"              /* r12                                                   */
    "str r2, [r1, #52]\n\t"              /* r11                                                   */
    "str r2, [r1, #48]\n\t"              /* r10                                                   */
    "str r2, [r1, #44]\n\t"              /* r9                                                    */
    "str r2, [r1, #40]\n\t"              /* r8                                                    */
    "str r2, [r1, #36]\n\t"              /* r7                                                    */
    "str r2, [r1, #32]\n\t"              /* r6                                                    */
    "str r2, [r1, #28]\n\t"              /* r5                                                    */
    "str r2, [r1, #24]\n\t"              /* r4                                                    */
    "str r2, [r1, #20]\n\t"              /* r3                                                    */
    "str r2, [r1, #16]\n\t"              /* r2                                                    */
    "str r2, [r1, #12]\n\t"              /* r1                                                    */
    "str r0, [r1, #8]\n\t"               /* r0 = thread argument to x_start_thread.               */
    "mov r2, #0x13\n\t"                  /* Build initial cpsr (SVC mode, interrupts enabled).    */
    "str r2, [r1, #04]\n\t"              /* Store cpsr.                                           */
    "mov r2, #1\n\t"                     /* Build frame type (interrupted frame).                 */
    "str r2, [r1]\n\t"                   /* Save frame type.                                      */
    "mov pc, lr\n\t"                     /* We return here !!                                     */
    "1: .word x_thread_start\n\t"
    : /* outputs */
      "=m" (thread->cpu.sp)              /* %0                                                    */
    : /* inputs */
      "m" (thread->e_stack)              /* %1                                                    */
    : /* clobbered */
      "r1", "r2"
  );


}

void x_thread_switch(x_thread t_current, x_thread t_next) {

  asm volatile (
    "mov r2, #0\n\t"
    "mrs r3, cpsr\n\t"
    "stmfd sp!, {r2, r3, r4 - r11, lr}\n\t"
    "mov r2, #0xd3\n\t"
    "msr cpsr, r2\n\t"
    "str sp, [%2, %0]\n\t"
    "ldr sp, [%1, %0]\n\t"
    "bl x_thread_switching\n\t"
    "bl x_host_post\n\t"
    "ldmfd sp!, {r2, r3}\n\t"
    "msr spsr, r3\n\t"
    "cmp r2, #0\n\t"
    "ldmeqfd sp!, {r4 - r11, pc}^\n\t"
    "ldmfd sp!, {r0 - r12, lr, pc}^\n\t"
    : /* outputs */
    : /* inputs */
      "r" (offsetof(x_Thread, cpu.sp)),
      "r" (t_next),
      "r" (t_current)
    : /* clobbered */
      "r2", "r3"
  );

  /*
  ** Anything that's put here will never be executed...
  */
  
}

/*
** Jumpers are springboards for BL statements to symbols that are beyond the +-32 Mb range that
** can be encoded in the 24 bits remaining in a BL instruction. When this can not be done, we
** introduce a jumper; the code that needs relocation is doing a BL to this jumper and the code
** at the jumper looks like:
**
** 0x.......0 ldr pc, [pc]                ; Note that PC is pointing allready 8 bytes further or 
** 0x.......4 <pointer to next jumper>    ; Keep this pointer here for the 8 bytes offset...   |
** 0x.......8 <the address of the symbol> ; <----------- it's pointing to this word -----------+
**
** So the jumper is the springboard to the real address that needs to be executed. We don't need
** to fumble around with the link register R14, since that contains the full 32 bit address we
** should return to after the call. I wonder what it will do on our cache though... rather, I don't
** wanna know...
** 
** This happens quite a few times in the module code since the executable code is in the heap range
** and on some ARM systems, symbols of the executable code and the heap code are more than 900Mb out
** of each others range! Go figure...
*/

typedef struct x_Jumper * x_jumper;

#define LDR_PC 0xe51ff000    // ldr pc, [pc] ; jump to the address encoded 8 bytes further

typedef struct x_Jumper {
  x_uword ldr_pc;
  x_jumper next;
  x_address address;
} x_Jumper;

static x_int num_jumpers = 0;
static x_jumper jumpers = NULL;

#define R_ARM_PC24           1
#define R_ARM_ABS32          2

#define R_ARM_PC24_MASK      0x00ffffff
#define R_ARM_PC24_SIGN      0x00800000

#define PLUS_32_MEG          (x_int)0x02000000
#define MINUS_32_MEG         (x_int)0xfe000000

/*
** Calculate the signed offset for a R_ARM_PC24 relocation, NOT taking into account that is should
** fit as words in a 24 bit space. So we calculate the offset in bytes and just return it. The caller
** should decide what to do with it...
*/

static x_int x_calculate_offset(x_address S, x_address A, x_address P) {

  x_address a;
  
  /*
  ** Extract the addend from the passed addend argument. For an R_ARM_PC24 relocation, bits 0 - 23
  ** encode a signed offset in words (ARM ELF Documentation, page 32 of 44, Figure 4-4). 
  ** We first extract and then sign extend if required, after which we convert from words to bytes.
  */
  
  a = (A & R_ARM_PC24_MASK);
  if (a & R_ARM_PC24_SIGN) {
    a |= ~ R_ARM_PC24_MASK;
  }
  a <<= 2;

  /*
  ** We calculate the signed offset in bytes required for this relocation and return it.
  */

  return (S - P + a);
  
}

x_int x_cpu_relocate(x_int type, x_address S, x_address A, x_address P) {

  x_int status = 0;
  x_int offset;
  x_jumper j;
  
//  loempa(9, "Relocation type %d, S = 0x%08x, A = 0x%08x, P = 0x%08x *P = 0x%08x.\n", type, S, A, P, *(x_address *)P);

  switch (type) {
    case R_ARM_PC24 : {

      /*
      ** See what the offset would be. If it is bigger than 32 meg, positive or negative, we have
      ** to use a jumper. When this is the case, we look for a jumper that maybe exists allready with
      ** the correct symbol to jump to, if not, we create such a jumper.
      */
    
      offset = x_calculate_offset(S, A, P);
      if ((offset > PLUS_32_MEG) || (offset < MINUS_32_MEG)) {
        loempa(9, "We need a jumper to reach symbol 0x%08x, offset is %c%d Mb.\n", S, ((offset > 0) ? '+' : '-'), ((offset > 0) ? offset : -offset) / (1024 * 1024));
        for (j = jumpers; j; j = j->next) {
          if (j->address == S) {
            break;
          }
        }

        if (j == NULL) {
          j = x_mem_alloc(sizeof(x_Jumper));
          j->ldr_pc = LDR_PC;
          j->next = jumpers;
          j->address = S;
          jumpers = j;
          num_jumpers += 1;
          loempa(9, "Created new jumper for symbol 0x%08x, %d jumpers in use.\n", S, num_jumpers);
        }
        else {
          loempa(9, "Reused jumper 0x%08x, %d jumpers in use.\n", j, num_jumpers);
        }
        
        offset = x_calculate_offset((x_address) j, A, P);
        loempa(9, "New offset is now %c%d bytes.\n", ((offset > 0) ? '+' : '-'), ((offset > 0) ? offset : -offset));
      }
      else {
        loempa(9, "Initial offset is OK: %c%d bytes.\n", ((offset > 0) ? '+' : '-'), ((offset > 0) ? offset : -offset));
      }
      
      offset >>= 2;
      offset &= R_ARM_PC24_MASK;
      *(x_address *)P &= ~ R_ARM_PC24_MASK;
      *(x_address *)P |= offset;
      break;
    }

    case R_ARM_ABS32 : {
//      loempa(9, "S = 0x%08x, A = 0x%08x P = 0x%08x *P = 0x%08x.\n", S, A, P, *(x_address *)P);
      *(x_address *)P = S + A;
      break;
    }
    
    default : {
      loempa(9, "Unknown type %d.\n", type);
      status = -1;
    }
  }

  return status;

}
