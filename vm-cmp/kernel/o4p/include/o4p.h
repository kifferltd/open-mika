/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved. Parts copyright (c) 2004, 2005, 2009 by /k/ Embedded Java     *
* Solutions. All rights reserved.                                         *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

#ifndef _O4P_H
#define _O4P_H

#include "wonka.h"

 /*************************************************************************
 * Filename:                                                              *
 *   o4p.h                                                                *
 *                                                                        *
 * Description:                                                           *
 *                                                                        *
 *   "Oswald for POSIX" compatibility layer.                              *
 *                                                                        *
 *   We only implement the Oswald calls which are actually used by Wonka  *
 *   (the full Oswald API is much bigger than this).  If you don't see    *
 *   the Oswald system call you wanted here, feel free to implement it.   *
 *                                                                        *
 * Authors:                                                               *
 *   Dries Buytaert <dries.buytaert@acunia.com>                           *
 *   Chris Gray <chris.gray@kiffer.be>                                    *
 *                                                                        *
 *************************************************************************/

#include <pthread.h>
#include <time.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <string.h>
#include <errno.h>
#include <sched.h>
#include <dlfcn.h>
#include <sys/types.h>
#include <sys/time.h>
#include <sys/resource.h>

/*
** OS Specific stuff...
*/

#if defined (LINUX)

#define DEFAULT_SCHEDULER_ROOT      SCHED_OTHER
#define DEFAULT_SCHEDULER_USER      SCHED_OTHER

/*
** Linux doesn't have the following calls.
*/

//#define pthread_mutexattr_settype(mutex, type)   /* nothing */
#define pthread_attr_setstackaddr(ap, start)     0
#define pthread_attr_setstacksize(ap, size)      0

#elif defined (WINNT)

#define DEFAULT_SCHEDULER_ROOT      SCHED_OTHER
#define DEFAULT_SCHEDULER_USER      SCHED_OTHER

#define pthread_mutexattr_settype(mutex, type)   /* nothing */
#define pthread_attr_setstackaddr(ap, start)     0
#define pthread_attr_setstacksize(ap, size)      0

#elif defined (NETBSD)

#define DEFAULT_SCHEDULER_ROOT      SCHED_RR
#define DEFAULT_SCHEDULER_USER      SCHED_RR

#define pthread_mutexattr_settype(mutex, type)   /* nothing */
#define pthread_attr_setstackaddr(ap, start)     0
#define pthread_attr_setstacksize(ap, size)      0

#else

#error Unknown OS

#endif

/*
** End of OS specific stuff.
*/

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

/*
** The OSWALD default number of microseconds per timer interrupt. We
** keep it in O4P so that we can keep the same API.
*/

extern x_size usecs_per_tick;

/*
 * Parameters
 */

#define O4P_OR              0x00
#define O4P_OR_CLEAR        0x01
#define O4P_AND             0x02
#define O4P_AND_CLEAR       0x03

/*  
 * Types
 */
 
typedef struct x_Queue {
  pthread_mutex_t queue_mutex;       
  pthread_cond_t queue_not_empty;
  pthread_cond_t queue_not_full; 
  volatile int magic;

  volatile int available;
  volatile int capacity;

  w_word *messages;
  volatile w_word *write;
  volatile w_word *read;
  w_word *limit;
} x_Queue;

typedef struct x_Sem {
  pthread_mutex_t     sem_mutex;      
  pthread_cond_t      sem_cond;       /* to broadcast the freeness of the semaphore */

  volatile w_size      count;
  volatile int         waiters;
  volatile int deleted;
  volatile x_thread    owner;  /* The thread that holds the semaphore */
} x_Sem;

typedef struct x_Mutex {
  pthread_mutex_t    mtx_mutex;
  volatile x_thread  owner;
} x_Mutex;

