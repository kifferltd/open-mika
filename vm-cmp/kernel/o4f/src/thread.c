/**************************************************************************
* Copyright (c) 2020, 2021 by KIFFER Ltd. All rights reserved.            *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       *
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,            *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,       *
* EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                      *
**************************************************************************/

#include "oswald.h"
#include "task.h"

int num_x_threads = 0;
pthread_key_t x_thread_key;

static int num_started = 0;
static int num_deleted = 0;

static int task_seq;

/*
** The different thread states written as character strings and the function to
** get them in an appropriate way.
*/

static const char * _state2char[] = {
  "Ready",                        /*  0  corresponds to 'xe_unused' event type.                                                            */
  "Mutex",                        /*  1  synchronised with event type numbers.                                                             */
  "Queue",                        /*  2  synchronised with event type numbers.                                                             */
  "Mailbox" ,                     /*  3  synchronised with event type numbers.                                                             */
  "Semaphore",                    /*  4  synchronised with event type numbers.                                                             */
  "Signals",                      /*  5  synchronised with event type numbers.                                                             */
  "Monitor",                      /*  6  synchronised with event type numbers.                                                             */
  "Block",                        /*  7  synchronised with event type numbers.                                                             */
  "Map",                          /*  8  synchronised with event type numbers.                                                             */ 
  "Joining",                      /*  9  special event state that signals that a thread is waiting for a join.                             */
  "Waiting",                      /* 10                                                                                                    */
  "Suspended",                    /* 11                                                                                                    */
  "Sleeping",                     /* 12                                                                                                    */
  "Rescheduled",                  /* 13                                                                                                    */
  "Ended",                        /* 14                                                                                                    */
  "(?)",                          /* 15                                                                                                    */
  "Newborn",                      /* 16                                                                                                    */
  "Terminated",                   /* 17                                                                                                    */
  NULL,                           /* 99                                                                                                    */
};

const char * x_state2char(x_thread thread) {
  return thread->state ? _state2char[(thread->state > LAST_XT_STATE) ? xt_unknown : thread->state] : (thread->flags & TF_COMPETING) ? "Competing" : (thread->flags & TF_RECEIVING) ? "Receiving" : (thread->flags & TF_SENDING) ? "Sending" : "Ready";
}

/*
** Map an Oswald priority to the correct priority for a certain
** scheduler. Returns an int value that can be used as a
** sched_param value.
*/
// TODO check whther oswald types are supposed to be ascending or descending!
// For now on we assume 0 means least priority.

int mapPriority(unsigned int requested) {
	return requested;
}

/*
 * REPLACED BY MACRO
 * Prototype:
 *   x_thread x_thread_current(void);
 * Description:     
 *   Returns a pointer to the currently executing thread running
 *   thread.  If no thread is executing, this service returns a null
 *   pointer.

x_thread x_thread_current() {

  TaskHandle_t handle = xTaskGetCurrentTaskHandle();
  void * t = pvTaskGetThreadLocalStoragePointer(handle, O4F_LOCAL_STORAGE_OFFSET_X_THREAD);

  return (x_thread) t;
}
 */

/*
** Prototype:
**   void x_thread_yield(void)
** Description:
**   Yield the processor.
*/

void x_thread_yield() {

  taskYIELD();

}

/*
** Link a thread in our linked list.
*/

