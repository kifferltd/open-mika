/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved.                                                               *
* Parts copyright (c) 2004, 2005, 2006 by Chris Gray, /k/ Embedded Java   *
* Solutions.  All rights reserved.                                        *
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

#include "checks.h"
#include "clazz.h"
#include "hashtable.h"
#include "loading.h"
#include "methods.h"
#include "oswald.h"
#include "threads.h"
#include "ts-mem.h"
#include "vfs.h"
#include "vfs_fcntl.h"

extern w_clazz clazzClassLoader;

const char *abortMessages[] = {
  "Information",
  "ABORTING thread",
  "ABORTING group",
  "ABORTING root group",
  "ABORT Wonka",
};

char *verbose_cstring;
w_flags verbose_flags;

x_Mutex woempaMutex;

void PutString(char *s) {
  write(2, s, strlen(s));
}

char hexDigit(int n) {
  return n<10 ? n+'0' : n+'a'-10;
}

void PutHex (unsigned int Value, unsigned int Width) {
  char buf[9];
  int i, x;

  x = Value;
  for (i=7;i>=0;--i) {
    buf[i] = hexDigit(x&0xf);
    x >>= 4;
  }
  buf[8] = 0;
  PutString(Width>8?buf:buf+8-Width);
}

void PutDec (unsigned int Value, unsigned int Width) {
  char buf[12];
  int i, x;
  char sign;

  x = Value;
  if (x<0) {
    sign = '-';
    x = -x;
  }
  else sign = 0;

  for (i=10;i>=0;--i) {
    buf[i] = '0'+(x%10);
    x /= 10;
  }
  buf[11] = 0;
  PutString(Width>11?buf:buf+11-Width);
}

/*
** We use a hashtable to store the woempa trigger level associated with
** each sourcefile: the key is a C string, and if the string contains a
** '/' character then the hashcode and equality functions ignore everything
** to the left of the last '/' in the string.  (In other words, the whole
** path doesn't have to match, just the 'basename').  If a file is not
** found in the hashtable then its trigger level is assumed to be
** woempa_default_trigger_level.
**
** There is one special case" file "hashtable.c" has its own trigger level
** outside of the hashtable, to avoid a deadlock when woempa is called
** from inside a hashtable function.
*/

static w_hashtable woempa_hashtable = NULL;
static w_int woempa_default_trigger_level = 1;

static char *
get_basename(const char *filename) {
  char *basename = (char *)filename + strlen(filename);
  while (basename-filename > 0 && *--basename != '/');

  return basename;
}

w_word
filename_hash(w_word nameword) {
  char *filename = (char*)nameword;
  char *basename = filename + strlen(filename);
  int   hashcode = 0x654321;
  char  one_byte;

  basename = get_basename(filename);

  while ((one_byte = *basename++)) {
    hashcode = (hashcode * 255) ^ one_byte;
  }

  return hashcode;
}

w_boolean
filenames_equal(w_word nameword1, w_word nameword2) {
  char *filename1 = (char*)nameword1;
  char *filename2 = (char*)nameword2;
  char *basename1;
  char *basename2;

  basename1 = get_basename(filename1);
  basename2 = get_basename(filename2);

  return strcmp(basename1,basename2) == 0;
}

static int hashtable_trigger_level = 1;

void _setTriggerLevel(const char *file, int trigger) {
  if (strcmp(get_basename(file),"hashtable.c") == 0) {
    hashtable_trigger_level = trigger;
  }
  else {

    if (woempa_hashtable == NULL) {
      woempa_hashtable = ht_create((char*)"hashtable:woempa", 31, filename_hash, filenames_equal,0,0);
    }

  ht_write(woempa_hashtable,(w_word)file,(w_word)trigger);
  }
}

void setAllTriggerLevel(int trigger) {
  if (woempa_hashtable) {
    ht_destroy(woempa_hashtable);
    woempa_hashtable = NULL;
  }
  hashtable_trigger_level = trigger;
  woempa_default_trigger_level = trigger;
}

#define BUFSIZE        8192
static char woempa_buffer[BUFSIZE];

/*
** A small woempa aware function that can be called from the gdb debugger...
*/

