/**************************************************************************
* Copyright (c) 2008, 2021 by KIFFER Ltd. All rights reserved.            *
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

/*
**
** Utility functions for reflection related files.
**
** The implementations for the Java conversions and promotions are to be found
** in this file. See any good JVM book or JLS book for the different promotions 
** and conversions that can take place.
*/

#include "arrays.h"
#include "checks.h"
#include "clazz.h"
#include "constant.h"
#include "core-classes.h"
#include "descriptor.h"
#include "exception.h"
#include "fields.h"
#include "interpreter.h"
#include "jni.h"
#include "loading.h"
#include "methods.h"
#include "wmath.h"

/*
** For assigning fields and variables to each other. The reflection classes need to implement
** what is called widening for some operations, or when no type conversion is required, implement
** a so called 'identity conversion' or assignment. Checking which conversions are allowed and
** what operations need to be performed by the virtual machine in order to change from one type
** to another, is done through a matrix or lookup table of conversion function pointers. A NULL
** pointer means that the conversion <from> to <to> type is not allowed, a non NULL pointer means
** that the conversion is allowed and the function pointed to will perform the assignment.
** 
*/

/*
** Two 'identity conversion assignment' functions, for 32 bit data
** and for 64 bit data. Pretty straightforward...
*/

static void i_32(void *S, void *T) {

  *((w_word*)T) = *((w_word*)S);

}

static void i_64(void *S, void *T) {

  ((w_word*)T)[WORD_LSW] = ((w_word*)S)[WORD_LSW];
  ((w_word*)T)[WORD_MSW] = ((w_word*)S)[WORD_MSW];

}

/*
** If PACK_BYTE_FIELDS is defined then we need a further identity operation
** for 8-bit fields. Currently booleans are treated as 8-bit and shorts and
** chars are treated as 32-bit.
*/

#ifdef PACK_BYTE_FIELDS

static void i_8(void *S, void *T) {

  *((char*)T) = *((char*)S);

}

#else
#define i_8 i_32
#endif

/*
** The different conversion routine <from>__<to>. Note that after testing,
** some of these can be collapsed together for compacter code.
*/

static void C__I(void *S, void *T) {

  /*
  ** No sign extension takes place...
  */

  *((w_word*)T) = (*((w_word*)S) & 0x0000ffff);

}

static void C__J(void *S, void *T) {

  /*
  ** No sign extension takes place...
  */
  
  *((w_long*)T) = (*((w_word*)S) & 0x0000ffff);

}

static void C__F(void *S, void *T) {

  *((w_word*)T) = wfp_int32_to_float32(*((w_word*)S) & 0x0000ffff);

}

static void C__D(void *S, void *T) {

  w_double d = wfp_int32_to_float64(*((w_word*)S) & 0x0000ffff);

  *((w_double*)T) = d;

}

#ifdef PACK_BYTE_FIELDS
static void B__S(void *S, void *T) {

  *(w_int*)T = *(w_sbyte*)S;

}

static void B__I(void *S, void *T) {

  *(w_int*)T = *(w_sbyte*)S;

}

static void B__J(void *S, void *T) {

  w_long l = *((w_sbyte*)S);

  *((w_long*)T) = l;

}

static void B__F(void *S, void *T) {

  *((w_word*)T) = wfp_int32_to_float32((w_int)*((w_sbyte*)S));
  
}

static void B__D(void *S, void *T) {

  w_double d = wfp_int32_to_float64((w_int)*((w_sbyte*)S));

  *((w_double*)T) = d;

}

#else
static void B__S(void *S, void *T) {

  w_sbyte b = *(w_word*)S;
  w_short s = b;

  *(w_int*)T = s;

}

static void B__I(void *S, void *T) {

  w_sbyte b = *(w_word*)S;

  *(w_int*)T = b;

}

static void B__J(void *S, void *T) {

  w_sbyte b = *(w_word*)S;
  w_long l = b;

  *((w_long*)T) = l;

}

static void B__F(void *S, void *T) {

  w_sbyte b = *(w_word*)S;
  w_int i = b;

  *((w_word*)T) = wfp_int32_to_float32(i);
  
}

static void B__D(void *S, void *T) {

  w_sbyte b = *(w_word*)S;
  w_int i = b;
  w_double d = wfp_int32_to_float64(i);

  *((w_double*)T) = d;

}

#endif

