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
** $Id: monitor_test.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include "oswald.h"
#include "main_test.h"
#include "monitor_test.h"

/*
** Start up the monitor test
*/

void monitor_test(void* args){
    int t;	    
    m_thread = x_mem_alloc(11 * sizeof(x_Thread));
    m_stack =  x_mem_alloc(11 * stacksize);	    
    monitor1 = (x_monitor) x_mem_alloc(sizeof(x_Monitor));
    monitor2 = (x_monitor) x_mem_alloc(sizeof(x_Monitor));
    monitor3 = (x_monitor) x_mem_alloc(sizeof(x_Monitor));
    counter=0;

    ERRORs = FALSE;
    create_mem();

    x_monitor_create(monitor1);
    x_monitor_create(monitor2);
    x_monitor_create(monitor3);

	//Start up the different tests
    for(t=0;t<3;t++){
		x_thread_create(mstruct[t],&prog1,(void*)t,mstack[t],stacksize,5,TF_START);
    }
	for(t=3;t<6;t++){
		x_thread_create(mstruct[t],&prog2,(void*)t,mstack[t],stacksize,5,TF_START);
    }
	for(t=6;t<9;t++){
		x_thread_create(mstruct[t],&prog3,(void*)t,mstack[t],stacksize,5,TF_START);
    }
	for(t=9;t<10;t++){
		x_thread_create(mstruct[t],&prog4,(void*)t,mstack[t],stacksize,4,TF_START);
	}

	x_thread_suspend(x_thread_current());
	
}

/*
** This tests tests the creating and waiting functions of the monitors
*/

void prog1(void* arg){
	int temp;
	int data = (int) arg;
	int max = (data+1)*10;

	loempa(8,"MONITOR - RUNNING TESTPROGRAM 1\n");
	
	x_monitor_enter(monitor1, x_eternal);
	
	while(1){
		while(counter>=max){
			loempa(8,"MONITOR - Thread %d is waiting on monitor because counter should be < %d but counter is: %d\n",x_thread_current()->id,max,counter);
			x_monitor_wait(monitor1, x_eternal);
		}
			
		temp = counter;
		counter++;
		x_thread_sleep(50);
		temp++;

		if (counter!= temp || counter > 30 || counter < 0) {
			loempa(9, "MONITOR - ERROR, COUNTER ERROR:\n",counter);
			ERRORs = TRUE;
			exit(0);
		}

		loempa(8,"MONITOR - Thread %p is increasing counter to value: %d, counter now: %d\n",x_thread_current(),max,counter);			
		
	}
}

/*
** Another test for monitors
*/

void prog2(void* arg){
	x_status state;
	loempa(8,"MONITOR - RUNNING TESTPROGRAM 2\n");
	while(1){
		state = x_monitor_enter(monitor2, 100);
		while(state == xs_no_instance){
			state = x_monitor_enter(monitor2, 100);
			if (state == xs_no_instance)
				loempa(8,"MONITOR - Timeout occured, trying again\n");
		}
		x_thread_sleep(200 + (rand() % 50));
		x_monitor_exit(monitor2);
		x_thread_sleep(200 + (rand() % 50));
	}
	
}

/*
** Another test for monitors
*/

void prog3(void* arg){
	int data = (int) arg;
	loempa(8, "MONITOR - RUNNING TESTPROGRAM 3\n");
	if(data == 6 || data == 7){
		while(1){
			x_monitor_enter(monitor3,1000);
			x_monitor_wait(monitor3,x_eternal);
			x_thread_sleep(50 + (rand() % 50));
			x_monitor_exit(monitor3);
		}
	}
	else{
		while(1){
			x_thread_stop_waiting(mstruct[6]);
			x_thread_sleep(50 + (rand() % 50));
			x_thread_stop_waiting(mstruct[7]);
			x_thread_sleep(50 + (rand() % 50));
		}
	}
	

}

/*
** This tests tests the enter and notify functions of the monitors
*/

void prog4(void* arg){
	loempa(8, "MONITOR - RUNNING TESTPROGRAM 4\n");
	while(1){
		if(ERROR == TRUE){
			while(1){
				loempa(9,"MONITOR - ERROR\n");
				exit(0);
			}
		}
		else{
			loempa(8, "MONITOR - Check_thread has detected no error\n");
			if(counter == 30){
				loempa(8,"MONITOR - Teller had value 30 => it's my job to set it back to 0\n");
				x_monitor_enter(monitor1, x_eternal);
				counter=0;
				x_monitor_notify_all(monitor1); // notifty all to continue
				x_monitor_exit(monitor1);
			}
			x_thread_sleep(200);
		}
	}
	

}

/*
** Create some memory for the tests
*/

void create_mem(){
    int u;
    loempa(8,"MONITOR - CREATE SOME MEMORY\n");

    loempa(8,"MONITOR - Allocating memory for threads that test monitors\n");
    for(u=0;u<10;u++){
		mstruct[u] = x_mem_alloc(sizeof(x_Thread));
    }
}