void wprintf(const char *fmt, ...) {

  va_list ap;

  va_start(ap, fmt);
  x_vsnprintf(woempa_buffer, BUFSIZE, fmt , ap);
  va_end(ap);
#if defined(LINUX) || defined(NETBSD)
  write(1, woempa_buffer, strlen(woempa_buffer));
#else
  PutString(woempa_buffer);
  PutString((char *)"\15");
#endif

}

extern char *woempa_dump_file;
extern int  woempa_stderr;
extern int  woempa_bytecodecount;
extern int  woempa_bytecodetrigger;

void pclazz(w_clazz clazz) {
  woempa(9, "CLAZZ = %k\n", clazz);
}

void _woempa(const char *file, const char *function, int line, int level, const char *fmt, ... ) {

  va_list ap;
  int trigger = 0;
  w_size bufsize = BUFSIZE;
  static int od = 0;
    
  if (woempa_bytecodetrigger > 0 && 
      woempa_bytecodecount > woempa_bytecodetrigger)
    woempa_default_trigger_level = 2;

  if(woempa_dump_file) {
    if (! od) {
      /*         name     <--- flags --------------------------->  <------ modes -------------->  */
      od = open(woempa_dump_file, (VFS_O_WRONLY | VFS_O_CREAT | VFS_O_TRUNC), (VFS_S_IRUSR | VFS_S_IWUSR | VFS_S_IXUSR | VFS_S_IRGRP | VFS_S_IROTH));
    }
  }
  
  if (strcmp(get_basename(file),"hashtable.c") == 0) {
    trigger = hashtable_trigger_level;
  }
  else if (woempa_hashtable) {
    trigger = (int)ht_read(woempa_hashtable,(w_word)file);
  }
  else {
    trigger = woempa_default_trigger_level;
  }

  if (trigger < 1 || trigger > 9) {
    trigger = woempa_default_trigger_level;
  }
  
  if (trigger <= level && x_mutex_lock(&woempaMutex, x_eternal) == xs_success) {
      
    w_thread jthread = currentWonkaThread;
    w_frame  jframe  = jthread ? jthread->top : NULL;
    w_method jmeth   = jframe  ? jframe->method : NULL;
    
    x_snprintf(woempa_buffer, bufsize, 
#ifdef WOEMPA_JTHREAD_FORMAT
       WOEMPA_JTHREAD_FORMAT " "
#endif
#ifdef WOEMPA_LEVEL_FORMAT
       WOEMPA_LEVEL_FORMAT " "
#endif
#ifdef WOEMPA_BYTECODECOUNT_FORMAT
       WOEMPA_BYTECODECOUNT_FORMAT " "
#endif
#ifdef WOEMPA_METHOD_FORMAT
       WOEMPA_METHOD_FORMAT " "
#endif
#ifdef WOEMPA_FUNCTION_FORMAT
       WOEMPA_FUNCTION_FORMAT " "
#endif
       "%s",
#ifdef WOEMPA_JTHREAD_FORMAT
      jthread,
#endif
#ifdef WOEMPA_LEVEL_FORMAT
      level,
#endif
#ifdef WOEMPA_BYTECODECOUNT_FORMAT
      woempa_bytecodecount,
#endif
#ifdef WOEMPA_METHOD_FORMAT
      jmeth ? jmeth->spec.declaring_clazz : NULL,
      jmeth ? jmeth->spec.name : NULL,
      jmeth && jmeth->exec.code ? jframe->current - jmeth->exec.code : 0,
#endif
#ifdef WOEMPA_FUNCTION_FORMAT
      function,
      line,
#endif
      ": "
    );

    va_start (ap, fmt);
    (void)x_vsnprintf(woempa_buffer + strlen(woempa_buffer), bufsize - strlen(woempa_buffer), fmt , ap);
    va_end (ap);
   
#ifdef OS_NONE
    if(woempa_stderr) {
      PutString(woempa_buffer);
      PutString((char *)"\15");
    }
#else
#ifndef ECOS
    if(od) { 
      (void)write(od, woempa_buffer, strlen(woempa_buffer));
    }
#endif //ECOS
 
   
    if(woempa_stderr) {
      if (level >= 7 || (woempa_bytecodecount > woempa_bytecodetrigger && woempa_bytecodetrigger > 0)) {
        (void)write(2, woempa_buffer, strlen(woempa_buffer));
        fflush(NULL);
      }
    }
#endif

    (void)x_mutex_unlock(&woempaMutex);

  }

}