static void S__I(void *S, void *T) {

  w_int i = (*((w_word*)S) & 0x8000) ? (*((w_word*)S) | 0xffff0000) : (*((w_word*)S) & 0x00007fff);

  *((w_word*)T) = i;

}

static void S__J(void *S, void *T) {

  w_double l = *((w_int*)S);

  *((w_double*)T) = l;

}

static void S__F(void *S, void *T) {

  *((w_word*)T) = wfp_int32_to_float32(*((w_int*)S));

}

static void S__D(void *S, void *T) {

  w_double d = wfp_int32_to_float64(*((w_int*)S));

  *((w_double*)T) = d;

}

static void I__J(void *S, void *T) {

  w_long l = *((w_int*)S);

  *((w_long*)T) = l;

}

static void I__F(void *S, void *T) {

  *((w_word*)T) = wfp_int32_to_float32(*((w_int*)S));
  
}

static void I__D(void *S, void *T) {

  w_double d = wfp_int32_to_float64(*((w_int*)S));

  *((w_double*)T) = d;

}

static void J__F(void *S, void *T) {

  w_long l = *((w_long*)S);
  
  *((w_float*)T) = wfp_int64_to_float32(l);
  
}

static void J__D(void *S, void *T) {

  w_double d = wfp_int64_to_float64(*((w_long*)S));

  *((w_double*)T) = d;

}

static void F__D(void *S, void *T) {

  w_double d = wfp_float32_to_float64(*((w_float*)S));

  *((w_double*)T) = d;

}

typedef void (*w_widener)(void *S, void *T);


static w_widener widener_matrix[8][8] = {
  /*   +--->  to short int   long  byte  char  float doubl bool */
  /*   |                                                        */
  /* from                                                       */
  /* short  */ { i_32, S__I, S__J, NULL, NULL, S__F, S__D, NULL },
  /* int    */ { NULL, i_32, I__J, NULL, NULL, I__F, I__D, NULL },
  /* long   */ { NULL, NULL, i_64, NULL, NULL, J__F, J__D, NULL },
  /* byte   */ { B__S, B__I, B__J, i_8,  NULL, B__F, B__D, NULL },
  /* char   */ { NULL, C__I, C__J, NULL, i_32, C__F, C__D, NULL },
  /* float  */ { NULL, NULL, NULL, NULL, NULL, i_32, F__D, NULL },
  /* double */ { NULL, NULL, NULL, NULL, NULL, NULL, i_64, NULL },
  /* bool   */ { NULL, NULL, NULL, NULL, NULL, NULL, NULL, i_8  },
};

/*
** Possibly convert and assign one primitive value (from) to another (to). If the widening
** conversion is not permitted, this function returns WONKA_FALSE, otherwise WONKA_TRUE indicates success.
*/

w_boolean widen(w_clazz from_clazz, void *F_data, w_clazz to_clazz, void *T_data) {

  w_widener widener;
  w_int f;
  w_int t;

  if (clazzIsPrimitive(from_clazz) && clazzIsPrimitive(to_clazz)) {
    f = from_clazz->type & 0x0f;
    t = to_clazz->type & 0x0f;

    /*
    ** In any case, zero out the memory of the 'T_data' array.
    */

#ifdef PACK_BYTE_FIELDS
    if ((to_clazz == clazz_boolean) || (to_clazz == clazz_byte)) {
      ((char*)T_data)[0] = 0;
    }
    else {
      ((w_word*)T_data)[0] = 0;
      if (isSet(to_clazz->type, VM_TYPE_TWO_CELL)) {
        ((w_word*)T_data)[1] = 0;
      }
    }
#else
    ((w_word*)T_data)[0] = 0;
    if (isSet(to_clazz->type, VM_TYPE_TWO_CELL)) {
      ((w_word*)T_data)[1] = 0;
    }
#endif

    // TODO, the following if should become a runtime check
    if ((f <= 8) && (t <= 8)) {
      woempa(1, "Trying to widen and assign type '%k' to type '%k'...\n", from_clazz, to_clazz);
      widener = widener_matrix[f - 1][t - 1];
      if (widener) {
        (*widener)(F_data, T_data);
        return WONKA_TRUE;
      }
    }
  }

  /*
  ** An error occured. Since the 'f' and 't' indexes could be bogus, we just print the indexes and not the
  ** primitive2name element since we could segfault...
  */
  
  woempa(1, "... impossible to perform widening.\n");
  
  return WONKA_FALSE;
  
}

