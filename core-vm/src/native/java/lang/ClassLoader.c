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
** $Id: ClassLoader.c,v 1.14 2006/10/04 14:24:16 cvsroot Exp $
*/

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
    woempa(7, "Created %j by recycling %s and %s)\n", ClassLoader, system_loaded_class_hashtable->label, system_unloaded_class_hashtable->label);
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

  clazz = createClazz(thread, name, &bar, thisClassLoader, trusted);
 
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
    wprintf("Load %w: defined by %j in thread %t\n", name, thisClassLoader, thread);
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

w_instance ClassLoader_findLoadedClass(JNIEnv *env, w_instance This, w_instance nameString) {

  w_clazz clazz;
  w_string name;

  if (nameString) {
    name = String2string(nameString);
    woempa(1, "called with string %w.\n", name);

    clazz = seekClazzByName(name, This);

    if (clazz) {
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
    x_monitor_eternal(&system_loaded_class_hashtable->monitor);
    clazz = loadBootstrapClass(name);
    x_monitor_exit(&system_loaded_class_hashtable->monitor);
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
** Load the named class, and "resolve" it if resolve==true.
*/
w_instance ClassLoader_loadClass(JNIEnv *env, w_instance thisClassLoader, w_instance nameString, w_boolean resolve) {

  w_thread thread = JNIEnv2w_thread(env);
  w_clazz  clazz;
  w_string name = String2string(nameString);

  woempa(1, "Loading class %w.\n", name);
  
  woempa(7, "Loader: %j, name: %w\n", NULL, name);
  clazz = namedClassMustBeLoaded(NULL, name);

  if (clazz) {
    clazz2Class(clazz);  

    if (resolve) {
      mustBeLinked(clazz);
    }

    woempa(1, "--> clazz at %p.\n", clazz);
    return exceptionThrown(thread) ? NULL : clazz->Class;
  }

  return NULL;
}

/*
** Returns the class loader of the class which called ClassLoader.getCallingClassLoader().
**
** This logic is also needed by the java.util.ResourceBundle Class's getBundle(...)
** it will call this method straigth from java so w_instance pointer will not always be the Class of ClassLoader
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
  Result = newStringInstance(result);
  deregisterString(result);

  return Result;

}

void ClassLoader_static_installExtensionClassLoader(JNIEnv *env, w_instance classClassLoader, w_instance theExtensionClassLoader) {
  extensionClassLoader = theExtensionClassLoader;
}

void ClassLoader_static_setProxyFlag(JNIEnv *env, w_instance classClassLoader, w_instance theClass) {
  w_clazz clazz = Class2clazz(theClass);
  setFlag(clazz->flags, CLAZZ_IS_PROXY);
}


