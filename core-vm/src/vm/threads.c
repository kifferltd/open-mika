/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved.                                                               *
* Parts copyright (c) 2004, 2005, 2006, 2007, 2008, 2009 by Chris Gray,   *
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

#include "arrays.h"
#include "core-classes.h"
#include "exception.h"
#include "heap.h"
#include "methods.h"
#include "threads.h"
#include "wstrings.h"

w_boolean haveWonkaThreads = FALSE;

w_hashtable thread_hashtable;

w_int java_stack_size;

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
  new->rootFrame.flags = FRAME_ROOT;
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

  w_thread newthread;
  
  newthread = allocClearedMem(sizeof(w_Thread) + java_stack_size);

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
      w_printf("Join status %s - waiting 1 sec and try again\n", x_status2char(status));
      x_thread_sleep(x_millis2ticks(1000));
      status = x_thread_join(thread->kthread, &result, 100);
    }
  }
//#endif
  else {
    wabort(ABORT_WONKA, "Join status %s\n", x_status2char(status));
  }

  woempa(1,"Cleaning up %t\n", thread);
  if (thread->state != wt_unstarted) {
    status = x_thread_delete(thread->kthread);
  }
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

void addThreadCount(w_thread thread) {
  if (!thread->isDaemon) {
    nondaemon_thread_count += 1;
    woempa(7, "Adding non-daemon thread %w, total now %d\n", thread->name, nondaemon_thread_count);
  }
}

