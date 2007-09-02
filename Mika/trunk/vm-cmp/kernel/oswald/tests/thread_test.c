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
** $Id: thread_test.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <string.h>
#include <tests.h>
#include <oswald.h>

static x_sleep second;

#define THRT_STACK_SIZE ((1024 * 2) + MARGIN)

/*

                   cd_control                |                cd_thread
---------------------------------------------+-----------------------------------------------
 if state == 0, alloc mem for and create     |
 cd_thread, ready to start. Set state to     |
 1 and sleep. --------------------------------> if state == 1, set state to 2 and sleep.   
                                             |                    :
                                             |                    :
                                             |  if state == 2, set state to 3 and return.
                                             |  This thread stops effectively.
                                             | =========================================
                                             |
 if state == 3, release the memory, alloc    |
 new memory and create thread TF_SUSPENDED.  |
 Sleep.             :                        |  
                    :                        |
 Set state to 4 and start by means of a      |
 x_thread_resume the cd_thread. --------------> if state == 4, set state to 5 and sleep.
                                             |                    :
                                             |                    :
                                             |  if state == 5, set state to 6 and return.
                                             |  This thread stops effectively.
                                             | =========================================
                                             |
 When state == 6, release memory, set state  |
 to 7, create thread again with prio == 3,   |
 set ourselves to prio == 2 and yield..........(Check that state 7 never appears...)
 Set state to 8 and our priority to 4, and   |
 yield. --------------------------------------> If state == 8, set state to 9, set prio of  
                                             |  cd_control thread to 2 and yield.
                                             |                               |
 Check that state has changed to 9 and      <--------------------------------+
 sleep. --------------------------------------> If state == 9, set to 10 and sleep.
                                             |
 If state == 10, set state to 11 and suspend |
 this thread. --------------------------------> If state == 11, check that cd_control is
                                             |  in the suspended mode. Set state to 12
                                             |  and resume cd_control thread. Sleep
                                             |                                  |
 State should be 12, set state to 13 and    <-----------------------------------+
 sleep. --------------------------------------> If state == 13, suspend cd_control and
                                             |  set state to 14, sleep, set state to 15
                                             |  and resume cd_control. Return
 If state == 15, release memory and set     <-- This thread stops effectively.
 state to 0.                                 | ========================================= 
                                             |
                                             |

During the different phases or states of the state machine, we check that the different
thread fields are correct and that the thread is registered or unregistered properly in
the priority control blocks.

*/

static x_thread argument_thread;

static x_thread cb_thread;

static x_thread cd_thread;
static x_ubyte * cd_stack;
static x_thread cd_control;
static x_int cd_counter = 0;

static x_int cd_state = 0;

static x_thread many_thread;
static x_int many_state = 0;
static x_size many_created = 0;
static x_int warnings = 0;
static x_size warnings_number; // Number of threads running when last warning was issued

typedef struct t_many {
  x_thread thread;
  x_ubyte * stack;
} t_many;

static t_many *manys;

static void create_entry(void * t) {

  x_status status;
  x_thread thread = t;

  while (1) {

    x_assert(critical_status == 0);

//    oempa("Second state = %d\n", cd_state);  
    /*
    ** We check this state before we sleep...
    */
    
    if (cd_state == 7) {
      oempa("Should not see state 7 (this prio = %d, control prio = %d) !\n", thread->c_prio, cd_control->c_prio);
      exit(0);
    }
    
    if (cd_state == 1) {
      cd_state = 2;      
    }
    
    if (cd_state == 4) {
      cd_state = 5;
    }

    /*
    ** See if we are in a state to stop...
    */
    
    if (cd_state == 2) {
      cd_state = 3;
      return;
    }

    if (cd_state == 5) {
      cd_state = 6;
      return;
    }
    
    if (cd_state == 8) {
      cd_state = 9;
      if (thread->a_prio != 3 + prio_offset) {
        oempa("Bad priority %d != %d\n", thread->a_prio, 3 + prio_offset);
        exit(0);
      }
      x_thread_priority_set(cd_control, 2);// <----- between this point and this point -------+
    }                                  //                                                 |
                                       // We handed over control to the controller thread |
//  if (cd_state == 10) {              // that has checked that the state is really set to |
//    return;                          // 9.                                              |
//  }                                  //                                                 |
                                       //                                                 |
    if (cd_state == 9) {               // <-----------------------------------------------+
      cd_state = 10;
      x_thread_sleep(10);
    }

    if (cd_state == 11) {
      cd_state = 12;

      if (cd_control->state != xt_suspended) {
        oempa("Bad state %s\n", x_state2char(cd_control));
        exit(0);
      }

      if (isNotSet(cd_control->flags, TF_SUSPENDED)) {
        oempa("Bad flag 0x%08x\n", cd_control->flags);
        exit(0);
      }

      status = x_thread_resume(cd_control);
      if (status != xs_success) {
        oempa("Bad state '%s'\n", x_status2char(status));
        exit(0);
      }
      x_thread_sleep(2);
    }
    
    if (cd_state == 13) {
      status = x_thread_suspend(cd_control);
      if (status != xs_success) {
        oempa("Bad state '%s'\n", x_status2char(status));
        exit(0);
      }
      cd_state = 14;
      x_thread_sleep(4);
      cd_state = 15;
      status = x_thread_resume(cd_control);
      if (status != xs_success) {
        oempa("Bad state '%s'\n", x_status2char(status));
        exit(0);
      }
      return;
    }

  }
  
}

