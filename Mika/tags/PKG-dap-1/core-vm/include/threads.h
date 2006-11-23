#ifndef _THREADS_H
#define _THREADS_H

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
*                                                                         *
* Modifications for Mika copyright (c) 2004, 2005, 2006 by Chris Gray,    *
* /k/ Embedded Java Solutions. All rights reserved.                       *
*                                                                         *
**************************************************************************/

/*
** $Id: threads.h,v 1.28 2006/10/04 14:24:14 cvsroot Exp $
*/

#include "jni.h"
#include "oswald.h"
#include "wonka.h"

extern w_size numThreads;
extern w_thread W_Thread_system;
extern w_thread W_Thread_sysInit;
extern w_instance I_Thread_sysInit;
extern w_instance I_ThreadGroup_system;
extern w_method registerThread_method;
extern w_method deregisterThread_method;

void systemGroupManagerEntry(void);

#if defined(UNC20)
// Try to reduce space used by stacks on this platform
#define STACK_FACTOR                        2
static const w_int bytes_per_call        = 660;
#elif defined(ARM)
 #if defined(JSPOT)
  #define STACK_FACTOR                       5
 #else
  #define STACK_FACTOR                       3
 #endif
static const w_int bytes_per_call        = 660;
#elif defined(MIPS)
#define STACK_FACTOR                        4
static const w_size bytes_per_call       = 720;
#elif defined(PPC)
#define STACK_FACTOR                        3
static const w_size bytes_per_call       = 0x1b0;
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
#define WT_THREAD_INTERRUPTED         0x00000004 /* the thread has been interrupted */
#define WT_THREAD_NOT_GC_SAFE         0x00001000 /* the thread is engaged in activity which conflicts with GC. */
#define WT_THREAD_BLOCKED_BY_GC       0x00002000 /* the thread is forbidden any activity which conflicts with GC. */
#define WT_THREAD_SUSPEND_COUNT_MASK  0xffff0000 /* Number of times JDWP suspend has been invoked */
#define WT_THREAD_SUSPEND_COUNT_SHIFT 16

/*
 * Each slot consists of two words: the slot contents (c) and the slot data type (s).
 */
typedef struct w_Slot {
  x_word c;
  x_word s;
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
} w_Frame;

/*
** Tag symbols for the auxillary stack; note that when a tag is greater than 'stack_trace', the symbol contains
** the address of the monitor that is used to lock the object! Use the isMonitoredSlot function to check wether
** it is a monitored object.
*/

static const w_word stack_notrace   = 0; // The stack item does not refer to an object that needs GC tracing; must be 0!
static const w_word stack_trace     = 1; // Refers to an object that needs GC tracing, main stack and auxillary stack.

inline static w_int isMonitoredSlot(w_slot slot) {
  return (slot->s > stack_trace);
}

void callMethod(w_frame arguments, w_method method);

/*
* Stack frame flags
*/

#define FRAME_NATIVE        0x00000001   /* Frame is a host frame for a native method */
#define FRAME_JNI           0x00000002   /* Frame is used in a JNI call */
#define FRAME_LOADING       0x00000004   /* Frame built to invoke classloader */
#define FRAME_CLINIT        0x00000008   /* Frame is used to run a <clinit> method  */
#define FRAME_REFLECTION    0x00000010   /* Frame is used in reflection invocation */
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

#define DEBUG_STACKS

/*
 * Number of slots allocated to each thread, for the Java stack and auxiliary
 * stack combined.
 */
#define SLOTS_PER_THREAD 2048

/*
 * Min. no. of slots which must be free when we push something, else we
 * throw StackOverflowError.
 */
#define MIN_FREE_SLOTS 32

/*
** The JNIEnv* parameter of a JNI call points at field natenv of the current
** Wonka thread.  Putting natenv as the first field of w_Thread simplifies
** the inline functions w_thread2JNIEnv and JNIEnv2w_thread.
*/

typedef struct w_Thread {
  
  /*
  ** The JNI environment pointer. Make sure it is always the first element of the
  ** w_Thread structure since the JNIEnv2w_thread relies on this! An environment
  ** pointer can then just be cast to a w_thread pointer...
  */
  
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

  /*
  ** The native part of a Wonka thread, it's kernel thread, the stack and it's size.
  */

#ifdef JSPOT
  int counter;
#endif
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
  volatile w_Slot  slots[SLOTS_PER_THREAD]; // Reserve space for the slots
} w_Thread;

#define thread2ThreadGroup(t) getReferenceField((t)->Thread, F_Thread_parent)

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
  wt_unstarted    =  15,   /* The states [15 - 17] are Wonka thread specific states. */
  wt_dying        =  16,
  wt_dead         =  17,
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

