/**************************************************************************
* Copyright (c) 2021, 2022 by KIFFER Ltd. All rights reserved.            *
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

#ifndef _HEAP_H
#define _HEAP_H

#include "arrays.h"
#include "jni.h"
#include "oswald.h"

/*
** This file defines the API for dealing with the heap.
*/

/*
** Thread (if any) which is currently marking the heap.
*/
extern volatile w_thread marking_thread;

/*
** Thread (if any) which is currently sweeping the heap.
*/
extern volatile w_thread sweeping_thread;

/*
** Indication of the current heap occupancy: integer part of (rho / (1-rho)),
** where  rho = (memory_total - x_mem_avail()) / memory_total .
** It is calculated at the start of each GC cycle.
*/
extern w_int memory_load_factor;

/*
** Object flags word
**
** These flags are present at the same position (1 word before the start of
** the instance fields) in both transient and persistent objects.
**
** 0. Flag which is set in every Java instance.
*/
#define O_IS_JAVA_INSTANCE   0x80000000

/*
** 1. Object "colours" used by the mark-and-sweep algorithm.  Note that the
** position of the first four flags is essential to the correct working
** of the isMarked() funtion of collector.c .
** [CG 20060803] There is no SOFT_BLACK colour any more; in any given pass
** of GC either all soft references are treated as strong or they are all
** treated as weak.
** [CG 20060805] No WEAK_BLACK neither. :-)
*/

// A FINALIZE_BLACK object has been marked as reachable only becuse it is referred to by objects which are finalizable.
#define O_FINALIZE_BLACK     0x00000001
// A PHANTOM_BLACK object has been marked as reachable by phantom references only.
#define O_PHANTOM_BLACK      0x00000002
// A JDWP_BLACK object is one for which collection has been disabled using JDWP.
#define O_JDWP_BLACK         0x00000004
// A BLACK object has been marked as strongly reachable.
#define O_BLACK              0x00000008

/*
** 2. Finalization etc.
*/

// An object is FINALIZABLE if it has a finalizer which has not yet been called
#define O_FINALIZABLE        0x00000010 
// An object is FINALIZING if it is on the finalizer_fifo or its finalize() methodnqueue() method has been called
#define O_FINALIZING         0x00000020 
// An object is ENQUEUEABLE if it has an enqueue() which has not yet been called
#define O_ENQUEUEABLE        0x00000040 
//REMOVED: An object is ENQUEUEING if it is on the enqueue_fifo or its enqueue() method has been called
//#define O_ENQUEUEING         0x00000080 
// An object has WINDOW set if it is subject to analysis by the garbage collector.
#define O_WINDOW             0x00000100
// A GARBAGE object has become garbage and can be freed. 
#define O_GARBAGE            0x00000200
#ifdef CLASSES_HAVE_INSTANCE_CACHE
// A CACHED object has been cached for re-use and therefore should not be freed
#define O_CACHED             0x00000400
#endif

/*
** 3. Object locking
**  If O_HAS_LOCK is set, then the instance is linked to a monitor via lock_hashtable.
*/

#define O_HAS_LOCK           0x01000000

/*
** Collective names for related groups of flags
*/

/*
** Inferior versions of BLACKness.
*/

#define O_NEAR_BLACK (O_PHANTOM_BLACK | O_FINALIZE_BLACK)

/*
** Convert a set of object flags to a C string indicating the level of reachability
*/
#define flags2reachability(f) (isSet((f), O_BLACK) ? "strongly" : isSet((f), O_PHANTOM_BLACK) ? "phantom" : isSet((f), O_FINALIZE_BLACK) ? "finalize" : "not")

/*
** Convert a set of object flags to the address of the fifo onto which the object should be pushed
*/
#define flags2fifo(f) (isSet((f), O_BLACK) ? strongly_reachable_fifo : isSet((f), O_PHANTOM_BLACK) ? phantom_reachable_fifo : isSet((f), O_FINALIZE_BLACK) ? finalize_reachable_fifo: NULL)

/*
** Object header structure w_Object
** 
** Defines the header of a transient object.
** An instance pointer (w_instance) will point at the first word
** of the `fields' array).
*/

typedef struct w_Object {
// \texttt{w_Clazz} structure describing the class of this object
  w_clazz clazz;
// Object flags as defined above
  w_flags flags;
// the instance data for this object, this is a variable sized array.
  w_word fields[0];
} w_Object;

