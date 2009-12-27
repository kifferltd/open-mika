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

#include <stdio.h>
#include "tests.h"
#include "string.h"

#include "oswald.h"

#define MONT_STACK_SIZE ((1024 * 3) + MARGIN)

static x_sleep second;

/*
** We check if we got out of the waiting list in correct time, but when we have
** thread dumping enabled in the init thread, this can take too much time. We then
** set the following to false to indicate that it is not a real failure.
*/

static const x_boolean wait_ticks_fail = true;

/*
** The native implementation of our old faithfull Carillon. This code uses a single
** monitor, but is very heavy on contention.
*/

#define NUM_RINGERS          8
#define RESULT_LENGTH        256

typedef struct t_Ringer *    t_ringer;

typedef struct t_Ringer {
  int id;                    // Id of message
  const char * what;         // What to say as a message
  x_ubyte *stack;            // The stack of the ringer thread
  x_size stack_size;         // The size of the stack
  x_thread thread;           // The pointer to the thread structure
} t_Ringer;

static t_Ringer Ringers[] = {
  { 1, "brown",  NULL, 0, NULL },
  { 2, "dog",    NULL, 0, NULL },
  { 3, "fox",    NULL, 0, NULL },
  { 4, "jumps",  NULL, 0, NULL },
  { 5, "lazy",   NULL, 0, NULL },
  { 6, "over",   NULL, 0, NULL },
  { 7, "quick",  NULL, 0, NULL },
  { 8, "the",    NULL, 0, NULL },
};

/*
** Which id's to play in which order and the id that is currently playing...
*/

static x_int conductor[] = { 8, 7, 1, 3, 4, 6, 8, 5, 2 };
static x_int nox_playing = 0;
static x_int num_plays = (sizeof(conductor) / sizeof(conductor[0]));
static char result[RESULT_LENGTH];
static x_size runs = 0;

/*
** The monitor that controls the playing...
*/

static x_Monitor Monitor;

/*
** How many unecessary waits did we have for 1 sequence...
*/

static x_int sequence_clashes = 0;

/*
** The maximum number of waiting threads we had in a run...
*/

static x_size max_waiting = 0;

/*
** The maximum number of competing threads we had in a run...
*/

static x_int max_competing = 0;

static void next_sequence(x_int current) {

  x_status status;

#if 0
  if (Monitor.Event.n_competing > max_competing) {
    max_competing = Monitor.Event.n_competing;
  }

  if (Monitor.n_waiting > max_waiting) {
    max_waiting = Monitor.n_waiting;
  }
#endif
    
  status = x_monitor_enter(&Monitor, x_eternal);
  if (status != xs_success) {
    oempa("Status = %s\n", x_status2char(status));
    exit(0);
  }

  current += 1;
  if (current >= num_plays) {
    nox_playing = 0;
    if (strcmp(result, "the quick brown fox jumps over the lazy dog ") != 0) {
      oempa("Bad result = '%s'\n", result);
      exit(0);
    }
    else {

      // At this point, the monitor count should be 2, one for the initial entry and 1 for the enter_monitor above.
      
      if (Monitor.count != 2) {
        oempa("Monitor count isn't 2 but %d.\n", Monitor.count);
        exit(0);
      }

      runs += 1;
      oempa("Printing thread %p clashes = %d, max competing = %d, max waiting = %d\n", x_thread_current(), sequence_clashes, max_competing, max_waiting);
      //oempa("'%s' %d (cnt = %d, wait = %d comp = %d)\n", result, runs, Monitor.count, Monitor.n_waiting, Monitor.Event.n_competing);
      memset(result, 0x00, RESULT_LENGTH);
      sequence_clashes = 0;
      max_waiting = 0;
      max_competing = 0;
      x_thread_sleep(second * 2);
    }
  }  
  else {
    nox_playing = current;
  }
  
  status = x_monitor_notify_all(&Monitor);
  if (status != xs_success) {
    oempa("Status = %s\n", x_status2char(status));
    exit(0);
  }
    
  status = x_monitor_exit(&Monitor);
  if (status != xs_success) {
    oempa("Status = %s\n", x_status2char(status));
    exit(0);
  }

  if (Monitor.owner != x_thread_current()) {
#if 0
    if (x_thread_current()->a_prio != x_thread_current()->c_prio) {
      oempa("Our priority wasn't reset...\n");
      exit(0);
    }
#endif
  }

}

/*
** The ringer function
*/

static void Ringer(void *r) {

  t_ringer ringer = r;
  x_status status;
  x_int old_count;

  oempa("Ringer thread %p '%s' starting...\n", ringer->thread, ringer->what);
  
  status = x_monitor_enter(&Monitor, x_eternal);
  if (status != xs_success) {
    oempa("Status = %s\n", x_status2char(status));
    exit(0);
  }

  while (1) {
    if (conductor[nox_playing] == ringer->id) {
      sprintf(result + strlen(result), "%s ", ringer->what);
      next_sequence(nox_playing);
    }
    else {
      sequence_clashes += 1;
    }
    
    x_thread_sleep(1);
    
    old_count = Monitor.count;
    status = x_monitor_wait(&Monitor, x_eternal);
    if (status != xs_success) {
      oempa("Status = %s\n", x_status2char(status));
      exit(0);
    }
    
    // See if the count is correct w.r.t. the previous count

    if (old_count != Monitor.count) {
      oempa("Monitor count is not same as before ! %d != %d\n", old_count, Monitor.count);
      exit(0);
    }
    
    // See if our count is not getting out of bounds...
    
    if (Monitor.count > NUM_RINGERS) {
      oempa("Monitor count out of bounds %d > %d ! %d != %d\n", Monitor.count, NUM_RINGERS);
      exit(0);
    }
    
  }
  
}

