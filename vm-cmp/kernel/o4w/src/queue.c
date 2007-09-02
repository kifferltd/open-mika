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

#include <windows.h>
#include <stdio.h>
#include "oswald.h"

x_status x_queue_create(x_queue queue, void *messages, x_size capacity){
	InitializeCriticalSection(&queue->w_critical_section);
	__try {
		EnterCriticalSection(&queue->w_critical_section);
		loempa(7,"o4w - queue - create begin\n");
		queue->o4w_queue_deleted = 0;
		queue->messages = (x_word *)messages;
		queue->read = queue->messages;
		queue->write =queue->messages;
		queue->available = 0;
		queue->capacity = capacity;
		queue->limit = (x_word *)messages + capacity;
		queue->w_hEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
		loempa(7,"o4w - queue - queue created\n");
	}
	__finally {
		LeaveCriticalSection(&queue->w_critical_section);
	}
	return xs_success;
}


x_status x_queue_delete(x_queue queue){
	
	loempa(7,"o4w - queue - delete queue\n");

	EnterCriticalSection(&queue->w_critical_section);
   	if (!queue->w_hEvent) {
		loempa(5, "o4w - queue - Refuse to delete queue at %p: already deleted\n", queue);
		return xs_deleted;
	}

	loempa(5, "o4w - queue - deleting queue at %p: \n", queue);

	queue->o4w_queue_deleted = 1;
	queue->available = 0;

	CloseHandle(queue->w_hEvent);

	queue->w_hEvent	= NULL;

	LeaveCriticalSection(&queue->w_critical_section);DeleteCriticalSection(&queue->w_critical_section);

	loempa(7,"o4w - queue - queue deleted");
	return xs_success;
}

/*
** Prototype:
**   x_status x_queue_receive(x_queue queue, void **msg, x_sleep owait)
** Description:
**   Receive a message over a queue. The value of the pointer of the data
**   will be put in void** msg.
**  TIP: you must malloc a place in the memory for the void** because
**  it will be dereferenced in the implementation.
*/

x_status x_queue_receive(x_queue queue, void **msg, x_sleep owait){

	x_int timestatus;
	x_status status = xs_success;

	loempa(7, "o4w - queue - Receiving element from queue\n");
	__try {

		EnterCriticalSection(&queue->w_critical_section);
		loempa(5, "o4w - queue - Receiving element from queue, in critical section\n");

		if (queue->o4w_queue_deleted == 1) {
			loempa(5, "o4w - queue - Receiving element from queue,queue was deleted\n");
			status = xs_deleted;
			goto hastalavista;
			loempa(5, "o4w - queue - Receiving element from queue,queue was deleted\n");
		}
		if (queue->available == 0) {
			/*
			** Nothing here, let's see what we should do...
			*/
			loempa(5, "o4w - queue - Receiving element from queue, queue not available\n");

			if (owait == x_no_wait) {
				status = xs_no_instance;
				loempa(5, "o4w - queue - Receiving element from queue, queue not available and we don't wait\n");
				goto hastalavista;
			}

			if (owait == x_eternal) {
				loempa(5, "o4w - queue - Receiving element from queue,we will wait some time\n");
				while (queue->available == 0) {
					LeaveCriticalSection(&queue->w_critical_section);
					WaitForSingleObject(queue->w_hEvent,x_eternal);
					EnterCriticalSection(&queue->w_critical_section);
				}
			}
			else {
				/*
				** A timed wait, set up the timeout stuff...
				*/
				loempa(5,"o4w - queue - wait %d ticks on the queue\n",owait);

				while (queue->available == 0) {
					timestatus=		WaitForSingleObject(queue->w_hEvent,50);
					if(timestatus==WAIT_TIMEOUT){
						loempa(5,"o4w - queue - Queue seems empty,%d\n",queue->available);
						status=xs_no_instance;


						goto hastalavista;
					}
					/*
					** timer laten lopen voor bepaalde tijd
					** terwijl controleren of queue is empty
					** in dit geval doorgaan denk ik
					** anders zien of timer afgelopen is (timeout)
					** en dan uitgaan met status = xs_no_instance;
					*/
				}
			}
		}
		/*
		** OK, when we have reached this far, everything is OK to read a message...
		*/

		*(x_word *)msg = *queue->read;
		queue->read+=1;
		if (queue->read == queue->limit) {
			queue->read = queue->messages;
		}
		queue->available -= 1;

		/*
		** OK, a message was delivered successfuly, so we can signal waiting writers that there is
		** room again, let's signal this condition to a writing thread.
		*/
		
		PulseEvent(queue->w_hEvent);
hastalavista :
		loempa(7,"o4w - queue -end of receiving queue\n");
	}
	__finally {
		LeaveCriticalSection(&queue->w_critical_section);
	}
	return status;
}

