/**************************************************************************
* Copyright (c) 2020 by KIFFER Ltd. All rights reserved.                  *
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

#ifndef _O4F_H
#define _O4F_H

#include "wonka.h"

#include <time.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <string.h>
#include <errno.h>
#include <sched.h>
#include <sys/types.h>
#include <sys/time.h>
#include <sys/resource.h>
#include "FreeRTOS.h"
#include "semphr.h"

#define x_random      random

typedef unsigned short         x_ushort;

/// Amount of memory to reserve for `static' allocations during OS startup (Wonka doesn't need any).
#ifndef STATIC_MEMORY_SIZE
#define STATIC_MEMORY_SIZE 4096
#endif 
extern int num_x_threads;

#define S_RR    SCHED_RR
#define S_FIFO  SCHED_FIFO
#define S_OTHER SCHED_OTHER

#define FAKE_MAX_TASK_PRIORITY

#ifdef FAKE_MAX_TASK_PRIORITY
#define MAX_TASK_PRIORITY 100
#else
#define MAX_TASK_PRIORITY (configMAX_PRIORITIES - 1)
#endif

#define ADD_TICKS(a,b) ((a) == x_eternal || (b) == x_eternal ? x_eternal : (a) + (b))
#define SUBTRACT_TICKS(a,b) ((a) == x_eternal ? x_eternal : (a) - (b))

/*
** The OSWALD default number of microseconds per timer interrupt. 
*/

extern x_size usecs_per_tick;

/*
 * Parameters
 */

#define O4F_OR              0x00
#define O4F_OR_CLEAR        0x01
#define O4F_AND             0x02
#define O4F_AND_CLEAR       0x03

/*  
 * Types
 */
 
typedef struct x_Queue {
  volatile int magic;
  QueueHandle_t handle;
} x_Queue;

typedef struct x_Monitor {
  volatile x_size   magic;
  volatile w_int    count;
  volatile x_thread owner;
  SemaphoreHandle_t owner_mutex;
  QueueHandle_t     waiter_queue;
  QueueHandle_t     interrupted;
} x_Monitor;

typedef struct x_Mutex {
  volatile x_size   magic;
  SemaphoreHandle_t owner_mutex;
} x_Mutex;

#define SR_NO_MESSAGE       1
#define SR_SUSPEND          2
#define SR_RESUME           3
#define SR_INITIALIZED      4

#define MAX_THREAD_NAME_LENGTH 63

typedef struct x_Thread {
  volatile x_state      state;
  TaskHandle_t          handle;
  char                  name[MAX_THREAD_NAME_LENGTH + 1];

//  SemaphoreHandle_t       sleep_timer;            /* a mutex/cond to support thread_sleep */

  w_size                task_priority;    /* Priority this thread is mapped to. */

  void *                task_function;    /* The function the thread will call when it runs */
  void *                task_parameters;        /* The argument to be passed to that function */
  x_thread              o4f_thread_next;        /* Next thread in our linked list */

  volatile x_queue      queueing_on;
  volatile x_monitor    waiting_on;
  volatile w_int        waiting_with;
  volatile int          flags;

  volatile void *       xref;                   /* May be used to point to user thread control block */
  x_report              report;
#ifdef JAVA_PROFILE
  w_long     time_delta;
#endif
} x_Thread;

#ifdef JAVA_PROFILE

#include <sys/time.h>

static inline x_long x_systime_get(void) {
  struct timeval tv;
  gettimeofday(&tv, NULL);
  return tv.tv_sec * 1000000 + tv.tv_usec;
}
#endif

#define O4F_ENV_STATUS_INIT     0
#define O4F_ENV_STATUS_NORMAL   1

typedef struct O4fEnv {
  int status;
  int scheduler;
  x_thread threads;                      /* pointer to first element in linked list of threads */
  SemaphoreHandle_t threads_mutex;           
  SemaphoreHandle_t timer_mutex;           
  volatile w_size timer_ticks; /* the number of ticks passed since we 'booted' */
  FILE *log;
} O4fEnv;

extern O4fEnv *o4fe;

//void x_setup_kernel(void);
  /* Enter kernel. */
  
void setScheduler(int scheduler);

#define currentTicks                 (o4fe->timer_ticks)

extern void x_setup_timers(x_size);

/// Number of command line arguments (copy of argc)
extern int command_line_argument_count;

/// Command line arguments (copy of argv)
extern char **command_line_arguments; 

/** Memory chunk header
    +----------+
    ! reserved ! ) Just to bring the total up to 8 words.
    +----------+  
    !    id    ! User-supplied object type identifier.  Values 0..31 reserved.
    +----------+
    !   next   ! )
    +----------+ ) The linked list used to emulate Oswald's heap-walking stuff
    ! previous ! )
    +----------+
    !   file   ! )
    +----------+ ) Program location where allocated
    !   line   ! )
    +----------+
    !   size   ! Requested size in bytes
    +----------+
    !   check  ! Must point to `magic'
    +----------+
    ! contents !
    :          :
*/

typedef struct o4f_Memory_Chunk *o4f_memory_chunk;

typedef struct o4f_Memory_Chunk {
  w_word            id;
  w_size            size;
  o4f_memory_chunk  next;
  o4f_memory_chunk  previous;
#ifdef DEBUG
  char             *file;
  w_size            line;
  w_word            reserved0;
  char             *check;
#endif
} o4f_Memory_Chunk;

/*
** We only use bits 27..18 of the `id' word: in the `real' oswald this shares
** space with the chunk size and a couple of other flags.
*/
#define GARBAGE_TAG            0x20000000 // Piece of memory is garbage, can be reclaimed by our OWN garbage collector

#define OSWALD_PAGE_SIZE  (4096)

/// The maximum amount of memory we allow ourselves to allocate in a single chunk.
/// (Oswald has such a limit).
#ifndef MAX_SINGLE_ALLOC
#define MAX_SINGLE_ALLOC  8*1024*1024
#endif

x_long x_ticks2usecs(x_size ticks);

/**
 * Add the length of time represented by 'ticks' to the current system time,
 * placing the result in 'ts'.  The number of milliseconds to be added must
 * not exceed the maximum value of an x_long (2**63 - 1), i.e. about 24 days.
 */
extern void x_now_plus_ticks(x_long ticks, struct timespec *ts);
extern x_boolean x_deadline_passed(struct timespec *ts);

void _o4f_abort(char *file, int line, int type, char *message, x_status rc);

#define o4f_abort(t,m,rc) _o4f_abort(__FILE__, __LINE__, t, m, rc);

#define O4F_ABORT_BAD_STATUS      1
#define O4F_ABORT_OVERFLOW        3
#define O4F_ABORT_THREAD          4
#define O4F_ABORT_MONITOR         5

#endif /* _O4F_H */

