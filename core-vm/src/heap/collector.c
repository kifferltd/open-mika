/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved.                                                               *
* Parts copyright (c) 2004, 2005, 2006, 2007, 2008, 2009, 2010, 2011      *
* by Chris Gray, /k/ Embedded Java Solutions.  All rights reserved.       *
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

#define PRINTRATE 10

#include <string.h>

#include "arrays.h"
#include "checks.h"
#include "clazz.h"
#ifdef OS_NONE
#include "comms.h"
#endif
#include "constant.h"
#include "core-classes.h"
#include "fields.h"
#include "fifo.h"
#include "hashtable.h"
#include "interpreter.h"
#include "jni.h"
#include "loading.h"
#include "list.h"
#include "locks.h"
#include "methods.h"
#include "oswald.h"
#include "threads.h"
#include "ts-mem.h"
#include "wonka.h"
#include "wstrings.h"

/*
 * The number of ticks for which GC will wait after entering the COMPLETE phase.
 */
#define GC_COMPLETE_WAIT 0

/*
 * The number of ticks for which GC will wait for another thread to complete
 * its PREPARE/MARK phase.
 */
#define GC_OTHER_MARK_WAIT 3

/*
** If PIGS_MIGHT_FLY is defined, mark objects which are found to be unreachable
** as O_GARBAGE the first time around, and only reclaim the memory if they are
** found to be unreachable a second time. This of course raises the possibility
** that objects which were found to be unreachable on one pass will turn out
** to be reachable on the next, which ain't supposed to happen - it probably
** means that someone has sauirreled away a reference to the object, out of
** sight of the garbage collector. If CATCH_FLYING_PIGS is defined, we detect
** this case and abort the VM if it happens.
** For production code you shouldn't need to define either PIGS_MIGHT_FLY or
** CATCH_FLYING_PIGS. Defining PIGS_MIGHT_FLY can be a temporary work-around
** when objects are apparently getting reclaimed prematurely, but you should
** perform tests using CATCH_FLYING_PIGS to get to the root of the problem.
**
** Note: don't define PIGS_MIGHT_FLY here, define it in wonka.h (because other
** files such as strings.h also need to know about it).
*/

//#define CATCH_FLYING_PIGS

#ifndef PIGS_MIGHT_FLY
#ifdef CATCH_FLYING_PIGS
#warning If pigs can't fly there's no point in catching them.
#undef CATCH_FLYING_PIGS
#endif
#endif

#ifdef CATCH_FLYING_PIGS
#define FLYING_PIG_CHECK(o) _flying_pig_check((char*)__FUNCTION__,(char*)__FILE__,__LINE__,o)

static void _flying_pig_check(char *func, char *file, int line, w_object object) {
  if (isSet(object->flags, O_GARBAGE)) {
    w_instance instance = object->fields;

#ifdef DEBUG
    w_printf("Object allocated at %s line %d\n", block2chunk(object)->file, block2chunk(object)->line);
#endif
    if (object->clazz == clazzString) {
      wabort(ABORT_WONKA, "Flying pig %j (`%w')! in %s at %s:%d\n", instance, instance[F_String_wotsit], func, file, line);
    }
    else if (object->clazz == clazzThread) {
      wabort(ABORT_WONKA, "Flying pig %j (%T)! in %s at %s:%d\n", instance, instance[F_Thread_wotsit], func, file, line);
    }
    else {
      wabort(ABORT_WONKA, "Flying pig %j! in %s at %s:%d\n", instance, func, file, line);
    }
  }
}
#else
#define FLYING_PIG_CHECK(o)
#endif

//#define INSTANCE_STATS

#ifdef INSTANCE_STATS
static w_hashtable instance_stat_hashtable;

static void instance_stat_iteration(w_word clazz_word, w_word count, void* dummy1, void *dummy2) {
  w_printf("%8d %k\n", count, clazz_word);
}

static void reportInstanceStat(void) {
  w_printf("--- START INSTANCE STATS ---\n");
  ht_iterate(instance_stat_hashtable, instance_stat_iteration, NULL, NULL);
  w_printf("--- END INSTANCE STATS ---\n");
}
#endif

x_Monitor safe_points_Monitor;
x_monitor safe_points_monitor = &safe_points_Monitor;
volatile w_int number_unsafe_threads;

/*
** Enable this if you really want classes and class loaders to be subject
** to garbage collection. Otherwise they will always be treated as reachable,
** even when the mark phase fails to mark them ...
*/
#define COLLECT_CLASSES_AND_LOADERS

void persistentGarbageCollector(void);

#ifdef JDWP
extern void jdwp_set_garbage(w_instance);
#endif

/*
** If DISTRIBUTED_GC is defined, the reclaim callback mechanism is activated.
** The reclaim callback mechanism allows other memory users (not just the
** instance heap) to register with GC and to be invoked with a reclaim request
** when memory is running low. This allows these other memory users to e.g.
** operate a memory-sensitive cache.
**
** At present there are no (known) users of this mechanism. The AWT would be
** one obvious candidate: filesystems and protocol stacks implemented inside
** Wonka (not supplied by the underlying OS) could also benefit.
*/

#define DISTRIBUTED_GC

/*
** If USE_DISCARD_COLLECT is defined, discardMem and collectMem
** will be used instead of releaseMem.
*/

#define USE_DISCARD_COLLECT

/*
** If CONTRACT_FIFOS is defined, the various fifos will be shrunk down to one
** leaf at the start of each GC cycle.  This is useful when checking for memory
** leaks, because the total memory used by fifos depends only on the number of
** instances currently in use, not the high-water mark. Otherwise it represents
** unnecessary overhead and actually increases the probability of a catastrophic
** out-of-memory condition (not enough space to allocate the fifos required in
** order to perform GC).
**
** REDUCE_FIFOS has a similar effect, but in this case at most one leaf will be
** released from each fifo, so they will still shrink down but more slowly.  
** Kind of a Belgian compromise. ;>
*/
//#define CONTRACT_FIFOS
//#define REDUCE_FIFOS

#if (defined(CONTRACT_FIFOS) && defined(REDUCE_FIFOS))
#warning Both CONTRACT_FIFOS and REDUCE_FIFOS are defined, which is silly.
#endif

/*
** FIFO which holds all instances in the current GC window: the GC window
** is the set of instances which existed at the moment the current GC cycle
** began.  We give this one quite a large leaf size, as it is normal to have
** 1000's of objects.
*/

w_fifo window_fifo;
#define WINDOW_FIFO_LEAF_SIZE   1022

/*
** FIFO used to mark all strongly reachable instances.  We give this one
** quite a large leaf size too.
*/

static w_fifo strongly_reachable_fifo;
#define STRONG_FIFO_LEAF_SIZE 1022

/*
** FIFOs used to mark instances which are reachable only via reference types. 
** We give these a smaller leaf size.
*/

static w_fifo phantom_reachable_fifo;
static w_fifo finalize_reachable_fifo;
#define OTHER_FIFO_LEAF_SIZE 62

/*
** FIFO used to hold instances of Reference on which enqueue() should be called.. 
*/

w_fifo enqueue_fifo;
#define ENQUEUE_FIFO_LEAF_SIZE         OTHER_FIFO_LEAF_SIZE

/*
** List of instances waiting to be finalized.
*/

w_fifo finalizer_fifo;
#define FINALIZER_FIFO_LEAF_SIZE   OTHER_FIFO_LEAF_SIZE

/*
** Mutex used to protect the finalizer fifo from conflicting accesses.
*/

#ifndef THREAD_SAFE_FIFOS
static x_Mutex finalizer_fifo_Mutex;
x_mutex finalizer_fifo_mutex;
#endif

/*
** List of all existing reference objects (instances of java.lang.ref.Reference).
*/

w_fifo reference_fifo;
#define REFERENCE_FIFO_LEAF_SIZE   OTHER_FIFO_LEAF_SIZE

/*
** List of String instances which need to be released.
** We accumulate this during the sweep phase and only at the end do we actually
** release the instance and deregister the w_string;
** this way we only need to grab the string hashtable monitor once.
*/
w_fifo dead_string_fifo;
#define DEAD_STRING_FIFO_LEAF_SIZE   OTHER_FIFO_LEAF_SIZE

/*
** List of objects which have been released and which had a monitor allocated.
** We accumulate this during the sweep phase and only at the end do we actually
** remove the entry in lock_hashtable, delete the monitor and free the memory;
** this way we only need to grab the lock hashtable monitor once.
*/
w_fifo dead_lock_fifo;
#define DEAD_LOCK_FIFO_LEAF_SIZE   OTHER_FIFO_LEAF_SIZE

/*
** List of w_clazz structures which need to be released. We accumulate this
** during the sweep phase and only actually release the memory at the very
** end, because during sweep we need to check the clazz of every unreachable
** instance.
*/
w_fifo dead_clazz_fifo;
#define DEAD_CLAZZ_FIFO_LEAF_SIZE   OTHER_FIFO_LEAF_SIZE

/*
** Convert a w_fifo to its name as a C string
*/
#define fifo2name(f) \
  ( ((f) == window_fifo) ? "window_fifo" \
  : ((f) == strongly_reachable_fifo) ? "strongly_reachable_fifo" \
  : ((f) == phantom_reachable_fifo) ? "phantom_reachable_fifo" \
  : ((f) == finalize_reachable_fifo) ? "finalize_reachable_fifo" \
  : ((f) == enqueue_fifo) ? "enqueue_fifo" \
  : ((f) == finalizer_fifo) ? "finalizer_fifo" \
  : ((f) == reference_fifo) ? "reference_fifo" \
  : ((f) == dead_string_fifo) ? "dead_string_fifo" \
  : ((f) == dead_lock_fifo) ? "dead_lock_fifo" \
  : ((f) == dead_clazz_fifo) ? "dead_clazz_fifo" \
  : "unknown fifo" )

/*
** The Wonka thread which exists purely to perform garbage collection.
*/

w_thread gc_thread;

/*
** The unique instance of GarbageCollector corresponding to gc_thread.
*/

w_instance gc_instance;

/*
** A lock (monitor) associated with the GC thread.
*/

x_monitor gc_monitor;

/*
** (Almost) all accesses to gc_monitor are wrapped to check the status.
*/
#define PRINT_MONITOR_STATUS(l,f,s) printf("collect.c line %d: %s returned %d\n", (l), (f), (s))

#define GC_MONITOR_WAIT(t) gc_monitor_wait(t,__LINE__);
static void gc_monitor_wait(x_sleep t, int line) {
  x_status s = x_monitor_wait(gc_monitor, t);
  if (s != xs_success && s != xs_interrupted) PRINT_MONITOR_STATUS(line, "x_monitor_wait", s);
}

#define GC_MONITOR_NOTIFY gc_monitor_notify(__LINE__);
static void gc_monitor_notify(int line) {
  x_status s = x_monitor_notify_all(gc_monitor);
  if (s) PRINT_MONITOR_STATUS(line, "x_monitor_notify_all", s);
}

#define GC_MONITOR_EXIT gc_monitor_exit(__LINE__);
static void gc_monitor_exit(int line) {
  x_status s = x_monitor_exit(gc_monitor);
  if (s) PRINT_MONITOR_STATUS(line, "x_monitor_exit", s);
}

/*
** Pointer to an instance variable of gc_instance which is bumped up when
** GC is administered a 'kick' (e.g. when Runtime.gc() is called) and then
** decremented down to zero during subsequest GC passes.
*/

w_int *gc_kicks_pointer;

/*
** The `total' memory is the amount returned by x_mem_total, minus 
** min_heap_free.  Calculated during GC startup.
*/

static w_int memory_total;

/*
** The memory load factor is the integer part of (rho / (1-rho)), where
** rho = (memory_total - x_mem_avail()) / memory_total .
** It is calculated at the start of each GC cycle.
*/

w_int memory_load_factor;

