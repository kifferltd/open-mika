/**************************************************************************
* Copyright (c) 2020, 2022, 2023 by KIFFER Ltd. All rights reserved.      *
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
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       *
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    *
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         *
* OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  *
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                              *
**************************************************************************/

#include <string.h>
// TODO #include <sys/syscall.h>

#include "arrays.h"
#include "core-classes.h"
#include "exception.h"
#include "heap.h"
#include "loading.h"
#include "methods.h"
#include "mika_threads.h"
#include "wstrings.h"

#ifdef RESMON
extern w_boolean pre_thread_start_check(w_thread creatorThread, w_instance newThreadInstance);
extern void pre_thread_termination(w_thread thread);
#else
#define pre_thread_start_check(t,i) TRUE
#define pre_thread_termination(t)
#endif

volatile w_boolean haveWonkaThreads = FALSE;

w_hashtable thread_hashtable;

w_int java_stack_size;

w_int nondaemon_thread_count;
w_boolean system_init_thread_started;

volatile w_int blocking_all_threads;

const char* blocked_by_text[] = {"nowt", "GC", "JDWP", "GC+JDWP", "JITC", "GC+JITC", "JDWP+JITC", "GC+JDWP+JIYC"};

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

/**
 * Logic for recycling native threads: only used if ENABLE_THREAD_RECYCLING
 * is defined.
 */
#ifdef ENABLE_THREAD_RECYCLING
w_fifo xthread_fifo;
static x_Monitor xthreads_Monitor;
x_monitor xthreads_monitor;
#endif // ENABLE_THREAD_RECYCLING

static const char *unborn_thread_report(x_thread);
//static const char *dying_thread_report(x_thread);

/*
** Allocate and clear out the necessary fields for a new thread structure.
*/

void setUpRootFrame(w_thread thread) {
  thread->rootFrame.label = "frame:root";
  thread->rootFrame.previous = NULL;
  thread->rootFrame.method = NULL;
  thread->exception = NULL;
  thread->rootFrame.flags = FRAME_ROOT;
  thread->rootFrame.thread = thread;
  thread->rootFrame.jstack_base = thread->slots;
  thread->rootFrame.jstack_top = thread->rootFrame.jstack_base;
  thread->rootFrame.auxstack_base = last_slot(thread);
  thread->rootFrame.auxstack_top = thread->rootFrame.auxstack_base;
  thread->top = & thread->rootFrame;
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
#ifdef JNI
  newthread->natenv = &w_JNINativeInterface;
#endif
  newthread->label = (char *)"thread";
  newthread->name = name;
  newthread->Thread = Thread;
  newthread->ksize = stacksize;

  if (!parentthread->jpriority) {
    newthread->jpriority = 5;
  }
  else {
    newthread->jpriority = parentthread->jpriority;
  }
  newthread->kpriority = priority_j2k(newthread->jpriority,0);
  newthread->isDaemon = parentthread->isDaemon;

  SET_REFERENCE_SLOT(newthread->top->jstack_top, Thread);
  newthread->top->jstack_top += 1;
  newthread->state = wt_unstarted;
  setWotsitField(Thread, F_Thread_wotsit,  newthread);
  
  return newthread;
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

#ifdef RUNTIME_CHECKS
  if (strncmp(thread->label, "thread", 6)) {
    return NULL; /* CG: thread structure freed or corrupt??? */
  }
#endif

  return thread;
}

static char thread_report_buffer[92];

static const char *unborn_thread_report(x_thread x) {
  return "Thread not yet started";
}

const char *dying_thread_report(x_thread x) {
  return "Thread returning to Saturn";
}

const char *running_thread_report(x_thread x) {

  if (x) {
    w_thread t =  (w_thread)x->xref;
    x_snprintf(thread_report_buffer, 91, "%T", t);
    return thread_report_buffer;
  }

  return "!!! thread xref is NULL !!!";

}

#ifndef INIT_CLASS 
#define INIT_CLASS "wonka.vm.Init"
#endif

static w_string string_sysThread;
static w_string string_sysThreadGroup;

#ifndef JNI
/**
 ** Register system thread and call Init.main() from there, without using JNI.
 */
