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

/*
** $Id: monitor.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <oswald.h>

x_status x_monitor_create(x_monitor monitor) {

  monitor->owner = NULL;
  monitor->n_waiting = 0;
  monitor->l_waiting = NULL;
  monitor->count = 0;
  return x_event_init(&monitor->Event, xe_monitor);

}

inline static x_boolean xi_assert_monitor_count(x_monitor monitor) {

  x_thread cursor;
  x_int counted;
  
  for (counted = 0, cursor = monitor->l_waiting; cursor; cursor = cursor->l_waiting) {
    counted += 1;
  }
    
  if (counted != monitor->n_waiting) {
    loempa(9, "Counted %d threads waiting, monitor count %d.\n", counted, monitor->n_waiting);
    return false;
  }
  
  return true;

}

/*
** Internal routine to try enter a monitor.
*/

inline static x_status xi_monitor_try_enter(x_thread thread, x_monitor monitor, const x_boolean decrement_competing) {

  x_assert(critical_status);
  
  if (x_event_is_deleted(monitor)) {
    if (decrement_competing) {
      monitor->Event.n_competing--;
    }
    return xs_deleted;
  }

  if (x_event_type_bad(monitor, xe_monitor)) {
    return xs_bad_element;
  }
  
  if (monitor->owner == NULL) {
    xi_add_owned_event(thread, &monitor->Event);
    monitor->count = 1;
    monitor->owner = thread;
    return xs_success;
  }
  else if (monitor->owner == thread) {
    monitor->count += 1;
    return xs_success;
  }
  else {
    return xs_no_instance;
  }

}

x_status x_monitor_enter(x_monitor monitor, x_sleep timeout) {

  x_status status;
  x_thread thread = thread_current;

  if (x_in_context_critical(timeout)) {
    return xs_bad_context;
  }

  x_preemption_disable;

  status = xi_monitor_try_enter(thread, monitor, false);
  
  if (status == xs_no_instance) {
    if (timeout) {
      if (thread->c_prio < monitor->owner->c_prio) {
        xi_thread_priority_set(monitor->owner, thread->c_prio);
      }
      while (timeout && status == xs_no_instance) {
        timeout = x_event_compete_for(thread, &monitor->Event, timeout);
        status = xi_monitor_try_enter(thread, monitor, true);
      }
    }
  }

  x_assert(xi_assert_monitor_count(monitor));
  
  x_preemption_enable;

  return status;

}

/*
** An optimized function that is the same as x_monitor_enter(monitor, x_eternal) since
** this is used a lot in the memory implementation.
*/

x_status x_monitor_eternal(x_monitor monitor) {

  x_status status;
  x_thread thread = thread_current;

  x_preemption_disable;

  status = xi_monitor_try_enter(thread, monitor, false);
  
  if (status == xs_no_instance) {
    if (thread->c_prio < monitor->owner->c_prio) {
      xi_thread_priority_set(monitor->owner, thread->c_prio);
    }
    while (status == xs_no_instance) {
      x_event_compete_for(thread, &monitor->Event, x_eternal);
      status = xi_monitor_try_enter(thread, monitor, true);
    }
  }

  x_assert(xi_assert_monitor_count(monitor));
  
  x_preemption_enable;

  return status;

}

/*
** Add a thread to the list of waiting threads for a monitor ...
**
** Implementation note: if waiting would become a time critical element in Oswald, we could
** implement the walk over the priorities of the thread into a skiplist like structure. Each thread
** would then have not a single list by means of 'l_waiting' but it would have an array of priority
** ranges so that we can skip a number of priority comparisons.
*/

