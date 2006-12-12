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
