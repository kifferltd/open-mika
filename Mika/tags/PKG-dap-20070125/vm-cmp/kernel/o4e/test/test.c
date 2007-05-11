
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

