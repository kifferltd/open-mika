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
** $Id: queue_test.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <string.h>
#include <tests.h>

static x_sleep second;

#define QUET_STACK_SIZE ((1024 * 1) + MARGIN)

static x_thread th1;

static x_thread th2;
x_ubyte * st2;

static x_thread th3;
x_ubyte * st3;

static x_thread th4;
x_ubyte * st4;

#define Q_PRIO_2         3
#define Q_PRIO_3         3
#define Q_PRIO_4         2

static x_queue queue;
static x_ubyte * space;
static x_size queue_depth;
static x_int state = 0;
static x_int send_data = 0;
static x_int counter = 0;
static x_int check = 0;

/*

              thread 2               |              thread 3                |               thread 4                  |
              Q_PRIO_2               |              Q_PRIO_3                |               Q_PRIO_4                  |
-------------------------------------+--------------------------------------+-----------------------------------------+
                                     |                                      |                                         |
 When state == 1, set queue_depth to |                                      |                                         |
 random value < 200. Create queue    |                                      |                                         |
 and check the fields. Insert single |                                      |                                         |
 item in the queue and set state to  |                                      |                                         |
 2, then sleep. ----------------------> if state == 2, fill the queue so    |                                         |
                                     |  that a single space is left, then   |                                         |
                                     |  set the state to 3 and sleep for ----> if state == 3, fill the last space,    |
                                     |  10 ticks, then try pushing an item  |  which should succeed, then try to      |
                                     |  with x_eternal ticks.               |  push another, x_no_wait, which should  |
                                     |                 :                    |  fail. Set state to 4 and try to push   |
 if state == 4, try to send item,   <----------------------------------------- with x_eternal.                        |
 which should fail. Check our prio   |  (check that we don't see state 5)   |                   :                     |
 has not changed. Set state to 5 and |                 :                    |                   :                     |
 remove a single item, sleep 1 tick -----------------------------------------> Should succeed AND state should be 5.  |
 and then send a single item, with   |                 :                    |  Set state to 6 and sleep 10 ticks.     |
 no_wait ticks, which should fail.   |                 :                    |                                   |     |
                                     |                 :                    |                                   |     |
 if state == 6, record the time and <---------------------------------------------------------------------------+     |
 try to send item, with random time  |                 :                    |                                         |
 to wait. Should fail and check that |                 :                    |                                         |
 the time elapsed is larger than the |                 :                    |                                         |
 time we randomly selected. Set the  |                 :                    |                                         |
 state to 7 and sleep. ------------------------------------------------------> If state == 7, remove item from queue, |
                                     |                 :                    |  set state to 8 and sleep.              |
                                     |                                      |                  |                      |
                                     | Should succeed. State should be 8,  <-------------------+                      |
                                     | empty the queue and count items;     |                                         |
                                     | check that the order is OK. Set the  |                                         |
                                     | state to 9 and sleep 30 ticks.       |                                         |
                                     |                 |                    |                                         |
 If state is 9, try to receive      <------------------+---------------------> If state is 9, try to receive          |
 and item with x_eternal ticks.      |                 :                    |  and item with x_eternal ticks.         |
                :                    |                 :                    |                  :                      |
                :                    | Send two items on the queue          |                  :                      |
 (If we receive something and the    | and sleep.----------------------------> Receive should succeed and we set      |
 state is 9, something is wrong      |                                      |  the state to 10, then sleep.           |
 with scheduling)                    |                                      |                  |                      |
                :                    |                                      |                  |                      |
 Should succeed and the state must  <----------------------------------------------------------+                      |
 be 10. Set the state to 11 and      |                                      |                                         |
 sleep. ------------------------------> If state == 11, record the time and |                                         |
                                     |  select a random timeout. Try to     |                                         |
                                     |  receive for that timeout and check  |                                         |
                                     |  that it fails and we waited for at  |                                         |
                                     |  least that amount of time.          |                                         |
                                     |  Set state to 12. Sleep. -------------> If state == 12, set state to 13 and    |
                                     |                                      |  try receiving with x_eternal.          |
                                     |                                      |                  |                      |
 If state == 13, delete the queue.  <----------------------------------------------------------+                      |
 Should return xs_competing.         |                                      |                  :                      |
                                     |                                      |  Should return xs_deleted. Set state    |
 If state == 14, return, stop.       | If state == 14, return, stop.        |  to 14 and return, stop.                |
=====================================|======================================|=========================================|

Control thread

  if state == 0, allocate the memory and create the threads 1-3 and set state to 1.
  
  if state == 14, release the memory and set state to 0.

*/