/**
 * Our garbage collection algorithm has three main phases:
 * - the Preparation phase takes a snapshot of the instance heap,
 *   and makes a list of collectable (non-immortal) objects which
 *   existed at a certain moment.
 * - the Mark phase marks all instances which are strongly, softly,
 *   weakly, or phantom-reachable.
 * - the Sweep phase reclaims the memory of all the items which were
 *   identified as collectable in the Preparation phase, and not marked
 *   reachable in the Mark phase.  It also performs finalization and
 *   identifies which instances are only reachable via soft or weak counters, 
 *   and clears and enqueues references where appropriate.
 */

#define GC_PHASE_UNREADY   0
#define GC_PHASE_PREPARE   1
#define GC_PHASE_MARK      2
#define GC_PHASE_SWEEP     3
#define GC_PHASE_COMPLETE  4

extern w_int gc_phase;

/*
** Allocate an instance of an arbitrary non-array class.
*/
w_instance allocInstance(w_thread thread, w_clazz clazz);

/*
** Allocate an instance of String.
*/
w_instance allocStringInstance(w_thread thread);

/*
** Allocate an instance of Throwable or a subclass.
*/
w_instance allocThrowableInstance(w_thread thread, w_clazz clazz);

/*
** Allocation of array instances
*/
// Base function: allocate using source filename and line number, clazz, dimansions, flags.

w_instance allocArrayInstance(w_thread thread, w_clazz clazz, w_int fixedDimensions, w_int lengths[]);

// Special (optimised) case for 1-d arrays.

w_instance allocArrayInstance_1d(w_thread thread, w_clazz clazz, w_int length);

#ifndef offsetof
#define offsetof(TYPE, MEMBER) ((size_t) &((TYPE *)0)->MEMBER)
#endif

/*
** Convert an instance pointer to a pointer to the object header.
*/

#define instance2object(instance) ( (w_object)(((unsigned char *)(instance)) - offsetof(w_Object, fields)) )

/*
** Convert an instance pointer to a pointer to the corresponding w_Clazz structure.
*/

w_clazz instance2clazz(w_instance ins);

/*
** The object flags are located in 2 words back from the object fields
** (a w_instance pointer points at the first field).
*/

#define instance2flagsptr(instance) (w_word*)(((unsigned char *)(instance)) - offsetof(w_Object, fields) + offsetof(w_Object, flags))

/*
** Convenience macro
*/

#define instance2flags(ins) (*instance2flagsptr(ins))

#include "mika_threads.h"

/*
** To set a reference field of an array, always use this function, which 
** acts as a ``write barrier''.
*/

static inline void setArrayReferenceField(w_instance parent, w_instance child, w_int slot) {
  w_thread  thread = currentWonkaThread;
  w_boolean unsafe = enterUnsafeRegion(thread);

  if (child == NULL) {
    parent[F_Array_data + slot] = 0;
  }
  else {
    parent[F_Array_data + slot] = (w_word)(child);
    setFlag(instance2object(child)->flags, O_BLACK);
  }

    if (!unsafe) {
      enterSafeRegion(thread);
    }
}

/*
** ... or this one, if you know for a fact that the thread is already unsafe.
*/

static inline void setArrayReferenceField_unsafe(w_instance parent, w_instance child, w_int slot) {
  threadMustBeUnsafe(currentWonkaThread);

  if (child == NULL) {
    parent[F_Array_data + slot] = 0;
  }
  else {
    parent[F_Array_data + slot] = (w_word)(child);
    setFlag(instance2object(child)->flags, O_BLACK);
  }
}

/*
** Create a new global reference to an instance.
*/

void newGlobalReference(w_instance instance);

/*
** Delete a global reference to an instance.
*/

void deleteGlobalReference(w_instance instance);

/*
** The hashtable in which global references are stored. (See newGlobalRef).
*/

extern w_hashtable globals_hashtable;

void initHeap(void);

/*
** Start up the GC thread
*/

void startHeap(void);

void readStatics (w_clazz clazz);

/*
** Heap variables - treat as read-only!
** Number of instances currently in use
*/

extern w_int instance_use;

/*
** Number of instances allocated so far
*/

extern w_int instance_allocated;

/*
** Number of instances returned (garbage-collected) so far
*/
extern w_int instance_returned;

/*
** Create and initialise the garbage collector. Only call this once!
*/

void gc_create(w_thread thread, w_instance theGarbageCollector);

/*
** Run one pass of the garbage collector. Caller must own gc_monitor.
*/

void gc_collect(w_instance theGarbageCollector);

/*
** Ask the garbage collector to free up some momory, and return the amount
** which was really freed.  Caller must own gc_monitor.  Includes a built-in
** wait(), so may take a second or so to return.
*/

