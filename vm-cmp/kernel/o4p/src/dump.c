/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2007 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

#include "oswald.h"

void w_dump(const char*, ...);
char* w_dump_name(void *);
void w_dump_trace(void *);

void x_dump_thread(x_thread t) {

  if(!t->xref) {
    
    /*
    ** Not a wonka thread.
    */

    return;
    
  }
  
  w_dump("  \"%w\" ", w_dump_name(t->xref));
#ifdef LINUX
  w_dump(" (id: 0x%08x, pid: %d, prio: %d, state: %d %s", t, t->pid, t->o4p_thread_priority, t->state, x_state2char(t));
#else
  w_dump(" (id: 0x%08x, prio: %d, state: %d %s", t, t->o4p_thread_priority, t->state, x_state2char(t));
#endif
  w_dump(")\n");
  
  if(t->waiting_on) {
    x_dump_monitor("waiting on monitor", t->waiting_on);
  }
  if (isSet(t->flags, TF_RECEIVING)) {
    w_dump("    blocked receiving from queue\n");
  }

  if (isSet(t->flags, TF_SENDING)) {
    w_dump("    blocked sending to queue\n");
  }

  if (isSet(t->flags, TF_TIMEOUT)) {
    w_dump("    timed out\n");
  }

  if(t->xref) {
    w_dump_trace(t->xref);
  }

}

void x_dump_threads(void) {

  int res;
  x_thread t;

  res = pthread_mutex_lock(&o4pe->threadsLock);
  if (res != 0) {
    w_dump("Attempt to lock o4pe->threadsLock failed... %d\n", res);
    abort();
  }
  t = x_thread_current();
  if (t && t->xref) {
    w_dump("Current thread is \"%w\"\n", w_dump_name(t->xref));
  }
  for (t = o4pe->threads; t != NULL; t = t->o4p_thread_next) {
    x_dump_thread(t);
  }
  res = pthread_mutex_unlock(&o4pe->threadsLock);
  
  w_dump("\n");
}

void x_dump_mutex(char * msg, x_mutex mutex) {
  if (mutex == NULL)
     return;
  if(mutex->owner) {
    w_dump("   %sLocked", msg);
    if(mutex->owner->xref) {
      w_dump(" by \"%w\"", w_dump_name(mutex->owner->xref));
    }
    else {
      w_dump(" by native thread");
    }
    w_dump("\n");
  }
  else {
    w_dump("   %snot locked\n", msg);
  }
}

void x_dump_monitor(char * msg, x_monitor monitor) {
  if(!monitor) {
    w_dump("   %s (%p) Not yet initialized\n", msg, monitor);
  }
  else if (monitor->owner) {
    w_dump("   %s (%p) Locked", msg, monitor);
    if(monitor->owner->xref) {
      w_dump(" by \"%w\"", w_dump_name(monitor->owner->xref));
    }
    w_dump("\n");
  }
  else {
    w_dump("   %s (%p) not locked\n", msg, monitor);
  }
}

void x_dump_monitor_if_locked(char * msg, x_monitor monitor) {
  if(!monitor) {
    w_dump("   %s (%p) Not yet initialized", msg, monitor);
  }
  else if (monitor->owner) {
    w_dump("   %s (%p) Locked", msg, monitor);
    if(monitor->owner->xref) {
      w_dump(" by \"%w\"", w_dump_name(monitor->owner->xref));
    }
    w_dump("\n");
  }
}

