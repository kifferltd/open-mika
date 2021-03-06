/**************************************************************************
* Parts copyright (c) 2001, 2002 by Punch Telematix. All rights reserved. *
* Parts copyright (c) 2004, 2005, 2006, 2007, 2008, 2011, 2012            *
* by Chris Gray,  /k/ Embedded Java Solutions. All rights reserved.       *
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
#ifdef USE_LIBFFI
#include <ffi.h>
#endif

#include "clazz.h"
#include "constant.h"
#include "core-classes.h"
#include "descriptor.h"
#include "dispatcher.h"
#include "exception.h"
#include "hashtable.h"
#include "heap.h"
#include "interpreter.h"
#include "jdwp.h"
#include "loading.h"
#include "locks.h"
#include "methods.h"
#include "misc.h"
#include "opcodes.h"
#include "threads.h"
#include "wstrings.h"
#include "calls.h"
#include "fastcall.h"

extern w_hashtable lock_hashtable;
extern w_clazz clazzLinkageError;

static char *mangleName(w_word start, w_word stop, w_string source, char *destination) {

  char *iter = destination;
  w_word i;
  w_char c;
  char hex[5];

  for(i = start; i < stop; i++) {
    c = string_char(source, i);
    if(c & 0xFF80) { /* unicode */
      sprintf(hex, "%x", c);
      *(iter++) = '_';
      *(iter++) = '0';
      strcpy(iter, hex);
      iter += 4;
    } else {
      switch(c & 0x00FF) {
        case '.' : *(iter++) = '_'; break ;
        case '/' : *(iter++) = '_'; break ;
        case '[' : *(iter++) = '_'; *(iter++) = '3'; break;
        case ';' : *(iter++) = '_'; *(iter++) = '2'; break;
        case '_' : *(iter++) = '_'; *(iter++) = '1'; break ;
        default  : *(iter++) = c & 0x00FF;
      }
    }
  }

  return iter;

}

void searchNativeMethod(w_method method) {
  
  /* 
  ** Allocate memory for the JNI names :
  ** - short : e.g Java_java_lang_String_hashcode is fairly easy, we need 5 (Java_) + 1 
  **           (the _ between class & method) + 1 (\0 at the end) = 7 extra bytes.
  ** - long  : e.g. Java_java_lang_StringBuffer_append_Ljava_lang_String_2
  **           Add the length of the descriptor.
  ** To allow for mangling we double the variable part.
  */
  
  char    *jni_short = allocMem(7 + 2 * (string_length(method->spec.declaring_clazz->dotified) + string_length(method->spec.name)));
  w_size   jni_long_length;
  char    *jni_long;
  char    *iter = jni_short;
  w_size   i;
  
  woempa(1, "Reserving %d bytes for jni_short\n", 7 + 2 * (string_length(method->spec.declaring_clazz->dotified) + string_length(method->spec.name)));
  if (!jni_short) {
    wabort(ABORT_WONKA, "Unable to allocate jni_short\n");
  }
  strcpy(jni_short, "Java_");  /* First put in the Java_ at the beginning */
  iter += 5;                   /* Skip 5 characters */

  /*
  ** Go over the class (+package) name and add those to the jni name. Also change /'s into _'s 
  */

  iter = mangleName(0, string_length(method->spec.declaring_clazz->dotified), method->spec.declaring_clazz->dotified, iter);
  
  *(iter++) = '_';             /* The _ between class & method */
  
  /*
  ** Go over the method name and add those to the jni name. 
  */
  
  iter = mangleName(0, string_length(method->spec.name), method->spec.name, iter);
  
  *(iter++) = '\0';            /* End of the string */

  woempa(7, "JNI short : '%s'\n", jni_short);

  /*
  ** For the full blown name, we start from the short name and add the parameters
  */
  
  jni_long_length = 8 + 2* (string_length(method->spec.declaring_clazz->dotified) + string_length(method->spec.name));
  if (method->spec.arg_types) {
    for (i = 0; method->spec.arg_types[i]; ++i) {
      jni_long_length += string_length(method->spec.arg_types[i]->dotified) * 2 + 2;
    }
  }
  jni_long_length += string_length(method->spec.return_type->dotified) * 2;
  woempa(1, "Reserving %d bytes for jni_long\n", jni_long_length);
  jni_long = allocMem(jni_long_length);
  if (!jni_long) {
    wabort(ABORT_WONKA, "Unable to allocate jni_long\n");
  }
  strcpy(jni_long, jni_short); /* Make a copy */
  
  iter = (jni_long + strlen(jni_short));  /* Update our iteration pointer */
  *(iter++) = '_';             /* Add the 2 _'s between method and parameter */
  *(iter++) = '_';

  /*
  ** Go over the parameters and add those to the jni name. Also change /'s in _'s, 
  ** ;'s in _2's and ['s in _3's
  */
  
  if (method->spec.arg_types) {
    for (i = 0; method->spec.arg_types[i]; ++i) {
      w_string desc = clazz2desc(method->spec.arg_types[i]);
      if (!desc) {
        wabort(ABORT_WONKA, "Unable to create desc\n");
      }
      iter = mangleName(0, string_length(desc), desc, iter);
      deregisterString(desc);
    }
  }

  *(iter++) = '\0';            /* End of the string */
  
  woempa(7, "JNI long :  '%s'\n", jni_long);

  method->exec.function.void_fun = lookupModuleSymbol(jni_long);
  if (method->exec.function.void_fun == NULL) {
    method->exec.function.void_fun = lookupModuleSymbol(jni_short);
  }
  releaseMem(jni_short);
  releaseMem(jni_long);
}

void initialize_dispatcher(w_frame caller, w_method method) {

  if (isSet(method->flags, ACC_STATIC)) {
    if (mustBeInitialized(method->spec.declaring_clazz) == CLASS_LOADING_FAILED) {
      woempa(9, "Initializing %k threw exception %e.\n", method->spec.declaring_clazz, exceptionThrown(caller->thread));
      return;
    }
  }

  if (isSet(method->flags, ACC_NATIVE)) {
    initialize_native_dispatcher(caller, method);
  }
  else {
    initialize_bytecode_dispatcher(caller, method);
  }

}

/**
 * To interpret an unsynchronized bytecode method we just call the interpreter directly.
 */
#define interpret_unsynchronized interpret

/**
 * Invoke the interpreter with a lock on "this".
 */
