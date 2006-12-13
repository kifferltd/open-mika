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
** $Id: sem.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <oswald.h>

x_status x_sem_create(x_sem sem, x_size initial) {

  sem->current = initial;

  return x_event_init(&sem->Event, xe_semaphore);
  
}

inline static x_status xi_sem_try_get(x_sem sem, const x_boolean decrement_competing) {

  x_assert(critical_status);

  /*
  ** Check EF_DELETED before checking type of event since it can be changed
  ** by the x_event_destroy function.
  */

  if (x_event_is_deleted(sem)) {
    if (decrement_competing) {
      sem->Event.n_competing--;
    }
    return xs_deleted;
  }

  if (x_event_type_bad(sem, xe_semaphore)) {
    return xs_bad_element;
  }

  if (sem->current > 0) {
    sem->current -= 1;
    return xs_success;
  }

  return xs_no_instance;

}

x_status x_sem_get(x_sem sem, x_sleep timeout) {

  x_status status;
  x_thread thread = thread_current;

  if (x_in_context_critical(timeout)) {
    return xs_bad_context;
  }
  
  x_preemption_disable;

  status = xi_sem_try_get(sem, false);
  if (status == xs_no_instance) {
    if (timeout) {
      while (timeout && status == xs_no_instance) {
        timeout = x_event_compete_for(thread, &sem->Event, timeout);
        status = xi_sem_try_get(sem, true);
      }
    }
  }
  
  x_preemption_enable;
  
  return status;
  
}

x_status x_sem_put(x_sem sem) {

  x_status status;
  
  x_preemption_disable;

  if (x_event_is_deleted(sem)) {
    status = xs_deleted;
  }
  else if (x_event_type_bad(sem, xe_semaphore)) {
    status = xs_bad_element;
  }
  else {
    sem->current += 1;
    x_event_signal(&sem->Event);
    status = xs_success;
  }
  
  x_preemption_enable;
  
  return status;

}

x_status x_sem_delete(x_sem sem) {

  x_status status;
  
  x_preemption_disable;
  status = xi_event_destroy(&sem->Event);
  x_preemption_enable;
  
  return status;

}
