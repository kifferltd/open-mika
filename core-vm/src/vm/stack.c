/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved.                                                               *
* Parts copyright (c) 2004, 2005, 2006, 2007, 2011 by Chris Gray,         *
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

#undef debug

#include <string.h>

#include "clazz.h"
#include "core-classes.h"
#include "descriptor.h"
#include "dispatcher.h"
#include "exception.h"
#include "hashtable.h"
#include "wordset.h"
#include "ts-mem.h"
#include "methods.h"
#include "wstrings.h"
#include "locks.h"
#include "heap.h"
#include "mika_threads.h"

#include <assert.h>

w_frame getCurrentFrame(w_thread thread) {

  w_frame frame = thread->top;
  
  if (frame && frame->method == NULL) {
    frame = frame->previous;
  }
    
  if (frame) {
    frame = frame->previous;
  }

  if (frame && frame->method == NULL) {
    frame = frame->previous;
  }
 
  return frame;
}

w_frame getCallingFrame(w_thread thread) {

  w_frame frame = thread->top;
  
  if (frame && frame->method == NULL) {
    frame = frame->previous;
  }
    
  if (frame) {
    frame = frame->previous;
  }

  if (frame && frame->method == NULL) {
    frame = frame->previous;
  }
 
  if (frame) {
    frame = frame->previous;
  }

  if (frame && frame->method == NULL) {
    frame = frame->previous;
  }
 
  return frame;
}

// get current Java method
w_method getCurrentMethod(w_thread thread) {

  w_frame frame = getCurrentFrame(thread);

  if (frame) {
    return frame->method;
  }
  
  return NULL;

}

w_method getCallingMethod(w_thread thread) {

  w_frame frame = getCallingFrame(thread);
  
  if (frame) {
    return frame->method;
  }
  
  return NULL;

}


w_clazz getCurrentClazz(w_thread thread) {

  w_method current = getCurrentMethod(thread);

  if (current) {
    woempa(1, "Current clazz is %k.\n", current->spec.declaring_clazz);
    return current->spec.declaring_clazz;
  }
  else {
    return NULL;
  }

}


w_clazz getCallingClazz(w_thread thread) {

  w_method calling = getCallingMethod(thread);

  if (calling) {
    woempa(1, "Calling clazz is %k.\n", calling->spec.declaring_clazz);
    return calling->spec.declaring_clazz;
  }

  return NULL;

}

w_instance getCurrentInstance(w_thread thread) {

  w_frame frame = getCurrentFrame(thread);

  if (frame && isNotSet(frame->method->flags, ACC_STATIC)) {
    return (w_instance) frame->jstack_base[0].c;
  }

  return NULL;

}

w_instance getCallingInstance(w_thread thread) {

  w_frame frame = getCallingFrame(thread);
  
  if (frame && isNotSet(frame->method->flags, ACC_STATIC)) {
    return (w_instance) frame->jstack_base[0].c;
  }
  
  return NULL;

}

inline static void i_pushLocalReference(w_frame frame, w_instance instance) {
  w_thread thread = frame->thread;
  w_boolean unsafe;

  if (thread == marking_thread) {
    return;
  }

  if (!instance) {
    printf("pushLocalReference() - asked to push null, ignoring\n");
    return;
  }

  unsafe = enterUnsafeRegion(thread);

  if (frame->auxstack_top > last_slot(thread)) {
    wabort(ABORT_WONKA, "Local reference stack underflow detected in frame (%M) of %t when pushing local ref : %p > %p.", frame->method, thread, frame->auxstack_top, last_slot(thread));
  }

  if (frame->auxstack_top - frame->jstack_top < 1) {
    throwException(thread, clazzStackOverflowError, "unable to push local reference: %d on aux stack, %d on java stack", thread->slots + SLOTS_PER_THREAD - frame->auxstack_top, frame->jstack_top - thread->slots);
  }
  else {
    woempa(1, "Pushing %j as aux[%d] of %t\n", instance, last_slot(thread) - frame->auxstack_top, thread);
    frame->auxstack_top[0].c = (w_word) instance;
    frame->auxstack_top[0].s = stack_trace;
    frame->auxstack_top -= 1;

    setFlag(instance2flags(instance), O_BLACK);
  }

  if (!unsafe) {
    enterSafeRegion(thread);
  }
}

void pushMonitoredReference(w_frame frame, w_instance instance, x_monitor monitor) {
  w_thread thread = frame->thread;
  w_boolean unsafe = enterUnsafeRegion(thread);

  if (frame->auxstack_top > last_slot(thread)) {
    wabort(ABORT_WONKA, "Local reference stack underflow detected in frame (%M) of %t when pushing local ref : %p > %p.", frame->method, thread, frame->auxstack_top, last_slot(thread));
  }

  if (frame->auxstack_top - frame->jstack_top < 1) {
    throwException(thread, clazzStackOverflowError, "aux stack full");
  }
  else {
    woempa(1, "Pushing %j as aux[%d] of %t\n", instance, last_slot(thread) - frame->auxstack_top, thread);
    frame->auxstack_top[0].c = (w_word) instance;
    frame->auxstack_top[0].s = (w_word) monitor;
    frame->auxstack_top -= 1;
  }

  if (!unsafe) {
    enterSafeRegion(thread);
  }
}

void pushLocalReference(w_frame frame, w_instance instance) {
  i_pushLocalReference(frame, instance);
}

void addLocalReference(w_thread thread, w_instance instance) {
  if (thread) {
    i_pushLocalReference(thread->top, instance);
  }
}

void removeLocalReference(w_thread thread, w_instance instance) {

  w_frame frame = thread->top;
  w_slot slot;

  w_boolean unsafe = enterUnsafeRegion(thread);

  woempa(1, "Removing %j from auxs of %t\n", instance, thread);
  for (slot = (w_slot)frame->auxstack_top + 1; slot <= frame->auxstack_base; ++slot) {
    if (slot->c == (w_word) instance && slot->s == stack_trace) {
      woempa(1, "  - is aux[%d]\n", last_slot(thread) - slot);
      slot->s = stack_notrace;
      break;
    }
  }
  while (frame->auxstack_top < frame->auxstack_base && frame->auxstack_top[1].s == stack_notrace) {
    frame->auxstack_top += 1;
    woempa(1, "  - skipped a zombie, now have %d auxs\n", last_slot(frame->thread) - frame->auxstack_top);
  }

  if (!unsafe) {
    enterSafeRegion(thread);
  }
}

/*
void stacktrace(void) {

  w_thread thread = currentWonkaThread;
  w_frame frame;
  w_int i = 0;

  for (frame = thread->top; frame; frame = frame->previous) {
    if (frame->method) {
      woempa(9, "%2d -> %M\n", i, frame->method);
    }
    else {
      woempa(9, "%2d -> arguments frame\n", i);
    }
    i += 1;
  }  
*/
 