/*
** 'killing_soft_references' is set true if either memory is tight or
** 'gc_kicks_pointer' is nonzero (indicating that a memory shortage
** occurred recently). It does not change during a GC cycle.
** When 'killing_soft_references' is true, all soft references encountered
** during this GC pass are treated as if they were weak; otherwise they are
** treated as strong.
*/

static w_boolean killing_soft_references;

extern void Thread_destructor(w_instance);
extern void Throwable_destructor(w_instance);
extern void ReferenceQueue_destructor(w_instance);

static void Class_destructor(w_instance theClass) {
  w_clazz clazz = getWotsitField(theClass, F_Class_wotsit);

  if (!clazz) {
    return;
  }

  if (isSet(verbose_flags, VERBOSE_FLAG_LOAD)) {
    w_printf("Unloading %K\n", clazz);
  }

  clearWotsitField(theClass, F_Class_wotsit);
  woempa(7, "Adding %K to dead_clazz_fifo\n", clazz);
  if (putFifo(clazz, dead_clazz_fifo) < 0) {
    w_printf("Failed to put %K on dead_clazz_fifo\n", clazz);
  };
}

/*
** Release the memory held by a w_UnloadedClazz in a class loader's unloaded
** class hashtable.
*/
static void trashUnloadedClasses(w_word key, w_word value, void *dummy1, void*dummy2) {
  w_clazz clazz = (w_clazz)key;

  woempa(7, "trashing %K (%d references)\n", clazz, value);
  releaseMem(clazz);
}

/*
** Release the memory held by a w_Package structs in a class loader's package
** hashtable.
*/
static void trashPackages(w_word key, w_word value, void *dummy1, void*dummy2) {
  w_package package = (w_package)value;

  woempa(7, "trashing %w in %j\n", package->name, package->loader);
  if (isSet(verbose_flags, VERBOSE_FLAG_LOAD)) {
    w_printf("Removing package %w from %j\n", package->name, package->loader);
  }
  destroyPackage(package);
}

static void ClassLoader_destructor(w_instance theClassLoader) {
  w_hashtable class_hashtable;

  woempa(9, "Destroying ClassLoader instance %j\n", theClassLoader);
  if (isSet(verbose_flags, VERBOSE_FLAG_LOAD)) {
    w_printf("Destroying ClassLoader instance %j\n", theClassLoader);
  }
  class_hashtable = getWotsitField(theClassLoader, F_ClassLoader_loaded_classes);
  if (class_hashtable) {
    clearWotsitField(theClassLoader, F_ClassLoader_loaded_classes);
    releaseMem(class_hashtable->label);
    ht_destroy(class_hashtable);
  }

  class_hashtable = getWotsitField(theClassLoader, F_ClassLoader_unloaded_classes);
  if (class_hashtable) {
    clearWotsitField(theClassLoader, F_ClassLoader_unloaded_classes);
    woempa(7, "Releasing hashtable %s for %j\n", class_hashtable->label, theClassLoader);
    if (class_hashtable->occupancy) {
      ht_iterate(class_hashtable, trashUnloadedClasses, NULL, NULL);
    }
    releaseMem(class_hashtable->label);
    ht_destroy(class_hashtable);
  }

  class_hashtable = getWotsitField(theClassLoader, F_ClassLoader_packages);
  if (class_hashtable) {
    clearWotsitField(theClassLoader, F_ClassLoader_packages);
    woempa(7, "Releasing hashtable %s for %j\n", class_hashtable->label, theClassLoader);
    if (class_hashtable->occupancy) {
      ht_iterate(class_hashtable, trashPackages, NULL, NULL);
    }
    releaseMem(class_hashtable->label);
    ht_destroy(class_hashtable);
  }
}

/*
** The thread (if any) which is currently running a PREPARE/MARK sequence
** on the heap.
*/
volatile w_thread marking_thread;

/*
** The thread (if any) which is currently sweeping the window_fifo.
*/
volatile w_thread sweeping_thread;

static w_boolean checkStrongRefs(w_object);

/*
** Reclaim an instance by returning its memory to the global object heap.
** The value returned is the size of the instance in bytes (the amount of 
** memory freed is generally more than this).
*/
static void reallyReallyReleaseInstance(w_object object) {
#ifdef USE_DISCARD_COLLECT
  discardMem(object);
#else
  releaseMem(object);
#endif

  instance_returned += 1;
  instance_use -= 1;
#ifdef JAVA_PROFILE
  object->clazz->instances--;
#endif 
}

static w_int reallyReleaseInstance(w_object object) {
  w_size  bytes = 0;
  w_clazz clazz = object->clazz;
  w_instance instance = object->fields;
#ifdef CLASSES_HAVE_INSTANCE_CACHE
  w_boolean done = FALSE;
  static int fudge;
#endif

  if (isSet(object->flags, O_HAS_LOCK)) {
    if (putFifo(instance, dead_lock_fifo) < 0) {
      wabort(ABORT_WONKA, "Failed to put %j on dead_lock_fifo\n", instance);
    };
    unsetFlag(object->flags, O_HAS_LOCK);
  }

  if (clazz->previousDimension) {
    bytes = (clazz->previousDimension->bits * instance2Array_length(instance) + 7) / 8;
    bytes = (bytes + 7) & ~7;
    bytes = bytes + sizeof(w_int) + sizeof(w_Object);
    woempa(1, "%j has %d elements of %d bits each = %d bytes\n", instance, instance2Array_length(instance), clazz->previousDimension->bits, bytes);
  }
  else {
    bytes = clazz->bytes_needed;
    woempa(1, "%j = %d bytes\n", instance, bytes);
  }

#ifdef USE_OBJECT_HASHTABLE
  if (!ht_erase(object_hashtable, (w_word)object)) {
    wabort(ABORT_WONKA, "Sky! Could not erase object %p from object hashtable!\n", object);
  }
  woempa(1, "Removed %j from object_hashtable, now contains %d objects\n", instance, object_hashtable->occupancy);
#endif

#ifdef JDWP
  jdwp_set_garbage(object->fields);
#endif

#ifdef CLASSES_HAVE_INSTANCE_CACHE
  if (!clazz->previousDimension && (!memory_load_factor || (++fudge % memory_load_factor) == 0)) {
#ifndef THREAD_SAFE_FIFOS
    x_mutex_lock(clazz->cache_mutex, x_eternal);
#endif
    woempa(7, "putting %j to cache_fifo of %k\n", object->fields, clazz);
    done = putFifo(object, clazz->cache_fifo) >= 0;
#ifndef THREAD_SAFE_FIFOS
    x_mutex_unlock(clazz->cache_mutex);
#endif
  }

  if (done) {
    setFlag(object->flags, O_CACHED);
  }
  else {
#endif
    reallyReallyReleaseInstance(object);
#ifdef CLASSES_HAVE_INSTANCE_CACHE
  }
#endif

  return bytes;
}

static w_int releaseInstance(w_object object) {
  w_clazz clazz;
  w_instance instance;

  if (object == NULL) {
    wabort(ABORT_WONKA, "Releasing NULL object !!\n");
  }
  
  clazz = object->clazz;
  instance = object->fields;

  if (isSet(object->flags, O_HAS_LOCK)) {
    w_thread owner_thread = monitorOwner(instance);
    if (owner_thread) {
      woempa(9, "Hold on a moment - monitor of %j still owned by %t\n", instance, owner_thread);
      return 0;
    }
  }

//  woempa(1, "(GC) Releasing %p (object %p) = %k flags %s\n", (char *)instance, object, clazz, printFlags(object->flags));

  if (clazz == clazzString && getWotsitField(instance, F_String_wotsit)) {
    w_string s = getWotsitField(instance, F_String_wotsit);
    if (s && s->interned == instance) {
      s->interned = NULL;
      woempa(3, "Uninterned %j (%w)\n", instance, s);
    }
    woempa(3, "Deferring sweeping of %j (%w)\n", instance, s);
    if (putFifo(instance, dead_string_fifo) < 0) {
      wabort(ABORT_WONKA, "Failed to put %j on dead_string_fifo", instance);
    };

    return 0;
  }
  else if (clazz == clazzClass) {
#ifdef COLLECT_CLASSES_AND_LOADERS
    Class_destructor(instance);
#else
// [CG 20040330] This could help get rid of "dead" class's static vars?
//    w_printf("%K is now garbage\n", Class2clazz(instance));
//    setClazzState(Class2clazz(instance), CLAZZ_STATE_GARBAGE);
    return 0;
#endif
  }
  else if (isSet(clazz->flags, CLAZZ_IS_CLASSLOADER)) {
#ifdef COLLECT_CLASSES_AND_LOADERS
    ClassLoader_destructor(instance);
#else
    return 0;
#endif
  }
  else if (isSet(clazz->flags, CLAZZ_IS_THREAD)) {
    Thread_destructor(instance);
  }
  else if (isSet(clazz->flags, CLAZZ_IS_THROWABLE)) {
    Throwable_destructor(instance);
  } else if(clazz == clazzReferenceQueue) {
    ReferenceQueue_destructor(instance);
  }

  return reallyReleaseInstance(object);
}

/*
** Function impliesMark returns true iff the highest-order 1 bit in flag1
** is at least as high-order as the single 1 bit in flag2.
** N.B. flag2 must be a power of 2!
*/
static w_boolean impliesMark(w_word flag1, w_word flag2) {
  return flag1 > (flag2 - 1);
}

/*
** Function isMarked returns true iff the object in question is already
** marked either with the given flag or by one which `dominates' it, i.e.
** precedes it in the series 
**   O_BLACK > O_PHANTOM_BLACK > O_FINALIZE_BLACK.
** Note that we rely on the fact that the flags in question appear in
** the object flags word contiguously and in the order shown above.
*/
static w_boolean isMarked(w_object o, w_word flag) {
  return ((flag != O_FINALIZE_BLACK) ? impliesMark(o->flags & (O_BLACK | O_PHANTOM_BLACK), flag) : (w_boolean)isSet(o->flags, O_BLACK | O_FINALIZE_BLACK));
}

#define isNotMarked(o,flag) !isMarked(o,flag)

/*
** Try to add a given instance to a given fifo. Return 1 if successful,
** -1 if failed (because fifo is already full and could not be extended).
*/
static w_int tryPutFifo(w_instance instance, w_fifo fifo) {
  if (!instance) {
    wabort(ABORT_WONKA, "Attempt to enqueue null instance!\n");
  }

  // CG 20031212 
  // VOODOO!!! This never triggers, but if I leave it out I get random segfaults! Go figure!
  if (!instance2clazz(instance)) {
    wabort(ABORT_WONKA, "Instance at %p has no class!\n", instance);
  }

  woempa(1, "Pushing %j onto fifo %p\n", instance, fifo);
  if (putFifo(instance, fifo) < 0) {
    woempa(9, "Shiver my timbers! Couldn't push instance %j onto fifo %p\n", instance, fifo);
    wabort(ABORT_WONKA, "Could not push %j onto fifo %p!\n", instance, fifo);

    return -1;

  };

  return 1;
}

/*
** If the given instance is not already marked with the given flag (or a
** "stronger" one), mark it and put it on the given queue. Returns 0 if
** nothing was done, 1 if the instance was queued successfully, -1 if
** enqueueing failed.
*/
#ifdef CATCH_FLYING_PIGS
#define markInstance(instance,fifo,flag) _markInstance(instance,fifo,flag,(char*)__FUNCTION__,(char*)__FILE__,__LINE__)

