/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
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
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: event.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <oswald.h>

/*
** Initialize the event subsystem.
*/

x_ubyte * x_events_setup(x_ubyte * memory) {
  return memory;
}

/*
** Initialize an event. This function should be called by any event creation function.
*/

x_status x_event_init(x_event event, x_type type) {

  event->l_owned = NULL;
  event->l_competing = NULL;
  event->n_competing = 0;
  event->flags_type = type;

  return xs_success;
  
}

/*
** Remove an event from the list of owned events of a thread. The update pointer is pointing to an
** event. At the beginning it is pointing to the first event owned by the thread, at subsequent
** points in the for loop, it is pointing to the preceeding event, linked through the event->l_owned
** field.
*/

void xi_remove_owned_event(x_thread thread, x_event event) {

  x_event current;
  volatile x_event * update = &thread->l_owned;

  x_assert(critical_status);

  for (current = *update; current; current = current->l_owned) {
    if (current == event) {
      *update = current->l_owned;
      return;
    }
    update = &current->l_owned;
  }

  if (runtime_checks) {
    loempa(9, "Problem; not found.\n");
    abort();
  }

}

inline static x_boolean xi_assert_event_count(x_event event) {

  x_thread cursor;
  x_int counted;
  
  for (counted = 0, cursor = event->l_competing; cursor; cursor = cursor->l_competing) {
    counted += 1;
  }
    
  if (counted != event->n_competing) {
    loempa(9, "Counted %d threads competing, event has %d count.\n", counted, event->n_competing);
    return false;
  }
  
  return true;
  
}

/*
** Add a thread to the list of competing threads for an event.
**
** Competing threads are added according to priority, which IMHO is the most fair
** option. For threads having the same priority, hard or soft, will be added at
** the end of the threads with the same priority, which is also fair; longer competing
** threads will be served first...
**
** Note that the opposite function to remove a thread from the list of competing threads
** for an event is defined in scheduler.c.
*/

inline static void xi_add_competing_thread(x_event event, x_thread thread) {

  x_thread current;
  volatile x_thread * update;

  x_assert(critical_status);
  x_assert(thread->l_competing == NULL);

  /*
  ** First see if there is allready a competing thread. If not, just update the l_competing
  ** field of the event.
  */
 
  if (event->l_competing == NULL) {
    event->l_competing = thread;
  }
  else {
    update = &event->l_competing;
    for (current = *update; current; current = current->l_competing) {
      if (current->c_prio > thread->c_prio) {
        thread->l_competing = current;
        *update = thread;
        return;
      }
      update = &current->l_competing;
    }
    
    /*
    ** If we still haven't found the correct place, based on priority and we reached the
    ** end of the list, indicated by reaching this point (i.e. not taken the return in
    ** the for loop), we add it at the end of the list, which is in 'update'.
    */
    
    *update = thread;

  }
  
}

/*
** When a thread is woken up from a pending list, the moment of wake up is recorded in the
** thread->wakeup field. The x_event_compete_for returns the amount of ticks that were still
** remaining from the original timeout. Now between the moment of wakeup and the moment
** that is 'now', where the thread starts really doing some work again, more ticks could have passed,
** we compensate this with the following function so that we can potentially start competing
** for an event again, if the condition is not satisfied yet and we still have ticks to compete left.
** We compensate only when the timeout is not eternal. When the remaining timeout is smaller than the 
** time we compensate for, we return 0 (note that x_sleep is unsigned so otherwise, we get a wrapround...).
*/

inline static x_sleep x_compensate_timeout(x_thread thread) {

  x_sleep compensation;

  if (thread->wakeup == 0) {
    return 0;
  }
  else {
    if (thread->sticks != x_eternal) {
      if (system_ticks >= thread->wakeup) {
        compensation = system_ticks - thread->wakeup;              // normal case
      }
      else {
        compensation = x_eternal - system_ticks - thread->wakeup;  // ticks have wrapped around since timeout...
      }

      if (thread->sticks > compensation) {
        return thread->sticks - compensation;
      }
      else {
        return 0;
      }
    }
  }

  return x_eternal;

}

/*
** Function to call when the current thread wants to acquire a certain event, with a timeout,
** that can be 'wait forever' or a normal timeout, but never 'no wait'. Note that this function
** should only be called from within a protected region.
**
** The return value is the number of ticks we still had left when competing ended; this can very well
** be a 'wait forever' value.
*/

x_sleep x_event_compete_for(x_thread thread, x_event event, x_sleep timeout) {

  x_assert(critical_status);
  x_assert(thread == thread_current);

  /*
  ** We increment the competers count; this count is not decremented by the event routines, as it 
  ** DOES NOT actually list the number of threads on the competing list. It DOES indicate the number
  ** of threads on the competitors list plus the number of threads that haven't acknowledged yet that
  ** they are re-evaluating the event in their '_try_' functions.
  */
  
  event->n_competing += 1;
  xi_thread_becomes_pending(thread, xi_event_timeout_action, timeout, x_event_type_get(event));
  thread->competing_for = event;
  xi_add_competing_thread(event, thread);
  x_assert(xi_assert_event_count(event));

  /*
  ** All is setup to wait for a change in this event; do a rescheduling...
  */

  xi_thread_reschedule();

  /*
  ** At this point, we'll be out of the list of pending threads, either by a timeout
  ** or by a signal that the event became available. We let the calling function sort
  ** out if the event is available for use or not. We return the compensated time we
  ** competed for.
  */

  x_assert(critical_status);

  return x_compensate_timeout(thread);

}

/*
** Signal to the first thread in the competing list, that the event has changed state. 
** The first competing thread is woken up and can proceed to check wether this event change 
** is the one he wanted to see...
*/

