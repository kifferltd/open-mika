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
