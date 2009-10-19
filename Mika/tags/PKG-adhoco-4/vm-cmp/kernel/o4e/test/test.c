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


#include <cyg/kernel/kapi.h>

#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include "oswald.h"
#include "o4e.h"
#include "test.h"
#include "thread_test.h"
#include "monitor_test.h"
#include "sem_test.h"
#include "queue_test.h"
#include "mutex_test.h"
#include "memory_test.h"


int stacksize = 8192;

void cyg_user_start(void){
	

    // 32K stacks 
    void* stack = malloc(stacksize);   // threads test
    void* stack2 = malloc(stacksize);  // monitors
    void* stack3 = malloc(stacksize);  // queues
    void* stack4 = malloc(stacksize);  // mutexes
    void* stack5 = malloc(stacksize);  // semaphores
    void* stack6 = malloc(stacksize);  // memory test
    
    control_thread = malloc(sizeof(x_Thread));		//the memory to hold our control_thread strucuture
    monitor_thread = malloc(sizeof(x_Thread));		//the memory to hold our control_thread strucuture
    sem_thread = malloc(sizeof(x_Thread));		//the memory to hold our thread strucuture for semaphore checking
    queue_thread = malloc(sizeof(x_Thread));              
    mutex_thread = malloc(sizeof(x_Thread));
    memory_thread = malloc(sizeof(x_Thread));
    
    O4eEnvInit();
    woempa(9,"In cyg_user_start!!\n");

	//Thread test
	x_thread_create(control_thread,&testprogram,(void*)0,stack,stacksize,2,TF_START);
	//Monitor test
	x_thread_create(monitor_thread,&monitor_testprogram,(void*)0,stack2,stacksize,2,TF_START);
	//Sem Test
	x_thread_create(sem_thread,&sem_test,(void*)0,stack5,stacksize,2,TF_START);
	//Queue test
	x_thread_create(queue_thread,&queue_test,(void*)0,stack3,stacksize,2,TF_START);
	//Mutex test
	x_thread_create(mutex_thread,&mutex_test,(void*)0,stack4,stacksize,2,TF_START);
	//Memory Test
	x_thread_create(memory_thread,&memory_test,(void*)0,stack6,stacksize,2,TF_START);          	

}

