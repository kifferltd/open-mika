/**************************************************************************
* Copyright (c) 2004, 2005, 2006, 2007, 2008, 2010, 2011, 2012, 2020,     *
* 2021, 2022, 2023 by KIFFER Ltd. All rights reserved.                    *
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

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdarg.h>
#ifdef NATIVE_FP
#include <math.h>
#endif

#include "arrays.h"
#ifndef __BYTE_ORDER
#error How come __BYTE_ORDER is not defined here?
#endif

#include "clazz.h"
#include "constant.h"
#include "core-classes.h"
#include "descriptor.h"
#include "exception.h"
#include "fastcall.h"
#include "fields.h"
#include "hashtable.h"
#include "mikaMath.h"
#include "profile.h"
#include "reflection.h"
#include "wmath.h"
#include "heap.h"
#include "interpreter.h"
#include "dispatcher.h"
#include "loading.h"
#include "locks.h"
#include "ts-mem.h"
#include "methods.h"
#include "opcodes.h"
#include "wstrings.h"
#include "mika_threads.h"
#include "checks.h"
#ifdef JDWP
#include "jdwp.h"
#include "jdwp_events.h"
#endif

// #define woempa(level, ...) _woempa(__FILE__, __FUNCTION__, __LINE__, level, __VA_ARGS__)
void _woempa(const char *file, const char *function, int line, int level, const char *format, ...);

extern char * instruction2char[];
extern w_hashtable lock_hashtable;

void trace(w_frame frame);

extern w_int woempa_bytecodecount;
long long bcc = 0;

extern x_mutex mutex64;

//#define TRIGGER 4800000 // 4782340
#define TRIGGER 9920900

#ifdef DEBUG
#ifdef bar

static void stackCheck(w_frame frame) {

  w_slot base = frame->jstack_base + frame->method->exec.local_i;
  w_slot limit = base + frame->method->exec.stack_i;
  w_slot slot;
  w_instance instance;
  w_int i = 0;
  static w_long checks = 0;

  static w_string skip_1 = NULL;
  static w_string skip_2 = NULL;
  static w_string skip_3 = NULL;
  static w_string skip_4 = NULL;
  
  if (skip_1 == NULL) {
    skip_1 = ascii2String("toString_internal", strlen("toString_internal"));
    skip_2 = ascii2String("test_remainder", strlen("test_remainder"));
    skip_3 = ascii2String("check_remainder", strlen("check_remainder"));
    skip_4 = ascii2String("test_random", strlen("test_random"));
  }

  checks += 1;
  
  /*
  ** Check for overflow and underflow...
  */
    
  if (frame->jstack_top < base) {
    woempa(9, "Stack underflow...\n");
    wabort(ABORT_WONKA, "Stack underflow in %t\n", frame->thread);
  }
  
  if (frame->jstack_top > limit) {
    woempa(9, "!!!! Stack overflow (%d max, %d cur)...\n", frame->method->exec.stack_i, frame->jstack_top - base);
    woempa(9, "Method %M\n", frame->method);
    wabort(ABORT_WONKA, "Stack overflow in %t\n", frame->thread);
  }

  /*
  ** Check for stuff on the stack and in locals that is marked traceable and is not an object.
  ** Probably not feasible if slots do not have a separate type word. 
  */

#ifndef USE_OBJECT_HASHTABLE
  i = 0;
  for (slot = frame->jstack_base; slot < frame->jstack_base + frame->method->exec.local_i; slot += 1) {
    instance = (w_instance) GET_SLOT_CONTENTS(slot);
    if (instance) {
      if (GET_SLOT_SCANNING(slot) == stack_trace && ! (x_mem_tag_get(block2chunk(instance2object(instance))) & OBJECT_TAG)) {
        woempa(9, "A %lld opcodes done...\n", checks);
        woempa(9, "A Method %M, slot %d\n", frame->method, i);
        woempa(9, "A Method takes %d argument slots.\n", frame->method->exec.arg_i);
        wabort(ABORT_WONKA, "Non-instance found in instance slot\n");
      }
    }
    i += 1;
  }

  i = 0;
  for (slot = frame->jstack_base + frame->method->exec.local_i; slot < frame->jstack_top; slot += 1) {
    instance = (w_instance) GET_SLOT_CONTENTS(slot);
    if (instance) {
      if (SET_SLOT_SCANNIN(slot) == stack_trace && ! (x_mem_tag_get(block2chunk(instance2object(instance))) & OBJECT_TAG)) {
        woempa(9, "B %lld opcodes done, reference %p...\n", checks, instance);
        woempa(9, "B Method %M, item %d\n", frame->method, i);
        woempa(9, "B Method takes %d stack slots.\n", frame->method->exec.stack_i);
        wabort(ABORT_WONKA, "Non-instance found in instance slot\n");
      }
    }
  }
#endif

  /*
  ** See if anything unprotected is on the stack or slots...
  */

  if (frame->method->spec.name == skip_3) return;

  i = 0;
  for (slot = frame->jstack_base; slot < frame->jstack_base + frame->method->exec.local_i; slot += 1) {
    instance = (w_instance) GET_SLOT_CONTENTS(slot);
    if (GET_SLOT_SCANNING(slot) == stack_notrace && (w_ubyte *) instance > heap_start && (w_ubyte *) instance < heap_current) {
      if (x_mem_is_block(block2chunk(instance2object(instance)))) {
        if (x_mem_tag_get(block2chunk(instance2object(instance))) & OBJECT_TAG) {
          woempa(9, "C %lld opcodes done %lld...\n", checks, bcc);
          woempa(9, "C Method %M, slot %d\n", frame->method, i);
          woempa(9, "C Method takes %d argument slots.\n", frame->method->exec.arg_i);
          wabort(ABORT_WONKA, "Instance found in non-instance slot (may be bogus)\n");
        }
      }
    }
    i += 1;
  }

  if (frame->method->spec.name == skip_1) return;
  else if (frame->method->spec.name == skip_2) return;
  else if (frame->method->spec.name == skip_4) return;

  i = 0;
  for (slot = frame->jstack_base + frame->method->exec.local_i; slot < frame->jstack_top; slot += 1) {
    instance = (w_instance) GET_SLOT_CONTENTS(slot);
    if (GET_SLOT_SCANNING(slot) == stack_notrace && (w_ubyte *) instance > heap_start && (w_ubyte *) instance < heap_current) {
      if (x_mem_is_block(block2chunk(instance2object(instance)))) {
        if (x_mem_tag_get(block2chunk(instance2object(instance))) & OBJECT_TAG) {
          woempa(9, "D %lld opcodes done, reference %p...\n", checks, instance);
          woempa(9, "D Method %M, item %d\n", frame->method, i);
          woempa(9, "D Method takes %d stack slots.\n", frame->method->exec.stack_i);
          wabort(ABORT_WONKA, "Instance found in non-instance slot (may be bogus)\n");
        }
      }
    }
  }

}

#endif /* bar */
#endif /* DEBUG */

volatile w_thread jitting_thread;

#define JITC_LOCK
#ifdef JITC_LOCK
#ifdef RUNTIME_CHECKS
#define checkOswaldStatus(s) if ((s) != xs_success) wabort(ABORT_WONKA, "x_monitor_xxx() call returned status = %d", (s))
#else
#define checkOswaldStatus(s)
#endif

/*
** Acquire the JIT compiler lock; like the GC or JDWP lock, this means that
** no other threads can enter the Unsafe state. Currently used only for
** bytecode-rewriting purposes within the interpreter itself.
** This function must always be called in the GC-Unsafe state (so there can
** be no contention with GC or JDWP).
*/
// Not yet used so ifdef'd out to avoid compiler warnings
#ifdef HIPPOPOTAMUS_ON_A_POGO_STICK
static void acquireJitcLock(w_thread thread) {
  x_status status;

  woempa(7, "JITC: start locking other threads\n");
  threadMustBeSafe(thread);
  if (number_unsafe_threads < 0) {
    wabort(ABORT_WONKA, "number_unsafe_threads = %d!", number_unsafe_threads);
  }
  x_monitor_eternal(safe_points_monitor);
  while(number_unsafe_threads > 0 || blocking_all_threads) {
    woempa(7, "waiting for %d other threads to reach a safe point\n", number_unsafe_threads - 1);

    status = x_monitor_wait(safe_points_monitor, x_eternal);
    if (status != xs_interrupted) {
      checkOswaldStatus(status);
    }

  }
  woempa(7, "JITC: setting blocking_all_threads to BLOCKED_BY_JITC\n");
  setFlag(blocking_all_threads, BLOCKED_BY_JITC);
  jitting_thread = currentWonkaThread;

// no point after setting a blocking flag
//  x_monitor_notify_all(safe_points_monitor);
  x_monitor_exit(safe_points_monitor);
}

/*
** Release the JIT compiler lock.
*/
static void releaseJitcLock(w_thread thread) {
  woempa(7, "JITC: finished locking other threads\n");
  threadMustBeSafe(thread);
  woempa(7, "JITC: start unlocking other threads\n");
  x_monitor_eternal(safe_points_monitor);
  unsetFlag(blocking_all_threads, BLOCKED_BY_JITC);
  jitting_thread = NULL;
  x_monitor_notify_all(safe_points_monitor);
  x_monitor_exit(safe_points_monitor);
  woempa(7, "JITC: finished unlocking other threads\n");
}
#endif

#else

#define acquireJitcLock(t)
#define releaseJitcLock(t)

#endif

/*
 * If OVERLAPPING_FRAMES is defined, the arguments pushed in the calling frame
 * are re-used directly in the called frame, i.e. the calling and called frame
 * overlap. Otherwise the frames are non-overlapping and the arguments are
 * copied from the calling to the called frame.
 */
#define OVERLAPPING_FRAMES

#ifndef OVERLAPPING_FRAMES
/*
** Function to copy the stack top arguments of the caller to
** the locals argument slots of the callee. Uses loop unrolling
** for the final 16 or lower number of arguments. The 'num' argument
** contains the number of argument slots that this function takes.
**
** For a straightforward copy, we first decrement the 'args' argument,
** that points to the (empty) top of the stack, with the number of
** argument words required.
*/

inline static void stack2locals(w_Slot locals[], volatile w_Slot *args, w_int num) {

  w_slot argsptr = args - 1;
  w_slot localsptr = locals + num - 1;

  args -= num;

  switch (num) {
    default: {
      while (16 < num--) {
        COPY_SLOT_TO_FROM(localsptr--, argsptr--);
      }
    }
    
    case 16:
      COPY_SLOT_TO_FROM(localsptr--, argsptr--);
    case 15:
      COPY_SLOT_TO_FROM(localsptr--, argsptr--);
    case 14:
      COPY_SLOT_TO_FROM(localsptr--, argsptr--);
    case 13:
      COPY_SLOT_TO_FROM(localsptr--, argsptr--);
    case 12:
      COPY_SLOT_TO_FROM(localsptr--, argsptr--);
    case 11:
      COPY_SLOT_TO_FROM(localsptr--, argsptr--);
    case 10:
      COPY_SLOT_TO_FROM(localsptr--, argsptr--);
    case  9:
      COPY_SLOT_TO_FROM(localsptr--, argsptr--);
    case  8:
      COPY_SLOT_TO_FROM(localsptr--, argsptr--);
    case  7:
      COPY_SLOT_TO_FROM(localsptr--, argsptr--);
    case  6:
      COPY_SLOT_TO_FROM(localsptr--, argsptr--);
    case  5:
      COPY_SLOT_TO_FROM(localsptr--, argsptr--);
    case  4:
      COPY_SLOT_TO_FROM(localsptr--, argsptr--);
    case  3:
      COPY_SLOT_TO_FROM(localsptr--, argsptr--);
    case  2:
      COPY_SLOT_TO_FROM(localsptr--, argsptr--);
    case  1:
      COPY_SLOT_TO_FROM(localsptr--, argsptr--);
    case  0: break;

  }

}
#endif

inline static w_byte truncate_i2b(w_int i) {
  return (w_char) (i & 0x000000ff);
}

inline static w_int extend_b2i(w_word b) {
  return ((b & 0x80) ? (b | 0xffffff00) : (b & 0x0000007f));
}

inline static w_int extend_s2i(w_int s) {
  return ((s & 0x8000) ? (w_int) (s | 0xffff0000) : (w_int) (s & 0x00007fff));
}

// FIXME: not declared inline because gcc bleats about it being to big
// Note: The top of stack can also contain return address of 'jsr'.
static void do_astore(w_frame frame, w_word slot, w_Slot **tosptr) {

  COPY_SLOT_TO_FROM(frame->jstack_base+slot, (*tosptr)-1);
  (*tosptr) -= 1;

}

inline static void do_zload(w_frame frame, w_word slot, w_Slot **tosptr) {

  COPY_SLOT_TO_FROM((*tosptr), frame->jstack_base+slot);
  COPY_SLOT_TO_FROM((*tosptr)+1, frame->jstack_base+slot + 1);
  woempa(1, "zload %d : loaded %08x %08x\n", slot, GET_SLOT_CONTENTS(*tosptr), GET_SLOT_CONTENTS((*tosptr) + 1));
  (*tosptr) += 2;

}

inline static void do_zstore(w_frame frame, w_word slot, w_Slot **tosptr) {

  (*tosptr) -= 2;
  COPY_SLOT_TO_FROM(frame->jstack_base+slot, (*tosptr));
  COPY_SLOT_TO_FROM(frame->jstack_base+slot + 1, (*tosptr)+1);
  woempa(1, "zstore %d : stored %08x %08x\n", slot, GET_SLOT_CONTENTS(*tosptr), GET_SLOT_CONTENTS((*tosptr) + 1));

}

static void do_frem(w_Slot**);
static w_double do_drem(w_Slot**);

#define byte_operand             (* (++current))
#define short_operand            ((w_short)(current[1] << 8 | current[2]))
#define int_operand              ((w_int)((current[1] << 24) | (current[2] << 16) | (current[3] << 8) | current[4]))

#ifdef DEBUG
// FIXME: not declared inline because gcc bleats about it being to big
static void updateDebugInfo(w_frame frame, w_code current, w_slot tos) { 
  frame->current = current;
  frame->jstack_top = tos;
  woempa(1, "%M offset[%d] (%s)\n", frame->method, current - frame->method->exec.code, opcode_names[*current]);
  //if (woempa_bytecodecount > 1000000) w_printf("%M offset[%d] (%s)\n", frame->method, current - frame->method->exec.code, opcode_names[*current]);
  woempa_bytecodecount += 1; 
  if (!threadIsUnsafe(frame->thread)) { 
    wabort(ABORT_WONKA, "GC_UNSAFE not set at offset[%d] (%s) of %M\n", current - frame->method->exec.code, opcode_names[*current], frame->method);
  }
}
#else
#define updateDebugInfo(f,c,t)
#endif

#ifdef JDWP
/*
** Check to see whether a pending single-step event should be triggered.
** 0. If no pending event, exit.
** 1. If step depth is OUT, exit.
** 2. If step size is LINE and line number information is available but no
**    line has current pc as start_pc, exit.
** 3. If step->frame is non-NULL and is not current frame, exit.
** 4. If we make it this far, set up step->location and trigger the event.
*/
static void checkSingleStep1(w_frame frame, w_code current, w_slot tos) {
  jdwp_step step = frame->thread->step;

  if (!step) {

    return;

  }

  if (step->depth == 2) {

    return;

  }

  if (step->frame && step->frame != frame) {
    woempa(1, "Not in target frame %p (%m), will not trigger SingleStep event\n", step->frame, step->frame->method);

    return;
  }

  if (step->size && frame->method->exec.debug_info && frame->method->exec.debug_info->lineNums) {
    w_LineNum *ln = frame->method->exec.debug_info->lineNums;
    w_int n = frame->method->exec.debug_info->numLineNums;
    w_int i;

    woempa(7, "Step size is LINE and have info, look for line with start_pc == %d\n", current - frame->method->exec.code);
    for (i = 0; i < n; ++i) {
      if (ln->start_pc == current - frame->method->exec.code) {
        woempa(7, "OK, found it at line %d\n", ln->line_number);
        break;
      }
      ++ln;
    }

    if (i >= n) {
      woempa(7, "Didn't find one, will not trigger SingleStep event\n");

      return;
    }
  }
        
  step->location.method = frame->method;
  step->location.pc = current - frame->method->exec.code;

  frame->current = current;
  frame->jstack_top = tos;
  enterSafeRegion(frame->thread);
  jdwp_event_step(frame->thread);
  enterUnsafeRegion(frame->thread);
}