typedef struct x_Monitor {
  pthread_mutex_t   mon_mutex;
  pthread_cond_t    mon_cond;
  volatile w_int    count;
  volatile int      magic;
  volatile x_thread owner;
  volatile void    *interrupted;
  volatile int      n_waiting;
} x_Monitor;

#define O4P_AUTO_ACTIVATE    0x01

#define SR_NO_MESSAGE       1
#define SR_SUSPEND          2
#define SR_RESUME           3
#define SR_INITIALIZED      4

typedef struct x_Thread {
  volatile x_state      state;
  pid_t                 pid;

  pthread_mutex_t       sleep_timer;            /* a mutex/cond to support thread_sleep */
  pthread_cond_t        sleep_cond;

  pthread_attr_t        attributes;             /* the attributes */

  pthread_t             o4p_pthread;            /* POSIX.4 thread. */
  w_size                o4p_thread_priority;    /* Priority this thread is mapped to. */

#if defined(_POSIX_THREAD_PRIORITY_SCHEDULING)

  struct sched_param    o4p_thread_sched;       /* Scheduling policy, incl.  priority level. */
  w_int                 o4p_thread_tid;         /* Thread-specific PID (linux only) */
  int                   o4p_thread_schedPolicy; /* SCHED_FIFO or SCHED_RR */
#endif

  void *                o4p_thread_function;    /* The function the thread will call when it runs */
  void *                o4p_thread_argument;    /* The argument to be passed to that function */
  x_thread              o4p_thread_next;        /* Next thread in our linked list */

  volatile x_queue      queueing_on;
  volatile x_monitor    waiting_on;
  volatile w_int        waiting_with;
#ifndef HAVE_TIMEDWAIT
  volatile w_int        sleep_ticks;           /* Number of ticks for which to sleep */
  pthread_cond_t       *sleeping_on_cond;  /* Condition variable on which to broadcast when timer expires */
  pthread_mutex_t      *sleeping_on_mutex; /* Mutex which should be owned when doing so */
#endif
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

#define O4P_ENV_STATUS_INIT     0
#define O4P_ENV_STATUS_NORMAL   1

typedef struct O4pEnv {
  int scheduler;
  int status;
  void *staticMemory;                    /* memory used to fake Oswald's static allocation thang */
  x_thread threads;                      /* pointer to first element in linked list of threads */
  pthread_mutex_t threadsLock;           
  pthread_mutex_t timer_lock;           
  volatile w_size timer_ticks; /* the number of ticks passed since we 'booted' */
  FILE *log;
} O4pEnv;

extern O4pEnv *o4pe;

typedef void(*x_entry)(void* arg);

//void x_setup_kernel(void);
  /* Enter kernel. */
  
void setScheduler(int scheduler);

#define currentTicks                 (o4pe->timer_ticks)

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

typedef struct o4p_Memory_Chunk *o4p_memory_chunk;

typedef struct o4p_Memory_Chunk {
  w_word            id;
  w_size            size;
  o4p_memory_chunk  next;
  o4p_memory_chunk  previous;
#ifdef DEBUG
  char             *file;
  w_size            line;
  w_word            reserved0;
  char             *check;
#endif
} o4p_Memory_Chunk;

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

x_size x_ticks2usecs(x_size ticks);

/**
 * Add the length of time represented by 'ticks' to the current system time,
 * placing the result in 'ts'.  The number of milliseconds to be added must
 * not exceed the maximum value of an x_long (2**63 - 1), i.e. about 24 days.
 */
extern void x_now_plus_ticks(x_long ticks, struct timespec *ts);
extern x_boolean x_deadline_passed(struct timespec *ts);

void _o4p_abort(char *file, int line, int type, char *message, int rc);

#define o4p_abort(t,m,rc) _o4p_abort(__FILE__, __LINE__, t, m, rc);

#define O4P_ABORT_BAD_STATUS      1
#define O4P_ABORT_PTHREAD_RETCODE 2
#define O4P_ABORT_OVERFLOW        3

#endif /* _O4P_H */

