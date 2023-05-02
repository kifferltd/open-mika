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

#include "tests.h"

static x_sleep second;

#define SEMT_STACK_SIZE ((1024 * 1) + MARGIN)

/*
              thread 1               |              thread 2                |               thread 3                  | 
-------------------------------------+--------------------------------------+-----------------------------------------+ 
 If state == 1, create random value  |                                      |                                         |              
 for initial, allocate and create    |                                      |                                         |              
 sem. Set state to 2 and sleep. ------> If state == 2, do sem_get, should   |                                         |              
                                     |  succeed, set state to 3 and sleep. --> If state == 3, sem_get full capacity   |              
                                     |                                      |  of semaphore, set state to 4 and       |              
                                     |                                      |  sleep.                                 |              
                                     |                                      |    |                                    |              
                                     |  If state == 4, sem_put once, check <-----+                                    |              
                                     |  the count, set state to 5 and       |                                         |              
                                     |  sleep.                              |                                         |              
                                     |    |                                 |                                         |              
 If state == 5, sem_get once, this  <-----+                                 |                                         |              
 should succeed; set state to 6 and  |                                      |                                         |              
 sleep. ------------------------------> If state == 6, record the time,     |                                         |              
                                     |  try sem_get for random time; should |                                         |              
                                     |  fail. Check that we waited long     |                                         |              
                                     |  enough, set state to 7 and sleep. ---> If state == 7, sem_put all but 1, set  |              
                                     |                                      |  state to 8 and sleep.                  |              
                                     |                                      |                   |                     |              
 Put last element and check intial  <-----------------------------------------------------------+                     |              
 and current are same. Set state to  |                                      |                                         |              
 9 and sleep. ------------------------> If state == 9, try sem_put, should  |                                         |              
                                     |  succeed and current should be       |                                         |              
                                     |  larger than initial. Set state to   |                                         |              
                                     |  10 and sleep. -----------------------> If state == 10, sem_get all, set state |              
                                     |                                      |  to 11 and sem_get(eternal)             |              
                                     |                                      |                   |                     |              
 If state == 11, sleep random time  <-----------------------------------------------------------+                     |              
 and sem_put once.                   |                                      |                   :                     |              
              +--------------------------------------------------------------> Should succeed, set state to 12 and    |              
                                     |                                      |  try sem_get(eternal)                   |              
                                     |                                      |                   |                     |              
 If state == 12, delete semaphore,  <-----------------------------------------------------------+                     |              
 should return xs_competing.         |                                      |                   :                     |              
              +--------------------------------------------------------------> Should return xs_deleted, set state to |              
 If state == 13, return, stop.       | If state == 13, return, stop.        |  13 and return, stop.                   |              
=====================================|======================================|=========================================|              

*/

static x_thread cd_control;

static x_thread th1;
static x_ubyte * st1;

static x_thread th2;
static x_ubyte * st2;

static x_thread th3;
static x_ubyte * st3;

static x_int state;

static x_sem sem;
static x_size initial;

static x_int sem_counter = 0;
static x_int sem_check = 0;

void sem_1(void * t) {

  x_status status;
  
  while (1) {
    if (state == 1) {
      initial = (random() % 10) + 1;
      sem = x_mem_get(sizeof(x_Sem));
      memset(sem, 0xff, sizeof(x_Sem));
      status = x_sem_create(sem, initial);
      if (status != xs_success) {
        oempa("Bad status '%s'\n", x_status2char(status));
        exit(0);
      }
      oempa("At count %d, semaphore created with initial %d.\n", sem_counter, initial);
      state = 2;
    }
    
    if (state == 5) {
      status = x_sem_get(sem, x_no_wait);
      if (status != xs_success) {
        oempa("Bad status '%s'\n", x_status2char(status));
        exit(0);
      }
      state = 6;
    }
    
    if (state == 8) {
      status = x_sem_put(sem);
      if (status != xs_success) {
        oempa("Bad status '%s'\n", x_status2char(status));
        exit(0);
      }
      state = 9;
    }

    if (state == 11) {
      x_thread_sleep((random() % 10) + 5);
      status = x_sem_put(sem);
      if (status != xs_success) {
        oempa("Bad status '%s'\n", x_status2char(status));
        exit(0);
      }
    }
    
    if (state == 12) {
      status = x_sem_delete(sem);
      if (status != xs_competing) {
        oempa("Bad status '%s'\n", x_status2char(status));
        exit(0);
      }
    }
    
    if (state == 13) {
      return;
    }
    
    x_thread_sleep(10);
  }
  
}

