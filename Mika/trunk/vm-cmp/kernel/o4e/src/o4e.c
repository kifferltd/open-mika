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

#include "oswald.h"

#include <stdio.h>
#include <unistd.h>
#include <stdarg.h>
#include <signal.h>
#include "o4e.h"

int command_line_argument_count;
char ** command_line_arguments;
cyg_uint32 msec_per_tick;
int num_x_threads = 0;	 // in o4e.h extern int num_x_threads => for extern acces to the number of threads
O4eEnv* o4eenv;		// a structure that keeps some info of the num of threads (and maybe some other stuff one day)
static cyg_ucount32 t_index;	// index for the thread specific data
void suspend_thread(void* arg);	// helper function for the suspending of threads (forward declaration)
// for our memory
cyg_handle_t spool;			// The memory pool for our static memory allocation
cyg_mempool_var spool_struct;		// The structure for our static memory pool
volatile o4e_memory_chunk reference_chunk;	// A chunk that we use as a reference to acces our linked list of chunks
x_Monitor Memory_Monitor;		// Monitor used for protecting the memory (used in mem_lock and unlock)
x_monitor memory_monitor;
x_thread mem_walk_owner;		// pointer to the function that is currently executing x_mem_walk
Collect_Result collect_result;	// struct used to hold the feedback of the mem_collect function
static cyg_ucount32 free_mem;	// the memory that is still available
static cyg_ucount32 total_mem;	// the total memory that is available

// setting up O4eEnv

void O4eEnvInit(void) {
    static O4eEnv theEnv;
    o4eenv = &theEnv;
    o4eenv->timer_ticks = 0;	// we change this 
    o4eenv->num_threads = 0;
    o4eenv->num_started = 0;    
    o4eenv->num_deleted = 0;
    cyg_mutex_init(&o4eenv->o4e_mutex);
    cyg_mutex_init(&o4eenv->printmutex);
    o4e_woempa(9,"O4eEnvInit \n");
    t_index = cyg_thread_new_data_index(); // To get an index for our thread specific data, this make sure we get a free index
    o4e_woempa(9,"Index for thread data: %d \n",t_index);
}

// TimeInit => calculate the time of a timer tick (msec_per_tick)
void TimeInit(void){
	//cyg_uint32 dividend;
	//cyg_uint32 divisor;
	//cyg_uint32 period;
	//cyg_uint32 factor = 3686400;	// the factor for calculating the rtc period
	cyg_resolution_t res;
	cyg_handle_t rclock;
	rclock = cyg_real_time_clock();
	/* Should work, but it doesn`t, you can set it at compile time in the configtool
	o4e_woempa(9,"In time Init with requested msec: %lu\n",msec_per_tick);
	dividend = 1000000000;	// ECOS Standard
	divisor = 1000 / msec_per_tick;		// depends on the wanted msec_per_tick
	res.dividend = dividend;
	res.divisor = divisor;
	cyg_clock_set_resolution(rclock,res);	// set the resolution ecos *thinks* he is working with
	o4e_woempa(9,"Before, period: %lu\n",period);
	period = factor / divisor;
	o4e_woempa(9,"Before, period: %lu\n",period);
	hal_clock_initialize(period);	// set the actual resolution
	o4e_woempa(9,"After\n");
	*/
	res = cyg_clock_get_resolution(rclock);	// checking ...
	msec_per_tick = (res.dividend/res.divisor) / 1000000;
	o4e_woempa(9,"There are %lu millisec in a timer tick\n",msec_per_tick);
}

// We use a memory pool for the static mem
// We use the native malloc and free functions for the heap => faster then memory pool
// This function sets up the memorypool for the static memory and
// checks that we can allocate HEAP_MAX (config this in o4e.h)
// size of the static mempool should be defined more accurate
int mem_init(void){
	void* dynam_base;
       	void* stat_base;
	o4e_woempa(9,"Initialising our memory pools for the heap and the static mem \n");
	reference_chunk = NULL;
	dynam_base = malloc(HEAP_MAX);
	stat_base = malloc(STATIC_MEM);
	if(stat_base == NULL ){
		o4e_woempa(9,"FAILED TO ALLOCATE THE MEMORY FOR THE STATIC MEMORY POOL => ERROR\n");
		return FALSE;
	}
	if(dynam_base == NULL ){
		o4e_woempa(9,"FAILED TO ALLOCATE THE MEMORY FOR THE HEAP => ERROR\n");
		return FALSE;
	}
	
	free(dynam_base);
	cyg_mempool_var_create(stat_base,STATIC_MEM,&spool,&spool_struct);
	memory_monitor = &Memory_Monitor;
	x_monitor_create(memory_monitor);
	mem_walk_owner = NULL;
	o4e_woempa(9,"Initialising the memory was a succes\n");
	total_mem = HEAP_MAX;
	free_mem = total_mem;
	o4e_woempa(9,"Total memory: %d\n",total_mem);
	o4e_woempa(9,"Free memory: %d\n",free_mem);
	return TRUE;
}

// registers a thread 
void threadRegister(x_thread newthread){
    
    cyg_mutex_lock(&o4eenv->o4e_mutex);
    
    o4eenv->num_threads += 1;
    num_x_threads = o4eenv->num_threads;
    
    cyg_mutex_unlock(&o4eenv->o4e_mutex);
            
}

// unregisters a thread (small stupid function, but it may grow someday)
void threadUnRegister(x_thread oldthread) {
     cyg_mutex_lock(&o4eenv->o4e_mutex);
    
    o4eenv->num_threads -= 1;
    num_x_threads = o4eenv->num_threads;
    
    cyg_mutex_unlock(&o4eenv->o4e_mutex);
}

// the start function of each thread
// Put thread specific data in the thread (the thread structure)
void start_routine(void* thread_arg){
    
    x_thread thread = (x_thread) thread_arg;
    o4e_woempa(9,"Starting the entry function for thread %p \n",thread);
    o4eenv->num_started += 1;
    cyg_thread_set_data(t_index,(CYG_ADDRWORD) thread);
    (*(x_entry)thread->o4e_thread_function)(thread->o4e_thread_argument);
    o4e_woempa(9,"Thread %p returned normally\n",thread);
    
    if (thread->curStatus != O4E_TERMINATED){  //O4E_TERMINATED => if deleted !!
	thread->curStatus = O4E_COMPLETED;
    }
    cyg_cond_broadcast(&thread->o4e_cond);
    o4e_woempa(9,"Thread status is now %d \n", thread->curStatus);
}




/*
 * Prototype:
 *   x_status x_thread_create(x_thread thread_ptr,
 *                        void (*entry_function) (void*), void* entry_input, 
 *                        void *stack_start, x_size stack_size, x_size priority, 
 *                        x_word flags);
 * Description:
 *   This service creates an application thread that starts execution
 *   at the specified task entry function.  The stack, priority, 
 *   preemption, and time-slice are among the attributes specified by
 *   the input parameters.
 *   Threads are the most important part of Oswald.  What exactly is
 *   a thread?  A thread is typically defined as a semi-independent
 *   program segment with a dedicated purpose.  The combined 
 *   processing of all threads makes an application.
 */

