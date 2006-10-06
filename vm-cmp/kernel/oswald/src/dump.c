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
** $Id: dump.c,v 1.1.1.1 2004/07/12 14:07:44 cvs Exp $
*/

#include "oswald.h"

void w_dump(const char*, ...);
char* w_dump_name(void *);
void w_dump_trace(void *);

/*
** An array we use to quickly store the thread pointers.
*/

static x_thread all_threads[2048];

void x_dump_thread(x_thread t) {

  x_size size;
  x_size used;
  x_size left;
  x_int percentage;
  x_thread oid = NULL; // Owner 
  x_int type;
  x_event event;
  x_monitor monitor;
  x_mutex mutex;

  if(!t->xref) {
    
    /*
    ** Not a wonka thread.
    */

    return;
    
  }
  
  x_stack_info(t, &size, &used, &left);
  percentage = (size * 100 - left * 100) / size;

  if (t->competing_for) {
    event = (x_event)t->competing_for;
    type = x_event_type_get(event);
    if (type == xe_monitor) {
      monitor = (x_monitor)event;
      if (monitor->owner) {
        oid = monitor->owner;
      }
    }
    else if (type == xe_mutex) {
      mutex = (x_mutex)event;
      if (mutex->owner) {
        oid = mutex->owner;
      }
    }
  }

  w_dump("  \"%w\" ", w_dump_name(t->xref));
  w_dump(" (id: 0x%08x(%d), prio: %d, state: %d %s, switches: %d", t, t->id, t->c_prio, t->state, x_state2char(t), t->num_switches);
  w_dump(", stacksize: %d", size);
#ifdef DEBUG
  w_dump(", stack used: %d (%d%%)", used, percentage);
#endif
  w_dump(")\n");
  
  if(t->waiting_for) {
    w_dump("    waiting: 0x%08x (%d)", t->waiting_for, t->waiting_for->n_waiting);
  }
  if(t->competing_for) {
    w_dump("    competing: 0x%08x ", t->competing_for);
    switch(x_event_type_get(t->competing_for)) {
      case xe_monitor: w_dump("(monitor)"); break;
      case xe_mutex:   w_dump("(mutex)"); break;
      default:         w_dump("(unknown)"); break;
    }
    w_dump(" owned by: 0x%08x(%d)", oid, (oid ? oid->id : 0));
    if(oid && oid->xref) w_dump(" \"%w\"", w_dump_name(oid->xref));
  }
  if(t->waiting_for || t->competing_for) {
    w_dump("\n");
  }
 
  if(t->xref) {
    w_dump_trace(t->xref);
  }

}

void x_dump_threads() {

  x_size i;
  x_pcb pcb;
  x_thread t;
  x_size num_threads = 0;
  x_size cursor = 0;
  static x_size max = 0;

  w_dump(" Threads :\n");
  
  for (i = 0; i < NUM_PRIORITIES - 1; i++) {
    pcb = x_prio2pcb(i);
    if (pcb->t_ready || pcb->t_pending) {

      /*
      ** Ready threads
      */

      t = pcb->t_ready;
      if (t) {
        do {
          num_threads += 1;
          all_threads[cursor++] = t;
          t = t->next;
        } while (t != pcb->t_ready);
      }
      
      /*
      ** Pending threads
      */

      t = pcb->t_pending;
      if (t) {
        do {
          num_threads += 1;
          all_threads[cursor++] = t;
          t = t->next;
        } while (t != pcb->t_pending);
      }

    }
  }

  if (num_threads > max) {
    max = cursor;
  }
  
  for (i = 0; i < cursor; i++) {
    x_dump_thread(all_threads[i]);
  }
  
  w_dump("\n");
}

void x_dump_mutex(char * msg, x_mutex mutex) {
  if(mutex->owner) {
    w_dump("   %s (%p) Locked", msg, mutex);
    if(mutex->owner->xref) {
      w_dump(" by \"%w\"", w_dump_name(mutex->owner->xref));
    }
    w_dump("\n");
  }
  else {
    w_dump("   %s (%p) not locked\n", msg, mutex);
  }
}

void x_dump_monitor(char * msg, x_monitor monitor) {
  if(!monitor) {
    w_dump("   %s (%p) Not yet initialized", msg, monitor);
  }
  else if(monitor->owner) {
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
  else if(monitor->owner) {
    w_dump("   %s (%p) Locked", msg, monitor);
    if(monitor->owner->xref) {
      w_dump(" by \"%w\"", w_dump_name(monitor->owner->xref));
    }
    w_dump("\n");
  }
}

