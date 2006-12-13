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
** $Id: dump.c,v 1.7 2006/10/04 14:24:20 cvsroot Exp $
*/    
 
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