inline static void xi_add_waiting_thread(x_monitor monitor, x_thread thread) {

  x_thread current;
  volatile x_thread * update;
  x_ubyte c_prio;

  x_assert(critical_status);

  thread->waiting_for = monitor;

  if (runtime_checks && thread->l_waiting != NULL) {
    loempa(9, "waiting is not null for thread %d.\n", thread->id);
    abort();
  }
  
  if (monitor->l_waiting == NULL) {
    x_assert(monitor->n_waiting == 0);
    monitor->l_waiting = thread;
    monitor->n_waiting = 1;
  }
  else {
    monitor->n_waiting += 1;
    update = &monitor->l_waiting;
    c_prio = thread->c_prio;
    for (current = *update; current; current = current->l_waiting) {
      if (current->c_prio > c_prio) {
        thread->l_waiting = *update;
        *update = thread;
        return;
      }
      update = &current->l_waiting;
    }
    
    /*
    ** If we get here, the return in the for loop was not taken, but there where threads in the 
    ** list. Update the 'update' value, which means, add the thread at the end of the list.
    */
    
    *update = thread;

  }

}

/*
** Reset the fields in a thread used for waiting functionality. I.e.
** reset the waiting list and the monitor we were waiting for to NULL.
*/

inline static void xi_wait_reset(x_thread thread) {
  thread->l_waiting = NULL;
  thread->waiting_for = NULL;
}

/*
** Remove a thread from the waiting list of a monitor; reverse function
** of xi_add_waiting_thread.
*/

void xi_remove_waiting_thread(x_monitor monitor, x_thread thread) {

  x_thread current;
  volatile x_thread * update = &monitor->l_waiting;

  x_assert(irq_depth || critical_status);

  for (current = *update; current; current = current->l_waiting) {
    if (current == thread) {
      *update = current->l_waiting;
      xi_wait_reset(current);
      monitor->n_waiting -= 1;
      return;
    }
    update = &current->l_waiting;
  }

  if (runtime_checks) {
    loempa(9, "Thread %d was not on event waiting list !!\n", thread->id);
    abort();
  }
    
}

/*
** Exit a monitor.
*/

x_status x_monitor_exit(x_monitor monitor) {

  x_status status = xs_success;
  x_thread thread = thread_current;

  x_preemption_disable;

  if (x_event_is_deleted(monitor)) {
    status = xs_deleted;
  }
  else if (x_event_type_bad(monitor, xe_monitor)) {
    status = xs_bad_element;
  }
  else if (monitor->owner == NULL || monitor->owner != thread) {
    status = xs_not_owner;
  }
  else {
  
    monitor->count -= 1;

    if (monitor->count == 0) {
      xi_remove_owned_event(thread, &monitor->Event);
      monitor->owner = NULL;
      if (thread->c_prio != thread->a_prio) {
        xi_thread_priority_set(thread, xi_find_safe_priority(thread));
      }
      x_event_signal(&monitor->Event);
    }
  }

  x_assert(xi_assert_monitor_count(monitor));

  x_preemption_enable;
    
  return status;

}

/*
** Force release of a monitor by another thread. For callbacks.
*/

x_status x_monitor_release(x_monitor monitor) {

  x_status status = xs_success;

  x_preemption_disable;

  if (x_event_is_deleted(monitor)) {
    status = xs_deleted;
  }
  else if (x_event_type_bad(monitor, xe_monitor)) {
    status = xs_bad_element;
  }
  else if (monitor->owner) {
    xi_remove_owned_event(monitor->owner, &monitor->Event);
    monitor->owner = NULL;
    monitor->count = 0;
    x_event_signal(&monitor->Event);
  }

  x_assert(xi_assert_monitor_count(monitor));

  x_preemption_enable;
    
  return status;

}

/*
** The special compete_for function we use for re-acquiring a monitor event.
** It is special since we don't add the re-acquiring thread in priority order
** in the list, but just insert it at the front. We also don't return nor take
** a timeout value since it will always be x_eternal...
*/

