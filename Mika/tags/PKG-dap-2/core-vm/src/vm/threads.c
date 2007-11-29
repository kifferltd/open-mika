/**************************************************************************
* Copyright (c) 2001, 2002, 2002, 2003 by Acunia N.V. All rights reserved.*
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
*                                                                         *
* Mika(TM) modifications Copyright (C) 2004, 2006 Chris Gray,             *
* /k/ Embedded Java Solutions.  All rights reserved.                      *
*                                                                         *
**************************************************************************/

/*
** $Id: threads.c,v 1.33 2006/10/04 14:24:17 cvsroot Exp $
*/

#include <string.h>

#include "arrays.h"
#include "core-classes.h"
#include "exception.h"
#include "heap.h"
#include "methods.h"
#include "threads.h"
#include "wstrings.h"

w_boolean haveWonkaThreads = WONKA_FALSE;

w_hashtable thread_hashtable;

w_int nondaemon_thread_count;
w_boolean system_init_thread_started;

volatile w_int blocking_all_threads;

/*
** Statics and globals
*/

const unsigned char state2liveness[] = {
  0, /* 0: Unstarted      */
  1, /* 1: Ready          */
  1, /* 2: Locking        */
  1, /* 3: Waiting        */
  0, /* 4: Not used       */
  1, /* 5: Suspended      */
  0, /* 6: Not used       */
  0, /* 7: Dying          */
  0, /* 8: Dead           */
  0, /* 9: It's dead, Jim */
};

/*
** The internal Wonka representation of the system group and thread
** and the static slots for java/lang/Thread and java/lang/ThreadGroup.
*/

w_instance I_Thread_sysInit;
w_instance I_ThreadGroup_system;

w_thread W_Thread_sysInit;
w_thread W_Thread_system;

jclass   class_ThreadGroup;

static x_Mutex idLock;

/*
** Hashtable relating host OS thread control block to our w_Thread structure.
**    key:   host OS thread id, 
**    value: address of our w_Thread structure
*/

w_hashtable thread_hashtable;

#define THREAD_HASHTABLE_SIZE 51

static const char *unborn_thread_report(x_thread);
static const char *dying_thread_report(x_thread);

#define SYSTEM_STACK_SIZE 65536

/* [CG 20050601]
 * For O4P we let the system supply the stack, since LinuxThreads ignores the
 * one we supply anyway. (NetBSD probably does the Right Thing, but whatever ...)
 */
#ifdef O4P
#define ur_thread_stack NULL
#else
static char ur_thread_stack[SYSTEM_STACK_SIZE];
#endif
static x_Thread ur_thread_x_Thread;

/*
** Allocate and clear out the necessary fields for a new thread structure.
*/

void setUpRootFrame(w_thread new) {

  new->rootFrame.previous = NULL;
  new->rootFrame.method = NULL;
  new->exception = NULL;
  new->rootFrame.thread = new;
  new->rootFrame.jstack_base = new->slots;
  new->rootFrame.jstack_top = new->rootFrame.jstack_base;
  new->rootFrame.auxstack_base = last_slot(new);
  new->rootFrame.auxstack_top = new->rootFrame.auxstack_base;
  new->top = & new->rootFrame;
}

//  Java priority -------->    0   1   2   3   4   5   6   7   8   9  10
static w_int java2kprio[] = { 50, 48, 46, 44, 42, 40, 38, 36, 34, 32, 30};

w_int priority_j2k(w_int java_prio, w_int trim) {

  java_prio = (java_prio > 10) ? 10 : java_prio;
  java_prio = (java_prio <  0) ?  0 : java_prio;

  return java2kprio[java_prio];
    
}

