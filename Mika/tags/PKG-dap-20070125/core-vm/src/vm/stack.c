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
* Modifications copyright (c) 2004, 2005, 2006 by Chris Gray,             *
* /k/ Embedded Java Solutions. All rights reserved.                       *
*                                                                         *
**************************************************************************/

/*
** $Id: stack.c,v 1.10 2006/10/04 14:24:17 cvsroot Exp $
*/

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
#include "threads.h"

#ifdef JSPOT
  #include "hotspot.h"
#endif

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
 
