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
** $Id: signals_test.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <string.h>
#include <tests.h>

static x_sleep second;

#define SIGT_STACK_SIZE ((1024 * 1) + MARGIN)

static x_ubyte * buffer;
#define BUFFER_SIZE (128)

static x_thread signals_thread;
static x_int num_threads;
static x_int num_signals;

typedef struct s_Thread * s_thread;

typedef struct s_Thread {
  x_Thread Thread;
  x_thread thread;
  x_ubyte * stack;
  x_int set_ops;
  x_int get_ops;
  x_int success;
  x_int fail;
  x_int deleted;
} s_Thread;

/*
** The array of tester threads and the array of tested signals.
*/

static s_Thread * s_Threads;
static x_signals * s_signals;

/*
** The state of testing:
**
** 0 signals_control is initializing the threads and the signals.
** 1 threads can go on testing signal operations.
** 2 signals_control has deleted the signals, threads should return (stop) after the current operation.
** 3 signals_control is cleaning up the memory and printing a report.
**
*/

static volatile x_int state = 0;

#define GET_LIMIT (10)

static void do_signals(void * t) {

  s_thread ts = t;
  x_flags flags;
  x_flags actual;
  x_option option;
  x_status status = xs_success;
  x_sleep to;
  unsigned int old_ticks;
  unsigned int cur_ticks;
  x_signals signals = NULL;
  x_int operation = 0;

  while (1) {
    x_assert(critical_status == 0);
    if (state == 1) {
      operation = x_random() % 16;
      to = x_random() % 20;
      if (to == 19) {
        to = x_eternal;
        x_snprintf(buffer, BUFFER_SIZE, "eternal ");
      }
      else {
        x_snprintf(buffer, BUFFER_SIZE, "%2d ticks", to);
      }
      signals = s_signals[x_random() % num_signals];


      /*
      ** Perform a 'get' operation...
      */

      if (operation <= GET_LIMIT) {
        ts->get_ops += 1;
        flags = x_random() & 0x0000000f;
        option = x_random() & 0x00000003;
        old_ticks = system_ticks;
        status = x_signals_get(signals, flags, option, &actual, to);
        cur_ticks = system_ticks;

        /*
        ** First check for a deleted status and check wether it is appropriate.
        */

        if (status == xs_deleted) {
          if (state == 2) {
            ts->deleted += 1;
            return;
          }
          else {
            oempa("Deleted while not correct state...\n");
            exit(0);
          }
        }

        /*
        ** Check that the number of competing threads looks normal...
        */
        
        if (num_threads < (x_int)signals->Event.n_competing) {
          oempa("Number of competitors is wrong %d < %d\n", num_threads, signals->Event.n_competing);
          exit(0);
        }
        
        /*
        ** Check that the timing looks correct, i.e. did we wait long enough for success...
        */
        
        if (status != xs_success && cur_ticks - old_ticks < to) {
          oempa("thread %d has a timing problem to = %d, current ticks = %d, started = %d\n", to, ts->thread->id, cur_ticks, old_ticks);
          exit(0);
        }

        /*
        ** Check the 'or' correctness
        */
        
        if (status == xs_success && (option == xo_or || option == xo_or_clear)) {
          if ((actual & flags) == 0) {
            oempa("thread %d has a problem\n", ts->thread->id);
            exit(0);
          }
          ts->success += 1;
        }        
        else {
          ts->fail += 1;
        }

        /*
        ** Check the 'and' correctness
        */

        if (status == xs_success && (option == xo_and || option == xo_and_clear)) {
          if ((actual & flags) != flags) {
            oempa("thread %d has a problem\n", ts->thread->id);
            exit(0);
          }
          ts->success += 1;
        }        
        else {
          ts->fail += 1;
        }

      }

      /*
      ** Perform a 'set' operation...
      */

      else {
        ts->set_ops += 1;
        flags = x_random() & 0x0000000f;
        option = x_random() & 0x00000003;
        status = x_signals_set(signals, flags, option);
        
        if (status == xs_deleted) {
          if (state != 2) {
            oempa("Bad state\n");
            exit(0);
          }
          else {
            ts->deleted += 1;
            return;
          }
        }

        /*
        ** See that we get a correct 'xs_bad_option' if we pass the clear option to a 'set' operation.
        */

        if ((option == xo_or_clear || option == xo_and_clear) && status != xs_bad_option) {
          oempa("thread %d has a problem\n", ts->thread->id);
          exit(0);
        }
        ts->success += 1;
      }

      x_thread_sleep(120);

    }

    if (operation <= GET_LIMIT) {
      oempa("Thread %3d GET, signals = %p, to = %s, status = '%s'\n", ts->thread->id, signals, buffer, x_status2char(status));
    }
    else {
      oempa("Thread %3d SET, signals = %p, status = '%s'\n", ts->thread->id, signals, x_status2char(status));
    }        

    if (state == 2) {
      return;
    }

  }
  
}