static void Carillon(void *t) {

  x_int initialized = 0;
  x_int i;
  x_size prio_low = 1;
  x_size prio_high = prio_low + 1;
  x_size min_prio;
  x_size previous_min_prio = 0;
  t_ringer r;
  x_int pass = 0;
  x_status status;

  while (1) {
    if (! initialized) {
      x_monitor_create(&Monitor);
      for (i = 0; i < NUM_RINGERS; i++) {
        Ringers[i].thread = x_mem_get(sizeof(x_Thread));
        Ringers[i].stack_size = MONT_STACK_SIZE;
        Ringers[i].stack = x_mem_get(MONT_STACK_SIZE);
        status = x_thread_create(Ringers[i].thread, Ringer, &Ringers[i], Ringers[i].stack, Ringers[i].stack_size, prio_offset + prio_low, TF_START);
        if (status != xs_success) {
          oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
          exit(0);
        }
        else {
          oempa("Ringer thread number %d, thread = %p\n", i, Ringers[i].thread);
        }
      }
      initialized = 1;
    }

    /*
    ** Find minimum used priority...
    */

    min_prio = 10000;
    for (i = 0; i < NUM_RINGERS; i++) {
      r = &Ringers[i];
#if 0
      if (min_prio > r->thread->a_prio) {
        min_prio = r->thread->a_prio;
      }
#endif
    }

    if (min_prio > 5) {
      min_prio = 1;
    }

    if (previous_min_prio != 0) {
      pass += 1;
      oempa("************** Priority changing pass %d, min_prio = %d  *************************\n", pass, min_prio);
      prio_low = min_prio + 1; // introduce a shift...
      prio_high = prio_low + 1;
      x_thread_priority_set(Ringers[0].thread, prio_low);
      for (i = 1; i < NUM_RINGERS; i++) {
        r = &Ringers[i];
        x_thread_priority_set(r->thread, prio_high);
      }
    }

    previous_min_prio = min_prio;

    x_thread_sleep(second * 2);

  }
  
}

/*
** The dining philosophers program WITHOUT specific notification of Thomas Cargill. 
**
** In his paper about "Specific Notification" http://www.sni.net/~cargill, Thomas Cargill has a
** java program that implements the dining philosophers in two ways; one without his pattern for
** specific notification, which yields a correct result w.r.t synchronisation but that is very
** unfair (3 out of 5 philosophers starve to death) and another that he rewrote with his synchronization
** pattern to avoid this situation. We have implemented monitors such that this would not happen.
** Therefore, the unfair first program of Thomas should run fine on Oswald. This is checked by the
** 'fairness' check.
*/

#define NUMBER_FORKS 5

/*
** The fairness ratio defines the ratio between the most eaten plates and the least eaten
** plates. It should be 1 in steady state but getting our monitor list wrong would upset
** the test. This will be signalled by an abort...
*/

#define FAIRNESS_RATIO 3

typedef struct t_Fork * t_fork;

typedef struct t_Fork {
  x_int free;
  x_Monitor monitor;
} t_Fork;

static t_Fork forks[NUMBER_FORKS];
static const x_int number_forks = NUMBER_FORKS;

typedef struct t_Phil * t_phil;

typedef struct t_Phil {
  x_size plates;
  t_fork fork_a;
  t_fork fork_b;
  x_ubyte * stack;
  x_size stack_size;
  x_thread thread;
} t_Phil;

static t_Phil * philosophers;

static void pickup_fork(t_fork afork) {

  x_status status;
  
  status = x_monitor_enter(&afork->monitor, x_eternal);
  if (status != xs_success) {
    oempa("Status = %s\n", x_status2char(status));
    exit(0);
  }

  // Don't sleep here since we would destroy checking for our fairness rules...
  
  if (afork->monitor.owner != x_thread_current()) {
    oempa("Owner is wrong!\n");
    exit(0);
  }
  
  while (afork->free == 0) {
    status = x_monitor_wait(&afork->monitor, x_eternal);
    if (status != xs_success) {
      oempa("Status = %s\n", x_status2char(status));
      exit(0);
    }
  }
  
  afork->free = 0;

  status = x_monitor_exit(&afork->monitor);
  if (status != xs_success) {
    oempa("Status = %s\n", x_status2char(status));
    exit(0);
  }
    
}

static void laydown_fork(t_fork afork) {

  x_status status;
  
  status = x_monitor_enter(&afork->monitor, x_eternal);
  if (status != xs_success) {
    oempa("Status = %s\n", x_status2char(status));
    exit(0);
  }
  
  // Don't sleep here since we would destroy checking for our fairness rules...

  if (afork->monitor.owner != x_thread_current()) {
    oempa("Owner is wrong!\n");
    exit(0);
  }
  
  afork->free = 1;
  
  status = x_monitor_notify(&afork->monitor);
  if (status != xs_success) {
    oempa("Status = %s\n", x_status2char(status));
    exit(0);
  }

  status = x_monitor_exit(&afork->monitor);
  if (status != xs_success) {
    oempa("Status = %s\n", x_status2char(status));
    exit(0);
  }
    
}

static x_boolean report = true;
static x_int num_reports = 0;

static void Philosopher(void *p) {

  t_phil phil = p;
  x_int i;
  x_size max_plates;
  x_size min_plates;
  x_size ratio;
  //x_size stack_used;
  //x_size stack_size;
  //x_size stack_left;

  oempa("Philosopher %p is getting his feet under the table...\n", x_thread_current());
    
  while (1) {
    //x_assert(critical_status == 0);
    pickup_fork(phil->fork_a);
    pickup_fork(phil->fork_b);
    
    x_thread_sleep(1);
    phil->plates++;

    /*
    ** Check the maximum and minimum number of plates consumed, and use this to check for our
    ** fairness algorithm.
    */
        
    max_plates = 0;
    min_plates = 0xffffffff;
    for (i = 0; i < number_forks; i++) {
      if (philosophers[i].plates > max_plates) {
        max_plates = philosophers[i].plates;
      }
      if (min_plates > philosophers[i].plates) {
        min_plates = philosophers[i].plates;
      }
    }

    ratio = (min_plates) ? (max_plates / min_plates) : max_plates;
    //x_stack_info(x_thread_current(), &stack_size, &stack_used, &stack_left);
    if (report) {
     // oempa("Thread %p stack: used %d of %d bytes, %d bytes left.\n", x_thread_current(), stack_used, stack_size, stack_left);
      oempa("Philosopher %p has eaten %d plates (max = %d, min = %d, ratio = %d)\n", x_thread_current(), phil->plates, max_plates, min_plates, ratio);
      report = false;      
      num_reports += 1;
    }      

    /*
    ** Check the fairness, in steady state, fairness should be 1, but at startup, it could get to 3...
    */
    
    if (ratio > FAIRNESS_RATIO) {
      oempa("Ratio is out of bounds %d > %d\n", ratio, FAIRNESS_RATIO);
      exit(0);
    }

    laydown_fork(phil->fork_b);
    laydown_fork(phil->fork_a);
    
    /*
    ** Do some napping in between to digest the stuff, note that the sleeping time can not be
    ** random since we would upset our fairness check...
    */
    
    x_thread_sleep(second * 2);

  }
  
}