void threadRegister(x_thread xnew) {

  BaseType_t res;
  volatile x_thread current;

  current = NULL;
  if (o4fe->status == O4F_ENV_STATUS_NORMAL) {
    res = xSemaphoreTake(o4fe->threads_mutex, portMAX_DELAY);
    if (res != pdPASS) {
      o4f_abort(O4F_ABORT_THREAD, "threadRegister: xSemaphoreTake(o4fe->threads_mutex, portMAX_DELAY) failed", res);
    }
  }

  xnew->o4f_thread_next = NULL;

  if (o4fe->threads == NULL) {
    o4fe->threads = xnew;
    current = xnew;
  }
  else {
    for (current = o4fe->threads; current != NULL; current = current->o4f_thread_next) {
      if (current->o4f_thread_next == NULL) {
        current->o4f_thread_next = xnew;
        break;
      }
    }
  }

  if (current == NULL) {
    o4f_abort(O4F_ABORT_THREAD, "threadRegister: no thread to register", 0);
  }

  num_x_threads += 1;

  if (o4fe->status == O4F_ENV_STATUS_NORMAL) {
    xSemaphoreGive(o4fe->threads_mutex);
  }
}

/*
** And the reverse, get rid of a thread in our list.
*/

void threadUnregister(x_thread thread) {

  BaseType_t res;
  x_thread current;
  x_thread previous;

  if (o4fe->status == O4F_ENV_STATUS_NORMAL) {
    res = xSemaphoreTake(o4fe->threads_mutex, portMAX_DELAY);
    if (res != pdPASS) {
      o4f_abort(O4F_ABORT_THREAD, "threadUnregister: xSemaphoreTake() failed", res);
    }
  }

  previous = NULL;
  for (current = o4fe->threads; current != NULL; current = current->o4f_thread_next) {
    if (current == thread) {
      break;
    }
    previous = current;
  }

  if (current == NULL) {
    /*
    ** This shouldn't happen.
    */
    o4f_abort(O4F_ABORT_THREAD, "threadUnregister: Thread not found!", 0);
  }
  else if (previous != NULL) {
    /*
    ** Somewhere in the middle or at the end.
    */
    previous->o4f_thread_next = current->o4f_thread_next;
  }
  else {
    /*
    ** This is the first element in the list.
    */
    o4fe->threads = current->o4f_thread_next;
  }

  num_x_threads -= 1;

  num_deleted += 1;

  if (o4fe->status == O4F_ENV_STATUS_NORMAL) {
    xSemaphoreGive(o4fe->threads_mutex);
  }

//  loempa(9, "Thread unregistered, %d deleted %d started...\n", num_deleted, num_started);
}

void start_routine(void *thread_ptr) {
  x_thread thread;

  num_started += 1;
  thread = (x_thread )thread_ptr;
  loempa(2, "setting ThreadLocalStoragePointer[%i] of task %p to %p\n", O4F_LOCAL_STORAGE_OFFSET_X_THREAD, thread->handle, thread);
  vTaskSetThreadLocalStoragePointer(NULL, O4F_LOCAL_STORAGE_OFFSET_X_THREAD, thread);
  if (thread->xref) {
    loempa(2,"Mika thread %t starting\n", thread->xref);
  }
  else {
    loempa(2,"Native thread %p starting\n", thread);
  }
// TODO set priority

  thread->state = xt_ready;

  if (thread->xref) {
    loempa(2,"Mika thread %t started\n", thread->xref);
  }
  else {
    loempa(2,"Native thread %p started\n", thread);
  }
  (*(x_entry)thread->task_function)(thread->task_parameters);
  if (thread->xref) {
    loempa(2,"Mika thread %t returned normally\n", thread->xref);
  }
  else {
    loempa(2,"Native thread %p returned normally\n", thread);
  }

  thread->state = xt_ended;
  vTaskDelete(NULL);
  vTaskSuspend(NULL);
}

static void dumpTaskState(x_thread thread) {
   switch(eTaskGetState(thread->handle)) {
      case eRunning :
         printf( "Task %s is the current Running task\n", thread->name);
         break;
      case eReady :
         printf( "Task %s is Ready to run\n", thread->name);
         break;
      case  eBlocked :
         printf( "Task %s is Blocked\n", thread->name);
         break;
      case eSuspended :
         printf( "Task %s is Suspended\n", thread->name);
         break;
      case eDeleted :
         printf( "Task %s has been Deleted\n", thread->name);
         break;
      default:
         printf("Task %s has unknown state %d\n", thread->name);
   }
}
   
