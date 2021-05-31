/**************************************************************************
* Copyright (c) 2020, 2021 by KIFFER Ltd. All rights reserved.            *
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

#define MONITOR_MAX_THREADS 100

/*
** Initialise a new monitor.
*/
x_status x_monitor_create(x_monitor monitor) {
  loempa(2, "Creating a monitor at %p\n", monitor);

  memset(monitor, 0, sizeof(x_Monitor));
  monitor->magic = 0xf1e2d3c4;
// N.B. this creates the semaphore in the locked state
  monitor->waiter_sem = xSemaphoreCreateCounting(MONITOR_MAX_THREADS, 0);
// N.B. this creates the mutex in the free state
// The mutex doesn't need to be recursive, because we do our own counting
  monitor->owner_mutex = xSemaphoreCreateMutex();

  loempa(2, "Monitor at %p has mutex %p\n", monitor, &monitor->owner_mutex);

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
    o4f_abort(O4F_ABORT_MONITOR, "x_monitor_delete: monitor is in use", 0);
  }
  vSemaphoreDelete(monitor->waiter_sem);
  vSemaphoreDelete(monitor->owner_mutex);

  loempa(2, "Monitor at %p deleted\n", monitor);

  return xs_success;
}

/*
** Enter a monitor.
*/
x_status x_monitor_enter(x_monitor monitor, x_sleep timeout) {
  x_thread current = x_thread_current();
  BaseType_t retcode; 

  if (xSemaphoreGetMutexHolder(monitor->owner_mutex) == current->handle) {
    monitor->count += 1;
    loempa(2, "Thread %p already owns monitor %p, count now %d\n", current, monitor, monitor->count);

    return xs_success;
  }

  setFlag(current->flags, TF_COMPETING);
  current->waiting_on = monitor;
  retcode = xSemaphoreTake(monitor->owner_mutex, timeout);
  current->waiting_on = NULL;
  unsetFlag(current->flags, TF_COMPETING);
  switch (retcode) {
    case pdPASS: {
      // The monitor was free and we just acquired it.
      monitor->owner = current;
      monitor->count = 1;
      loempa(2, "Thread %p now owns monitor %p, count now %d\n", current, monitor, monitor->count);

      return xs_success;
    }

    case pdFAIL:
      return xs_no_instance;

    default:
      o4f_abort(O4F_ABORT_MONITOR, "x_monitor_enter: xSemaphoreTake() returned unknown code", retcode);

  }

  return xs_unknown; // (unreachable)
}

/*
** Wait for a monitor
*/
x_status x_monitor_wait(x_monitor monitor, x_sleep timeout) {
  x_thread current = x_thread_current();
  x_sleep expiry_ticks = timeout + xTaskGetTickCount();
  BaseType_t retcode; 

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
  loempa(2, "Thread %p is waiting on monitor %p until tick %d\n", current, monitor, expiry_ticks);

  monitor->n_waiting++;
  loempa(2, "Now there are %d threads waiting on  monitor %p\n", monitor->n_waiting, monitor);

// TODO we need to release owner_mutex and block on waiter_sem,
// BUT if any other thread aquires owner_mutex and then issues a notify,
// the behavour must be as if this thread were already blocked on waiter_sem.
// The Plan Is that the notifying thread will Give monitor->waiter_sem n_waiting times,
// so if this thread did not yet Take the monitor then there will be one Give "in the bank".
  retcode = xSemaphoreGive(monitor->owner_mutex);
  if (retcode != pdPASS) {
      // The only reason to fail is if we are not the owner, bu this should never happen
      o4f_abort(O4F_ABORT_MONITOR, "x_monitor_wait: xSemaphoreGive() failed", retcode);
  }

// normally we will block here, UNLESS another thread snuck in and already called x_monitor_notify
  retcode = xSemaphoreTake(monitor->waiter_sem, timeout);

  switch (retcode) {
    case pdPASS: 
      if (monitor->magic != 0xf1e2d3c4) {
        return xs_deleted;
      }
      // fall through to re-acquire the mutex
      break;

    case pdFAIL:
      return xs_no_instance;

    default:
      o4f_abort(O4F_ABORT_MONITOR, "x_monitor_wait: xSemaphoreTake() returned unknown code", retcode);
  }

  // somebody gave the semaphore, time to re-acquire the mutex
  int remaining = expiry_ticks - xTaskGetTickCount();
  retcode = xSemaphoreTake(monitor->owner_mutex, remaining == (x_sleep) remaining ? (x_sleep) remaining : 0);
  unsetFlag(current->flags, TF_TIMEOUT);

  switch (retcode) {
    case pdPASS: {
      // The monitor was free and we just acquired it.
      monitor->owner = current;
      monitor->count = current->waiting_with;
      loempa(2, "Thread %p has re-acquired monitor %p with count %i\n", current, monitor, monitor->count);

      current->waiting_on = NULL;
      current->waiting_with = 0;
      current->state = xt_ready;

      return xs_success;
    }

    case pdFAIL:
      return xs_no_instance;

    default:
      o4f_abort(O4F_ABORT_MONITOR, "x_monitor_enter: xSemaphoreTake() returned unknown code", retcode);
  }

  return xs_unknown; // (unreachable)
}

