#ifndef _OSWALD_H
#define _OSWALD_H

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
** $Id: oswald.h,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <windows.h>
#include "xmisc.h"
#include "loempa.h"
#include "o4w.h"
#include "vsprintf.h"

#define NUM_PRIORITIES			64
#define NUM_HARD_PRIORITIES		62
#ifdef	MAX_PRIORITY			//Window-threads uses this also but it has a maximum value of 256
#undef	MAX_PRIORITY
#endif
#define MAX_PRIORITY			128
#define MIN_PRIORITY			1
#define NUM_SOFT_PRIORITIES		(MAX_PRIORITY - NUM_HARD_PRIORITIES)
#define MIN_SOFT_PRIORITY		(NUM_HARD_PRIORITIES + 1)
#define NUM_GROUPS				(NUM_PRIORITIES / 8)        /* 8 bits per bytes */


#define isSet(x, flag)            ((x) & (flag))
#define isNotSet(x, flag)         (!isSet((x), (flag)))
#define setFlag(x, flag)          ((x) |= (flag))
#define unsetFlag(x, flag)        ((x) &= ~(flag))
#define maskFlags(m, f)           ((m) & (f))

#ifdef INFINITE
#undef INFINITE
#endif

#define x_no_wait	0
#define x_eternal	0xffffffff


#define x_mem_total	x_mem_gettotal


#define TF_START              0x00000100 /* Special value to pass to x_thread_create to immediately start a thread. */
#define TF_ETERNAL            0x00000200 /* Thread is eternally sleeping */
#define TF_SUSPENDED          0x00000400 /* Thread is currently suspended */
#define TF_TIMER              0x00000800 /* Special flag for the timer handler thread. */
#define TF_JOIN_ENDED         0x00001000 /* Thread we joined with has ended normally. */
#define TF_JOIN_EXIT          0x00002000 /* Thread we joined with has called x_thread_exit. */
#define TF_JOIN_DELETED       0x00004000 /* Thread we joined with has been suspended and then deleted. */
#define TF_STATE_MASK         0x000000ff /* Saved thread status in suspend, one of the above xt_ states. */
#define TF_COUNTER_MASK       0xffff0000 /* Mask to keep count of number of nested suspends/resumes. */
#define TF_COUNTER_SHIFT      (16)       /* Number of bits to shift down to achieve the nested count. */
#define TF_COUNT_ONE          0x00010000 /* Nested count is set to 1. */


#define MONITOR_DELETED		0
#define MONITOR_READY		1


/*
** This is the entry point which the OS will call when it has completed
** its own internal initialisation.
*/
void x_os_main(int argc, char** argv);


/*
** Some important functions
*/

void x_kernel_setup(void);
void x_init_entry(void *memory);
x_status x_oswald_init(x_size max_heap, x_size millis);


/*
** The Oswald time API (part of).
*/

x_sleep x_time_get(void);

x_size x_millis2ticks(x_size millis);
x_size x_seconds2ticks(x_size seconds);
x_size x_ticks2usecs(x_size ticks);
x_size x_usecs2ticks(x_size usecs);


/*
** The OSwald API for threads
*/

x_status	x_thread_create(x_thread thread, x_entry entry, void *argument, x_ubyte *b_stack, x_size s_stack, x_size prio, x_flags flags);
x_status	x_thread_delete(x_thread thread);
x_status	x_thread_priority_set(x_thread thread, x_size new_priority);
x_status	x_thread_quantum_set(x_thread thread, x_size new_quantum);
x_status	x_thread_resume(x_thread thread);
x_status	x_thread_suspend(x_thread thread);
x_status	x_thread_yield(void);
x_status	x_thread_sleep(x_sleep timer_ticks);
x_status	x_thread_join(x_thread thread, void **result, x_sleep timeout);
x_status	x_thread_register(x_thread thread);
x_status	x_thread_unregister(x_thread thread);
x_status	x_thread_init(void);

