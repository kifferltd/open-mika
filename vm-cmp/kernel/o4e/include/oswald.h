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


//ecos priorities are between 1 and 32
#define NUM_PRIORITIES     32  

//Possible values for the `flags` argument of x_thread_create
#define TF_START	  0x00000000
#define TF_SUSPENDED	  0x00000200
#define TF_JOIN_ENDED         0x00001000 /* Thread we joined with has ended normally. */
#define TF_JOIN_EXIT          0x00002000 /* Thread we joined with has called x_thread_exit. */
#define TF_JOIN_DELETED       0x00004000 /* Thread we joined with has been suspended and then deleted. */
#define x_no_wait	0
#define x_eternal 0xffffffff
#define isNotSet(x, flag)           (!isSet((x), (flag)))
#define unsetFlag(x, flag)        ((x) &= ~(flag))

#include "o4e.h"
#include "xmisc.h"
#include "vsprintf.h"

/*
** Oswald API for threads that we are implementing
*/

x_status x_thread_create(x_thread thread, x_entry entry_function, void* entry_input, void *stack_start, x_size stack_size, x_size priority, x_word flags);
x_status x_thread_delete(x_thread thread);
x_size x_thread_priority_get(x_thread thread);
x_status x_thread_priority_set(x_thread thread, x_size new_priority);
x_status x_thread_resume(x_thread thread);
x_status x_thread_sleep(x_sleep timer_ticks);
x_status x_thread_suspend(x_thread thread);
x_status x_thread_join(x_thread thread, void **result, x_sleep timeout);

x_thread x_thread_current(void);
void x_thread_yield(void);

/*  These functions lock/unlock the scheduler, it's implemented in eCos so
 *  we just need to map it.
 */
void x_scheduler_disable(void);
void x_scheduler_enable(void);


/*  Semaphores
 */
x_status x_sem_create(x_Semaphore *semaphore, x_size initial_count);
x_status x_sem_delete(x_Semaphore *semaphore);
x_status x_sem_get(x_Semaphore *semaphore, x_sleep wait_option);
x_status x_sem_put(x_Semaphore *semaphore);

/*  Mutexes
 */
x_status x_mutex_create(x_Mutex *mutex);
x_status x_mutex_delete(x_Mutex *mutex);
x_status x_mutex_lock(x_Mutex *mutex, x_sleep timeout);
x_status x_mutex_unlock(x_Mutex *mutex);


/*  Queues
 *
 * We set the queue's to a size of 256, in the configtool. eCos doesn't support
 * this dynamically. This is the maximum value given in wonka (in ./wonka/src/vm/thread.c:)
 */
x_status x_queue_create(x_Queue *queue, void *queue_start, x_size queue_size);
x_status x_queue_delete(x_Queue *queue);
x_status x_queue_receive(x_Queue *queue, void **data, x_sleep wait);
x_status x_queue_send(x_Queue *queue, void *src_msg, x_sleep wait);
x_status x_queue_flush(x_Queue *queue, void(*do_this)(void *data));

/*
** For the time being we have our own implementation of x_Monitor and the
** x_monitor_... primitives.
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
** The Oswald memory API.
*/
x_status x_mem_lock(x_sleep timeout);
x_status x_mem_unlock(void);

void* x_mem_alloc(x_size size);
void* x_mem_calloc(x_size size);
void x_mem_free(void*);
void *x_mem_realloc(void *old, x_size size);

x_status x_mem_walk(x_sleep timeout, void (*callback)(void * mem, void * arg), void *arg);

x_size x_mem_total(void);
x_size x_mem_gettotal(void);
#define x_mem_total	x_mem_gettotal
x_size x_mem_avail(void);

x_status x_mem_tag_set(void * mem, x_word tag);
x_word x_mem_tag_get(void * mem);
x_size x_mem_size(void * mem);
void x_mem_discard(void * block);
x_status x_mem_collect(x_size * bytes, x_size * num);
x_boolean x_mem_is_block(void * mem);

void* x_alloc_static_mem(void * memory, x_size size);

/*
** The Oswald time API (part of).
*/
x_sleep x_time_get(void);

x_size x_millis2ticks(x_size millis);
x_size x_seconds2ticks(x_size seconds);
x_size x_ticks2usecs(x_size ticks);
x_size x_usecs2ticks(x_size usecs);

x_status x_oswald_init(x_size requested_heap, x_size msec);

/*
** Don't use the memory monitor directly: always call x_memory_lock/unlock.
** We make the monitor visible here for debugging purposes only.
*/
extern x_monitor memory_monitor;

#endif /* _OSWALD_H */