static void queue_2(void * t) {

  x_status status;
  x_time start;
  x_time stop;
  x_time wait;
  x_int read_data;
  
  while (1) {
    x_assert(critical_status == 0);
    if (state == 1) {
      queue_depth = (x_random() % 190) + 2;
      oempa("At count %d, creating queue with depth %d words (sizeof(x_Queue) = %d).\n", counter, queue_depth, sizeof(x_Queue));
      space = x_mem_get(queue_depth * sizeof(x_word));
      memset(space, 0xff, queue_depth * sizeof(x_word));
      
      queue = x_mem_get(sizeof(x_Queue));
      memset(queue, 0xff, sizeof(x_Queue));

      status = x_queue_create(queue, space, queue_depth);
      if (status != xs_success) {
        oempa("bad state '%s'\n", x_status2char(status));
        exit(0);
      }

      send_data = 0;
      status = x_queue_send(queue, (void *)send_data, x_no_wait);
      if (status != xs_success) {
        oempa("bad state '%s'\n", x_status2char(status));
        exit(0);
      }
      send_data += 1;
      state = 2;

      x_thread_sleep(10);
      
    }

    if (state == 4) {
      status = x_queue_send(queue, (void *)send_data, x_no_wait);
      if (status != xs_no_instance) {
        oempa("bad state '%s'\n", x_status2char(status));
        exit(0);
      }
      
      if (th2->c_prio != Q_PRIO_2 + prio_offset) {
        oempa("Bad priority %d, should be %d.\n", th2->c_prio, Q_PRIO_2 + prio_offset);
        exit(0);
      }
      
      state = 5;
      
      status = x_queue_receive(queue, (void **)&read_data, x_no_wait);
      if (status != xs_success) {
        oempa("bad state '%s'\n", x_status2char(status));
        exit(0);
      }
      
      if (read_data != 0) {
        oempa("Bad data %d != 0\n", read_data);
        exit(0);
      }
      
      x_thread_sleep(1);
      
      status = x_queue_send(queue, (void *)send_data, x_no_wait);
      if (status != xs_no_instance) {
        oempa("bad state '%s'\n", x_status2char(status));
        exit(0);
      }
      
    }
    
    if (state == 6) {
      start = x_time_get();
      wait = x_random() % 10 + 1;
      status = x_queue_send(queue, (void *)send_data, wait);
      stop = x_time_get();
      if (status != xs_no_instance) {
        oempa("bad state '%s'\n", x_status2char(status));
        exit(0);
      }
      
      if (stop - start < wait) {
        oempa("Bad wait time behaviour.\n");
        exit(0);
      }
      
      state = 7;
      
      x_thread_sleep(1);
    }
    
    if (state == 9) {
      status = x_queue_receive(queue, (void **)&read_data, x_eternal);
      
      /*
      ** Check that state isn't 9, should be 10.
      */
      
      if (state == 9) {
        oempa("Bad machine state.\n");
        exit(0);
      }
      if (status != xs_success) {
        oempa("bad state '%s'\n", x_status2char(status));
        exit(0);
      }
      if (state == 9) {
        oempa("Bad machine state %d.\n", state);
        exit(0);
      }
      state = 11;
      x_thread_sleep(1);
    }
    
    if (state == 13) {
      status = x_queue_delete(queue);
      if (status != xs_competing) {
        oempa("Bad state '%s' %d\n", x_status2char(status), status);
        exit(0);
      }
    }

    if (state == 14) {
      return;
    }
    
    x_thread_sleep(5);
  }
  
}