w_int _markInstance(w_instance instance, w_fifo fifo, w_word flag, char *func, char *file, int line) {
  w_object object = instance2object(instance);
  w_int retcode;

  _flying_pig_check(func, file, line, object);

#else
w_int markInstance(w_instance instance, w_fifo fifo, w_word flag) {
  w_object object = instance2object(instance);
  w_int retcode;

#endif
#ifdef DEBUG_STACKS
  {
    int depth = (char*)marking_thread->native_stack_base - (char*)&object;
    if (depth > marking_thread->native_stack_max_depth) {
      if (isSet(verbose_flags, VERBOSE_FLAG_STACK)) {
        w_printf("GC: thread %t stack base %p, end %p, now at %p, used = %d%%\n", marking_thread, marking_thread->native_stack_base, (char*)marking_thread->native_stack_base - marking_thread->ksize, &object, (depth * 100) / marking_thread->ksize);
      }
      marking_thread->native_stack_max_depth = depth;
    }
  }
#endif

  if (isMarked(object, flag)) {

    return 0;

  }

  woempa(1, "Pushing %j onto fifo @ %p, setting flag %d\n", instance, fifo, flag);
  setFlag(object->flags, flag);
  retcode = tryPutFifo(instance, fifo);
  if (retcode < 0) {

    return retcode;

  }

  return 1;
}

/*
** Mark a class (possibly an unloaded class) which is reachable via the
** current class. For unloaded classes we just mark the classloader,
** for loaded classes we mark the Class instance. 'child' must not be NULL.
static w_int markChildClazz(w_clazz parent, w_clazz child, w_fifo fifo, w_word flag) {
  if (child == parent) {

    return 0;

  }

  if (getClazzState(child) == CLAZZ_STATE_UNLOADED) {
    if (child->loader) {

      return markInstance(child->loader, fifo, flag);

    }
  }
  else if (child->Class) {

    return markInstance(child->Class, fifo, flag);

  }

  return 0;
}
*/

/*
** If a Class is reachable (because instances exist, or because it is
** known to a ClassLoader), and it has been initialised, we queue all
** of its (unmarked) static fields for marking.
*/
w_int markClazzReachable(w_clazz clazz, w_fifo fifo, w_word flag) {
  w_instance child_instance;
  w_int      i;
  w_int      queued = 0;
  w_int      retcode;
  w_int      state = getClazzState(clazz);

  if (state < CLAZZ_STATE_LOADED || state == CLAZZ_STATE_BROKEN) {
    return 0;
  }

  child_instance = clazz2Class(clazz);
  if (child_instance) {
    retcode = markInstance(child_instance, fifo, flag);
    if (retcode < 0) {

      return retcode;

    }
    queued += retcode;
  }
#ifdef RUNTIME_CHECKS
  else {
    wabort(ABORT_WONKA, "No Class instance for class %k\n", clazz);
  }
#endif

  if (clazz->references) {
    w_int n;

    n = sizeOfWordset(&clazz->references);

    for (i = 0; i < n; ++i) {
      woempa(1, "(GC) %K references %K\n", clazz, elementOfWordset(&clazz->references, i));
      child_instance = clazz2Class((w_clazz)elementOfWordset(&clazz->references, i));
      if (child_instance) {
        retcode = markInstance(child_instance, fifo, flag);
        if (retcode < 0) {

          return retcode;

        }
        queued += retcode;
      }
#ifdef RUNTIME_CHECKS
      else {
        wabort(ABORT_WONKA, "No Class instance for class %k\n", clazz);
      }
#endif
    }
  }

  woempa(1, "(GC) Marking constant pool of class %k\n",clazz);
  for (i = 1; i < (w_int)clazz->numConstants; ++i) {
    if (clazz->tags[i] == COULD_NOT_RESOLVE) {
      child_instance = (w_instance)clazz->values[i];
      woempa(1, "(GC)    --> constant[%d] of %k contains instance %j.\n", i, clazz, child_instance);
      if (child_instance) {
        retcode = markInstance(child_instance, fifo, flag);
        if (retcode < 0) {

          return retcode;

        }
        queued += retcode;
      }
    }
    else if (clazz->tags[i] == RESOLVED_STRING) {
      child_instance = (w_instance)clazz->values[i];
      woempa(1, "(GC)    --> constant[%d] of %k contains string in instance %p.\n", i, clazz, child_instance);
      woempa(1, "constant[%d] of %k contains string `%w' in instance %p.\n", i, clazz, child_instance[F_String_wotsit], child_instance);
      if (child_instance) {
        retcode = markInstance(child_instance, fifo, flag);
        if (retcode < 0) {

          return retcode;

        }
        queued += retcode;
      }
    }
  }

  if (clazz->previousDimension) {
    child_instance = clazz2Class(clazz->previousDimension);
    if (child_instance) {
      retcode = markInstance(child_instance, fifo, flag);
      if (retcode < 0) {

        return retcode;

      }
      queued += retcode;
    }
#ifdef RUNTIME_CHECKS
    else {
      wabort(ABORT_WONKA, "No Class instance for class %k\n", clazz);
    }
#endif
  }

  if (getClazzState(clazz) >= CLAZZ_STATE_SUPERS_LOADED) {
    w_clazz super = getSuper(clazz);
    if (super) {
      woempa(1, "(GC) Marking Class instance of superclass (%K) of %K\n", super[0], clazz);
      child_instance = clazz2Class(super);
      if (child_instance) {
        retcode = markInstance(child_instance, fifo, flag);
        if (retcode < 0) {

          return retcode;

        }
        queued += retcode;
      }
#ifdef RUNTIME_CHECKS
      else {
        wabort(ABORT_WONKA, "No Class instance for class %k\n", clazz);
      }
#endif
    }

    if (clazz->interfaces) {
      for (i = 0; i < clazz->numInterfaces; ++i) {
        child_instance = clazz2Class(clazz->interfaces[i]);
        if (child_instance) {
          retcode = markInstance(child_instance, fifo, flag);
          if (retcode < 0) {

            return retcode;

          }
          queued += retcode;
        }
#ifdef RUNTIME_CHECKS
        else {
          wabort(ABORT_WONKA, "No Class instance for class %k\n", clazz);
        }
#endif
      }
    }
  }

  if (clazz->staticFields) {
    woempa(1,"(GC) Marking static fields of class %k\n",clazz);
    for (i = 0; i < (w_int)clazz->numStaticFields; ++i) {
      if (isSet(clazz->own_fields[i].flags, FIELD_IS_REFERENCE)) {
        child_instance = (w_instance)clazz->staticFields[clazz->own_fields[i].size_and_slot];
        woempa(1, "(GC)    --> field %w of %k contains instance %p.\n", clazz->own_fields[i].name, clazz, child_instance);
        if (child_instance) {
          retcode = markInstance(child_instance, fifo, flag);
          if (retcode < 0) {

            return retcode;

          }
          queued += retcode;
        }
      }
      else {
        woempa(1, "(GC)    --> field %w of %k is primitive, getting out.\n",  clazz->own_fields[i].name, clazz);
        break;
      }
    }
  }

  return queued;
}

/*
** Mark one frame of a Java stack: we push onto the fifo every local variable
** and every stack item which looks like a reference and hasn't already been
** marked.
*/

w_int markFrameReachable(w_frame frame, w_fifo fifo, w_word flag) {

  w_int queued = 0;
  volatile w_slot item;
  w_instance child_instance;
  w_instance thisThread = frame->thread->Thread;
  w_method method = frame->method;
  w_int      retcode;

  if (method && isSet(method->flags, ACC_STATIC)) {
    woempa(1, "Frame %p is executing static method %m of %K, marking the latter\n", frame, method, method->spec.declaring_clazz);
    markClazzReachable(method->spec.declaring_clazz, fifo, flag);
  }
  item = (volatile w_slot) frame->jstack_base;
  while (item <= (volatile w_slot) frame->auxstack_base) {
    if (item == frame->jstack_top) {
      item = frame->auxstack_top + 1;
    }
    if (item->s == stack_trace && item->c) {
      child_instance = (w_instance) item->c;
      if (child_instance != thisThread) {
        retcode = markInstance(child_instance, fifo, flag);
        if (retcode < 0) {

          return retcode;

        }
        queued += retcode;
      }
    }
    item += 1;
  }

  return queued;

}

/*
** If a Thread is reachable (e.g. is reachable from the system thread group)
** and it has a valid Java stack then we mark every frame.
*/

w_int markThreadReachable(w_object object, w_fifo fifo, w_word flag) {

  w_thread thread = getWotsitField(object->fields, F_Thread_wotsit);
  w_int queued = 0;
  w_int retcode;
  volatile w_frame frame;
  volatile w_slot auxs;
  w_instance i;
#ifdef DEBUG
  w_int numFrames = 0;
#endif

  if (thread == NULL || thread->state == wt_dead) {
    return 0;
  }

  if (thread != marking_thread && isSet(thread->flags, WT_THREAD_NOT_GC_SAFE)) {
    wabort(ABORT_WONKA, "attempt by thread %t to mark thread %t while it is not in a safe state (current method is %M)\n", marking_thread, thread, thread->top->method);
    return -1;
  }

  if (thread->top && thread->top->method && (thread->top->method->flags & METHOD_IS_COMPILED)) {
    wabort(ABORT_WONKA, "attempt to mark thread %t while the top-most frame %p is controlled by a JITed method\n", thread, thread->top);
  }

  woempa(1, "Scanning aux stack of %t (%d items)\n", thread, last_slot(thread) - thread->top->auxstack_top);
  for (auxs = (volatile w_slot) last_slot(thread); auxs > (volatile w_slot) thread->top->auxstack_top; auxs -= 1) {
    if (auxs->c && (auxs->s == stack_trace || isMonitoredSlot(auxs))) {
      i = (w_instance) auxs->c;
      retcode = markInstance(i, fifo, flag);
      if (retcode < 0) {

        return retcode;

      }
      queued += retcode;
    }
  }
  
  if (thread->protected) {
    retcode = markInstance(thread->protected, fifo, flag);
    if (retcode < 0) {

      return retcode;

    }
    queued += retcode;
  }

  if (exceptionThrown(thread)) {
    retcode = markInstance(exceptionThrown(thread), fifo, flag);
    if (retcode < 0) {

      return retcode;

    }
    queued += retcode;
  }

  for (frame = (volatile w_frame) thread->top; frame; frame = (volatile w_frame) frame->previous) {
#ifdef DEBUG
    woempa(1, "Scanning frame[%d] of %t (%M)\n", numFrames, thread, frame->method);
    numFrames += 1;
#endif
    retcode = markFrameReachable(frame, fifo, flag);
    if (retcode < 0) {

      return retcode;
    }
    queued += retcode;
  }

#ifdef DEBUG
  woempa(1, "(GC) Enfifoed %d objects in %d frames of thread %w.\n", queued, numFrames, NM(thread));

  woempa(1, "(GC) Marked stack of thread %w: %d frames, %d queued.\n", NM(thread), numFrames, queued);
#endif

  return queued;

}

/*
** If a Throwable is reachable and it has a stack trace attached then we mark
** every class referenced from the stack trace.
*/

w_int markThrowableReachable(w_object object, w_fifo fifo, w_word flag) {
  w_Exr *record = getWotsitField(object->fields, F_Throwable_records);
  w_int queued = 0;
  w_int retcode;

  while (record) {
    if (record->method) {
      woempa(3, "%j record at position %d (%m:%d) refers to class %k, marking the latter\n", object->fields, record->position, record->method, record->pc, record->method->spec.declaring_clazz);
      retcode = markClazzReachable(record->method->spec.declaring_clazz, fifo, flag);
      if (retcode < 0) {

        return retcode;

      }

      queued += retcode;
      record = record->position ? record + 1 : NULL;
    }
  }

  return queued;
}

void markLoadedClass(w_word key, w_word value, void *pointer, void*dummy) {
  w_clazz clazz = (w_clazz)value;
  w_int  *counter = pointer;
  w_int retcode;

  if (*counter >= 0) {
    woempa(1, "--> marking %K\n", clazz);
    retcode = markClazzReachable(clazz, strongly_reachable_fifo, O_BLACK);
    if (retcode < 0) {
      *counter = retcode;
    }
    else {
      *counter += retcode;
    }
    woempa(1, "markLoadedClass(%K) returned %d\n", clazz, retcode);
  }
}

static w_int markClassLoaderReachable(w_instance loader, w_fifo fifo, w_word flag) {
  w_int  queued = 0;

  w_hashtable ht = loader2loaded_classes(loader);

  if (!ht) {

    return 0;

  }

  woempa(1, "Marking loaded classes of %j\n", loader);
  ht_iterate(ht, markLoadedClass, &queued, NULL);

  return queued;
}

/*
*/
w_int markReferentReachable(w_instance referent, w_word flag) {
  w_int  queued = 0;
  w_int  retcode = 0;
  w_object ref_obj;
  w_fifo fifo;
 
  if (referent == NULL) {

    return 0;

  }

  ref_obj = instance2object(referent);
  retcode = 0;

  FLYING_PIG_CHECK(ref_obj);

  fifo = flags2fifo(flag);

  if (isNotMarked(ref_obj, flag)) {
    setFlag(ref_obj->flags, flag);
    retcode = tryPutFifo(referent, fifo);
  }

  if (retcode < 0) {

    return retcode;

  }
  queued += retcode;

  return queued;
}

w_int markChildren(w_object o, w_fifo fifo, w_word flag);

static void finalizeReference(w_instance instance) {

#ifndef THREAD_SAFE_FIFOS
  x_mutex_lock(finalizer_fifo_mutex, x_eternal);
#endif
  if (tryPutFifo(instance, finalizer_fifo) >= 0) {
    setFlag(instance2flags(instance), O_FINALIZING);
    unsetFlag(instance2flags(instance), O_FINALIZABLE);
  }
#ifndef THREAD_SAFE_FIFOS
  x_mutex_unlock(finalizer_fifo_mutex);
#endif
}

/*
** Mark all instances in the fifo with the given flag, if they're not
** already marked with the same flag or one which `dominates' it the
** sense explained in the definition of isMarked() above.
** If we mark an instance then we also mark its Class (if not already
** marked) and append all its children to the fifo.  (`Children' means
** elements of a reference array or reference fields of a non-array
** instance). If it is an instance of Thread, Class, ClassLoader, or
** Reference (or a subclass) then there is also some type-specific
** scanning to be done.
*/

void enqueuedReference(void* reference) {
  if (putFifo(reference, enqueue_fifo) < 0) {
    wabort(ABORT_WONKA, "Could not push %j onto enqueue_fifo!\n", reference);
  }
}

w_int markFifo(w_fifo fifo, w_word flag) {

  w_instance parent_instance;
  w_object   parent_object;
  w_clazz    clazz;
  w_instance child_instance;
  w_int      i;
  w_int      retcode;
  w_int      queued = 0;

  woempa(1, "Marking FIFO %p, flag is 0x%02x, contains %d elements\n", fifo, flag, occupancyOfFifo(fifo));

  while ((parent_instance = getFifo(fifo))) {
    parent_object = instance2object(parent_instance);
    woempa(1, "(GC) Tracing instance %j\n", parent_instance);

    FLYING_PIG_CHECK(parent_object);

    setFlag(parent_object->flags, flag);

    /*
    ** Mark the Class of this instance.
    */
    child_instance = parent_object->clazz->Class;
    if (child_instance) {
      retcode = markInstance(child_instance, strongly_reachable_fifo, O_BLACK);
      if (retcode < 0) {

        return retcode;

      }
      queued += retcode;
    }
#ifdef RUNTIME_CHECKS
    else {
      wabort(ABORT_WONKA, "No Class instance for class %k\n", parent_object->clazz);
    }
#endif

    /*
    ** Mark the Thread which owns this instance's lock, if any.
    */
    if (isSet(parent_object->flags, O_HAS_LOCK)) {
      w_thread owner_thread = monitorOwner(parent_instance);
      if (owner_thread) {
        woempa(1, "Lock on %j is owned by %t\n", parent_instance, owner_thread);
        if (owner_thread->Thread) {
          woempa(1, "Marking %j reachable\n", owner_thread->Thread);
          retcode = markThreadReachable(instance2object(owner_thread->Thread), strongly_reachable_fifo, O_BLACK);
          if (retcode < 0) {

            return retcode;

          }
          queued += retcode;
        }
      }
    }

    /*
    ** If this is an instance of java.lang.Class, we have a lot of work to do.
    */
    if (parent_object->clazz == clazzClass && (clazz = getWotsitField(parent_instance, F_Class_wotsit))) {
      woempa(1,"(GC) Object %p is instance of %k\n",parent_object,parent_object->clazz);
      retcode = markClazzReachable(clazz, strongly_reachable_fifo, O_BLACK);
      if (retcode < 0) {

        return retcode;

      }
      queued += retcode;
    }
    else if(parent_object->clazz == clazzReferenceQueue) {
      //Mark the native fifo ...
      w_fifo qfifo = getWotsitField(parent_instance, F_ReferenceQueue_fifo);
      x_monitor monitor = getWotsitField(parent_instance, F_ReferenceQueue_lock);

      if(monitor && qfifo) {
        w_instance reference;
        x_monitor_eternal(monitor);
        forEachInFifo(qfifo,enqueuedReference);
        x_monitor_exit(monitor);
        while ((reference = getFifo(enqueue_fifo))) {
          markInstance(reference,fifo, flag);
        }
      }
    }

    /*
    ** If this is a thread, we scan the stack.
    */
    else if (isSet(parent_object->clazz->flags, CLAZZ_IS_THREAD) && getWotsitField(parent_instance, F_Thread_wotsit)) {
      woempa(1, "(GC) %p is instance of %k, a subclass of Thread.\n", parent_instance, parent_object->clazz);
      retcode = markThreadReachable(parent_object, fifo, flag);
      if (retcode < 0) {
        return retcode;
      }
      queued += retcode;
    }
    /*
    ** If this is a throwable, we scan the stack trace.
    */
    else if (isSet(parent_object->clazz->flags, CLAZZ_IS_THROWABLE) && getWotsitField(parent_instance, F_Throwable_records)) {
      woempa(1, "(GC) %p is instance of %k, a subclass of Throwable.\n", parent_instance, parent_object->clazz);
      retcode = markThrowableReachable(parent_object, fifo, flag);
      if (retcode < 0) {
        return retcode;
      }
      queued += retcode;
    }
    /*
    ** For a classloader, we scan all the classes it has loaded.
    */
    else if (isSet(parent_object->clazz->flags, CLAZZ_IS_CLASSLOADER)) {
      woempa(1, "(GC) %p is instance of %k, a subclass of ClassLoader.\n", parent_instance, parent_object->clazz);
      retcode = markClassLoaderReachable(parent_instance, fifo, flag);
      if (retcode < 0) {
        return retcode;
      }
      queued += retcode;
    }
    /*
    ** For a reference type, we scan the referent.
    */
    else if (isSet(parent_object->clazz->flags, CLAZZ_IS_REFERENCE)) {
      w_instance referent = getWotsitField(parent_instance, F_Reference_referent);
      woempa(7, "(GC) Object %p is instance of %k, a subclass of Reference\n", parent_object, parent_object->clazz);
      woempa(7, "(GC) It refers to %j\n", referent);
      if(isSet(parent_object->flags, O_ENQUEUEABLE)) {
        retcode = tryPutFifo(parent_instance, reference_fifo);
        if (retcode < 0) {
          wabort(ABORT_WONKA, "Couldn't add to reference_fifo (%d items), PANIC\n", occupancyOfFifo(reference_fifo));
        }
      }

      if (referent) {
        //w_object ref_obj = instance2object(referent);
        w_word reachability = 0;

        if (isSuperClass(clazzSoftReference,parent_object->clazz)) {
          reachability = killing_soft_references ? 0 : O_BLACK;
        }
        else if (isSuperClass(clazzWeakReference,parent_object->clazz)) {
          reachability = 0;
        }
        else if (isSuperClass(clazzPhantomReference,parent_object->clazz)) {
          reachability = O_PHANTOM_BLACK;
          woempa(7, "(GC) Marking phantom reference to %j\n", getWotsitField(parent_instance, F_Reference_referent));
        }
        else {
          wabort(ABORT_WONKA, "Eh??? %k is not a subclass of SoftReference, WeakReference, or PhantomReference!\n", parent_object->clazz);
        }

        if (reachability) {
          retcode = markReferentReachable(referent, reachability);
          if (retcode < 0) {
            return retcode;
          }
          queued += retcode;
        }
      }
    }

    /*
    ** Now call all the children...
    */
    
    if (parent_object->clazz->dims) {
      /*
      ** It's an array clazz; if the array clazz stores non primitives 
      ** (other arrays or references), put them all in the FIFO.
      */
      if (parent_object->clazz->dims > 1 || isNotSet(parent_object->clazz->previousDimension->flags, CLAZZ_IS_PRIMITIVE)) {
        woempa(1,"(GC) Tracing references from %k@%p\n",parent_object->clazz,parent_instance);
        for (i = 0; i < instance2Array_length(parent_instance); i++) {
          if ((child_instance = (w_instance)parent_instance[F_Array_data + i])) {
            retcode = markInstance(child_instance, fifo, flag);
            if (retcode < 0) {

              return retcode;

            }
            queued += retcode;
          } 
        } 
      }
    }
    else {
      /*
      ** It's a normal clazz, just go over all the fields that could contain 
      ** references to other instances.
      */
      retcode = markChildren(parent_object, fifo, flag);
      if (retcode < 0) {

        return retcode;

      }
      queued += retcode;


    }
  }

  woempa(1,"Finished marking FIFO %p\n", fifo);

  return queued;

}

#define GC_MAX_PRIORITY 10

w_int markChildren(w_object o, w_fifo fifo, w_word flag) {

  w_instance child_instance;
  w_int      queued;
  w_clazz    clazz;
  w_int      i;
  w_int      retcode;

  queued = 0;
  clazz = o->clazz;
  woempa(1, "Tracing instance fields of %k@%p\n", clazz, o->fields);
  for (i = clazz->instanceSize - clazz->numReferenceFields; i < clazz->instanceSize; ++i) {
    if ((child_instance = (w_instance)o->fields[i])) {
      retcode = markInstance(child_instance, fifo, flag);
      if (retcode < 0) {

        return retcode;

      }
      queued += retcode;
    }
  }

  return queued;

}

w_int gc_phase = GC_PHASE_UNREADY;

/*
 * In the Preparation phase, we take a snapshot of the instance heap
 *  and build a fifo which contains all items in the snapshot.
 */

#ifdef DEBUG
typedef struct w_Chs {
  w_int count_1;
  w_int count_2;
  w_int count_3;
} w_Chs;

typedef struct w_Chs * w_chs;
#endif

#ifdef USE_OBJECT_HASHTABLE
void preparation_iteration(w_word key, w_word value, void * arg1, void *arg2) {
  w_int retcode = 0;
  w_object object = (w_object)key;

#ifdef CLASSES_HAVE_INSTANCE_CACHE
  if (isSet(object->flags, O_CACHED)) {
    return TRUE;
  }
#endif

  unsetFlag(object->flags, O_BLACK | O_NEAR_BLACK);

  if (!object->clazz) {
    wabort(ABORT_WONKA, "((w_object)%p)->clazz == NULL\n", object);
  }

  if (isSet(object->flags, (O_FINALIZING | O_FINALIZABLE))) {
    w_size i;
    w_instance child_instance;
    w_object child_object;
    for (i = object->clazz->instanceSize - object->clazz->numReferenceFields; i < object->clazz->instanceSize; ++i) {
      child_instance = (w_instance)object->fields[i];
      if (child_instance) {
        child_object = instance2object(child_instance);
        FLYING_PIG_CHECK(child_object);

        setFlag(child_object->flags, O_FINALIZE_BLACK);
        retcode = tryPutFifo(child_instance, finalize_reachable_fifo);
        if (retcode < 0) {
          wabort(ABORT_WONKA, "Couldn't add to finalize_reachable_fifo (%d items), PANIC\n", occupancyOfFifo(finalize_reachable_fifo));
        }
      }
    }
  }
  woempa(1, "Pushing %j onto window_fifo, setting flag O_WINDOW\n", object->fields);
  setFlag(object->flags, O_WINDOW);
  retcode = tryPutFifo(object->fields, window_fifo);
  if (retcode < 0) {
    wabort(ABORT_WONKA, "Couldn't add to window_fifo (%d items), PANIC\n", occupancyOfFifo(window_fifo));
  }
}
#else
w_boolean preparation_iteration(void * mem, void * arg) {
  w_int retcode = 0;
  w_object object = chunk2object(mem);

#ifdef CLASSES_HAVE_INSTANCE_CACHE
  if (isSet(object->flags, O_CACHED)) {
    return TRUE;
  }
#endif

  unsetFlag(object->flags, O_BLACK | O_NEAR_BLACK);

  if (!object->clazz) {
    wabort(ABORT_WONKA, "((w_object)%p)->clazz == NULL\n", object);
  }

  if (isSet(object->flags, (O_FINALIZING | O_FINALIZABLE))) {
    w_size i;
    w_instance child_instance;
    w_object child_object;
    for (i = object->clazz->instanceSize - object->clazz->numReferenceFields; i < object->clazz->instanceSize; ++i) {
      child_instance = (w_instance)object->fields[i];
      if (child_instance) {
        child_object = instance2object(child_instance);
        FLYING_PIG_CHECK(child_object);

        setFlag(child_object->flags, O_FINALIZE_BLACK);
        retcode = tryPutFifo(child_instance, finalize_reachable_fifo);
        if (retcode < 0) {
          wabort(ABORT_WONKA, "Couldn't add to finalize_reachable_fifo (%d items), PANIC\n", occupancyOfFifo(finalize_reachable_fifo));
        }
      }
    }
  }
  woempa(1, "Pushing %j onto window_fifo, setting flag O_WINDOW\n", object->fields);
  setFlag(object->flags, O_WINDOW);
  retcode = tryPutFifo(object->fields, window_fifo);
  if (retcode < 0) {
    wabort(ABORT_WONKA, "Couldn't add to window_fifo (%d items), PANIC\n", occupancyOfFifo(window_fifo));
  }
  
  return TRUE;

}
#endif

static void prepreparation(w_thread thread) {
  x_status status;

  woempa(7, "%t: start locking other threads\n", thread);
  if (number_unsafe_threads < 0) {
    wabort(ABORT_WONKA, "number_unsafe_threads = %d!", number_unsafe_threads);
  }
  x_monitor_eternal(safe_points_monitor);
#ifdef JDWP
  while(isSet(blocking_all_threads, BLOCKED_BY_JDWP)) {
    woempa(7, "JDWP is blocking all threads, not possible to run yet.\n");
    status = x_monitor_wait(safe_points_monitor, GC_STATUS_WAIT_TICKS);
  }
#endif
  woempa(2, "preprepare: %t setting blocking_all_threads to BLOCKED_BY_GC\n", thread);
  setFlag(blocking_all_threads, BLOCKED_BY_GC);
  while (number_unsafe_threads > 0) {
    woempa(7, "number_unsafe_threads is %d, waiting\n", number_unsafe_threads);
    status = x_monitor_wait(safe_points_monitor, GC_STATUS_WAIT_TICKS);
  }
  x_monitor_notify_all(safe_points_monitor);
  x_monitor_exit(safe_points_monitor);
  woempa(7, "%t: finished locking other threads\n", marking_thread);
#ifdef TRACE_MEM_ALLOC
  _heapCheck("collector.c", 1571);
#endif
  x_thread_priority_set(thread->kthread, priority_j2k(10, 1));
}

static void postmark(w_thread thread) {
  x_thread_priority_set(thread->kthread, thread->kpriority);

  woempa(7, "%t: start unlocking other threads\n", marking_thread);
  x_monitor_eternal(safe_points_monitor);
  woempa(2, "postmark: %t setting blocking_all_threads to 0\n", marking_thread);
  unsetFlag(blocking_all_threads, BLOCKED_BY_GC);
  x_monitor_notify_all(safe_points_monitor);
  x_monitor_exit(safe_points_monitor);
  woempa(7, "%t: finished unlocking other threads\n", marking_thread);
  x_thread_priority_set(thread->kthread, priority_j2k(thread->jpriority, 0));
}

w_int preparationPhase(void) {
  w_int memory_busy = memory_total - x_mem_avail();

  while (getFifo(window_fifo));
  while (getFifo(strongly_reachable_fifo));
  while (getFifo(phantom_reachable_fifo));
  while (getFifo(finalize_reachable_fifo));

#ifdef CONTRACT_FIFOS
  retcode = contractFifo(window_fifo);
  woempa(1, "Contracted window_fifo, freed %d bytes\n", retcode);
  retcode = contractFifo(strongly_reachable_fifo);
  woempa(1, "Contracted strongly_reachable_fifo, freed %d bytes\n", retcode);
  retcode = contractFifo(phantom_reachable_fifo);
  woempa(1, "Contracted phantom_reachable_fifo, freed %d bytes\n", retcode);
  retcode = contractFifo(finalize_reachable_fifo);
  woempa(1, "Contracted finalize_reachable_fifo, freed %d bytes\n", retcode);
#endif

#ifdef REDUCE_FIFOS
  retcode = reduceFifo(window_fifo);
  woempa(1, "Reduced window_fifo, freed %d bytes\n", retcode);
  retcode = reduceFifo(strongly_reachable_fifo);
  woempa(1, "Reduced strongly_reachable_fifo, freed %d bytes\n", retcode);
  retcode = reduceFifo(phantom_reachable_fifo);
  woempa(1, "Reduced phantom_reachable_fifo, freed %d bytes\n", retcode);
  retcode = reduceFifo(finalize_reachable_fifo);
  woempa(1, "Reduced finalize_reachable_fifo, freed %d bytes\n", retcode);
#endif

  memory_load_factor = memory_busy / (memory_total - memory_busy);
  if (memory_load_factor < 0) {
    memory_load_factor = 0;
  }
  killing_soft_references = (*gc_kicks_pointer) || (memory_load_factor > 1);

#ifdef USE_OBJECT_HASHTABLE
  ht_iterate(object_hashtable, preparation_iteration, NULL, NULL);
#else
  x_mem_lock(x_eternal);
  x_mem_scan(x_eternal, OBJECT_TAG, preparation_iteration, NULL);
  x_mem_unlock();
#endif

  return 0;
}

w_boolean ReferenceQueue_append(JNIEnv *env, w_instance this, w_instance reference);

static void miniSweepReferences(void) {
  w_instance parent_instance;
  w_instance referent_instance;
  w_object referent_object;

  woempa(7, "(GC) Performing mini-sweep of references.\n");
  while ((parent_instance = getFifo(reference_fifo))) {
    if ((referent_instance = getWotsitField(parent_instance, F_Reference_referent))) {
      referent_object = instance2object(referent_instance);
      if (isNotSet(referent_object->flags, O_BLACK | O_FINALIZE_BLACK)) {
        if(isSuperClass(clazzPhantomReference,instance2clazz(parent_instance))) {
          if(isSet(referent_object->flags, O_FINALIZABLE | O_FINALIZING)) {
            continue;
          }
        }
        else {
          woempa(7, "(GC) Clearing reference %j from %j \n", referent_instance, parent_instance);
          clearWotsitField(parent_instance, F_Reference_referent);
        }
        if(getReferenceField(parent_instance,F_Reference_ref_queue)) { 
          woempa(7, "(GC): enqueueing %j\n", parent_instance);
          ReferenceQueue_append(NULL,getReferenceField(parent_instance,F_Reference_ref_queue), parent_instance);
        } else {
          unsetFlag(instance2object(parent_instance)->flags, O_ENQUEUEABLE);
        }
      }
    }
  }
}

static w_size gc_start_ticks;

static void thread_iteration(w_word key, w_word value, void * arg1, void *arg2) {
  w_thread thread = (w_thread)value;
  w_instance Thread = thread->Thread;

  if (Thread && thread->state != wt_dead && thread->state != wt_unstarted) {
    markInstance(Thread, strongly_reachable_fifo, O_BLACK);
  }
}

#ifdef MONITOR_STRING_HASHTABLE
w_int total_bytes;
w_int total_references;
w_int total_ref_bytes;

static void stringstats(w_word key, w_word value) {
  w_string s = (w_string) key;
  total_bytes += string_length(s);
  total_references += s->refcount;
  total_ref_bytes += s->refcount * string_length(s);
}
#endif

/*
 * In the Mark phase, we first mark all the (transient) `roots':
 *  - system thread group (and hence all threads)
 *  - system class hashtable (static fields of system classes)
 *  - global references hashtable
 * For every item we mark, we append all references it contains to
 * the appropriate fifo (strong, weak, or phantom).  Therefore
 * by the end of this phase we have marked all items in the window
 * which were reachable at the time the snapshot was taken.
 *  We now make one pass over the
 * strong, weak and phantom fifo's (in that order) before proceeding
 * to the Sweep phase.
 *
 * Returns the number of objects marked if successful (useless, and
 * probably inaccurate, but at least nonnegative), or a negative error
 * code.  In the latter case the mark phase did not complete, and it
 * would be folly to sweep.
 */
w_int markPhase(void) {

  w_fifo     temp_fifo;
  w_instance instance;
  w_int      retcode;
  w_int      marked = 0;

  woempa(7, "(GC) Marking globals hashtable.\n");
  temp_fifo = ht_list_keys(globals_hashtable);
  if (!temp_fifo) {
    woempa(7, "ht_list_keys(globals_hashtable) returned NULL, quitting markPhase\n");

    return -1;

  }
  while ((instance = getFifo(temp_fifo))) {
    retcode = markInstance(instance, strongly_reachable_fifo, O_BLACK);
    if (retcode < 0) {

      return retcode;

    }
    marked += retcode;
  }
  releaseFifo(temp_fifo);

  woempa(7, "(GC) Marking thread hashtable.\n");
  ht_iterate(thread_hashtable, thread_iteration, NULL, NULL);

  do {
    //if (times_round++) printf("Loop %d, ticks = %ud\n", times_round, x_time_get() - gc_start_ticks);
    // TODO: do we still need this if we are marking thread_hashtable?
    woempa(7, "(GC) Marking system ThreadGroup.\n");
    retcode = markInstance(I_ThreadGroup_system, strongly_reachable_fifo, O_BLACK);
    if (retcode < 0) {
      woempa(7, "markInstance(I_ThreadGroup_system, strongly_reachable_fifo, O_BLACK) returned %d, quitting markPhase\n", retcode);

      return -1;

    }
    marked += retcode;

    woempa(7, "(GC) Marking strongly_reachable_fifo.\n");
    retcode = markFifo(strongly_reachable_fifo, O_BLACK);
    if (retcode < 0) {
      woempa(7, "markFifo(strongly_reachable_fifo, O_BLACK) returned %d, quitting markPhase\n", retcode);

      return retcode;

    }
    marked += retcode;

    woempa(7, "(GC) Marking phantom_reachable_fifo.\n");
    retcode = markFifo(phantom_reachable_fifo, O_PHANTOM_BLACK);
    if (retcode < 0) {
      woempa(7, "markFifo(phantom_reachable_fifo, O_PHANTOM_BLACK) returned %d, quitting markPhase\n", retcode);

      return retcode;

    }
    marked += retcode;

    woempa(7, "(GC) Marking finalize_reachable_fifo.\n");
    retcode = markFifo(finalize_reachable_fifo, O_FINALIZE_BLACK);
    if (retcode < 0) {
      woempa(7, "markFifo(finalize_reachable_fifo, O_FINALIZE_BLACK) returned %d, quitting markPhase\n", retcode);

      return -1;

    }
    woempa(1, "(GC) Marked %d objects FINALIZE_BLACK\n", retcode);
    marked += retcode;

  } while (
           !isEmptyFifo(strongly_reachable_fifo) || 
           !isEmptyFifo(finalize_reachable_fifo) || 
           !isEmptyFifo(phantom_reachable_fifo));

  miniSweepReferences();

#ifdef MONITOR_STRING_HASHTABLE
  w_printf("string_hashtable contains %d entries\n", string_hashtable->occupancy);
  total_bytes = 0;
  total_references = 0;
  total_ref_bytes = 0;
  ht_lock(string_hashtable);
  ht_every(string_hashtable, stringstats);
  ht_unlock(string_hashtable);
  w_printf("total of %d references > %d per string\n", total_references, total_references / string_hashtable->occupancy);
  w_printf("total bytes stored = %d, reference-weighted = %d\n", total_bytes, total_ref_bytes);
#endif

  return marked;

}

/*
 * Each of the following checks returns WONKA_TRUE if the instance may be 
 * collected, WONKA_FALSE otherwise. An object is eligible for GC if _all_
 * these tests return WONKA_TRUE.
 *
 * "Strong" references are the conventional kind (e.g. a field or an array 
 * member).
 */
static w_boolean checkStrongRefs(w_object object) {

  if (isSet(object->flags, O_BLACK )) {
    woempa(1, "Not collecting %j because it is strongly reachable\n", object->fields);

    return WONKA_FALSE;

  }

#ifdef CLASSES_HAVE_INSTANCE_CACHE
  if (isSet(object->flags, O_CACHED)) {
    woempa(1, "Not collecting %j because it is in a cache\n", object->fields);

    return WONKA_FALSE;

  }
#endif

#ifdef JDWP
  if (isSet(object->flags, O_JDWP_BLACK)) {
    woempa(1, "Not collecting %j because it is set non-collectable by JDWP\n", object->fields);

    return WONKA_FALSE;

  }
#endif

  return WONKA_TRUE;

}

/*
 * If an object is unreachable but has a finalizer, we enqueue it for 
 * finalization (if not already done). The object cannot be collected until
 * its finalizer has run.
 */
static w_boolean checkFinalization(w_object object) {

  if (isSet(object->flags, (O_FINALIZING))) {
    woempa(7, "Not collecting %j because it is currently being enqueued or finalized\n", object->fields);

    return WONKA_FALSE;

  }

  if (isNotSet(object->flags, O_FINALIZABLE)) {

    return WONKA_TRUE;

  }

  woempa(1, "Not collecting %j because it is finalizable.\n", object->fields);
  woempa(1, "Putting %j onto finalization queue.\n", object->fields);
  finalizeReference(object->fields);

  return WONKA_FALSE;

}

/*
 * If an object is reachable via phantom references, or it is referred to by
 * some object which has a finalizer (even if the referring object is 
 * unreachable) then it cannot yet be collected.   
 */
static w_boolean checkPhantomRefs(w_object object) {

  if (isSet(object->flags, O_PHANTOM_BLACK)) {
    woempa(7, "Not collecting %j because it is reachable via phantom reference(s)\n", object->fields);
    FLYING_PIG_CHECK(object);

    return WONKA_FALSE;

  }
  if (isSet(object->flags, O_FINALIZE_BLACK)) {
    woempa(1, "Not collecting %j because it is reachable via yet-to-be-finalized object(s)\n", object->fields);
    FLYING_PIG_CHECK(object);

    return WONKA_FALSE;

  }

  return WONKA_TRUE;

}

/*
** Release all the String instances in the dead_string_fifo.
*/
static w_int collect_dead_strings(void) {
  w_instance instance;
  w_int bytes = 0;

  ht_lock(string_hashtable);
  while ((instance = getFifo(dead_string_fifo))) {
    w_string string = getWotsitField(instance, F_String_wotsit);
    if (string) {
      if (checkStrongRefs(instance2object(instance))) {
        clearWotsitField(instance, F_String_wotsit);
        deregisterString(string);
        bytes += reallyReleaseInstance(instance2object(instance));
      }
    }
  }
  ht_unlock(string_hashtable);

  return bytes;
}

extern w_hashtable lock_hashtable;

/*
** Release all the locks in the dead_lock_fifo.
*/
static w_int collect_dead_locks(void) {
  w_instance locked_instance;
  w_int count = 0;

  ht_lock(lock_hashtable);
  while ((locked_instance = getFifo(dead_lock_fifo))) {
    x_monitor mon = (x_monitor) ht_erase_no_lock(lock_hashtable, (w_word)locked_instance);
    if (mon) {
      x_monitor_delete(mon);
      releaseMem(mon);
    }
#ifdef RUNTIME_CHECKS
    else {
      wabort(ABORT_WONKA, "No monitor corresponding to %p\n", locked_instance);
    }
#endif
    ++count;
  }
  ht_unlock(lock_hashtable);

  return count * sizeof(x_Monitor);
}

/*
** Array used to sort the dead classes which must be collected.
*/
static w_clazz *dead_clazz_array;

/*
** Hashtable used to aid in the sorting.
*/
static w_hashtable dead_clazz_hashtable;

/*
** Swap function for the sort.
*/
static void dead_clazz_swap(int i, int j) {
  w_clazz this_clazz = dead_clazz_array[i];
  w_clazz other_clazz = dead_clazz_array[j];

  dead_clazz_array[i] = other_clazz;
  dead_clazz_array[j] = this_clazz;
  ht_write(dead_clazz_hashtable, (w_word)this_clazz, (w_word)j);
  ht_write(dead_clazz_hashtable, (w_word)other_clazz, (w_word)i);
}

/*
** Collect all the classes on dead_clazz_fifo.
** First we sort them such that a subclass always precedes its superclass and
** an implementation always precedes the interface - it's safer that way.
*/
static w_int collect_dead_clazzes(void) {
  w_int bytes_freed = 0;
  w_int i = 0;
  w_int j;
  w_int k;
  w_int n = occupancyOfFifo(dead_clazz_fifo);
  w_clazz this_clazz;
  w_clazz other_clazz;

  dead_clazz_array = allocMem(n * sizeof(w_clazz));
  dead_clazz_hashtable = ht_create("hashtable:dead classes", n * 2 + 7, NULL, NULL, 0, 0xffffffff);
  while ((this_clazz = getFifo(dead_clazz_fifo))) {
    ht_write(dead_clazz_hashtable, (w_word)this_clazz, i);
    dead_clazz_array[i++] = this_clazz;
  }

  for (i = 1; i < n; ++i) {
    w_int clazz_state;
    this_clazz = dead_clazz_array[i];
    clazz_state = getClazzState(this_clazz);
    if (clazz_state < CLAZZ_STATE_LOADED || clazz_state == CLAZZ_STATE_BROKEN) {
      continue;
    }

#ifdef CLASSES_HAVE_INSTANCE_CACHE
    if (this_clazz->cache_fifo) {
      w_object cached;
#ifndef THREAD_SAFE_FIFOS
      x_mutex_lock(this_clazz->cache_mutex, x_eternal);
#endif
      while((cached = getFifo(this_clazz->cache_fifo))) {
        reallyReallyReleaseInstance(cached);
      }
      releaseFifo(this_clazz->cache_fifo);
#ifndef THREAD_SAFE_FIFOS
      x_mutex_unlock(this_clazz->cache_mutex);
      x_mutex_delete(this_clazz->cache_mutex);
      releaseMem(this_clazz->cache_mutex);
#endif
    }
#endif

    for (k = 0; k < this_clazz->numSuperClasses; ++k) {
      other_clazz = this_clazz->supers[k];
      j = (w_int)ht_read(dead_clazz_hashtable, (w_word)other_clazz);
      if (j >= 0 && j < i) {
        dead_clazz_swap(i, j);
        i = j;
      }
    }

    for (k = 0; k < this_clazz->numDirectInterfaces; ++k) {
      other_clazz = this_clazz->interfaces[k];
      j = (w_int)ht_read(dead_clazz_hashtable, (w_word)other_clazz);
      if (j >= 0 && j < i) {
        dead_clazz_swap(i, j);
        i = j;
      }
    }

    other_clazz = this_clazz->previousDimension;
    if (other_clazz) {
       j = (w_int)ht_read(dead_clazz_hashtable, (w_word)other_clazz);
        if (j >= 0 && j < i) {
        dead_clazz_swap(i, j);
        i = j;
      }
    }
  }

  for (i = 0; i < n; ++i) {
    this_clazz = dead_clazz_array[i];
    woempa(7, "Burying dead %K\n", this_clazz);
    if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
      w_printf("GC: dead %K\n", this_clazz);
    }
    if (this_clazz->previousDimension) {
      this_clazz->previousDimension->nextDimension = NULL;
    }
    bytes_freed += destroyClazz(this_clazz);
  }
  ht_destroy(dead_clazz_hashtable);
  releaseMem(dead_clazz_array);

  return bytes_freed;
}

