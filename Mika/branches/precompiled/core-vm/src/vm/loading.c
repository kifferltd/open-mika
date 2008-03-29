/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved.                                                               *
* Parts copyright (c) 2004, 2005, 2006, 2007, 2008 by Chris Gray,         *
* /k/ Embedded Java Solutions. All rights reserved.                       *
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

#include "argument.h"
#include "bar.h"
#include "clazz.h"
#include "checks.h"
#include "descriptor.h"
#include "exception.h"
#include "interpreter.h"
#include "loading.h"
#include "locks.h"
#include "methods.h"
#include "reflection.h"
#include "ts-mem.h"
#include "wstrings.h"
#include "wonka.h"
#include "fastcall.h"

#ifdef USE_ZLIB
#include "unzip.h"
#include "zutil.h"
#else
#include "zipfile.h"
#endif

extern char *fsroot;

w_instance systemClassLoader = NULL;

/*
** Two hashtables which map class names onto ``fixup'' functions.
*/
w_hashtable fixup1_hashtable;
w_hashtable fixup2_hashtable;

/*
** The initial size of \texttt{fixup_hashtable}
*/
#define NATIVE_FUN_HT_SIZE 37

/*
** A hashtable which maps class x imethod -> method
*/
w_hashtable2k interface_hashtable;

/*
** The initial size of \texttt{interface_hashtable}
*/
#define INTERFACE_HT_SIZE 101

// char     *bootdirname;
char     *bootzipname;
#ifdef USE_ZLIB
unzFile bootzipfile;
#else
z_zipFile bootzipfile;
#endif

/*
** Some well-known strings that are required during loading.
** They are registered in startLoading().
*/
// Names of primitive classes
w_string string_boolean;
w_string string_byte;
w_string string_c_h_a_r;
w_string string_double;
w_string string_float;
w_string string_int;
w_string string_long;
w_string string_short;
w_string string_void;

// Names of classfile attributes
w_string string_ConstantValue;
w_string string_Code;
w_string string_serialVersionUID;
w_string string_InnerClasses;
w_string string_Deprecated;
w_string string_Exceptions;
w_string string_InnerClasses;
w_string string_LineNumberTable;
w_string string_LocalVariableTable;
w_string string_Reference;
w_string string_SourceFile;
w_string string_Synthetic;
#ifdef SUPPORT_BYTECODE_SCRAMBLING
w_string string_be_kiffer_Scrambled;
#endif

// Names and signatures of special methods
w_string string_run;
w_string string_finalize;
w_string string_loadClass;
w_string string_angle_brackets_init;
w_string string_angle_brackets_clinit;
w_string string_no_params_V;
w_string string_no_params;
w_string string_params_String_return_Class;
w_string string_L_java_lang_String;

// The "name" of the bootstrap class loader
w_string string_bootstrap;

w_clazz atype2clazz[12];

w_clazz createPrimitiveArrayClazz(w_int pi);

static inline w_string loader2name(w_instance loader) {
  if (loader) {

    return String2string(getReferenceField(loader, F_ClassLoader_ownname));

  }

  return string_bootstrap;
}

/*
** Compare two w_Clazz pointers.
*/
w_boolean sameClazz(w_clazz clazz1, w_clazz clazz2) {
  w_boolean result = WONKA_FALSE;

  /*
  ** Do a quick check first, when the two memory addresses match, the clazzes match.
  ** Otherwise, compare the 'name ' and 'loader' fields.
  */

  woempa(1, "Comparing {%j}%w (%p) with {%j}%w (%p)\n", clazz1->loader, clazz1->dotified, clazz1, clazz2->loader, clazz2->dotified, clazz2);
  if (clazz1 == clazz2) {
    woempa(1, "Identical, return TRUE\n");
    result = WONKA_TRUE;
  }
  else if (!!getClazzState(clazz1) == !!getClazzState(clazz2)
        && clazz1->loader == clazz2->loader
        && clazz1->dotified == clazz2->dotified) {
    woempa(1, "Same loadedness, loader, same name, return TRUE\n");
    result = WONKA_TRUE;
  }

  woempa(1, "Compared {%j}%w (%p) with {%j}%w (%p): they are %s\n", clazz1->loader, clazz1->dotified, clazz1, clazz2->loader, clazz2->dotified, clazz2, result ? "the same" : "different");

  return result;

}

/*
** Compare two pointers to w_Clazz (or w_UnloadedClazz) pointers.
** [CG 20061201]
** Always load both classes, it saves a whole lot of pain when we try to
** unload a class and we need to untangle its references to other classes.
*/
w_boolean sameClassReference(w_clazz *clazzptr1, w_clazz *clazzptr2) {
/* [CG 20061201] see above
  if (*clazzptr1 == *clazzptr2) {

    return WONKA_TRUE;

  }
*/

  if ((*clazzptr1)->dotified != (*clazzptr2)->dotified) {

    return WONKA_FALSE;

  }

/* [CG 20061201] see above
  if ((getClazzState(*clazzptr1) == CLAZZ_STATE_UNLOADED) && (getClazzState(*clazzptr2) == CLAZZ_STATE_UNLOADED) && (*clazzptr1)->loader == (*clazzptr2)->loader) {

    return WONKA_TRUE;

  }
*/

  if (mustBeLoaded(clazzptr1) == CLASS_LOADING_FAILED) {
    woempa(7, "Failed to load %K\n", *clazzptr1);

    return WONKA_FALSE;

  }
  else {
    woempa(1, "*clazzptr1 is %K\n", *clazzptr1);
  }

  if (mustBeLoaded(clazzptr2) == CLASS_LOADING_FAILED) {
    woempa(7, "Failed to load %K\n", *clazzptr2);

    return WONKA_FALSE;

  }
  else {
    woempa(1, "*clazzptr2 is %K\n", *clazzptr2);
  }

  return *clazzptr1 == *clazzptr2;

}

/*
** For use in hashtables.
*/
w_boolean clazz_comparator(w_word clazz1_word, w_word clazz2_word) {
  return sameClazz((w_clazz)clazz1_word, (w_clazz)clazz2_word);
}

/*
** Hashcode for a w_UnloadedClass.
*/
w_word clazz_hashcode(w_word clazz_word) {
  w_clazz clazz = (w_clazz)clazz_word;

  return (w_word)clazz->dotified ^ (w_word)clazz->loader;

}



/*
** Register a w_UnloadedClazz. The result returned is a w_UnloadedClazz*
** which may or not be the same as the one passed as input: if it is
** different, then the one passed as input has been released.
*/

w_clazz registerUnloadedClazz(w_clazz clazz) {
 
  w_clazz result;
  w_hashtable hashtable = loader2unloaded_classes(clazz->loader);

  woempa(1, "Registering %k at %p with %s\n", clazz, clazz, hashtable->label);
  result = (w_clazz)ht_register(hashtable, (w_word)clazz);
  if (result) {
    if (result != clazz) {
      woempa(1, "Found %k at %p, releasing %p\n", result, result, clazz);
      releaseMem(clazz);
    }
    else {
      woempa(1, "Found %k at %p already\n", result,result);
    }
  }
  else {
    woempa(1,"Adding %k at %p\n", clazz, clazz);
    result = clazz;
  }

  return result;
  
}

/*
** Deregister a w_UnloadedClazz. When we see that the registered count drops
** to 0, we release the w_UnloadedClazz structure.
*/