w_clazz getWrappedValue(w_instance value, void **data) {
  w_clazz value_clazz = instance2clazz(value);

  woempa(1, "Trying to unwrap %j.\n", value);
  if (value_clazz == clazzShort) {
    *(w_word**)data = wordFieldPointer(value, F_Short_value);

    return clazz_short;

  }
  if (value_clazz == clazzInteger) {
    *(w_word**)data = wordFieldPointer(value, F_Integer_value);

    return clazz_int;

  }
  if (value_clazz == clazzLong) {
    *(w_word**)data = wordFieldPointer(value, F_Long_value);

    return clazz_long;

  }
  if (value_clazz == clazzByte) {
#ifdef PACK_BYTE_FIELDS
    *(char**)data = byteFieldPointer(value, F_Byte_value);
#else
    *(w_word**)data = wordFieldPointer(value, F_Byte_value);
#endif

    return clazz_byte;

  }
  if (value_clazz == clazzCharacter) {
    *(w_word**)data = wordFieldPointer(value, F_Character_value);

    return clazz_char;

  }
  if (value_clazz == clazzFloat) {
    *(w_word**)data = wordFieldPointer(value, F_Float_value);

    return clazz_float;

  }
  if (value_clazz == clazzDouble) {
    *(w_word**)data = wordFieldPointer(value, F_Double_value);

    return clazz_double;

  }
  if (value_clazz == clazzBoolean) {
#ifdef PACK_BYTE_FIELDS
    *(char**)data = byteFieldPointer(value, F_Boolean_value);
#else
    *(w_word**)data = wordFieldPointer(value, F_Boolean_value);
#endif

    return clazz_boolean;

  }

  return NULL;
}

/*
** Create a wrapper instance for a certain primitive type, returns NULL when the type is
** not a primitive type.
*/

w_instance createWrapperInstance(w_thread thread, w_clazz clazz, w_int *slot) {
  w_clazz wrapper_clazz;

  threadMustBeSafe(thread);

  switch (clazz->type & 0x0f) {
    case VM_TYPE_SHORT:
      wrapper_clazz = clazzShort;
      *slot = F_Short_value;
      break;

    case VM_TYPE_INT:
      wrapper_clazz = clazzInteger;
      *slot = F_Integer_value;
      break;

    case VM_TYPE_LONG:
      wrapper_clazz = clazzLong;
      *slot = F_Long_value;
      break;

    case VM_TYPE_BYTE:
      wrapper_clazz = clazzByte;
      *slot = F_Byte_value;
      break;

    case VM_TYPE_CHAR:
      wrapper_clazz = clazzCharacter;
      *slot = F_Character_value;
      break;

    case VM_TYPE_FLOAT:
      wrapper_clazz = clazzFloat;
      *slot = F_Float_value;
      break;

    case VM_TYPE_DOUBLE:
      wrapper_clazz = clazzDouble;
      *slot = F_Double_value;
      break;

    case VM_TYPE_BOOLEAN:
      wrapper_clazz = clazzBoolean;
      *slot = F_Boolean_value;
      break;

    default:
      wrapper_clazz = NULL;
  }
  
  if (clazz) {
    w_instance result;

    enterUnsafeRegion(thread);
    result = allocInstance(thread, wrapper_clazz);
    enterSafeRegion(thread);

    return result;
  }

  wabort(ABORT_WONKA, "Wrong primitive VM_TYPE %d\n", clazz->type);

  return NULL;
  
}

/**
 * Wrap the exception pending on the given thread in an exception of type
 * wrapper_clazz, storing a reference to the original exception at offset
 * field_offset.
 */
void wrapException(w_thread thread, w_clazz wrapper_clazz, w_size field_offset) {
  w_instance wrappee = exceptionThrown(thread);
  w_instance wrapper;

  threadMustBeSafe(thread);

  if (wrappee) {
    addLocalReference(thread, wrappee);
  // First clear the exception or allocating an instance won't work!
    clearException(thread);
    if (mustBeInitialized(wrapper_clazz) == CLASS_LOADING_FAILED) {
      // Ouch, we had a problem loading the wrapper class. Better get the hell out ...
      return;
    }
    enterUnsafeRegion(thread);
    wrapper = allocInstance(thread, wrapper_clazz);
    if (wrapper) {
      woempa(9, "Wrapping %e in %e\n", wrappee, wrapper);
      setReferenceField_unsafe(wrapper, wrappee, field_offset);
      throwExceptionInstance(thread, wrapper);
      removeLocalReference(thread, wrappee);
    }
    enterSafeRegion(thread);
  }
}

