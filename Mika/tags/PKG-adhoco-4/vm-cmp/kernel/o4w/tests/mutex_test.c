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
** $Id: mutex_test.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include "oswald.h"
#include "main_test.h"
#include "mutex_test.h"


/*
** Start up the mutex test
*/

void mutex_test(void* args){
    int y;
	count = 0;

	loempa(8,"MUTEX - MUTEX TEST\n");

	mu_thread = x_mem_alloc(3 * sizeof(x_Thread));
	mu_stack  = x_mem_alloc(3 * stacksize);

	mutex = x_mem_alloc(sizeof(x_Mutex));
 	create_mutex_mem();
	
	x_mutex_create(mutex);	
	for(y=0; y < 2; y++){
		x_thread_create(mutex_struct[y],&mutex_prog,(void*)y,mutex_stack[y],stacksize,5,TF_START);
	}
	x_thread_create(mutex_struct[2],&mutex_prog,(void*)3,mutex_stack[3],stacksize,3,TF_START);

}

/*
** Create some memory
*/

void create_mutex_mem(){
    int u;
    loempa(8,"MUTEX - Creating memory for the mutexes and Allocating it\n");
    for(u=0;u<3;u++){
		mutex_struct[u] = x_mem_alloc(sizeof(x_Thread));
    }
}

/*
** The test for the mutexes
*/

void mutex_prog(void* arg) {
	for(;;){
		x_status status;
		x_status rval;
		
		status = x_mutex_lock(mutex, 20);
		if (status == xs_success) {
			loempa(8,"MUTEX - Lock success, status: %d \n", status);
			count = 0 ;
			x_thread_sleep(50);
			rval = x_mutex_unlock(mutex);
			loempa(8, "MUTEX - mutex unlocked after sleep, status: %d\n",rval);
		} else {
			count++;
			loempa(8, "MUTEX - thread lock failed for %d times, status: %d\n", count, status);
		}
	}
}