void interpret_instance_synchronized(w_frame caller, w_method method) {
  w_instance thiz;
  x_monitor m;
  x_status status;
  
  thiz = (w_instance) caller->jstack_top[- method->exec.arg_i].c;
  woempa(1, "Dispatching %m, lock = %j\n", method, thiz);

  m = getMonitor(thiz);

  status = x_monitor_eternal(m);
  if (status != xs_success) {
    wabort(ABORT_WONKA, "unable to obtain instance monitor: status = %d\n", status);
  }
  interpret(caller, method);
  status = x_monitor_exit(m);
  if (status == xs_not_owner) {
    throwException(caller->thread, clazzIllegalMonitorStateException, "monitor not owned on synchronized method exit");
  }
  else if (status != xs_success) {
    wabort(ABORT_WONKA, "unable to release instance monitor: status = %d\n", status);
  }

}

/**
 * Invoke the interpreter with a lock on the Class object corresponding to the method's declaring class.
 */
void interpret_static_synchronized(w_frame caller, w_method method) {
  w_instance o;
  x_monitor m;
  x_status status;

  woempa(1, "Dispatching %m\n", method);
  o = clazz2Class(method->spec.declaring_clazz);

  m = getMonitor(o);

  status = x_monitor_eternal(m);
  if (status != xs_success) {
    wabort(ABORT_WONKA, "unable to obtain class monitor: status = %d\n", status);
  }
  interpret(caller, method);
  status = x_monitor_exit(m);
  if (status == xs_not_owner) {
    throwException(caller->thread, clazzIllegalMonitorStateException, "monitor not owned on synchronized method exit");
  }
  else if (status != xs_success) {
    wabort(ABORT_WONKA, "unable to release class monitor: status = %d\n", status);
  }

}

static void trap(w_frame caller, w_method method) {
  woempa(9, "Trapped %M\n", method);
  wabort(ABORT_WONKA, "Trapped %M\n", method);
}

void interpret_profiled(w_frame caller, w_method method) {

  trap(caller, method);

}

void interpret_instance_synchronized_profiled(w_frame caller, w_method method) {

  trap(caller, method);

}

void interpret_static_synchronized_profiled(w_frame caller, w_method method) {

  trap(caller, method);

}

#ifdef USE_SPECIAL_CASE_DISPATCHERS
void void_return_only(w_frame caller, w_method method) {
  int pops = method->exec.arg_i;
  woempa(7, "Dispatching %m\n", method);
#ifdef BACKPATCH_SPECIAL_CASES
  if (isNotSet(caller->flags, FRAME_NATIVE) && (caller->current && pops < 7)) {
    switch (*caller->current) {
    case in_invokenonvirtual:
    case in_invokestatic:
      woempa(7, "zapping call to %M at pc[%d] of %M (was: %d %d %d)\n", method, caller->current - caller->method->exec.code, caller->method, caller->current[0], caller->current[1], caller->current[2]);
      if (pops > 1) {
        caller->current[0] = pop2;
        pops -= 2;
      }
      else if (pops) {
        caller->current[0] = pop;
        pops -= 1;
      }
      else {
        caller->current[0] = nop;
      }
      if (pops > 1) {
        caller->current[1] = pop2;
        pops -= 2;
      }
      else if (pops) {
        caller->current[1] = pop;
        pops -= 1;
      }
      else {
        caller->current[1] = nop;
      }
      if (pops > 1) {
        caller->current[2] = pop2;
      }
      else if (pops) {
        caller->current[2] = pop;
      }
      else {
        caller->current[2] = nop;
      }
      woempa(1, "zapped call to %M at pc[%d] of %M (now: %d %d %d)\n", method, caller->current - caller->method->exec.code, caller->method, caller->current[0], caller->current[1], caller->current[2]);

    default:;
    }
  }
#endif

  caller->jstack_top -= pops;
}

void return_this(w_frame caller, w_method method) {
  woempa(1, "Dispatching %m()\n", method);
#ifdef BACKPATCH_SPECIAL_CASES
  if (isNotSet(caller->flags, FRAME_NATIVE)) {
    switch (*caller->current) {
    case in_invokenonvirtual:
    case in_invokestatic:
      woempa(7, "zapping call to %M at pc[%d] of %M (was: %d %d %d)\n", method, caller->current - caller->method->exec.code, caller->method, caller->current[0], caller->current[1], caller->current[2]);
      caller->current[0] = nop;
      caller->current[1] = nop;
      caller->current[2] = nop;
      woempa(1, "zapped call to %M at pc[%d] of %M (now: %d %d %d)\n", method, caller->current - caller->method->exec.code, caller->method, caller->current[0], caller->current[1], caller->current[2]);

    default:;
    }
  }
#endif
}

void return_null(w_frame caller, w_method method) {
  woempa(1, "Dispatching %m()\n", method);
  caller->jstack_top -= method->exec.arg_i - 1;
  caller->jstack_top[-1].s = stack_trace;
  caller->jstack_top[-1].c = 0;
}

void return_iconst(w_frame caller, w_method method) {
  woempa(1, "Dispatching %m()\n", method);
  caller->jstack_top -= method->exec.arg_i - 1;
  caller->jstack_top[-1].s = stack_notrace;
  caller->jstack_top[-1].c = method->exec.code[0] == iconst_m1 ? -1 : method->exec.code[0] - iconst_0;
}