w_frame invoke(w_thread thread, w_method method, w_instance This, w_instance Arguments) {

  w_instance *arguments;
  w_clazz F_clazz;
  w_word *F_data;
  w_clazz T_clazz;
  w_word  T_data[2];
  w_int numArgs;
  w_int  spec_nargs;
  w_int i;
  w_frame frame;

  threadMustBeSafe(thread);

  woempa(1, "(REFLECTION) Invoking %M, %d argument words.\n", method, method->exec.arg_i);
  frame = pushFrame(thread, method);
  frame->flags |= FRAME_REFLECTION;

  numArgs = instance2Array_length(Arguments);
  spec_nargs = 0;
  if (method->spec.arg_types) {
    while (method->spec.arg_types[spec_nargs]) {
      woempa(1, "Method %w arg[%d] is %w\n", method->spec.name, spec_nargs, method->spec.arg_types[spec_nargs]->dotified);
      ++spec_nargs;
    }
  }

  if (numArgs != spec_nargs) {
    woempa(7, "Array contains %d args, method %w has %d args\n", numArgs, method->spec.name, spec_nargs);
    throwException(thread, clazzIllegalArgumentException, NULL);
  }
  else {
    if (mustBeInitialized(method->spec.declaring_clazz) != CLASS_LOADING_FAILED) {
      if (! isSet(method->flags, ACC_STATIC)) {
#ifdef TRACE_CLASSLOADERS
        w_instance loader = instance2clazz(This)->loader;
        if (loader && !getBooleanField(loader, F_ClassLoader_systemDefined)) {
          frame->udcl = loader;
        }
        else {
          frame->udcl = frame->previous->udcl;
        }
#endif
        SET_REFERENCE_SLOT(frame->jstack_top, (w_word) This);
        frame->jstack_top += 1;
      }
      arguments = instance2Array_instance(Arguments);
      for (i = 0; i < numArgs; i++) {
        if (mustBeLoaded(&method->spec.arg_types[i]) == CLASS_LOADING_FAILED) {
          break;
        }

        T_clazz = method->spec.arg_types[i];
        if (isSet(T_clazz->flags, CLAZZ_IS_PRIMITIVE)) {
          F_clazz = getWrappedValue(arguments[i], (void**)&F_data);

          if (F_clazz == NULL || !widen(F_clazz, F_data, T_clazz, T_data)) {
            if (F_clazz == NULL) {
              woempa(7, "Argument %d is not a wrapper (%k)!\n", i, instance2clazz(arguments[i]));
            }
            else {
              woempa(7, "Widening is impossible!\n");
            }
            throwException(thread, clazzIllegalArgumentException, NULL);
            break;
          }

          if (isSet(T_clazz->type, VM_TYPE_TWO_CELL)) {
            SET_SCALAR_SLOT(frame->jstack_top, T_data[0]);
            SET_SCALAR_SLOT(frame->jstack_top+1, T_data[1]);
            frame->jstack_top += 2;
          }
          else {
            SET_SCALAR_SLOT(frame->jstack_top, T_data[0]);
            frame->jstack_top += 1;
          }
        }
        else {
          woempa(1, "Argument %d: target class is %k, argument is %j\n", i, T_clazz, arguments[i]);
          if (arguments[i] == NULL || isAssignmentCompatible(instance2clazz(arguments[i]), T_clazz)) {
            SET_REFERENCE_SLOT(frame->jstack_top, (w_word) arguments[i]);
            frame->jstack_top += 1;
          }
          else {
            woempa(7, "%j is not assignment compatible with %k.\n", arguments[i], T_clazz);
            throwException(thread, clazzIllegalArgumentException, NULL);
            break;
          }
        }
      }

    /*
    ** If no exception is pending, all the arguments were OK and they are in the frame, let's run it now...
    */

      if (! exceptionThrown(thread)) {
        callMethod(frame, method);
        if (exceptionThrown(thread)) {
          wrapException(thread, clazzInvocationTargetException, F_InvocationTargetException_target);
        }
      }
    }
  }

  return frame;

}

/*
 * Wrap up a byte-sized value (i.e. w_boolean or w_byte) in an object of class
 * Boolean or Byte respectively. The value is passed as a word because that's
 * the way parameters are generally passed in C. Returns NULL if the wrapper
 * instance could not be created.
 */