w_thread createThread(w_thread parentthread, w_instance Thread, w_instance parentThreadGroup, w_string name, w_size stacksize) {

  w_instance Name;
  w_thread newthread;
  
  newthread = allocClearedMem(sizeof(w_Thread));

  if (!newthread) {
    woempa(9, "Unable to allocate w_Thread for %w\n", name);

    return NULL;

  }

  setUpRootFrame(newthread);
  newthread->natenv = &w_JNINativeInterface;
  newthread->label = (char *)"thread";
  newthread->name = name;
  newthread->Thread = Thread;
  newthread->kthread = allocClearedMem(sizeof(x_Thread));
  if (!newthread->kthread) {
    woempa(9, "Unable to allocate x_Thread for %w\n", name);
    releaseMem(newthread);

    return NULL;

  }

  newthread->kthread->xref = newthread;
  newthread->ksize = stacksize;

  if (!parentthread->jpriority) {
    newthread->jpriority = 5;
  }
  else {
    newthread->jpriority = parentthread->jpriority;
  }
  newthread->kpriority = priority_j2k(newthread->jpriority,0);
  newthread->isDaemon = parentthread->isDaemon;

  newthread->top->jstack_top[0].c = (w_word) Thread;
  newthread->top->jstack_top[0].s = stack_trace;
  newthread->top->jstack_top += 1;
  newthread->state = wt_unstarted;
  setWotsitField(Thread, F_Thread_wotsit,  newthread);
  Name = newStringInstance(name);
  if (Name) {
    setReferenceField(Thread, Name, F_Thread_name);
  }
  removeLocalReference(parentthread, Name);

  return newthread;

}

void terminateThread(w_thread thread) {

  void * result;
  x_status status;
  w_string name;

  status = x_thread_join(thread->kthread, &result, 100);
  if (status == xs_success || status == xs_no_instance) {
    woempa(1, "Join status %s\n", x_status2char(status));
  }
//#ifdef O4P
  else if (status == xs_bad_state) {
    // Something else trying to join this thread, but it's always our job
    // to clean up. Just wait a bit and carry on.
    while (status == xs_bad_state) {
      wprintf("Join status %s - waiting 1 sec and try again\n", x_status2char(status));
      x_thread_sleep(x_millis2ticks(1000));
      status = x_thread_join(thread->kthread, &result, 100);
    }
  }
//#endif
  else {
    wabort(ABORT_WONKA, "Join status %s\n", x_status2char(status));
  }

  woempa(1,"Cleaning up %t\n", thread);
  status = x_thread_delete(thread->kthread);
  if (status == xs_success) {
    woempa(1,"Cleaned up %t\n", thread);

    thread->kthread->xref = NULL;
    thread->kthread->report = dying_thread_report;

    if (thread->kthread == &ur_thread_x_Thread) {
      woempa(9,"This is the ur-thread, so I won't releaseMem memory that wasn't allocMem'd.\n");
    }
    else {
      if (thread->kthread) {
        releaseMem(thread->kthread);
	thread->kthread = NULL;
      }  
      if (thread->kstack) {
        releaseMem(thread->kstack);
	thread->kstack = NULL;
      }  
      name = thread->name;
      if (name) {
        deregisterString(name);
	thread->name = NULL;
      }
      // TODO: delete mutex
    }
    //

  }
  else {
    wabort(ABORT_WONKA, "Thread delete status %s\n", x_status2char(status));
  }
  releaseMem(thread);

}

void addThreadToGroup(w_thread thread, w_instance parentThreadGroup) {
#ifdef RUNTIME_CHECKS
  w_thread calling_thread = currentWonkaThread;

  threadMustBeSafe(calling_thread);
#endif

  /*
  ** Do the accounting of the thread use
  */

  if (!thread->isDaemon) {
    nondaemon_thread_count += 1;
    woempa(7, "Adding non-daemon thread %w, total now %d\n", thread->name, nondaemon_thread_count);
  }
  ++parentThreadGroup[F_ThreadGroup_totalCount];
}

void removeThreadFromGroup(w_thread thread, w_instance parentThreadGroup) {
#ifdef RUNTIME_CHECKS
  w_thread calling_thread = currentWonkaThread;

  threadMustBeSafe(calling_thread);
#endif

  if (!thread->isDaemon) {
    --nondaemon_thread_count;
    woempa(1, "Removed non-daemon thread %w, total now %d\n", thread->name, nondaemon_thread_count);
  }
  --parentThreadGroup[F_ThreadGroup_totalCount];
}