void w_dump_info(void);

void _wabort(const char *function, int line, int scope, const char *fmt, ... ) {
  x_thread kthread;
  x_status status = xs_success;
  w_thread thread = NULL;
  va_list ap;
  w_size bufsize = BUFSIZE;

  if (haveWonkaThreads) {
    status = x_mutex_lock(&woempaMutex, 5);
  }
#ifdef OS_NONE
  (void)write(1, (char *)"\15",1);
#endif
  x_snprintf(woempa_buffer, bufsize, "\n%s: %s %4d: ", abortMessages[scope], function, line);

  va_start (ap, fmt);
  (void)x_vsnprintf(woempa_buffer + strlen(woempa_buffer), bufsize - strlen(woempa_buffer), fmt , ap);
  va_end (ap);

  (void)write(1, woempa_buffer, strlen(woempa_buffer));
#ifdef OS_NONE
  (void)write(1, (char *)"\15",1);
#endif
  kthread = x_thread_current();
/*
** Replace with thread_hashtable lookup for now
  thread = (w_thread)kthread->wx_user_data;
*/
  if (thread_hashtable) {
    thread = (w_thread)ht_read(thread_hashtable, (w_word)kthread);
    if ((*(w_word*)thread->label)!=(*(w_word*)"thread")){
      woempa(9,"currentWonkaThread called at %s:%d from non-wonka thread (label = %s)\n", function, line, thread->label);
      thread = NULL;
    }
  }

  w_dump_info();

#ifdef DEBUG
  if (thread) {
    PutString((char*)"Failing thread: ");
    PutString(threadDescription(thread)); 
    PutString((char *)"\12");
  }
  else {
    PutString((char*)"Unable to identify failing thread, sorry mate...\n");
  }
#ifdef OS_NONE
  (void)write(1, (char *)"\15",1);
#endif
#endif

  if (haveWonkaThreads && status == xs_success) {
    (void)x_mutex_unlock(&woempaMutex);
  }

  if (scope > ABORT_INFO) {
#ifdef OS_NONE
    (void)x_thread_sleep(x_millis2ticks(5000));
    x_thread_deletei(x_thread_current());
#else
    abort();
#endif
  }
  
}

static char dump_buffer[BUFSIZE];

void w_dump(const char *fmt, ... ) {
  va_list ap;
  
  va_start (ap, fmt);
  (void)x_vsnprintf(dump_buffer, BUFSIZE, fmt , ap);
  va_end (ap);

#if defined(LINUX) || defined(NETBSD)
  (void)write(2, dump_buffer, strlen(dump_buffer));
#else
  PutString(dump_buffer);
  PutString((char *)"\15");
#endif
}

w_string w_dump_name(void *xref)
{
  return ((w_thread)xref)->name;
}

void w_dump_threads(void) {
  x_dump_threads();
}

extern volatile w_thread marking_thread;

void w_dump_trace(void *xref) {
  w_thread thread = (w_thread)xref;
  w_frame frame = thread->top;
  w_method method;
  w_int pc;

  w_dump("     Local references: %d\n",thread->slots + SLOTS_PER_THREAD - thread->top->auxstack_top);

  if (thread == marking_thread) {
    w_dump("     marking heap\n");
  }
  else if (thread == sweeping_thread) {
    w_dump("     sweeping heap\n");
  }
  else if (thread == jitting_thread) {
    w_dump("     JIT-compiling\n");
  }

  if (isSet(thread->flags, WT_THREAD_NOT_GC_SAFE)) {
    w_dump("     unsafe\n");
  }
  while (frame) {
    if (frame->method) {
      method = frame->method;
      pc = frame->current - method->exec.code;

      if (isSet(method->flags, METHOD_IS_COMPILED)) {
        w_dump("       %K/%m (jitted method)\n", method->spec.declaring_clazz, method);
      }
      else if (isSet(method->flags, ACC_NATIVE)) {
        w_dump("       %K/%m (native method)\n", method->spec.declaring_clazz, method);
      }
      else {
        if (method->exec.debug_info){
          w_dump("       %K/%m line %d (pc %d)\n", method->spec.declaring_clazz, method, code2line(method, method->exec.code + pc), pc);
        }
        else {
          w_dump("       %K/%m pc = %d\n", method->spec.declaring_clazz, method, pc);
        }
      }
    }
    frame = frame->previous;
  }
}

