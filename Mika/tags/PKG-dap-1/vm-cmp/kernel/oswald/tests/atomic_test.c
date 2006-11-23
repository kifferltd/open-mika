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
** $Id: atomic_test.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <string.h>
#include <tests.h>
#include <oswald.h>

static x_sleep second;

static x_thread ee_thread;
static int ee_state = 0;
static int int_1 = 1;
static int int_2 = 2;

#define ATOT_STACK_SIZE ((1024 * 4) + MARGIN)

static x_thread ee_current = NULL;

static void atomic_enter_exit(void * t) {

  x_thread thread = t;
  x_int i;
  static x_int exit_clashes = 0;
  static x_int loops = 0;
  
  while (1) {
    x_assert(critical_status == 0);
    if (ee_state == 1) {
    
      loops++;

      /*
      ** Test the atomic swap operation while not being in the critical region.
      */
      
      x_atomic_swap(&int_1, &int_2);

      if (int_1 == 2 && int_2 != 1) {
        oempa("Swapped wrong: int_1 = %d, int_2 = %d\n", int_1, int_2);
        exit(0);
      }
      
      if (int_1 == 1 && int_2 != 2) {
        oempa("Unswapped wrong: int_1 = %d, int_2 = %d\n", int_1, int_2);
        exit(0);
      }

      /*
      ** Test atomic regions.
      */

      while (! xi_enter_atomic(&ee_current, thread)) {
        x_thread_sleep(x_random() % 100 + 10);
      }
      
      oempa("Thread %d entered... (%d clashes)\n", thread->id, exit_clashes);

      /*
      ** Check if it contains the right value. Carefull because the ee_current could
      ** also contain the looking value of '1'. Do this a few times, i.e. 10 and sleep
      ** in between each attempt, so that we give the other threads a good run for their
      ** money...
      */
      
      for (i = 0; i < 10; i++) {
        if (ee_current == (x_thread) 0x00000001) {
          oempa("Would have a clash...\n");
        }
        if (ee_current != thread) {
          if (ee_current != (x_thread) 1) {
            oempa("Bad (%d) ee_current = 0x%08x\n", i, ee_current);
            exit(0);
          }
        }

        /*
        ** The following sleep is only for TEST purposes. Don't ever do this in
        ** production code, unless you want to get a kick in the but or have
        ** an extremely good reason for it!
        */

        x_thread_sleep(20);
      }

      /*
      ** Exit the atomic region. We only record exit clashes because these are
      ** the only ones caused by 2 threads clashing; i.e. one thread is trying
      ** to enter while there is the 'looking' value in the memory location. In the
      ** enter case we would have a clash each time the owner thread is sleeping in
      ** the loop above and an other thread was wanting to plant it's thread pointer
      ** in 'ee_current'. The test is made such that this must happen often, so we
      ** don't count these...
      */
      
      while (! xi_exit_atomic(&ee_current)) {
        oempa("Exit clash %d, %d loops\n", ++exit_clashes, loops);
        x_thread_sleep(10);
      }

      x_thread_sleep(x_random() % 100 + 20);
      
    }
    
    if (ee_state == 2) {
      return;
    }
    
  }
  
}

static void ee_test(void *t) {

  x_thread th1;
  x_ubyte * st1;
  x_thread th2;
  x_ubyte * st2;
  x_thread th3;
  x_ubyte * st3;
  x_thread th4;
  x_ubyte * st4;
  x_thread th5;
  x_ubyte * st5;
  x_thread th6;
  x_ubyte * st6;
  x_status status;
  
  while (1) {
    x_assert(critical_status == 0);
    if (ee_state == 0) {
      th1 = x_mem_get(sizeof(x_Thread));
      memset(th1, 0xff, sizeof(x_Thread));
      th2 = x_mem_get(sizeof(x_Thread));
      memset(th2, 0xff, sizeof(x_Thread));
      th3 = x_mem_get(sizeof(x_Thread));
      memset(th3, 0xff, sizeof(x_Thread));
      th4 = x_mem_get(sizeof(x_Thread));
      memset(th4, 0xff, sizeof(x_Thread));
      th5 = x_mem_get(sizeof(x_Thread));
      memset(th5, 0xff, sizeof(x_Thread));
      th6 = x_mem_get(sizeof(x_Thread));
      memset(th6, 0xff, sizeof(x_Thread));
      
      st1 = x_mem_get(ATOT_STACK_SIZE);
      st2 = x_mem_get(ATOT_STACK_SIZE);
      st3 = x_mem_get(ATOT_STACK_SIZE);
      st4 = x_mem_get(ATOT_STACK_SIZE);
      st5 = x_mem_get(ATOT_STACK_SIZE);
      st6 = x_mem_get(ATOT_STACK_SIZE);
      
      status = x_thread_create(th1, atomic_enter_exit, th1, st1, ATOT_STACK_SIZE, prio_offset + 5, TF_START);
      if (status != xs_success) {
        oempa("Status is '%s'\n", x_status2char(status));
        exit(0);
      }
      status = x_thread_create(th2, atomic_enter_exit, th2, st2, ATOT_STACK_SIZE, prio_offset + 5, TF_START);
      if (status != xs_success) {
        oempa("Status is '%s'\n", x_status2char(status));
        exit(0);
      }
      status = x_thread_create(th3, atomic_enter_exit, th3, st3, ATOT_STACK_SIZE, prio_offset + 5, TF_START);
      if (status != xs_success) {
        oempa("Status is '%s'\n", x_status2char(status));
        exit(0);
      }
      status = x_thread_create(th4, atomic_enter_exit, th4, st4, ATOT_STACK_SIZE, prio_offset + 5, TF_START);
      if (status != xs_success) {
        oempa("Status is '%s'\n", x_status2char(status));
        exit(0);
      }
      status = x_thread_create(th5, atomic_enter_exit, th5, st5, ATOT_STACK_SIZE, prio_offset + 5, TF_START);
      if (status != xs_success) {
        oempa("Status is '%s'\n", x_status2char(status));
        exit(0);
      }
      status = x_thread_create(th6, atomic_enter_exit, th6, st6, ATOT_STACK_SIZE, prio_offset + 5, TF_START);
      if (status != xs_success) {
        oempa("Status is '%s'\n", x_status2char(status));
        exit(0);
      }
      ee_current = (x_thread)0;
      ee_state = 1;
    }
    
    x_thread_sleep(second * 5);
    
  }
  
}

x_ubyte * atomic_test(x_ubyte * memory) {

  x_status status;

  second = x_seconds2ticks(1);  

  ee_thread = x_alloc_static_mem(memory, sizeof(x_Thread));
  status = x_thread_create(ee_thread, ee_test, ee_thread, x_alloc_static_mem(memory, ATOT_STACK_SIZE), ATOT_STACK_SIZE, prio_offset + 4, TF_START);
  if (status != xs_success) {
    oempa("Status is '%s'\n", x_status2char(status));
    exit(0);
  }

  return memory;
  
}
