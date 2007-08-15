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
#include "monitor_test.h"


/*
 * A Test Program for monitors
 * Principle: Prog1 works together with Prog2
 * They are testing the wait and notify function as well as the integrity of a shared variable
 * Prog2 is testing the enter-exit behavior with timeouts
 * Prog3 is testing the function x_thread_stop_waiting(x_thread thread)
 */
void monitor_testprogram(void* args){
    int t;	    
    m_thread = malloc(11 * sizeof(x_Thread));
    m_stack =  malloc(11 * stacksize);	    
    monitor = (x_monitor) malloc(sizeof(x_Monitor));
    monitor2 = (x_monitor) malloc(sizeof(x_Monitor));
    monitor3 = (x_monitor) malloc(sizeof(x_Monitor));
    teller=0;
    ERROR = FALSE;
    create_mem();
    x_monitor_create(monitor);
    x_monitor_create(monitor2);
    x_monitor_create(monitor3);
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

void prog1(void* arg){
	int temp;
	int data = (int) arg;
	int max = (data+1)*10;
	woempa(9,"Thread %d is entering prog1 \n",data);
	x_monitor_enter(monitor,x_eternal);
	while(1){
		while(teller>=max){
			woempa(9,"Thread %p is waiting on monitor because teller should be < %d but teller is: %d\n",x_thread_current(),max,teller);
			x_monitor_wait(monitor,x_eternal);
		}
			
		temp = teller;
		teller++;
		x_thread_sleep(50);
		temp++;
		if (teller!= temp || teller > 30 || teller < 0){	//ERROR
			woempa(9,"ERROR,teller:\n",teller);
			ERROR = TRUE;
		}
		woempa(9,"Thread %p is increasing teller to value: %d, teller now: %d\n",x_thread_current(),max,teller);			
		
	}
	
}
void prog2(void* arg){
	x_status state;
	int y;
	int data = (int) arg;
	woempa(9,"Thread %d is entering countprog2 \n",data);
	while(1){
		state = x_monitor_enter(monitor2,100);
		while(state == xs_no_instance){
			state = x_monitor_enter(monitor2,100);
			if (state == xs_no_instance)
				woempa(9,"Timeout occured, trying again \n");
		}
		x_thread_sleep(200 + (rand() % 50));
		x_monitor_exit(monitor2);
		x_thread_sleep(200 + (rand() % 50));
	}
	
}
void prog3(void* arg){
	int data = (int) arg;
	woempa(9,"Thread %d is entering prog3 \n",data);
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

void prog4(void* arg){
	int data = (int) arg;
	woempa(9,"Thread %d is entering checkprog \n",data);
	while(1){
		if(ERROR == TRUE){
			while(1){
				woempa(9,"ERROR\n");
			}
		}
		else{
			woempa(9,"Check_thread has detected no error\n");
			if(teller == 30){
				woempa(9,"Teller had value 30 => it's my job to set it back to 0\n");
				x_monitor_enter(monitor,x_eternal);
				teller=0;
				x_monitor_notify_all(monitor); // notifty all to continue
				x_monitor_exit(monitor);
			}
			x_thread_sleep(200);
		}
	}
	

}


void create_mem(){
    int u;
    woempa(9,"Entering function to create mem for the monitortest\n");
    cyg_mempool_fix_create(m_thread,11 * sizeof(x_Thread),sizeof(x_Thread),&monitorpool1,&mpool1);
    cyg_mempool_fix_create(m_stack,11 * stacksize,stacksize,&monitorpool2,&mpool2);
    woempa(9,"Memory for monitor test created\n");

    woempa(9,"Allocating memory for threads that test monitors\n");
    for(u=0;u<10;u++){
	mstruct[u] = cyg_mempool_fix_alloc(monitorpool1);
	mstack[u] = cyg_mempool_fix_alloc(monitorpool2);
	
    }
    woempa(9,"Na alloc\n");
	
	
}
