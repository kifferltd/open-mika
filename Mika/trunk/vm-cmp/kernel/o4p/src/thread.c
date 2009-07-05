/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2007, 2008 by Chris Gray, /k/ Embedded Java         *
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

#include "oswald.h"

int num_x_threads = 0;
pthread_key_t x_thread_key;

static int num_started = 0;
static int num_deleted = 0;

/*
** The different thread states written as character strings and the function to
** get them in an appropriate way.
*/

static const char * _state2char[] = {
  "Ready",                        /*  0  corresponds to 'xe_unused' event type.                                                            */
  "Mutex",                        /*  1  synchronised with event type numbers.                                                             */
  "Queue",                        /*  2  synchronised with event type numbers.                                                             */
  "Mailbox" ,                     /*  3  synchronised with event type numbers.                                                             */
  "Semaphore",                    /*  4  synchronised with event type numbers.                                                             */
  "Signals",                      /*  5  synchronised with event type numbers.                                                             */
  "Monitor",                      /*  6  synchronised with event type numbers.                                                             */
  "Block",                        /*  7  synchronised with event type numbers.                                                             */
  "Map",                          /*  8  synchronised with event type numbers.                                                             */ 
  "Joining",                      /*  9  special event state that signals that a thread is waiting for a join.                             */
  "Waiting",                      /* 10                                                                                                    */
  "Suspended",                    /* 11                                                                                                    */
  "Sleeping",                     /* 12                                                                                                    */
  "Rescheduled",                  /* 13                                                                                                    */
  "Ended",                        /* 14                                                                                                    */
  "(?)",                          /* 15                                                                                                    */
  "Newborn",                      /* 16                                                                                                    */
  "Terminated",                   /* 17                                                                                                    */
  NULL,                           /* 99                                                                                                    */
};

const char * x_state2char(x_thread thread) {
  return thread->state ? _state2char[(thread->state > LAST_XT_STATE) ? xt_unknown : thread->state] : (thread->flags & TF_COMPETING) ? "Competing" : (thread->flags & TF_RECEIVING) ? "Receiving" : (thread->flags & TF_SENDING) ? "Sending" : "Ready";
}

/*
** Map an Oswald priority to the correct priority for a certain
** scheduler. Returns an int value that can be used as a
** sched_param value.
*/

int mapPriority(int policy, unsigned int requested) {

  int max;
  int min;
  int result;

#ifdef DEBUG
  switch (policy) {
    case SCHED_OTHER: loempa(1, "SCHED_OTHER: requested prio = %2d \n", requested); break;
    case SCHED_FIFO:  loempa(1, "SCHED_FIFO: requested prio = %2d \n", requested); break;
    case SCHED_RR:    loempa(1, "SCHED_RR: requested prio = %2d \n", requested); break;
  }
#endif

#if defined(_POSIX_THREAD_PRIORITY_SCHEDULING)
  max = sched_get_priority_max(policy);
  min = sched_get_priority_min(policy);
  if (min < 0 && max < 0) {
    result = min + ((min - max) * ((NUM_PRIORITIES - 1)-requested) / NUM_PRIORITIES);
  }
  else {
    result = min + ((max - min) * requested / NUM_PRIORITIES);
  }
  loempa(1, "requested priority %d, min is %d, max is %d, result is %d\n", requested, min, max, result);

#else
  max = 0;
  min = 0;
  result = 0;
#endif

#ifdef DEBUG
  loempa(1, "max prio = %d min prio = %d result prio = %d\n", max, min, result);
#endif

  return result;

}

/*
 * Prototype:
 *   x_thread x_thread_current(void);
 * Description:     
 *   Returns a pointer to the currently executing thread running
 *   thread.  If no thread is executing, this service returns a null
 *   pointer.
 */

x_thread x_thread_current() {

  return pthread_getspecific(x_thread_key);

}

/*
** Prototype:
**   void x_thread_yield(void)
** Description:
**   Yield the processor.
*/

void x_thread_yield() {

  sched_yield();

}

/*
** Link a thread in our linked list.
*/

