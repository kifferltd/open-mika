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
** $Id: mutex_test.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <stdio.h>
#include <string.h>
#include <tests.h>

static x_sleep second;

#define MUTT_STACK_SIZE ((1024 * 2) + MARGIN)

/*

The finite state machine with 3 threads for testing the mutex functionality. The create_delete thread
will create the 3 threads and brings the state from 0 to 1 after the threads are created, then this
finite state machine begins looping...

-------------------------------+-------------------------------+-------------------------------------+
                               |                               |                                     |
           thread 2            |           thread 3            |             thread 4                |
                               |                               |                                     |
 if state == 1, alloc mem for  |                               |                                     |
 mutex and create it. Check    |                               |                                     |
 that it is in the list and    |                               |                                     |
 set state to 2. We check that |                               |                                     |
 event is properly initialized |                               |                                     |
 and then we sleep. ------------> if state == 2, try locking   |                                     |
                               |  which should succeed. Check  |                                     |
                               |  that we are the owner and    |                                     |
                               |  set state to 3. Sleep --------> try locking, should fail. Set the  |
                               |                               |  state to 4 and try locking with    |
                               |                               |  eternal timeout. Should block. +   |
                               |                               |                                 |   |
 If state is 4, try deleting  <------------------------------------------------------------------+   |
 the mutex. This should fail   |                               |                                     |
 with 'not owner'. Set state   |                               |                                     |
 to 5 and sleep. We also check |                               |                                     |
 that n_competing == 1. --------> Try deleting the mutex; this |                                     |
                               |  should succeed and somewhere |                                     |
                               |  in the deletion process, we  |                                     |
                               |  switch to thread 3 that is   |                                     |
                               |  asking for a lock. In this  --> The state of the previous lock     |
                               |  thread we also check that,   |  attempt should return xs_deleted   |
                               |  the priority inversion       |  and if this is the case, we set    |
                               |  system is working...         |  the state to 6 and sleep. +        |
                               |                               |                            |        |
 Set the state to 7 and sleep <-------------------------------------------------------------+        |
 (We could do some other check |                               |                                     |
 here ?) -----------------------> Check that the event is out  |                                     |
                               |  of the list. Release the     |                                     |
                               |  memory of the mutex and      |                                     |
                               |  set the state to 1 and then  |                                     |
                               |  sleep. This brings us back   |                                     |
                               |  to the beginning state.      |                                     |
                               |                               |                                     |
-------------------------------+-------------------------------+-------------------------------------+

*/

static x_thread t1;
static x_ubyte *st1;

static x_thread t2;
static x_ubyte *st2;

static x_thread t3;
static x_ubyte *st3;

static x_thread t4;
static x_ubyte *st4;

#define CD_1_PRIO  4
#define CD_2_PRIO  4
#define CD_3_PRIO  3

#define REPEAT_SLEEP (second * 4)

static x_mutex create_delete_mutex;

static x_int create_delete_state = 0;
static x_int create_delete_check = 0;
static x_int create_delete_counter = 0;

