/**************************************************************************
* Copyright (c) 2020, 2021, 2023 by KIFFER Ltd. All rights reserved.      *
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

#include "oswald.h"

#include <stdio.h>
#include <unistd.h>
#include <stdarg.h>
#include <sys/time.h>
#include <signal.h>
#include "semphr.h"

typedef void (*cleanpush)(void *);

int command_line_argument_count;
char** command_line_arguments;

extern x_size usecs_per_tick;

O4fEnv *o4fe;

static char init_task_name[10];
static int init_task_seq;

static O4fEnv theEnvironment = { O4F_ENV_STATUS_INIT };
static StaticSemaphore_t threads_mutex_storage;
  
void *start_routine(void *thread_ptr);

/*
** XOR-shift PRNG which generates a 32-bit unsigned int on each call.
** See: https://en.wikipedia.org/wiki/Xorshift
*/

/* The state word must be initialized to non-zero */
// static uint32_t xor32state = 0x3569ac;

x_word xorshift32(x_word state) {
        /* Algorithm "xor" from p. 4 of Marsaglia, "Xorshift RNGs" */
        uint32_t x = xor32state;
        x ^= x << 13;
        x ^= x >> 17;
        x ^= x << 5;
        return xor32state = x;
}

/*
** Initialize the emulation environment.
*/

static void oswaldEnvInit(void) {

  o4fe = &theEnvironment;

// N.B. xSemaphoreCreateRecursiveMutex() creates the mutex in the "free" state
  o4fe->threads_mutex = xSemaphoreCreateRecursiveMutexStatic(&threads_mutex_storage);

}

static SemaphoreHandle_t Scheduler_Mutex;

void x_scheduler_disable(void) {
  vTaskSuspendAll();
}

void x_scheduler_enable(void) {
// TODO check return code
  xTaskResumeAll();
}

extern x_size heap_size; 
extern x_size heap_remaining; 

// TODO decide on a value
#define INITIAL_THREAD_PRIORITY 2

extern x_size max_heap_bytes;

x_status x_oswald_init(x_size max_heap, x_size millis) {
  heap_size = max_heap;
  max_heap_bytes = max_heap;
  oswaldEnvInit();

// Let the application set itself up. Any threads created here will only start to execute when vTaskStartScheduler is called.

  x_os_main(command_line_argument_count, command_line_arguments);

  heap_remaining = heap_size;
  x_mem_init();
  // ignore any millis which are passed in, use portTICK_RATE_MS instead.
  x_setup_timers(portTICK_RATE_MS);

  printf("= = = O S W A L D   S T A R T E D = = =\r\n\r\n");
  printf("heap size = %d millis_per_tick = %d\n", heap_size, portTICK_RATE_MS);

  o4fe->status = O4F_ENV_STATUS_NORMAL;
  vTaskStartScheduler();

  /* just for the compiler, in fact we would never get here */
  return xs_success;
}

void vApplicationStackOverflowHook( TaskHandle_t xTask, char * pcTaskName ) {
  printf("Stack overflow in task %s\n", pcTaskName);
}