void interpret_getter(w_frame caller, w_method method) {
  w_int const_index = (method->exec.code[2] << 8) | method->exec.code[3];
  w_field field = getResolvedFieldConstant(method->spec.declaring_clazz, const_index);
  w_instance objectref;
#ifdef BACKPATCH_SPECIAL_CASES
#error This code is dangerous - the calls to addResolvedFieldConstant() are not thread-safe
  w_boolean unsafe;

  woempa(1, "Dispatching %m - prelude\n", method);
  woempa(1, "calling method is %M, is %s\n", caller->previous->method, isSet(caller->previous->method->flags, ACC_NATIVE) ? "native" : "bytecode");
  if (isSet(caller->previous->method->flags, ACC_NATIVE)) {
    interpret(caller, method);
  }
  else {
    switch (*caller->current) {
    case in_invokenonvirtual:
    case in_invokestatic:
    // The method cannot be overridden, so patch the point of call to a getfield opcode.
      const_index = addResolvedFieldConstantToPool(caller->method->spec.declaring_clazz, field);
      woempa(7, "Add new resolved field (%k %w of %k) constant to %k : %d\n", field->value_clazz, field->name, field->declaring_clazz, caller->method->spec.declaring_clazz, const_index);
      woempa(7, "Replacing call to %M at pc[%d] of %M by a getfield instruction<n", method, caller->current - caller->method->exec.code, caller->method);
      caller->current[0] = getfield;
      caller->current[1] = const_index >> 8;
      caller->current[2] = const_index & 0xff;

    case in_invokevirtual:
      // OK the method can be overridden, so no possibility to backpatch the 
      // point of call. But now we are here, let's do the getfield ...
#endif
      woempa(1, "Dispatching %m\n", method);
      objectref = (w_instance)caller->jstack_top[-1].c;
      if (!objectref) {
        caller->thread->exception = allocInstance(caller->thread, clazzNullPointerException);
        removeLocalReference(caller->thread, caller->thread->exception);
      }
      else {
        caller->jstack_top[-1].s = stack_notrace;
        if (isSet(field->flags, FIELD_IS_REFERENCE)) {
          caller->jstack_top[-1].c = objectref[instance2clazz(objectref)->instanceSize + field->size_and_slot];
          caller->jstack_top[-1].s = stack_trace;
        }
#ifdef PACK_BYTE_FIELDS
        else if ((field->size_and_slot & FIELD_SIZE_MASK) <= FIELD_SIZE_8_BITS) {
          caller->jstack_top[-1].c = *byteFieldPointer(objectref, FIELD_OFFSET(field->size_and_slot));
        }
#endif
        else {
          caller->jstack_top[-1].c = wordFieldPointer(objectref, FIELD_OFFSET(field->size_and_slot))[0];
          if (isSet(field->flags, FIELD_IS_LONG)) {
            caller->jstack_top[ 0].s = stack_notrace;
            caller->jstack_top[ 0].c = wordFieldPointer(objectref, FIELD_OFFSET(field->size_and_slot))[1];
            woempa(1, "getfield %w of %j : got %08x %08x\n", field->name, objectref, caller->jstack_top[-1].c, caller->jstack_top[0].c);
            caller->jstack_top += 1;
          }
        }
      }
#ifdef BACKPATCH_SPECIAL_CASES
      break;

    default:
      unsafe = enterSafeRegion(caller->thread);
      interpret(caller, method);
      if (unsafe) {
        enterUnsafeRegion(caller->thread);
      }
    }
  }
#endif
}

void interpret_setter(w_frame caller, w_method method) {
  w_int const_index = (method->exec.code[3] << 8) | method->exec.code[4];
  w_field field = getResolvedFieldConstant(method->spec.declaring_clazz, const_index);
  w_instance objectref;
#ifdef BACKPATCH_SPECIAL_CASES
#error This code is dangerous - the calls to addResolvedFieldConstant() are not thread-safe
  w_boolean unsafe;

  woempa(1, "Dispatching %m - prelude\n", method);
  woempa(1, "calling method is %M, is %s\n", caller->previous->method, isSet(caller->previous->method->flags, ACC_NATIVE) ? "native" : "bytecode");
  if (isSet(caller->previous->method->flags, ACC_NATIVE)) {
    interpret(caller, method);
  }
  else {
    switch (*caller->current) {
    case in_invokenonvirtual:
    case in_invokestatic:
      // The method cannot be overridden, so patch the point of call to a putfield opcode.
      const_index = addResolvedFieldConstantToPool(caller->method->spec.declaring_clazz, field);
      woempa(7, "Add new resolved field (%k %w of %k) constant to %k : %d\n", field->value_clazz, field->name, field->declaring_clazz, caller->method->spec.declaring_clazz, const_index);
      woempa(7, "Replacing call to %M at pc[%d] of %M by a getfield instruction<n", method, caller->current - caller->method->exec.code, caller->method);
      caller->current[0] = putfield;
      caller->current[1] = const_index >> 8;
      caller->current[2] = const_index & 0xff;

    case in_invokevirtual:
      // OK the method can be overridden, so no possibility to backpatch the 
      // point of call. But now we are here, let's do the putfield ...
#endif
      woempa(1, "Dispatching %m()\n", method);
      objectref = (w_instance)caller->jstack_top[-2].c;
      if (!objectref) {
        caller->thread->exception = allocInstance(caller->thread, clazzNullPointerException);
        removeLocalReference(caller->thread, caller->thread->exception);
      }
      else {
        if (isSet(field->flags, FIELD_IS_REFERENCE)) {
          setReferenceField(objectref, (w_instance) caller->jstack_top[-1].c, field->size_and_slot);
        }
#ifdef PACK_BYTE_FIELDS
        else if ((field->size_and_slot & FIELD_SIZE_MASK) <= FIELD_SIZE_8_BITS) {
          *byteFieldPointer(objectref, FIELD_OFFSET(field->size_and_slot)) = (w_sbyte)caller->jstack_top[-1].c;
        }
#endif
        else { 
        // Note that we excluded methods which set 64-bit fields by requiring 
        // that arg_i == 1. Therefore we don't have to worry about them here.
          *wordFieldPointer(objectref, FIELD_OFFSET(field->size_and_slot)) = caller->jstack_top[-1].c;
        }
        caller->jstack_top -= 2;
      }
#ifdef BACKPATCH_SPECIAL_CASES
      break;

    default:
      unsafe = enterSafeRegion(caller->thread);
      interpret(caller, method);
      if (unsafe) {
        enterUnsafeRegion(caller->thread);
      }
    }
  }
#endif
}

void interpret_getstaticker(w_frame caller, w_method method) {
  w_int const_index = (method->exec.code[1] << 8) | method->exec.code[2];
  w_field field = getResolvedFieldConstant(method->spec.declaring_clazz, const_index);
#ifdef BACKPATCH_SPECIAL_CASES
  w_boolean unsafe;

  woempa(1, "Dispatching %m - prelude\n", method);
  woempa(1, "calling method is %M, is %s\n", caller->previous->method, isSet(caller->previous->method->flags, ACC_NATIVE) ? "native" : "bytecode");
  if (isSet(caller->previous->method->flags, ACC_NATIVE)) {
    interpret(caller, method);
  }
  else {
    switch (*caller->current) {
    case in_invokenonvirtual:
    case in_invokestatic:
    // The method cannot be overridden, so patch the point of call to a getfield opcode.
      const_index = addResolvedFieldConstantToPool(caller->method->spec.declaring_clazz, field);
      woempa(7, "Add new resolved field (%k %w of %k) constant to %k : %d\n", field->value_clazz, field->name, field->declaring_clazz, caller->method->spec.declaring_clazz, const_index);
      woempa(7, "Replacing call to %M at pc[%d] of %M by a getfield instruction<n", method, caller->current - caller->method->exec.code, caller->method);
      caller->current[0] = getstatic;
      caller->current[1] = const_index >> 8;
      caller->current[2] = const_index & 0xff;

    case in_invokevirtual:
      // OK the method can be overridden, so no possibility to backpatch the 
      // point of call. But now we are here, let's do the getstatic ...
#endif
      woempa(1, "Dispatching %m\n", method);
      caller->jstack_top[0].s = stack_notrace;
      caller->jstack_top[0].c = field->declaring_clazz->staticFields[field->size_and_slot];

      if (isSet(field->flags, FIELD_IS_LONG)) {
        caller->jstack_top[1].s = stack_notrace;
        caller->jstack_top[1].c = field->declaring_clazz->staticFields[field->size_and_slot + 1];
        woempa(1, "getstatic %w of %k : got %08x %08x\n", field->name, field->declaring_clazz, caller->jstack_top[0].c, caller->jstack_top[1].c);
        caller->jstack_top += 2;
      }
      else {
        if (isSet(field->flags, FIELD_IS_REFERENCE)) {
          caller->jstack_top[0].s = stack_trace;
        }
        caller->jstack_top += 1;
      }
#ifdef BACKPATCH_SPECIAL_CASES
      break;

    default:
      unsafe = enterSafeRegion(caller->thread);
      interpret(caller, method);
      if (unsafe) {
        enterUnsafeRegion(caller->thread);
      }
    }
  }
#endif
}
#endif // USE_SPECIAL_CASE_DISPATCHERS