void deregisterUnloadedClazz(w_clazz clazz) {
  w_clazz garbage = NULL;
  w_hashtable hashtable = loader2unloaded_classes(clazz->loader);

  woempa(1, "Deregistering %k\n", clazz);
  garbage = (w_clazz)ht_deregister(hashtable, (w_word)clazz);
  if (garbage) {
    woempa(7,"Releasing %K (%p)\n",garbage,garbage);
    deregisterString(garbage->dotified);
    releaseMem(garbage);
  }
  else {
    woempa(1, "Retaining %k (%p), as still %d reference(s)\n", clazz, clazz, ht_read(hashtable, (w_word)clazz));
  }
}

/*
** If a class with the given name is already known to initiating_loader,
** return a pointer to the corresponding w_Clazz structure. Otherwise,
** construct a w_UnloadedClazz structure which records the name and 
** initiating classloader.
*/
w_clazz identifyClazz(w_string name, w_instance initiating_loader) {
  w_clazz result = NULL;

  result = (w_clazz)ht_read_no_lock(loader2loaded_classes(initiating_loader), (w_word)name);

  if (!result) {
    result = allocMem(sizeof(w_UnloadedClazz));
    if (!result) {
      wabort(ABORT_WONKA, "Unable to allocate new UnloadedClazz\n");
    }

    result->dotified = registerString(name);
    result->label    = (char *)"clazz(unloaded)";
    result->flags    = 0; 
    result->loader   = initiating_loader;

    result = registerUnloadedClazz(result);
  }

  return result;
}

/*
** If *clazzptr is already loaded, do nothing; otherwise try to load the class.
** If nothing is done, returns CLASS_LOADING_DID_NOTHING.
** If class was not loaded and loading succeeds, updates *clazzptr
** and returns CLASS_LOADING_SUCCEEDED.
** If If class was not loaded and loading failed, returns
** CLASS_LOADING_FAILED. A NoClassDefFoundError or other Error will be pending 
** on the current thread.
*/
w_int mustBeLoaded(volatile w_clazz *clazzptr) {
  w_thread  thread = currentWonkaThread;
  w_int     result = CLASS_LOADING_DID_NOTHING;
  w_clazz   current = *clazzptr;
  w_int     state = getClazzState(current);
  x_monitor monitor;
  x_status  status;

  if (state == CLAZZ_STATE_UNLOADED) {
    w_clazz loaded;

    if (current->loader) {
      woempa(1, "Need to load class %p (%w) using loader %j (%w)\n", current, current->dotified, current->loader, loader2name(current->loader));
    }
    else {
      woempa(1, "Need to load class %p (%w) using bootstrap class loader\n", current, current->dotified);
    }
    loaded = namedClassMustBeLoaded(current->loader, current->dotified);
    if (!loaded) {
      w_instance exception = exceptionThrown(thread);

      if (!exception) {
        throwException(thread, clazzNoClassDefFoundError, "%w", current->dotified);
      }
      else if (isAssignmentCompatible(instance2object(exception)->clazz, clazzException)) {
        wrapException(thread,clazzNoClassDefFoundError, F_Throwable_cause);
      }

      result = CLASS_LOADING_FAILED;
    }
    else {
      result = CLASS_LOADING_SUCCEEDED;
      woempa(1, "Replacing %p with %p\n", current, loaded);
      deregisterUnloadedClazz(current);
      *clazzptr = loaded;
    }
  }
  else if (state < CLAZZ_STATE_LOADED) {
    threadMustBeSafe(thread);

    monitor = current->resolution_monitor;

    x_monitor_eternal(monitor);
    while(state < CLAZZ_STATE_LOADED) {
      woempa(1, "Another thread (%t) is loading %k (%p), %t waiting\n", current->resolution_thread, current, current, thread);
      status = x_monitor_wait(monitor, CLASS_STATE_WAIT_TICKS);
      if (status == xs_interrupted) {
        x_monitor_eternal(monitor);
      }
      state = getClazzState(current);
    }
    x_monitor_exit(monitor);
  }

  if (current && getClazzState(current) == CLAZZ_STATE_BROKEN) {
    throwException(thread, clazzNoClassDefFoundError, "%w", current->failure_message);
    result = CLASS_LOADING_FAILED;
  }

  return result;
}

/*
static void printBootstrapClass(w_word key, w_word value) {
  w_string name = (w_string) key;
  wprintf("  %w\n", name);
}
*/

/*
** Set the systemClassLoader global variable. From now on the System
** Class Loader must be used for all system classes.
*/
void setSystemClassLoader(w_instance scl) {
//  w_int n;
  x_monitor_eternal(&system_loaded_class_hashtable->monitor);
/*
  wprintf("Bootstrap classes:\n");
  n = ht_every(system_loaded_class_hashtable, printBootstrapClass);
  wprintf("Total of %d bootstrap classes\n\n", n);
*/
  if (systemClassLoader) {
    woempa(9, "Ahoy there! Someone tried to install SystemClassLoader twice ...\n");
  }
  else {
    woempa(7, "*** SystemClassLoader created, instance is %p ***\n", scl);
    systemClassLoader = scl;
    newGlobalReference(scl);
    //releaseZipFile(bootzipfile);
  }
  x_monitor_exit(&system_loaded_class_hashtable->monitor);
}

#ifdef JDWP
/*
** Execute a given function for every ClassLoader registered with the system.
** Uses the static Vector JDWP.refsToClassLoaders, which contains weak references
** to class loaders. Returns a w_fifo which contains an entry for each class
** loader processed, or NULL if no ClassLoader was found.
** Note: only used by JDWP, and the Vector is only maintained if JDWP is enabled.
*/
w_fifo forEachClassLoader(void* (*fun)(w_instance)) {
  w_instance refsToClassLoaders = getStaticReferenceField(clazzJDWP, F_JDWP_refsToClassLoaders);
  w_instance elementData;
  w_instance *weakrefs;
  w_int elementCount;
  w_fifo outer_fifo = NULL;
  w_int i;

  if (refsToClassLoaders && (elementData = getReferenceField(refsToClassLoaders, F_Vector_elementData))) {
    weakrefs = instance2Array_instance(elementData);
    elementCount = getIntegerField(refsToClassLoaders, F_Vector_elementCount);
    outer_fifo = allocFifo(63);

    for (i = 0; i < elementCount; ++i) {
      w_instance ref = *weakrefs++;
      w_instance classloader = getWotsitField(ref, F_Reference_referent);

      if (classloader) {
        w_fifo inner_fifo = fun(classloader);

        if (inner_fifo) {
          putFifo(inner_fifo, outer_fifo);
        }
      }
    }
  }

  return outer_fifo;
}
#endif

/*
** Create the w_Clazz structure for a given primitive type, given its name
** (e.g. "boolean"), VM_TYPE (e.g. VM_TYPE_BOOLEAN) and the number of bits 
** needed to store it (0, 1, 8, 16, 32, or 64).
*/
static w_clazz createPrimitive(w_string name, w_ubyte type, w_int bits) {
  w_clazz clazz = allocClazz();

  clazz->dotified = registerString(name);
  clazz->flags = CLAZZ_IS_PRIMITIVE | ACC_FINAL | ACC_PUBLIC | (CLAZZ_STATE_LOADED << CLAZZ_STATE_SHIFT);
  clazz->loader = NULL;
  clazz->type = type;
  clazz->bits = bits;
  clazz->resolution_monitor = allocMem(sizeof(x_Monitor));
  if (!clazz->resolution_monitor) {
    wabort(ABORT_WONKA, "Unable to allocate clazz->resolution_monitor\n");
  }
  x_monitor_create(clazz->resolution_monitor);
  woempa(1, "Created clazz_%w @ %p\n", name, clazz);

  return clazz;
}

