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
** $Id: scheduler.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <windows.h>
#include <malloc.h>
#include <stdlib.h>
#include "oswald.h"

x_thread main_thread;
extern O4wEnv * o4wenv;

/*
** When we started the program, we have also started a thread.
** This thread will be the first element of our ll.
*/

x_status x_thread_init() {

	main_thread = malloc(sizeof(x_Thread));

	main_thread->is_main_thread = TRUE;
	main_thread->c_prio = 3;
	main_thread->c_quantums = 100;
	main_thread->id = GetCurrentThreadId();
	main_thread->just_created = 1;
	main_thread->just_deleted = 0;
	main_thread->next = NULL;
	main_thread->previous = NULL;
	main_thread->state = xt_ready;
	main_thread->w_thread = GetCurrentThreadId();
	main_thread->w_hthread = GetCurrentThread();

	list_init(main_thread);
	InitializeCriticalSection(&main_thread->w_critical_section);

	return xs_success;
}

/*
** Just create a Windows thread
*/

x_status x_thread_create(x_thread thread, x_entry entry, void *argument, x_ubyte *b_stack, x_size s_stack, x_size prio, x_flags flags) {
	
	thread->just_created = 1;
	thread->just_deleted = 0;
	thread->is_main_thread = FALSE;

	loempa(7, "Creating a thread with a priority of %d\n", prio);

	/*
	** We first do some checks and return if any fails.
	*/

	if( (thread == NULL) || (entry == NULL) ) {
		if (thread == NULL) {
			loempa(9, "  +->Error: Creating a thread and thread is null...\n");
			return xs_unknown;
		}
		if (entry == NULL) {
			loempa(9, "  +->Error: Creating a thread and entry is null...\n");
			return xs_unknown;
		}
		return xs_bad_argument;
	}
	
	if (prio > MAX_PRIORITY || prio < MIN_PRIORITY) {
		loempa(9, "  +->Error: Creating a thread and the thread priority is %d...\n", prio);
		return xs_bad_argument;
	}
	
	if (isNotSet(flags, TF_TIMER)) {
		
		/*
		** Only TF_SUSPENDED or TF_START is allowed, never both
		*/
		if (isSet(flags, TF_SUSPENDED) && isSet(flags, TF_START)) {
			loempa(9, "  +->Error: Creating a thread and TF_SUSPENDED or TF_START are both set...%d\n", prio);
			return xs_bad_argument;
		}
		
		/*
		** Only TF_SUSPENDED or TF_START can be given, no other flag is acceptable
		*/
		
		if (flags & (x_word)(~(TF_SUSPENDED | TF_START))) {
			loempa(9, "  +->Error: Creating a thread and there is a flag set that is not acceptable...%d\n", prio);
			return xs_bad_argument;
		}

	}
	
	InitializeCriticalSection(&thread->w_critical_section);
	EnterCriticalSection(&thread->w_critical_section);

	//Setting the thread priority
	loempa(5, "  +->Initializing new thread priority...\n");
	if (prio > NUM_HARD_PRIORITIES) {
		thread->a_prio	 = prio;
		thread->c_prio	 = 63;
		thread->a_quantums = 4;
		thread->c_quantums = thread->a_quantums;
	}
	else {
		thread->a_prio	 = prio;
		thread->c_prio	 = thread->a_prio;
		thread->a_quantums = 4;
		thread->c_quantums = thread->a_quantums;
	}

	//Setting the other members
	loempa(5, "  +->Initializing thread environment...\n");
	thread->id		= o4wenv->num_threads;
	thread->entry		= entry;
	thread->argument	= argument;
	thread->flags		= flags;
	
	thread->waiting_on	= NULL;		// thread is waiting on null monitors
	thread->waiting_with	= 0;		// thread hasn't entered any monitors

	if (isSet(flags, TF_START)) {
		loempa(5, "  +->Start the thread in start mode...\n");
		thread->state = xt_ready;
	}
	else {
		loempa(5, "  +->Start the thread in suspend mode...\n");
		thread->state = xt_suspended;
		setFlag(thread->flags, TF_COUNT_ONE);
	}
	
	//Create the thread
	loempa(5, "  +->Creating the new thread...\n");

	thread->w_hthread = CreateThread(
		NULL,							// pointer to security attributes
		0,							// initial thread stack size
		start_routine,				// pointer to thread function
		thread,						// argument for new thread
		CREATE_SUSPENDED,				// creation flags
		&thread->w_thread				// pointer to receive thread ID
		);

	//Was there an error while creating the thread...
	if(thread->w_hthread == NULL) {
		loempa(9, "  +->Thread %d created with errorcode %d...\n", thread->w_thread, GetLastError());
		return xs_bad_state;
	}

	//Setting the thread priority
	x_thread_priority_set(thread, thread->c_prio);

	//Setting the thread quantum
	x_thread_quantum_set(thread, thread->c_quantums);

	//Resume the thread if TF_START was set...
	if(thread->state == xt_ready) {
		DWORD resumeError = 0;
		resumeError = ResumeThread(thread->w_hthread);
		if(resumeError == 0xFFFFFFFF) {
			loempa(9, "  +->New thread %d resumed with errorcode %d...\n", thread->w_thread, GetLastError());
			return xs_unknown;
		}
	}

	LeaveCriticalSection(&thread->w_critical_section);
	return xs_success;
}


