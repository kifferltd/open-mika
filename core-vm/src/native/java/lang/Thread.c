/**************************************************************************
* Copyright (c) 2004, 2008, 2010, 2011, 2012, 2014 by Chris Gray,         *
* KIFFER Ltd. All rights reserved.                                        *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS *
* OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)   *
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,     *
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING   *
* IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE      *
* POSSIBILITY OF SUCH DAMAGE.                                             *
**************************************************************************/

#include <string.h>
#include <sys/syscall.h>

#include "core-classes.h"
#include "debug.h"
#include "exception.h"
#include "jdwp.h"
#include "jdwp_events.h"
#include "locks.h"
#include "threads.h"
#include "wstrings.h"

#ifdef JDWP
extern void jdwp_event_thread_start(w_thread);
extern void jdwp_event_thread_end(w_thread);
extern w_method jdwp_Thread_run_method;
#endif

#ifdef ENABLE_THREAD_RECYCLING
extern x_monitor xthreads_monitor;
#endif

static jclass   class_Thread;
static jmethodID run_method;

extern const char *dumpThread(x_thread);

static volatile w_int numberOfThreadInstances;

w_instance Thread_currentThread(JNIEnv *env, w_instance ThreadClass) {

  woempa(1, "Current Thread instance is %p.\n", JNIEnv2Thread(env));
  
  return JNIEnv2Thread(env);

}

static w_int seqnum = 0;

static void threadEntry(void * athread) {

#ifdef DEBUG_STACKS
  volatile
#endif
  w_thread thread = athread;
  JNIEnv  *env = w_thread2JNIEnv(thread);
  volatile w_boolean gc_is_running;
  w_thread oldthread;
  x_status monitor_status;
  x_thread kthread = thread->kthread;

#ifdef DEBUG_STACKS
  thread->native_stack_base = &thread;
  thread->native_stack_max_depth = 0;
#endif

  threadMustBeSafe(thread);
  if (!run_method) {
    class_Thread = clazz2Class(clazzThread);
    run_method = (*env)->GetMethodID(env, class_Thread, "_run", "()V"); 
    woempa(7,"run_method is %M\n",run_method);
#ifdef JDWP
    jdwp_Thread_run_method = (*env)->GetMethodID(env, class_Thread, "_run", "()V"); 
#endif
  }

#ifdef ENABLE_THREAD_RECYCLING
  // TODO - we give each thread a lifetime of sqrt(N + 20), where n is the number of 
  // instances of Thread currently existing.  Anyone have a better idea?
  int lifetime = (numberOfThreadInstances + 20) / 2;
  lifetime = (lifetime + (numberOfThreadInstances + 20) / lifetime) / 2;
  lifetime = (lifetime + (numberOfThreadInstances + 20) / lifetime) / 2;
  if (isSet(verbose_flags, VERBOSE_FLAG_THREAD)) {
    w_printf("there are %d Thread instances outstanding, allocating lifetime %d to kthread %p\n", numberOfThreadInstances, lifetime, kthread);
  }
  setLifetime(kthread, lifetime);
  while (getLifetime(kthread)) {
#endif
    if (isSet(verbose_flags, VERBOSE_FLAG_THREAD)) {
      w_printf("starting %t using kthread %p\n", thread, kthread);
    }
    if(jpda_hooks) {
      jdwp_event_thread_start(thread);
    }
    oldthread = (w_thread)ht_write(thread_hashtable, (w_word)kthread, (w_word)thread);

#ifdef RUNTIME_CHECKS
    if (oldthread) {
      wabort(ABORT_WONKA, "Sapristi! that os thread %p was already registered!\n", kthread);
    }
#endif

#ifndef O4P
#error TODO: This code needs to be modified for other host OSs
#endif
    w_int pid = getpid();
    w_int tid = syscall(__NR_gettid);
#ifdef RESMON
    setIntegerField(thread->Thread, F_Thread_pid, pid);
    setIntegerField(thread->Thread, F_Thread_tid, tid);
#endif
    if (isSet(verbose_flags, VERBOSE_FLAG_THREAD)) {
#ifdef O4P
      w_printf("Start %t: pid is %d lwp %d\n", thread, pid, tid);
#else
      w_printf("Start %t\n", thread);
#endif
    }
#ifdef JDWP
    jdwp_event_thread_start(thread);
#endif
    thread->state = wt_ready;
    callMethod(thread->top, run_method);
    thread->top = & thread->rootFrame;
    if (exceptionThrown(thread) && isSet(verbose_flags, VERBOSE_FLAG_THROW)) {
      w_printf("Uncaught exception in %t: %e, is someone calling stop()?\n", thread, exceptionThrown(thread));
    }

    removeThreadCount(thread);
    thread->state = wt_dying;
    if (isSet(verbose_flags, VERBOSE_FLAG_THREAD)) {
      w_printf("finished %t using kthread %p\n", thread, kthread);
    }

#ifdef JDWP
    jdwp_event_thread_end(thread);
#endif
    enterUnsafeRegion(thread);

    pre_thread_termination(thread);
    ht_erase(thread_hashtable,(w_word)thread->kthread);
    thread->state = wt_dead;
    if (isSet(verbose_flags, VERBOSE_FLAG_THREAD)) {
#ifdef O4P
      w_printf("Finish %t: pid was %d lwp %d\n", thread, getpid(), syscall(__NR_gettid));
#else
      w_printf("Finish %t\n", thread);
#endif
    }
    deleteGlobalReference(thread->Thread);
    enterSafeRegion(thread);

#ifdef ENABLE_THREAD_RECYCLING
    x_monitor_eternal(xthreads_monitor);
    woempa(7, "%t ->kthread was %p, setting to NULL\n", thread, kthread);
    kthread->xref = NULL;
    if (thread->Thread) {
      thread->Thread = NULL;
    }

    useLifetime(kthread);
    if (isSet(verbose_flags, VERBOSE_FLAG_THREAD)) {
      w_printf("kthread %p has %d lives remaining\n", kthread, getLifetime(kthread));
    }
    if (getLifetime(kthread)) {
      // detach the kthread from the thread. Note that if the kthread is EOL then we leave
      // the link in place so that Thread_destructor can perform clean-up.
      thread->kthread = NULL;
      thread = NULL;

      putFifo(kthread, xthread_fifo);
      if (isSet(verbose_flags, VERBOSE_FLAG_THREAD)) {
        w_printf("added kthread %p to native thread pool, now contains %d kthreads\n", kthread, occupancyOfFifo(xthread_fifo));
      }
      while (!(thread = kthread->xref)) {
        x_monitor_wait(xthreads_monitor, x_eternal);
      }
    }
    x_monitor_exit(xthreads_monitor);
  }
#endif
}