static void Dining_Philosophers(void *t) {

  x_int initialized = 0;
  x_int i;
  t_fork fork_a;
  t_fork fork_b;
  t_phil phil;
  x_int number = 0;
  x_status status;
  
  for (i = 0; i < number_forks; i++) {
    forks[i].free = 1;
    x_monitor_create(&forks[i].monitor);
  }
  
  while (1) {
    //x_assert(critical_status == 0);
    if (! initialized) {
      philosophers = x_mem_get(number_forks * sizeof(t_Phil));
      memset(philosophers, 0x00, number_forks * sizeof(t_Phil));
      for (i = 0; i < number_forks; i++) {
        oempa("Taking forks %d and %d for philosopher %d.\n", i, (i + 1) % number_forks, number);
        philosophers[i].thread = x_mem_get(sizeof(x_Thread));
        philosophers[i].stack = x_mem_get(MONT_STACK_SIZE);
        philosophers[i].stack_size = MONT_STACK_SIZE;
        fork_a = &forks[i];
        fork_b = &forks[(i + 1) % number_forks];
        phil = &philosophers[number++];
        if (i % 2 == 0) {
          phil->fork_a = fork_a;
          phil->fork_b = fork_b;
        }
        else {
          phil->fork_a = fork_b;
          phil->fork_b = fork_a;
        }
        status = x_thread_create(phil->thread, Philosopher, phil, phil->stack, phil->stack_size, prio_offset + 3, TF_START);
        if (status != xs_success) {
          oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
          exit(0);
        }
        else {
          oempa("Philosopher thread %p created.\n", phil->thread);
        }
      }
      initialized = 1;
    }
    x_thread_sleep(second * 2);
    report = true;
  }
  
}

static x_thread c_thread;
static x_ubyte * c_stack;
static x_thread p_thread;
static x_ubyte * p_stack;

static x_thread control;
static x_int state = 0;