/**
 * Table of dispatchers.
 */
w_callfun dispatchers[] = {

  initialize_dispatcher,                       //  0  will select the correct dispatcher the first time

  interpret_unsynchronized,                    //  1  interprete, unsynchronized, either static or instance calls
  interpret_instance_synchronized,             //  2  instance synchronized call
  interpret_static_synchronized,               //  3  static synchronized call
  NULL,                                        //  4  spare

  native_instance_synchronized_reference,      //  5
  native_instance_synchronized_32bits,         //  6
  native_instance_synchronized_64bits,         //  7
  native_instance_synchronized_void,           //  8

  native_instance_unsynchronized_reference,    //  9
  native_instance_unsynchronized_32bits,       // 10
  native_instance_unsynchronized_64bits,       // 11
  native_instance_unsynchronized_void,         // 12

  native_static_synchronized_reference,        // 13
  native_static_synchronized_32bits,           // 14
  native_static_synchronized_64bits,           // 15
  native_static_synchronized_void,             // 16

  native_static_unsynchronized_reference,      // 17
  native_static_unsynchronized_32bits,         // 18
  native_static_unsynchronized_64bits,         // 19
  native_static_unsynchronized_void,           // 20

  /* dispatchers for J-spot or profiling:  */
  
  interpret_profiled,                          // 21  interprete, unsynchronized, profiled, either static or instance calls
  interpret_instance_synchronized_profiled,    // 22  instance synchronized profiled call
  interpret_static_synchronized_profiled,      // 23  static synchronized profiled call

  /* Dispatchers for special cases : (a) */
#ifdef USE_SPECIAL_CASE_DISPATCHERS
  void_return_only,                            // 24  vreturn
  return_this,                                 // 25  aload_0; *return
  return_null,                                 // 26  aconst_null; areturn
  return_iconst,                               // 27  iconst; ireturn
/*
  interpret_getter,                            // 28  aload_0; getfield; *return
  interpret_setter,                            // 29  aload_0; aload1; putfield; vreturn
  interpret_getstaticker,                      // 30  getstatic; *return
*/
#endif
};

#ifdef USE_LIBFFI
static w_hashtable desclet_hashtable;

static w_string desc2desclet(w_string desc) {
  w_int i;
  w_int j;
  w_int l = 0;
  w_int dims = 0;
  w_char buff[256];

  buff[0] = 0;
  for (i = 1, j = 1; i < (w_int)string_length(desc) && !buff[0]; ++i) {
    w_char ch = string_char(desc, i);
    switch(ch) {
    case '[':
      ++dims;
      continue;

    case ')':
      l = j;
      j = 0;
      break;

    case 'L':
      while (string_char(desc, ++i) != ';');
      // fall through
    default:
      buff[j++] = dims ? 'L' : ch;
    }
    dims = 0;
  }

  return unicode2String(buff, l);
}

/**
 * Convert a descriptor char (one of V, S, I, J, B, C, F, D, Z, L) to an ffi_type.
 * It is important that the resulting type exactly match the type used inside 
 * Mika: e.g. jboolean = w_boolean = int32_t, so Z must translate to ffi_type_sint32.
 * (Whether this is really a good representation for booleans is another matter,
 * the point is that it must be consistent.)
 */
ffi_type *char2ffi_type(w_char ch) {
  switch (ch) {
  case 'V':
    return &ffi_type_void;

  case 'S':
    return &ffi_type_sint16;

  case 'I':
    return &ffi_type_sint32;

  case 'J':
    return &ffi_type_sint64;

  case 'B':
    return &ffi_type_sint8;

  case 'C':
    return &ffi_type_uint32;

  case 'F':
#ifdef HAUSER_FP
    return &ffi_type_uint32;
#else
    return &ffi_type_float;
#endif

  case 'D':
#ifdef HAUSER_FP
    return &ffi_type_uint64;
#else
    return &ffi_type_double;
#endif

  case 'Z':
    return &ffi_type_sint32;

  case 'L':
    return &ffi_type_pointer;

  default:
    wabort(ABORT_WONKA, "Dolloping doubloons! unknown type char '%02x'", ch); 
    return NULL;
  }
}

static ffi_cif *desc2cif(w_string desc) {
  w_string desclet = desc2desclet(desc);
  w_int l = (w_int)string_length(desclet);
  ffi_cif* cifptr = NULL;
  ffi_status s;
  ffi_type *rettype;
  ffi_type **argtype = allocMem(sizeof(ffi_type*) * (l + 2));;
  int i;

  // TODO: not really thread-safe, but the first call will be single-threaded anyway, right?
  if (!desclet_hashtable) {
    desclet_hashtable = ht_create("hashtable:desclets", 101, NULL, NULL, 0, 0);
  }

  ht_lock(desclet_hashtable);
  cifptr = (ffi_cif*)ht_read_no_lock(desclet_hashtable, (w_word)desclet);
  if (cifptr) {
    releaseMem(argtype);
  }
  else {
    rettype = char2ffi_type(string_char(desclet, 0));
    argtype[0] = &ffi_type_pointer;
    argtype[1] = &ffi_type_pointer;
    for (i = 1; i < l; ++i) {
      w_char ch = string_char(desclet, i);
      argtype[i + 1] = char2ffi_type(ch);
    }
    cifptr = allocClearedMem(sizeof(ffi_cif));
    s = ffi_prep_cif (cifptr, FFI_DEFAULT_ABI, string_length(desclet) + 1, rettype, argtype);

    ht_write_no_lock(desclet_hashtable, (w_word)desclet, (w_word)cifptr);
  }
  ht_unlock(desclet_hashtable);

  return cifptr;
}
#endif

