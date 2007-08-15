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
#include "mutex_test.h"
/*
 */

// the test program
void mutex_test(void* args){
    	int y;
	int count = 0;

	mu_thread = malloc(3 * sizeof(x_Thread));
	mu_stack  = malloc(3 * stacksize);

	mutex = malloc(sizeof(x_Mutex));
   	create_mutex_mem();
	
	x_mutex_create(mutex);	
	
	woempa(9,"beginning mutex threads... \n");
	for(y=0; y < 2; y++){
		x_thread_create(mutex_struct[y],&mutex_prog,(void*)y,mutex_stack[y],stacksize,5,TF_START);
	}
	// Create a thread with higher priority
	x_thread_create(mutex_struct[3],&mutex_prog,(void*)3,mutex_stack[3],stacksize,3,TF_START);

}


void create_mutex_mem(){
    int u;
    woempa(9,"Entering function to create mem for the mutex test\n");
    cyg_mempool_fix_create(mu_thread,4 * sizeof(x_Thread),sizeof(x_Thread),&mutex_pool_handle1,&mutex_pool1);
    cyg_mempool_fix_create(mu_stack,4 * stacksize,stacksize,&mutex_pool_handle2,&mutex_pool2);
    woempa(9,"Memory for monitor test created\n");
    
    woempa(9,"Allocating memory for threads that test mutexes\n");
    for(u=0;u<3;u++){
           mutex_struct[u] = cyg_mempool_fix_alloc(mutex_pool_handle1);
           mutex_stack[u]  = cyg_mempool_fix_alloc(mutex_pool_handle2);
	            
    }
    woempa(9,"Na alloc\n");
}


void mutex_prog(void* arg) {

/*	
	// simple test  -- watch it ! run only 1 thread
        x_mutex_lock(mutex,200);
        woempa(9, "MUTEX - IN LOCK\n");
        x_mutex_unlock(mutex);
        woempa(9, "MUTEX - UNLOCKED\n");
*/										

	

	for(;;){
		x_status status;
		x_status rval;
		
		status = x_mutex_lock(mutex, 20);
		if (status == xs_success) {
			woempa(9, "MUTEX - Lock success, status: %d \n", status);
			count = 0 ;
			x_thread_sleep(50);
			woempa(9,"MUTEX - thread waked up\n");
			rval = x_mutex_unlock(mutex);
			woempa(9, "MUTEX - mutex unlocked after sleep, status: %d\n",rval);
		} else {
			count++;
			woempa(9, "MUTEX - thread lock failed for %d times, status: %d\n", count, status);
		}
	}

}