/*
** Note [CG 20031112] :
** don't use woempa() or wabort() in here, 'coz they call currentWonkaThread ...
*/
w_thread _currentWonkaThread(const char *f, int l) {
  x_thread kthread;
  w_thread  thread;

  if (!haveWonkaThreads) {

    return NULL;

  }

  kthread = x_thread_current();

  if(!kthread) {
    return NULL; /* Fix for crash on O4P. */
  }

  thread = (w_thread)kthread->xref;
  
  if (!thread) {
    return NULL; /* MB: Thread can't have been set up yet */
  }

  if (*((w_word*)thread->label)!=*((w_word*)"thread")  
     && *((w_word*)thread->label)!=*((w_word*)"group")){
    return NULL; /* CG: thread structure freed or corrupt??? */
  }

  return thread;
}

static char thread_report_buffer[92];

static const char *unborn_thread_report(x_thread x) {
  return "Thread not yet started";
}

static const char *dying_thread_report(x_thread x) {
  return "Thread returning to Saturn";
}

const char *running_thread_report(x_thread x) {

  w_thread t;

  if (x) {
    t = x->xref;
    x_snprintf(thread_report_buffer, 91, "%T", t);
    return thread_report_buffer;
  }

  return "!!! Wonka thread xref is NULL !!!";

}

void initKernel() {

  install_term_handler();
  x_mutex_create(&idLock);

  x_thread_create(&ur_thread_x_Thread, 
    startWonka, NULL, ur_thread_stack, 
    SYSTEM_STACK_SIZE, SYSTEM_GROUP_MANAGER_PRIORITY, TF_START);
}

#define INIT_CLASS "wonka.vm.Init"

extern int woempa_bytecodecount;

static w_string string_sysThread;
static w_string string_sysThreadGroup;