void initialize_native_dispatcher(w_frame caller, w_method method) {

  w_int i;

  if (method->exec.function.void_fun == NULL) {
    woempa(9, "Oh deary me: method %M has no code -> Trying to look it up\n", method);
    searchNativeMethod(method);
    if (method->exec.function.void_fun == NULL) {
      woempa(9, "Oh deary me: method %M has no code...\n", method);
      throwException(caller->thread, clazzLinkageError, "native method %M has no code...\n", method);
      return;
    }
  }

  if (isSet(method->flags, ACC_STATIC)) {
    i = 13;
  }
  else {
    i = 5;
  }

  if (isNotSet(method->flags, ACC_SYNCHRONIZED)) {
    i += 4;
  }

  if (method->spec.return_type == clazz_void) {
    i += 3;
  }
  else if (method->exec.return_i == 2) {
    i += 2;
  }
  else if (isSet(method->spec.return_type->flags, CLAZZ_IS_PRIMITIVE)) {
    i += 1;
  }

  woempa(1, "Will call native code at %p using dispatcher[%d]\n", method->exec.function.void_fun, i);
  method->exec.dispatcher = dispatchers[i];
#ifdef USE_LIBFFI
  method->exec.cif = desc2cif(method->desc);
#endif

  callMethod(caller, method);
}

static void prepareNativeFrame(w_frame frame, w_thread thread, w_frame caller, w_method method) {

  frame->flags = FRAME_NATIVE;
  frame->label = "frame";
  frame->previous = caller;
  frame->thread = thread;
  frame->method = method;
  frame->jstack_top = frame->jstack_base;
  frame->jstack_base[0].s = stack_notrace;
  frame->auxstack_base = caller->auxstack_top;
  frame->auxstack_top = caller->auxstack_top;
#ifdef TRACE_CLASSLOADERS
  { 
    w_instance loader = isSet(method->flags, ACC_STATIC) 
                        ? method->spec.declaring_clazz->loader
                        : instance2clazz(frame->jstack_base[- method->exec.arg_i].c)->loader;
    if (loader && !getBooleanField(loader, F_ClassLoader_systemDefined)) {
      frame->udcl = loader;
    }
    else {
      frame->udcl = caller->udcl;
    }
  }
#endif
}

void native_instance_synchronized_reference(w_frame caller, w_method method) {
  w_Frame theFrame;
  w_frame frame = &theFrame; 
  w_int idx = - method->exec.arg_i;
  w_instance o;
  x_monitor m;
  volatile w_thread thread = caller->thread;
  w_long long_result;
  w_instance ref_result;

  woempa(1, "Calling %M\n", method);
  frame->jstack_base = caller->jstack_top; 
  prepareNativeFrame(frame, thread, caller, method);

  threadMustBeSafe(thread);

  o = (w_instance) caller->jstack_top[idx].c;
  m = getMonitor(o);
  x_monitor_eternal(m);

  thread->top = frame;

  frame->jstack_top[0].c = 0;
  frame->jstack_top[0].s = stack_trace;
  frame->jstack_top += 1;
  long_result = _call_instance(w_thread2JNIEnv(thread), (w_slot)caller->jstack_top, method);
  // WAS: ref_result = *((w_instance*)&long_result);
  memcpy(&ref_result, &long_result, sizeof(w_instance));
  x_monitor_exit(m);

  if (thread->exception) {
    woempa(1, "%m threw %e, ignoring return value\n", method, thread->exception);
    caller->jstack_top[idx].s = stack_notrace;
    caller->jstack_top += idx + 1;
    thread->top = caller;
  }
  else {
    enterUnsafeRegion(thread);
    woempa(1, "%m result = %08x\n", method, ref_result);
    caller->jstack_top[idx].c = (w_word)ref_result;
    caller->jstack_top[idx].s = stack_trace;
    caller->jstack_top += idx + 1;
    if (ref_result) {
      setFlag(instance2flags(ref_result), O_BLACK);
    }
    thread->top = caller;
    enterSafeRegion(thread);
  }
}

void native_instance_synchronized_32bits(w_frame caller, w_method method) {

  w_Frame theFrame;
  w_frame frame = &theFrame;
  w_int idx = - method->exec.arg_i;
  w_instance o;
  x_monitor m;
  volatile w_thread thread = caller->thread;
  w_long long_result;

  woempa(1, "Calling %M\n", method);
  frame->jstack_base = caller->jstack_top; 
  prepareNativeFrame(frame, thread, caller, method);

  threadMustBeSafe(thread);

  o = (w_instance) caller->jstack_top[idx].c;
  m = getMonitor(o);
  x_monitor_eternal(m);

  thread->top = frame;
  
  long_result = _call_instance(w_thread2JNIEnv(thread), (w_slot)caller->jstack_top, method);
  x_monitor_exit(m);

  enterUnsafeRegion(thread);
  caller->jstack_top += idx;
  caller->jstack_top[0].s = stack_notrace;
  // WAS: caller->jstack_top[0].c = *((w_word*)&long_result);
  memcpy((w_word*)&caller->jstack_top[0].c, &long_result, sizeof(w_word));
  woempa(1, "%m result = %08x\n", method, caller->jstack_top[0].c);
  caller->jstack_top += 1;
  thread->top = caller;
  enterSafeRegion(thread);
}

void native_instance_synchronized_64bits(w_frame caller, w_method method) {

  w_Frame theFrame;
  w_frame frame = &theFrame;
  w_int idx = - method->exec.arg_i;
  w_instance o;
  x_monitor m;
  volatile w_thread thread = caller->thread;
  union {w_long l; w_word w[2];} result;

  woempa(1, "Calling %M\n", method);
  frame->jstack_base = caller->jstack_top; 
  prepareNativeFrame(frame, thread, caller, method);

  threadMustBeSafe(thread);

  o = (w_instance) caller->jstack_top[idx].c;
  m = getMonitor(o);
  x_monitor_eternal(m);

  thread->top = frame;
  
  result.l= _call_instance(w_thread2JNIEnv(thread), (w_slot)caller->jstack_top, method);
  x_monitor_exit(m);

  enterUnsafeRegion(thread);
  caller->jstack_top += idx;
  caller->jstack_top[0].s = stack_notrace;
  caller->jstack_top[0].c = result.w[0];
  caller->jstack_top[1].s = stack_notrace;
  caller->jstack_top[1].c = result.w[1];
  woempa(1, "%m result = %08x %08x\n", method, caller->jstack_top[0].c, caller->jstack_top[1].c);
  caller->jstack_top += 2;
  thread->top = caller;
  enterSafeRegion(thread);
}