DWORD WINAPI start_routine(void *thread_argument) {

	x_thread thread;

	//Executing the thread
	thread = (x_thread) thread_argument;
	thread->id = GetCurrentThreadId();

	loempa(7, "We are in the start routine, thread %d has been started\n", thread->id);

	x_thread_register(thread);
	(*(x_entry)thread->entry)(thread->argument);
	x_thread_unregister(thread);

	//Signal everyone that the thread has ended...
	thread->state = xt_ended;
	PulseEvent(thread->w_hthread);

	loempa(7,"The start routine has been ended, the thread %d is ready for clean up\n", thread->id);
	return 0x15;
}


/*
** Registers a thread
*/

x_status x_thread_register(x_thread thread){

	loempa(5, "  +->Registering thread %d ...\n", thread->id);

	//Register the thread
	EnterCriticalSection(&o4wenv->w_critical_section);
	list_insert(main_thread, thread);
	thread->is_registered = TRUE;
	o4wenv->num_threads += 1;
	LeaveCriticalSection(&o4wenv->w_critical_section);

	return xs_success;
}


/*
** Unregisters a thread
*/

x_status x_thread_unregister(x_thread thread) {

	loempa(5, "  +->UnRegistering thread...\n");
	//Clean up the thread
	EnterCriticalSection(&o4wenv->w_critical_section);
	thread->just_deleted = 1;
	if (thread->is_registered == TRUE)
		list_remove(thread);
	o4wenv->num_threads -= 1;
	LeaveCriticalSection(&o4wenv->w_critical_section);

	return xs_success;
}

/*
** Get the current thread (get it from the ll)
*/

x_thread x_thread_current() {

	int finito = 0;
	x_thread c_thread;

	loempa(7,"Getting the current thread %d...\n", GetCurrentThreadId());

	//Looking in the list for the current thread
	for( c_thread = main_thread; (c_thread->w_thread != GetCurrentThreadId()) && (finito == 0); c_thread = c_thread->previous) {
		if(c_thread->previous == main_thread)
			finito = 1;
	}

	if (c_thread == NULL || finito == 1) {
		loempa(9, "  +->Aiaiai, could't get the current thread!!!\n");
		exit(0);
	}

	return c_thread;
}

/*
** Yield the current thread
** Windows has no function to yield a thread, but putting it to sleep
** for milliseconds has the same effect
*/

