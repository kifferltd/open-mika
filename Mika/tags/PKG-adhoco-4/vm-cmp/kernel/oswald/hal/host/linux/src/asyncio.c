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
** $Id: asyncio.c,v 1.2 2006/03/14 09:52:29 cvs Exp $
*/

#include <stdio.h>
#include <fcntl.h>
#include <unistd.h>

#include "oswald.h"
#include "asyncio.h"

#include <errno.h>
#include <string.h>
#include <signal.h>

#define ASYNC_TABLE_SIZE 127
#define LEVEL 9 

x_int x_async_ready = 0;

typedef struct x_Async_Entry {
  x_int fd;                     // The filedescriptor.
  x_Monitor monitor;            // The monitor the thread will wait on. 
  x_int flags;                  // flags.
  x_int count;                  // use count;
  struct x_Async_Entry *next;   // The next entry.
  struct x_Async_Entry *prev;   // The previous entry. 
} x_Async_Entry;

typedef x_Async_Entry *x_async_entry;

static x_async_entry *x_async_table;

static struct sigaction io;

/*
** Get an entry from the hashtable.
*/

x_async_entry x_async_get(x_int fd) {
  x_async_entry iter;

  x_preemption_disable;
  iter = x_async_table[fd % ASYNC_TABLE_SIZE];

  while(iter) {
    if(iter->fd == fd) {
      iter->count++;
      x_preemption_enable;
      return iter;
    }
    iter = iter->next;
  }

  x_preemption_enable;
  return NULL;
}

/*
** Add an entry to the hashtable.
*/

void x_async_add(x_async_entry entry) {
  x_preemption_disable;
  entry->next = x_async_table[entry->fd % ASYNC_TABLE_SIZE];
  if(entry->next) {
    entry->next->prev = entry;
  }
  x_async_table[entry->fd % ASYNC_TABLE_SIZE] = entry;
  x_preemption_enable;
}

/*
** Remove an entry from the hashtable.
*/

void x_async_remove(x_async_entry entry) {
  x_preemption_disable;
  if(entry->prev && entry->next) {
    entry->prev->next = entry->next;
    entry->next->prev = entry->prev;
  }
  else if(!entry->prev) { /* First on the list */
    x_async_table[entry->fd % ASYNC_TABLE_SIZE] = entry->next;
    if(entry->next) entry->next->prev = NULL;
  }
  else { /* Last on the list */
    entry->prev->next = NULL;
  }
  x_preemption_enable;
}

/*
** The signal handler which gets called everytime a SIGIO signal is
** received.
*/

static void x_async_handler(int signum, siginfo_t * sis, void * arg) {
  x_async_entry entry;
  x_status status;

  x_preemption_disable;
  
  // loempa(LEVEL, "Got an IO signal. fd %d, code %d, band %d\n", sis->si_fd, sis->si_code, sis->si_band);

  entry = x_async_get(sis->si_fd);

  if(entry) {

    /*
    ** There's IO on one of the registered filedescriptors.
    ** Set a flag to indicate something is available.
    */
    
    entry->flags = 1;
    
    if(entry->monitor.n_waiting) {

      /*
      ** There is a monitor waiting on this filedescriptor. This means a thread has 
      ** called x_async_block and is blocking until it's notified.
      ** x_monitor_kick_all is used because it does not require us to enter the 
      ** monitor before we can notfiy it. Since we're in the signal handler and 
      ** not an oswald thread, this is not possible.
      */
      
      status = x_monitor_kick_all(&entry->monitor);
    }

    entry->count--;
  }

  x_preemption_enable;
}

/*
**
*/

void x_async_kick(x_async_entry entry) {
  loempa(LEVEL, "Kicking fd %d\n", entry->fd);
  entry->flags = 1;
  x_monitor_kick_all(&entry->monitor);
}

/*
** Register a filedescriptor.
*/