static void cd_1(void * t) {

  x_status status;
  x_time nap = 1;
  x_event event;
  x_thread thread = t;
  x_ushort type;

  oempa("thread id %d\n", thread->id);
  
  while (1) {
    x_assert(critical_status == 0);

    if (create_delete_state == 1) {
      create_delete_mutex = x_mem_get(sizeof(x_Mutex));
      memset(create_delete_mutex, 0xff, sizeof(x_Mutex));
      status = x_mutex_create(create_delete_mutex);
      if (status != xs_success) {
        oempa("Status = %s\n", x_status2char(status));
        exit(0);
      }

      /*
      ** Test our event type and flag setting capabilities.
      */

      event = &create_delete_mutex->Event;
      x_event_type_set(event, xe_mutex);

      type = x_event_type_get(event);
      if (type != xe_mutex) {
        oempa("Bad type 0x%04x\n", event->flags_type);
        exit(0);
      }

      x_event_flag_set(event, 0xffff);
      if (event->flags_type != 0xff01) {
        oempa("Bad type & event flags 0x%04x != 0xff01 (0xff00 | mutex type 0x0001)\n", event->flags_type);
        exit(0);
      }

      x_event_flag_unset(event, 0x80ff);
      if (event->flags_type != 0x7f01) {
        oempa("Bad type & event flags 0x%04x != 0x7f01 (0x7f00 | mutex type 0x0001)\n", event->flags_type);
        exit(0);
      }

      if (x_event_flag_is_set(event, 0x80ff)) {
        oempa("Should not trigger 0x%04x!\n", event->flags_type);
        exit(0);
      }

      if (! x_event_flag_is_not_set(event, 0x80ff)) {
        oempa("Should not trigger 0x%04x!\n", event->flags_type);
        exit(0);
      }
      
      /*
      ** Now set a new type and check the flags are not compromised...
      */
      
      x_event_type_set(event, 0xffff);

      type = x_event_type_get(event);
      if (type != 0x00ff) {
        oempa("Bad type 0x%04x\n", event->flags_type);
        exit(0);
      }

      if (event->flags_type != 0x7fff) {
        oempa("Bad type & event flags 0x%04x != 0x7fff\n", event->flags_type);
        exit(0);
      }

      if (x_event_flag_is_set(event, 0x80ff)) {
        oempa("Should not trigger 0x%04x!\n", event->flags_type);
        exit(0);
      }

      if (! x_event_flag_is_not_set(event, 0x80ff)) {
        oempa("Should not trigger 0x%04x!\n", event->flags_type);
        exit(0);
      }
      
      /*
      ** Set type and flags back to normal so that we can proceed with the mutex tests...
      */

      x_event_type_set(event, xe_mutex);
      x_event_flag_unset(event, 0xff00);
      
      /*
      ** Mutex specific tests.
      */

      if (create_delete_mutex->owner != NULL) {
        oempa("owner is not null.\n");
        exit(0);
      }
      
      if (create_delete_mutex->Event.l_owned != NULL) {
        oempa("l_owned is not null.\n");
        exit(0);
      }

      if (x_event_type_get(&create_delete_mutex->Event) != xe_mutex) {
        oempa("Bad event type.\n");
        exit(0);
      }

      if (create_delete_mutex->Event.n_competing != 0) {
        oempa("n_competing is not 0.\n");
        exit(0);
      }

      if (create_delete_mutex->Event.l_competing != NULL) {
        oempa("l_competing is not NULL.\n");
        exit(0);
      }

      if (x_event_flag_is_set(&create_delete_mutex->Event, 0xffff)) {
        oempa("flags is not 0.\n");
        exit(0);
      }

      /*
      ** See that the threads are not competing for anything...
      */
      
      if (t2->competing_for != NULL) {
        oempa("Thread %d should not yet be competing...\n", t2->id);
        exit(0);
      }

      if (t3->competing_for != NULL) {
        oempa("Thread %d should not yet be competing...\n", t3->id);
        exit(0);
      }

      if (t4->competing_for != NULL) {
        oempa("Thread %d should not yet be competing...\n", t4->id);
        exit(0);
      }

      create_delete_state = 2;
      nap = x_random() % 5 + 1;
    }

    if (create_delete_state == 4) {
      status = x_mutex_delete(create_delete_mutex);
      if (status != xs_not_owner) {
        oempa("Status = %s\n", x_status2char(status));
        exit(0);
      }

      /*
      ** See that thread 4 is competing.
      */
      
      if (create_delete_mutex->Event.l_competing != t4) {
        oempa("Thread 4 is not competing...\n");
        exit(0);
      }

      if (create_delete_mutex->Event.n_competing != 1) {
        oempa("n_competing is wrong...\n");
        exit(0);
      }

      if (t4->competing_for != &create_delete_mutex->Event) {
        oempa("Thread 4 is competing for wrong event...\n");
        exit(0);
      }
      
      create_delete_state = 5;
      nap = x_random() % 9 + 1;
    }

    if (create_delete_state == 6) {
      // Could check something here ?? Otherwise, we can reduce one state...
      create_delete_state = 7;
    }

    x_thread_sleep(nap);

  }
  
}

static void cd_2(void * t) {

  x_status status;
  x_time nap = 1;
  x_thread thread = t;

  oempa("thread id %d\n", thread->id);
  
  while (1) {
    x_assert(critical_status == 0);

    if (create_delete_state == 2) {
      status = x_mutex_lock(create_delete_mutex, x_no_wait);
      if (status != xs_success) {
        oempa("Status = %s\n", x_status2char(status));
        exit(0);
      }
      
      /*
      ** Check ownership...
      */

      if (create_delete_mutex->owner != thread) {
        oempa("Bad owner\n");
        exit(0);
      }

      create_delete_state = 3;
      nap = x_random() % 4 + 1;
    }

    if (create_delete_state == 5) {
    
      /*
      ** Check if we have our priority properly increased to CD_3_PRIO for avoiding
      ** priority inversion.
      */
      
      if (thread->a_prio != CD_3_PRIO + prio_offset + 1) {
        oempa("Priority inversion up not working properly: %d != %d\n", thread->a_prio, CD_3_PRIO + prio_offset + 1);
        exit(0);
      }
      
      status = x_mutex_delete(create_delete_mutex);
      if (status != xs_competing) {
        oempa("Status = %s %d\n", x_status2char(status), status);
        exit(0);
      }
 
      /*
      ** Don't set state, the higher priority thread 3 is trying to get a lock and will
      ** take over here and changed status, so we don't need to change it. This thread 3
      ** will change to state 6...
      */

    }

    if (create_delete_state == 7) {

      /*
      ** Check if we have our priority properly decreased back to CD_2_PRIO that has been
      ** increased to fight priority inversion with thread 3.
      */
      
      if (thread->a_prio != CD_2_PRIO + prio_offset) {
        oempa("Priority inversion down not working properly: %d != %d\n", thread->a_prio, CD_2_PRIO + prio_offset);
        exit(0);
      }

      if (thread->l_owned != NULL) {
        oempa("We shouldn't own an event now...\n");
        exit(0);
      }

      if (create_delete_mutex->Event.l_competing != NULL) {
        oempa("l_competing is not NULL.\n");
        exit(0);
      }

      if (create_delete_mutex->Event.l_owned != NULL) {
        oempa("l_owned is not NULL.\n");
        exit(0);
      }
      
      x_mem_free(create_delete_mutex);
      
      /*
      ** OK, the whole game starts again...
      */
      
      create_delete_state = 1;
      create_delete_check = 0;
      create_delete_counter ++;

      oempa("Mutex fsm ran %d times (sizeof(Event) = %d, sizeof(x_Mutex) = %d).\n", create_delete_counter, sizeof(x_Event), sizeof(x_Mutex));

      nap = x_random() % REPEAT_SLEEP + 1;

    }

    x_thread_sleep(nap);

  }
}

