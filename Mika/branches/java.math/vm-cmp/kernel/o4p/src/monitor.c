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

#ifdef LINUX
#include <sys/time.h>
#endif

#include "oswald.h"
#include "wordset.h"

/*
** Initialise a new monitor.
*/
x_status x_monitor_create(x_monitor monitor) {
  loempa(2, "Creating a monitor at %p\n", monitor);

  monitor->owner = NULL;
  monitor->count = 0;
  monitor->magic = 0xf1e2d3c4;
  monitor->interrupted = NULL;

  pthread_mutex_init(&monitor->mon_mutex, NULL);
  pthread_cond_init(&monitor->mon_cond, NULL);

  loempa(2, "Monitor at %p has mutex %p, cond %p\n", monitor, &monitor->mon_mutex, &monitor->mon_cond);

  return xs_success;
}

/*
** Delete a monitor.
*/
x_status x_monitor_delete(x_monitor monitor) {
  x_thread current = x_thread_current();

  if (monitor->owner && monitor->owner != current) {
    loempa(9, "Refuse to delete monitor at %p: owned by %p, caller is %p\n", monitor, monitor->owner, current);

    return xs_not_owner;
  }

  if (monitor->magic != 0xf1e2d3c4U) {
    loempa(9, "Refuse to delete monitor at %p: already deleted\n", monitor);

    return xs_deleted;
  }

  loempa(2, "Deleting the monitor at %p\n", monitor);
  monitor->magic = 0;
  if (monitor->owner)  {
    // Let all the other waiters finish.
    while (monitor->n_waiting != 0) {
      pthread_cond_broadcast(&monitor->mon_cond);
      pthread_cond_wait(&monitor->mon_cond, &monitor->mon_mutex);
    }

    monitor->owner = NULL;
    pthread_mutex_unlock(&monitor->mon_mutex);
  }
  pthread_mutex_destroy(&monitor->mon_mutex);
  pthread_cond_destroy(&monitor->mon_cond);

  loempa(2, "Monitor at %p deleted\n", monitor);

  return xs_success;
}

/*
** Enter a monitor.
*/
x_status x_monitor_enter(x_monitor monitor, x_sleep timeout) {
  x_thread current = x_thread_current();
  int retcode; 

  retcode = pthread_mutex_trylock(&monitor->mon_mutex);
  if (retcode == 0) {
    // The monitor was free and we just acquired it.
    monitor->owner = current;
    monitor->count = 1;
    loempa(2, "Thread %p now owns monitor %p, count now %d\n", current, monitor, monitor->count);

    return xs_success;
  }

  if (retcode == EBUSY) {
    // There are two ways for the monitor to be busy - either we are the
    // owner, or someone else is.
    if (monitor->owner == current) {
      monitor->count += 1;
      loempa(2, "Thread %p already owns monitor %p, count now %d\n", current, monitor, monitor->count);

      return xs_success;
    }

    // OK, it's someone else.
    if (timeout == x_no_wait) {

      loempa(2, "Thread %p tried to obtain monitor %p, mutex was busy\n", current, monitor);
      return xs_no_instance;
    }

    current->waiting_on = monitor;
    setFlag(current->flags, TF_COMPETING);
    if (timeout == x_eternal) {
      retcode = pthread_mutex_lock(&monitor->mon_mutex);
      if (retcode !=0 ) {
        o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_mutex_lock()", retcode);
      }

      current->waiting_on = NULL;
      unsetFlag(current->flags, TF_COMPETING);
      monitor->owner = current;
      monitor->count = 1;
      loempa(2, "Thread %p now owns monitor %p, count now %d\n", current, monitor, monitor->count);

      return xs_success;
    }

    wabort(ABORT_WONKA, "x_monitor_enter: finite timeout not supported.\n", retcode);

  }

  o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_mutex_trylock()", retcode);

  return -1; // (unreachable)
}

#ifndef HAVE_TIMEDWAIT
extern w_boolean join_sleeping_threads(x_thread);
extern void leave_sleeping_threads(x_thread);
#endif

