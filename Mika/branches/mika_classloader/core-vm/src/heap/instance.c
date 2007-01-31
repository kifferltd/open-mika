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
** $Id: instance.c,v 1.20 2006/10/04 14:24:16 cvsroot Exp $
*/

//#define DEBUG

#include "string.h"
#include "arrays.h"
#include "checks.h"
#include "clazz.h"
#include "core-classes.h"
#include "descriptor.h"
#include "exception.h"
#include "fifo.h"
#include "hashtable.h"
#include "loading.h"
#include "list.h"
#include "ts-mem.h"
#include "wstrings.h"
#include "threads.h"
#include "wordset.h"

extern w_hashtable lock_hashtable;

w_size min_heap_free;

char * print_instance_short(char * buffer, int * remain, void * data, int w, int p, unsigned int f) {
  w_instance instance = data;
  w_int    nbytes;
  char    *temp;
  
  if (instance == NULL) {
    strncpy(buffer, (char *)"<NULL>", *remain);
    nbytes = *remain < 6 ? *remain : 6;
    *remain -= nbytes;

    return buffer + nbytes;

  }

  temp = buffer;
  nbytes = x_snprintf(temp, *remain, "%k@%p", instance2clazz(instance), instance);
  *remain -= nbytes;

  return temp + nbytes;
}

// TODO: make this call toString() if not the default one of java.lang.Object ...
char * print_instance_long(char * buffer, int * remain, void * data, int w, int p, unsigned int f) {
  w_instance instance = data;
  w_int    nbytes;
  char    *temp;
  
  if (instance == NULL) {
    strncpy(buffer, (char *)"<NULL>", *remain);
    nbytes = *remain < 6 ? *remain : 6;
    *remain -= nbytes;

    return buffer + nbytes;

  }

  temp = buffer;
  nbytes = x_snprintf(temp, *remain, "%K@%p", instance2clazz(instance), instance);
  *remain -= nbytes;

  return temp + nbytes;
}

w_clazz instance2clazz(w_instance ref) {

  if (ref == NULL) {
    return clazzObject;
  }

  return instance2object(makeLocal(ref))->clazz;

}

w_int instance_use = 0;
w_int instance_allocated = 0;
w_int instance_returned = 0;
w_object instance_first = NULL;

inline static void registerObject(w_object object, w_thread thread) {
  woempa(1, "Registering %j\n", object->fields);
  instance_use += 1;
  instance_allocated += 1;
  addLocalReference(thread, object->fields);
  setFlag(object->flags, O_IS_JAVA_INSTANCE);
#ifdef USE_OBJECT_HASHTABLE
  if (ht_write(object_hashtable, (w_word)object, (w_word)object)) {
    wabort(ABORT_WONKA, "Sky! Could not add object %p to object hashtable!\n", object);
  }
  woempa(1, "Added object %j to object_hashtable, now contains %d objects\n", object->fields, object_hashtable->occupancy);
#endif

  x_mem_tag_set(block2chunk(object), (x_mem_tag_get(block2chunk(object)) | OBJECT_TAG));
}

inline static void checkClazz(w_clazz clazz) {

  if (! clazz) {
    wabort(ABORT_WONKA, "Clazz is NULL\n");
  }
}

/*
 * MAX_RETRIES defines how often a low-priority thread (Java priority 1) will
 * try to satisfy a heap_request by calling gc_request(); higher-priority
 * threads will make less tries, and a thread of priority > 10 will only
 * return TRUE from heap_request if sufficient memory is already available.
 * retry_incr is calculated such that (priority * num_retries * retry_incr)
 * is at least 100.
 */
#define MAX_RETRIES 10
static const int retry_incr = 100 / MAX_RETRIES;