static void signals_control(void * t) {

  x_status status;
  x_int counter = 0;
  x_int loop = 0;
  x_int i;
  x_boolean still_living;
  x_int set_ops;
  x_int get_ops;
  x_int success;
  x_int fail;
  x_int deleted;

  while (irq_depth) {
    x_thread_sleep(10);
    continue;
  }
  
  while (1) {
    x_assert(critical_status == 0);
    counter += 1;
    if (state == 0) {
      loop += 1;
      buffer = x_mem_get(BUFFER_SIZE);

      /*
      ** Create the signals array and the signals.
      */
      
      num_signals = x_random() % 4 + 1;
      s_signals = x_mem_get(sizeof(x_signals) * num_signals);
      for (i = 0; i < num_signals; i++) {
        s_signals[i] = x_mem_get(sizeof(x_Signals));
        memset(s_signals[i], 0xff, sizeof(x_Signals));
        x_signals_create(s_signals[i]);
        if (s_signals[i]->flags != 0x00000000) {
          oempa("Bad signals start 0x%08x\n", s_signals[i]->flags);
          exit(0);
        }
      }
      oempa("Starting signals tests, sizeof(x_Signals) = %d\n", sizeof(x_Signals));

      /*
      ** Create the tester thread array and the threads.
      */
      
      num_threads = x_random() % 6 + 2;
      s_Threads = x_mem_get(sizeof(s_Thread) * num_threads);
      memset(s_Threads, 0x00, sizeof(s_Thread) * num_threads);
      for (i = 0; i < num_threads; i++) {
        s_Threads[i].thread = &s_Threads[i].Thread;
        s_Threads[i].stack = x_mem_get(SIGT_STACK_SIZE);
        status = x_thread_create(s_Threads[i].thread, do_signals, &s_Threads[i], s_Threads[i].stack, SIGT_STACK_SIZE, prio_offset + 4, TF_START);
        if (status != xs_success) {
          oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
          exit(0);
        }
      }
      state = 1;
    }
    
    if (counter % 10 == 0) {

      /*
      ** Set state and delete all signals. Since we run at a higher priority, the tester threads will
      ** not see state 2 before all signals are deleted.
      */

      state = 2;

      for (i = 0; i < num_signals; i++) {
        status = x_signals_delete(s_signals[i]);
      }

      /*
      ** Check that all threads are dead...
      */

      still_living = true;
      while (still_living) {
        still_living = false;
        for (i = 0; i < num_threads; i++) {
          if (s_Threads[i].thread->state != xt_ended) {
            still_living = true;
            break;
          }
        }
        x_thread_sleep(second);
      }

      
      state = 3;

      /*
      ** Release the memory, sleep and restart...
      */
      
      set_ops = 0;
      get_ops = 0;
      success = 0;
      fail = 0;
      deleted = 0;
      for (i = 0; i < num_threads; i++) {
        set_ops += s_Threads[i].set_ops;
        get_ops += s_Threads[i].get_ops;
        success += s_Threads[i].success;
        fail += s_Threads[i].fail;
        deleted += s_Threads[i].deleted;
        x_mem_free(s_Threads[i].stack);
      }
      x_mem_free(s_Threads);

      for (i = 0; i < num_signals; i++) {
        x_mem_free(s_signals[i]);
      }
      x_mem_free(s_signals);
      x_mem_free(buffer);

      oempa("At count %d, in %d threads, %d get, %d set, %d success, %d fail, %d deleted.\n", counter, num_threads, get_ops, set_ops, success, fail, deleted);
      x_thread_sleep(second * 30);
      state = 0;
      
    }

    x_thread_sleep(second * 1);

  }
  
}

x_ubyte * signals_test(x_ubyte * memory) {

  x_status status;

  second = x_seconds2ticks(1);
  
  signals_thread = x_alloc_static_mem(memory, sizeof(x_Thread));
  status = x_thread_create(signals_thread, signals_control, signals_thread, x_alloc_static_mem(memory, SIGT_STACK_SIZE), SIGT_STACK_SIZE, prio_offset + 3, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }

  return memory;
  
}