x_int x_async_register(x_int fd) {
  x_int result;
  x_async_entry entry;

  /*
  ** Change the filedescriptor to make it nonblocking, asynchronous and
  ** send a SIGIO whenever something changes on that filedescriptor.
  */

  result =  fcntl(fd, F_SETFL, O_NONBLOCK|O_ASYNC);
  result |= fcntl(fd, 10, SIGRTMIN + 1);
  result |= fcntl(fd, F_SETOWN, getpid());

  if(result) {
    loempa(LEVEL, "BAD MOJO on fd %d!!!\n", fd);
    return result;
  }

  /*
  ** Check if this filedescriptor isn't already registered.
  */
 
  if(!(entry = x_async_get(fd))) {

    /*
    ** It's not registered. 
    ** Allocate an entry, initialize the monitor and add it to the hashtable.
    */
    
    entry = x_mem_calloc(sizeof(x_Async_Entry));
    entry->fd = fd;
    x_monitor_create(&entry->monitor);

    x_async_add(entry);
  
    loempa(LEVEL, "Added an entry for fd %d\n", fd);
  }
  else {

    /*
    ** The entry already exists. This should not happen, but is not a big
    ** problem.
    */
    
    loempa(9, "Entry already existed for fd %d\n", fd);
    entry->flags = 0;
    entry->count--;
  }
    
  return 0;
}

/*
** Unregister a filedescriptor.
*/

void x_async_unregister(x_int fd) {
  x_async_entry entry = x_async_get(fd);
  if(entry) {

    loempa(LEVEL, "Removed entry %d\n", entry->fd);

    /*
    ** Remove the entry from the hashtable and delete the monitor.
    */
    

    x_async_remove(entry);


    /*
    ** count must be 1 (this functions also uses the entry).
    */

    while(entry->count > 1) {
      x_async_kick(entry);
      x_thread_sleep(1);
    }

    x_monitor_eternal(&entry->monitor);

    x_monitor_delete(&entry->monitor);

    x_mem_free(entry);
  }
}

/*
** Block until there's a change on the given filedescriptor, 
** or if a timeout has occured.
*/

x_int x_async_block(x_int fd, x_int timeout) {
  x_async_entry entry = x_async_get(fd);
  x_status status;
  if(entry) {

    /*
    ** Check if we received data before we entered the call.
    */ 

    if(entry->flags) {
      entry->flags = 0;
      entry->count--;
      return 0;
    }
   
    /*
    ** Enter the monitor.
    */
    
    status = x_monitor_eternal(&entry->monitor);

    /*
    ** Check once again if something changed while we 
    ** acquired the monitor.
    */
    
    if(entry->flags) {
      entry->flags = 0;
      x_monitor_exit(&entry->monitor);
      entry->count--;
      return 0;
    }
   
    /*
    ** Wait until we are notified by the signal handler.
    */
    
    status = x_monitor_wait(&entry->monitor, timeout);

    /*
    ** We've been notified (or a timeout has occured). 
    */

    if(status == xs_success) {
      x_monitor_exit(&entry->monitor);
    }

    if(entry->flags == 1) {
      
      /*
      ** There's new data.
      */

      loempa(LEVEL, "New data on %d\n", fd);
      
      entry->flags = 0;

      entry->count--;
      return 0;
    }
    else {

      /*
      ** Timeout.
      */
      
      loempa(LEVEL, "Timeout on %d\n", fd);
      entry->count--;
      return -1;
    }
    entry->count--;
  }
  else {
    loempa(9, "No entry for fd %d\n", fd);
    x_thread_sleep(2);
  }
  return xs_success;
}

/*
** Setup the asyncio.
*/

void x_async_setup(void) {

  x_int result;

  /*
  ** Register the SIGIO handler.
  */

  io.sa_sigaction = x_async_handler;
  sigemptyset(&io.sa_mask);
  io.sa_flags = SA_SIGINFO;
  result = sigaction(SIGRTMIN + 1, &io, NULL);
  if (result == -1) {
    loempa(9, "Registering the io handler failed: %s (%d).\n", strerror(errno), errno);
    abort();
  }

  /*
  ** Allocate the hashtable.
  */

  x_async_table = x_mem_calloc(ASYNC_TABLE_SIZE * sizeof(x_Async_Entry));

  x_async_ready = 1;
}