/*
** Sweep the window_fifo until either at least `target' bytes are freed
** or the fifo is exhausted.  Caller must own gc_monitor.
*/

w_size sweep(w_int target) {
  w_instance instance;
  w_object   object;
  w_int      object_size;
  w_int      objects_examined = 0;
  w_int      objects_freed = 0;
  w_int      bytes_freed = 0;
  w_size     objects_collected = 0;
  w_size     bytes_collected = 0;
  w_int      do_collect;

  if (gc_phase < GC_PHASE_SWEEP) {
    woempa(9, "Not in SWEEP phase.\n");

    return 0;

  }

  while (window_fifo) {
    woempa(1, "getFifo(window_fifo)\n");
    instance = getFifo(window_fifo);
    woempa(1, "instance %p\n", instance);
    if (!instance) {
      if (!isEmptyFifo(window_fifo)) {
        woempa(9, "Hole in window fifo!\n");
        continue;
      }

      woempa(7, "Exhausted window_fifo after collecting %d bytes in %d objects.\n", bytes_freed, objects_freed);

      break;

    }
#ifdef DEBUG
    else if (instance == (w_instance)0xaaaaaaaa) {
      woempa(7, "Oo er\n");
      continue;
    }
#endif
    object = instance2object(instance);
    woempa(1, "object %p -> clazz %p\n", object, object->clazz);
    if (object->clazz) {
      woempa(1, "Examining %k@%p\n", object->clazz, instance);
    }
    else {
      woempa(9, "((w_object)%p)->clazz == NULL\n", object);
      continue;
    }
    ++objects_examined;
    unsetFlag(object->flags, O_WINDOW);
    if ( checkStrongRefs(object)
      && checkFinalization(object)
      && checkPhantomRefs(object)
       ) {

#ifdef PIGS_MIGHT_FLY
      if (isNotSet(object->flags, O_GARBAGE)) {
        woempa(1, "Marking %j as garbage\n", object->fields);
        setFlag(object->flags, O_GARBAGE);
      }
      else {
        woempa(1, "%j was already marked garbage, collecting it\n", object->fields);
#endif
        do_collect = 1;
        if (isSet(object->clazz->flags, CLAZZ_IS_THREAD)) {
          w_thread thread = getWotsitField(object->fields, F_Thread_wotsit);

#ifdef ENABLE_THREAD_RECYCLING
#else
          if (thread) {
            if (thread->state != wt_dead && thread->state != wt_unstarted) {
              woempa(9, "Hold on a moment - thread '%t' is still running...\n", thread);
              do_collect = 0;
            }
            else if (thread->kthread && thread->kthread->waiting_on) {
#ifdef OSWALD
#error For OSWALD we need to do something elsei here, TBD
#endif
              woempa(9, "Hold on a moment - thread '%w' is still waiting on monitor %p...\n", thread, thread->kthread->waiting_on);
              do_collect = 0;
            }
          }
#endif
        }
        else if (isSet(object->clazz->flags, CLAZZ_IS_CLASSLOADER)) {
          w_int ndef = numberOfDefinedClasses(object->fields);
          if (ndef) {
#ifdef DEBUG
            woempa(9, "Hold on a moment - loaded class hashtable of %j still holds %d classes defined by this loader\n", object->fields, ndef);
#endif
            do_collect = 0;
          }
        }

        if (do_collect) {
          object_size = releaseInstance(object);
          bytes_freed += object_size;
        }
        objects_freed += do_collect;

        if (bytes_freed >= target) {
          woempa(1, "Target was %d, freed %d, job done.\n", target, bytes_freed);

          break;

        }
#ifdef PIGS_MIGHT_FLY
      }
#endif
    }
#ifdef INSTANCE_STATS
    else if (instance_stat_hashtable) {
      w_size count = ht_read(instance_stat_hashtable, object->clazz);
      ht_write(instance_stat_hashtable, object->clazz, count + 1);
    }
#endif
  }
      if (!isEmptyFifo(dead_string_fifo)) {
        bytes_freed += collect_dead_strings();
      }
      if (!isEmptyFifo(dead_lock_fifo)) {
        bytes_freed += collect_dead_locks();
      }
      if (!isEmptyFifo(dead_clazz_fifo)) {
        bytes_freed += collect_dead_clazzes();
      }

  woempa(7, "(GC) Collected %i bytes from %d objects out of %d.\n", bytes_freed, objects_freed, objects_examined);

#ifdef USE_DISCARD_COLLECT
  x_mem_collect(&bytes_collected, &objects_collected);
  woempa(1, "(GC) Oswald sez: Collected %i bytes from %d objects.\n", bytes_collected, objects_collected);
#endif

  return bytes_freed;

}

