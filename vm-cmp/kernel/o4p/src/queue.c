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

#include "oswald.h"

/* 
 * Prototype:
 *    x_status x_queue_create(x_queue queue, void *queue_start,
 *                         w_size queue_size);
 * Description (of a message queue): 
 *    This service creates a message queue that is typically used for
 *    inter-thread communication.  Message queues are created either
 *    during initialization or during run-time.  Once created, 
 *    messages are copied to a queue by 'x_queue_send' and are copied
 *    from a queue by 'x_queue_receive'.  Each entry in the queue is 
 *    a `void *'.
 *    Application threads can suspend while attempting to send or
 *    receive a message from a queue.  Typically, thread suspension
 *    involves waiting for a message from an empty queue.  However,
 *    it is also possible for a thread to suspend trying to send a
 *    a message to a full queue.
 * Implementation:
 *    POSIX threads don't provide a sufficent queueing mechanism, but
 *    it can be implemented using condition variables and a mutex.
 *    The mutex is used to protect the internal data structure of the 
 *    queue, while the condition variables are used for synchronizing
 *    the producers and consumers.
 */
                                                                                                         
x_status x_queue_create(x_queue queue, void *messages, w_size capacity) {
  pthread_mutex_init(&queue->queue_mutex, &o4pe->mutexattr);
  pthread_mutex_init(&queue->queue_not_empty, &o4pe->mutexattr);
  pthread_mutex_init(&queue->queue_not_full, &o4pe->mutexattr);

  queue->magic = 0xb5a69788;

  queue->messages = (x_word *)messages;
  queue->read = queue->messages;
  queue->write = queue->messages;
  queue->available = 0;
  queue->capacity = capacity;
  queue->limit = (x_word *)messages + capacity;

  return xs_success;
}

/*
 * Prototype:
 *   x_status x_queue_delete(x_queue queue);
 * Description:
 *   Deletes the specified message queue.
 */
 
x_status x_queue_delete(x_queue queue) {
  pthread_mutex_lock(&queue->queue_mutex);
  queue->magic = 0;
  if (pthread_cond_broadcast(&queue->queue_not_full) != xs_success) {
    wabort(ABORT_WONKA, "broadcast failed on queue_not_full\n");
  }
  if (pthread_cond_broadcast(&queue->queue_not_empty) != xs_success) {
    wabort(ABORT_WONKA, "broadcast failed on queue_not_full\n");
  }
  if (pthread_cond_destroy(&queue->queue_not_full) != xs_success) {
    wabort(ABORT_WONKA, "destroy failed on queue_not_full\n");
  }
  if (pthread_cond_destroy(&queue->queue_not_empty) != xs_success) {
    wabort(ABORT_WONKA, "destroy failed on queue_not_full\n");
  }
  queue->available = 0;
  pthread_mutex_unlock(&queue->queue_mutex);

  return xs_success;
}
#ifndef HAVE_TIMEDWAIT
extern w_boolean join_sleeping_threads(x_thread);
extern void leave_sleeping_threads(x_thread);
#endif

/*
 * Prototype:
 *   x_status x_queue_receive(x_queue queue, void **destination_ptr,
 *                         x_sleep wait_option);
 * Description:
 *   This service retrieves a message from the queue.  The message 
 *   retrieved is copied from the queue into the memory area specified
 *   by the destination pointer.
 * Implementation:
 *   POSIX threads don't provide a sufficent queueing mechanism, but 
 *   it can be implemented using condition variables and a mutex.
 *   The mutex is used to protect the internal data structure of the 
 *   queue, while the condition variables are used for synchronizing
 *   the producers and consumers.
 */
