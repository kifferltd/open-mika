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
** $Id: sem_test.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include "oswald.h"
#include "o4w.h"
#include "main_test.h"
#include "sem_test.h"

static x_size initial;

/*
** Start up the memory test
*/

void sem_test(void* args){

	loempa(8,"SEMAPHORE - MAIN TEST PROGRAM\n");

	initial = (rand() % 10) + 1;

	s_thread	= x_mem_alloc(2 * sizeof(x_Thread));
	s_stack		= x_mem_alloc(2 * stacksize);
	sem			= x_mem_alloc(sizeof(x_Semaphore));
	
	x_sem_create(sem, initial);
	
	x_thread_create(x_mem_alloc(2 * sizeof(x_Thread)),&sem_put_prog,(void*)0,x_mem_alloc(2 * stacksize),stacksize,5,TF_START);
	x_thread_create(x_mem_alloc(2 * sizeof(x_Thread)),&sem_get_prog,(void*)1,x_mem_alloc(2 * stacksize),stacksize,5,TF_START);
}

/*
** Start up the put test
*/

void sem_put_prog(void* arg) {
	x_int i;
    x_int delay;
    x_status stat;

	loempa(8,"SEMAPHORE - PUT TEST PROGRAM\n");

	for( i=1; i!=0;i++) {
		delay = i + (rand() % 50);
        stat = x_sem_put(sem);
        if( stat == 0 )
			loempa(8,"SEMAPHORE - Semaphore put succes, nr of sems: %d \n", sem->count);
        else
			loempa(9, "SEMAPHORE - Problem with semaphore, returned %d\n ",stat);
        x_thread_sleep(delay);
		loempa(8,"SEMAPHORE - Put-thread sleeping for %d ticks\n", delay);
		loempa(8,"SEMAPHORE - Put-thread sleeping for %d ticks\n", i);
	}
}

/*
** Start up the get test
*/

void sem_get_prog(void* arg) {
	x_int i;
    x_int delay;
	
	loempa(8,"SEMAPHORE - GET TEST PROGRAM\n");

    for(i=1;i!=0;i++) {
		delay = 10;
        if( sem->count == 0) {
			loempa (8,"SEMAPHORE - Semaphore count 0, nothing can be get!, yielding\n");
        }
		x_sem_get(sem,100);
		loempa(8,"SEMAPHORE - sempahore decreased to %d\n", sem->count);
		loempa(8,"SEMAPHORE - getthread sleeping for %d ticks\n", delay);
        x_thread_sleep(delay);
		loempa(8,"SEMAPHORE - putthread sleeping for %d ticks\n", i);
	}
}
