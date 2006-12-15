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
