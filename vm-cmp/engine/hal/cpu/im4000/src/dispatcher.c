/**************************************************************************
* Copyright (c) 2008, 2011, 2012, 2021, 2022, 2023 by KIFFER Ltd.         *
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
//#include "interpreter.h"
#include "jdwp.h"
#include "loading.h"
#include "locks.h"
#include "methods.h"
#include "misc.h"
#include "opcodes.h"
#include "mika_threads.h"
#include "wstrings.h"
#include "calls.h"
#include "fastcall.h"

extern w_hashtable lock_hashtable;
extern w_clazz clazzLinkageError;

#ifdef JNI
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
#endif

static void initialize_bytecode_dispatcher(w_frame caller, w_method method);

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

static void emulate_bytecode(w_frame caller, w_method method) {
  w_thread thread = currentWonkaThread;
  w_slot tos = (w_slot)caller->jstack_top;
  w_u64 result;

  /* WAS :
  w_word *args = allocMem(method->exec.arg_i * sizeof(w_word));
  for (w_size i = 0; i < method->exec.arg_i; ++i) {
    args[i] = GET_SLOT_CONTENTS(--tos);
  }
  activate_frame(method, method->exec.arg_i, args, &result);
  releaseMem(args);
  */

#ifndef USE_OBJECT_HASHTABLE
#error This code only works if USE_OBJECT_HASHTABLE is defined!
#endif 

  activate_frame(method, method->exec.arg_i, tos - method->exec.arg_i, &result);
  tos -= method->exec.arg_i;

  /**/

  caller->jstack_top -= method->exec.arg_i;
  // TODO assuming that we do not come here if an exception was thrown
  int n = 0;
  switch(method->exec.return_i) {
    case 2:
      SET_SLOT_CONTENTS(caller->jstack_top++, result.words[n++]);
      // fall through

    case 1:
      SET_SLOT_CONTENTS(caller->jstack_top++, result.words[n++]);
      // fall through

    case 0:
      caller->jstack_top = tos + n;
      break;

    default:
      wabort(ABORT_WONKA, "Impossible exec.return_i value : %d\n", method->exec.return_i);
  }
  thread->top = caller;
}

/**
 * Execute bytecode with no lock.
 */
static void bytecode_unsynchronized(w_frame caller, w_method method) {
  emulate_bytecode(caller, method);
}

/**
 * Execute bytecode with a lock on "this".
 */
void bytecode_instance_synchronized(w_frame caller, w_method method) {
  w_instance thiz;
  x_monitor m;
  x_status status;
  
  thiz = (w_instance) GET_SLOT_CONTENTS(caller->jstack_top - method->exec.arg_i);
  woempa(1, "Dispatching %m, lock = %j\n", method, thiz);

  m = getMonitor(thiz);

  status = x_monitor_eternal(m);
  if (status != xs_success) {
    wabort(ABORT_WONKA, "unable to obtain instance monitor: status = %d\n", status);
  }
  emulate_bytecode(caller, method);
  status = x_monitor_exit(m);
  if (status == xs_not_owner) {
    throwException(caller->thread, clazzIllegalMonitorStateException, "monitor not owned on synchronized method exit");
  }
  else if (status != xs_success) {
    wabort(ABORT_WONKA, "unable to release instance monitor: status = %d\n", status);
  }

}

/**
 * Execute bytecode with a lock on the Class object corresponding to the method's declaring class.
 */
void bytecode_static_synchronized(w_frame caller, w_method method) {
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
  emulate_bytecode(caller, method);
  status = x_monitor_exit(m);
  if (status == xs_not_owner) {
    throwException(caller->thread, clazzIllegalMonitorStateException, "monitor not owned on synchronized method exit");
  }
  else if (status != xs_success) {
    wabort(ABORT_WONKA, "unable to release class monitor: status = %d\n", status);
  }

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
  SET_REFERENCE_SLOT(caller->jstack_top-1, 0);
}

void return_iconst(w_frame caller, w_method method) {
  woempa(1, "Dispatching %m()\n", method);
  caller->jstack_top -= method->exec.arg_i - 1;
  SET_SCALAR_SLOT(caller->jstack_top-1, method->exec.code[0] == iconst_m1 ? -1 : method->exec.code[0] - iconst_0);
}

#endif // USE_SPECIAL_CASE_DISPATCHERS

