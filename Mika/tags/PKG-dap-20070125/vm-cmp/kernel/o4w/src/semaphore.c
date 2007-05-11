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
** $Id: semaphore.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <stdlib.h>
#include <stdio.h>
#include <windows.h>
#include "o4w.h"
#include "oswald.h"

/*
 * Prototype:
 *   x_status x_sem_create(x_Semaphore *semaphore, x_size initial_count) {
 * Description (of counting semaphores):
 *   Creates a counting semaphore for inter-thread synchronization.
 *   The initial semaphore count is specified as an input parameter.
 *   A semaphore can be used to guard accesses to a resource, or,
 *   alternatively, just to let processes wait for something to
 *   happen.  The 'x_sem_get' and 'x_sem_put' operations
 *   basicaly decrement and increment an integer count associated with
 *   the semaphore (hence the name).  When a process waits on a
 *   semaphore whose count is positive, then the process just 
 *   continues, having decremented the semaphore count possibly to
 *   zero.  If the semaphore count is zero, then the waiting process
 *   is blocked (suspended) until another process posts the semaphore
 *   by incrementing the semaphore count.
 */ 

x_Semaphore *reference_sem=NULL;
char naam[20];
int id;
DWORD aantal_semaphores = 1000;
DWORD max_aantal_semaphores = 2000;
x_status x_sem_create(x_Semaphore *semaphore, x_size initial_count) {
	InitializeCriticalSection(&semaphore->w_critical_section);
	__try {

		EnterCriticalSection(&semaphore->w_critical_section);
		loempa(7,"o4w - semaphore - create begin\n");
		if(reference_sem == NULL)
		id=0;
		
			//semaphore->id = 0;
		loempa(5,"o4w - semaphore - created %d\n",&semaphore->w_hEvent);
		//semaphore->id++;
			id++;
		semaphore->id=id;
		
		loempa(9,"o4w - semaphore - created %d\n",semaphore->id);
		sprintf(naam, "%i", semaphore->id); /* write the double to a string */
		loempa(5,"o4w - semaphore - created %s\n",naam);
		loempa(5,"o4w - semaphore - created %d\n",initial_count);
		SetLastError(0);
		loempa(5,"o4w - semaphore - created %d\n",initial_count);
		sprintf(semaphore->naam, "%i", semaphore->id);
		//loempa(9,"o4w - semaphore - created %c\n",semaphore->naam);
		semaphore->w_hEvent=CreateSemaphore(NULL, initial_count, max_aantal_semaphores,(LPCTSTR)semaphore->naam);
			semaphore->deleted = 0;
			semaphore->waiters = 0;
			semaphore->count = initial_count;
		
			if(reference_sem == NULL){
				list_init(semaphore);
				reference_sem = semaphore;
				loempa(5,"o4w - semaphore - new list started\n");
			}
			else{
				list_insert(reference_sem, semaphore);
			}
		
	}
	__finally {
		LeaveCriticalSection(&semaphore->w_critical_section);
	}
	loempa(7,"o4w - semaphore - created\n");
	return xs_success;
}

 
/*
 * Prototype:
 *   x_status x_sem_delete(x_Semaphore *semaphore_ptr);
 * Description:
 *   The 'x_sem_delete' operation deletes the specified 
 *   counting semaphore.
 */
 
x_status x_sem_delete(x_Semaphore *semaphore) {
	DWORD countbefore=0;
  	EnterCriticalSection(&semaphore->w_critical_section);

	if (!semaphore->w_hEvent) {
		loempa(5, "Refuse to delete semaphore at %p: already deleted\n", semaphore);
		return xs_deleted;
	}
	countbefore=semaphore->count;
		
	if(reference_sem!=NULL){
		loempa(5,"o4w - semaphore - reference_sem is not null\n" );
		do{
			loempa(5,"o4w - semaphore - next semaphore exist\n" );
			if(semaphore->id==reference_sem->id){
				loempa(7, "Deleting the semaphore at %p\n", semaphore);
				CloseHandle(semaphore->w_hEvent);
				semaphore->w_hEvent	= NULL;
				semaphore->deleted	= 1;
				semaphore->count	= 0;
				semaphore->waiters	= 0;
				semaphore->previous	= NULL;
				semaphore->next		= NULL;
				//semaphore->naam		= NULL;			
				if(semaphore == reference_sem) {
					if(reference_sem->next == reference_sem){	//only one chunk = ref_chunk
						loempa(5,"  +->There is only one semaphore in the linked list of semaphores...\n");
						reference_sem = NULL;
					}
					else{
						loempa(5,"  +->We are removing the reference semaphore..\n");
						reference_sem = semaphore->next;	// make another one the reference_chunk
						list_remove(semaphore);
					}

				}
			}
			reference_sem=reference_sem->next;
		}while((reference_sem->id!=reference_sem->next->id) && (semaphore->count==countbefore));
	}
	LeaveCriticalSection(&semaphore->w_critical_section);
	DeleteCriticalSection(&semaphore->w_critical_section);
	loempa(7, "semaphore at %p deleted\n", semaphore);

	return xs_success;
}
  