static w_boolean heap_request(w_thread thread, w_int bytes) {

  w_int   count = 0;

  if (!thread) {
    woempa(1, "Called with thread==null\n");
    return TRUE;
  }

  if (thread == gc_thread) {
    woempa(1, "Called by gc thread.\n");
    return TRUE;
  }

  if (gc_instance == NULL) {
    woempa(1, "Called before gc exists: gc_instance = %p.\n", gc_instance);
    return TRUE;
  }

  while (!expandFifo(instance_allocated - instance_returned + 1, window_fifo)) {
    printf("No space to expand window_fifo ...\n");
    gc_reclaim(8192, NULL);
  }

  if(thread->jpriority > 10) {

    return x_mem_avail() - bytes > min_heap_free;

  }

  gc_reclaim(bytes, NULL);

  if (x_mem_avail() - bytes > min_heap_free) {

    return WONKA_TRUE;

  }

  do {
    count += retry_incr;
    if (count > 100) {
      wprintf("TOO MANY RETRIES\n");

      return WONKA_FALSE;

    }
    //wprintf("RETRY #%d for %d bytes, %d bytes available (min = %d)\n", count, bytes, x_mem_avail(), min_heap_free);
    gc_reclaim(bytes, NULL);
  } while ((x_mem_avail() - bytes) < min_heap_free);

  return WONKA_TRUE;
}

static w_instance allocInstance_common(w_thread thread, w_object object, w_clazz clazz) {
  object->clazz = clazz;

#ifdef JAVA_PROFILE
  profileAllocInstance(thread, clazz);
#endif 
  
  registerObject(object,thread);

  return object->fields;
}

w_instance allocInstance_initialized(w_thread thread, w_clazz clazz) {
  w_object object = NULL;

#ifdef RUNTIME_CHECKS
  checkClazz(clazz);

  if (getClazzState(clazz) != CLAZZ_STATE_INITIALIZED
   && getClazzState(clazz) != CLAZZ_STATE_INITIALIZING) {
    wabort(ABORT_WONKA, "Cannot create instance of %K, is not initialized\n", clazz);
    return NULL;
  }
#endif

  woempa(1, "clazz is %k at %p, requested size is %d words, instance needs %d bytes.\n", clazz, clazz, clazz->instanceSize, clazz->bytes_needed);

  if (heap_request(thread, (w_int)clazz->bytes_needed)) {
    object = allocClearedMem(clazz->bytes_needed);
  }

  if (! object) {
    return NULL;
  }

  if (isSet(clazz->flags, CLAZZ_HAS_FINALIZER)) {
    woempa(1, "Clazz %k has a finalizer, setting instance at %p FINALIZABLE\n", clazz, object->fields);
    setFlag(object->flags, O_FINALIZABLE);
  }

  return allocInstance_common(thread, object, clazz);
}

w_instance allocInstance(w_thread thread, w_clazz clazz) {

  if (isSet(clazz->flags, CLAZZ_IS_THROWABLE)) {

    return allocThrowableInstance(thread, clazz);

  }

  threadMustBeSafe(thread);

  /*
  ** The class must be initialized.
  */

  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {
    return NULL;
  }

  return allocInstance_initialized(thread, clazz);
}

w_instance allocThrowableInstance(w_thread thread, w_clazz clazz) {

  w_object object = NULL;

#ifdef RUNTIME_CHECKS
  checkClazz(clazz);
#endif

  threadMustBeSafe(thread);

  /*
  ** The class must be initialized.
  */

  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {
    return NULL;
  }

  woempa(1, "clazz is %k at %p, requested size is %d words, instance needs %d bytes.\n", clazz, clazz, clazz->instanceSize, clazz->bytes_needed);

  object = allocClearedMem(clazz->bytes_needed);

  if (! object) {
    return NULL;
  }

  if (isSet(clazz->flags, CLAZZ_HAS_FINALIZER)) {
    woempa(1, "Clazz %k has a finalizer, setting instance at %p FINALIZABLE\n", clazz, object->fields);
    setFlag(object->flags, O_FINALIZABLE);
  }

  fillThrowable(thread, object->fields);

  return allocInstance_common(thread, object, clazz);
}

w_instance allocStringInstance(w_thread thread) {

  w_object object;

  object = allocClearedMem(clazzString->bytes_needed);
  if (! object) {
    return NULL;
  }

  return allocInstance_common(thread, object, clazzString);
}