/*
 * Prototype:
 *   x_status x_thread_create(x_thread thread_ptr,
 *                        void (*entry_function) (void*), void* entry_input, 
 *                        void *stack_start, w_size stack_size, w_size priority, 
 *                        w_word flags);
 * Description:
 *   This service creates an application thread that starts execution
 *   at the specified task entry function.  The stack, priority, 
 *   preemption, and time-slice are among the attributes specified by
 *   the input parameters.
 *
 *   [CG 20050601, rev. 20210517] Note that the stack_start parameter is ignored - 
 *   we let FreeRTOS allocate the stack memory. Therefore this parameter should
 *   be set to NULL.
 */

x_status x_thread_create(x_thread thread, void (*entry_function)(void*), void* entry_input, void *stack_start, w_size stack_size, w_size priority, w_word flags) {

   int status = 0;
   x_status rval = xs_success;

   loempa(2, "x_thread_create(thread %p, entry function %p, entry params %p, stack start %p, stack depth %d, priority %d, flags %08x)\n", thread, entry_function, entry_input, stack_start, stack_size, priority, flags);
   if (thread == NULL) {
     loempa(9, "Thread is %p\n", thread);
     return xs_bad_argument;
   }

   if (entry_function == NULL) {
     loempa(9, "Entry function is %p\n", entry_function);
     return xs_bad_argument;
   }

   if ((priority < 0) || (priority >= configMAX_PRIORITIES)) {
     loempa(2, "Prio is %d! EXIT\n", priority);
     return xs_bad_argument;
   }

   if (stack_start) {
     loempa(2, "O4F WARNING: stack_start is non-NULL (%p), but x_thread_create ignores stack_start\n", stack_start);
   }
   loempa(2, "x_thread_create: setting up FreeRTOS task %s\n", "");
   thread->task_function = entry_function;
   thread->waiting_on = NULL;
   thread->waiting_with = 0;
   thread->flags = 0;
   thread->stack_depth = stack_size;
   thread->task_priority = priority;
   thread->task_priority = mapPriority(priority);

   thread->task_parameters = entry_input;

   loempa(2, "x_thread_create: registering FreeRTOS task %s\n", "");
   threadRegister(thread);
   snprintf(thread->name, MAX_THREAD_NAME_LENGTH, "task_%04d", ++task_seq);

   if (flags & TF_SUSPENDED) {
     thread->state = xt_newborn;
     thread->flags = 0;
   }
   else {
     thread->state = xt_ready;
     thread->flags = 0; // WAS: 1
     //thread->name[0] = 0;
     loempa(2, "===>>>  creating task using pvTaskCode %p, pcName %s, usStackDepth %d, pvParameters %p, uxPriority %d, pxCreatedTask %p\n", 
                                       start_routine, thread->name, stack_size, (void *)thread, thread->task_priority, &thread->handle);
     status = xTaskCreate(start_routine, thread->name, stack_size, (void *)thread, thread->task_priority, &thread->handle);
     loempa(2, "===>>>  status = %d\n", status);
     if (status == errCOULD_NOT_ALLOCATE_REQUIRED_MEMORY) {
        return xs_no_mem;
     }
     else if (status != pdPASS) {
       o4f_abort(O4F_ABORT_THREAD, "x_thread_create: xTaskCreate() failed with status %d", status);
     }
   }

   return rval;
}

/*
 * Prototype:
 *   x_status x_thread_attach_current(x_thread thread_ptr);
 * Description:
 *   This service fills in a x_Thread structure corresponding to the
 *   currently executing FreeRTOS Task.
 */