/*
** Notify one thread waiting on a monitor
*/
x_status x_monitor_notify(x_monitor monitor) {
  if (monitor->n_waiting-- == 0) {
    loempa(2, "Thread %p would notify one thread of monitor %p, but no thread is waiting\n", monitor, x_thread_current());

    return xs_no_instance;
  }

  loempa(2, "Thread %p is notifying one thread of monitor %p\n", monitor, x_thread_current());
  BaseType_t retcode = xSemaphoreGive(monitor->waiter_sem);
  switch (retcode) {
    case pdPASS:
      return xs_success;
    case pdFAIL:
      return xs_not_owner;
    default:
      o4f_abort(O4F_ABORT_MONITOR, "x_monitor_exit: xSemaphoreGive() failed with return code", retcode);
  }
}

/*
** Notify all threads waiting on a monitor
*/
x_status x_monitor_notify_all(x_monitor monitor) {
  
  while (monitor->n_waiting) {
    loempa(2, "notiying 1 thread of %d\n", monitor->n_waiting);
    x_status status = x_monitor_notify(monitor);
    if (status != xs_success && status != xs_no_instance) {
      return status;
    }
  }

  return xs_success;
}

/*
** Leave a monitor
*/
x_status x_monitor_exit(x_monitor monitor) {
  x_thread current = x_thread_current();

  if (monitor->owner != current) {
    loempa(9, "Thread %p cannot leave monitor %p - owner is %p\n", current, monitor, monitor->owner);
    return xs_not_owner;
  }

  monitor->count -= 1;
  if (monitor->count == 0) {
    monitor->owner = NULL;

    BaseType_t retcode = xSemaphoreGive(monitor->owner_mutex);
    switch (retcode) {
      case pdPASS:
        loempa(2, "Thread %p no longer owns monitor %p, count now 0\n", current, monitor);
        return xs_success;

      default:
        o4f_abort(O4F_ABORT_MONITOR, "x_monitor_exit: xSemaphoreGive() failed with return code", retcode);
        return xs_unknown;
    }
  }
  else {
    loempa(2, "Thread %p still owns monitor %p, count now %d\n", current, monitor, monitor->count);
  }

  return xs_success;
}

x_status x_monitor_stop_waiting(x_monitor monitor, x_thread thread) {
  if (thread->waiting_on && thread->waiting_on == monitor) {
    loempa(2, "Thread %p will stop thread %p from waiting on monitor %p\n", x_thread_current(), thread, monitor);

    BaseType_t retcode = xTaskNotify(thread->handle, 0, eNoAction);
    monitor->n_waiting--;
    switch (retcode) {
      case pdPASS: 
        return xs_success;
      case pdFAIL:
        return xs_no_instance;
      default:
        o4f_abort(O4F_ABORT_MONITOR, "x_monitor_stop_waiting: xTaskNotify() returned unknown code", retcode);
    }
  }

  return xs_no_instance;
}

