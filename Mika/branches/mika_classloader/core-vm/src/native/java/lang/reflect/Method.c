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
** $Id: Method.c,v 1.6 2006/10/04 14:24:16 cvsroot Exp $
**
** Implementation of the native methods for java/lang/reflect/Method
*/

#include <string.h>

#include "core-classes.h"
#include "clazz.h"
#include "constant.h"
#include "fields.h"
#include "loading.h"
#include "methods.h"
#include "wstrings.h"
#include "descriptor.h"
#include "arrays.h"
#include "interpreter.h"
#include "checks.h"
#include "threads.h"
#include "heap.h"
#include "ts-mem.h"
#include "reflection.h"
#include "exception.h"

w_instance Method_getDeclaringClass(JNIEnv *env, w_instance thisMethod) {
  w_method method;

  method = getWotsitField(thisMethod, F_Method_wotsit);
  
  return clazz2Class(method->spec.declaring_clazz);
  
}

w_instance Method_getName(JNIEnv *env, w_instance thisMethod) {
  w_method method = getWotsitField(thisMethod, F_Method_wotsit);
  
  return newStringInstance(method->spec.name);
  
}

w_int Method_getModifiers(JNIEnv *env, w_instance thisMethod) {

  w_method method = getWotsitField(thisMethod, F_Method_wotsit);
  
  return method->flags & 0x0000ffff;
  
}

w_instance Method_getReturnType(JNIEnv *env, w_instance thisMethod) {
  w_method method = getWotsitField(thisMethod, F_Method_wotsit);

  if (mustBeLoaded(&method->spec.return_type) == CLASS_LOADING_FAILED) {
    woempa(7, "Attempt to load %k failed: %e\n", method->spec.return_type, exceptionThrown(JNIEnv2w_thread(env)));
    return NULL;
  }

  return clazz2Class(method->spec.return_type);
  
}

w_instance Method_getParameterTypes(JNIEnv *env, w_instance thisMethod) {

  w_thread thread = JNIEnv2w_thread(env);
  w_method method = getWotsitField(thisMethod, F_Method_wotsit);
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

/*
**
*/

w_instance Method_getExceptionTypes(JNIEnv *env, w_instance thisMethod) {

  w_thread thread = JNIEnv2w_thread(env);
  w_method method;
  w_instance Exceptions;
  w_clazz  exception;
  w_int    numthrows;
  w_int    i;
  w_int    length;

  method = getWotsitField(thisMethod, F_Method_wotsit);
  // TODO exception =
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
      wassert(exception);	
      setArrayReferenceField(Exceptions, clazz2Class(exception), i);
    }
  }

  return Exceptions;

}

w_boolean Method_equals(JNIEnv *env, w_instance thisMethod, w_instance theObject) {

  w_method this_method = getWotsitField(thisMethod, F_Method_wotsit);
  w_boolean result = WONKA_FALSE;
  w_clazz objectClazz = instance2clazz(theObject);
  w_method that_method;

  if (objectClazz == clazzMethod) {
    that_method = getWotsitField(theObject, F_Method_wotsit);
    result = (this_method==that_method);
  }

  return result;
   
}

w_instance Method_invoke0(JNIEnv *env, w_instance thisMethod, w_instance theObject, w_instance Arguments) {

  w_thread thread = JNIEnv2w_thread(env);
  w_method method = getWotsitField(thisMethod, F_Method_wotsit);
  w_frame  frame;
  w_instance This = NULL;
  w_clazz  returns;
  w_instance result = NULL;
  w_instance protected = NULL;
  w_clazz clazz;
  w_clazz calling_clazz = getCallingClazz(thread);
  w_instance calling_instance = getCallingInstance(thread);

  woempa(1,"Calling class is %k\n", calling_clazz);
  if (calling_instance) {
    woempa(1,"Calling instance is %j\n", calling_instance);
  }
  else {
    woempa(1,"No calling instance, calling method is static\n");
  }

  /*
  ** Stop the call if AccessibleObject is not set and the caller would not
  ** normally be able to access this method.
  */
  if (!getBooleanField(thisMethod, F_AccessibleObject_accessible)
     && !isAllowedToCall(calling_clazz, method, theObject ? instance2clazz(theObject) : NULL)) {
    throwException(thread, clazzIllegalAccessException,NULL);

    return NULL;

  }

  if (mustBeLoaded(&method->spec.return_type) == CLASS_LOADING_FAILED) {
    woempa(7, "(REFLECTION) invoke failed: %k\n", instance2clazz(exceptionThrown(thread)));
    frame = NULL;
  }
  else if (mustBeInitialized(method->spec.declaring_clazz) == CLASS_LOADING_FAILED) {
    frame = NULL;
  }
  else {
    woempa(1, "Asked to invoke Method %M on instance %p of %k.\n", method, This, instance2clazz(This));

    if (isSet(method->flags, ACC_STATIC)) {
      This = clazz2Class(method->spec.declaring_clazz);
      woempa(7, "Invoking static method %M on class instance of %k.\n", method, instance2clazz(This));
      /*
      ** Static method: ignore theObject, pass class where method was defined
      */
      This = clazz2Class(method->spec.declaring_clazz);
    }
    else {
      /*
      ** Nonstatic method: use theObject to determine the actual class, and
      ** use that to find a possibly overridden method.
      */
      This = theObject;

      if (isNotSet(method->flags, ACC_PRIVATE)) {
        clazz = instance2clazz(This);
        method = isSet(method->spec.declaring_clazz->flags, ACC_INTERFACE) ? interfaceLookup(method, clazz) : virtualLookup(method, clazz);
      }
      woempa(7, "After overriding analysis, invoking %M on instance %j.\n", method, This);

    }        

    frame = invoke(env, method, This, Arguments);
  }        

  if (! exceptionThrown(thread)) {
    returns = method->spec.return_type;
    if (isSet(returns->flags, CLAZZ_IS_PRIMITIVE)) {
      if (method->spec.return_type == clazz_void) {
        result = NULL;
      }
      else {
        w_int slot;
        result = createWrapperInstance(thread, returns, &slot);
        if (!result) {
          throwOutOfMemoryError(thread);

          return NULL;

        }

        if (isSet(returns->type, VM_TYPE_TWO_CELL)) {
          wordFieldPointer(result, slot)[1] = frame->jstack_top[-1].c;
          wordFieldPointer(result, slot)[0] = frame->jstack_top[-2].c;
        }
        else {
#ifdef PACK_BYTE_FIELDS
          if ((slot & FIELD_SIZE_MASK) <= FIELD_SIZE_8_BITS) {
            *byteFieldPointer(result, slot) = (char)frame->jstack_top[-1].c;
          }
          else {
            *wordFieldPointer(result, slot) = frame->jstack_top[-1].c;
          }
#else
          *wordFieldPointer(result, slot) = frame->jstack_top[-1].c;
#endif
        }
      }
    }
    else {
      result = (w_instance) frame->jstack_top[-1].c;
      protected = result;
    }
  }
  else {
    woempa(9, "(REFLECTION) invoke failed: %k\n", instance2clazz(exceptionThrown(thread)));
  }

  if (frame) {
    deactivateFrame(frame, protected);
  }

  return result;

}