w_instance wrapByteValue(w_thread thread, w_clazz clazz, w_word value) {
  w_int slot; // = 0; // (to prevent a compiler warning)
  w_instance wrapper;

  wrapper = createWrapperInstance(thread, clazz, &slot);
  if (wrapper) {
#ifdef PACK_BYTE_FIELDS
    *byteFieldPointer(wrapper, slot) = (char)value;
#else
    *wordFieldPointer(wrapper, slot) = value;
#endif
  }

  return wrapper;
}

/*
 * Wrap up a word-sized value (i.e. w_short, w_char, w_int, or w_float) in an 
 * object of class Short, Character, Integer, or Float respectively.  
 * Returns NULL if the wrapper instance could not be created.
 */
w_instance wrapWordValue(w_thread thread, w_clazz clazz, w_word value) {
  w_int slot; // = 0; // (to prevent a compiler warning)
  w_instance wrapper;

  wrapper = createWrapperInstance(thread, clazz, &slot);

  if (wrapper) {
    *wordFieldPointer(wrapper, slot) = value;
  }

  return wrapper;
}

/*
 * Wrap up a w_long in an object of class Long.  
 * Returns NULL if the wrapper instance could not be created.
 */
w_instance wrapLongValue(w_thread thread, w_clazz clazz, w_long value) {
  w_int slot; // = 0; // (to prevent a compiler warning)
  w_instance wrapper;

  wrapper = createWrapperInstance(thread, clazz, &slot);

  if (wrapper) {
    union { w_long l; w_word w[2]; } temp;
    temp.l = value;
    wordFieldPointer(wrapper, slot)[0] = temp.w[0];
    wordFieldPointer(wrapper, slot)[1] = temp.w[1];
  }

  return wrapper;
}

/*
 * Wrap up a w_double in an object of class Long.  
 * Returns NULL if the wrapper instance could not be created.
 */
w_instance wrapDoubleValue(w_thread thread, w_clazz clazz, w_double value) {
  w_int slot; // = 0; // (to prevent a compiler warning)
  w_instance wrapper;

  wrapper = createWrapperInstance(thread, clazz, &slot);

  if (wrapper) {
    union { w_double d; w_word w[2]; } temp;
    temp.d = value;
    wordFieldPointer(wrapper, slot)[0] = temp.w[0];
    wordFieldPointer(wrapper, slot)[1] = temp.w[1];
  }

  return wrapper;
}

w_instance wrapProxyArgs(w_thread thread, w_method current_method, va_list arg_list) {
  w_instance arguments;
  w_int numArgs = 0;
  w_fifo arg_fifo = allocFifo(254);
  w_clazz arg_clazz;
  w_instance param;
  w_int    i;
  w_word   word_value;

  threadMustBeSafe(thread);
  while (current_method->spec.arg_types[numArgs]) {
    woempa(7, "Method %w arg[%d] is %s %k\n", current_method->spec.name, numArgs, clazzIsPrimitive(current_method->spec.arg_types[numArgs]) ? "primitive" : "reference", current_method->spec.arg_types[numArgs]);
    if (mustBeLoaded(&current_method->spec.arg_types[numArgs]) == CLASS_LOADING_FAILED) {
      releaseFifo(arg_fifo);

      return NULL;

    }
    arg_clazz = current_method->spec.arg_types[numArgs];
    if (clazzIsPrimitive(arg_clazz)) {
      switch (arg_clazz->type & 0x0f) {
      case VM_TYPE_BOOLEAN:
      case VM_TYPE_BYTE:
        param = wrapByteValue(thread, arg_clazz, va_arg(arg_list, w_word));
	break;

      case VM_TYPE_CHAR:
      case VM_TYPE_SHORT:
      case VM_TYPE_INT:
        param = wrapWordValue(thread, arg_clazz, va_arg(arg_list, w_word));
	break;
  
      case VM_TYPE_FLOAT:
            
        /*
        ** Danger, danger, floats are always passed as doubles.
        ** We need to get them as a double and then convert them
        ** to a float.
        */
            
      {
        union { double d; wfp_float64 f64; } temp;
        temp.d = va_arg(arg_list, double);
        word_value = wfp_float64_to_float32(temp.f64);
        param = wrapWordValue(thread, arg_clazz, word_value);
      }
      break;
            
      case VM_TYPE_LONG:
        param = wrapLongValue(thread, arg_clazz, va_arg(arg_list, w_long));
        break;

      case VM_TYPE_DOUBLE:
        param = wrapDoubleValue(thread, arg_clazz, va_arg(arg_list, w_double));
        break;
   
      default: 
        param = NULL; // to suppress a compiler warning
        wabort(ABORT_WONKA, "Incorrect primitive type 0x%02x.\n", arg_clazz->type);        
      }
    }
    else {
      param = va_arg(arg_list, w_instance);
    }
    if (putFifo(param, arg_fifo) < 0) {
      wabort(ABORT_WONKA, "unable to add Proxy parameter to fifo\n");
    }
    ++numArgs;
  }

  enterUnsafeRegion(thread);
  arguments = allocArrayInstance_1d(thread, clazzArrayOf_Object, numArgs);
  enterSafeRegion(thread);
  if (!arguments) {
    releaseFifo(arg_fifo);

    return NULL;

  }

  for (i = 0; i < numArgs; ++i) {
    setArrayReferenceField(arguments, getFifo(arg_fifo), i);
  }

  releaseFifo(arg_fifo);

  return arguments;
}

