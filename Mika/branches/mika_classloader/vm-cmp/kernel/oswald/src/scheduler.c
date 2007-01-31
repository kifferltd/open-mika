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
** $Id: scheduler.c,v 1.2 2006/09/11 13:21:39 cvsroot Exp $
*/

#include <oswald.h>

volatile x_word critical_status = 0;

static const x_ubyte hard_quantums = 1;

static x_ubyte const pcb2bit[] = {
  0x01, /* 00000001 */
  0x02, /* 00000010 */
  0x04, /* 00000100 */
  0x08, /* 00001000 */
  0x10, /* 00010000 */
  0x20, /* 00100000 */
  0x40, /* 01000000 */
  0x80, /* 10000000 */
};

static x_ubyte const pcb2map[] = {
  0, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
  4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
  5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
  4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
  6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
  4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
  5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
  4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
  7, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
  4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
  5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
  4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
  6, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
  4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
  5, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
  4, 0, 1, 0, 2, 0, 1, 0, 3, 0, 1, 0, 2, 0, 1, 0,
};

/*
** The unsynchronized map for unique thread id's. When there is an overflow,
** we just bump up the overflow_id. N.B. MAX_THREADS must be a multiple of 32!
*/

static x_umap thread_ids;
#define MAX_THREADS 2048
static x_size overflow_id = MAX_THREADS;

/*
** The thread_current pointer contains the currently executing thread. The
** thread_next contains the thread that needs to be scheduled next. Both pointers
** could refer to the same thread, but when a rescheduling needs to take place,
** this will not be the case.
*/

volatile x_thread thread_current = NULL;
volatile x_thread thread_next = NULL;
x_int thread_count = 0;

/*
** The 'sleepers' is the start of the linked list of sleeping threads or threads waiting or competing for
** an event to happen within a finite amount of time. 
**
** The 'eternals' is the same kind of list for threads that are waiting or competing an infinite amount of
** time for something to happen.
**
** In the x_Thread structure, both are linked by means of the 'snext' field.
*/

volatile x_thread sleepers = NULL;
volatile x_thread eternals = NULL;

/*
** The mother of all threads...
*/

x_thread thread_init;

static x_Thread Thread_idle;
x_thread thread_idle = &Thread_idle;

/*
** The table with priority control blocks.
*/

static x_Pcb pcbs[NUM_PRIORITIES];

/*
** The list of priority control blocks to quickly select the highest available
** pcb to run.
*/

static volatile x_ubyte ready_group;
static volatile x_ubyte ready_table[NUM_GROUPS];
static volatile x_pcb   ready_pcb;
static x_pcb            soft_pcb;

/*
** Given a priority, return the priority control block.
*/

inline static x_pcb xi_prio2pcb(x_size prio) {
  return (prio < NUM_HARD_PRIORITIES) ? &pcbs[prio] : soft_pcb;
}

/*
** A non inlined version for use in other files.
*/

x_pcb x_prio2pcb(x_size prio) {
  return xi_prio2pcb(prio);
}

/*
** Return the pcb that has the highest priority with threads ready to run.
*/

inline static x_pcb xi_highest_ready_pcb(void) {

  x_ubyte y = pcb2map[ready_group];
  x_ubyte x = pcb2map[ready_table[y]];

  ready_pcb = &pcbs[(y << 3) + x];
  
  return ready_pcb;

}

/*
** The thread revolver for real time threads. Note that this is only called from
** the timer interrupt routine for realizing preemption. So it is guaranteed to
** run atomically. This also applies to the soft revolver thread.
*/

static void xi_thread_hard_next(x_pcb pcb) {

  x_thread thread = pcb->t_ready;

  x_assert(thread);

  /*
  ** Careful here. We take the ->next field. This means that the ring revolves counter clock wise.
  ** It requires us to add threads in the counter clock wise orientation in the pcb->t_ready field in 
  ** the function that makes threads ready or we could oscillate between two threads. The x_list_insert
  ** macro should be used in stead of the x_list_insert_begin macro. Since the next thread could have
  ** gotten an unfair advantage, it's quantums are checked for 0; we replenish its quantums and
  ** search further untill we find a next thread that has some quantums left.
  */

  if (thread->c_quantums == 0) {
    thread->c_quantums = thread->a_quantums;
    thread = thread->next;
//    while (thread->c_quantums == 0) {
//      thread->c_quantums = thread->a_quantums;
//      thread = thread->next;
//    }
    pcb->t_ready = thread;
  }
  else {
    thread->c_quantums -= 1;
  }
    
}

/*
** The thread revolver for soft round robin threads.
*/

static void xi_thread_soft_next(x_pcb pcb) {

  x_thread thread = pcb->t_ready;
  x_thread cursor;

  x_assert(thread);

  if (thread->c_quantums == 0) {
    for (cursor = thread->next; cursor != thread; cursor = cursor->next) {
      if (cursor->c_quantums) {
        pcb->t_ready = cursor;
        cursor->c_quantums -= 1;
        return;
      }
    }

    /*
    ** If we end up here, all threads have exhausted their quantums, we reload them, also the pending ones
    ** if there are pending ones.
    */

    if (pcb->t_pending) {
      thread = pcb->t_pending;
      cursor = thread;
      do {
        cursor->c_quantums = MAX_PRIORITY + 1 - cursor->a_prio;
        cursor = cursor->next;
      } while (cursor != thread);
    }

    if (pcb->t_ready) {
      thread = pcb->t_ready;
      cursor = thread;
      do {
        cursor->c_quantums = MAX_PRIORITY + 1 - cursor->a_prio;
        cursor = cursor->next;
      } while (cursor != thread);
    }
    
    pcb->t_ready = thread;
  }
  else {
    thread->c_quantums -= 1;
  }
    
}