void x_event_signal(x_event event) {

  x_thread next;
  
  x_assert(critical_status);

  /*
  ** When there are other threads competing, we pick up the first (highest priority) and
  ** get it out of the list. In any case, we reschedule since the calling function (thread) could
  ** have changed priority. Note that there are two lists to be manipulated now, the global list of
  ** pending threads from which we escape with 'remove pending' and the list of threads that are
  ** competing for this event to happen, from which we remove ourselves as the next thread, which
  ** is the first thread in the list.
  */
  
  if (event->l_competing) {
    x_assert(event->l_competing != thread_current);
    next = event->l_competing;
    event->l_competing = next->l_competing;
    xi_thread_remove_pending(next);
    event->n_competing -= 1;
    next->l_competing = NULL;
    next->competing_for = NULL;
    x_assert(xi_assert_event_count(event));
  }

  xi_thread_reschedule();

  x_assert(critical_status);
  
}

/*
** Signal to all competing threads that a certain event has changed state. All threads are removed
** from the pending list and from the event's competitors list. 
*/

void x_event_signal_all(x_event event) {

  x_thread competitor;
  x_thread next;

  x_assert(critical_status);
 
  for (competitor = event->l_competing; competitor; competitor = next) {
    xi_thread_remove_pending(competitor);
    next = competitor->l_competing;
    competitor->l_competing = NULL;
    competitor->competing_for = NULL;
  }

  event->l_competing = NULL;
  event->n_competing = 0;
  x_assert(xi_assert_event_count(event));

  xi_thread_reschedule();

  x_assert(critical_status);
  
}

/*
** Remove an event from the system. Must be called from a critical section.
*/

x_status xi_event_destroy(x_event event) {

  x_thread competitor;
  x_thread next;
  x_status status = xs_success;
  x_sleep sleep = 1;
  x_int type = x_event_type_get(event);
  
  x_assert(critical_status);

  /*
  ** Check for impossible signature of the event. 
  */
    
  if (type <= xe_unused || type >= xe_unknown) {
    status = xs_bad_element;
  }
  else if (x_event_is_deleted(event)) {
    status = xs_deleted;
  }
  else if (event->n_competing) {
    x_event_flag_set(event, EVENT_FLAG_DELETED);
    status = xs_competing;
    
    /*
    ** The following is essentially the same as x_signal_all, only we don't reset event->n_competing
    ** since this must be done by the respective threads in their try functions to indicate that they
    ** are not competing anymore. We do set event->l_competing to NULL since no thread can be competing
    ** for this event anymore.
    */
    
    for (competitor = event->l_competing; competitor; competitor = next) {
      xi_thread_remove_pending(competitor);
      next = competitor->l_competing;
      competitor->l_competing = NULL;
      competitor->competing_for = NULL;
    }
    
    event->l_competing = NULL;
    
    /*
    ** We must check for n_competing bigger than 0 and not negative. For each event, when we
    ** enter the timed wait condition, we do a ..._try_... call with decrement_competing set
    ** to true (this function is inlined, so that the compiler optimises the if clause away).
    ** But at the first entry we haven't done a compete_for yet that increments the n_competing.
    ** Therefore, we could decrement the n_competing below 0 (0xffff or -1) because of a race
    ** condition, i.e. when this function is called before we could do the first compete_for.
    ** Therefore, we also compensate the n_competing number, after the while clause.
    ** Does this look strange? It is, I'll have to look into this... TODO
    */

    while ((signed short)event->n_competing > 0) {
      x_thread_sleep(sleep++);
      if (sleep == 25) {
        status = xs_incomplete;
        break;
      }
    }

    if ((signed short)event->n_competing < 0) {
      event->n_competing = 0;
    }

    xi_thread_reschedule();

  }

  x_assert(critical_status);

  return status;
  
}

/*
** Find the safest (against priority inversion) priority for a certain thread that owns a
** set of events. This means checking for all the events that we own, and inheriting the priority of
** the highest competing thread. If we don't own any events anymore, we return our assigned priority.
*/

x_size xi_find_safe_priority(x_thread thread) {

  x_size prio = thread->a_prio;
  x_event event;

  x_assert(critical_status);

  for (event = thread->l_owned; event; event = event->l_owned) {
    if (event->l_competing && event->l_competing->c_prio < prio) {
      prio = event->l_competing->c_prio;
      break;
    }
  }

  x_assert(critical_status);

  return prio;
  
}

/*
** Wait for an event to have no competing other threads on it anymore, for a certain number of ticks. Returns
** xs_success when no threads are waiting on the event anymore. Returns xs_incomplete when not all threads
** did acknowledge that the event has been deleted; returns xs_bad_element when the event has an invalid 
** type or wasn't deleted yet. 
**
** If it is safe to return the memory of the event, in case of xs_complete status, to the free pool 
** of memory is up to the discretion of the caller...
*/

x_status x_event_join(void * e, x_time ticks) {

  x_status status = xs_bad_element;
  x_event event = e;
  x_int type = x_event_type_get(event);
  
  //                        shouldn't this be && ??
  if ((type > xe_unused && type < xe_unknown) || x_event_flag_is_not_set(event, EVENT_FLAG_DELETED)) {
    status = xs_success;
    while ((signed short)event->n_competing > 0 && ticks) {
      x_thread_sleep(1);
      if (ticks == 0) {
        status = xs_incomplete;
        break;
      }
      if (ticks != x_eternal) {
        ticks -= 1;
      }
    }

    if ((signed short)event->n_competing < 0) {
      event->n_competing = 0;
    }
    else if (event->n_competing == 0) {
      status = xs_success;
    }
    
  }

  return status;  

}
