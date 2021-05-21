/**************************************************************************
* Copyright (c) 2020 by KIFFER Ltd. All rights reserved.                  *
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

//#include <dlfcn.h>

#include "core-classes.h"
#include "hashtable.h"
#include "ts-mem.h"
#include "vfs.h"
#include "wstrings.h"
#include "exception.h"
#include "clazz.h"
#include "misc.h"
#include "methods.h"
#include "wonka.h"
#include "arrays.h"
#include "exec.h"
#include "loading.h"
#include "checks.h"
#include "profile.h"

void histogram(void);
void w_dump(const char *fmt, ... );

#ifdef JAVA_PROFILE

static w_word cl = 0;

static x_boolean statistics_timings_callback(void * mem, void * arg) {
  w_fifo       fifo;
  w_clazz      clazz = NULL;
  w_method     method = NULL;
  w_word       i;
  w_object     object = chunk2object(mem);
  w_long       external_time;
  w_long       instances;

  if(isAssignmentCompatible(object->clazz, clazzClassLoader)) {

    fifo = ht_list_values(loader2loaded_classes((w_instance)object->fields));

    w_dump("TL%03d: %w\n", cl, object->clazz->dotified);

    while((clazz = getFifo(fifo)) != NULL) {

      for(i = 0; i < clazz->numDeclaredMethods; i++) {

        method = &clazz->own_methods[i];

        external_time = 0;
        instances = 0;

        if(method->exec.callData) {
          w_int tableEntry = 0;
          w_profileMethodCallData data;
          while(tableEntry < METHOD_CALL_TABLE_SIZE) {
            data = ((w_profileMethodCallData *)method->exec.callData)[tableEntry];
            while(data) {
              if (data->child->exec.runs) {
                external_time += (data->child->exec.runtime * data->count / data->child->exec.runs);
              }
              data = data->next;
            }
            tableEntry++;
          }
        }
        if(method->exec.instanceData) {
          w_int tableEntry = 0;
          w_profileMethodInstanceData data;
          while(tableEntry < METHOD_INSTANCE_TABLE_SIZE) {
            data = ((w_profileMethodInstanceData *)method->exec.instanceData)[tableEntry];
            while(data) {
              instances += data->count;
              data = data->next;
            }
            tableEntry++;
          }
        }

        external_time = method->exec.runtime - external_time;
        if(external_time < 0) external_time = 0;

        if (!isSet(method->flags, ACC_ABSTRACT | ACC_NATIVE | METHOD_IS_COMPILED)) {
          if(method->exec.runs && method->exec.bytecodes && !method->exec.checked) {

            w_dump("TD%03d: %8d |%14lld %6lld |%11lld %10lld |%11lld %10lld |%11lld %10lld |%8lld %6lld |  J | %w.%m\n", cl,
                method->exec.runs, method->exec.bytecodes, method->exec.bytecodes / method->exec.runs, 
                method->exec.totaltime, method->exec.totaltime / method->exec.runs, 
                method->exec.runtime, method->exec.runtime / method->exec.runs, 
                external_time, external_time / method->exec.runs, instances, instances / method->exec.runs,
                method->spec.declaring_clazz->dotified, method);
            method->exec.checked = 1;
          }
        }
        else if (isSet(method->flags, METHOD_IS_COMPILED)) {
          if(method->exec.runs) {

            w_dump("TD%03d: %8d |%14lld %6lld |%11lld %10lld |%11lld %10lld |%11lld %10lld |%8lld %6lld |  C | %w.%m\n", cl,
                method->exec.runs, 0LL, 0LL, 
                method->exec.totaltime, method->exec.totaltime / method->exec.runs, 
                method->exec.runtime, method->exec.runtime / method->exec.runs, 
                external_time, external_time / method->exec.runs, instances, instances / method->exec.runs,
                method->spec.declaring_clazz->dotified, method);
            method->exec.checked = 1;
          }
        }
        else if (isSet(method->flags, ACC_NATIVE)) {
          if(method->exec.runs) {
            w_dump("TD%03d: %8d |%14lld %6lld |%11lld %10lld |%11lld %10lld |  N | %w.%m\n", cl,
                method->exec.runs, 0LL, 0LL, 
                method->exec.totaltime, method->exec.totaltime / method->exec.runs, 
                method->exec.runtime, method->exec.runtime / method->exec.runs, 
                method->spec.declaring_clazz->dotified, method);
            method->exec.runs = 0;
          }
        }
      }
        
        if(method->exec.callData) {
          w_int tableEntry = 0;
          w_profileMethodCallData data;
          while(tableEntry < METHOD_CALL_TABLE_SIZE) {
            data = ((w_profileMethodCallData *)method->exec.callData)[tableEntry];
            while(data) {
              if (data->child->exec.runs) {
                w_dump("TD%03dc: %10d (%8d %11lld) -> %11lld %w.%m\n", cl, data->count, 
                  data->child->exec.runs, data->child->exec.runtime,
                  (data->child->exec.runtime * data->count / data->child->exec.runs),
                  data->child->spec.declaring_clazz->dotified, data->child);
              }
              else {
                w_dump("TD%03dc: %10d (%8d ???????????) -> %11lld %w.%m\n", cl, data->count, 
                  data->child->exec.runs, data->child->exec.runtime,
                  data->child->spec.declaring_clazz->dotified, data->child);
              }
              data = data->next;
            }
            tableEntry++;
          }
        }
        if(method->exec.instanceData) {
          w_int tableEntry = 0;
          w_profileMethodInstanceData data;
          while(tableEntry < METHOD_INSTANCE_TABLE_SIZE) {
            data = ((w_profileMethodInstanceData *)method->exec.instanceData)[tableEntry];
            while(data) {
              w_dump("TD%03di: %10d of %8lld -> %3lld%% %3lld%% %w\n", cl, data->count,
                  data->clazz->total_instances, (data->count * 100) / data->clazz->total_instances, 
                  (data->count * 100) / instances, data->clazz->dotified);
              data = data->next;
            }
            tableEntry++;
          }
        }

    }

    cl++;

    releaseFifo(fifo);
  }

  return TRUE;
}

static void statistics_timings(void) {
  w_dump("XX: - Profile data : \nXX:\n");
  w_dump("XX:        Runs | Total bytecds    Avg | Total time        Avg |   Run time        Avg | Non-acc time      Avg | Instances  Avg |Type| Method\n");

  cl = 0;

  x_mem_lock(x_eternal);
  x_mem_scan(x_eternal, OBJECT_TAG, statistics_timings_callback, NULL);
  x_mem_unlock();

  w_dump("XX:\n\n");
}

static x_boolean statistics_instances_callback(void * mem, void * arg) {
  w_fifo       fifo;
  w_clazz      clazz = NULL;
  w_object     object = chunk2object(mem);

  if(isAssignmentCompatible(object->clazz, clazzClassLoader)) {

    fifo = ht_list_values(loader2loaded_classes((w_instance)object->fields));

    w_dump("IL%03d: %w\n", cl, object->clazz->dotified);

    while((clazz = getFifo(fifo)) != NULL) {
      if(clazz->total_instances) {
        w_dump("ID%03d: %8lld |%7lld |%9lld |%5d | %w\n", cl,
            clazz->total_instances, 
            clazz->max_instances, clazz->max_instances * clazz->bytes_needed,
            clazz->bytes_needed, clazz->dotified);
      }
    }

    cl++;

    releaseFifo(fifo);
  } 

  return TRUE;
}

static void statistics_instances(void) {
  w_dump("XX:       Total |    Max | Tot. mem |  Mem | Class\n");

  cl = 0;
  
  x_mem_lock(x_eternal);
  x_mem_scan(x_eternal, OBJECT_TAG, statistics_instances_callback, NULL);
  x_mem_unlock();

  w_dump("XX:\n\n");
}

#endif
/*
** Iterator called from Runtime.exit0(); for every object which is an instance
** of PlainSocketImpl or PlainDatagramSocketImpl, call close() on its file
** descriptor. This logic could be applied to other classes with "externalities"
** also.
*/
static w_boolean shutdown_iteration(void * mem, void * arg) {
  w_object object = chunk2object(mem);
  w_int fd;

  if(isSuperClass(clazzPlainSocketImpl, object->clazz)) {
    fd = (w_int)getWotsitField(object->fields, F_PlainSocketImpl_wotsit);
    if (fd >= 0) {
      close(fd);
    }
  }
  else if(isSuperClass(clazzPlainDatagramSocketImpl, object->clazz)) {
    fd = (w_int)getWotsitField(object->fields, F_PlainDatagramSocketImpl_wotsit);
    if (fd >= 0) {
      close(fd);
    }
  }

  return TRUE;
}

