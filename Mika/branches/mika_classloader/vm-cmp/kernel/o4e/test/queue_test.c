
#include <cyg/kernel/kapi.h>

#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include "oswald.h"
#include "o4e.h"
#include "test.h"
#include "queue_test.h"
/*
 */

// the test program
cyg_mutex_t locker;

void queue_test(void* args){
    	int y;
	int i;
  	void* space;
	produced = 0 ;

	q_thread = malloc(2 * sizeof(x_Thread));
	q_stack  = malloc(2 * stacksize);

	queue = malloc(sizeof(x_Queue));
   	create_queue_mem();
	
	//space = malloc(7 * sizeof(int[4]));

	// Space and capacity do nothing, all queues are 256 bytes long
	x_queue_create(queue, space, 7);	
	cyg_mutex_init(&locker);
	
	for(i=0;i<5;i++){
		stuurtext[i]=i;
	}

	
	x_thread_create(queue_struct[0],&queue_producer,(void*)0,queue_stack[0],stacksize,5,TF_START);
	x_thread_create(queue_struct[1],&queue_consumer,(void*)1,queue_stack[1],stacksize,5,TF_START);

}


void create_queue_mem(){
    int u;
    woempa(9,"Entering function to create mem for the monitortest\n");
    cyg_mempool_fix_create(q_thread,3 * sizeof(x_Thread),sizeof(x_Thread),&queue_pool_handle1,&queue_pool1);
    cyg_mempool_fix_create(q_stack,3 * stacksize,stacksize,&queue_pool_handle2,&queue_pool2);
    woempa(9,"Memory for monitor test created\n");
    
    woempa(9,"Allocating memory for threads that test queues\n");
    for(u=0;u<2;u++){
           queue_struct[u] = cyg_mempool_fix_alloc(queue_pool_handle1);
           queue_stack[u]  = cyg_mempool_fix_alloc(queue_pool_handle2);
	            
    }
    woempa(9,"Na alloc\n");
}


void queue_consumer(void* arg) {

	int i;
	int returned;
	int p;
	int teller = 0 ;
        int * mydata ;   
        int ontvangtext[5] ;
	void** data = malloc(sizeof(int*)) ;
					


	for(;;) {
	        if((produced == 1) && (cyg_mutex_trylock(&locker)==1)) {
			woempa(9,"QUEUE, Consuming...\n");
			for (i=0 ; i<5; i++) {
		                x_queue_receive(queue, data, 50);
		                mydata = *data;
				ontvangtext[i] = *(int*)mydata ;
		        }			
			woempa(9,"QUEUE, boodschap ontvangen %d %d %d %d %d\n ", ontvangtext[0], ontvangtext[1]
					,ontvangtext[2], ontvangtext[3], ontvangtext[4] ) ;
			if (! ( (ontvangtext[0] == stuurtext[0]) && 
	                 	(ontvangtext[1] == stuurtext[1]) &&
				(ontvangtext[2] == stuurtext[2]) &&
				(ontvangtext[3] == stuurtext[3]) &&
				(ontvangtext[4] == stuurtext[4])
			       )
			   ) 
			{
				for(;;) { woempa (9, "ERROR - queue in de mist gegaan\n");}
			
			}
			woempa(9, "QUEUE - done with Consuming\n");
			produced = 0;
			cyg_mutex_unlock(&locker);
		}
		x_thread_sleep(200 + rand()%50);
			
	}
	
}



void queue_producer(void* arg) {

	x_status returned;
	int i;

	for(;;)	{
		if((produced == 0) && (cyg_mutex_trylock(&locker) == 1)) {
			woempa(9, "QUEUE - Producing ...\n");
			for (i=0 ; i<5; i++) {
				returned = x_queue_send(queue, &stuurtext[i], 50);
			        if (returned == xs_no_instance) woempa(9, "Send failed, due to timeout");
				else woempa(9,"Added a pointer %d to %p to the queue\n",stuurtext[i],&stuurtext[i]);
			}
			produced = 1;
			woempa(9,"QUEUE - done with producing\n");
		
			cyg_mutex_unlock(&locker);
		}
		x_thread_sleep(200 + rand()%50);

	}
}