void sweepPhase(void) {
  w_size collected = sweep(2000000000);

  if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
    w_printf("GC: thread %t swept %d bytes\n", currentWonkaThread, collected);
  }
}

/*
** List of w_reclaim_listener's, and a monitor to protect access thereto.
*/

w_wordset reclaim_listener_list = NULL;
x_Monitor reclaim_listener_Monitor;
x_monitor reclaim_listener_monitor = NULL;

static int enter_reclaim_listener_monitor(void) {
  x_status status = x_monitor_eternal(reclaim_listener_monitor);

  if (status == xs_no_instance) {

    return FALSE;

  }
  else if (status != xs_success) {
    wabort(ABORT_WONKA, "Vreed - x_monitor_eternal(reclaim_listener_monitor) returned %d\n", status);
  }
  woempa(1, "Entered reclaim_listener_monitor, status = %d\n", status);

  return TRUE;
}

static void exit_reclaim_listener_monitor(void) {
  x_status status = x_monitor_exit(reclaim_listener_monitor);

  if (status != xs_success) {
    wabort(ABORT_WONKA, "Vreed - x_monitor_exit(reclaim_listener_monitor) returned %d\n", status);
  }
    woempa(1, "Left reclaim_listener_monitor, status = %d\n", status);
}

/*
** We only start making callbacks after accumulating 'reclaim_threshold' bytes'
** worth of requests.  The requests are accumulated in reclaim_accumulator.
*/