/*
** Function to set up the priority control blocks. Is called once from x_kernel_setup.
*/

static x_Umap Thread_ids;
static x_word Thread_map[MAX_THREADS/32];

void x_pcbs_setup(void) {

  x_int i;
  
  for (i = 0; i < NUM_PRIORITIES; i++) {
    pcbs[i].x_pos = i & 0x07;
    pcbs[i].y_pos = i >> 3;
    pcbs[i].x_bit = pcb2bit[i & 0x07];
    pcbs[i].y_bit = pcb2bit[i >> 3];
    pcbs[i].t_pending = NULL;
    pcbs[i].t_ready = NULL;
    pcbs[i].revolver = xi_thread_hard_next;
  }

  pcbs[NUM_PRIORITIES - 1].revolver = xi_thread_soft_next;
  soft_pcb = &pcbs[NUM_PRIORITIES - 1];

  for (i = 0; i < NUM_GROUPS; i++) {
    ready_table[i] = 0x00;
  }
  
  ready_group = 0x00;

  /*
  ** Create the unsynchronized thread id map and reserve position 0 allready since that
  ** is for the init thread.
  */
  
  thread_ids = &Thread_ids;
  x_umap_create(thread_ids, MAX_THREADS, Thread_map);
  x_umap_set(thread_ids, 0);
}

/*
** Add a thread to a certain circular list.
*/

inline static void xi_thread_add_ready_list(x_pcb pcb, x_thread thread, const x_boolean advantage) {

  x_assert(irq_depth || critical_status);

  if (pcb->t_ready == NULL) {
    x_list_init(thread);
    pcb->t_ready = thread;
    
    /*
    ** Only re-evalute the ready group when we changed from an empty
    ** ready list to a non empty ready list.
    */
    
    ready_group |= pcb->y_bit;
    ready_table[pcb->y_pos] |= pcb->x_bit;
  }
  else {
    x_list_insert(pcb->t_ready, thread);

    if (advantage && thread->c_quantums) {
      thread->c_quantums -= 1;
      pcb->t_ready = thread;
    }

  }

}

inline static void xi_thread_add_pending_list(x_pcb pcb, x_thread thread) {

  x_assert(critical_status);

  if (pcb->t_pending == NULL) {
    x_list_init(thread);
    pcb->t_pending = thread;
  }
  else {
    x_list_insert_begin(pcb->t_pending, thread);
  }

}

/*
** and the reverse
*/

inline static void xi_thread_remove_ready_list(x_pcb pcb, x_thread thread) {

  x_assert(critical_status);

  x_list_remove(thread);
  
  if (pcb->t_ready == thread) {

    /*
    ** If thread->next == thread, it means no threads are ready to run and the
    ** ready list becomes empty. Only then re-evalute the ready group.
    */
      
    if (thread->next == thread) {
      pcb->t_ready = NULL;
      if ((ready_table[pcb->y_pos] &= ~pcb->x_bit) == 0) {
        ready_group &= ~pcb->y_bit;
      }
    }
    else {
      pcb->t_ready = thread->next;
    }
  }

}

inline static void xi_thread_remove_pending_list(x_pcb pcb, x_thread thread) {

  x_assert(irq_depth || critical_status);

  x_list_remove(thread);
  
  if (pcb->t_pending == thread) {
    if (thread->next == thread) {
      pcb->t_pending = NULL;
    }
    else {
      pcb->t_pending = thread->next;
    }
  }
  
}

/*
** Reschedule is not going to ask the PCB to revolve, so no quantum is consumed.
** It should only be used from within protected regions. It is an internal function
** that should be called whenever a priority could have changed.
*/

void xi_thread_reschedule(void) {

  x_pcb pcb;

  if (irq_depth == 0) {
    pcb = xi_highest_ready_pcb();
    thread_next = pcb->t_ready;
    if (thread_next != thread_current) { 
#ifdef JAVA_PROFILE
      x_long current_time = x_systime_get();
      thread_current->time_last = current_time;
      thread_next->time_delta += current_time - thread_next->time_last;
#endif
      x_thread_switch(thread_current, thread_next);
    }
  }
  
}

/*
** Move a thread from the pending state to the ready state within a pcb. If the thread
** was allready 'ready', nothing happens. This should be called from within a protected
** environment.
*/

inline static void xi_thread_make_ready(x_thread thread, const x_boolean advantage) {

  x_pcb pcb = xi_prio2pcb(thread->c_prio);

  x_assert(irq_depth || critical_status);

  if (isPending(thread)) {
    thread->state = xt_ready;
    xi_thread_remove_pending_list(pcb, thread);
    xi_thread_add_ready_list(pcb, thread, advantage);
  }
  else if (runtime_checks) {
    loempa(9, "Thread %d was not in pending state but in '%s'.\n", thread->id, x_state2char(thread));
    abort();
  }

}