void Runtime_static_exit0 (JNIEnv* env, w_instance thisClass, w_int exitcode) {

  x_mem_scan(x_eternal, OBJECT_TAG, shutdown_iteration, NULL);
#ifdef JAVA_PROFILE
  statistics_timings();
  statistics_instances();
#endif

//  histogram();
  
#if defined(LINUX) || defined(NETBSD)
  exit(exitcode);
#else
  wabort(ABORT_WONKA, "exit(%d)\n",exitcode);
#endif
}

w_int Runtime_load0 (JNIEnv* env, w_instance thisRuntime, w_instance libpathString) {

  w_thread   thread = JNIEnv2w_thread(env);
  w_string   libpath = NULL;
  w_ubyte *  path = NULL;
  void *     handle;
  w_int      i;
  
  if(libpathString) {
    libpath = String2string(libpathString);
    i = string_length(libpath) * 3 + 1;
    path = allocMem(i);
    if (!path) {
      wabort(ABORT_WONKA, "Unable to allocate space for path\n");
    }
    x_snprintf(path, i, "%w", libpath);
  }

  woempa(7, "Calling loadModule ...\n");
  handle = loadModule(NULL, path);
  if (handle) {
    woempa(7, "Successfully loaded library %w: handle = %p\n", libpath, handle);
  }
  else {
    char *loading_problem = dlerror();
    if (!loading_problem) {
      loading_problem = "<null>";
    }
    throwException(thread, clazzUnsatisfiedLinkError, "Error when loading library: path = '%w' dlerror = %s", libpath, loading_problem);
  }

  if(path) releaseMem(path);

  return (w_int)handle;
}