void Thread_create(JNIEnv *env, w_instance thisThread, w_instance parentThreadGroup, w_instance nameString, w_instance theRunnable) {

  char *   buffer;
  w_string name;
  w_thread currentthread = JNIEnv2w_thread(env);
  w_thread newthread;
  w_clazz  runnable_clazz;
  w_size   stacksize;

  runnable_clazz = instance2clazz(theRunnable);
  if (runnable_clazz == clazzGarbageCollector) {
    stacksize = gc_stack_size;
  }
  else if (runnable_clazz == clazzInit) {
    stacksize = init_stack_size;
  }
  else {
    stacksize = default_stack_size;
  }

  woempa(1, "parentThreadGroup is %p.\n", parentThreadGroup);
  woempa(9, "Runnable is a %k, stack size will be %d\n", runnable_clazz, stacksize);

  if (!nameString) {
    buffer = allocMem(THREAD_NAME_BUFFER_SIZE * sizeof(w_byte));
    if (!buffer) {

      return;

    }

    x_snprintf(buffer, THREAD_NAME_BUFFER_SIZE, "Thread-%d", ++seqnum);
    name = cstring2String(buffer, strlen(buffer));
    releaseMem(buffer);
  } 
  else {
    name = String2string(nameString);
    registerString(name);
  }

  woempa(1, "Reclaiming 256 KB so that launching many threads => performing much GC\n");
  gc_reclaim(262144, NULL);
  woempa(1, "Creating thread '%w' for group %j, is%s the system group.\n", name, parentThreadGroup, parentThreadGroup == I_ThreadGroup_system ? "" : " not");
  newthread = createThread(currentthread, thisThread, parentThreadGroup, name, stacksize);
  if (!newthread) {

    return;

  }

  ++numberOfThreadInstances;
  newthread->state = wt_unstarted;

}