/*
** Move a thread from the ready state to the pending state within a pcb. If the thread
** was allready 'pending', nothing happens. This should be called from within a protected
** environment.
*/

inline static void xi_thread_make_pending(x_thread thread, x_size state) {

  x_pcb pcb = xi_prio2pcb(thread->c_prio);

  x_assert(critical_status);

  if (isReady(thread)) {
    thread->state = state;
    xi_thread_remove_ready_list(pcb, thread);
    xi_thread_add_pending_list(pcb, thread);
  }
  else if (runtime_checks) {
    loempa(9, "Thread %d was not in ready state but in %s.\n", thread->id, x_state2char(thread));
    abort();
  }

}

/*
** Attach a thread to the pcb he belongs to and to the list he belongs to.
*/

void xi_thread_add_pcb(x_thread thread) {

  x_pcb pcb = xi_prio2pcb(thread->c_prio);

  x_assert(critical_status);

  if (isReady(thread)) {
    xi_thread_add_ready_list(pcb, thread, false);
  }
  else {
    xi_thread_add_pending_list(pcb, thread);
  }

}

/*
** Remove a thread from a certain pcb. It should only be called for threads that are in
** a non ready state. So first call xi_thread_make_pending before you call this function.
*/

inline static void xi_thread_remove_pcb(x_thread thread) {

  x_pcb pcb = xi_prio2pcb(thread->c_prio);

  x_assert(critical_status);

  if (! isReady(thread)) {
    xi_thread_remove_pending_list(pcb, thread);
  }

}

/*
** Remove a thread from a list of threads joining another thread. The thread we are joining with is in
** the field thread->joining_with.
*/

static void xi_remove_joining_thread(x_thread thread) {

  x_thread current;
  volatile x_thread * update = &thread->joining_with->l_joining_us;
  
  for (current = *update; current; current = current->l_joining_with) {
    if (current == thread) {
      *update = thread->l_joining_with;
      thread->l_joining_with = NULL;
      thread->joining_with = NULL;
      return;
    }
    update = &current->l_joining_with;
  }

  if (runtime_checks) {
    loempa(9, "Thread not found in joining list.\n");
    abort();
  }
  
}

/*
** A non static and non inlined function for waking up threads from a sleeping state. It's
** passed as a function pointer so it can not be inlined.
*/

void xi_sleep_timeout_action(x_thread thread) {

  /*
  ** Make the thread ready to run, the rescheduling will take place somewhere else
  ** and the xi_pending_tick function will get us out of the list of pending threads.
  */

  xi_thread_make_ready(thread, false);

}

/*
** Timeout action for a thread that is trying to join with another thread. Get us out of the
** list of threads trying to join and make us ready.
*/

void xi_join_timeout_action(x_thread thread) {

  xi_remove_joining_thread(thread);
  xi_thread_make_ready(thread, false);

}

/*
** A timeout function for threads 'waiting' on a monitor. This is not the same as a thread competing on
** a change in state of an event. Get us out of the list of threads 'waiting' on this monitor and make
** use ready to run again.
*/

void xi_wait_timeout_action(x_thread thread) {

  x_assert(irq_depth || critical_status);

  xi_remove_waiting_thread(thread->waiting_for, thread);

  xi_thread_make_ready(thread, false);

}

/*
** Remove a thread from the list of competing threads of an event. This function really belongs in
** event.c but is in this file so that it can be inlined. It is only called once, from within the event
** timeout function, defined below.
*/

inline static void xi_remove_competing_thread(x_event event, x_thread thread) {

  x_thread current;
  volatile x_thread * update = &event->l_competing;

  for (current = *update; current; current = current->l_competing) {
    if (current == thread) {
      *update = thread->l_competing;
      thread->l_competing = NULL;
      event->n_competing -= 1;
      return;
    }
    update = &current->l_competing;
  }

  if (runtime_checks) {

    /*
    ** If we reach this point, i.e. we did no take the return in the for loop, something is
    ** wrong.
    */

    loempa(9, "Thread %d was not on event list !!\n", thread->id);
    abort();
  }

}

void xi_event_timeout_action(x_thread thread) {

  /*
  ** Get us out of the list of threads competing for this event since we timed out
  ** and make us ready to run again. The timer tick interrupt will do the scheduling...
  */

  xi_remove_competing_thread(thread->competing_for, thread);
  xi_thread_make_ready(thread, false);

}

/*
** The timer tick function for pending threads. When a thread times out, we remove it from the
** list and execute it's timeout function. Should be called from within a critical block. This block
** is called from within the timer tick interrupt only.
*/

void xi_pending_tick(void) {

  x_assert(irq_depth);
  
  if (sleepers) {
    sleepers->sticks -= 1;
    while (sleepers && sleepers->sticks == 0) {
      (*sleepers->action)(sleepers);
      sleepers = sleepers->snext;
    }
  }

}

/*
** Add a thread to the list of sleeping threads, either the 'normal' sleepers or the
** 'eternal' sleepers. This function should be called from within a protected region
** as it doesn NO protection itself and does NO task switching itself.
*/

