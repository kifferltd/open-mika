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