x_status x_thread_attach_current(x_thread thread) {
   loempa(2, "x_thread_attach_current(%p)\n", thread);
   if (thread == NULL) {
     loempa(9, "Thread is %p\n", thread);
     return xs_bad_argument;
   }

   loempa(2, "x_thread_attach_current: setting up x_thread\ %sn", "");
   thread->task_function = NULL;

   thread->handle = xTaskGetCurrentTaskHandle();
   thread->waiting_on = NULL;
   thread->waiting_with = 0;
   thread->flags = 0;
   thread->state = xt_ready;
   thread->task_parameters = NULL;
   threadRegister(thread);

   return xs_success;
}

/*
 * Prototype:
 *   x_status x_thread_detach(x_thread thread_ptr);
 * Description:
 *   This service cleans up in a x_Thread structure
 */

x_status x_thread_detach(x_thread thread) {
   loempa(2, "x_thread_detach(%p)\n", thread);
   if (thread == NULL) {
     loempa(9, "Thread is %p\n", thread);
     return xs_bad_argument;
   }

   threadUnregister(thread);

   return xs_success;
}

/*
 * Prototype:
 *   x_status x_thread_delete(x_thread thread_ptr);
 * Description:
 *   Deletes the specified application thread.  Since the specified
 *   thread must be in a terminated or completed state, this service
 *   cannot be called from a thread attempting to delete itself.
 */

x_status x_thread_delete(x_thread thread) {
  x_status rval = xs_success;
  void *status;

  /*
  ** Check if we are calling ourselves, if yes, 
  ** return an error since this is impossible.
  */

  if (xTaskGetCurrentTaskHandle() == thread->handle) {
    rval = xs_bad_state;
  }
  else {
    loempa(2, "Thread %p is being deleted\n", thread);
    threadUnregister(thread);
      
  }

  return rval;
}

 
/*
 * Prototype:
 *   w_int x_thread_priority_set(x_thread thread_ptr, 
 *                               w_size new_priority);
 * Description:
 *   Changes the priority of the specified thread.  Valid priorities
 *   range from 0 through (configMAX_PRIORITIES - 1), where 0 represents the 
 *   highest priority level. 
 */
 
x_size x_thread_priority_set(x_thread thread, w_size new_priority) {
  x_size old_priority = thread->task_priority;

  if (new_priority >= configMAX_PRIORITIES) {
    loempa(9, "x_thread_priority_set(): priority %d is out of range, ignoring\n", new_priority);
  }
  else {
    // map 0 (highest priority) to (configMAX_PRIORITIES – 1), (configMAX_PRIORITIES - 1) to 1
    // WAS: thread->task_priority = configMAX_PRIORITIES - 1 - ((configMAX_PRIORITIES - 2)*new_priority/NUM_PRIORITIES);
    thread->task_priority = new_priority;
    loempa(2, "oswald priority %d maps to FreeRTOS priority %d of %d\n", new_priority, thread->task_priority, configMAX_PRIORITIES);
  }

  return old_priority;

}

w_size x_thread_priority_get(x_thread thread) {
  return thread->task_priority;
}
 
/*
 * Prototype:
 *   x_status x_thread_resume(x_thread thread_ptr);
 * Description:
 *   Resumes a thread that was previously created without an automatic start.
 */
 
x_status x_thread_resume(x_thread thread) {
  x_status retValue = xs_success;

  if (thread->state == xt_newborn) {
    int status;

    loempa(2, "Starting new born thread %p\n", thread);
    thread->state = xt_ready;
    loempa(7, "===>>>  creating task using pvTaskCode %p, pcName %s, usStackDepth %d, pvParameters %p, uxPriority %d, pxCreatedTask %p\n", 
                                       start_routine, thread->name, thread->stack_depth, (void *)thread, thread->task_priority, &thread->handle);
    status = xTaskCreate(start_routine, thread->name, thread->stack_depth, (void *)thread, thread->task_priority, &thread->handle);
    loempa(7, "===>>>  status = %d\n", status);
    if (status == errCOULD_NOT_ALLOCATE_REQUIRED_MEMORY) {
       return xs_no_mem;
    }
    else if (status != pdPASS) {
      o4f_abort(O4F_ABORT_THREAD, "xTaskCreate() failed with status %d", status);
      return xs_no_instance;
    }
  }
  else {
    thread->state = xt_ready;
    vTaskResume(thread->handle);
  }
  
  return retValue;
} 


