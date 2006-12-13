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
** $Id: scheduler_test.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include "oswald.h"
#include "main_test.h"
#include "scheduler_test.h"

extern O4wEnv * o4wenv;

/*
** Start up the scheduler test
*/

void scheduler_test(void* args){

	int y;
	loempa(8,"SCHEDULER - SCHEDULER TEST\n");

    for(;;) {
		loempa(8,"SCHEDULER - Number of threads: %d\n", o4wenv->num_threads);
		alloc_mempools();
		do{
			for(y=0;y<10;y++){
				x_thread_create(struct_mem[y],&sleep_prog,(void*)y,		NULL,1,5,TF_START);
			}
			for(y=10;y<15;y++){
				x_thread_create(struct_mem[y],&suspend_prog,(void*)y,	NULL,1,5,TF_START);
			}
			for(y=15;y<20;y++){
				x_thread_create(struct_mem[y],&stupid_prog,(void*)y,	NULL,1,5,TF_START);
			}
			WaitForAll();
			loempa(8,"SCHEDULER - Number of threads: %d\n", o4wenv->num_threads);
		} while(o4wenv->num_threads > 1);
		free_mempools();
	}
}

/*
** Wait until all thread have been ended
*/

void WaitForAll(void){
	int i;
	loempa(8,"SCHEDULER - WAITFORALL\n");
	for(i=0;i<15;i++){
		while (x_thread_join((x_thread)struct_mem[i],NULL,0) != xs_success ){ 
			Sleep(1000);
		}
		loempa(8, "SCHEDULER - Thread %d finished\n", ((x_thread)struct_mem[i])->w_thread);
		x_thread_delete((x_thread)struct_mem[i]);		
	}
	loempa(8,"SCHEDULER - Done waiting for other threads to finish\n");
}

/*
** Test to see if we can put a thread to sleep
*/

void sleep_prog(void* args){
    int delay;
	loempa(8, "SCHEDULER - SLEEP PROGRAM\n");
    delay = 200 + (rand() % 50);
    loempa(8, "SCHEDULER - Thread %d is going to sleep for %d clock ticks\n", GetCurrentThreadId(), delay);
    x_thread_sleep(delay);
    loempa(8, "SCHEDULER - Thread %d is going to sleep for %d clock ticks again\n", GetCurrentThreadId(), delay);
    x_thread_sleep(delay);
}

/*
** When can we suspend and delete a thread?
*/

void suspend_prog(void* arg){
	int data = (int) arg;
	loempa(8, "SCHEDULER - SUSPEND PROGRAM\n");

	if(data < 14)
		x_thread_suspend((x_thread)struct_mem[data+1]);
	else
		x_thread_suspend((x_thread)struct_mem[14]);

	x_thread_priority_set(x_thread_current(),8);
	x_thread_yield();

	if(data < 14)
		x_thread_resume((x_thread)struct_mem[data+1]);
	else
		x_thread_resume((x_thread)struct_mem[14]);

	//try to delete the ones that are doing the stupid program
	x_thread_delete((x_thread)struct_mem[data+5]);
	
	// suspends the threads => then delete them
	x_thread_suspend((x_thread)struct_mem[data+5]);
	x_thread_delete((x_thread)struct_mem[data+5]);
}

/*
** Just a stupid and infinite program
*/

void stupid_prog(void* arg){
   	int temp;
	int temp2;
	loempa(8, "SCHEDULER - SUSPEND PROGRAM\n");
	for(;;){
		temp=0;
		temp2=0;
		while(temp<20000){
			temp++;
			temp2 = temp / 2;
			temp2 = temp2 + temp + temp2/2;
		}
		x_thread_priority_set(x_thread_current(),8);
		x_thread_sleep(100);
	}
}

/*
** Create some memory
*/

/*
** Allocate some memory
*/

void alloc_mempools(void){
    int u;
    loempa(8,"SCHEDULER - Allocating mempools\n");
    for(u=0;u<20;u++){
		struct_mem[u] = x_mem_alloc(sizeof(x_Thread));
    }
}

/*
** Free some memory
*/

void free_mempools(void){	
    int u;
    loempa(8,"SCHEDULER - Freeing mempools\n");
    for(u=0;u<20;u++){
		x_mem_free(struct_mem[u]);
    }
}
