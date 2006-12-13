
#include <cyg/kernel/kapi.h>

#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include "oswald.h"
#include "o4e.h"
#include "test.h"
#include "thread_test.h"


/*
** Test program to test the proper working of our threads
** Principle: A control thread makes 20 threads
** The control thread waits until they are all finished
** 10 threads sleep randomly
** 5 threads suspend themself or another thread
** 5 threads execute a infinite program but are deleted by the 5 previous threads (the suspend threads)
*/

// the test program
void testprogram(void* args){
    int u;

    base1 = malloc(24 * sizeof(x_Thread));
    base2 = malloc(24 * stacksize);
 
    create_mempools();
 
    for(;;){	//forever, forever...
	int y;
	woempa(9,"Number of threads %d \n",num_x_threads);
	
	alloc_mempools();	
	
	do{	// control thread must be the only one running => again our hole test	    
	    
	    for(y=0;y<10;y++){
		x_thread_create(struct_mem[y],&sleepprog,(void*)y,stack_mem[y],stacksize,5,TF_START);
	    }
	    for(y=10;y<15;y++){
		x_thread_create(struct_mem[y],&suspendprog,(void*)y,stack_mem[y],stacksize,5,TF_START);
	    }
 	    for(y=15;y<20;y++){
		x_thread_create(struct_mem[y],&stupidprog,(void*)y,stack_mem[y],stacksize,5,TF_START);
	    }
 
	    WaitForAll();
	    woempa(9,"Number of threads: %d\n",num_x_threads);
	       
	    
	}while(num_x_threads > 1);

	free_mempools();
    
    }

}

void sleepprog(void* args){
    int data = (int) args;
    int delay;
    woempa(9,"Entering sleepprogram for thread %d \n",data);
    delay = 200 + (rand() % 50);
    woempa(9,"Thread %d is going to sleep for %d clock ticks\n",data,delay);
    x_thread_sleep(delay);
    woempa(9,"Thread %d is going to sleep for %d clock ticks again\n",data,delay);
    x_thread_sleep(delay);
}

void create_mempools(void){

    woempa(9,"In create_mempools\n");
    cyg_mempool_fix_create(base1,21 * sizeof(x_Thread),sizeof(x_Thread),&mempool1,&pool1);
    woempa(9,"mempool 1 created\n");
    cyg_mempool_fix_create(base2,21 * stacksize,stacksize,&mempool2,&pool2);
    woempa(9,"mempool 2 created\n");
}

void alloc_mempools(void){
    int u;
    woempa(9,"In alloc_mempools\n");
    for(u=0;u<20;u++){
	struct_mem[u] = cyg_mempool_fix_alloc(mempool1);
	stack_mem[u] = cyg_mempool_fix_alloc(mempool2);
	
    }
    woempa(9,"Na alloc\n");
}

void free_mempools(void){	
    int u;
    woempa(9,"In free_mempools\n");
    for(u=0;u<20;u++){
	cyg_mempool_fix_free(mempool1,struct_mem[u]);
	cyg_mempool_fix_free(mempool2,stack_mem[u]);
	
    }
    woempa(9,"Na free mempool\n");
		
}

void WaitForAll(void){
	int i;
	woempa(9,"Waiting for other threads to finish \n");
	for(i=0;i<20;i++){
		woempa(9,"Waiting on thread %d to finish\n",i);
		x_thread_join((x_thread)struct_mem[i],NULL,0);
		woempa(9,"Thread %d finished\n",i);
		x_thread_delete((x_thread)struct_mem[i]);		
	}
	woempa(9,"Done waiting for other threads to finish \n");

}

// suspend the next one in the line
// last one suspends itself (if already suspended => no problemo)
// setPrio to 4 => wait until the others are finished
// then resume threads

void suspendprog(void* arg){
	int t;
	int data = (int) arg;
        woempa(9,"Entering suspendprog voor thread: %d \n",data);
	if(data < 14)
		x_thread_suspend((x_thread)struct_mem[data+1]);
	else
		x_thread_suspend((x_thread)struct_mem[14]);
	x_thread_priority_set(x_thread_current(),8);
	x_thread_yield();	// yield to the next one in line
	if(data < 14)
		x_thread_resume((x_thread)struct_mem[data+1]);
	else
		x_thread_resume((x_thread)struct_mem[14]); // resumes itself => no problemo
	//try to delete the ones that are doing the stupid program
	//will not work => not suspended or completed
	x_thread_delete((x_thread)struct_mem[data+5]);
	
	// suspends the threads => then delete them
	x_thread_suspend((x_thread)struct_mem[data+5]);
	x_thread_delete((x_thread)struct_mem[data+5]);
	
	
}
// infinite program
void stupidprog(void* arg){
	int data = (int) arg;
    	int temp;
	int temp2;
	woempa(9,"Entering stupidprog voor thread: %d \n",data);
	for(;;){	// forever 
		temp=0;
		temp2=0;
		while(temp<20000){
			temp++;
			temp2 = temp / 2;
			temp2 = temp2 + temp + temp2/2;
		}
		x_thread_priority_set(x_thread_current(),8); 	//will be deleted because the other thread 
								// with prio 8 will do this
		
	}
}