x_status x_thread_yield() {

	x_thread thread = (x_thread) x_thread_current();
	loempa(7, "Yield thread %d (Putting thread to sleep for 0 millis)\n", thread->id);

	//Does the thread exists or was it cancelled?
	if (thread != NULL && thread != 0 && thread->just_deleted != 1) {
		EnterCriticalSection(&thread->w_critical_section);
		thread->state = xt_sleeping;
		Sleep(0);
		thread->state = xt_ready;
		LeaveCriticalSection(&thread->w_critical_section);
	}
	else {
		loempa(9, "  +->Thread %d has not been put in the yield state!\n", thread->id);
		return xs_bad_state;
	}
	loempa(5, "  +->Thread %d returning back to reality!\n", thread->w_thread);

	return xs_success;
}

/*
** Delete this thread (if it wasn't already terminated!)
*/

x_status x_thread_delete(x_thread thread) {

	DWORD lpExitCode = 0;

	loempa(7, "Deleting a thread\n");
	EnterCriticalSection(&o4wenv->w_critical_section);

	if (thread == 0 || thread == NULL || thread->just_deleted == 1) {
		loempa(9, "  +->Bad, bad, boy! You gave me a non existing thread!!!\n");
		LeaveCriticalSection(&o4wenv->w_critical_section);
		return xs_bad_state;
	}

	else {
		EnterCriticalSection(&thread->w_critical_section);

		/*
		** Check if we are calling ourselves, if yes, 
		** return an error since this is impossible.
		*/

		if ( (GetCurrentThreadId() != thread->w_thread) && (thread->state == xt_suspended || thread->state == xt_ended) ) {
			loempa(9, "  +->Error deleting thread, we are in a bad state!\n");
			LeaveCriticalSection(&thread->w_critical_section);
			LeaveCriticalSection(&o4wenv->w_critical_section);
			return xs_unknown;
		}
		else {

			x_boolean exitCodeError = 0;
			x_boolean terminateError = 0;

			//We must first get the exit code of the thread we want to delete
			exitCodeError = GetExitCodeThread(thread->w_hthread, &lpExitCode);
			if(exitCodeError == 0) {
				loempa(9, "  +->Got lpExitCode for thread %d with errorcode %d...\n", thread->id, GetLastError());
				LeaveCriticalSection(&thread->w_critical_section);
				LeaveCriticalSection(&o4wenv->w_critical_section);
				return xs_bad_state;
			}
			loempa(7, "  +->Got the lpExitCode for thread %d: %d\n", thread->w_thread, lpExitCode);

			//Now terminate the thread
			terminateError = TerminateThread(thread->w_hthread, lpExitCode);
			if(terminateError == 0) {
				loempa(9, "  +->TerminateThread %d with errorcode %d...\n", thread->w_thread, GetLastError());
				LeaveCriticalSection(&thread->w_critical_section);
				LeaveCriticalSection(&o4wenv->w_critical_section);
				return xs_bad_state;
			}
			else{
				thread->state = xt_ended;
				o4wenv->num_deleted += 1;
				LeaveCriticalSection(&thread->w_critical_section);
				LeaveCriticalSection(&o4wenv->w_critical_section);
				DeleteCriticalSection(&thread->w_critical_section);
				x_thread_unregister(thread);
				loempa(5,"  +->Thread successfully deleted...\n");		// signal everybody that the thread was stopped
				return xs_success;
			}
		}
		LeaveCriticalSection(&thread->w_critical_section);
	}
	LeaveCriticalSection(&o4wenv->w_critical_section);
	return xs_success;
}

/*
** Set the priority of the thread
*/

x_status x_thread_priority_set(x_thread thread, x_size new_priority) {

	x_int nPriority = 0;
	x_boolean setError = 0;
	loempa(7,"Setting the priority of a thread\n");

	if (thread == 0 || thread == NULL || thread->just_deleted == 1) {
		loempa(9, "  +->Bad, bad, boy! You gave me a non existing thread!!!\n");
		return xs_unknown;
	}
	EnterCriticalSection(&thread->w_critical_section);

	//Is the new priority valid?
	if (new_priority < MAX_PRIORITY && new_priority >= MIN_PRIORITY) {
		thread->a_prio = new_priority;
		if ((thread->c_prio != new_priority) || thread->just_created == 1) {
			thread->just_created = 0;
			
			nPriority = x_map_priority(thread->c_prio);
			
			setError = SetThreadPriority(
				thread->w_hthread,	// handle to the thread
				nPriority			// thread priority level
				); 

			if (setError == 0) {
				loempa(5, "  +->We received this errorcode while setting a priority to thread %d: %d\n", thread->w_thread, GetLastError());
			}
		}
	}

	LeaveCriticalSection(&thread->w_critical_section);
	return xs_success;
}

