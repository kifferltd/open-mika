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

#include "oswald.h"
// TODO use a Queue?
#include "wordset.h"
#include "semphr.h"

/*
** Initialise a new monitor.
*/
x_status x_monitor_create(x_monitor monitor) {
  loempa(2, "Creating a monitor at %p\n", monitor);

  memset(monitor, 0, sizeof(x_Monitor));
  monitor->magic = 0xf1e2d3c4;
// N.B. creates the semaphore in the already-owned state
  monitor->mon_mutex = xSemaphoreCreateRecursiveMutex();

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

  if (monitor->magic != 0xf1e2d3c4) {
    loempa(9, "Refuse to delete monitor at %p: already deleted\n", monitor);

    return xs_deleted;
  }

  loempa(2, "Deleting the monitor at %p\n", monitor);
  monitor->magic = 0;
  if (monitor->owner)  {
    o4f_abort(O4F_ABORT_MONITOR, "releasing a monitor while it is in use", monitor);
  }
  vSemaphoreDelete(monitor->mon_mutex);

  loempa(2, "Monitor at %p deleted\n", monitor);

  return xs_success;
}

/*
** Enter a monitor.
*/
x_status x_monitor_enter(x_monitor monitor, x_sleep timeout) {
  x_thread current = x_thread_current();
  int retcode; 

  if (monitor->owner == current) {
    monitor->count += 1;
    loempa(2, "Thread %p already owns monitor %p, count now %d\n", current, monitor, monitor->count);

    return xs_success;
  }

  if (timeout == x_no_wait) {
    retcode = xSemaphoreTakeRecursive(monitor->mon_mutex, 0);
    if (retcode == pdPASS) {
      // The monitor was free and we just acquired it.
      monitor->owner = current;
      monitor->count = 1;
      loempa(2, "Thread %p now owns monitor %p, count now %d\n", current, monitor, monitor->count);

      return xs_success;
    }

    if (retcode == EBUSY) {
      return xs_no_instance;
    }

    o4f_abort(O4F_ABORT_MONITOR, "xSemaphoreTakeRecursive(.., 0)", retcode);

    return -1; // (unreachable)
  }

  if (timeout == x_eternal) {
    current->waiting_on = monitor;
    setFlag(current->flags, TF_COMPETING);
    retcode = xSemaphoreTakeRecursive(monitor->mon_mutex, portMAX_DELAY);
    if (retcode !=0 ) {
      o4f_abort(O4F_ABORT_MONITOR, "xSemaphoreTakeRecursive(..., portMAX_DELAY)", retcode);
    }

    monitor->owner = current;
    monitor->count = 1;
    current->waiting_on = NULL;
    unsetFlag(current->flags, TF_COMPETING);
    loempa(2, "Thread %p now owns monitor %p, count now %d\n", current, monitor, monitor->count);

    return xs_success;
  }

  o4f_abort(ABORT_WONKA, "x_monitor_enter: finite timeout not supported.\n", retcode);

  return -1; // (unreachable)
}

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
// TODO wait on the semaphore (forever)
  }
  else {
// TODO wait on the semaphore (timeout)
  }

  if (retcode == 0 || retcode == EAGAIN || retcode == ETIMEDOUT) {

    monitor->n_waiting--;

    if (monitor->magic != 0xf1e2d3c4) {
      status = xs_deleted;
// TODO return code ???
      xSemaphoreGiveRecursive(monitor->mon_mutex);
    }
// TODO - interrupt handling (should be a Mika level!)
/* WAS :
    else if (isInWordset((w_wordset*)&monitor->interrupted, (w_word)current)) {
      status = xs_interrupted;

      loempa(1, "%p (%t) is in ((x_monitor)%p)->interrupted, removing it and setting status to xs_interrupted\n", current, current->xref, monitor);
      while(removeFromWordset((w_wordset*)&monitor->interrupted, (w_word)current));

      loempa(2, "Thread %p has been interrupted", current);
    }
*/
    else {
      status = xs_success;
    }

    unsetFlag(current->flags, TF_TIMEOUT);
    monitor->owner = current;
    monitor->count = current->waiting_with;

    loempa(2, "Thread %p has re-acquired monitor %p with count %i\n", current, monitor, monitor->count);

    current->waiting_on = NULL;
    current->waiting_with = 0;
    current->state = xt_ready;
  }
  else {
    o4f_abort(O4F_ABORT_MONITOR, "foo", retcode);
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

// TODO signal the monitor

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

// TODO signal the monitor
  
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

    retcode = xSemaphoreGiveRecursive(monitor->mon_mutex);
    if (retcode) {
      o4f_abort(O4F_ABORT_MONITOR, "xSemaphoreGiveRecursive()", retcode);
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
// TODO - deal with interrupts
// WAS :    addToWordset((w_wordset*)&monitor->interrupted, (w_word)thread);

    loempa(1, "%p (%t) is still in ((x_monitor)%p)->interrupted\n", thread, thread->xref, monitor);
// TODO - broadcast a signal on the monitor
    x_monitor_exit(monitor);

    return xs_success;
  }

  return xs_no_instance;
}