/**
 * If the exception thrown is neither declared by current_method nor a 
 * RuntimeException nor an Error, wrap it in an UndeclaredThrowableException.
 * This function must only be called if thread->exception is non-null!
 * Note: here we might load an exception class as a side-effect, sorry about
 * that. And if the loading fails we just skip the check, sorry about that too.
 */
static void wrapProxyException(w_thread thread, w_method current_method) {
  w_clazz thrown_clazz = instance2clazz(exceptionThrown(thread));
  w_clazz allowed_clazz;
  w_boolean found = FALSE;
  int i;

  for (i = 0; i < current_method->numThrows; ++i) {
    allowed_clazz = getClassConstant(current_method->spec.declaring_clazz, current_method->throws[i], thread);
    //w_printf("    allowed: %k\n", allowed_clazz);
    if (allowed_clazz && isSuperClass(allowed_clazz, thrown_clazz)) {
      found = TRUE;
      break;
    }
  }
  if (found) {
    //w_printf("Thrown is a subclass of %k\n", allowed_clazz);
  }
  else {
    if(isSuperClass(clazzRuntimeException, thrown_clazz)) {
      //w_printf("Thrown is a subclass of RuntimeException\n", allowed_clazz);
    }
    else {
      if (isSuperClass(clazzError, thrown_clazz)) {
        //w_printf("Thrown is a subclass of Error\n", allowed_clazz);
      }
      else {
        wrapException(thread, clazzUndeclaredThrowableException, F_UndeclaredThrowableException_undeclaredThrowable);
      }
    }
  }
}

void voidProxyMethodCode(w_thread thread, w_instance thisProxy, ...) {
#ifdef JNI
  JNIEnv *env = w_thread2JNIEnv(thread);
#endif
  w_frame  current_frame = thread->top;
  w_frame  new_frame;
  w_instance handler;
  w_clazz  target_clazz;
  w_method target_method;
  w_method current_method;
  w_instance currentMethod;
  w_instance arguments;
  va_list  arg_list;

  threadMustBeSafe(thread);

  current_method = current_frame->method;
  woempa(7, "Calling void proxy method %M on %J\n", current_method, thisProxy);
  while (current_method->parent) {
    current_method = current_method->parent;
  }
  woempa(7, "Parent method : %M\n", current_method);
  handler = getReferenceField(thisProxy, F_Proxy_h);
  woempa(7, "handler : %j\n", handler);
  target_clazz = instance2clazz(handler);
  woempa(7, "Target class : %K\n", target_clazz);
#ifdef JNI
  target_method = (*env)->GetMethodID(env, clazz2Class(target_clazz), "invoke", "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;");
#else
  target_method = find_method(target_clazz, "invoke", "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;");
#endif
  woempa(7, "Target method : %M\n", target_method);

  if (current_method->spec.arg_types) {
    va_start(arg_list, thisProxy);
    arguments = wrapProxyArgs(thread, current_method, arg_list);
    va_end(arg_list);
  }
  else {
    arguments = NULL;
  }

  if (exceptionThrown(thread)) {

    return;

  }

  enterUnsafeRegion(thread);
  currentMethod = allocInstance(thread, clazzMethod);
  enterSafeRegion(thread);
  if (!currentMethod) {
    woempa(9, "Unable to allocate Method\n");

    return;

  }
  setWotsitField(currentMethod, F_Method_wotsit, current_method);
  new_frame = pushFrame(thread, target_method);
  new_frame->flags |= FRAME_REFLECTION;

  SET_REFERENCE_SLOT(new_frame->jstack_top, (w_word)handler);
  new_frame->jstack_top += 1;
  SET_REFERENCE_SLOT(new_frame->jstack_top, (w_word)thisProxy);
  new_frame->jstack_top += 1;
  SET_REFERENCE_SLOT(new_frame->jstack_top, (w_word)currentMethod);
  new_frame->jstack_top += 1;
  SET_REFERENCE_SLOT(new_frame->jstack_top, (w_word)arguments);
  new_frame->jstack_top += 1;
  woempa(7, "Calling %m with parameters(%j, %j, %j, %j)\n", target_method, GET_SLOT_CONTENTS(new_frame->jstack_top - 4), GET_SLOT_CONTENTS(new_frame->jstack_top - 3), GET_SLOT_CONTENTS(new_frame->jstack_top - 2), GET_SLOT_CONTENTS(new_frame->jstack_top - 1));
  removeLocalReference(thread, currentMethod);
  callMethod(new_frame, target_method);

  if (exceptionThrown(thread)) {
    wrapProxyException(thread, current_method);
  }

  deactivateFrame(new_frame, NULL);
}

