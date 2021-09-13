/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2004, 2005, 2006, 2010 by Chris Gray, /k/ Embedded  *
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
#include "bar.h"
#include "chars.h"
#include "clazz.h"
#include "core-classes.h"
#include "fields.h"
#include "exception.h"
#include "hashtable.h"
#include "heap.h"
#include "list.h"
#include "locks.h"
#include "loading.h"
#include "methods.h"
#include "oswald.h"
#include "wstrings.h"
#include "ts-mem.h"
#include "verifier.h"
#include "vfs.h"

extern Wonka_InitArgs *system_vm_args;

w_instance extensionClassLoader;
w_instance applicationClassLoader;

#define PACKAGE_HASHTABLE_SIZE           19
#define CLASSLOADER_HASHTABLE_SIZE       89

static w_boolean checkClassName(w_string slashed) {
  w_boolean start = WONKA_TRUE;
  w_size l = string_length(slashed);
  w_size i;
  w_char ch;

  for (i = 0; i < l; ++i) {
    ch = string_char(slashed, i);
    if (ch == '.') {
      start = WONKA_TRUE;
    }
    else if (start) {
      if (charIsJavaIdentifierStart(ch)) {
        start = WONKA_FALSE;
      }
      else {
        return WONKA_FALSE;
      }
    }
    else {
      if (!charIsJavaIdentifierPart(ch)) {
        return WONKA_FALSE;
      }
    }
  }

  return WONKA_TRUE;
}

void ClassLoader_create(JNIEnv *env, w_instance ClassLoader) {

  w_clazz  clazz = instance2clazz(ClassLoader);
  w_hashtable class_hashtable;
  char   * label_buf;

  if (clazz == clazzSystemClassLoader) {
    setWotsitField(ClassLoader, F_ClassLoader_loaded_classes, system_loaded_class_hashtable);
    setWotsitField(ClassLoader, F_ClassLoader_unloaded_classes, system_unloaded_class_hashtable);
    setWotsitField(ClassLoader, F_ClassLoader_packages, system_package_hashtable);
    woempa(7, "Created %j by recycling %s, %s and %s)\n", ClassLoader, system_loaded_class_hashtable->label, system_unloaded_class_hashtable->label, system_package_hashtable->label);
  }
  else {
    woempa(7, "Created new %j\n", ClassLoader);
    label_buf = allocMem((w_size)(string_length(clazz->dotified) + 44));
    if (!label_buf) {
      wabort(ABORT_WONKA, "Unable to allocate label_buf\n");
    }
    x_snprintf(label_buf, string_length(clazz->dotified) + 43, "hashtable:%j-loaded-classes", ClassLoader);
    class_hashtable = ht_create(label_buf, CLASSLOADER_HASHTABLE_SIZE, NULL, NULL, 0, 0);
    if (!class_hashtable) {
      wabort(ABORT_WONKA, "Unable to allocate loaded class hashtable\n");
    }
    setWotsitField(ClassLoader, F_ClassLoader_loaded_classes, class_hashtable);
    woempa(7, "%j: loaded_classes in %s\n", ClassLoader, class_hashtable->label);
    label_buf = allocMem((w_size)(string_length(clazz->dotified) + 46));
    if (!label_buf) {
      wabort(ABORT_WONKA, "Unable to allocate label_buf\n");
    }
    x_snprintf(label_buf, string_length(clazz->dotified) + 45, "hashtable:%j-unloaded-classes", ClassLoader);
    class_hashtable = ht_create(label_buf, CLASSLOADER_HASHTABLE_SIZE, clazz_hashcode, clazz_comparator, 0, 0);
    if (!class_hashtable) {
      wabort(ABORT_WONKA, "Unable to allocate unloaded class hashtable\n");
    }
    setWotsitField(ClassLoader, F_ClassLoader_unloaded_classes, class_hashtable);
    woempa(7, "%j: unloaded_classes in %s\n", ClassLoader, class_hashtable->label);
    label_buf = allocMem((w_size)(string_length(clazz->dotified) + 38));
    if (!label_buf) {
      wabort(ABORT_WONKA, "Unable to allocate label_buf\n");
    }
    x_snprintf(label_buf, string_length(clazz->dotified) + 37, "hashtable:%j-packages", ClassLoader);
    class_hashtable = ht_create(label_buf, PACKAGE_HASHTABLE_SIZE, NULL, NULL, 0, 0);
    if (!class_hashtable) {
      wabort(ABORT_WONKA, "Unable to allocate package hashtable\n");
    }
    setWotsitField(ClassLoader, F_ClassLoader_packages, class_hashtable);
    woempa(7, "%j: packages in %s\n", ClassLoader, class_hashtable->label);
  }
}

w_boolean ClassLoader_checkClassName(JNIEnv *env, w_instance This, w_instance nameString) {
  w_string name = String2string(nameString);

  return !name || checkClassName(name);
}

