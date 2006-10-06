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
** $Id: memory_test.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include "oswald.h"
#include "main_test.h"
#include "memory_test.h"

#ifdef WINNT
x_size testMemMeg = 50;
#else
x_size testMemMeg = 15;
#endif

char alloc_stack[4096];		// A stack for our alloc_thread
char calloc_stack[4096];	// A stack for our calloc_thread
char realloc_stack[4096];	// A stack for our realloc_thread
char tag_stack[4096];		// A stack for our tag_thread

/*
** Start up the memory test
*/

void memory_test(void* args) {

	alloc_thread	= x_mem_alloc(sizeof(x_Thread));
	calloc_thread	= x_mem_alloc(sizeof(x_Thread));
	realloc_thread	= x_mem_alloc(sizeof(x_Thread));
	tag_thread		= x_mem_alloc(sizeof(x_Thread));

	loempa(8,"MEMORY - MEMORY TEST PROGRAM\n");

	x_thread_create(alloc_thread,	&alloc_prog,	(void*)0,	alloc_stack,	4096,	4,	TF_START);
	x_thread_create(calloc_thread,	&calloc_prog,	(void*)0,	calloc_stack,	4096,	5,	TF_START);
	x_thread_create(realloc_thread,	&realloc_prog,	(void*)0,	realloc_stack,	4096,	5,	TF_START);
	x_thread_create(tag_thread,		&tag_prog,		(void*)0,	tag_stack,		4096,	5,	TF_START);

	x_thread_suspend(x_thread_current());

}

/*
** Allocation test
*/

void alloc_prog(void* arg) {

	void* memory;
	x_size piep;
	x_size bytes;
	x_size blocks;

	loempa(8,"MEMORY - ALLOCATION TEST PROGRAM\n");

	while(1) {
		piep = 0;
		do {
			loempa(8,"MEMORY - Mem available = %i\n",x_mem_avail());
			loempa(8,"MEMORY - Allocating some memory\n");
			memory = x_mem_alloc(1024*1024); //allocate 1 Meg
			x_mem_discard(memory);
			Sleep(10);
			piep +=1;
		} while( piep < testMemMeg);
		loempa(8,"MEMORY - Collecting the memory\n");
		x_mem_collect(&bytes, &blocks);
		loempa(8,"MEMORY - Mem available = %i \n",x_mem_avail());
		x_thread_sleep(80 + (rand() % 50)); // let the others work
	}
}

/*
** Clear and allocation test
*/

void calloc_prog(void* arg) {
	int counter;
	void* memory;
	char* temp;
	loempa(8,"MEMORY - CLEAR AND ALLOCATE TEST PROGRAM\n");
	while(1) {
		memory = x_mem_calloc(100); 	//alloc 100 cleared bytes
		temp = (char*) memory;
		for(counter=0; counter<100; counter++){
			if(*temp == 0){
				loempa(8,"MEMORY - Char at pos %d is cleared\n",counter);
			}
			else{
				loempa(8,"MEMORY - ERROR, CLEAR AND ALLOCATE FAILED\n",counter);
				exit(0);
			}
			temp++;
		}
		x_mem_free(memory);
		x_thread_sleep(100 + (rand() % 50));
	}
}

/*
** ReAllocation test
*/

void realloc_prog(void* arg) {
	int* memory;
	int test = 1234567;
	loempa(8,"MEMORY - REALLOCATION TEST PROGRAM\n");
	while(1){
		memory = x_mem_alloc(100);
		*memory = test;
		loempa(8,"MEMORY - Tekst: %d\n",*memory);
		memory = x_mem_realloc(memory,sizeof(int));
		if(*memory == 1234567)
			loempa(8,"MEMORY - Tekst: %d\n",*memory);
		else{
			loempa(8,"MEMORY - ERROR, REALLOCATE FAILED!!");
			exit(0);
		}
		x_mem_free(memory);
		x_thread_sleep(100 + (rand() % 50));
	}
}

/*
** Tag test
*/

void tag_prog(void* arg) {
	x_size size;
	x_word tag;
	void* memory;
	loempa(8,"MEMORY - TAG TEST PROGRAM\n");
	while(1){
		memory = x_mem_alloc(100);
		// set tag
		x_mem_tag_set(memory, 12345);
		if(!x_mem_is_block(memory)){
			loempa(8,"MEMORY - ERROR, TAG NOT SET\n");
			exit(0);
		}
		tag = x_mem_tag_get(memory);
		if(tag!=12345){
			loempa(8,"MEMORY - ERROR, READING OF TAG FAILED\n");
			exit(0);
		}
		size = x_mem_size(memory);
		if(size!=100){
			loempa(8,"MEMORY - ERROR, SIZE IS NOT CONSISTENT\n");
			exit(0);
		}

		loempa(8,"MEMORY - Testing of tag and size was succesful\n");
		loempa(8,"MEMORY - Memory block had size %d and tag %d\n",size,tag);
		x_mem_free(memory);
		x_thread_sleep(100 +(rand() % 50));
	}
}

