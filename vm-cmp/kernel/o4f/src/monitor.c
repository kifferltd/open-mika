/**************************************************************************
* Copyright (c) 2020, 2021, 2022 by KIFFER Ltd. All rights reserved.      *
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
  x_list_init(&monitor->monitor_queue);
  monitor->monitor_queue.monitor = monitor;
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
    loempa(9, "Refuse to allow thread %p to delete monitor at %p: owned by %p\n", current, monitor, monitor->owner);

    return xs_not_owner;
  }

  if (monitor->magic != 0xf1e2d3c4) {
    loempa(9, "Refuse to delete monitor at %p: already deleted\n", monitor);

    return xs_deleted;
  }

  loempa(2, "Deleting the monitor at %p\n", monitor);
  monitor->magic = 0;
  if (monitor->owner)  {
    o4f_abort(O4F_ABORT_MONITOR, "x_monitor_delete: monitor is still owned by a thread", monitor->owner);
  }

  if (!x_list_is_empty(&monitor->monitor_queue))  {
    o4f_abort(O4F_ABORT_MONITOR, "x_monitor_delete: at least one thread is queued on monitor", monitor->monitor_queue.next);
  }
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
      xTaskNotifyStateClear(NULL);
      loempa(2, "Thread %p now owns monitor %p, count now %d\n", current, monitor, monitor->count);

      return xs_success;
    }

    case pdFAIL:
      return xs_no_instance;

    default:
      o4f_abort(O4F_ABORT_MONITOR, "x_monitor_enter: xSemaphoreTake() returned unknown code %d", retcode);

  }

  return xs_unknown; // (unreachable)
}

/*
** Wait for a monitor
*/
x_status x_monitor_wait(x_monitor monitor, x_sleep timeout) {
  x_thread current = x_thread_current();
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

  vTaskSuspendAll();
  current->monitor_queue.monitor = monitor;
  x_list_insert(&monitor->monitor_queue, &current->monitor_queue);
  retcode = xTaskResumeAll();
  loempa(2, "xTaskResumeAll() returned %s, see FreeRTOS Reference Manual for interpretation\n", retcode ? "pdTRUE" : "pdFALSE");

  // Ensure there is no notification hanging around from befor the wait
  xTaskNotifyStateClear(NULL);

  retcode = xSemaphoreGive(monitor->owner_mutex);
  if (retcode != pdPASS) {
      // The only reason to fail is if we are not the owner, but this should never happen
      o4f_abort(O4F_ABORT_MONITOR, "x_monitor_wait: xSemaphoreGive() failed", xs_unknown);
  }

  // normally we will block here, UNLESS another thread snuck in and already called x_monitor_notify
  uint32_t old_count = ulTaskNotifyTake(pdFALSE, timeout);

  if (monitor->magic != 0xf1e2d3c4) {
    return xs_deleted;
  }

  loempa(2, "Thread %p has been notified on monitor %p, count was %d\n", current, monitor, old_count);
  // either somebody gave the semaphore or we timed out, time to re-acquire the mutex
  vTaskSuspendAll();
  x_list_remove(&current->monitor_queue);
  current->monitor_queue.monitor = NULL;
  xTaskResumeAll();

  // now we need to regain ownership of the monitor
  // no time limit here, otherwise we could end up exiting the function without owning the monitor
  retcode = xSemaphoreTake(monitor->owner_mutex, portMAX_DELAY);
  if (retcode != pdPASS) {
      // This would mean that we did not obtain the monitorr, but this should not happen with timeout = portMAX_DELAY
      o4f_abort(O4F_ABORT_MONITOR, "x_monitor_wait: xSemaphoreTake() failed", xs_unknown);
  }

  unsetFlag(current->flags, TF_TIMEOUT);

  // we are the owner of the monitor again
  monitor->owner = current;
  monitor->count = current->waiting_with;
  loempa(2, "Thread %p has re-acquired monitor %p with count %i\n", current, monitor, monitor->count);

  // ... and we are no longer waiting for it
  current->waiting_on = NULL;
  current->waiting_with = 0;
  current->state = xt_ready;

  /*
  ** xSemaphoreTake() returns pdFAIL in case of a timeout, but for us this is also a normal case -
  ** so we return xs_success regardless (as expected by the higher-level waitMonitor() function).
  */
  loempa(2, "x_monitor_enter: xSemaphoreTake() returned %d\n", retcode);

  return xs_success;
}

/*
** Notify one thread waiting on a monitor
*/
x_status x_monitor_notify(x_monitor monitor) {
  if (x_list_is_empty(&monitor->monitor_queue)) {
    loempa(2, "Thread %p is not notifying any thread of monitor %p, because none is waiting\n", x_thread_current(), monitor);
    return xs_no_instance;
  }
  loempa(2, "Thread %p is notifying thread %p waiting on monitor %p\n", x_thread_current(), monitor->monitor_queue.next->thread, monitor);
  xTaskNotifyGive(monitor->monitor_queue.next->thread->handle);

  return xs_success;
}

/*
** Notify all threads waiting on a monitor
*/
x_status x_monitor_notify_all(x_monitor monitor) {
  
  x_status status =  xs_success;
  UBaseType_t old_priority = uxTaskPriorityGet(NULL);
  vTaskPrioritySet(NULL, MAX_TASK_PRIORITY);
  x_monitor_queue_element sentinel = &monitor->monitor_queue;
  x_monitor_queue_element element;
  for (element = sentinel->next; element != sentinel; element = element->next) {
    xTaskNotifyGive(element->thread->handle);
  }
  vTaskPrioritySet(NULL, old_priority);

  return status == xs_no_instance ? xs_success: status;
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

  if (current->monitor_queue.monitor) {
    o4f_abort(O4F_ABORT_MONITOR, "x_monitor_exit: thread is still waiting on a monitor", xs_unknown);
  }

  xTaskNotifyStateClear(NULL);
  monitor->count -= 1;
  if (monitor->count == 0) {
    monitor->owner = NULL;

    BaseType_t retcode = xSemaphoreGive(monitor->owner_mutex);
    switch (retcode) {
      case pdPASS:
        loempa(2, "Thread %p no longer owns monitor %p, count now 0\n", current, monitor);
        return xs_success;

      default:
        o4f_abort(O4F_ABORT_MONITOR, "x_monitor_exit: xSemaphoreGive() failed with return code %d", retcode);
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

    xTaskNotifyGive(thread->handle);
    vTaskSuspendAll();
    x_list_remove(&thread->monitor_queue);
    thread->monitor_queue.monitor = NULL;
    BaseType_t retcode = xTaskResumeAll();
    loempa(2, "xTaskResumeAll() returned %s, see FreeRTOS Reference Manual for interpretation\n", retcode ? "pdTRUE" : "pdFALSE");

    return xs_success;
  }

  return xs_no_instance;
}