#ifdef DISTRIBUTED_GC
static volatile w_int reclaim_threshold;
volatile w_int reclaim_accumulator = 0;
#endif

void registerReclaimCallback(w_reclaim_callback callback) {

  x_status status;

  if (!reclaim_listener_monitor) {
    reclaim_listener_monitor = &reclaim_listener_Monitor;
    status = x_monitor_create(reclaim_listener_monitor);
  }

  while (!enter_reclaim_listener_monitor()) {
    x_thread_sleep(1);
  }
  if (!addToWordset(&reclaim_listener_list, (w_word)callback)) {
    woempa(9, "Was not able to add reclaim listener to list\n");
  }
  exit_reclaim_listener_monitor();

}

void deregisterReclaimCallback(w_reclaim_callback callback) {

  while (!enter_reclaim_listener_monitor()) {
    x_thread_sleep(1);
  }
  removeFromWordset(&reclaim_listener_list, (w_word)callback);
  exit_reclaim_listener_monitor();

}

/*
** Function gc_reclaim() is called when we would like to reclaim `requested'
** bytes of memory.  Note: after calling the callbacks we reset the accumulator
** to zero even if the amount reclaimed fell short of that requested.  We
** assume that the inability (or unwillingness) to release memory is likely
** to last a little while, and setting reclaim_accumulator = remaining would
** tend to trigger another reclaim cycle rather soon.  For further study.
*/

