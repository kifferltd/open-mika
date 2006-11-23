
#include <cyg/kernel/kapi.h>

#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include "oswald.h"
#include "o4e.h"
#include "test.h"
#include "sem_test.h"
/*
 */

// the test program
void sem_test(void* args){
    	int y;

	s_thread = malloc(2 * sizeof(x_Thread));
	s_stack  = malloc(2 * stacksize);

	sem  = malloc(sizeof(x_Semaphore));
   	create_sem_mem();
	

	x_thread_create(sem_struct[0],&sem_put_prog,(void*)0,sem_stack[0],stacksize,5,TF_START);
	x_thread_create(sem_struct[1],&sem_get_prog,(void*)1,sem_stack[1],stacksize,5,TF_START);

}


void create_sem_mem(){
    int u;
    woempa(9,"Entering function to create mem for the semtest\n");
    cyg_mempool_fix_create(s_thread,3 * sizeof(x_Thread),sizeof(x_Thread),&sem_pool_handle1,&sem_pool1);
    cyg_mempool_fix_create(s_stack,3 * stacksize,stacksize,&sem_pool_handle2,&sem_pool2);
    woempa(9,"Memory for monitor test created\n");
    
    woempa(9,"Allocating memory for threads that test sems\n");
    for(u=0;u<2;u++){
           sem_struct[u] = cyg_mempool_fix_alloc(sem_pool_handle1);
           sem_stack[u]  = cyg_mempool_fix_alloc(sem_pool_handle2);
	            
    }
    woempa(9,"Na alloc\n");
}


void sem_put_prog(void* arg) {

	int i;
        int delay;
        x_status stat;
        for( i=0; i<1000;i++) {
                delay = i + (rand() % 50);
                stat = x_sem_put(sem);
                if( stat == 0 ) woempa(9,"Semaphore put succes, nr of sems: %d \n", sem->current);
                else woempa(9, "problem with semaphore, returned %d\n ",stat);
                x_thread_sleep(delay);
                woempa(9,"putthread sleeping for %d ticks\n", delay);
        }
}


void sem_get_prog(void* arg) {

        int i;
        int delay;
        for(i=0;i<1000;i++) {
                delay = 10;
                if( sem->current == 0) {

                      woempa (9,"Semaphore count 0, nothing can be get!, yielding\n");
                }
                else {
                      x_sem_get(sem,100);
                      woempa(9,"sempahore decreased to %", sem->current);
                }
                woempa(9,"getthread sleeping for %d ticks\n", delay);
                x_thread_sleep(delay);
        }
}