static void invokeInitMain(w_instance arglist) {
  w_method candidate;
  w_frame frame;
  w_size   i;
  w_size   j;

  w_string registerThread_name_string = utf2String("registerThread", strlen("registerThread"));
  w_string deregisterThread_name_string = utf2String("deregisterThread", strlen("deregisterThread"));
  w_string reg_dereg_desc_string = utf2String("(Ljava/lang/Thread;)V", strlen("(Ljava/lang/Thread;)V"));

  w_method register_method = NULL;
  w_method deregister_method = NULL;
  for (i = 0; i < clazzThreadGroup->numDeclaredMethods; ++i) {
    candidate = &clazzThreadGroup->own_methods[i];
    woempa(1, "Checking %M\n", candidate);

    if (candidate->spec.name == registerThread_name_string && candidate->spec.desc == reg_dereg_desc_string) {
      register_method = candidate;
      woempa(1, "Found ThreadGroup.registerThread() at %p\n", register_method);
    }
    else if (candidate->spec.name == deregisterThread_name_string && candidate->spec.desc == reg_dereg_desc_string) {
      deregister_method = candidate;
      woempa(1, "Found ThreadGroup.deregisterThread() at %p\n", deregister_method);
    }

    if (register_method && deregister_method) {
      break;
    }
  }

  deregisterString(registerThread_name_string);
  deregisterString(deregisterThread_name_string);
  deregisterString(reg_dereg_desc_string);

  if (register_method==NULL) {
    wabort(ABORT_WONKA,"Uh oh: class java.lang.ThreadGroup doesn't have a method registerThread(java.lang.Thread).  Game over.\n");
  }
  if (deregister_method==NULL) {
    wabort(ABORT_WONKA,"Uh oh: class java.lang.ThreadGroup doesn't have a method deregisterThread(java.lang.Thread).  Game over.\n");
  }

  // TODO check exceptionThrown(W_Thread_sysInit) is null after each method call?

  frame = pushFrame(W_Thread_sysInit, register_method);
  frame->flags |= FRAME_NATIVE;

  SET_REFERENCE_SLOT(frame->jstack_top, I_ThreadGroup_system);
  frame->jstack_top += 1;
  SET_REFERENCE_SLOT(frame->jstack_top, I_Thread_sysInit);
  frame->jstack_top += 1;

  callMethod(frame, register_method);
  
  deactivateFrame(frame, NULL);

  w_string initClassName = cstring2String(INIT_CLASS, strlen(INIT_CLASS));
  w_string dotified = undescriptifyClassName(initClassName);
  w_clazz initClazz = namedClassMustBeLoaded(systemClassLoader, dotified);
  deregisterString(dotified);
  deregisterString(initClassName);

  mustBeLinked(initClazz);

  w_string main_name_string = utf2String("main", strlen("main"));
  w_string main_desc_string = utf2String("([Ljava/lang/String;)V", strlen("([Ljava/lang/String;)V"));

  w_method main_method = NULL;
  for (i = 0; i < initClazz->numDeclaredMethods; ++i) {
    candidate = &initClazz->own_methods[i];

    if (candidate->spec.name == main_name_string && candidate->spec.desc == main_desc_string) {
      main_method = candidate;
      break;
    }
  }

  printf("calling Init.main\n");
  frame = pushFrame(W_Thread_sysInit, main_method);
  frame->flags |= FRAME_NATIVE;

  SET_REFERENCE_SLOT(frame->jstack_top, arglist);
  frame->jstack_top += 1;

  callMethod(frame, main_method);
  
  deactivateFrame(frame, NULL);

  frame = pushFrame(W_Thread_sysInit, deregister_method);
  frame->flags |= FRAME_NATIVE;

  SET_REFERENCE_SLOT(frame->jstack_top, I_ThreadGroup_system);
  frame->jstack_top += 1;
  SET_REFERENCE_SLOT(frame->jstack_top, I_Thread_sysInit);
  frame->jstack_top += 1;

  callMethod(frame, register_method);
  
  deactivateFrame(frame, NULL);

}
#endif

extern x_size heap_remaining;

#ifdef PRINT_LOADED_CLASSES
static void printClassName(w_word key, w_word value) {
  w_clazz clazz = (w_clazz)value;
  woempa(7, "%K\n", clazz);
}
#endif

