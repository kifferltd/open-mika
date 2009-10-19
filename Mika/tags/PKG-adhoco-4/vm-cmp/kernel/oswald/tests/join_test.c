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
** $Id: join_test.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <string.h>
#include <tests.h>
#include <oswald.h>

static x_sleep second;

#define JOIN_STACK_SIZE ((1024 * 2) + MARGIN)

/*
** Structures for testing cascaded joins; i.e. thread joining a thread that has joined
** another thread, that has joined another thread, ...
*/

typedef struct cc_Arg * cc_arg;

typedef struct cc_Arg {
  x_int     number;
  x_entry   entry;
  x_int     sleep;
  x_thread  parent;
  x_thread  child;
  x_ubyte * stack;
  cc_arg    next;
  cc_arg    previous;
} cc_Arg;

/*
** This is the final thread entry. It doesn't join itself on yet another
** thread, but returns after some sleeping time so that the cascade can
** unwind from the joins...
*/

static void cc_final_entry(void * a) {

  cc_arg arg = a;

  oempa("Number %d is final; now going to sleep for %d ticks.\n", arg->number, arg->sleep);

  x_thread_sleep(arg->sleep);

  oempa("Number %d is final; done sleeping; returning...\n", arg->number);

}

/*
** This is the intermediate threads entry; it creates another thread on which it will
** join after the sleeping time in the cascaded argument structure.
*/

static void cc_intermediate_entry(void * a) {

  cc_arg arg = a;
  x_status status;
  void * result;
  x_int tries;

  arg->parent = thread_current;  
  arg->child = x_mem_get(sizeof(x_Thread));
  arg->stack = x_mem_get(JOIN_STACK_SIZE);
  status = x_thread_create(arg->child, arg->entry, arg->next, arg->stack, JOIN_STACK_SIZE, prio_offset + 2, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }

  oempa("Number %d, has created thread %d; now going to sleep for %d ticks.\n", arg->parent->id, arg->child->id, arg->sleep);
  
  tries = 0;
  do {
    x_thread_sleep(arg->sleep);
    status = x_thread_join(arg->child, &result, x_eternal);
  } while (status != xs_success && tries++ < 2);
  
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }  

  oempa("Number %d, joined thread %d.\n", arg->parent->id, arg->child->id);

  status = x_thread_delete(arg->child);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }  
  x_mem_free(arg->child);
  x_mem_free(arg->stack);

  oempa("Number %d, released thread %d; returning...\n", arg->number, arg->child->id);
  
}

static cc_Arg cc_Args[] = {
  { 0, cc_intermediate_entry,  70, NULL, NULL, NULL, NULL, NULL },
  { 0, cc_intermediate_entry,  90, NULL, NULL, NULL, NULL, NULL },
  { 0, cc_intermediate_entry, 110, NULL, NULL, NULL, NULL, NULL },
  { 0, cc_intermediate_entry, 130, NULL, NULL, NULL, NULL, NULL },
  { 0, cc_intermediate_entry, 150, NULL, NULL, NULL, NULL, NULL },
  { 0, cc_intermediate_entry, 170, NULL, NULL, NULL, NULL, NULL },
  { 0, cc_final_entry,        190, NULL, NULL, NULL, NULL, NULL },
  { 0, NULL,                  210, NULL, NULL, NULL, NULL, NULL },
};

static void cascade_entry(void * arg) {

  x_thread first;
  x_ubyte * stack;
  x_status status;
  x_int i;
  x_int n = sizeof(cc_Args) / sizeof(cc_Arg);
  void * result;
  x_int pass = 0;
  
  for (i = 0; i < n; i++) {
    cc_Args[i].number = i;
    if (i + 1 < n) {
      cc_Args[i].next = & cc_Args[i + 1];
    }
    if (i > 0) {
      cc_Args[i].previous = & cc_Args[i - 1];
    }
  }

  while (1) {
    x_assert(critical_status == 0);
    pass += 1;
    first = x_mem_get(sizeof(x_Thread));
    stack = x_mem_get(JOIN_STACK_SIZE);
    status = x_thread_create(first, cc_intermediate_entry, cc_Args, stack, JOIN_STACK_SIZE, prio_offset + 2, TF_START);
    if (status != xs_success) {
      oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
      exit(0);
    }

    x_thread_sleep(10);
    
    status = x_thread_join(first, &result, x_eternal);
    if (status != xs_success) {
      oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
      exit(0);
    }  

    status = x_thread_delete(first);
    if (status != xs_success) {
      oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
      exit(0);
    }  
    x_mem_free(first);
    x_mem_free(stack);
  
    oempa("Multi join test, pass %d.\n", pass);
    
    x_thread_sleep(second * ((x_random() % 8) + 4));

  }  
  
}