/*
 * If libnameString is null:
 *   libnameString = name of the library to be sought (without lib...so)
 *   libpathString = colon-separated list of paths to be searched.
 * If libnameString is non-null:
 *   libpathString = complete path to the library, e.g. /Mika/fsroot/fwdir/libfoo.so .
 */
w_int Runtime_loadLibrary0 (JNIEnv* env, w_instance thisRuntime, w_instance libnameString, w_instance libpathString) {

  w_thread   thread = JNIEnv2w_thread(env);
  w_string   libname = NULL;
  w_string   libpath = NULL;
  w_ubyte *  name = NULL;
  w_ubyte *  path = NULL;
  void *     handle;
  w_int      i;
  
  if(libnameString) {
    libname = String2string(libnameString);
    i = string_length(libname) * 3 + 7;
    name = allocMem(i);
    if (!name) {
      wabort(ABORT_WONKA, "Unable to allocate space for name\n");
    }
    x_snprintf(name, i, "lib%w.so", libname);
  }
  
  if(libpathString) {
    libpath = String2string(libpathString);
    i = string_length(libpath) * 3 + 1;
    path = allocMem(i);
    if (!path) {
      wabort(ABORT_WONKA, "Unable to allocate space for path\n");
    }
    x_snprintf(path, i, "%w", libpath);
  }

  woempa(7, "Calling loadModule ...\n");
  handle = loadModule(name, path);
  if (handle) {
    woempa(7, "Successfully loaded library %w from %w: handle = %p\n", libname, libpath, handle);
  }
  else {
    char *loading_problem = dlerror();
    if (!loading_problem) {
      loading_problem = "<null>";
    }
    throwException(thread, clazzUnsatisfiedLinkError, "Error when loading library: name = '%w', path = '%w' dlerror = %s", libname, libpath, loading_problem);
  }

  if(name) releaseMem(name);
  if(path) releaseMem(path);

  return (w_int)handle;
}

w_long
Runtime_totalMemory (JNIEnv* env, w_instance thisRuntime) {
  return (w_long)(x_mem_total());
}

w_long
Runtime_freeMemory (JNIEnv* env, w_instance thisRuntime) {
  return (w_long)(x_mem_avail());
}

