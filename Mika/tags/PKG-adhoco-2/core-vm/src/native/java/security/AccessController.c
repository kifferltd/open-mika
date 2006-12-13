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
** $Id: AccessController.c,v 1.7 2006/10/04 14:24:17 cvsroot Exp $
*/

#include "arrays.h"
#include "core-classes.h"
#include "descriptor.h"
#include "interpreter.h"
#include "jni.h"
#include "loading.h"
#include "wstrings.h"
#include "threads.h"
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
  result = allocArrayInstance_1d(thread, clazzArrayOf_ProtectionDomain, item);

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