#ifdef JDWP
extern w_thread jdwp_thread;
#endif

w_size gc_reclaim(w_int requested, w_instance caller) {

#ifdef DISTRIBUTED_GC
  w_size   i;
  w_int   reclaimed_this_cycle;
  w_int   remaining = 0;
  w_int   initial = 0;
  w_thread thread = currentWonkaThread;
  w_int weighted = requested * (memory_load_factor + 1);

  if (requested < 0) {
    woempa(1, "requested < 0, ignoring\n");

    return 0;

  }

  if (!reclaim_listener_list) {
    woempa(1, "No reclaim_listener_list, no fun\n");
    if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
      w_printf("GC: No reclaim_listener_list\n");
    }

    return 0;

  }

  if (isSet(blocking_all_threads, BLOCKED_BY_GC)) {
    if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
      w_printf("GC: cowardly refusal by thread %t to start a rival garbage collection cycle\n", thread);
      reclaim_accumulator /= 2;
    }
    return 0;
  }
#ifdef JDWP
  else if (isSet(blocking_all_threads, BLOCKED_BY_JDWP)) {
    if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
      w_printf("GC: cowardly refusal by thread %t to start garbage collection cycle while JDWP is suspending VM\n", thread);
    }
    return 0;
  }
#endif
  
  reclaim_accumulator += weighted;
  remaining = initial = reclaim_accumulator;

  if (x_mem_avail() - requested <= min_heap_free || reclaim_accumulator > reclaim_threshold) {
    if (isSet(verbose_flags, VERBOSE_FLAG_GC) && sizeOfWordset(&reclaim_listener_list) && gc_phase != GC_PHASE_UNREADY) {
      w_printf("GC: thread %t trying to reclaim %d bytes\n", thread, remaining);
    }

    if (thread 
     && thread != marking_thread && thread != sweeping_thread 
#ifdef JDWP
     && thread != jdwp_thread 
#endif
     && threadIsSafe(thread) && enter_reclaim_listener_monitor()
       ) {
      reclaimed_this_cycle = 0;
      for (i= 0; remaining > 0 && i < sizeOfWordset(&reclaim_listener_list); ++i) {
        w_reclaim_callback callback = (w_reclaim_callback)elementOfWordset(&reclaim_listener_list, i);
        exit_reclaim_listener_monitor();
        reclaimed_this_cycle += callback(remaining * (memory_load_factor + 1), caller);
        if (!enter_reclaim_listener_monitor()) {
          break;
        }
      }
      exit_reclaim_listener_monitor();
      remaining = remaining - reclaimed_this_cycle;
      if (isSet(verbose_flags, VERBOSE_FLAG_GC) && sizeOfWordset(&reclaim_listener_list) && gc_phase != GC_PHASE_UNREADY) {
        w_printf("GC: thread %t was able to reclaim %d bytes\n", thread, initial - remaining);
      }
      reclaim_accumulator = 0;
    }
    else if (thread) {
      if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
        w_printf("GC: thread %t postponing request for %d bytes because %s\n", thread, requested,
          thread == marking_thread ? " is marking_thread" :
          thread == sweeping_thread ? " is sweeping_thread" :
#ifdef JDWP
          thread == jdwp_thread ? " is JDWP thread" :
#endif
          !threadIsSafe(thread) ? " is not GC safe" :
          " enter_reclaim_listener_monitor() failed");
      }
      setFlag(thread->flags, WT_THREAD_GC_PENDING);
      thread->to_be_reclaimed += weighted;
      reclaim_accumulator -= weighted;
    }
  }

  return initial - remaining;
#else
  return 0;
#endif

}

/*
** Function internal_reclaim_callback calls gc_request() to try to reclaim memory.
*/

w_size internal_reclaim_callback(w_int requested, w_instance instance) {

  w_size reclaimed = 0;
  x_status status;
  w_int monitor_attempts = 0;
  const w_int max_monitor_attempts = 10;

  status = x_monitor_enter(gc_monitor, 0);
  while ((status != xs_success) && (++monitor_attempts) < max_monitor_attempts) {
    woempa(7, "Could not acquire gc_monitor, retrying.\n");
    x_thread_sleep(1);
    if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
      w_printf("GC: thread %t found gc_monitor busy, retry #%d\n", currentWonkaThread, monitor_attempts);
    }
    status = x_monitor_enter(gc_monitor, 0);
  }
  if (status != xs_success) {
    woempa(7, "Could not acquire gc_monitor for internal_reclaim_callback.\n");
    if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
      w_printf("GC: thread %t was unable to reclaim any memory (gc_monitor busy)\n", currentWonkaThread);
    }
  }
  else {
    reclaimed += gc_request(requested);
    woempa(1, "Called with requested = %d, instance = %p : reclaimed %d\n", requested, instance, reclaimed);
    GC_MONITOR_EXIT
  }

  return reclaimed;

}

/*
** Function gc_create() is called by the constructor of GarbageCollector
** (which is a singleton, so this code is only executed once).
*/

void gc_create(JNIEnv *env, w_instance theGarbageCollector) {
  gc_thread = JNIEnv2w_thread(env);
  gc_monitor = getMonitor(theGarbageCollector);
  gc_kicks_pointer = (void*)wordFieldPointer(theGarbageCollector, F_GarbageCollector_kicks);
  instance_returned = 0;
  marking_thread = NULL;
  sweeping_thread = NULL;
  memory_total = x_mem_total() - min_heap_free;
  memory_load_factor = 1;
  reclaim_threshold = memory_total / 3;
#ifndef THREAD_SAFE_FIFOS
  finalizer_fifo_mutex = &finalizer_fifo_Mutex;
  x_mutex_create(finalizer_fifo_mutex);
#endif
  window_fifo = allocThreadSafeFifo(WINDOW_FIFO_LEAF_SIZE);
  expandFifo(16384, window_fifo);
  strongly_reachable_fifo = allocFifo(STRONG_FIFO_LEAF_SIZE);
  expandFifo(16384, strongly_reachable_fifo);
  phantom_reachable_fifo = allocFifo(OTHER_FIFO_LEAF_SIZE);
  finalize_reachable_fifo = allocFifo(OTHER_FIFO_LEAF_SIZE);
  enqueue_fifo = allocFifo(ENQUEUE_FIFO_LEAF_SIZE);
  finalizer_fifo = allocThreadSafeFifo(FINALIZER_FIFO_LEAF_SIZE);
  reference_fifo = allocFifo(REFERENCE_FIFO_LEAF_SIZE);
  dead_string_fifo = allocFifo(DEAD_STRING_FIFO_LEAF_SIZE);
  dead_lock_fifo = allocFifo(DEAD_LOCK_FIFO_LEAF_SIZE);
  dead_clazz_fifo = allocFifo(DEAD_CLAZZ_FIFO_LEAF_SIZE);
  gc_instance = theGarbageCollector;
  woempa(7,"         window_fifo at %p\n", window_fifo);
  woempa(7,"      finalizer_fifo at %p\n", finalizer_fifo);
  woempa(7,"        enqueue_fifo at %p\n", enqueue_fifo);
  woempa(7,"      reference_fifo at %p\n", reference_fifo);
  woempa(7,"      dead_lock_fifo at %p\n", dead_lock_fifo);
  woempa(7,"     dead_clazz_fifo at %p\n", dead_clazz_fifo);
  woempa(7, "I_ThreadGroup_system = %j.\n", I_ThreadGroup_system);
  woempa(7, "     W_Thread_system = %p.\n", W_Thread_system);
  woempa(7, "           gc_thread = %p.\n", gc_thread);
  woempa(7, "   Current instances = %d.\n", instance_use);
#ifdef USE_OBJECT_HASHTABLE
  woempa(7, "    Object hashtable = %p.\n", object_hashtable);
#endif

  woempa(7, "Registering internal_reclaim_callback\n");
  registerReclaimCallback(internal_reclaim_callback);

}


