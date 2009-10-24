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
** $Id: queue.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <oswald.h>

x_status x_queue_create(x_queue queue, void * messages, x_size capacity) {

  /*
  ** A queue can hold no more then 0xffff items.
  */

  if (capacity > 0x0000ffff) {
    return xs_bad_argument;
  }
  
  queue->messages = (x_word *)messages;
  queue->read = queue->messages;
  queue->write = queue->messages;
  queue->available = 0;
  queue->capacity = capacity;
  queue->limit = (x_word *)messages + capacity;

  return x_event_init(&queue->Event, xe_queue);

}

inline static x_status xi_queue_try_send(x_queue queue, void * message, const x_boolean decrement_competing) {

  /*
  ** Check type after deleted flag...
  */

  if (x_event_is_deleted(queue)) {
    if (decrement_competing) {
      queue->Event.n_competing--;    
    }
    return xs_deleted;
  }

  if (x_event_type_bad(queue, xe_queue)) {
    return xs_bad_element;
  }

  if (queue->available < queue->capacity) {
    *queue->write = (x_word)message;
    queue->write += 1;
    if (queue->write == queue->limit) {
      queue->write = queue->messages;
    }
    queue->available += 1;
    x_event_signal_all(&queue->Event);
    return xs_success;
  }

  return xs_no_instance;
  
}

x_status x_queue_send(x_queue queue, void * message, x_sleep timeout) {

  x_status status;
  x_thread thread = thread_current;

  if (x_in_context_critical(timeout)) {
    return xs_bad_context;
  }
  
  x_preemption_disable;

  status = xi_queue_try_send(queue, message, false);

  if (status == xs_no_instance) {
    if (timeout) {
      while (timeout && status == xs_no_instance) {
        timeout = x_event_compete_for(thread, &queue->Event, timeout);
        status = xi_queue_try_send(queue, message, true);
      }
    }
  }

  x_assert(critical_status);
  
  x_preemption_enable;

  return status;

}

inline static x_status xi_queue_try_receive(x_queue queue, void ** message, const x_boolean decrement_competing) {

  x_assert(critical_status);

  if (x_event_is_deleted(queue)) {
    if (decrement_competing) {
      queue->Event.n_competing--;
    }
    return xs_deleted;
  }

  if (x_event_type_bad(queue, xe_queue)) {
    return xs_bad_element;
  }

  if (queue->available) {
    *(x_word *)message = *queue->read;
    queue->read += 1;
    if (queue->read == queue->limit) {
      queue->read = queue->messages;
    }
    queue->available -= 1;
    x_event_signal_all(&queue->Event);
    return xs_success;
  }

  return xs_no_instance;
  
}

x_status x_queue_receive(x_queue queue, void ** message, x_sleep timeout) {

  x_status status;
  x_thread thread = thread_current;

  if (x_in_context_critical(timeout)) {
    return xs_bad_context;
  }

  x_preemption_disable;

  status = xi_queue_try_receive(queue, message, false);
  if (status == xs_no_instance) {
    if (timeout) {
      while (timeout && status == xs_no_instance) {
        timeout = x_event_compete_for(thread, &queue->Event, timeout);
        status = xi_queue_try_receive(queue, message, true);
      }
    }
  }

  x_assert(critical_status);
  
  x_preemption_enable;

  return status;

}

x_status x_queue_flush(x_queue queue, void (*fcb)(void * data)) {

  x_status status;
  void * data;
  
  x_preemption_disable;

  if (x_event_is_deleted(queue)) {
    status = xs_deleted;
  }
  else if (x_event_type_bad(queue, xe_queue)) {
    status = xs_bad_element;
  }
  else if (queue->available) {

    while (queue->available) {
      data = (void *) *queue->read;
      queue->read += 1;
      if (queue->read == queue->limit) {
        queue->read = queue->messages;
      }
      queue->available -= 1;
      x_preemption_enable;
      fcb(data);
      x_preemption_disable;
    }

    x_event_signal_all(&queue->Event);
    status = xs_success;
    
  }
  else {
    status = xs_no_instance;
  }
  
  x_preemption_enable;

  return status;
  
}

// !!! NOT YET implemented above !!!
x_status x_queue_suspend(x_queue queue) {

  x_preemption_disable;

  x_event_flag_set(&queue->Event, EVENT_FLAG_SUSPENDED);

  x_preemption_enable;
  
  return xs_success;

}

x_status x_queue_resume(x_queue queue) {

  x_status status;
  
  x_preemption_disable;
  
  if (x_event_flag_is_set(&queue->Event, EVENT_FLAG_SUSPENDED)) {
    x_event_flag_unset(&queue->Event, EVENT_FLAG_SUSPENDED);
    status = xs_success;
  }
  else {
    status = xs_no_instance;
  }
  
  x_preemption_enable;
  
  return status;

}

x_status x_queue_delete(x_queue queue) {

  x_status status;
  
  x_preemption_disable;
  status = xi_event_destroy(&queue->Event);  
  x_preemption_enable;
  
  return status;
  
}

/*
** A special timer queue receive function that doesn't check the context...
** Besides the fact that no check is done for bad context, this function is
** exactly the same as the normal x_queue_receive.
*/

x_status x_timer_queue_receive(x_queue queue, void ** message, x_sleep timeout) {

  x_status status;
  x_thread thread = thread_current;

  x_preemption_disable;

  status = xi_queue_try_receive(queue, message, false);
  if (status == xs_no_instance) {
    if (timeout) {
      while (timeout && status == xs_no_instance) {
        timeout = x_event_compete_for(thread, &queue->Event, timeout);
        status = xi_queue_try_receive(queue, message, true);
      }
    }
  }

  x_preemption_enable;

  return status;

}