void startInitialThreads(void* data) {

#ifdef DEBUG_STACKS
  volatile
#endif
  w_method method = NULL;
  w_instance arglist;
  w_int dims;
  w_int    i;
#ifdef JNI
  JNIEnv  *env = w_thread2JNIEnv(W_Thread_sysInit);
#endif
  jclass   class_Init;
  w_instance String;
  w_boolean unsafe;

#ifdef DEBUG_STACKS
  W_Thread_sysInit->native_stack_base = &method;
  W_Thread_sysInit->native_stack_max_depth = 0;
#endif

  woempa(9, "********* %d instances in use after initial loading; heap remaining = %d. ************\n", instance_use, heap_remaining);
#ifdef PRINT_LOADED_CLASSES
  ht_every(system_loaded_class_hashtable, printClassName);
#endif


/* TODO
  if (isSet(verbose_flags, VERBOSE_FLAG_THREAD)) {
    w_printf("Start %t: pid is %d lwp is %d\n", W_Thread_sysInit, getpid(), syscall(__NR_gettid));
  }
*/
  mustBeInitialized(clazzString);
  enterSafeRegion(currentWonkaThread);
  woempa(7, "Getting string instance of '%w', thread is '%t'\n", string_sysThreadGroup, currentWonkaThread);
  setReferenceField(I_ThreadGroup_system, getStringInstance(string_sysThreadGroup), F_ThreadGroup_name);
  woempa(7, "Getting string instance of '%w', thread is '%t'\n", string_sysThread, currentWonkaThread);
  setReferenceField(I_Thread_sysInit, getStringInstance(string_sysThread), F_Thread_name);
  setBooleanField(I_Thread_sysInit, F_Thread_started, TRUE);
#ifdef RESMON
#ifndef O4P
#error TODO: This code needs to be modified for other host OSs
#endif
  {
    w_int pid = getpid();
    w_int tid = syscall(__NR_gettid);
    setIntegerField(I_Thread_sysInit, F_Thread_pid, pid);
    setIntegerField(I_Thread_sysInit, F_Thread_tid, tid);
  }
#endif

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
      woempa(9,"args[%d] = \"%w\"\n",i,String2string(instance2Array_instance(arglist)[i]));
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

#ifdef JNI
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
#else
  invokeInitMain(arglist);
#endif

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
/* TODO
  if (isSet(verbose_flags, VERBOSE_FLAG_THREAD)) {
    w_printf("Finish %t: pid was %d lwp %d\n", W_Thread_sysInit, getpid(), syscall(__NR_gettid));
  }
*/
}

static x_Mutex Mutex64;
x_mutex mutex64;

void startKernel() {
  enterSafeRegion(currentWonkaThread);
  nondaemon_thread_count = 0;

  mustBeInitialized(clazzThreadGroup);
  mustBeInitialized(clazzThread);

#ifdef ENABLE_THREAD_RECYCLING
  xthread_fifo = allocFifo(32);
  xthreads_monitor = &xthreads_Monitor;
  x_monitor_create(xthreads_monitor);
#endif // ENABLE_THREAD_RECYCLING

  thread_hashtable = ht_create((char*)"hashtable:threads", THREAD_HASHTABLE_SIZE, NULL, NULL, 0, 0);
  woempa(7, "Created thread_hashtable at %p\n",thread_hashtable);
  I_ThreadGroup_system = allocInstance(NULL, clazzThreadGroup);
  woempa(1,"created I_ThreadGroup_system at %p\n",I_ThreadGroup_system);
  string_sysThreadGroup = cstring2String("SystemThreadGroup", 17);
  I_Thread_sysInit = allocInstance(NULL, clazzThread);
  string_sysThread = cstring2String(INIT_THREAD_NAME, strlen(INIT_THREAD_NAME));

  W_Thread_sysInit = allocClearedMem(sizeof(w_Thread) + java_stack_size);

  if (!W_Thread_sysInit) {
    wabort(ABORT_WONKA, "Couldn't allocate memory for initial thread!\n");
  }

  setUpRootFrame(W_Thread_sysInit);
#ifdef JNI
  W_Thread_sysInit->natenv = &w_JNINativeInterface;
#endif
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
  x_monitor_create(safe_points_monitor);

/* [CG 20050601]
 * For O4P and O4F we let the system supply the stack, since LinuxThreads ignores the
 * one we supply anyway. (NetBSD probably does the Right Thing, but whatever ...)
 */
#if defined (O4P) || defined (FREERTOS)
  W_Thread_sysInit->kstack = NULL;
#else
  W_Thread_sysInit->kstack = allocMem(init_stack_size);
#endif
  W_Thread_sysInit->ksize = init_stack_size;

  W_Thread_sysInit->kthread = &ur_thread_x_Thread;
  W_Thread_sysInit->kthread->xref = W_Thread_sysInit;
  W_Thread_sysInit->kthread->report = unborn_thread_report;
  W_Thread_sysInit->kpriority = priority_j2k(USER_PRIORITY,0);

  setWotsitField(I_Thread_sysInit, F_Thread_wotsit, W_Thread_sysInit);
  setReferenceField(I_Thread_sysInit, I_ThreadGroup_system, F_Thread_parent);
  setIntegerField(I_Thread_sysInit, F_Thread_priority, 5);
  woempa(7, "registering os thread %p as thread instance %p\n", W_Thread_sysInit->kthread, W_Thread_sysInit);
  ht_write(thread_hashtable, (w_word)W_Thread_sysInit->kthread, (w_word)W_Thread_sysInit);

  W_Thread_sysInit->kthread->xref = W_Thread_sysInit;
  W_Thread_sysInit->kthread->report = running_thread_report;
  startInitialThreads(NULL);
}