void native_instance_synchronized_void(w_frame caller, w_method method) {

  w_Frame theFrame;
  w_frame frame = &theFrame;
  w_int idx = - method->exec.arg_i;
  w_instance o;
  x_monitor m;
  volatile w_thread thread = caller->thread;
 
  woempa(1, "Calling %M\n", method);
  frame->jstack_base = caller->jstack_top; 
  prepareNativeFrame(frame, thread, caller, method);

  threadMustBeSafe(thread);

  o = (w_instance) caller->jstack_top[idx].c;
  m = getMonitor(o);
  x_monitor_eternal(m);

  thread->top = frame;
  
  _call_instance(w_thread2JNIEnv(thread), (w_slot)caller->jstack_top, method);
  x_monitor_exit(m);

  enterUnsafeRegion(thread);
  caller->jstack_top += idx;
  thread->top = caller;
  enterSafeRegion(thread);

}

void native_instance_unsynchronized_reference(w_frame caller, w_method method) {

  w_Frame theFrame;
  w_frame frame = &theFrame;
  w_int idx = - method->exec.arg_i;
  volatile w_thread thread = caller->thread;
  w_long long_result;
  w_instance ref_result;
  
  woempa(1, "Calling %M\n", method);
  frame->jstack_base = caller->jstack_top; 
  prepareNativeFrame(frame, thread, caller, method);

  thread->top = frame;
  
  frame->jstack_top[0].c = 0;
  frame->jstack_top[0].s = stack_trace;
  frame->jstack_top += 1;
  long_result = _call_instance(w_thread2JNIEnv(thread), (w_slot)caller->jstack_top, method);
  // WAS: ref_result = *((w_instance*)&long_result);
  memcpy(&ref_result, &long_result, sizeof(w_instance));

  if (thread->exception) {
    woempa(1, "%m threw %e, ignoring return value\n", method, thread->exception);
    caller->jstack_top[idx].s = stack_notrace;
    caller->jstack_top += idx + 1;
    thread->top = caller;
  }
  else {
    enterUnsafeRegion(thread);
    woempa(1, "%m result = %08x\n", method, ref_result);
    caller->jstack_top[idx].c = (w_word)ref_result;
    caller->jstack_top[idx].s = stack_trace;
    caller->jstack_top += idx + 1;
    // See note above
    if (ref_result) {
      setFlag(instance2flags(ref_result), O_BLACK);
    }
    thread->top = caller;
    enterSafeRegion(thread);
  }
}

void native_instance_unsynchronized_32bits(w_frame caller, w_method method) {

  w_Frame theFrame;
  w_frame frame = &theFrame;
  w_long  long_result;
  w_int idx = - method->exec.arg_i;
  volatile w_thread thread = caller->thread;

  woempa(1, "Calling %M\n", method);
  frame->jstack_base = caller->jstack_top; 
  prepareNativeFrame(frame, thread, caller, method);

  thread->top = frame;
  long_result = _call_instance(w_thread2JNIEnv(thread), (w_slot)caller->jstack_top, method);

  enterUnsafeRegion(thread);
  caller->jstack_top += idx;
  caller->jstack_top[0].s = stack_notrace;
  // WAS: caller->jstack_top[0].c = *((w_word*)&long_result);
  memcpy((w_word*)&caller->jstack_top[0].c, &long_result, sizeof(w_word));
  woempa(1, "%m result = %08x\n", method, caller->jstack_top[0].c);
  caller->jstack_top += 1;
  thread->top = caller;
  enterSafeRegion(thread);
}

void native_instance_unsynchronized_64bits(w_frame caller, w_method method) {

  w_Frame theFrame;
  w_frame frame = &theFrame;
  union {w_long l; w_word w[2];} result;
  w_int idx = - method->exec.arg_i;
  volatile w_thread thread = caller->thread;

  woempa(1, "Calling %M\n", method);
  frame->jstack_base = caller->jstack_top; 
  prepareNativeFrame(frame, thread, caller, method);

  thread->top = frame;
  
  result.l = _call_instance(w_thread2JNIEnv(thread), (w_slot)caller->jstack_top, method);

  enterUnsafeRegion(thread);
  caller->jstack_top += idx;
  caller->jstack_top[0].s = stack_notrace;
  caller->jstack_top[0].c = result.w[0];
  caller->jstack_top[1].s = stack_notrace;
  caller->jstack_top[1].c = result.w[1];
  woempa(1, "%m result = %08x %08x\n", method, caller->jstack_top[0].c, caller->jstack_top[1].c);
  caller->jstack_top += 2;
  thread->top = caller;
  enterSafeRegion(thread);
}

void native_instance_unsynchronized_void(w_frame caller, w_method method) {

  w_Frame theFrame;
  w_frame frame = &theFrame;
  w_int idx = - method->exec.arg_i;
  volatile w_thread thread = caller->thread;

  woempa(1, "Calling %M\n", method);
  frame->jstack_base = caller->jstack_top; 
  prepareNativeFrame(frame, thread, caller, method);

  thread->top = frame;
  
  _call_instance(w_thread2JNIEnv(thread), (w_slot)caller->jstack_top, method);

  enterUnsafeRegion(thread);
  caller->jstack_top += idx;
  thread->top = caller;
  enterSafeRegion(thread);
}

void native_static_synchronized_reference(w_frame caller, w_method method) {

  w_Frame theFrame;
  w_frame frame = &theFrame;
  w_int idx = - method->exec.arg_i;
  w_instance o;
  x_monitor m;
  volatile w_thread thread = caller->thread;
  w_long long_result;
  w_instance ref_result;

  woempa(1, "Calling %M\n", method);
  frame->jstack_base = caller->jstack_top; 
  prepareNativeFrame(frame, thread, caller, method);

  threadMustBeSafe(thread);

  o = clazz2Class(frame->method->spec.declaring_clazz);
  m = getMonitor(o);
  x_monitor_eternal(m);

  thread->top = frame;
  
  frame->jstack_top[0].c = 0;
  frame->jstack_top[0].s = stack_trace;
  frame->jstack_top += 1;
  long_result = _call_static(w_thread2JNIEnv(thread), o, (w_slot)caller->jstack_top, method);
  // WAS: ref_result = *((w_instance*)&long_result);
  memcpy(&ref_result, &long_result, sizeof(w_instance));
  x_monitor_exit(m);
  if (thread->exception) {
    woempa(1, "%m threw %e, ignoring return value\n", method, thread->exception);
    caller->jstack_top[idx].s = stack_notrace;
    caller->jstack_top += idx + 1;
    thread->top = caller;
  }
  else {
    enterUnsafeRegion(thread);
    woempa(1, "%m result = %08x\n", method, ref_result);
    caller->jstack_top[idx].c = (w_word)ref_result;
    caller->jstack_top[idx].s = stack_trace;
    caller->jstack_top += idx + 1;
    // See remark above
    if (ref_result) {
      setFlag(instance2flags(ref_result), O_BLACK);
    }
    thread->top = caller;
    enterSafeRegion(thread);
  }
}

