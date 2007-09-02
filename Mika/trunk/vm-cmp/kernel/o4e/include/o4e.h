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


#ifndef _O4E_H
#define _O4E_H

/*
** $Id: o4e.h,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

//#include "list.h"
#include "cyg/kernel/kapi.h"

 /*************************************************************************
 * Filename:                                                              *
 *   o4p.c                                                                *
 *                                                                        *
 * Description:                                                           *
 *                                                                        *
 *   "Oswald for ECOS" compatibility layer.                              *
 *                                                                        *
 *   We only implement the Oswald calls which are actually used by Wonka  *
 *   (the full Oswald API is much bigger than this).  If you don't see    *
 *   the Oswald system call you wanted here, feel free to implement it.   *
 *                                                                        *
 * Authors:                                                               *
 *   Dries Buytaert <dries.buytaert@acunia.com>                           *
 *   Chris Gray <chris.gray@acunia.com>                                   *
 *                                                                        *
 *************************************************************************/

                
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <signal.h>
#include <string.h>
#include <errno.h>
#include <semaphore.h>
#include <dlfcn.h>
#include "debug.h"
#include "types.h"
extern int num_x_threads;
extern char** command_line_arguments;
extern int command_line_argument_count;
//TRUE -- FALSE
#ifndef FALSE
#define FALSE	0
#endif
#ifndef TRUE
#define TRUE	1
#endif
/*  
 * Types
 */
 
typedef unsigned int x_time;
typedef unsigned long  x_sleep;
typedef x_word x_flags;
typedef struct x_Thread* x_thread;
typedef struct x_Monitor* x_monitor;
typedef const char * (*x_report)(x_thread thread);


typedef enum {
	// true and false are already defined in ecos as:
	// true = !false
	// false = 0
	// so we pick some names, and everything will work fine
	x_false = 0x00000000,
	x_true  = 0x00000001,
} x_boolean ;



void O4eEnvInit(void);	// for initializing the env
int mem_init(void);	// for initialising our memory

typedef struct x_Thread {
  int curStatus;            /* actual status of the thread*/
  cyg_mutex_t o4e_thread_mutex;		//mutex used when messing with the thread state
  cyg_cond_t o4e_cond;			/*condition variable that serves for telling everyone that this thread has finished (threads can wait on the cond)*/
  cyg_mutex_t o4e_cond_mutex;            // used because ecos is missing the join function  
  void *o4e_thread_stack_start;        /* Starting address of thread's memory area. */        
  x_size o4e_thread_stack_size;         /* Number of bytes in the stack memory area. */
  cyg_handle_t o4e_thread;               /* the handle to the created ecos thread */
  cyg_thread o4e_thread_struct;		/* used by ecos to store the info of the thread  */
  x_size o4e_thread_priority;
    
  void *o4e_thread_function;           /* The function the thread will call when it runs */
  void* o4e_thread_argument;           /* The argument to be passed ton that function */
  
  x_monitor waiting_on;			// the monitor the thread is waiting on
  x_int waiting_with;			// the amount of times the thread has entered the monitor
					//	=> used when thread waits on monitor to hold the amount 
  void* xref;				// wonka uses this pointer
  x_report report;			// wonka uses this pointer
} x_Thread;


// states a thread can be in
#define O4E_READY	    0
#define O4E_COMPLETED       1
#define O4E_TERMINATED      2
#define O4E_SUSPENDED       3
#define O4E_SLEEP           4
#define O4E_NEWBORN        12

// states of a monitor
#define MONITOR_DELETED		0
#define MONITOR_READY		1


typedef void(*x_entry)(void* arg);

typedef struct O4eEnv {
    cyg_tick_count_t timer_ticks;	// the ticks of the real time clock at 
    					// x_setup_kernel(just before Wonka starts)
    cyg_mutex_t o4e_mutex;
    cyg_mutex_t printmutex;	//mutex for printing (io not thread safe in ecos)
    int num_started;	// number of threads started
    int num_deleted;	// number of threads deleted
    int num_threads;	// number of threads on the scheduler
} O4eEnv;


// our own woempa implementation
#if defined DEBUG
#define o4e_woempa(level, format, a...) if(level>=DEBUG_LEVEL)o4e_print(format, ##a)
#else
#define o4e_woempa(level, format, a...)
#endif
void o4e_print(char* string, ...);



typedef struct x_Semaphore {
  cyg_sem_t ecos_sem;                         /* eCos counting semaphore. */ 
  cyg_mutex_t mutex;
  int deleted;
  int current;
} x_Semaphore;

typedef x_Semaphore *x_sem;
#define x_Sem	x_Semaphore

typedef struct x_Mutex {
  cyg_mutex_t ecos_mut;                      /* eCos mutex */
  x_thread owner;                        /* for checking the owner */
  int locked;
  int deleted;
  cyg_mutex_t locker_mutex;
  cyg_cond_t locker_cond;
} x_Mutex;

typedef x_Mutex *x_mutex;


typedef struct x_Queue {
  cyg_mbox mbox;
  cyg_handle_t handle;
  x_size capacity;
  int deleted;
  cyg_mutex_t mutex;
} x_Queue;

typedef x_Queue *x_queue;

typedef struct x_Monitor{
	cyg_mutex_t condmutex;		// the mutex for the condition variable
	cyg_cond_t condvar;		// the cond variable to wait on	
	x_thread owner;			// the current owner of the monitor
	x_int count;			// the times the owner has entered the monitor
	int status;			// status of our monitor
	cyg_cond_t enter_cond;		// a helper cond to signal a thread that wants to enter the mon
	cyg_mutex_t enter_mutex;	// that the condmutex has been unlocked
} x_Monitor;

//The maximum amount of memory someone can allocate in a single call.
//Oswald has such a limit so we stick to that limit
#define MAX_SINGLE_ALLOC	8*1024*1024	//8 MEG
#define HEAP_MAX		1024*1024*25	//25 MEG
#define STATIC_MEM		1024*256	//256 KBytes Static memory used during system startup

//The tags that we use in o4e
#define GARBAGE_TAG		0x20000000

// the struct for a piece of memory
typedef struct o4e_Memory_Chunk* o4e_memory_chunk;

typedef struct o4e_Memory_Chunk{
	x_word id;
	o4e_memory_chunk next;
	o4e_memory_chunk previous;
	x_size size;
	int check;	// must be TRUE to be a valid block of memory
}o4e_Memory_Chunk;

typedef struct Collect_Result{
	x_size collect_bytes;	// number of bytes collected
	x_size collect_count;	// number of blocks collected
}Collect_Result;

// wonka.h stuff
// included here because we want o4e to stand alone as much as possible in the beginning
#ifndef isSet
#define isSet(x, flag)              ((x) & (flag))
#endif
#ifndef setFlag
#define setFlag(x, flag)            ((x) |= (flag))
#endif

// call this function to define some threads
x_ubyte* x_os_main(int arg_count, char** arguments, x_ubyte* static_mem); 

#endif /* _O4P_E */

