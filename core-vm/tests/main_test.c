/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: main_test.c,v 1.2 2004/11/18 23:51:52 cvs Exp $
*/

#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#include "oswald.h"
#include "wonka.h"
#include "tests.h"

Wonka_InitArgs  system_InitArgs;

char *woempa_dump_file = NULL;
int   woempa_stderr = 1;
int   woempa_bytecodecount   = 0;
int   woempa_bytecodetrigger = 0;

/*
** Set either the do_all to 'true' for all tests to be launched, or set it
** to 'false' and select each test individually.
*/

static x_boolean        do_all = false;

static x_boolean  do_hashtable = true;
static x_boolean       do_fifo = true;
static x_boolean     do_ts_mem = false; // <= Fails, segfault...

/*
** Main for total process.
*/

int main(int argc, char * argv[]) {

  x_oswald_init(70 * 1024 * 1024, 50);

  return 0;

}

/*
** Oswald main program for the test modules.
*/

x_ubyte * x_os_main(x_int argc, char * argv[], x_ubyte *memory) {

  x_size used = (x_size)memory;

  if (do_all) {
    oempa("Initializing all test modules...\n");
    memory = hashtable_test(memory);
    memory = fifo_test(memory);
    memory = ts_mem_test(memory);
  }
  else {
    if (do_hashtable) {
      memory = hashtable_test(memory);
    }
    if (do_fifo) {
      memory = fifo_test(memory);
    }
    if (do_ts_mem) {
      memory = ts_mem_test(memory);
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
  
  block = x_mem_alloc(size);
  while (block == NULL && i++ < 50) {
    x_thread_sleep(10);
    block = x_mem_alloc(size);
    if (block) {
      oempa("CHECK: Could only get %d bytes of memory after %d attempts.\n", size, i);
    }
  }
  
  if (block == NULL) {
    oempa("Could not fullfil %d bytes after %d attempts.\n", size, i);
    exit(0);
  }
  
  return block;
  
}

#define BSIZE 160

x_int oswald_vsnprintf(char * buf, x_size bufsize, const char *fmt, va_list args);
x_int oswald_snprintf(char * buf, int bufsize, const char *fmt, ...);

void _oempa(const char *function, const int line, const char *fmt, ...) {

  va_list ap;
  char buffer[BSIZE];
  x_ubyte * cursor = buffer;
  x_size i;

  for (i = 0; i < BSIZE; i++) {
    *cursor++ = 0;
  }
#if defined(OSWALD)
  x_snprintf(buffer, BSIZE, "0x%08x OS %24s %4d: ", critical_status, function, line);
#elif defined(O4P)
  x_snprintf(buffer, BSIZE, "OS %24s %4d: ", function, line);
#else
  x_snprintf(buffer, BSIZE, "OS %24s %4d: ", function, line);
#endif

  va_start(ap, fmt);
  x_vsnprintf(buffer + strlen(buffer), BSIZE - strlen(buffer), fmt, ap);
  va_end(ap);

  write(1, buffer, strlen(buffer));

}

w_void wonka_init(w_void) {
  static w_int done = 0;
  if(!done) {
    done = 1;
  }
}

