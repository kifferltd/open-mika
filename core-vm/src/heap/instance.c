/**************************************************************************
* Copyright (c) 2008, 2009, 2020, 2021, 2022 by KIFFER Ltd.               * 
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
#include "mika_threads.h"
#include "wordset.h"

extern w_hashtable lock_hashtable;

w_hashtable globals_hashtable;

#define GLOBALS_HASHTABLE_SIZE   1439

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

static void registerObject(w_object object, w_thread thread) {

  if (thread) {
    threadMustBeUnsafe(thread);
  }

  woempa(1, "Registering %j\n", object->fields);
  instance_use += 1;
  instance_allocated += 1;
  addLocalReference(thread, object->fields);
  setFlag(object->flags, O_IS_JAVA_INSTANCE | O_BLACK);
#ifdef USE_OBJECT_HASHTABLE
  if (ht_write(object_hashtable, (w_word)object, (w_word)object)) {
    wabort(ABORT_WONKA, "Sky! Could not add object %p to object hashtable!\n", object);
  }
  woempa(1, "Added object %j to object_hashtable, now contains %d objects\n", object->fields, object_hashtable->occupancy);
#endif

  x_mem_tag_set(block2chunk(object), (x_mem_tag_get(block2chunk(object)) | OBJECT_TAG));
}

#ifdef RUNTIME_CHECKS
static void checkClazz(w_clazz clazz) {

  if (! clazz) {
    wabort(ABORT_WONKA, "Clazz is NULL\n");
  }

  if (! clazz->dims 
    && getClazzState(clazz) != CLAZZ_STATE_INITIALIZED
    && getClazzState(clazz) != CLAZZ_STATE_INITIALIZING) {
    wabort(ABORT_WONKA, "Cannot create instance of %K, is not initialized\n", clazz);
  }
}
#else
#define checkClazz(c)
#endif

#ifdef CLASSES_HAVE_INSTANCE_CACHE
w_instance allocInstanceFromCache(w_clazz clazz) {
  w_object object;

#ifndef THREAD_SAFE_FIFOS
  x_mutex_lock(clazz->cache_mutex, x_eternal);
#endif
  object = getFifo(clazz->cache_fifo);
  if(object) {
    woempa(7, "fetched %j from cache_fifo of %k\n", object->fields, clazz);
    memset(object, 0, clazz->bytes_needed);
  }
  else {
    woempa(7, "cache miss: %k\n", clazz);
  }
#ifndef THREAD_SAFE_FIFOS
  x_mutex_unlock(clazz->cache_mutex);
#endif

  return object;
}
#endif

static w_instance allocInstance_common(w_thread thread, w_object object, w_clazz clazz) {
  object->clazz = clazz;

  threadMustBeUnsafe(thread);

/*
#ifdef JAVA_PROFILE
  profileAllocInstance(thread, clazz);
#endif
*/ 
  
  registerObject(object,thread);

  return object->fields;
}

w_instance allocInstance(w_thread thread, w_clazz clazz) {
  w_object object = NULL;

  checkClazz(clazz);

  if (clazz == clazzString) {

    return allocStringInstance(thread);

  }

  if (isSet(clazz->flags, CLAZZ_IS_THROWABLE)) {

    return allocThrowableInstance(thread, clazz);

  }

  woempa(1, "clazz is %k at %p, requested size is %d words, instance needs %d bytes.\n", clazz, clazz, clazz->instanceSize, clazz->bytes_needed);

#ifdef CLASSES_HAVE_INSTANCE_CACHE
  object = allocInstanceFromCache(clazz);
  if (!object) 
#endif
    object = allocClearedMem(clazz->bytes_needed);

  if (! object) {
    return NULL;
  }

  if (isSet(clazz->flags, CLAZZ_HAS_FINALIZER)) {
    woempa(1, "Clazz %k has a finalizer, setting instance at %p FINALIZABLE\n", clazz, object->fields);
    setFlag(object->flags, O_FINALIZABLE);
  }

  return allocInstance_common(thread, object, clazz);
}