unsigned int x_queue_receive(x_queue queue, void **msg, x_sleep owait) {
  x_status status = xs_success;
  x_thread current = x_thread_current();
  int retcode;

  if ((retcode = pthread_mutex_lock(&queue->queue_mutex))) {
    o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_mutex_lock()", retcode);
  }

  loempa(2, "Receiving element from queue\n");

  /*
  ** We have the lock, either because nobody is reading from the queue, or another thread
  ** has called this function and went into a pthread_cond_wait state, that temporarily
  ** releases the above mutex. Anyhow, we can update the number of readers of the queue.
  */

  if (queue->magic != 0xb5a69788) {
    status = xs_deleted;
    goto hastalavista;
  }

  if (queue->available == 0) {
    /*
    ** Nothing here, let's see what we should do...
    */
    if (owait == x_no_wait) {
      status = xs_no_instance;
      goto hastalavista;
    }
    
    setFlag(current->flags, TF_RECEIVING);
    current->queueing_on = queue;
#ifndef HAVE_TIMEDWAIT
    current->sleeping_on_cond = &queue->queue_not_empty;
    current->sleeping_on_mutex = &queue->queue_mutex;
#endif
    if (owait == x_eternal) {
      while (queue->available == 0) {
        pthread_cond_wait(&queue->queue_not_empty, &queue->queue_mutex);
      }
    }
    else {

#ifdef HAVE_TIMEDWAIT
      {
        struct timespec deadline;
        x_now_plus_ticks((x_long)owait, &deadline);
        while (queue->available == 0) {
          pthread_cond_timedwait(&queue->queue_not_empty, &queue->queue_mutex, &deadline);
          if (x_deadline_passed(&deadline)) {
            status = xs_no_instance;
            break;
          } 
        }
      }
#else
      current->sleep_ticks = owait;
      join_sleeping_threads(current);
      while (queue->available == 0) {
        pthread_cond_wait(&queue->queue_not_empty, &queue->queue_mutex);
        if (isSet(current->flags, TF_TIMEOUT)) {
          status = xs_no_instance;

          break;
	}
      }
      leave_sleeping_threads(current);
      current->sleep_ticks = 0;
#endif
    }
    current->queueing_on = NULL;
    unsetFlag(current->flags, TF_RECEIVING | TF_TIMEOUT);
#ifndef HAVE_TIMEDWAIT
    current->sleeping_on_cond = NULL;
    current->sleeping_on_mutex = NULL;
#endif
  }

  if (status == xs_success) {
    *(x_word *)msg = *queue->read;
    queue->read += 1;
    if (queue->read == queue->limit) {
      queue->read = queue->messages;
    }
    queue->available -= 1;
  
    if ((retcode = pthread_cond_broadcast(&queue->queue_not_full))) {  
      o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_cond_broadcast()", retcode);
    }
  }

hastalavista:

  if ((retcode = pthread_mutex_unlock(&queue->queue_mutex))) {
    o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_mutex_unlock()", retcode);
  }
 
  return status;
}

/*
 * Prototype:
 *   x_status x_queue_send(x_queue queue, void *source_ptr, 
 *                      x_sleep wait_option);
 * Description:
 *   This service sends a message to the specified message queue.  
 *   The message send is copied to the queue from the memory area
 *   specified by the source pointer.
 * Implementation:
 *   POSIX threads don't provide a queueing sufficent mechanism, but
 *   it can be implemented using condition variables and a mutex.  The
 *   mutex is used to protect the internal data structure of the 
 *   queue, while the condition variables are used for synchronizing 
 *   the producers and consumers.
 */
                                                                                                 