static w_instance internalAllocArrayInstance(w_thread thread, w_clazz clazz, w_size size) {

  w_object object = NULL;
  w_size   bytes;

  checkClazz(clazz);  

  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {

    return NULL;

  }

  /*
  ** Calculate the number of bytes we need. Note that checking the requirements with the quota has been
  ** done allready by the calling function.
  */
  
  bytes = sizeof(w_Object) + (size * sizeof(w_word));

  if (heap_request(thread, bytes)) {
    object = allocClearedMem(bytes);
    woempa(1, "Allocated a %k at %p, need %d bytes\n", clazz, object->fields, bytes);
  }

  if (object == NULL) {
    return NULL;
  }

  return allocInstance_common(thread, object, clazz);
}

inline static w_size roundBitsToWords(w_int bits) {
  return ((bits + 31) & ~31) >> 5;
}

/*
** A helper structure for allocating arrays.
*/

typedef struct w_Aas * w_aas;

typedef struct w_Aas {
  w_instance Array;               // The array instance of this dimension
  w_clazz    clazz;               // Clazz of this dimension
//  w_int      size;                // The size in words of this dimension
  w_int      length;              // The length of this array
  w_aas      next;                // The link to the next dimension, is NULL for the last dimension.
} w_Aas;

/*
** This function to allocate and fill subarrays is called recursively. So keep it very
** short and sweet. On an X86 with GCC 3.x compiled, it requires about 48 stack bytes per
** invocation, so if there is a nutcase that wants a 255 dimensional array, he needs somewhat
** more than 12K stack. 
*/

static void fillParentArray(w_thread thread, w_aas parent) {

  w_int x;

  if (parent->next) {
    for (x = 0; x < parent->length && ! exceptionThrown(thread); x++) {
      woempa(7, "%k parent[%d]->next->size = %d\n", parent->next->clazz, x, 1 + roundBitsToWords(parent->next->clazz->previousDimension->bits * parent->next->length));
      parent->next->Array = internalAllocArrayInstance(thread, parent->next->clazz, 1 + roundBitsToWords(parent->next->clazz->previousDimension->bits * parent->next->length));
      if (exceptionThrown(thread)) {
        return;
      }
      parent->next->Array[F_Array_length] = parent->next->length;

      enterUnsafeRegion(thread);      
      setArrayReferenceField_unsafe(parent->Array, parent->next->Array, x);
      popLocalReference(thread->top);
      enterSafeRegion(thread);
      fillParentArray(thread, parent->next);
    }
  }
}

w_instance allocArrayInstance_1d(w_thread thread, w_clazz clazz, w_int length) {

  w_instance result;
  jlong size;

  threadMustBeSafe(thread);

  woempa(1, "Allocating an instance of %k (1 dimension, length %d)\n", clazz, length);

  size = ((jlong)clazz->previousDimension->bits) * ((jlong) length);
  if (size > 0x7fffffff) {
    throwException(thread, clazzOutOfMemoryError, NULL);
    return NULL;
   }

  woempa(1, "%k has %d elements of %d bits, size = %d\n", clazz, length, clazz->previousDimension->bits, 1 + roundBitsToWords(clazz->previousDimension->bits * length));
  result = internalAllocArrayInstance(thread, clazz, 1 + roundBitsToWords(clazz->previousDimension->bits * length));
  if (result) {
    result[F_Array_length] = length;
  }

  if (exceptionThrown(thread) && result) {
    releaseMem(result);
    result = NULL;
  }
  
  return result;
    
}

w_instance reallocArrayInstance_1d(w_thread thread, w_instance oldarray, w_int newlength) {
  w_clazz clazz = instance2clazz(oldarray);
  w_instance newarray = NULL;
  w_size bytes;

  threadMustBeSafe(thread);

  woempa(7, "Reallocating an instance of %k (1-d): old length = %d, new length = %d\n", clazz, oldarray[F_Array_length], newlength);

  bytes = (1 + roundBitsToWords(clazz->previousDimension->bits * newlength)) * 4;
  woempa(7, "New %k has %d elements of %d bits, size = %d bytes\n", clazz, newlength, clazz->previousDimension->bits, bytes);
  bytes += sizeof(w_Object);

  if (heap_request(thread, bytes)) {
    w_object oldobject = instance2object(oldarray);
    w_object newobject = reallocMem(oldobject, bytes);

    if (newobject) {
      if (newobject != oldobject) {
        woempa(7, "New array is different to old\n");
        registerObject(newobject, thread);
        newarray = newobject->fields;
      }
      else {
        woempa(7, "New array is same as old\n");
        newarray = oldarray;
      }
      newarray[F_Array_length] = newlength;
    }
    else {
      woempa(9, "Unable to allocate new array!\n");
    }
  }
  else {
    throwOutOfMemoryError(thread);
  }

  return newarray;
}

