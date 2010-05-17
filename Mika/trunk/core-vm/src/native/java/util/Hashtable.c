/**************************************************************************
* Copyright (c) 2010 by Chris Gray, /k/ Embedded Java Solutions.          *
* All rights reserved.                                   *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of /k/ Embedded Java Solutions nor the names of     *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL /K/ EMBEDDED JAVA SOLUTIONS OR OTHER CONTRIBUTORS BE  *
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR     *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,       *
* EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                      *
**************************************************************************/

#include "core-classes.h"
#include "exception.h"
#include "fields.h"
#include "interpreter.h"
#include "loading.h"
#include "methods.h"
#include "threads.h"
#include "wmath.h"

static w_int i_firstBusySlot(w_instance objectref, w_int start) {
  w_int i = start;
  w_int cap = getIntegerField(objectref, F_Hashtable_capacity);
  w_instance ki = getReferenceField(objectref, F_Hashtable_keys);
  w_instance *ka = instance2Array_instance(ki);

  if (i >= 0) {
    for(; i < cap; ++i) {
      if(ka[i]) { 

        return i;

      }
    }
  }

  return -1;
}

w_int Hashtable_firstBusySlot(JNIEnv *env, w_instance thisHashtable, w_int i) {
  return i_firstBusySlot(thisHashtable, i);
}

static w_method hashCode_method;
static w_method equals_method;

/**
** Get a raw value for the rhnashCode() of keyObject. "Raw" means it may be 
** negative (down to -modulus) if the compiler feels that way, the caller
** needs to test for this and compensate.
*/
static w_int getrawhash(JNIEnv *env, w_instance keyObject, w_int modulus) {
  w_thread thread = JNIEnv2w_thread(env);
  w_frame new_frame;
  w_int hash;

  if (!hashCode_method) {
    // assumed: mustBeInitialized(clazzObject);
    w_instance class_Object = clazz2Class(clazzObject);
    hashCode_method = (*env)->GetMethodID(env, class_Object, "hashCode", "()I");
    woempa(7, "hashCode_method = %M\n", hashCode_method);
  }

  new_frame = activateFrame(thread, virtualLookup(hashCode_method, instance2clazz(keyObject)), 0, 1, keyObject, stack_trace);
  hash = (w_int) new_frame->jstack_top[-1].c;
  deactivateFrame(new_frame, NULL);

  return hash % modulus;
}

/**
** Compare two objects using the first's "equals" method. 
*/
static w_int objectsequal(JNIEnv *env, w_instance aObject, w_instance bObject) {
  w_thread thread = JNIEnv2w_thread(env);
  w_frame new_frame;
  w_boolean match;

  if (!equals_method) {
    // assumed: mustBeInitialized(clazzObject);
    w_instance class_Object = clazz2Class(clazzObject);
    equals_method = (*env)->GetMethodID(env, class_Object, "equals", "(Ljava/lang/Object;)Z");
    woempa(7, "equals_method = %M\n", equals_method);
  }

  woempa(1, "%M (%j)\n", virtualLookup(equals_method, instance2clazz(aObject)), bObject);
  new_frame = activateFrame(thread, virtualLookup(equals_method, instance2clazz(aObject)), 0, 2, aObject, stack_trace, bObject, stack_trace);
  match = (w_boolean) new_frame->jstack_top[-1].c;
  deactivateFrame(new_frame, NULL);

  return match;
}

w_instance Hashtable_get(JNIEnv *env, w_instance thisHashtable, w_instance keyObject) {
  w_thread thread = JNIEnv2w_thread(env);
  w_int cap = getIntegerField(thisHashtable, F_Hashtable_capacity);
  w_int hash;
  w_boolean match;
  w_instance ki = getReferenceField(thisHashtable, F_Hashtable_keys);
  w_instance *ka = instance2Array_instance(ki);
  w_instance candidatekey;

  if (!keyObject) {
    throwException(thread, clazzNullPointerException, NULL);

    return NULL;
  }

  woempa(1, "Hashtable_get: hashtable = %j keyObject = %j\n", thisHashtable, keyObject);
  hash = getrawhash(env, keyObject, cap);

  do {
    if (hash < 0) {
      hash += cap;
    }

    candidatekey = ka[hash];
  woempa(1, "Hashtable_get: hash = %d candidatekey = %j\n", hash, candidatekey);
      
    if(!candidatekey) {

      woempa(1, "Hashtable_get: returning %j\n", NULL);
      return NULL;

    }

    match = objectsequal(env, keyObject, candidatekey);
    if(match) {
      w_instance vi = getReferenceField(thisHashtable, F_Hashtable_values);
      w_instance *va = instance2Array_instance(vi);

      woempa(1, "Hashtable_get: returning %j\n", va[hash]);
      return va[hash];

    }

    hash--;
  } while(1);
}

