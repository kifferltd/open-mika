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
** $Id: queue_test.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include "oswald.h"
#include "main_test.h"
#include "queue_test.h"


CRITICAL_SECTION	w_critical_section;		/* Critical sections */

/*
** Start up the queue test
*/

void queue_test(void* args){
	int i;
	static x_size queue_depth;
	static x_ubyte * space;

	loempa(8,"QUEUE - QUEUE TEST PROGRAM\n");

	queue_depth = (rand() % 190) + 2;
	produced = 0 ;

	space		= x_mem_alloc(queue_depth * sizeof(x_word));
	q_thread	= x_mem_alloc(2 * sizeof(x_Thread));
	q_stack		= x_mem_alloc(2 * stacksize);
	queue		= x_mem_alloc(sizeof(x_Queue));

	loempa(8,"QUEUE - Create a queue\n");
	x_queue_create(queue, space, 7);

	InitializeCriticalSection(&queue->w_critical_section);
	for(i=0;i<10;i++){
		stuurtext[i]=i*2;
	}

	x_thread_create(x_mem_alloc(2 * sizeof(x_Thread)),&queue_producer,(void*)0,x_mem_alloc(2 * stacksize),stacksize,2,TF_START);
	x_thread_create(x_mem_alloc(2 * sizeof(x_Thread)),&queue_consumer,(void*)1,x_mem_alloc(2 * stacksize),stacksize,2,TF_START);
}

/*
** Start up the consumer test
*/

void queue_consumer(void* arg) {
	int i;
    int ontvangtext[5] ;
	void** data = x_mem_alloc(sizeof(int*)) ;

	loempa(8, "QUEUE - CONSUMER PROGRAM\n");

	for(;;) {
		if(produced == 1) {

			loempa(8,"QUEUE - Consuming...\n");

			for (i=0 ; i<5; i++) {
				loempa(8,"QUEUE - receive elements\n");
				x_queue_receive(queue, data, 50);
				loempa(8,"QUEUE - received elements %d\n",*(x_word*)data);
				ontvangtext[i] = *(int*)data;
			}

			loempa(8,"QUEUE - message received %d %d %d %d %d\n", ontvangtext[0], ontvangtext[1]
				,ontvangtext[2], ontvangtext[3], ontvangtext[4] ) ;
			loempa(8,"QUEUE - message posted %d %d %d %d %d\n", stuurtext[0], stuurtext[1]
				,stuurtext[2], stuurtext[3], stuurtext[4] ) ;

			if (! ( (ontvangtext[0] == stuurtext[0]) && 
				(ontvangtext[1] == stuurtext[1]) &&
				(ontvangtext[2] == stuurtext[2]) &&
				(ontvangtext[3] == stuurtext[3]) &&
				(ontvangtext[4] == stuurtext[4]))) 
			{
				for(;;) { 
					loempa (9, "QUEUE - ERROR, queue went up in smoke\n");
				}
			}
			loempa(8, "QUEUE - Done with Consuming\n");
			produced = 0;
		}
		x_thread_sleep(200 + rand()%50);
	}
}

/*
** Start up the producer test
*/

void queue_producer(void* arg) {

	x_status returned;
	int i;
	for(;;)	{
		if(produced == 0) {
			loempa(8, "QUEUE - Producing...\n");
			for (i=0 ; i<5; i++) {
				loempa(8,"QUEUE - Queue sending before timeout\n");
				returned = x_queue_send(queue, (void*) stuurtext[i], 50);
				if (returned == xs_no_instance)
					loempa(9, "QUEUE - Send failed, due to timeout");
				else
					loempa(8, "QUEUE - Added a pointer %d to %p to the queue\n",stuurtext[i],&stuurtext[i]);
			}
			produced = 1;
			loempa(8, "QUEUE - Done with producing\n");
		}
		x_thread_sleep(200 + rand()%50);
	} 
}

