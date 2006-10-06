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
* Modifications copyright (c) 2006 by Chris Gray,                         *
* /k/ Embedded Java Solutions. All rights reserved.                       *
*                                                                         *
**************************************************************************/


/*
** $Id: SecurityManager.c,v 1.3 2006/10/04 14:24:16 cvsroot Exp $
*/

#include "arrays.h"
#include "core-classes.h"
#include "fields.h"
#include "methods.h"
#include "threads.h"

w_instance SecurityManager_getClassContext(JNIEnv *env, w_instance thisSecurityManger) {

  w_thread   thread = JNIEnv2w_thread(env);
  w_frame frame;
  w_int count;
  w_instance result;
  w_instance *result_array;

  count = 0;
  for (frame = thread->top; frame; frame = frame->previous) {
    if (frame->method) {
      count += 1;
    }
  }

  result = allocArrayInstance_1d(thread, clazzArrayOf_Class, count);

  if (result) {
    result_array = instance2Array_instance(result);
    count = 0;
    for (frame = thread->top; frame; frame = frame->previous) {
      if (frame->method) {
        result_array[count++] = frame->method->spec.declaring_clazz->Class;
      }
    }
  }

  return result;

}

w_instance SecurityManager_getNonPrivilegedClassContext(JNIEnv *env, w_instance thisSecurityManger) {

  w_thread   thread = JNIEnv2w_thread(env);
  w_frame frame;
  w_int count;
  w_instance result;
  w_instance *result_array;

  count = 0;
  for (frame = thread->top; frame; frame = frame->previous) {
    if (frame->method) {
      if (isSet(frame->flags, FRAME_PRIVILEGED)) {
        break;
      }
      count += 1;
    }
  }
  
  result = allocArrayInstance_1d(thread, clazzArrayOf_Class, count);

  if (result) {
    result_array = instance2Array_instance(result);
    count = 0;
    for (frame = thread->top; frame; frame = frame->previous) {
      if (frame->method) {
        if (isSet(frame->flags, FRAME_PRIVILEGED)) {
          result_array[count++] = frame->method->spec.declaring_clazz->Class;
          break;
        }
        count += 1;
      }
    }
  }

  return result;

}

w_instance SecurityManager_currentClassLoader(JNIEnv *env, w_instance thisSecurityManger) {

  w_thread thread = JNIEnv2w_thread(env);
  w_frame frame;
  w_instance currentClass;
  w_instance result = NULL;

  for (frame = thread->top; frame; frame = frame->previous) {
    if (frame->method) {
      currentClass = clazz2Class(frame->method->spec.declaring_clazz);
      if ((result = getReferenceField(currentClass, F_Class_loader))) {
        break;
      }
    }
  }

  return result;

}

w_instance SecurityManager_currentLoadedClass(JNIEnv *env, w_instance thisSecurityManger) {

  w_thread thread = JNIEnv2w_thread(env);
  w_frame frame;
  w_instance currentClass;
  w_instance result = NULL;

  for (frame = thread->top; frame; frame = frame->previous) {
    if (frame->method) {
      currentClass = clazz2Class(frame->method->spec.declaring_clazz);
      if (getReferenceField(currentClass, F_Class_loader)) {
        result = currentClass;
        break;
      }
    }
  }

  return result;

}

  