static void preparePrimitive(w_clazz clazz, w_string string, w_int pi) {
  woempa(1, "Preparing primitive class `%w'\n", string);
  if (pi != P_void) {
    atype2clazz[pi] = createPrimitiveArrayClazz(pi);
  }
  setClazzState(clazz, CLAZZ_STATE_INITIALIZED);
  clazz->Class = NULL;
  primitive2clazz[pi] = clazz;
  primitive2clazz[pi]->nextDimension = atype2clazz[pi];

  registerClazz(NULL, clazz, NULL);
  woempa(1, "Prepared primitive class `%w'\n", string);
}

w_method finalize_method;
w_method loadClass_method;

static void wabort_missing_method(const char *classname, const char *methodname) {
    wabort(ABORT_WONKA, "Oops - %s doesn't have a %s method\n", classname, methodname);
}

static void identify_special_methods(void) {
  w_method m;
  w_size   i;

  for (i = 0; i < clazzObject->numDeclaredMethods; i++) {
    m = &clazzObject->own_methods[i];
    if (m->spec.name == string_finalize && m->desc == string_no_params_V) {
      woempa(1, "Class %k has finalizer %m\n", clazzObject, m);
      finalize_method = m;
    }
  }
  if (!finalize_method) {
    wabort_missing_method("java.lang.Object", "finalize()");
  }

  for (i = 0; i < clazzClassLoader->numDeclaredMethods; i++) {
    m = &clazzClassLoader->own_methods[i];
    if (m->spec.name == string_loadClass && m->desc == string_params_String_return_Class) {
      woempa(7, "Class %k has loadClass method %m\n", clazzClassLoader, m);
      loadClass_method = m;
    }
  }
  if (!loadClass_method) {
    wabort_missing_method("java.lang.ClassLoader", "loadClass(String,boolean)");
  }
}

static w_boolean attach_class_iteration(void * name, void * cl) {
  w_clazz clazz = cl;

  if (!clazz->Class) {
    attachClassInstance(clazz, NULL);
  }
}

static void attach_class_instances(void) {
  ht_iterate(system_loaded_class_hashtable, attach_class_iteration, NULL, NULL);
}

/*
** Search colon-separated list bcp and extract the first string which ends
** in `.jar' or `.zip'.
** The string is returned in allocMem'd memory, bcp is not modified.
*/
static char *getFirstJarFileName(char *bcp) {
  char *firstzipname = NULL;

  if (bcp) {
    w_int i = 0;
    w_int j = 0;
    w_int l = strlen(bcp);
    woempa(7, "bootclasspath is %s, length is %d\n", bcp, l);
    while (bcp[i]) {
      for (j = i; j < l && bcp[j] != ':'; ++j);
      woempa(7, "Element begins at bcp[%d], ends at bcp[%d]\n", i, j);
      if (j > i + 4 && bcp[j - 4] == '.' && bcp[j - 3] == 'j' && bcp[j - 2] == 'a' && bcp[j - 1] == 'r') {
        woempa(7, "Element ends in `jar', so we use bcp[%d..%d]\n", i, j - 1);

        break;

      }
      if (j > i + 4 && bcp[j - 4] == '.' && bcp[j - 3] == 'z' && bcp[j - 2] == 'i' && bcp[j - 1] == 'p') {
        woempa(7, "Element ends in `zip', so we use bcp[%d..%d]\n", i, j - 1);

        break;

      }

      if (j == l) {
        woempa(7, "Element does not end with `.jar' or `.zip' and bootclasspath exhausted\n");

        break;

      }

      woempa(7, "Element does not end with `jar' or `zip', start again from bcp[%d]\n", j + 1);
      i = j + 1;
    }

    if (i < l) {
      w_int k = 0;
      if ((l - i > 2 ) && (bcp[i] == '{') && (bcp[i + 1] == '}') && bcp[i + 2] == '/') {
        woempa(7, "Element starts with '{}/', replace by '%s/'\n", fsroot);
        k = strlen(fsroot) - 2;
      }
      woempa(7, "Allocating %d bytes for firstzipname\n", j - i + k + 1);
      firstzipname = allocClearedMem(j - i + k + 1);
      if (k) {
        strncpy(firstzipname, fsroot, strlen(fsroot));
        strncpy(firstzipname + k + 2, &bcp[i + 2], j - i - 2);
        firstzipname[j - i + k] = 0;
      }
      else {
        strncpy(firstzipname, &bcp[i], j - i);
        firstzipname[j - i] = 0;
      }
      woempa(7, "firstzipname = '%s'\n", firstzipname);
    }
  }

  return firstzipname ? firstzipname : (char*)""; // TODO : think of a better default?
}