/*
** Wait for a monitor
*/
x_status x_monitor_wait(x_monitor monitor, x_sleep timeout) {
  x_thread current = x_thread_current();
  x_status status = xs_success;
  int retcode; 

  if (monitor->owner != current) {
    loempa(9, "Thread %p is not allowed to wait on monitor %p - owner is %p\n", current, monitor, monitor->owner);

    return xs_not_owner;
  }

  loempa(2, "Setting thread %p as waiter of monitor %p, owner=%p count=%d\n", current, monitor, monitor->owner, monitor->count);

  current->waiting_on = monitor;
  current->waiting_with = monitor->count;
  current->state = xt_waiting;

  monitor->owner = NULL;
  monitor->count = 0;

  monitor->n_waiting++;

  if (timeout == x_eternal) {
    current->sleeping_on_cond = &monitor->mon_cond;
    current->sleeping_on_mutex = &monitor->mon_mutex;
    retcode = pthread_cond_wait(&monitor->mon_cond, &monitor->mon_mutex);
  }
  else {
#ifdef HAVE_TIMEDWAIT
    struct timespec ts;
    x_now_plus_ticks(timeout, &ts);
    current->sleeping_on_cond = &monitor->mon_cond;
    current->sleeping_on_mutex = &monitor->mon_mutex;
    retcode = pthread_cond_timedwait(&monitor->mon_cond, &monitor->mon_mutex, &ts);
#else // pthreads lib has no pthread_cond_timed_wait(), fake it
    loempa(2, "Setting thread %p sleep_ticks to %d\n", current, timeout);
    current->sleep_ticks = timeout;
    current->sleeping_on_cond = &monitor->mon_cond;
    current->sleeping_on_mutex = &monitor->mon_mutex;
    join_sleeping_threads(current);
    retcode = pthread_cond_wait(&monitor->mon_cond, &monitor->mon_mutex);
    leave_sleeping_threads(current);
    current->sleep_ticks = 0;
#endif
  }

  current->sleeping_on_cond = NULL;
  current->sleeping_on_mutex = NULL;
  loempa(2, "Thread %p has returned from pthread_cond_*wait, ret=%d\n", current, retcode);
  if (retcode == 0 || retcode == EAGAIN || retcode == ETIMEDOUT) {

    monitor->n_waiting--;

    if (monitor->magic != 0xf1e2d3c4U) {
      status = xs_deleted;

      pthread_cond_signal(&monitor->mon_cond);
      pthread_mutex_unlock(&monitor->mon_mutex);
    }
    else if (isInWordset((w_wordset*)&monitor->interrupted, (w_word)current)) {
      status = xs_interrupted;

      loempa(1, "%p (%t) is in ((x_monitor)%p)->interrupted, removing it and setting status to xs_interrupted\n", current, current->xref, monitor);
      while(removeFromWordset((w_wordset*)&monitor->interrupted, (w_word)current));

      pthread_mutex_unlock(&monitor->mon_mutex);

      loempa(2, "Thread %p has been interrupted", current);
    }
    else {
      status = xs_success;

      unsetFlag(current->flags, TF_TIMEOUT);
      monitor->owner = current;
      monitor->count = current->waiting_with;

      loempa(2, "Thread %p has re-acquired monitor %p with count %i\n", current, monitor, monitor->count);
    }

    current->waiting_on = NULL;
    current->waiting_with = 0;
    current->state = xt_ready;
  }
  else {
    o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_cond_[timed]wait()", retcode);
  }

  return status;
}

/*
** Notify one thread waiting on a monitor
*/
x_status x_monitor_notify(x_monitor monitor) {
  x_thread current = x_thread_current();
  int retcode;

  if (monitor->owner != current) {
    loempa(2, "Thread %p is not allowed to notify monitor %p - owner is %p\n", current, monitor, monitor->owner);

    return xs_not_owner;
  }
  loempa(2, "Thread %p is notifying another thread\n", current);

  if ((retcode = pthread_cond_signal(&monitor->mon_cond))) {
    o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_cond_signal()", retcode);
  }

  return xs_success;
}

/*
** Notify all threads waiting on a monitor
*/
x_status x_monitor_notify_all(x_monitor monitor) {
  x_thread current = x_thread_current();
  x_status status = xs_success;
  int retcode;

  if (monitor->owner != current) {
    loempa(2, "Thread %p is not allowed to notify monitor %p - owner is %p\n", current, monitor, monitor->owner);

    return xs_not_owner;
  }

  if ((retcode = pthread_cond_broadcast(&monitor->mon_cond))) {
    o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_cond_broadcast()", retcode);
  }
  
  return status;
}

/*
** Leave a monitor
*/
x_status x_monitor_exit(x_monitor monitor) {
  x_thread current = x_thread_current();
  int retcode;

  if (monitor->owner != current) {
    loempa(9, "Thread %p cannot leave monitor %p - owner is %p\n", current, monitor, monitor->owner);
    return xs_not_owner;
  }

  monitor->count -= 1;
  if (monitor->count == 0) {
    monitor->owner = NULL;

    retcode = pthread_mutex_unlock(&monitor->mon_mutex);
    if (retcode) {
      o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_mutex_unlock()", retcode);
      return xs_unknown;
    }
    loempa(2, "Thread %p no longer owns monitor %p, count now 0\n", current, monitor);
  }
  else {
    loempa(2, "Thread %p still owns monitor %p, count now %d\n", current, monitor, monitor->count);
  }

  return xs_success;
}

x_status x_monitor_stop_waiting(x_monitor monitor, x_thread thread) {
  if (thread->waiting_on && thread->waiting_on == monitor) {
    loempa(2, "Thread %p will stop thread %p from waiting on monitor %p\n", x_thread_current(), thread, monitor);

    // take control of the monitor by locking the mutex.
    x_monitor_enter(monitor, x_eternal);

    loempa(1, "adding %p (%t) to ((x_monitor)%p)->interrupted\n", thread, thread->xref, monitor);
    addToWordset((w_wordset*)&monitor->interrupted, (w_word)thread);

    loempa(1, "%p (%t) is still in ((x_monitor)%p)->interrupted\n", thread, thread->xref, monitor);
    pthread_cond_broadcast(&monitor->mon_cond);
    x_monitor_exit(monitor);
    loempa(2, "Done calling pthread_cond_broadcast\n");

    return xs_success;
  }

  return xs_no_instance;
}