static void monitor_tests(void * t) {

  x_status status;
  x_int counter = 0;
  x_int i;
  x_thread x;
  char * buffer = NULL;
  x_size buffer_size = 10;

  while (1) {
    //x_assert(critical_status == 0);
    counter += 1;
    if (state == 0) {
      buffer = x_mem_get(buffer_size);
      c_thread = x_mem_get(sizeof(x_Thread));
      c_stack = x_mem_get(MONT_STACK_SIZE);
      p_thread = x_mem_get(sizeof(x_Thread));
      p_stack = x_mem_get(MONT_STACK_SIZE);


      status = x_thread_create(c_thread, Carillon, c_thread, c_stack, MONT_STACK_SIZE, prio_offset + 5, TF_START);
      if (status != xs_success) {
        oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      else {
        oempa("Carillon master thread, id %p.\n", c_thread);
      }
      status = x_thread_create(p_thread, Dining_Philosophers, p_thread, p_stack, MONT_STACK_SIZE, prio_offset + 5, TF_START);
      if (status != xs_success) {
        oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      else {
        oempa("Dining philosopher master thread, id %p.\n", p_thread);
      }

      state = 1;
    }

    /*
    ** We check on last thread of the ringers not being NULL since that indicates 
    ** that some other high priority threads didn't give us enough
    ** time yet to start up...
    */
        
    if (Ringers[NUM_RINGERS - 1].thread != NULL) {
      for (i = 0; i < number_forks; i++) {
        x = philosophers[i].thread;
        //oempa("%p P %d state = %10s, sticks = %3d, prios %d %d, %d plates\n", i, x, x_state2char(x), x->sticks, x->a_prio, x->c_prio, philosophers[i].plates);
        oempa("%p P %d state = %10s, %d plates\n", i, x, x_state2char(x), philosophers[i].plates);
      }
      for (i = 0; i < NUM_RINGERS; i++) {
        x = Ringers[i].thread;
#if 0
        if (x->sticks == x_eternal) {
          x_snprintf(buffer, 10, "---");
        }
        else {
          x_snprintf(buffer, 10, "%3d", x->sticks);
        }
#endif
        //oempa("%d R %d state = %10s, sticks = %s, prios %d %d\n", i, x->id, x_state2char(x), buffer, x->a_prio, x->c_prio);
        oempa("%p P %d state = %10s, %d plates\n", i, x, x_state2char(x), philosophers[i].plates);
      }
    }

    /*
    ** Give the ringers and Philosophers some time to initialize...
    */
    
    if (state == 1) {
      state = 2;
      x_thread_sleep(second * 5);
    }
    
    x_thread_sleep(second * (random() % 5 + 5));

  }
  
}

/*

The finite state machine to test Oswald monitors. There are 5 threads, at 3
different priorities. In the column indicated by 'M', the number of times
the tread has the monitor is noted, with a '+' sign when the thread owns the
monitor, with the '-' sign when the count is saved and the thread doesn't
have the monitor.

The state of the finite state machine is called 'state' below, but the name
of the variable in the source code lower down is 'fsm_state'. The sleep
amount for each thread at the end is indicated by FSMS.

In the following description ======= denotes a waiting line with the threads
waiting numbered.

For printing out the following state text, you'd best use landscape...

             Prio 4              |              Prio 4              |              Prio 4              |              Prio 3              |               Prio 2             |
                1                |                 2                |                 3                |                 4                |                  5               |
-------------------------------M-+--------------------------------M-+--------------------------------M-+--------------------------------M-+--------------------------------M-+
If state == 1, create monitor !  |                               !  |                               !  |                               !  |                               !  |
and set state to 2. ----------!  |-> If state == 2, enter monitor!  |                               !  |                               !  |                               !  |
                              !  | and enter monitor, set state  !+2|                               !  |                               !  |                               !  |
                              !  | to 3, while (state != 4) do --!  |-> If state == 3, enter monitor!  |                               !  |                               !  |
                              !  | sleep a few ticks, then  <----!  |-should fail, set state to 4,  !  |                               !  |                               !  |
                              !  | while (state != 5), wait -----!-2|-> try to enter with eternal,  !  |                               !  |                               !  |
                              !  | eternal.     :                !  | should succeed. Set state to  !+1|                               !  |                               !  |
                              !  |              :                !  | 5 and notify single, while    !  |                               !  |                               !  |
                              !  |              :<---------------!  |- ! (state == 12 | state == 13)!-1|                               !  |                               !  |
                              !  | should succeed, set state to  !+2| wait eternal :                !  |                               !  |                               !  |
                              !  | 6; while state != 7 sleep ----!  |------>-------:-------->-------!  |-> If state == 6, set state to !  |                               !  |
                              !  |              :                !  |              :                !  | 7, try to enter monitor with  !  |                               !  |
                              !  | The state is now 7 <----------!  |------<-------:--------<-------!  |- eternal.     :               !  |                               !  |
                              !  | Set state to 8 and just wait  !  |              :                !  |               :               !  |                               !  |
                              !  | for 5 ticks. -------->--------!-2|------>-------:-------->-------!  |-> should succeed and state    !+1|                               !  |
                              !  |              :                !  |              :                !  | should be 8, do a notify      !  |                               !  |
                              !  |              :                !  |              :                !  | single and wait for FSMS * 2  !  |                               !  |
                              !  | should succeed; set the  <----!+2|------<-------:--------<-------!  |- ticks                        !-1|                               !  |
                              !  | state to 9, while state != 10 !  |              :                !  |               :               !  |                               !  |
                              !  | wait eternal -------->--------!-2|------>-------:-------->-------!  |-> should succeed, check that  !+1|                               !  |
                              !  |              :                !  |              :                !  | state == 9, set state to 10   !  |                               !  |
                              !  |              :<---------------!  |------<-------:--------<-------!  |-and do a notify single, while !  |                               !  |
                              !  | should succeed, set state to  !+2|              :                !  | state != 12, wait eternal     !-1|                               !  |
                              !  | 11, while state != 14, wait   !  |              :                !  |               :               !  |                               !  |
                              !  | eternal (we don't check for   !  |              :                !  |               :               !  |                               !  |
                              !  | states 12 & 13) ----->--------!-2|------>-------:-------->-------!  |------->-------:------->-------!  |-> If state == 11, enter the   !  |
                              !  |              :                !  |              :                !  |               :               !  | monitor, enter monitor,       !+2|
                              !  |              :                !  |              :                !  |               :               !  | set state to 12 and notify    !  |
                              !  |              :                !  |              :                !  |               :               !  | all, while state != 16,       !  |
                              !  |              :                !  |              :                !  | should succeed and state <----!+1|-  wait eternal                !-2|
                              !  |              :                !  |              :                !  | should be 12, set state to 13 !  |              :                !  |
                              !  |              :                !  |              :<---------------!  |-and notify all, while the     !  |              :                !  |
                              !  |              :                !  | should succeed and state must !+1| state != 18, wait eternal     !-1|              :                !  |
                              !  |              :                !  | be 13, set state to 14, notify!  |               :               !  |              :                !  |
                              !  |              :<---------------!  |-all and while state != 20 do  !  |               :               !  |              :                !  |
                              !  | should succeed and state must !+2| a wait eternal                !-1|               :               !  |              :                !  |
                              !  | be 14, set state to 15 and    !  |              :                !  |               :               !  |              :                !  |
                              !  | while ! (state == 18 || state !-2|              :                !  |               :               !  |              :                !  |
If state == 15, n_waiting <---!  |- == 20 || state == 21) wait   !  |              :                !  |               :               !  |              :                !  |
monitor should be 4, enter    !  | with eternal.:                !  |              :                !  |               :               !  |              :                !  |
the monitor, set state to 16, !+1|              :                !  |              :                !  |               :               !  |              :                !  |
notify all, while state != 17,!  | ============ : == 1 ========= !  | ============ : == 2 ========= !  | ============= : == 3 ======== !  | == 4 ======= : ============== !  |
wait eternal -----------------!-1|------->------:-------->-------!  |----->--------:-------->-------!  |------->-------:-------->------!  |-> should succeed, set state   !+2|
             :                !  |              :                !  |              :                !  |               :               !  | to 17, exit monitor, check we !+1|
             :                !  |              :                !  |              :                !  |               :               !  | are still owner and count is  !  |
should succeed, n_waiting <---!+1|-------<------:--------<-------!  |-----<--------:--------<-------!  |-------<-------:--------<------!  |-1, notify all and exit the    ! 0|
should be 0, set state to 18  !  |              :                !  |              :                !  |               :               !  | monitor.                      !  |
and notify single, while the  !  |              :                !  |              :                !  |               :               !  |                               !  |
state != 19, wait eternal ----!-1|------->------:-------->-------!  |----->--------:-------->-------!  |-> should succeed, count should!+1|                               !  |
             :                !  |              :                !  |              :                !  | be 1, set state to 19 and     !  |                               !  |
should succeed, n_waiting is  !+1|              :                !  |              :                !  | notify all, exit monitor.     ! 0|                               !  |
0, set state to 20 and do     !  |              :                !  |              :                !  |                               !  |                               !  |
while state != 21, wait for   !-1|              :                !  |              :                !  |                               !  |                               !  |
eternal ----------------------!  |------->------:-------->-------!  |-> should succeed, count should!+1|                               !  |                               !  |
             :                !  |              :                !  | be 1, set state to 21 and do  !  |                               !  |                               !  |
should succeed, n_waiting <---!+1|-------<------:--------<-------!  |-a notify all, then exit the   ! 0|                               !  |                               !  |
should be 0, set state 22     !  |              :                !  | monitor.                      !  |                               !  |                               !  |
notify all and while state is !  |              :                !  |                               !  |                               !  |                               !  |
not 23, wait eternal ---------!-1|-> should succeed, set state   !+2|                               !  |                               !  |                               !  |
             :                !  | to 23, notify all and then    !  |                               !  |                               !  |                               !  |
             :                !  | while (state != 24) {         !  |                               !  |                               !  |                               !  |
should succeed, n_waiting <---!+1|-  wait for 5 (10/2) ticks.    !-2|                               !  |                               !  |                               !  |
should be 1, now do           !  | }            :                !  |                               !  |                               !  |                               !  |
for (i = 0; i < 10; i++) {    !  |              :                !  |                               !  |                               !  |                               !  |
  if (n_waiting == 0) {       !  |              :                !  |                               !  |                               !  |                               !  |
    break;                    !  |              :                !  |                               !  |                               !  |                               !  |
  }                           !  |              :                !  |                               !  |                               !  |                               !  |
  x_thread_sleep(1);          !  |              :                !  |                               !  |                               !  |                               !  |
}                             !  |              :                !  |                               !  |                               !  |                               !  |
check that i is below 10, we  !  |              :                !  |                               !  |                               !  |                               !  |
must have seen n_waiting go   !  |              :                !  |                               !  |                               !  |                               !  |
to 0 in the loop. Set state   !  |              :                !  |                               !  |                               !  |                               !  |
to 24, notify single and      !  |              :                !  |                               !  |                               !  |                               !  |
exit the monitor. ------------! 0|-> should succeed, exit monitor!+2|                               !  |                               !  |                               !  |
                              !  | twice and set state to 25.    ! 0|                               !  |                               !  |                               !  |
*/

static x_monitor monitor;
static volatile x_int fsm_state;

typedef struct mt_Thread * mt_thread;

typedef struct mt_Thread {
  x_Thread Thread;
  x_thread thread;
  x_ubyte * stack;
  x_ubyte prio;
  x_int ps;
  x_int cs;
} mt_Thread;

static mt_Thread * mt_Threads;

static const x_int num_fsm_threads = 5;

inline static void set_state(mt_thread mt, x_int new) {
  mt->ps = fsm_state;
  fsm_state = new;
  mt->cs = new;
}

static void dump_fsm_threads(void) {

  x_int i;
  x_thread t;
  
  if (monitor->owner) {
    oempa("Monitor FSM state %d, monitor owned by %d, %d waiting.\n", fsm_state, monitor->owner, monitor->n_waiting);
  }
  else {
    oempa("Monitor FSM state %d, monitor free, %d waiting\n", fsm_state, monitor->n_waiting);
  }
  for (i = 0; i < num_fsm_threads; i++) {
    t = mt_Threads[i].thread;
    //oempa("Mon FSM thread %d (%p), p a %d c %d, fsm p %2d c %2d, state = '%s'\n", i + 1, t->a_prio, t->c_prio, mt_Threads[i].ps, mt_Threads[i].cs, x_state2char(t));
    oempa("Mon FSM thread %d (%p), p a %d c %d, state = '%s'\n", i + 1, mt_Threads[i].ps, mt_Threads[i].cs, x_state2char(t));
  }

}

static x_int fsm_sleep_time;

static void mt_1(void * mta) {

  x_status status;
  x_int i;
  mt_thread mt = mta;

  while (1) {
    //x_assert(critical_status == 0);

    if (fsm_state == 1) {
      monitor = x_mem_get(sizeof(x_Monitor));
      memset(monitor, 0xff, sizeof(x_Monitor));
      status = x_monitor_create(monitor);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        dump_fsm_threads();
        exit(0);
      }
      
#if 0
      if (monitor->n_waiting != 0) {
        oempa("Bad n_waiting = %d\n", monitor->n_waiting);
        dump_fsm_threads();
        exit(0);
      }

      if (monitor->l_waiting != NULL) {
        oempa("Bad l_waiting = 0x%08x\n", monitor->l_waiting);
        dump_fsm_threads();
        exit(0);
      }
#endif

      if (monitor->owner != NULL) {
        oempa("Bad owner = 0x%08x\n", monitor->owner);
        dump_fsm_threads();
        exit(0);
      }

      if (monitor->count != 0) {
        oempa("Bad count = %d\n", monitor->count);
        dump_fsm_threads();
        exit(0);
      }

      /*
      ** If we passed these simple tests, we can proceed.
      */
      
      set_state(mt, 2);
    }
    
    if (fsm_state == 15) {
      if (monitor->n_waiting != 4) {
        oempa("Waiting should be 4 in stead of %d.\n", monitor->n_waiting);
        dump_fsm_threads();
        exit(0);
      }
      status = x_monitor_enter(monitor, x_eternal);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        dump_fsm_threads();
        exit(0);
      }
      set_state(mt, 16);
      status = x_monitor_notify_all(monitor);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        dump_fsm_threads();
        exit(0);
      }
      
      while (fsm_state != 17) {
        status = x_monitor_wait(monitor, x_eternal);
        if (status != xs_success) {
          oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
          dump_fsm_threads();
          exit(0);
        }
      }
      
      if (monitor->owner != x_thread_current()) {
        oempa("Bad owner %p != %p\n", monitor->owner, x_thread_current());
        dump_fsm_threads();
        exit(0);
      }
      if (monitor->count != 1) {
        oempa("Bad count %d != 1.\n", monitor->count);
        dump_fsm_threads();
        exit(0);
      }

      if (monitor->n_waiting != 0) {
        oempa("Waiting should be 0 in stead of %d.\n", monitor->n_waiting);
        dump_fsm_threads();
        exit(0);
      }

      set_state(mt, 18);
      status = x_monitor_notify(monitor);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        dump_fsm_threads();
        exit(0);
      }

      while (fsm_state != 19) {
        status = x_monitor_wait(monitor, x_eternal);
        if (status != xs_success) {
          oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
          dump_fsm_threads();
          exit(0);
        }
      }
      
      if (monitor->owner != x_thread_current()) {
        oempa("Bad owner %p != %p\n", monitor->owner, x_thread_current());
        dump_fsm_threads();
        exit(0);
      }
      if (monitor->count != 1) {
        oempa("Bad count %d != 1.\n", monitor->count);
        dump_fsm_threads();
        exit(0);
      }
      if (monitor->n_waiting != 0) {
        oempa("Waiting should be 0 in stead of %d.\n", monitor->n_waiting);
        dump_fsm_threads();
        exit(0);
      }

      set_state(mt, 20);
      while (fsm_state != 21) {
        status = x_monitor_wait(monitor, x_eternal);
        if (status != xs_success) {
          oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
          exit(0);
        }
      }

      if (monitor->owner != x_thread_current()) {
        oempa("Bad owner %p != %p\n", monitor->owner, x_thread_current());
        exit(0);
      }
      if (monitor->count != 1) {
        oempa("Bad count %d != 1.\n", monitor->count);
        exit(0);
      }
      if (monitor->n_waiting != 0) {
        oempa("Waiting should be 0 in stead of %d.\n", monitor->n_waiting);
        exit(0);
      }
      
      set_state(mt, 22);
      status = x_monitor_notify_all(monitor);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      while (fsm_state != 23) {
        status = x_monitor_wait(monitor, x_eternal);
        if (status != xs_success) {
          oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
          exit(0);
        }
      }

      x_preemption_disable;
      if (monitor->owner != x_thread_current()) {
        oempa("Bad owner %p != %p\n", monitor->owner, x_thread_current());
        exit(0);
      }
      if (monitor->count != 1) {
        oempa("Bad count %d != 1.\n", monitor->count);
        exit(0);
      }
      if (monitor->n_waiting != 1) {
        oempa("Waiting should be 1 in stead of %d.\n", monitor->n_waiting);
        exit(0);
      }
      x_preemption_enable;
      
      for (i = 0; i < 20; i++) {
        if (monitor->n_waiting == 0) {
          break;
        }
        x_thread_sleep(1);
      }
      if (i == 20) {
        oempa("Thread 2 didn't go out of waiting list after 10 ticks.\n");
        if (wait_ticks_fail) {
          exit(0);
        }
      }
      set_state(mt, 24);
      status = x_monitor_notify(monitor);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      status = x_monitor_exit(monitor);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      
      return;
      
      
    }
    
    x_thread_sleep(fsm_sleep_time);
    
  }
  
}