/*
** Get the current priority
*/

x_size x_thread_priority_get(x_thread thread) {

	loempa(7,"Getting the priority of a thread\n");

	if (thread == 0 || thread == NULL || thread->just_deleted == 1) {
		loempa(9, "  +->Bad, bad, boy! You gave me a non existing thread!!!\n");
		return xs_unknown;
	}

	EnterCriticalSection(&thread->w_critical_section);
	loempa(5,"  +->Thread has a priority of %d\n", thread->c_prio);
	loempa(5,"  +->Thread has a windows-priority of %d\n", GetThreadPriority(thread->w_hthread));
	
	LeaveCriticalSection(&thread->w_critical_section);
	return thread->c_prio;
}

/*
** These functions are used to set and get the quantum time for a thread
** This is only supported under WindowsCE, and not under any other version.
** Therefore we do not map it! (Default is normally 100)
*/

x_status x_thread_quantum_set(x_thread thread, x_size new_quantum) {
  return xs_success;
}

x_size x_thread_quantum_get(x_thread thread) {
	return 100;
}

/*
** Suspend the thread
*/

x_status x_thread_suspend(x_thread thread) {

	DWORD suspendError = 0;
	loempa(7, "Suspending a thread\n");

	if (thread == 0 || thread == NULL || thread->just_deleted == 1) {
		loempa(9, "  +->Bad, bad, boy! You gave me a non existing thread!!!\n");
		return xs_bad_argument;
	}

	if(thread == x_thread_current()){
		//Create another thead to suspend the current thread, else the flags aren't up to date
		HANDLE c_thread;
		loempa(5, "  +->Thread is suspending itself!\n");
		c_thread = CreateThread(
			NULL,							// pointer to security attributes
			0,								// initial thread stack size
			suspend_self,					// pointer to thread function
			thread,							// argument for new thread
			0,								// creation flags
			NULL							// pointer to receive thread ID
			);
	}
	else {
		// thread is suspending another thread 
		if (thread->state != xt_suspended) {
			EnterCriticalSection(&thread->w_critical_section);

			thread->state = xt_suspended;
			loempa(5,"  +->Thread %d suspended\n", thread->w_thread);
			suspendError = SuspendThread(thread->w_hthread);
			if (suspendError == 0xFFFFFFFF) {
				loempa(9, "  +->Thread %d suspended with errorcode %d...\n", thread->w_thread, GetLastError());
				LeaveCriticalSection(&thread->w_critical_section);
				return xs_bad_state;
			}
			LeaveCriticalSection(&thread->w_critical_section);
		}
		else{
			loempa(9,"  +->Thread %d already suspended...\n", thread->w_thread);
			return xs_no_instance;
		}
	}
	return xs_success;
}

/*
** The thread to suspend the current thread
*/

DWORD WINAPI suspend_self(void *thread_argument) {
	DWORD suspendError = 0;

    x_thread thread = (x_thread) thread_argument;
    EnterCriticalSection(&thread->w_critical_section);
	thread->state = xt_suspended;
	suspendError = SuspendThread(thread->w_hthread);
	if (suspendError == 0xFFFFFFFF) {
		loempa(9, "  +->Thread %d suspended with errorcode %d...\n", thread->w_thread, GetLastError());
		LeaveCriticalSection(&thread->w_critical_section);
		return xs_bad_state;
	}
    loempa(5, "  +->Thread %d suspended...\n", thread->id);    
    LeaveCriticalSection(&thread->w_critical_section);
	return 0x15;
}