unsigned int x_queue_send(x_queue queue, void *msg, x_sleep wait) {
  x_status status = xs_success;
  x_thread current = x_thread_current();
  int retcode;

  if ((retcode = pthread_mutex_lock(&queue->queue_mutex))) {
    o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_mutex_lock()", retcode);
  }

  /*
  ** See x_queue_receive for info on the following line...
  */

  if (queue->magic != 0xb5a69788) {
    status = xs_deleted;
    goto hastalavista;
  }
  
  if (queue->available == queue->capacity) {
    /*
    ** Queue is full, see what we should do...
    */
    if (wait == x_no_wait) {
      status = xs_no_instance;
      goto hastalavista;
    }

    setFlag(current->flags, TF_SENDING);
    current->queueing_on = queue;
#ifndef HAVE_TIMEDWAIT
    current->sleeping_on_cond = &queue->queue_not_full;
    current->sleeping_on_mutex = &queue->queue_mutex;
#endif

    if (wait == x_eternal) {
      while (queue->available == queue->capacity) {
        if ((retcode = pthread_cond_wait(&queue->queue_not_full, &queue->queue_mutex))) {
          o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_cond_wait()", retcode);
        }
      }
    }
    else {
#ifdef HAVE_TIMEDWAIT
      {
        struct timespec deadline;
        x_now_plus_ticks((x_long)wait, &deadline);
        while (queue->available == queue->capacity) {
          pthread_cond_timedwait(&queue->queue_not_full, &queue->queue_mutex, &deadline);
          if (x_deadline_passed(&deadline)) {
            status = xs_no_instance;
            break;
          }
        }
      }
#else
      current->sleep_ticks = wait;
      join_sleeping_threads(current);
      while (queue->available == queue->capacity) {
        pthread_cond_wait(&queue->queue_not_full, &queue->queue_mutex);
        if (isSet(current->flags, TF_TIMEOUT)) {
          status = xs_no_instance;

          break;
	}
      }

      leave_sleeping_threads(current);
      current->sleep_ticks = 0;
#endif
    }

    current->queueing_on = NULL;
    unsetFlag(current->flags, TF_SENDING | TF_TIMEOUT);
#ifndef HAVE_TIMEDWAIT
    current->sleeping_on_cond = NULL;
    current->sleeping_on_mutex = NULL;
#endif
  }

  if (status == xs_success) {
    *queue->write = (x_word)msg;
    queue->write += 1;
    if (queue->write == queue->limit) {
      queue->write = queue->messages;
    }
    queue->available += 1;
  
    if ((retcode = pthread_cond_broadcast(&queue->queue_not_empty))) {
      o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_cond_broadcast()", retcode);
    }
  }

hastalavista:

  if ((retcode = pthread_mutex_unlock(&queue->queue_mutex))) {
      o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_cond_unlock()", retcode);
    }

  return status;
}

/*
 * Prototype:
 *   x_status x_queue_flush(x_queue queue, void(*do_this)(void *data))
 * Description:
 *   This service retrieves each message from the queue in turn.  The 
 *   function `do_this' is called on each message as it is received.
 * Implementation:
 *   POSIX threads don't provide a sufficent queueing mechanism, but 
 *   it can be implemented using condition variables and a mutex.
 *   The mutex is used to protect the internal data structure of the 
 *   queue, while the condition variables are used for synchronizing
 *   the producers and consumers.
 */
 
unsigned int x_queue_flush(x_queue queue, void(*do_this)(void *data)) {
  x_status status = xs_success;
  void * data;
  int retcode;

  if ((retcode = pthread_mutex_lock(&queue->queue_mutex))) {
    o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_mutex_lock()", retcode);
  }

  /*
  ** We have the lock, either because nobody is reading from the queue, or another thread
  ** has called this function and went into a pthread_cond_wait state, that temporarily
  ** releases the above mutex. Anyhow, we can update the number of readers of the queue.
  */

  if (queue->magic != 0xb5a69788) {
    status = xs_deleted;
    goto hastalavista;
  }

  if (queue->available == 0) {
    status = xs_success;
    goto hastalavista;
  }

  /*
  ** OK, when we have reached this far, everything is OK to read messages ...
  */

  while (queue->available) {
    data = (void *) *queue->read;
    queue->read += 1;
    if (queue->read == queue->limit) {
      queue->read = queue->messages;
    }
    queue->available -= 1;
    do_this(data);
  }
 
  /*
  ** OK, a message was delivered successfuly, so we can signal waiting writers that there is
  ** room again, let's signal this condition to a writing thread.
  */
  
  if ((retcode = pthread_cond_broadcast(&queue->queue_not_full))) {
    o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_cond_broadcast()", retcode);
  }

hastalavista:

  if ((retcode = pthread_mutex_unlock(&queue->queue_mutex))) {
    o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_mutex_unlock()", retcode);
  }
 
  return status;
}