static void create_delete(void * t) {

  x_int found;
  x_thread thread = t;
  x_status status;
  
  while (1) {

    x_assert(critical_status == 0);

    if (cd_thread) {
//      oempa("State = %d c = %d, t = %d (%s)\n", cd_state, cd_control->c_prio, cd_thread->c_prio, x_state2char(cd_thread));
    }

    if (cd_state == 0) {
      cd_thread = x_mem_get(sizeof(x_Thread));
      memset(cd_thread, 0xff, sizeof(x_Thread));
      cd_stack = x_mem_get(THRT_STACK_SIZE);
      status = x_thread_create(cd_thread, create_entry, cd_thread, cd_stack, THRT_STACK_SIZE, prio_offset + 4, TF_START);
      if (status != xs_success) {
        oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      found = find_thread_in_pcbs(cd_thread);

      if (found == 0) {
        oempa("Thread not found!\n");
        exit(0);
      }
      
      if (!isReady(cd_thread)) {
        oempa("Thread state not ready but %d!\n", cd_thread->state);
        exit(0);
      }
      
      cd_state = 1;
    }
    
    if (cd_state == 3) {
      if (cd_thread->state != xt_ended) {
        oempa("Bad state...\n");
        exit(0);
      }
      
      found = find_thread_in_pcbs(cd_thread);
      if (found != 0) {
        oempa("Should not be found %d...\n", found);
        exit(0);
      }
      
      x_mem_free(cd_thread);
      x_mem_free(cd_stack);

      cd_thread = x_mem_get(sizeof(x_Thread));
      memset(cd_thread, 0xff, sizeof(x_Thread));
      cd_stack = x_mem_get(THRT_STACK_SIZE);
      status = x_thread_create(cd_thread, create_entry, cd_thread, cd_stack, THRT_STACK_SIZE, prio_offset + 2, TF_SUSPENDED);
      if (status != xs_success) {
        oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }

      found = find_thread_in_pcbs(cd_thread);
      if (found != 2) {
        oempa("Thread in bad list...\n");
        exit(0);
      }

      x_thread_sleep(10);

      cd_state = 4;
      
      x_thread_resume(cd_thread);
    }

    if (cd_state == 6) {
      x_mem_free(cd_thread);
      x_mem_free(cd_stack);

      /*
      ** Start new thread, again...
      */
      
      cd_state = 7;
      x_thread_priority_set(thread, 2);
      cd_thread = x_mem_get(sizeof(x_Thread));
      memset(cd_thread, 0xff, sizeof(x_Thread));
      cd_stack = x_mem_get(THRT_STACK_SIZE);
      status = x_thread_create(cd_thread, create_entry, cd_thread, cd_stack, THRT_STACK_SIZE, prio_offset + 3, TF_START);
      if (status != xs_success) {
        oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      x_thread_yield();

      cd_state = 8;
      x_thread_priority_set(thread, 5);
      x_thread_yield();

      /*
      ** Only check the state when running hard priority tests.
      */

      if (cd_state != 9 && prio_offset == 0) {
        oempa("Bad state %d\n", cd_state);
        exit(0);
      }

    }

    if (cd_state == 10) {
      cd_state = 11;
            
      status = x_thread_suspend(thread);
      if (status != xs_success) {
        oempa("Bad state '%s'\n", x_status2char(status));
        exit(0);
      }

      /*
      ** We have been resumed...
      */

      if (cd_state != 12) {
        oempa("Bad state %d\n", cd_state);
        exit(0);
      }
      
      if (isSet(thread->flags, TF_SUSPENDED)) {
        oempa("Bad flag 0x%08x\n", thread->flags);
        exit(0);
      }
      
      if (thread->state != xt_ready) {
        oempa("Bad thread state %d\n", thread->state);
        exit(0);
      }
    
      x_thread_priority_set(thread, 4);
      cd_state = 13;
      
    }

    if (cd_state == 15) {
      x_thread_priority_set(thread, 4); // changed from 5 to 4
      if (cd_thread->state != xt_ended) {
        oempa("Thread should be dead, not %s\n", x_state2char(cd_thread));
        exit(0);
      }
      x_mem_free(cd_thread);
      x_mem_free(cd_stack);
      cd_counter += 1;
      oempa("Thread create/delete machine ran %d times; %d threads in system.\n", cd_counter, thread_count);
      cd_state = 0;
      x_thread_sleep(second * 20);
    }

    /*
    ** Check error states before and after the sleep...
    */

    if (cd_state == 14) {
      oempa("Bad state %d\n", cd_state);
      exit(0);
    }
    
    x_thread_sleep(100);

    if (cd_state == 14) {
      oempa("Bad state %d\n", cd_state);
      exit(0);
    }
  
    x_thread_sleep(100);
    
  }  
  
}

static x_int many_counter = 0;

static void do_stuff(void * t) {

  x_thread thread = t;
  
  while (1) {

    x_assert(critical_status == 0);

    many_counter += 1;
    
    if (many_counter % 100 == 0) {
      oempa("Thread %d (prio %d) -> %d\n", thread->id, thread->a_prio, many_counter);
    }
    
    if (many_state == 2) {
      return;
    }
    
    x_thread_sleep(x_random() % second + 5);

  }
  
}

#define MAX_LIVING_COUNTER (500)

static void many(void * t) {

  x_int number = 0;
  x_int counter = 0;
  x_int i;
  x_boolean still_living;
  x_int loop = 0;
  x_int living_counter;
  x_size prio;
  x_status status;
  
  while (1) {

    x_assert(critical_status == 0);

    counter += 1;
    
    if (many_state == 0) {
      loop += 1;
      number = x_random() % 50 + 20;
      many_created += number;
      manys = x_mem_get(sizeof(t_many) * number);
      for (i = 0; i < number; i++) {
        manys[i].thread = x_mem_get(sizeof(x_Thread));
        manys[i].stack = x_mem_get(THRT_STACK_SIZE);
        prio = (x_random() % 3) + 2;
//        prio = 5;
        status = x_thread_create(manys[i].thread, do_stuff, manys[i].thread, manys[i].stack, THRT_STACK_SIZE, prio_offset + prio, TF_START);
        if (status != xs_success) {
          oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
          exit(0);
        }
      }
      oempa("Loop %d; %d created, %d threads now, total %d, warnings = %d (%d total at warning)...\n", loop, many_created, number, thread_count, warnings, warnings_number);
      many_state = 1;
    }

    if (counter % 4 == 0) {
      many_state = 2;
      still_living = true;
      living_counter = 0;
      while (still_living && living_counter < MAX_LIVING_COUNTER) {
        still_living = false;
        for (i = 0; i < number; i++) {
          if (manys[i].thread->state != xt_ended) {
            still_living = true;
            living_counter += 1;
            break;
          }
        }
        x_thread_sleep(50);
      }
      
      if (living_counter == MAX_LIVING_COUNTER) {
        oempa("Some threads wouldn't die...\n");
        exit(0);
      }
      
      for (i = 0; i < number; i++) {
        x_mem_free(manys[i].thread);
        x_mem_free(manys[i].stack);
      }
      
      x_mem_free(manys);
      counter = 0;
      many_state = 0;
    }
    
    x_thread_sleep(second * 5);
    
  }
  
}

/*
** Check to see that stacks are not messed up across thread switches. In the long running
** tests, we should have been interrupted quite a few times. We check that this happens
** correctly with this test. We define the function afterwards to make sure that it
** doesn't get inlined and optimized away...
*/

extern x_int check_arguments(x_int one, x_int two, x_int three, x_int four, x_int five, x_int six, x_int seven, x_int eight, x_int nine, x_int ten);

static void argument(void * t) {

  x_int counter = 0;
  x_int result;
  
  while (1) {

    x_assert(critical_status == 0);

    counter += 1;
    result = check_arguments(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    if (result != 55) {
      oempa("Result is wrong 55 != %d.\n", result);
      exit(0);
    }
    if (counter % 100 == 0) {
      oempa("Checked arguments and result %d times.\n", counter);
    }
    x_thread_sleep(second);
  }
  
}

x_int check_arguments(x_int one, x_int two, x_int three, x_int four, x_int five, x_int six, x_int seven, x_int eight, x_int nine, x_int ten) {

  if (one != 1 || two != 2 || three != 3 || four != 4 || five != 5) {
    oempa("Arguments: %d %d %d %d %d %d %d %d %d %d\n", one, two, three, four, five, six, seven, eight, nine, ten);
    exit(0);
  }
  
  if (six != 6 || seven != 7 || eight != 8 || nine != 9 || ten != 10) {
    oempa("Arguments: %d %d %d %d %d %d %d %d %d %d\n", one, two, three, four, five, six, seven, eight, nine, ten);
    exit(0);
  }
  
  return one + two + three + four + five + six + seven + eight + nine + ten;
  
}

/*
** Test callback suspend mechanism...
*/

typedef struct cb_Event {
  union {
    x_Mutex Mutex;
    x_Monitor Monitor;
  } event;
  int state; /* 0 = empty, 1 = free mutex, 2 = owned mutex, 3 = free monitor, 4 = owned monitor */
} cb_Event;

/*
** The argument for the callback function.
*/

typedef struct CBA {
  int current;
  int found;
  int total;
} CBA;

static CBA cba = { 0, 0, 0 };

static cb_Event * cb_events;
static int cb_num;
static int num_locked;
static int round;
static int rounds;

static int cb_state = 0;

static void take_events(void * t) {

  int command;
  int which;
  int what;
  int free_slots = cb_num;
  x_mutex mut;
  x_monitor mon;
  x_status status;
  int saved_round = 100;

  while (1) {

    x_assert(critical_status == 0);
  
    if (cb_state != 1) {
      x_thread_sleep(10);
      continue;
    }
    
    command = x_random() % 3;
    which = x_random() % cb_num;

    if (command == 0 && free_slots > 0) {

      /*
      ** Create a new event given there is a free slot...
      */

      what = x_random() % 2;
      if (cb_events[which].state == 0) {
        free_slots -= 1;
        num_locked += 1;
        if (what == 0) {
          mut = & cb_events[which].event.Mutex;
          status = x_mutex_create(mut);
          if (status != xs_success) {
            oempa("Bad status '%s'\n", x_status2char(status));
            exit(0);
          }
          status = x_mutex_lock(mut, x_eternal);
          if (status != xs_success) {
            oempa("Bad status '%s'\n", x_status2char(status));
            exit(0);
          }
          cb_events[which].state = 2;
        }
        else {
          mon = & cb_events[which].event.Monitor;
          status = x_monitor_create(mon);
          if (status != xs_success) {
            oempa("Bad status '%s'\n", x_status2char(status));
            exit(0);
          }
          status = x_monitor_enter(mon, x_eternal);
          if (status != xs_success) {
            oempa("Bad status '%s'\n", x_status2char(status));
            exit(0);
          }
          cb_events[which].state = 4;
        }
      }
    }
    else if (command == 1) {

      /*
      ** unlock an event if it is locked
      */

      if (cb_events[which].state == 2) {
        status = x_mutex_unlock(& cb_events[which].event.Mutex);
        if (status != xs_success) {
          oempa("Bad status '%s'\n", x_status2char(status));
          exit(0);
        }
        cb_events[which].state = 1;
        num_locked -= 1;
      }
      else if (cb_events[which].state == 4) {
        status = x_monitor_exit(& cb_events[which].event.Monitor);
        if (status != xs_success) {
          oempa("Bad status '%s'\n", x_status2char(status));
          exit(0);
        }
        cb_events[which].state = 3;
        num_locked -= 1;
      }
    }
    else {

      /*
      ** lock an event if it is unlocked
      */

      if (cb_events[which].state == 1) {
        status = x_mutex_lock(& cb_events[which].event.Mutex, x_eternal);
        if (status != xs_success) {
          oempa("Bad status '%s'\n", x_status2char(status));
          exit(0);
        }
        cb_events[which].state = 2;
        num_locked += 1;
      }
      else if (cb_events[which].state == 3) {
        status = x_monitor_enter(& cb_events[which].event.Monitor, x_eternal);
        if (status != xs_success) {
          oempa("Bad status '%s'\n", x_status2char(status));
          exit(0);
        }
        cb_events[which].state = 4;
        num_locked += 1;
      }
    }
    if (saved_round != round) {
      oempa("Round %d of %d, %d locked of %d.\n", round, rounds, num_locked, cb_num);
      saved_round = round;
    }
    x_thread_sleep(5);
  }
  
}

static x_boolean callback(x_event event, void * arg) {

  x_status status;
  CBA * a = arg;
  cb_Event * e;  
  int i;
  
  a->current += 1;

  for (i = 0; i < cb_num; i++) {
    e = & cb_events[i];
    if (e->state == 1 || e->state == 2) {
      if ((x_mutex)event == & e->event.Mutex) {
        oempa("Found mutex at %d (%d of %d)\n", i, a->current, a->total);
        status = x_mutex_release((x_mutex)event);
        if (status != xs_success) {
          oempa("Bad status '%s'\n", x_status2char(status));
          exit(0);
        }
        a->found += 1;
        break;
      }
    }
    else if (e->state == 3 || e->state == 4) {
      if ((x_monitor)event == & e->event.Monitor) {
        oempa("Found monitor at %d (%d of %d)\n", i, a->current, a->total);
        status = x_monitor_release((x_monitor)event);
        if (status != xs_success) {
          oempa("Bad status '%s'\n", x_status2char(status));
          exit(0);
        }
        a->found += 1;
        break;
      }
    }
  }

  return true;

}


static void cb_test(void *t) {

  x_thread owner = NULL;
  x_ubyte * owner_stack = NULL;
  int i;
  x_status status;
  int tests = 0;
  
  while (1) {

    x_assert(critical_status == 0);
    
    if (cb_state == 0) {
      round = 0;
      owner = x_mem_get(sizeof(x_Thread));
      memset(owner, 0xff, sizeof(x_Thread));
      owner_stack = x_mem_get(THRT_STACK_SIZE);
      status = x_thread_create(owner, take_events, owner, owner_stack, THRT_STACK_SIZE, prio_offset + 5, TF_START);
      if (status != xs_success) {
        oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      
      cb_num = (x_random() % 10) + 4;
      cb_events = x_mem_get(sizeof(cb_Event) * cb_num);
      for (i = 0; i < cb_num; i++) {
        cb_events[i].state = 0;
        memset(& cb_events[i].event.Monitor, 0xff, sizeof(x_Monitor));
      }
      num_locked = 0;
      rounds = x_random() % 50 + 5;
      cb_state = 1;
      tests += 1;
      oempa("Created array for %d events, %d rounds (%d total tests, %d total threads).\n", cb_num, rounds, tests, thread_count);
    }

    if (cb_state == 1) {
      round += 1;
      if (round == rounds) {
        cb_state = 2;
      }
    }

    if (cb_state == 2) {
      cba.total = num_locked;
      cba.current = 0;
      cba.found = 0;
      status = x_thread_suspend_cb(owner, callback, & cba);
      if (status != xs_success) {
        oempa("Bad status '%s'\n", x_status2char(status));
        exit(0);
      }
      if (cba.found != cba.total) {
        oempa("Missed an owned event: %d != %d\n", cba.found, cba.total);
        exit(0);
      }
      status = x_thread_delete(owner);
      if (status != xs_success) {
        oempa("Bad status '%s'\n", x_status2char(status));
        exit(0);
      }
      x_mem_free(owner);
      x_mem_free(owner_stack);
      for (i = 0; i < cb_num; i++) {
        if (cb_events[i].state == 1 || cb_events[i].state == 2) {
          status = x_mutex_delete(& cb_events[i].event.Mutex);
          if (status != xs_success) {
            oempa("Bad status '%s'\n", x_status2char(status));
            exit(0);
          }
        }
        else if (cb_events[i].state == 3 || cb_events[i].state == 4) {
          status = x_monitor_delete(& cb_events[i].event.Monitor);
          if (status != xs_success) {
            oempa("Bad status '%s'\n", x_status2char(status));
            exit(0);
          }
        }
      }
      x_mem_free(cb_events);
      cb_state = 0;
    }
    
    x_thread_sleep(second);

  }
  
}

/*
** Sleep and wakeup tests...
*/

static x_thread sleep_thread;
static x_thread wakeup_thread;

#define SWUT_STACK_SIZE ((1024 * 2) + MARGIN)

static void do_sleep(void *arg) {

  x_size now_time;
  x_thread thread = thread_current;
  
  while (1) {
    x_assert(critical_status == 0);
    oempa("Thread %d is now going to sleep...\n", thread->id);
    now_time = system_ticks;
    x_thread_sleep(x_eternal);
    oempa("Eternal: thread %d is alive again after %d ticks!\n", thread->id, system_ticks - now_time);
    now_time = system_ticks;
    x_thread_sleep(second * 20);
    oempa("Limited: thread %d is alive again after %d ticks!\n", thread->id, system_ticks - now_time);
  }
  
}

static void do_wakeup(void *arg) {

  x_status status;
  x_thread thread = thread_current;
  
  x_thread_sleep(10);
  
  while (1) {
    x_assert(critical_status == 0);
    oempa("Thread %d is going to wakeup the sleeper...\n", thread->id);
    status = x_thread_wakeup(sleep_thread);
    oempa("Wakeup status is '%s'.\n", x_status2char(status));
    x_thread_sleep(second * 10);
  }

}

static x_ubyte * sleep_wakeup_test(x_ubyte * memory) {

  x_ubyte * stack;
  x_status status;

  sleep_thread = x_alloc_static_mem(memory, sizeof(x_Thread));
  wakeup_thread = x_alloc_static_mem(memory, sizeof(x_Thread));
  
  stack = x_alloc_static_mem(memory, SWUT_STACK_SIZE);
  status = x_thread_create(sleep_thread, do_sleep, NULL, stack, SWUT_STACK_SIZE, prio_offset + 1, TF_START);
  if (status != xs_success) {
    oempa("%s: bad status '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }
  else {
    oempa("Thread id = %d.\n", sleep_thread->id);
  }
  
  stack = x_alloc_static_mem(memory, SWUT_STACK_SIZE);
  status = x_thread_create(wakeup_thread, do_wakeup, NULL, stack, SWUT_STACK_SIZE, prio_offset + 5, TF_START);
  if (status != xs_success) {
    oempa("%s: bad status '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }
  else {
    oempa("Thread id = %d.\n", wakeup_thread->id);
  }

  return memory;
  
}

static volatile x_int je_state = 0;

typedef struct je_Thread {
  x_thread thread;
  x_ubyte * stack;
} je_Thread;

typedef struct je_Thread * je_thread;

static je_Thread jet_1;
static je_Thread jet_2;
static je_Thread jet_3;
static je_Thread jet_4;
static je_Thread jet_5;

static x_Monitor je_Monitor;
static x_Mutex je_Mutex;

void jee_1(void * arg) {

  while (1) {
    x_assert(critical_status == 0);
    if (je_state == 3) {
      oempa("Going to return from this thread. Normal end...\n");
      x_thread_sleep(10);
      return;
    }
//    oempa("je_state = %d, sleeping...\n", je_state);
    x_thread_sleep(15);
  }
  
}

/*
** To make sure that we burn cycles in jet_2 and jet_3. Don't make 
** it static or it's optimized away.
*/

volatile unsigned long long burn_counter = 0;

void jee_2(void * arg) {

  /*
  ** Start the thread with locking a mutex and a monitor.
  ** When our time has come, indicated by je_state == 5, we do an x_thread_exit.
  */

  x_mutex_create(&je_Mutex);
  x_mutex_lock(&je_Mutex, x_eternal);
  x_monitor_create(&je_Monitor);
  x_monitor_enter(&je_Monitor, x_eternal);
  
  while (1) {
    x_assert(critical_status == 0);
    if (je_state == 5) {
      x_thread_exit((void *)0xcafebabe);
    }
    burn_counter += 1;
  }

}


void jee_3(void * arg) {

  /*
  ** Note that in this thread we DONT call any Oswald function that sets or resets
  ** the critical_status since we want to check pre-emptive suspension...
  */
  
  while (1) {
    x_assert(critical_status == 0);
    burn_counter += 1;
    if (burn_counter % 1000000 == 0) {
      oempa("Burning at %d, critical status = 0x%08x\n", burn_counter, critical_status);
    }
    if (critical_status != 0) {
      oempa("Bad critical status 0x%08x, should be 0!\n", critical_status);
      exit(0);
    }
  }
  
}

void jee_4(void * arg) {

  x_status status;
  void * result;
  
  while (1) {
    x_assert(critical_status == 0);
    if (je_state == 1) {
      /*
      ** First a simple test such that unexisting threads are caught...
      */
      
      status = x_thread_join((x_thread)0xdeadbeef, &result, x_eternal);
      if (status != xs_no_instance) {
        oempa("Thread 0xdeadbeef should return xs_no_instance, returned '%s'.\n", x_status2char(status));
        exit(0);
      }
    
      /*
      ** Wait some time so that all worker threads get started...
      */
      
      x_thread_sleep(second * 5);
      je_state = 2;
      oempa("Trying to join for normal end...\n");
      status = x_thread_join(jet_1.thread, &result, x_eternal);
      if (status != xs_success) {
        oempa("Bad status '%s'\n", x_status2char(status));
        exit(0);
      }
      if (je_state != 3) {
        oempa("Bad state %d\n", je_state);
        exit(0);
      }
      if (result != (void *)-1) {
        oempa("Bad result 0x%08x\n", result);
        exit(0);
      }
      else {
        oempa("OK, result from normal end was 0x%08x\n", result);
      }
      if (isSet(thread_current->flags, TF_JOIN_ENDED)) {
        oempa("Flag TF_JOIN_ENDED not unset properly.\n");
        exit(0);
      }
    }
    else if (je_state == 3) { 
      // check that monitor and mutex are owned properly
      je_state = 4;
      status = x_thread_join(jet_2.thread, &result, x_eternal);
      if (status != xs_success) {
        oempa("Bad status '%s'\n", x_status2char(status));
        exit(0);
      }
      if (je_state != 5) {
        oempa("Bad state %d\n", je_state);
        exit(0);
      }
      if (result != (void *)0xcafebabe) {
        oempa("Bad result 0x%08x\n", result);
        exit(0);
      }
      else {
        oempa("OK, result from x_thread_exit was 0x%08x\n", result);
      }
      if (isSet(thread_current->flags, TF_JOIN_EXIT)) {
        oempa("Flag TF_JOIN_EXIT not unset properly.\n");
        exit(0);
      }
      if (thread_current->joining_with != NULL) {
        oempa("Threads 'joining_with' not properly reset.\n");
        exit(0);
      }
      if (thread_current->l_joining_with != NULL) {
        oempa("Threads 'l_joining_with' not properly reset.\n");
        exit(0);
      }
    }
    else if (je_state == 5) {
      je_state = 6;
      status = x_thread_join(jet_3.thread, &result, x_eternal);
      if (status != xs_success) {
        oempa("Bad status '%s'\n", x_status2char(status));
        exit(0);
      }
      if (result != (void *)-2) {
        oempa("Bad result 0x%08x\n", result);
        exit(0);
      }
      else {
        oempa("OK, result from suspend/delete was 0x%08x\n", result);
      }
      if (isSet(thread_current->flags, TF_JOIN_DELETED)) {
        oempa("Flag TF_JOIN_DELETED not unset properly.\n");
        exit(0);
      }
      if (thread_current->joining_with != NULL) {
        oempa("Threads 'joining_with' not properly reset.\n");
        exit(0);
      }
      if (thread_current->l_joining_with != NULL) {
        oempa("Threads 'l_joining_with' not properly reset.\n");
        exit(0);
      }
      je_state = 8;
      return;
    }
    
    x_thread_sleep(1);
    
  }
  
}

void jee_5(void * arg) {

  x_status status;
  void * result;
  
  while (1) {
    x_assert(critical_status == 0);

    if (je_state == 2) {
      je_state = 3;
      oempa("Trying to join for normal end...\n");
      status = x_thread_join(jet_1.thread, &result, x_eternal);
      if (status != xs_success) {
        oempa("Bad status '%s'\n", x_status2char(status));
        exit(0);
      }
      if (result != (void *)-1) {
        oempa("Bad result 0x%08x\n", result);
        exit(0);
      }
      else {
        oempa("OK, result from normal end was 0x%08x\n", result);
      }
      if (thread_current->joining_with != NULL) {
        oempa("Threads 'joining_with' not properly reset.\n");
        exit(0);
      }
      if (thread_current->l_joining_with != NULL) {
        oempa("Threads 'l_joining_with' not properly reset.\n");
        exit(0);
      }
    }
    else if (je_state == 4) {
      je_state = 5;
      status = x_thread_join(jet_2.thread, &result, x_eternal);
      if (status != xs_success) {
        oempa("Bad status '%s'\n", x_status2char(status));
        exit(0);
      }
      if (result != (void *)0xcafebabe) {
        oempa("Bad result 0x%08x\n", result);
        exit(0);
      }
      else {
        oempa("OK, result from x_thread_exit was 0x%08x\n", result);
      }
      if (thread_current->joining_with != NULL) {
        oempa("Threads 'joining_with' not properly reset.\n");
        exit(0);
      }
      if (thread_current->l_joining_with != NULL) {
        oempa("Threads 'l_joining_with' not properly reset.\n");
        exit(0);
      }
    }
    else if (je_state == 6) {
    
      /*
      ** We suspend and delete the jet_3 thread. We don't use a callback since the thread does
      ** not have any events in ownership.
      */
      
      x_thread_suspend(jet_3.thread);
      x_thread_delete(jet_3.thread);
      je_state = 7;
    }
    else if (je_state == 8) {
      return;
    }
    
    x_thread_sleep(1);

  }
  
}

void jet_create(je_thread jet, void (*te)(void * arg), x_size prio) {

  x_size stack_size = 1024 * 3;
  x_status status;
  
  jet->thread = x_mem_get(sizeof(x_Thread));
  jet->stack = x_mem_get(stack_size);
  
  if (jet->thread && jet->stack) {
    status = x_thread_create(jet->thread, te, jet->thread, jet->stack, stack_size, prio_offset + prio, TF_START);
    if (status != xs_success) {
      oempa("Bad status '%s'\n", x_status2char(status));
      exit(0);
    }
    else {
      oempa("Thread %d started...\n", jet->thread->id);
    }
  }
  else {
    oempa("Could not allocate memory.\n");
    exit(0);
  }
  
}

void je_test(void * t) {

  x_status status;
  x_int loop = 0;
  void * result;
  
  while (1) {
    x_assert(critical_status == 0);
    if (je_state == 0) {
      jet_create(&jet_1, jee_1, 5);
      jet_create(&jet_2, jee_2, 5);
      jet_create(&jet_3, jee_3, 6);
      jet_create(&jet_4, jee_4, 3);
      jet_create(&jet_5, jee_5, 4);
      je_state = 1;
      loop += 1;
      oempa("All set up for loop %d of join/exit fsm; %d free bytes.\n", loop, x_mem_avail());
    }
    else if (je_state == 8) {
      status = x_thread_delete(jet_1.thread);
      if (status != xs_success) {
        oempa("Bad status '%s'\n", x_status2char(status));
        exit(0);
      }
      x_mem_free(jet_1.thread);
      x_mem_free(jet_1.stack);

      status = x_thread_delete(jet_2.thread);
      if (status != xs_success) {
        oempa("Bad status '%s'\n", x_status2char(status));
        exit(0);
      }
      x_mem_free(jet_2.thread);
      x_mem_free(jet_2.stack);

      status = x_thread_delete(jet_3.thread);
      if (status != xs_success) {
        oempa("Bad status '%s'\n", x_status2char(status));
        exit(0);
      }
      x_mem_free(jet_3.thread);
      x_mem_free(jet_3.stack);

      /*
      ** For jet_4 and jet_5, we use join ourselves but don't check on a return
      ** value for success...
      */
      
      x_thread_join(jet_4.thread, &result, x_eternal);
      status = x_thread_delete(jet_4.thread);
      if (status != xs_success) {
        oempa("Bad status '%s'\n", x_status2char(status));
        exit(0);
      }
      x_mem_free(jet_4.thread);
      x_mem_free(jet_4.stack);

      x_thread_join(jet_5.thread, &result, x_eternal);
      status = x_thread_delete(jet_5.thread);
      if (status != xs_success) {
        oempa("Bad status '%s'\n", x_status2char(status));
        exit(0);
      }
      x_mem_free(jet_5.thread);
      x_mem_free(jet_5.stack);
      
      je_state = 0;
    }
    
    x_thread_sleep(second);

  }
  
}

void nested_sr(void * t) {

  x_int loop = 0;
  
  while (1) {
    x_assert(critical_status == 0);
    x_thread_sleep(70);
    loop += 1;
    oempa("Loop %d, going to sleep...\n", loop);
  }
  
}

void suspend_resume(void * t) {

  x_ubyte * sr_stack;
  x_thread sr_thread;
  x_status status;
  x_int i;
  x_int num;
  
  sr_thread = x_mem_get(sizeof(x_Thread));
  sr_stack = x_mem_get(THRT_STACK_SIZE);
  status = x_thread_create(sr_thread, nested_sr, sr_thread, sr_stack, THRT_STACK_SIZE, prio_offset + 3, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }
  
  while (1) {
    x_assert(critical_status == 0);
    num = x_random() % 100 + 1;

    /*
    ** Suspend number of times...
    */
    
    for (i = 0; i < num; i++) {
      status = x_thread_suspend(sr_thread);
      if (status != xs_success) {
        oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
    }

    x_thread_sleep(second * 1);
    
    /*
    ** Resume number of times.
    */

    for (i = 0; i < num - 1; i++) {
      status = x_thread_resume(sr_thread);
      if (status != xs_success) {
        oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
    }
    
    /*
    ** This last one should really resume the thread...
    */
    
    status = x_thread_resume(sr_thread);
    if (status != xs_success) {
      oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
      exit(0);
    }
    
    x_thread_sleep(second * 1);

  }
  
}

static x_thread jecontrol_thread;
static x_thread nsr_thread;

x_ubyte * thread_test(x_ubyte * memory) {

  x_status status;

  second = x_seconds2ticks(1);

  cd_control = x_alloc_static_mem(memory, sizeof(x_Thread));
  status = x_thread_create(cd_control, create_delete, cd_control, x_alloc_static_mem(memory, THRT_STACK_SIZE), THRT_STACK_SIZE, prio_offset + 4, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }
  else {
    oempa("Thread id = %d.\n", cd_control->id);
  }

  many_thread = x_alloc_static_mem(memory, sizeof(x_Thread));
  status = x_thread_create(many_thread, many, many_thread, x_alloc_static_mem(memory, THRT_STACK_SIZE), THRT_STACK_SIZE, prio_offset + 4, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }
  else {
    oempa("Thread id = %d.\n", many_thread->id);
  }

  argument_thread = x_alloc_static_mem(memory, sizeof(x_Thread));
  status = x_thread_create(argument_thread, argument, argument_thread, x_alloc_static_mem(memory, THRT_STACK_SIZE), THRT_STACK_SIZE, prio_offset + 4, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }
  else {
    oempa("Thread id = %d.\n", argument_thread->id);
  }

  /*
  ** test callback suspend mechanism.
  */

  cb_thread = x_alloc_static_mem(memory, sizeof(x_Thread));
  status = x_thread_create(cb_thread, cb_test, cb_thread, x_alloc_static_mem(memory, THRT_STACK_SIZE), THRT_STACK_SIZE, prio_offset + 4, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }
  else {
    oempa("Thread id = %d.\n", cb_thread->id);
  }

  memory = sleep_wakeup_test(memory);

  /*
  ** Test join/exit mechanism.
  */

  jecontrol_thread = x_alloc_static_mem(memory, sizeof(x_Thread));
  status = x_thread_create(jecontrol_thread, je_test, jecontrol_thread, x_alloc_static_mem(memory, THRT_STACK_SIZE), THRT_STACK_SIZE, prio_offset + 3, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }
  else {
    oempa("Thread id = %d.\n", jecontrol_thread->id);
  }

  /*
  ** Test nested suspend/resume behaviour.
  */
  
  nsr_thread = x_alloc_static_mem(memory, sizeof(x_Thread));
  status = x_thread_create(nsr_thread, suspend_resume, nsr_thread, x_alloc_static_mem(memory, THRT_STACK_SIZE), THRT_STACK_SIZE, prio_offset + 3, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }

  return memory;
  
}

