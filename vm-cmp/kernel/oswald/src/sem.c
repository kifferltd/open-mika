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