static void mt_2(void * mta) {

  x_status status;
  mt_thread mt = mta;
  
  while (1) {
    //x_assert(critical_status == 0);
    if (fsm_state == 2) {
      status = x_monitor_enter(monitor, x_eternal);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      status = x_monitor_enter(monitor, x_eternal);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }

      set_state(mt, 3);
      while (fsm_state != 4) {
        x_thread_sleep(20);
      }

      while (fsm_state != 5) {
        status = x_monitor_wait(monitor, x_eternal);
        if (status != xs_success) {
          oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
          exit(0);
        }
      }

      if (monitor->owner != x_thread_current()) {
        oempa("Bad owner %p != %p\n", monitor->owner, x_thread_current());
        exit(0);
      }
      if (monitor->count != 2) {
        oempa("Bad count %d != 2.\n", monitor->count);
        exit(0);
      }

      set_state(mt, 6);
      while (fsm_state != 7) {
        x_thread_sleep(20);
      }

      set_state(mt, 8);
      status = x_monitor_wait(monitor, 5);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }

      set_state(mt, 9);

      if (monitor->owner != x_thread_current()) {
        oempa("Bad owner %d != %d\n", monitor->owner, x_thread_current());
        exit(0);
      }
      if (monitor->count != 2) {
        oempa("Bad count %d != 2.\n", monitor->count);
        exit(0);
      }

      while (fsm_state != 10) {
        status = x_monitor_wait(monitor, x_eternal);
        if (status != xs_success) {
          oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
          exit(0);
        }
      }

      if (monitor->owner != x_thread_current()) {
        oempa("Bad owner %d != %d\n", monitor->owner, x_thread_current());
        exit(0);
      }
      if (monitor->count != 2) {
        oempa("Bad count %d != 2.\n", monitor->count);
        exit(0);
      }

      set_state(mt, 11);

      while (! (fsm_state == 14)) {
        status = x_monitor_wait(monitor, x_eternal);
        if (status != xs_success) {
          oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
          exit(0);
        }
      }

      if (monitor->owner != x_thread_current()) {
        oempa("Bad owner %p != %p\n", monitor->owner, x_thread_current());
        exit(0);
      }
      if (monitor->count != 2) {
        oempa("Bad count %d != 2.\n", monitor->count);
        exit(0);
      }

      set_state(mt, 15);

      while (! (fsm_state == 18 || fsm_state == 20 || fsm_state == 22)) {
        status = x_monitor_wait(monitor, x_eternal);
        if (status != xs_success) {
          oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
          exit(0);
        }
      }

      if (monitor->owner != x_thread_current()) {
        oempa("Bad owner %p != %p\n", monitor->owner, x_thread_current());
        exit(0);
      }
      if (monitor->count != 2) {
        oempa("Bad count %d != 2.\n", monitor->count);
        exit(0);
      }

      set_state(mt, 23);

      status = x_monitor_notify_all(monitor);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      while (fsm_state != 24) {
        status = x_monitor_wait(monitor, 5);
        if (status != xs_success) {
          oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
          exit(0);
        }
      }

      if (monitor->owner != x_thread_current()) {
        oempa("Bad owner %p != %p\n", monitor->owner, x_thread_current());
        exit(0);
      }
      if (monitor->count != 2) {
        oempa("Bad count %d != 2.\n", monitor->count);
        exit(0);
      }

      status = x_monitor_exit(monitor);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      status = x_monitor_exit(monitor);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }

      set_state(mt, 25);

      return;      

    }
    
    x_thread_sleep(fsm_sleep_time);
    
  }
  
}