extern x_mutex   finalizer_fifo_mutex;
extern x_mutex   enqueue_fifo_mutex;
extern x_mutex   string_mutex;
extern x_monitor reclaim_listener_monitor;
extern x_monitor gc_monitor;
#if defined(AWT_XSIM) || defined(AWT_FDEV)
extern x_monitor  tree_lock;
#endif

extern w_hashtable lock_hashtable;

static const char *gc_phase_names[] = {
  "unready", "prepare", "mark", "sweep", "complete", "??????"
};

static char lock_buffer[256];

void lock_iterator(w_word key, w_word value, void *v1, void *v2) {
  w_instance i = (w_instance)key;
  x_monitor  m = (x_monitor)value;
  if (instance2clazz(i) == clazzClass) {
    x_snprintf(lock_buffer, 254, "%k", Class2clazz(i));
  }
  else {
    x_snprintf(lock_buffer, 254, "%j", i);
  }
  x_dump_monitor_if_locked(lock_buffer, m);
}

void w_dump_locks(void) {
  w_dump(" Locks :\n");
  x_dump_mutex("    Finalizer fifo mutex : ", finalizer_fifo_mutex);
  x_dump_monitor("  Lock hashtable monitor : ", &lock_hashtable->monitor);
  x_dump_monitor("String hashtable monitor : ", &string_hashtable->monitor);
  x_dump_monitor("        Reclaim listener : ", reclaim_listener_monitor);
  x_dump_monitor("          Memory monitor : ", memory_monitor);
  x_dump_monitor("              GC monitor : ", gc_monitor);
#if defined(AWT_XSIM) || defined(AWT_FDEV)
  x_dump_monitor("            AWT treelock : ", tree_lock);
#endif
#ifndef GC_SAFE_POINTS_USE_NO_MONITORS
  x_dump_monitor("       GC status monitor : ", safe_points_monitor);
#endif
  w_dump("      GC number unsafe threads : %d\n", number_unsafe_threads);
  w_dump("                 GC phase : %s\n", gc_phase_names[gc_phase > 4 ? 5 : gc_phase]);
  if (marking_thread) {
    w_dump("        GC marking thread : %t\n", marking_thread);
  }
  if (sweeping_thread) {
    w_dump("       GC sweeping thread : %t\n", sweeping_thread);
  }
  if (blocking_all_threads) {
    w_dump("        blocking all threads : %s\n", isSet(blocking_all_threads, BLOCKED_BY_JITC) ? "JITC" : isSet(blocking_all_threads, BLOCKED_BY_GC) ? "  GC" : isSet(blocking_all_threads, BLOCKED_BY_JDWP) ? "JDWP" : "no");
  }
  w_dump("\n");
  w_dump("   Instance locks :\n");
  ht_iterate(lock_hashtable, lock_iterator, NULL, NULL);
  w_dump("\n");
}

static int object_size;

