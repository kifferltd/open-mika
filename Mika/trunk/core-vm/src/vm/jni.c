/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved.                                                               *
* Parts copyright (c) 2004, 2005, 2006, 2007 by Chris Gray, /k/ Embedded  *
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
#include <stdio.h>
#include <stdarg.h>

#include "jni.h"

#include "arrays.h"
#include "bar.h"
#include "clazz.h"
#include "core-classes.h"
#include "descriptor.h"
#include "fields.h"
#include "hashtable.h"
#include "heap.h"
#include "interpreter.h"
#include "loading.h"
#include "threads.h"
#include "wmath.h"
#include "wstrings.h"
#include "checks.h"
#include "locks.h"
#include "exception.h"
#include "ts-mem.h"
#include "methods.h"
#include "misc.h"

w_hashtable globals_hashtable;

#define GLOBALS_HASHTABLE_SIZE   1439

#ifdef RUDOLPH
#ifndef MODULES
extern void init_awt(void);
#endif
#endif

struct JavaVMInitArgs *system_vm_args;

typedef union JNITypes {
  jboolean  z;
  jbyte     b;
  jchar     c;
  jshort    s;
  jint      i;
  jfloat    f;
  jlong     j;
  jdouble   d;
  jobject   o;
  w_word    w0;
  w_word    w[2];
} JNITypes;

static jclass class_NativeThread;
static jmethodID jmethodID_underscore;
static jclass class_Runtime;
static jmethodID jmethodID_loadLibrary0;

static jmethodID get_underscore_jmethodID(JNIEnv *env) {
  if (!class_NativeThread) {
    class_NativeThread = clazz2Class(clazzNativeThread);
  }

  if (!jmethodID_underscore) {
    jmethodID_underscore = (*env)->GetMethodID(env, class_NativeThread, "_", "()V");
    if (!jmethodID_underscore) {
      wabort(ABORT_WONKA, "Unable to locate method NativeThread._\n");
    }
  }

  return jmethodID_underscore;
}

static jmethodID get_loadLibrary0_jmethodID(JNIEnv *env) {
  if (!class_Runtime) {
    class_Runtime = clazz2Class(clazzRuntime);
  }

  if (!jmethodID_loadLibrary0) {
    jmethodID_loadLibrary0 = (*env)->GetMethodID(env, class_Runtime, "loadLibrary0", "(Ljava/lang/String;Ljava/lang/String;)V");
    if (!jmethodID_loadLibrary0) {
      wabort(ABORT_WONKA, "Unable to locate method Runtime._\n");
    }
  }

  return jmethodID_loadLibrary0;
}

/*
** Write a C array of booleans (stored one per byte) into a Java array instance.
*/

void writeBooleansToArray(w_instance Array, w_int start, w_int length, jboolean *elements) {
  w_int    i;
  w_sbyte *  data_8;
  w_byte   bitmask;

  /*
  ** Find the correct offset (start / 8) and the correct bitposition
  ** (1 << (start % 8)) to start with...
  */

  data_8 = instance2Array_byte(Array) + (int)(start / 8);
  bitmask = 1;
  if (start % 8) bitmask <<= (start % 8);
  for (i = 0; i < length; i++) {
    if (elements[i]) {
      (*data_8) |=  bitmask;   /* set bit TRUE */
    }
    else {
      (*data_8) &= ~bitmask;   /* set bit FALSE */
    }
    
    bitmask <<= 1;
    if ((bitmask&0xff) == 0) {
      data_8 += 1;
      bitmask = 1;
    }
  }
  woempa(1, "(JNI) Wrote %d boolean elements from %d of Array %p.\n", length, start, Array);

}

/*
** Write a C array of bytes back into a Java array instance.
*/

void writeBytesToArray(w_instance Array, w_int start, w_int length, jbyte *elements) {
  w_sbyte *  dest = instance2Array_byte(Array) + start;

  w_memcpy(dest,elements,length*sizeof(w_byte));
  woempa(1, "(JNI) Wrote %d 8 bit elements from %d of Array %p.\n", length, start, Array);
}

/*
** Write a C array of shorts back into a Java array instance.
*/

void writeShortsToArray(w_instance Array, w_int start, w_int length, jshort *elements) {
  w_short * dest = instance2Array_short(Array) + start;

  w_memcpy(dest,elements,length*sizeof(w_short));
  woempa(1, "(JNI) Wrote %d 16 bit elements from %d of Array %p.\n", length, start, Array);
}

/*
** Write a C array of chars back into a Java array instance.
*/

void writeCharsToArray(w_instance Array, w_int start, w_int length, jchar *elements) {
  w_char* dest = instance2Array_char(Array) + start;

  w_memcpy(dest,elements,length*sizeof(w_char));
  woempa(1, "(JNI) Wrote %d 16 bit elements from %d of Array %p.\n", length, start, Array);
}

/*
** Write a C array of words back into a Java array instance.
*/

void writeIntsToArray(w_instance Array, w_int start, w_int length, w_int*elements) {
  w_int* dest = instance2Array_int(Array) + start;

  w_memcpy(dest,elements,length*sizeof(w_int));
  woempa(1, "(JNI) Written %d ints from %d of Array %p.\n", length, start, Array);
}

/*
** Write a C array of longs back into a Java array instance.
*/

void writeLongsToArray(w_instance Array, w_int start, w_int length, w_long *elements) {
  w_long* dest = instance2Array_long(Array) + start;

  w_memcpy(dest,elements,length*sizeof(w_long));
  woempa(1, "(JNI) Written %d longs from %d of Array %p.\n", length, start, Array);
}

/*
** Write a C array of floats back into a Java array instance.
*/

void writeFloatsToArray(w_instance Array, w_int start, w_int length, w_float *elements) {
  w_float* dest = instance2Array_float(Array) + start;

  w_memcpy(dest,elements,length*sizeof(w_float));
  woempa(1, "(JNI) Written %d 32 bit elements from %d of Array %p.\n", length, start, Array);
}

/*
** Write a C array of doubles back into a Java array instance.
*/

void writeDoublesToArray(w_instance Array, w_int start, w_int length, w_double *elements) {
  w_double* dest = instance2Array_double(Array) + start;

  w_memcpy(dest,elements,length*sizeof(w_double));
  woempa(1, "(JNI) Written %d doubles from %d of Array %p.\n", length, start, Array);
}

void readBooleansFromArray(w_instance Array, w_int start, w_int length, jboolean *buffer) {

  w_sbyte*  data_8;
  w_int    i;
  w_byte   bitmask;

  /*
  ** Find the correct offset (start / 8) and the correct bitposition
  ** (1 << (start % 8)) to start with...
  */

  data_8 = instance2Array_byte(Array) + (int)(start / 8);
  bitmask = 1 << (start % 8);
  for (i = 0; i < length; i++) {
    buffer[i] = (w_word)(*data_8 & bitmask);
    bitmask <<= 1;
    if (bitmask == 0) {
      data_8 += 1;
      bitmask = 1;
    }
  }
  woempa(1, "(JNI) Read %d boolean elements from Array %p offset %d into buffer %p.\n", length, Array, start, buffer);
}

void readBytesFromArray(w_instance Array, w_int start, w_int length, jbyte *buffer) {
  w_sbyte*  source = instance2Array_byte(Array) + start;

  w_memcpy(buffer,source,length*sizeof(w_byte));
  woempa(1, "(JNI) Read %d bytes from Array %p offset %d into buffer %p.\n", length, Array, start, buffer);
}

void readCharsFromArray(w_instance Array, w_int start, w_int length, w_char *buffer) {
  w_char*  source = instance2Array_char(Array) + start;

  w_memcpy(buffer,source,length*sizeof(w_char));
  woempa(1, "(JNI) Read %d chars from Array %p offset %d into buffer %p.\n", length, Array, start, buffer);
}

void readShortsFromArray(w_instance Array, w_int start, w_int length, w_short *buffer) {
  w_short*  source = instance2Array_short(Array) + start;

  w_memcpy(buffer,source,length*sizeof(w_short));
  woempa(1, "(JNI) Read %d shorts from Array %p offset %d into buffer %p.\n", length, Array, start, buffer);
}

void readIntsFromArray(w_instance Array, w_int start, w_int length, w_int*buffer) {
  w_int*  source = instance2Array_int(Array) + start;

  w_memcpy(buffer,source,length*sizeof(w_int));
  woempa(1, "(JNI) Read %d ints from Array %p offset %d into buffer %p.\n", length, Array, start, buffer);
}

void readLongsFromArray(w_instance Array, w_int start, w_int length, w_long *buffer) {
  w_long*  source = instance2Array_long(Array) + start;

  w_memcpy(buffer,source,length*sizeof(w_long));
  woempa(1, "(JNI) Read %d longs from Array %p offset %d into buffer %p.\n", length, Array, start, buffer);
}

void readFloatsFromArray(w_instance Array, w_int start, w_int length, w_float *buffer) {
  w_float*  source = instance2Array_float(Array) + start;

  w_memcpy(buffer,source,length*sizeof(w_float));
  woempa(1, "(JNI) Read %d floats from Array %p offset %d into buffer %p.\n", length, Array, start, buffer);
}

void readDoublesFromArray(w_instance Array, w_int start, w_int length, w_double *buffer) {
  w_double*  source = instance2Array_double(Array) + start;

  w_memcpy(buffer,source,length*sizeof(w_double));
  woempa(1, "(JNI) Read %d doubles from Array %p offset %d into buffer %p.\n", length, Array, start, buffer);
}

/*
** Convert an array of jvalues into items, starting from a method descriptor.
*/

static void jvalues2items(w_method method, jvalue arguments[], w_frame frame) {

  w_clazz arg_clazz;
  w_int i;

  if (method->spec.arg_types) {
    for (i = 0; method->spec.arg_types[i]; i++) {
      if (mustBeLoaded(&method->spec.arg_types[i]) != CLASS_LOADING_FAILED) {
        arg_clazz = method->spec.arg_types[i];
        if (clazzIsPrimitive(arg_clazz)) {
          frame->jstack_top[0].s = stack_notrace;
          switch (arg_clazz->type & 0x0f) {
            case VM_TYPE_BOOLEAN: frame->jstack_top[0].c = arguments[i].z; break;
            case VM_TYPE_CHAR:    frame->jstack_top[0].c = arguments[i].c; break;
            case VM_TYPE_FLOAT:   frame->jstack_top[0].c = arguments[i].f; break;
            case VM_TYPE_BYTE:    frame->jstack_top[0].c = arguments[i].b; break;
            case VM_TYPE_SHORT:   frame->jstack_top[0].c = arguments[i].s; break;
            case VM_TYPE_INT:     frame->jstack_top[0].c = arguments[i].i; break;

            case VM_TYPE_LONG: 
              frame->jstack_top[0].c = ((w_word*)&arguments[i])[0];
              frame->jstack_top[1].c = ((w_word*)&arguments[i])[1];
              frame->jstack_top[1].s = stack_notrace;
              frame->jstack_top += 1;
              break;

            case VM_TYPE_DOUBLE:
              frame->jstack_top[0].c = ((w_word*)&arguments[i])[0];
              frame->jstack_top[1].c = ((w_word*)&arguments[i])[1];
              frame->jstack_top[1].s = stack_notrace;
              frame->jstack_top += 1;
              break;

            default: 
              wabort(ABORT_WONKA, "Incorrect primitive type 0x%02x.\n", arg_clazz->type);        
          }
        }
        else {
          frame->jstack_top[0].c = arguments[i].i;
          frame->jstack_top[0].s = stack_trace;
        }
        frame->jstack_top += 1;
      }
    }
  }

}

/*
** Convert a va_list into items, starting from a method descriptor.
*/

static void va_list2items(w_method method, va_list list, w_frame frame) {

  w_clazz arg_clazz;
  w_int i;

  if (method->spec.arg_types) {
    for (i = 0; method->spec.arg_types[i]; i++) {
      if (mustBeLoaded(&method->spec.arg_types[i]) != CLASS_LOADING_FAILED) {
        frame->jstack_top[0].s = stack_notrace;
        arg_clazz = method->spec.arg_types[i];
        if (clazzIsPrimitive(arg_clazz)) {
          switch (arg_clazz->type & 0x0f) {
            case VM_TYPE_BOOLEAN:
            case VM_TYPE_CHAR:
            case VM_TYPE_BYTE:
            case VM_TYPE_SHORT:
            case VM_TYPE_INT:
              frame->jstack_top[0].c = va_arg(list, w_word);
              break;
  
            case VM_TYPE_FLOAT:
              frame->jstack_top[0].c = va_arg(list, wfp_float32);
              break;
            
            case VM_TYPE_LONG:
	      {
                union { w_long l; w_word w[2]; } arg;
                arg.l = va_arg(list, w_long);
                frame->jstack_top[0].c = arg.w[0];
                frame->jstack_top[1].c = arg.w[1];
                frame->jstack_top[1].s = stack_notrace;
                frame->jstack_top += 1;
                break;
	      }
            case VM_TYPE_DOUBLE:
              {
                union { wfp_float64 d; w_word w[2]; } arg;
                arg.d = va_arg(list, wfp_float64);
		/*
#ifdef ARM
                frame->jstack_top[1].c = arg.w[0];
                frame->jstack_top[0].c = arg.w[1];
#else
*/
                frame->jstack_top[0].c = arg.w[0];
                frame->jstack_top[1].c = arg.w[1];
		/*
#endif
*/
                frame->jstack_top[1].s = stack_notrace;
                frame->jstack_top += 1;
              }
              break;
            default: wabort(ABORT_WONKA, "Incorrect primitive type 0x%02x.\n", arg_clazz->type);        
          }
        }
        else {
          frame->jstack_top[0].c = (w_word) va_arg(list, jobject);
          frame->jstack_top[0].s = stack_trace;
        }
        frame->jstack_top += 1;
      }
      else {
      }
    }
  }
}