void startInitialThreads(void* data) {

#ifdef DEBUG_STACKS
  volatile
#endif
  w_method method = NULL;
  w_instance arglist;
  w_int dims;
  w_int    i;
  JNIEnv  *env = w_thread2JNIEnv(W_Thread_sysInit);
  jclass   class_Init;
  w_instance String;
  w_boolean unsafe;

#ifdef DEBUG_STACKS
  W_Thread_sysInit->native_stack_base = &method;
  W_Thread_sysInit->native_stack_max_depth = 0;
#endif

  woempa(9, "********* %d instances in use after initial loading. ************\n", instance_use);

#ifdef DEBUG
  wprintf("Start %t: pid is %d\n", W_Thread_sysInit, getpid());
#endif
  mustBeInitialized(clazzString);
  woempa(7, "Getting string instance of '%w', thread is '%t'\n", string_sysThreadGroup, currentWonkaThread);
  setReferenceField(I_ThreadGroup_system, newStringInstance(string_sysThreadGroup), F_ThreadGroup_name);
  woempa(7, "Getting string instance of '%w', thread is '%t'\n", string_sysThread, currentWonkaThread);
  setReferenceField(I_Thread_sysInit, newStringInstance(string_sysThread), F_Thread_name);
  setBooleanField(I_Thread_sysInit, F_Thread_started, WONKA_TRUE);

/*
** First we gather the command line arguments (if any: in the embedded case
** there cannot be any), and use the first as the name of an initial class
** to run and the rest as arguments to be passed to that class's <init>(String[])
** method.  The name goes in arglist[0] and the arguments go in arglists[1...].
*/

  if (command_line_argument_count > 0) {
    dims = command_line_argument_count;
    woempa(7,"Allocating array of %d String[s]\n",dims);
    arglist = allocArrayInstance_1d(W_Thread_sysInit, clazzArrayOf_String, dims);
    // CG 20040114 removeLocalReference(W_Thread_sysInit, arglist);

    for (i = 0; i < command_line_argument_count; i++) {
      woempa(7, "Getting string instance of '%s', thread is '%t'\n", command_line_arguments[i], currentWonkaThread);
      String = newStringInstance(cstring2String(command_line_arguments[i], strlen(command_line_arguments[i])));
      setArrayReferenceField(arglist, String, i);
      // CG 20040114 removeLocalReference(W_Thread_sysInit, String);
      woempa(9,"args[%d] = \"%w\" bytecodecount = %d\n",i,String2string(instance2Array_instance(arglist)[i]), woempa_bytecodecount);
    }
  }
  else {
    dims = 0;
    woempa(1,"Not allocating an array of String[s]\n");
    arglist = NULL;
  }

  if (bootstrap_exception) {
    wabort(ABORT_WONKA, "Bootstrapping failed: %e", bootstrap_exception);
  }

  addThreadToGroup(W_Thread_sysInit, I_ThreadGroup_system);
  system_init_thread_started = TRUE;

/*
** Now we call Init.main() with arglist as its parameter.
*/
  woempa(7, "Invoking main([Ljava.lang.String;) of %s ...\n", INIT_CLASS);

  method = (*env)->GetMethodID(env, class_ThreadGroup, "registerThread", "(Ljava/lang/Thread;)V");
  if (method==NULL) {
    wabort(ABORT_WONKA,"Uh oh: class java.lang.ThreadGroup doesn't have a method registerThread(java.lang.Thread).  Game over.\n");
  }
  (*env)->CallVoidMethod(env, I_ThreadGroup_system, method, I_Thread_sysInit);
  if ((*env)->ExceptionCheck(env)) {
    (*env)->ExceptionDescribe(env);
  }

  class_Init = (*env)->FindClass(env, INIT_CLASS);
  if (!class_Init) {
    wabort(ABORT_WONKA,"Uh oh: didn't find class " INIT_CLASS ".  Game over\n");
  }

  method = (*env)->GetStaticMethodID(env, class_Init, "main", "([Ljava/lang/String;)V");
  if (method==NULL) {
    wabort(ABORT_WONKA,"Uh oh: class " INIT_CLASS " doesn't have a method main(String[]).  Game over.\n");
  }
  (*env)->CallStaticVoidMethod(env, class_Init, method, arglist);
  if ((*env)->ExceptionCheck(env)) {
    (*env)->ExceptionDescribe(env);
  }

  method = (*env)->GetMethodID(env, class_ThreadGroup, "deregisterThread", "(Ljava/lang/Thread;)V");
  if (method==NULL) {
    wabort(ABORT_WONKA,"Uh oh: class java.lang.ThreadGroup doesn't have a method deregisterThread(java.lang.Thread).  Game over.\n");
  }
  (*env)->CallVoidMethod(env, I_ThreadGroup_system, method, I_Thread_sysInit);
  if ((*env)->ExceptionCheck(env)) {
    (*env)->ExceptionDescribe(env);
  }

  setBooleanField(I_Thread_sysInit, F_Thread_stopped, WONKA_TRUE);
  W_Thread_sysInit->top = & W_Thread_sysInit->rootFrame;
  removeThreadFromGroup(W_Thread_sysInit, thread2ThreadGroup(W_Thread_sysInit));
  W_Thread_sysInit->state = wt_dying;
  unsafe = enterUnsafeRegion(W_Thread_sysInit);
  ht_erase(thread_hashtable,(w_word)W_Thread_sysInit->kthread);
  if (!unsafe) {
    enterSafeRegion(W_Thread_sysInit);
  }
  W_Thread_sysInit->state = wt_dead;
#ifdef DEBUG
  wprintf("Finish %t: pid was %d\n", W_Thread_sysInit, getpid());
#endif
}