inline static void xi_monitor_compete_for(x_thread thread, x_monitor monitor) {

  x_event event = &monitor->Event;

  x_assert(critical_status);
  
  thread->competing_for = event;

  thread->l_competing = event->l_competing;
  event->l_competing = thread;

  event->n_competing++;
  xi_thread_becomes_pending(thread, xi_event_timeout_action, x_eternal, xe_monitor);
  xi_thread_reschedule();

}

/*
** The 'wait' implementation of a monitor.
*/

x_status x_monitor_wait(x_monitor monitor, x_sleep timeout) {

  x_status status = xs_success;
  x_thread thread = thread_current;

  x_preemption_disable;

  /*
  ** Necessary checks ...
  */

  if (x_event_is_deleted(monitor)) {
    status = xs_deleted;
  }
  else if (x_event_type_bad(monitor, xe_monitor)) {
    status = xs_bad_element;
  }
  else if (monitor->owner == NULL || monitor->owner != thread) {
    status = xs_not_owner;
  }
  else {

    /*
    ** Record count and release claims ...
    */

    thread->m_count = monitor->count;
    xi_remove_owned_event(thread, &monitor->Event);
    monitor->count = 0;
    monitor->owner = NULL;

    /*
    ** Put ourselves in the waiting list and signal the availability of the monitor to the next
    ** competing thread, if there is one...
    */
  
    xi_thread_becomes_pending(thread, xi_wait_timeout_action, timeout, xt_waiting);
    xi_add_waiting_thread(monitor, thread);
    unsetFlag(thread->flags, TF_WAIT_INT);
    x_event_signal(&monitor->Event);

    /*
    ** OK, we are back from waiting. We were either notified, or the wait count was over or we
    ** where interrupted on this monitor. Note that waiting for and the waiting list have been
    ** properly reset allready, these fields are NULL now.
    */

    if (x_event_is_deleted(monitor)) {
      status = xs_deleted;
    }
    else if (isSet(thread->flags, TF_WAIT_INT)) {
      status = xs_interrupted;
      unsetFlag(thread->flags, TF_WAIT_INT);
    }
    else {
      status = xi_monitor_try_enter(thread, monitor, false);
  
      if (status == xs_no_instance) {
        if (thread->c_prio < monitor->owner->c_prio) {
          xi_thread_priority_set(monitor->owner, thread->c_prio);
        }
        while (status == xs_no_instance) {
          xi_monitor_compete_for(thread, monitor);
          status = xi_monitor_try_enter(thread, monitor, true);
        }
      }
    }

    if (status == xs_success) {
      monitor->count = thread->m_count;
    }

  }

  x_assert(xi_assert_monitor_count(monitor));
  
  x_preemption_enable;
  
  return status;
  
}

/*
** The single thread notification function. Make the first thread waiting, ready to run. It will
** be the highest priority thread.
*/

x_status x_monitor_notify(x_monitor monitor) {

  x_thread next;
  x_status status = xs_success;
  
  x_preemption_disable;

  if (x_event_is_deleted(monitor)) {
    status = xs_deleted;
  }
  else if (x_event_type_bad(monitor, xe_monitor)) {
    status = xs_bad_element;
  }
  else if (monitor->owner == NULL || monitor->owner != thread_current) {
    status = xs_not_owner;
  }
  else {

    /*
    ** Get the first waiting thread from the list and reschedule...
    */
  
    next = monitor->l_waiting;
    if (next) {
      xi_thread_remove_pending(next);
      monitor->l_waiting = next->l_waiting;
      xi_wait_reset(next);
      monitor->n_waiting -= 1;
    }

    xi_thread_reschedule();
    
  }

  x_assert(xi_assert_monitor_count(monitor));

  x_preemption_enable;

  return status;

}

/*
** Notify all waiting threads and empty the waiting list.
*/