void xi_thread_becomes_pending(x_thread thread, x_action action, x_sleep timeOut, x_word state) {

  x_thread current;
  x_thread previous;
  x_sleep current_sticks;
  x_sleep previous_sticks;

  x_assert(critical_status);
  x_assert(thread == thread_current);
  
  thread->snext = NULL;
  thread->action = action;
  thread->wakeup = 0;

  if (timeOut == x_eternal) {
    thread->snext = eternals;
    thread->sticks = x_eternal;
    eternals = thread;
    setFlag(thread->flags, TF_ETERNAL);
  }  
  else {
    previous = NULL;
    current_sticks = 0;
    previous_sticks = 0;
    for (current = sleepers; current; current = current->snext) {
      current_sticks += current->sticks;

      if (current_sticks > timeOut) {
        if (previous) {
          thread->snext = previous->snext;
          previous->snext = thread;
          thread->sticks = timeOut - previous_sticks;
          thread->snext->sticks -= thread->sticks;
        }
        else {
          sleepers->sticks -= timeOut;
          thread->sticks = timeOut;
          thread->snext = sleepers;
          sleepers = thread;
        }
        break;
      }
      
      previous = current;
      previous_sticks = current_sticks;

    }

    /*
    ** Check if we have linked in this thread allready in the previous for loop, if not
    ** it should go on the end of the line...
    */
    
    if (current == NULL) {
      if (previous) {
        previous->snext = thread;
      }
      else {
        sleepers = thread;
      }
      thread->sticks = timeOut - current_sticks;
    }

  }

  /*
  ** Now move the thread to the pending list in the pcb.
  */

  xi_thread_make_pending(thread, state);

}

void xi_thread_remove_pending(x_thread thread) {

  x_thread current;
  volatile x_thread * update;
  x_sleep current_sticks;

  x_assert(critical_status);

  if (isSet(thread->flags, TF_ETERNAL)) {
  
    /*
    ** An eternal sleeping thread ...
    */
    
    current_sticks = x_eternal;
    update = &eternals;

    unsetFlag(thread->flags, TF_ETERNAL);
    for (current = eternals; current; current = current->snext) {
      if (current == thread) {
        *update = current->snext;
        break;
      }
      update = &current->snext;
    }

    if (current == NULL && runtime_checks) {
      loempa(9, "Thread %d (eternal sleep list) to remove not found. State = %s.\n", thread->id, x_state2char(thread));
      abort();
    }

  }
  else {
  
    /*
    ** A non eternal sleeping thread...
    */

    current_sticks = 0;
    update = &sleepers;
    
    for (current = sleepers; current; current = current->snext) {
      current_sticks += current->sticks;
      if (current == thread) {
        *update = current->snext;
        break;
      }
      update = &current->snext;
    }

    if (current == NULL && runtime_checks) {
      loempa(9, "Thread %d (non eternal sleep) to remove not found...\n", thread->id);
      abort();
    }

    /*
    ** ... and adjust the sticks value of the next thread waiting, if there is a next thread waiting ...
    */
    
    if (current->snext) {
      current->snext->sticks += current->sticks;
    }

  }

  /*
  ** ... store the number of sticks that the thread still had to go in it's sticks field.
  */
    
  thread->sticks = current_sticks;
  thread->wakeup = system_ticks;

  /*
  ** The only case were we give an unfair advantage to the thread since being removed 
  ** from the pending list before the timeout means that we have some business to do
  ** on which maybe other threads are waiting.
  */

  xi_thread_make_ready(thread, true);

}

/*
** Set the current priority of a thread. It does not change the assigned priority since
** this function is also used for the priority inversion prevention mechanisms. The external
** x_thread_priority_set also changes the assigned priority.
*/

void xi_thread_priority_set(x_thread thread, x_size nex_prio) {

  x_pcb cur_pcb;
  x_pcb nex_pcb;

  x_assert(critical_status);

  if (nex_prio == thread->c_prio) {
    return;
  }

  cur_pcb = xi_prio2pcb(thread->c_prio);
  nex_pcb = xi_prio2pcb(nex_prio);

  /*
  ** Get the thread out of the appropriate of the two lists in the current pcb and on the new pcb, after adjusting
  ** it's priority.
  */

  thread->c_prio = nex_prio;

  if (isReady(thread)) {
    xi_thread_remove_ready_list(cur_pcb, thread);
    xi_thread_add_ready_list(nex_pcb, thread, false);
  }
  else {
    xi_thread_remove_pending_list(cur_pcb, thread);
    xi_thread_add_pending_list(nex_pcb, thread);
  }

  xi_thread_reschedule();

}

/*
** The external function with protection, that changes the assigned priority.
*/

x_status x_thread_priority_set(x_thread thread, x_size nex_prio) {

  x_status status = xs_bad_argument;
  
  if (nex_prio < MAX_PRIORITY && nex_prio >= MIN_PRIORITY) {

    x_preemption_disable;
    thread->a_prio = nex_prio;
    if (thread->c_prio != nex_prio) {
      xi_thread_priority_set(thread, nex_prio);
    }
    x_preemption_enable;
    
    status = xs_success;
    
  }

  return status;
  
}

x_size x_thread_priority_get(x_thread thread) {
  return thread->c_prio;
}

/*
** Potentially signal the threads that are joining with 'thread'. If no threads were
** joining, nothing happens.
*/

