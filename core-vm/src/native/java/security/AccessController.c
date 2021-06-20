/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2004, 2005, 2006 by Chris Gray, /k/ Embedded Java   *
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

#include "arrays.h"
#include "core-classes.h"
#include "descriptor.h"
#include "interpreter.h"
#include "jni.h"
#include "loading.h"
#include "wstrings.h"
#include "mika_threads.h"
#include "wordset.h"
#include "fields.h"
#include "methods.h"
#include "exception.h"
#include "wonka.h"
#include "wordset.h"

w_instance do_privileged_action(w_thread thread, w_instance action, w_instance inherited_context) {

  w_frame frame;
  w_instance result = NULL;

// TODO: if called by reflection, set PRIVILEGED flag on the frame which
// invoked reflection, not that of Method.reflect() ...

  frame = activateFrame(thread, instance2clazz(action)->runner, FRAME_PRIVILEGED, 1, action, stack_trace);
  woempa(1, "(PRIVILEGED) Starting frame with %M, parameter is %j.\n", instance2clazz(action)->runner, action);
  if (exceptionThrown(thread)) {
    woempa(1, "(PRIVILEGED) Done calling %M, exception %k was thrown.\n", instance2clazz(action)->runner, instance2clazz(exceptionThrown(thread)));
  }
  else{
    result = (w_instance) frame->jstack_top[-1].c;
  }
  deactivateFrame(frame, result);
  woempa(1, "(PRIVILEGED) Done calling %M, returned %p.\n", instance2clazz(action)->runner, result);

  return result;

}

static w_clazz    clazzArrayOf_ProtectionDomain;

void AccessController_static_initialize(JNIEnv *env, w_instance classAccessController) {

  w_string string_ArrayOf_ProtectionDomain  = cstring2String("[java.security.ProtectionDomain", 31);

  clazzArrayOf_ProtectionDomain = namedArrayClassMustBeLoaded(NULL, string_ArrayOf_ProtectionDomain);
  // TODO: handle exception
  mustBeInitialized(clazzArrayOf_ProtectionDomain);
  // TODO: handle exception
}

w_instance 
AccessController_static_doPrivileged0
( JNIEnv *env, w_instance classAccessController, 
  w_instance action, w_instance context
) {
  w_thread thread = JNIEnv2w_thread(env);
  w_clazz  clazz = instance2clazz(action);
  w_instance result;

  
  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {
    result = NULL;
  }
  else {
    result = do_privileged_action(thread, action, context);
  }


  return result;
}

w_instance AccessController_static_get_calling_domains(JNIEnv *env, w_instance AccessController) {

  w_thread   thread = JNIEnv2w_thread(env);
  w_frame    current;
  w_int      count;
  w_wordset  domains = NULL;
  w_instance result;
  w_instance *result_array;
  w_int      item;
  w_int      i;
  w_instance domain;

  current = thread->top;

  threadMustBeSafe(thread);
  while (current) {
    if (current->method) {
      domain = getReferenceField(clazz2Class(current->method->spec.declaring_clazz), F_Class_domain);
      if (domain && !isInWordset(&domains, (w_word)domain)) {
        if (!addToWordset(&domains, (w_word)domain)) {
          wabort(ABORT_WONKA, "Unable to add domain to wordset\n");
        }
      }
      else {
        woempa(1,"Frame %p (%M) has no associated protection domain, skipping\n",current, current->method);
      }
      woempa(1,"Frame %p (%M) is %sprivileged.\n",current, current->method, isSet(current->flags, FRAME_PRIVILEGED)?"":"not ");
      current = isSet(current->flags, FRAME_PRIVILEGED) ? NULL : current->previous;
    }
    else {
      current = isSet(current->flags, FRAME_PRIVILEGED) ? NULL : current->previous;
    }
  }
  count = sizeOfWordset(&domains);
  woempa(1,"Found %d protection domains on stack\n", count);

  item = count;
  mustBeInitialized(clazzArrayOf_ProtectionDomain);
  enterUnsafeRegion(thread);
  result = allocArrayInstance_1d(thread, clazzArrayOf_ProtectionDomain, item);
  enterSafeRegion(thread);

  if (count == 0) {

    return result;

  }

  if (result) {
    result_array = instance2Array_instance(result);
    /*
    ** Fill in the array.
    */
    for (i = 0; i < count; ++i) {
      result_array[i] = (w_instance)elementOfWordset(&domains, i);
      woempa(1, "result[%d] = %p\n", i, result_array[i]);
    }
  }
  else {
    woempa(9, "Could not allocate result array %k with size %d\n", clazzArrayOf_ProtectionDomain, count); 
  }

  releaseWordset(&domains);

  return result;
}

w_instance AccessController_static_get_inherited_context(JNIEnv *env, w_instance AccessController) {

#ifdef bar
  w_thread thread = JNIEnv2w_thread(env);
  w_frame current;

  current = thread->top ? thread->top->previous : NULL;
  while (current && !current->inherited_context) {
    current = current->previous;
  }

  if (current) {
    woempa(1, "Inherited context is %p, found in frame %p\n", current->inherited_context, current);
  }
  else {
    woempa(1, "No inherited context found, returning NULL\n");
  }

  return current ? current->inherited_context : NULL;
#endif
  return NULL;

}

