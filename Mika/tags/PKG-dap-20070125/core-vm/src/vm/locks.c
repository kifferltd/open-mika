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
*   Philips-site 5 box 3        info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
*                                                                         *
* Modifications copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: locks.c,v 1.6 2006/10/04 14:24:17 cvsroot Exp $
*/

#include <string.h>

#include "clazz.h"
#include "core-classes.h"
#include "exception.h"
#include "hashtable.h"
#include "heap.h"
#include "locks.h"
#include "ts-mem.h"
#include "threads.h"
#include "wonka.h"
#include "wordset.h"

/*
** The lock_hashtable maps instances onto x_monitor's.
*/

w_hashtable lock_hashtable;

void initLocks() {
}

/*
** allocMonitor: called to associate a monitor with an object with which no
** monitor is yet associated.
*/

x_monitor allocMonitor(w_instance instance) {

  x_monitor mon;
  x_monitor newmon = allocMem(sizeof(x_Monitor));
  // Note: we allocate the monitor outside of the lock, to avoid deadlocks
  // with gc when the sweep phase calls releaseMonitor().

  ht_lock(lock_hashtable);

  mon = (x_monitor)ht_read_no_lock(lock_hashtable, (w_word)instance);
  if (mon) {
    woempa(1, "instance %p already had a monitor %p\n", instance, mon);
    releaseMem(newmon);
  }
  else {
    mon = newmon;
    if (!mon) {
      wabort(ABORT_WONKA, "Unable to allocate space for object monitor\n");
    }
    x_monitor_create(mon);

    if (mon) {
      woempa(1, "created new lock %p for instance %p.\n", mon, instance);
      ht_write_no_lock(lock_hashtable, (w_word)instance, (w_word)mon);
      setFlag(instance2flags(instance), O_HAS_LOCK);
    }
    else {
      woempa(9, "No memory to create x_Monitor\n");
      throwOutOfMemoryError(currentWonkaThread);
    }
  }

  ht_unlock(lock_hashtable);

  return mon;

}

/*
** releaseMonitor: called to release the monitor with an object (should only
** be called when the object is unreachable).
*/

void releaseMonitor(w_instance instance) {

  x_monitor mon;

  ht_lock(lock_hashtable);
  mon = (x_monitor) ht_erase_no_lock(lock_hashtable, (w_word)instance);

  if (mon) {
    unsetFlag(instance2flags(instance), O_HAS_LOCK);
    woempa(1, "Deleting monitor %p of instance %p\n", mon, instance);
    x_monitor_delete(mon);
  }
  else {
    woempa(1, "Instance %p has no monitor\n", instance);
  }

  ht_unlock(lock_hashtable);

  // Note: we release the monitor outside of the lock, to avoid deadlocks.
  if (mon) {
    releaseMem(mon);
  }
}

/*
** getMonitor: either returns the existing monitor or allocates one.
*/

x_monitor getMonitor(w_instance instance) {
  if (isSet(instance2flags(instance), O_HAS_LOCK)) {
    return (x_monitor) ht_read(lock_hashtable, (w_word) instance);
  }
  else {
    return allocMonitor(instance);
  }
}

/*
** enterMonitor: called to acquire a lock on an object to which a w_Lock
** structure may or may not already be attached.
*/

void enterMonitor(w_instance instance) {

  x_status status;
  x_monitor mon;

  if (isSet(instance2flags(instance), O_HAS_LOCK)) {
    mon = (x_monitor) ht_read(lock_hashtable, (w_word)instance);
  }
  else {
    mon = allocMonitor(instance);
  }

  woempa(1, "Instance %j : monitor is %p\n", instance, mon);
  status = x_monitor_eternal(mon);
  if (status != xs_success) {
    woempa(9, "x_monitor_eternal returned %d\n",status);
    throwException(currentWonkaThread, clazzInternalError, "x_monitor_eternal = '%s'", x_status2char(status));
  }
  else {
    woempa(1, "%w entered monitor of %p\n", currentWonkaThread->name, instance);
  }

}

/*
** exitMonitor: called to relinquish a lock.
*/ 

