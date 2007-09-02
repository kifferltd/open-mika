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
** $Id: block_test.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
**
** Block test routines.
*/

#include <string.h>
#include <tests.h>

static x_sleep second;

#define BLOT_STACK_SIZE ((1024 * 1) + MARGIN)

/*
** A block tester thread structure and the list structure we use to keep blocks on a list.
*/

typedef struct b_Block * b_block;

typedef struct b_Block {
  b_block next;
  b_block previous;
  x_ubyte data[0];
} b_Block;

typedef struct b_Thread * b_thread;

typedef struct b_Thread {
  x_thread thread;
  x_ubyte * stack;
  x_int state;
  x_size in_use;
  x_size no_wait_allocs;
  x_size wait_allocs;
  x_size eternal_allocs;
  x_size releases;
  x_size allocs;
  b_Block list;
} b_Thread;

static b_Thread * b_Threads;    /* The array with info for each tester thread. */
static x_size num_threads;      /* The number of tester threads for a loop. */
static x_size num_eternal;      /* The number of tester threads waiting with 'x_eternal', to defy deadlocks... */

static x_block pool;            /* The block pool that is being scrutinzed. */
static x_ubyte *space;          /* The data space area of the blocks. */
static x_size bsize;            /* Size of a single block. */
static x_size ssize;            /* Size of the complete space area. */
static x_size max;              /* The number of blocks that are available in the block pool. */
static x_int loops = 0;         /* The number of test loops. */

static volatile x_int state = 0;         /* The state of the testing. */

static void dump_threads(void) {

  b_thread bt;
  x_size i;
  x_int in_use = 0;
  x_int total = 0;
  
  x_preemption_disable;
  oempa("+-------+-------+--------+---------+------+---------+--------+----------+\n");
  oempa("|   id  | state | in use | no wait | wait | eternal | allocs | releases |\n");
  oempa("+-------+-------+--------+---------+------+---------+--------+----------+\n");
  for (i = 0; i < num_threads; i++) {
    bt = &b_Threads[i];
    in_use += bt->in_use;
    total += bt->allocs;
    total += bt->releases;
    oempa("| %5d | %3d   |    %3d |   %5d |%5d |   %5d |  %5d |    %5d |\n", 
      bt->thread->id, bt->state, bt->in_use, bt->no_wait_allocs, bt->wait_allocs, bt->eternal_allocs, bt->allocs, bt->releases);
  }
  oempa("+-------+-------+--------+---------+------+---------+--------+----------+\n");
  oempa("Loop %d, %d blocks in use of max %d blocks, in %d threads, %d operations.\n", loops, in_use, max, num_threads, total);
  x_preemption_enable;

}

static void link_block(b_thread t, b_block b) {
  
  x_size j;

  j = bsize;
  j -= sizeof(void *);
  j -= sizeof(void *);

  memset(b->data, (x_ubyte)t->thread->id, j);
  
  t->in_use += 1;
  t->allocs += 1;
  x_list_insert(&t->list, b);
  
}

static void unlink_block(b_thread t, b_block b) {

  x_int i;
  x_int j;

  j = (volatile int)bsize;
  j -= sizeof(void *);
  j -= sizeof(void *);

  for (i = 0; i < j; i++) {
    if (b->data[i] != (x_ubyte)t->thread->id) {
      oempa("Wrong...\n");
      exit(0);
    }
  }

  x_list_remove(b);
  x_block_release(b);
  t->in_use -= 1;
  t->releases += 1;
  
}

static void b_tester(void * t) {

  b_thread bt = t;
  x_status status;
  x_time start;
  x_time stop;
  x_time wait;
  x_time nap;
  b_block block;
  x_boolean allocate;
  x_boolean skip_no_wait;
  x_boolean skip_wait;
  
  while (1) {
    if (state == 1) {
      status = xs_no_instance;
    
      /*
      ** See if we should allocate or release a block...
      */
      
      allocate = x_random() & 0x00000007;
      allocate = (allocate > 0) ? 1 : 0; // We favor allocations over releases...
      skip_no_wait = x_random() & 0x00000001;
      if (allocate) {
        block = NULL;
        if (! skip_no_wait) {
          status = x_block_allocate(pool, (void *)&block, x_no_wait);
        }
        if (skip_no_wait || status != xs_success) {
          skip_wait = x_random() & 0x00000001;
          if (! skip_wait) {
            start = x_time_get();
            wait = (x_random() % 3) + 1;
            status = x_block_allocate(pool, (void *)&block, wait);
            stop = x_time_get();
          }
          if (skip_wait || status != xs_success) {
            // check time also...
            num_eternal += 1;
            if (num_eternal < num_threads) {
              status = x_block_allocate(pool, (void *)&block, x_eternal);
              if (status == xs_success) {
                link_block(bt, block);
                bt->eternal_allocs += 1;
              }
            }
            else {
              status = xs_success; // Force success when we were not allowed to go x_eternal...
            }
            num_eternal -= 1;
          }
          else {
            link_block(bt, block);
            bt->wait_allocs += 1;
          }
        }
        else {
          link_block(bt, block);
          bt->no_wait_allocs += 1;
        }
        
        if (status != xs_success) {
          if (state == 2) {
            if (status != xs_deleted) {
              oempa("Bad status %s\n", x_status2char(status));
              exit(0);
            }
          }
        }
      }
      else {

        /*
        ** Release a block if we have any...
        */

        if (bt->in_use) {
          unlink_block(bt, bt->list.next);
        }
      }

      nap = x_random() % 4 + 4;
      x_thread_sleep(nap);

    }
    else if (state == 2) {
      while (bt->in_use) {
        unlink_block(bt, bt->list.next);
      }
      bt->state = 2;
      return;
    }

  } 
  
}

