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

#include "checks.h"
#include "clazz.h"
#include "constant.h"
#include "core-classes.h"
#include "descriptor.h"
#include "reflection.h"
#include "wstrings.h"
#include "methods.h"
#include "threads.h"
#include "heap.h"
#include "loading.h"
#include "exception.h"
#include "constant.h"
#include "interpreter.h" // for deactivateFrame, but should be in methods.h

w_instance Constructor_getDeclaringClass(JNIEnv *env, w_instance thisConstructor) {
  w_method method;

  method = getWotsitField(thisConstructor, F_Constructor_wotsit);
  
  return clazz2Class(method->spec.declaring_clazz);
  
}

w_instance Constructor_getName(JNIEnv *env, w_instance thisConstructor) {
  w_method method = getWotsitField(thisConstructor, F_Constructor_wotsit);
  
  return newStringInstance(method->spec.declaring_clazz->dotified);
  
}

w_int Constructor_getModifiers(JNIEnv *env, w_instance thisConstructor) {

  w_method method = getWotsitField(thisConstructor, F_Constructor_wotsit);
  
  return method->flags & 0x0000ffff;
  
}

w_instance Constructor_getParameterTypes(JNIEnv *env, w_instance thisConstructor) {

  w_thread thread = JNIEnv2w_thread(env);
  w_method method = getWotsitField(thisConstructor, F_Constructor_wotsit);
  w_instance Parameters;
  w_clazz parameter;
  w_int numParameters;
  w_int i;
  w_int length;
  
  if (method->spec.arg_types) {
    for (numParameters = 0; method->spec.arg_types[numParameters]; ++numParameters);
  }
  else {
    numParameters = 0;
  }
  length = numParameters;
  Parameters = allocArrayInstance_1d(thread, clazzArrayOf_Class, length);
  
  if (Parameters) {
    for (i = 0; i < numParameters; i++) {
      if (mustBeLoaded(&method->spec.arg_types[i]) == CLASS_LOADING_FAILED) {
        woempa(7, "Attempt to load %k failed: %e\n", method->spec.arg_types[i], exceptionThrown(thread));
        return NULL;
      }

      parameter = method->spec.arg_types[i];

      setArrayReferenceField(Parameters, clazz2Class(parameter), i);
    }
  }

  return Parameters;
   
}

w_instance Constructor_getExceptionTypes(JNIEnv *env, w_instance thisConstructor) {

  w_thread thread = JNIEnv2w_thread(env);
  w_method method;
  w_instance Exceptions;
  w_clazz exception;
  w_int numthrows;
  w_int i;
  w_int length;

  method = getWotsitField(thisConstructor, F_Constructor_wotsit);
  mustBeLinked(method->spec.declaring_clazz);
  if (exceptionThrown(thread)) {

    return NULL;

  }
  numthrows = method->numThrows;
  length = numthrows;

  Exceptions = allocArrayInstance_1d(thread, clazzArrayOf_Class, length);

  if (Exceptions) {
    for (i = 0; i < numthrows; i++) {
      exception = getClassConstant(method->spec.declaring_clazz, method->throws[i]);
      if (mustBeReferenced(exception) == CLASS_LOADING_FAILED) {
        return NULL;
      }
      setArrayReferenceField(Exceptions, clazz2Class(exception), i);
    }
  }

  return Exceptions;

}

w_instance Constructor_newInstance0(JNIEnv *env, w_instance thisConstructor, w_instance Arguments) {
  w_thread thread = JNIEnv2w_thread(env);
  w_method init;
  w_frame frame;
  w_instance new = NULL;
  w_clazz calling_clazz = getCallingClazz(thread);
  w_instance calling_instance = getCallingInstance(thread);

  woempa(1,"Calling class is %k\n", calling_clazz);
  if (calling_instance) {
    woempa(1,"Calling instance is %j\n", calling_instance);
  }
  else {
    woempa(1,"No calling instance, calling method is static\n");
  }

  init = getWotsitField(thisConstructor, F_Constructor_wotsit);

  if (!getBooleanField(thisConstructor, F_AccessibleObject_accessible) && 
      !isAllowedToCall(calling_clazz, init, init->spec.declaring_clazz)) {
    throwException(thread, clazzIllegalAccessException, NULL);
  }
  else {
    if (mustBeInitialized(init->spec.declaring_clazz) == CLASS_LOADING_FAILED) {

      return NULL;

    }
    new = allocInstance(thread, init->spec.declaring_clazz);
    if (new) {
      frame = invoke(env, init, new, Arguments);
      if (exceptionThrown(thread)) {
        woempa(9, "(REFLECTION) invoke failed: %k\n", instance2clazz(exceptionThrown(thread)));
      }
      deactivateFrame(frame, new);
    }
  }

  return new;
    
}