void xi_signal_joiners(x_thread thread, x_flags flag, void * result) {

  x_thread joiner;
  x_thread next;

  /*
  ** See if any threads where trying to join on this thread. If that is the case, we go over the list and
  ** remove these threads from the pending list and set the appropriate flag, TF_JOIN_ENDED.
  */

  for (joiner = thread->l_joining_us; joiner; joiner = next) {
    setFlag(joiner->flags, flag);
    next = joiner->l_joining_with;
    xi_remove_joining_thread(joiner);
    xi_thread_remove_pending(joiner);
    joiner->join_result = result;
  }
  
}

/*
** This cleanup is called when a thread stops normally, i.e. it returns from
** the starting function. It will get itself of the pcb, put himself in an
** ended state and will reschedule to the next thread in line.
*/

static void xi_thread_cleanup(x_thread thread) {

  x_pcb pcb;
  
  x_preemption_disable;

  xi_thread_make_pending(thread, xt_ended);
  xi_thread_remove_pcb(thread);
  thread->next = NULL;
  thread->previous = NULL;
  x_umap_reset(thread_ids, thread->id);
  thread_count -= 1;

  xi_signal_joiners(thread, TF_JOIN_ENDED, (void *) -1);

  pcb = xi_highest_ready_pcb();
  thread_next = pcb->t_ready;

  if (thread_next == NULL) {
    loempa(9, "Stop the world. We were the only thread left...\n");
  }

  /*
  ** We exit the critical section and do a reschedule. Normally this reschedule
  ** is being done from a critical region, but in this case, we don't care since
  ** if x_preemption_enable would invoke a thread switch since an interrupt is pending, 
  ** it is ok, if no interrupt is pending we force a switch with the reschedule call.
  */

  xi_thread_reschedule();

  /*
  ** The thread is really dead now. We should not even get any further.
  */
  
  loempa(9, "We've gone to far...\n");
  abort();
  
}

/*
** The internal function to make a thread suspended. The 'cb' argument can be NULL in which
** case it behaves as a normal suspend.
*/

static x_status xi_thread_suspend(x_thread thread, x_boolean (*cb)(x_event event, void * arg), void * arg) {

  x_status status = xs_no_instance;
  x_state state;
  x_event event;
  x_boolean dont_suspend = false;
  x_size nested;

  x_preemption_disable;

  if (thread->state == xt_suspended) {
    nested = (thread->flags & TF_COUNTER_MASK) >> TF_COUNTER_SHIFT;
    nested += 1;
    thread->flags &= ~ TF_COUNTER_MASK;
    thread->flags |= (nested << TF_COUNTER_SHIFT);
    status = xs_success;
  }
  else if (thread->state != xt_ended) {

    /*
    ** If there is a callback request, do it now...
    */

    if (cb) {
      for (event = thread->l_owned; event; event = event->l_owned) {
        if (cb(event, arg) == false) {
          dont_suspend = true;
        }
      }
    }

    /*
    ** If the callback said we should not suspend, we leave immediately.
    */
    
    if (dont_suspend) {
      x_preemption_enable;
      return xs_owner;
    }

    /*
    ** If a thread is ready, we have an easy case; just make it pending with the
    ** state of xt_suspended.
    **
    ** But if the thread is in one of the pending states allready (waiting for 
    ** an event or sleeping) we remove it from the pending list first and take actions based
    ** on the type of handler.
    */

    state = thread->state;

    if (isReady(thread)) {
      setFlag(thread->flags, TF_SUSPENDED);
      xi_thread_make_pending(thread, xt_suspended);
    }
    else {

      xi_thread_remove_pending(thread);

      if (thread->action == xi_sleep_timeout_action) {
        loempa(9, "Suspended a sleeping thread.\n");
      }
      else if (thread->action == xi_wait_timeout_action) {
        loempa(9, "Suspended a thread waiting on a monitor.\n");
        xi_remove_waiting_thread(thread->waiting_for, thread);
      }
      else if (thread->action == xi_event_timeout_action)  {
        loempa(9, "Suspended a thread competing for an event.\n");
        xi_remove_competing_thread(thread->competing_for, thread);
      }
      else if (thread->action == xi_join_timeout_action)  {
        loempa(9, "Suspended a thread joining another thread.\n");
        xi_remove_joining_thread(thread);
      }
      else {
        loempa(9, "Unknown action handler...\n");
        abort();
      }

      setFlag(thread->flags, TF_SUSPENDED);

      /*
      ** Note that the xi_thread_remove_pending has made the thread ready again,
      ** so although we got into this clause because the thread was not ready, we
      ** have to make it pending again.
      */

      xi_thread_make_pending(thread, xt_suspended);

    }

    setFlag(thread->flags, TF_COUNT_ONE);

    x_set_saved_state(thread, state);
    
    status = (thread->l_owned == NULL) ? xs_success : xs_owner;

    xi_thread_reschedule();

  }

  x_preemption_enable;
  
  return status;
  
}

/*
** The two externally visible suspend calls. The first is a normal (dangerous) suspend
** call, the second allows to pass a callback pointer that is called for each event owned
** by the thread that needs suspension, so that the possibility to release a mutex or 
** monitor exists.
*/

x_status x_thread_suspend(x_thread thread) {

  return xi_thread_suspend(thread, NULL, NULL);
  
}

