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
#include "memory_test.h"

/*
 * Test program to test the proper working of our memory allocation stuff
 * Principle:
 * Alloc_prog will allocate mem until the pool is full and it will then clear the memory that it has allocated
 * After that the thread will go to sleep to let the other threads do some work
 * Calloc_prog is just a simple test to check the calloc function
 * Realloc_prog will test the realloc function
 * Tag_prog will test the tag setting and teh x_mem_size and the x_mem_is_block function
*/
char alloc_stack[4096];		// A stack for our alloc_thread
char calloc_stack[4096];	// A stack for our calloc_thread
char realloc_stack[4096];	// A stack for our realloc_thread
char tag_stack[4096];

void memory_test(void* args){
	int temp;
	alloc_thread = malloc(sizeof(x_Thread));
	calloc_thread = malloc(sizeof(x_Thread));
	realloc_thread = malloc(sizeof(x_Thread));
	tag_thread = malloc(sizeof(x_Thread));
	woempa(9,"Entering memory test program\n");
	temp = mem_init();
	if(temp == TRUE){
		x_thread_create(alloc_thread,&alloc_prog,(void*)0,alloc_stack,4096,4,TF_START);
		x_thread_create(calloc_thread,&calloc_prog,(void*)0,calloc_stack,4096,5,TF_START);
		x_thread_create(realloc_thread,&realloc_prog,(void*)0,realloc_stack,4096,5,TF_START);
		x_thread_create(tag_thread,&tag_prog,(void*)0,tag_stack,4096,5,TF_START);
		x_thread_suspend(x_thread_current());
		
	}
	else{
		woempa(9,"ERROR while allocating mempools for our heap and static mem\n");
		for(;;){
			woempa(9,"ERROR!!!\n");
		}
	}
	
}

void alloc_prog(void* arg){
	void* memory;
	w_size bytes;
	w_size blocks;
	woempa(9,"Total mem avail = %i\n",x_mem_gettotal());
	while(1){
		do{
			woempa(9,"Mem available = %i \n",x_mem_avail());
			memory = x_mem_alloc(1094*500); //allocate +- 0.5 Meg until memory == full
			x_mem_discard(memory);
		}while(memory!=NULL);
		woempa(9,"Before collect\n");
		x_mem_collect(&bytes,&blocks);
		woempa(9,"Behind collect\n");
		woempa(9,"Mem available = %i \n",x_mem_avail());
		x_thread_sleep(800 + (rand() % 50)); // let the others work

	}
}

void calloc_prog(void* arg){
	int counter;
	void* memory;
	char* temp;
	woempa(9,"Entering calloc_prog\n");
	while(1){
		memory = x_mem_calloc(100); 	//alloc 100 cleared bytes
		temp = (char*) memory;
		for(counter=0;counter<100;counter++){
			if(*temp == 0){
				woempa(9,"Char at pos %d is cleared\n",counter);
			}
			else{
				for(;;){ //will someone see this?? 
					woempa(9,"ERROR, char at position %d is not cleared\n",counter);
				}
			}
			temp++;
		}
		x_mem_free(memory);
		x_thread_sleep(100 + (rand() % 50));
	}
}

void realloc_prog(void* arg){
	int* memory;
	int test = 1234567;
	woempa(9,"Entering realloc prog\n");
	while(1){
		memory = x_mem_alloc(100);
		*memory = test;
		woempa(9,"Tekst: %d\n",*memory);
		memory = x_mem_realloc(memory,sizeof(int));
		if(*memory == 1234567)
			woempa(9,"Tekst: %d\n",*memory);
		else{
			for(;;){ // will someone see this?
				woempa(9,"ERROR, REALLOC FAILED!!");
			}
		}
		x_mem_free(memory);
		x_thread_sleep(100 + (rand() % 50));
	}
}

void tag_prog(void* arg){
	w_size size;
	w_word tag;
	void* memory;
	woempa(9,"Entering tag_prog\n");
	while(1){
		memory = x_mem_alloc(100);
		// set tag
		x_mem_tag_set(memory,12345);
		if(!x_mem_is_block(memory)){
			for(;;){
				woempa(9,"ERROR, memory block that is a block is`t seen as a valid block\n");
			}
		}
		tag = x_mem_tag_get(memory);
		if(tag!=12345){
			for(;;){
				woempa(9,"ERROR, READING OF TAG FAILED\n");
			}

		}
		size = x_mem_size(memory);
		if(size!=100){
			for(;;){
				woempa(9,"ERROR, size of memory block is not consistent \n");
			}

		}
		woempa(9,"Testing of tag and size was succesful\n");
		woempa(9,"Memory block had size %d and tag %d\n",size,tag);
		x_mem_free(memory);
		x_thread_sleep(100 +(rand() % 50));
	}
}

