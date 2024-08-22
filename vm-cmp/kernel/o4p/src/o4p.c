/**************************************************************************
* Copyright (c) 2009, 2023, 2024 by Chris Gray, KIFFER Ltd.               *
* All rights reserved.                                                    *
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
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS *
* OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)   *
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,     *
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING   *
* IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE      *
* POSSIBILITY OF SUCH DAMAGE.                                             *
**************************************************************************/

#include "oswald.h"

#include <stdio.h>
#include <unistd.h>
#include <stdarg.h>
#include <sys/time.h>
#include <signal.h>

typedef void (*cleanpush)(void *);

int command_line_argument_count;
char** command_line_arguments;

extern x_size usecs_per_tick;

O4pEnv *o4pe;

extern pthread_key_t x_thread_key;
void *start_routine(void *thread_ptr);

/*
** This function is called once for setting up the Oswald emulation environment.
** It is called from main, before going to x_os_main. It prepares the
** linked list of timers and threads. If not called explicitly, the default
** policy is used (which is the best for the OS), so only call this when you
** know what you are doing.

void setScheduler(int scheduler) {
  o4pe->scheduler = scheduler;
}
*/

/*
** Initialize the emulation environment.
*/

static void oswaldEnvInit(void) {

  static O4pEnv theEnvironment;
  
  o4pe = &theEnvironment;
  
  o4pe->threads = NULL;
  o4pe->status = O4P_ENV_STATUS_INIT;
  o4pe->timer_ticks = 0;
  if (geteuid()) {
    o4pe->scheduler = DEFAULT_SCHEDULER_USER;
  }
  else {
    o4pe->scheduler = DEFAULT_SCHEDULER_ROOT;
  }

  pthread_mutexattr_init(&o4pe->mutexattr);
  pthread_mutexattr_settype(&o4pe->mutexattr, PTHREAD_MUTEX_RECURSIVE);
  pthread_mutex_init(&o4pe->timer_lock, &o4pe->mutexattr);
  pthread_mutex_init(&o4pe->threadsLock, &o4pe->mutexattr);
}

void x_scheduler_disable(void) {
  int ret;

  wabort(ABORT_WONKA, "Don't use x_scheduler_disable, it doesn't work!\n");
}

void x_scheduler_enable(void) {
}

extern x_size heap_size; 
extern x_size heap_remaining; 

static void x_setup_kernel(x_size millis) {

  x_thread thread;

  oswaldEnvInit();

  pthread_key_create(&x_thread_key, NULL);  

  /*
  ** Let the application define some threads. Remember that the oswald kernel doesn't start
  ** scheduling until after the completion of this function. So we set the 
  ** condition.
  */

  x_os_main(command_line_argument_count, command_line_arguments);

  heap_remaining = heap_size;
  x_mem_init();
  x_setup_timers(millis);

  /*
  ** We now set the status to normal and start all threads in the list that have been
  ** set to O4P_AUTO_START.
  */

  o4pe->status = O4P_ENV_STATUS_NORMAL;

  /*
  ** We don't use a mutex, we are sure that we are the only thread running
  ** at this moment.
  */

  for (thread = o4pe->threads; thread; thread = thread->o4p_thread_next) {
    if (thread->state == xt_ready && (thread->flags & O4P_AUTO_ACTIVATE)) {
      thread->flags &= ~O4P_AUTO_ACTIVATE;
      thread->state = xt_ready;
      pthread_create(&thread->o4p_pthread, &thread->attributes, start_routine, (void *)thread);
    }
  }

#ifdef SIGINT_IS_WABORT
  o4p_sa.sa_handler = handle_SIGINT;
  sigemptyset(&o4p_sa.sa_mask);
  o4p_sa.sa_flags = 0;
  if (sigaction(SIGINT, &o4p_sa, NULL) < 0) {
    wabort(ABORT_WONKA, "Unable to set up signal handler!\n");
  }
#endif
} 

extern x_size max_heap_bytes;

x_status x_oswald_init(x_size max_heap, x_size millis) {
  heap_size = max_heap;
  max_heap_bytes = max_heap;
  x_setup_kernel(millis);

  // x_oswald_init is not expected to return to its caller.
  // TODO can we not do something more useful here?
  while (1) {
    sleep(1);
  }

  return xs_success;
}