w_int gc_request(w_int bytes);

/*
** Run the finalizers of all the finalizable objects discovered by gc_collect().
*/

void gc_finalization(void);

/*
** Enqueue all the `dead' references discovered by gc_collect().
*/

void gc_references(void);

/*
** Perform miscellaneous tasks.
*/

void gc_housekeeping(void);

/*
**  GC monitor
**  Acquire and perform a wait on this lock in order to know when memory
**  may have become available.
*/

extern x_monitor gc_monitor;

/*
** GC kicks pointer
** If nonzero, the garbage collector has been "kicked" and has not yet
** finished responding to the stimulus. Increment this and notify gc_monitor
** in order to stimulate GC into doing some work.
*/
extern w_int *gc_kicks_pointer;

/*
** A w_reclaim_callback is called by the garbage collector to ask the callee
** to be so kind as to free up some memory.  The first parameter is the number
** of bytes that GC would like to reclaim, and the second is a w_instance
** which somehow identifies the subsystem which is asking for memory to be
** reclaimed (for example, the AWT might use the unique instance of 
** java.awt.Toolkit for this purpose).  The value NULL is reserved for use
** by the heap itself.  This parameter can be used to implement a kind of
** priority among subsystems, e.g. subsystem X might be more willing to
** give up memory to subsystem Y than to Z.
**
** The result returned is the number of bytes that the 
** callee was able or willing to release.  (This may only be an estimate, 
** but it needs to be a good one if the mechanism is to work  properly).
*/

typedef w_size(*w_reclaim_callback)(w_int requested, w_instance caller);

/*
** Listeners are registered and deregistered using registerReclaimCallback
** and deregisterReclaimCallback respectively.  In the current implementation,
** so long as no callbacks are deregistered the callbacks will always be 
** used in the order in which they were registered.
** A w_reclaim_callback can be called by any thread, at any priority!
** In particular, it is perfectly normal for a call to gc_reclaim() to
** cause the caller's own callback to be invoked.
** Callbacks should be written with this in mind.
*/

void registerReclaimCallback(w_reclaim_callback);
void deregisterReclaimCallback(w_reclaim_callback);

/*
** To force a reclaim, gc_reclaim(bytes) is called.
*/

w_size gc_reclaim(w_int bytes, w_instance caller);

/*
** The finalizer_fifo is used by the garbage collector thread to inform the
** finalizer thread of objects which need finalizing.  Access is protected
** by a mutex.
*/

extern w_fifo  finalizer_fifo;
extern x_mutex finalizer_fifo_mutex;

/*
** The enqueue_fifo is used by the garbage collector thread to inform the
** finalizer thread of objects which need enqueueg.  Access is protected
** by a mutex.
*/

extern w_fifo  enqueue_fifo;
extern x_mutex enqueue_fifo_mutex;

/*
** min_heap_free is the amount of heap which will be reserved for native
** code (cannot be used by Java instances).
*/

extern w_size     min_heap_free;

/*
** gc_instance and gc_thread are the instance of com.acunia.wonka.GarbageCollector
** and the w_Thread associated with the Garbage Collector singleton.
*/

extern w_instance gc_instance;
extern w_thread   gc_thread;

void startCollector(void);

// TODO get rid of this stupid thing
#define makeLocal(objectref) (objectref)

char * print_instance_short(char * buffer, int * remain, void * data, int w, int p, unsigned int f);
char * print_instance_long(char * buffer, int * remain, void * data, int w, int p, unsigned int f);

inline static void setInstanceFlags(w_instance instance, w_word flags) {
  setFlag(instance2object(instance)->flags, flags);
}

extern w_fifo window_fifo;

/*
** If USE_OBJECT_HASHTABLE is defined, we maintain a hashtable (set) of all 
** allocated objects, and use this instead of heap-walking to define the
** window for a GC pass. This also means that isProbablyAnInstance() is reliable
** (returns true iff the candidate is a Java instance, and should never crash).
*/

//#define USE_OBJECT_HASHTABLE

#ifdef USE_OBJECT_HASHTABLE
extern w_hashtable object_hashtable;

#define isProbablyAnInstance(foo) (!object_hashtable || ht_read(object_hashtable, instance2object(foo)))
#else
#define isProbablyAnInstance(foo) (((instance2flags(foo) & 0xfefffc00) == O_IS_JAVA_INSTANCE) && (strcmp(instance2clazz(foo)->label, "clazz") == 0))
#endif

#endif /* _HEAP_H */
