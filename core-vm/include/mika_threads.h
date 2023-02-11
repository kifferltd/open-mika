/**************************************************************************
* Copyright (c) 2007, 2008, 2009, 2010, 2011, 2014, 2021, 2022, 2023      *
* by Chris Gray, KIFFER Ltd. All rights reserved.                         *
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

#ifndef _THREADS_H
#define _THREADS_H

#include "jni.h"
#include "oswald.h"
#include "wonka.h"

// If TRACE_CLASSLOADERS is defined, we keep track of the nearest enclosing
// user-defined class loader in each frame.
#if (defined(DEBUG) || defined(RESMON))
#ifndef TRACE_CLASSLOADERS
#define TRACE_CLASSLOADERS
#endif
#endif

#ifdef RESMON
extern w_boolean pre_thread_start_check(w_thread creatorThread, w_instance newThreadInstance);
extern void pre_thread_termination(w_thread thread);
#else
#define pre_thread_start_check(t,i) TRUE
#define pre_thread_termination(t)
#endif

extern w_size numThreads;
extern w_thread W_Thread_system;
extern w_thread W_Thread_sysInit;
extern w_instance I_Thread_sysInit;
extern w_instance I_ThreadGroup_system;
extern w_method registerThread_method;
extern w_method deregisterThread_method;

void systemGroupManagerEntry(void);
 
#if defined(FREERTOS)

static const w_size default_stack_size   = (configMINIMAL_STACK_SIZE);
static const w_size gc_stack_size        = (configMINIMAL_STACK_SIZE);
static const w_size init_stack_size      = (configMINIMAL_STACK_SIZE);
static const w_size group_stack_size     = (configMINIMAL_STACK_SIZE);
static const w_size driver_stack_size    = (configMINIMAL_STACK_SIZE);

#else

// TODO clean up this mess!
#if defined(ARM)
#define STACK_FACTOR                       3
static const w_int bytes_per_call        = 660;
#elif defined(ARMEL)
#define STACK_FACTOR                       3
static const w_int bytes_per_call        = 660;
#elif defined(IM4000)
#define STACK_FACTOR                       4
static const w_int bytes_per_call        = 660;
#elif defined(MIPS)
#define STACK_FACTOR                        4
static const w_size bytes_per_call       = 720;
#elif defined(PPC)
#define STACK_FACTOR                        3
static const w_size bytes_per_call       = 0x1b0;
#elif defined(SH4)
#define STACK_FACTOR                        3
static const w_size bytes_per_call       = 660;
#elif defined(X86)
#define STACK_FACTOR                        5 
static const w_size bytes_per_call       = 500;
#else
#error "Define STACK_FACTOR"
#endif

static const w_size default_stack_size   = 1024 * 32 * STACK_FACTOR;
static const w_size gc_stack_size        = 1024 * 32 * STACK_FACTOR;
static const w_size init_stack_size      = 1024 * 32 * STACK_FACTOR;
static const w_size group_stack_size     = 1024 * 32 * STACK_FACTOR;
static const w_size driver_stack_size    = 1024 * 32 * STACK_FACTOR;

#endif

#define USER_PRIORITY                       5  // This is a java priority

#define GROUP_MANAGER_PRIORITY              10 // Java priority
/* N.B. The kernel priority is set a notch higher, quasi 10.5 */

#define SYSTEM_GROUP_MANAGER_PRIORITY       15 // Java priority
/* N.B. The kernel priority is set a notch higher, quasi 15.5 */

#define THREAD_NAME_BUFFER_SIZE             256

/*
** Map a Java priority to a kernel thread priority.
** Parameter jp is the Java priority, and the sign of parameter trim
** is used to set priorities ``just above'' or ``just below'' jp
** (in the Java sense of ``above/below'').
*/

#define REVERSE_PRIORITY
#ifdef  REVERSE_PRIORITY
#define _j2k(jp) (NUM_PRIORITIES*(16-(jp))/16)
#else
#define _j2k(jp) (NUM_PRIORITIES*((jp))/16)
#endif
#define OLDpriority_j2k(jp,trim) (((jp)<1 ? _j2k(1) : (jp)>15 ? _j2k(15) : _j2k(jp)) + ((trim)>0 ? -1 : (trim)<0 ? 1 : 0))

w_int priority_j2k(w_int java_prio, w_int trim);

#define w_threadFromThreadInstance(i)       (getWotsitField((i), F_Thread_wotsit))