/*
 * Prototype:
 *   x_status x_sem_get(x_Semaphore *semaphore_ptr, 
 *                         x_sleep wait_option);
 * Description: 
 *   The get operation decreases the semaphore by one.  If the 
 *   semaphore is 0, the get operation will not be succesful.
 *   Selecting x_no_wait as wait option results in an immidiate
 *   return from this call regardless of whether or not it was
 *   succesful, while selecting x_eternal causes the calling
 *   thread to suspend until a semaphore instance is available.
 */


x_status x_sem_get(x_Semaphore *semaphore, x_sleep owait) {
	x_status rval= xs_no_instance;
	DWORD countbefore=0;
	int have_it = 0;
	loempa(7,"o4w - semaphore - semaphore get begin\n");
	loempa(5,"o4w - semaphore - event FROM sem : %d\n",&semaphore->w_hEvent);
	__try {
		EnterCriticalSection(&semaphore->w_critical_section);
		if(reference_sem!=NULL){
			loempa(5,"o4w - semaphore - reference_sem is not null\n" );
			do{
				countbefore=semaphore->count;
				loempa(5,"o4w - semaphore - next semaphore exist\n" );
				if(semaphore->id==reference_sem->id){
					if (semaphore->count < aantal_semaphores) {
						SetLastError(0);
					loempa(5,"o4w - semaphore - joepie, there is a semaphore\n" );
					//sprintf(naam, "%i", semaphore->id);
					semaphore->w_hEvent=CreateSemaphore(NULL,semaphore->count,max_aantal_semaphores,(LPCTSTR)semaphore->naam);
					if(GetLastError()==ERROR_ALREADY_EXISTS){
						loempa(5,"o4w - semaphore - found semaphore %d\n",&semaphore->w_hEvent);
						if(semaphore->count>0){	
							semaphore->count -=1;
							have_it = 1;
							rval=xs_success;
					}}
						else{
							loempa(9,"semaphore %d couldn't be get , errorcode %d\n",semaphore->w_hEvent,GetLastError());
						}
					}
					else {
						if (semaphore->deleted ==1) {
							loempa(5,"o4w - semaphore - semaphore already deleted\n");
							rval = xs_deleted;
						}
						else{
							if( owait==x_eternal||owait!=x_no_wait){
								semaphore->waiters +=1;
								while(!have_it){
									if(owait==x_eternal){
										LeaveCriticalSection(&semaphore->w_critical_section);
										SetLastError(0);
										loempa(5,"o4w - semaphore - event : %d\n",semaphore->w_hEvent);
										PulseEvent(semaphore->w_hEvent);		// tell the ones that are trying to enter that they may enter
										loempa(5,"o4w - semaphore - event : %d\n",semaphore->w_hEvent);
										if(GetLastError() != 0) {
											loempa(9, "  +->Event for semaphore pulsed with errorcode %d...\n", GetLastError());
										}
										loempa(5,"o4w - semaphore - leave critical to wait \n");
										WaitForSingleObject(semaphore->w_hEvent,x_eternal);
										EnterCriticalSection(&semaphore->w_critical_section);
										loempa(5,"o4w - semaphore - getsemaphore after a while\n");
										if(semaphore->count<aantal_semaphores){
												SetLastError(0);
					loempa(5,"o4w - semaphore - joepie, there is a semaphore\n" );
					//sprintf(naam, "%i", semaphore->id);
					semaphore->w_hEvent=CreateSemaphore(NULL,semaphore->count,max_aantal_semaphores,(LPCTSTR)semaphore->naam);
					if(GetLastError()==ERROR_ALREADY_EXISTS){
						loempa(5,"o4w - semaphore - found semaphore %d\n",&semaphore->w_hEvent);
						if(semaphore->count>0){	
							semaphore->count -=1;
							have_it = 1;
							rval=xs_success;
					}}
										}
									}
									else {
										if(WaitForSingleObject(semaphore->w_hEvent, owait) != WAIT_TIMEOUT){
											if(semaphore->count <aantal_semaphores){
														SetLastError(0);
					loempa(5,"o4w - semaphore - joepie, there is a semaphore\n" );
					//sprintf(naam, "%i", semaphore->id);
					semaphore->w_hEvent=CreateSemaphore(NULL,semaphore->count,max_aantal_semaphores,(LPCTSTR)semaphore->naam);
					if(GetLastError()==ERROR_ALREADY_EXISTS){
						loempa(5,"o4w - semaphore - found semaphore %d\n",&semaphore->w_hEvent);
						if(semaphore->count>0){	
							semaphore->count -=1;
							have_it = 1;
							rval=xs_success;
					}}
											}
										}
										else {
											rval=xs_no_instance;
											break;
										}
									}
								}
								semaphore->waiters -=1;
							}
						}
					}
				
			
				}
				reference_sem=reference_sem->next;
				
			
			}while((reference_sem->id!=reference_sem->next->id) && (semaphore->count==countbefore));
				
		}
		if(countbefore == 0 && reference_sem->count==0){
			loempa(9,"o4w - semaphore - There aren no things to get\n");
		}
		else if(countbefore == 0)
			loempa(9,"o4w - semaphore - There is no semophore in the list\n");
			
	}
	__finally
	{
		LeaveCriticalSection(&semaphore->w_critical_section);
	}
	loempa(7,"o4w - semaphore - end of searching semaphore\n");
	return (have_it ? xs_success : rval);
}