void startKernel() {
  nondaemon_thread_count = 0;

  thread_hashtable = ht_create((char*)"hashtable:threads", THREAD_HASHTABLE_SIZE, NULL, NULL, 0, 0);
  woempa(7, "Created thread_hashtable at %p\n",thread_hashtable);
#ifdef USE_OBJECT_HASHTABLE
  object_hashtable = ht_create((char*)"hashtable:objects", 32767, NULL, NULL, 0, 0);
  woempa(7, "Created object_hashtable at %p\n",object_hashtable);
#endif
  I_ThreadGroup_system = allocInstance(NULL, clazzThreadGroup);
  woempa(1,"created I_ThreadGroup_system at %p\n",I_ThreadGroup_system);
  string_sysThreadGroup = cstring2String("SystemThreadGroup", 17);
  I_Thread_sysInit = allocInstance(NULL, clazzThread);
  string_sysThread = cstring2String("SystemInitThread", 16);

  W_Thread_sysInit = allocClearedMem(sizeof(w_Thread));

  if (!W_Thread_sysInit) {
    wabort(ABORT_WONKA, "Couldn't allocate memory for initial thread!\n");
  }

  setUpRootFrame(W_Thread_sysInit);
  W_Thread_sysInit->natenv = &w_JNINativeInterface;
  W_Thread_sysInit->label = (char*)"thread:sysInit";
  W_Thread_sysInit->name = string_sysThread;
  W_Thread_sysInit->state = wt_unstarted;
  W_Thread_sysInit->isDaemon = WONKA_FALSE;
  W_Thread_sysInit->jpriority = USER_PRIORITY;
  W_Thread_sysInit->Thread = I_Thread_sysInit;

  setUpRootFrame(W_Thread_sysInit);

  class_ThreadGroup = clazz2Class(clazzThreadGroup);
#ifndef GC_SAFE_POINTS_USE_NO_MONITORS
  x_monitor_create(safe_points_monitor);
#endif

/* [CG 20050601]
 * For O4P we let the system supply the stack, since LinuxThreads ignores the
 * one we supply anyway. (NetBSD probably does the Right Thing, but whatever ...)
 */
#ifndef O4P
  W_Thread_sysInit->kstack = allocMem(init_stack_size);
#endif
  W_Thread_sysInit->ksize = init_stack_size;

  W_Thread_sysInit->kthread = (x_thread)allocClearedMem(sizeof(x_Thread));
  W_Thread_sysInit->kthread->xref = W_Thread_sysInit;
  W_Thread_sysInit->kthread->report = unborn_thread_report;
  W_Thread_sysInit->kpriority = priority_j2k(USER_PRIORITY,0);

  setWotsitField(I_Thread_sysInit, F_Thread_wotsit, W_Thread_sysInit);
  setReferenceField(I_Thread_sysInit, I_ThreadGroup_system, F_Thread_parent);
  woempa(7, "registering os thread %p as thread instance %p\n", W_Thread_sysInit->kthread, W_Thread_sysInit);
  ht_write(thread_hashtable, (w_word)W_Thread_sysInit->kthread, (w_word)W_Thread_sysInit);

  W_Thread_sysInit->kthread->xref = W_Thread_sysInit;
  x_thread_create(W_Thread_sysInit->kthread, startInitialThreads, 0, W_Thread_sysInit->kstack, W_Thread_sysInit->ksize, W_Thread_sysInit->kpriority, TF_SUSPENDED);
  W_Thread_sysInit->kthread->report = running_thread_report;
}

char * print_thread_short(char* buffer, int *remain, void *data, int w, int p, unsigned int f) {

  w_thread t = (w_thread) data;
  w_size   length;
  w_string thread_name;

  if (*remain < 1) {
    return buffer;
  }

  if (t) {
    thread_name = t->name ? t->name : string_NULL;
    length = x_snprintf(buffer, *remain, "%w", thread_name);
    *remain -= length;
  }
  else {
    length = x_snprintf(buffer, *remain, "<NULL>");
    *remain -= length;
  }

  return buffer + length;

}

char * thread_progress_names[] = {"unstarted ", "running ", "bogus ", "completed "};

