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
** $Id: monitor.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <windows.h>
#include <stdlib.h>
#include "oswald.h"

/*
** Create a monitor, the locks and the condition variables
*/

x_status x_monitor_create(x_monitor monitor) {

	loempa(7,"Creating a monitor\n");
	if(monitor == NULL || monitor == 0) {
		loempa(9, "  +->Bad, bad, boy! You gave me a non existing monitor!!!\n");
		return xs_bad_argument;
	}

	monitor->owner		= NULL;
	monitor->count		= 0;
	monitor->status		= MONITOR_READY;

	monitor->enterMutex = malloc(sizeof(x_Mutex));
	monitor->condMutex = malloc(sizeof(x_Mutex));

	x_mutex_create(monitor->condMutex);
	x_mutex_create(monitor->enterMutex);

	x_cond_init(&monitor->condCond, &monitor->condMutex);
	x_cond_init(&monitor->enterCond, &monitor->condMutex);

	loempa(5, "  +->Monitor created successfully...\n");
	return xs_success;
}

/*
** Delete the monitor
*/

x_status x_monitor_delete(x_monitor monitor) {

	x_thread current = x_thread_current();

	loempa(7,"Deleting a monitor\n");
	if(monitor == NULL || monitor == 0) {
		loempa(9, "  +->Bad, bad, boy! You gave me a non existing monitor!!!\n");
		return xs_bad_argument;
	}

	if ((monitor->owner != current) && (monitor->owner != NULL)) {
		loempa(9, "  +->Refuse to delete monitor at %p: owned by %p, caller is %p\n", monitor, monitor->owner, current);
		return xs_not_owner;
	}

	if (monitor->status == MONITOR_DELETED) {
		loempa(9, "  +->Refuse to delete monitor at %p: already deleted\n", monitor);
		return xs_deleted;
	}

	x_cond_destroy(&monitor->condCond);
	x_cond_destroy(&monitor->enterCond);

	x_mutex_delete(monitor->condMutex);
	x_mutex_delete(monitor->enterMutex);
	
	monitor->status = MONITOR_DELETED;

	loempa(5, "  +->Monitor at %p deleted\n", monitor);
	return xs_success;
}

/*
** Enter the monitor
*/

x_status x_monitor_enter(x_monitor monitor, x_sleep timeout) {
	
	DWORD event = 0;
	x_thread current = x_thread_current();

	loempa(7, "Thread is entering a monitor\n");
	if(monitor == NULL || monitor == 0) {
		loempa(9, "  +->Bad, bad, boy! You gave me a non existing monitor!!!\n");
		return xs_bad_argument;
	}

	if (monitor->owner == current) {
		monitor->count += 1;
		loempa(9, "  +->Thread %d already owns monitor %p, count now %d\n", current->w_thread, monitor->owner->w_thread, monitor->count);
	}
	else if (monitor->owner == NULL) {
		if (x_mutex_lock(monitor->condMutex, x_eternal) != xs_success) {
			loempa(9, "  +->We did not get the authority to enter: %d, errorCode: %d...\n", event, GetLastError());
			return xs_unknown;
		}
		monitor->owner = current;
		monitor->count = 1;
	}
	else {
		if (timeout == x_no_wait) {
			loempa(5, "  +->No waiting time is set...\n");
			if (x_mutex_lock(monitor->condMutex, 0) != xs_success) {
				loempa(9, "  +->We did not get the authority to enter: %d, errorCode: %d...\n", event, GetLastError());
				return xs_no_instance;
			}
			else {
				loempa(5,"  +->We entered the monitor, it was successfull!\n");
			}
		}
		else if (timeout == x_eternal) {
			loempa(5, "  +->Eternal wait is set...\n");
			if (x_mutex_lock(monitor->condMutex, x_eternal) != xs_success) {
				loempa(9, "  +->We did not get the authority to enter: %d, errorCode: %d...\n", event, GetLastError());
				return xs_no_instance;
			}
			loempa(5, "  +->We got the mutex while entering the monitor...\n");
		}
		else {
			loempa(5,"  +->Trying to enter monitor with timeout %d \n",timeout);
			x_mutex_lock(monitor->enterMutex, x_eternal);
			if (x_cond_timed_wait(&monitor->enterCond, NULL, timeout) != xs_success){
				if(x_mutex_lock(monitor->condMutex, 0) == WAIT_OBJECT_0){
					loempa(5,"  +->Obtained monitor within timeout %d\n", timeout);
					x_mutex_unlock(monitor->enterMutex);
				}
				else{
					loempa(5,"  +->x_monitor_enter: timeout due to fail trylock!! \n");
					x_mutex_unlock(monitor->enterMutex);
					return xs_no_instance;
				}
			}
			else {
				loempa(5, "x_monitor_enter: timeout!!\n");
				x_mutex_unlock(monitor->enterMutex);
				return xs_no_instance;
			}
		}
		monitor->owner = current;
		monitor->count = 1;
		loempa(5, "  +->Thread %p has obtained monitor %p, count = 1\n", current->w_thread, monitor->owner->w_thread);
	}

	return xs_success;
}

/*
** Exit the monitor
*/