#define WT_THREAD_IS_NATIVE           0x00000001 /* the thread joined the VM using the AttachCurrentThread JNI call */
#define WT_THREAD_INTERRUPTED         0x00000002 /* the thread has been interrupted */
#define WT_THREAD_THROWING_OOME       0x00000004 /* the thread is throwing an OutOfMemoryError */
#define WT_THREAD_GC_PENDING          0x00000008 /* the thread should perform GC as soon as it becomes safe. */
#define WT_THREAD_NOT_GC_SAFE         0x00001000 /* the thread is engaged in activity which conflicts with GC. */
#define WT_THREAD_SUSPEND_COUNT_MASK  0xffff0000 /* Number of times JDWP suspend has been invoked */
#define WT_THREAD_SUSPEND_COUNT_SHIFT 16

/*
 * Each slot consists of two words: the slot contents (c) and the slot data type (s).
 */
typedef struct w_Slot {
  w_word c;
  w_word s;
} w_Slot;

/*
 * For the flags which a frame can have, see the WT__xxx symbols above.
 * The label of a frame is ASCII "frame", optionally followed by more chars.
 *
 * jstack_base points to the start of the Java stack for this method (often
 * local variable #0 = 'this'), while jstack_top points to the location where
 * the next push would occur.
 */
typedef struct w_Frame {
  volatile w_word flags;               // Flags
  char *label;
  w_slot jstack_base;                  // Array of slots for locals, stack, and return value
  volatile w_slot auxstack_top;        // push ==> *top->c = c; *top->s = s; top -= 1; pop ==> l->c = top[+1].c; l->s = top[+1].s; top += 1;
  w_slot auxstack_base;                  // Array of slots for locals, stack, and return value
  volatile w_slot jstack_top;          // push ==> *top->c = c; *top->s = s; top += 1; pop ==> l->c = top[-1].c; l->s = top[-1].s; top -= 1;
  w_frame         previous;            // points to caller or arguments stub frame
  volatile w_method method;            // points to method that this frame refers to, when NULL it's a stub frame
  w_thread        thread;              // points to current wonka thread
  volatile w_code current;             // The opcode pointer at method call or exception; pc = frame->current - frame->method_.exec.code
  volatile w_instance * map;           // A pointer to an array of references (stack map)
#ifdef TRACE_CLASSLOADERS
  w_instance      udcl;                // Nearest user-defined class loader or NULL
#endif
} w_Frame;

/*
** Tag symbols for the auxillary stack; note that when a tag is greater than 'stack_trace', the symbol contains
** the address of the monitor that is used to lock the object! Use the isMonitoredSlot function to check wether
** it is a monitored object.
*/

static const w_word stack_notrace   = 0; // The stack item does not refer to an object that needs GC tracing; must be 0!
static const w_word stack_trace     = 1; // Refers to an object that needs GC tracing, main stack and auxillary stack.

#define isMonitoredSlot(slot) ((slot)->s > stack_trace)

void callMethod(w_frame arguments, w_method method);

/*
* Stack frame flags
*/

#define FRAME_NATIVE        0x00000001   /* Frame is a host frame for a native method */
#define FRAME_JNI           0x00000002   /* Frame is used in a JNI call */
#define FRAME_LOADING       0x00000004   /* Frame built to invoke classloader */
#define FRAME_CLINIT        0x00000008   /* Frame is used to run a <clinit> method  */
#define FRAME_REFLECTION    0x00000010   /* Frame is used in reflection invocation */
#define FRAME_ROOT          0x00000020   /* Frame is the root frame of a thread */
#define FRAME_PRIVILEGED    0x00000040   /* Frame was built using doPrivileged */
#define FRAME_STACKMAP      0x00000080   /* Frame has stack map  */


/**
** Get the security domain associated with a frame.
** Note that for the time being we simply ignore native code.
** Steven, you may need to adapt this.
*/
#define frame2domain(f) getReferenceField(clazz2Class((f)->method->clazz), F_Class_domain))

/**
** getCurrentMethod and getCurrentClazz return the Java method currently
** being executed and the class in which it was defined, respectively.
** getCallingMethod and getCallingClazz return the method which called
** the current Java method and the class in which it was defined.
** Note that we say the -Java- method: if called from within a native
** method, these functions will return information for the Java method
** which invoked the native code.
** getCurrentInstance returns the 'this' of the instance method currently
** being executed (or null if a static method), and getCallingInstance
** does the same for the method which called the current Java method.
*/