/*
** Generic calls for the virtual method invocations.
*/

// static jvalue run_32_method(w_thread thread, w_frame frame, w_method method) {
static w_word run_32_method(w_thread thread, w_frame frame, w_method method) {

//  jvalue result;
  w_word result = 0;
  w_instance protected = NULL;

  callMethod(frame, method);
  if (! exceptionThrown(thread)) {
//    result.i = frame->top[-1].c;
    result = frame->jstack_top[-1].c;
    if (isNotSet(method->spec.return_type->flags, CLAZZ_IS_PRIMITIVE)) {
//      protected = result.l;
      protected = (w_instance)result;
    }
  }

  deactivateFrame(frame, protected);
  
  return result;

}

// static jvalue run_64_method(w_thread thread, w_frame frame, w_method method) {
static w_dword run_64_method(w_thread thread, w_frame frame, w_method method) {

  union { w_dword dw; w_word w[2]; } result;

  woempa(1, "running 64_method %M for thread '%t'\n", frame->method, thread);
  callMethod(frame, method);
  if (! exceptionThrown(thread)) {
    result.w[0] = frame->jstack_top[-2].c;
    result.w[1] = frame->jstack_top[-1].c;
  }
  woempa(1, "finished running 64_method %M for thread '%t'\n", frame->method, thread);

  deactivateFrame(frame, NULL);

  return result.dw;

}

jint GetVersion(JNIEnv *env) {
  return JNI_VERSION_1_2;
}

jclass DefineClass(JNIEnv *env, const char *name, jobject loader, const jbyte *buf, jsize buflen) {

  w_thread  thread = JNIEnv2w_thread(env);
  w_clazz   clazz;
  w_instance theClass;
  w_BAR bar;

  woempa(1,"installing %s from %p using %j (thread %w)\n",name,buf,loader,NM(thread));

  bar.buffer = (w_byte*)buf;
  bar.length = buflen;
  bar.current = 0;

  enterMonitor(loader);

// [CG 20010322] not passing the name given, as I can't find a consistent
// description of what format it used (slashes or dots, etc.)
  clazz = createClazz(thread, NULL, &bar, loader, FALSE);

  exitMonitor(loader);

  if (exceptionThrown(thread)) {
    return NULL;
}

  theClass = clazz2Class(clazz);
  setReferenceField(theClass, loader, F_Class_loader);

  return theClass;

}

void ExceptionClear(JNIEnv* env) {

  w_thread thread = JNIEnv2w_thread(env);

  clearException(thread);

  if (thread->Thread) {
    setReferenceField(thread->Thread, NULL, F_Thread_thrown);
  }

}

void ExceptionDescribe(JNIEnv* env) {

  w_thread thread = JNIEnv2w_thread(env);
  w_instance throwable;
  w_instance Message;

  if (thread->Thread && (throwable = getReferenceField(thread->Thread, F_Thread_thrown))) {
    Message = getReferenceField(throwable, F_Throwable_detailMessage);
    woempa(9, "Thread '%w' has thrown an instance of '%k'.\n", NM(thread), instance2clazz(throwable));
    if (Message) {
       woempa(9, "Throwable message is '%w'.\n", getWotsitField(Message, F_String_wotsit));
    }
    // Do we need to clear the exception ?
    setReferenceField(thread->Thread, NULL, F_Thread_thrown);
    clearException(thread);
  }

}

jboolean ExceptionCheck(JNIEnv *env) {
  w_thread thread = JNIEnv2w_thread(env);

  return (thread->Thread && getReferenceField(thread->Thread, F_Thread_thrown));
  
}

jthrowable ExceptionOccurred(JNIEnv *env) {

  return exceptionThrown(JNIEnv2w_thread(env));
  
}

void FatalError(JNIEnv *env, const char *message) {
  wabort(ABORT_WONKA, "FatalError: %s\n",message);
}


    
jclass FindClass(JNIEnv *env, const char *Name) {

  w_thread thread = JNIEnv2w_thread(env);
  w_string name = cstring2String(Name, strlen(Name));
  w_method caller = thread->top->method;
  w_string dotified;
  w_instance loader;
  w_clazz clazz;
  jclass result;
  w_instance exception;

  if (!name) {
    wabort(ABORT_WONKA, "Unable to create name\n");
  }

  if (caller && caller == get_loadLibrary0_jmethodID(env)) {
    // This looks like a lot of "previous->", but we need to skip 2 frames
    // for the JNI call to loadLibrary0 plus the frame of loadLibrary ... 
    caller = thread->top->previous->previous->previous->method;
    woempa(1, "(JNI) Searching for class '%s'; caller is Runtime.loadLibrary0() so using previous frame %M\n", Name, caller);
  }

  if (caller && caller != get_underscore_jmethodID(env)) {
    woempa(1, "(JNI) Searching for class '%s', called from %M\n", Name, caller);
    loader = clazz2loader(caller->spec.declaring_clazz);
    woempa(1, "(JNI) Using class loader %j of %m.\n", loader, caller);
  }
  else {
    loader = applicationClassLoader ? applicationClassLoader : systemClassLoader;
    woempa(1, "(JNI) Searching for class '%s', thread %t has no stack frame so using %j\n", Name, thread, loader);
  }

  dotified = undescriptifyClassName(name);
  if (!dotified) {
    wabort(ABORT_WONKA, "Unable to undescriptify class name\n");
  }
  clazz = namedClassMustBeLoaded(loader, dotified);
  deregisterString(dotified);

  exception = exceptionThrown(thread);
  if (exception) {
    woempa(1, "(JNI) exception %j\n", exceptionThrown(thread));
    if(isAssignmentCompatible(instance2object(exception)->clazz, clazzException)) {
      wrapException(thread,clazzNoClassDefFoundError, F_Throwable_cause);
    }

    return NULL;
  }

  mustBeLinked(clazz);

  exception = exceptionThrown(thread);
  if (exception) {
    if(isAssignmentCompatible(instance2object(exception)->clazz, clazzException)) {
      wrapException(thread,clazzNoClassDefFoundError, F_Throwable_cause);
    }

    result = NULL;
  }
  else {
    result = clazz2Class(clazz);
  }
  
  woempa(1, "(JNI) result %j\n", result);
  
  deregisterString(name);
  
  return result;

}

jclass GetSuperclass(JNIEnv *env, jclass class) {
  return clazz2Class(getSuper(Class2clazz(class)));
}

jboolean IsAssignableFrom(JNIEnv *env, jclass class1, jclass class2) {
  return isAssignmentCompatible(Class2clazz(class1), Class2clazz(class2));
}

jint Throw(JNIEnv *env, jthrowable Throwable) {

  throwExceptionInstance(JNIEnv2w_thread(env), Throwable);
  
  return 0;

}

jint ThrowNew(JNIEnv *env, jclass class, const char *message) {

  w_thread thread = JNIEnv2w_thread(env);
  w_clazz  clazz  = Class2clazz(class);
  w_string string = cstring2String(message, strlen(message));
  w_instance Throwable;
  w_instance Message;

  if (!string) {
    wabort(ABORT_WONKA, "Unable to create string\n");
  }
  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {
    wabort(ABORT_WONKA, "Unable to initalize %k\n", clazz);
  }
  enterUnsafeRegion(thread);
  Throwable = allocInstance_initialized(thread, clazz);
  if (!Throwable) {
    wabort(ABORT_WONKA, "Unable to create Throwable\n");
  }
  Message = newStringInstance(string);
  enterSafeRegion(thread);
  if (Message) {
    setReferenceField(Throwable, Message, F_Throwable_detailMessage);
    throwExceptionInstance(thread, Throwable);
    removeLocalReference(thread, Message);
  }

  return 0;

}


jobject NewGlobalRef(JNIEnv *env, jobject obj) {
  w_thread thread = JNIEnv2w_thread(env);
  w_boolean unsafe = enterUnsafeRegion(thread);

  newGlobalReference(obj);
  if (!unsafe) {
    enterSafeRegion(thread);
  }
  return obj;
}

jobject NewLocalRef(JNIEnv *env, jobject obj) {
  w_thread thread = JNIEnv2w_thread(env);

  woempa(1, "creating new local reference to instance %p of %k\n", obj, instance2clazz((w_instance)obj));
  addLocalReference(thread, obj);

  return obj;

}

void DeleteGlobalRef(JNIEnv *env, jobject obj) {

  /*
  ** Decrement the reference count, if the new reference count returned is 0, we erase
  ** the entry from the hashtable.
  */

  woempa(1, "deleting global reference to instance %p\n", obj);
  deleteGlobalReference(obj);

}

void DeleteLocalRef(JNIEnv *env, jobject instance) {
  w_thread thread = JNIEnv2w_thread(env);

  woempa(1, "deleting local reference to instance %p of %k\n", instance, instance2clazz((w_instance)instance));
  removeLocalReference(thread, instance);

}

jboolean IsSameObject(JNIEnv *env, jobject ref1, jobject ref2) {
  return ref1 == ref2;
}

jobject AllocObject(JNIEnv *env, jclass class) {

  w_thread thread = JNIEnv2w_thread(env);
  w_clazz  clazz  = Class2clazz(class);
  w_instance new = NULL;

  if (mustBeInitialized(clazz) != CLASS_LOADING_FAILED) {
    enterUnsafeRegion(thread);
    new = allocInstance_initialized(thread, clazz);
    enterSafeRegion(thread);
  }

  return new;
  
}

jobject NewObject(JNIEnv *env, jclass class, jmethodID methodID, ...) {

  w_thread thread = JNIEnv2w_thread(env);
  w_clazz  clazz  = Class2clazz(class);
  w_instance new;
  w_frame frame;
  va_list list;

  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {
    return NULL;
  }
  
  enterUnsafeRegion(thread);
  new = allocInstance_initialized(thread, clazz);
  enterSafeRegion(thread);

  if (new) {
    frame = pushFrame(thread, methodID);
    frame->flags |= FRAME_JNI;
    frame->jstack_top[0].c = (w_word) new;
    frame->jstack_top[0].s = stack_trace;
    frame->jstack_top += 1;
    va_start(list, methodID);
    va_list2items(methodID, list, frame);
    va_end(list);
    if (exceptionThrown(thread)) {
      new = NULL;
    }
    else {
      callMethod(frame, methodID);
    }
    deactivateFrame(frame, NULL);
  }

  return new;

}

jobject NewObjectV(JNIEnv *env, jclass class, jmethodID methodID, va_list args) {

  w_thread thread = JNIEnv2w_thread(env);
  w_clazz  clazz  = Class2clazz(class);
  w_instance new;
  w_frame frame;

  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {
    return NULL;
  }
  enterUnsafeRegion(thread);
  new = allocInstance_initialized(thread, clazz);
  enterSafeRegion(thread);

  if (new) {
    frame = pushFrame(thread, methodID);
    frame->flags |= FRAME_JNI;
    frame->jstack_top[0].c = (w_word) new;
    frame->jstack_top[0].s = stack_trace;
    frame->jstack_top += 1;
    va_list2items(methodID, args, frame);
    if (exceptionThrown(thread)) {
      new = NULL;
    }
    else {
      callMethod(frame, methodID);
    }
    deactivateFrame(frame, NULL);
  }

  return new;

}

jobject NewObjectA(JNIEnv *env, jclass class, jmethodID methodID, jvalue *args) {

  w_thread thread = JNIEnv2w_thread(env);
  w_clazz  clazz  = Class2clazz(class);
  w_instance new;
  w_frame frame;

  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {
    return NULL;
  }

  enterUnsafeRegion(thread);
  new = allocInstance_initialized(thread, clazz);
  enterSafeRegion(thread);

  if (new) {
    frame = pushFrame(thread, methodID);
    frame->flags |= FRAME_JNI;
    frame->jstack_top[0].c = (w_word) new;
    frame->jstack_top[0].s = stack_trace;
    frame->jstack_top += 1;
    jvalues2items(methodID, args, frame);
    if (exceptionThrown(thread)) {
      new = NULL;
    }
    else {
      callMethod(frame, methodID);
    }
    deactivateFrame(frame, NULL);
  }

  return new;

}