/*
** Prototype:
**   x_status x_queue_send(x_queue queue, void *msg, x_sleep wait)
** Description:
**   Send a message over a queue.
** Implementation:
**  We pass the value of the message not the message itself.
*/

x_status x_queue_send(x_queue queue, void *msg, x_sleep wait) {

	x_status status = xs_success;
	int timestatus;
	
	loempa(7,"o4w - queue - star sending queue\n");
	__try {
		EnterCriticalSection(&queue->w_critical_section);

		/*
		** See x_queue_receive for info on the following line...
		*/

		if (queue->o4w_queue_deleted == 1) {
			status = xs_deleted;
			loempa(5,"o4w - queue - queue was deleted");
			goto hastalavista;
		}

		if (queue->available == queue->capacity) {
			/*
			** Queue is full, see what we should do...
			*/
			loempa(5,"o4w - queue - queue is available sending can statrt\n");
			
			if (wait == x_no_wait) {
				status = xs_no_instance;
				goto hastalavista;
				loempa(5,"o4w - queue - queue isn't empty at all\n");
			}
			
			if (wait == x_eternal) {
				while (queue->available == queue->capacity) {
					LeaveCriticalSection(&queue->w_critical_section);
					WaitForSingleObject(queue->w_hEvent,x_eternal);
					EnterCriticalSection(&queue->w_critical_section);
					/*
					** timer laten lopen voor bepaalde tijd
					** terwijl controleren of queue is empty
					** in dit geval doorgaan denk ik
					** anders zien of timer afgelopen is (timeout)
					** en dan uitgaan met status = xs_no_instance;
					*/
				}
			}
			else {
		
				/*
				** A timed wait, set up the timeout stuff...
				*/
				
				loempa(5,"o4w - queue - waiting on %d in queue\n",wait);
				while (queue->available == queue->capacity) {
					timestatus=		WaitForSingleObject(queue->w_hEvent,10);
					if(timestatus==WAIT_TIMEOUT){
						loempa(5,"o4w - queue - Queue seems empty,%d\n",queue->available);
						status=xs_no_instance;
						goto hastalavista;
					}
					/*
					** timer laten lopen voor bepaalde tijd
					** terwijl controleren of queue is empty
					** in dit geval doorgaan denk ik
					** anders zien of timer afgelopen is (timeout)
					** en dan uitgaan met status = xs_no_instance;
					*/
				}
			}
		}

		*queue->write = (x_word)msg;
		queue->write+=1;

		if (queue->write == queue->limit) {
			loempa(5,"o4w - queue - queue = limit\n");
			queue->write = queue->messages;
		}
		queue->available += 1;

		/*
		** Let's signal the fact that something is in the queue to a reading thread.
		*/

		PulseEvent(queue->w_hEvent);
hastalavista:
		loempa(5,"o4w - queue - hastalavista");
	}
	__finally {
		LeaveCriticalSection(&queue->w_critical_section);
	}
	loempa(7,"o4w - queue - End of sending queue\n");
	return status;
}


/*
 * Prototype:
 *   x_status x_queue_flush(x_Queue *queue, void(*do_this)(void *data))
 * Description:
 *   Flushes the queue, it releases all the messages. The queue will be empty.
 * Implementation:
 */
x_status x_queue_flush(x_queue queue, void(*do_this)(void *data)) {

	x_status status = xs_success;
	void * data;

	loempa(7,"o4w - queue - Begin flush");

	__try {

		EnterCriticalSection(&queue->w_critical_section);
		/*
		** We have the lock, either because nobody is reading from the queue, or another thread
		** has called this function and went into a pthread_cond_wait state, that temporarily
		** releases the above mutex. Anyhow, we can update the number of readers of the queue.
		*/
		if (queue->o4w_queue_deleted == 1) {
			status = xs_deleted;
			goto hastalavista;
		}

		if (queue->available == 0) {
			status = xs_success;
			goto hastalavista;
			loempa(5,"o4w - queue - hastalavista");
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
		
		PulseEvent(queue->w_hEvent);
hastalavista:
		loempa(5,"o4w - queue - hastalavista");
	}
	__finally {
		LeaveCriticalSection(&queue->w_critical_section);
	}

	loempa(7,"o4w - queue - end of flush");
	return status;
}