/*
** [CG 20100517] Not ready for prime time yet, seems that entries are getting
** garbage-collected during a rehash.

void Hashtable_rehash(JNIEnv *env, w_instance thisHashtable) {
  woempa(1, "Hashtable_rehash: hashtable = %j\n", thisHashtable);
  w_thread thread = JNIEnv2w_thread(env);
  w_int cap = getIntegerField(thisHashtable, F_Hashtable_capacity);
  w_int newsize = cap * 2 + 1;
  w_int oldsize = cap;
  w_instance oldki = getReferenceField(thisHashtable, F_Hashtable_keys);
  woempa(1, "Hashtable_rehash: old keys = %j\n", oldki);
  w_instance *oldka = instance2Array_instance(oldki);
  w_instance oldvi = getReferenceField(thisHashtable, F_Hashtable_values);
  woempa(1, "Hashtable_rehash: old values = %j\n", oldvi);
  w_instance *oldva = instance2Array_instance(oldvi);
  w_instance newki = allocArrayInstance_1d(thread, clazzArrayOf_Object, newsize);
  w_instance *newka = instance2Array_instance(newki);
  woempa(1, "Hashtable_rehash: new keys = %j\n", newki);
  w_instance newvi = allocArrayInstance_1d(thread, clazzArrayOf_Object, newsize);
  woempa(1, "Hashtable_rehash: new values = %j\n", newvi);
  w_float loadfactor = getFloatField(thisHashtable, F_Hashtable_loadFactor);
  w_int threshold;
  w_int oldindex;
  w_boolean was_unsafe;

  woempa(1, "Hashtable_rehash: old size = %d new size = %d\n", oldsize, newsize);
  setIntegerField(thisHashtable, F_Hashtable_capacity, newsize);
  threshold = wfp_float32_to_int32_round_to_zero(wfp_float32_mul(wfp_int32_to_float32(newsize), loadfactor));
  setIntegerField(thisHashtable, F_Hashtable_threshold, threshold);
  woempa(1, "Hashtable_rehash: new threshold = %d occupancy = %d\n", threshold, getIntegerField(thisHashtable, F_Hashtable_occupancy));

  // Probably we will always be called in safe mode, but just to be sure ...
  // (we need to go unsafe because we are manipulating reference arrays).
  was_unsafe = enterUnsafeRegion(thread);

  for (oldindex = 0; oldindex < oldsize; ++oldindex) {
    woempa(1, "Hashtable_rehash: oldindex = %d\n", oldindex);
    w_instance keyObject = oldka[oldindex];
    if (keyObject) {
      w_int hash = getrawhash(env, keyObject, newsize);
      do {
        if (hash < 0) {
          hash += newsize;
        }
        woempa(1, "Hashtable_rehash:   hash = %d\n", hash);
        if (!newka[hash]) {
        woempa(1, "Hashtable_rehash:   setting newkeys[%d] to %j\n", hash, keyObject);
          //newka[hash] = keyObject;
          setArrayReferenceField_unsafe(newki, keyObject, hash);
          woempa(1, "Hashtable_rehash:   setting newvalues[%d] to %j\n", hash, oldva[oldindex]);
          setArrayReferenceField_unsafe(newvi, oldva[oldindex], hash);
          break;
        }
        hash--;
      } while(1);
    }
  }

  setReferenceField_unsafe(thisHashtable, newki, F_Hashtable_keys);
  setReferenceField_unsafe(thisHashtable, newvi, F_Hashtable_values);
  // TODO: should probably also popLocalReference(thread, newvi) and ... newki.

  if (!was_unsafe) {
    enterSafeRegion(thread);
  }
}
  
w_instance Hashtable_put(JNIEnv *env, w_instance thisHashtable, w_instance keyObject, w_instance newvalueObject) {
  woempa(1, "Hashtable_put: hashtable = %j key = %j newvalue = %j\n", thisHashtable, keyObject, newvalueObject);
  w_thread thread = JNIEnv2w_thread(env);
  w_int cap = getIntegerField(thisHashtable, F_Hashtable_capacity);
  w_int occupancy = getIntegerField(thisHashtable, F_Hashtable_occupancy);
  w_int threshold = getIntegerField(thisHashtable, F_Hashtable_threshold);
  woempa(1, "Hashtable_put: capacity = %d occupancy = %d occupancy = %d\n", cap, occupancy, threshold);
  w_int hash;
  w_boolean match;
  w_instance ki = getReferenceField(thisHashtable, F_Hashtable_keys);
  w_instance *ka = instance2Array_instance(ki);
  w_instance vi = getReferenceField(thisHashtable, F_Hashtable_values);
  w_instance *va = instance2Array_instance(vi);
  w_instance candidatekey;
  w_instance oldvalue;
  w_boolean was_unsafe;

  if (!keyObject || !newvalueObject) {
    throwException(thread, clazzNullPointerException, NULL);

    return NULL;
  }

  woempa(1, "Hashtable_put: keys = %j values = %j\n", ki, vi);
  hash = getrawhash(env, keyObject, cap);

  was_unsafe = enterUnsafeRegion(thread);
  do {
    if (hash < 0) {
      hash += cap;
    }

    candidatekey = ka[hash];
    woempa(1, "Hashtable_put: hash = %d candidatekey = %j\n", hash, candidatekey);
      
    if (!candidatekey) {
      woempa(1, "Hashtable_put: setting keys[%d] to %j\n", hash, keyObject);
      setArrayReferenceField_unsafe(ki, keyObject, hash);
      woempa(1, "Hashtable_put: setting values[%d] to %j\n", hash, newvalueObject);
      setArrayReferenceField_unsafe(vi, newvalueObject, hash);
      ++occupancy;
      setIntegerField(thisHashtable, F_Hashtable_occupancy, occupancy);
      if (occupancy > threshold) {
        // REHASH
        if (!was_unsafe) {
          enterSafeRegion(thread);
        }
        Hashtable_rehash(env, thisHashtable);
      }
      else if (!was_unsafe) {
        enterSafeRegion(thread);
      }

      woempa(1, "Hashtable_put: returning %j\n", NULL);
      return NULL;
    }

    match = objectsequal(env, candidatekey, keyObject);
    if (match) {
      oldvalue = va[hash];
      pushLocalReference(thread, oldvalue);
      woempa(1, "Hashtable_put: setting values[%d] to %j\n", hash, newvalueObject);
      setArrayReferenceField_unsafe(vi, newvalueObject, hash);

      if (!was_unsafe) {
        enterSafeRegion(thread);
      }

      woempa(1, "Hashtable_put: returning %j\n", oldvalue);
      return oldvalue;
    }

    hash--;
  } while(1);
}
*/