void threadRegister(x_thread xnew) {

  int res;
/*
** volatile??? Yeah, well gcc is weird sometimes.
*/
  volatile x_thread current;

  current = NULL;
  res = pthread_mutex_lock(&o4pe->threadsLock);
  if (res != 0) {
    o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_mutex_lock()", res);
  }

  xnew->o4p_thread_next = NULL;

  if (o4pe->threads == NULL) {
    o4pe->threads = xnew;
    current = xnew;
  }
  else {
    for (current = o4pe->threads; current != NULL; current = current->o4p_thread_next) {
      if (current->o4p_thread_next == NULL) {
        current->o4p_thread_next = xnew;
        break;
      }
    }
  }

  if (current == NULL) {
    wabort(ABORT_WONKA, "oops\n");
  }

  num_x_threads += 1;

  pthread_mutex_unlock(&o4pe->threadsLock);

}

/*
** And the reverse, get rid of a thread in our list.
*/

void threadUnregister(x_thread thread) {

  int res;
  x_thread current;
  x_thread previous;

  res = pthread_mutex_lock(&o4pe->threadsLock);
  if (res != 0) {
    o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_mutex_lock()", res);
  }

  previous = NULL;
  for (current = o4pe->threads; current != NULL; current = current->o4p_thread_next) {
    if (current == thread) {
      break;
    }
    previous = current;
  }

  if (current == NULL) {
    /*
    ** This shouldn't happen.
    */
    wabort(ABORT_WONKA, "Thread %p not found !\n", thread);
  }
  else if (previous != NULL) {
    /*
    ** Somewhere in the middle or at the end.
    */
    previous->o4p_thread_next = current->o4p_thread_next;
  }
  else {
    /*
    ** This is the first element in the list.
    */
    o4pe->threads = current->o4p_thread_next;
  }

  num_x_threads -= 1;

  num_deleted += 1;

  pthread_mutex_unlock(&o4pe->threadsLock);

//  loempa(9, "Thread unregistered, %d deleted %d started...\n", num_deleted, num_started);
}

#ifndef HAVE_TIMEDWAIT
extern w_boolean join_sleeping_threads(x_thread);
extern void leave_sleeping_threads(x_thread);
#endif

void *start_routine(void *thread_ptr) {
  x_thread thread;

  num_started += 1;
  thread = (x_thread )thread_ptr;
  if (thread->xref) {
    loempa(7,"Mika thread %t starting\n", thread->xref);
  }
  else {
    loempa(7,"Native thread %p starting\n", thread);
  }
  pthread_setspecific(x_thread_key, thread);

  thread->state = xt_ready;

  if (thread->xref) {
    loempa(7,"Mika thread %t started\n", thread->xref);
  }
  else {
    loempa(7,"Native thread %p started\n", thread);
  }
  (*(x_entry)thread->o4p_thread_function)(thread->o4p_thread_argument);
  if (thread->xref) {
    loempa(7,"Mika thread %t returned normally\n", thread->xref);
  }
  else {
    loempa(7,"Native thread %p returned normally\n", thread);
  }

#ifndef HAVE_TIMEDWAIT
  if (thread->sleeping_on_cond) {
    pthread_cond_broadcast(thread->sleeping_on_cond);
  }
#endif
  thread->state = xt_ended;
#ifndef HAVE_TIMEDWAIT
  leave_sleeping_threads(thread);
#endif

  return NULL;
  
}

/*
 * Prototype:
 *   x_status x_thread_create(x_thread thread_ptr,
 *                        void (*entry_function) (void*), void* entry_input, 
 *                        void *stack_start, w_size stack_size, w_size priority, 
 *                        w_word flags);
 * Description:
 *   This service creates an application thread that starts execution
 *   at the specified task entry function.  The stack, priority, 
 *   preemption, and time-slice are among the attributes specified by
 *   the input parameters.
 *   Threads are the most important part of Oswald.  What exactly is
 *   a thread?  A thread is typically defined as a semi-independent
 *   program segment with a dedicated purpose.  The combined 
 *   processing of all threads makes an application.
 *
 *   [CG 20050601] Note that the stack_start parameter is ignored - we let
 *   the underlying OS allocate the stack memory. Therefore this parameter
 *   should be set to NULL.
 */