/*
** If a step of depth 2 (OUT) is pending and the current frame is the one
** we are stepping OUT of, change it to a step INTO. We call this on exit 
** from every interpreted method, and it results in a single step event 
** being triggered by the first bytecode encountered in the next surrounding 
** interpreted frame.
*/
static void checkSingleStep2(w_frame frame) {
  jdwp_step step = (jdwp_step)frame->thread->step;

  // [GR 20100317] It doesn't matter what kind of step you take.
  // [GR 20100317] If the frame matches (step over & step out) or frame == NULL
  // [GR 20100317] (step into), then an event is needed
  if (step && ((step->frame == NULL) ||(step->frame == frame))) {
    // [GR 20100317] reduce step size to min. If line (size == 1) remains 
    // [GR 20100317] selected then you can stop on unexpected places
    step->size = 0;
    step->depth = 0;
    step->frame = NULL;
  }
}
#else
#define checkSingleStep1(frame,current,tos)
#define checkSingleStep2(frame)
#endif

#define do_next_opcode           {        \
  updateProfileBytecodes(frame);          \
  updateDebugInfo(frame, current + 1, tos); \
  checkSingleStep1(frame, current + 1, tos);\
  goto * jumps[*(++current)];             \
}

#define do_this_opcode           {        \
  updateProfileBytecodes(frame);          \
  updateDebugInfo(frame, current, tos);   \
  checkSingleStep1(frame, current, tos);  \
  goto * jumps[*current];                 \
}

#define add_to_opcode(a)        {         \
  updateProfileBytecodes(frame);          \
  current += (a);                         \
  updateDebugInfo(frame, current, tos);   \
  checkSingleStep1(frame, current, tos);  \
  goto * jumps[*current];                 \
}

#define do_conditional(c)        {        \
  updateProfileBytecodes(frame);          \
  if (!(c)) {                             \
    current += 3;                         \
    updateDebugInfo(frame, current, tos); \
    checkSingleStep1(frame, current, tos);\
    goto * jumps[*current];               \
  }                                       \
  else {                                  \
    gcSafePoint(thread);                  \
    current += short_operand;             \
    updateDebugInfo(frame, current, tos); \
    checkSingleStep1(frame, current, tos);\
    goto * jumps[*current];               \
  }                                       \
}

#define do_the_exception         {        \
  goto c_exception;                       \
}

#define do_throw_clazz(c)        {             \
  clazz = (c);                            \
  goto c_clazz2exception;                 \
}

#define do_OutOfMemoryError(n) { i = (n); goto c_OutOfMemoryError; }

#define do_AbstractMethodError goto c_AbstractMethodError

static w_code searchHandler(w_frame frame);

#ifdef bar
typedef struct w_Hist {
  w_int code;
  w_int called;
} w_Hist;

typedef struct w_Hist * w_hist;

w_Hist hist[256];

int histcompare(const void * a, const void * b) {

  w_hist ah = (w_hist) a;
  w_hist bh = (w_hist) b;
  
  if (ah->called > bh->called) return -1;
  else if (ah->called < bh->called) return 1;
  return 0;
  
}

void histogram(void) {
  int i;
  for (i = 0; i < 255; i++) {
    hist[i].code = i;
  }
  qsort(hist, 255, sizeof(w_Hist), histcompare);
  for (i = 0; i < 255; i++) {
    printf("%-25s %8d\n", instruction2char[hist[i].code], hist[i].called);
  }
}
#endif

/*
 * MAX_RETRIES defines how often a low-priority thread (Java priority 1) will
 * try to satisfy a enough_free_memory by calling gc_request(); higher-priority
 * threads will make less tries, and a thread of priority > 10 will only
 * return TRUE from enough_free_memory if sufficient memory is already available.
 * retry_incr is calculated such that (priority * num_retries * retry_incr)
 * is at least 100.
 */
#define MAX_RETRIES 10
static const int retry_incr = 100 / MAX_RETRIES;

static w_boolean enough_free_memory(w_thread thread, w_int bytes) {
  w_int count = 0;
  w_boolean unsafe;

  if (thread == gc_thread || gc_instance == NULL || (signed)x_mem_avail() - bytes > (signed)min_heap_free) {
    return TRUE;
  }

  if (thread->jpriority > 10) {
    return FALSE;
  }

  unsafe = enterSafeRegion(thread);
  while (count < 100 && (signed)x_mem_avail() - bytes <= (signed)min_heap_free) {
    gc_reclaim(bytes, NULL);
    count += retry_incr * thread->jpriority;
  }
  if (unsafe) {
    enterUnsafeRegion(thread);
  }
  return count < 100;
}

// FIXME: not declared inline because gcc bleats about it being to big
static void i_callMethod(w_frame caller, w_method method) {

#ifdef JAVA_PROFILE
  w_thread thread = caller->thread;
  x_long time_start;    
  x_long time_delta;    
#endif
#ifdef DEBUG_STACKS
  w_int depth = (char*)caller->thread->native_stack_base - (char*)&depth;
  if (depth > caller->thread->native_stack_max_depth) {
    if (isSet(verbose_flags, VERBOSE_FLAG_STACK)) {
      w_printf("%M: thread %t stack base %p, end %p, now at %p, used = %d%%\n", method, caller->thread, caller->thread->native_stack_base, (char*)caller->thread->native_stack_base - caller->thread->ksize, &depth, (depth * 100) / caller->thread->ksize);
    }
    caller->thread->native_stack_max_depth = depth;
  }
#endif

  woempa(1, "CALLING %M, dispatcher is %p\n", method, method->exec.dispatcher);
  if (caller->auxstack_top - (caller->jstack_top + method->exec.stack_i) > MIN_FREE_SLOTS
#ifdef DEBUG_STACKS
    && caller->thread->ksize - depth > 4096
#endif
  ) {
#ifdef JAVA_PROFILE
    if(method->exec.dispatcher) {
      updateProfileCalls(caller->method, method);
      time_start = x_systime_get();
      time_delta = caller->thread->kthread->time_delta;
      // w_dump(" --> %M\n", method);
    
      method->exec.dispatcher(caller, method);
      
      // w_dump(" <-- %8lld %M\n", x_systime_get() - time_start - (caller->thread->kthread->time_delta - time_delta), method);
      method->exec.runtime += x_systime_get() - time_start - (caller->thread->kthread->time_delta - time_delta);
      method->exec.totaltime += x_systime_get() - time_start;
    }
    else {
      method->exec.dispatcher(caller, method);
    }
#else
    method->exec.dispatcher(caller, method);
#endif
    woempa(1, "RETURNED from %M\n", method);
  }
  else {
    w_boolean unsafe = enterSafeRegion(caller->thread);

    throwException(caller->thread, clazzStackOverflowError, "unable to call %M: %d on aux stack, %d on java stack, need %d + %d free slots", method, caller->thread->slots + SLOTS_PER_THREAD - caller->auxstack_top, caller->jstack_top - caller->thread->slots, method->exec.stack_i, MIN_FREE_SLOTS);
    if (unsafe) {
      enterUnsafeRegion(caller->thread);
    }
  }
}

/*
** Non inlined version for functions outside this file...
*/

void callMethod(w_frame caller, w_method method) {
  woempa(1, "thread stack base %p, end %p, now at %p, used = %d%%\n", caller->thread->kstack, caller->thread->kstack + caller->thread->ksize, &caller, ((((char*)caller->thread->kstack + caller->thread->ksize) - (char*)&caller) * 100) / caller->thread->ksize);
  i_callMethod(caller, method);
}

static void ** labels = NULL;

extern void fast_StringBuffer_append_String(w_frame);
extern void fast_StringBuffer_append_char(w_frame);
extern void fast_StringBuffer_toString(w_frame);
extern void fast_String_create_empty(w_frame);
extern void fast_String_create_byte(w_frame);
extern void fast_String_create_char(w_frame);
extern void fast_String_equals(w_frame);
extern void fast_String_hashCode(w_frame);
extern void fast_String_length(w_frame);
extern void fast_String_substring(w_frame);
extern void fast_String_indexOf_char(w_frame);
extern void fast_String_charAt(w_frame);
extern void fast_String_toString(w_frame);
extern void fast_String_startsWith(w_frame);
extern void fast_Character_isLetter(w_frame);
extern void fast_Character_isWhitespace(w_frame);
extern void fast_Character_isDigit_char(w_frame);
extern void fast_Character_forDigit_int_int(w_frame);
extern void fast_Character_digit_char_int(w_frame);
extern void fast_System_static_currentTimeMillis(w_frame);
#ifdef NATIVE_MATH
extern void fast_Math_static_sqrt(w_frame);
extern void fast_Math_static_sin(w_frame);
extern void fast_Math_static_cos(w_frame);
extern void fast_Math_static_tan(w_frame);
extern void fast_Math_static_asin(w_frame);
extern void fast_Math_static_atan(w_frame);
extern void fast_Math_static_log(w_frame);
extern void fast_Math_static_exp(w_frame);
#endif

typedef void (*w_fast_method)(w_frame);
static w_fast_method fast_method_table[] = {
  fast_StringBuffer_append_String,
  fast_StringBuffer_append_char,
  fast_StringBuffer_toString,
  fast_String_create_empty,
  fast_String_create_byte,
  fast_String_create_char,
  fast_String_equals,
  fast_String_hashCode,
  fast_String_length,
  fast_String_substring,
  fast_String_indexOf_char,
  fast_String_charAt,
  fast_String_toString,
  fast_String_startsWith,
  NULL,
  fast_Character_isLetter,
  fast_Character_isWhitespace,
  fast_Character_isDigit_char,
  fast_Character_forDigit_int_int,
  fast_Character_digit_char_int,
  fast_System_static_currentTimeMillis,
#ifdef NATIVE_MATH
  fast_Math_static_sqrt,
  fast_Math_static_sin,
  fast_Math_static_cos,
  fast_Math_static_tan,
  fast_Math_static_asin,
  fast_Math_static_atan,
  fast_Math_static_log,
  fast_Math_static_exp,
#endif
};