x_status x_thread_create(x_thread thread, void (*entry_function)(void*), void* entry_input, void *stack_start, x_size stack_size, x_size priority, x_word flags) {
    x_status rval = xs_success;

    if ((thread == NULL) || (entry_function == NULL) || (stack_start == NULL)) {
	if (thread == NULL) o4e_woempa(9,"Thread is null \n");
	if (stack_start == NULL) o4e_woempa(9,"Stack is null\n");
	if (entry_function == NULL) o4e_woempa(9,"Entry function is null\n");
	rval = xs_bad_argument;
     }
    else if ((priority < 0) || (priority > (NUM_PRIORITIES - 1))) {
	 o4e_woempa(9, "Prio is %d! EXIT\n", priority);
         rval = xs_bad_argument;
         exit(0);
    }
    else {
     
     cyg_mutex_init(&thread->o4e_thread_mutex);		//mutex used when messing with the thread state
     cyg_mutex_init(&thread->o4e_cond_mutex);
     cyg_cond_init(&thread->o4e_cond,&thread->o4e_cond_mutex);			//initialise cond and the mutex that goes with it
     thread->o4e_thread_stack_start = stack_start;
     thread->o4e_thread_stack_size = stack_size;
     thread->o4e_thread_function = entry_function;
     
     
     thread->waiting_on = NULL;		// thread is waiting on null monitors
     thread->waiting_with = 0;		// thread hasn't entered any monitors
    

     /*
     ** Set scheduling and priority
     */

     thread->o4e_thread_priority = priority;
     thread->o4e_thread_argument = entry_input;
   
     threadRegister(thread);
  
     /*
     ** Threads with TF_START should be started 
     ** Threads with TF_SUSPENDED should be left in the default state (suspended) when ecos creates a thread
     ** Threads aren`t started until cyg_sched_start has been called  
     */

     cyg_thread_create(priority,(cyg_thread_entry_t*)&start_routine,(cyg_addrword_t)thread,"An_ecos_thread",stack_start,stack_size,&thread->o4e_thread,&thread->o4e_thread_struct);
     thread->curStatus = O4E_NEWBORN;
     if (flags == TF_START){
         cyg_thread_resume(thread->o4e_thread);
	 thread->curStatus = O4E_READY;
	 o4e_woempa(9,"Thread %p resumed with priority: %d\n",thread,thread->o4e_thread_priority);
     }
     else if(flags == TF_SUSPENDED){
	thread->curStatus = O4E_SUSPENDED;	
     }
     else{
	o4e_woempa(9,"In x_thread_create: illegal flag\n");
	return xs_bad_argument;
     }
     rval = xs_success;

   }

   return rval;
}


/*
 * Prototype:
 *   x_status x_thread_delete(x_thread thread_ptr);
 * Description:
 *   Deletes the specified application thread.  Since the specified
 *   thread must be in a terminated or completed state, this service
 *   cannot be called from a thread attempting to delete itself or from a thread that is not in either of the desired states.
 */

x_status x_thread_delete(x_thread thread) {
  int teller = 0;
  x_status rval = xs_success;
  cyg_mutex_lock(&thread->o4e_thread_mutex);
  /*
  ** Check if we are calling ourselves or if the thread isn`t in one of the desired states 
  ** Return an error if something is not as it should be.
  */

  if ( (cyg_thread_self() != thread->o4e_thread) && (thread->curStatus==O4E_SUSPENDED || thread->curStatus==O4E_COMPLETED) ){
    threadUnRegister(thread);
    // try 100 times, if it isn`t deleted in 100 tries something is wrong ...
    while(!cyg_thread_delete(thread->o4e_thread) && teller<=100){
	cyg_thread_delay(1);
	teller++;
    }
    if (teller==100){
	rval = xs_bad_state;
	o4e_woempa(9,"BAD!! Ecos thread hasn`t been deleted after %d times!!\n",teller);	
    }
    else{
	thread->curStatus=O4E_TERMINATED;
	cyg_cond_broadcast(&thread->o4e_cond);
	o4e_woempa(9,"Thread %p deleted in %d times\n",thread,teller);	// signal everybody that the thread was stopped
    }
  }
  else {
      o4e_woempa(9,"Thread %p hasn't been deleted because not suspended or completed\n",thread);
      rval = xs_bad_state;  
    
  }
  cyg_mutex_unlock(&thread->o4e_thread_mutex);
  return xs_success;
}

 
/*
 * Prototype:
 *   x_int x_thread_priority_set(x_thread thread_ptr, 
 *                               x_size new_priority);
 * Description:
 *   Changes the priority of the specified thread.  Valid priorities
 *   range from 0 through NUM_PRIORITIES , where 0 represents the 
 *   highest priority level. (shouldn`t be 0 in ecos, 0 is the ecos kernel thread) 
 */
 
x_status x_thread_priority_set(x_thread thread, x_size new_priority) {
     x_status status = xs_success;
     if(new_priority > 0 && new_priority < NUM_PRIORITIES){
         thread->o4e_thread_priority = new_priority;
	 cyg_thread_set_priority(thread->o4e_thread,new_priority);
	 o4e_woempa(9,"Thread %p changed to priority %d \n",thread,new_priority); 
     }
     else{
         status = xs_bad_argument;
     }

  

  return status;

}

// get the priority of a thread 
x_size x_thread_priority_get(x_thread thread) {
  return thread->o4e_thread_priority;
}
 
/*
 * Prototype:
 *   x_status x_thread_resume(x_thread thread_ptr);
 * Description:
 *   Resumes or prepares for executions a thread that was previously
 *   suspended by a 'x_thread_suspend'-call.  In addition, this
 *   resumes threads that were created without and automatic start (with the argument TF_SUSPENDED).
 */
 
x_status x_thread_resume(x_thread thread) {
  x_status retValue = xs_success;
  
  cyg_mutex_lock(&thread->o4e_thread_mutex);
  if (thread->curStatus != O4E_SUSPENDED){
      retValue = xs_no_instance;
      o4e_woempa(9,"Thread %p not resumed because it was not suspended\n",thread);
  }
  else{
      cyg_thread_resume(thread->o4e_thread);
      thread->curStatus = O4E_READY;
      o4e_woempa(9,"Thread %p resumed with priority: %d\n",thread,thread->o4e_thread_priority);
  }
  cyg_mutex_unlock(&thread->o4e_thread_mutex);

  return retValue;
} 

/* Prototype:
 *   x_status x_thread_sleep(x_sleep timer_ticks);
 * Description:
 *   This operation causes the calling trhead to suspend for the
 *   specified number of timer ticks.  Obvioulsy, this operation 
 *   should be called from a thread. We simply map the this function 
 *   to the ecos delay function. 
 */
 
x_status x_thread_sleep(x_sleep timer_ticks) {
  
  
  x_thread thread = (x_thread) cyg_thread_get_data(t_index); 
  o4e_woempa(3,"Trying to put thread %p to sleep for %d ticks\n",thread,timer_ticks);
  cyg_mutex_lock(&thread->o4e_thread_mutex);
  
  thread->curStatus = O4E_SLEEP;
  
  o4e_woempa(3,"Thread %p is going to sleep for %d ticks\n",thread,timer_ticks);
  cyg_thread_delay(timer_ticks);
  o4e_woempa(3,"Thread %p is has done with his sleep\n",thread);    
  cyg_mutex_unlock(&thread->o4e_thread_mutex);
      
  return xs_success;
}

/*
 * Prototype:
 *   x_status x_thread_suspend(x_thread thread_ptr);
 * Description:
 *   Suspends the specified application thread.  A thread may call
 *   this service to suspend itself.  Once supsended, the thread must
 *   be resumed by calling 'x_thread_resume' in order to execute
 *   again.
 *  A Thread may suspends itself!!!
 */

x_status x_thread_suspend(x_thread thread) {  
  x_status retValue = xs_success;
  
  if((x_thread)cyg_thread_get_data(t_index)==thread){	// thread is suspending itself => create another thread to suspend the thread (avoiding deadlock)
      cyg_thread threadstruct;
      cyg_handle_t c_thread;
      char stack[4096];		//a 4K stack => big enough for our little thread
      o4e_woempa(9,"Thread is suspending itself \n");
      cyg_thread_create(1,(cyg_thread_entry_t*)&suspend_thread,(cyg_addrword_t)thread,"suspend thread",&stack,4096,&c_thread,&threadstruct);
      o4e_woempa(9,"thread created \n");
      cyg_thread_resume(c_thread);
      cyg_thread_yield();		// in case the thread should also have prio 1, yield control
      cyg_thread_delete(c_thread);  	// delete thread from scheduler
           
  }
  else{						// thread is suspending another thread 
      if (thread->curStatus != O4E_SUSPENDED){
          cyg_mutex_lock(&thread->o4e_thread_mutex);
          thread->curStatus = O4E_SUSPENDED;
          o4e_woempa(9,"Thread %p suspended\n",thread);    
          cyg_thread_suspend(thread->o4e_thread);	  
          cyg_mutex_unlock(&thread->o4e_thread_mutex); 		// can`t do this when a thread is suspending itself, never get here so deadlock
      }
      else{
          o4e_woempa(9,"Thread already suspended \n");
          retValue = xs_no_instance;
      }
 }
  
  

  return retValue;
}