w_frame getTopFrame(w_thread thread);
w_method getCurrentMethod(w_thread thread);
w_clazz getCurrentClazz(w_thread thread);
w_instance getCurrentInstance(w_thread thread);
w_method getCallingMethod(w_thread thread);
w_clazz getCallingClazz(w_thread thread);
w_instance getCallingInstance(w_thread thread);

extern w_int java_stack_size;

/*
 * Number of slots allocated to each thread, for the Java stack and auxiliary
 * stack combined.
 */
#define SLOTS_PER_THREAD (java_stack_size / sizeof(w_Slot))

/*
 * Min. no. of slots which must be free when we push something, else we
 * throw StackOverflowError.
 */
#define MIN_FREE_SLOTS 32

typedef struct w_Thread {
  const JNINativeInterface*   natenv;

  char      *label;                  // "thread", or "thread:<whatever>"
  w_flags    flags;                  // See WT_... above
  w_string   name;                   // Same field 'name' of java.lang.Thread
  volatile x_ubyte  state;           // See wt_... below
  w_ubyte    isDaemon;               // nonzero iff this is a daemon thread
  w_short    jpriority;              // Java priority of this thread
  volatile w_frame    top;
  volatile w_instance exception;      // currently pending exception when not NULL, is also in thread->Thread[F_Thread_thrown]
  volatile w_instance Thread;        // corresponding instance of java.lang.Thread
  w_instance protected;              // instance which we wish to protect from GC (e.g. reference returned by a method)
  w_size     to_be_reclaimed;        // amount of memory we should try to reclaim next time we are GC-safe

  /*
  ** The native part of a Wonka thread, it's kernel thread, the stack and it's size.
  */

#ifdef JDWP
  volatile void *step;                // Current jdwp_step or NULL
#endif
  
  x_thread  kthread;
  w_ubyte *   kstack;
  w_size     ksize;
  w_size     kpriority;
#ifdef DEBUG
  w_size     nframes;
#endif
#ifdef DEBUG_STACKS
  void      *native_stack_base;
  int        native_stack_max_depth;
#endif
  w_Frame    rootFrame;               // The root frame.
  volatile w_Slot  slots[0];  // Need to add space for the slots when allocating
} w_Thread;

/*
 * Pointer to the last slot (auxstack_base of the root frame).
 */
#define last_slot(t) ((t)->slots + SLOTS_PER_THREAD - 1)

/*
** Thread states
*/

typedef enum {
  wt_ready        =   0,   /* The states [0 - 14] are corresponding to Oswald */
                           /* thread states, where Wonka has such a state. */
  wt_waiting      =  10,
  wt_sleeping     =  12,
  wt_ended        =  14,
  wt_unstarted    =  15,   /* The states [15 - 18] are Wonka thread specific states. */
  wt_starting     =  16,
  wt_dying        =  17,
  wt_dead         =  18,
} wt_state;

inline static wt_state threadState(w_thread thread) {

  x_state state = thread->kthread ? x_thread_state(thread->kthread) : wt_ready;

  return (state == xt_ready) ? thread->state : state;

}

inline static int threadIsActive(w_thread thread) {

  return threadState(thread) < wt_ended;

}

w_thread  _currentWonkaThread(const char *f, int l);
#define currentWonkaThread     _currentWonkaThread(__FUNCTION__, __LINE__)
#define currentWonkaGroup      ((currentWonkaThread)->group)