x_status x_sem_put(x_Semaphore *semaphore) {
	DWORD countbefore=0;
	x_status rval = xs_success;
	loempa(7,"o4w - semaphore - The current count of semaphores by entering the put_sem %d\n",&semaphore->count);

	__try {
		EnterCriticalSection(&semaphore->w_critical_section);
		countbefore=semaphore->count;
		
		if(reference_sem!=NULL){
			loempa(5,"o4w - semaphore - reference_sem is not null\n" );
			do{
				loempa(5,"o4w - semaphore - next semaphore exist\n" );
				if(semaphore->id==reference_sem->id){
					SetLastError(0);
						loempa(5,"o4w - The event of semaphores %d\n",semaphore->w_hEvent);
						if (ReleaseSemaphore(semaphore->w_hEvent,1,NULL)==TRUE){
							semaphore->count += 1;
							if(GetLastError() != 0) {
								loempa(9, "  +->Event for semaphore not relased with errorcode %d...\n", GetLastError());
								Sleep(2000);
							}	
							loempa(5,"o4w - semaphore - this semaphore is released %d\n",&semaphore->w_hEvent);
							
						if(semaphore->waiters) {
							SetLastError(0);
							
							PulseEvent(semaphore->w_hEvent);
							if(GetLastError() != 0) {
								loempa(9, "  +->Event for semaphore pulsed with errorcode %d...\n", GetLastError());
								Sleep(2000);
							}
						}

						loempa(5,"o4w - semaphore - this semaphore is counted %d\n",semaphore->count );
					}
					else{
						loempa(5,"o4w - semaphore - this semaphore is created in the put %d\n",semaphore->w_hEvent);
						loempa(5,"o4w - semaphore -couldn't find semaphore %d\n",&semaphore->w_hEvent);
					}
				}
				reference_sem=reference_sem->next;
			
			}while((reference_sem->id!=reference_sem->next->id) && (semaphore->count==countbefore));
		}
	}
	__finally {
		LeaveCriticalSection(&semaphore->w_critical_section);
	}
	
	return rval;
}