void exitMonitor(w_instance instance) {

  x_status status;
  x_monitor mon;
  w_thread  thread = currentWonkaThread;

  mon = (x_monitor) ht_read(lock_hashtable, (w_word)instance);

  if (mon) {
    woempa(1, "Instance %p : monitor is %p\n", instance, mon);
    status = x_monitor_exit(mon);
    woempa(1, "%w left monitor of %p\n", thread->name, instance);
    if (status == xs_not_owner) {
      throwException(thread, clazzIllegalMonitorStateException, "not owner");
    }
    else if (status) {
      woempa(9, "x_monitor_exit returned %d\n",status);
      throwException(thread, clazzInternalError, "x_monitor_exit = '%s'", x_status2char(status));
    }
  }
  else {
    woempa(9, "Instance %p : monitor is %p\n", instance, mon);
    throwException(thread, clazzIllegalMonitorStateException, "no lock");
  }

}

/*
** Wait on a monitor.
*/

void waitMonitor(w_instance instance, x_sleep timeout) {
  x_status status;
  x_monitor mon;
  w_thread  thread = currentWonkaThread;
  wt_state  old_state = threadState(thread);

  mon = (x_monitor)ht_read(lock_hashtable, (w_word)instance);

  if (mon) {
    woempa(1, "Instance %p : monitor is %p\n", instance, mon);
    thread->state = wt_waiting;

    status = x_monitor_wait(mon, timeout);
    thread->state = old_state;
    woempa(1, "%w waited on %p\n", thread->name, instance);
    if (status == xs_not_owner) {
      throwException(thread, clazzIllegalMonitorStateException, "not owner");
    }
    else if (status == xs_interrupted) {
      status = x_monitor_eternal(mon);
      woempa(7, "re-entered monitor: x_monitor_eternal returned %d\n",status);
    }
    else if (status) {
      woempa(9, "x_monitor_wait returned %s\n",x_status2char(status));
      throwException(thread, clazzInternalError, "x_monitor_wait() returned %s", x_status2char(status));
    }
  }
  else {
    woempa(9, "Instance %p : monitor is %p\n", instance, mon);
    throwException(thread, clazzIllegalMonitorStateException, "no lock");
  }
}

/*
** Notify thread(s) waiting on a monitor.
*/

void notifyMonitor(w_instance instance, int notifyAll) {
  x_status status;
  x_monitor mon;
  w_thread  thread = currentWonkaThread;

  mon = (x_monitor)ht_read(lock_hashtable, (w_word)instance);

  woempa(1, "Instance %p : monitor is %p\n", instance, mon);
  if (notifyAll) {
    status = x_monitor_notify_all(mon);
    woempa(1, "%w notified all waiting on %p\n", thread->name, instance);
    if (status == xs_not_owner) {
      throwException(thread, clazzIllegalMonitorStateException, "not owner");
    }
    else if (status) {
      woempa(9, "x_monitor_notify_all returned %d\n",status);
      throwException(thread, clazzInternalError, "x_monitor_notify_all");
    }
  }
  else { // single notify
    status = x_monitor_notify(mon);
    woempa(1, "Notified one waiting on %p\n", instance);
    if (status == xs_not_owner) {
      throwException(thread, clazzIllegalMonitorStateException, "not owner");
    }
    else if (status) {
      woempa(9, "x_monitor_exit returned %d\n",status);
      throwException(thread, clazzInternalError, "x_monitor_exit");
    }
  }
}

w_thread monitorOwner(w_instance instance) {
  x_monitor mon = NULL;

  if (isSet(instance2flags(instance), O_HAS_LOCK)) {
    mon = (x_monitor) ht_read(lock_hashtable, (w_word)instance);
  }

  // This is a bit naughty, because it assumes something about the internal
  // structure of x_monitor and x_thread. It works for both O4P and OSwald,
  // but really we should add some macros such as x_monitor_owner and
  // x_thread_xref to the OSwald API.
  if (!mon || !mon->owner) {

    return NULL;

  }

  return mon->owner->xref;
}