/* Prototype:
 *   x_status x_thread_sleep(x_sleep timer_ticks);
 * Description:
 *   This operation causes the calling thread to suspend for the
 *   specified number of timer ticks.
 */
 
x_status x_thread_sleep(x_sleep timer_ticks) {
  x_thread thread;
  int retcode;

  thread = x_thread_current();

  if (thread == NULL) {
    /*
    ** We were cancelled before we got the chance of starting to sleep...
    */
    return xs_success;
  }

  thread->state = xt_sleeping;

  if (x_eternal == timer_ticks) {
    for (;;) {
      vTaskDelay(pdMS_TO_TICKS(1000));
    }
  }
  else {
    vTaskDelay(timer_ticks);
  }

  unsetFlag(thread->flags, TF_TIMEOUT);

  thread->state = xt_ready;

  return xs_success;
}

/*
 * Prototype:
 *   x_status x_thread_suspend(x_thread thread_ptr);
 * Description:
 *   Suspends the specified application thread.  A thread may call
 *   this service to suspend itself.  Once suspended, the thread must
 *   be resumed by calling 'x_thread_resume' in order to execute
 *   again.
 */

x_status x_thread_suspend(x_thread thread) {  
  x_status retValue = xs_success;

  thread->state = xt_suspended;
  vTaskSuspend(thread->handle);

  return xs_success;
}

x_status x_thread_join(x_thread joinee, void **result, x_sleep timeout) {
  x_status status = xs_unknown;
  x_thread joiner = x_thread_current();
  struct timespec one_tick_ts;
  struct timespec end;

  if (!joinee->state) {
    return xs_success;
  }

  one_tick_ts.tv_sec = 0;
  one_tick_ts.tv_nsec = 1000 * x_ticks2usecs(1);
  
  joiner->state = xt_joining;

  if (timeout == x_eternal) {
     while (1) {
       if (joinee->state >= xt_ended) {
         break;
       }

// vTaskDelay(1) will block the calling state for 1 tick,
// vTaskDelay(0) is equivalent to taskYIELD(), which means
// that it will not yield to a lower-priority task ...
// so we use a1-tick delay if the target task has lower priority.
       taskYIELD();
     }
  }
  else {
     x_now_plus_ticks((x_long)timeout, &end);

     do {
       if (joinee->state >= xt_ended) {
         break;
       }

       vTaskDelay(joinee->task_priority < joiner->task_priority);
     } while (!x_deadline_passed(&end));
  }

  joiner->state = xt_ready;

  if (isSet(joiner->flags, TF_TIMEOUT)) {
    unsetFlag(joiner->flags, TF_TIMEOUT);
    status = xs_no_instance;
  }
  else {
    status = xs_success;
  }

  return status;
}

/*
** Wake up a thread that is sleeping or joining, limited or eternal. If the thread
** is not sleeping or joining, nothing happens and we return the xs_no_instance status.
*/

x_status x_thread_wakeup(x_thread thread) {
  xTaskNotify(thread->handle, 0, eNoAction);
 
  return xs_success;
}

x_status x_thread_stop_waiting(x_thread thread) {
  x_monitor monitor = thread->waiting_on;

  if (monitor) {
    loempa(2, "Thread %p will stop thread %p from waiting on %p\n", x_thread_current(), thread, monitor);
    return x_monitor_stop_waiting(monitor, thread);
  }

  return xs_no_instance;
}

x_status x_thread_signal(x_thread thread, w_int signum) {
  o4f_abort(O4F_ABORT_THREAD, "function x_thread_signal() is not implemented", 0);
  return xs_unknown;
}

