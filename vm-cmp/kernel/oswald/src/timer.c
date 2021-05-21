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
*                                                                         *
* Modifications copyright (c) 2003 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: timer.c,v 1.2 2006/09/11 13:21:39 cvsroot Exp $
*/

#include <oswald.h>

/*
** A special queue receive function for the timer handler thread. It is special since
** it does not check wether we are in a critical context. We need to do this since
** we call it with x_eternal as timeout and the thread_current is the timer handler
** and this would normally return xs_bad_context...
*/

x_status x_timer_queue_receive(x_queue queue, void ** message, x_sleep timeout);

/*
** Our internal lists of armed timers and the list of all timers (armed and inactive).
** All timers are linked through the 'next' and 'previous' fields of the timer structure.
** The list of armed timers is linked through the 'list' field of the timer structure.
**
** Note that this is a 'truncated' timer structure. We only use the first two fields.
*/

static struct {
  x_timer previous;
  x_timer next;
} All_timers;

static x_timer armed_timers = NULL;
static x_timer all_timers = (x_timer)&All_timers;
static x_size timer_id = 0;

/*
** The queue that is used to pass timers on to the handler thread. We keep the last status
** of sending a timer on the queue in 'queue_status'. Note that we can handle QUEUE_CAPACITY
** timers in the queue. If we need more, redefine QUEUE_CAPACITY.
*/

#define QUEUE_CAPACITY 30
#define QUEUE_THREAD_STACK_SIZE 8192
static x_queue timers_queue = NULL;
static x_status queue_status;

/*
** The thread that will handle the timer expiration functions and it's stack size.
*/

x_thread thread_timer;

/*
** Add a timer to the list of active timers.
*/

static void xi_timer_add_pending(x_timer new, x_sleep timeOut) {

  x_timer current;
  x_timer previous;
  x_sleep current_sticks;
  x_sleep previous_sticks;

  x_assert(critical_status);

  new->list = NULL; // see if we can move this into the clauses so that we don't have to do it each time...

  if (runtime_checks && isSet(new->flags, TIMER_ARMED)) {
    loempa(9, "Timer was allready armed!\n");
    abort();
  }

  setFlag(new->flags, TIMER_ARMED);
    
  previous = NULL;
  current_sticks = 0;
  previous_sticks = 0;
  for (current = armed_timers; current; current = current->list) {
    current_sticks += current->delta;

    if (current_sticks > timeOut) {
      if (previous) {
        new->list = previous->list;
        previous->list = new;
        new->delta = timeOut - previous_sticks;
        new->list->delta -= new->delta;
      }
      else {
        armed_timers->delta -= timeOut;
        new->delta = timeOut;
        new->list = armed_timers;
        armed_timers = new;
      }
      break;
    }
      
    previous = current;
    previous_sticks = current_sticks;

  }

  /*
  ** Check if we have linked in this timer in the previous for loop, if not
  ** it should go on the end of the line...
  */
    
  if (current == NULL) {
    if (previous) {
      previous->list = new;
    }
    else {
      armed_timers = new;
    }
    new->delta = timeOut - current_sticks;
  }

  x_assert(critical_status);

}

/*
** Remove a timer from the list of active timers.
*/

static void xi_timer_remove_pending(x_timer to_remove) {

  x_timer current;
  x_timer previous = NULL;

  x_assert(critical_status);

  if (runtime_checks && isNotSet(to_remove->flags, TIMER_ARMED)) {
    loempa(9, "Timer was not armed!\n");
    abort();
  }

  for (current = armed_timers; current; current = current->list) {
    if (current == to_remove) {
      break;
    }
    previous = current;
  }

  if (current) { // TODO, rewwrite this with *update, this if clause is useless, when we remove, there must be a 'current'

    unsetFlag(current->flags, TIMER_ARMED);

    if (! previous) {
      armed_timers = current->list;
    }
    else {
      previous->list = current->list;
    }

    /*
    ** ... and adjust the delta timeout value if there is a next timer ...
    */
    
    if (current->list) {
      current->list->delta += current->delta;
    }

  }

  x_assert(critical_status);

}

/*
** This is the function with the while(1) loop that is run in the timer handler thread. This
** thread only handles the firing function if the timer is still valid (TIMER_ARMED and TIMER_FIRED is set).
*/

static void handle_timeouts(void * t) {

  x_timer timer;
  x_status status;

  while (1) {
    status = x_timer_queue_receive(timers_queue, (void **)&timer, x_eternal);
    if (status == xs_success && isSet(timer->flags, TIMER_FIRED) && isSet(timer->flags, TIMER_ARMED)) {
      unsetFlag(timer->flags, TIMER_ARMED | TIMER_FIRED);
//      loempa(9, "Timer %d (%p) fired. Fired %d times allready...\n", timer->id, timer, timer->fired);
      timer->fired += 1;
      (*timer->timerfire)(timer);
      if (timer->repeat) {
        x_preemption_disable;
        xi_timer_add_pending(timer, timer->repeat);
        x_preemption_enable;
      }
    }
    else {
      loempa(9, "Problem with timer thread. Status = '%s'\n", x_status2char(status));
      abort();
    }
  }
  
}

/*
** Initialize the timer system.
*/

static x_Queue Queue_system;
static void *Queue_system_storage[QUEUE_CAPACITY];
static x_Thread Queue_system_thread;
static char Queue_system_stack[QUEUE_THREAD_STACK_SIZE];