/*
** Resume a thread
*/

x_status x_thread_resume(x_thread thread) {

	DWORD resumeError = 0;
	x_status status = xs_success;
	loempa(7, "Resuming a thread\n");

	if (thread == 0 || thread == NULL || thread->just_deleted == 1) {
		loempa(9, "  +->Bad, bad, boy! You gave me a non existing thread!!!\n");
		return xs_unknown;
	}

	EnterCriticalSection(&thread->w_critical_section);
	if (thread->state != xt_suspended){
		status = xs_no_instance;
		loempa(9,"  +->Thread %d not resumed because it was not suspended!\n", thread->id);
	}
	else{
		resumeError = ResumeThread(thread->w_hthread);
		if (resumeError == 0xFFFFFFFF) {
			loempa(9, "  +->Thread %d suspended with errorcode %d...\n", thread->w_thread, GetLastError());
			LeaveCriticalSection(&thread->w_critical_section);
			return xs_bad_state;
		}
		thread->state = xt_ready;
		loempa(5,"  +->Thread %d resumed successful\n", thread->id);
	}
	LeaveCriticalSection(&thread->w_critical_section);

	return status;
}

/*
** Put a thread to sleep
*/

x_status x_thread_sleep(x_sleep timer_ticks) {

	x_thread thread = (x_thread) x_thread_current();
	DWORD millis = x_ticks2usecs(timer_ticks) / 1000;

	loempa(7, "Putting thread %d to sleep for %d millis\n", thread->id, millis);

	EnterCriticalSection(&thread->w_critical_section);

	//Does the thread exists or was it cancelled?
	if (thread != 0 || thread != NULL || thread->just_deleted == 0) {
		thread->state = xt_sleeping;
		Sleep(millis);
		thread->state = xt_ready;
	}
	else {
		loempa(9, "  +->Thread has not been put to sleep!\n");
		return xs_bad_state;
	}
	loempa(5, "  +->Thread %d returning back to reality!\n", thread->id);
	LeaveCriticalSection(&thread->w_critical_section);

	return xs_success;
}

/*
** Create a join
*/

x_status x_thread_join(x_thread thread, void **result, x_sleep timeout) {
	
	DWORD event;
	x_status status;
	
	loempa(7, "Join with a thread, timeout = %d\n", timeout);

	if (thread == 0 || thread == NULL || thread->just_deleted == 1) {
		loempa(9, "  +->Bad, bad, boy! You gave me a non existing thread!!!\n");
		return xs_success;
	}

	EnterCriticalSection(&thread->w_critical_section);

	thread->state = xt_sleeping;
	event = WaitForSingleObject(
		thread->w_hthread,		// handle to object to wait for
		(DWORD) timeout			// time-out interval in milliseconds
		); 
	thread->state = xt_ready;

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
	thread->state = xt_ready;
	LeaveCriticalSection(&thread->w_critical_section);

	return status;
}

/*
** Map the oswald priority to the windows priority
*/

x_int x_map_priority(x_ubyte o_prio) {
	switch(o_prio) {
    case 0:
		return THREAD_PRIORITY_TIME_CRITICAL;
		break;
    case 1:
		return THREAD_PRIORITY_HIGHEST;
		break;
    case 2:
		return THREAD_PRIORITY_ABOVE_NORMAL;
		break;
    case 3:
		return THREAD_PRIORITY_NORMAL;
		break;
    case 4:
		return THREAD_PRIORITY_BELOW_NORMAL;
		break;
    case 5:
		return THREAD_PRIORITY_LOWEST;
		break;
    case 6:
		return THREAD_PRIORITY_IDLE;
		break;
    default:
		return THREAD_PRIORITY_IDLE;
		break;
	}

	loempa(7, "  +->We tried to map a thread priority, but it failed!");
	return THREAD_PRIORITY_NORMAL;
}

/*
** You can't enable or disable the Windows thread-scheduler
*/

void x_scheduler_disable() {
}

void x_scheduler_enable(){
}