#ifdef DUMP_CLASSLOADERS
static x_boolean classloaders_callback(void * mem, void * arg) {

  w_instance   instance;
  w_object     object;
  w_clazz      clazz;
  w_hashtable  loaded_classes;
  w_hashtable  unloaded_classes;
  x_monitor    monitor;

  object_size += x_mem_size(mem);
  object = chunk2object(mem);
  instance = object->fields;
  clazz = object->clazz;

  if(isAssignmentCompatible(clazz, clazzClassLoader)) {
    loaded_classes = loader2loaded_classes(instance);
    unloaded_classes = loader2unloaded_classes(instance);
    w_dump("     %j has %d loaded classes, %d unloaded;\n", instance, loaded_classes->occupancy, unloaded_classes->occupancy);

    monitor = &loaded_classes->monitor;
    w_dump("       loaded class monitor is %p\n", monitor);
    
    if(monitor->owner) {
      w_dump("       loaded_hashtable (0x%08x) locked", monitor);
      if(monitor->owner->xref) {
        w_dump(" by \"%w\"", ((w_thread)(monitor->owner->xref))->name);
      }
      w_dump("\n");
    }
    
    monitor = &unloaded_classes->monitor;
    w_dump("       unloaded class monitor is %p\n", monitor);
    
    if(monitor->owner) {
      w_dump("       unloaded_hashtable (0x%08x) locked", monitor);
      if(monitor->owner->xref) {
        w_dump(" by \"%p\"", ((w_thread)(monitor->owner->xref)));
        w_dump(" by \"%w\"", ((w_thread)(monitor->owner->xref))->name);
      }
      w_dump("\n");
    }

    monitor = (x_monitor)ht_read(lock_hashtable, (w_word)instance);
    
    if(monitor && monitor->owner) {
      w_dump("       instance (0x%08x) locked", monitor);
      if(monitor->owner->xref) {
        w_dump(" by \"%p\"", ((w_thread)(monitor->owner->xref)));
        w_dump(" by \"%w\"", ((w_thread)(monitor->owner->xref))->name);
      }
      w_dump("\n");
    }
  }

  return TRUE;
}
#endif

#ifdef DUMP_CLASSES
static x_boolean classes_callback(void * mem, void * arg) {

  w_instance   instance;
  w_object     object;
  w_clazz      clazz;
  w_clazz      target_clazz;
  x_monitor    monitor;

  object = chunk2object(mem);
  instance = object->fields;
  clazz = object->clazz;

  if (clazz == clazzClass) {
    target_clazz = Class2clazz(instance);
    monitor = target_clazz->resolution_monitor;
    if(monitor->owner) {
      w_dump("%K resolution_monitor (0x%08x) locked", target_clazz, monitor);
      if(monitor->owner->xref) {
        w_dump(" by \"%w\"", ((w_thread)(monitor->owner->xref))->name);
      }
      w_dump("\n");
    }

    monitor = (x_monitor)ht_read(lock_hashtable, (w_word)instance);
    
    if(monitor && monitor->owner) {
      w_dump("%K Class instance (0x%08x) locked", target_clazz, monitor);
      if(monitor->owner->xref) {
        w_dump(" by \"%w\"", ((w_thread)(monitor->owner->xref))->name);
      }
      w_dump("\n");
    }
  }

  return TRUE;
}
#endif

#ifdef DUMP_CLASSLOADERS
void w_dump_classloaders(void) {
  w_dump("   Classloaders :\n");
  x_mem_lock(x_eternal);
  object_size = 0;
  x_mem_scan(x_eternal, OBJECT_TAG, classloaders_callback, NULL);
  x_mem_unlock();
  w_dump("\n");
}
#endif

#ifdef DUMP_CLASSES
void w_dump_classes(void) {
  w_dump("   Class locks :\n");
  x_mem_lock(x_eternal);
  x_mem_scan(x_eternal, OBJECT_TAG, classes_callback, NULL);
  x_mem_unlock();
  w_dump("\n");
}
#endif

void w_dump_meminfo(void) {
  w_dump(" Memory :\n");
  w_dump("         Total : %9d\n", x_mem_total());
  w_dump("     Available : %9d\n", x_mem_avail());
//  w_dump("          Used : %9d (of which %d in objects)\n", x_mem_total() - x_mem_avail(), object_size);
  w_dump("          Used : %9d\n", x_mem_total() - x_mem_avail());
  w_dump("\n");
}

extern w_hashtable globals_hashtable;

void w_dump_info() {
  w_dump("\n");
  w_dump_threads();
  w_dump_locks();
#ifdef DUMP_CLASSES
  w_dump_classes();
#endif
#ifdef DUMP_CLASSLOADERS
  w_dump_classloaders();
#endif
  w_dump(" Global References: %d\n\n",globals_hashtable->occupancy);
  w_dump_meminfo();
}