static void mt_3(void * mta) {

  x_status status;
  mt_thread mt = mta;
  
  while (1) {
    //x_assert(critical_status == 0);
    if (fsm_state == 3) {
      status = x_monitor_enter(monitor, 5);
      if (status != xs_no_instance) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      set_state(mt, 4);
      status = x_monitor_enter(monitor, x_eternal);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      set_state(mt, 5);
      status = x_monitor_notify(monitor);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }

      while (! (fsm_state == 12 || fsm_state == 13)) {
        status = x_monitor_wait(monitor, x_eternal);
        if (status != xs_success) {
          oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
          exit(0);
        }
      }

      if (monitor->owner != x_thread_current()) {
        oempa("Bad owner %p != %p\n", monitor->owner, x_thread_current());
        exit(0);
      }
      if (monitor->count != 1) {
        oempa("Bad count %d != 1.\n", monitor->count);
        exit(0);
      }

      if (fsm_state != 13) {
        oempa("Bad state %d != 13.\n", fsm_state);
        exit(0);
      }

      set_state(mt, 14);
      status = x_monitor_notify_all(monitor);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }

//      while (! (fsm_state == 20 || fsm_state == 18)) {
      while (fsm_state != 20) {
        status = x_monitor_wait(monitor, x_eternal);
        if (status != xs_success) {
          oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
          exit(0);
        }
      }

      if (monitor->owner != x_thread_current()) {
        oempa("Bad owner %p != %p\n", monitor->owner, x_thread_current());
        exit(0);
      }
      if (monitor->count != 1) {
        oempa("Bad count %d != 1.\n", monitor->count);
        exit(0);
      }

      while (fsm_state != 20) {
        x_thread_sleep(100);
      }
      
      set_state(mt, 21);
      status = x_monitor_notify_all(monitor);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      status = x_monitor_exit(monitor);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      
      return;
 
    }
    
    x_thread_sleep(fsm_sleep_time);
    
  }
  
}

static void mt_4(void * mta) {

  x_status status;
  mt_thread mt = mta;
  
  while (1) {
    //x_assert(critical_status == 0);
    if (fsm_state == 6) {
      set_state(mt, 7);
      status = x_monitor_enter(monitor, x_eternal);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      if (fsm_state != 8) {
        oempa("Bad state %d != 8.\n", fsm_state);
        exit(0);
      }
      status = x_monitor_notify(monitor);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      status = x_monitor_wait(monitor, fsm_sleep_time * 3);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }

      if (monitor->owner != x_thread_current()) {
        oempa("Bad owner %p != %p\n", monitor->owner, x_thread_current());
        exit(0);
      }
      if (monitor->count != 1) {
        oempa("Bad count %d != 1.\n", monitor->count);
        exit(0);
      }
      if (fsm_state != 9) {
        oempa("Bad state %d != 9.\n", fsm_state);
        exit(0);
      }

      set_state(mt, 10);
      status = x_monitor_notify(monitor);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      while (fsm_state != 12) {
        status = x_monitor_wait(monitor, x_eternal);
        if (status != xs_success) {
          oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
          exit(0);
        }
      }

      if (monitor->owner != x_thread_current()) {
        oempa("Bad owner %p != %p\n", monitor->owner, x_thread_current());
        exit(0);
      }
      if (monitor->count != 1) {
        oempa("Bad count %d != 1.\n", monitor->count);
        exit(0);
      }

      if (fsm_state != 12) {
        oempa("Bad state %d != 12.\n", fsm_state);
        exit(0);
      }
      set_state(mt, 13);
      status = x_monitor_notify(monitor);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }

      while (fsm_state != 18) {
        status = x_monitor_wait(monitor, x_eternal);
        if (status != xs_success) {
          oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
          exit(0);
        }
      }

      if (monitor->owner != x_thread_current()) {
        oempa("Bad owner %p != %p\n", monitor->owner, x_thread_current());
        exit(0);
      }
      if (monitor->count != 1) {
        oempa("Bad count %d != 1.\n", monitor->count);
        exit(0);
      }

      set_state(mt, 19);
      status = x_monitor_notify_all(monitor);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      status = x_monitor_exit(monitor);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }

      return;

    }
    
    x_thread_sleep(fsm_sleep_time);
    
  }
  
}