jboolean IsInstanceOf(JNIEnv *env, jobject obj, jclass class) {
///By definition, a null object is instance of every class, and therefore always should return true
  if(!obj) {
    return JNI_TRUE;
  }
  else {
    return isAssignmentCompatible(instance2clazz(obj), Class2clazz(class));
  }
}

jclass GetObjectClass(JNIEnv *env, jobject obj) {

  return clazz2Class(instance2clazz(obj));

}

/*
** Search for a method with a certain template in a certain clazz, based on a clazz.
** The template is in fact a method but we have to resolve the correct method ourselves.
** It may well be the method that was passed as a template.
*/

w_method resolveMethodForCall(w_clazz clazz, w_method template) {
  return methodIsInterface(template) ? interfaceLookup(template, clazz) : virtualLookup(template, clazz);
}

/*
** => Calling java class functions froom within JNI c-code
*/

// jvalue CallMethod32(JNIEnv *env, jobject instance, w_method method, va_list args) {
w_word CallMethod32(JNIEnv *env, jobject instance, w_method method, va_list args) {

  w_thread thread = JNIEnv2w_thread(env);
  w_frame frame;

  frame = pushFrame(thread, method);
  frame->flags |= FRAME_JNI;

  frame->jstack_top[0].c = (w_word) instance;
  frame->jstack_top[0].s = stack_trace;
  frame->jstack_top += 1;

  va_list2items(method, args, frame);

  return run_32_method(thread, frame, method);

}

// jvalue CallMethod32A(JNIEnv *env, jobject instance, w_method method, jvalue *args) {
w_word CallMethod32A(JNIEnv *env, jobject instance, w_method method, jvalue *args) {

  w_thread thread = JNIEnv2w_thread(env);

  w_frame frame = pushFrame(thread, method);
  frame->flags |= FRAME_JNI;

  frame->jstack_top[0].c = (w_word) instance;
  frame->jstack_top[0].s = stack_trace;
  frame->jstack_top += 1;

  jvalues2items(method, args, frame);
  
  return run_32_method(thread, frame, method);

}

// jvalue CallMethod64(JNIEnv *env, jobject instance, w_method method, va_list args) {
w_dword CallMethod64(JNIEnv *env, jobject instance, w_method method, va_list args) {

  w_thread thread = JNIEnv2w_thread(env);

  w_frame frame = pushFrame(thread, method);

  frame->flags |= FRAME_JNI;
  frame->jstack_top[0].c = (w_word) instance;
  frame->jstack_top[0].s = stack_trace;
  frame->jstack_top += 1;

  va_list2items(method, args, frame);
  
  return run_64_method(thread, frame, method);

}

//jvalue CallMethod64A(JNIEnv *env, jobject instance, w_method method, jvalue *args) {
w_dword CallMethod64A(JNIEnv *env, jobject instance, w_method method, jvalue *args) {

  w_thread thread = JNIEnv2w_thread(env);
  w_frame frame = pushFrame(thread, method);

  frame->flags |= FRAME_JNI;
  frame->jstack_top[0].c = (w_word) instance;
  frame->jstack_top[0].s = stack_trace;
  frame->jstack_top += 1;

  jvalues2items(method, args, frame);
  
  return run_64_method(thread, frame, method);

}

w_void CallMethodVoid(JNIEnv *env, jobject instance, w_method method, va_list args) {

  w_thread thread = JNIEnv2w_thread(env);
  w_frame frame = pushFrame(thread, method);
  frame->flags |= FRAME_JNI;

  frame->jstack_top[0].c = (w_word) instance;
  frame->jstack_top[0].s = stack_trace;
  frame->jstack_top += 1;

  va_list2items(method, args, frame);
  if (!exceptionThrown(thread)) {
    callMethod(frame, method);
  }
  deactivateFrame(frame, NULL);
  
}

w_void CallMethodVoidA(JNIEnv *env, jobject instance, w_method method, jvalue *args) {

  w_thread thread = JNIEnv2w_thread(env);
  w_frame frame = pushFrame(thread, method);
  frame->flags |= FRAME_JNI;

  frame->jstack_top[0].c = (w_word) instance;
  frame->jstack_top[0].s = stack_trace;
  frame->jstack_top += 1;

  jvalues2items(method, args, frame);
  if (!exceptionThrown(thread)) {
    callMethod(frame, method);
  }
  deactivateFrame(frame, NULL);

}

/*
** => object function calls
*/

jobject CallObjectMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {

  va_list  list;
  jobject  result = NULL;
  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);
  
  va_start(list, methodID);
  result = (w_instance)CallMethod32(env, obj, method, list);
  va_end(list);
  
  return result;

}

jobject CallObjectMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {

  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);

  return (w_instance)CallMethod32(env, obj, method, args);

}

jobject CallObjectMethodA(JNIEnv *env, jobject obj, jmethodID methodID, jvalue *args) {

  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);

  return (w_instance)CallMethod32A(env, obj, method, args);

}

/*
** => boolean function calls
*/

jboolean CallBooleanMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {

  va_list  list;
  jboolean result;
  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);
  
  va_start(list, methodID);
  result = (jboolean)CallMethod32(env, obj, method, list);
  va_end(list);
  
  return result;

}

jboolean CallBooleanMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {

  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);

  return (jboolean)CallMethod32(env, obj, method, args); 

}

jboolean CallBooleanMethodA(JNIEnv *env, jobject obj, jmethodID methodID, jvalue *args) {

  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);

  return (jboolean)CallMethod32A(env, obj, method, args); 

}

/*
** => byte function calls
*/

jbyte CallByteMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {

  va_list  list;
  jbyte    result = 0;
  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);
  
  va_start(list, methodID);
  result = (jbyte)CallMethod32(env, obj, method, list);
  va_end(list);
  
  return result;

}

jbyte CallByteMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {

  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);

  return (jbyte)CallMethod32(env, obj, method, args); 

}

jbyte CallByteMethodA(JNIEnv *env, jobject obj, jmethodID methodID, jvalue *args) {

  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);

  return (jbyte)CallMethod32A(env, obj, method, args); 

}

/*
** => char function calls
*/

jchar CallCharMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {

  va_list  list;
  jchar    result = 0;
  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);
  
  va_start(list, methodID);
  result = (jchar)CallMethod32(env, obj, method, list);
  va_end(list);
  
  return result;

}

jchar CallCharMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {

  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);

  return (jchar)CallMethod32(env, obj, method, args); 

}

jchar CallCharMethodA(JNIEnv *env, jobject obj, jmethodID methodID, jvalue *args) {

  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);

  return (jchar)CallMethod32A(env, obj, method, args); 

}

/*
** => short function calls
*/

jshort CallShortMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {

  va_list  list;
  jshort   result = 0;
  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);
  
  va_start(list, methodID);
  result = (jshort)CallMethod32(env, obj, method, list);
  va_end(list);
  
  return result;

}

jshort CallShortMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {

  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);

  return (jshort)CallMethod32(env, obj, method, args); 

}

jshort CallShortMethodA(JNIEnv *env, jobject obj, jmethodID methodID, jvalue *args) {

  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);

  return (jshort)CallMethod32A(env, obj, method, args); 

}

/*
** => int function calls
*/

jint CallIntMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {

  va_list  list;
  jint     result = 0;
  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);
  
  va_start(list, methodID);
  result = (jint)CallMethod32(env, obj, method, list);
  va_end(list);
  
  return result;

}

jint CallIntMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {

  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);

  return (jint)CallMethod32(env, obj, method, args); 

}

jint CallIntMethodA(JNIEnv *env, jobject obj, jmethodID methodID, jvalue *args) {

  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);

  return (jint)CallMethod32A(env, obj, method, args); 

}

/*
** => long function calls
*/

jlong CallLongMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {

  va_list  list;
  jlong    result = 0;
  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);
  
  va_start(list, methodID);
  result = (jlong)CallMethod64(env, obj, method, list);
  va_end(list);
  
  return result;

}

jlong CallLongMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {

  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);
  return (jlong)CallMethod64(env, obj, method, args); 

}

jlong CallLongMethodA(JNIEnv *env, jobject obj, jmethodID methodID, jvalue *args) {

  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);

  return (jlong)CallMethod64A(env, obj, method, args); 

}

/*
** => float function calls
*/

jfloat CallFloatMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {

  va_list  list;
  jfloat   result = 0;
  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);
  
  va_start(list, methodID);
  result = (jfloat)CallMethod32(env, obj, method, list);
  va_end(list);
  
  return result;

}

jfloat CallFloatMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {

  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);

  return (jfloat)CallMethod32(env, obj, method, args); 

}

jfloat CallFloatMethodA(JNIEnv *env, jobject obj, jmethodID methodID, jvalue *args) {

  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);

  return (jfloat)CallMethod32A(env, obj, method, args); 

}

/*
** => double function calls
*/

jdouble CallDoubleMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {

  va_list  list;
  jdouble    result = 0;
  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);
  
  va_start(list, methodID);
  result = (jdouble)CallMethod64(env, obj, method, list);
  va_end(list);
  
  return result;

}

jdouble CallDoubleMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {

  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);

  return (jdouble)CallMethod64(env, obj, method, args); 

}

jdouble CallDoubleMethodA(JNIEnv *env, jobject obj, jmethodID methodID, jvalue *args) {

  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);

  return (jdouble)CallMethod64A(env, obj, method, args); 

}

/*
** => void function calls
*/

w_void CallVoidMethod(JNIEnv *env, jobject obj, jmethodID methodID, ...) {

  va_list  list;
  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);
  
  va_start(list, methodID);
  CallMethodVoid(env, obj, method, list);
  va_end(list);

}

w_void CallVoidMethodV(JNIEnv *env, jobject obj, jmethodID methodID, va_list args) {

  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);

  CallMethodVoid(env, obj, method, args); 

}

w_void CallVoidMethodA(JNIEnv *env, jobject obj, jmethodID methodID, jvalue *args) {

  w_method method = resolveMethodForCall(instance2clazz((w_instance)obj), methodID);

  CallMethodVoidA(env, obj, method, args); 

}

/*
** =>  nonvirtual object function calls
*/

jobject CallNonvirtualObjectMethod(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, ...) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);
  va_list  list;
  jobject  result = NULL;
  
  va_start(list, methodID);
  result = (jobject)CallMethod32(env, obj, method, list);
  va_end(list);
  
  return result;

}

jobject CallNonvirtualObjectMethodV(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, va_list args) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);

  return (jobject)CallMethod32(env, obj, method, args); 

}

jobject CallNonvirtualObjectMethodA(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, jvalue *args) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);

  return (jobject)CallMethod32A(env, obj, method, args);

}

/*
** =>  nonvirtual boolean function calls
*/

jboolean CallNonvirtualBooleanMethod(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, ...) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);
  va_list  list;
  jboolean result = 0;
  
  va_start(list, methodID);
  result = (jboolean)CallMethod32(env, obj, method, list);
  va_end(list);
  
  return result;

}

jboolean CallNonvirtualBooleanMethodV(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, va_list args) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);

  return (jboolean)CallMethod32(env, obj, method, args); 

}

jboolean CallNonvirtualBooleanMethodA(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, jvalue *args) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);

  return (jboolean)CallMethod32A(env, obj, method, args); 

}

/*
** =>  nonvirtual byte function calls
*/

jbyte CallNonvirtualByteMethod(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, ...) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);
  va_list  list;
  jbyte    result = 0;
  
  va_start(list, methodID);
  result = (jbyte)CallMethod32(env, obj, method, list);
  va_end(list);
  
  return result;

}

jbyte CallNonvirtualByteMethodV(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, va_list args) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);

  return (jbyte)CallMethod32(env, obj, method, args); 

}

jbyte CallNonvirtualByteMethodA(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, jvalue *args) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);

  return (jbyte)CallMethod32A(env, obj, method, args); 

}

/*
** =>  nonvirtual char function calls
*/

jchar CallNonvirtualCharMethod(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, ...) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);
  va_list  list;
  jchar    result = 0;
  
  va_start(list, methodID);
  result = (jchar)CallMethod32(env, obj, method, list);
  va_end(list);
  
  return result;

}

jchar CallNonvirtualCharMethodV(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, va_list args) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);

  return (jchar)CallMethod32(env, obj, method, args); 

}

jchar CallNonvirtualCharMethodA(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, jvalue *args) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);

  return (jchar)CallMethod32A(env, obj, method, args); 

}

/*
** =>  nonvirtual short function calls
*/

jshort CallNonvirtualShortMethod(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, ...) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);
  va_list  list;
  jshort   result = 0;
  
  va_start(list, methodID);
  result = (jshort)CallMethod32(env, obj, method, list);
  va_end(list);
  
  return result;

}

jshort CallNonvirtualShortMethodV(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, va_list args) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);

  return (jshort)CallMethod32(env, obj, method, args); 

}

jshort CallNonvirtualShortMethodA(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, jvalue *args) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);

  return (jshort)CallMethod32A(env, obj, method, args); 

}

/*
** =>  nonvirtual int function calls
*/

jint CallNonvirtualIntMethod(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, ...) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);
  va_list  list;
  jint    result = 0;
  
  va_start(list, methodID);
  result = (jint)CallMethod32(env, obj, method, list);
  va_end(list);
  
  return result;

}