x_status x_monitor_exit(x_monitor monitor) {

	x_thread current_thread = x_thread_current();

	loempa(7, "Thread is leaving a monitor\n");
	if(monitor == NULL || monitor == 0) {
		loempa(9, "  +->Bad, bad, boy! You gave me a non existing monitor!!!\n");
		return xs_bad_argument;
	}
	
	if (monitor->owner != current_thread) {
		loempa(9, "  +->Thread %d cannot leave the monitor, it is not the owner...\n", current_thread->w_thread);
		return xs_not_owner;
	}

	monitor->count -= 1;
	if (monitor->count == 0) {
		monitor->owner = NULL;
		x_mutex_unlock(monitor->condMutex);
		x_mutex_lock(monitor->enterMutex, x_eternal);
		x_cond_signal(&monitor->enterCond);
		x_mutex_unlock(monitor->enterMutex);
		loempa(5, "  +->Thread %d no longer owns the monitor, count now 0\n", current_thread->w_thread);
	}
	else {
		loempa(5, "  +->Thread %d still owns the monitor, count now %d\n", current_thread->w_thread, monitor->count);
	}

	return xs_success;
}

/*
** The wait function of the monitor
*/

x_status x_monitor_wait(x_monitor monitor, x_sleep timeout) {

	x_thread current_thread = x_thread_current();


	if(monitor == NULL || monitor == 0) {
		loempa(9, "  +->Bad, bad, boy! You gave me a non existing monitor!!!\n");
		return xs_bad_argument;
	}

	if (monitor->owner != current_thread) {
		loempa(9, "  +->Thread %d is not allowed to wait on monitor %d\n", current_thread->w_thread, monitor->owner->w_thread);
		return xs_not_owner;
	}

	loempa(5, "  +->Thread %d is waiting on monitor %d, has count %i\n", current_thread->w_thread, monitor->owner->w_thread, monitor->count);

	current_thread->waiting_on = monitor;
	current_thread->waiting_with = monitor->count;
    
	monitor->owner = NULL;
	monitor->count = 0;

	x_cond_signal(&monitor->enterCond);

	if(timeout == x_eternal) {
		x_cond_wait(&monitor->condCond, &monitor->condMutex);
		loempa(5,"  +->Infinite wait on monitor %d has ended...\n", monitor);
	}
	else{
		if(x_cond_timed_wait(&monitor->condCond, NULL, timeout) == xs_deadlock) {
			loempa(9,"  +->Waiting on monitor %d failed, a timeout occured...\n", monitor);
			return xs_no_instance;
		}
	}
	
	if(monitor->owner == NULL) {			// check if nobody else is the owner in case of the stop_waiting call
		monitor->owner = current_thread;
		monitor->count = current_thread->waiting_with;
		current_thread->waiting_on = NULL;
		current_thread->waiting_with = 0;
		loempa(5, "  +->Thread %d has re-acquired monitor %d\n", current_thread->w_thread, monitor->owner->w_thread);
		return xs_success;
	}
	else{		// thread has been given the call x_thread_stop_waiting() and someone else already owns the monitor;
		loempa(9,"  +->Thread %d has stopt waiting on monitor %d",current_thread->w_thread, monitor);	
		x_mutex_unlock(monitor->condMutex);
		return xs_no_instance;
	}
}

/*
** Notify just one thread (monitor)
*/

x_status x_monitor_notify(x_monitor monitor) {

	x_thread current_thread = x_thread_current();

	if(monitor == NULL || monitor == 0) {
		loempa(9, "  +->Bad, bad, boy! You gave me a non existing monitor!!!\n");
		return xs_bad_argument;
	}

	if (monitor->owner != current_thread) {
		loempa(9, "  +->Thread %d is not allowed to notify the monitor...\n", current_thread->w_thread);
		return xs_not_owner;
	}

	x_cond_signal(&monitor->condCond);

	loempa(5,"  +->One thread notified on monitor %d...\n", monitor);	//	=> signals the first that was waiting 
	
	return xs_success;
}

/*
** send a broadcast to all the threads that are waiting
*/

x_status x_monitor_notify_all(x_monitor monitor) {

	x_status status = xs_success;
	x_thread current_thread = x_thread_current();

	loempa(7,"Notify all threads...\n");

	if(monitor == NULL || monitor == 0) {
		loempa(9, "  +->Bad, bad, boy! You gave me a non existing monitor!!!\n");
		return xs_bad_argument;
	}

	if (monitor->owner != current_thread) {
		loempa(9, "  +->Thread %d is not allowed to notify the monitor...\n", current_thread->w_thread);
		return xs_not_owner;
	}

	x_cond_broadcast(&monitor->condCond);
	loempa(5,"  +->All threads notified on monitor %d...\n",monitor);
	
	return status;
}

/*
** Tell the thread to stop waiting
*/

x_status x_thread_stop_waiting(x_thread thread) {
	x_monitor monitor;

	if(thread == NULL || thread == 0) {
		loempa(9, "  +->Bad, bad, boy! You gave me a non existing monitor!!!\n");
		return xs_bad_argument;
	}

	monitor = thread->waiting_on;

	if (monitor != NULL) {
		loempa(5, "  +->Thread %d will stop waiting on monitor %d\n", thread->w_thread, monitor);

		x_thread_delete(thread);
		loempa(5, "  +->Thread %d has stopt waiting on monitor %d\n", thread->w_thread, monitor);
		return xs_success;
	}
	else{
		loempa(9,"  +->Thread %d wasn`t waiting on a monitor, so why stop waiting?\n", thread->w_thread);
		return xs_no_instance;
	}
}