x_status x_thread_create(x_thread thread, void (*entry_function)(void*), void* entry_input, void *stack_start, w_size stack_size, w_size priority, w_word flags) {

   int status = 0;
   x_status rval = xs_success;

   loempa(2, "x_thread_create\n");
   if (thread == NULL) {
     loempa(9, "Thread is null\n");
     return xs_bad_argument;
   }

   if (entry_function == NULL) {
     loempa(9, "Entry function is null\n");
     return xs_bad_argument;
   }

   if ((priority < 0) || (priority > (NUM_PRIORITIES - 1))) {
     loempa(2, "Prio is %d! EXIT\n", priority);
     return xs_bad_argument;
   }

   if (stack_start) {
     printf("O4P WARNING: stack_start is non-NULL (%p), but x_thread_create ignores stack_start\n", stack_start);
   }
   loempa(2, "x_thread_create: setting up pthread\n");
   pthread_mutex_init(&thread->sleep_timer, NULL);
   pthread_cond_init(&thread->sleep_cond, NULL);
   thread->o4p_thread_function = entry_function;
   pthread_attr_init(&thread->attributes);

   thread->pid = getpid();
   thread->waiting_on = NULL;
   thread->waiting_with = 0;
   thread->flags = 0;

   /*
   ** Set scheduling and priority
   */

#if defined(_POSIX_THREAD_PRIORITY_SCHEDULING)
   pthread_attr_setinheritsched(&thread->attributes, PTHREAD_EXPLICIT_SCHED);
   switch (o4pe->scheduler) {
     case S_RR:
       status = pthread_attr_setschedpolicy(&thread->attributes, SCHED_RR);
       thread->o4p_thread_schedPolicy = SCHED_RR;
       break;

     case S_FIFO:
       status = pthread_attr_setschedpolicy(&thread->attributes, SCHED_FIFO);
       thread->o4p_thread_schedPolicy = SCHED_FIFO;
       break;

     case S_OTHER:
       status = pthread_attr_setschedpolicy(&thread->attributes, SCHED_OTHER);
       thread->o4p_thread_schedPolicy = SCHED_OTHER;
       break;
   }
     
   if (status != 0) {
     /*
     ** Fall back to other
     */
     status = pthread_attr_setschedpolicy(&thread->attributes, SCHED_OTHER);
     thread->o4p_thread_schedPolicy = SCHED_OTHER;
   }

   thread->o4p_thread_priority = priority;
   thread->o4p_thread_sched.sched_priority = mapPriority(thread->o4p_thread_schedPolicy, priority);
#endif

// Try something new for a start, otherwise we are unable to implement join
// We have to join thread explicitly now when we delete them
// This didn't seem to work anyway (zombies all over the place)
//     pthread_attr_setdetachstate(&thread->attributes, PTHREAD_CREATE_DETACHED);
   thread->o4p_thread_argument = entry_input;

   loempa(2, "x_thread_create: setting up pthread stack\n");
   /*
   ** Set the stack
   */
     
#ifdef _POSIX_THREAD_ATTR_STACKADDR
   status = pthread_attr_setstacksize(&thread->attributes, (size_t)stack_size);
   if (status != 0) {
     o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_attr_setstacksize()", status);
     abort();
   }
#endif

   loempa(2, "x_thread_create: registering pthread\n");
   threadRegister(thread);

   /*
   ** We don't actually start the POSIX threads emulating our Oswald 
   ** threads here when we are in x_os_main().
   */

   if (o4pe->status == O4P_ENV_STATUS_INIT) {
     if (flags & TF_SUSPENDED) {
       thread->state = xt_newborn;
       thread->flags = 0;
     }
     else {
       thread->state = xt_ready;
       thread->flags = O4P_AUTO_ACTIVATE;
     }
   }
   else {
     if (flags & TF_SUSPENDED) {
       thread->state = xt_newborn;
       thread->flags = 0;
     }
     else {
       thread->state = xt_ready;
       thread->flags = 1;
       status = pthread_create(&thread->o4p_pthread, &thread->attributes, start_routine, (void *)thread);
       /*
       ** We can only set the scheduling parameters when the thread is created!
       */
#if defined(_POSIX_THREAD_PRIORITY_SCHEDULING)
       pthread_setschedparam(thread->o4p_pthread, thread->o4p_thread_schedPolicy, &thread->o4p_thread_sched);
#endif
       if (status != 0) {
         o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_create()", status);
       }
     }
   }
   rval = xs_success;

   return rval;
}

/*
 * Prototype:
 *   x_status x_thread_attach_current(x_thread thread_ptr);
 * Description:
 *   This service fills in a x_Thread structure corresponding to the
 *   currently executing pthread.
 */