jint CallNonvirtualIntMethodV(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, va_list args) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);

  return (jint)CallMethod32(env, obj, method, args); 

}

jint CallNonvirtualIntMethodA(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, jvalue *args) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);

  return (jint)CallMethod32A(env, obj, method, args); 

}

/*
** =>  nonvirtual long function calls
*/

jlong CallNonvirtualLongMethod(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, ...) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);
  va_list  list;
  jlong    result = 0;
  
  va_start(list, methodID);
  result = (jlong)CallMethod64(env, obj, method, list);
  va_end(list);
  
  return result;

}

jlong CallNonvirtualLongMethodV(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, va_list args) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);

  return (jlong)CallMethod64(env, obj, method, args); 

}

jlong CallNonvirtualLongMethodA(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, jvalue *args) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);

  return (jlong)CallMethod64A(env, obj, method, args); 

}

/*
** =>  nonvirtual float function calls
*/

jfloat CallNonvirtualFloatMethod(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, ...) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);
  va_list  list;
  jfloat   result = 0;
  
  va_start(list, methodID);
  result = (jfloat)CallMethod32(env, obj, method, list);
  va_end(list);
  
  return result;

}

jfloat CallNonvirtualFloatMethodV(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, va_list args) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);

  return (jfloat)CallMethod32(env, obj, method, args); 

}

jfloat CallNonvirtualFloatMethodA(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, jvalue *args) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);

  return (jfloat)CallMethod32A(env, obj, method, args); 

}

/*
** =>  nonvirtual double function calls
*/

jdouble CallNonvirtualDoubleMethod(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, ...) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);
  va_list  list;
  jlong    result = 0;
  
  va_start(list, methodID);
  result = (jdouble)CallMethod64(env, obj, method, list);
  va_end(list);
  
  return result;

}

jdouble CallNonvirtualDoubleMethodV(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, va_list args) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);

  return (jdouble)CallMethod64(env, obj, method, args); 

}

jdouble CallNonvirtualDoubleMethodA(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, jvalue *args) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);

  return (jdouble)CallMethod64A(env, obj, method, args); 

}

/*
** =>  nonvirtual void function calls
*/

w_void CallNonvirtualVoidMethod(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, ...) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);
  va_list  list;
  
  va_start(list, methodID);
  CallMethodVoid(env, obj, method, list);
  va_end(list);

}

w_void CallNonvirtualVoidMethodV(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, va_list args) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);

  CallMethodVoid(env, obj, method, args); 

}

w_void CallNonvirtualVoidMethodA(JNIEnv *env, jobject obj, jclass class, jmethodID methodID, jvalue *args) {

  w_method method = resolveMethodForCall(Class2clazz(class), methodID);

  CallMethodVoidA(env, obj, method, args); 

}

/*
** =>  member variable access
*/

/*
** =>  retrieve the member variable field ID
*/

jfieldID GetFieldID(JNIEnv *env, jclass class, const char *name, const char *sig) {

  w_string name_string = cstring2String((char *)name, strlen(name));
  w_string desc_string = cstring2String((char *)sig, strlen(sig));
  w_field result;

  if (!name_string || !desc_string) {
    wabort(ABORT_WONKA, "Unable to create name and desc string\n");
  }
  //w_dump("(JNI) name='%s', signature='%s' in %k\n",name,sig,Class2clazz(class));
    
  /*
  ** Search for it, match only instance fields, but match any of the 
  ** "public" or "private" field modifiers.
  */
  
  result = searchClazzHierarchyForField(Class2clazz(class), name_string, desc_string, MATCH_INSTANCE_FIELD, MATCH_ANY);

  deregisterString(desc_string);
  deregisterString(name_string);
  
  return result;
}

/*
** =>  Get the value of a variable, given the variables field ID
*/

#ifdef PACK_BYTE_FIELDS
#define GetGeneric1Field GetGeneric8Field

JNITypes GetGeneric8Field(JNIEnv *env, jobject obj, jfieldID fieldID) {
  w_thread thread = JNIEnv2w_thread(env);
  w_clazz clazz = instance2clazz((w_instance)obj);

  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {
    JNITypes j;
    return j;
  }
  return (JNITypes)(jbyte)*byteFieldPointer(((w_instance)obj), FIELD_OFFSET(fieldID->size_and_slot));
}

#define GetGeneric16Field GetGeneric32Field
#else
#define GetGeneric1Field GetGeneric32Field
#define GetGeneric8Field GetGeneric32Field
#define GetGeneric16Field GetGeneric32Field
#endif

JNITypes GetGeneric32Field(JNIEnv *env, jobject obj, jfieldID fieldID) {
  w_clazz clazz = instance2clazz((w_instance)obj);

  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {
    JNITypes j;
    return j;
  }
  return (JNITypes)((w_instance)obj)[FIELD_OFFSET(fieldID->size_and_slot)];
}

JNITypes GetGeneric64Field(JNIEnv *env, jobject obj, jfieldID fieldID) {
  w_clazz clazz = instance2clazz((w_instance)obj);
  w_long result;

  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {
    JNITypes j;

    return j;

  }
  result =  ((w_instance)obj)[FIELD_OFFSET(fieldID->size_and_slot) + 1];  /* MSW */
  result <<= 32;
  result |= ((w_instance)obj)[FIELD_OFFSET(fieldID->size_and_slot)];  /* LSW */
  return (JNITypes)result;
}

jobject GetObjectField(JNIEnv *env, jobject obj, jfieldID fieldID) {
  w_clazz clazz = instance2clazz((w_instance)obj);
  jobject obje;

  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {
    return NULL;
  }
  obje = (jobject)((w_instance)obj)[instance2clazz((w_instance)obj)->instanceSize + fieldID->size_and_slot];
  //w_dump("No Exception thrown, returnig %p from %p (%k) %d\n",obje, obj,instance2clazz((w_instance)obj),fieldID->size_and_slot);
  return obje;
}

jboolean GetBooleanField(JNIEnv *env, jobject obj, jfieldID fieldID) {
  return (GetGeneric1Field(env, obj, fieldID)).z;
}

jbyte GetByteField(JNIEnv *env, jobject obj, jfieldID fieldID) {
  return (GetGeneric8Field(env, obj, fieldID)).b;
}

jchar GetCharField(JNIEnv *env, jobject obj, jfieldID fieldID) {
  return (GetGeneric16Field(env, obj, fieldID)).c;
}

jshort GetShortField(JNIEnv *env, jobject obj, jfieldID fieldID) {
  return (GetGeneric16Field(env, obj, fieldID)).s;
}

jint GetIntField(JNIEnv *env, jobject obj, jfieldID fieldID) {
  return (GetGeneric32Field(env, obj, fieldID)).i;
}

jlong GetLongField(JNIEnv *env, jobject obj, jfieldID fieldID) {
  return (GetGeneric64Field(env, obj, fieldID)).j;
}

jfloat GetFloatField(JNIEnv *env, jobject obj, jfieldID fieldID) {
  return (GetGeneric32Field(env, obj, fieldID)).f;
}

jdouble GetDoubleField(JNIEnv *env, jobject obj, jfieldID fieldID) {
  return (GetGeneric64Field(env, obj, fieldID)).d;
}

/*
** =>  set the a variable to a certain value, given the variables field ID
*/

#ifdef PACK_BYTE_FIELDS
#define SetGeneric1Field SetGeneric8Field

w_void SetGeneric8Field(JNIEnv *env, jobject obj, jfieldID fieldID, w_word value) {

  w_clazz clazz = instance2clazz((w_instance)obj);

  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {

    return;

  }
  woempa(1,"set field[%d] of instance %p of %k to 0x%08x\n",FIELD_OFFSET(fieldID->size_and_slot),(w_instance)obj,instance2clazz((w_instance)obj),value);
  *byteFieldPointer(((w_instance)obj), FIELD_OFFSET(fieldID->size_and_slot)) = value;
}

#define SetGeneric16Field SetGeneric32Field
#else
#define SetGeneric1Field SetGeneric32Field
#define SetGeneric8Field SetGeneric32Field
#define SetGeneric16Field SetGeneric32Field
#endif

w_void SetGeneric32Field(JNIEnv *env, jobject obj, jfieldID fieldID, w_word value) {

  w_clazz clazz = instance2clazz((w_instance)obj);

  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {

    return;

  }
  woempa(1,"set field[%d] of instance %p of %k to 0x%08x\n",FIELD_OFFSET(fieldID->size_and_slot),(w_instance)obj,instance2clazz((w_instance)obj),value);
  ((w_instance)obj)[FIELD_OFFSET(fieldID->size_and_slot)] = value;
}

w_void SetGeneric64Field(JNIEnv *env, jobject obj, jfieldID fieldID, jlong value) {

  w_clazz clazz = instance2clazz((w_instance)obj);

  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {

    return;

  }
  
  woempa(1,"set field[%d-%d] of instance %p of %k to 0x%08x%08x\n", FIELD_OFFSET(fieldID->size_and_slot), FIELD_OFFSET(fieldID->size_and_slot) + 1,
          (w_instance)obj,instance2clazz((w_instance)obj),LSW_PART(value),MSW_PART(value));

  ((w_instance)obj)[FIELD_OFFSET(fieldID->size_and_slot)] = LSW_PART(value);
  ((w_instance)obj)[FIELD_OFFSET(fieldID->size_and_slot) + 1] = MSW_PART(value);
}

w_void SetObjectField(JNIEnv *env, jobject obj, jfieldID fieldID, jobject value) {

  w_instance instance = obj;
  w_instance wval = value;
  w_clazz clazz = instance2clazz(instance);

  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {

    return;

  }
  woempa(1,"set field[%d] of instance %p of %k to %p\n",fieldID->size_and_slot,instance,instance2clazz(instance),wval);

  setReferenceField(instance, wval, fieldID->size_and_slot);

}

w_void SetBooleanField(JNIEnv *env, jobject obj, jfieldID fieldID, jboolean value) {
  SetGeneric1Field(env, obj, fieldID, (w_word)value);
}

w_void SetByteField(JNIEnv *env, jobject obj, jfieldID fieldID, jbyte value) {
  SetGeneric8Field(env, obj, fieldID, (w_word)value);
}

w_void SetCharField(JNIEnv *env, jobject obj, jfieldID fieldID, jchar value) {
  SetGeneric16Field(env, obj, fieldID, (w_word)value);
}

w_void SetShortField(JNIEnv *env, jobject obj, jfieldID fieldID, jshort value) {
  SetGeneric16Field(env, obj, fieldID, (w_word)value);
}

w_void SetIntField(JNIEnv *env, jobject obj, jfieldID fieldID, jint value) {
  SetGeneric32Field(env, obj, fieldID, (w_word)value);
}

w_void SetLongField(JNIEnv *env, jobject obj, jfieldID fieldID, jlong value) {
  SetGeneric64Field(env, obj, fieldID, (jlong)value);
}

w_void SetFloatField(JNIEnv *env, jobject obj, jfieldID fieldID, jfloat value) {
  SetGeneric32Field(env, obj, fieldID, (w_word)value);
}

w_void SetDoubleField(JNIEnv *env, jobject obj, jfieldID fieldID, jdouble value) {
  SetGeneric64Field(env, obj, fieldID, (jlong)value);
}

/*
** =>  static  function calls
*/

/*
** =>  static function method ID retrieval
*/

jmethodID GetStaticMethodID(JNIEnv *env, jclass class, const char *utf8name, const char *utf8sig) {
  w_clazz  clazz = Class2clazz(class);
  w_clazz  super;
  w_size   i;
  w_size   j;
  w_method method = NULL;
  w_method candidate;
  w_string name_string;
  w_string desc_string;
  w_MethodSpec *spec;

  name_string = utf2String(utf8name, strlen(utf8name));
  desc_string = utf2String(utf8sig, strlen(utf8sig));
  if (!name_string || !desc_string) {
    wabort(ABORT_WONKA,"Unable to convert name and desc to w_string\n",clazz, name_string, desc_string);
  }
  if (createMethodSpecUsingDescriptor(clazz, name_string, desc_string, &spec) == CLASS_LOADING_FAILED) {
    wabort(ABORT_WONKA,"Uh oh: failed to build method spec using declaring_clazz %k, name %w, desc %w.\n",clazz, name_string, desc_string);
  }

  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {
    return NULL;

  }

  woempa(1,"Seeking  method %w%w in %K\n", name_string, desc_string, spec->declaring_clazz);
 
  for (i = 0; i < clazz->numDeclaredMethods; ++i) {
    candidate = &clazz->own_methods[i];

    woempa(1, "  candidate: %w %w\n", candidate->desc, candidate->desc);
    if ((candidate->spec.name == name_string) && (candidate->desc == desc_string)) {
      method = candidate;
      break;
    }
  }

  if (!method) {
    for (j = 0; j < clazz->numSuperClasses; ++j) {
      super = clazz->supers[j];
      woempa(1, "Seek %w in %K\n", spec->name, super);
      for (i = 0; i < super->numDeclaredMethods; ++i) {
        candidate = &super->own_methods[i];

        if (candidate->spec.name == name_string && candidate->desc == desc_string) {
          method = candidate;
          break;
        }
      }
    }
  }

  if (method) {
    if (!methodMatchesSpec(method, spec)) {
      woempa(9,"%M matches name %w and descriptor %w, but classes do not resolve correctly.\n", method, name_string, desc_string);
      method = NULL;
    }
    else if (isNotSet(method->flags, ACC_STATIC)) {
      woempa(9,"Was looking for a static method %w%w, but %M is not static\n", name_string, desc_string, method);
      method = NULL;
    }
    else {
      woempa(1,"Found  method %M\n", method);
    }
  }

  releaseMethodSpec(spec);
  releaseMem(spec);
  deregisterString(name_string);
  deregisterString(desc_string);

  return method;
}