static void queue_3(void * t) {

  x_status status;
  x_size i;
  x_size read_data;
  x_time start;
  x_time stop;
  x_time wait;
  
  while (1) {
    x_assert(critical_status == 0);
    if (state == 5) {
      oempa("Bad machine state for this thread.\n");
      exit(0);
    }
    
    if (state == 2) {
      for (i = send_data; i < queue_depth - 1; i++) {
        status = x_queue_send(queue, (void *)send_data, x_no_wait);
        if (status != xs_success) {
          oempa("Could not fill queue at point %d, depth = %d.\n", i, queue_depth);
          exit(0);
        }
        send_data += 1;
      }
      
      state = 3;
      
      x_thread_sleep(10);

      status = x_queue_send(queue, (void *)send_data, x_eternal);
      if (status != xs_success) {
        oempa("bad state '%s'\n", x_status2char(status));
        exit(0);
      }
      
      send_data += 1;

      /*
      ** The correct state is only valid when running hard priority tests, when we run soft
      ** priority tests, we skip the test.
      */
            
      if (state != 8 && prio_offset == 0) {
        oempa("Bad FSM state %d.\n", state);
        exit(0);
      }
            
      for (i = 0; i < queue_depth; i++) {
        status = x_queue_receive(queue, (void **)&read_data, x_no_wait);
        if (status != xs_success) {
          oempa("bad state '%s' at %d, depth = %d.\n", x_status2char(status), i, queue_depth);
          exit(0);
        }
        
        /*
        ** Because of the flow through the finite state machine. The checking of the numbers is a bit
        ** strange...
        */
        
        if (i + 1 < queue_depth) {
          if (read_data != i + 2) {
            oempa("Bad data at %d: %d != %d, for queue depth %d.\n", i, read_data, i + 2, queue_depth);
            exit(0);
          }
        }
        else {
          if (read_data != queue_depth) {
            oempa("Bad data at %d: %d != %d, for queue depth %d.\n", i, read_data, queue_depth, queue_depth);
            exit(0);
          }
        }
      }
      
      state = 9;
      
      x_thread_sleep(30);

      status = x_queue_send(queue, (void *)send_data, x_no_wait);
      if (status != xs_success) {
        oempa("bad state '%s'\n", x_status2char(status));
        exit(0);
      }
      
      send_data += 1;

      status = x_queue_send(queue, (void *)send_data, x_no_wait);
      if (status != xs_success) {
        oempa("bad state '%s'\n", x_status2char(status));
        exit(0);
      }
      
      send_data += 1;

    }
    
    if (state == 11) {
      start = x_time_get();
      wait = x_random() % 10 + 10;
      status = x_queue_receive(queue, (void **)&read_data, wait);
      stop = x_time_get();
      if (status != xs_no_instance) {
        oempa("bad state '%s'\n", x_status2char(status));
        exit(0);
      }

      if (stop - start < wait) {
        oempa("Bad wait time behaviour.\n");
        exit(0);
      }

      state = 12;

    }
    
    if (state == 14) {
      return;
    }
    
    x_thread_sleep(2);

  }
  
}