void suspend_thread(void* arg){		//function that suspends a thread 
    x_thread thread = (x_thread) arg;
    cyg_mutex_lock(&thread->o4e_thread_mutex);
    thread->curStatus = O4E_SUSPENDED;
    o4e_woempa(9,"Thread %p suspended\n",thread);    
    cyg_thread_suspend(thread->o4e_thread);	  
    cyg_mutex_unlock(&thread->o4e_thread_mutex);
    cyg_thread_exit();	
}
// puts the current thread in sleep mode until thread has terminated or until timeout has passed
// ECOS does not have a join function so we use our own `problably stupid` implementation
// null or 0 for the timeout => will wait until thread is completed
// result can become => 	TF_JOIN_ENDED 	=> thread we are waiting on has exited normaly
//				TF_JOIN_DELETED	=> thread we are waiting on has been deleted
x_status x_thread_join(x_thread thread, void **result, x_sleep timeout) {	
  x_status status;
  cyg_bool_t booltje = 1;
  
  x_thread running_thread = (x_thread) cyg_thread_get_data(t_index);
  
  if (running_thread==thread){
    o4e_woempa(9,"Thread %p want's to join itself, this is impossible\n",thread);  
    status = xs_deadlock;
  }
  
  else if(thread->curStatus==O4E_COMPLETED){
    o4e_woempa(9,"Thread %p was already completed\n",thread);
    status = xs_success;
    if(result!=NULL){
	*(unsigned int**)result = (unsigned int*)TF_JOIN_ENDED;      
    }
  }
  else if(thread->curStatus==O4E_TERMINATED){
    o4e_woempa(9,"Thread %p was previous deleted\n",thread);
    status = xs_success;
    if(result!=NULL){
	*(unsigned int**)result = (unsigned int*)TF_JOIN_DELETED;      
    }
  }
  else{
      o4e_woempa(9,"Thread %p is waiting on thread %p to finish\n",running_thread,thread);    
      cyg_mutex_lock(&running_thread->o4e_thread_mutex);
  
      running_thread->curStatus = O4E_SLEEP;

      cyg_mutex_lock(&thread->o4e_cond_mutex); 	// lock the mutex because wait will unlock it !!
  
      if (timeout == 0){
          cyg_cond_wait(&thread->o4e_cond);	// wait until thread has completed 
	  					// (when completed it will signal the o4e_cond condition)
      
      }
      else{
          booltje = cyg_cond_timed_wait(&thread->o4e_cond,cyg_current_time() + timeout);
      }
      
      if(booltje == 1){
          cyg_mutex_unlock(&thread->o4e_cond_mutex);	// unlock mutex because you are the owner !!
          status = xs_success;
	  if(result != NULL){
	      if(thread->curStatus == O4E_COMPLETED){
	         *(unsigned int**)result = (unsigned int*)TF_JOIN_ENDED;      
	      }
	      else{
	          *(unsigned int**)result = (unsigned int*)TF_JOIN_DELETED;
	      }
	  }    
       }
      else{
        o4e_woempa(9,"Timeout passed in joining thread %p\n",thread);
        status = xs_no_instance;		// What value should be returned if a timeout occurs??? Probably xs_no_instance 
      }
      running_thread->curStatus = O4E_READY;
  
      cyg_mutex_unlock(&running_thread->o4e_thread_mutex);		
  }
  return status;
}

/*
 * Prototype:
 *   x_thread x_thread_current(void);
 * Description:     
 *   Returns a pointer to the currently executing thread running
 *   thread.  If no thread is executing, this service returns a null
 *   pointer.
 */

x_thread x_thread_current() {
  x_thread main_thread;
  x_thread cur_thread = (x_thread) (cyg_thread_get_data(t_index));
  if (cur_thread == NULL){
	main_thread = (x_thread) x_alloc_static_mem(NULL,sizeof(x_Thread));
	// there isn`t any info about the current thread
	// This is only the case when we boot, if the main thread is calling x_thread_current()
	// In this case we create a x_Thread structure around this thread
	// We get the memory from our static memory
	main_thread->curStatus = O4E_READY;
	cyg_mutex_init(&main_thread->o4e_thread_mutex);
	cyg_mutex_init(&main_thread->o4e_cond_mutex);
	cyg_cond_init(&main_thread->o4e_cond,&main_thread->o4e_cond_mutex);
	main_thread->o4e_thread_stack_start = NULL;
	main_thread->o4e_thread_stack_size = 0;
	main_thread->o4e_thread = cyg_thread_self();
	main_thread->o4e_thread_priority = cyg_thread_get_priority(cyg_thread_self());
	o4e_woempa(9,"The priority of the main thread is %d\n",cyg_thread_get_priority(cyg_thread_self()));
	main_thread->o4e_thread_function = NULL;
	main_thread->o4e_thread_argument = NULL;
	main_thread->waiting_on = NULL;
	main_thread->waiting_with = 0;
  	cyg_thread_set_data(t_index,(CYG_ADDRWORD) main_thread);
	return main_thread;
  }
  return cur_thread;

}

/*
** Yields control to the next runnable thread of equal priority, if no such thread exists
*  this function has no effect. 
*/

void x_thread_yield(void) {

    x_thread thread = (x_thread)cyg_thread_get_data(t_index);
    o4e_woempa(2,"Thread %p is yielding it`s control\n",thread);    
    cyg_thread_yield();		// This is all we have to do here


}

/*
** Our overloaded woempa function
*  Later on we just leave it out and everything will be fine
*/

void o4e_print(char* string, ...){
    va_list ap;
    cyg_mutex_lock(&o4eenv->printmutex);
    va_start(ap,string);
    vfnprintf(stdout,INT_MAX,string,ap);	// look at printf.cxx in the ecos-repository to see how this can work
    va_end(ap);
    cyg_mutex_unlock(&o4eenv->printmutex);
    

}



/*  These functions lock/unlock the scheduler, it's implemented in eCos so
 *  we just need to map it.
 */
void x_scheduler_disable(void) {
  cyg_scheduler_lock();
}

void x_scheduler_enable(void){
  cyg_scheduler_unlock();
}

/*
 * SEMAPHORES
 */


/*
 * Prototype:
 *   x_status x_sem_create(x_Semaphore *semaphore_ptr, 
 *                            x_size initial_count);
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
 * Implementation:
 *   Believe it or not, but counting semaphores are provided in
 *   eCos, so basicaly we just have to map the eCos interface
 *   onto the Oswald API.
 */ 
 
x_status x_sem_create(x_Semaphore *semaphore, x_size initial_count) {
  
  x_status rval = xs_success;

  cyg_mutex_init(&semaphore->mutex);
  cyg_mutex_lock(&semaphore->mutex);
  cyg_semaphore_init(&semaphore->ecos_sem, (cyg_ucount32) initial_count );
  semaphore->deleted = 0;
  semaphore->current = initial_count;
  o4e_woempa(2,"o4e - semaphores - new sem created.\n");
  cyg_mutex_unlock(&semaphore->mutex);
  return rval;
}

/*
 * Prototype:
 *   x_status x_sem_delete(x_Semaphore *semaphore_ptr);
 * Description:
 *   The 'x_sem_delete' operation deletes the specified 
 *   counting semaphore.
 * Implementation:
 *   Counting semaphores are implemented in eCos, but we must 
 *   check for waiting threads by cyg_semaphore_peek.
 *   eCos will behave unpredictable (read: crash) when we destroy
 *   a semaphore when threads are still waiting on it. It's up to
 *   the programmer to check for this. We will set the deleted field
 *   on the sem, but the semaphore itself will be destroyed on
 *   the memory-freeing without checking...
 */
 