char * print_thread_long(char* buffer, int *remain, void *data, int w, int p, unsigned int f) {

  w_thread t = (w_thread) data;
  w_instance ti = t->Thread;
  w_size   length;
  w_string thread_name;
  char    *thread_progress;
  w_method method;
  w_int pc;

  if (*remain < 1) {
    return buffer;
  }

  if (t) {
    thread_name = t->name ? t->name : string_NULL;

    if (ti) {
      thread_progress = thread_progress_names[getBooleanField(ti, F_Thread_started) + 2 * getBooleanField(ti, F_Thread_stopped)];
    }
    else {
      thread_progress = "unallocated ";
    }
    length = x_snprintf(buffer, *remain, thread_progress);

    if (! t->top || ! (method = t->top->method)) {
      length += x_snprintf(buffer + length, *remain, "Thread %w p%-2d (no stack frame available)", thread_name, t->jpriority);
    }
    else {
      if (isSet(method->flags, METHOD_IS_COMPILED)) {
        length += x_snprintf(buffer + length, *remain, "Thread %w p%-2d, jitted ", thread_name, t->jpriority);
      }
      else if (isSet(method->flags, ACC_NATIVE)) {
        length += x_snprintf(buffer + length, *remain, "Thread %w p%-2d, ", thread_name, t->jpriority);
      }
      else {
        pc = t->top->current - method->exec.code;
        if (method->exec.debug_info) {
          length += x_snprintf(buffer + length, *remain, "Thread %w p%-2d, line %d [%05d] in ", thread_name, t->jpriority, code2line(method, t->top->current), pc);
        }
        else {
          length += x_snprintf(buffer + length, *remain, "Thread %w p%-2d, [%05d] in ", thread_name, t->jpriority, pc);
        }
      }
      length += x_snprintf(buffer + length, *remain - length, "%M", method);
    }
  }
  else {
    length = x_snprintf(buffer, *remain, "<NULL>");
  }

  *remain -= length;

  return buffer + length;

}

char * threadDescription(w_thread t) {

  x_snprintf(thread_report_buffer, 91, "%T", t);

  return thread_report_buffer;

}

w_boolean enterUnsafeRegion(const w_thread thread) {
  if (isSet(thread->flags, WT_THREAD_NOT_GC_SAFE)) {
    woempa(2, "enterUnsafeRegion: %t has flag already set\n", thread);

    return WONKA_TRUE;

  }

  if (thread == marking_thread) {
    woempa(2, "enterUnsafeRegion: %t is marking_thread, ignore\n", thread);
    setFlag(thread->flags, WT_THREAD_NOT_GC_SAFE);

    return WONKA_FALSE;

  }

#ifdef GC_SAFE_POINTS_USE_NO_MONITORS
  while (TRUE) {
    while (blocking_all_threads) {
      x_thread_sleep(1);
    }
    ++ number_unsafe_threads;
    if (blocking_all_threads) {
      -- number_unsafe_threads;
      //x_thread_sleep(2);
    }
    else {
      break;
    }
  }
  setFlag(thread->flags, WT_THREAD_NOT_GC_SAFE);
#else
  x_monitor_eternal(safe_points_monitor);
  while (blocking_all_threads || isSet(thread->flags, WT_THREAD_SUSPEND_COUNT_MASK)) {
    x_status status;

    woempa(2, "enterUnsafeRegion: %t found blocking_all_threads set, waiting\n", thread);
    status = x_monitor_wait(safe_points_monitor, GC_STATUS_WAIT_TICKS);
    if (status == xs_interrupted) {
      x_monitor_eternal(safe_points_monitor);
     }
  }
  ++ number_unsafe_threads;
  woempa(2, "enterUnsafeRegion: %t incremented number_unsafe_threads to %d\n", thread, number_unsafe_threads);
  setFlag(thread->flags, WT_THREAD_NOT_GC_SAFE);
  x_monitor_notify_all(safe_points_monitor);
  x_monitor_exit(safe_points_monitor);
#endif

  return WONKA_FALSE;
}