static void queue_4(void * t) {

  x_int read_data;
  x_status status;
  
  while (1) {
    x_assert(critical_status == 0);
    if (state == 3) {
      status = x_queue_send(queue, (void *)send_data, x_no_wait);
      if (status != xs_success) {
        oempa("bad state '%s'\n", x_status2char(status));
        exit(0);
      }

      send_data += 1;

      status = x_queue_send(queue, (void *)send_data, x_no_wait);
      if (status != xs_no_instance) {
        oempa("bad state '%s'\n", x_status2char(status));
        exit(0);
      }

      state = 4;

      status = x_queue_send(queue, (void *)send_data, x_eternal);
      if (status != xs_success) {
        oempa("bad state '%s'\n", x_status2char(status));
        exit(0);
      }
      
      if (state != 5) {
        oempa("Bad machine state for this thread %d.\n", state);
        exit(0);
      }
      
      state = 6;
      x_thread_sleep(10);
      
    }
    
    if (state == 7) {
      status = x_queue_receive(queue, (void **)&read_data, x_no_wait);
      if (status != xs_success) {
        oempa("bad state '%s'\n", x_status2char(status));
        exit(0);
      }
      if (read_data != 1) {
        oempa("Bad data %d != 0\n", read_data);
        exit(0);
      }
      state = 8;
      x_thread_sleep(10);
    }
    
    if (state == 9) {
      status = x_queue_receive(queue, (void **)&read_data, x_eternal);
      if (status != xs_success) {
        oempa("bad state '%s'\n", x_status2char(status));
        exit(0);
      }

      state = 10;

      x_thread_sleep(10);
    }
    
    if (state == 12) {
      state = 13;
      status = x_queue_receive(queue, (void **)&read_data, x_eternal);
      if (status != xs_deleted) {
        oempa("bad state '%s'\n", x_status2char(status));
        exit(0);
      }
      state = 14;
      return;
    }
    
    x_thread_sleep(10);
  }

}

static void control(void * t) {

  x_int tries;

  while (irq_depth) {
    continue;
  }
      
  while (1) {
    x_assert(critical_status == 0);

    check += 1;

    if (state == 0) {
      th2 = x_mem_get(sizeof(x_Thread));
      memset(th2, 0xff, sizeof(x_Thread));
      st2 = x_mem_get(QUET_STACK_SIZE);
      x_thread_create(th2, queue_2, th2, st2, QUET_STACK_SIZE, prio_offset + Q_PRIO_2, TF_START);

      th3 = x_mem_get(sizeof(x_Thread));
      memset(th3, 0xff, sizeof(x_Thread));
      st3 = x_mem_get(QUET_STACK_SIZE);
      x_thread_create(th3, queue_3, th3, st3, QUET_STACK_SIZE, prio_offset + Q_PRIO_3, TF_START);

      th4 = x_mem_get(sizeof(x_Thread));
      memset(th4, 0xff, sizeof(x_Thread));
      st4 = x_mem_get(QUET_STACK_SIZE);
      x_thread_create(th4, queue_4, th4, st4, QUET_STACK_SIZE, prio_offset + Q_PRIO_4, TF_START);

      state = 1;
    }

    if (state == 14) {
      tries = 0;
      while (th2->state != xt_ended && tries < 200) {
        x_thread_sleep(1);
        tries += 1;
      }

      while (th3->state != xt_ended && tries < 400) {
        x_thread_sleep(1);
        tries += 1;
      }

      while (th4->state != xt_ended && tries < 600) {
        x_thread_sleep(1);
        tries += 1;
      }

      if (tries >= 200) {
        oempa("Couldn't kill threads...\n");
        exit(0);
      }
      
      x_mem_free(queue);
      x_mem_free(space);

      x_mem_free(th2);
      x_mem_free(th3);
      x_mem_free(th4);

      x_mem_free(st2);
      x_mem_free(st3);
      x_mem_free(st4);

      counter += 1;
      state = 0;
      check = 0;
      
      x_thread_sleep(second);

    }

    if (check > 60) {
      oempa("Queue state machine stuck at state %d.\n", state);
      exit(0);
    }
    
    x_thread_sleep(second);

  }
  
}

x_ubyte * queue_test(x_ubyte * memory) {

  second = x_seconds2ticks(1);
  
  th1 = x_alloc_static_mem(memory, sizeof(x_Thread));
  x_thread_create(th1, control, th1, x_alloc_static_mem(memory, QUET_STACK_SIZE), QUET_STACK_SIZE, prio_offset + 4, TF_START);
  
  return memory;
  
}