x_status x_thread_attach_current(x_thread thread) {
   loempa(2, "x_thread_attach_current\n");
   if (thread == NULL) {
     loempa(9, "Thread is null\n");
     return xs_bad_argument;
   }

   loempa(2, "x_thread_attach_current: setting up x_thread\n");
   pthread_mutex_init(&thread->sleep_timer, NULL);
   pthread_cond_init(&thread->sleep_cond, NULL);
   thread->o4p_thread_function = NULL;
   // not needed?
   // pthread_attr_init(&thread->attributes);

   thread->pid = getpid();
   thread->waiting_on = NULL;
   thread->waiting_with = 0;
   thread->flags = 0;
   thread->state = xt_ready;
   thread->o4p_thread_argument = NULL;
   threadRegister(thread);

   return xs_success;
}

/*
 * Prototype:
 *   x_status x_thread_detach(x_thread thread_ptr);
 * Description:
 *   This service cleans up in a x_Thread structure corresponding to  a pthread
 */

x_status x_thread_detach(x_thread thread) {
   loempa(2, "x_thread_detach\n");
   if (thread == NULL) {
     loempa(9, "Thread is null\n");
     return xs_bad_argument;
   }

   pthread_mutex_destroy(&thread->sleep_timer);
   pthread_cond_destroy(&thread->sleep_cond);
   threadUnregister(thread);

   return xs_success;
}

/*
 * Prototype:
 *   x_status x_thread_delete(x_thread thread_ptr);
 * Description:
 *   Deletes the specified application thread.  Since the specified
 *   thread must be in a terminated or completed state, this service
 *   cannot be called from a thread attempting to delete itself.
 */

x_status x_thread_delete(x_thread thread) {
  x_status rval = xs_success;
  void *status;

  /*
  ** Check if we are calling ourselves, if yes, 
  ** return an error since this is impossible.
  */

  if (pthread_equal(pthread_self(), thread->o4p_pthread)) {
    rval = xs_bad_state;
  }
  else {
    loempa(2, "Thread %p is being deleted\n", thread);
    threadUnregister(thread);
      
    // make sure the thread is stopped (since threads are no longer detached, we have to do this)
    pthread_join(thread->o4p_pthread, &status);
  }
  pthread_mutex_destroy(&thread->sleep_timer);
  pthread_cond_destroy(&thread->sleep_cond);
  pthread_attr_destroy(&thread->attributes);

  return rval;
}

 
/*
 * Prototype:
 *   w_int x_thread_priority_set(x_thread thread_ptr, 
 *                               w_size new_priority);
 * Description:
 *   Changes the priority of the specified thread.  Valid priorities
 *   range from 0 through (NUM_PRIORITIES - 1), where 0 represents the 
 *   highest priority level. 
 */
 
w_int x_thread_priority_set(x_thread thread, w_size new_priority) {
  w_int old_priority = (int) thread->o4p_thread_priority;

  /* does nothing, pthreads priorities don't work in linux anyway */

  return old_priority;

}

w_size x_thread_priority_get(x_thread thread) {
  return thread->o4p_thread_priority;
}
 
/*
 * Prototype:
 *   x_status x_thread_resume(x_thread thread_ptr);
 * Description:
 *   Resumes a thread that was previously created without an automatic start.
 */
 
x_status x_thread_resume(x_thread thread) {
  x_status retValue = xs_success;

  if (thread->state == xt_newborn) {
    int status;

    loempa(2, "Starting new born thread.\n");
    thread->state = xt_ready;
    status = pthread_create(&thread->o4p_pthread, &thread->attributes, start_routine, (void *)thread);
    if (status == ENOMEM) {
       return xs_no_mem;
    }
    else if (status != 0) {
      return xs_no_instance;
    }
#if defined(_POSIX_THREAD_PRIORITY_SCHEDULING)
    // Don't check return code for this one, if it fails it's not a big deal.
    // (For example it can fail because the thread already terminated).
    pthread_setschedparam(thread->o4p_pthread, thread->o4p_thread_schedPolicy, &thread->o4p_thread_sched);
#endif
  }
  else {
    thread->state = xt_ready;
  }
  
  return retValue;
} 


/* Prototype:
 *   x_status x_thread_sleep(x_sleep timer_ticks);
 * Description:
 *   This operation causes the calling thread to suspend for the
 *   specified number of timer ticks.
 */
 
