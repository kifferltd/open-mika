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
*                                                                         *
* Modifications copyright (c) 2003 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: wax.c,v 1.3 2006/09/11 13:21:39 cvsroot Exp $
*/

#include <unistd.h>
#include <stdarg.h>
#include <stdio.h>
#include <string.h>
#include <time.h>

#include "oswald.h"
#ifndef __uClinux__
#include <asyncio.h>
#endif

int command_line_argument_count;
char **command_line_arguments; 

/*
** The different thread states written as character strings and the function to
** get them in an appropriate way.
*/

static const char * _state2char[] = {
  "Ready",                        /*  0  corresponds to 'xe_unused' event type.                                                            */
  "Mutex",                        /*  1  synchronised with event type numbers.                                                             */
  "Queue",                        /*  2  synchronised with event type numbers.                                                             */
  "Mailbox" ,                     /*  3  synchronised with event type numbers.                                                             */
  "Semaphore",                    /*  4  synchronised with event type numbers.                                                             */
  "Signals",                      /*  5  synchronised with event type numbers.                                                             */
  "Monitor",                      /*  6  synchronised with event type numbers.                                                             */
  "Block",                        /*  7  synchronised with event type numbers.                                                             */
  "Map",                          /*  8  synchronised with event type numbers.                                                             */ 
  "Joining",                      /*  9  special event state that signals that a thread is waiting for a join.                             */
  "Waiting",                      /* 10                                                                                                    */
  "Suspended",                    /* 11                                                                                                    */
  "Sleeping",                     /* 12                                                                                                    */
  "Rescheduled",                  /* 13                                                                                                    */
  "Ended",                        /* 14                                                                                                    */
  NULL,                           /* 15                                                                                                    */
};

const char * x_state2char(x_thread thread) {
  return _state2char[(thread->state > xt_unknown) ? xt_unknown : thread->state];
}

#define IDLE_STACK_SIZE           (1024 * 10)
#define INIT_STACK_SIZE           (1024 * 10)

inline static x_size round_up(x_size value, x_size rounding) {  
  return (value + (rounding - 1)) & ~(rounding - 1);
}
  
/*
void * xi_alloc_static_mem(x_ubyte ** memory, x_size size) {

  x_ubyte * block;
  x_ubyte * cursor;
  x_size i;
  
  printf("xi_alloc_static_mem: memory pointer was at %p, allocating %d bytes\n", *memory, size);
  size = round_up(size, 8);
  *memory = (x_ubyte *)(((x_size)(*memory) + 7) & 0xfffffff8);
  block = *memory; 
  cursor = *memory;
  for (i = 0; i < size; i++) {
    *cursor++ = 0;
  }
  *memory += size;
  printf("xi_alloc_static_mem: memory pointer now at %p\n", *memory);

  return block;   

}
*/

/*
** The entry for our idle thread.
*/

void x_idle_entry(void * t) {

  unsigned long long idles = 0;
  struct timespec ts0;
  struct timespec ts1;

  ts0.tv_sec = 0;
  ts0.tv_nsec = 200000000;

  while (1) {
/*
#ifdef DEBUG
    if ((idles % 300) == 0) {
      loempa(9, "----------- INIT 60 seconds tick, %d threads ---------------------------------\n", thread_count);
      x_pcbs_dump();
    }
#endif
*/
    idles++;
    nanosleep(&ts0, &ts1);
  }
  
}

const char * report_init(x_thread thread) {
  return "init thread";
}

/*
** Prepare the init thread; this init thread is the first thread that runs and will
** call the application defined threads...
**
*/

static x_Thread Thread_init;
static char Thread_init_stack[INIT_STACK_SIZE];

static void x_init_setup(void) {

  x_word * cursor;
  
  thread_init = &Thread_init;
  thread_init->b_stack = Thread_init_stack;
  thread_init->e_stack = thread_init->b_stack + INIT_STACK_SIZE - 1;
  thread_init->l_exception = NULL;
  thread_init->report = report_init;

  /*
  ** Word align the stack end (start of stack for the cpu which grows down).
  */
  
  thread_init->e_stack = (x_word)thread_init->e_stack & 0xfffffff4;
  thread_init->entry = x_init_entry;
  thread_init->id = 0;
  
  loempa(9, "Init thread = 0x%08x\n", thread_init);

  /*
  ** Preset the stack for size calculcations. See the comments in the x_thread_create function.
  */

  for (cursor = (x_word *)(thread_init->e_stack - sizeof(x_word)); cursor >= (x_word *)thread_init->b_stack; cursor--) {
    *cursor = 0xaaaaaaaa;
  }
        
  /*
  ** Make sure thread_current and thread_next are this init thread so that no
  ** context switch takes place as soon as we enable interrupts.
  */
  
  thread_current = thread_init;
  thread_next = thread_init;

  x_stack_init(thread_init);
}