void removeThreadCount(w_thread thread) {
  if (!thread->isDaemon) {
    --nondaemon_thread_count;
    woempa(1,"Removed non-daemon thread %w, total now %d\n", thread->name, nondaemon_thread_count);
  }
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
  w_printf("Start %t: pid is %d\n", W_Thread_sysInit, getpid());
#endif
  mustBeInitialized(clazzString);
  woempa(7, "Getting string instance of '%w', thread is '%t'\n", string_sysThreadGroup, currentWonkaThread);
  setReferenceField(I_ThreadGroup_system, getStringInstance(string_sysThreadGroup), F_ThreadGroup_name);
  woempa(7, "Getting string instance of '%w', thread is '%t'\n", string_sysThread, currentWonkaThread);
  setBooleanField(I_Thread_sysInit, F_Thread_started, TRUE);

/*
** First we gather the command line arguments (if any: in the embedded case
** there cannot be any), and use the first as the name of an initial class
** to run and the rest as arguments to be passed to that class's <init>(String[])
** method.  The name goes in arglist[0] and the arguments go in arglists[1...].
*/

  if (command_line_argument_count > 0) {
    dims = command_line_argument_count;
    woempa(7,"Allocating array of %d String[s]\n",dims);
    enterUnsafeRegion(W_Thread_sysInit);
    arglist = allocArrayInstance_1d(W_Thread_sysInit, clazzArrayOf_String, dims);
    enterSafeRegion(W_Thread_sysInit);
    for (i = 0; i < command_line_argument_count; i++) {
      woempa(7, "Getting string instance of '%s', thread is '%t'\n", command_line_arguments[i], currentWonkaThread);
      String = getStringInstance(cstring2String(command_line_arguments[i], strlen(command_line_arguments[i])));
      setArrayReferenceField(arglist, String, i);
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

  addThreadCount(W_Thread_sysInit);
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

  setBooleanField(I_Thread_sysInit, F_Thread_stopped, TRUE);
  W_Thread_sysInit->top = & W_Thread_sysInit->rootFrame;
  removeThreadCount(W_Thread_sysInit);
  W_Thread_sysInit->state = wt_dying;
  unsafe = enterUnsafeRegion(W_Thread_sysInit);
  ht_erase(thread_hashtable,(w_word)W_Thread_sysInit->kthread);
  if (!unsafe) {
    enterSafeRegion(W_Thread_sysInit);
  }
  W_Thread_sysInit->state = wt_dead;
#ifdef DEBUG
  w_printf("Finish %t: pid was %d\n", W_Thread_sysInit, getpid());
#endif
}

static x_Mutex Mutex64;
x_mutex mutex64;

void startKernel() {
  nondaemon_thread_count = 0;

  mustBeInitialized(clazzThreadGroup);
  mustBeInitialized(clazzThread);
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

  W_Thread_sysInit = allocClearedMem(sizeof(w_Thread) + java_stack_size);

  if (!W_Thread_sysInit) {
    wabort(ABORT_WONKA, "Couldn't allocate memory for initial thread!\n");
  }

  setUpRootFrame(W_Thread_sysInit);
  W_Thread_sysInit->natenv = &w_JNINativeInterface;
  W_Thread_sysInit->label = (char*)"thread:sysInit";
  W_Thread_sysInit->name = string_sysThread;
  W_Thread_sysInit->state = wt_unstarted;
  W_Thread_sysInit->isDaemon = FALSE;
  W_Thread_sysInit->jpriority = USER_PRIORITY;
  W_Thread_sysInit->Thread = I_Thread_sysInit;

  mutex64 = &Mutex64;

  x_mutex_create(mutex64);

  mustBeInitialized(clazzClass);
  mustBeInitialized(clazzExceptionInInitializerError);
  mustBeInitialized(clazzAbstractMethodError);
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

#ifdef RUNTIME_CHECKS
#define checkOswaldStatus(s) if ((s) != xs_success) wabort(ABORT_WONKA, "x_monitor_xxx() call returned status = %d", (s))
#else
#define checkOswaldStatus(s)
#endif

w_boolean enterUnsafeRegion(const w_thread thread) {
  x_status status;

  if (isSet(thread->flags, WT_THREAD_NOT_GC_SAFE)) {
    woempa(2, "enterUnsafeRegion: %t has flag already set\n", thread);

    return TRUE;

  }

#ifdef GC_SAFE_POINTS_USE_NO_MONITORS
  while (TRUE) {
  if (thread != marking_thread) {
    while (blocking_all_threads) {
      x_thread_sleep(1);
    }
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
  status = x_monitor_eternal(safe_points_monitor);
  if (thread != marking_thread) {
    while (blocking_all_threads || isSet(thread->flags, WT_THREAD_SUSPEND_COUNT_MASK)) {
      woempa(2, "enterUnsafeRegion: %t found blocking_all_threads set, waiting\n", thread);
      status = x_monitor_wait(safe_points_monitor, GC_STATUS_WAIT_TICKS);
      checkOswaldStatus(status);
    }
  }
  ++ number_unsafe_threads;
  woempa(2, "enterUnsafeRegion: %t incremented number_unsafe_threads to %d\n", thread, number_unsafe_threads);
  setFlag(thread->flags, WT_THREAD_NOT_GC_SAFE);
  status = x_monitor_notify_all(safe_points_monitor);
  checkOswaldStatus(status);
  status = x_monitor_exit(safe_points_monitor);
  checkOswaldStatus(status);
#endif

  return FALSE;
}

w_boolean enterSafeRegion(const w_thread thread) {
  x_status status;

  if (isNotSet(thread->flags, WT_THREAD_NOT_GC_SAFE)) {
    woempa(2, "enterSafeRegion: %t has flag unset\n", thread);

    return FALSE;

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
  status = x_monitor_eternal(safe_points_monitor);
  checkOswaldStatus(status);
  -- number_unsafe_threads;
  woempa(2, "enterSafeRegion: %t decremented number_unsafe_threads to %d\n", thread, number_unsafe_threads);
  unsetFlag(thread->flags, WT_THREAD_NOT_GC_SAFE);
  status = x_monitor_notify_all(safe_points_monitor);
  checkOswaldStatus(status);
  status = x_monitor_exit(safe_points_monitor);
  checkOswaldStatus(status);
#endif

  if (isSet(thread->flags, WT_THREAD_GC_PENDING)) {
    unsetFlag(thread->flags, WT_THREAD_GC_PENDING);
    gc_reclaim(thread->to_be_reclaimed, NULL);
    thread->to_be_reclaimed = 0;
  }

  return TRUE;
}

void _gcSafePoint(w_thread thread
#ifdef RUNTIME_CHECKS
, char *file, int line
#endif
) {
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
  x_status status = x_monitor_eternal(safe_points_monitor);
  checkOswaldStatus(status);
#ifdef RUNTIME_CHECKS
  if (number_unsafe_threads <= 0) {
    wabort(ABORT_WONKA, "number_unsafe_threads = %d in _gcSafePoint()!\n", number_unsafe_threads);
  }
#endif
  -- number_unsafe_threads;
  woempa(7, "gcSafePoint -> enterSafeRegion: %t decremented number_unsafe_threads to %d in %s:%d\n", thread, number_unsafe_threads, file, line);
  unsetFlag(thread->flags, WT_THREAD_NOT_GC_SAFE);
  status = x_monitor_notify_all(safe_points_monitor);
  checkOswaldStatus(status);

  if (thread->to_be_reclaimed) {
    x_monitor_exit(safe_points_monitor);
    checkOswaldStatus(status);
    gc_reclaim(thread->to_be_reclaimed, NULL);
    thread->to_be_reclaimed = 0;
    status = x_monitor_eternal(safe_points_monitor);
    checkOswaldStatus(status);
  }

  while (blocking_all_threads || isSet(thread->flags, WT_THREAD_SUSPEND_COUNT_MASK)) {
    woempa(7, "gcSafePoint -> enterUnsafeRegion: %t found blocking_all_threads set by %s, waiting in %s:%d\n", thread, isSet(blocking_all_threads, BLOCKED_BY_JITC) ? "JITC" : isSet(blocking_all_threads, BLOCKED_BY_GC) ? "GC" : "JDWP", file, line);
    status = x_monitor_wait(safe_points_monitor, GC_STATUS_WAIT_TICKS);
    checkOswaldStatus(status);
  }
  ++ number_unsafe_threads;
  woempa(7, "gcSafePoint -> enterUnsafeRegion: %t incremented number_unsafe_threads to %d in %s:%d\n", thread, number_unsafe_threads, file, line);
  setFlag(thread->flags, WT_THREAD_NOT_GC_SAFE);
  status = x_monitor_exit(safe_points_monitor);
  checkOswaldStatus(status);
#endif
}