static void mt_5(void * mta) {

  x_status status;
  mt_thread mt = mta;
  x_thread thread = mt->thread;
  
  while (1) {
    //x_assert(critical_status == 0);
    if (fsm_state == 11) {
      status = x_monitor_enter(monitor, x_eternal);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      status = x_monitor_enter(monitor, x_eternal);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      set_state(mt, 12);
      status = x_monitor_notify_all(monitor);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      while (fsm_state != 16) {
        status = x_monitor_wait(monitor, x_eternal);
        if (status != xs_success) {
          oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
          exit(0);
        }
      }

      if (monitor->owner != thread) {
        oempa("Bad owner %p != %p\n", monitor->owner, thread);
        exit(0);
      }
      if (monitor->count != 2) {
        oempa("Bad count %d != 2.\n", monitor->count);
        exit(0);
      }

      set_state(mt, 17);      
      status = x_monitor_exit(monitor);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      if (monitor->owner != thread) {
        oempa("Bad owner\n");
        exit(0);
      }
      status = x_monitor_notify_all(monitor);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      status = x_monitor_exit(monitor);
      if (status != xs_success) {
        oempa("%s: Bad status '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }

      return;
      
    }
    
    x_thread_sleep(fsm_sleep_time);
    
  }
  
}

//static x_thread fsm_thread;

static const x_size fsm_prio_low = 3;
static const x_size fsm_prio_mid = 2;
static const x_size fsm_prio_high = 1;

static void monitor_fsm(void * t) {

  x_int i;
  x_int counter = 0;
  x_int check_counter = 0;
  x_int check_max = 0;
  x_status status;
  //x_boolean do_stop = false;
  
  while (1) {
    //x_assert(critical_status == 0);
    check_counter += 1;
    if (fsm_state == 0) {
      monitor = NULL;
      counter += 1;
      check_counter = 0;
      mt_Threads = x_mem_get(num_fsm_threads * sizeof(mt_Thread));
      memset(mt_Threads, 0x00, num_fsm_threads * sizeof(mt_Thread));
      for (i = 0; i < num_fsm_threads; i++) {
        mt_Threads[i].thread = &mt_Threads[i].Thread;
        mt_Threads[i].stack = x_mem_get(MONT_STACK_SIZE);
      }
      status = x_thread_create(mt_Threads[0].thread, mt_1, &mt_Threads[0], mt_Threads[0].stack, MONT_STACK_SIZE, prio_offset + fsm_prio_low, TF_START);
      mt_Threads[0].prio = prio_offset + fsm_prio_low;
      if (status != xs_success) {
        oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      status = x_thread_create(mt_Threads[1].thread, mt_2, &mt_Threads[1], mt_Threads[1].stack, MONT_STACK_SIZE, prio_offset + fsm_prio_low, TF_START);
      mt_Threads[1].prio = prio_offset + fsm_prio_low;
      if (status != xs_success) {
        oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      status = x_thread_create(mt_Threads[2].thread, mt_3, &mt_Threads[2], mt_Threads[2].stack, MONT_STACK_SIZE, prio_offset + fsm_prio_low, TF_START);
      mt_Threads[2].prio = prio_offset + fsm_prio_low;
      if (status != xs_success) {
        oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      status = x_thread_create(mt_Threads[3].thread, mt_4, &mt_Threads[3], mt_Threads[3].stack, MONT_STACK_SIZE, prio_offset + fsm_prio_mid, TF_START);
      mt_Threads[3].prio = prio_offset + fsm_prio_mid;
      if (status != xs_success) {
        oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      status = x_thread_create(mt_Threads[4].thread, mt_5, &mt_Threads[4], mt_Threads[4].stack, MONT_STACK_SIZE, prio_offset + fsm_prio_high, TF_START);
      mt_Threads[4].prio = prio_offset + fsm_prio_high;
      if (status != xs_success) {
        oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      fsm_state = 1;
    }

    if (check_counter > check_max) {
      check_max = check_counter;
    }

    if (check_counter > 30) {
      //oempa("Stuck at state state = %d; critical_state = %d\n", fsm_state, critical_status);
      oempa("Stuck at state state = %d\n", fsm_state);
      dump_fsm_threads();
      exit(0);
    }

#if 0
    /*
    ** See if we have our priorities still intact.
    */

    for (i = 0; i < num_fsm_threads; i++) {
      if (mt_Threads[i].thread->a_prio != mt_Threads[i].prio) {
        oempa("State %d: assigned priority of thread %d has changed from %d to %d!\n", fsm_state, i + 1, mt_Threads[i].prio, mt_Threads[i].thread->a_prio);
        do_stop = true;
      }
    }

    if (do_stop) {
      exit(0);
    }
#endif
    
    if (fsm_state == 25) {
      oempa("At loop %d, reseting FSM, check max = %d (sizeof(x_Monitor) = %d).\n", counter, check_max, sizeof(x_Monitor));
      for (i = 0; i < num_fsm_threads; i++) {
#if 0
        if (mt_Threads[i].thread->waiting_for != NULL) {
          oempa("Waiting for thread %d is not NULL.\n", i);
          exit(0);
        }
        if (mt_Threads[i].thread->l_owned != NULL) {
          oempa("Owned list for thread %d is not NULL.\n", i);
          exit(0);
        }
        if (mt_Threads[i].thread->a_prio != mt_Threads[i].thread->c_prio) {
          oempa("Priority of thread %d has not been reset properly...\n", i);
          exit(0);
        }
        if (mt_Threads[i].thread->a_prio != mt_Threads[i].prio) {
          oempa("Assigned priority of thread %d has changed from %d to %d!\n", i, mt_Threads[i].prio, mt_Threads[i].thread->a_prio);
          exit(0);
        }
#endif
      }

      for (i = 0; i < num_fsm_threads; i++) {
        x_mem_free(mt_Threads[i].stack);
      }
      x_mem_free(mt_Threads);
      x_monitor_delete(monitor);
      x_mem_free(monitor);
      fsm_state = 0;
      x_thread_sleep(second * 5);
    }

    x_thread_sleep(second * 2);

  }
  
}

static x_thread int_thread;
static x_int int_state = 0;

static void monitor_waiter(void * m) {

  x_monitor mon = m;
  x_status status;

  oempa("Waiter thread that will be stopped alive...\n");
  
  while (1) {
    if (int_state == 1) {
      status = x_monitor_enter(mon, x_eternal);
      if (status != xs_success) {
        oempa("Monitor enter should be OK!\n");
        exit(0);
      }
      
      /*
      ** OK, we have the monitor and the state is 1; we notify the other thread
      ** we're in this state and start waiting forever. This wait will be
      ** stopped by the other thread, so we should get xs_interrupted as status.
      */
      
      int_state = 2;
      status = x_monitor_notify(mon);
      if (status != xs_success) {
        oempa("Monitor notify should be OK!\n");
        exit(0);
      }

      oempa("Stoppable thread going into wait that will be interrupted...\n");
      while (int_state == 2) {
        status = x_monitor_wait(mon, x_eternal);
        if (status != xs_interrupted) {
          oempa("Should be '%s' is now '%s'\n", x_status2char(xs_interrupted), x_status2char(status));
          exit(0);
        }
      }
      
    }
    else if (int_state == 3) {
      oempa("Test finished...\n");
      int_state = 4;
      return;
    }
    else {
      x_thread_sleep(5);
    }
  }
  
}

static void monitor_stopper(void * thread) {

  x_thread stopper = thread;
  x_monitor mon = NULL;
  x_thread waiter = NULL;
  x_status status;
  x_ubyte * stack = NULL;
  x_int int_runs = 0;

  while (1) {
    oempa("int_state=%d;\n", int_state);
    if (int_state == 0) {
    
      /*
      ** Create the monitor first and then enter it before creating the waiter
      ** thread.
      */

      int_runs += 1;      
      mon = x_mem_get(sizeof(x_Monitor));
      stack = x_mem_get(MONT_STACK_SIZE);
      x_monitor_create(mon);
      status = x_monitor_enter(mon, x_eternal);
      if (status != xs_success) {
        oempa("Monitor enter should be OK!\n");
        exit(0);
      }

      /*
      ** Now create the waiter thread and start it, make it have a priority that
      ** is slightly higher than this thread.
      */
      
      waiter = x_mem_get(sizeof(x_Thread));
      status = x_thread_create(waiter, monitor_waiter, mon, stack, MONT_STACK_SIZE, 0, TF_START);
      if (status != xs_success) {
        oempa("Thread create should be OK!\n");
        exit(0);
      }
      
      /*
      ** Now set the state to 1 and wait; thread 2 is trying to enter the monitor and will
      ** get it as soon as we start waiting.
      */

      oempa("Stop waiting thread, run number %d;\n", int_runs);
      int_state = 1;
      while (int_state == 1) {
        status = x_monitor_wait(mon, x_eternal);
        if (status != xs_success) {
          oempa("Monitor wait should be OK not '%s'!\n", x_status2char(status));
          exit(0);
        }
      }
    }
    else if (int_state == 2) {
    
      /*
      ** We're going to stop the other thread from waiting; first we change its priority to
      ** a notch lower than this thread and do a stop waiting; this should give 'success'. Then
      ** we try a second time, which should give 'no instance'. We decrease the priority first
      ** since we want the other thread not to take over before we tried our stop waiting a
      ** second time. Then we set the priority again to the original 'notch higher' of this
      ** stopper thread.
      */
      
      int_state = 3;
      oempa("Going to stop other thread from waiting...\n");
      //x_thread_priority_set(waiter, stopper->c_prio + 1);
      status = x_thread_stop_waiting(waiter);
      if (status != xs_success) {
        oempa("Should be '%s' is now '%s'\n", x_status2char(xs_success), x_status2char(status));
        exit(0);
      }
/*
      status = x_thread_stop_waiting(waiter);
      if (status != xs_no_instance) {
        oempa("Should be '%s' is now '%s'\n", x_status2char(xs_no_instance), x_status2char(status));
        exit(0);
      }
*/
      //x_thread_priority_set(waiter, stopper->c_prio - 1);
    }
    else if (int_state == 4) {
      oempa("Cleaning up after run %d; %d bytes available.\n", int_runs, x_mem_avail());
      status = x_thread_delete(waiter);
      if (status != xs_success) {
        oempa("Should be '%s' is now '%s'\n", x_status2char(xs_success), x_status2char(status));
        exit(0);
      }
      status = x_monitor_delete(mon);
      if (status != xs_success) {
        oempa("Should be '%s' is now '%s'\n", x_status2char(xs_success), x_status2char(status));
        exit(0);
      }
      x_mem_free(waiter);
      x_mem_free(stack);
      x_mem_free(mon);
      int_state = 0;
      x_thread_sleep(second);
    }
    else {
      x_thread_sleep(5);
    }
  }

}

x_ubyte * monitor_test(x_ubyte * memory) {

  x_status status;

  second = x_seconds2ticks(1);
  oempa("1 second = %d ticks.\n", second);
  
  fsm_sleep_time = x_usecs2ticks(500000);
  oempa("FSM sleep time = %d ticks.\n", fsm_sleep_time);
  
  control = x_alloc_static_mem(memory, sizeof(x_Thread));
  status = x_thread_create(control, monitor_tests, control, x_alloc_static_mem(memory, MONT_STACK_SIZE), MONT_STACK_SIZE, prio_offset + 2, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }
  else {
    oempa("Monitor control thread 1, id = %p\n", control);
  }
#if 0
  fsm_thread = x_alloc_static_mem(memory, sizeof(x_Thread));
  status = x_thread_create(fsm_thread, monitor_fsm, fsm_thread, x_alloc_static_mem(memory, MONT_STACK_SIZE), MONT_STACK_SIZE, prio_offset + fsm_prio_high, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }
  else {
    oempa("Monitor control thread 2, id = %p\n", fsm_thread);
  }
#endif

  int_thread = x_alloc_static_mem(memory, sizeof(x_Thread));
  status = x_thread_create(int_thread, monitor_stopper, int_thread, x_alloc_static_mem(memory, MONT_STACK_SIZE), MONT_STACK_SIZE, 4, TF_START);

  return memory;

}