void x_init_timers(void) {

  x_status status;
  
  /*
  ** Initialize our list of all timers.
  */
  
  x_list_init(all_timers);
  
  /*
  ** Create the queue on which elapsed timers will be posted.
  */
  
  timers_queue = &Queue_system;
  x_queue_create(timers_queue, Queue_system_storage, QUEUE_CAPACITY);
  
  /*
  ** Create the thread with priority 0 (the highest) that will handle the timeout function.
  */
  
  thread_timer = &Queue_system_thread;
  status = x_thread_create(thread_timer, handle_timeouts, thread_timer, Queue_system_stack, QUEUE_THREAD_STACK_SIZE, 0, TF_START | TF_TIMER);
  if (status != xs_success) {
    loempa(9, "Bad status '%s'\n", x_status2char(status));
    abort();
  }
}

/*
** Function to be called from the timer tick interrupt routine.
*/

void xi_timers_tick(void) {

  if (armed_timers) {
    armed_timers->delta -= 1;
    while (armed_timers && armed_timers->delta == 0) {
      setFlag(armed_timers->flags, TIMER_FIRED);
      queue_status = x_queue_send(timers_queue, (void *)armed_timers, x_no_wait);
      armed_timers = armed_timers->list;
    }
  }
  
}

/*
** The public timer routines.
*/

x_status x_timer_create(x_timer timer, x_timerfire timerfire, x_sleep initial, x_sleep repeat, x_flags flags) {

  x_status status;

  if (initial) {
    timer->initial = initial;
    timer->repeat = repeat;
    timer->fired = 0;
    timer->timerfire = timerfire;

    /*
    ** Make sure that the user doesn't mess up our internal flags, while preserving the only
    ** internal flag that is user settable, i.e. TIMER_AUTO_START.
    */
  
    if (isSet(flags, TIMER_AUTO_START)) {
      flags = (TIMER_USER_FLAGS & flags) | TIMER_AUTO_START;
    }
    else {
      flags = (TIMER_USER_FLAGS & flags);
    }

    timer->flags = flags;
    
    x_preemption_disable;

    timer->id = timer_id;
    timer_id += 1;
    x_list_insert(all_timers, timer);
    setFlag(timer->flags, TIMER_LINKED);
    
    /*
    ** Start the timer if required...
    */
    
    if (isSet(flags, TIMER_AUTO_START)) {
      xi_timer_add_pending(timer, initial);    
    }

    x_preemption_enable;

    status = xs_success;
        
  }
  else {
    status = xs_tick_error;
  }
  
  return status;

}

x_status x_timer_deactivate(x_timer timer) {

  x_preemption_disable;
  
  if (isSet(timer->flags, TIMER_ARMED)) {
  
    /*
    ** If the timer is armed, it can be in two states:
    **
    ** 1) it has fired allready and hasn't been handled yet by the timer handler thread (it's in the queue)
    ** 2) in the list, waiting to go off (not yet in the queue)
    **
    ** Check which case applies and handle accordingly. For 1) this means just setting the fired flag
    ** to false, while in 2), we remove the timer from the list.
    */
    
    if (isSet(timer->flags, TIMER_FIRED)) {
      unsetFlag(timer->flags, TIMER_FIRED);
    }
    else {
      xi_timer_remove_pending(timer);
    }
    
    unsetFlag(timer->flags, TIMER_ARMED);
  }

  x_preemption_enable;
    
  return xs_success;

}

x_status x_timer_delete(x_timer timer) {

  x_status status;
  
  x_preemption_disable;

  if (isSet(timer->flags, TIMER_LINKED)) {
  
    /*
    ** See if the timer is armed and maybe fired allready, but is not yet processed. If that is the
    ** case, unarm the timer first and get it out of the list.
    */
    
    if (isSet(timer->flags, TIMER_ARMED)) {
      if (isSet(timer->flags, TIMER_FIRED)) {
        unsetFlag(timer->flags, TIMER_FIRED);
      }
      else {
        xi_timer_remove_pending(timer);
      }
    }
    
    /*
    ** Now remove if from the list of all timers.
    */
    
    x_list_remove(timer);
    unsetFlag(timer->flags, TIMER_LINKED);
    status = xs_success;
  }
  else {
    status = xs_timer_error;
  }
  
  x_preemption_enable;
  
  return status;
  
}

x_status x_timer_activate(x_timer timer) {

  x_status status;
  x_sleep timeout;

    
  if (isNotSet(timer->flags, TIMER_LINKED)) {
    status = xs_timer_error;
  }
  else if (isSet(timer->flags, TIMER_ARMED)) {
    status = xs_activate_error;
  }
  else {
    if (timer->initial) {
      timeout = timer->initial;
    }
    else {
      timeout = timer->repeat;
    }

    /*
    ** If we have a positive timeout, submit the timer to the list,
    ** otherwise report a tick error.
    */
    
    if (timeout) {
      x_preemption_disable;
      xi_timer_add_pending(timer, timeout);
      x_preemption_enable;
      status = xs_success;
    }
    else {
      status = xs_tick_error;
    }

  }
  
  return status;

}

x_status x_timer_change(x_timer timer, x_sleep initial, x_sleep repeat) {

  x_status status;

  if (isSet(timer->flags, TIMER_ARMED)) {
    status = xs_timer_error;
  }
  else if (initial) {
    x_preemption_disable;
    timer->fired = 0;
    timer->initial = initial;
    timer->repeat = repeat;
    x_preemption_enable;
    status = xs_success;
  }
  else {
    status = xs_tick_error;
  }
  
  return status;

}