x_status x_thread_suspend_cb(x_thread thread, x_boolean (*cb)(x_event event, void * arg), void * arg) {

  return xi_thread_suspend(thread, cb, arg);
  
}

/*
** Resume a thread. When the thread is not in any pending state, we return
** xs_no_instance, otherwise, we make the thread ready to run and return
** xs_success.
*/

x_status x_thread_resume(x_thread thread) {

  x_status status = xs_bad_state;
  x_state state;
  x_size nested;
  
  x_preemption_disable;

  if (isPending(thread)) {
    if (isSet(thread->flags, TF_SUSPENDED)) {
      nested = (thread->flags & TF_COUNTER_MASK) >> TF_COUNTER_SHIFT;
      nested -= 1;
      if (nested) {
        thread->flags &= ~ TF_COUNTER_MASK;
        thread->flags |= (nested << TF_COUNTER_SHIFT);
      }
      else {
        unsetFlag(thread->flags, TF_SUSPENDED);
        state = x_get_saved_state(thread);
        loempa(1, "Making thread that was suspended ready. Saved state = %d\n", state);
        xi_thread_make_ready(thread, false);
        xi_thread_reschedule();
      }
      status = xs_success;
    }
    else {
      status = xs_bad_state;
    }
  }

  x_preemption_enable;
  
  return status;
  
}

/*
** The join/exit mechanism. The join mechanism is handled like a special kind of event. A thread can wait on another thread
** to exit and get the return value of the thread if it has ended within the timeout window. The exit argument is copied
** to the joining thread.
*/

inline static x_boolean xi_thread_exists(x_thread thread) {

  x_boolean found = false;
  x_size i;
  x_pcb pcb;
  x_thread t;

  x_assert(critical_status);
  
  for (i = 0; i < NUM_PRIORITIES; i++) {
    pcb = x_prio2pcb(i);

    /*
    ** Search in ready list...
    */
    
    t = pcb->t_ready;
    if (t) {
      do {
        if (t == thread) {
          found = true;
          break;
        }
        t = t->next;
      } while (t != pcb->t_ready);
    }

    /*
    ** ... if not yet found, search in pending list ...
    */
    
    t = pcb->t_pending;
    if (! found && t) {
      do {
        if (t == thread) {
          found = true;
          break;
        }
        t = t->next;
      } while (t != pcb->t_pending);
    }

    /*
    ** ... if found, stop searching.
    */
    
    if (found) {
      break;
    }

  }

  return found;
  
}

x_status x_thread_join(x_thread joining_with, void ** result, x_window window) {

  x_thread current = thread_current;
  x_status status = xs_no_instance;
  
  x_preemption_disable;
  
  /*
  ** First see if the thread we want to join with exists and is not our own thread.
  */

  if (current == joining_with) {
    status = xs_deadlock;
  }
  else if (xi_thread_exists(joining_with)) {
    if (window == x_no_wait) {
      window = 1;
    }
    
    /*
    ** Set the thread we are joing with in our own 'joining_with' field and link us
    ** in the list of the thread we want to join with. We then become pending until a 
    ** timeout occurs or the thread we want to join with has ended or called x_thread_exit
    ** or was suspended and deleted.
    */

    current->joining_with = joining_with;
    current->l_joining_with = joining_with->l_joining_us;
    joining_with->l_joining_us = current;

    xi_thread_becomes_pending(current, xi_join_timeout_action, window, xt_joining);
    xi_thread_reschedule();
    
    /*
    ** OK, we are back. Either a timeout has gotten us here, or the thread has ended. We examine our own
    ** flags for either TF_JOIN_ENDED, TF_JOIN_EXIT or TF_JOIN_DELETED. In any case, we are out of the joining list;
    ** either the timeout has gotten us out of there or the thread that ended or was deleted has gotten us out of there.
    */
    
    if (isSet(current->flags, TF_JOIN_ENDED | TF_JOIN_EXIT | TF_JOIN_DELETED)) {
      *result = current->join_result;
      unsetFlag(current->flags, TF_JOIN_ENDED | TF_JOIN_EXIT | TF_JOIN_DELETED);
      status = xs_success;
    }
  }  

  x_preemption_enable;
    
  return status;

}

void x_thread_exit(void * result) {

  x_thread thread = thread_current;
  x_event event;
  x_thread next;
  x_type type;

  x_preemption_disable;
  
  /*
  ** All events (monitors or mutexes) that are owned by this thread, are released.
  */
  
  for (event = thread->l_owned; event; event = event->l_owned) {
    type = x_event_type_get(event);
    xi_remove_owned_event(thread, event);
    if (type == xe_monitor) {
      ((x_monitor)event)->owner = NULL;
    }
    else if (type == xe_mutex) {
      ((x_mutex)event)->owner = NULL;
    }

    /*
    ** The following is essentially an x_event_signal without a reschedule
    ** taking place, we'll do that later.
    */
    
    if (event->l_competing) {
      next = event->l_competing;
      event->l_competing = next->l_competing;
      xi_thread_remove_pending(next);
      event->n_competing -= 1;
      next->l_competing = NULL;
      next->competing_for = NULL;
    }

  }

  // todo, check if we can't use xi_thread_cleanup...
  xi_thread_make_pending(thread, xt_suspended);
  xi_thread_remove_pcb(thread);
  thread->state = xt_ended;
  thread->next = NULL;
  thread->previous = NULL;
  thread_count -= 1;
  x_umap_reset(thread_ids, thread->id);
  xi_signal_joiners(thread, TF_JOIN_EXIT, result);
  xi_thread_reschedule();

}