w_word singleProxyMethodCode(w_thread thread, w_instance thisProxy, ...) {
#ifdef JNI
  JNIEnv *env = w_thread2JNIEnv(thread);
#endif
  w_frame  current_frame = thread->top;
  w_frame  new_frame;
  w_instance handler;
  w_clazz  target_clazz;
  w_clazz  return_type;
  w_method target_method;
  w_method current_method;
  w_instance currentMethod;
  w_instance arguments;
  w_instance protect;
  w_word   result;
  va_list  arg_list;

  threadMustBeSafe(thread);

  current_method = current_frame->method;
  woempa(7, "Calling single-length proxy method %M on %J\n", current_method, thisProxy);
  while (current_method->parent) {
    current_method = current_method->parent;
  }
  woempa(7, "Parent method : %M\n", current_method);
  handler = getReferenceField(thisProxy, F_Proxy_h);
  woempa(7, "handler : %j\n", handler);
  target_clazz = instance2clazz(handler);
  woempa(7, "Target class : %K\n", target_clazz);
#ifdef JNI
  target_method = (*env)->GetMethodID(env, clazz2Class(target_clazz), "invoke", "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;");
#else
  target_method = find_method(target_clazz, "invoke", "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;");
#endif
  woempa(7, "Target method : %M\n", target_method);

  if (current_method->spec.arg_types) {
    va_start(arg_list, thisProxy);
    arguments = wrapProxyArgs(thread, current_method, arg_list);
    va_end(arg_list);
  }
  else {
    arguments = NULL;
  }

  if (exceptionThrown(thread)) {

    return 0;

  }

  return_type = current_method->spec.return_type;

  enterUnsafeRegion(thread);
  currentMethod = allocInstance(thread, clazzMethod);
  enterSafeRegion(thread);
  if (!currentMethod) {
    woempa(9, "Unable to allocate Method\n");

    return 0;

  }
  setWotsitField(currentMethod, F_Method_wotsit, current_method);
  new_frame = pushFrame(thread, target_method);
  new_frame->flags |= FRAME_REFLECTION;

  SET_REFERENCE_SLOT(new_frame->jstack_top, (w_word)handler);
  new_frame->jstack_top += 1;
  SET_REFERENCE_SLOT(new_frame->jstack_top, (w_word)thisProxy);
  new_frame->jstack_top += 1;
  SET_REFERENCE_SLOT(new_frame->jstack_top, (w_word)currentMethod);
  new_frame->jstack_top += 1;
  SET_REFERENCE_SLOT(new_frame->jstack_top, (w_word)arguments);
  new_frame->jstack_top += 1;
  woempa(7, "Calling %m with parameters(%j, %j, %j, %j)\n", target_method, GET_SLOT_CONTENTS(new_frame->jstack_top - 4), GET_SLOT_CONTENTS(new_frame->jstack_top - 3), GET_SLOT_CONTENTS(new_frame->jstack_top - 2), GET_SLOT_CONTENTS(new_frame->jstack_top - 1));
  removeLocalReference(thread, currentMethod);
  callMethod(new_frame, target_method);
  // TODO - if the exception thrown is neither declared by the interface
  // method nor a RuntimeException nor an Error, wrap it in UndeclaredThrowableException

  protect = NULL;
  result = 0;
  if (exceptionThrown(thread)) {
    wrapProxyException(thread, current_method);
  }
  else {
    if (isSet(return_type->flags, CLAZZ_IS_PRIMITIVE)) {
      w_instance wrapped = (w_instance)GET_SLOT_CONTENTS(new_frame->jstack_top - 1);
      w_word *data;

      if (wrapped) {
        getWrappedValue(wrapped, (void**)&data);
        result = *data;
      }
      else {
        throwException(thread, clazzNullPointerException, NULL);
      }
    }
    else {
      result = GET_SLOT_CONTENTS(new_frame->jstack_top - 1);
      protect = (w_instance)result;
    }
  }
  deactivateFrame(new_frame, protect);

  return result;
}