void sem_2(void * t) {

  x_status status;
  x_time start;
  x_time stop;
  x_time wait;
  
  while (1) {
    if (state == 2) {
      status = x_sem_get(sem, x_no_wait);
      if (status != xs_success) {
        oempa("Bad status '%s'\n", x_status2char(status));
        exit(0);
      }
      state = 3;
    }
    
    if (state == 4) {
      status = x_sem_put(sem);
      if (status != xs_success) {
        oempa("Bad status '%s'\n", x_status2char(status));
        exit(0);
      }
      state = 5;
    }
    
    if (state == 6) {
      start = x_time_get();
      wait = random() % 10 + 1;
      status = x_sem_get(sem, wait);
      stop = x_time_get();
      if (status != xs_no_instance) {
        oempa("Bad status '%s'\n", x_status2char(status));
        exit(0);
      }
      if (stop - start < wait) {
        oempa("Bad wait time behaviour (%d is less than %d).\n", stop-start, wait); 
        //exit(0);
      }
      
      state = 7;
    }
    
    if (state == 9) {
      status = x_sem_put(sem);
      if (status != xs_success) {
        oempa("Bad status '%s'\n", x_status2char(status));
        exit(0);
      }
      state = 10;
    }
    
    if (state == 13) {
      return;
    }
    
    x_thread_sleep(5);
  }
  
}

void sem_3(void * t) {

  x_status status;
  x_size i;
  
  while (1) {
    //x_assert(critical_status == 0);
    if (state == 3) {
      for (i = 1; i < initial; i++) {
        status = x_sem_get(sem, x_no_wait);
        if (status != xs_success) {
          oempa("Bad status '%s'\n", x_status2char(status));
          exit(0);
        }
      }
      if (sem->count != 0) {
        oempa("Semaphore current wrong. %d != 0\n", sem->count);
        exit(0);
      }
      state = 4;
    }
    
    if (state == 7) {
      for (i = 1; i < initial; i++) {
        status = x_sem_put(sem);
        if (status != xs_success) {
          oempa("Bad status '%s'\n", x_status2char(status));
          exit(0);
        }
      }
      state = 8;
    }
    
    if (state == 10) {
      for (i = 0; i < initial; i++) {
        status = x_sem_get(sem, x_no_wait);
        if (status != xs_success) {
          oempa("Bad status '%s'\n", x_status2char(status));
          exit(0);
        }
      }
      state = 11;
      status = x_sem_get(sem, x_eternal);
      if (status != xs_success) {
        oempa("Bad status '%s'\n", x_status2char(status));
        exit(0);
      }
      state = 12;
      status = x_sem_get(sem, x_eternal);
      if (status != xs_deleted) {
        oempa("%d Bad status '%s'\n", state, x_status2char(status));
        exit(0);
      }
      state = 13;
      return;
    }
    
    x_thread_sleep(10);
  }
  
}

void cd_sem(void * t) {

  //x_int tries;
  x_status status;
  
  while (1) {
    sem_check += 1;
    if (state == 0) {
      th1 = x_mem_get(sizeof(x_Thread));
      memset(th1, 0xff, sizeof(x_Thread));
      st1 = x_mem_get(SEMT_STACK_SIZE);
      status = x_thread_create(th1, sem_1, th1, st1, SEMT_STACK_SIZE, prio_offset + 4, TF_START);
      if (status != xs_success) {
        oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }

      th2 = x_mem_get(sizeof(x_Thread));
      memset(th2, 0xff, sizeof(x_Thread));
      st2 = x_mem_get(SEMT_STACK_SIZE);
      status = x_thread_create(th2, sem_2, th2, st2, SEMT_STACK_SIZE, prio_offset + 4, TF_START);
      if (status != xs_success) {
        oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }

      th3 = x_mem_get(sizeof(x_Thread));
      memset(th3, 0xff, sizeof(x_Thread));
      st3 = x_mem_get(SEMT_STACK_SIZE);
      status = x_thread_create(th3, sem_3, th3, st3, SEMT_STACK_SIZE, prio_offset + 4, TF_START);
      if (status != xs_success) {
        oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }

      state = 1;
      
    }
    
    if (state == 13) {
      x_thread_delete(th1);
      x_thread_delete(th2);
      x_thread_delete(th3);

      x_mem_free(sem);

      x_mem_free(th1);
      x_mem_free(st1);

      x_mem_free(th2);
      x_mem_free(st2);

      x_mem_free(th3);
      x_mem_free(st3);
      
      sem_counter += 1;
      sem_check = 0;
      state = 0;
      x_thread_sleep(second);
    }

    if (sem_check > 30) {
      oempa("Semaphore state machine stuck at %d.\n", state);
      exit(0);
    }
    
    oempa("cd_sem sleeping\n");
    x_thread_sleep(10);
    
  }
  oempa("cd_sem completed\n");
  
}

/* 
** The classic producer-consumer example, implemented with semaphores.
*/

#define BUFFER_SIZE 16

