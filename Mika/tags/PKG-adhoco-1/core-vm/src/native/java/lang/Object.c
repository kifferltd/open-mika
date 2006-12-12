/**************************************************************************
* Copyright  (c) 2001, 2002 by Acunia N.V. All rights reserved.           *
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
** $Id: Object.c,v 1.4 2006/10/04 14:24:16 cvsroot Exp $
*/

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

  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {

    return NULL;

  }

  if(clazz->dims) {
    theClone =  cloneArray(thread, thisObject);
    woempa(1, "object is an array: new instance @ %p\n",theClone);
  } else if(implementsInterface(clazz,clazzCloneable)) {

    w_int instanceSize = clazz->instanceSize;
    w_int i;

    theClone = allocInstance_initialized(thread, clazz);

    if (theClone) {
      woempa(1, "cloneable object is of class %k, size is %d words, new instance @ %p\n", clazz, instanceSize,theClone);
      for(i=0;i<instanceSize;++i)
      
        theClone[i] = thisObject[i];
    }

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
  w_long   sleep_millis = millis + ((w_long)nanos >> 20);
  x_sleep  sleep_ticks = sleep_millis ? x_millis2ticks((w_size)sleep_millis) : x_eternal;
  
  if (isNotSet(instance2flags(thisObject), O_HAS_LOCK)) {
    throwException(thread, clazzIllegalMonitorStateException, "not owner");

    return;
  }

  if (thread->flags & WT_THREAD_INTERRUPTED) {
    throwException(thread, clazzInterruptedException, NULL);
    thread->flags &= ~WT_THREAD_INTERRUPTED;

    return;

  }

  waitMonitor(thisObject, sleep_ticks);

  if (thread->flags & WT_THREAD_INTERRUPTED) {
    throwException(thread, clazzInterruptedException, NULL);
    thread->flags &= ~WT_THREAD_INTERRUPTED;
  }
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