static void cd_3(void * t) {

  x_status status;
  x_thread thread = t;

  oempa("thread id %d\n", thread->id);
  
  while (1) {
    x_assert(critical_status == 0);

    if (create_delete_state == 3) {
      status = x_mutex_lock(create_delete_mutex, x_no_wait);
      if (status != xs_no_instance) {
        oempa("Status = %s\n", x_status2char(status));
        exit(0);
      }

      create_delete_state = 4;

      /*
      ** The following will block and must return x_deleted as status as another
      ** thread will delete the mutex under our feet...
      */
      
      status = x_mutex_lock(create_delete_mutex, x_eternal);
      if (status != xs_deleted) {
        oempa("Status = %s\n", x_status2char(status));
        exit(0);
      }

      /*
      ** See that we are not competing anymore...
      */
      
      if (create_delete_mutex->Event.l_competing == t4) {
        oempa("%d: Thread 4 (%d) should not be competing...\n", create_delete_counter, t4->id);
        exit(0);
      }

      if (create_delete_mutex->Event.n_competing != 0) {
        oempa("n_competing is wrong...\n");
        exit(0);
      }

      if (t4->competing_for != NULL) {
        oempa("Thread 4 should not be competing anymore...\n");
        exit(0);
      }

      create_delete_state = 6;
      
    }

    x_thread_sleep(2);

  }
  
}

static void create_delete(void * t) {

  x_status status;

  while (1) {
    x_assert(critical_status == 0);

    if (create_delete_state == 0) {
      t2 = x_mem_get(sizeof(x_Thread));
      memset(t2, 0xff, sizeof(x_Thread));
      t3 = x_mem_get(sizeof(x_Thread));
      memset(t3, 0xff, sizeof(x_Thread));
      t4 = x_mem_get(sizeof(x_Thread));
      memset(t4, 0xff, sizeof(x_Thread));
      st2 = x_mem_get(MUTT_STACK_SIZE);
      st3 = x_mem_get(MUTT_STACK_SIZE);
      st4 = x_mem_get(MUTT_STACK_SIZE);
      status = x_thread_create(t2, cd_1, t2, st2, MUTT_STACK_SIZE, prio_offset + CD_1_PRIO, TF_START);
      if (status != xs_success) {
        oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      status = x_thread_create(t3, cd_2, t3, st3, MUTT_STACK_SIZE, prio_offset + CD_2_PRIO, TF_START);
      if (status != xs_success) {
        oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      status = x_thread_create(t4, cd_3, t4, st4, MUTT_STACK_SIZE, prio_offset + CD_3_PRIO, TF_START);
      if (status != xs_success) {
        oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      create_delete_state = 1;
    }

    create_delete_check += 1;
    
    if (create_delete_check > 1000) {
      oempa("Stuck at state %d.\n", create_delete_state);
      exit(0);
    }

    x_thread_sleep(REPEAT_SLEEP / 4);

  }
  
}

static x_ubyte * mutex_create_delete_test(x_ubyte * memory) {

  x_status status;

  t1 = x_alloc_static_mem(memory, sizeof(x_Thread));
  st1 = x_alloc_static_mem(memory, MUTT_STACK_SIZE);
  status = x_thread_create(t1, create_delete, t1, st1, MUTT_STACK_SIZE, prio_offset + 4, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }
  else {
    oempa("Mutex create delete master thread %d.\n", t1->id);
  }
    
  return memory;
  
}

// TODO more tests...

x_ubyte * mutex_test(x_ubyte * memory) {

  second = x_seconds2ticks(1);
  
  memory = mutex_create_delete_test(memory);
  
  return memory;
  
}