jmethodID GetMethodID(JNIEnv *env, jclass class, const char *utf8name, const char *utf8sig) {
  w_clazz  clazz = Class2clazz(class);
  w_clazz  super;
  w_size   i;
  w_size   j;
  w_method method = NULL;
  w_method candidate;
  w_string name_string;
  w_string desc_string;
  w_MethodSpec *spec;

  name_string = utf2String(utf8name, strlen(utf8name));
  desc_string = utf2String(utf8sig, strlen(utf8sig));
  if (!name_string || !desc_string) {
    wabort(ABORT_WONKA,"Unable to convert name and desc to w_string\n",clazz, name_string, desc_string);
  }
  if (createMethodSpecUsingDescriptor(clazz, name_string, desc_string, &spec) == CLASS_LOADING_FAILED) {
    wabort(ABORT_WONKA,"Uh oh: failed to build method spec using declaring_clazz %k, name %w, desc %w.\n",clazz, name_string, desc_string);
  }

  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {
    return NULL;

  }

  woempa(1,"Seeking  method %w%w in %K\n", name_string, desc_string, spec->declaring_clazz);
 
  for (i = 0; i < clazz->numDeclaredMethods; ++i) {
    candidate = &clazz->own_methods[i];

    if (candidate->spec.name == name_string && candidate->desc == desc_string) {
      method = candidate;
      break;
    }
  }

  if (!method) {
    for (j = 0; j < clazz->numSuperClasses; ++j) {
      super = clazz->supers[j];
      woempa(1, "Seek %w in %K\n", spec->name, super);
      for (i = 0; i < super->numDeclaredMethods; ++i) {
        candidate = &super->own_methods[i];

        if (candidate->spec.name == name_string && candidate->desc == desc_string) {
          method = candidate;
          break;
        }
      }
    }
  }

  if (!method) {
    for (j = 0; j < clazz->numInterfaces; ++j) {
      super = clazz->interfaces[j];
      woempa(1, "Seek %w in %K\n", spec->name, super);
      for (i = 0; i < super->numDeclaredMethods; ++i) {
        candidate = &super->own_methods[i];

        if (candidate->spec.name == name_string && candidate->desc == desc_string) {
          method = candidate;
          break;
        }
      }
    }
  }

  if (method) {
    if (!methodMatchesSpec(method, spec)) {
      woempa(9,"%M matches name %w and descriptor %w, but classes do not resolve correctly.\n", method, name_string, desc_string);
      method = NULL;
    }
    else if (isSet(method->flags, ACC_STATIC)) {
      woempa(9,"Was looking for an instance method %w%w, but %M is static\n", name_string, desc_string, method);
      method = NULL;
    }
    else {
      woempa(1,"Found  method %M\n", method);
    }
  }

  releaseMethodSpec(spec);
  releaseMem(spec);
  deregisterString(name_string);
  deregisterString(desc_string);

  return method;
}

/*
** =>  static object function calls
*/

jobject CallStaticObjectMethod(JNIEnv *env, jclass class, jmethodID methodID, ...) {
  va_list  list;
  jobject  result = NULL;
  
  va_start(list, methodID);
  result = (jobject)CallMethod32(env, NULL, methodID, list);
  va_end(list);
  
  return result;
}

jobject CallStaticObjectMethodV(JNIEnv *env, jclass class, jmethodID methodID, va_list args) {
  return (jobject)CallMethod32(env, NULL, methodID, args); 
}

jobject CallStaticObjectMethodA(JNIEnv *env, jclass class, jmethodID methodID, jvalue *args) {
  return (jobject)CallMethod32A(env, NULL, methodID, args); 
}

/*
** =>  static boolean function calls
*/

jboolean CallStaticBooleanMethod(JNIEnv *env, jclass class, jmethodID methodID, ...) {
  va_list  list;
  jboolean result = 0;
  
  va_start(list, methodID);
  result = (jboolean)CallMethod32(env, NULL, methodID, list);
  va_end(list);
  
  return result;
}

jboolean CallStaticBooleanMethodV(JNIEnv *env, jclass class, jmethodID methodID, va_list args) {
  return (jboolean)CallMethod32(env, NULL, methodID, args); 
}

jboolean CallStaticBooleanMethodA(JNIEnv *env, jclass class, jmethodID methodID, jvalue *args) {
  return (jboolean)CallMethod32A(env, NULL, methodID, args); 
}

/*
** =>  static byte function calls
*/

jbyte CallStaticByteMethod(JNIEnv *env, jclass class, jmethodID methodID, ...) {
  va_list  list;
  jbyte    result = 0;
  
  va_start(list, methodID);
  result = (jbyte)CallMethod32(env, NULL, methodID, list);
  va_end(list);
  
  return result;
}

jbyte CallStaticByteMethodV(JNIEnv *env, jclass class, jmethodID methodID, va_list args) {
  return (jbyte)CallMethod32(env, NULL, methodID, args); 
}

jbyte CallStaticByteMethodA(JNIEnv *env, jclass class, jmethodID methodID, jvalue *args) {
  return (jbyte)CallMethod32A(env, NULL, methodID, args); 
}

/*
** =>  static char function calls
*/

jchar CallStaticCharMethod(JNIEnv *env, jclass class, jmethodID methodID, ...) {
  va_list  list;
  jchar    result = 0;
  
  va_start(list, methodID);
  result = (jchar)CallMethod32(env, NULL, methodID, list);
  va_end(list);
  
  return result;
}

jchar CallStaticCharMethodV(JNIEnv *env, jclass class, jmethodID methodID, va_list args) {
  return (jchar)CallMethod32(env, NULL, methodID, args); 
}

jchar CallStaticCharMethodA(JNIEnv *env, jclass class, jmethodID methodID, jvalue *args) {
  return (jchar)CallMethod32A(env, NULL, methodID, args); 
}

/*
** =>  static short function calls
*/

jshort CallStaticShortMethod(JNIEnv *env, jclass class, jmethodID methodID, ...) {
  va_list  list;
  jshort   result = 0;
  
  va_start(list, methodID);
  result = (jshort)CallMethod32(env, NULL, methodID, list);
  va_end(list);
  
  return result;
}

jshort CallStaticShortMethodV(JNIEnv *env, jclass class, jmethodID methodID, va_list args) {
  return (jshort)CallMethod32(env, NULL, methodID, args); 
}

jshort CallStaticShortMethodA(JNIEnv *env, jclass class, jmethodID methodID, jvalue *args) {
  return (jshort)CallMethod32A(env, NULL, methodID, args); 
}

/*
** =>  static int function calls
*/

jint CallStaticIntMethod(JNIEnv *env, jclass class, jmethodID methodID, ...) {
  va_list  list;
  jint     result = 0;
  
  va_start(list, methodID);
  result = (jint)CallMethod32(env, NULL, methodID, list);
  va_end(list);
  
  return result;
}

jint CallStaticIntMethodV(JNIEnv *env, jclass class, jmethodID methodID, va_list args) {
  return (jint)CallMethod32(env, NULL, methodID, args); 
}

jint CallStaticIntMethodA(JNIEnv *env, jclass class, jmethodID methodID, jvalue *args) {
  return (jint)CallMethod32A(env, NULL, methodID, args); 
}

/*
** =>  static long function calls
*/

jlong CallStaticLongMethod(JNIEnv *env, jclass class, jmethodID methodID, ...) {
  va_list  list;
  jlong    result = 0;
  
  va_start(list, methodID);
  result = (jlong)CallMethod64(env, NULL, methodID, list);
  va_end(list);
  
  return result;
}

jlong CallStaticLongMethodV(JNIEnv *env, jclass class, jmethodID methodID, va_list args) {
  return (jlong)CallMethod64(env, NULL, methodID, args); 
}

jlong CallStaticLongMethodA(JNIEnv *env, jclass class, jmethodID methodID, jvalue *args) {
  return (jlong)CallMethod64A(env, NULL, methodID, args); 
}

/*
** =>  static float function calls
*/

jfloat CallStaticFloatMethod(JNIEnv *env, jclass class, jmethodID methodID, ...) {
  va_list  list;
  jfloat   result = 0;
  
  va_start(list, methodID);
  result = (jfloat)CallMethod32(env, NULL, methodID, list);
  va_end(list);
  
  return result;
}

jfloat CallStaticFloatMethodV(JNIEnv *env, jclass class, jmethodID methodID, va_list args) {
  return (jfloat)CallMethod32(env, NULL, methodID, args); 
}

jfloat CallStaticFloatMethodA(JNIEnv *env, jclass class, jmethodID methodID, jvalue *args) {
  return (jfloat)CallMethod32A(env, NULL, methodID, args); 
}

/*
** =>  static double function calls
*/

jdouble CallStaticDoubleMethod(JNIEnv *env, jclass class, jmethodID methodID, ...) {
  va_list  list;
  jdouble  result = 0;
  
  va_start(list, methodID);
  result = (jdouble)CallMethod64(env, NULL, methodID, list);
  va_end(list);
  
  return result;
}

jdouble CallStaticDoubleMethodV(JNIEnv *env, jclass class, jmethodID methodID, va_list args) {
  return (jdouble)CallMethod64(env, NULL, methodID, args); 
}

jdouble CallStaticDoubleMethodA(JNIEnv *env, jclass class, jmethodID methodID, jvalue *args) {
  return (jdouble)CallMethod64A(env, NULL, methodID, args); 
}

/*
** =>  static void function calls
*/
   
w_void CallStaticVoidMethod(JNIEnv *env, jclass class, jmethodID methodID, ...) {
  va_list  list;
  
  va_start(list, methodID);
  CallMethodVoid(env, NULL, methodID, list);
  va_end(list);
}

w_void CallStaticVoidMethodV(JNIEnv *env, jclass class, jmethodID methodID, va_list args) {
  CallMethodVoid(env, NULL, methodID, args); 
}

w_void CallStaticVoidMethodA(JNIEnv *env, jclass class, jmethodID methodID, jvalue *args) {     
  CallMethodVoidA(env, NULL, methodID, args); 
}

/*
** static member variable field access:
** get the member variable field ID
*/

jfieldID GetStaticFieldID(JNIEnv *env, jclass class, const char *name, const char *sig) {

  w_clazz  clazz  = Class2clazz(class);
  w_string name_string = cstring2String((char *)name, strlen(name));
  w_string desc_string = cstring2String((char *)sig, strlen(sig));
  w_field result;
  
  if (!name_string || !desc_string) {
    wabort(ABORT_WONKA, "Unable to create name and desc string\n");
  }
  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {

    return NULL;

  }
  
  woempa(1, "(JNI) Returning field ID for field '%s', descriptor '%s'.\n", name, sig);
  
  result = searchClazzHierarchyForField(Class2clazz(class), name_string, desc_string, MATCH_STATIC_FIELD, 0);

  deregisterString(desc_string);
  deregisterString(name_string);
  
  return result;

}

/*
** get the static member variable value from field ID
*/

JNITypes GetStatic32Field(JNIEnv *env, jclass class, jfieldID fieldID) {

  w_clazz  clazz  = Class2clazz(class);

  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {
    JNITypes j;

    return j;

  }

  woempa(1, "(JNI) Getting field '%w' at slot %d from class %k.\n", NM(fieldID), fieldID->size_and_slot, clazz);

  return (JNITypes)clazz->staticFields[fieldID->size_and_slot];

}

JNITypes GetStatic64Field(JNIEnv *env, jclass class, jfieldID fieldID) {

  w_clazz clazz = Class2clazz(class);
  w_long result;

  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {
    JNITypes j;

    return j;

  }
  woempa(1, "(JNI) Getting field '%w' at slot %d from class %k.\n", NM(fieldID), fieldID->size_and_slot, clazz);
  
  result = *(w_long*)&clazz->staticFields[fieldID->size_and_slot];
  
  return (JNITypes)result;

}

jobject GetStaticObjectField(JNIEnv *env, jclass class, jfieldID fieldID) {
  return (GetStatic32Field(env, class, fieldID)).o;
}

jboolean GetStaticBooleanField(JNIEnv *env, jclass class, jfieldID fieldID) {
  return (GetStatic32Field(env, class, fieldID)).z;
}

jbyte GetStaticByteField(JNIEnv *env, jclass class, jfieldID fieldID) {
  return (GetStatic32Field(env, class, fieldID)).b;
}

