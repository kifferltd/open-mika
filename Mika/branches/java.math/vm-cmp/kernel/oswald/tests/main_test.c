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
** $Id: main_test.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include <tests.h>

/*
** Set either the do_all to 'true' for all tests to be launched, or set it
** to 'false' and select each test individually.
*/

static x_boolean       do_all = true;

static x_boolean   do_monitor = true;
static x_boolean     do_timer = false;
static x_boolean     do_mutex = false;
static x_boolean    do_thread = false;
static x_boolean    do_atomic = false;
static x_boolean     do_queue = false;
static x_boolean       do_sem = false;
static x_boolean    do_memory = false;
static x_boolean     do_block = false;
static x_boolean       do_map = false;
static x_boolean   do_signals = false;
static x_boolean   do_modules = false;
static x_boolean do_exception = false;
static x_boolean      do_list = false;
static x_boolean     do_stack = false;
static x_boolean      do_join = false;

/*
** Main for total process.
*/

int main(int argc, char * argv[]) {

  x_oswald_init(12 * 1024 * 1024, 5);

  return 1;
    
}

/*
** Oswald main program for the test modules.
*/

x_size prio_offset = 0;

x_ubyte * x_os_main(x_int argc, char * argv[], x_ubyte *memory) {

  x_size used = (x_size)memory;

  if (do_all) {
    oempa("Initializing all test modules...\n");
    memory = monitor_test(memory);
    memory = timer_test(memory);
    memory = mutex_test(memory);
    memory = thread_test(memory);
    memory = atomic_test(memory);
    memory = queue_test(memory);
    memory = sem_test(memory);
    memory = block_test(memory);
    memory = map_test(memory);
    memory = signals_test(memory);
//    memory = list_test(memory);
//    memory = modules_test(memory);
    memory = exception_test(memory);
//    memory = stack_test(memory);
    memory = join_test(memory);
    memory = memory_test(memory);
  }
  else {
    if (do_monitor) {
      memory = monitor_test(memory);
    }
    if (do_timer) {
      memory = timer_test(memory);
    }
    if (do_mutex) {
      memory = mutex_test(memory);
    }
    if (do_thread) {
      memory = thread_test(memory);
    }
    if (do_atomic) {
      memory = atomic_test(memory);
    }
    if (do_queue) {
      memory = queue_test(memory);
    }
    if (do_sem) {
      memory = sem_test(memory);
    }
    if (do_block) {
      memory = block_test(memory);
    }
    if (do_map) {
      memory = map_test(memory);
    }
    if (do_signals) {
      memory = signals_test(memory);
    }
    if (do_exception) {
      memory = exception_test(memory);
    }
    if (do_stack) {
//      memory = stack_test(memory);
    }
    if (do_list) {
//      memory = list_test(memory);
    }
    if (do_modules) {
//      memory = modules_test(memory);
    }
    if (do_join) {
      memory = join_test(memory);
    }
    if (do_memory) {
      memory = memory_test(memory);
    }
  }

  used = (x_size) memory - used;
  
  oempa("Done initializing, used %d static bytes.\n", used);
  
  return memory;
 
}

/*
** Function to allocate a number of bytes for the tester functions. Since the memory
** check can take away all bytes, we loop around untill the memory checker thread has
** released enough bytes.
*/

void * x_mem_get(x_size size) {

  x_int i = 0;
  void * block;
  x_size bytes_discarded;
  x_size blocks_discarded;
  
  block = x_mem_alloc(size);
  while (block == NULL && i < 50) {
    i++;
    x_thread_sleep(1000);
    x_mem_collect(&bytes_discarded, &blocks_discarded);
    oempa("CHECK: discarded %d bytes in %d blocks\n", bytes_discarded, blocks_discarded);
    block = x_mem_alloc(size);
    if (block) {
      oempa("CHECK: Could only get %d bytes of memory after %d attempts.\n", size, i);
    }
    if (i % 10 == 0) {
      oempa("%d attempt for %d bytes. Taking long sleep...\n", i, size);
      x_thread_sleep(10000);
    }
  }
  
  if (block == NULL) {
    oempa("Could not fullfil %d bytes after %d attempts.\n", size, i);
    abort();
  }
  
  return block;
  
}

#define BSIZE 160

x_int oswald_vsnprintf(char * buf, x_size bufsize, const char *fmt, va_list args);
x_int oswald_snprintf(char * buf, int bufsize, const char *fmt, ...);

ssize_t write(int fd, const void *buf, size_t count); 

void _oempa(const char *function, const int line, const char *fmt, ...) {

  va_list ap;
  char buffer[BSIZE];
  x_size i;

  i = x_snprintf(buffer, BSIZE, "0x%08x OS %24s %4d: ", critical_status, function, line);
  va_start(ap, fmt);
  i += x_vsnprintf(buffer + i, BSIZE - strlen(buffer), fmt, ap);
  va_end(ap);

  write(1, buffer, i);

}

/*
** Try to find a thread over all the pcbs, regardless wether it's pending or ready. Return 0
** when not found, 1 when found and ready, 2 when found and pending.
*/

x_int find_thread_in_pcbs(x_thread thread) {

  x_pcb pcb;
  x_int i;
  x_thread t;

  x_preemption_disable;
  
  for (i = 0; i < NUM_PRIORITIES; i++) {
    pcb = x_prio2pcb((x_size)i);

    t = pcb->t_ready;
    if (t) {
      do {
        if (t == thread) {
          x_preemption_enable;
          return 1;
        }
        t = t->next;
      } while (t != pcb->t_ready);
    }

    t = pcb->t_pending;
    if (t) {
      do {
        if (t == thread) {
          x_preemption_enable;
          return 2;
        }
        t = t->next;
      } while (t != pcb->t_pending);
    }
  }

  x_preemption_enable;

  return 0;
  
}
