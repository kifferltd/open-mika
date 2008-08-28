/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2004, 2008 by Chris Gray, /k/ Embedded Java         *
* Solutions. All rights reserved.                                         *
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

static jclass   class_Thread;
static jmethodID run_method;

extern const char *dumpThread(x_thread);

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
  w_boolean gc_is_running = (gc_instance != NULL);
  x_status monitor_status;

#ifdef DEBUG_STACKS
  thread->native_stack_base = &thread;
  thread->native_stack_max_depth = 0;
#endif

  if (!run_method) {
    class_Thread = clazz2Class(clazzThread);
    run_method = (*env)->GetMethodID(env, class_Thread, "_run", "()V"); 
    woempa(7,"run_method is %M\n",run_method);
#ifdef JDWP
    jdwp_Thread_run_method = (*env)->GetMethodID(env, class_Thread, "_run", "()V"); 
#endif
  }
  if (isSet(verbose_flags, VERBOSE_FLAG_THREAD)) {
#ifdef O4P
   wprintf("Start %t: pid is %d\n", thread, getpid());
#else
   wprintf("Start %t\n", thread);
#endif
  }
#ifdef JDWP
  jdwp_event_thread_start(thread);
#endif
  thread->state = wt_ready;
  callMethod(thread->top, run_method);
  thread->top = & thread->rootFrame;
  if (exceptionThrown(thread) && isSet(verbose_flags, VERBOSE_FLAG_THROW)) {
    wprintf("Uncaught exception in %t: %e, is someone calling stop()?\n", thread, exceptionThrown(thread));
  }

  removeThreadCount(thread);
  thread->state = wt_dying;
#ifdef JDWP
  jdwp_event_thread_end(thread);
#endif
  /*
  ** Don't futz with the thread_hashtable etc. while prepare/mark is in progress
  */
  if (gc_is_running) {
    monitor_status = x_monitor_eternal(gc_monitor);
    if (monitor_status != xs_success) {
      wabort(ABORT_WONKA, "Unable to enter gc_monitor!\n");
    }
    while (gc_phase == GC_PHASE_PREPARE || gc_phase == GC_PHASE_MARK) {
      monitor_status = x_monitor_wait(gc_monitor, 10);
      if (monitor_status == xs_interrupted) {
        monitor_status = x_monitor_eternal(gc_monitor);
      }
    }
  }

  ht_erase(thread_hashtable,(w_word)thread->kthread);
  thread->state = wt_dead;
  if (gc_is_running) {
    x_monitor_exit(gc_monitor);
  }
  deleteGlobalReference(thread->Thread);
  if (isSet(verbose_flags, VERBOSE_FLAG_THREAD)) {
#ifdef O4P
   wprintf("Finish %t: pid was %d\n", thread, getpid());
#else
   wprintf("Finish %t\n", thread);
#endif
  }

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

  woempa(1, "Creating thread '%w' for group %j, is%s the system group.\n", name, parentThreadGroup, parentThreadGroup == I_ThreadGroup_system ? "" : " not");
  newthread = createThread(currentthread, thisThread, parentThreadGroup, name, stacksize);
  if (!newthread) {

    return;

  }

 newthread->state = wt_unstarted;

}

void Thread_destructor(w_instance thisThread) {

  w_thread thread = getWotsitField(thisThread, F_Thread_wotsit);

  if (thread) {
    thread->Thread = NULL;
    woempa(7, "Destroying %t\n", thread);
    if (isNotSet(thread->flags, WT_THREAD_IS_NATIVE)) {
      terminateThread(thread);
      clearWotsitField(thisThread, F_Thread_wotsit);
    }
  }

}

w_instance Thread_getName(JNIEnv *env, w_instance thisThread) {

  w_thread thread = getWotsitField(thisThread, F_Thread_wotsit);

  return newStringInstance(thread->name);

}

void Thread_setName0(JNIEnv *env, w_instance thisThread, w_instance nameString) {

  w_thread thread = getWotsitField(thisThread, F_Thread_wotsit);
  w_string oldname = thread->name;
  w_string newname = String2string(nameString);

  registerString(newname);
  deregisterString(oldname);

  thread->name = newname;

}

w_int Thread_start0(JNIEnv *env, w_instance thisThread) {
  w_thread current_thread = JNIEnv2w_thread(env);
  w_thread this_thread = getWotsitField(thisThread, F_Thread_wotsit);
  w_thread oldthread;
  x_status status;
  w_int    result = 0;
  w_boolean gc_is_running = (gc_instance != NULL);
  x_status monitor_status;

/* [CG 20050601]
 * For O4P we let the system supply the stack, since LinuxThreads ignores the
 * one we supply anyway. (NetBSD probably does the Right Thing, but whatever ...)
 */
#ifndef O4P
  this_thread->kstack = allocMem(this_thread->ksize);
  if (!this_thread->kstack) {

    return -1;

  }
#endif

  threadMustBeSafe(current_thread);

  this_thread->state = wt_starting;
  enterUnsafeRegion(current_thread);
  oldthread = (w_thread)ht_write(thread_hashtable, (w_word)this_thread->kthread, (w_word)this_thread);

  if (oldthread) {
    wabort(ABORT_WONKA, "Sapristi! that os thread %p was already registered!\n", this_thread->kthread);
  }

  x_thread_create(this_thread->kthread, threadEntry, this_thread, this_thread->kstack, this_thread->ksize, this_thread->kpriority, TF_SUSPENDED);
  this_thread->kthread->report = running_thread_report;

  woempa(7, "Starting Java Thread %t.\n", this_thread);

  newGlobalReference(thisThread);
  addThreadCount(this_thread);
  enterSafeRegion(current_thread);
  status = x_thread_resume(this_thread->kthread);
  if (status == xs_success) {
    if(jpda_hooks) {
      jdwp_event_thread_start(this_thread);
    }
  }
  else {
    result = status;
  }

  return result;

}

