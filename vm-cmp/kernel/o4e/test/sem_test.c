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
