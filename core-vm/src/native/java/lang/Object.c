/**************************************************************************
* Parts copyright (c) 2001, 2002 by Punch Telematix.                      *
* All rights reserved.                                                    *
* Parts copyright (c) 2004, 2005, 2006, 2008, 2011 by Chris Gray,         *
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

#include "arrays.h"
#include "checks.h"
#include "clazz.h"
#include "exception.h"
#include "loading.h"
#include "locks.h"
#include "Math.h"
#include "methods.h"
#include "oswald.h"
#include "wstrings.h"


static inline w_int objectHashCode(w_instance instance) {

  return (w_int)instance;

}


w_instance Object_clone(JNIEnv *env, w_instance thisObject) {
  w_thread thread = JNIEnv2w_thread(env);
  w_clazz  clazz = instance2clazz(thisObject);
  w_instance theClone = NULL;

  threadMustBeSafe(thread);
  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {

    return NULL;

  }

  if(clazz->dims) {
    theClone =  cloneArray(thread, thisObject);
    woempa(1, "object is an array: new instance @ %p\n",theClone);
  } else if(implementsInterface(clazz,clazzCloneable)) {

    w_int instanceSize = clazz->instanceSize;
    w_int i;

    enterUnsafeRegion(thread);
    theClone = allocInstance(thread, clazz);

    if (theClone) {
      woempa(1, "cloneable object is of class %k, size is %d words, new instance @ %p\n", clazz, instanceSize,theClone);
      for(i=0;i<instanceSize;++i)
      
        theClone[i] = thisObject[i];
    }
    enterSafeRegion(thread);

  }
  else {
    throwException(thread, clazzCloneNotSupportedException, NULL);
  }

  return theClone;
}

w_instance 
Object_getClass (
  JNIEnv *env, w_instance thisObject
) {
  w_clazz clazz = instance2clazz(thisObject);

  woempa(1, "Getting class instance of %k.\n", instance2clazz(thisObject));

  return clazz2Class(clazz);

}

w_boolean Object_equals(JNIEnv *env, w_instance thisObject, w_instance thatObject) {

  if (thisObject == thatObject) {
    return WONKA_TRUE;
  }

  return WONKA_FALSE;
  
}

w_int Object_hashCode(JNIEnv *env, w_instance thisObject) {
  w_int hashcode;

  woempa(1, "Getting hash code of %j.\n", thisObject);
  
  hashcode = objectHashCode(thisObject);
  woempa(1, "Instance %p hash code is 0x%08x\n", thisObject, hashcode);

  return hashcode;
}

void Object_wait(JNIEnv *env, w_instance thisObject, w_long millis, w_int nanos) {
  w_thread thread = JNIEnv2w_thread(env);
  x_sleep  sleep_ticks = 0;
 
  if(millis < 0 || nanos < 0 || nanos >= 1000000) {
    throwException(thread,clazzIllegalArgumentException,NULL);
  }

 
  if (isNotSet(instance2flags(thisObject), O_HAS_LOCK)) {
    throwException(thread, clazzIllegalMonitorStateException, "lock has no owner");

    return;
  }

  if (testForInterrupt(thread)) {
    return;
  }

  /*
   * The combination millis==0, nanos==0 means "wait forever".
   * But any nonzero number of nanos would mean "wait for a very short time" ...
   */
  if (millis == 0) {
    if (nanos == 0) {
      sleep_ticks = x_eternal;
    }
    else {
      sleep_ticks = 1;
    }
  }

  /* [CG 20110126, see comment in Thread_wait0 dated 20070509]
  ** This isn't ideal, because the time taken to go around the loop is
  ** additional to the wait time, so we could build up a cumulative
  ** excess of expectancy. However I don't think it's worth it to try
  ** to read the system clock and perform arithmetic on it.
  ** All of this because x_sleep is 32 bits instead of 64 ...
  */
  while (millis > THREE_WEEKS_MILLIS) {
    waitMonitor(thisObject, THREE_WEEKS_TICKS);
    millis -= THREE_WEEKS_MILLIS;

    if (testForInterrupt(thread)) {
      return;
    }
  }

  if (!sleep_ticks) {
    sleep_ticks = x_millis2ticks((w_size)millis);
  }
  if (sleep_ticks) {
    waitMonitor(thisObject, sleep_ticks);
  }

  testForInterrupt(thread);
}

void Object_notify(JNIEnv *env, w_instance thisObject) {

  woempa(1, "Env %p; Notify single thread waiting on instance of %k.\n", env, instance2clazz(thisObject));

  if (isNotSet(instance2flags(thisObject), O_HAS_LOCK)) {
    throwException(JNIEnv2w_thread(env), clazzIllegalMonitorStateException, "not owner");

    return;
  }

  notifyMonitor(thisObject, NOTIFY);
  
}

void Object_notifyAll(JNIEnv *env, w_instance thisObject) {

  woempa(1, "Env %p; Notify all threads waiting on instance of %k.\n", env, instance2clazz(thisObject));

  if (isNotSet(instance2flags(thisObject), O_HAS_LOCK)) {
    throwException(JNIEnv2w_thread(env), clazzIllegalMonitorStateException, "not owner");

    return;
  }

  notifyMonitor(thisObject, NOTIFY_ALL);
  
}