void x_thread_switching(void) {

  thread_current->cpu.status = critical_status;
  thread_current = thread_next;
  critical_status = thread_current->cpu.status;
  thread_current->num_switches += 1;

}                                                                          

/*
** This function is called after a thread switch has been performed (not yet).
*/

void x_thread_switched(x_thread thread) {
}

/*
** This function is the entry point for each thread. It starts the thread and
** could do other householding stuff... 
*/

void x_thread_start(x_thread thread) {

  thread->state = xt_ready;
  (*thread->entry)(thread->argument);

#ifdef JAVA_PROFILE
  thread->time_delta = 0;
  thread->time_last = x_systime_get();
#endif
  
  /*
  ** Get this thread out of our system; we never return from this call...
  */

  x_preemption_disable;
  
  xi_thread_cleanup(thread);

}

/*
** Report the maximum number of stack bytes used by means of binary search for the
** preset stack content; in fact, where the preset value is overwritten...
*/

inline static x_uword * word_pointer(void * pointer) {
  return (x_uword *) ((x_size)((x_ubyte *)pointer + 3) & (x_size)~3);
}

void x_stack_info(x_thread thread, x_size * size, x_size * used, x_size * left) {

  x_uword * current = NULL;
  x_uword * previous;
  x_uword * low = (x_uword *) thread->b_stack;
  x_uword * high = (x_uword *) thread->e_stack;
  x_size s_used;

  x_preemption_disable;
  
  do {
    previous = current;
    current = word_pointer(low + ((high - low) / 2));
    if (*current == 0xaaaaaaaa) {
      low = current;
    }
    else {
      high = current;
    }
  } while (current != previous);

  s_used = thread->e_stack - (x_ubyte *)current;
  
  if (used) {
    *used = s_used;
  }
  
  if (size) {
    *size = thread->e_stack - thread->b_stack;
  }

  if (left) {
    *left = (thread->e_stack - thread->b_stack) - s_used;
  }    

  x_preemption_enable;
  
}

/*
** Create a thread.
*/

x_status x_thread_create(x_thread thread, x_entry entry, void *argument, x_ubyte *b_stack, x_size s_stack, x_size prio, x_flags flags) {

  x_word * cursor;
  x_size id;
  x_status status;
  x_stack stack = NULL;

  /*
  ** Do some checks first and return if any fails. Only the timer thread with TF_TIMER flag
  ** set is allowed priority 0.
  */

  if (prio > MAX_PRIORITY || prio < MIN_PRIORITY) {
    if (isNotSet(flags, TF_TIMER)) {
      return xs_bad_argument;
    }
  }

  if (s_stack < MIN_STACK_SIZE || entry == NULL) {
    return xs_bad_argument;
  }
  
  if (isNotSet(flags, TF_TIMER)) {
  
    /*
    ** Only TF_SUSPENDED or TF_START is allowed, never both.
    */
    
    if (isSet(flags, TF_SUSPENDED) && isSet(flags, TF_START)) {
      return xs_bad_argument;
    }
    
    /*
    ** Only TF_SUSPENDED or TF_START can be given, no other flag is acceptable.
    */
    
    if (flags & (x_word)(~(TF_SUSPENDED | TF_START))) {
      return xs_bad_argument;
    }

  }

  if (b_stack == NULL) {
    printf("Thread stack is NULL!\n");
    abort();
  }

  x_preemption_disable;
  
  if (prio > NUM_HARD_PRIORITIES) {
    thread->a_prio = prio;
    thread->c_prio = 63;
    thread->a_quantums = 1;
    thread->c_quantums = (MAX_PRIORITY + 1 - thread->a_prio) / 2;
  }
  else {
    thread->a_prio = prio;
    thread->c_prio = thread->a_prio;
    thread->a_quantums = hard_quantums;
    thread->c_quantums = thread->a_quantums;
  }

  thread->l_owned = NULL;
  thread->l_competing = NULL;
  thread->l_waiting = NULL;
  thread->waiting_for = NULL;
  thread->competing_for = NULL;
  thread->m_count = 0;
  thread->num_switches = 0;
  thread->l_joining_with = NULL;
  thread->l_joining_us = NULL;
  thread->l_exception = NULL;
  thread->join_result = NULL;
  thread->joining_with = NULL;
  thread->report = NULL;
  thread->flags = flags;

#ifdef JAVA_PROFILE
  thread->time_delta = 0;
  thread->time_last = x_systime_get();
#endif

  if (b_stack) {
    thread->b_stack = b_stack;
    thread->e_stack = b_stack + s_stack - 1;
  
    /*
    ** Set the trigger value for checking just 32 bytes shorter than the end of the stack. When the trigger is
    ** hit, Oswald stops anyway. Note again that we speak about the 'end' since the stack grows downwards from
    ** e_stack to b_stack.
    */
  
    thread->trigger = (x_word *)((x_word)(b_stack + 1024) & 0xfffffff4);

    /*
    ** Make sure that the stack begins at a word aligned boundary. It is not a problem that we
    ** 'chop' off the stack this way since the stack is full descending; this means that the
    ** stack points to the last item pushed and will be decremented before something is pushed
    ** on it. So even when this 'chopping' makes the stack point to a word, of which some of
    ** the bytes are not ours, this word will never be written into.
    */
  
    thread->e_stack = (x_word)thread->e_stack & 0xfffffff4;
  
    /*
    ** Preset the stack to 0xaaaaaaaa or 10101010101010101010101010101010 in binary for stack
    ** checking later. We start at a one word offset from the beginning of the stack, since we
    ** don't want to write in memory we potentially don't own. See the note above here too...
    */

    if (runtime_checks || debug) {
      for (cursor = (x_word *)(thread->e_stack - sizeof(x_word)); cursor >= (x_word *)thread->b_stack; cursor--) {
        *cursor = 0xaaaaaaaa;
      }
    }
  }
  else {
    thread->e_stack = (unsigned char *)stack->top;
    setFlag(thread->flags, TF_VIRTUAL_STACK);
  }
  
  thread->entry = entry;
  thread->argument = argument;
  x_stack_init(thread);
  if (isSet(flags, TF_START)) {
    thread->state = xt_ready;
  }
  else {
    thread->state = xt_suspended;
    setFlag(thread->flags, TF_COUNT_ONE);
  }

  /*
  ** Initialize the thread random number generator.
  */
  
  x_init_random(thread);
  
  /*
  ** Attach ourselves to a priority control block.
  */

  status = x_umap_any(thread_ids, &id);

  if (status == xs_success) {
    thread->id = id;
  }
  else {
    overflow_id += 1;
    thread->id = overflow_id;
  }
  thread_count += 1;

  xi_thread_add_pcb(thread);

  xi_thread_reschedule();

  x_preemption_enable;

  return xs_success;
    
}