w_boolean enterSafeRegion(const w_thread thread) {
  if (isNotSet(thread->flags, WT_THREAD_NOT_GC_SAFE)) {
    woempa(2, "enterSafeRegion: %t has flag unset\n", thread);

    return WONKA_FALSE;

  }

  if (thread == marking_thread) {
    woempa(2, "enterSafeRegion: %t is marking_thread, ignore\n", thread);
    unsetFlag(thread->flags, WT_THREAD_NOT_GC_SAFE);

    return WONKA_TRUE;

  }

#ifdef RUNTIME_CHECKS
  if (number_unsafe_threads <= 0) {
    wabort(ABORT_WONKA, "number_unsafe_threads = %d in enterSafeRegion()!\n", number_unsafe_threads);
  }
#endif

#ifdef GC_SAFE_POINTS_USE_NO_MONITORS
  -- number_unsafe_threads;
  unsetFlag(thread->flags, WT_THREAD_NOT_GC_SAFE);
#else
  x_monitor_eternal(safe_points_monitor);
  -- number_unsafe_threads;
  woempa(2, "enterSafeRegion: %t decremented number_unsafe_threads to %d\n", thread, number_unsafe_threads);
  unsetFlag(thread->flags, WT_THREAD_NOT_GC_SAFE);
  x_monitor_notify_all(safe_points_monitor);
  x_monitor_exit(safe_points_monitor);
#endif

  return WONKA_TRUE;
}

void _gcSafePoint(w_thread thread
#ifdef RUNTIME_CHECKS
, char *file, int line
#endif
) {
  if (isSet(blocking_all_threads, BLOCKED_BY_GC)) {
    if (thread == marking_thread || isNotSet(thread->flags, WT_THREAD_NOT_GC_SAFE)) {

      return;

    }
  }

#ifdef GC_SAFE_POINTS_USE_NO_MONITORS
    -- number_unsafe_threads;
    unsetFlag(thread->flags, WT_THREAD_NOT_GC_SAFE);
    while (TRUE) {
      while (blocking_all_threads) {
        x_thread_sleep(1);
      }
      ++ number_unsafe_threads;
      if (blocking_all_threads) {
        -- number_unsafe_threads;
        //x_thread_sleep(2);
      }
      else {
        break;
      }
    }
    setFlag(thread->flags, WT_THREAD_NOT_GC_SAFE);
#else
  x_monitor_eternal(safe_points_monitor);
#ifdef RUNTIME_CHECKS
  if (number_unsafe_threads <= 0) {
    wabort(ABORT_WONKA, "number_unsafe_threads = %d in _gcSafePoint()!\n", number_unsafe_threads);
  }
#endif
  -- number_unsafe_threads;
  woempa(7, "gcSafePoint -> enterSafeRegion: %t decremented number_unsafe_threads to %d in %s:%d\n", thread, number_unsafe_threads, file, line);
  unsetFlag(thread->flags, WT_THREAD_NOT_GC_SAFE);
  x_monitor_notify_all(safe_points_monitor);

  while (blocking_all_threads || isSet(thread->flags, WT_THREAD_SUSPEND_COUNT_MASK)) {
    x_status status;

    woempa(7, "gcSafePoint -> enterUnsafeRegion: %t found blocking_all_threads set by %s, waiting in %s:%d\n", thread, isSet(blocking_all_threads, BLOCKED_BY_GC) ? "GC" : "JDWP", file, line);
    status = x_monitor_wait(safe_points_monitor, GC_STATUS_WAIT_TICKS);
    if (status == xs_interrupted) {
      x_monitor_eternal(safe_points_monitor);
     }
  }
  ++ number_unsafe_threads;
  woempa(7, "gcSafePoint -> enterUnsafeRegion: %t incremented number_unsafe_threads to %d in %s:%d\n", thread, number_unsafe_threads, file, line);
  setFlag(thread->flags, WT_THREAD_NOT_GC_SAFE);
  x_monitor_exit(safe_points_monitor);
#endif
}
