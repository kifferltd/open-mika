/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                                    *
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

  
