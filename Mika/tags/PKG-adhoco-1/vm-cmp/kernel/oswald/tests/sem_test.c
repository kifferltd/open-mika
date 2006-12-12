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
** $Id: sem_test.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <string.h>
#include <tests.h>

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
    x_assert(critical_status == 0);
    if (state == 1) {
      initial = (x_random() % 10) + 1;
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
      x_thread_sleep((x_random() % 10) + 5);
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
    x_assert(critical_status == 0);
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
      wait = x_random() % 10 + 1;
      status = x_sem_get(sem, wait);
      stop = x_time_get();
      if (status != xs_no_instance) {
        oempa("Bad status '%s'\n", x_status2char(status));
        exit(0);
      }
      
      if (stop - start < wait) {
        oempa("Bad wait time behaviour.\n"); 
        exit(0);
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
    x_assert(critical_status == 0);
    if (state == 3) {
      for (i = 1; i < initial; i++) {
        status = x_sem_get(sem, x_no_wait);
        if (status != xs_success) {
          oempa("Bad status '%s'\n", x_status2char(status));
          exit(0);
        }
      }
      if (sem->current != 0) {
        oempa("Semaphore current wrong. %d != 0\n", sem->current);
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

  x_int tries;
  x_status status;
  
  while (irq_depth) {
    oempa("Waiting for kernel initialization...\n");
    x_thread_sleep(1);
  }
  
  while (1) {
    x_assert(critical_status == 0);
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
      tries = 0;
      while (th1->state != xt_ended && tries < 200) {
        tries += 1;
        x_thread_sleep(1);
      }

      while (th2->state != xt_ended && tries < 400) {
        tries += 1;
        x_thread_sleep(1);
      }

      while (th3->state != xt_ended && tries < 600) {
        tries += 1;
        x_thread_sleep(1);
      }

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
    
    x_thread_sleep(10);
    
  }
  
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
      oempa("T1 thread %d tried %d times to get data (around %d)...\n", thread_current->id, tries, b->buffer[b->writepos]);
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
    x_assert(critical_status == 0);
    for (n = 0; n < 10000; n++) {
      put(&buffer, n);
      if (n % 500 == 0) {
        oempa("T1 thread %d: pass %d, current %d\n", thread->id, pass, n);
      }
      x_thread_sleep(1);
    }
    put(&buffer, OVER);
    oempa("T1 thread %d: pass %d done.\n", thread->id, pass);
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
    x_assert(critical_status == 0);
    while (1) {
      x_assert(critical_status == 0);
      d = get(&buffer);
      if (d == OVER) break;
      if (check != d) {
        oempa("Problem d = %d, check = %d.\n", d, check);
        exit(0);
      }
      if (d % 500 == 0) {
        oempa("T1 thread %d: pass %d, running, current is %d.\n", thread->id, pass, d);
      }
      check++;
    }
    oempa("T1 thread %d: pass %d done.\n", thread->id, pass);
    pass++;
    x_thread_sleep(second * 10);
    check = 0;
  }

}

static x_thread th_a;
static x_thread th_b;

static x_ubyte * consumer_producer(x_ubyte * memory) {

  x_ubyte * stack;
  x_status status;

  init(&buffer);

  th_a = x_alloc_static_mem(memory, sizeof(x_Thread));
  th_b = x_alloc_static_mem(memory, sizeof(x_Thread));

  /*
  ** The producer and consumer threads. Note that the producer has a lower 
  ** priority than the consumer. 
  */

  stack = x_alloc_static_mem(memory, SEMT_STACK_SIZE);
  status = x_thread_create(th_a, producer, th_a, stack, SEMT_STACK_SIZE, prio_offset + 5, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }

  stack = x_alloc_static_mem(memory, SEMT_STACK_SIZE);
  status = x_thread_create(th_b, consumer, th_b, stack, SEMT_STACK_SIZE, prio_offset + 4, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }

  return memory;
  
}

x_ubyte * sem_test(x_ubyte * memory) {

  x_status status;

  second = x_seconds2ticks(1);
    
  cd_control = x_alloc_static_mem(memory, sizeof(x_Thread));
  status = x_thread_create(cd_control, cd_sem, cd_control, x_alloc_static_mem(memory, SEMT_STACK_SIZE), SEMT_STACK_SIZE, prio_offset + 5, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }

  memory = consumer_producer(memory);

  return memory;
  
}
