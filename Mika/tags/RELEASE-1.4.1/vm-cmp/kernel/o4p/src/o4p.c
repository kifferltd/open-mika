/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


/*
** $Id: o4p.c,v 1.6 2006/03/27 11:50:50 cvs Exp $
*/    
 
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
*/

void setScheduler(int scheduler) {
  o4pe->scheduler = scheduler;
}

/*
** Initialize the emulation environment.
*/

static void oswaldEnvInit(void) {

  static O4pEnv theEnvironment;
  
  o4pe = &theEnvironment;
  
  o4pe->threads = NULL;
  o4pe->staticMemory = malloc(STATIC_MEMORY_SIZE);
  o4pe->status = O4P_ENV_STATUS_INIT;
  o4pe->timer_ticks = 0;
  if (geteuid()) {
    o4pe->scheduler = DEFAULT_SCHEDULER_USER;
  }
  else {
    o4pe->scheduler = DEFAULT_SCHEDULER_ROOT;
  }
  pthread_mutex_init(&o4pe->timer_lock, NULL);

  pthread_mutex_init(&o4pe->threadsLock, NULL);

}

static pthread_mutex_t Scheduler_Mutex;

void x_scheduler_disable(void) {
  int ret;

  wabort(ABORT_WONKA, "Don't use x_scheduler_disable, it doesn't work!\n");
  ret = pthread_mutex_lock(&Scheduler_Mutex);
  loempa(2, "Thread %p locked scheduler\n", x_thread_current());
  if (ret != 0) {
    loempa(9, "We failed scheduler lock with %d!\n", ret);
  }
}

void x_scheduler_enable(void) {
  int ret;

  ret = pthread_mutex_unlock(&Scheduler_Mutex);
  loempa(2, "Thread %p released scheduler\n", x_thread_current());
  if (ret != 0) {
    loempa(9, "We failed scheduler lock with %d!\n", ret);
  }
}

extern x_size heap_size; 

static void x_setup_kernel(x_size millis) {

  x_thread thread;

  pthread_mutex_init(&Scheduler_Mutex, NULL);

  oswaldEnvInit();

  pthread_key_create(&x_thread_key, NULL);  

  /*
  ** Let the application define some threads. Remember that the oswald kernel doesn't start
  ** scheduling until after the completion of this function. So we set the 
  ** condition.
  */

  x_os_main(command_line_argument_count, command_line_arguments, o4pe->staticMemory);

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

  return xs_success;
}