x_status x_sem_delete(x_Semaphore *semaphore) {

  x_status rval = xs_success;
  cyg_count32 val ;

  cyg_mutex_lock(&semaphore->mutex);  

  cyg_semaphore_peek(&semaphore->ecos_sem, &val);
  if (val == 0){
    cyg_semaphore_destroy(&semaphore->ecos_sem);
    o4e_woempa(9,"o4e - semaphores - sem succesfully destroyed.\n");
    semaphore->deleted = 1 ;
  } 
  else {
    o4e_woempa(9,"o4e - semaphores - failed to destroy sem, still thread waiting...\n");
    semaphore->deleted = 1;
  }
  
  cyg_mutex_unlock(&semaphore->mutex);  // the mutex doesn't have to be destroyed,
  								// because destroy doesn't do squad!
  return rval;

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
 * Implementation:
 *   Believe it or not, but counting semaphores are provided in
 *   eCos, so basicaly we just have to map the eCos interface on
 *   the Oswald interface.  
 */

x_status x_sem_get(x_Semaphore *semaphore, x_sleep owait) {

  cyg_count32 val;
  x_status rval = xs_no_instance;
  cyg_bool_t result;

  cyg_mutex_lock(&semaphore->mutex);

  if (semaphore->deleted == 1) {       // Check if the mutex isn't deleted
    o4e_woempa(9, "o4e - semaphores - sem already deleted, returning xs_deleted\n");
    rval = xs_deleted;
  } 
  else  {

    if( owait == x_eternal ){
      cyg_mutex_unlock(&semaphore->mutex);	    
      cyg_semaphore_wait(&semaphore->ecos_sem);
      cyg_mutex_lock(&semaphore->mutex);
      rval = xs_success ; 
      o4e_woempa(2,"o4e - semaphores - sem get after eternal wait\n");
    }

    else if ( owait == x_no_wait ) {
      if (cyg_semaphore_trywait(&semaphore->ecos_sem) == 1) {
        o4e_woempa(9,"o4e - semaphores - sem get failed with no_wait\n");
        rval = xs_success ;
      }
    }

    else {

      
      cyg_mutex_unlock(&semaphore->mutex);
      result = cyg_semaphore_timed_wait(&semaphore->ecos_sem, cyg_current_time() + (cyg_tick_count_t) owait);
      cyg_mutex_lock(&semaphore->mutex);
      if(result == 1) {
        o4e_woempa(2,"o4e - semaphores - sem get after waiting %d ticks\n", owait);
        rval = xs_success ;
        if( semaphore->deleted == 1 ) {  //checking again for sem_delete while waiting
          o4e_woempa(2,"o4e - semaphores - 2 Bad, sem deleted while waiting\n");
          rval = xs_deleted ;
        }
      } else o4e_woempa(2,"o4e - semaphores - couldn't get sem, timed out\n");
    }
  }

  cyg_semaphore_peek(&semaphore->ecos_sem,&semaphore->current);
  o4e_woempa(2,"o4e - semaphores - count decreased to %d",val);

  cyg_mutex_unlock(&semaphore->mutex);
  return rval;
}


/*
 * Prototype:
 *   x_status x_sem_put(x_Semaphore *semaphore_ptr);
 * Description:
 *   This service, which is the inverse of the 'x_sem_get'
 *   operation, increases the semaphore by one.  
 * Implementation:
 *   Believe it or not, but counting semaphores are provided in
 *   eCos, so basicaly we just have to map the eCos interface on
 *   the Oswald interface. One drawback: post will activate the 
 *   thread at the top of the waiting queue, not the one with the
 *   highest priority
 */
 
x_status x_sem_put(x_Semaphore *semaphore) {

  cyg_count32 val;
  x_status rval = xs_success;

  cyg_mutex_lock(&semaphore->mutex);

  if(semaphore->deleted == 1) {
    rval = xs_deleted ;
  }
  else {
    cyg_semaphore_post(&semaphore->ecos_sem);
    o4e_woempa(2,"o4e - semaphores - semaphores put\n");
  }

  cyg_semaphore_peek(&semaphore->ecos_sem,&semaphore->current);
  o4e_woempa(2,"o4e - semaphores - current count: %d\n", val);

  cyg_mutex_unlock(&semaphore->mutex);

  return rval;
}


/*
 *  MUTEXES
 */

/*
 * Prototype:
 *   x_status x_mutex_create(x_Mutex *mutex)
 * Description:
 *   Creates a new mutex.
 * Implementation:
 *   Mutexes are supported in eCos, only the window
 *   of time used to (un)lock the mutexes is implemented 
 *   on top of the eCos API.
 */

x_status x_mutex_create(x_Mutex *mutex) {
  // OSwald: memory reserved by the caller
  // eCos  : uncertain

  x_status rval = xs_success;
  cyg_mutex_init(&mutex->locker_mutex);
  cyg_cond_init(&mutex->locker_cond, &mutex->locker_mutex);
  cyg_mutex_init(&mutex->ecos_mut);
  mutex->locked = 0;
  mutex->deleted = 0;
  mutex->owner = x_thread_current();
  o4e_woempa(5,"o4e - mutex - mutex created");


  return rval;
}

/*
 * Prototype:
 *   x_status x_mutex_delete(x_Mutex *mutex)
 * Description:
 *   Deletes the mutex.
 * Implementation:
 *   Mutexes are supported in eCos, only the window
 *   of time used to (un)lock the mutexes is implemented 
 *   on top of the eCos API.
 *   We check if the thread is owner of the mutex and only then
 *   we unlock (if necessary) and delete the mutex.
 */

x_status x_mutex_delete(x_Mutex *mutex) {

  o4e_woempa(5,"o4e - mutex - delete entered\n");

  if( mutex->owner != x_thread_current() ) {
    o4e_woempa(5,"o4e - mutex - wrong thread deleting the mutex\n");
    return xs_not_owner;
  } 
  else if ( (mutex->locked == 1) && (mutex->owner == x_thread_current()) ) { 
    mutex->deleted = 1;
    cyg_mutex_unlock(&mutex->ecos_mut);
    cyg_mutex_destroy(&mutex->ecos_mut);
    o4e_woempa(5,"o4e - mutex - mutex unlocked & destroyed\n");
    return xs_success;
  }
  else {
    mutex->deleted = 1;
    cyg_mutex_destroy(&mutex->ecos_mut);
    o4e_woempa(5,"o4e - mutex - mutex destroyed\n");
    return xs_success;
  }
  
}

/*
 * Prototype:
 *   x_status x_mutex_lock(x_Mutex *mutex, x_sleep timeout)
 * Description:
 *   Tries to lock the mutex within the timeout.
 * Implementation:
 *   Mutexes are supported in eCos, only the window
 *   of time used to (un)lock the mutexes is implemented 
 *   on top of the eCos API.
 *   eCos has priority inheritance on mutexes but mlqueue
 *   MUST be selected as scheduler. The queue on the 
 *   mutexes aren't ordered on priority but are just FIFO. 
 *
 *   We work only with trylocks in this method. Lock will 
 *   place the requesting threads in a queue. The threads 
 *   in the queue will dealt with first, but this isn't the 
 *   purpose of this function. Because it will make sure that 
 *   the thread that will unlock always gets the first shot at
 *   locking the mutex, bypassing the waiting threads.
 *
 *   So we will try to lock and after an unlock we'll yield
 *   the thread, so all the waiting threads will go first.
 * 
 *   If the mutex isn't directly acquired, it will block for
 *   the system for the rest of the timeout.
 */

x_status x_mutex_lock(x_Mutex *mutex, x_sleep timeout) {
	int result;
	
	if(mutex->deleted == 1)	{
		o4e_woempa(9,"o4e - mutex - mutex already deleted, can't be locked\n");
		return xs_deleted;
	}
	else{
		cyg_mutex_lock(&mutex->locker_mutex);
		if(cyg_mutex_trylock(&mutex->ecos_mut)){
			o4e_woempa(2,"Got mutex from the first time\n");
			mutex->owner=x_thread_current();
			mutex->locked=1;
			cyg_mutex_unlock(&mutex->locker_mutex);
			return xs_success;
		}
		else{
			if(timeout == x_eternal){
				o4e_woempa(2,"Waiting forever for the mutex\n");
				cyg_mutex_unlock(&mutex->locker_mutex);
				cyg_mutex_lock(&mutex->ecos_mut);
				cyg_mutex_lock(&mutex->locker_mutex);
				mutex->owner=x_thread_current();
				mutex->locked=1;
				cyg_mutex_unlock(&mutex->locker_mutex);
				return xs_success;
			}
			else{
				result = cyg_cond_timed_wait(&mutex->locker_cond,cyg_current_time()+timeout);
				if(result == TRUE){
					if(cyg_mutex_trylock(&mutex->ecos_mut)){
						o4e_woempa(2,"Got the mutex within the timeout\n");
						mutex->owner=x_thread_current();
						mutex->locked=1;
						cyg_mutex_unlock(&mutex->locker_mutex);
						return xs_success;
					}
					else{
						o4e_woempa(9,"Should not get here often\n");
						cyg_mutex_unlock(&mutex->locker_mutex);
						return xs_no_instance;
					}
				}
				else{
					o4e_woempa(2,"Timeout occured\n");
					cyg_mutex_unlock(&mutex->locker_mutex);
					return xs_no_instance;
				}
			}
		}
	}
}

/*
 * Prototype:
 *   x_status x_mutex_unlock(x_Mutex *mutex)
 * Description:
 *   Unlocks the mutex if the unlocking thread is the owner.
 * Implementation:
 *   Mutexes are supported in eCos, only the window
 *   of time used to (un)lock the mutexes is implemented 
 *   on top of the eCos API.
 *   We check if the thread is owner of the mutex and only then
 *   we unlock the mutex.
 *   eCos has priority inheritance on mutexes but mlqueue
 *   MUST be selected as scheduler. The queue on the 
 *   mutexes aren't ordered on priority but are just FIFO. 
 *
 *   For an explanation for all the weird this, see the
 *   function above.
 */
x_status x_mutex_unlock(x_Mutex *mutex){
	o4e_woempa(2, "UNLOCKING MUTEX\n");
	if (mutex->owner != x_thread_current()) {
		o4e_woempa(9, "o4e - mutex - thread not owner, unlock failed\n" );
		return xs_bad_element;
	}
	else if(mutex->deleted == 1){
		o4e_woempa(2,"Trying to unlock deleted mutex\n");
		return xs_bad_element;
	}
	else if(mutex->locked == 1){
		cyg_mutex_lock(&mutex->locker_mutex);
		o4e_woempa(2,"o4e - mutex - beginning mutex unlock seq by thread %d\n", x_thread_current());
		cyg_mutex_unlock(&mutex->ecos_mut);
		mutex->locked = 0;
		cyg_cond_signal(&mutex->locker_cond);
		cyg_mutex_unlock(&mutex->locker_mutex);
		x_thread_yield();
		o4e_woempa(2, "o4e - mutex - unlock finished by thread %d and cond signalled\n", cyg_thread_self());
		return xs_success;
	}
	else {
		o4e_woempa(2, "o4e - mutex - mutex wasn't locked\n");
		return xs_no_instance;
	}

}




/*  Queue's
 *
 */

/*
 * Prototype:
 *   x_status x_queue_create(x_Queue *queue, 
 *       void *queue_start, x_size queue_size)
 * Description:
 *   Creates a queue with fixed size.
 * Implementation:
 *  Queue's are implemented as eCos Message Boxes with 
 *  a fixed size of 256 items. Thread queues are sorted by
 *  priority if you set the CYGIMP_KERNEL_SCHED_SORTED_QUEUES key in the
 *  configtool.
 */

x_status x_queue_create(x_Queue *queue, void *queue_start, x_size queue_size){
  
  cyg_mutex_init(&queue->mutex);
  cyg_mutex_lock(&queue->mutex);

  o4e_woempa(2,"o4e - queue - create begin\n");
  
  cyg_mbox_create(&queue->handle,&queue->mbox) ;
  queue->deleted = 0;
  queue->capacity = 256; // fixed size of the queue, set in the eCos configtool
                         // by CYGNUM_KERNEL_SYNCH_MBOX_QUEUE_SIZE
  o4e_woempa(5,"o4e - queue - queue created");
  cyg_mutex_unlock(&queue->mutex);

  return xs_success;
}


/*
 * Prototype:
 *   x_status x_queue_delete(x_Queue *queue)
 * Description:
 *   Deletes a queue.
 * Implementation:
 *  Queue's are implemented as eCos Message Boxes with 
 *  a fixed size of 256 items. Thread queues are sorted by
 *  priority if you set the CYGIMP_KERNEL_SCHED_SORTED_QUEUES key in the
 *  configtool.
 */
x_status x_queue_delete(x_Queue *queue){
  
  cyg_mutex_lock(&queue->mutex);
  cyg_mbox_delete(queue->handle) ;
  queue->deleted = 1;
  o4e_woempa(5,"o4e - queue - queue deleted");
  cyg_mutex_unlock(&queue->mutex);

  return xs_success;
}

/*
 * Prototype:
 *   x_status x_queue_receive(x_Queue *queue, void **data, x_sleep wait)
 * Description:
 *   Receive a message over a queue. The value of the pointer of the data
 *   will be put in void** data.
 * Implementation:
 *  Queue's are implemented as eCos Message Boxes with 
 *  a fixed size of 256 items. Thread queues are sorted by
 *  priority if you set the CYGIMP_KERNEL_SCHED_SORTED_QUEUES key in the
 *  configtool.
 *  
 *  TIP: you must malloc a place in the memory for the void** because
 *  it will be dereferenced in the implementation.
 */
x_status x_queue_receive(x_Queue *queue, void **data, x_sleep wait){
  x_status rval = xs_no_instance;

  o4e_woempa(2,"o4e - queue - entered receive\n");
    
  // returns a pointer to the data, if timeouts passes it will return NULL
  if (wait == x_eternal ) {
  	o4e_woempa(2, "Queue receive met eternal wait, thread : %p \n",x_thread_current() );
	*data = cyg_mbox_get(queue->handle);
	o4e_woempa(2, "Queue receive met eternal wait finished, thread : %p \n",x_thread_current() );
	rval = xs_success ;
  }
  else {
	  
    *data = (cyg_mbox_timed_get(queue->handle, (cyg_tick_count_t) wait + cyg_current_time() + 5000));
    
    if (*data == NULL) {
	    o4e_woempa(9,"Queue received timed out\n");
	    rval = xs_no_instance ;
    }
    else if (queue->deleted == 1) {
      o4e_woempa(2,"o4e - queue - Q deleted in receive timeout, thread : %p \n",x_thread_current());
      rval = xs_bad_context;
    } 
    else {
      o4e_woempa(2,"o4e - queue - received message, thread : %p \n",x_thread_current());
      rval = xs_success;
    
    }
  }
  return rval;
}


/*
 * Prototype:
 *   x_status x_queue_send(x_Queue *queue, void *src_msg, x_sleep wait)
 * Description:
 *   Send a message over a queue.
 * Implementation:
 *  We pass the value of the message not the message itself.
 *
 *  Queue's are implemented as eCos Message Boxes with 
 *  a fixed size of 256 items. Thread queues are sorted by
 *  priority if you set the CYGIMP_KERNEL_SCHED_SORTED_QUEUES key in the
 *  configtool.
 */
x_status x_queue_send(x_Queue *queue, void *src_msg, x_sleep wait){

  x_status rval = xs_success;
  cyg_bool_t res;
  
  o4e_woempa(2,"o4e - queue - entered send\n");

  if ( wait == x_eternal ) {
  	o4e_woempa(2, "Queue send with eternal wait\n");
	cyg_mbox_put(queue->handle, src_msg );
	o4e_woempa(2, "Queue send with eternal wait finished\n");
	rval = xs_success ;
  }
  else {
    res = cyg_mbox_timed_put(queue->handle, src_msg, cyg_current_time() + (cyg_tick_count_t) wait) ;
    o4e_woempa(2,"o4e - queue - result of ecos timed put %d\n", res);
    if ( res == 0 ) {
      if (queue->deleted == 1) {
        o4e_woempa(2,"o4e - queue - queue deleted while send timeout\n");
        rval = xs_deleted;
      } else {
        rval = xs_no_instance;
        o4e_woempa(2,"o4e - queue - queue send failed\n");
      }
    } else o4e_woempa(2,"o4e - queue - msg send to queue\n");
  }
  return rval;
}



/*
 * Prototype:
 *   x_status x_queue_flush(x_Queue *queue, void(*do_this)(void *data))
 * Description:
 *   Flushes the queue, it releases all the messages. The queue will be empty.
 * Implementation:
 *  Queue's are implemented as eCos Message Boxes with 
 *  a fixed size of 256 items. Thread queues are sorted by
 *  priority if you set the CYGIMP_KERNEL_SCHED_SORTED_QUEUES key in the
 *  configtool.
 */
x_status x_queue_flush(x_Queue *queue, void(*do_this)(void *data)){

  void * mydata;
  cyg_count32 i;
  x_status rval = xs_success;

  if (queue->deleted == 1) {
    rval=xs_deleted;
  }
  else {
    for (i = cyg_mbox_peek(queue->handle); i > 0 ; i--) {
      mydata = cyg_mbox_get(queue->handle);
      do_this(mydata);
    }

  }
  o4e_woempa(5,"o4e - queue - Q flushed, size %d\n", cyg_mbox_get(queue->handle));

  return rval;
}

/*
** Initialise a new monitor.
*/
x_status x_monitor_create(x_monitor monitor) {
  o4e_woempa(2, "Creating a monitor at %p\n", monitor);
  monitor->owner = NULL;
  monitor->count = 0;
  cyg_mutex_init(&monitor->condmutex);
  cyg_cond_init(&monitor->condvar,&monitor->condmutex);
  cyg_mutex_init(&monitor->enter_mutex);
  cyg_cond_init(&monitor->enter_cond,&monitor->enter_mutex);
  monitor->status = MONITOR_READY;
  o4e_woempa(2, "Monitor at %p has mutex %p, condvar %p\n", monitor, &monitor->condmutex, &monitor->condvar);
  return xs_success;
}

/*
** Delete a monitor.
** You should not delete a monitor while there are threads waiting on it => bad programming
*/
x_status x_monitor_delete(x_monitor monitor) {
  x_thread current_thread = x_thread_current();

  if ((monitor->owner != current_thread) && (monitor->owner != NULL)) {
    o4e_woempa(2, "Refuse to delete monitor at %p: owned by %p, caller is %p\n", monitor, monitor->owner, current_thread);

    return xs_not_owner;

  }
  if (monitor->status == MONITOR_DELETED) {
    o4e_woempa(9, "Refuse to delete monitor at %p: already deleted\n", monitor);

    return xs_deleted;

  }
  o4e_woempa(2, "Deleting the monitor at %p\n", monitor);

  cyg_cond_destroy(&monitor->condvar);	//this does nothing in ecos for the moment but one day...
  cyg_mutex_destroy(&monitor->condmutex);
  cyg_cond_destroy(&monitor->enter_cond);
  cyg_mutex_destroy(&monitor->enter_mutex);
  monitor->status = MONITOR_DELETED;
  o4e_woempa(2, "Monitor at %p deleted\n", monitor);

  return xs_success;
}

/*
** Enter a monitor.
*/
x_status x_monitor_enter(x_monitor monitor, x_sleep timeout) {
  cyg_bool_t result;
  x_thread current_thread = x_thread_current();
  
  o4e_woempa(2, "Thread %p is trying to enter monitor %p\n", current_thread, monitor);
  if (monitor->owner == current_thread) {
    monitor->count += 1;
    o4e_woempa(5, "Thread %p already owns monitor %p, count now %d\n", current_thread, monitor, monitor->count);
  }
  else if (monitor->owner == NULL) {
    result = cyg_mutex_lock(&monitor->condmutex);
    if (result==FALSE) {
      o4e_woempa(9, "x_monitor_enter: cyg_mutex_lock did not work \n");
      return xs_unknown;
    }
    monitor->owner = current_thread;
    monitor->count = 1;
  }
  else {
    if (timeout == x_no_wait) {
      result = cyg_mutex_trylock(&monitor->condmutex);
      if (result == FALSE) {
        o4e_woempa(5, "Thread %p tried to obtain monitor %p, mutex was busy\n", current_thread, monitor);
        return xs_no_instance;
      }
      else {
        o4e_woempa(5,"Enter monitor: cyg_mutex_trylock was succesfull! \n");
        
      }
    }
    else if (timeout == x_eternal) {
      result = cyg_mutex_lock(&monitor->condmutex);
      if (result == FALSE) {
        o4e_woempa(5, "x_monitor_enter: cyg_mutex_lock failed \n");
        return xs_unknown;
      }
    }
    else {
      o4e_woempa(5,"trying to enter monitor with timeout %d \n",timeout);
      cyg_mutex_lock(&monitor->enter_mutex);
      if (cyg_cond_timed_wait(&monitor->enter_cond,cyg_current_time() + timeout) == TRUE){
        if(cyg_mutex_trylock(&monitor->condmutex) == TRUE){
    	    o4e_woempa(5,"obtained monitor %p within timeout %d \n",monitor,timeout);
	    cyg_mutex_unlock(&monitor->enter_mutex);	// 3 hours searching where the deadlock was when we forgot this statement!!!
	}
	else{
	    o4e_woempa(5,"x_monitor_enter: timeout due to fail trylock!! \n");
	    cyg_mutex_unlock(&monitor->enter_mutex);
	    return xs_no_instance;
	}
      }
      else {
        o4e_woempa(5, "x_monitor_enter: timeout!!\n");
        cyg_mutex_unlock(&monitor->enter_mutex);  //unlock the mutex
	return xs_no_instance;
      }
    }
    monitor->owner = current_thread;
    monitor->count = 1;
  }
  
  o4e_woempa(2, "Thread %p has obtained monitor %p, count = %d, owner = %p \n", current_thread, monitor,monitor->count,monitor->owner);

  return xs_success;
}

/*
** Wait for a monitor
*/
x_status x_monitor_wait(x_monitor monitor, x_sleep timeout) {
  x_thread current_thread = x_thread_current();
  cyg_bool_t result; 

  if (monitor->owner != current_thread) {
    o4e_woempa(2, "Thread %p is not allowed to wait on monitor %p - owner is %p\n", current_thread, monitor, monitor->owner);

    return xs_not_owner;
  }

  o4e_woempa(2, "Thread %p is waiting on monitor %p, has count %i\n", current_thread, monitor, monitor->count);

  current_thread->waiting_on = monitor;
  current_thread->waiting_with = monitor->count;
    
  monitor->owner = NULL;
  monitor->count = 0;
  cyg_cond_signal(&monitor->enter_cond);	// tell the ones that are trying to enter that they may enter
  if(timeout == x_eternal){
  	cyg_cond_wait(&monitor->condvar);
  }
  else{
	result = cyg_cond_timed_wait(&monitor->condvar,cyg_current_time() + timeout);
	if(result == FALSE){
		if(monitor->owner!=NULL){
			o4e_woempa(9,"Waiting on monitor %p failed for thread %p, timeout occured \n",monitor,current_thread);
			return xs_no_instance;
		}
	}
  }
  if(monitor->owner == NULL){ // check if nobody else is the owner in case of the stop_waiting call
	monitor->owner = current_thread;
  	monitor->count = current_thread->waiting_with;
  	current_thread->waiting_on = NULL;
  	current_thread->waiting_with = 0;
  	o4e_woempa(2, "Thread %p has re-acquired monitor %p\n", current_thread, monitor);
      	return xs_success;
  }
  else{	// thread has been given the call x_thread_stop_waiting() and someone else already owns the monitor;
	 o4e_woempa(9,"Thread %p has stop`t waiting on monitor %p",current_thread,monitor);	
	 cyg_mutex_unlock(&monitor->condmutex);	//unlock reaquired mutex because youre not the owner
	 return xs_no_instance;
  }
}

/*
** Notify one thread waiting on a monitor
*/
x_status x_monitor_notify(x_monitor monitor) {
  x_thread current_thread = x_thread_current();
  
  if (monitor->owner != current_thread) {
    o4e_woempa(7, "Thread %p is not allowed to notify monitor %p - owner is %p\n", current_thread, monitor, monitor->owner);

    return xs_not_owner;
  }

  cyg_cond_signal(&monitor->condvar);  				// wake up one thread waiting on the monitor
  o4e_woempa(7,"One thread notified on monitor %p\n",monitor);	//	=> signals the first that was waiting  
  
  return xs_success;
}

/*
** Notify all threads waiting on a monitor
*/
x_status x_monitor_notify_all(x_monitor monitor) {
  x_thread current_thread = x_thread_current();
  x_status status = xs_success;

  if (monitor->owner != current_thread) {
    o4e_woempa(7, "Thread %p is not allowed to notify monitor %p - owner is %p\n", current_thread, monitor, monitor->owner);

    return xs_not_owner;
  }
  cyg_cond_broadcast(&monitor->condvar);
  o4e_woempa(5,"All threads notified on monitor %p \n",monitor);  
  return status;
}

/*
** Leave a monitor
*/
x_status x_monitor_exit(x_monitor monitor) {
  x_thread current_thread = x_thread_current();
  
  if (monitor->owner != current_thread) {
    o4e_woempa(9, "Thread %p cannot leave monitor %p - owner is %p\n", current_thread, monitor, monitor->owner);
    return xs_not_owner;
  }

  o4e_woempa(2, "Thread %p is leaving monitor %p, has count %i\n", current_thread, monitor, monitor->count);
  monitor->count -= 1;
  if (monitor->count == 0) {
    monitor->owner = NULL;
    
    cyg_mutex_unlock(&monitor->condmutex);
    cyg_mutex_lock(&monitor->enter_mutex);
    cyg_cond_signal(&monitor->enter_cond);	// notify one thread that he may enter the monitor
    cyg_mutex_unlock(&monitor->enter_mutex);	// in case one is waiting 
    o4e_woempa(2, "Thread %p no longer owns monitor %p, count now 0\n", current_thread, monitor);
  }
  else {
    o4e_woempa(2, "Thread %p still owns monitor %p, count now %d\n", current_thread, monitor, monitor->count);
  }

  return xs_success;
}

x_status x_thread_stop_waiting(x_thread thread) {
  x_monitor monitor = thread->waiting_on;
  if (monitor != NULL) {
    o4e_woempa(8, "Thread %p will stop waiting on monitor %p\n", thread, monitor);
    
    cyg_thread_release(thread->o4e_thread);
    
    o4e_woempa(8, "Thread %p has stopt waiting on monitor %p\n", thread, monitor);
    return xs_success;
  }
  else{
  	o4e_woempa(2,"Thread  %p wasn`t waiting on a monitor so why stop waiting? \n",thread);
  	return xs_no_instance;
  }
}

/*
 * The functions for our memory API
*/

x_size x_mem_gettotal(void){
	return total_mem;
}

x_size x_mem_avail(void){
	return free_mem;
}

// returns the base adress of the static memory pool
void* x_mem_get_stat_base(void){
	cyg_mempool_info info;
	cyg_mempool_var_get_info(spool,&info);
	return info.base;
}

void* chunk2mem(o4e_memory_chunk chunk){
	return (((char*)chunk) + sizeof(o4e_Memory_Chunk));
}

o4e_memory_chunk mem2chunk(void* mem){
	return (o4e_memory_chunk)(((char*)mem) - sizeof(o4e_Memory_Chunk));
}


void* x_mem_alloc(x_size size){
	void* new;
	x_mem_lock(x_eternal);
	if(size > MAX_SINGLE_ALLOC){
		o4e_woempa(9,"Attempt to allocate %d bytes, maximum is %d\n",size,MAX_SINGLE_ALLOC);
		x_mem_unlock();
		return NULL;
	}
	else{
		new = malloc(size+sizeof(o4e_Memory_Chunk));
	}
	
	if(new != NULL){
		o4e_memory_chunk chunk = (o4e_memory_chunk)new;
		chunk->size = size;
		chunk->check = TRUE;
		chunk->id = 0;
		// if there isn't a reference_chunk there isn't memory allocated on the heap
		if(reference_chunk == NULL){
			list_init(chunk);
			reference_chunk = chunk;
		}
		else{
			list_insert(reference_chunk,chunk);
		}
		x_mem_unlock();
		free_mem = free_mem - (size+sizeof(o4e_Memory_Chunk));
		return chunk2mem(chunk);
	}
	//problably not enough memory free
	//check this
	else{
		
		if(size > x_mem_avail()){
			o4e_woempa(9,"Attempt to allocate %d bytes, available mem is %d\n",size,x_mem_avail());
			x_mem_unlock();
			return NULL;

		}
		else{
			o4e_woempa(9,"this shouldn't happen: x_mem_alloc failed!!!!\n");
			x_mem_unlock();
			return NULL;
		}
	}
}

void x_mem_free(void* mem){
	o4e_memory_chunk chunk = mem2chunk(mem);
	x_mem_lock(x_eternal);
	if(chunk->check != TRUE){
		o4e_woempa(9,"ERROR !!!!!! THIS %p IS NOT A VALID MEMORY BLOCK\n",mem);
	}
	else{
		o4e_woempa(6,"Returning %d bytes to our heap of free memory\n",sizeof(o4e_Memory_Chunk)+chunk->size);
		if(chunk == reference_chunk){
			if(reference_chunk->next == reference_chunk){	//only one chunk = ref_chunk
				o4e_woempa(9,"There is only one mem_chunk in the ll of chunks\n");
				reference_chunk = NULL;
			}
			else{
				o4e_woempa(9,"We are removing the reference chunk\n");
				reference_chunk = chunk->next;	// make another one the reference_chunk
				list_remove(chunk);
			}
		}
		else{
			list_remove(chunk);
		}
		o4e_woempa(6,"Memory chunk %p removed from linked list of chunks\n",chunk);
		free(chunk);
		free_mem = free_mem + (sizeof(o4e_Memory_Chunk)+chunk->size);
	}
	x_mem_unlock();
}

// simple implementation of calloc
// first alloc => then make al of the memory 0
void* x_mem_calloc(x_size size){
	void* mem = x_mem_alloc(size);
	return memset(mem,0,size);
}

void* x_mem_realloc(void* old_mem, x_size size){
	o4e_memory_chunk old_chunk = mem2chunk(old_mem);
	o4e_memory_chunk new_chunk;
       	x_mem_lock(x_eternal);
	if(old_chunk->check != TRUE){
		o4e_woempa(9,"ERROR, Block of memory %p is not a valid block\n",old_mem);
		return NULL;
	}
	
	new_chunk = mem2chunk(x_mem_alloc(size));
	o4e_woempa(3,"Copying %d bytes from old mem_chunk %p to new mem_chunk %p\n",size,old_chunk,new_chunk);
	// just copy the memory, not the struct ...
	memcpy(chunk2mem(new_chunk),chunk2mem(old_chunk),size);
	// copy the chunk id !!
	new_chunk->id = old_chunk->id;
	// free the memory that we don`t need anymore
	x_mem_free(old_mem);
	x_mem_unlock();
	return chunk2mem(new_chunk);

}

x_status x_mem_lock(x_sleep timeout){
	return x_monitor_enter(memory_monitor,timeout);
}
x_status x_mem_unlock(void){
	return x_monitor_exit(memory_monitor);
}

x_status x_mem_tag_set(void* mem, x_word tag) {
  o4e_memory_chunk chunk = mem2chunk(mem);
  
  if (chunk->check != TRUE) {
    o4e_woempa(9,"Memory block %p is not valid!\n", mem);

    return xs_unknown;
  }

  o4e_woempa(5,"Marking chunk %p with id 0x%x\n", mem, tag);
  chunk->id = tag;

  return xs_success;
}

x_word x_mem_tag_get(void * mem) {
  o4e_memory_chunk chunk = mem2chunk(mem);
  
  if (chunk->check != TRUE) {
    o4e_woempa(9,"Memory block %p is not valid!\n", mem);

    return 0;

  }

  o4e_woempa(5,"Chunk %p has id 0x%x\n", mem, chunk->id);

  return chunk->id;
}

x_size x_mem_size(void * mem) {
  o4e_memory_chunk chunk = mem2chunk(mem);
  
  if (chunk->check != TRUE) {
    o4e_woempa(9,"Memory block %p is not valid!\n", mem);

    return 0;

  }

  o4e_woempa(5,"Chunk %p has size %d\n", mem, chunk->size);

  return chunk->size;
}

x_boolean x_mem_is_block(void * mem) {
  o4e_memory_chunk chunk = mem2chunk(mem);
  
  return (chunk->check == TRUE);
}

x_status x_mem_walk(x_sleep timeout, void (*callback)(void * mem, void * arg), void * arg) {
  x_status status = xs_success;
  o4e_memory_chunk cursor;
  o4e_memory_chunk next;
  // lock the memory while playing with it
  status = x_mem_lock(timeout);
  if (status != xs_success) {
    return status;
  }

  if(reference_chunk == NULL){
	o4e_woempa(9,"No memory to walk over\n");
	x_mem_unlock();
	return xs_no_instance;
  }
  if(mem_walk_owner == x_thread_current()){		// check and return is consistent with oswald API
	o4e_woempa(9,"Thread %p is calling x_mem_walk more than once\n",x_thread_current());
	x_mem_unlock();
	return xs_bad_context;
  }
  mem_walk_owner = x_thread_current();
  // walk over the memory (the for loop excludes the reference chunk)
  for (cursor = reference_chunk->next; cursor != reference_chunk; cursor = next) {
    	next = cursor->next;
	callback(chunk2mem(cursor),arg);
  }
  callback(chunk2mem(reference_chunk),arg); // now do the callback on the reference chunk
  mem_walk_owner = NULL;
  status = x_mem_unlock();

  return status;
}



static void discard_callback(void * mem, void * arg) {
  o4e_memory_chunk chunk = mem2chunk(mem);
  collect_result = (*(Collect_Result*) arg);
  
  if (isSet(chunk->id, GARBAGE_TAG)) {
    collect_result.collect_bytes += chunk->size;
    collect_result.collect_count += 1;
    x_mem_free(mem);
  }
}

void x_mem_discard(void * block) {
  if(block != NULL){
  	o4e_memory_chunk chunk = mem2chunk(block);
  	if(chunk->check != TRUE){
		o4e_woempa(9,"Not a valid memory block to discard\n");
 	}
  	else{
  		o4e_woempa(5,"Discarding memory block %p \n",block);
		setFlag(chunk->id, GARBAGE_TAG);
  	}
  }
  else
	  o4e_woempa(9,"Memory block you are trying to discard == null\n");
}

x_status x_mem_collect(x_size * bytes, x_size * num) {
  x_status status;
  //o4e_woempa(9,"-------------------------\n");
  //o4e_woempa(9,"Free mem: %d\n",free_mem);
  collect_result.collect_bytes = 0;
  collect_result.collect_count = 0;
  status = x_mem_walk(x_eternal, discard_callback, &collect_result);
  if (status == xs_success && bytes != NULL) {
    *bytes = collect_result.collect_bytes;
  }
  if (status == xs_success && num != NULL) {
    *num = collect_result.collect_count;
  }
  //o4e_woempa(9,"Free mem: %d\n",free_mem);
  //o4e_woempa(9,"Collected %d blocks and %d bytes\n",collect_result.collect_count,collect_result.collect_bytes);
  //o4e_woempa(9,"Collected total: %d\n",collect_result.collect_count*sizeof(o4e_Memory_Chunk)+collect_result.collect_bytes);
  //o4e_woempa(9,"-------------------------\n");
  return status;
}



// allocate static memory out of our static memory pool of size STATIC_MEM
// we don't use the memory pointer, we have an implementation without it
void* x_alloc_static_mem(void* memory, x_size size){
	void* new;
	new = cyg_mempool_var_try_alloc(spool,size);
	if(new == NULL){
		o4e_woempa(9,"ALLOCATION OF STATIC MEMORY FAILED, STATIC MEMORY TO SMALL\n");
		return NULL;
	}
	else return new;

}


/*
 * The functions that initialise our o4e stuff.
 * The function x_oswald_init(requested heap size, msec / tick) is called from the main function in main.c
 * x_setup_kernel will call x_os_main from the application that runs (wonka) to initialise threads
*/

void x_setup_kernel(void){
	int test;
	
	O4eEnvInit();	// initialise our o4e
	TimeInit();	// initialize our real time clock 
	test = mem_init();	// initialise our memory stuff
	// test to see if we succeeded in creating the memory
	// if not => stop and display error message
	if (test == FALSE){
		while(1){
			o4e_woempa(9,"FAILED TO INTIALISE MEMORY\n");
			cyg_thread_delay(200);
		}
	}
	o4e_woempa(9,"Before x_os_main\n");
	// let the application (wonka) define some threads
	x_os_main(command_line_argument_count, command_line_arguments, (x_ubyte*)x_mem_get_stat_base());
	o4e_woempa(9,"After x_os_main\n");
	// unlock the scheduler, other threads can start working now
	o4e_woempa(9,"Before scheduler unlock\n");
	cyg_scheduler_unlock();
	o4e_woempa(9,"After scheduler unlock\n");
	// timer ticks at this moment (used in x_time_get)
	o4eenv->timer_ticks = cyg_current_time();
	o4e_woempa(9,"Ending setup kernel !!\n");
}

x_status x_oswald_init(x_size requested_heap, x_size msec){
	cyg_thread_set_priority(cyg_thread_self(),1);
	// First thing to do: lock the scheduler => no thread can start until we are ready with our start up
	cyg_scheduler_lock();
	// For the moment we ignore the heap
	// The heap size must be set in o4e.h, it isn`t negociable for embedded systems
	// We set the msec_per_tick to calculate the exact values for the RTC in TimeInit
	msec_per_tick = msec;
	x_setup_kernel();
	return xs_success;
}

/*
** Functions for time calculations, we have changed the resolution of the realtime clock to 
** msec_per_tick
*/

x_size x_seconds2ticks(x_size seconds) {
  	int msec = seconds * 1000;
	return (x_size) (msec / msec_per_tick) ;
}

x_size x_millis2ticks(x_size millis) {
	x_size ticks = (millis / msec_per_tick);
    	return ticks ? ticks : 1; // see that you return not 0
}

x_size x_ticks2usecs(x_size ticks) {
  	
	return (msec_per_tick * ticks * 1000);
}

x_size x_usecs2ticks(x_size usecs) {
  	x_size msec = usecs / 1000;
	return (msec <= 0) ? 1 : ((x_size)(msec / msec_per_tick));
}
// we return the number of ticks that have passed
// since wonka started (since x_setup_kernel)
x_sleep x_time_get(void){
	return (x_sleep)(cyg_current_time() - o4eenv->timer_ticks);
}