void native_static_synchronized_32bits(w_frame caller, w_method method) {

  w_Frame theFrame;
  w_frame frame = &theFrame;
  w_long long_result;
  w_int idx = - method->exec.arg_i;
  w_instance o;
  x_monitor m;
  volatile w_thread thread = caller->thread;

  woempa(1, "Calling %M\n", method);
  frame->jstack_base = caller->jstack_top; 
  prepareNativeFrame(frame, thread, caller, method);

  threadMustBeSafe(thread);

  o = clazz2Class(frame->method->spec.declaring_clazz);
  m = getMonitor(o);
  x_monitor_eternal(m);

  thread->top = frame;
  
  long_result = _call_static(w_thread2JNIEnv(thread), o, (w_slot)caller->jstack_top, method);
  x_monitor_exit(m);

  enterUnsafeRegion(thread);
  caller->jstack_top += idx;
  caller->jstack_top[0].s = stack_notrace;
  // WAS: caller->jstack_top[0].c = *((w_word*)&long_result);
  memcpy((w_word*)&caller->jstack_top[0].c, &long_result, sizeof(w_word));
  woempa(1, "%m result = %08x\n", method, caller->jstack_top[0].c);
  caller->jstack_top += 1;
  thread->top = caller;
  enterSafeRegion(thread);
}

void native_static_synchronized_64bits(w_frame caller, w_method method) {

  w_Frame theFrame;
  w_frame frame = &theFrame;
  union {w_long l; w_word w[2];} result;
  w_int idx = - method->exec.arg_i;
  w_instance o;
  x_monitor m;
  volatile w_thread thread = caller->thread;

  woempa(1, "Calling %M\n", method);
  frame->jstack_base = caller->jstack_top; 
  prepareNativeFrame(frame, thread, caller, method);

  threadMustBeSafe(thread);

  o = clazz2Class(frame->method->spec.declaring_clazz);
  m = getMonitor(o);
  x_monitor_eternal(m);

  thread->top = frame;
  
  result.l = _call_static(w_thread2JNIEnv(thread), o, (w_slot)caller->jstack_top, method);
  x_monitor_exit(m);

  enterUnsafeRegion(thread);
  caller->jstack_top += idx;
  caller->jstack_top[0].s = stack_notrace;
  caller->jstack_top[0].c = result.w[0];
  caller->jstack_top[1].s = stack_notrace;
  caller->jstack_top[1].c = result.w[1];
  woempa(1, "%m result = %08x %08x\n", method, caller->jstack_top[0].c, caller->jstack_top[1].c);
  caller->jstack_top += 2;
  thread->top = caller;
  enterSafeRegion(thread);
}

void native_static_synchronized_void(w_frame caller, w_method method) {

  w_Frame theFrame;
  w_frame frame = &theFrame;
  w_int idx = - method->exec.arg_i;
  w_instance o;
  x_monitor m;
  volatile w_thread thread = caller->thread;

  woempa(1, "Calling %M\n", method);
  frame->jstack_base = caller->jstack_top; 
  prepareNativeFrame(frame, thread, caller, method);

  threadMustBeSafe(thread);

  o = clazz2Class(frame->method->spec.declaring_clazz);
  m = getMonitor(o);
  x_monitor_eternal(m);

  thread->top = frame;
  
  _call_static(w_thread2JNIEnv(thread), o, (w_slot)caller->jstack_top, method);
  x_monitor_exit(m);

  enterUnsafeRegion(thread);
  caller->jstack_top += idx;
  thread->top = caller;
  enterSafeRegion(thread);
}

void native_static_unsynchronized_reference(w_frame caller, w_method method) {

  w_Frame theFrame;
  w_frame frame = &theFrame;
  w_int idx = - method->exec.arg_i;
  volatile w_thread thread = caller->thread;
  w_long long_result;
  w_instance ref_result;

  woempa(1, "Calling %M\n", method);
  frame->jstack_base = caller->jstack_top; 
  prepareNativeFrame(frame, thread, caller, method);

  thread->top = frame;
  
  frame->jstack_top[0].c = 0;
  frame->jstack_top[0].s = stack_trace;
  frame->jstack_top += 1;
  long_result = _call_static(w_thread2JNIEnv(thread), clazz2Class(frame->method->spec.declaring_clazz), (w_slot)caller->jstack_top, method);
  // WAS: ref_result  = *((w_instance*)&long_result);
  memcpy(&ref_result, &long_result, sizeof(w_instance));
  if (thread->exception) {
    woempa(1, "%m threw %e, ignoring return value\n", method, thread->exception);
    caller->jstack_top[idx].s = stack_notrace;
    caller->jstack_top += idx + 1;
    thread->top = caller;
  }
  else {
    enterUnsafeRegion(thread);
    woempa(1, "%m result = %08x\n", method, ref_result);
    caller->jstack_top[idx].c = (w_word)ref_result;
    caller->jstack_top[idx].s = stack_trace;
    caller->jstack_top += idx + 1;
    if (ref_result) {
      setFlag(instance2flags(ref_result), O_BLACK);
    }
    thread->top = caller;
    enterSafeRegion(thread);
  }
}

void native_static_unsynchronized_32bits(w_frame caller, w_method method) {

  w_Frame theFrame;
  w_frame frame = &theFrame;
  w_long long_result;
  w_int idx = - method->exec.arg_i;
  volatile w_thread thread = caller->thread;

  woempa(1, "Calling %M\n", method);
  frame->jstack_base = caller->jstack_top; 
  prepareNativeFrame(frame, thread, caller, method);

  thread->top = frame;
  
  long_result = _call_static(w_thread2JNIEnv(thread), clazz2Class(frame->method->spec.declaring_clazz), (w_slot)caller->jstack_top, method);

  enterUnsafeRegion(thread);
  caller->jstack_top += idx;
  caller->jstack_top[0].s = stack_notrace;
  // WAS: caller->jstack_top[0].c = ((w_word*)&long_result)[0];
  memcpy((w_word*)&caller->jstack_top[0].c, &long_result, sizeof(w_word));
  woempa(1, "%m result = %08x\n", method, caller->jstack_top[0].c);
  caller->jstack_top += 1;
  thread->top = caller;
  enterSafeRegion(thread);
}