static char Thread_idle_stack[IDLE_STACK_SIZE];

void x_kernel_setup(x_ubyte *memory) {

  x_status status;

  critical_status = 1;
  
  loempa(9, "Setting up cpu\n");
  memory = x_cpu_setup(memory);
  loempa(9, "Setting up irqs\n");
  x_irqs_setup();
  loempa(9, "Setting up pcbs\n");
  x_pcbs_setup();
  loempa(9, "Setting up events\n");
  memory = x_events_setup(memory);
  loempa(9, "Setting up init thread\n");
  x_init_setup();

  /*
  ** thread_init is now a valid pointer; we set up here the non cpu and host specific
  ** stuff for thread_init.
  */

  thread_init->a_prio = 0;
  thread_init->c_prio = 0;
  thread_init->a_quantums = 5;
  thread_init->c_quantums = 5;

  thread_init->state = xt_ready;
  xi_thread_add_pcb(thread_init);
  thread_count = 1;

  loempa(9, "Setting up idle thread\n");
  status = x_thread_create(thread_idle, x_idle_entry, thread_idle, Thread_idle_stack, IDLE_STACK_SIZE, MAX_PRIORITY, TF_START);
  if (status != xs_success) {
    loempa(9, "Status = '%s'\n", x_status2char(status));
    abort();
  }

  thread_current = thread_init;
  thread_next = thread_init;

  /*
  ** Since we have a thread_current and the thread_current has a state, only now can be
  ** setup the host, since the timer is started in x_host_setup and we need a valid
  ** thread_current for that timer.
  */

  loempa(9, "Setting up host\n");
  x_host_setup();

  /*
  ** As a last operation, set the memory argument for the initial thread.
  */
  
  thread_init->argument = memory;

  /*
  ** This call starts our init thread.
  */

  critical_status = 0;
  loempa(9, "Starting init thread\n");
  x_init_start(thread_init);
  loempa(9, "Started init thread\n");

}

/*
** The entry for our initial thread. Note that it gets passed the static memory pointer
** and not it's own thread reference.
*/

void x_init_entry(void * memory) {

  x_ubyte * start_heap;

  /*
  ** Interrupts are possibly enabled (depending on cpu startup function). During initialization,
  ** we disable them and enable them after setting up the kernel.
  */

  loempa(9, "Disabling interrupts for further initializing kernel in init thread.\n");

  x_preemption_disable;

  irq_depth = 0;

  x_init_timers();
  start_heap = x_os_main(command_line_argument_count, command_line_arguments, memory);
  x_mem_init();
  x_host_break(start_heap);
#ifndef __uClinux__
  x_async_setup();
#endif

  loempa(9, "User x_os_main called, releasing interrupts...\n");

  x_preemption_enable;

  while (1) {
    loempa(9, "----------- INIT 60 seconds tick, %d threads ---------------------------------\n", thread_count);
#ifdef DEBUG
    x_pcbs_dump();
#endif
    x_thread_sleep(x_seconds2ticks(30));
  }
  
}

/*
** Our loempa friend...
*/

#ifdef DEBUG

#define BSIZE 160

ssize_t write(int fd, const void *buf, size_t count); 

void _loempa(const char *function, const int line, const int level, const char *fmt, ...) {

  va_list ap;
  char buffer[BSIZE];
  x_size i;

  i = x_snprintf(buffer, BSIZE, "OS %35s %4d: ", function, line);
  va_start(ap, fmt);
  i += x_vsnprintf(buffer + i, BSIZE - i, fmt, ap);
  va_end(ap);

  write(1, buffer, i);

}

#endif

void _assert(const char * message) {

  loempa(9, "assertion failed: %s\n", message);
  abort();
  
}