void Thread_stop0(JNIEnv *env, w_instance thisThread, w_instance Throwable) {

  w_thread thread = (w_thread) thisThread[F_Thread_wotsit];

  thread = getWotsitField(thisThread, F_Thread_wotsit);
  if (!threadIsActive(thread)) {
    woempa(7, "Thread %t is already dying, ignoring %e\n", thread, Throwable);
    return;
  }

  throwExceptionInstance(thread, Throwable);

  if(jpda_hooks) {
    jdwp_event_thread_end(thread);
  }
  
  /*
  ** [CG 20040102] This too ...
  */

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


w_int Thread_getPriority(JNIEnv *env, w_instance thisThread) {

  w_thread thread;

  thread = getWotsitField(thisThread, F_Thread_wotsit);

  return thread->jpriority;

}

void Thread_setPriority0(JNIEnv *env, w_instance thisThread, w_int newPriority) {

  w_thread thread = getWotsitField(thisThread, F_Thread_wotsit);

  woempa(1,"requested priority is %d\n",newPriority);

  thread->jpriority = newPriority;

  thread->kpriority = priority_j2k(thread->jpriority,0);
  if (threadIsActive(thread)) {
   x_thread_priority_set(thread->kthread, thread->kpriority);
  }
}

void Thread_setDaemon0(JNIEnv *env, w_instance thisThread, w_boolean on) {

  w_thread thread = getWotsitField(thisThread, F_Thread_wotsit);

  thread->isDaemon = on;

}

w_boolean Thread_isInterrupted(JNIEnv *env, w_instance thisThread) {

  w_thread thread = getWotsitField(thisThread, F_Thread_wotsit);
  w_boolean interrupted = isSet(thread->flags, WT_THREAD_INTERRUPTED);

  if (isSet(verbose_flags, VERBOSE_FLAG_THREAD)) {
    wprintf("Thread.isInterrupted(): %t has %sbeen interrupted\n", thread, interrupted ? "" : "not ");
  }

  return interrupted;

}

void Thread_interrupt(JNIEnv *env, w_instance thisThread) {

  w_thread thread = getWotsitField(thisThread, F_Thread_wotsit);
  x_status status;

  woempa(1, "thread %t is interrupting %t\n", JNIEnv2w_thread(env), thread);
  if (isSet(verbose_flags, VERBOSE_FLAG_THREAD)) {
    wprintf("Thread.interrupt(): %t is interrupting %t\n", currentWonkaThread, thread);
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
      wprintf("Thread.interrupted(): %t has been interrupted, clearing flag\n", thread);
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

static w_boolean checkForInterrupt(w_thread thread) {
  if (isSet(thread->flags, WT_THREAD_INTERRUPTED)) {
    unsetFlag(thread->flags, WT_THREAD_INTERRUPTED);
    throwException(thread, clazzInterruptedException, NULL);
    woempa(6, "THROWING an InterruptedException\n");

    return TRUE;

  }

  return FALSE;
}

#define ONE_MINUTE_MICROS 60000000LL
#define ONE_MINUTE_TICKS (x_usecs2ticks(60000000))

void Thread_sleep0(JNIEnv *env, w_instance Thread, w_long millis, w_int nanos) {

  w_thread thread = getWotsitField(Thread, F_Thread_wotsit);
  volatile w_long micros = 0;

  if (millis < 0 || nanos < 0 || nanos >= 1000000) {
    throwException(thread, clazzIllegalArgumentException, NULL);
    return;
  }

  micros = millis ? (millis * 1000) + (nanos / 1000) : 1000 ;
  
  woempa(1, "thread will go to sleep!!! %t\n", thread);

  if (checkForInterrupt(thread)) {
    if (isSet(verbose_flags, VERBOSE_FLAG_THREAD)) {
      wprintf("Thread.sleep(): %t has been interrupted before sleep()\n", thread);
    }

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
  while (micros > ONE_MINUTE_MICROS) {
    x_thread_sleep(ONE_MINUTE_TICKS);
    micros -= ONE_MINUTE_MICROS;
    if (checkForInterrupt(thread)) {
      if (isSet(verbose_flags, VERBOSE_FLAG_THREAD)) {
        wprintf("Thread.sleep(): %t has been interrupted during sleep()\n", thread);
      }
      thread->state = wt_ready;

      return;
    }
  }

  x_thread_sleep(x_usecs2ticks(micros));
  thread->state = wt_ready;
  if (checkForInterrupt(thread)) {
    if (isSet(verbose_flags, VERBOSE_FLAG_THREAD)) {
      wprintf("Thread.sleep(): %t has been interrupted during sleep()\n", thread);
    }
  }
}

w_boolean Thread_static_holdsLock(JNIEnv *env, w_instance classThread, w_instance instance) {
  w_thread thread = JNIEnv2w_thread(env);

  if (!instance) {
    throwException(thread, clazzNullPointerException, NULL);

    return FALSE;
  }

  return monitorOwner(instance) == thread;
}