static void controller(void * t) {

  b_thread bt;
  x_size i;
  x_int counter = 0;
  x_int limit = 40;
  x_status status;
  x_boolean all_done;
  
  while (1) {
    counter += 1;
    
    if (state == 0) {
      limit = x_random() % 60 + 5;
      num_threads = x_random() % 20 + 2;
      num_eternal = 0;
      
      b_Threads = x_mem_get(num_threads * sizeof(b_Thread));
      for (i = 0; i < num_threads; i++) {
        bt = &b_Threads[i];
        memset(bt, 0x00, sizeof(b_Thread));
        bt->thread = x_mem_get(sizeof(x_Thread));
        bt->stack = x_mem_get(BLOT_STACK_SIZE);
        x_list_init(&bt->list);
        status = x_thread_create(bt->thread, b_tester, bt, bt->stack, BLOT_STACK_SIZE, prio_offset + 3, TF_START);
        if (status != xs_success) {
          oempa("%s: Status = '%s'\n", __FILE__, x_status2char(status));
          exit(0);
        }
      }
      
      ssize = (x_random() % (1024 * 4)) + 512;
      space = x_mem_get(ssize);
      memset(space, 0xff, ssize);
      bsize = (x_random() % 256) + sizeof(void *) + sizeof(void *) + 1;
      pool = x_mem_get(sizeof(x_Block));
      memset(pool, 0xff, sizeof(x_Block));
      x_block_create(pool, bsize, space, ssize);
      max = pool->bolls_left;
      oempa("At loop %d, limit %d, %d threads, blocksize %d bytes, space %d bytes, %d blocks\n", loops, limit, num_threads, bsize, ssize, pool->bolls_left);
      state = 1;
    }

    if (counter == limit) {
      oempa("Starting delete, number of threads competing = %d\n", pool->Event.n_competing);
      state = 2;
      status = x_block_delete(pool);
      oempa("Status of pool delete = '%s', %d competing\n", x_status2char(status), pool->Event.n_competing);
      if (! (status == xs_success || status == xs_competing)) {
        oempa("Bad status %s\n", x_status2char(status));
        dump_threads();
        exit(0);
      }
      status = x_event_join(pool, 10);
      oempa("Status of event join = '%s'\n", x_status2char(status));
    }
    
    if (state == 2) {
    
      /*
      ** Loop untill all test threads have acknowledged that they have released all the
      ** memory...
      */
      
      all_done = false;
      while (! all_done) {
        dump_threads();
        all_done = true;
        for (i = 0; i < num_threads; i++) {
          bt = &b_Threads[i];
          if (bt->state != 2) {
            all_done = false;
          }
          if (bt->thread->state != xt_ended) {
            all_done = false;
          }
        }
        x_thread_sleep(10);
      }

      /*
      ** Now clean up...
      */

      for (i = 0; i < num_threads; i++) {
        bt = &b_Threads[i];
        x_mem_free(bt->thread);
        x_mem_free(bt->stack);
      }

      x_mem_free(pool);
      x_mem_free(space);
      x_mem_free(b_Threads);
      
      /*
      ** Sleep and restart...
      */
      
      counter = 0;
      state = 0;
      loops += 1;
      x_thread_sleep(second * 30);
      
    }
    
    x_thread_sleep(20);

  }

}

static x_thread control;

x_ubyte * block_test(x_ubyte * memory) {

  x_status status;

  second = x_seconds2ticks(1);
  
  control = x_alloc_static_mem(memory, sizeof(x_Thread));
  status = x_thread_create(control, controller, control, x_alloc_static_mem(memory, BLOT_STACK_SIZE), BLOT_STACK_SIZE, prio_offset + 2, TF_START);
  if (status != xs_success) {
    oempa("%s: Status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }

  return memory;
  
}
