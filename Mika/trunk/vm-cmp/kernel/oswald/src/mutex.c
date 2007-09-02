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
** $Id: mutex.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <oswald.h>

/*
** Create a mutex.
*/

x_status x_mutex_create(x_mutex mutex) {

  mutex->owner = NULL;
  return x_event_init(&mutex->Event, xe_mutex);
    
}

x_status x_mutex_delete(x_mutex mutex) {

  x_status status = xs_success;
  x_thread thread = thread_current;
  
  x_preemption_disable;
  
  if (mutex->owner != NULL && mutex->owner != thread) {
    status = xs_not_owner;
  }
  else {
  
    /*
    ** Get this mutex event from the list of owned events and call destroy...
    */
  
    if (mutex->owner) {
      xi_remove_owned_event(mutex->owner, &mutex->Event); 
    }
    status = xi_event_destroy(&mutex->Event);

    /*
    ** We undo any possible priority inversion measures after we did the destroy, not before...
    */

    if (thread->c_prio != thread->a_prio) {
      xi_thread_priority_set(thread, xi_find_safe_priority(thread));
    }
  }

  x_preemption_enable;
  
  return status;
    
}

/*
** Internal routine to try lock a mutex.
*/

inline static x_status x_mutex_try_lock(x_thread thread, x_mutex mutex, const x_boolean decrement_competing) {

  /*
  ** Note that the check for the deleted flag should come before the check for event
  ** type integrity since the x_event_destroy function that deletes and event, resets
  ** the type to unknown...
  */

  if (x_event_is_deleted(mutex)) {
    if (decrement_competing) {
      mutex->Event.n_competing--;
    }
    return xs_deleted;
  }

  if (x_event_type_bad(mutex, xe_mutex)) {
    return xs_bad_element;
  }
  
  if (mutex->owner == NULL) {
    xi_add_owned_event(thread, &mutex->Event);
    mutex->owner = thread;
    return xs_success;
  }
  else if (mutex->owner == thread) {
    return xs_deadlock;
  }

  return xs_no_instance;

}

/*
** Lock a mutex within a certain time window.
*/

x_status x_mutex_lock(x_mutex mutex, x_sleep timeout) {

  x_status status;
  x_thread thread = thread_current;

  if (x_in_context_critical(timeout)) {
    return xs_bad_context;
  }

  x_preemption_disable;

  status = x_mutex_try_lock(thread, mutex, false);
  if (status == xs_no_instance) {
    if (timeout) {
      if (thread->c_prio < mutex->owner->c_prio) {
        xi_thread_priority_set(mutex->owner, thread->c_prio);
      }
      while (timeout && status == xs_no_instance) {
        timeout = x_event_compete_for(thread, &mutex->Event, timeout);
        status = x_mutex_try_lock(thread, mutex, true);
      }
    }
  }

  x_assert(critical_status);
  
  x_preemption_enable;

  return status;
    
}

/*
** Unlock a mutex...
*/

x_status x_mutex_unlock(x_mutex mutex) {

  x_status status;
  x_thread thread = thread_current;
  
  x_preemption_disable;

  if (x_event_type_bad(mutex, xe_mutex)) {
    status = xs_bad_element;
  }
  else if (x_event_is_deleted(mutex)) {
    status = xs_deleted;
  }
  else if (mutex->owner == NULL || mutex->owner != thread) {
    status = xs_not_owner;
  }
  else {

    /*
    ** First get it of the list of events we own; note that from this moment on, we
    ** know that mutex->owner == thread_current and we use the later for passing as
    ** argument to other functions since it saves us some indirections.
    */
    
    xi_remove_owned_event(thread, &mutex->Event);
    
    /*
    ** ... release it ...
    */
  
    mutex->owner = NULL;

    /*
    ** ... check if we did a priority inheritance and undo it ...
    */
    
    if (thread->c_prio != thread->a_prio) {
      xi_thread_priority_set(thread, xi_find_safe_priority(thread));
    }

    /*
    ** ... now it's safe to signal the availability of this event.
    */
    
    x_event_signal(&mutex->Event);
    status = xs_success;

  }
      
  x_preemption_enable;
  
  return status;
  
}

/*
** Force release of a mutex by a thread that is not the owner. For callbacks.
*/

x_status x_mutex_release(x_mutex mutex) {

  x_status status = xs_success;
  
  x_preemption_disable;

  if (x_event_type_bad(mutex, xe_mutex)) {
    status = xs_bad_element;
  }
  else if (x_event_is_deleted(mutex)) {
    status = xs_deleted;
  }
  else if (mutex->owner) {

    /*
    ** First get it of the list of events of the owner.
    */
    
    xi_remove_owned_event(mutex->owner, &mutex->Event);
    
    /*
    ** ... release it ...
    */
  
    mutex->owner = NULL;

    /*
    ** ... now it's safe to signal the availability of this event.
    */
    
    x_event_signal(&mutex->Event);
    status = xs_success;

  }
      
  x_preemption_enable;
  
  return status;
  
}