w_instance ClassLoader_defineClass(JNIEnv *env, w_instance thisClassLoader, w_instance nameString, w_instance Data, w_int offset, w_int length, w_instance pd) {

  w_thread thread = JNIEnv2w_thread(env);
  w_byte    *data = instance2Array_byte(Data);
  w_clazz    clazz;
  w_instance theClass;
  w_BAR bar;
  w_string name;
  w_boolean trusted = (instance2clazz(thisClassLoader) == clazzSystemClassLoader);
  // TODO make configurable, e.g. trust no one or also trust extension classes.

  woempa(1,"installing %w from %p (thread %w)\n",String2string(nameString),data,NM(thread));

  bar.buffer = data + offset;
  bar.length = length;
  bar.current = 0;

  if (nameString) {
    name = String2string(nameString);
    if (!checkClassName(name)) {
      throwException(thread, clazzClassFormatError, "Illegal class name: %w", name);

      return NULL;

    }
  }
  else {
    name = NULL;
  }

  if (!loader2loaded_classes(thisClassLoader)) {
    throwException(thread, clazzSecurityException, "%j is not fully initialised", thisClassLoader);
    return NULL;
  }

  enterMonitor(thisClassLoader);
  clazz = createClazz(thread, name, &bar, thisClassLoader, trusted);
  exitMonitor(thisClassLoader);
 
  if (!clazz && !exceptionThrown(thread)) {
    throwException(thread, clazzClassFormatError, "%w", name);
  }

  if (exceptionThrown(thread)) {
    return NULL;
  }

  theClass = clazz2Class(clazz);
  setReferenceField(theClass, pd, F_Class_domain);
  setReferenceField(theClass, thisClassLoader, F_Class_loader);

  if (isSet(verbose_flags, VERBOSE_FLAG_LOAD)) {
    w_printf("Load %w: defined by %j in thread %t\n", name, thisClassLoader, thread);
  }

  return theClass;

}

void ClassLoader_resolveClass(JNIEnv *env, w_instance This, w_instance theClass) {

  w_thread thread = JNIEnv2w_thread(env);
  w_clazz  clazz = Class2clazz(theClass);

  if (theClass) {
    mustBeLinked(clazz);
    if (exceptionThrown(thread)) {
      return;
    }
  }
  else {
    throwException(thread, clazzNullPointerException, NULL);
  }

}

w_instance ClassLoader_findLoadedClass(JNIEnv *env, w_instance thisClassLoader, w_instance nameString) {

  w_clazz clazz;
  w_string name;

  if (!loader2loaded_classes(thisClassLoader)) {
    w_thread thread = JNIEnv2w_thread(env);
    throwException(thread, clazzSecurityException, "%j is not fully initialised", thisClassLoader);
  }
  else if (nameString) {
    name = String2string(nameString);

    clazz = seekClazzByName(name, thisClassLoader);

    if (clazz) {
      w_int state = getClazzState(clazz);
      if (state <= CLAZZ_STATE_LOADING || state > CLAZZ_STATE_INITIALIZED) {

        return NULL;

      }

      return clazz2Class(clazz);
    } 
  }

  return NULL;

}

w_instance ClassLoader_findBootstrapClass(JNIEnv *env, w_instance This, w_instance nameString) {

  w_thread thread = JNIEnv2w_thread(env);
  w_clazz clazz;
  w_string name;

  if (!nameString) {
    throwException(thread, clazzNullPointerException, "name is null");
    return NULL;
  }

  name = String2string(nameString);
  woempa(1, "called with string %w.\n", name);

  clazz = seekClazzByName(name, NULL);

  if (!clazz) {
    threadMustBeSafe(thread);
    ht_lock(system_loaded_class_hashtable);
    clazz = loadBootstrapClass(name);
    ht_unlock(system_loaded_class_hashtable);
  }

  return clazz ? clazz2Class(clazz) : NULL;

}


void
ClassLoader_setSigners
( JNIEnv *env, w_instance This,
  w_instance theClass, w_instance theObjectArray
) {
  // TODO 
}

/*
** Returns the class loader of the class which called ClassLoader.getCallingClassLoader().
**
** This logic is also needed by the java.util.ResourceBundle Class's getBundle(...)
** it will call this method straight from java so w_instance pointer will not always be the Class of ClassLoader
*/

w_instance ClassLoader_getCallingClassLoader(JNIEnv *env, w_instance thisClassLoader) {
  w_thread thread = JNIEnv2w_thread(env);
  w_clazz clazz = getCallingClazz(thread);
  
  return clazz->loader;
}

w_instance ClassLoader_getCallingCallingClassLoader(JNIEnv *env, w_instance thisClassLoader) {
  w_thread thread = JNIEnv2w_thread(env);
  w_frame frame = thread->top;
  int count = 3;
  
  while (count && frame) {
    if (frame->method) {
      count -= 1;
    }
    frame = frame->previous;
  }

  if (frame && frame->method) {
    return frame->method->spec.declaring_clazz->loader;
  }
  else {
    return NULL;
  }
}

/*
** Get the -classpath or -cp parameter from the command line (if any) and
** make it into an instance of java.lang.String. 
*/

w_instance ClassLoader_getCommandLineClasspath(JNIEnv *env, w_instance class) {
  w_instance Result;
  w_string result;
  result = cstring2String(system_vm_args->classpath, strlen(system_vm_args->classpath));
  Result = getStringInstance(result);
  deregisterString(result);

  return Result;

}

void ClassLoader_static_installExtensionClassLoader(JNIEnv *env, w_instance classClassLoader, w_instance theExtensionClassLoader) {
  extensionClassLoader = theExtensionClassLoader;
}

void ClassLoader_static_installApplicationClassLoader(JNIEnv *env, w_instance classClassLoader, w_instance theApplicationClassLoader) {
  applicationClassLoader = theApplicationClassLoader;
}

void ClassLoader_static_setProxyFlag(JNIEnv *env, w_instance classClassLoader, w_instance theClass) {
  w_clazz clazz = Class2clazz(theClass);
  setFlag(clazz->flags, CLAZZ_IS_PROXY);
}