/******************************************************************************

  GC SAFE POINTS

  The GC safe-point mechanism associates a GC Status with each thread:
    - GC Status "NOT_GC_SAFE": the thread is engaged in activites which are
      incompatible with the "prepare" or "mark" phase of garbage collection,
      i.e. it is modifying the stack or modifying reference fields of classes,
      objects, or arrays.
    - GC Status "BLOCKED_BY_GC": another thread is in either the "prepare" or
      the "mark" phase of garbage collection, and therefore this thread is 
      forbidden from entering the "NOT_GC_SAFE" state.
    - GC Status "neutral": the thread is in neither of the two states above,
      and is free to enter one or other of them.
  State diagram:
                  <- enterUnsafeRegion        GC by other thread ->
    NOT_GC_SAFE                        neutral                    BLOCKED_BY_GC
                    enterSafeRegion ->            <- finished
                  
                  

  Initially a thread has GC Status "neutral". If an interpreted method is 
  called then enterUnsafeRegion is called, so that interpreted code is treated
  as "mostly unsafe"; on exit from the interpreter enterSafeRegion is called.
  During interpretation, matched pairs of enterSafeRegion/enterUnsafeRegion
  are made around:
    - allocating an instance. (The allocation routines call x_mem_lock(),
      so mutual exclusion with GC is already guaranteed).
    - calling another interpreted or JNI method, including side-effects 
      such as the <clinit> of a class on its first active use.  If the 
      called method is another interpreted method, another call to 
      enterUnsafeRegion will be made in the nested interpreter; if it
      is a JNI method then it will be entered with GC status "neutral".
      J-spotted methods are entered with GC Status NOT_GC_SAFE, i.e. they
      too are treaded as "mostly unsafe".
  Before any transfer of control within the method, a call to gcSafePoint is 
  made. If the safe point request flag (see below) is set, this is equivalent 
  to enterSafeRegion immediately followed by enterUnsafeRegion, and gives us
  a guarantee that the "neutral" state will be entered reasonably often. 

  The "write barrier" macros setReferenceField, setStaticReferenceField,
  and setArrayReferenceField all call enterUnsafeRegion before updating
  the value of the field, and restore the previous status afterward. 

  When a thread (any thread, not just the Undertaker) initiates the prepare/
  mark phase of GC, it first sets the GC Request flag of every other thread,
  and then tries to bring all other thread to GC Status BLOCKED_BY_GC. Only
  when all other threads have reached this status will the prepare/mark phase
  be carried out; the other threads are then allowed to return to "neutral"
  while the sweep phase proceeds. (From "neutral" they may of course enter
  NOT_GC_SAFE at any time: this is harmless).

  The net result is to ensure that no thread can enter NOT_GC_SAFE (and hence
  no thread can modify stacks or the reachability graph of the heap) during
  the prepare/mark phase of GC; equally, the prepare/mark phase cannot be
  entered while any thread is engaged in such "mutating" activity.

  Currently we probably define too much code as "unsafe": however this is
  better than too little, and it gives us a basis to optimise from.
  
  For an explanation of the mutual exclusion algorithm, see the definition
  of w_Thread above.

******************************************************************************/

/**
 The monitor used to guard gc safety (and JDWP suspend) actions
*/
extern x_monitor safe_points_monitor;

/**
 The number of threads currently in a gc-unsafe state
*/
extern volatile w_int number_unsafe_threads;

/**
 Set to BLOCKED_BY_GC if no thread may enter an unsafe state, BLOCKED_BY_JITC
 if the JIT compiler or other function is rewriting bytecode, JDWP if JDWP is
 suspending all threads
*/
extern volatile w_int blocking_all_threads;
#define BLOCKED_BY_GC   1
#define BLOCKED_BY_JDWP 2
#define BLOCKED_BY_JITC 4
#define BLOCKED_BY_WABORT 0x80

extern volatile w_thread marking_thread;
extern volatile w_thread jitting_thread;

// Time to wait for ownership of HC state to change.
#define GC_STATUS_WAIT_TICKS x_eternal

extern w_boolean enterUnsafeRegion(const w_thread thread);
extern w_boolean enterSafeRegion(const w_thread thread);

#define threadIsSafe(t) ((t) && isNotSet((t)->flags, WT_THREAD_NOT_GC_SAFE))

#define threadIsUnsafe(t) ((t) && isSet((t)->flags, WT_THREAD_NOT_GC_SAFE))

#ifdef RUNTIME_CHECKS
void _gcSafePoint(w_thread thread, char *file, int line);

inline static void gcSafePoint(w_thread thread) {
  if (blocking_all_threads) {
    woempa(7, "gcSafePoint(): %s:%d (%s): all threads blocked by%s%s%s%s\n", __FILE__, __LINE__, __FUNCTION__, blocking_all_threads & BLOCKED_BY_GC ? " GC" : "", blocking_all_threads & BLOCKED_BY_JDWP ? " JDWP" : "", blocking_all_threads & BLOCKED_BY_JITC ? " JITC" : "" , blocking_all_threads & BLOCKED_BY_WABORT ? " WABORT" : "");
    if (thread == marking_thread) {
      woempa(7, "gcSafePoint(): %s:%d (%s): %t is marking thread, ignoring\n", __FILE__, __LINE__, __FUNCTION__, thread);
    }
    else if (threadIsSafe(thread)) {
      woempa(7, "gcSafePoint(): %s:%d (%s): %t is already safe, ignoring\n", __FILE__, __LINE__, __FUNCTION__, thread);
    }
    else {
      woempa(7, "gcSafePoint(): %s:%d (%s): %t entering safe point\n", __FILE__, __LINE__, __FUNCTION__, thread);
      _gcSafePoint(thread, __FILE__, __LINE__);
      woempa(7, "gcSafePoint(): %s:%d (%s): %t leaving safe point\n", __FILE__, __LINE__, __FUNCTION__, thread);
      }
  }
}