void startLoading(void) {
  w_string string_java_lang_Object = cstring2String("java.lang.Object", 16);
  w_string string_java_lang_Cloneable = cstring2String("java.lang.Cloneable", 19);
  w_string string_java_io_Serializable = cstring2String("java.io.Serializable", 20);
  w_string string_java_lang_Throwable = cstring2String("java.lang.Throwable", 19);
  w_string string_java_lang_Class = cstring2String("java.lang.Class", 15);
  w_string string_java_lang_ClassLoader = cstring2String("java.lang.ClassLoader", 21);
  w_string string_java_lang_Thread = cstring2String("java.lang.Thread", 16);
  w_string string_java_lang_ThreadGroup = cstring2String("java.lang.ThreadGroup", 21);
  w_string string_java_lang_ref_Reference = cstring2String("java.lang.ref.Reference", 23);
  w_string string_java_lang_reflect_Constructor = cstring2String("java.lang.reflect.Constructor", 29);

  string_boolean = cstring2String("boolean", 7);
  string_byte = cstring2String("byte", 4);
  string_c_h_a_r = cstring2String("char", 4);
  string_double = cstring2String("double", 6);
  string_float = cstring2String("float", 5);
  string_int = cstring2String("int", 3);
  string_long = cstring2String("long", 4);
  string_short = cstring2String("short", 5);
  string_void = cstring2String("void", 4);

  string_ConstantValue = cstring2String("ConstantValue", 13);
  string_Code = cstring2String("Code", 4);
  string_serialVersionUID = cstring2String("serialVersionUID", 16);
  string_InnerClasses = cstring2String("InnerClasses", 12);
  string_Deprecated = cstring2String("Deprecated", 10);
  string_Exceptions = cstring2String("Exceptions", 10);
  string_LineNumberTable = cstring2String("LineNumberTable", 15);
  string_LocalVariableTable = cstring2String("LocalVariableTable", 18);
  string_Reference = cstring2String("java/lang/ref/Reference", 23);
  string_SourceFile = cstring2String("SourceFile", 10);
  string_Synthetic = cstring2String("Synthetic", 9);

#ifdef SUPPORT_BYTECODE_SCRAMBLING
  string_be_kiffer_Scrambled = cstring2String("be.kiffer.Scrambled", 19);
#endif
  string_run = cstring2String("run", 3);
  string_finalize = cstring2String("finalize", 8);
  string_angle_brackets_init = cstring2String("<init>", 6);
  string_angle_brackets_clinit = cstring2String("<clinit>", 8);
  string_loadClass = cstring2String("loadClass", 9);
  string_no_params = cstring2String("()", 2);
  string_no_params_V = cstring2String("()V", 3);
  string_params_String_return_Class = cstring2String("(Ljava/lang/String;)Ljava/lang/Class;", 37);
  string_L_java_lang_String = cstring2String("Ljava/lang/String;", 18);

  fastcall_init_tables();

  string_bootstrap = cstring2String("bootstrap class loader", 22);

  clazz_boolean = createPrimitive(string_boolean, VM_TYPE_BOOLEAN, 1);
  clazz_char    = createPrimitive(string_c_h_a_r, VM_TYPE_CHAR, 16);
  clazz_float   = createPrimitive(string_float, VM_TYPE_FLOAT, 32);
  clazz_double  = createPrimitive(string_double, VM_TYPE_DOUBLE + VM_TYPE_TWO_CELL, 64);
  clazz_byte    = createPrimitive(string_byte, VM_TYPE_BYTE, 8);
  clazz_short   = createPrimitive(string_short, VM_TYPE_SHORT, 16);
  clazz_int     = createPrimitive(string_int, VM_TYPE_INT, 32);
  clazz_long    = createPrimitive(string_long, VM_TYPE_LONG + VM_TYPE_TWO_CELL, 64);
  clazz_void    = createPrimitive(string_void, VM_TYPE_VOID, 0);

  bootzipname = getFirstJarFileName(bootclasspath);
  woempa(7, "Using boot zipfile `%s'\n", bootzipname);
#ifdef USE_ZLIB
  bootzipfile = unzOpen(bootzipname);
#else
  bootzipfile = parseZipFile(bootzipname);
#endif
  if (!bootzipfile) {
    wabort(ABORT_WONKA, "Unable to open zipfile `%s'\n", bootzipname);
  }

  fixup1_hashtable = ht_create((char*)"hashtable:fixup1", NATIVE_FUN_HT_SIZE, NULL , NULL, 0, 0);
  fixup2_hashtable = ht_create((char*)"hashtable:fixup2", NATIVE_FUN_HT_SIZE, NULL , NULL, 0, 0);
  interface_hashtable = ht2k_create((char*)"hashtable:interfaces",INTERFACE_HT_SIZE);

  system_loaded_class_hashtable = ht_create((char*)"hashtable:system-loaded-classes", 97, NULL , NULL, 0, 0);
  woempa(7,"created system_loaded_class_hashtable at %p\n",system_loaded_class_hashtable);

  system_unloaded_class_hashtable = ht_create((char*)"hashtable:system-unloaded-classes", 97, clazz_hashcode, clazz_comparator, 0, 0);
  woempa(7,"created system_unloaded_class_hashtable at %p\n",system_unloaded_class_hashtable);

  collectCoreFixups();

  /*
  ** Initialize base classes, we have to resolve things manually so keep
  ** this procedure intact.
  **  1) load class java/lang/Object.
  **  2) load java/lang/Cloneable, java/lang/Serializable, java/lang/Throwable. 
  **  3) create clazz_Array. clazz_Array acts like clazzObject but has
  **     a modified method table and a modified interfaces table. 
  **     It overrides the clone method of java/lang/Object and adds two 
  **     new interfaces: Cloneable and Serializable.
  **     (We need to do this before any subclasses of Throwable are loaded,
  **     in order to ensure that they are marked CLAZZ_IS_THROWABLE).
  **  4) load java/lang/Class, java/lang/ClassLoader, java/lang/Thread, 
  **     java/lang/ThreadGroup, and java/lang/ref/Reference.
  **  5) load all the classes mentioned in core-classes.in.
  **  6) For each primitive class xxx, create:
  **       -An instance Class_xxx of class java/lang/Class, linked to
  **        a w_Clazz structure (clazz_xxx).
  **        The name associated with the Class instance is "xxx.class".
  **       -Entries in the array atype2clazz[], which is indexed by P_xxx.
  **
  ** From that moment on, it should be possible to load any class without
  ** resolution problems and  to create instances of Object, Class, String
  ** Thread, or ThreadGroup without first checking that the class is prepared.
  */
  
  x_monitor_eternal(&system_loaded_class_hashtable->monitor);

  /****
  * 1 *
  ****/

  woempa(7,"Step 1: load java.lang.Object\n");
  clazzObject = loadBootstrapClass(string_java_lang_Object);

 /****
 * 2 *
 ****/

  woempa(7,"Step 2: load java.lang.Cloneable, java.io.Serializable, java.lang.Throwable.\n");
// load class java/lang/Cloneable
  clazzCloneable = seekClazzByName(string_java_lang_Cloneable, NULL);
  if (!clazzCloneable) {
    clazzCloneable = loadBootstrapClass(string_java_lang_Cloneable);
  }
// load class java/io/Serializable
  clazzSerializable = seekClazzByName(string_java_io_Serializable, NULL);
  if (!clazzSerializable) {
    clazzSerializable = loadBootstrapClass(string_java_io_Serializable);
  }
// load class java/lang/Throwable
  clazzThrowable = seekClazzByName(string_java_lang_Throwable, NULL);
  if (!clazzThrowable) {
    clazzThrowable = loadBootstrapClass(string_java_lang_Throwable);
  }
  setFlag(clazzThrowable->flags, CLAZZ_IS_THROWABLE);
 
  /****
  * 3 *
  ****/

  woempa(7,"Step 3: create array pseudo-class\n");
  clazz_Array = createClazzArray();
  mustBeReferenced(clazz_Array);

 /****
 * 4 *
 ****/

  woempa(7,"Step 4: load java.lang.Class, java.lang.ClassLoader\n");
// load class java/lang/Class
  clazzClass = seekClazzByName(string_java_lang_Class, NULL);
  if (!clazzClass) {
    clazzClass = loadBootstrapClass(string_java_lang_Class);
  }
// load class java/lang/ClassLoader
  clazzClassLoader = seekClazzByName(string_java_lang_ClassLoader, NULL);
  if (!clazzClassLoader) {
    clazzClassLoader = loadBootstrapClass(string_java_lang_ClassLoader);
  }
  setFlag(clazzClassLoader->flags, CLAZZ_IS_CLASSLOADER);

 /****
 * 5 *
 ****/

  woempa(7,"Step 5: attach an instance of Class to everything loaded so far, and locate the special methods Object.finalize() and ClassLoader.loadClass().\n");
  identify_special_methods();

 /****
 * 6 *
 ****/

  woempa(7,"Step 6: load java.lang.Thread, java.lang.ref.Reference, java.lang.reflect.Constructor\n");
// load class java/lang/Thread
  clazzThread = seekClazzByName(string_java_lang_Thread, NULL);
  if (!clazzThread) {
    clazzThread = loadBootstrapClass(string_java_lang_Thread);
  }
  setFlag(clazzThread->flags, CLAZZ_IS_THREAD);
// load class java/lang/ThreadGroup
  clazzThreadGroup = seekClazzByName(string_java_lang_ThreadGroup, NULL);
  if (!clazzThreadGroup) {
    clazzThreadGroup = loadBootstrapClass(string_java_lang_ThreadGroup);
  }

// load class java/lang/ref/Reference
  clazzReference = seekClazzByName(string_java_lang_ref_Reference, NULL);
  if (!clazzReference) {
    clazzReference = loadBootstrapClass(string_java_lang_ref_Reference);
  }
  setFlag(clazzReference->flags, CLAZZ_IS_REFERENCE);

// load class java/lang/reflect/Constructor
  clazzConstructor = seekClazzByName(string_java_lang_reflect_Constructor, NULL);
  if (!clazzConstructor) {
    clazzConstructor = loadBootstrapClass(string_java_lang_reflect_Constructor);
  }

 /****
 * 7 *
 ****/
  woempa(7,"Step 7: loading core classes \n");
  loadCoreClasses();
  x_monitor_exit(&system_loaded_class_hashtable->monitor);


 /****
 * 8 *
 ****/
  woempa(7,"Step 8: create primitive classes and their array classes\n");

  preparePrimitive(clazz_boolean, string_boolean, P_boolean);
  preparePrimitive(clazz_char, string_c_h_a_r, P_char);
  preparePrimitive(clazz_float, string_float, P_float);
  preparePrimitive(clazz_double, string_double, P_double);
  preparePrimitive(clazz_byte, string_byte, P_byte);
  preparePrimitive(clazz_short, string_short, P_short);
  preparePrimitive(clazz_int, string_int, P_int);
  preparePrimitive(clazz_long, string_long, P_long);
  preparePrimitive(clazz_void, string_void, P_void);

  /*
  ** For some strange reason, SUN seems to fix the SUIDs for arrays of primitives. 
  ** So we set the cached SUID for the primitive array clazzes to the one given by 'serialver'.
  */
  
  atype2clazz[P_boolean]->suid = 6309297032502205922LL;
  atype2clazz[P_char]->suid = -5753798564021173076LL;
  atype2clazz[P_float]->suid = 836686056779680834LL;
  atype2clazz[P_double]->suid = 4514449696888150558LL;
  atype2clazz[P_byte]->suid = -5984413125824719648LL;
  atype2clazz[P_short]->suid = -1188055269542874886LL;
  atype2clazz[P_int]->suid = 5600894804908749477LL;
  atype2clazz[P_long]->suid = 8655923659555304851LL;

  /*
  ** Create some other frequently used descriptors and classes.
  ** Again, fix some SUID's of these classes...
  */
  
  clazzArrayOf_Object = getNextDimension(clazzObject, NULL);
  clazzArrayOf_Object->suid = -8012369246846506644LL;

  clazzArrayOf_Class = getNextDimension(clazzClass, NULL);
  clazzArrayOf_Class->suid = -6118465897992725863LL;

  clazzArrayOf_String = getNextDimension(clazzString, NULL);
  clazzArrayOf_String->suid = -5921575005990323385LL;

  /*
  ** Set up the primitive2wrapper array...
  */
  
  primitive2wrapper[P_boolean] = clazzBoolean;
  primitive2wrapperSlot[P_boolean] = F_Boolean_value;

  primitive2wrapper[P_char] = clazzCharacter;
  primitive2wrapperSlot[P_char] = F_Character_value;

  primitive2wrapper[P_float] = clazzFloat;
  primitive2wrapperSlot[P_float] = F_Float_value;

  primitive2wrapper[P_double] = clazzDouble;
  primitive2wrapperSlot[P_double] = F_Double_value;

  primitive2wrapper[P_byte] = clazzByte;
  primitive2wrapperSlot[P_byte] = F_Byte_value;

  primitive2wrapper[P_short] = clazzShort;
  primitive2wrapperSlot[P_short] = F_Short_value;

  primitive2wrapper[P_int] = clazzInteger;
  primitive2wrapperSlot[P_int] = F_Integer_value;

  primitive2wrapper[P_long] = clazzLong;
  primitive2wrapperSlot[P_long] = F_Long_value;

  mustBeInitialized(clazzClass);
  // All the classes which are used in do_throw_clazz(c) (interpreter.c)
  // must be pre-initialized.
  mustBeInitialized(clazzInvocationTargetException);
  mustBeInitialized(clazzNullPointerException);
  mustBeInitialized(clazzOutOfMemoryError);
  mustBeInitialized(clazzArithmeticException);
  mustBeInitialized(clazzArrayIndexOutOfBoundsException);
  mustBeInitialized(clazzArrayStoreException);
  mustBeInitialized(clazzClassCastException);
  mustBeInitialized(clazzIllegalAccessError);
  mustBeInitialized(clazzIllegalMonitorStateException);
  mustBeInitialized(clazzIncompatibleClassChangeError);
  mustBeInitialized(clazzInternalError);
  mustBeInitialized(clazzLinkageError);
  mustBeInitialized(clazzNegativeArraySizeException);
  mustBeInitialized(clazzNoSuchMethodException);
  //
  mustBeInitialized(clazzArrayOf_Object);
  mustBeInitialized(clazzArrayOf_String);
  mustBeInitialized(clazzArrayOf_Class);

  attach_class_instances();

  woempa(7, "Forced class loading complete, loaded %d classes.\n",system_loaded_class_hashtable->occupancy);

}