/* 
** Circular buffer of integers. 
*/

struct prodcons {
  int buffer[BUFFER_SIZE];      /* the actual data */
  int readpos, writepos;        /* positions for reading and writing */
  x_Sem sem_read;               /* number of elements available for reading */
  x_Sem sem_write;              /* number of locations available for writing */
};

/*
** Initialize a buffer
*/

static void init(struct prodcons * b) {

  x_sem_create(&b->sem_write, BUFFER_SIZE - 1);
  x_sem_create(&b->sem_read,  0);
  b->readpos = 0;
  b->writepos = 0;

}

/*
** Store an integer in the buffer
*/

static void put(struct prodcons * b, int data) {

  x_status status;

  /* 
  ** Wait until buffer is not full 
  */

  status = x_sem_get(&b->sem_write, 100);
  if (status == xs_success) {

    /* 
    ** Write the data and advance write pointer 
    */

    b->buffer[b->writepos] = data;
    b->writepos++;
    if (b->writepos >= BUFFER_SIZE) b->writepos = 0;

    /* 
    ** Signal that the buffer contains one more element for reading 
    */

    status = x_sem_put(&b->sem_read);
    if (status != xs_success) {
      oempa("T1 Problem: %s\n", x_status2char(status));
    }
  }
}

/* 
** Read and remove an integer from the buffer
*/

static int get(struct prodcons * b) {

  x_status status = 100;
  int data = 0;
  int tries = 0;

  /* 
  ** Wait until buffer is not empty. Status is preset to an unknown
  ** number.
  */

  while (status != xs_success) {
    status = x_sem_get(&b->sem_read, 10);
    if (status == xs_success) {

      /* 
      ** Read the data and advance read pointer 
      */

      data = b->buffer[b->readpos];
      b->readpos++;
      if (b->readpos >= BUFFER_SIZE) b->readpos = 0;

      /* 
      ** Signal that the buffer has now one more location for writing 
      */

      status = x_sem_put(&b->sem_write);
      if (status != xs_success) {
        oempa("%d: T1 Problem %s\n", __LINE__, x_status2char(status));
        exit(0);
      }
    }

    tries += 1;

    if (tries % 100 == 0) {
      oempa("T1 thread %p tried %d times to get data (around %d)...\n", x_thread_current(), tries, b->buffer[b->writepos]);
    }

  }
  
  return data;

}

#define OVER (-1)

static struct prodcons buffer;

static void producer(void * t) {

  int n;
  int pass = 0;
  x_thread thread = t;

  while (1) {
    for (n = 0; n < 10000; n++) {
      put(&buffer, n);
      if (n % 500 == 0) {
        oempa("T1 thread %p: pass %d, current %d\n", thread, pass, n);
      }
      x_thread_sleep(1);
    }
    put(&buffer, OVER);
    oempa("T1 thread %p: pass %d done.\n", thread, pass);
    pass++;
    x_thread_sleep(second * 10);
  }

}

static void consumer(void * t) {

  int d;
  int pass = 0;
  int check = 0;
  x_thread thread = t;
  
  while (1) {
    while (1) {
      d = get(&buffer);
      if (d == OVER) break;
      if (check != d) {
        oempa("Problem d = %d, check = %d.\n", d, check);
        exit(0);
      }
      if (d % 500 == 0) {
        oempa("T1 thread %p: pass %d, running, current is %d.\n", thread, pass, d);
      }
      check++;
    }
    oempa("T1 thread %p: pass %d done.\n", thread, pass);
    pass++;
    x_thread_sleep(second * 10);
    check = 0;
  }

}

static x_thread th_a;
static x_thread th_b;

static void consumer_producer() {

  x_ubyte * stack;
  x_status status;

  init(&buffer);

  th_a = x_alloc_mem(sizeof(x_Thread));
  th_b = x_alloc_mem(sizeof(x_Thread));

  /*
  ** The producer and consumer threads. Note that the producer has a lower 
  ** priority than the consumer. 
  */

  stack = x_alloc_mem(SEMT_STACK_SIZE);
  status = x_thread_create(th_a, producer, th_a, stack, SEMT_STACK_SIZE, prio_offset + 5, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }

  stack = x_alloc_mem(SEMT_STACK_SIZE);
  status = x_thread_create(th_b, consumer, th_b, stack, SEMT_STACK_SIZE, prio_offset + 4, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }

  return;
  
}

void sem_test() {

  x_status status;

  second = x_seconds2ticks(1);
    
  cd_control = x_alloc_mem(sizeof(x_Thread));
  status = x_thread_create(cd_control, cd_sem, cd_control, x_alloc_mem(SEMT_STACK_SIZE), SEMT_STACK_SIZE, prio_offset + 5, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }

  return;
  
}