jchar GetStaticCharField(JNIEnv *env, jclass class, jfieldID fieldID) {
  return (GetStatic32Field(env, class, fieldID)).c;
}

jshort GetStaticShortField(JNIEnv *env, jclass class, jfieldID fieldID) {
  return (GetStatic32Field(env, class, fieldID)).s;
}

jint GetStaticIntField(JNIEnv *env, jclass class, jfieldID fieldID) {
  return (GetStatic32Field(env, class, fieldID)).i;
}

jlong GetStaticLongField(JNIEnv *env, jclass class, jfieldID fieldID) {
  return (GetStatic64Field(env, class, fieldID)).j;
}

jfloat GetStaticFloatField(JNIEnv *env, jclass class, jfieldID fieldID) {
  return (GetStatic32Field(env, class, fieldID)).f;
}

jdouble GetStaticDoubleField(JNIEnv *env, jclass class, jfieldID fieldID) {
  return (GetStatic64Field(env, class, fieldID)).d;
}

/*
** set the static member variable in a field ID to given value
*/

w_void SetStatic32Field(JNIEnv *env, jclass class, jfieldID fieldID, w_word value) {

  w_clazz clazz = Class2clazz(class);

  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {

    return;

  }
  clazz->staticFields[fieldID->size_and_slot] = value;

}

w_void SetStatic64Field(JNIEnv *env, jclass class, jfieldID fieldID, jlong value) {

  w_clazz clazz = Class2clazz(class);

  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {

    return;

  }
  *(w_long*)&clazz->staticFields[fieldID->size_and_slot] = value;

}

w_void SetStaticObjectField(JNIEnv *env, jclass class, jfieldID fieldID, jobject value) {
  setStaticReferenceField(fieldID->declaring_clazz, fieldID->size_and_slot, value);
}

w_void SetStaticBooleanField(JNIEnv *env, jclass class, jfieldID fieldID, jboolean value) {
  SetStatic32Field(env, class, fieldID, (w_word)value);
}

w_void SetStaticByteField(JNIEnv *env, jclass class, jfieldID fieldID, jbyte value) {
  SetStatic32Field(env, class, fieldID, (w_word)value);
}

w_void SetStaticCharField(JNIEnv *env, jclass class, jfieldID fieldID, jchar value) {
  SetStatic32Field(env, class, fieldID, (w_word)value);
}

w_void SetStaticShortField(JNIEnv *env, jclass class, jfieldID fieldID, jshort value) {
  SetStatic32Field(env, class, fieldID, (w_word)value);
}

w_void SetStaticIntField(JNIEnv *env, jclass class, jfieldID fieldID, jint value) {
  SetStatic32Field(env, class, fieldID, (w_word)value);
}

w_void SetStaticLongField(JNIEnv *env, jclass class, jfieldID fieldID, jlong value) {
  SetStatic64Field(env, class, fieldID, (jlong)value);
}

w_void SetStaticFloatField(JNIEnv *env, jclass class, jfieldID fieldID, jfloat value) {
  SetStatic32Field(env, class, fieldID, (w_word)value);
}

w_void SetStaticDoubleField(JNIEnv *env, jclass class, jfieldID fieldID, jdouble value) {
  SetStatic64Field(env, class, fieldID, (jlong)value);
}


jsize GetStringLength(JNIEnv *env, jstring String) {

  w_string string = String2string(String);

  woempa(1, "(JNI) Length of '%w' is %d JAVA chars.\n", string, String, string_length(string));
  
  return string_length(string);
  
}

const jchar *GetStringChars(JNIEnv *env, jstring String, jboolean *isCopy) {

  w_string string = String2string(String);
  w_char  *chars = NULL;

  if (string_length(string)) {
    chars  = allocMem(string_length(string) * sizeof(w_char));
    if (!chars) {
      wabort(ABORT_WONKA, "Unable to alloc space for chars\n");
    }
    w_string2chars(string, chars);
  }
  woempa(1,"(JNI) created array of %d chars at %p\n",string_length(string),chars);

  if (isCopy) {
    *isCopy = WONKA_TRUE;
  }

  return chars;
  
}

void ReleaseStringChars(JNIEnv *env, jstring string, const jchar *chars) {

  if (chars) {
    woempa(1, "(JNI) release array of chars at %p\n", chars);
    releaseMem((void *)chars);
  }

}
          
jstring NewString(JNIEnv *env, const jchar * uchars, jsize len) {

  w_string string = unicode2String((w_char*) uchars, (w_size) len);
  w_instance String = NULL;

  if (string) {
    String = newStringInstance(string);
    deregisterString(string);
  }
  return String;

}

jstring NewStringUTF(JNIEnv *env, const char * bytes) {

  w_string string = utf2String((char *) bytes, strlen(bytes));
  w_instance String = NULL;

  if (string) {
    String = newStringInstance(string);
    deregisterString(string);
  }
  return String;
  
}

jsize GetStringUTFLength(JNIEnv *env, jstring String) {
  w_string string = String2string(String);
  w_size   i;
  jsize    result = 0;
 
/*
  if (string_is_latin1(string)) {

    return string_length(string);

  }
*/

  for (i=0; i<string_length(string); ++i) {
    w_char ch = string_char(string, i);
    if (ch == 0) {
      result += 2;
    }
    else if (ch < 0x80 ) {
      result += 1;
    }
    else if (ch < 0x800 ) {
      result += 2;
    }
    else {
      result += 3;
    }
  }

  return result;
}

const jbyte * GetStringUTFChars(JNIEnv *env, jstring String, jboolean *isCopy) {

  w_string string = String2string(String);
  w_size   max_length = string_length(string) * 3;
  jbyte *  result = allocMem((max_length + 1) * sizeof(jbyte));
  jbyte *  utf8ptr = result;
  w_size   i;
 
  if (!result) {
    wabort(ABORT_WONKA, "Unable to alloc space for result\n");
  }
  for (i = 0; i < string_length(string); i++) {
    w_char ch = string_char(string, i);
    if (ch == 0) {
      *utf8ptr++ = 0xc0;
      *utf8ptr++ = 0x80;
    }
    else if (ch < 0x80 ) {
      *utf8ptr++ = string_char(string, i);
    }
    else if (ch < 0x800 ) {
      *utf8ptr++ = 0xc0 + (string_char(string, i) >> 6);
      *utf8ptr++ = 0x80 + (string_char(string, i) & 0x3f);
    }
    else {
      *utf8ptr++ = 0xe0 + (string_char(string, i) >> 12);
      *utf8ptr++ = 0x80 + ((string_char(string, i) >> 6) & 0x3f);
      *utf8ptr++ = 0x80 + (string_char(string, i) & 0x3f);
    }
  }
  *utf8ptr = '\0'; /* Terminating string */

  if (isCopy) {
    *isCopy = JNI_TRUE;
  }

  return result;

}

void GetStringRegion(JNIEnv *env, jstring String, jsize start, jsize len, jchar * buf) {

  w_string string = String2string(String);
  jchar *  bufptr = (jchar*)buf;
  jsize    i;
  w_thread thread = JNIEnv2w_thread(env);
  
  if (start > (jsize) string_length(string)) {
    throwException(thread, clazzStringIndexOutOfBoundsException, "start %d > String %d chars.\n", start, string_length(string));
    return;
  }
  
  if (len > (jsize) string_length(string) - start) {
    throwException(thread, clazzStringIndexOutOfBoundsException, "start %d + len %d > String %d chars.\n", start, len, string_length(string));
    return;
  }

  for (i = 0; i < len; i++) {
    *bufptr++ = string_char(string, start + i);
  }

}

void GetStringUTFRegion(JNIEnv *env, jstring String, jsize start, jsize len, char * buf) {

  w_string string = String2string(String);
  jbyte *  utf8ptr = (signed char*)buf;
  jsize    i;
  w_thread thread = JNIEnv2w_thread(env);
  
  if (start > (jsize) string_length(string)) {
    throwException(thread, clazzStringIndexOutOfBoundsException, "start %d > String %d chars.\n", start, string_length(string));
    return;
  }
  
  if (len > (jsize) string_length(string) - start) {
    throwException(thread, clazzStringIndexOutOfBoundsException, "start %d + len %d > String %d chars.\n", start, len, string_length(string));
    return;
  }

  for (i = 0; i < len; i++) {
    w_char ch = string_char(string, start + i);
    if (ch == 0) {
      *utf8ptr++ = 0xc0;
      *utf8ptr++ = 0x80;
    }
    else if (ch < 0x80 ) {
      *utf8ptr++ = ch;
    }
    else if (ch < 0x800 ) {
      *utf8ptr++ = 0xc0 + (ch >> 6);
      *utf8ptr++ = 0x80 + (ch & 0x3f);
    }
    else {
      *utf8ptr++ = 0xe0 + (ch >> 12);
      *utf8ptr++ = 0x80 + ((ch >> 6) & 0x3f);
      *utf8ptr++ = 0x80 + (ch & 0x3f);
    }
  }

}

void ReleaseStringUTFChars(JNIEnv *env, jstring string, const char *utf) {
  releaseMem((void*)utf);
}                  

jsize GetArrayLength(JNIEnv *env, jarray array) {
  return (jsize)instance2Array_length(array);
}
  
jarray NewObjectArray(JNIEnv *env, jsize length, jclass elementType, jobject initialElement) {

  w_instance initval = initialElement;
  w_thread thread = JNIEnv2w_thread(env);
  w_instance Array;
  w_int i;
  w_clazz elementClazz = Class2clazz(elementType);
  w_clazz arrayClazz;
  w_int alength;
  // [CG 20040315] We should probably default to the application class loader
  w_method caller = thread->top->method;
  w_instance loader;

  threadMustBeSafe(thread);
  if (caller) {
    woempa(1, "(JNI) Asked to construct an array of %d '%s', called from %M\n", length, elementType, caller);
    loader = clazz2loader(caller->spec.declaring_clazz);
    woempa(1, "(JNI) Using class loader %p of %k.\n", loader, instance2clazz(loader));
  }
  else {
    loader = systemClassLoader;
    woempa(1, "(JNI) Asked to construct an array of %d '%s', thread %t has no stack frame so using bootstrap class loader\n", length, elementType, thread);
  }


  woempa(1, "Asked to construct an array of %d %k's, all set to 0x%08x\n", length, elementClazz, initval);

  arrayClazz = getNextDimension(elementClazz, loader);
  if (exceptionThrown(thread)) {
    return NULL;
  }
  mustBeInitialized(arrayClazz);
  if (exceptionThrown(thread)) {
    return NULL;
  }

  woempa(1, "New array will be of class %k\n", arrayClazz);
  alength = length;
  enterUnsafeRegion(thread);
  Array = allocArrayInstance_1d(JNIEnv2w_thread(env), arrayClazz, alength);
  enterSafeRegion(thread);
  woempa(1, "New array is %j\n", Array);

  if (Array) {
    for (i = 0; i < length; i++) {
      Array[F_Array_data + i] = (w_word) initval;
    }
    woempa(1, "(JNI) Length %d, type %k, Array = %p.\n", length, clazz_Array, Array);
  }

  return Array;
  
}

jobject GetObjectArrayElement(JNIEnv *env, jobjectArray Array, jsize aindex) {

  w_instance *instanceArray = instance2Array_instance(Array);

  woempa(1, "(JNI) (%k)(%p)[%d] = %p.\n", instance2clazz(Array), Array, aindex, instanceArray[aindex]);

  return instanceArray[aindex];
  
}

void SetObjectArrayElement(JNIEnv *env, jobjectArray Array, jsize aindex, jobject value) {

  woempa(1, "(JNI) (%k)(%p)[%d] = %p (%k).\n", instance2clazz(Array), Array, aindex, value, instance2clazz(value));
  setArrayReferenceField(Array, value, aindex);
  
}

static w_instance newTypeArray(w_thread thread, w_clazz clazz, w_int length) {
  w_instance Array;

  threadMustBeSafe(thread);
  mustBeInitialized(clazz);
  if (exceptionThrown(thread)) {
    return NULL;
  }

  enterUnsafeRegion(thread);
  Array = allocArrayInstance_1d(thread, clazz, length);
  enterSafeRegion(thread);

  woempa(1, "(JNI) Length %d, type %k, Array = %p.\n", length, clazz, Array);
 
  return Array;
  
}

/*
** Create primitive array types. Look at the 'newarray' opcode explanation
** for the meaning of the array indexes.
*/

jbooleanArray NewBooleanArray(JNIEnv *env, jsize length) {
  return newTypeArray(JNIEnv2w_thread(env), atype2clazz[P_boolean], length);
}

jbyteArray NewByteArray(JNIEnv *env, jsize length) {
  return newTypeArray(JNIEnv2w_thread(env), atype2clazz[P_byte], length);
}

jcharArray NewCharArray(JNIEnv *env, jsize length) {
  return newTypeArray(JNIEnv2w_thread(env), atype2clazz[P_char], length);
}