static volatile x_int mu_state = 0;

typedef struct mu_Arg {
  x_thread  joinee;
  x_thread  joiner;
  x_ubyte * stack;
} mu_Arg;

typedef struct mu_Arg * mu_arg;

static void mu_final_entry(void * arg) {

  x_thread thread = arg;
  
  while (mu_state != 3) {
    x_thread_sleep(10);
  }

  oempa("Final thread %d is going to signal all joiners...\n", thread->id);
  
}

static void mu_joiner_entry(void * a) {

  mu_arg arg = a;
  void * result;
  x_status status;

  while (1) {
    x_assert(critical_status == 0);
    if (mu_state == 2) {
      oempa("Thread %d going to join final thread %d...\n", arg->joiner->id, arg->joinee->id);
      status = x_thread_join(arg->joinee, &result, x_eternal);
      if (status != xs_success) {
        oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
      break;
    }
    else {
      x_thread_sleep(40);
    }
  }

  oempa("Thread %d returning...\n", thread_current->id);
  
}

static void multi_entry(void * arg) {

  mu_Arg * joiners;
  x_thread joinee;
  x_ubyte * stack;
  x_status status;
  x_int n;
  x_int i;
  x_int pass = 0;
  x_int tries;

  joinee = x_mem_get(sizeof(x_Thread));
  stack = x_mem_get(JOIN_STACK_SIZE);
  
  while (1) {
    x_assert(critical_status == 0);
    pass += 1;
    mu_state = 1;
    n = (x_random() % 15) + 1;
    oempa("Starting multi thread join with %d joiners on a single thread.\n", n);
    status = x_thread_create(joinee, mu_final_entry, joinee, stack, JOIN_STACK_SIZE, prio_offset + 1, TF_START);
    if (status != xs_success) {
      oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
      exit(0);
    }

    joiners = x_mem_get(n * sizeof(mu_Arg));
    for (i = 0; i < n; i++) {
      joiners[i].joiner = x_mem_get(sizeof(x_Thread));
      memset(joiners[i].joiner, 0xaa, sizeof(x_Thread));
      joiners[i].stack = x_mem_get(JOIN_STACK_SIZE);
      joiners[i].joinee = joinee;
      status = x_thread_create(joiners[i].joiner, mu_joiner_entry, & joiners[i], joiners[i].stack, JOIN_STACK_SIZE, prio_offset + 2, TF_START);
      if (status != xs_success) {
        oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
        exit(0);
      }
    }
    
    x_thread_sleep(second);

    mu_state = 2;
    
    x_thread_sleep(second);

    mu_state = 3;
    
    x_thread_sleep(second);

    for (i = 0; i < n; i++) {
      tries = 0;
      while (tries++ < 20 && joiners[i].joiner->state != xt_ended) {
        x_thread_sleep(30);
      }
      if (joiners[i].joiner->state != xt_ended) {
        oempa("Bad state '%s'; I would expect 'ended' ...\n", x_state2char(joiners[i].joiner));
        exit(0);
      }
      if (joiners[i].joiner->joining_with != NULL) {
        oempa("Joining with field not reset properly.\n");
        exit(0);
      }
      if (joiners[i].joiner->l_joining_with != NULL) {
        oempa("Joining with list not reset properly.\n");
        exit(0);
      }
      if (joiners[i].joiner->l_joining_us != NULL) {
        oempa("Joining us should be NULL.\n");
        exit(0);
      }
      x_mem_free(joiners[i].joiner);
      x_mem_free(joiners[i].stack);
    }

    x_mem_free(joiners);

    oempa("Multi thread join on single thread with %d threads; pass %d OK\n", n, pass);
    
    x_thread_sleep(second * ((x_random() % 8) + 4));
    
  }
  
}

/*
** In the following tests, we check the proper join/wakeup behaviour of Oswald. A thread is
** created, the jowa_sleeper, that sleeps a random time and on which the jowa_joiner thread
** is going to try to join for a certain timeout window. A third thread, the jowa_thread, is
** going to wakeup the jowa_joiner thread. Since timeouts are random, it will test the behaviour
** of a thread that allready has exited and is not joinable anymore, and how the join list internally
** are kept in a good state.
*/

static x_thread jowa_sleeper;
static x_thread jowa_joiner;

static x_ubyte * jowa_sleeper_stack;
static x_ubyte * jowa_joiner_stack;

volatile static x_status join_status;
static const x_size join_notover = 100;

static void jowa_sleeper_entry(void * arg) {

  x_size sleep = (x_random() % 60) + 40;
  x_thread thread = arg;
  
  oempa("Thread %d going to sleep %d ticks before returning...\n", thread->id, sleep);
  x_thread_sleep(sleep);
  oempa("OK, we're returning...\n");
  
}

static void jowa_joiner_entry(void * arg) {

  void * result;
  x_size window = (x_random() % 60) + 40;
  x_thread thread = arg;
  
  oempa("Thread %d going to join sleeper thread with timeout %d ticks.\n", thread->id, window);
  join_status = join_notover;
  join_status = x_thread_join(jowa_sleeper, &result, window);
  oempa("Join result = '%s'\n", x_status2char(join_status));
  
}

static void jowa_entry(void * arg) {

  x_status status;
  x_int attempt;
  x_int pass = 0;
  x_size sleep;
  x_thread thread = arg;
  static const x_int max_attempts = 10;
  
  while (1) {
    x_assert(critical_status == 0);
    pass += 1;
    jowa_sleeper = x_mem_get(sizeof(x_Thread));
    jowa_sleeper_stack = x_mem_get(JOIN_STACK_SIZE);
    x_thread_create(jowa_sleeper, jowa_sleeper_entry, jowa_sleeper, jowa_sleeper_stack, JOIN_STACK_SIZE, prio_offset + 4, TF_START);
    jowa_joiner = x_mem_get(sizeof(x_Thread));
    jowa_joiner_stack = x_mem_get(JOIN_STACK_SIZE);
    x_thread_create(jowa_joiner, jowa_joiner_entry, jowa_joiner, jowa_joiner_stack, JOIN_STACK_SIZE, prio_offset + 4, TF_START);

    sleep = (x_random() % 60) + 40;
    oempa("Thread %d going to sleep for %d ticks...\n", thread->id, sleep);
    x_thread_sleep(sleep);
    oempa("OK, sleeping over; going to wakeup joiner thread...\n");
    
    status = x_thread_wakeup(jowa_joiner);
    oempa("Woken up joiner thread: status = '%s'\n", x_status2char(status));
    if (join_status != join_notover) {
      oempa("Join status %s\n", x_status2char(join_status));
    }

    attempt = 0;
    while (jowa_joiner->state != xt_ended && attempt++ < max_attempts) {
      x_thread_sleep(40);
    }
    if (attempt == max_attempts) {
      oempa("Could not find joiner ended after %d attempts; state = %d\n", attempt, jowa_joiner->state);
      abort();
    }

    attempt = 0;
    while (jowa_sleeper->state != xt_ended && attempt++ < max_attempts) {
      x_thread_sleep(40);
    }
    if (attempt == max_attempts) {
      oempa("Could not find sleeper ended after %d attempts; state = %d\n", attempt, jowa_joiner->state);
      abort();
    }

    oempa("Join/wakeup test pass %d OK.\n", pass);
    x_thread_delete(jowa_joiner);
    x_thread_delete(jowa_sleeper);
    x_mem_free(jowa_joiner);
    x_mem_free(jowa_sleeper);
    x_mem_free(jowa_joiner_stack);
    x_mem_free(jowa_sleeper_stack);

    x_thread_sleep(second * ((x_random() % 8) + 4));
    
  }
  
}

static x_thread cascade_thread;
static x_thread multi_thread;
static x_thread jowa_thread;

x_ubyte * join_test(x_ubyte * memory) {

  x_status status;

  second = x_seconds2ticks(1);

  cascade_thread = x_alloc_static_mem(memory, sizeof(x_Thread));
  status = x_thread_create(cascade_thread, cascade_entry, cascade_thread, x_alloc_static_mem(memory, JOIN_STACK_SIZE), JOIN_STACK_SIZE, prio_offset + 4, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }
  else {
    oempa("Thread id = %d.\n", cascade_thread->id);
  }

  multi_thread = x_alloc_static_mem(memory, sizeof(x_Thread));
  status = x_thread_create(multi_thread, multi_entry, multi_thread, x_alloc_static_mem(memory, JOIN_STACK_SIZE), JOIN_STACK_SIZE, prio_offset + 4, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }
  else {
    oempa("Thread id = %d.\n", multi_thread->id);
  }

  jowa_thread = x_alloc_static_mem(memory, sizeof(x_Thread));
  status = x_thread_create(jowa_thread, jowa_entry, jowa_thread, x_alloc_static_mem(memory, JOIN_STACK_SIZE), JOIN_STACK_SIZE, prio_offset + 4, TF_START);
  if (status != xs_success) {
    oempa("%s: status = '%s'\n", __FILE__, x_status2char(status));
    exit(0);
  }
  else {
    oempa("Thread id = %d.\n", jowa_thread->id);
  }

  return memory;
  
}