/*
 * If 'thread' has been interrupted, clear its interrupt flag, throw
 * InterruptedException, and return true. Otherwise return false.
 */
w_boolean testForInterrupt(w_thread thread) {
  if (thread->flags & WT_THREAD_INTERRUPTED) {
    throwException(thread, clazzInterruptedException, NULL);
    thread->flags &= ~WT_THREAD_INTERRUPTED;

    return TRUE;

  }

  return FALSE;
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
    length = x_snprintf(buffer, *remain, "%sThread %w p%-2d of %j", thread_progress, thread_name, t->jpriority, getReferenceField(ti, F_Thread_parent));

    if (! t->top || ! (method = t->top->method)) {
      length += x_snprintf(buffer + length, *remain, " (no stack frame available)");
    }
    else {
      if (isSet(method->flags, METHOD_IS_COMPILED)) {
        length += x_snprintf(buffer + length, *remain, ", jitted ");
      }
      else if (isSet(method->flags, ACC_NATIVE)) {
        length += x_snprintf(buffer + length, *remain, " ");
      }
      else {
        pc = t->top->current - method->exec.code;
        if (method->exec.debug_info) {
          length += x_snprintf(buffer + length, *remain, ", line %d [%05d] in ", code2line(method, t->top->current), pc);
        }
        else {
          length += x_snprintf(buffer + length, *remain, ", [%05d] in ", pc);
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

  status = x_monitor_eternal(safe_points_monitor);
  if (thread != marking_thread) {
    while (blocking_all_threads || isSet(thread->flags, WT_THREAD_SUSPEND_COUNT_MASK)) {
      woempa(2, "enterUnsafeRegion: %t found threads blocked by %s, waiting\n", thread, BLOCKED_BY_TEXT);
      status = x_monitor_wait(safe_points_monitor, x_eternal);
      checkOswaldStatus(status);
    }
  }
  ++ number_unsafe_threads;
  woempa(2, "enterUnsafeRegion: %t incremented number_unsafe_threads to %d\n", thread, number_unsafe_threads);
  setFlag(thread->flags, WT_THREAD_NOT_GC_SAFE);
// no point in this after incrementing number_unsafe_threads
//  status = x_monitor_notify_all(safe_points_monitor);
//  checkOswaldStatus(status);
  status = x_monitor_exit(safe_points_monitor);
  checkOswaldStatus(status);

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

  status = x_monitor_eternal(safe_points_monitor);
  checkOswaldStatus(status);
  -- number_unsafe_threads;
  woempa(2, "enterSafeRegion: %t decremented number_unsafe_threads to %d\n", thread, number_unsafe_threads);
  unsetFlag(thread->flags, WT_THREAD_NOT_GC_SAFE);
// [CG 20230816] only notifying when number_unsafe_threads reaches zero seems to result in occasional deadlocks
//  if (number_unsafe_threads == 0) {
    status = x_monitor_notify_all(safe_points_monitor);
    checkOswaldStatus(status);
//  }
  status = x_monitor_exit(safe_points_monitor);
  checkOswaldStatus(status);

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
// [CG 20230816] only notifying when number_unsafe_threads reaches zero seems to result in occasional deadlocks
//  if (number_unsafe_threads == 0) {
    status = x_monitor_notify_all(safe_points_monitor);
    checkOswaldStatus(status);
//  }

  if (thread->to_be_reclaimed) {
    x_monitor_exit(safe_points_monitor);
    x_thread_yield();
    status = x_monitor_eternal(safe_points_monitor);
    checkOswaldStatus(status);
  }

  while (blocking_all_threads || isSet(thread->flags, WT_THREAD_SUSPEND_COUNT_MASK)) {
    woempa(7, "gcSafePoint -> enterUnsafeRegion: %t found threads blocked by %s, waiting in %s:%d\n", thread, BLOCKED_BY_TEXT, file, line);
    status = x_monitor_wait(safe_points_monitor, x_eternal);
    checkOswaldStatus(status);
  }
  ++ number_unsafe_threads;
  woempa(7, "gcSafePoint -> enterUnsafeRegion: %t incremented number_unsafe_threads to %d in %s:%d\n", thread, number_unsafe_threads, file, line);
  setFlag(thread->flags, WT_THREAD_NOT_GC_SAFE);
  status = x_monitor_exit(safe_points_monitor);
  checkOswaldStatus(status);
}