w_instance allocThrowableInstance(w_thread thread, w_clazz clazz) {

  w_object object = NULL;

  checkClazz(clazz);

  woempa(1, "clazz is %k at %p, requested size is %d words, instance needs %d bytes.\n", clazz, clazz, clazz->instanceSize, clazz->bytes_needed);

#ifdef CLASSES_HAVE_INSTANCE_CACHE
  object = allocInstanceFromCache(clazz);
  if (!object)
#endif
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

  w_object object = NULL;

#ifdef CLASSES_HAVE_INSTANCE_CACHE
  object = allocInstanceFromCache(clazzString);
  if (!object)
#endif
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

  bytes = sizeof(w_Object) + (size * sizeof(w_word));

  object = allocClearedMem(bytes);
  woempa(1, "Allocated a %k at %p, need %d bytes\n", clazz, object->fields, bytes);
  if (object == NULL) {
    return NULL;
  }

  return allocInstance_common(thread, object, clazz);
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

      setArrayReferenceField_unsafe(parent->Array, parent->next->Array, x);
      popLocalReference(thread->top);
      fillParentArray(thread, parent->next);
    }
  }
}

w_instance allocArrayInstance_1d(w_thread thread, w_clazz clazz, w_int length) {

  w_instance result;
  w_long size;

  woempa(1, "Allocating an instance of %k (1 dimension, length %d)\n", clazz, length);

  size = ((jlong)clazz->previousDimension->bits) * ((jlong) length);
  if (size > 0x7fffffff) {
    throwOutOfMemoryError(thread, -1);
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
  w_object oldobject;
  w_object newobject;
  w_instance newarray = NULL;
  w_size bytes;

  threadMustBeUnsafe(thread);

  woempa(7, "Reallocating an instance of %k (1-d): old length = %d, new length = %d\n", clazz, oldarray[F_Array_length], newlength);

  bytes = (1 + roundBitsToWords(clazz->previousDimension->bits * newlength)) * 4;
  woempa(7, "New %k has %d elements of %d bits, size = %d bytes\n", clazz, newlength, clazz->previousDimension->bits, bytes);
  bytes += sizeof(w_Object);

  oldobject = instance2object(oldarray);
  newobject = reallocMem(oldobject, bytes);

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

  return newarray;
}

w_instance allocArrayInstance(w_thread thread, w_clazz clazz, w_int dimensions, w_int lengths[]) {

  w_int i;
  w_Aas * Aas;
  w_clazz current;
  w_instance result;
  w_boolean unsafe;

  //threadMustBeSafe(thread);

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
  woempa(7, "Allocating an instance of %k (%d dimensions)\n", clazz, dimensions);
  for (i = 0; i < dimensions; i++) {
    jlong size = ((jlong)clazz->previousDimension->bits) * ((jlong) lengths[0]);
    if (size > 0x7fffffff) {
      throwOutOfMemoryError(thread, -1);
      x_mem_free(Aas);
      return NULL;
    }
    Aas[i].next = (i == dimensions - 1) ? NULL : Aas + i + 1;
    Aas[i].length = lengths[i];
    woempa(7, "Dimension %d has length %d, bits/element is %d\n", i, lengths[i], current->previousDimension->bits);

    Aas[i].clazz = current;
    current = current->previousDimension;
  }

  unsafe = enterUnsafeRegion(thread);      
  woempa(7, "%k root size = %d\n", clazz, 1 + roundBitsToWords(clazz->previousDimension->bits * lengths[0]));
  result = internalAllocArrayInstance(thread, clazz, 1 + roundBitsToWords(clazz->previousDimension->bits * lengths[0]));
  Aas[0].Array = result;
  if (result) {
    result[F_Array_length] = Aas[0].length;
    fillParentArray(thread, Aas);
  }
  if (!unsafe) {
    enterSafeRegion(thread);
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
  woempa(7, "%s global reference to instance %p.\n", found ? "further" : "new", instance);

}

void deleteGlobalReference(w_instance instance) {
  ht_deregister(globals_hashtable, (w_word)instance);
}

#define MIN_MIN_HEAP_FREE (256 * 1024)

#ifdef RESMON
w_hashtable resmon_memory_hashtable;
#endif

void startHeap() {

  lock_hashtable = ht_create((char*)"hashtable:locks", 131, NULL, NULL, 0, 0);
#ifdef RESMON
  resmon_memory_hashtable = ht_create("hashtable:resmon_memory", 13, NULL, NULL, 0, 0);
#endif

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
  w_printf("%6d instances of %k\n", value, key);
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