w_instance allocArrayInstance(w_thread thread, w_clazz clazz, w_int dimensions, w_int lengths[]) {

  w_int i;
  w_Aas * Aas;
  w_clazz current;
  w_instance result;

  threadMustBeSafe(thread);

  if (dimensions == 1) {
    return allocArrayInstance_1d(thread, clazz, lengths[0]);
  }
  if (dimensions <= 0) {
    return NULL;
  }
  if (dimensions > 255) {
    return NULL;
  }

  Aas = x_mem_alloc(sizeof(w_Aas) * dimensions);
  if (!Aas) {
    return NULL;
  }

  current = clazz;
  woempa(1, "Allocating an instance of %k (%d dimensions)\n", clazz, dimensions);
  for (i = 0; i < dimensions; i++) {
    jlong size = ((jlong)clazz->previousDimension->bits) * ((jlong) lengths[0]);
    if (size > 0x7fffffff) {
      throwException(thread, clazzOutOfMemoryError, NULL);
      x_mem_free(Aas);
      return NULL;
    }
    Aas[i].next = (i == dimensions - 1) ? NULL : Aas + i + 1;
    Aas[i].length = lengths[i];
    woempa(1, "Dimension %d has length %d, bits/element is %d\n", i, lengths[i], current->previousDimension->bits);

    Aas[i].clazz = current;
    current = current->previousDimension;
  }

  woempa(7, "%k root size = %d\n", clazz, 1 + roundBitsToWords(clazz->previousDimension->bits * lengths[0]));
  result = internalAllocArrayInstance(thread, clazz, 1 + roundBitsToWords(clazz->previousDimension->bits * lengths[0]));
  Aas[0].Array = result;
  if (result) {
    result[F_Array_length] = Aas[0].length;
    fillParentArray(thread, Aas);
  }

  x_mem_free(Aas);

  if (exceptionThrown(thread) && result) {
    releaseMem(result);
    result = NULL;
  }
  
  return result;
    
}

void newGlobalReference(w_instance instance) {

  w_instance found;

  found = (w_instance) ht_register(globals_hashtable, (w_word)instance);
  woempa(1, "%s global reference to instance %p.\n", found ? "further" : "new", instance);

}

void deleteGlobalReference(w_instance instance) {
  ht_deregister(globals_hashtable, (w_word)instance);
}

#define MIN_MIN_HEAP_FREE (256 * 1024)

void startHeap() {

  lock_hashtable = ht_create((char*)"hashtable:locks", 131, NULL, NULL, 0, 0);

  min_heap_free = x_mem_total() / 20;
  if (min_heap_free > MIN_MIN_HEAP_FREE) {
    min_heap_free = MIN_MIN_HEAP_FREE;
  }

}

#ifdef DEBUG 

static w_hashtable ht_temp;
  
static x_boolean register_callback(void * mem, void * arg) {

  w_object   object;
  w_clazz    clazz;

  object = chunk2object(mem);
  clazz = object->clazz;
  ht_register(ht_temp, (w_word)clazz); 
  
  return TRUE;
  
}


static void print_callback(w_word key, w_word value, void * arg1, void * arg2) {
  wprintf("%6d instances of %k\n", value, key);
}

void reportInstanceStat(void) {

  ht_temp = ht_create("hashtable:reportInstanceStat", 2047, NULL, NULL, 0, 0);
  
  x_mem_lock(x_eternal);
  x_mem_scan(x_eternal, OBJECT_TAG, register_callback, NULL);
  x_mem_unlock();
  
  printf("--- Start Instance Stats --- \n");
  ht_iterate(ht_temp, print_callback, NULL, NULL);
  printf("--- End Instance Stats --- \n");

  ht_destroy(ht_temp);
}

#endif