void gc_collect(w_instance theGarbageCollector) {
  w_thread   this_thread = currentWonkaThread;
  w_int gc_pass_count = getIntegerField(theGarbageCollector, F_GarbageCollector_passes);
  w_int done = 0;
  w_int retcode;

  threadMustBeSafe(this_thread);

  woempa(7,"(GC) Begin pass %d\n",gc_pass_count);
  if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
    w_printf("GC: starting scheduled pass %d : %d instances in use (%d allocated, %d freed)\n", gc_pass_count, instance_use, instance_allocated, instance_returned);
    w_printf("GC: %d bytes available out of %d, memory load factor = %d, %skilling soft references.\n", x_mem_avail(), memory_total, memory_load_factor, killing_soft_references ? "" : "not ");
  }
#ifdef TRACE_MEM_ALLOC
  //if (gc_pass_count % (PRINTRATE*PRINTRATE) == 0) reportMemStat(1);
#endif

  while (done < 2) {
    if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
      w_printf("GC: done = %d (%s), GC phase = %s.\n", done, done == 0 ? "nothing" : done == 1 ? "mark" : done == 2 ? "mark+sweep" : "fail", gc_phase == GC_PHASE_PREPARE ? "PREPARE" : gc_phase == GC_PHASE_MARK ? "MARK" : gc_phase == GC_PHASE_SWEEP ? "SWEEP" : gc_phase == GC_PHASE_COMPLETE ? "COMPLETE" : "UNREADY");
    }
    switch (gc_phase) {
      case GC_PHASE_UNREADY:
      case GC_PHASE_COMPLETE:
        woempa(7, "Thread %w: Phase = COMPLETE, doing a PREPARE and MARK\n", this_thread->name);
        if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
          w_printf("GC: thread %t setting GC phase to PREPARE\n", this_thread);
	  gc_start_ticks = x_time_get();
        }
        marking_thread = this_thread;
        gc_phase = GC_PHASE_PREPARE;
        prepreparation(this_thread);
        retcode = preparationPhase();
        if (retcode < 0) {
          woempa(9, "Aborting PREPARE\n");
          if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
            w_printf("GC: thread %t aborting PREPARE\n", this_thread);
          }
          done = 99;
          gc_phase = GC_PHASE_COMPLETE;
          marking_thread = NULL;
          GC_MONITOR_NOTIFY
          postmark(this_thread);

          break;

        }
        if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
          w_printf("GC: thread %t setting GC phase to MARK\n", this_thread);
        }
        gc_phase = GC_PHASE_MARK;
        retcode = markPhase();
        postmark(this_thread);
        if (retcode < 0) {
          woempa(9, "Aborting MARK\n");
          if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
            w_printf("GC: thread %t aborting MARK\n", this_thread);
          }
          done = 99;
          gc_phase = GC_PHASE_COMPLETE;
          marking_thread = NULL;
          GC_MONITOR_NOTIFY
	  if (GC_COMPLETE_WAIT) {
            GC_MONITOR_WAIT(GC_COMPLETE_WAIT)
	  }

          break;

        }
        woempa(7,"(GC) Thread %w: Entering SWEEP phase.\n", this_thread->name);
        if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
          w_printf("GC: prepare/mark phase lasted %d ms\n", x_ticks2usecs(x_time_get() - gc_start_ticks) / 1000);
          w_printf("GC: thread %t setting GC phase to SWEEP\n", this_thread);
        }
        gc_phase = GC_PHASE_SWEEP;
        marking_thread = NULL;
        GC_MONITOR_NOTIFY
        done += 1;
        /* fall through */

      case GC_PHASE_SWEEP:
        if (sweeping_thread) {
          if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
            w_printf("GC: thread %t will not sweep, %t is already doing so\n", this_thread, sweeping_thread);
          }
          done = 99;
          gc_phase = GC_PHASE_COMPLETE;
          GC_MONITOR_NOTIFY
	  if (GC_COMPLETE_WAIT) {
            GC_MONITOR_WAIT(GC_COMPLETE_WAIT)
	  }

          break;
        }
        woempa(7, "Thread %w: Phase = SWEEP, joining in the fun\n", this_thread->name);
        if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
          w_printf("GC: thread %t performing sweep\n", this_thread);
        }
	sweeping_thread = this_thread;
#ifdef INSTANCE_STATS
        instance_stat_hashtable = ht_create((char*)"hashtable:instance-stats", 65535, NULL , NULL, 0, 0);
#endif
        sweepPhase();
#ifdef INSTANCE_STATS
        reportInstanceStat();
        ht_destroy(instance_stat_hashtable);
        instance_stat_hashtable = NULL;
#endif
	sweeping_thread = NULL;
        if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
          w_printf("GC: thread %t setting GC phase to COMPLETE\n", this_thread);
        }
        gc_phase = GC_PHASE_COMPLETE;
        marking_thread = NULL;
        GC_MONITOR_NOTIFY
        done *= 2;
        if (done) {
          woempa(7, "Thread %t: Have performed a complete cycle\n", this_thread);
        }
        else {
          woempa(7, "Thread %t: Have not performed a complete cycle\n", this_thread);
	  if (GC_COMPLETE_WAIT) {
            GC_MONITOR_WAIT(GC_COMPLETE_WAIT)
          }
	}
        break;

      default: // GC_PHASE_PREPARE/MARK
        woempa(7, "Thread %w: Phase = PREPARE/MARK, waiting\n", this_thread->name);
	if (GC_OTHER_MARK_WAIT) {
          GC_MONITOR_WAIT(GC_OTHER_MARK_WAIT)
	}
    }
  }
  woempa(7,"(GC) End pass %d : %d instances returned so far\n",gc_pass_count, instance_returned);
  if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
    w_printf("GC: finished scheduled pass %d : %d instances in use (%d allocated, %d freed)\n", gc_pass_count, instance_use, instance_allocated, instance_returned);
    w_printf("GC: thread %t collected %d objects\n", this_thread, instance_returned);
  }
}

w_int gc_request(w_int requested) {
  w_thread   this_thread = currentWonkaThread;
  w_int released = 0;
  w_int remaining = requested;
  w_int swept;
  w_int tries = memory_load_factor + 2;
  w_int retcode;

  if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
    w_printf("GC: %t starting unscheduled pass : %d instances in use (%d allocated, %d freed)\n", this_thread, instance_use, instance_allocated, instance_returned);
    w_printf("GC: %d bytes available out of %d, memory load factor = %d, requested = %d, %skilling soft references.\n", x_mem_avail(), memory_total, memory_load_factor, remaining, killing_soft_references ? "" : "not ");
  }

  while (tries > 0 && remaining > 0) {
    if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
      w_printf("GC: tries remaining = %d, looking to collect %d bytes, GC phase = %s.\n", tries, remaining, gc_phase == GC_PHASE_PREPARE ? "PREPARE" : gc_phase == GC_PHASE_MARK ? "MARK" : gc_phase == GC_PHASE_SWEEP ? "SWEEP" : gc_phase == GC_PHASE_COMPLETE ? "COMPLETE" : "UNREADY");
    }
    switch (gc_phase) {
      case GC_PHASE_PREPARE:
      case GC_PHASE_MARK:
        if (this_thread == marking_thread) {
          woempa(9, "Recursive gc_request????\n");
          tries = 0;
        }
        else {
          woempa(7, "Thread %w: Phase = PREPARE/MARK, waiting\n", this_thread->name);
          if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
            w_printf("GC: thread %t found heap was being marked by %t, waiting for %d ticks\n", this_thread, marking_thread, GC_OTHER_MARK_WAIT);
          }
	  if (GC_OTHER_MARK_WAIT) {
            GC_MONITOR_WAIT(GC_OTHER_MARK_WAIT)
          }
	}
        break;

      case GC_PHASE_SWEEP:
	if (sweeping_thread) {
          if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
            w_printf("GC: thread %t will not sweep, %t is already doing so\n", this_thread, sweeping_thread);
          }
          tries = 0;

	  break;
	}

        if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
          w_printf("GC: thread %t is sweeping\n", this_thread);
        }
        woempa(7, "Thread %w: Phase = SWEEP, joining in the fun\n", this_thread->name);
        if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
          w_printf("GC: thread %t performing sweep\n", this_thread);
        }
	sweeping_thread = this_thread;
        swept = sweep(remaining);
	sweeping_thread = NULL;
        released += swept;
        remaining -= swept;
        woempa(7, "Thread %w: Reclaimed %d bytes, %d to go\n", this_thread->name, swept, remaining);
        if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
          w_printf("GC: thread %t swept %d bytes, %d to go\n", this_thread, swept, remaining);
        }
        if (swept == 0) {
          if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
            w_printf("GC: thread %t setting GC phase to COMPLETE\n", this_thread);
          }
          gc_phase = GC_PHASE_COMPLETE;
          marking_thread = NULL;
          GC_MONITOR_NOTIFY
        }
        tries -= 1;

        break;

      case GC_PHASE_COMPLETE:
        woempa(7, "Thread %w: Phase = COMPLETE, doing a PREPARE and MARK\n", this_thread->name);
        if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
          w_printf("GC: thread %t setting GC phase to PREPARE\n", this_thread);
	  gc_start_ticks = x_time_get();
        }
        marking_thread = this_thread;
        gc_phase = GC_PHASE_PREPARE;
        prepreparation(this_thread);
        retcode = preparationPhase();
        if (retcode < 0) {
          woempa(9, "Aborting PREPARE\n");
          if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
            w_printf("GC: thread %t aborting PREPARE\n", this_thread);
          }
          gc_phase = GC_PHASE_COMPLETE;
          marking_thread = NULL;
          GC_MONITOR_NOTIFY
          postmark(this_thread);
          tries = 0;

          break;

        }
        if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
          w_printf("GC: thread %t setting GC phase to MARK\n", this_thread);
        }
        gc_phase = GC_PHASE_MARK;
        retcode = markPhase();
        postmark(this_thread);
        if (retcode < 0) {
          woempa(9, "Aborting MARK\n");
          if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
            w_printf("GC: thread %t aborting MARK\n", this_thread);
          }
          gc_phase = GC_PHASE_COMPLETE;
          marking_thread = NULL;
          GC_MONITOR_NOTIFY
          tries = 0;

          break;

        }
        if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
          w_printf("GC: prepare/mark phase lasted %d ms\n", x_ticks2usecs(x_time_get() - gc_start_ticks) / 1000);
          w_printf("GC: thread %t setting GC phase to SWEEP\n", this_thread);
        }
        gc_phase = GC_PHASE_SWEEP;
        marking_thread = NULL;
        GC_MONITOR_NOTIFY
        break;

      default: 
        /*  GC_PHASE_UNREADY - don't try to reclaim anything yet */
        tries = 0;
    }
  }
  if (isSet(verbose_flags, VERBOSE_FLAG_GC)) {
    w_printf("GC: finished unscheduled pass : %d instances in use (%d allocated, %d freed)\n", instance_use, instance_allocated, instance_returned);
    w_printf("GC: thread %t freed %d bytes\n", this_thread, released);
  }
  woempa(7, "Released: %d bytes\n", released);
  reclaim_accumulator = 0;

  return released;

}