jshortArray NewShortArray(JNIEnv *env, jsize length) {
  return newTypeArray(JNIEnv2w_thread(env), atype2clazz[P_short], length);
}

jintArray NewIntArray(JNIEnv *env, jsize length) {
  return newTypeArray(JNIEnv2w_thread(env), atype2clazz[P_int], length);
}

jlongArray NewLongArray(JNIEnv *env, jsize length) {
  return newTypeArray(JNIEnv2w_thread(env), atype2clazz[P_long], length);
}

jfloatArray NewFloatArray(JNIEnv *env, jsize length) {
  return newTypeArray(JNIEnv2w_thread(env), atype2clazz[P_float], length);
}

jdoubleArray NewDoubleArray(JNIEnv *env, jsize length) {
  return newTypeArray(JNIEnv2w_thread(env), atype2clazz[P_double], length);
}

jboolean *GetBooleanArrayElements(JNIEnv *env, jbooleanArray array, jboolean *isCopy) {

  w_instance array_instance = array;
  w_size length = instance2Array_length(array_instance);
  jboolean *buffer = allocMem(length * sizeof(jboolean));
  jboolean* dest = buffer;
  w_sbyte *arrayAsBytes = instance2Array_byte(array_instance);
  w_size i;
  
  if (!buffer) {
    wabort(ABORT_WONKA, "Unable to alloc space for buffer\n");
  }
  woempa(1, "(JNI) array of %d bits at %p expands to %d jbooleans (%d bytes) at %p\n", length, array_instance, length, (w_size)(length * sizeof(jboolean)), buffer);

  for (i = 0; i < length; i++) {
    *dest++ = !!(arrayAsBytes[i/8] & (1<<(i%8)));
  }
  
  if (isCopy) {
    *isCopy = WONKA_TRUE;
  }

  return buffer;

}

jbyte *GetByteArrayElements(JNIEnv *env, jbyteArray array, jboolean *isCopy) {
  if (isCopy) {
    *isCopy = WONKA_FALSE;
  }

  return instance2Array_byte(array);
}

jchar *GetCharArrayElements(JNIEnv *env, jcharArray array, jboolean *isCopy) {
  if (isCopy) {
    *isCopy = WONKA_FALSE;
  }

  return instance2Array_char(array);
}

jshort *GetShortArrayElements(JNIEnv *env, jshortArray array, jboolean *isCopy) {
  if (isCopy) {
    *isCopy = WONKA_FALSE;
  }

  return instance2Array_short(array);
}

jint *GetIntArrayElements(JNIEnv *env, jintArray array, jboolean *isCopy) {
  if (isCopy) {
    *isCopy = WONKA_FALSE;
  }

  return instance2Array_int(array);
}

jlong *GetLongArrayElements(JNIEnv *env, jlongArray array, jboolean *isCopy) {
  if (isCopy) {
    *isCopy = WONKA_FALSE;
  }

  return instance2Array_long(array);
}

jfloat *GetFloatArrayElements(JNIEnv *env, jfloatArray array, jboolean *isCopy) {
  if (isCopy) {
    *isCopy = WONKA_FALSE;
  }

  return instance2Array_float(array);
}

jdouble *GetDoubleArrayElements(JNIEnv *env, jdoubleArray array, jboolean *isCopy) {
  if (isCopy) {
    *isCopy = WONKA_FALSE;
  }

  return instance2Array_double(array);
}

void ReleaseBooleanArrayElements(JNIEnv *env, jbooleanArray array, jboolean *elements, jint mode) {
  w_instance array_instance = array;
  w_int length = instance2Array_length(array_instance);

  if (mode!=JNI_ABORT) {
    writeBooleansToArray(array_instance, 0, length, elements);
  }
  
  if (mode!=JNI_COMMIT) {
    woempa(1,"(JNI) release copy at %p\n",elements);
    releaseMem(elements);
  }
}

void ReleaseByteArrayElements(JNIEnv *env, jbyteArray array, jbyte *elements, jint mode) {
}

void ReleaseCharArrayElements(JNIEnv *env, jcharArray array, jchar *elements, jint mode) {
}

void ReleaseShortArrayElements(JNIEnv *env, jshortArray array, jshort *elements, jint mode) {
}

void ReleaseIntArrayElements(JNIEnv *env, jintArray array, jint *elements, jint mode) {
}

void ReleaseLongArrayElements(JNIEnv *env, jlongArray array, jlong *elements, jint mode) {
}

void ReleaseFloatArrayElements(JNIEnv *env, jfloatArray array, jfloat *elements, jint mode) {
}

void ReleaseDoubleArrayElements(JNIEnv *env, jdoubleArray array, jdouble *elements, jint mode) {
}                                                        

w_void GetBooleanArrayRegion(JNIEnv *env, jbooleanArray array, jsize start, jsize len, jboolean *buf) {
  readBooleansFromArray(array, start, len, buf);
}

w_void GetByteArrayRegion(JNIEnv *env, jbyteArray array, jsize start, jsize len, jbyte *buf) {
  readBytesFromArray(array, start, len, buf);
}

w_void GetCharArrayRegion(JNIEnv *env, jcharArray array, jsize start, jsize len, jchar *buf) {
  readCharsFromArray(array, start, len, buf);
}

w_void GetShortArrayRegion(JNIEnv *env, jshortArray array, jsize start, jsize len, jshort *buf) {
  readShortsFromArray(array, start, len, buf);
}

w_void GetIntArrayRegion(JNIEnv *env, jintArray array, jsize start, jsize len, jint *buf) {
  readIntsFromArray(array, start, len, buf);
}

w_void GetLongArrayRegion(JNIEnv *env, jlongArray array, jsize start, jsize len, jlong *buf) {
  readLongsFromArray(array, start, len, buf);
}

w_void GetFloatArrayRegion(JNIEnv *env, jfloatArray array, jsize start, jsize len, jfloat *buf) {
  readFloatsFromArray(array, start, len, buf);
}

w_void GetDoubleArrayRegion(JNIEnv *env, jdoubleArray array, jsize start, jsize len, jdouble *buf) {
  readDoublesFromArray(array, start, len, buf);
}

w_void SetBooleanArrayRegion(JNIEnv *env, jbooleanArray array, jsize start, jsize len, jboolean *buf) {
  writeBooleansToArray(array, start, len, buf);
}

w_void SetByteArrayRegion(JNIEnv *env, jbyteArray array, jsize start, jsize len, jbyte *buf) {
  writeBytesToArray(array, start, len, buf);
}

w_void SetCharArrayRegion(JNIEnv *env, jcharArray array, jsize start, jsize len, jchar *buf) {
  writeShortsToArray(array, start, len, (jshort*)buf);
}

w_void SetShortArrayRegion(JNIEnv *env, jshortArray array, jsize start, jsize len, jshort *buf) {
  writeShortsToArray(array, start, len, buf);
}

w_void SetIntArrayRegion(JNIEnv *env, jintArray array, jsize start, jsize len, jint *buf) {
  writeIntsToArray(array, start, len, buf);
}

w_void SetLongArrayRegion(JNIEnv *env, jlongArray array, jsize start, jsize len, jlong *buf) {
  writeLongsToArray(array, start, len, buf);
}

w_void SetFloatArrayRegion(JNIEnv *env, jfloatArray array, jsize start, jsize len, jfloat *buf) {
  writeFloatsToArray(array, start, len, buf);
}

w_void SetDoubleArrayRegion(JNIEnv *env, jdoubleArray array, jsize start, jsize len, jdouble *buf) {
  writeDoublesToArray(array, start, len, buf);
}
                
jint RegisterNatives(JNIEnv *env, jclass class, const JNINativeMethod *methods, jint nMethods) {
  registerNatives(Class2clazz(class), methods, nMethods);
  return 0;
}

jint UnregisterNatives(JNIEnv *env, jclass class) {
  wabort(ABORT_WONKA, "JNI function not yet implemented.\n");
  return -1;
}
  
jint MonitorEnter(JNIEnv *env, jobject obj) {
  enterMonitor((w_instance)obj);
  return 0;
}

jint MonitorExit(JNIEnv *env, jobject obj) {
  exitMonitor((w_instance)obj);
  return 0;
}
 

/*
** GetPrimitiveArrayCritical / ReleasePrimitiveArrayCritical: these exclude
** garbage collection. The calls must be matched!!!
*/

void* GetPrimitiveArrayCritical(JNIEnv *env, jarray array, jboolean *isCopy)  {
  w_thread thread = JNIEnv2w_thread(env);
  w_instance array_instance = array;
  void     *buffer = NULL;
  w_clazz  array_clazz = instance2clazz(array);
  w_clazz  component_clazz = array_clazz->previousDimension;
  jboolean tmp = FALSE;

  enterUnsafeRegion(thread);
  if(component_clazz == clazz_boolean ) {
    buffer = GetBooleanArrayElements(env, array_instance, &tmp);
  }
  else if(component_clazz == clazz_byte) {
    buffer = GetByteArrayElements(env, array_instance, &tmp);
  }
  else if(component_clazz == clazz_char ) {
    buffer = GetCharArrayElements(env, array_instance, &tmp);
  }
  else if(component_clazz == clazz_short ) {
    buffer = GetShortArrayElements(env, array_instance, &tmp);
  }
  else if(component_clazz == clazz_int ) {
    buffer = GetIntArrayElements(env, array_instance, &tmp);
  }
  else if(component_clazz == clazz_long ) {
    buffer = GetLongArrayElements(env, array_instance, &tmp);
  }
  else if(component_clazz == clazz_float ) {
    buffer = GetFloatArrayElements(env, array_instance, &tmp);
  }
  else if(component_clazz == clazz_double ) {
    buffer = GetDoubleArrayElements(env, array_instance, &tmp);
  }
  else {
    buffer = NULL;
  }

  if (isCopy) {
    *isCopy = tmp;
  }

  return buffer;
}


void  ReleasePrimitiveArrayCritical(JNIEnv *env, jarray array, void *carray, jint mode) {
  w_thread thread = JNIEnv2w_thread(env);
  w_instance array_instance = array;
  w_int length = instance2Array_length(array_instance);
  w_clazz  array_clazz = instance2clazz(array);
  w_clazz  component_clazz = array_clazz->previousDimension;

  if(component_clazz == clazz_boolean ) {
    if (mode!=JNI_ABORT) {
      writeBooleansToArray(array_instance, 0, length, (jboolean*)carray);
    }

    if (mode!=JNI_COMMIT) {
      woempa(1,"(JNI) release copy at %p\n",carray);
      releaseMem(carray);
    }
  }
  enterSafeRegion(thread);
}

/*
** GetStringCritical / ReleaseStringCritical:
** since all string buffer locks in Wonka are critical, we can just forward these calls
** to the standard GetStringchars/ReleaseStringChars functions
*/

const jchar *GetStringCritical(JNIEnv *env, jstring String, jboolean *isCopy) {
  woempa(1,"redirecting GetStringCritical to GetStringChars(Always critical in Wonka)\n");
  return GetStringChars(env,String,isCopy);
}


void ReleaseStringCritical(JNIEnv *env, jstring string, const jchar *chars) {
  woempa(1,"redirecting ReleaseStringCritical to ReleaseStringChars(Always critical in Wonka)\n");
  ReleaseStringChars(env, string,chars);
}

/*
** There is only one JVM; he wears green trousers and a plum jacket,
** and sports a top hat and a walking-cane.
*/

static JavaVM mrWonka;

/*
** You can ask for any version you want, so long as it's positive.
** Mr. Wonka hates negative attitudes.
*/

jint JNI_GetDefaultJavaVMInitArgs(void *vm_args) {
  return ((Wonka_InitArgs*)vm_args)->version & 0x80000000 ? -1 : 0;
}

/*
** He is unique.
*/

jint JNI_GetCreatedJavaVMs(JavaVM **vmBuf, jsize bufLen, jsize *nVMs) {
  if (bufLen) {
    mrWonka = &w_JNIInvokeInterface;
    *vmBuf = &mrWonka;
    *nVMs = 1;
  }
  return 0;
}

jint JNI_CreateJavaVM(JavaVM **p_VM, JNIEnv **p_env, void *vm_args) {

  globals_hashtable = ht_create((char*)"hashtable:global-refs", GLOBALS_HASHTABLE_SIZE, NULL, NULL, 0, 0);
  if (!globals_hashtable) {
    wabort(ABORT_WONKA, "Unable to create globals_hashtable\n");
  }
  woempa(1, "(JNI) created globals_hashtable at %p\n", globals_hashtable);

  system_vm_args = (struct JavaVMInitArgs *)vm_args;

  startHeap();
  startLoading();
  startKernel();
  
#ifdef RUDOLPH  
#ifdef MODULES 
  loadModule("mod_awt");
#else 
  init_awt();
#endif
#endif

  x_thread_resume(W_Thread_sysInit->kthread);

  return 0;

}

jint GetJavaVM( JNIEnv *env,  JavaVM** vm) {
  mrWonka = &w_JNIInvokeInterface;
  *vm = &mrWonka;
  
  return 0;
}