void interpret(w_frame caller, w_method method) {

  static void * codeJumpTable[] = {
    && c_nop, && c_aconst_null, && c_iconst_m1, && c_iconst_0, && c_iconst_1, && c_iconst_2, && c_iconst_3, 
    && c_iconst_4, && c_iconst_5, && c_lconst_0, && c_lconst_1, && c_fconst_0, && c_fconst_1, && c_fconst_2, 
    && c_dconst_0, && c_dconst_1, && c_bipush, && c_sipush, && c_ldc, && c_ldc_w, && c_ldc2_w, && c_iload, 
    && c_lload, && c_fload, && c_dload, && c_aload, && c_iload_0, && c_iload_1, && c_iload_2, && c_iload_3, 
    && c_lload_0, && c_lload_1, && c_lload_2, && c_lload_3, && c_fload_0, && c_fload_1, && c_fload_2, 
    && c_fload_3, && c_dload_0, && c_dload_1, && c_dload_2, && c_dload_3, && c_aload_0, && c_aload_1, 
    && c_aload_2, && c_aload_3, && c_iaload, && c_laload, && c_faload, && c_daload, && c_aaload, && c_baload, 
    && c_caload, && c_saload, && c_istore, && c_lstore, && c_fstore, && c_dstore, && c_astore, && c_istore_0, 
    && c_istore_1, && c_istore_2, && c_istore_3, && c_lstore_0, && c_lstore_1, && c_lstore_2, && c_lstore_3, 
    && c_fstore_0, && c_fstore_1, && c_fstore_2, && c_fstore_3, && c_dstore_0, && c_dstore_1, && c_dstore_2, 
    && c_dstore_3, && c_astore_0, && c_astore_1, && c_astore_2, && c_astore_3, && c_iastore, && c_lastore, 
    && c_fastore, && c_dastore, && c_aastore, && c_bastore, && c_castore, && c_sastore, && c_pop, && c_pop2, 
    && c_dup, && c_dup_x1, && c_dup_x2, && c_dup2, && c_dup2_x1, && c_dup2_x2, && c_swap, && c_iadd, && c_ladd, 
    && c_fadd, && c_dadd, && c_isub, && c_lsub, && c_fsub, && c_dsub, && c_imul, && c_lmul, && c_fmul, && c_dmul, 
    && c_idiv, && c_ldiv, && c_fdiv, && c_ddiv, && c_irem, && c_lrem, && c_frem, && c_drem, && c_ineg, && c_lneg, 
    && c_fneg, && c_dneg, && c_ishl, && c_lshl, && c_ishr, && c_lshr, && c_iushr, && c_lushr, && c_iand, 
    && c_land, && c_ior, && c_lor, && c_ixor, && c_lxor, && c_iinc, && c_i2l, && c_i2f, && c_i2d, && c_l2i, 
    && c_l2f, && c_l2d, && c_f2i, && c_f2l, && c_f2d, && c_d2i, && c_d2l, && c_d2f, && c_i2b, && c_i2c, 
    && c_i2s, && c_lcmp, && c_fcmpl, && c_fcmpg, && c_dcmpl, && c_dcmpg, && c_ifeq, && c_ifne, && c_iflt, 
    && c_ifge, && c_ifgt, && c_ifle, && c_if_icmpeq, && c_if_icmpne, && c_if_icmplt, && c_if_icmpge, 
    && c_if_icmpgt, && c_if_icmple, && c_if_acmpeq, && c_if_acmpne, && c_goto, && c_jsr, && c_ret, 
    && c_tableswitch, && c_lookupswitch, && c_ireturn, && c_lreturn, && c_freturn, && c_dreturn, && c_areturn, 
    && c_vreturn, && c_getstatic, && c_putstatic, && c_getfield, && c_putfield, && c_invokevirtual, 
    && c_invokespecial, && c_invokestatic, && c_invokeinterface, && i_new, && c_new, && c_newarray, 
    && c_anewarray, && c_arraylength, && c_athrow, && c_checkcast, && c_instanceof, && c_monitorenter, 
    && c_monitorexit, && c_wide, && c_multianewarray, && c_ifnull, && c_ifnonnull, && c_goto_w, && c_jsr_w, 
    && c_breakpoint, && i_getfield_byte, && i_getfield_single, && i_getfield_double, && i_getfield_ref, && i_invokestatic, && i_invokenonvirtual, 
    && i_invokesuper, && i_invokevirtual, && i_invokefast, && i_invokeinterface, && i_getstatic_ref, && i_getstatic_double, && i_getstatic_single, 
    && i_putstatic_ref, && i_putstatic_double, && i_putstatic_single, && c_nocode_219, && i_putfield_byte, && i_putfield_single, 
    && i_putfield_double, && i_putfield_ref, && i_ldc_class, && i_ldc_string, && i_ldc_scalar, && i_ldc_w_class, 
    && i_ldc_w_string, && i_ldc_w_scalar, && c_nocode_230, && c_nocode_231, && c_nocode_232, && c_nocode_233, 
    && c_nocode_234, && c_nocode_235, && c_nocode_236, && c_nocode_237, && c_nocode_238, && c_nocode_239, 
    && c_nocode_240, && c_nocode_241, && c_nocode_242, && c_nocode_243, && c_nocode_244, && c_nocode_245, 
    && c_nocode_246, && c_nocode_247, && c_nocode_248, && c_nocode_249, && c_nocode_250, && c_nocode_251, 
    && c_nocode_252, && c_nocode_253, && c_find_handler, && c_no_handler,
  };

  w_Frame theFrame;
  const w_frame frame = &theFrame;
  const w_thread thread = caller->thread;
  const w_clazz cclazz = method->spec.declaring_clazz;
// disable use of register variables for now
#ifdef USE_REGISTERS
#if defined(X86)
  register w_code current asm ("%ebx") = method->exec.code;
  register void ** jumps = codeJumpTable;
#elif defined(ARM)
  register w_code current asm ("r8") = method->exec.code;
  register void ** jumps asm ("r9") = codeJumpTable;
#else
  register w_code current = method->exec.code;
  register void ** jumps = codeJumpTable;
#endif
#else
  w_code current = method->exec.code;
  void ** jumps = codeJumpTable;
#endif
  w_Slot *tos;
  w_int i;
  w_instance o;
  w_instance a = NULL;
  w_method x = NULL;
  w_clazz clazz = NULL;
  x_monitor m;
  w_field field;
  w_int s;
  w_code operand_ptr;
  w_int * table;
  w_Mopair * mopair;
  w_boolean from_unsafe;

  frame->current = current;
  from_unsafe = enterUnsafeRegion(thread);
  woempa(1, "GC state on entry to %M was %s\n", method, from_unsafe ? "UNSAFE" : "SAFE");
  labels = codeJumpTable;
#ifdef OVERLAPPING_FRAMES
  frame->jstack_base = caller->jstack_top - method->exec.arg_i;
#else
  frame->jstack_base = caller->jstack_top;
#endif

  frame->jstack_top = frame->jstack_base + method->exec.local_i;
  frame->flags = 0;
  frame->previous = caller;
  frame->auxstack_base = caller->auxstack_top;
  frame->auxstack_top = caller->auxstack_top;
  frame->thread = thread;
  frame->method = method;
#ifdef TRACE_CLASSLOADERS
  { 
    w_instance loader = isSet(method->flags, ACC_STATIC)
                        ? method->spec.declaring_clazz->loader
                        : instance2clazz(GET_SLOT_CONTENTS(frame->jstack_base))->loader;
    if (loader && !getBooleanField(loader, F_ClassLoader_systemDefined)) {
      frame->udcl = loader;
    }
    else {
      frame->udcl = caller->udcl;
    }
  }
#endif

#ifndef OVERLAPPING_FRAMES
  stack2locals(frame->jstack_base, caller->jstack_top, method->exec.arg_i);
#endif
  // [CG 20230530] I guess this is meant to permit GC of copied arguments? Looks dubious. 
  for (i = method->exec.arg_i; i < method->exec.local_i; i++) {
    SET_SLOT_SCANNING(frame->jstack_base + i, stack_notrace);
  }

  /*
  ** It is crucial for GC that the following statement only comes after the frame has been
  ** setup correctly.
  */
  
  thread->top = frame;
  tos = (w_Slot*)frame->jstack_top;
  updateDebugInfo(frame, current, tos);
  checkSingleStep1(frame, current, tos);

  /*
  ** Note that pc is a signed integer and that values -1 and -2, currently, are used to store Wonka specific
  ** opcodes for exception handling.  The order in which the opcodes appear
  ** is based on histogram information; we want to keep the cache as warm as possible...
  */

  goto * jumps[*current];

  c_aload: {
    i = byte_operand;
    COPY_SLOT_TO_FROM(tos, frame->jstack_base + i);
    ++tos;
    do_next_opcode;
  }

  c_aload_0: {
    COPY_SLOT_TO_FROM(tos, frame->jstack_base);
    ++tos;
    do_next_opcode;
  }
  
  c_aload_1: {
    COPY_SLOT_TO_FROM(tos, frame->jstack_base + 1);
    ++tos;
    do_next_opcode;
  }
  
  c_aload_2: {
    COPY_SLOT_TO_FROM(tos, frame->jstack_base + 2);
    ++tos;
    do_next_opcode;
  }
  
  c_aload_3: {
    COPY_SLOT_TO_FROM(tos, frame->jstack_base + 3);
    ++tos;
    do_next_opcode;
  }

#ifdef PACK_BYTE_FIELDS
  i_getfield_byte: {
    field = getResolvedFieldConstant(cclazz, short_operand);
    i = FIELD_OFFSET(field->size_and_slot);
    o = (w_instance) GET_SLOT_CONTENTS(tos-1);

    if (o == NULL) {
      do_throw_clazz(clazzNullPointerException);
    }

    SET_SCALAR_SLOT(tos-1, *byteFieldPointer(o, i));

    add_to_opcode(3);
  }
#endif

  i_getfield_single: {
    field = getResolvedFieldConstant(cclazz, short_operand);
    i = FIELD_OFFSET(field->size_and_slot);
    o = (w_instance) GET_SLOT_CONTENTS(tos-1);

    if (o == NULL) {
      do_throw_clazz(clazzNullPointerException);
    }

    SET_SCALAR_SLOT(tos-1, *wordFieldPointer(o, i));

    add_to_opcode(3);
  }

  i_getfield_double: {
    w_boolean isVolatile;

    field = getResolvedFieldConstant(cclazz, short_operand);
    isVolatile = isSet(field->flags, ACC_VOLATILE);

    i = FIELD_OFFSET(field->size_and_slot);
    o = (w_instance) GET_SLOT_CONTENTS(tos-1);

    if (o == NULL) {
      do_throw_clazz(clazzNullPointerException);
    }

    if (isVolatile) {
      x_mutex_lock(mutex64, x_eternal);
    }

    SET_SCALAR_SLOT(tos-1, wordFieldPointer(o, i)[0]);
    SET_SCALAR_SLOT(tos, wordFieldPointer(o, i)[1]);
    woempa(1, "getfield %w of %j : got %08x %08x\n", field->name, o, GET_SLOT_CONTENTS(tos-1), GET_SLOT_CONTENTS(tos));
    if (isVolatile) {
      x_mutex_unlock(mutex64);
    }

    tos += 1;
    add_to_opcode(3);
  }

  i_getfield_ref: {
    field = getResolvedFieldConstant(cclazz, short_operand);
    i = field->size_and_slot;
    o = (w_instance) GET_SLOT_CONTENTS(tos-1);

    if (o == NULL) {
      do_throw_clazz(clazzNullPointerException);
    }

    SET_REFERENCE_SLOT(tos-1, o[instance2clazz(o)->instanceSize  + i]);
    add_to_opcode(3);
  }

  c_ireturn: c_freturn: {
    caller->jstack_top -= method->exec.arg_i;
    COPY_SLOT_TO_FROM(caller->jstack_top, tos-1);
    caller->jstack_top += 1;
    frame->jstack_top = tos;
    goto c_common_return;
  }

  c_iload_0: c_fload_0: {
    COPY_SLOT_TO_FROM(tos, frame->jstack_base);
    ++tos;
    do_next_opcode;
  }
  
  c_goto: {
    gcSafePoint(thread);
    add_to_opcode(short_operand);
  }

  c_ifeq: c_ifnull: {
    tos -= 1; 
    do_conditional(GET_SLOT_CONTENTS(tos) == 0);
  }
  
  c_iload_1: c_fload_1: {
    COPY_SLOT_TO_FROM(tos, frame->jstack_base + 1);
    ++tos;
    do_next_opcode;
  }
  
  c_iload_2: c_fload_2: {
    COPY_SLOT_TO_FROM(tos, frame->jstack_base + 2);
    ++tos;
    do_next_opcode;
  }
  
  c_iload_3: c_fload_3: {
    COPY_SLOT_TO_FROM(tos, frame->jstack_base + 3);
    ++tos;
    do_next_opcode;
  }

  c_iload: c_fload: {
    i = byte_operand;
    COPY_SLOT_TO_FROM(tos, frame->jstack_base + i);
    ++tos;
    do_next_opcode;
  }

  i_getstatic_single: {
    field = (w_field)cclazz->values[(w_ushort) short_operand];
    SET_SCALAR_SLOT(tos, field->declaring_clazz->staticFields[field->size_and_slot]);
    ++tos;

    add_to_opcode(3);
  }

  i_getstatic_double: {
    field = (w_field)cclazz->values[(w_ushort) short_operand];
    w_boolean isVolatile = isSet(field->flags, ACC_VOLATILE);

    if (isVolatile) {
      x_mutex_lock(mutex64, x_eternal);
    }
    SET_SCALAR_SLOT(tos, field->declaring_clazz->staticFields[field->size_and_slot]);
    ++tos;
    SET_SCALAR_SLOT(tos, field->declaring_clazz->staticFields[field->size_and_slot + 1]);
    ++tos;
    if (isVolatile) {
      x_mutex_unlock(mutex64);
    }

    add_to_opcode(3);
  }

  i_getstatic_ref: {
    field = (w_field)cclazz->values[(w_ushort) short_operand];
    SET_REFERENCE_SLOT(tos, field->declaring_clazz->staticFields[field->size_and_slot]);
    ++tos;

    add_to_opcode(3);
  }

  i_putstatic_single: {
    field = (w_field)cclazz->values[(w_ushort) short_operand];
    w_word *ptr = (w_word *)&field->declaring_clazz->staticFields[field->size_and_slot];
    *ptr = GET_SLOT_CONTENTS(tos-1);
    --tos;
    add_to_opcode(3);
  }

  i_putstatic_double: {
    field = (w_field)cclazz->values[(w_ushort) short_operand];
    w_boolean isVolatile = isSet(field->flags, ACC_VOLATILE);
    w_word *ptr = (w_word *)&field->declaring_clazz->staticFields[field->size_and_slot];

    if (isVolatile) {
      x_mutex_lock(mutex64, x_eternal);
    }
    *ptr++ = GET_SLOT_CONTENTS(tos-2);
    *ptr = GET_SLOT_CONTENTS(tos-1);
    if (isVolatile) {
      x_mutex_unlock(mutex64);
    }
    tos -= 2;
    add_to_opcode(3);
  }

  i_putstatic_ref: {
    field = (w_field)cclazz->values[(w_ushort) short_operand];
    w_word *ptr = (w_word *)&field->declaring_clazz->staticFields[field->size_and_slot];

    *ptr = GET_SLOT_CONTENTS(tos-1);
    --tos;
    add_to_opcode(3);
  }

  i_invokestatic: {
    frame->current = current;
    frame->jstack_top = tos;
    x = getResolvedMethodConstant(cclazz, (w_ushort) short_operand);
    if (isNotSet(x->flags, METHOD_UNSAFE_DISPATCH)) {
      enterSafeRegion(thread);
      i_callMethod(frame, x);
      enterUnsafeRegion(thread);
    }
    else {
      i_callMethod(frame, x);
    }
    current += 2;
    tos = (w_Slot*)frame->jstack_top;
    goto check_async_exception;
  }

  c_ifne: c_ifnonnull: {
    tos -= 1; 
    do_conditional(GET_SLOT_CONTENTS(tos) != 0);
  }

  c_aaload: {
    a = (w_instance) GET_SLOT_CONTENTS(tos-2);
    i = (w_int) GET_SLOT_CONTENTS(tos-1);

    if (a == NULL) {
      do_throw_clazz(clazzNullPointerException);
    }

    if (i >= instance2Array_length(a) || i < 0) {
      do_throw_clazz(clazzArrayIndexOutOfBoundsException);
    }

    tos -= 1;
    SET_SLOT_CONTENTS(tos-1, (w_word) instance2Array_instance(a)[i]);

    do_next_opcode;
  }

  c_istore_3: c_fstore_3: {
    tos -= 1;
    COPY_SLOT_TO_FROM(frame->jstack_base+3, tos);
    do_next_opcode;
  }

  c_ifge: {
    tos -= 1; 
    do_conditional((w_int) GET_SLOT_CONTENTS(tos) >= 0);
  }

  c_iconst_0: {
    SET_SCALAR_SLOT(tos, 0);
    tos += 1;
    do_next_opcode;
  }

  c_fcmpl: i = -1; goto do_fcmp;
  c_fcmpg: i = +1; goto do_fcmp;

  do_fcmp: {
    union {w_float f; w_word w;} float_x;
    union {w_float f; w_word w;} float_y;
    float_y.w = GET_SLOT_CONTENTS(tos-1);
    float_x.w = GET_SLOT_CONTENTS(tos-2);
    --tos;
    if (wfp_float32_is_NaN(float_x.f) || wfp_float32_is_NaN(float_y.f) ) {
      // do nothing, result will be 'i' on entry
    }
    else if (wfp_float32_eq(float_x.f, float_y.f)) {
      i = 0;
    }
    else if (wfp_float32_lt(float_x.f, float_y.f)) {
      i = -1;
    }
    else {
      i = 1;
    }
    SET_SLOT_CONTENTS(tos-1, i);
    do_next_opcode;
  }

  c_iinc: {
    i = byte_operand;
    s = (w_int) (w_sbyte) byte_operand;
    SET_SLOT_CONTENTS(frame->jstack_base + i, GET_SLOT_CONTENTS(frame->jstack_base + i) + s);
    do_next_opcode;
  }


  c_istore: c_fstore: {
    tos -= 1;
    i = byte_operand;
    COPY_SLOT_TO_FROM(frame->jstack_base+i, tos);
    do_next_opcode;
  }

  c_isub: {
    tos -= 1;
    SET_SCALAR_SLOT(tos-1, (w_int) GET_SLOT_CONTENTS(tos-1) - (w_int) GET_SLOT_CONTENTS(tos));
    do_next_opcode;
  }

  c_irem: {
    i = (w_int) GET_SLOT_CONTENTS(tos-1);

    if (i == 0) {
      do_throw_clazz(clazzArithmeticException);
    }

    s = (w_int) GET_SLOT_CONTENTS(tos-2);

    if (s != (w_int) 0x80000000 || i != -1) {
      SET_SLOT_CONTENTS(tos-2, s - (w_int) (s / i) * i);
    }
    else {
      SET_SLOT_CONTENTS(tos-2, 0);
    }
  
    tos -= 1;
    do_next_opcode;
  }

  c_lload_0: c_dload_0: {
    do_zload(frame, 0, &tos);
    do_next_opcode;
  }

  c_checkcast: {
    frame->jstack_top = tos;
    enterSafeRegion(thread);
    clazz = getClassConstant(cclazz, (w_ushort) short_operand, thread);
    enterUnsafeRegion(thread);
    if (thread->exception) {
      do_the_exception;
    }
    
    o = (w_instance) GET_SLOT_CONTENTS(tos-1);
    if (o) {
      w_boolean compatible;

      // TODO: make isAssignmentCompatible() GC-safe (means using constraints)
      enterSafeRegion(thread);
      compatible = isAssignmentCompatible(instance2object(o)->clazz, clazz);
      enterUnsafeRegion(thread);
      if (!compatible) {
        do_throw_clazz(clazzClassCastException);
      }
    }
    add_to_opcode(3);
  }

  c_bipush: {
    w_int shift1 = byte_operand << 24;
    w_int shift2 = shift1 >> 24;
    SET_SCALAR_SLOT(tos, (w_word) shift2);
    tos += 1;
    do_next_opcode;
  }

  c_iconst_1: {
    SET_SCALAR_SLOT(tos, 1);
    tos += 1;
    do_next_opcode;
  }

  c_areturn: {
    caller->jstack_top -= method->exec.arg_i;
    if (thread->exception) {
      SET_SLOT_SCANNING(caller->jstack_top, stack_notrace);
      caller->jstack_top += 1;
    }
    else {
      COPY_SLOT_TO_FROM(caller->jstack_top, tos-1);
      caller->jstack_top += 1;
    }
    frame->jstack_top = tos;
    goto c_common_return;
  }

  c_ldc2_w: {
    w_ushort operand = short_operand;
    w_long j;
    memcpy(&j, (void*)&cclazz->values[operand], sizeof(w_long));
    w_long2slots(j, tos);
    tos += 2;
    add_to_opcode(3);
  }

  c_if_icmplt: {
    tos -= 2; 
    do_conditional((w_int) GET_SLOT_CONTENTS(tos)  < (w_int) GET_SLOT_CONTENTS(tos+1));
  }
  
#ifdef PACK_BYTE_FIELDS
  i_putfield_byte: {
    field = getResolvedFieldConstant(cclazz, short_operand);
    i = FIELD_OFFSET(field->size_and_slot);
    o = (w_instance) GET_SLOT_CONTENTS(tos-2);
    if (o == NULL) {
      do_throw_clazz(clazzNullPointerException);
    }

    *byteFieldPointer(o, i) = (w_sbyte)GET_SLOT_CONTENTS(tos-1);
    tos -= 2;
    add_to_opcode(3);
  }
#endif

  i_putfield_single: {
    field = getResolvedFieldConstant(cclazz, short_operand);
    i = FIELD_OFFSET(field->size_and_slot);
    o = (w_instance) GET_SLOT_CONTENTS(tos-2);
    if (o == NULL) {
      do_throw_clazz(clazzNullPointerException);
    }

    *wordFieldPointer(o, i) = GET_SLOT_CONTENTS(tos-1);
    tos -= 2;
    add_to_opcode(3);
  }

  i_putfield_double: {
    w_boolean isVolatile;

    field = getResolvedFieldConstant(cclazz, short_operand);
    isVolatile = isSet(field->flags, ACC_VOLATILE);

    i = FIELD_OFFSET(field->size_and_slot);
    o = (w_instance) GET_SLOT_CONTENTS(tos-3);
    if (o == NULL) {
      do_throw_clazz(clazzNullPointerException);
    }

    if (isVolatile) {
      x_mutex_lock(mutex64, x_eternal);
    }
    wordFieldPointer(o, i)[0] = GET_SLOT_CONTENTS(tos-2);
    wordFieldPointer(o, i)[1] = GET_SLOT_CONTENTS(tos-1);
    woempa(1, "putfield %w of %j : put %08x %08x\n", field->name, o, GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    if (isVolatile) {
      x_mutex_unlock(mutex64);
    }
    tos -= 3;
    add_to_opcode(3);
  }

  i_putfield_ref: {
    field = getResolvedFieldConstant(cclazz, short_operand);
    i = field->size_and_slot;
    o = (w_instance) GET_SLOT_CONTENTS(tos-2);
    if (o == NULL) {
      do_throw_clazz(clazzNullPointerException);
    }

    setReferenceField_unsafe(o, (w_instance) GET_SLOT_CONTENTS(tos-1), i);
    tos -= 2;
    add_to_opcode(3);
  }

  c_if_icmpeq: c_if_acmpeq: {
    tos -= 2; 
    do_conditional((w_int) GET_SLOT_CONTENTS(tos) == (w_int) GET_SLOT_CONTENTS(tos+1));
  }
  
  c_iflt: {
    tos -= 1; 
    do_conditional((w_int) GET_SLOT_CONTENTS(tos)  < 0);
  }

  c_iadd: {
    tos -= 1;
    SET_SLOT_CONTENTS(tos-1, (w_int) GET_SLOT_CONTENTS(tos-1) + (w_int) GET_SLOT_CONTENTS(tos));
    do_next_opcode;
  }

  c_dcmpl: i = -1; goto do_dcmp;
  c_dcmpg: i = +1; goto do_dcmp;

  do_dcmp: {
    union {w_double d; w_word w[2];} double_x;
    union {w_double d; w_word w[2];} double_y;
    double_x.w[0] = GET_SLOT_CONTENTS(tos-4);
    double_x.w[1] = GET_SLOT_CONTENTS(tos-3);
    double_y.w[0] = GET_SLOT_CONTENTS(tos-2);
    double_y.w[1] = GET_SLOT_CONTENTS(tos-1);
    tos -= 3;
    if (wfp_float64_is_NaN(double_x.d) || wfp_float64_is_NaN(double_y.d) ) {
      SET_SLOT_CONTENTS(tos-1, i);
    }
    else if (wfp_float64_lt(double_x.d, double_y.d)) {
      SET_SLOT_CONTENTS(tos-1, -1);
    }
    else if (wfp_float64_eq(double_x.d, double_y.d)) {
      SET_SLOT_CONTENTS(tos-1, 0);
    }
    else {
      SET_SLOT_CONTENTS(tos-1, 1);
    }
    do_next_opcode;
  }

  c_dup: {
    COPY_SLOT_TO_FROM(tos, tos-1);
    tos += 1;
    do_next_opcode;
  }

  c_astore: {
    do_astore(frame, byte_operand, &tos);
    do_next_opcode;
  }

  c_iand: {
    tos -= 1;
    SET_SLOT_CONTENTS(tos-1, GET_SLOT_CONTENTS(tos-1) & GET_SLOT_CONTENTS(tos));
    do_next_opcode;
  }

  c_baload: {
    a = (w_instance) GET_SLOT_CONTENTS(tos-2);
    i = (w_int) GET_SLOT_CONTENTS(tos-1);

    if (a == NULL) {
      do_throw_clazz(clazzNullPointerException);
    }

    if (i >= instance2Array_length(a) || i < 0) {
      do_throw_clazz(clazzArrayIndexOutOfBoundsException);
    }

    tos -= 1;
    if (instance2object(a)->clazz->previousDimension == clazz_boolean) {
      SET_SCALAR_SLOT(tos-1, (instance2Array_byte(a)[i / 8] >> (i % 8)) & 1);
    }
    else {
      SET_SCALAR_SLOT(tos-1, instance2Array_byte(a)[i]);
    }

    do_next_opcode;
  }

  c_i2f: {
    union {w_float f; w_word w;} float_x;
    float_x.f = wfp_int32_to_float32((w_int)GET_SLOT_CONTENTS(tos-1));
    SET_SLOT_CONTENTS(tos-1, float_x.w);
    do_next_opcode;
  }

  i_new:
    clazz = getResolvedClassConstant(cclazz, (w_ushort) short_operand);
    // fall through

  new_common: {
    if (!enough_free_memory(thread, clazz->bytes_needed)) {
      do_OutOfMemoryError(clazz->bytes_needed);
    }
    if (thread->exception) {
      do_the_exception;
    }
    o = allocInstance(thread, clazz);
    SET_REFERENCE_SLOT(tos, o);
    if (thread->exception) {
      do_the_exception;
    }
    tos += 1;

    if (o) {
      removeLocalReference(thread, o);
    }
    current += 2;
    goto check_async_exception;
  }

  /*
  ** Check for a pending asynchronous exception.
  */

  check_async_exception: {
    if (thread->exception) {
      frame->jstack_top = tos;
      frame->current = current;
      current = searchHandler(frame);
      tos = (w_Slot*)frame->jstack_top;
    }
    do_next_opcode;
  }

  c_fmul: {
    union {w_float f; w_word w;} float_x;
    union {w_float f; w_word w;} float_y;
    union {w_float f; w_word w;} float_z;
    float_y.w = GET_SLOT_CONTENTS(tos-1);
    float_x.w = GET_SLOT_CONTENTS(tos-2);
    float_z.f = wfp_float32_mul(float_x.f, float_y.f);
    tos -= 1;
    SET_SLOT_CONTENTS(tos-1, float_z.w);
    do_next_opcode;
  }

  c_sipush: {
    SET_SCALAR_SLOT(tos, (w_int) short_operand);
    tos += 1;
    add_to_opcode(3);
  }

  c_aastore: {
    a = (w_instance) GET_SLOT_CONTENTS(tos-3);
    i = (w_int) GET_SLOT_CONTENTS(tos-2);

    if (! a) {
      do_throw_clazz(clazzNullPointerException);
    }

    if (i >= instance2Array_length(a) || i < 0) {
      do_throw_clazz(clazzArrayIndexOutOfBoundsException);
    }

    o = (w_instance) GET_SLOT_CONTENTS(tos-1);
    if (o) {
      w_boolean compatible;

      // TODO: make isAssignmentCompatible() GC-safe (means using constraints)
      enterSafeRegion(thread);
      compatible = isAssignmentCompatible(instance2object(o)->clazz, instance2object(a)->clazz->previousDimension);
      enterUnsafeRegion(thread);
      if (!compatible) {
        do_throw_clazz(clazzArrayStoreException);
      }
    }
    setArrayReferenceField_unsafe(a, o, i);
    tos -= 3;
    do_next_opcode;
  }

  c_astore_2: {
    do_astore(frame, 2, &tos);
    do_next_opcode;
  }

  c_l2i: {
    woempa(1, "l2i: x = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    w_int int_x = (w_int) slots2w_long(tos-2);
    SET_SLOT_CONTENTS(tos-2, (w_word) int_x);
    woempa(1, "l2i : result = %08x\n", GET_SLOT_CONTENTS(tos-2));
    goto c_pop;
  }

  c_pop: {
    tos -= 1;
    do_next_opcode;
  }

  c_istore_2: c_fstore_2: {
    tos -= 1;
    COPY_SLOT_TO_FROM(frame->jstack_base+2, tos);
    do_next_opcode;
  }

  c_astore_3: {
    do_astore(frame, 3, &tos); 
    do_next_opcode;
  }

  c_arraylength: {
    a = (w_instance) GET_SLOT_CONTENTS(tos-1);
    if (a == NULL) {
      do_throw_clazz(clazzNullPointerException);
    }
    SET_SCALAR_SLOT(tos-1, instance2Array_length(a));
    do_next_opcode;
  }

  c_i2c: {
    SET_SLOT_CONTENTS(tos-1, GET_SLOT_CONTENTS(tos-1) & 0x0000ffff);
    do_next_opcode;
  }

  c_monitorenter: {
    o = (w_instance) GET_SLOT_CONTENTS(tos-1);
    woempa(1, "monitorenter(%j)\n", o);
    if (o == NULL) {
      do_throw_clazz(clazzNullPointerException);
    }

    frame->jstack_top = tos;
    enterSafeRegion(thread);
    m = getMonitor(o);
    x_monitor_eternal(m);
    enterUnsafeRegion(thread);
    // [CG 20230530] suppressing structured locking tests for now
    // pushMonitoredReference(frame, o, m);
    tos -= 1;
    goto check_async_exception;
  }

  c_monitorexit: {
    w_slot slot;
    w_slot base = frame->auxstack_base;
    w_boolean found = FALSE;

    o = (w_instance) GET_SLOT_CONTENTS(tos-1);
    if (o == NULL) {
      do_throw_clazz(clazzNullPointerException);
    }

    // [CG 20230530] suppressing structured locking tests for now
    /*
    for (slot = frame->auxstack_top + 1; base - slot >= 0; ++slot) {
      if (isMonitoredSlot(slot)) {
        woempa(1, "  aux[%d] is a monitored slot : o = %j\n", last_slot(frame->thread) - slot, GET_SLOT_CONTENTS(slot));
        if (GET_SLOT_CONTENTS(slot) == (w_word) o) {
          found = TRUE;
          m = (x_monitor) slot->s;
          if (x_monitor_exit(m) == xs_not_owner) {
            do_throw_clazz(clazzIllegalMonitorStateException);
          }
          SET_SLOT_SCANNING(slot, stack_notrace);
          woempa(1, "Removed monitored object %j from aux[%d] of %t\n", o, last_slot(frame->thread) - slot, frame->thread);
          break;
        }
        else {
          woempa(9, "Monitored object mismatch - expected %j, found %j\n", o, GET_SLOT_CONTENTS(slot));
          do_throw_clazz(clazzInternalError);
        }
      }
      else {
        woempa(1, "  aux[%d] is not a monitored slot\n", last_slot(frame->thread) - slot);
      }
    }
    if (!found) {
      do_throw_clazz(clazzIllegalMonitorStateException);
    }
    while (frame->auxstack_top < base && frame->auxstack_top[1].s == stack_notrace) {
      frame->auxstack_top += 1;
      woempa(1, "Removed zombie from thread %t, now have %d auxs\n", frame->thread, frame->auxstack_top - last_slot(frame->thread));
    }
    */
    // [CG 20230530] simplified code without structured locking tests
    m = getMonitor(o);
    if (x_monitor_exit(m) == xs_not_owner) {
      do_throw_clazz(clazzIllegalMonitorStateException);
    }
      
    tos -= 1;
    do_next_opcode;
  }

  c_bastore: {
    a = (w_instance) GET_SLOT_CONTENTS(tos-3);
    i = (w_int) GET_SLOT_CONTENTS(tos-2);

    if (! a) {
      do_throw_clazz(clazzNullPointerException);
    }

    if (i >= instance2Array_length(a) || i < 0) {
      do_throw_clazz(clazzArrayIndexOutOfBoundsException);
    }

    if (instance2object(a)->clazz->previousDimension == clazz_boolean) {
      s = instance2Array_byte(a)[i / 8];
      // TODO: this is not thread-safe!!! FIXME
      s &= 0xff ^ (1 << i % 8);
      instance2Array_byte(a)[i / 8] = s | ((GET_SLOT_CONTENTS(tos-1) & 1) << (i % 8));
    }
    else {
      instance2Array_byte(a)[i] = truncate_i2b(GET_SLOT_CONTENTS(tos-1));
    }
    tos -= 3;
    do_next_opcode;
  }

  c_lcmp: {
    woempa(1, "lcmp : x = %08x %08x, y = %08x %08x\n", GET_SLOT_CONTENTS(tos-4), GET_SLOT_CONTENTS(tos-3), GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    w_long long_x = slots2w_long(tos-4);
    w_long long_y = slots2w_long(tos-2);
    tos -= 3;
    SET_SLOT_CONTENTS(tos-1, (long_x > long_y) ? 1 : (long_x == long_y) ? 0 : 0xffffffff);
    woempa(1, "lcmp : result = %08x\n", GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_nop: {
    do_next_opcode;
  }

  c_aconst_null: {
    SET_SCALAR_SLOT(tos, 0);
    tos += 1;
    do_next_opcode;
  }

  c_iconst_m1: {
    SET_SCALAR_SLOT(tos, -1);
    tos += 1;
    do_next_opcode;
  }

  c_iconst_2: {
    SET_SCALAR_SLOT(tos, 2);
    tos += 1;
    do_next_opcode;
  }

  c_iconst_3: {
    SET_SCALAR_SLOT(tos, 3);
    tos += 1;
    do_next_opcode;
  }

  c_iconst_4: {
    SET_SCALAR_SLOT(tos, 4);
    tos += 1;
    do_next_opcode;
  }

  c_iconst_5: {
    SET_SCALAR_SLOT(tos, 5);
    tos += 1;
    do_next_opcode;
  }

  c_fconst_0: {
    SET_SCALAR_SLOT(tos, F_ZERO);
    tos += 1;
    do_next_opcode;
  }

  c_fconst_1: {
    SET_SCALAR_SLOT(tos, F_ONE);
    tos += 1;
    do_next_opcode;
  }

  c_fconst_2: {
    SET_SCALAR_SLOT(tos, F_TWO);
    tos += 1;
    do_next_opcode;
  }

  c_lconst_0: {
    w_long2slots(0LL, tos);
    tos += 2;
    do_next_opcode;
  }

  c_lconst_1: {
    w_long2slots(1LL, tos);
    tos += 2;
    do_next_opcode;
  }

  c_dconst_0: {
    w_double2slots(D_ZERO, tos);
    tos += 2;
    do_next_opcode;
  }

  c_dconst_1: {
    w_double2slots(D_ONE, tos);
    tos += 2;
    do_next_opcode;
  }

  c_ldc_w: {
    w_ConstantType *tag; 
    i = (w_ushort) short_operand; 
    tag = &cclazz->tags[i]; 
#ifdef JAVA5
    if ((*tag & CONSTANT_TYPE_MASK) == CONSTANT_CLASS) {
      w_clazz target_clazz;
      frame->jstack_top = tos;
      enterSafeRegion(thread);
      target_clazz = getClassConstant(cclazz, i, thread);
      enterUnsafeRegion(thread);
      if (thread->exception) {
        do_the_exception;
      }
      SET_REFERENCE_SLOT(tos, clazz2Class(target_clazz));
      *current = in_ldc_w_class;
    }
    else
#endif
    if (*tag == RESOLVED_STRING) {
      SET_REFERENCE_SLOT(tos, cclazz->values[i]);
      *current = in_ldc_w_string;
    }
    else if ((*tag & CONSTANT_TYPE_MASK) == CONSTANT_STRING) {
      wabort(ABORT_WONKA, "loading unresolved string constant!");
    }
    else {
      SET_SCALAR_SLOT(tos, cclazz->values[i]);
      *current = in_ldc_w_scalar;
    }
    current += 2;
    tos += 1;
    goto check_async_exception;
  }
  
  i_ldc_w_scalar: {
    i = (w_ushort) short_operand; 
    SET_SCALAR_SLOT(tos, cclazz->values[i]);
    current += 2;
    tos += 1;
    do_next_opcode;
  }

  i_ldc_w_string: {
    i = (w_ushort) short_operand; 
    SET_REFERENCE_SLOT(tos, cclazz->values[i]);
    current += 2;
    tos += 1;
    do_next_opcode;
  }

#ifdef JAVA5
  i_ldc_w_class: {
    w_clazz target_clazz;
    i = (w_ushort) short_operand; 
    target_clazz = getResolvedClassConstant(cclazz, i);
    SET_REFERENCE_SLOT(tos, target_clazz->Class);
    current += 2;
    tos += 1;
    do_next_opcode;
  }
#endif

  c_ldc: {
    w_ConstantType *tag; 
    i = (w_ushort) byte_operand;
    tag = &cclazz->tags[i]; 
#ifdef JAVA5
    if ((*tag & CONSTANT_TYPE_MASK) == CONSTANT_CLASS) {
      w_clazz target_clazz;
      frame->jstack_top = tos;
      enterSafeRegion(thread);
      target_clazz = getClassConstant(cclazz, i, thread);
      enterUnsafeRegion(thread);
      if (thread->exception) {
        do_the_exception;
      }
      SET_REFERENCE_SLOT(tos, clazz2Class(target_clazz));
      current[-1] = in_ldc_class;
    }
    else
#endif
    if (*tag == RESOLVED_STRING) {
      SET_REFERENCE_SLOT(tos, cclazz->values[i]);
      current[-1] = in_ldc_string;
    }
    else if ((*tag & CONSTANT_TYPE_MASK) == CONSTANT_STRING) {
      wabort(ABORT_WONKA, "loading unresolved string constant!");
    }
    else {
      SET_SCALAR_SLOT(tos, cclazz->values[i]);
      current[-1] = in_ldc_scalar;
    }
    tos += 1;
    goto check_async_exception;
  }

  i_ldc_scalar: {
    i = (w_ushort) byte_operand;
    SET_SCALAR_SLOT(tos, cclazz->values[i]);
    tos += 1;
    do_next_opcode;
  }

  i_ldc_string: {
    i = (w_ushort) byte_operand;
    SET_REFERENCE_SLOT(tos, cclazz->values[i]);
    tos += 1;
    do_next_opcode;
  }

#ifdef JAVA5
  i_ldc_class: {
    w_clazz target_clazz;
    i = (w_ushort) byte_operand;
    target_clazz = getResolvedClassConstant(cclazz, i);
    SET_REFERENCE_SLOT(tos, target_clazz->Class);
    tos += 1;
    do_next_opcode;
  }
#endif

  c_lload: c_dload: {
    do_zload(frame, byte_operand, &tos);
    do_next_opcode;
  }

  c_lload_1: c_dload_1: {
    do_zload(frame, 1, &tos);
    do_next_opcode;
  }
  
  c_lload_2: c_dload_2: {
    do_zload(frame, 2, &tos);
    do_next_opcode;
  }
  
  c_lload_3: c_dload_3: {
    do_zload(frame, 3, &tos);
    do_next_opcode;
  }

  c_iaload: c_faload: {
    a = (w_instance) GET_SLOT_CONTENTS(tos-2);
    i = (w_int) GET_SLOT_CONTENTS(tos-1);

    if (a == NULL) {
      do_throw_clazz(clazzNullPointerException);
    }

    if (i >= instance2Array_length(a) || i < 0) {
      do_throw_clazz(clazzArrayIndexOutOfBoundsException);
    }

    tos -= 1;
    SET_SCALAR_SLOT(tos-1, instance2Array_float(a)[i]);

    do_next_opcode;
  }

  c_daload: c_laload: {
    a = (w_instance) GET_SLOT_CONTENTS(tos-2);
    i = (w_int) GET_SLOT_CONTENTS(tos-1);

    if (a == NULL) {
      do_throw_clazz(clazzNullPointerException);
    }

    if (i >= instance2Array_length(a) || i < 0) {
      do_throw_clazz(clazzArrayIndexOutOfBoundsException);
    }

    w_long l = instance2Array_long(a)[i];
    w_long2slots(l, tos-2);
    do_next_opcode;
  }

  c_caload: {
    a = (w_instance) GET_SLOT_CONTENTS(tos-2);
    i = (w_int) GET_SLOT_CONTENTS(tos-1);

    if (a == NULL) {
      do_throw_clazz(clazzNullPointerException);
    }

    if (i >= instance2Array_length(a) || i < 0) {
      do_throw_clazz(clazzArrayIndexOutOfBoundsException);
    }

    tos -= 1;
    SET_SCALAR_SLOT(tos-1, instance2Array_char(a)[i]);
    do_next_opcode;
  }

  c_saload: {
    a = (w_instance) GET_SLOT_CONTENTS(tos-2);
    i = (w_int) GET_SLOT_CONTENTS(tos-1);

    if (a == NULL) {
      do_throw_clazz(clazzNullPointerException);
    }

    if (i >= instance2Array_length(a) || i < 0) {
      do_throw_clazz(clazzArrayIndexOutOfBoundsException);
    }

    tos -= 1;
    SET_SCALAR_SLOT(tos-1, instance2Array_short(a)[i]);
    do_next_opcode;
  }

  c_lstore: c_dstore: {
    do_zstore(frame, byte_operand, &tos);
    do_next_opcode;
  }

  c_istore_0: c_fstore_0: {
    tos -= 1;
    COPY_SLOT_TO_FROM(frame->jstack_base, tos);
    do_next_opcode;
  }
  
  c_istore_1: c_fstore_1: {
    tos -= 1;
    COPY_SLOT_TO_FROM(frame->jstack_base+1, tos);
    do_next_opcode;
  }
  

  c_lstore_0: c_dstore_0: {
    do_zstore(frame, 0, &tos);
    do_next_opcode;
  }
  
  c_lstore_1: c_dstore_1: {
    do_zstore(frame, 1, &tos);
    do_next_opcode;
  }
  
  c_lstore_2: c_dstore_2: {
    do_zstore(frame, 2, &tos);
    do_next_opcode;
  }
  
  c_lstore_3: c_dstore_3: {
    do_zstore(frame, 3, &tos);
    do_next_opcode;
  }

  c_astore_0: {
    do_astore(frame, 0, &tos); 
    do_next_opcode;
  }

  c_astore_1: {
    do_astore(frame, 1, &tos); 
    do_next_opcode;
  }
  
  c_iastore: c_fastore: {
    a = (w_instance) GET_SLOT_CONTENTS(tos-3);
    i = (w_int) GET_SLOT_CONTENTS(tos-2);

    if (! a) {
      do_throw_clazz(clazzNullPointerException);
    }

    if (i >= instance2Array_length(a) || i < 0) {
      do_throw_clazz(clazzArrayIndexOutOfBoundsException);
    }

    instance2Array_float(a)[i] = GET_SLOT_CONTENTS(tos-1);
    tos -= 3;
    do_next_opcode;
  }

  c_lastore: c_dastore: {
    a = (w_instance) GET_SLOT_CONTENTS(tos-4);
    i = (w_int) GET_SLOT_CONTENTS(tos-3);

    if (! a) {
      do_throw_clazz(clazzNullPointerException);
    }

    if (i >= instance2Array_length(a) || i < 0) {
      do_throw_clazz(clazzArrayIndexOutOfBoundsException);
    }

    w_long l = slots2w_long(tos-2);
    instance2Array_long(a)[i] = l;
    tos -= 4;
    do_next_opcode;
  }


  c_castore: c_sastore: {
    a = (w_instance) GET_SLOT_CONTENTS(tos-3);
    i = (w_int) GET_SLOT_CONTENTS(tos-2);

    if (! a) {
      do_throw_clazz(clazzNullPointerException);
    }

    if (i >= instance2Array_length(a) || i < 0) {
      do_throw_clazz(clazzArrayIndexOutOfBoundsException);
    }

    instance2Array_char(a)[i] = (GET_SLOT_CONTENTS(tos-1) & 0x0000ffff);
    tos -= 3;
    do_next_opcode;
  }
  

  c_pop2: {
    tos -= 2;
    do_next_opcode;
  }

  c_dup_x1: {
    COPY_SLOT_TO_FROM(tos, tos-1);
    COPY_SLOT_TO_FROM(tos-1, tos-2);
    COPY_SLOT_TO_FROM(tos-2, tos);
    tos += 1;
    do_next_opcode;
  }

  c_dup_x2: {
    COPY_SLOT_TO_FROM(tos, tos-1);
    COPY_SLOT_TO_FROM(tos-1, tos-2);
    COPY_SLOT_TO_FROM(tos-2, tos-3);
    COPY_SLOT_TO_FROM(tos-3, tos);
    tos += 1;
    do_next_opcode;
  }

  c_dup2: {
    COPY_SLOT_TO_FROM(tos+1, tos-1);
    COPY_SLOT_TO_FROM(tos, tos-2);
    tos += 2;
    do_next_opcode;
  }

  c_dup2_x1: {
    COPY_SLOT_TO_FROM(tos+1, tos-1);
    COPY_SLOT_TO_FROM(tos, tos-2);
    COPY_SLOT_TO_FROM(tos-1, tos-3);
    COPY_SLOT_TO_FROM(tos-2, tos+1);
    COPY_SLOT_TO_FROM(tos-3, tos);
    tos += 2;
    do_next_opcode;
  }

  c_dup2_x2: {
    tos += 2;
    COPY_SLOT_TO_FROM(tos-1, tos-3);
    COPY_SLOT_TO_FROM(tos-2, tos-4);
    COPY_SLOT_TO_FROM(tos-3, tos-5);
    COPY_SLOT_TO_FROM(tos-4, tos-6);
    COPY_SLOT_TO_FROM(tos-5, tos-1);
    COPY_SLOT_TO_FROM(tos-6, tos-2);
    do_next_opcode;
  }

  c_swap: {
    // Using *tos for temporary storage, without incrementing tos afterward
    COPY_SLOT_TO_FROM(tos, tos-1);
    COPY_SLOT_TO_FROM(tos-1, tos-2);
    COPY_SLOT_TO_FROM(tos-2, tos);
    do_next_opcode;
  }

  c_ladd: {
    woempa(1, "ladd : x = %08x %08x y = %08x %08x\n", GET_SLOT_CONTENTS(tos-4), GET_SLOT_CONTENTS(tos-3), GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    w_long sum = slots2w_long(tos-4) + slots2w_long(tos-2);
    tos -= 2;
    w_long2slots(sum, tos-2);
    woempa(1, "ladd : result = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_fadd: {
    union {w_float f; w_word w;} float_x;
    union {w_float f; w_word w;} float_y;
    union {w_float f; w_word w;} float_z;
    float_y.w = GET_SLOT_CONTENTS(tos-1);
    float_x.w = GET_SLOT_CONTENTS(tos-2);
    float_z.f = wfp_float32_add(float_x.f, float_y.f);
    tos -= 1;
    SET_SLOT_CONTENTS(tos-1, float_z.w);
    do_next_opcode;
  }

  c_dadd: {
    woempa(1, "dadd : x = %08x %08x y = %08x %08x\n", GET_SLOT_CONTENTS(tos-4), GET_SLOT_CONTENTS(tos-3), GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    w_double sum = wfp_float64_add(slots2w_double(tos-4), slots2w_double(tos-2));
    tos -= 2;
    w_double2slots(sum, tos-2);
    woempa(1, "dadd : result = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_lsub: {
    woempa(1, "lsub : x = %08x %08x y = %08x %08x\n", GET_SLOT_CONTENTS(tos-4), GET_SLOT_CONTENTS(tos-3), GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    w_long diff = slots2w_long(tos-4) - slots2w_long(tos-2);
    tos -= 2;
    w_long2slots(diff, tos-2);
    woempa(1, "lsub : result = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_fsub: {
    union {w_float f; w_word w;} float_x;
    union {w_float f; w_word w;} float_y;
    union {w_float f; w_word w;} float_z;
    float_y.w = GET_SLOT_CONTENTS(tos-1);
    float_x.w = GET_SLOT_CONTENTS(tos-2);
    float_z.f = wfp_float32_sub(float_x.f, float_y.f);
    tos -= 1;
    SET_SLOT_CONTENTS(tos-1, float_z.w);
    do_next_opcode;
  }

  c_dsub: {
    woempa(1, "dsub : x = %08x %08x y = %08x %08x\n", GET_SLOT_CONTENTS(tos-4), GET_SLOT_CONTENTS(tos-3), GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    w_double diff = wfp_float64_sub(slots2w_double(tos-4), slots2w_double(tos-2));
    tos -= 2;
    w_double2slots(diff, tos-2);
    woempa(1, "dsub : result = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_imul: {
    tos -= 1;
    SET_SLOT_CONTENTS(tos-1, (w_int) GET_SLOT_CONTENTS(tos-1) * (w_int) GET_SLOT_CONTENTS(tos));
    do_next_opcode;
  }

  c_lmul: {
    woempa(1, "lmul : x = %08x %08x y = %08x %08x\n", GET_SLOT_CONTENTS(tos-4), GET_SLOT_CONTENTS(tos-3), GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    w_long prod = slots2w_long(tos-4) * slots2w_long(tos-2);
    tos -= 2;
    w_long2slots(prod, tos-2);
    woempa(1, "lmul : result = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_dmul: {
    woempa(1, "dmul : x = %08x %08x y = %08x %08x\n", GET_SLOT_CONTENTS(tos-4), GET_SLOT_CONTENTS(tos-3), GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    w_double product = wfp_float64_mul(slots2w_double(tos-4), slots2w_double(tos-2));
    tos -= 2;
    w_double2slots(product, tos-2);
    woempa(1, "dmul : result = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_idiv: {
    w_int v1 = (w_int)GET_SLOT_CONTENTS(tos-2);
    w_int v2 = (w_int)GET_SLOT_CONTENTS(tos-1);

    if (v2 == 0) {
      do_throw_clazz(clazzArithmeticException);
    }
  
    tos -= 2;

    if ((v1 >= 0) && (v2 > 0)) {
      SET_SLOT_CONTENTS(tos, v1 / v2);
    }
    else if (v1 == (w_int) 0x80000000) {
      if (v2 == -1) {
        SET_SLOT_CONTENTS(tos, 0x80000000);
      }
      else {
        SET_SLOT_CONTENTS(tos, v1 / v2);      
      }
    }
    else if ((v1 < 0) && (v2 < 0)) {
      v1 = -v1;
      v2 = -v2;
      SET_SLOT_CONTENTS(tos, v1 / v2);
    }
    else if (v1 < 0) {
      v1 = -v1;
      SET_SLOT_CONTENTS(tos, -(v1 / v2));
    }
    else {
      v2 = -v2;
      SET_SLOT_CONTENTS(tos, -(v1 / v2));
    }
  
    tos += 1;
    do_next_opcode;
  }

  c_ldiv: {
    woempa(1, "ldiv : x = %08x %08x y = %08x %08x\n", GET_SLOT_CONTENTS(tos-4), GET_SLOT_CONTENTS(tos-3), GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    w_long dividend = slots2w_long(tos-4);
    w_long divisor = slots2w_long(tos-2);
    w_long quotient;

    if (divisor == 0LL) {
      do_throw_clazz(clazzArithmeticException);
    }

    if (dividend >= 0LL && divisor > 0LL) {
      quotient = dividend / divisor;
    }
    else if (dividend == (w_long) 0x8000000000000000LL) {
      if (divisor == -1) {
        quotient = 0x8000000000000000LL;
      }
      else {
        quotient = dividend / divisor;
      }
    }
    else if (dividend < 0LL && divisor < 0LL) {
      dividend = -dividend;
      divisor = -divisor;
      quotient = dividend / divisor;
    }
    else if (dividend < 0LL) { /* divisor > 0LL */
      dividend = -dividend;
      quotient = -(dividend / divisor);
    }
    else { /* dividend > 0, divisor < 0 */
      divisor = -divisor;
      quotient = -(dividend / divisor);
    }

    tos -= 2;
    w_long2slots(quotient, tos-2);
    woempa(1, "ldiv : result = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_fdiv: {
    union {w_float f; w_word w;} float_x;
    union {w_float f; w_word w;} float_y;
    union {w_float f; w_word w;} float_z;
    float_y.w = GET_SLOT_CONTENTS(tos-1);
    float_x.w = GET_SLOT_CONTENTS(tos-2);
    float_z.f = wfp_float32_div(float_x.f, float_y.f);
    tos -= 1;
    SET_SLOT_CONTENTS(tos-1, float_z.w);
    do_next_opcode;
  }

  c_ddiv: {
    woempa(1, "ddiv : x = %08x %08x y = %08x %08x\n", GET_SLOT_CONTENTS(tos-4), GET_SLOT_CONTENTS(tos-3), GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    w_double quotient = wfp_float64_div(slots2w_double(tos-4), slots2w_double(tos-2));
    tos -= 2;
    w_double2slots(quotient, tos-2);
    woempa(1, "ddiv : result = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_lrem: {
    woempa(1, "lrem : x = %08x %08x y = %08x %08x\n", GET_SLOT_CONTENTS(tos-4), GET_SLOT_CONTENTS(tos-3), GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    w_long dividend = slots2w_long(tos-4);
    w_long divisor = slots2w_long(tos-2);
    w_long remainder;

    if (divisor == 0LL) {
      do_throw_clazz(clazzArithmeticException);
    }

    if (dividend == (w_long) 0x8000000000000000LL && divisor == -1LL) {
      remainder = 0LL;
    }
    else {
      remainder = dividend - divisor * (w_long) (dividend / divisor);
    }

    tos -= 2;
    w_long2slots(remainder, tos-2);
    woempa(1, "lrem : result = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_frem: {
    do_frem(&tos);
    do_next_opcode;
  }

  c_drem: {
    woempa(7, "drem : x = %08x %08x y = %08x %08x\n", GET_SLOT_CONTENTS(tos-4), GET_SLOT_CONTENTS(tos-3), GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    w_double2slots(do_drem(&tos), tos-2);
    woempa(7, "drem : result = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_ineg: {
    SET_SLOT_CONTENTS(tos-1, - (w_int) GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_lneg: {
    woempa(1, "lneg : x = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    w_long negation = -slots2w_long(tos-2);
    w_long2slots(negation, tos-2);
    woempa(1, "lneg : result = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_fneg: {
    union {w_float f; w_word w;} float_x;
    float_x.w = GET_SLOT_CONTENTS(tos-1);
    float_x.f = wfp_float32_negate(float_x.f);
    SET_SLOT_CONTENTS(tos-1, float_x.w);
    do_next_opcode;
  }

  c_dneg: {
    woempa(1, "dneg : x = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    w_double2slots(wfp_float64_negate(slots2w_double(tos-2)), tos-2);
    woempa(1, "dneg : result = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_ishl: {
    tos -= 1;
    SET_SLOT_CONTENTS(tos-1, GET_SLOT_CONTENTS(tos-1) << (GET_SLOT_CONTENTS(tos) & 0x0000001f));
    do_next_opcode;
  }

  c_lshl: {
    i = GET_SLOT_CONTENTS(tos-1) & 0x0000003f;
    tos -= 1;
    woempa(1, "lshl : x = %08x %08x, i = %d\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1), i);
    w_long shifted = slots2w_long(tos-2) << i;
    w_long2slots(shifted, tos-2);
    woempa(1, "lshl : result = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_ishr: {
    s = GET_SLOT_CONTENTS(tos-1) & 0x0000001f;
    i = GET_SLOT_CONTENTS(tos-2);
    tos -= 1;
    if (i > 0) {
      SET_SLOT_CONTENTS(tos-1, i >> s);
    }
    else {
      SET_SLOT_CONTENTS(tos-1, -1 - (( -1 - i) >> s));
    }
    do_next_opcode;
  }

  c_lshr: {
    i = GET_SLOT_CONTENTS(tos-1) & 0x0000003f;
    tos -= 1;
    woempa(1, "lshr : x = %08x %08x, i = %d\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1), i);
    w_long shiftee = slots2w_long(tos-2);
    w_long shifted = shiftee >> i;
    w_long2slots(shifted, tos-2);
    woempa(1, "lshr : result = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_iushr: {
    tos -= 1;
    SET_SLOT_CONTENTS(tos-1, GET_SLOT_CONTENTS(tos-1) >> (GET_SLOT_CONTENTS(tos) & 0x0000001f));
    do_next_opcode;
  }

  c_lushr: {
    i = GET_SLOT_CONTENTS(tos-1) & 0x0000003f;
    tos -= 1;
    woempa(1, "lushr : x = %08x %08x, i = %d\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1), i);
    w_long shiftee = slots2w_long(tos-2);
    w_long shifted = shiftee < 0LL ? ~((~shiftee) >> i) : shiftee >> i;
    w_long2slots(shifted, tos-2);
    woempa(1, "lushr : result = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }


  c_land: {
    woempa(1, "land : x = %08x %08x, y = %08x %08x\n", GET_SLOT_CONTENTS(tos-4), GET_SLOT_CONTENTS(tos-3), GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    w_long conj = slots2w_long(tos-4) & slots2w_long(tos-2);
    w_long2slots(conj, tos-4);
    tos -= 2;
    woempa(1, "land : result = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_ior: {
    tos -= 1;
    SET_SLOT_CONTENTS(tos-1, GET_SLOT_CONTENTS(tos-1) | GET_SLOT_CONTENTS(tos));
    do_next_opcode;
  }

  c_lor: {
    woempa(1, "lor : x = %08x %08x, y = %08x %08x\n", GET_SLOT_CONTENTS(tos-4), GET_SLOT_CONTENTS(tos-3), GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    w_long disj = slots2w_long(tos-4) | slots2w_long(tos-2);
    w_long2slots(disj, tos-4);
    tos -= 2;
    woempa(1, "lor : result = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_ixor: {
    tos -= 1;
    SET_SLOT_CONTENTS(tos-1, GET_SLOT_CONTENTS(tos-1) ^ GET_SLOT_CONTENTS(tos));
    do_next_opcode;
  }

  c_lxor: {
    woempa(1, "lor : x = %08x %08x, y = %08x %08x\n", GET_SLOT_CONTENTS(tos-4), GET_SLOT_CONTENTS(tos-3), GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    w_long xor = slots2w_long(tos-4) ^ slots2w_long(tos-2);
    w_long2slots(xor, tos-4);
    tos -= 2;
    woempa(1, "lor : result = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_i2l: {
    woempa(1, "i2l: x = %08x\n", GET_SLOT_CONTENTS(tos-1));
    w_long convert = (w_long)GET_SLOT_CONTENTS(tos-1);
    convert <<= 32;
    convert >>= 32;
    tos += 1;
    w_long2slots(convert, tos-2);
    woempa(1, "i2l : result = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_i2d: {
    union {w_double d; w_word w[2];} double_x;
    double_x.d = wfp_int32_to_float64((w_int)GET_SLOT_CONTENTS(tos-1));
    tos += 1;
    SET_SCALAR_SLOT(tos-2, double_x.w[0]);
    SET_SCALAR_SLOT(tos-1, double_x.w[1]);
    do_next_opcode;
  }

  c_f2l: {
    woempa(1, "f2l: x = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    union{w_float f; w_word w;} float_x;
    float_x.w = GET_SLOT_CONTENTS(tos-1);
    w_long convert = wfp_float32_is_NaN(float_x.f) ? 0LL : wfp_float32_to_int64_round_to_zero(float_x.f);
    tos += 1;
    w_long2slots(convert, tos-2);
    woempa(1, "l2f : result = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_f2d: {
    woempa(1, "f2d: x = %08x\n", GET_SLOT_CONTENTS(tos-1));
    union {w_float f; w_word w;} float_x;
    float_x.w = GET_SLOT_CONTENTS(tos-1);
    w_double double_x = slots2w_double(tos-2);
    tos += 1;
    w_double2slots(wfp_float32_to_float64(float_x.f), tos-2);
    woempa(1, "f2d : result = %08x %08x\n", GET_SLOT_CONTENTS(tos-1), GET_SLOT_CONTENTS(tos-2));
    do_next_opcode;
  }

  c_l2f: {
    woempa(1, "l2f: x = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    union {w_float f; w_word w;} float_x;
    float_x.f = wfp_int64_to_float32(slots2w_long(tos-2));
    SET_SLOT_CONTENTS(tos-2, float_x.w);
    woempa(1, "l2f : result = %08x\n", GET_SLOT_CONTENTS(tos-2));
    goto c_pop;
  }

  c_l2d: {
    woempa(1, "l2d: x = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    w_double2slots(wfp_int64_to_float64(slots2w_long(tos-2)), tos-2);
    woempa(1, "l2d : result = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_f2i: {
    union {w_float f; w_word w;} float_x;
    float_x.w = GET_SLOT_CONTENTS(tos-1);
    SET_SLOT_CONTENTS(tos-1, wfp_float32_is_NaN(float_x.f) ? 0 : wfp_float32_to_int32_round_to_zero(float_x.f));
    do_next_opcode;
  }

  c_d2i: {
    woempa(1, "d2i: x = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    w_double double_x = slots2w_double(tos-2);
    tos -= 1;
    SET_SLOT_CONTENTS(tos-1, wfp_float64_is_NaN(double_x) ? 0 : (w_int) wfp_float64_to_int32_round_to_zero(double_x));
    woempa(1, "d2i : result = %08x\n", GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_d2l: {
    woempa(1, "d2l: x = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    w_double d = slots2w_double(tos-2);
    w_long result = wfp_float64_is_NaN(d) ? 0LL : wfp_float64_to_int64_round_to_zero(d);
    w_long2slots(result, tos-2);
    woempa(1, "d2l : result = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_d2f: {
    woempa(1, "d2f: x = %08x %08x\n", GET_SLOT_CONTENTS(tos-2), GET_SLOT_CONTENTS(tos-1));
    w_double double_x = slots2w_double(tos-2);
    tos -= 1;
    SET_SLOT_CONTENTS(tos-1, wfp_float64_to_float32(double_x));
    woempa(1, "d2f : result = %08x\n", GET_SLOT_CONTENTS(tos-1));
    do_next_opcode;
  }

  c_i2b: {
    SET_SLOT_CONTENTS(tos-1, extend_b2i(GET_SLOT_CONTENTS(tos-1) & 0x000000ff));
    do_next_opcode;
  }


  c_i2s: {
    SET_SLOT_CONTENTS(tos-1, extend_s2i(GET_SLOT_CONTENTS(tos-1) & 0x0000ffff));
    do_next_opcode;
  }

  c_ifgt: {
    tos -= 1; 
    do_conditional((w_int) GET_SLOT_CONTENTS(tos)  > 0);
  }

  c_ifle: {
    tos -= 1; 
    do_conditional((w_int) GET_SLOT_CONTENTS(tos) <= 0);
  }

  c_if_icmpne: c_if_acmpne: {
    tos -= 2; 
    do_conditional((w_int) GET_SLOT_CONTENTS(tos) != (w_int) GET_SLOT_CONTENTS(tos+1));
  }
  
  c_if_icmpge: {
    tos -= 2; 
    do_conditional((w_int) GET_SLOT_CONTENTS(tos) >= (w_int) GET_SLOT_CONTENTS(tos+1));
  }
  
  c_if_icmpgt: {
    tos -= 2; 
    do_conditional((w_int) GET_SLOT_CONTENTS(tos)  > (w_int) GET_SLOT_CONTENTS(tos+1));
  }
  
  c_if_icmple: {
    tos -= 2; 
    do_conditional((w_int) GET_SLOT_CONTENTS(tos) <= (w_int) GET_SLOT_CONTENTS(tos+1));
  }

  c_jsr: {
    SET_SCALAR_SLOT(tos, (w_word) (current + 3));
    tos += 1;
    add_to_opcode(short_operand);
  }

  c_ret: {
    current = (w_code) GET_SLOT_CONTENTS(frame->jstack_base + *(current + 1));
    do_this_opcode;
  }

  c_tableswitch: {
    gcSafePoint(thread);
    operand_ptr = (w_code) ((w_word)(current + 4) & ~3);
    table = (w_int*) operand_ptr;
    tos -= 1;
     i = (w_int) GET_SLOT_CONTENTS(tos);
    if (i >= table[1] && i <= table[2]) {
      add_to_opcode(table[i - table[1] + 3]);
    }
    else {
      add_to_opcode(table[0]);
    }
  }

  c_lookupswitch: {
    gcSafePoint(thread);
    operand_ptr = (w_code)((w_word)(current + 4) & ~3);
    table = (w_int*) operand_ptr;
    // todo, make this a binary lookup instead of linear search...
    mopair = (w_Mopair *) (table + 2);

    tos -= 1;
    s = (w_int) GET_SLOT_CONTENTS(tos);

    for (i = 0; i < table[1]; i++) {
      if (mopair[i].m == s) {
        add_to_opcode(mopair[i].o);
      }
      if (mopair[i].m > s) {
        add_to_opcode(table[0]);
      }
    }
    add_to_opcode(table[0]);
  }

  c_lreturn: c_dreturn: {
    caller->jstack_top -= method->exec.arg_i;
    COPY_SLOT_TO_FROM(caller->jstack_top, tos-2);
    COPY_SLOT_TO_FROM(caller->jstack_top+1, tos-1);
    caller->jstack_top += 2;
    frame->jstack_top = tos;
    goto c_common_return;
  }

  c_vreturn: {
    caller->jstack_top -= method->exec.arg_i;
    frame->jstack_top = tos;
    goto c_common_return;
  }

  c_getstatic: {
    frame->jstack_top = tos;

    enterSafeRegion(thread);
    field = getFieldConstant(cclazz, short_operand);
    enterUnsafeRegion(thread);

    if (thread->exception) {
      do_the_exception;
    }
    // TODO - is this check really necessary?
    else if (!field) {
      do_throw_clazz(clazzLinkageError);
    }
    else if (isNotSet(field->flags, ACC_STATIC)) {
      do_throw_clazz(clazzIncompatibleClassChangeError);
    }

    enterSafeRegion(thread);
    mustBeInitialized(field->declaring_clazz);
    enterUnsafeRegion(thread);
    if (thread->exception) {
      do_the_exception;
    }

    if (isSet(field->flags, FIELD_IS_LONG)) {
      current[0] = in_getstatic_double;
    
      goto i_getstatic_double;
    }
    else if (isSet(field->flags, FIELD_IS_REFERENCE)) {
      current[0] = in_getstatic_ref;
    
      goto i_getstatic_ref;
    }
    else {
      current[0] = in_getstatic_single;
    
      goto i_getstatic_single;
    }
  }

  c_putstatic: {
    frame->jstack_top = tos;
    enterSafeRegion(thread);
    field = getFieldConstant(cclazz, (w_ushort) short_operand);

    enterUnsafeRegion(thread);

    if (thread->exception) {
      do_the_exception;
    }
    // TODO - is this check really necessary?
    else if (!field) {
      do_throw_clazz(clazzLinkageError);
    }
    else if (isNotSet(field->flags, ACC_STATIC)) {
      do_throw_clazz(clazzIncompatibleClassChangeError);
    }

    enterSafeRegion(thread);
    mustBeInitialized(field->declaring_clazz);
    enterUnsafeRegion(thread);
    if (thread->exception) {
      do_the_exception;
    }

    if (isSet(field->flags, FIELD_IS_LONG)) {
      current[0] = in_putstatic_double;
    
      goto i_putstatic_double;
    }
    else if (isSet(field->flags, FIELD_IS_REFERENCE)) {
      current[0] = in_putstatic_ref;
    
      goto i_putstatic_ref;
    }
    else {
      current[0] = in_putstatic_single;
    
      goto i_putstatic_single;
    }
  }

  c_getfield: {
    frame->jstack_top = tos;
    enterSafeRegion(thread);
    field = getFieldConstant(cclazz, (w_ushort) short_operand);
    enterUnsafeRegion(thread);

    if (thread->exception) {
      do_the_exception;
    }
    // TODO - is this check really necessary?
    else if (!field) {
      do_throw_clazz(clazzLinkageError);
    }
    else if (isSet(field->flags, ACC_STATIC)) {
      do_throw_clazz(clazzIncompatibleClassChangeError);
    }

    if (isSet(field->flags, FIELD_IS_REFERENCE)) {
      current[0] = in_getfield_ref;

      goto i_getfield_ref;
    }
    else {
      if (isSet(field->flags, FIELD_IS_LONG)) {
        current[0] = in_getfield_double;

        goto i_getfield_double;
      }
#ifdef PACK_BYTE_FIELDS
      else if ((field->size_and_slot & FIELD_SIZE_MASK) <= FIELD_SIZE_8_BITS) {
        current[0] = in_getfield_byte;

        goto i_getfield_byte;
      }
#endif
      else {
        current[0] = in_getfield_single;

        goto i_getfield_single;
      }
    }
  }

  c_putfield: {
    frame->jstack_top = tos;
    enterSafeRegion(thread);
    field = getFieldConstant(cclazz, (w_ushort) short_operand);
    enterUnsafeRegion(thread);

    if (thread->exception) {
      do_the_exception;
    }
    // TODO - is this check really necessary?
    else if (!field) {
      do_throw_clazz(clazzLinkageError);
    }
    else if (isSet(field->flags, ACC_STATIC)) {
      do_throw_clazz(clazzIncompatibleClassChangeError);
    }

    if (isSet(field->flags, FIELD_IS_REFERENCE)) {
      i = -i;
      current[0] = in_putfield_ref;

      goto i_putfield_ref;
    }
    else {
      if (isSet(field->flags, FIELD_IS_LONG)) {
        current[0] = in_putfield_double;

        goto i_putfield_double;
      }
#ifdef PACK_BYTE_FIELDS
      else if ((field->size_and_slot & FIELD_SIZE_MASK) <= FIELD_SIZE_8_BITS) {
        current[0] = in_putfield_byte;

        goto i_putfield_byte;
      }
#endif
      else {
        *current = in_putfield_single;

        goto i_putfield_single;
      }
    }
  }

  c_invokevirtual: {
    frame->jstack_top = tos;
    enterSafeRegion(thread);
    frame->current = current;
    x = getMethodConstant(cclazz, (w_ushort) short_operand);
    enterUnsafeRegion(thread);
    if (thread->exception) {
      do_the_exception;
    }

    if (isSet(x->flags, ACC_STATIC)) {
      do_throw_clazz(clazzIncompatibleClassChangeError);
    }

    if (isSet(x->flags, METHOD_NO_OVERRIDE) && x->exec.code) {
      woempa(1, "Replacing invokevirtual by invokenonvirtual for %M at [%d] in %M\n", x, current - method->exec.code, method);
      *current = in_invokenonvirtual;

      goto i_invokenonvirtual;

    }
    else {
      *current = in_invokevirtual;

      goto i_invokevirtual;

    }

  }

  i_invokevirtual: {
    w_instance objectref;

    frame->current = current;
    frame->jstack_top = tos;
    x = getResolvedMethodConstant(cclazz, (w_ushort) short_operand);

    objectref = (w_instance) GET_SLOT_CONTENTS(tos- x->exec.arg_i);
    if (! objectref) {
      do_throw_clazz(clazzNullPointerException);
    }

    clazz = instance2clazz(objectref);
    x = virtualLookup(x, clazz);
    if (!x) {
      do_the_exception;
    }
    if (isSet(x->flags, ACC_ABSTRACT)) {
      do_AbstractMethodError;
    }

    if (isNotSet(x->flags, METHOD_UNSAFE_DISPATCH)) {
      enterSafeRegion(thread);
      i_callMethod(frame, x);
      enterUnsafeRegion(thread);
    }
    else {
      i_callMethod(frame, x);
    }
    current += 2;
    tos = (w_Slot*)frame->jstack_top;
    goto check_async_exception;
  }

  c_invokespecial: {
    frame->jstack_top = tos;
    enterSafeRegion(thread);
    frame->current = current;
    x = getMethodConstant(cclazz, (w_ushort) short_operand);
    enterUnsafeRegion(thread);
    if (thread->exception) {
      do_the_exception;
    }
    if (isSet(x->flags, ACC_STATIC)) {
      do_throw_clazz(clazzIncompatibleClassChangeError);
    }

    if ((x->spec.declaring_clazz->flags & (ACC_FINAL | ACC_SUPER)) != ACC_SUPER || isSet(x->flags, ACC_PRIVATE) || isSet(x->flags, METHOD_IS_CONSTRUCTOR) || !isSuperClass(x->spec.declaring_clazz, getSuper(frame->method->spec.declaring_clazz))) {
      if (x->exec.arg_i < 4) {
        if (x->exec.code && x->exec.code[0] == aload_0 && x->exec.code[1] == areturn) {
          woempa(1, "zapping invokespecial %M at pc[%d] of %M (was: %d %d %d)\n", x, current - method->exec.code, method, current[0], current[1], current[2]);
          *current = x->exec.arg_i > 0 ? pop : nop;
          *(++current) = x->exec.arg_i > 1 ? pop : nop;
          *(++current) = x->exec.arg_i > 2 ? pop : nop;
          woempa(1, "zapped invokespecial %M at pc[%d] of %M (now: %d %d %d)\n", x, current - method->exec.code, method, current[0], current[1], current[2]);
          tos -= x->exec.arg_i - 1;
          do_next_opcode;
          // that's a goto, code below is not executed
        }
      }
      woempa(1, "Replacing invokespecial by invokenonvirtual for %M\n", x);
      *current = in_invokenonvirtual;

      goto i_invokenonvirtual;

    }
    else {
      *current = in_invokesuper;

      goto i_invokesuper;

    }
  }

  i_invokenonvirtual: {
    frame->current = current;
    frame->jstack_top = tos;
    x = getResolvedMethodConstant(cclazz, (w_ushort) short_operand);

    if (! GET_SLOT_CONTENTS(tos - x->exec.arg_i)) {
      do_throw_clazz(clazzNullPointerException);
    }

    if (isNotSet(x->flags, METHOD_UNSAFE_DISPATCH)) {
      enterSafeRegion(thread);
      i_callMethod(frame, x);
      enterUnsafeRegion(thread);
    }
    else {
      i_callMethod(frame, x);
    }
    current += 2;
    tos = (w_Slot*)frame->jstack_top;
    goto check_async_exception;
  }

  i_invokesuper: {
    w_clazz super = getSuper(frame->method->spec.declaring_clazz);

    if (!super) {
      do_throw_clazz(clazzIncompatibleClassChangeError);
    }

    frame->current = current;
    frame->jstack_top = tos;
    x = getResolvedMethodConstant(cclazz, (w_ushort) short_operand);
    x = virtualLookup(x, super);

    if (!x) {
      do_the_exception;
    }
    if (isSet(x->flags, ACC_ABSTRACT)) {
      clazz = super;
      do_AbstractMethodError;
    }

    if (! GET_SLOT_CONTENTS(tos - x->exec.arg_i)) {
      do_throw_clazz(clazzNullPointerException);
    }

    if (isNotSet(x->flags, METHOD_UNSAFE_DISPATCH)) {
      enterSafeRegion(thread);
      i_callMethod(frame, x);
      enterUnsafeRegion(thread);
    }
    else {
      i_callMethod(frame, x);
    }
    current += 2;
    tos = (w_Slot*)frame->jstack_top;
    goto check_async_exception;
  }

  i_invokefast: {
#ifdef RUNTIME_CHECKS
    w_slot p = frame->auxstack_top;
#endif
    frame->jstack_top = tos;
    fast_method_table[short_operand](frame);

/*
#ifdef RUNTIME_CHECKS
    if(p != frame->auxstack_top) {
      wabort(ABORT_WONKA, "Fast method %d grew aux stack by %d, top = %j\n",short_operand, p - frame->auxstack_top, frame->auxstack_top[-1]);
    }
#endif
*/

    if (thread->exception) {
      do_the_exception;
    }
    tos = (w_Slot*)frame->jstack_top;
    add_to_opcode(3);
  }

  c_invokestatic: {
    frame->jstack_top = tos;
    enterSafeRegion(thread);
    frame->current = current;
    x = getMethodConstant(cclazz, (w_ushort) short_operand);
    enterUnsafeRegion(thread);

    if (thread->exception) {
      do_the_exception;
    }

    if (!(isSet(x->flags, ACC_STATIC) && isNotSet(x->flags, METHOD_IS_CLINIT))) {
      do_throw_clazz(clazzIncompatibleClassChangeError);
    }

    if (thread->exception) {
      do_the_exception;
    }

    *current = in_invokestatic;
    
    goto i_invokestatic;
  }

  c_invokeinterface: {
    w_method   interf_method;

    frame->current = current;
    frame->jstack_top = tos;
    enterSafeRegion(thread);
    interf_method = getIMethodConstant(cclazz, (w_ushort) short_operand);
    if (interf_method) {
      mustBeInitialized(interf_method->spec.declaring_clazz);
    }
    enterUnsafeRegion(thread);
    if (thread->exception) {
      do_the_exception;
    }
    else if (!interf_method) {
      do_throw_clazz(clazzNoSuchMethodError);
    }

    *current = in_invokeinterface;

    goto i_invokeinterface;

  }

  i_invokeinterface: {
    w_method   interf_method;
    w_method   actual_method;
    w_instance objectref;
    w_clazz    actual_clazz;

    frame->current = current;
    frame->jstack_top = tos;
    interf_method = getResolvedIMethodConstant(cclazz, (w_ushort) short_operand);

    objectref = (w_instance) GET_SLOT_CONTENTS(tos- interf_method->exec.arg_i);

    if (! objectref) {
      do_throw_clazz(clazzNullPointerException);
    }

    woempa(1, "Object %j should implement %m\n", objectref, interf_method);
    actual_clazz = instance2object(objectref)->clazz;
    enterSafeRegion(thread);
    actual_method = interfaceLookup(interf_method, actual_clazz);
    enterUnsafeRegion(thread);

    if (thread->exception) {
      do_the_exception;
    }
    if (!actual_method || isSet(actual_method->flags, ACC_ABSTRACT)) {
      x = interf_method;
      clazz = actual_clazz;
      do_AbstractMethodError;
    }
    if (isSet(actual_method->flags, ACC_STATIC)) {
      do_throw_clazz(clazzIncompatibleClassChangeError);
    }
    if (isNotSet(actual_method->flags, ACC_PUBLIC)) {
      do_throw_clazz(clazzIllegalAccessError);
    }

    if (isNotSet(actual_method->flags, METHOD_UNSAFE_DISPATCH)) {
      enterSafeRegion(thread);
      i_callMethod(frame, actual_method);
      enterUnsafeRegion(thread);
    }
    else {
      i_callMethod(frame, actual_method);
    }
    current += 4;
    tos = (w_Slot*)frame->jstack_top;
    goto check_async_exception;
  }

  c_new: {
    frame->jstack_top = tos;
    enterSafeRegion(thread);
    clazz = getClassConstant(cclazz, (w_ushort) short_operand, thread);

    if (clazz) {
      mustBeInitialized(clazz);
      if(!thread->exception && isSet(clazz->flags, ACC_ABSTRACT | ACC_INTERFACE)) {
        throwException(thread, clazzInstantiationError, "%k", clazz);
      }
    }

    enterUnsafeRegion(thread);
    if (thread->exception) {
      do_the_exception;
    }

    *current = in_new;
    
    goto new_common;
  }


  c_newarray: {
    w_size bytes;
    s = GET_SLOT_CONTENTS(tos-1);

    if (s < 0) {
      do_throw_clazz(clazzNegativeArraySizeException);
    }

    clazz = atype2clazz[*(current + 1)];
    bytes = (F_Array_data + roundBitsToWords(clazz->previousDimension->bits * s)) * sizeof(w_word);
    if (!enough_free_memory(thread, bytes)) {
      do_OutOfMemoryError(bytes);
    }

    frame->jstack_top = tos;
    enterSafeRegion(thread);
    mustBeInitialized(clazz);
    enterUnsafeRegion(thread);
    if (thread->exception) {
      do_the_exception;
    }
    woempa(1, "Allocating array of %d %k\n", s, clazz->previousDimension);
    a = allocArrayInstance_1d(thread, clazz, (w_int)s);

    if (!a) {
      do_the_exception;
    }

    SET_REFERENCE_SLOT(tos-1, a);
    removeLocalReference(thread, a);
    current += 1;
    goto check_async_exception;
  }

  c_anewarray: {
    w_size bytes;
    s = GET_SLOT_CONTENTS(tos-1);
    if (s < 0) {
      do_throw_clazz(clazzNegativeArraySizeException);
    }
    bytes = (F_Array_data + s) * sizeof(w_word);
    if (!enough_free_memory(thread, bytes)) {
      do_OutOfMemoryError(bytes);
    }

    frame->jstack_top = tos;
    enterSafeRegion(thread);
    clazz = getClassConstant(cclazz, (w_ushort) short_operand, thread);

    if (clazz) {
      clazz = getNextDimension(clazz, clazz2loader(frame->method->spec.declaring_clazz));
      if (clazz) {
        mustBeInitialized(clazz);
      }
    }
    else if (isAssignmentCompatible(instance2clazz(thread->exception), clazzException)) {
      wrapException(thread,clazzNoClassDefFoundError, F_Throwable_cause);
    }

    enterUnsafeRegion(thread);
    if (thread->exception) {
      do_the_exception;
    }

    woempa(1, "Allocating array of %d %k\n", s, clazz->previousDimension);
    a = allocArrayInstance_1d(thread, clazz, s);
    if (!a) {
      do_the_exception;
    }

    SET_REFERENCE_SLOT(tos-1, a);
    removeLocalReference(thread, a);
    current += 2;
    goto check_async_exception;
  }

  c_athrow: {
    woempa(1, "athrow(%e)\n", GET_SLOT_CONTENTS(tos-1));
/*
    { // [CG 20071130] Check we don't have any monitors left - see JVMS 8.13
      w_slot base = frame->auxstack_base;
      w_slot slot = frame->auxstack_top;
      for (slot = slot + 1; base - slot >= 0; ++slot) {
        if (isMonitoredSlot(slot)) {
          do_throw_clazz(clazzIllegalMonitorStateException);
        }
      }
    }
*/
    if (GET_SLOT_CONTENTS(tos-1) == 0) {
      do_throw_clazz(clazzNullPointerException);
    }
    thread->exception = (w_instance) GET_SLOT_CONTENTS(tos-1);
    frame->jstack_top = tos;
    do_the_exception;
  }

  c_instanceof: {
    frame->jstack_top = tos;
    enterSafeRegion(thread);
    clazz = getClassConstant(cclazz, (w_ushort) short_operand, thread);
    enterUnsafeRegion(thread);
    if (thread->exception) {
      do_the_exception;
    }
    // TODO: make isAssignmentCompatible() GC-safe (means using constraints)
    enterSafeRegion(thread);
    o = (w_instance) GET_SLOT_CONTENTS(tos-1);
    SET_SCALAR_SLOT(tos-1, (o == NULL) ? 0 : (isAssignmentCompatible(instance2object(o)->clazz, clazz) ? 1 : 0));
    enterUnsafeRegion(thread);
    add_to_opcode(3);
  }


  c_wide: {
    current += 1;
    s = *current;
    i = (w_int) short_operand;
    
    switch (s) {
      case iload:  
      case fload: 
        COPY_SLOT_TO_FROM(tos, frame->jstack_base+i);
        tos += 1;
        add_to_opcode(3);

      case aload: 
        COPY_SLOT_TO_FROM(tos, frame->jstack_base+i);
	++tos;
        add_to_opcode(3);

      case lload:  
      case dload: 
        do_zload(frame, i, &tos); 
        add_to_opcode(3);

      case istore: 
      case fstore: 
        tos -= 1;
        COPY_SLOT_TO_FROM(frame->jstack_base+i, tos);
        add_to_opcode(3);

      case astore:
        do_astore(frame, i, &tos); 
        add_to_opcode(3);

      case lstore: 
      case dstore: 
        do_zstore(frame, i, &tos); 
        add_to_opcode(3);

      case ret: 
        current = (w_code) GET_SLOT_CONTENTS(frame->jstack_base + i); 
        do_this_opcode;

      case iinc:
        current += 2;
        SET_SLOT_CONTENTS(frame->jstack_base + i, GET_SLOT_CONTENTS(frame->jstack_base + i) + (w_int) short_operand);
        current += 3;
        do_this_opcode;
    }
  }

  c_multianewarray: {
    w_ubyte ndims;
    w_int * dimensions;
    w_boolean enough;
    w_size bytes;
    w_clazz element_clazz;

    frame->jstack_top = tos;
    ndims = current[3];
// CG 20230422 I guess this is the intention?
    s = ndims;
//
    dimensions = allocMem(sizeof(w_int) * ndims);
    if (!dimensions) {
      do_the_exception;
    }
    for (i = 0; i < s; i++) {
      dimensions[i] = GET_SLOT_CONTENTS(tos - ndims + i);
      if (dimensions[i] < 0) {
        releaseMem(dimensions);
        do_throw_clazz(clazzNegativeArraySizeException);
      }
    }
    tos -= ndims;

    frame->jstack_top = tos;
    enterSafeRegion(thread);
    clazz = getClassConstant(cclazz, (w_ushort) short_operand, thread);
    if (clazz) {
      mustBeInitialized(clazz);
    }
    else if (isAssignmentCompatible(instance2clazz(thread->exception), clazzException)) {
      wrapException(thread,clazzNoClassDefFoundError, F_Throwable_cause);
    }
    enterUnsafeRegion(thread);
    if (thread->exception) {
      do_the_exception;
    }

    {
      bytes = 1;
      element_clazz = clazz->previousDimension;
      for (i = 0; i < ndims - 1; ++i) {
        bytes *= F_Array_data + dimensions[i];
        element_clazz = element_clazz->previousDimension;
      }
      bytes *= (F_Array_data + roundBitsToWords(element_clazz->bits * dimensions[ndims - 1])) * sizeof(w_word);
      enough = enough_free_memory(thread, bytes);
    }
    if (!enough) {
      releaseMem(dimensions);
      do_OutOfMemoryError(-1);
    }
    if (thread->exception) {
      releaseMem(dimensions);
      do_the_exception;
    }

    a = allocArrayInstance(thread, clazz, s, dimensions);
    releaseMem(dimensions);
    if (!a) {
      do_the_exception;
    }

    SET_REFERENCE_SLOT(tos, a);
    tos += 1;
    removeLocalReference(thread, a);
    current += 3;
    do_next_opcode;
  }

  c_goto_w: {
    gcSafePoint(thread);
    add_to_opcode(int_operand);
  }

  c_jsr_w: {
    SET_SCALAR_SLOT(tos, (w_word) (current + 5));
    tos += 1;
    add_to_opcode(int_operand);
  }

  c_breakpoint: {
#ifdef JDWP
    w_ubyte original_bytecode;

    frame->current = current;
    enterSafeRegion(thread);
    original_bytecode = jdwp_event_breakpoint(current);
    enterUnsafeRegion(thread);
    goto * jumps[original_bytecode];
#endif
  }

#ifndef PACK_BYTE_FIELDS
  i_getfield_byte:
  i_putfield_byte:
#endif
#ifndef JAVA5
  i_ldc_class:
  i_ldc_w_class:
#endif
  c_nocode_219: 
  c_nocode_230: 
  c_nocode_231: c_nocode_232: c_nocode_233: c_nocode_234: c_nocode_235: c_nocode_236: c_nocode_237:
  c_nocode_238: c_nocode_239: c_nocode_240: c_nocode_241: c_nocode_242: c_nocode_243: c_nocode_244:
  c_nocode_245: c_nocode_246: c_nocode_247: c_nocode_248: c_nocode_249: c_nocode_250: c_nocode_251:
  c_nocode_252: c_nocode_253: wabort(ABORT_WONKA, "Illegal opcode %d in %M at [%d]\n", *current, frame->method, current - method->exec.code);

  /*
  ** Exception logic:
  ** c_AbstractMethodError: throws an AbstractMethodError with a message
  ** describing the offending method and jumps to c_find_handler.
  */

  c_AbstractMethodError: 
    {
      // Assuming that frame->jstack_top is up to date
      enterSafeRegion(thread);
      throwException(thread, clazzAbstractMethodError, "%M in %K", x, clazz);
      enterUnsafeRegion(thread);
      frame->current = current;
      frame->jstack_top = tos;
    }
    goto c_find_handler;

  /*
  ** c_OutOfMemoryError: throws an OutOfMemoryError with a message
  ** describing the amount to be allocated and jumps to c_find_handler.
  */

  c_OutOfMemoryError: 
    {
      // Assuming that frame->jstack_top is up to date
      enterSafeRegion(thread);
      throwOutOfMemoryError(thread, i);
      enterUnsafeRegion(thread);
      frame->current = current;
      frame->jstack_top = tos;
    }
    goto c_find_handler;

  /*
  ** c_clazz2exception: allocates the exeception instance given by clazz 'c' and falls through to c_exception
  */
  c_clazz2exception: {
    threadMustBeUnsafe(thread);
    if (!thread->exception) {
      frame->jstack_top = tos;
#ifdef RUNTIME_CHECKS
      if (getClazzState(clazz) != CLAZZ_STATE_INITIALIZED) {
        wabort(ABORT_WONKA, "'s Blood! Throwing uninitialized %k\n", clazz);
      }
#endif
      thread->exception = allocThrowableInstance(thread, clazz);
      if (thread->exception) {
        removeLocalReference(thread, thread->exception);
      }
    }
  }
  
  /*
  **       c_exception: (opcode at -1) records the current opcode in the frame, falls through to c_find_handler
  */
  c_exception: {
    throwExceptionInstance(thread, thread->exception);
    frame->current = current;
  }
  
  /*
  **    c_find_handler: tries to find a handler, handler returns correct code pointer or code pointer for 'c_no_handler'
  */
  c_find_handler: {
    frame->jstack_top = tos;
    current = searchHandler(frame);
    tos = (w_Slot*)frame->jstack_top;
    do_next_opcode;
  }

  /*
  **      c_no_handler: (opcode at -2) is called when there is no handler and returns
  */
  c_no_handler: {
    frame->auxstack_top = frame->auxstack_base;
    goto c_common_return;
  }

  c_common_return:
    // [CG 20230530] suppressing structured locking tests for now
    /*
    { // [CG 20071114] Check we don't have any monitors left - see JVMS 8.13
      w_slot base = frame->auxstack_base;
      w_slot slot = frame->auxstack_top;
      for (slot = slot + 1; base - slot >= 0; ++slot) {
        if (isMonitoredSlot(slot)) {
          do_throw_clazz(clazzIllegalMonitorStateException);
        }
      }
    }
    */
    thread->top = caller;
    checkSingleStep2(frame);
    if (from_unsafe) {
      woempa(1, "GC state on exit from %M remains UNSAFE\n", method);
    }
    else {
      woempa(1, "GC state on exit from %M restored to SAFE\n", method);
      enterSafeRegion(thread);
    }

}

void do_frem(w_Slot **tosptr) {
  union {w_float f; w_word w;} float_x;
  union {w_float f; w_word w;} float_y;
  union {w_float f; w_word w;} result;

  float_y.w = GET_SLOT_CONTENTS((*tosptr) - 1);
  float_x.w = GET_SLOT_CONTENTS((*tosptr) - 2);

  if (wfp_float32_is_NaN(float_x.f) || wfp_float32_is_NaN(float_y.f) || wfp_float32_eq(float_y.f , F_ZERO) || wfp_float32_is_Infinite(float_x.f)) {
    result.f = F_NAN;
  }
  else {
    if (wfp_float32_eq(float_x.f , F_ZERO) || wfp_float32_is_Infinite(float_y.f)) {
      result.f = float_x.f;
    }
    else   {
      result.f = wfp_float32_mul(wfp_float32_abs(float_y.f), F_FLOAT_MAX_VALUE);
      if (wfp_float32_lt(result.f, wfp_float32_abs(float_x.f))) {
        woempa(1, "fmod: division would overflow, returning zero as remainder.\n");
        if (wfp_float32_is_negative(float_x.f)) {
          result.f = F_MINUS_ZERO;
        }
        else {
          result.f = F_ZERO;
        }
      }
      else {
        result.f = wfp_float32_div(float_x.f, float_y.f);
        result.f = wfp_int32_to_float32(wfp_float32_to_int32_round_to_zero(result.f));
        result.f = wfp_float32_mul(float_y.f, result.f);
        result.f = wfp_float32_sub(float_x.f, result.f);
      }
    }
  }

  (*tosptr) -= 1;
  SET_SLOT_CONTENTS((*tosptr) - 1, result.w);

}

w_double do_drem(w_Slot **tosptr) {
  w_double double_x = slots2w_double(*tosptr-4);
  w_double double_y = slots2w_double(*tosptr-2);
  w_double double_z = wfp_float64_div(double_x, double_y);
    w_double quotient = wfp_float64_div(slots2w_double(*tosptr-4), slots2w_double(*tosptr-2));
  *tosptr -= 2;
  if (wfp_float64_is_NaN(double_x) || wfp_float64_is_NaN(double_y) || wfp_float64_eq(double_y, D_ZERO) || wfp_float64_is_Infinite(double_x)) {
    return D_NAN; 
  }               

  if (wfp_float64_eq(double_x, D_ZERO) || wfp_float64_is_Infinite(double_y)) {
    return double_x;
  }

  double_z = wfp_float64_mul(wfp_float64_abs(double_y), D_DOUBLE_MAX_VALUE);
  if (wfp_float64_lt(double_z, wfp_float64_abs(double_x))) {
    woempa(7, "drem: division would overflow, returning zero as remainder.\n");
    if (wfp_float64_is_negative(double_x)) {
      return D_MINUS_ZERO;
    }
    else {
      return D_ZERO;
    }
  }

  double_z = wfp_float64_div(double_x, double_y);

  /*
  ** Rounding is not what we would like since it doesn't round to zero ...
  */

  double_z = wfp_float64_round_to_int(double_z);
  double_z = wfp_float64_mul(double_y, double_z);
  double_z = wfp_float64_sub(double_x, double_z);

  /*
  ** The sign of result must be equal to sign of n this is not guaranteed
  ** anymore because of the rounding.
  */

  if (wfp_float64_signBit(double_x) && (! wfp_float64_signBit(double_z) && double_z)) {
    if (wfp_float64_signBit(double_y)){
      return wfp_float64_add(double_y, double_z);
    }
    else{
      return wfp_float64_sub(double_z, double_y);
    }
  }

  if ((! wfp_float64_signBit(double_x)) && wfp_float64_signBit(double_z) && double_z) {
    if (wfp_float64_signBit(double_y)){
      return wfp_float64_sub(double_z, double_y);
    }
    else{
      return wfp_float64_add(double_y, double_z);
    }
  }

  return double_z;

/* WAS:
  union {w_double d; w_word w[2];} double_x;
  union {w_double d; w_word w[2];} double_y;
  union {w_double d; w_word w[2];} double_z;
  double_x.w[0] = (*tosptr)[-4].c;
  double_x.w[1] = (*tosptr)[-3].c;
  double_y.w[0] = (*tosptr)[-2].c;
  double_y.w[1] = (*tosptr)[-1].c;
  *tosptr -= 2;

  double_z.d = wfp_float64_div(double_x.d, double_y.d);

  if (wfp_float64_is_NaN(double_x.d) || wfp_float64_is_NaN(double_y.d) || wfp_float64_eq(double_y.d, D_ZERO) || wfp_float64_is_Infinite(double_x.d)) {
    double_z.d = D_NAN; 
  }               
  else {        
    if (wfp_float64_eq(double_x.d, D_ZERO) || wfp_float64_is_Infinite(double_y.d)) {
      double_z.d = double_x.d;
    }
    else {
      double_z.d = wfp_float64_mul(wfp_float64_abs(double_y.d), D_DOUBLE_MAX_VALUE);
      if (wfp_float64_lt(double_z.d, wfp_float64_abs(double_x.d))) {
        woempa(1, "dmod: division would overflow, returning zero as remainder.\n");
        if (wfp_float64_is_negative(double_x.d)) {
          double_z.d = D_MINUS_ZERO;
        }
        else {
          double_z.d = D_ZERO;
        }
      }
      else {
        double_z.d = wfp_float64_div(double_x.d, double_y.d);

        /.
        .. Rounding is not what we would like since it doesn't round to zero ...
        ./

        double_z.d = wfp_float64_round_to_int(double_z.d);
        double_z.d = wfp_float64_mul(double_y.d, double_z.d);
        double_z.d = wfp_float64_sub(double_x.d, double_z.d);

        /.
        .. The sign of result must be equal to sign of n this is not guaranteed
        .. anymore because of the rounding.
        ./

        if (wfp_float64_signBit(double_x.d) && (! wfp_float64_signBit(double_z.d) && double_z.d)) {
          if (wfp_float64_signBit(double_y.d)){
            double_z.d = wfp_float64_add(double_y.d, double_z.d);
          }
          else{
            double_z.d = wfp_float64_sub(double_z.d, double_y.d);
          }
        }
        else if ((! wfp_float64_signBit(double_x.d)) && wfp_float64_signBit(double_z.d) && double_z.d) {
          if (wfp_float64_signBit(double_y.d)){
            double_z.d = wfp_float64_sub(double_z.d, double_y.d);
          }
          else{
            double_z.d = wfp_float64_add(double_y.d, double_z.d);
          }
        }
      }
    }
  }

  (*tosptr)[-2].c = double_z.w[0];
  (*tosptr)[-1].c = double_z.w[1];
*/
}

#ifdef JDWP
extern void jdwp_event_exception(w_instance throwable, w_method catch_method, w_int catch_pc);
#endif

static w_code searchHandler(w_frame frame) {

  w_thread thread = frame->thread;
  w_exception ex;
  w_clazz cc;
  w_int i;
  w_slot auxs;
  w_int pc = frame->current - frame->method->exec.code;
  w_instance pending;
  w_code nohandler = frame->method->exec.code - 3;

  threadMustBeUnsafe(thread);
  /*
  ** Store the pending exception locally and clear the thread exception, since
  ** resolving the class constant could result in loading/initializing etc. and
  ** that will check for pending exceptions. When we don't handle the exception
  ** ourselves, we reassign pending to the thread exception field. When resolving
  ** the catched clazz results itself in an exception, we don't handle anything, it will
  ** propagate probably to the final thread handler in the sky.
  */
  
  if (isSet(verbose_flags, VERBOSE_FLAG_THROW)) {
    w_printf("Thrown: Seeking handler for %e in %M, thread %t\n", thread->exception, frame->method, thread);
  }
  woempa(7, "Seeking handler for %k in %t, current frame is running %M\n", instance2clazz(thread->exception), thread, frame->method);
  threadMustBeUnsafe(thread);
  auxs = (w_slot)frame->auxstack_top;
  pending = thread->exception;
  pushLocalReference(frame, pending);
  thread->exception = NULL;

  for (i = 0; i <  frame->method->exec.numExceptions; i++) {
      ex = &frame->method->exec.exceptions[i];
      if (ex->type_index) {
        enterSafeRegion(thread);
        cc = getClassConstant(frame->method->spec.declaring_clazz, ex->type_index, thread);
        enterUnsafeRegion(thread);
/* [CG 20080206] This can never be executed surely?
        if (thread->exception) {
          pending = thread->exception;
          pushLocalReference(frame, pending);
          break;
        }
*/
      }
      else {
        cc = NULL;
      }
      if (pc >= ex->start_pc && pc < ex->end_pc) {
        if (cc == NULL || isSuperClass(cc, instance2object(pending)->clazz)) {
          if (isSet(verbose_flags, VERBOSE_FLAG_THROW)) {
            w_printf("Thrown: Catching %e in %M, thread %t\n", pending, frame->method, thread);
          }
          woempa(7, ">>>> Found a handler for %j (as %k) at pc = %d in method %M <<<<\n",
              pending, cc, ex->handler_pc, frame->method);
          frame->jstack_top = frame->jstack_base + frame->method->exec.local_i;
          SET_REFERENCE_SLOT(frame->jstack_top, pending);
          frame->jstack_top += 1;
#ifdef JDWP
          enterSafeRegion(thread);
          jdwp_event_exception(pending, frame->method, ex->handler_pc);
          enterUnsafeRegion(thread);
#endif
          setReferenceField_unsafe(thread->Thread, NULL, F_Thread_thrown);
          thread->exception = NULL;
          frame->auxstack_top = auxs;
          return frame->method->exec.code + ex->handler_pc - 1;
        }
      }
  }

  thread->exception = pending;

  setReferenceField_unsafe(thread->Thread, pending, F_Thread_thrown);

  if (isSet(verbose_flags, VERBOSE_FLAG_THROW)) {
    w_printf("Thrown: Propagating %e in %M, thread %t\n", thread->exception, frame->method, thread);
  }
  frame->auxstack_top = auxs;

  return nohandler;
  
}

void dumpcalls(w_frame frame) {

  w_frame current = frame;
  w_int i = 0;
  
  while (current) {
    if (current->method) {
      woempa(9, "%2d method %m clazz %k\n", i, current->method, current->method->spec.declaring_clazz);
    }
    else {
      woempa(9, "%2d ------------------\n", i);
    }
    i += 1;
    current = current->previous;
  }

}

w_frame pushFrame(w_thread thread, w_method method) {

  w_frame frame;

  /*
  ** Add at least 2 slots for an eventual 64 bit return value.
  */

  frame = allocClearedMem(sizeof(w_Frame));
//  frame->jstack_base = allocClearedMem(sizeof(w_Slot) * (method->exec.arg_i + method->exec.stack_i + 2));

  woempa(1,"Calling %M from %t using frame at %p (previous at %p)\n", method, thread, frame, thread->top);

  if (frame) {
    frame->label = "frame";
    frame->jstack_base = thread->top->jstack_top; 
    frame->jstack_top = frame->jstack_base;
    frame->auxstack_base = thread->top->auxstack_top; 
    frame->auxstack_top = frame->auxstack_base;
    frame->previous = thread->top;
    frame->thread = thread;
    frame->method = method;
    frame->current = method->exec.code;
    frame->flags = isSet(method->flags, ACC_NATIVE) ? FRAME_NATIVE : 0;
#ifdef TRACE_CLASSLOADERS
  { 
   // N.B. If method is virtual then the caller should overwrite the static udcl
   // with the appropriate runtime one ...
    w_instance loader = method->spec.declaring_clazz->loader;
    if (loader && !getBooleanField(loader, F_ClassLoader_systemDefined)) {
      frame->udcl = loader;
    }
    else {
      frame->udcl = thread->top->udcl;
    }
  }
#endif

    thread->top = frame;
    woempa(1, "%t jstack_base = jstack_top = %p, auxstack_base = auxstack_top = %p\n", frame->jstack_top, frame->auxstack_top);
  }

  return frame;

}

w_frame activateFrame(w_thread thread, w_method method, w_word flags, w_int nargs, ...) {

  va_list args;
  w_frame frame = pushFrame(thread, method);
  w_int i = 0;

  threadMustBeSafe(thread);
  if (frame) {
    va_start(args, nargs);
    while (i < nargs) {
      // [CG 20230531] Doing it this way so that the slot-type arguments will be consumed even if they are not used.
      w_word contents = va_arg(args, w_word);
      w_word scanning = va_arg(args, w_word);
      //
      SET_SLOT_CONTENTS(frame->jstack_top, contents);
      SET_SLOT_SCANNING(frame->jstack_top, scanning);
      frame->jstack_top += 1;
      i += 1;
    }
    va_end(args);

    frame->flags |= flags;
    i_callMethod(frame, method);
  }
  
  return frame;

}

void deactivateFrame(w_frame frame, w_instance protect) {

  w_frame newtop = frame->previous;
  w_thread thread = frame->thread;

  thread->protected = protect;
  thread->top = newtop;

//  releaseMem(frame->jstack_base);
  releaseMem(frame);

}