/*
** Convert the given string from descriptor form (e.g. "Ljava/lang/Class;", "I",** "[Ljava/lang/String;", "[Z") to Class.getName() form ("java.lang.Class", "int",
** but "[Ljava.lang.String;", "[Z" <shrug>).  Returns a registered w_string.
*/

w_string undescriptifyClassName(w_string string) {

  w_string FQName = string;
  w_size   i;
  w_char *charbuf;


  woempa(1, "Starting with %w\n", string);
  if (string_char(string, 0) == 'V') {
    FQName = registerString(string_void);
  }
  else if (string_char(string, 0) == 'I') {
    FQName = registerString(string_int);
  }
  else if (string_char(string, 0) == 'J') {
    FQName = registerString(string_long);
  }
  else if (string_char(string, 0) == 'Z') {
    FQName = registerString(string_boolean);
  }
  else if (string_char(string, 0) == 'F') {
    FQName = registerString(string_float);
  }
  else if (string_char(string, 0) == 'D') {
    FQName = registerString(string_double);
  }
  else if (string_char(string, 0) == 'C') {
    FQName = registerString(string_c_h_a_r);
  }
  else if (string_char(string, 0) == 'B') {
    FQName = registerString(string_byte);
  }
  else if (string_char(string, 0) == 'L') {
    charbuf = allocMem(string_length(string) * sizeof(w_char));
    if (!charbuf) {
      wabort(ABORT_WONKA, "Unable to allocate charbuf\n");
    }
    for (i = 1; i < string_length(string) - 1; ++i) {
      charbuf[i - 1] = (string_char(string, i) == '/') ? '.' : string_char(string, i);
    }
    FQName = unicode2String(charbuf, string_length(string) - 2);
    if (!FQName) {
      wabort(ABORT_WONKA, "Unable to allocate FQName\n");
    }
    releaseMem(charbuf);
  }
  else {
    FQName = slashes2dots(FQName);
  }

  return FQName;
  
}

/*
** Search for the clazz called 'classname', and return its address or NULL.
** 'classname' is in the format returned by Class.getName().
*/