x_thread	x_thread_current(void);

x_size		x_thread_priority_get(x_thread thread);
x_size		x_thread_quantum_get(x_thread thread);

x_int		x_map_priority(x_ubyte o_prio);

DWORD WINAPI	start_routine(void *thread_argument);

void x_scheduler_disable(void);
void x_scheduler_enable(void);

/*
** The OSwald API for Mutexes
*/

x_status x_mutex_create(x_mutex mutex);
x_status x_mutex_delete(x_mutex mutex);
x_status x_mutex_lock(x_mutex mutex, x_sleep timeout);
x_status x_mutex_unlock(x_mutex mutex);
x_status x_mutex_release(x_mutex mutex);


/*
** The OSwald API for Monitors
*/

x_status x_monitor_create(x_monitor);
x_status x_monitor_delete(x_monitor);
x_status x_monitor_enter(x_monitor, x_sleep);
x_status x_monitor_exit(x_monitor);
x_status x_monitor_wait(x_monitor, x_sleep);
x_status x_monitor_notify(x_monitor);
x_status x_monitor_notify_all(x_monitor);
x_status x_thread_stop_waiting(x_thread);


/*
** The Oswald API for Memory.
*/


x_status	x_mem_lock(x_sleep timeout);
x_status	x_mem_unlock(void);

void		x_mem_init(x_ubyte *start);

void *		_x_mem_alloc(x_size bytes, const char *file, int line);
void *		_x_mem_calloc(x_size bytes, const char *file, int line);

#define		x_mem_alloc(bytes)	_x_mem_alloc((bytes), __FILE__,__LINE__)
#define		x_mem_calloc(bytes)	_x_mem_calloc((bytes), __FILE__,__LINE__)

void		x_mem_free(void*);
void *		x_mem_realloc(void *old, x_size size);

x_status	x_mem_walk(x_sleep timeout, void (*callback)(void * mem, void * arg), void *arg);
x_size		x_mem_total(void);

#define		x_mem_gettotal(m) x_mem_total(m)

x_size		x_mem_avail(void);
x_status	x_mem_tag_set(void * mem, x_word tag);
x_word		x_mem_tag_get(void * mem);
x_size		x_mem_size(void * mem);
void		x_mem_discard(void * block);
x_status	x_mem_collect(x_size * bytes, x_size * num);
x_boolean	x_mem_is_block(void * mem);

void *		x_alloc_static_mem(void * memory, x_size size);


/*
** The OSwald API for QUEUES
*/

x_status x_queue_create(x_queue queue, void *messages, x_size capacity);
x_status x_queue_delete(x_queue queue);
x_status x_queue_receive(x_queue queue, void **msg, x_sleep owait);
x_status x_queue_send(x_queue queue, void *msg, x_sleep wait);
x_status x_queue_flush(x_queue queue, void(*do_this)(void *data));


/*
** The OSwald API for Semaphores
*/

x_status x_sem_create(x_Semaphore *semaphore, x_size initial_count);
x_status x_sem_put(x_Semaphore *semaphore);
x_status x_sem_get(x_Semaphore *semaphore, x_sleep owait);
x_status x_sem_delete(x_Semaphore *semaphore);


/*
** An OSwald API for Condition Variables
*/

x_status x_cond_init(x_cond_t * condition_variable, x_mutex * x_cond_mutex);
x_status x_cond_destroy(x_cond_t * condition_variable);
x_status x_cond_wait(x_cond_t * condition_variable, x_mutex * x_cond_mutex);
x_status x_cond_timed_wait(x_cond_t * condition_variable, x_mutex * x_cond_mutex, x_sleep timeout);
x_status x_cond_broadcast(x_cond_t * condition_variable);
x_status x_cond_signal(x_cond_t * condition_variable);


/*
** Don't use the memory monitor directly: always call x_memory_lock/unlock.
** We make the monitor visible here for debugging purposes only.
*/
extern x_monitor memory_monitor;

#endif