static void _threadMustBeSafe(w_thread thread, char *file, int line, const char *function) {
  if (threadIsUnsafe(thread)) {
    wabort(ABORT_WONKA, "Thread must be GC safe at %s:%d (%s)!\n", file, line, function);
  }
}

static void _threadMustBeUnsafe(w_thread thread, char *file, int line, const char *function) {
  if (threadIsSafe(thread)) {
    wabort(ABORT_WONKA, "Thread must not be GC safe at %s:%d (%s)!\n", file, line, function);
  }
}

#define threadMustBeSafe(t) _threadMustBeSafe((t), __FILE__, __LINE__, __FUNCTION__)
#define threadMustBeUnsafe(t) _threadMustBeUnsafe((t), __FILE__, __LINE__, __FUNCTION__)
#else
void _gcSafePoint(w_thread thread);
#define gcSafePoint(t) if (blocking_all_threads) _gcSafePoint(t)
#define threadMustBeSafe(t)
#define threadMustBeUnsafe(t)
#endif

/// Check that a w_thread pointer really does point to a w_Thread
#ifdef RUNTIME_CHECKS
static w_thread checkThreadPointer(volatile w_thread t) {
  if (strncmp(t->label, "thread", 6)) {
    wabort(ABORT_WONKA, "%p is not a thread!\n", t);
  }
  return t;
}

static JNIEnv *w_thread2JNIEnv(w_Thread *t) {
  return &checkThreadPointer(t)->natenv;
}

#else

#define checkThreadPointer(t) (t)

inline static JNIEnv *w_thread2JNIEnv(w_Thread *t) {
  return &(t)->natenv;
}
#endif

extern w_hashtable thread_hashtable;

void initKernel(void);
void startKernel(void);
w_thread w_threadCreate(w_thread parent, w_object root);

w_int nextGroupId(void);
w_int nextThreadId(void);

w_thread createThread(w_thread current, w_instance Thread, w_instance ThreadGroup, w_string name, w_size stacksize);

w_method  findRunMethod(w_clazz);

char *threadDescription(w_thread);

void addThreadCount(w_thread thread);
void removeThreadCount(w_thread thread);

#define STACK_PRESET                        0xaa

extern w_int nondaemon_thread_count;
extern w_boolean system_init_thread_started;
extern x_Thread ur_thread_x_Thread;

extern const char *dying_thread_report(x_thread);
extern const char *running_thread_report(x_thread);
extern char * print_thread_short(char*, int*, void*, int w, int p, unsigned int f);
extern char * print_thread_long(char*, int*, void*, int w, int p, unsigned int f);

/*
** This variable changes from FALSE to TRUE when the initial Wonka threads
** have been set up.
*/
extern w_boolean haveWonkaThreads;

void addLocalReference(w_thread thread, w_instance instance);
void pushLocalReference(w_frame frame, w_instance instance);
void pushMonitoredReference(w_frame frame, w_instance instance, x_monitor monitor);
void removeLocalReference(w_thread thread, w_instance instance);

/*
** Remove the topmost local reference from a stack frame.
*/
inline static void popLocalReference(w_frame frame) {
  if (frame->auxstack_top < frame->auxstack_base) {
    woempa(7, "Popping aux[%d] of %t (%j)\n", last_slot(frame->thread) - frame->auxstack_top, frame->thread, frame->auxstack_top[1].c);
    frame->auxstack_top += 1;
  }
}

/*
** Allocate and clear out the necessary fields for a new thread structure.
*/

void setUpRootFrame(w_thread new);

/*
 * Definitions used by Object.wait and Thread.sleep; three weeks corresponds to
 * almost 0x7fffffff millis (0x6C258C00 to be precise).
 */
#define THREE_WEEKS_MILLIS 1814400000LL
#define THREE_WEEKS_TICKS (x_millis2ticks(THREE_WEEKS_MILLIS))

/*
 * If 'thread' has been interrupted, clear its interrupt flag, throw
 * InterruptedException, and return true. Otherwise return false.
 */
w_boolean testForInterrupt(w_thread thread);

/**
 * Logic for recycling native threads: only used if ENABLE_THREAD_RECYCLING
 * is defined.
 */
#ifdef ENABLE_THREAD_RECYCLING
extern w_fifo xthread_fifo;
extern x_monitor xthreads_monitor;
#endif // ENABLE_THREAD_RECYCLING


#endif /* _THREADS_H */