w_clazz seekClazzByName(w_string classname, w_instance initiating_loader) {
  w_hashtable hashtable = loader2loaded_classes(initiating_loader);
  w_clazz result;

  woempa(1, "Seeking class '%w' in %s.\n", classname, hashtable->label);
  result = (w_clazz)ht_read_no_lock(hashtable, (w_word)classname);

  if (result && initiating_loader) {
    while (getClazzState(result) == CLAZZ_STATE_LOADING && result->resolution_thread != currentWonkaThread) {
      woempa(7, "%K is being loaded by %t, waiting in %t\n", result, result->resolution_thread, currentWonkaThread);
      waitMonitor(initiating_loader, 2);
    }
  }

  return result;
}

/*
** Create the array clazz with one more dimension than the given clazz.
*/
w_clazz createNextDimension(w_clazz base_clazz, w_instance initiating_loader) {
  w_thread thread = currentWonkaThread;
  w_clazz array_clazz;
  w_char  *name_buffer;
  w_string temp_name;
  w_string desc_name;

  threadMustBeSafe(thread);

  /*
  ** We make a clone of clazz_Array. We don't need to increment
  ** all refcounts that are being used in this clazz.
  */

  array_clazz = allocClazz();
  temp_name = clazz2desc(base_clazz);
  if (!temp_name) {
    wabort(ABORT_WONKA, "Unable to allocate temp_name\n");
  }

  name_buffer = allocMem((string_length(temp_name) + 1) * sizeof(w_char));
  if (!name_buffer) {
    wabort(ABORT_WONKA, "Unable to allocate name_buffer\n");
  }

  if (array_clazz == NULL || temp_name == NULL || name_buffer == NULL) {
    woempa(9, "Could not allocate w_Clazz for [%k\n", base_clazz);
    if (array_clazz) {
      releaseMem(array_clazz);
    }

    if (temp_name) {
      deregisterString(temp_name);
    }

    if (name_buffer) {
      releaseMem(name_buffer);
    }

    return NULL;

  }

  w_memcpy(array_clazz, clazz_Array, sizeof(w_Clazz));
  woempa(1, "Creating next dimension of clazz %k at %p\n", base_clazz, array_clazz);

  array_clazz->dims = base_clazz->dims + 1;
  array_clazz->type = (array_clazz->dims > 1 ? VM_TYPE_MULTI : VM_TYPE_MONO) + VM_TYPE_REF + (base_clazz->type & 0x0f);
  array_clazz->loader = base_clazz->loader;
  array_clazz->bits = 32;

  name_buffer[0] = '[';
  w_string2chars(temp_name, name_buffer + 1);
  desc_name = unicode2String(name_buffer, string_length(temp_name) + 1);
  if (!desc_name) {
    wabort(ABORT_WONKA, "Unable to convert desc name to w_string\n");
  }
  deregisterString(temp_name);
  releaseMem(name_buffer);
  temp_name = slashes2dots(desc_name);
  if (!temp_name) {
    wabort(ABORT_WONKA, "Unable to dotify desc name\n");
  }
  deregisterString(desc_name);
  array_clazz->dotified = temp_name;

  woempa(1, "Clazz %k dims is %d, type is 0x%02x, defining loader is %j (%w)\n", array_clazz, array_clazz->dims, array_clazz->type, array_clazz->loader, loader2name(array_clazz->loader));

  if (base_clazz->dims == 0 && isSet(base_clazz->flags, CLAZZ_IS_PRIMITIVE)) {
    wabort(ABORT_WONKA, "This function cannot be used to create an array of primtives!\n");
  }
  else {
    array_clazz->previousDimension = base_clazz;
    base_clazz->nextDimension = array_clazz;
  }

  if (thread) {
     enterUnsafeRegion(thread);
  }

  if (clazzClass && clazzClass->Class) {
    attachClassInstance(array_clazz, thread);
  }
  setClazzState(array_clazz, CLAZZ_STATE_LINKED);

  woempa(7, "Array clazz %k at %p defined by %j (%w), initiated by %j (%w).\n", array_clazz,array_clazz,array_clazz->loader, loader2name(array_clazz->loader), initiating_loader, loader2name(initiating_loader));
  setFlag(array_clazz->flags, ACC_ABSTRACT | ACC_PUBLIC | ACC_FINAL);

  if (thread) {
    enterSafeRegion(thread);
  }

  return registerClazz(currentWonkaThread, array_clazz, initiating_loader);
}

/*
** Get the array clazz with one more dimension than the given clazz.
** If the array clazz does not already exist, create it.
** If base_clazz is NULL then we return NULL.
*/
w_clazz getNextDimension(w_clazz base_clazz, w_instance initiating_loader) {
  w_clazz array_clazz;

  if (!base_clazz) {

    return NULL;

  }

  threadMustBeSafe(currentWonkaThread);

  if (initiating_loader) {
    enterMonitor(initiating_loader);
  }
  array_clazz = base_clazz->nextDimension;
  if (!array_clazz) {
    woempa(1, "Clazz %k doesn't yet have a nextDimension, creating it\n", base_clazz);
    array_clazz = createNextDimension(base_clazz, initiating_loader);
  }
  else {
    woempa(1, "Clazz %k already has nextDimension %k\n", base_clazz, array_clazz);
  }
  if (initiating_loader) {
    exitMonitor(initiating_loader);
  }

  return array_clazz;

}


/*
** Create the 1-D array clazz corresponding to a given primitive type.
*/

w_clazz createPrimitiveArrayClazz(w_int pi) {

  w_clazz element_clazz = NULL;
  w_clazz result = NULL;
  w_string desc_string = NULL;

  woempa(1, "Creating array class for primitive #%d.\n", pi);

  switch(pi) {
    case P_boolean:
      element_clazz = clazz_boolean;
      desc_string = cstring2String("[Z",2);
      break;

    case P_byte:
      element_clazz = clazz_byte;
      desc_string = cstring2String("[B",2);
      break;

    case P_char:
      element_clazz = clazz_char;
      desc_string = cstring2String("[C",2);
      break;

    case P_short:
      element_clazz = clazz_short;
      desc_string = cstring2String("[S",2);
      break;

    case P_int:
      element_clazz = clazz_int;
      desc_string = cstring2String("[I",2);
      break;

    case P_float:
      element_clazz = clazz_float;
      desc_string = cstring2String("[F",2);
      break;

    case P_long:
      element_clazz = clazz_long;
      desc_string = cstring2String("[J",2);
      break;

    case P_double:
      element_clazz = clazz_double;
      desc_string = cstring2String("[D",2);
      break;

    default:
      wabort(ABORT_WONKA, "Eh???\n");
  }

  result = allocClazz();
  if (result == NULL) {
    woempa(9,"Unable to create w_Clazz for %w\n", desc_string);
    return NULL;
  }
  w_memcpy(result, clazz_Array, sizeof(w_Clazz));

  setFlag(result->flags, ACC_FINAL | ACC_PUBLIC);
  result->loader = NULL;
  result->bits = 32;
  result->Class = NULL;
//  attachClassInstance(result, thread);
  result->dotified = desc_string;

  result->previousDimension = primitive2clazz[pi];
  result->dims = 1;

  setClazzState(result, CLAZZ_STATE_LINKED);

  woempa(1, "Created array clazz %k, dimensions %d.\n", result, result->dims);

  if (!result->previousDimension) {
    result->previousDimension = element_clazz;
    result->dims = 1;
    element_clazz->nextDimension = result;
  }

  return registerClazz(NULL, result, NULL);
}


