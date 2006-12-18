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
