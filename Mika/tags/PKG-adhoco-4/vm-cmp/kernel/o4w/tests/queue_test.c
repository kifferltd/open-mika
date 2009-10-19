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

