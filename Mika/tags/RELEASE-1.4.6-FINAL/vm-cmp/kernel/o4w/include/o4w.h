#ifndef _O4W_H
#define _O4W_H

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
** $Id: o4w.h,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <windows.h>
#include "types.h"
#include "list.h"
#include "loempa.h"

// Some externals
extern char**	command_line_arguments;
extern int		command_line_argument_count;
extern x_ulong	msec_per_tick;
extern DWORD	lastError;

// WindowsCE specific things to write to the screen
#ifdef WINCE
	FILE * fp_1;		//Write to a file
	HWND ListBox;		//Write to a listbox
	LONG WINAPI WindowProc (HWND,UINT,WPARAM,LPARAM);
#endif

void O4wEnvInit(void);	// for initializing the env

typedef x_word                    x_flags;
typedef unsigned long			  x_sleep;
typedef BOOL					  x_boolean;

typedef CRITICAL_SECTION		  x_critical_section;

typedef struct x_Thread *         x_thread;
typedef struct x_Monitor *        x_monitor;
typedef struct x_Mutex *		  x_mutex;
typedef struct x_Memory_Chunk *	  x_memory_chunk;
typedef struct x_Semaphore *	  x_sem;
typedef struct x_Queue *		  x_queue;

typedef void (*x_entry)(void *argument);
typedef const char * (*x_report)(x_thread thread);

#define x_Sem				x_Semaphore

#define true				TRUE
#define false				FALSE

#define thread_current x_thread_current()

//The tags that we use in o4w
#ifndef STATIC_MEMORY_SIZE
#define STATIC_MEMORY_SIZE 4096
#endif

#define TAG_MASK			0x1ff00000 // The tag can hold a 9 bit information NUMBER, numbers 0 - 31 are reserved.
#define GARBAGE_TAG			0x20000000 // Piece of memory is garbage, can be reclaimed by our OWN garbage collector

/// The maximum amount of memory we allow ourselves to allocate in a single chunk.
/// (Oswald has such a limit).
#ifndef MAX_SINGLE_ALLOC
#define MAX_SINGLE_ALLOC	8*1024*1024
#endif


extern x_size	heap_remaining;
extern x_size	heap_size; 
extern x_mutex	heap_mutex;
extern x_thread heap_owner;
extern x_int    heap_claims;


typedef enum {
	xt_ready        =  0,
	xt_mutex        =  1,
	xt_queue        =  2,
	xt_mailbox      =  3,
	xt_semaphore    =  4,
	xt_signals      =  5,
	xt_monitor      =  6,
	xt_block        =  7,
	xt_map          =  8,
	xt_joining      =  9,
	xt_waiting      = 10,
	xt_suspended    = 11,
	xt_sleeping     = 12,
	xt_rescheduled  = 13,
	xt_ended        = 14,
	xt_unknown      = 15,
	xt_dummy	    = 99,
} x_state;


typedef struct O4wEnv {
    x_size				timer_ticks;			/* The number of timer ticks since we have booted */
    x_int				num_threads;			/* number of threads on the scheduler */
	x_int				num_started;
	x_int				num_deleted;
	void *				staticMemory;			/* memory used to fake Oswald's static allocation thang */
	DWORD				x_thread_key;			/* Used to hold all the created threads */
	CRITICAL_SECTION	w_critical_section;		/* Critical sections */
} O4wEnv;


typedef struct {
	enum {
		SIGNAL		= 0,
		BROADCAST	= 1,
		MAX_EVENTS	= 2,
	}x_event_state;

	x_size				waiters_count;			/* Count of the number of waiters.				*/
	x_critical_section	waiters_count_lock;		/* Serialize access to <waiters_count>.			*/
	HANDLE				events[MAX_EVENTS];		/* Signal and broadcast event HANDLEs.			*/
} x_cond_t;


typedef struct x_Thread {

	x_thread	next;
	x_thread	previous;
	x_ubyte		a_prio;				/* assigned priority.                                                                                  */
	volatile	x_ubyte c_prio;		/* current priority, used against priority inversion.                                                  */
	x_ubyte		c_quantums;			/* How many quantums does this thread has left.                                                        */
	x_ubyte		a_quantums;			/* Assigned number of quantums                                                                         */

	x_entry		entry;
	void *		argument;
	x_flags		flags;				/* Thread flags and suspend counter in upper 16 bits.                                                  */

	x_monitor	waiting_on;			/* the monitor the thread is waiting on																	*/
	x_int		waiting_with;		/* the amount of times the thread has entered the monitor												*/

	x_int		id;

	volatile x_ubyte state;			/* The state a thread is in; check out the x_type enum below. Numbers correspond with event types.     */
	
	x_boolean		just_created;				/* Has the current thread just been created? */
	x_boolean		just_deleted;				/* Has the current thread just been deleted? */
	x_boolean		is_main_thread;				/* Is this the big thread of the program?	 */
	x_boolean		is_registered;				/* Is the thread registered?				 */
	HANDLE			w_hthread;					/* HANDLE to the Windows thread				 */
	DWORD			w_thread;					/* The thread id under windows				 */
	DWORD				w_creation_flags;		/*											 */
	CRITICAL_SECTION	w_critical_section;		/* Critical sections						 */

	void *				xref;					/* May be used to point to user thread control block */
	x_report			report;
} x_Thread;


typedef struct x_Mutex {
	x_thread 	owner;						/* the owner of the mutex							*/
	x_int 		locked;
	x_int		deleted;
	HANDLE		w_hmutex;					/* HANDLE to the Windows thread						*/
} x_Mutex;


typedef struct x_Monitor {
	x_thread	owner;							/* the current owner of the monitor				*/
	x_int		count;							/* the times the owner has entered the monitor	*/
	x_int		status;							/* status of our monitor						*/

	x_cond_t	enterCond;						/* HANDLE to the Windows events					*/
	x_cond_t	condCond;						/* HANDLE to the Windows events					*/

	x_mutex		enterMutex;
	x_mutex		condMutex;

} x_Monitor;


typedef struct x_Memory_Chunk {
	char			*file;
	char			*check;
	x_word			id;
	x_word			reserved0;
	x_size			line;
	x_size			size;
	x_memory_chunk	next;
	x_memory_chunk	previous;
} x_Memory_Chunk;


struct collect_result {
	x_size collect_bytes;
	x_size collect_count;
} collect_result;


typedef struct x_Queue {
	int				o4w_queue_deleted;
	int				available;
	int				capacity;
	x_word *		messages;
	x_word *		write;
	x_word *		read;
	x_word *		limit;
	HANDLE			w_hEvent;					/* HANDLE to the Windows events */
	CRITICAL_SECTION	w_critical_section;		/* Critical sections */
} x_Queue;


typedef struct x_Semaphore {
	x_int			id;
	x_int			waiters;
	x_int			deleted;
	x_sem			next;
	x_sem			previous;
	x_size			count;
	char			naam[20];
	HANDLE			w_hEvent;				/* HANDLE to the Windows events */
	CRITICAL_SECTION	w_critical_section;		/* Critical sections							*/
} x_Semaphore;


x_ubyte* x_os_main(int arg_count, char** arguments, x_ubyte* static_mem); 
DWORD WINAPI suspend_self(void *thread_argument);

#endif