/*
** If the class with the given name (in "dotted" form) is not loaded,
** then if it is an array class then try to create it, else abort.
*/

w_clazz namedArrayClassMustBeLoaded(w_instance initiating_loader, w_string name) {

  w_thread    thread = currentWonkaThread;
  w_clazz  current;

  if (initiating_loader) {
    // wprintf("requesting lock for %j\n", initiating_loader);
    enterMonitor(initiating_loader);
    // wprintf("obtained lock for %j\n", initiating_loader);
  }

  current = seekClazzByName(name, initiating_loader);

  if (current == NULL) {
    w_char * namebuff = allocMem(string_length(name) * sizeof(w_char));
    w_string prevname = NULL;
    w_int length;

    if (!namebuff) {
      wabort(ABORT_WONKA, "Unable to allocate namebuff\n");
    }

    if (isSet(verbose_flags, VERBOSE_FLAG_LOAD)) {
      wprintf("Load %w: initiating class loader is %j in thread %t\n", name, initiating_loader, thread);
    }

    w_string2chars(name, namebuff);
    length = string_length(name);

    if(length == 2) { 
      switch (namebuff[1]) {
      case 'Z':
        prevname = registerString(string_boolean);
        break;

      case 'C':
        prevname = registerString(string_c_h_a_r);
        break;

      case 'F':
        prevname = registerString(string_float);
        break;

      case 'D':
        prevname = registerString(string_double);
        break;

      case 'B':
        prevname = registerString(string_byte);
        break;

      case 'S':
        prevname = registerString(string_short);
        break;

      case 'I':
        prevname = registerString(string_int);
        break;

      case 'J':
        prevname = registerString(string_long);
        break;
      default:
        break;
      }
    }  
    if(prevname == NULL && length > 1) {    
      char* result;
      if (length > 3 && namebuff[1] == 'L' && namebuff[2] != '[' && namebuff[length-1] == ';') {
        prevname = unicode2String(namebuff + 2, length - 3);
      } else {
        prevname = unicode2String(namebuff + 1, length - 1);
      }
    }
    releaseMem(namebuff);
    if(prevname == NULL) {    
      if (initiating_loader) {
        // wprintf("releasing lock for %j\n", initiating_loader);
        exitMonitor(initiating_loader);
      }
      throwException(thread, clazzClassNotFoundException, "%j could not load %w", initiating_loader, name);
      return NULL;
    }

    current = namedClassMustBeLoaded(initiating_loader, prevname);
    deregisterString(prevname);
    current = getNextDimension(current, initiating_loader);

    if (isSet(verbose_flags, VERBOSE_FLAG_LOAD)) {
      if (!current) {
        wprintf("Load %w: class not found by %j\n", name, initiating_loader);
      }
      else if (exceptionThrown(thread)) {
        wprintf("Load %w: failed with %e\n", name, exceptionThrown(thread));
      }
      else {
        wprintf("Load %w: defining class loader is %j\n", name, current->loader);
      }
    }
  }

  if (initiating_loader) {
    // wprintf("releasing lock for %j\n", initiating_loader);
    exitMonitor(initiating_loader);
  }

  return current;

}

/*
** Helper function that returns WONKA_TRUE iff the name begins with
** "java." or "wonka.", optionally preceded by one or more "["s,
** and contains only ISO Latin 1 characters.
*/
w_boolean namedClassIsSystemClass(w_string name) {
  w_ubyte *ch;

  if (!string_is_latin1(name)) {
    woempa(1, "'%w' is not Latin-1 => not a system class\n", name);

    return WONKA_FALSE;

  }

  ch = name->contents.bytes;
  while (*ch == '[') ++ch;

  woempa(1, "Length %d, prefix %c%c%c%c%c%c\n", string_length(name) - (ch - name->contents.bytes), ch[0], ch[1], ch[2], ch[3], ch[4], ch[5]);
  if (((int)string_length(name) > (ch - name->contents.bytes) + 5 && strncmp(ch, "java.", 5) == 0)
    ||
      ((int)string_length(name) > (ch - name->contents.bytes) + 6 && strncmp(ch, "wonka.", 6) == 0)
    || strncmp(ch, "boolean", string_length(name)) == 0
    || strncmp(ch, "byte", string_length(name)) == 0
    || strncmp(ch, "short", string_length(name)) == 0
    || strncmp(ch, "int", string_length(name)) == 0
    || strncmp(ch, "long", string_length(name)) == 0
    || strncmp(ch, "float", string_length(name)) == 0
    || strncmp(ch, "double", string_length(name)) == 0
    || strncmp(ch, "char", string_length(name)) == 0
  ) {
    woempa(1, "'%w' is a system class\n", name);

    return WONKA_TRUE;

  }
  woempa(1, "'%w' is not a system class\n", name);

  return WONKA_FALSE;
}

w_clazz namedClassMustBeLoaded(w_instance classLoader, w_string name) {

  w_thread    thread = currentWonkaThread;
  w_instance  effectiveLoader = classLoader;
  w_clazz     current;

  woempa(7, "Class %w must be loaded.\n", name);
  if (string_char(name, 0) == (w_char)'[') {
    return namedArrayClassMustBeLoaded(classLoader, name);
  }

  if (!systemClassLoader) {
    woempa(7, "System class loader not defined, so use bootstrap class loader\n");
  }
  else if (!classLoader) {
    woempa(7, "System class loader exists, so use it instead of bootstrap class loader to load %w\n", name);
    effectiveLoader = systemClassLoader;
  }
  else if (!classLoader || namedClassIsSystemClass(name)) {
    woempa(7, "Class %w is a system class, so use system class loader instead of %j\n", name, classLoader);
    effectiveLoader = systemClassLoader;
  }
  else {
    woempa(7, "Class %w is not a system class, so use %j\n", name, classLoader);
  }

  if (effectiveLoader) {
    // wprintf("requesting lock for %j\n", effectiveLoader);
    enterMonitor(effectiveLoader);
    // wprintf("obtained lock for %j\n", effectiveLoader);
  }

  current = seekClazzByName(name, effectiveLoader);

  woempa(7, "seekClazzByName result = %p\n", current);
  if (current == NULL) {
    if (isSet(verbose_flags, VERBOSE_FLAG_LOAD)) {
      wprintf("Load %w: initiating class loader is %j in thread %t\n", name, classLoader, thread);
    }

    if (effectiveLoader == NULL) {
      current = loadBootstrapClass(name);
    }
    else {
      w_clazz placeholder = allocClazz();

      placeholder->resolution_thread = thread;
      setClazzState(placeholder, CLAZZ_STATE_LOADING);
      registerClazz(thread, placeholder, effectiveLoader);
      exitMonitor(effectiveLoader);

      current = loadNonBootstrapClass(effectiveLoader, name);

      enterMonitor(effectiveLoader);
      if (current && getClazzState(current) == CLAZZ_STATE_LOADING) {
        // Loading did not succeed, so delete the placeholder.
        releaseMem(placeholder);
        current = NULL;
      }
    }
    if (isSet(verbose_flags, VERBOSE_FLAG_LOAD)) {
      if (!current) {
        wprintf("Load %w: class not found by %j\n", name, effectiveLoader);
      }
      else if (exceptionThrown(thread)) {
        wprintf("Load %w: failed with %e\n", name, exceptionThrown(thread));
      }
      else {
        wprintf("Load %w: defining class loader is %j\n", name, current->loader);
      }
    }
  }

  if (effectiveLoader) {
    // wprintf("releasing lock for %j\n", effectiveLoader);
    exitMonitor(effectiveLoader);
  }

  if (! current && ! exceptionThrown(thread)) {
    throwException(thread, clazzNoClassDefFoundError, "%j could not load %w", effectiveLoader, name);
  }

  return current;

}

