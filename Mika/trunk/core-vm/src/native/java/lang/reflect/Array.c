/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2004, 2006, 2007, 2008 by Chris Gray, /k/ Embedded  *
* Java Solutions. All rights reserved.                                    *
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

#include <string.h>

#include "arrays.h"
#include "clazz.h"
#include "core-classes.h"
#include "descriptor.h"
#include "heap.h"
#include "loading.h"
#include "ts-mem.h"
#include "reflection.h"
#include "wstrings.h"
#include "threads.h"
#include "exception.h"

w_instance Array_static_get(JNIEnv *env, w_instance Class, w_instance Array, w_int idx) {

  w_thread thread = JNIEnv2w_thread(env);
  w_clazz clazz = instance2clazz(Array);
  w_clazz component_clazz;

  if (!Array) {
    throwException(thread, clazzNullPointerException, NULL);
    return NULL;
  }

  if (!clazz->dims) {
    throwException(thread, clazzIllegalArgumentException, "not an array class");
    return NULL;
  }

  if (idx < 0 || idx >= instance2Array_length(Array)) {
    throwException(thread, clazzArrayIndexOutOfBoundsException, NULL);
    return NULL;
  }

  component_clazz = clazz->previousDimension;

  if (clazzIsPrimitive(component_clazz)) {
    w_clazz    wrapper_clazz = NULL;
    w_instance wrapped;
    w_word     wrappee;
    w_double   vdouble;
    w_long     vlong;

    switch (component_clazz->type & 0x0f) {
      case VM_TYPE_BOOLEAN:
        wrapper_clazz = clazzBoolean;
        break;

      case VM_TYPE_CHAR:
        wrapper_clazz = clazzCharacter;
        break;

      case VM_TYPE_FLOAT:
        wrapper_clazz = clazzFloat;
        break;

      case VM_TYPE_DOUBLE:
        wrapper_clazz = clazzDouble;
        break;

      case VM_TYPE_BYTE:
        wrapper_clazz = clazzByte;
        break;

      case VM_TYPE_SHORT:
        wrapper_clazz = clazzShort;
        break;

      case VM_TYPE_INT:
        wrapper_clazz = clazzInteger;
        break;

      case VM_TYPE_LONG:
        wrapper_clazz = clazzLong;
        break;

      default:
        wabort(ABORT_WONKA, "Illegal primitive type 0x%02x\n");
    }

    mustBeInitialized(wrapper_clazz);
    enterUnsafeRegion(thread);
    wrapped = allocInstance(thread, wrapper_clazz);
    enterSafeRegion(thread);
    if (wrapped) {
      switch (component_clazz->type & 0x0f) {
        case VM_TYPE_BOOLEAN:
          {
            w_int byteidx = idx / 8;
            w_int bitidx = idx % 8;

            w_sbyte bytevalue = instance2Array_byte(Array)[byteidx];
            bytevalue = (bytevalue >> bitidx) & 1;
            wrappee = (w_word)bytevalue;
            setBooleanField(wrapped, F_Boolean_value, wrappee);
          }
          break;

        case VM_TYPE_CHAR:
          setCharacterField(wrapped, FIELD_OFFSET(F_Character_value), instance2Array_char(Array)[idx]);
          break;

        case VM_TYPE_FLOAT:
          setFloatField(wrapped, FIELD_OFFSET(F_Float_value), instance2Array_float(Array)[idx]);
          break;

        case VM_TYPE_DOUBLE:
          vdouble = instance2Array_double(Array)[idx];
          setDoubleField(wrapped, FIELD_OFFSET(F_Double_value), vdouble);
          break;

        case VM_TYPE_BYTE:
          setByteField(wrapped, FIELD_OFFSET(F_Byte_value), instance2Array_byte(Array)[idx]);
          break;

        case VM_TYPE_SHORT:
          setShortField(wrapped, FIELD_OFFSET(F_Short_value), instance2Array_short(Array)[idx]);
          break;

        case VM_TYPE_INT:
          setIntegerField(wrapped, FIELD_OFFSET(F_Integer_value), instance2Array_int(Array)[idx]);
          break;

        case VM_TYPE_LONG:
          vlong = instance2Array_long(Array)[idx];
          setLongField(wrapped, FIELD_OFFSET(F_Long_value), vlong);
          break;

        default:
          wabort(ABORT_WONKA, "Illegal primitive type 0x%02x\n");
      }

    }

    return wrapped;

  }
  else {
    return instance2Array_instance(Array)[idx];
  }

}

w_int Array_static_getLength(JNIEnv *env, w_instance Class, w_instance Array) {
  w_thread thread = JNIEnv2w_thread(env);

  if (!Array) {
  	throwException(thread, clazzNullPointerException, NULL);
  	return -1;
  }	
  if (!instance2clazz(Array)->dims){
    throwException(thread, clazzIllegalArgumentException, "not an array class");
    return -1;
  }	
  return instance2Array_length(Array);
  
}