/*
** Delete a thread. A thread must be in an ended state or a suspended state before 
** it can be deleted our a bad_state status is returned.
*/

x_status x_thread_delete(x_thread thread) {

  x_status status;
  
  x_preemption_disable;
  
  if (thread->state == xt_ended) {
  
    /*
    ** xi_thread_cleanup has done all of the work allready; this is a normal end, i.e.
    ** the thread returned from it's starting function.
    */
    
    status = xs_success;
  }
  else if (thread->state == xt_suspended) {
  
    /*
    ** Do some stuff that is normally done in xi_thread_cleanup.
    */
    
    xi_thread_remove_pcb(thread);
    thread->state = xt_ended;
    thread->next = NULL;
    thread->previous = NULL;
    thread_count -= 1;
    x_umap_reset(thread_ids, thread->id);
    xi_signal_joiners(thread, TF_JOIN_DELETED, (void *) -2);
    status = xs_success;
  }
  else {
    status = xs_bad_state;
  }

  x_preemption_enable;

  return status;
      
}

/*
** This function makes the current thread sleep for the specified amount of ticks.
*/

void x_thread_sleep(x_sleep ticks) {

  if (ticks) {
    x_preemption_disable;
    xi_thread_becomes_pending(thread_current, xi_sleep_timeout_action, ticks, xt_sleeping);
    xi_thread_reschedule();
    x_preemption_enable;
  }

}

/*
** Wake up a thread that is sleeping or joining, limited or eternal. If the thread 
** is not sleeping or joining, nothing happens and we return the xs_no_instance status.
*/

x_status x_thread_wakeup(x_thread thread) {

  x_status status = xs_no_instance;
  
  x_preemption_disable;

  if (thread->state == xt_sleeping) {
    xi_thread_remove_pending(thread);
    xi_thread_reschedule();
    status = xs_success;
  }
  else if (thread->state == xt_joining) {
    xi_remove_joining_thread(thread);
    xi_thread_remove_pending(thread);
    setFlag(thread->flags, TF_JOIN_EXIT);
    thread->join_result = (void *) -3;
    xi_thread_reschedule();
    status = xs_success;
  }

  x_preemption_enable;

  return status;
    
}

/*
** Force the current thread to give up the processor. This call guarantees that the next thread
** in row will start. This could very well be the same thread that is yielding.
*/

inline static x_status xi_thread_yield(void) {

  x_pcb pcb;

  x_assert(critical_status);

  pcb = xi_prio2pcb(thread_current->c_prio);
  if (pcb->t_ready == pcb->t_ready->next) {
    return xs_no_instance;
  }

  if (pcb->t_ready && pcb->t_ready == thread_current && pcb->t_ready->next) {
    pcb->t_ready = pcb->t_ready->next;
    xi_thread_reschedule();
  }

  return xs_success;

}

/*
** Yield to a higher priority real time thread. When there was no higher priority thread we
** yielded to, this function returns xs_no_instance, otherwise it returns xs_success.
*/

x_status x_thread_yield(void) {

  x_status status;
  
  x_preemption_disable;
  status = xi_thread_yield();
  x_preemption_enable;

  return status;
  
}

x_status x_thread_quantums_set(x_thread thread, x_size quantums) {

  x_status status = xs_bad_argument;

  if (quantums > 0 && quantums < 64) {
    x_preemption_disable;
    thread->a_quantums = quantums;
    if (thread->c_quantums > quantums) {
      thread->c_quantums = quantums;
    }
    x_preemption_enable;
    status = xs_success;
  }

  return status;

}
