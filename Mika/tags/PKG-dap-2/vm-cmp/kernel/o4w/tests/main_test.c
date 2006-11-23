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
** $Id: main_test.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include "oswald.h"
#include "main_test.h"
#include "scheduler_test.h"
#include "mutex_test.h"
#include "monitor_test.h"
#include "memory_test.h"
#include "queue_test.h"
#include "sem_test.h"

int stacksize = 8192;

x_ubyte* x_os_main(int argument_count, char ** arguments, x_ubyte* FirstUnusedMemory) {

    // 32K stacks 
    void* stack1 = x_mem_alloc(stacksize);	// threads test
    void* stack2 = x_mem_alloc(stacksize);	// mutex
    void* stack3 = x_mem_alloc(stacksize);	// monitor
    void* stack4 = x_mem_alloc(stacksize);	// memory
    void* stack5 = x_mem_alloc(stacksize);	// queues
    void* stack6 = x_mem_alloc(stacksize);	// semaphores
    
    control_thread	= x_mem_alloc(sizeof(x_Thread));		//the memory to hold our control_thread strucuture
    mutex_thread	= x_mem_alloc(sizeof(x_Thread));		//the memory to hold our mutex_thread strucuture
    monitor_thread	= x_mem_alloc(sizeof(x_Thread));		//the memory to hold our monitor_thread strucuture
    memory_thread	= x_mem_alloc(sizeof(x_Thread));		//the memory to hold our memory_thread strucuture
    queue_thread	= x_mem_alloc(sizeof(x_Thread));		//the memory to hold our queue_thread strucuture
    sem_thread		= x_mem_alloc(sizeof(x_Thread));		//the memory to hold our sem_thread strucuture
    
    loempa(8, "MAIN: We are in the main program and memory is allocated\n");

	//Thread test
	x_thread_create(control_thread,	&scheduler_test,	(void*)0,	stack1,	stacksize,	2,	TF_START);
	//Mutex test
	x_thread_create(mutex_thread,	&mutex_test,		(void*)0,	stack2,	stacksize,	2,	TF_START);
	//Monitor test
	x_thread_create(monitor_thread,	&monitor_test,		(void*)0,	stack3,	stacksize,	2,	TF_START);
	//Memory Test
	x_thread_create(memory_thread,	&memory_test,		(void*)0,	stack4,	stacksize,	2,	TF_START);          	
	//Queue test
	x_thread_create(queue_thread,	&queue_test,		(void*)0,	stack5,	stacksize,	2,	TF_START);
	//Sem Test
	x_thread_create(sem_thread,		&sem_test,			(void*)0,	stack6,	stacksize,	2,	TF_START);

	return FirstUnusedMemory;
}