w_clazz loadNonBootstrapClass(w_instance initiating_loader, w_string name) {

  w_thread thread = currentWonkaThread;
  w_instance theClass = NULL;
  w_clazz    initiating_loader_clazz = instance2clazz(initiating_loader);
  w_instance Name;
  w_frame    frame;
  w_clazz    clazz;
  w_method   method;
  w_instance exception;

  method = virtualLookup(loadClass_method, initiating_loader_clazz);
  woempa(7, "Trying to load class %w using %m of %j\n", name, method, initiating_loader);

  Name = newStringInstance(name);
  if (!Name) {
    woempa(9, "Unable to get String instance of '%w'\n", name);

    return NULL;

  }
  frame = activateFrame(thread, method, FRAME_LOADING, 2, initiating_loader, stack_trace, Name, stack_trace);
  exception = exceptionThrown(thread);
  if (! exception) {
    theClass = (w_instance) frame->jstack_top[-1].c;
  }
  deactivateFrame(frame, theClass);
  removeLocalReference(thread, Name);

  if (exception && theClass) {
    woempa(9, "Odd. I asked %j to load %w, and it gave me back %j but also threw %e\n", initiating_loader, name, theClass, exception);
    theClass = NULL;
  }

  if (theClass == NULL) {
    woempa(7, "Ah. I asked %j to load %w, and it returned NULL.\n", initiating_loader,name);
    if (exception) {
      woempa(7, "Exception thrown = %e\n", exception);
    }
    else {
      woempa(7, "No exception throw, throwing ClassNotFoundException\n");
      throwException(thread, clazzClassNotFoundException, "loadNonBootstrapClass: %w", name);
    }

    return NULL;
  }

  clazz = Class2clazz(theClass);
  woempa(7, "clazz = %K (%p)\n", clazz, clazz);

  if (clazz->dotified != name) {
    woempa(9,"Scandal! I asked %j to load %w, and the bounder loaded %k!\n", initiating_loader, name, clazz);
    throwException(thread, clazzLinkageError, "%w", name);
    return NULL;
  }

  woempa(7, "Loaded %k at %p, defining class loader is %j (%w), initiating class loader is %j (%w)\n", clazz, clazz, clazz->loader, loader2name(clazz->loader), initiating_loader, loader2name(initiating_loader));

  return clazz;

}

#ifdef USE_ZLIB
static void wabort_unzip_problem(char * zipfilename, char *entryname, const char *problem, int unzerr) {
    wabort(ABORT_WONKA, "Bootstrap jar file '%s', entry '%s' : %s, error code is %d\n", zipfilename, entryname, problem, unzerr);
}
#endif

/**
 ** Look for an entry called 'filename' in bootzipfile; if found, set *barptr
 ** accordingly and return TRUE. Otherwise return FALSE and leave *barfile alone.
 */
w_boolean getBootstrapFile(char *filename, w_BAR *barptr) {
#ifdef USE_ZLIB
  int      z_rc;
  char    *buffer;
  unz_file_info file_info;

  z_rc = unzLocateFile(bootzipfile, filename, 1);
  if (z_rc != UNZ_OK) {
    wabort_unzip_problem(bootzipname, filename, "cannot locate entry", z_rc);
  }
  z_rc = unzGetCurrentFileInfo(bootzipfile, &file_info, NULL, 0, NULL, 0, NULL, 0);
  if (z_rc != UNZ_OK) {
    wabort_unzip_problem(bootzipname, filename, "cannot get info for entry", z_rc);
  }
  buffer = allocMem(file_info.uncompressed_size);
  if (!buffer) {
    wabort_unzip_problem(bootzipname, filename, "cannot allocate buffer for entry", 0);
  }
  z_rc = unzOpenCurrentFile(bootzipfile);
  if (z_rc != UNZ_OK) {
    wabort_unzip_problem(bootzipname, filename, "cannot open entry", z_rc);
  }
  z_rc = unzReadCurrentFile(bootzipfile, buffer, file_info.uncompressed_size);
  if (z_rc < 0) {
    wabort_unzip_problem(bootzipname, filename, "cannot extract entry", z_rc);
  }
  barptr->buffer = buffer;
  barptr->length = file_info.uncompressed_size;
  barptr->current = 0;
  releaseMem(buffer);
#else
  z_zipEntry ze;

  ze = findZipEntry(bootzipfile, filename);
  if (!ze) {

    return FALSE;

  }

  woempa(7, "Zip file entry '%s' at %p\n", filename, ze);
  if (!uncompressZipEntry(ze)) {

    return FALSE;

  }

  woempa(7, "Uncompressed data at %p, length is %d bytes\n", ze->u_data, ze->u_size);
  barptr->buffer = ze->u_data;
  barptr->length = ze->u_size;
  barptr->current = 0;
#endif

  return TRUE;
}

/**
 ** Remove a file from bootzipfile. 
 ** FIXME: Currently does nothing when USE_ZLIB is defined.
 */
void deleteBootstrapFile(char *filename) {
#ifndef USE_ZLIB
  z_zipEntry ze;

  ze = findZipEntry(bootzipfile, filename);
  if (ze) {
    deleteZipEntry(ze);
  }
#endif
}

/**
 ** Load the class with the given name by finding an entry in bootzipfile.
 ** If no such entry is found we abort the VM, there is no plan B.
 */
w_clazz loadBootstrapClass(w_string name) {
  w_size   length;
  char    *filename;
  w_clazz  clazz = NULL;
  w_BAR    bar;
  w_size   i;
  char    *dollar;
  w_char   ch;

  length = string_length(name);
  filename = allocMem(length + 7);
  if (!filename) {
    wabort(ABORT_WONKA, "Unable to allocate space for filename\n");
  }
  for (i = 0; i < length; ++i) {
    ch = string_char(name, i);
    filename[i] = ch == '.' ? '/' : ch;
  }
  filename[length++] = '.';
  filename[length++] = 'c';
  filename[length++] = 'l';
  filename[length++] = 'a';
  filename[length++] = 's';
  filename[length++] = 's';
  filename[length] = 0;
  while ((dollar = strstr(filename, "_dollar_"))) {
    *dollar = 36;
    memcpy(dollar + 1, dollar + 8, (filename + length) - dollar - 7);
  }
  woempa(7, "Need a file called '%s'\n", filename);
  if (!getBootstrapFile(filename, &bar)) {
    wabort(ABORT_WONKA, "Unable to find entry '%s' in bootstrap jar file '%s'\n", filename, bootzipname);
  }

  clazz = createClazz(NULL, NULL, &bar, NULL, TRUE);
  if (!clazz) {
    wabort(ABORT_WONKA, "Swoggle my horn! Attempt to load system class %w failed ...\n",name);
  }

  deleteBootstrapFile(filename);

  return clazz;
}