#ifndef OSWALD
#ifndef JDWP
//#define GC_SAFE_POINTS_USE_NO_MONITORS
#endif
#endif

/*
** The GC_SAFE_POINTS_USE_NO_MONITORS code relies on the fact that only two
** threads contend for the thread safety flags: the marking thread and the
** thread itself. JDWP introduces a third player, so we need to use a monitor.
*/
#if defined(GC_SAFE_POINTS_USE_NO_MONITORS) && defined(JWDP)
#error If JWDP is defined then GC_SAFE_POINTS_USE_NO_MONITORS must not be defined
#endif

/**
 The monitor used to guard gc safety (and JDWP suspend) actions
*/
#ifndef GC_SAFE_POINTS_USE_NO_MONITORS
extern x_monitor safe_points_monitor;
#endif

/**
 The number of threads currently in a gc-unsafe state
*/
extern volatile w_int number_unsafe_threads;

/**
 Set to BLOCKED_BY_GC if no thread may enter an unsafe state, BLOCKED_BY_JDWP
 if JDWP is suspending all threads
*/
extern volatile w_int blocking_all_threads;
#define BLOCKED_BY_GC   1
#define BLOCKED_BY_JDWP 2

extern volatile w_thread marking_thread;

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
    woempa(7, "gsSafePoint(): %s:%d (%s): all threads blocked by %s\n", __FILE__, __LINE__, __FUNCTION__, blocking_all_threads & BLOCKED_BY_GC ? "GC" : "JDWP");
    if (thread == marking_thread) {
      woempa(7, "gsSafePoint(): %s:%d (%s): %t is marking thread, ignoring\n", __FILE__, __LINE__, __FUNCTION__, thread);
    }
    else if (threadIsSafe(thread)) {
      woempa(7, "gsSafePoint(): %s:%d (%s): %t is already safe, ignoring\n", __FILE__, __LINE__, __FUNCTION__, thread);
    }
    else {
      if (blocking_all_threads) {
        woempa(7, "gsSafePoint(): %s:%d (%s): %t entering safe point\n", __FILE__, __LINE__, __FUNCTION__, thread);
        _gcSafePoint(thread, __FILE__, __LINE__);
        woempa(7, "gsSafePoint(): %s:%d (%s): %t leaving safe point\n", __FILE__, __LINE__, __FUNCTION__, thread);
      }
    }
  }
}

inline static void _threadMustBeSafe(w_thread thread, char *file, int line, const char *function) {
  if (threadIsUnsafe(thread)) {
    wabort(ABORT_WONKA, "Thread must be GC safe at %s:%d (%s)!\n", file, line, function);
  }
}

inline static void _threadMustBeUnsafe(w_thread thread, char *file, int line, const char *function) {
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
static INLINE w_thread checkThreadPointer(w_thread t) {
  if (*((w_word*)t->label) != *((w_word*)"thread")
        && *((w_word*)t->label) != *((w_word*)"group")) {
    wabort(ABORT_WONKA, "%p is not a thread!\n", t);
  }
  return t;
}
#else
#define checkThreadPointer(t) (t)
#endif

inline static w_thread  JNIEnv2w_thread(JNIEnv *env) {
  return checkThreadPointer((w_thread)env);
}

inline static JNIEnv *w_thread2JNIEnv(w_Thread *t) {
  return &checkThreadPointer(t)->natenv;
}

inline static w_instance JNIEnv2Thread(JNIEnv *env) {
  return JNIEnv2w_thread(env)->Thread;
}

/*
** From an env pointer, give back the current top frame
*/

#define JNIEnv2frame(e)       (JNIEnv2w_thread(e)->top)

extern w_hashtable thread_hashtable;

void initKernel(void);
void startKernel(void);
w_thread w_threadCreate(w_thread parent, w_object root);

w_int nextGroupId(void);
w_int nextThreadId(void);

w_thread createThread(w_thread current, w_instance Thread, w_instance ThreadGroup, w_string name, w_size stacksize);

w_method  findRunMethod(w_clazz);

char *threadDescription(w_thread);

void addThreadToGroup(w_thread thread, w_instance ThreadGroup);
void removeThreadFromGroup(w_thread thread, w_instance ThreadGroup);

#define STACK_PRESET                        0xaa

extern w_int nondaemon_thread_count;
extern w_boolean system_init_thread_started;

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
 * Terminate a Wonka thread.
 */
void terminateThread(w_thread thread);

#endif /* _THREADS_H */