w_long doubleProxyMethodCode(w_thread thread, w_instance thisProxy, ...) {
#ifdef JNI
  JNIEnv *env = w_thread2JNIEnv(thread);
#endif
  w_frame  current_frame = thread->top;
  w_frame  new_frame;
  w_instance handler;
  w_clazz  target_clazz;
  w_clazz  return_type;
  w_method target_method;
  w_method current_method;
  w_instance currentMethod;
  w_instance arguments;
  union {w_long l; w_word w[2];} result;
  va_list  arg_list;

  threadMustBeSafe(thread);

  current_method = current_frame->method;
  woempa(7, "Calling double-length proxy method %M on %J\n", current_method, thisProxy);
  while (current_method->parent) {
    current_method = current_method->parent;
  }
  woempa(7, "Parent method : %M\n", current_method);
  handler = getReferenceField(thisProxy, F_Proxy_h);
  woempa(7, "handler : %j\n", handler);
  target_clazz = instance2clazz(handler);
  woempa(7, "Target class : %K\n", target_clazz);
#ifdef JNI
  target_method = (*env)->GetMethodID(env, clazz2Class(target_clazz), "invoke", "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;");
#else
  target_method = find_method(target_clazz, "invoke", "(Ljava/lang/Object;Ljava/lang/reflect/Method;[Ljava/lang/Object;)Ljava/lang/Object;");
#endif
  woempa(7, "Target method : %M\n", target_method);

  if (current_method->spec.arg_types) {
    va_start(arg_list, thisProxy);
    arguments = wrapProxyArgs(thread, current_method, arg_list);
    va_end(arg_list);
  }
  else {
    arguments = NULL;
  }

  if (exceptionThrown(thread)) {

    return 0LL;

  }

  return_type = current_method->spec.return_type;

  enterUnsafeRegion(thread);
  currentMethod = allocInstance(thread, clazzMethod);
  enterSafeRegion(thread);
  if (!currentMethod) {
    woempa(9, "Unable to allocate Method\n");

    return 0LL;

  }
  setWotsitField(currentMethod, F_Method_wotsit, current_method);
  new_frame = pushFrame(thread, target_method);
  new_frame->flags |= FRAME_REFLECTION;

  SET_REFERENCE_SLOT(new_frame->jstack_top, (w_word)handler);
  new_frame->jstack_top += 1;
  SET_REFERENCE_SLOT(new_frame->jstack_top, (w_word)thisProxy);
  new_frame->jstack_top += 1;
  SET_REFERENCE_SLOT(new_frame->jstack_top, (w_word)currentMethod);
  new_frame->jstack_top += 1;
  SET_REFERENCE_SLOT(new_frame->jstack_top, (w_word)arguments);
  new_frame->jstack_top += 1;
  woempa(7, "Calling %m with parameters(%j, %j, %j, %j)\n", target_method, GET_SLOT_CONTENTS(new_frame->jstack_top - 4), GET_SLOT_CONTENTS(new_frame->jstack_top - 3), GET_SLOT_CONTENTS(new_frame->jstack_top - 2), GET_SLOT_CONTENTS(new_frame->jstack_top - 1));
  removeLocalReference(thread, currentMethod);
  callMethod(new_frame, target_method);
  // TODO - if the exception thrown is neither declared by the interface
  // method nor a RuntimeException nor an Error, wrap it in UndeclaredThrowableException

  if (exceptionThrown(thread)) {
    wrapProxyException(thread, current_method);
    result.l = 0LL;
  }
  else {
    w_instance wrapped = (w_instance) GET_SLOT_CONTENTS(new_frame->jstack_top - 1);
    union {w_long l; w_word w[2];} *data;

    getWrappedValue(wrapped, (void**)&data);
    result.l = (*data).l;
  }

  deactivateFrame(new_frame, NULL);

  return result.l;
}


