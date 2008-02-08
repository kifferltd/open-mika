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
** $Id: cond.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <windows.h>
#include "oswald.h"

x_status x_cond_init(x_cond_t * condition_variable, x_mutex * x_cond_mutex) {
	// Initialize the count to 0.
	condition_variable->waiters_count = 0;

	// Create an auto-reset event.
	condition_variable->events[SIGNAL] = CreateEvent (NULL,  // no security
		FALSE, // auto-reset event
		FALSE, // non-signaled initially
		NULL); // unnamed

	// Create a manual-reset event.
	condition_variable->events[BROADCAST] = CreateEvent (NULL,  // no security
		TRUE,  // manual-reset
		FALSE, // non-signaled initially
		NULL); // unnamed

	InitializeCriticalSection (&condition_variable->waiters_count_lock);

	return xs_success;
}

x_status x_cond_wait(x_cond_t * condition_variable, x_mutex * x_cond_mutex) {

	x_int result;
	x_int last_waiter;

	// Avoid race conditions.
	EnterCriticalSection (&condition_variable->waiters_count_lock);
	condition_variable->waiters_count++;
	LeaveCriticalSection (&condition_variable->waiters_count_lock);

	// It's ok to release the <x_cond_mutex> here since Win32
	// manual-reset events maintain state when used with
	// <SetEvent>.  This avoids the "lost wakeup" bug...
	x_mutex_unlock(*x_cond_mutex);

	// Wait for either event to become signaled due to <x_cond_signal>
	// being called or <x_cond_broadcast> being called.
	result = WaitForMultipleObjects(2, condition_variable->events, FALSE, x_eternal);

	EnterCriticalSection(&condition_variable->waiters_count_lock);
	condition_variable->waiters_count--;
	last_waiter =
		result == WAIT_OBJECT_0 + BROADCAST 
		&& condition_variable->waiters_count == 0;
	LeaveCriticalSection(&condition_variable->waiters_count_lock);

	// Some thread called <x_cond_broadcast>.
	if (last_waiter)
		// We're the last waiter to be notified or to stop waiting, so
		// reset the manual event. 
		ResetEvent (condition_variable->events[BROADCAST]); 

	// Reacquire the <x_cond_mutex>.
	x_mutex_lock(*x_cond_mutex, x_eternal);

	return xs_success;
}

x_status x_cond_broadcast(x_cond_t * condition_variable) {

	x_int have_waiters;

	// Avoid race conditions.
	EnterCriticalSection (&condition_variable->waiters_count_lock);
	have_waiters = condition_variable->waiters_count > 0;
	LeaveCriticalSection (&condition_variable->waiters_count_lock);

	if (have_waiters)
		SetEvent(condition_variable->events[BROADCAST]);

	return xs_success;
}

x_status x_cond_signal(x_cond_t * condition_variable) {

	x_int have_waiters;

	// Avoid race conditions.
	EnterCriticalSection (&condition_variable->waiters_count_lock);
	have_waiters = condition_variable->waiters_count > 0;
	LeaveCriticalSection (&condition_variable->waiters_count_lock);

	if (have_waiters)
		SetEvent (condition_variable->events[SIGNAL]);

	return xs_success;
}

x_status x_cond_destroy(x_cond_t * condition_variable) {

	condition_variable->waiters_count = 0;

	// Delete an auto-reset event.
	CloseHandle(condition_variable->events[SIGNAL]);

	// Delete a manual-reset event.
	CloseHandle(condition_variable->events[BROADCAST]);

	return xs_success;
}

x_status x_cond_timed_wait(x_cond_t * condition_variable, x_mutex * x_cond_mutex, x_sleep timeout) {
	x_int result;
	x_int last_waiter;

	// Avoid race conditions.
	EnterCriticalSection (&condition_variable->waiters_count_lock);
	condition_variable->waiters_count++;
	LeaveCriticalSection (&condition_variable->waiters_count_lock);

	// It's ok to release the <x_cond_mutex> here since Win32
	// manual-reset events maintain state when used with
	// <SetEvent>.  This avoids the "lost wakeup" bug...
	if(x_cond_mutex != NULL)
		x_mutex_unlock(*x_cond_mutex);

	// Wait for either event to become signaled due to <x_cond_signal>
	// being called or <x_cond_broadcast> being called.
	result = WaitForMultipleObjects(2, condition_variable->events, FALSE, timeout);

	EnterCriticalSection(&condition_variable->waiters_count_lock);
	condition_variable->waiters_count--;
	last_waiter =
		result == WAIT_OBJECT_0 + BROADCAST 
		&& condition_variable->waiters_count == 0;
	LeaveCriticalSection(&condition_variable->waiters_count_lock);

	// Some thread called <x_cond_broadcast>.
	if (last_waiter)
		// We're the last waiter to be notified or to stop waiting, so
		// reset the manual event. 
		ResetEvent (condition_variable->events[BROADCAST]); 


	if(x_cond_mutex != NULL)
		x_mutex_lock(*x_cond_mutex, x_eternal);

	if((WAIT_OBJECT_0 <= result) && (result <= (WAIT_OBJECT_0 + 2 - 1)))
		return xs_success;
	else if((WAIT_ABANDONED_0 <= result) && (result <= (WAIT_ABANDONED_0 + 2 - 1)))
		return xs_bad_state;
	else if(WAIT_TIMEOUT == result)
		return xs_deadlock;
	else
		return xs_unknown;

	return xs_unknown;
}