void initialize_native_dispatcher(w_frame caller, w_method method) {

  w_int i;

  if (method->exec.function.void_fun == NULL) {
#ifdef JNI
    woempa(9, "Oh deary me: method %M has no code -> Trying to look it up\n", method);
    searchNativeMethod(method);
#else
    woempa(9, "Oh deary me: method %M has no code...\n", method);
    throwException(caller->thread, clazzLinkageError, "native method %M has no code...\n", method);
    return;
#endif
  }

  woempa(1, "Looking up descriptor %w in dispatchers_hashtable\n", method->spec.desc);
  w_word disp = ht_read(dispatchers_hashtable, (w_word)method->spec.desc);
  if (disp) {
    woempa(1, "Will call native code at %p using dispatcher at %0x08\n", method->exec.function.void_fun, disp);
    method->exec.dispatcher = (w_callfun)disp;
    callMethod(caller, method);

    return;
  }

  wabort(ABORT_WONKA, "No dispatcher found for descriptor %w", method->spec.desc);
}

static void prepareNativeFrame(w_frame frame, w_thread thread, w_frame caller, w_method method) {

  frame->flags = FRAME_NATIVE;
  frame->label = "frame";
  frame->previous = caller;
  frame->thread = thread;
  frame->method = method;
  frame->jstack_top = frame->jstack_base;
  SET_SLOT_SCANNING(frame->jstack_base, stack_notrace);
  frame->auxstack_base = caller->auxstack_top;
  frame->auxstack_top = caller->auxstack_top;
#ifdef TRACE_CLASSLOADERS
  { 
    w_instance loader = isSet(method->flags, ACC_STATIC) 
                        ? method->spec.declaring_clazz->loader
                        : instance2clazz(GET_SLOT_CONTENTS(frame->jstack_base - method->exec.arg_i))->loader;
    if (loader && !getBooleanField(loader, F_ClassLoader_systemDefined)) {
      frame->udcl = loader;
    }
    else {
      frame->udcl = caller->udcl;
    }
  }
#endif
}

static void prepareBytecode(w_method method);
w_boolean verifyMethod(w_method method);

static void initialize_bytecode_dispatcher(w_frame caller, w_method method) {
  void (*dispatcher)(w_frame caller, w_method method);
 
  threadMustBeSafe(caller->thread);
  
  if (isNotSet(method->flags, ACC_SYNCHRONIZED)) {
    dispatcher = bytecode_unsynchronized;
  }
  else if (isSet(method->flags, ACC_STATIC)) {
    dispatcher = bytecode_static_synchronized;
  }
  else {
    dispatcher = bytecode_instance_synchronized;
  }
 
  x_monitor_enter(&method->spec.declaring_clazz->resolutionMonitor, x_eternal);
  // Check that another thread didn't beat us to it
  if (method->exec.dispatcher == initialize_dispatcher) {
    prepareBytecode(method);
#ifdef USE_SPECIAL_CASE_DISPATCHERS
    if (dispatcher == bytecode_unsynchronized) {
      switch(method->flags & METHOD_TRIVIAL_CASES) {
      case METHOD_IS_VRETURN: 
        woempa(7, "Setting dispatcher of %M to 'void_return_only'\n", method);
        dispatcher = void_return_only;
        break;

      case METHOD_IS_RETURN_THIS:
        woempa(7, "Setting dispatcher of %M to 'return_this'\n", method);
        dispatcher = return_this;
        break;

      case METHOD_IS_RETURN_NULL:
        woempa(7, "Setting dispatcher of %M to 'return_null'\n", method);
        dispatcher = return_null;
        break;

      case METHOD_IS_RETURN_ICONST:
        woempa(7, "Setting dispatcher of %M to 'return_iconst'\n", method);
        dispatcher = return_iconst;
        break;
    
      default:
        ;
      }
    }
#endif
    method->exec.dispatcher = dispatcher;

    // TODO do we need this? probably SAFE mode is OK
    /*
    if (dispatcher_index == 1 || dispatcher_index >= 24) {
      setFlag(method->flags, METHOD_UNSAFE_DISPATCH);
      woempa(1, "Will call bytecode of %m using dispatcher[%d] in UNSAFE mode\n", method, dispatcher_index);
    }
    else {
      woempa(1, "Will call bytecode of %m using dispatcher[%d] in SAFE mode\n", method, dispatcher_index);
    }
    */

  }
  x_monitor_exit(&method->spec.declaring_clazz->resolutionMonitor);
  callMethod(caller, method);
}