w_int Hashtable_size(JNIEnv *env, w_instance thisHashtable) {
  return getIntegerField(thisHashtable, F_Hashtable_occupancy);
}
  
void fast_Hashtable_firstBusySlot(w_frame frame) {
  w_instance objectref = (w_instance) frame->jstack_top[-2].c;
  w_int i = (w_int) frame->jstack_top[-1].c;


  if (objectref) {
    w_int cap = getIntegerField(objectref, F_Hashtable_capacity);
    w_instance ki = getReferenceField(objectref, F_Hashtable_keys);
    w_instance *ka = instance2Array_instance(ki);
    woempa(1, "fast_Hashtable_firstBusySlot: i = %d cap = %d ki = %j ka = %p\n", i, cap, ki, ka);
    frame->jstack_top -= 1;
    frame->jstack_top[-1].s = 0;
    frame->jstack_top[-1].c = -1;

    if (i >= 0) {
      for(; i < cap; ++i) {
        if(ka[i]) { 
          woempa(1, "fast_Hashtable_firstBusySlot: returning %d\n", i);
          frame->jstack_top[-1].c = i;
          break;
        }
      }
      woempa(1, "fast_Hashtable_firstBusySlot: returning %d\n", -1);
    }
  }
  else {
    enterSafeRegion(frame->thread);
    throwException(frame->thread, clazzNullPointerException, NULL);
    enterUnsafeRegion(frame->thread);
  }
}

/*
w_boolean HashtableElementEnum_hasMoreElements(JNIEnv *env, w_instance thisHashtableElementEnum) {
  return Hashtable_firstBusySlot(env, getReferenceField(thisHashtableElementEnum, F_Hashtable_dollar_HashtableElementEnum_this_dollar_0), getIntegerField(thisHashtableElementEnum, F_Hashtable_dollar_HashtableElementEnum_nextSlot)) >= 0;
}

w_boolean HashtableKeyEnum_hasMoreElements(JNIEnv *env, w_instance thisHashtableKeyEnum) {
  return Hashtable_firstBusySlot(env, getReferenceField(thisHashtableKeyEnum, F_Hashtable_dollar_HashtableKeyEnum_this_dollar_0), getIntegerField(thisHashtableKeyEnum, F_Hashtable_dollar_HashtableKeyEnum_nextSlot)) >= 0;
}
*/


