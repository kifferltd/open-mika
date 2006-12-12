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

