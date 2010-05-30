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