void native_static_unsynchronized_64bits(w_frame caller, w_method method) {

  w_Frame theFrame;
  w_frame frame = &theFrame;
  union {w_long l; w_word w[2];} result;
  w_int idx = - method->exec.arg_i;
  volatile w_thread thread = caller->thread;

  woempa(1, "Calling %M\n", method);
  frame->jstack_base = caller->jstack_top; 
  prepareNativeFrame(frame, thread, caller, method);

  thread->top = frame;
  
  result.l = _call_static(w_thread2JNIEnv(thread), clazz2Class(frame->method->spec.declaring_clazz), (w_slot)caller->jstack_top, method);

  enterUnsafeRegion(thread);
  caller->jstack_top += idx;
  caller->jstack_top[0].s = stack_notrace;
  caller->jstack_top[0].c = result.w[0];
  caller->jstack_top[1].s = stack_notrace;
  caller->jstack_top[1].c = result.w[1];
  woempa(1, "%m result = %08x %08x\n", method, caller->jstack_top[0].c, caller->jstack_top[1].c);
  caller->jstack_top += 2;
  thread->top = caller;
  enterSafeRegion(thread);
}

void native_static_unsynchronized_void(w_frame caller, w_method method) {

  w_Frame theFrame;
  w_frame frame = &theFrame;
  w_int idx = - method->exec.arg_i;
  volatile w_thread thread = caller->thread;

  woempa(1, "Calling %M\n", method);
  frame->jstack_base = caller->jstack_top; 
  prepareNativeFrame(frame, thread, caller, method);

  thread->top = frame;
  
  _call_static(w_thread2JNIEnv(thread), clazz2Class(frame->method->spec.declaring_clazz), (w_slot)caller->jstack_top, method);

  enterUnsafeRegion(thread);
  caller->jstack_top += idx;
  thread->top = caller;
  enterSafeRegion(thread);
}

static void prepareBytecode(w_method method);
w_boolean verifyMethod(w_method method);

void initialize_bytecode_dispatcher(w_frame caller, w_method method) {
  w_int dispatcher_index;
 
  threadMustBeSafe(caller->thread);
  dispatcher_index = 1;   // interprete
  
  if (isSet(method->flags, ACC_SYNCHRONIZED)) {
    if (isSet(method->flags, ACC_STATIC)) {
      dispatcher_index += 2;
    }
    else {
      dispatcher_index += 1;
    }
  }
/*
#ifdef USE_SPECIAL_CASE_DISPATCHERS
  else if (isSet(method->flags, METHOD_NO_OVERRIDE | ACC_STATIC) && method->exec.arg_i == 1 && method->exec.code_length == 5 && method->exec.code[0] == aload_0 && method->exec.code[1] == getfield && method->exec.code[4] >= ireturn && method->exec.code[4] <= areturn) {
    woempa(7, "Identified a GETTER %M %d %d %d %d %d\n", method, opcode_names[method->exec.code[0]], opcode_names[method->exec.code[1]], method->exec.code[2], method->exec.code[3], opcode_names[method->exec.code[4]]); 
    getFieldConstant(method->spec.declaring_clazz, (method->exec.code[2] << 8) | method->exec.code[3]);
    if (caller->thread->exception) {
      dispatcher_index = 1;
    }
    else {
      dispatcher_index = 28;
    }
  }
  else if (isSet(method->flags, METHOD_NO_OVERRIDE | ACC_STATIC) && method->exec.arg_i == 2 && method->exec.code_length == 6 && method->exec.code[0] == aload_0 && method->exec.code[1] == aload_1 && method->exec.code[2] == putfield && method->exec.code[5] == vreturn) {
    woempa(7, "Identified a PUTTER %M %d %d %d %d %d\n", method, method->exec.code[0], method->exec.code[1], method->exec.code[2], method->exec.code[3], method->exec.code[4], method->exec.code[5]); 
    getFieldConstant(method->spec.declaring_clazz, (method->exec.code[3] << 8) | method->exec.code[4]);
    if (caller->thread->exception) {
      dispatcher_index = 1;
    }
    else {
      dispatcher_index = 29;
    }
  }
  else if (isSet(method->flags, METHOD_NO_OVERRIDE | ACC_STATIC) && method->exec.arg_i == 0 && method->exec.code_length == 4 && method->exec.code[0] == getstatic && method->exec.code[3] >= ireturn && method->exec.code[3] <= areturn) {
    woempa(7, "Identified a GETSTATIC %M %d %d %d\n", method, method->exec.code[1], method->exec.code[2], method->exec.code[3]);
    getFieldConstant(method->spec.declaring_clazz, (method->exec.code[1] << 8) | method->exec.code[2]);
    if (caller->thread->exception) {
      dispatcher_index = 1;
    }
    else {
      dispatcher_index = 30;
    }
  }
#endif
*/

  x_monitor_enter(method->spec.declaring_clazz->resolution_monitor, x_eternal);
  // Check that another thread didn't beat us to it
  if (method->exec.dispatcher == initialize_dispatcher) {
    prepareBytecode(method);
#ifdef USE_SPECIAL_CASE_DISPATCHERS
    if (dispatcher_index < 2) {
      switch(method->flags & METHOD_TRIVIAL_CASES) {
      case METHOD_IS_VRETURN: 
        woempa(7, "Setting dispatcher of %M to 'void_return_only'\n", method);
        dispatcher_index = 24;
        break;

      case METHOD_IS_RETURN_THIS:
        woempa(7, "Setting dispatcher of %M to 'return_this'\n", method);
        dispatcher_index = 25;
        break;

      case METHOD_IS_RETURN_NULL:
        woempa(7, "Setting dispatcher of %M to 'return_null'\n", method);
        dispatcher_index = 26;
        break;

      case METHOD_IS_RETURN_ICONST:
        woempa(7, "Setting dispatcher of %M to 'return_iconst'\n", method);
        dispatcher_index = 27;
        break;
    
      default:
        ;
      }
    }
#endif
    method->exec.dispatcher = dispatchers[dispatcher_index];
    if (dispatcher_index == 1 || dispatcher_index >= 24) {
      setFlag(method->flags, METHOD_UNSAFE_DISPATCH);
      woempa(7, "Will call bytecode of %m using dispatcher[%d] in UNSAFE mode\n", method, dispatcher_index);
    }
    else {
      woempa(7, "Will call bytecode of %m using dispatcher[%d] in SAFE mode\n", method, dispatcher_index);
    }
  }
  x_monitor_exit(method->spec.declaring_clazz->resolution_monitor);
  callMethod(caller, method);
}

