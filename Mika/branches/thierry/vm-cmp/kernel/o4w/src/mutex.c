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
** $Id: mutex.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <windows.h>
#include "oswald.h"

/*
** Create a mutex.
*/

x_status x_mutex_create(x_mutex mutex) {

	x_status status = xs_success;

	loempa(7, "Creating a new mutex\n");
	if (mutex == NULL) {
		loempa(9, "  +->Bad, bad, boy! You gave me a non existing mutex!!!\n");
		return xs_deleted;
	}

	mutex->locked = 0;
	mutex->deleted = 0;
	mutex->owner = x_thread_current();

	mutex->w_hmutex = CreateMutex(NULL, FALSE, NULL);
	if(mutex->w_hmutex == NULL) {
		loempa(9, "  +->Mutex %d created with errorcode %d...\n", mutex->owner->id, GetLastError());
		return xs_unknown;
	}
	
	loempa(5,"  +->Mutex %d created successfully...\n", mutex->owner->id);
	return status;
}


/*
** Delete a mutex.
*/

x_status x_mutex_delete(x_mutex mutex) {

	x_boolean releaseError = 0;
	x_boolean closeError = 0;

	loempa(7, "Deleting a mutex\n");
	if(mutex == NULL) {
		loempa(9, "  +->Bad, bad, boy! You gave me a non existing mutex!!!\n");
		return xs_deleted;
	}

	if( mutex->owner != x_thread_current() ) {
		loempa(9,"  +->This thread %d may not delete the mutex %d\n", x_thread_current()->id, mutex->owner->id);
		return xs_not_owner;
	}
	else if ( (mutex->locked == 1) && (mutex->owner == x_thread_current()) ) { 
		mutex->deleted = 1;
		releaseError = ReleaseMutex(mutex->w_hmutex);
		if(releaseError == 0) {
			loempa(9, "  +->Mutex %d released with errorcode %d...\n", mutex->owner->id, GetLastError());
			return xs_unknown;
		}

		closeError = CloseHandle(mutex->w_hmutex);
		if(closeError == 0) {
			loempa(9, "  +->Mutex %d deleted with errorcode %d...\n", mutex->owner->id, GetLastError());
			return xs_unknown;
		}

		loempa(5,"  +->Mutex %d has been unlocked and deleted\n", mutex->owner->id);
		return xs_success;
	}
	else {
		mutex->deleted = 1;
		closeError = CloseHandle(mutex->w_hmutex);
		if(closeError == 0) {
			loempa(9, "  +->Mutex %d deleted with errorcode %d...\n", mutex->owner->id, GetLastError());
			return xs_unknown;
		}
		loempa(5,"  +->Mutex %d has been deleted successfully...\n", mutex->owner->id);
		return xs_success;
	}
}

/*
** Lock a mutex within a certain time window.
*/

x_status x_mutex_lock(x_mutex mutex, x_sleep timeout) {

	DWORD event;
	x_status status;

	loempa(7, "Locking a mutex\n");
	if(mutex == NULL) {
		loempa(9, "  +->Bad, bad, boy! You gave me a non existing mutex!!!\n");
		return xs_deleted;
	}
	
	if(mutex->deleted == 1)	{
		loempa(5,"  +->Mutex %d was deleted, it can't be locked\n", mutex->owner->id);
		return xs_deleted;
	}
	else{
		event = WaitForSingleObject(
			mutex->w_hmutex,		// handle to object to wait for
			(DWORD) timeout			// time-out interval in milliseconds
			); 

		switch(event) {

		case WAIT_ABANDONED:	/* no such thread */
			status = xs_bad_state;
			loempa(5,"  +->The specified object is a mutex object that was not released by the thread that owned the mutex object before the owning thread terminated!\n");
			break;
		case WAIT_OBJECT_0:		/* detached, or other joiner */
			status = xs_success;
			loempa(5,"  +->The state of the specified object is signaled!\n");
			break;
		case WAIT_TIMEOUT:		/* is self */
			status = xs_deadlock;
			loempa(5,"  +->The time-out interval elapsed!\n");
			break;
		default:
			status = xs_unknown;
		}
	}

	return status;
}

/*
** Unlock a mutex...
*/

x_status x_mutex_unlock(x_mutex mutex) {

	x_boolean releaseError = 0;

	loempa(7, "Unlocking a mutex\n");
	if (mutex->owner->w_thread == 0) {
		loempa(9, "  +->Bad, bad, boy! You gave me a non existing mutex!!!\n");
		return xs_deleted;
	}

	releaseError = ReleaseMutex(mutex->w_hmutex);
	if(releaseError == 0) {
		loempa(9, "  +->Mutex %d released with errorcode %d...\n", mutex->owner->id, GetLastError());
		return xs_unknown;
	}

	loempa(5,"  +->Mutex %d unlocked successfully...\n", mutex->owner->id);
	return xs_success;
}

/*
** Force release of a mutex by a thread that is not the owner. For callbacks.
*/

x_status x_mutex_release(x_mutex mutex) {

	x_boolean releaseError = 0;

	loempa(7, "Release mutex %d\n", mutex->owner->w_thread);
	if (mutex->owner->w_thread == 0) {
		loempa(9, "  +->Bad, bad, boy! You gave me a non existing mutex!!!\n");
		return xs_deleted;
	}

	releaseError = ReleaseMutex(mutex->w_hmutex);
	if(releaseError == 0) {
		loempa(9, "  +->Mutex %d released with errorcode %d...\n", mutex->owner->id, GetLastError());
		return xs_unknown;
	}

	loempa(5,"  +->Mutex %d released successfully...\n", mutex->owner->id);
	return xs_success;
}