x_status x_thread_sleep(x_sleep timer_ticks) {
  x_thread thread;
  int retcode;

  thread = x_thread_current();

  if (thread == NULL) {
    /*
    ** We were cancelled before we got the chance of starting to sleep...
    */
    return xs_success;
  }

#ifndef HAVE_TIMEDWAIT
  thread->sleeping_on_cond = &thread->sleep_cond;
  thread->sleeping_on_mutex = &thread->sleep_timer;
#endif
  thread->state = xt_sleeping;

  pthread_mutex_lock(&thread->sleep_timer);
#ifdef HAVE_TIMEDWAIT
  {
    struct timespec ts;
    x_now_plus_ticks(timer_ticks, &ts);
    retcode = pthread_cond_timedwait(&thread->sleep_cond, &thread->sleep_timer, &ts);
  }
#else
  thread->sleep_ticks = timer_ticks;
  join_sleeping_threads(thread);
  retcode = pthread_cond_wait(&thread->sleep_cond, &thread->sleep_timer);
  leave_sleeping_threads(thread);
  thread->sleep_ticks = 0;
#endif
  pthread_mutex_unlock(&thread->sleep_timer);

#ifndef HAVE_TIMEDWAIT
  thread->sleeping_on_cond = NULL;
  thread->sleeping_on_mutex = NULL;
#endif
  unsetFlag(thread->flags, TF_TIMEOUT);

  thread->state = xt_ready;

  return xs_success;
}

/*
 * Prototype:
 *   x_status x_thread_suspend(x_thread thread_ptr);
 * Description:
 *   Suspends the specified application thread.  A thread may call
 *   this service to suspend itself.  Once suspended, the thread must
 *   be resumed by calling 'x_thread_resume' in order to execute
 *   again.
 */

x_status x_thread_suspend(x_thread thread) {  
  x_status retValue = xs_success;

  wabort(ABORT_WONKA, "DONT USE THIS, IS NOT EVEN WORKING A BIT, JUST SIGNALS THREAD TO STOP WHEN HE WANTS TO\n");

  thread->state = xt_suspended;

  return retValue;
}

x_status x_thread_join(x_thread joinee, void **result, x_sleep timeout) {
  x_status status = xs_unknown;
  x_thread joiner = x_thread_current();
  struct timespec one_tick_ts;
#ifdef HAVE_TIMEDWAIT
  struct timeval end;
#endif

  if (!joinee->state) {
    return xs_success;
  }

  one_tick_ts.tv_sec = 0;
  one_tick_ts.tv_nsec = 1000 * x_ticks2usecs(1);
#ifndef HAVE_TIMEDWAIT
  joiner->sleep_ticks = timeout;
#endif
  
  joiner->state = xt_joining;

  if (timeout == x_eternal) {
     while (1) {
       if (joinee->state >= xt_ended) {
         break;
       }

       nanosleep(&one_tick_ts, NULL);
     }
  }
  else {
#ifdef HAVE_TIMEDWAIT
     x_now_plus_ticks(timeout, &end);
#endif

     do {
       if (joinee->state >= xt_ended) {
         break;
       }

       nanosleep(&one_tick_ts, NULL);
#ifdef HAVE_TIMEDWAIT
     } while (!x_deadline_passed(&end));
#else
     } while (--joiner->sleep_ticks >= 0);
#endif
  }

  joiner->state = xt_ready;

#ifndef HAVE_TIMEDWAIT
  joiner->sleep_ticks = 0;
#endif

  if (isSet(joiner->flags, TF_TIMEOUT)) {
    unsetFlag(joiner->flags, TF_TIMEOUT);
    status = xs_no_instance;
  }
  else {
    status = xs_success;
  }

  return status;
}

/*
** Wake up a thread that is sleeping or joining, limited or eternal. If the thread
** is not sleeping or joining, nothing happens and we return the xs_no_instance status.
*/

x_status x_thread_wakeup(x_thread thread) {
  x_status result = xs_success;

  if (thread->state == xt_sleeping) {
    pthread_mutex_lock(&thread->sleep_timer);
    pthread_cond_broadcast(&thread->sleep_cond);
    pthread_mutex_unlock(&thread->sleep_timer);
  }
  else {
    result = xs_no_instance;
  }
  
  return result;
}

x_status x_thread_stop_waiting(x_thread thread) {
  x_monitor monitor = thread->waiting_on;

  if (monitor) {
    loempa(2, "Thread %p will stop thread %p from waiting on %p\n", x_thread_current(), thread, monitor);
    return x_monitor_stop_waiting(monitor, thread);
  }

  return xs_no_instance;
}

x_status x_thread_signal(x_thread thread, w_int signum) {
  pthread_t pt = thread->o4p_pthread;
  int rc;

  rc = pthread_kill(pt, signum);
  switch (rc) {
    case 0:
      return xs_success;

    case ESRCH:
      return xs_no_instance;

    default:
     return xs_bad_argument;
  }
}

