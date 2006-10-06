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
** $Id: timer_test.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
**
** Timer test routines.
*/

#include <tests.h>

static x_sleep second;

#define TIMT_STACK_SIZE ((1024 * 1) + MARGIN)

static x_timer timer1;
static x_timer timer2;
static x_timer timer3;
static x_thread thread1;

/*
** These control the maximum difference between the number of ticks a timer
** should fire at each time and the real time measured between 2 fire events. When
** the difference exceeds this value, the test is stopped (fails), but
** when we debug and dump threads, the init thread can sometimes take too much time
** and therefore we set max_difference_not_fatal to 'true'.
*/

const x_boolean max_difference_not_fatal = true;
const x_time max_difference = 10;

static void timer1_fire(x_timer timer) {

  static x_int changed = 0;
  static x_time last_ticks = 0;
  x_time diff;
  static x_int counter = 0;
  
  counter += 1;
  
  if (timer->fired == 1) {
    oempa("Timer %d changed %d times allready...\n", timer->id, changed);
    last_ticks = x_time_get();
    changed += 1;
  }
  else {
    diff = x_time_get() - last_ticks;
    if (counter % 100 == 0) {
      oempa("At counter %d: timer %d fired %d times, changed %d times... diff ticks = %d\n", counter, timer->id, timer->fired, changed, diff);
    }
    if (diff > timer->repeat) {
      if (diff - timer->repeat > max_difference) {
        oempa("Latency too high... %d <-> %d\n", diff, timer->repeat);
        if (! max_difference_not_fatal) {
          exit(0);
        }
      }
    }
    else {
      if (timer->repeat - diff > max_difference) {
        oempa("Latency too high... %d <-> %d\n", diff, timer->repeat);
        if (! max_difference_not_fatal) {
          exit(0);
        }
      }
    }
    last_ticks = x_time_get();
  }

}

static void timer2_fire(x_timer timer) {

  static x_time last_ticks = 0;
  static x_int counter = 0;

  counter += 1;
  
  if (last_ticks == 0) {
    last_ticks = x_time_get();
    oempa("First time timer fires...\n");
  }
  else {
    if (counter % 100 == 0) {
      oempa("At count %d: timer %d fired ... diff ticks = %d\n", counter, timer->id, x_time_get() - last_ticks);
    }
    last_ticks = x_time_get();
  }

}

static void thread1_entry(void * t) {

  x_status status;
  x_int counter;
  x_time initial;
  x_time repeat;
  
  status = x_timer_create(timer1, timer1_fire, 100, 200, TIMER_AUTO_START);
  
  oempa("Status of 1 create = %s\n", x_status2char(status));
  
  if (status != xs_success) {
    oempa("Timer create failed...\n");
    return;
  }

  status = x_timer_create(timer2, timer2_fire, 30, 50, TIMER_DONT_START);
  
  oempa("Status of 2 create = %s\n", x_status2char(status));
  
  if (status != xs_success) {
    oempa("Timer create failed...\n");
    return;
  }

  status = x_timer_create(timer3, timer2_fire, 30, 50, TIMER_AUTO_START);
  
  oempa("Status of 3 create = %s\n", x_status2char(status));
  
  if (status != xs_success) {
    oempa("Timer create failed...\n");
    return;
  }

  counter = 0;
  
  while (1) {
    x_assert(critical_status == 0);
    x_thread_sleep(second * 2);
    counter += 1;

    if (counter == 5) {
      oempa("Activating timer %d\n", timer2->id);
      status = x_timer_activate(timer2);
      oempa("status = %s\n", x_status2char(status));
    }

    if (counter % 7 == 0) {
      status = x_timer_deactivate(timer1);
      if (status != xs_success) {
        oempa("Problem: %s\n", x_status2char(status));
        exit(0);
      }
      initial = (x_random() % 500) + 1;
      repeat = (x_random() % 1000) + 1;
      oempa("------ Changing timer %d initial = %2d, repeat = %2d ----------------------\n", timer1->id, initial, repeat);
      status = x_timer_change(timer1, initial, repeat);
      if (status != xs_success) {
        oempa("Problem: %s\n", x_status2char(status));
        exit(0);
      }
      status = x_timer_activate(timer1);
      if (status != xs_success) {
        oempa("Problem: %s\n", x_status2char(status));
        exit(0);
      }
    }
    
    if (counter % 10 == 0) {
      status = x_timer_deactivate(timer2);
      if (status != xs_success) {
        oempa("Problem: %s\n", x_status2char(status));
        exit(0);
      }

      status = x_timer_delete(timer2);
      if (status != xs_success) {
        oempa("Problem: %s\n", x_status2char(status));
        exit(0);
      }

      initial = (x_random() % 500) + 1;
      repeat = (x_random() % 1000) + 1;
      status = x_timer_create(timer2, timer2_fire, initial, repeat, TIMER_AUTO_START);
      if (status != xs_success) {
        oempa("Problem: %s\n", x_status2char(status));
        exit(0);
      }
      
      oempa("****** Stopped, deleted and re-created timer %d, initial %d, repeat %d  *********\n", timer2->id, initial, repeat);

    }

    if (counter % 11 == 0) {
      status = x_timer_deactivate(timer3);
      if (status != xs_success) {
        oempa("Problem: %s\n", x_status2char(status));
        exit(0);
      }

      status = x_timer_delete(timer3);
      if (status != xs_success) {
        oempa("Problem: %s\n", x_status2char(status));
        exit(0);
      }

      initial = (x_random() % 500) + 1;
      repeat = (x_random() % 1000) + 1;
      status = x_timer_create(timer3, timer2_fire, initial, repeat, TIMER_AUTO_START);
      if (status != xs_success) {
        oempa("Problem: %s\n", x_status2char(status));
        exit(0);
      }
      
      oempa("****** Stopped, deleted and re-created timer %d, initial %d, repeat %d  *********\n", timer3->id, initial, repeat);

    }
  }
  
}

x_ubyte * timer_test(x_ubyte * memory) {

  x_ubyte * stack;
  x_status status;

  second = x_seconds2ticks(1);
    
  timer1 = x_alloc_static_mem(memory, sizeof(x_Timer));
  timer2 = x_alloc_static_mem(memory, sizeof(x_Timer));
  timer3 = x_alloc_static_mem(memory, sizeof(x_Timer));

  thread1 = x_alloc_static_mem(memory, sizeof(x_Thread));

  stack = x_alloc_static_mem(memory, TIMT_STACK_SIZE);
  status = x_thread_create(thread1, thread1_entry, thread1, stack, TIMT_STACK_SIZE, prio_offset + 3, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }

  return memory;
  
}