jint DestroyJavaVM(JavaVM *vm) {
  woempa(9,"Sorry, you can't do that.\n");
  return -1;
}

extern pthread_key_t x_thread_key;

/*
** In order to visit the Wonka Factory, you must be in possesion of valid
** "pieces of identity".  This function will furnish you with them.
*/

jint AttachCurrentThread(JavaVM *vm, JNIEnv **p_env, void *thr_args) {
  w_size     i;
  w_instance Thread;
  w_thread   thread;
  w_method   method = NULL;
  Wonka_AttachArgs *wonka_args = thr_args;
  char *cname = (char*)"Native Thread";
  w_MethodSpec spec;
  x_thread kthread = x_thread_current();

  if (!thread_hashtable) {
    woempa(9, "Attempt to attach before VM is properly initialised\n");
    return -1;
  }

  /*
  ** If x_thread_current() returned null there is no x_thread corresponding
  ** to the native thread, we need to create one.
  */
  if (!kthread) {
    kthread = allocClearedMem(sizeof(x_Thread));
    x_thread_attach_current(kthread);
  }

  thread = (w_thread)ht_read(thread_hashtable, (w_word)kthread);

  /*
  ** If the thread is already in thread_hashtable then we have nothing to do.
  */
  if (thread) {
    *p_env = w_thread2JNIEnv(thread);

    return 0;
  }


  if (wonka_args && wonka_args->name) {
    cname = wonka_args->name;
    woempa(7, "Setting thread name to %s\n", cname);
  }

  thread  = allocClearedMem(sizeof(w_Thread));
  if (!thread) {
    woempa(9, "Unable to allocate w_Thread\n");

    return -2;
  }

  setUpRootFrame(thread);
  thread->natenv = &w_JNINativeInterface;
  thread->label = (char *)"thread:native";
  thread->name = cstring2String(cname,strlen(cname));
  thread->state = wt_ready;
  thread->jpriority = 5; 
  thread->isDaemon = WONKA_FALSE;
  thread->flags = WT_THREAD_IS_NATIVE;
  thread->kthread = kthread;
  pthread_setspecific(x_thread_key, kthread);
  thread->kthread->xref = thread;
  thread->kpriority = x_thread_priority_get(thread->kthread);
  thread->ksize = 65536; // BOGUS - TODO: what to put here?
  thread->kthread->report = running_thread_report;
  woempa(7, "Registering os thread %p as Java thread %p\n", thread->kthread, thread);
  ht_write(thread_hashtable, (w_word)thread->kthread, (w_word)thread);
  if (mustBeInitialized(clazzNativeThread) == CLASS_LOADING_FAILED) {

    return -3;

  }

  enterUnsafeRegion(thread);
  Thread = allocInstance_initialized(NULL, clazzNativeThread);
  enterSafeRegion(thread);
  setReferenceField(Thread, I_ThreadGroup_system, F_Thread_parent);
  setWotsitField(Thread, F_Thread_wotsit,  thread);
  thread->Thread = Thread;
  woempa(7, "Native thread %p now has instance of %j.\n", thread, Thread);
  method = get_underscore_jmethodID(w_thread2JNIEnv(thread));

  if (method == NULL) {
    wabort(ABORT_WONKA,"Uh oh: class %k doesn't have a _()V method.  Game over.\n", clazzNativeThread);
  }

  thread->top->jstack_top[0].c = (w_word) Thread;
  thread->top->jstack_top[0].s = stack_trace;
  thread->top->jstack_top += 1;
  thread->top->method = method;

  woempa(7, "Adding %t to %j\n", thread, I_ThreadGroup_system);
  addThreadCount(thread);
  newGlobalReference(thread->Thread);

  *p_env = w_thread2JNIEnv(thread);

  return 0;

}

/*
** Thankyou for visiting the Wonka factory.  Don't forget to give your
** badge to the security squirrel on the way out.
**
** TODO: figure out how to release all monitors held by a thread which
** is dumb enough to call this function from inside a monitor. 8-0
*/

jint DetachCurrentThread(JavaVM *vm) {
  w_thread thread = currentWonkaThread;

  if (isNotSet(thread->flags, WT_THREAD_IS_NATIVE)) {
    woempa(9, "Attempting to detach non-native thread %t\n", thread);
    return 0;
  }

  unsetFlag(thread->flags, WT_THREAD_IS_NATIVE);

  if (thread->Thread) {
    woempa(7, "Removing %t. It's no longer a native thread.\n", thread);
    removeThreadCount(thread);
    deleteGlobalReference(thread->Thread);
    clearWotsitField(thread->Thread, F_Thread_wotsit);
    thread->Thread = NULL;
  }
  ht_erase(thread_hashtable,(w_word)thread->kthread);

  if (thread->kthread) {
    pthread_setspecific(x_thread_key, 0);
    x_thread_detach(thread->kthread);
    releaseMem(thread->kthread);
  }  
  if (thread->kstack) {
    releaseMem(thread->kstack);
  }  
  if (thread->name) {
    deregisterString(thread->name);
  }
  // TODO: delete mutex
  releaseMem(thread);

  return 0;
}


jint GetEnv(JavaVM *vm, void **env, jint version) {
  x_thread kthread = x_thread_current();

  if(kthread) {
    w_thread thread = (w_thread)ht_read(thread_hashtable, (w_word)kthread);

    if (thread != NULL) {
      *env =  w_thread2JNIEnv(thread); 
      return JNI_OK;
    }
  }

  *env = NULL;
  return JNI_EDETACHED;
  
}


const struct JNINativeInterface w_JNINativeInterface = {

  NULL,
  NULL,
  NULL,
  NULL,
  GetVersion,
  
  DefineClass,
  FindClass,

  NULL,
  NULL,
  NULL,
  
  GetSuperclass,                        /* 10 */
  IsAssignableFrom,
  
  NULL,

  Throw,
  ThrowNew,
  ExceptionOccurred,
  ExceptionDescribe,
  ExceptionClear,
  FatalError,
  
  NULL,
  NULL,                                 /* 20 */
  
  NewGlobalRef,
  DeleteGlobalRef,
  DeleteLocalRef,
  IsSameObject,
  
  NewLocalRef,
  NULL,
  
  AllocObject,
  NewObject,
  NewObjectV,
  NewObjectA,                           /* 30 */

  GetObjectClass,
  IsInstanceOf,
  
  GetMethodID,

  CallObjectMethod,
  CallObjectMethodV,
  CallObjectMethodA,
  CallBooleanMethod,
  CallBooleanMethodV,
  CallBooleanMethodA,
  CallByteMethod,                       /* 40 */
  CallByteMethodV,
  CallByteMethodA,
  CallCharMethod,
  CallCharMethodV,
  CallCharMethodA,
  CallShortMethod,
  CallShortMethodV,
  CallShortMethodA,
  CallIntMethod,
  CallIntMethodV,                       /* 50 */
  CallIntMethodA,
  CallLongMethod,
  CallLongMethodV,
  CallLongMethodA,
  CallFloatMethod,
  CallFloatMethodV,
  CallFloatMethodA,
  CallDoubleMethod,
  CallDoubleMethodV,
  CallDoubleMethodA,                    /* 60 */
  CallVoidMethod,
  CallVoidMethodV,
  CallVoidMethodA,

  CallNonvirtualObjectMethod,
  CallNonvirtualObjectMethodV,
  CallNonvirtualObjectMethodA,
  CallNonvirtualBooleanMethod,
  CallNonvirtualBooleanMethodV,
  CallNonvirtualBooleanMethodA,
  CallNonvirtualByteMethod,             /* 70 */
  CallNonvirtualByteMethodV,
  CallNonvirtualByteMethodA,
  CallNonvirtualCharMethod,
  CallNonvirtualCharMethodV,
  CallNonvirtualCharMethodA,
  CallNonvirtualShortMethod,
  CallNonvirtualShortMethodV,
  CallNonvirtualShortMethodA,
  CallNonvirtualIntMethod,
  CallNonvirtualIntMethodV,             /* 80 */
  CallNonvirtualIntMethodA,
  CallNonvirtualLongMethod,
  CallNonvirtualLongMethodV,
  CallNonvirtualLongMethodA,
  CallNonvirtualFloatMethod,
  CallNonvirtualFloatMethodV,
  CallNonvirtualFloatMethodA,
  CallNonvirtualDoubleMethod,
  CallNonvirtualDoubleMethodV,
  CallNonvirtualDoubleMethodA,          /* 90 */
  CallNonvirtualVoidMethod,
  CallNonvirtualVoidMethodV,
  CallNonvirtualVoidMethodA,

  GetFieldID,

  GetObjectField,
  GetBooleanField,
  GetByteField,
  GetCharField,
  GetShortField,
  GetIntField,                          /* 100 */
  GetLongField,
  GetFloatField,
  GetDoubleField,
  SetObjectField,
  SetBooleanField,
  SetByteField,
  SetCharField,
  SetShortField,
  SetIntField,
  SetLongField,                         /* 110 */
  SetFloatField,
  SetDoubleField,

  GetStaticMethodID,

  CallStaticObjectMethod,
  CallStaticObjectMethodV,
  CallStaticObjectMethodA,
  CallStaticBooleanMethod,
  CallStaticBooleanMethodV,
  CallStaticBooleanMethodA,
  CallStaticByteMethod,                 /* 120 */
  CallStaticByteMethodV,
  CallStaticByteMethodA,
  CallStaticCharMethod,
  CallStaticCharMethodV,
  CallStaticCharMethodA,
  CallStaticShortMethod,
  CallStaticShortMethodV,
  CallStaticShortMethodA,
  CallStaticIntMethod,
  CallStaticIntMethodV,                 /* 130 */
  CallStaticIntMethodA,
  CallStaticLongMethod,
  CallStaticLongMethodV,
  CallStaticLongMethodA,
  CallStaticFloatMethod,
  CallStaticFloatMethodV,
  CallStaticFloatMethodA,
  CallStaticDoubleMethod,
  CallStaticDoubleMethodV,
  CallStaticDoubleMethodA,              /* 140 */
  CallStaticVoidMethod,
  CallStaticVoidMethodV,
  CallStaticVoidMethodA,

  GetStaticFieldID,

  GetStaticObjectField,
  GetStaticBooleanField,
  GetStaticByteField,
  GetStaticCharField,
  GetStaticShortField,
  GetStaticIntField,                    /* 150 */
  GetStaticLongField,
  GetStaticFloatField,
  GetStaticDoubleField,
  SetStaticObjectField,
  SetStaticBooleanField,
  SetStaticByteField,
  SetStaticCharField,
  SetStaticShortField,
  SetStaticIntField,
  SetStaticLongField,                   /* 160 */
  SetStaticFloatField,
  SetStaticDoubleField,

  NewString,
  GetStringLength,
  GetStringChars,
  ReleaseStringChars,

  NewStringUTF,
  GetStringUTFLength,
  GetStringUTFChars,
  ReleaseStringUTFChars,                /* 170 */

  GetArrayLength,
  
  NewObjectArray,
  GetObjectArrayElement,
  SetObjectArrayElement,

  NewBooleanArray,
  NewByteArray,
  NewCharArray,
  NewShortArray,
  NewIntArray,
  NewLongArray,                         /* 180 */
  NewFloatArray,
  NewDoubleArray,

  GetBooleanArrayElements,
  GetByteArrayElements,
  GetCharArrayElements,
  GetShortArrayElements,
  GetIntArrayElements,
  GetLongArrayElements,
  GetFloatArrayElements,
  GetDoubleArrayElements,               /* 190 */

  ReleaseBooleanArrayElements,
  ReleaseByteArrayElements,
  ReleaseCharArrayElements,
  ReleaseShortArrayElements,
  ReleaseIntArrayElements,
  ReleaseLongArrayElements,
  ReleaseFloatArrayElements,
  ReleaseDoubleArrayElements,

  GetBooleanArrayRegion,
  GetByteArrayRegion,                   /* 200 */
  GetCharArrayRegion,
  GetShortArrayRegion,
  GetIntArrayRegion,
  GetLongArrayRegion,
  GetFloatArrayRegion,
  GetDoubleArrayRegion,

  SetBooleanArrayRegion,
  SetByteArrayRegion,
  SetCharArrayRegion,
  SetShortArrayRegion,                  /* 210 */
  SetIntArrayRegion,
  SetLongArrayRegion,
  SetFloatArrayRegion,
  SetDoubleArrayRegion,

  RegisterNatives,
  UnregisterNatives,
  
  MonitorEnter,
  MonitorExit,

  GetJavaVM, //reserved219,
  GetStringRegion,           /* 220 */
  GetStringUTFRegion,
  GetPrimitiveArrayCritical,
  ReleasePrimitiveArrayCritical,
  GetStringCritical,
  ReleaseStringCritical,
  NULL, //NewWeakGlobalRef,
  NULL, //reserved227,
  ExceptionCheck,

};

const struct JNIInvokeInterface w_JNIInvokeInterface = {
  NULL,
  NULL,
  NULL,
  DestroyJavaVM,
  AttachCurrentThread,
  DetachCurrentThread,
  GetEnv,
  NULL,
};