x_status x_monitor_notify_all(x_monitor monitor) {

  x_thread waiter;
  x_thread next;
  x_status status = xs_success;

  x_preemption_disable;

  if (x_event_type_bad(monitor, xe_monitor)) {
    status = xs_bad_element;
  }
  else if (x_event_is_deleted(monitor)) {
    status = xs_deleted;
  }
  else if (monitor->owner == NULL || monitor->owner != thread_current) {
    status = xs_not_owner;
  }
  else {

    /*
    ** Get all waiting threads from the list in the monitor, out of their pending
    ** status and reset the monitor waiting list and count to NULL ...
    */
    
    for (waiter = monitor->l_waiting; waiter; waiter = next) {
      xi_thread_remove_pending(waiter);
      next = waiter->l_waiting;
      xi_wait_reset(waiter);
    }

    monitor->l_waiting = NULL;
    monitor->n_waiting = 0;

    xi_thread_reschedule();
    
  }

  x_assert(xi_assert_monitor_count(monitor));

  x_preemption_enable;
  
  return status;

}

x_status x_monitor_kick_all(x_monitor monitor) {

  x_thread waiter;
  x_thread next;
  x_status status = xs_success;

  x_preemption_disable;

  if (x_event_type_bad(monitor, xe_monitor)) {
    status = xs_bad_element;
  }
  else if (x_event_is_deleted(monitor)) {
    status = xs_deleted;
  }
  else {

    /*
    ** Get all waiting threads from the list in the monitor, out of their pending
    ** status and reset the monitor waiting list and count to NULL ...
    */
    
    for (waiter = monitor->l_waiting; waiter; waiter = next) {
      xi_thread_remove_pending(waiter);
      next = waiter->l_waiting;
      xi_wait_reset(waiter);
    }

    monitor->l_waiting = NULL;
    monitor->n_waiting = 0;

    // xi_thread_reschedule();
    
  }

  x_assert(xi_assert_monitor_count(monitor));

  x_preemption_enable;
  
  return status;

}

/*
** Stop a certain thread that is waiting on a monitor. Note that the
** argument thread will get a xs_interrupted return status from x_monitor_wait
** and will not own the monitor in that case! Normally x_monitor_wait will
** always return with the monitor locked for that thread, but not in the
** interrupted case.
**
** When a thread has been stopped or interrupted, waiting on a monitor, all
** subsequent attempts to stop it will return xs_no_instance, even when the
** interrupted thread has not returned from the x_monitor_wait yet.
**
** Returns: 
**
** xs_success     | when the thread was successfully found in the list of
**                | waiting threads and was removed from the list.
** ---------------+-----------------------------------------------------------
** xs_no_instance | when the thread was not found in the list of waiting
**                | threads or the thread was interrupted allready.
*/

x_status x_thread_stop_waiting(x_thread thread) {

  x_status status = xs_no_instance;
  
  x_preemption_disable;
  
  if (thread->waiting_for && isNotSet(thread->flags, TF_WAIT_INT)) {
    x_assert(xi_assert_monitor_count(thread->waiting_for));
    xi_thread_remove_pending(thread);
    xi_remove_waiting_thread(thread->waiting_for, thread);
    setFlag(thread->flags, TF_WAIT_INT);
    status = xs_success;
  }

  x_preemption_enable;
  
  return status;
    
}

x_status x_monitor_delete(x_monitor monitor) {

  x_status status;
  x_thread thread = thread_current;

  x_preemption_disable;
    
  if (monitor->owner != NULL && monitor->owner != thread) {
    status = xs_not_owner;
  }
  else {
    if (monitor->owner == thread) {  
      xi_remove_owned_event(thread, &monitor->Event);
      monitor->owner = NULL;
      if (thread->c_prio != thread->a_prio) {
        xi_thread_priority_set(thread, xi_find_safe_priority(thread));
      }
    }
    status = xi_event_destroy(&monitor->Event);
  }

  x_assert(monitor->owner == NULL || status == xs_not_owner);

  x_assert(xi_assert_monitor_count(monitor));

  x_preemption_enable;
  
  return status;

}
