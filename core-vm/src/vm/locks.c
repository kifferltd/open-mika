/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved.                                                               *
* Parts copyright (c) 2004, 2005, 2006, 2007, 2010 by Chris Gray,         *
* /k/ Embedded Java Solutions. All rights reserved.                       *
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

#include <string.h>

#include "clazz.h"
#include "core-classes.h"
#include "exception.h"
#include "hashtable.h"
#include "heap.h"
#include "locks.h"
#include "ts-mem.h"
#include "mika_threads.h"
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

    woempa(1, "created new lock %p for instance %p.\n", mon, instance);
    ht_write_no_lock(lock_hashtable, (w_word)instance, (w_word)mon);
    setFlag(instance2flags(instance), O_HAS_LOCK);
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
    else if (status != xs_success && status != xs_interrupted) {
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

