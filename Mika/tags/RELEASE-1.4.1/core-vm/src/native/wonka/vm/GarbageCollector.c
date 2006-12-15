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
**************************************************************************/

/*
** $Id: GarbageCollector.c,v 1.2 2004/07/18 18:43:49 cvs Exp $
*/

#include "clazz.h"
#include "core-classes.h"
#include "heap.h"
#include "oswald.h"

void GarbageCollector_create(JNIEnv *env, w_instance theGarbageCollector) {
  gc_create(env, theGarbageCollector);
}

void GarbageCollector_collect(JNIEnv *env, w_instance theGarbageCollector) {
  gc_collect(theGarbageCollector);
}

w_int GarbageCollector_request(JNIEnv *env, w_instance theGarbageCollector, w_int requested) {
  return gc_request(requested);
}


w_long GarbageCollector_memTotal(JNIEnv *env, w_instance classGarbageCollector) {
  return x_mem_total();
}

w_long GarbageCollector_memAvail(JNIEnv *env, w_instance classGarbageCollector) {
  return x_mem_avail();
}