void cleanUpNativeThread(x_thread kthread) {

  x_status status = xs_success;
  void * result;

  if (isSet(verbose_flags, VERBOSE_FLAG_THREAD)) {
    w_printf("cleaning up kthread %p\n", kthread);
  }
  status = x_thread_join(kthread, &result, 100);

  if (status == xs_success || status == xs_no_instance) {
    woempa(1, "Join status %s\n", x_status2char(status));
  }
// [CG 20140201 I don't think we ever see xs_bad_state]
//  else if (status == xs_bad_state) {
//    // Something else trying to join this thread, but it's always our job
//    // to clean up. Just wait a bit and carry on.
//    while (status == xs_bad_state) {
//      w_printf("Join status %s - waiting 1 sec and try again\n", x_status2char(status));
//      x_thread_sleep(x_millis2ticks(1000));
//      status = x_thread_join(kthread, &result, 100);
//    }
//  }
  else {
    wabort(ABORT_WONKA, "Join status %s\n", x_status2char(status));
  }

  status = x_thread_delete(kthread);
  if (status != xs_success) {
    wabort(ABORT_WONKA, "Thread delete status %s\n", x_status2char(status));
  }
 
  kthread->xref = NULL;
  kthread->report = dying_thread_report;
}

void Thread_destructor(w_instance thisThread) {
  w_thread thread;

  if (numberOfThreadInstances > 0) {
    --numberOfThreadInstances;
  }
#ifdef ENABLE_THREAD_RECYCLING
  x_monitor_eternal(xthreads_monitor);
#endif
  thread = getWotsitField(thisThread, F_Thread_wotsit);

  if (thread) {
    thread->Thread = NULL;
    woempa(7, "Destroying %t\n", thread);
    if (isNotSet(thread->flags, WT_THREAD_IS_NATIVE)) {
  w_string name;
  x_thread kthread = thread->kthread;

  if (kthread) {
    cleanUpNativeThread(kthread);
  }
  woempa(1,"Cleaned up %t\n", thread);

  if (kthread == &ur_thread_x_Thread) {
    woempa(9,"This is the ur-thread, so I won't releaseMem memory that wasn't allocMem'd.\n");
  }
  else {
    if (kthread) {
      releaseMem(kthread);
      thread->kthread = NULL;
    }  
    if (thread->kstack) {
      releaseMem(thread->kstack);
      thread->kstack = NULL;
    }  
  }

  name = thread->name;
  if (name) {
    deregisterString(name);
    thread->name = NULL;
  }
  // TODO: delete mutex ???

  releaseMem(thread);
      clearWotsitField(thisThread, F_Thread_wotsit);
    }
  }
#ifdef ENABLE_THREAD_RECYCLING
  x_monitor_exit(xthreads_monitor);
#endif

}

void Thread_setName0(JNIEnv *env, w_instance thisThread, w_instance nameString) {

  w_thread thread = getWotsitField(thisThread, F_Thread_wotsit);
  if (thread) {
    w_string oldname = thread->name;
    w_string newname = String2string(nameString);

    registerString(newname);
    deregisterString(oldname);

    thread->name = newname;
  }
}

#ifdef ENABLE_THREAD_RECYCLING
w_boolean getXThreadFromPool(w_instance thisThread) {
  w_thread wthread = getWotsitField(thisThread, F_Thread_wotsit);
  w_boolean result;
  x_status monitor_status;
  x_thread kthread;

  x_monitor_eternal(xthreads_monitor);
  kthread = getFifo(xthread_fifo);
  if (!kthread) {
    x_monitor_exit(xthreads_monitor);

    return FALSE;
  }

  wthread->state = wt_starting;
  wthread->kthread = kthread;
  kthread->xref = wthread;
  kthread->report = running_thread_report;
  addThreadCount(wthread);
  if (isSet(verbose_flags, VERBOSE_FLAG_THREAD)) {
    w_printf("Binding %t to kthread %p, native thread pool now contains %d kthreads\n", wthread, kthread, occupancyOfFifo(xthread_fifo));
  }
  x_monitor_notify_all(xthreads_monitor);
  x_monitor_exit(xthreads_monitor);

  return TRUE;
}
#endif

