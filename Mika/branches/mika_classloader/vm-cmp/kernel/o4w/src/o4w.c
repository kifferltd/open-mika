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
** $Id: o4w.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <windows.h>
#include <stdlib.h>
#include "oswald.h"

DWORD lastError = 0;

int command_line_argument_count;
char ** command_line_arguments;

O4wEnv * o4wenv;			// a structure that keeps some info of the windows (thread0 environment
extern x_size usecs_per_tick;

x_status x_oswald_init(x_size max_heap, x_size millis) {
	
	loempa(7,"Initializing our Oswald Environment\n");

	heap_size = max_heap;
	msec_per_tick = millis;
	usecs_per_tick = millis * 1000;

	x_kernel_setup();

	return xs_success;
}

/*
** The functions that initialise our o4w stuff.
** The function x_oswald_init(requested heap size, msec / tick) is called from the main function in main.c
** x_setup_kernel will call x_os_main from the application that runs (wonka) to initialise threads
*/

void x_kernel_setup() {
	void* temp;

	loempa(7,"Setting up the kernel\n");
	O4wEnvInit();

	heap_remaining = 0;
	heap_remaining = heap_size;

	temp = malloc(heap_remaining);
	while (temp == NULL) {
		heap_remaining = heap_remaining * 15 / 16;
		temp = malloc(heap_remaining);
	}
	free(temp);

	heap_remaining &= 0xfff00000;

	loempa(7,"Proposed heap size = %d (%d MB), using %d (%d MB)\n", heap_size, heap_size / (1024*1024), heap_remaining, heap_remaining / (1024 * 1024));

	heap_mutex = malloc(sizeof(x_Mutex));
	x_mutex_create(heap_mutex);
	heap_owner = NULL;
	heap_claims = 0;

	x_mem_init(malloc(heap_remaining));

	x_os_main(command_line_argument_count, command_line_arguments, o4wenv->staticMemory);

	// Enter the infinite loop that takes care of our timer ticks

#ifdef WINNT
	while(1) {
		Sleep(1);	
		o4wenv->timer_ticks++;
	}
#endif

	loempa(9, "  +->We should never get here, except if we are working under WinCE!!!\n");
}

/*
** Set some windows specific things ready
*/

void O4wEnvInit() {
	static O4wEnv theEnv;
	loempa(7,"Initializing our Oswald for Windows Environment\n");

	o4wenv = &theEnv;
	o4wenv->timer_ticks = 0;
	o4wenv->num_threads = 0;
	o4wenv->num_started = 0;    
	o4wenv->num_deleted = 0;
	o4wenv->staticMemory = malloc(STATIC_MEMORY_SIZE);

	InitializeCriticalSection(&o4wenv->w_critical_section);
	x_thread_init();
}

x_sleep x_time_get(void){
	return o4wenv->timer_ticks;
}