/* WAS :
w_instance Array_static_newInstance_single(JNIEnv *env, w_instance Class, w_instance Component, w_int length) {

  w_thread thread = JNIEnv2w_thread(env);
  w_int dimensions;
  w_instance Array = NULL;
  w_clazz component_clazz = Class2clazz(Component);
  w_clazz array_clazz;

  if (length < 0) {
    throwException(thread, clazzNegativeArraySizeException, NULL);

    return NULL;

  }
  else if (!Component) {
    throwException(thread, clazzNullPointerException, NULL);

    return NULL;

  } else if(component_clazz == clazz_void) {
    throwException(thread, clazzIllegalArgumentException, NULL);
    return NULL;
  }

  woempa(9, "Creating instance of 1 dimension and component '%k'.\n", component_clazz);

  dimensions = length;
  array_clazz = getNextDimension(component_clazz);
  if (array_clazz) {
    mustBeInitialized(array_clazz);
    if (!exceptionThrown(thread)) {
      mustBeLinked(array_clazz);
    }
    if (! exceptionThrown(thread)) {
      enterUnsafeRegion(thread);
      Array = allocArrayInstance_1d(thread, array_clazz, dimensions);
      enterSafeRegion(thread);
    }
  }

  return Array;
  
}
*/

w_instance Array_static_newInstance_single(JNIEnv *env, w_instance Class, w_instance Component, w_int length) {

  w_thread thread = JNIEnv2w_thread(env);
  w_instance Array = NULL;
  w_clazz component_clazz = Class2clazz(Component);
  w_clazz array_clazz;
  w_instance initiating_loader = clazz2loader(getCallingClazz(thread));
  
  threadMustBeSafe(thread);

  if (length < 0) {
    throwException(thread, clazzNegativeArraySizeException, NULL);

    return NULL;

  }
  else if (!Component) {
    throwException(thread, clazzNullPointerException, NULL);

    return NULL;

  }
  else if(component_clazz == clazz_void) {
    throwException(thread, clazzIllegalArgumentException, NULL);
    return NULL;
  }

  woempa(9, "Creating instance of 1 dimension and component '%k'.\n", component_clazz);

  array_clazz = getNextDimension(component_clazz, initiating_loader);
  if (array_clazz) {
    mustBeInitialized(array_clazz);
    if (!exceptionThrown(thread)) {
      mustBeLinked(array_clazz);
    }
    if (! exceptionThrown(thread)) {
      enterUnsafeRegion(thread);
      Array = allocArrayInstance_1d(thread, array_clazz, length);
      enterSafeRegion(thread);
    }
  }

  return Array;
  
}

w_instance Array_static_newInstance_multi(JNIEnv *env, w_instance Class, w_instance Component, w_instance Dimensions) {

  w_int * dimensions;
  w_int i;
  w_int ndims;
  w_int *lengths;
  w_instance Array = NULL;
  w_thread thread = JNIEnv2w_thread(env);
  w_clazz clazz;

  threadMustBeSafe(thread);
  if (!Component || !Dimensions) {
    throwException(thread, clazzNullPointerException, NULL);
  }  
  else {
    woempa(9, "Creating instance of %d dimension(s) and component '%k'.\n", instance2Array_length(Dimensions), Class2clazz(Component));
    /*
    ** Check and copy our Dimensions elements...
    */

    ndims = instance2Array_length(Dimensions);
    if (ndims < 1 || ndims > 255) {
      throwException(thread, clazzIllegalArgumentException, "illegal number of dimensions");
    } 
    else {
      clazz = Class2clazz(Component);
    
      if (clazz == clazz_void) {
        throwException(thread, clazzIllegalArgumentException, NULL);
        return NULL;
      }


      /*
      ** Create an array of w_int for the length of each dimension...
      */

      dimensions = allocMem(ndims * sizeof(w_word));
      if (!dimensions) {
         return NULL;
      }
      lengths = instance2Array_int(Dimensions);
      for (i = 0; i < ndims; i++) {
        if (lengths[i] < 0) {
          woempa(9, "lengths[%d] = %d !!\n", i, lengths[i]);
          throwException(thread, clazzNegativeArraySizeException, NULL);
          break;
        }
        dimensions[i] = lengths[i];
      }

      /*
      ** Create now an array clazz from the dimensions and the component clazz
      */

      if (exceptionThrown(thread) == NULL) {
        w_instance initiating_loader = clazz2loader(getCallingClazz(thread));
  
        woempa(7, "Component class = %k\n", clazz);
        for (i = 0; i < ndims; i++) {
          clazz = getNextDimension(clazz, initiating_loader);
          if (exceptionThrown(thread)) {
            break;
          }
          woempa(7, "Initializing class %k\n", clazz);
          mustBeInitialized(clazz);
          if (exceptionThrown(thread)) {
            break;
          }
          woempa(7, "Resolved class %k\n", clazz);
        }

        if (! exceptionThrown(thread)) {
          woempa(7, "Allocating %k\n", clazz);
          enterUnsafeRegion(thread);
          Array = allocArrayInstance(thread, clazz, ndims, dimensions);
          enterSafeRegion(thread);
        }
      }
      releaseMem(dimensions);
    }
  }
  
  return Array;
  
}