w_int Thread_start0(JNIEnv *env, w_instance thisThread) {
  w_thread current_thread = JNIEnv2w_thread(env);
  w_thread this_thread = getWotsitField(thisThread, F_Thread_wotsit);
  w_thread oldthread;
  x_status status;
  w_int    result = 0;
  w_boolean gc_is_running = (gc_instance != NULL);
  x_status monitor_status;

#ifdef RUNTIME_CHECKS
  if (!this_thread) {
    wabort(ABORT_WONKA, "Whoa. Trying to start a Thread whose wotsit is NULL");
  }
  if (this_thread->kthread) {
    wabort(ABORT_WONKA, "Oops - %t already has a kthread attached", this_thread);
  }
  threadMustBeSafe(current_thread);
#endif

  if (!pre_thread_start_check(current_thread, thisThread)) {
    return xs_no_instance;
  }

  // Need to do this before calling getXThreadFromPool, because that function
  // can put the thread on the wthread_fifo and then we're no longer in control (the
  // thread could run to completion before we get a chance to create the 
  // global reference).
  newGlobalReference(thisThread);
  
#ifdef ENABLE_THREAD_RECYCLING
  if (!getXThreadFromPool(thisThread)) {
#else
  {
#endif
    if (isSet(verbose_flags, VERBOSE_FLAG_THREAD)) {
      w_printf("Allocating new kthread for %t\n", this_thread);
    }
    woempa(7, "Allocating new kthread for %t\n", this_thread);
    this_thread->kthread = allocClearedMem(sizeof(x_Thread));
/* [CG 20050601]
 * For O4P we let the system supply the stack, since LinuxThreads ignores the
 * one we supply anyway. (NetBSD probably does the Right Thing, but whatever ...)
 */
#ifndef O4P
    this_thread->kstack = allocMem(this_thread->ksize);
    if (!this_thread->kstack) {
      if (this_thread->kthread) {
        releaseMem(this_thread->kthread);
        this_thread->kthread = NULL;
      }
    }
#endif

    if (!this_thread->kthread) {
      woempa(9, "Unable to allocate x_Thread for %t\n", this_thread);

      return -1;
 
    }

    this_thread->kthread->xref = this_thread;
    this_thread->state = wt_starting;
    addThreadCount(this_thread);
    enterUnsafeRegion(current_thread);

    x_thread_create(this_thread->kthread, threadEntry, this_thread, this_thread->kstack, this_thread->ksize, this_thread->kpriority, TF_SUSPENDED);
    this_thread->kthread->report = running_thread_report;

    woempa(7, "Starting Java Thread %t.\n", this_thread);

    enterSafeRegion(current_thread);
    status = x_thread_resume(this_thread->kthread);
    result = status;
  }

  return result;

}

void Thread_stop0(JNIEnv *env, w_instance thisThread, w_instance Throwable) {

  w_thread thread = getWotsitField(thisThread, F_Thread_wotsit);
  if (!threadIsActive(thread)) {
    woempa(7, "Thread %t is already dying, ignoring %e\n", thread, Throwable);
    return;
  }

  throwExceptionInstance(thread, Throwable);

  if(jpda_hooks) {
    jdwp_event_thread_end(thread);
  }
  
  x_thread_wakeup(thread->kthread);
  if (threadState(thread) == wt_waiting) {
    x_thread_stop_waiting(thread->kthread);
  }
}

void Thread_suspend0(JNIEnv *env, w_instance thisThread) {
  woempa(9, "WARNING: Thread/suspend() does nothing!\n");
}

void Thread_resume0(JNIEnv *env, w_instance thisThread) {
  woempa(9, "WARNING: Thread/resume() does nothing!\n");
}


void Thread_setPriority0(JNIEnv *env, w_instance thisThread, w_int newPriority) {

  w_thread thread = getWotsitField(thisThread, F_Thread_wotsit);

  woempa(1,"requested priority is %d\n",newPriority);

  thread->jpriority = newPriority;

  thread->kpriority = priority_j2k(thread->jpriority,0);
  if (threadIsActive(thread) && thread->kthread) {
   x_thread_priority_set(thread->kthread, thread->kpriority);
  }
}

void Thread_setDaemon0(JNIEnv *env, w_instance thisThread, w_boolean on) {

  w_thread thread = getWotsitField(thisThread, F_Thread_wotsit);

  if(thread) {
    thread->isDaemon = on;
  }
}

w_boolean Thread_isInterrupted(JNIEnv *env, w_instance thisThread) {

  w_thread thread = getWotsitField(thisThread, F_Thread_wotsit);
  if(thread) {
    w_boolean interrupted = isSet(thread->flags, WT_THREAD_INTERRUPTED);

    if (isSet(verbose_flags, VERBOSE_FLAG_THREAD)) {
      w_printf("Thread.isInterrupted(): %t has %sbeen interrupted\n", thread, interrupted ? "" : "not ");
    }

    return interrupted;
  }

  return FALSE;
}

void Thread_interrupt(JNIEnv *env, w_instance thisThread) {

  w_thread thread = getWotsitField(thisThread, F_Thread_wotsit);
  x_status status;

  if (!thread) {
    return;
  }

  woempa(1, "thread %t is interrupting %t\n", JNIEnv2w_thread(env), thread);
  if (isSet(verbose_flags, VERBOSE_FLAG_THREAD)) {
    w_printf("Thread.interrupt(): %t is interrupting %t\n", currentWonkaThread, thread);
  }

  setFlag(thread->flags, WT_THREAD_INTERRUPTED);
  if (thread->state == wt_sleeping) {
    status = x_thread_wakeup(thread->kthread);
    woempa(7, "x_thread_wakeup status = %d\n", status);
    thread->state = wt_ready;
  }
  if (threadState(thread) == wt_waiting) {
    status = x_thread_stop_waiting(thread->kthread);
    woempa(7, "x_thread_stop_waiting status = %d\n", status);
  }

}

w_boolean Thread_static_interrupted(JNIEnv *env, w_instance classThread) {

  w_thread thread = JNIEnv2w_thread(env);
  w_boolean interrupted = isSet(thread->flags, WT_THREAD_INTERRUPTED);

  if (interrupted) {
    unsetFlag(thread->flags, WT_THREAD_INTERRUPTED);
    if (isSet(verbose_flags, VERBOSE_FLAG_THREAD)) {
      w_printf("Thread.interrupted(): %t has been interrupted, clearing flag\n", thread);
    }
  }
  
  return interrupted;

}

void Thread_static_yield(JNIEnv *env, w_instance ThreadClass) {

#ifdef DEBUG
  w_thread thread = JNIEnv2w_thread(env);

  woempa(1, "Yielding on %j (thread %t).\n", thread->Thread, thread);
#endif

  x_thread_yield();

}

void Thread_sleep0(JNIEnv *env, w_instance Thread, w_long millis, w_int nanos) {

  w_thread thread = getWotsitField(Thread, F_Thread_wotsit);

  if (millis < 0 || nanos < 0 || nanos >= 1000000) {
    throwException(thread,clazzIllegalArgumentException,NULL);
  }

  if (testForInterrupt(thread) || millis == 0 && (nanos / 1000) < HOST_TIMER_GRANULARITY) {
    return;
  }

  thread->state = wt_sleeping;
  /* [CG 20070509]
  ** This isn't ideal, because the time taken to go around the loop is
  ** additional to the sleep time, so we could build up a cumulative
  ** excess of somnolence. However I don't think it's worth it to try
  ** to read the system clock and perform arithmetic on it.
  ** All of this because x_sleep is 32 bits instead of 64 ...
  */
  while (millis > THREE_WEEKS_MILLIS) {
    x_thread_sleep(THREE_WEEKS_TICKS);
    millis -= THREE_WEEKS_MILLIS;

    if (testForInterrupt(thread)) {
      thread->state = wt_ready;

      return;
    }
  }

  if (millis) {
    x_thread_sleep(x_millis2ticks((x_size)millis));
  }
  else if (nanos) {
    x_thread_sleep(x_usecs2ticks(nanos / 1000));
  }
  thread->state = wt_ready;
  testForInterrupt(thread);
}

w_boolean Thread_static_holdsLock(JNIEnv *env, w_instance classThread, w_instance instance) {
  w_thread thread = JNIEnv2w_thread(env);

  if (!instance) {
    throwException(thread, clazzNullPointerException, NULL);

    return FALSE;
  }

  return monitorOwner(instance) == thread;
}

