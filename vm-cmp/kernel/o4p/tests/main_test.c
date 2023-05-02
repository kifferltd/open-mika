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

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "tests.h"

/*
** Set either the do_all to 'true' for all tests to be launched, or set it
** to 'false' and select each test individually.
*/

static x_boolean       do_all = FALSE;

static x_boolean   do_monitor = TRUE;
#if 0
static x_boolean     do_timer = FALSE;
static x_boolean     do_mutex = FALSE;
static x_boolean    do_thread = FALSE;
static x_boolean    do_atomic = FALSE;
static x_boolean     do_queue = FALSE;
#endif
static x_boolean       do_sem = FALSE;
#if 0
static x_boolean    do_memory = FALSE;
static x_boolean     do_block = FALSE;
static x_boolean       do_map = FALSE;
static x_boolean   do_signals = FALSE;
static x_boolean   do_modules = FALSE;
static x_boolean do_exception = FALSE;
static x_boolean      do_list = FALSE;
static x_boolean     do_stack = FALSE;
static x_boolean      do_join = FALSE;
#endif

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

void x_os_main(int argc, char **argv) {

  if (do_all) {
    oempa("Initializing all test modules...\n");
#if 0
    monitor_test();
    timer_test();
    mutex_test();
    thread_test();
    atomic_test();
    queue_test();
#endif
    sem_test();
#if 0
    block_test();
    map_test();
    signals_test();
//    list_test();
//    modules_test();
    exception_test();
//    stack_test();
    join_test();
    _test();
#endif
  }
  else {
    if (do_monitor) {
      monitor_test();
    }
#if 0
    if (do_timer) {
      timer_test();
    }
    if (do_mutex) {
      mutex_test();
    }
    if (do_thread) {
      thread_test();
    }
    if (do_atomic) {
      atomic_test();
    }
    if (do_queue) {
      queue_test();
    }
#endif
    if (do_sem) {
      sem_test();
    }
#if 0
    if (do_block) {
      block_test();
    }
    if (do_map) {
      map_test();
    }
    if (do_signals) {
      signals_test();
    }
    if (do_exception) {
      exception_test();
    }
    if (do_stack) {
//      stack_test();
    }
    if (do_list) {
//      list_test();
    }
    if (do_modules) {
//      modules_test();
    }
    if (do_join) {
      join_test();
    }
    if (do_memory) {
      memory_test();
    }
#endif
  }

  oempa("Done initializing.\n");
  
  return;
 
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

#if 0
x_int oswald_vsnprintf(char * buf, x_size bufsize, const char *fmt, va_list args);
x_int oswald_snprintf(char * buf, int bufsize, const char *fmt, ...);

ssize_t write(int fd, const void *buf, size_t count); 
#endif

void _oempa(const char *function, const int line, const char *fmt, ...) {

  va_list ap;
  char buffer[BSIZE];
  x_size i;

  i = x_snprintf(buffer, BSIZE, "         %24s %4d: ", function, line);
  va_start(ap, fmt);
  i += x_vsnprintf(buffer + i, BSIZE - strlen(buffer), fmt, ap);
  va_end(ap);

  write(1, buffer, i);

}
