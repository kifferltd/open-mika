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
** $Id: Reference.c,v 1.7 2006/10/04 14:24:16 cvsroot Exp $
*/

#include "clazz.h"
#include "core-classes.h"
#include "heap.h"

void Reference_set(JNIEnv *env, w_instance this, w_instance referent) {
  w_thread thread = JNIEnv2w_thread(env);
  w_boolean unsafe = enterUnsafeRegion(thread);
  woempa(1, "Setting reference from %j to %k at %p\n", this, instance2clazz(referent), (w_instance)referent);
  setWotsitField(this, F_Reference_referent, referent);
  if (!unsafe) {
    enterSafeRegion(thread);
  }
}

w_instance Reference_get(JNIEnv *env, w_instance this) {
  w_instance referent = getWotsitField(this, F_Reference_referent);
  woempa(1, "Getting reference from %j to %j\n", this, referent);

  if (!referent) {
    return NULL;
  }

  if (gc_monitor) {
    x_monitor_eternal(gc_monitor);
    while (gc_phase < GC_PHASE_SWEEP || (gc_phase == GC_PHASE_SWEEP && sweeping_thread)) {
      x_monitor_wait(gc_monitor, x_eternal);
    }

    //Ask for the reference again since it could set to NULL by the collector
    referent = getWotsitField(this, F_Reference_referent);

    if (!referent) {
      x_monitor_exit(gc_monitor);
      return NULL;
    }

    setFlag(instance2object(referent)->flags, O_BLACK);
    x_monitor_exit(gc_monitor);
  }
  else {
    setFlag(instance2object(referent)->flags, O_BLACK);
  }

  return referent;
}

void Reference_clear(JNIEnv *env, w_instance this) {
  w_thread thread = JNIEnv2w_thread(env);
  w_boolean unsafe = enterUnsafeRegion(thread);
  woempa(1, "User cleared reference from %j to %j\n", this, getWotsitField(this, F_Reference_referent));
  clearWotsitField(this, F_Reference_referent);
  if (!unsafe) {
    enterSafeRegion(thread);
  }
}

