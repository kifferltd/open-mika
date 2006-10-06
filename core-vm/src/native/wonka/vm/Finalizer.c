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
** $Id: Finalizer.c,v 1.2 2004/07/18 18:43:49 cvs Exp $
*/

#include "clazz.h"
#include "core-classes.h"
#include "fifo.h"
#include "heap.h"
#include "oswald.h"

w_instance Finalizer_nextFinalizee(JNIEnv *env, w_instance theFinalizer) {
  w_instance nextFinalizee;

  if (!finalizer_fifo) {

    return NULL;

  }

  x_mutex_lock(finalizer_fifo_mutex, x_eternal);
  nextFinalizee = getFifo(finalizer_fifo);
  if (nextFinalizee) {
    woempa(7, "Took %j from finalizer_fifo, now has %d elements\n", nextFinalizee, finalizer_fifo->numElements);
  }
  x_mutex_unlock(finalizer_fifo_mutex);

  return nextFinalizee;
}

w_instance Finalizer_nextEnqueueee(JNIEnv *env, w_instance theFinalizer) {
  w_instance nextEnqueueee;

  if (!enqueue_fifo) {

    return NULL;

  }

  x_mutex_lock(enqueue_fifo_mutex, x_eternal);
  nextEnqueueee = getFifo(enqueue_fifo);
  if (nextEnqueueee) {
    woempa(7, "Took %j from enqueue_fifo, now has %d elements\n", nextEnqueueee, enqueue_fifo->numElements);
  }
  x_mutex_unlock(enqueue_fifo_mutex);

  return nextEnqueueee;
}

/**
 ** When the object has been finalized we clear the FINALIZING flag.
 */
void Finalizer_finalized(JNIEnv *env, w_instance theFinalizer, w_instance finalizee) {
  unsetFlag(instance2object(finalizee)->flags, (O_FINALIZING));
}

/**
 ** When the object has been enqueued we clear the ENQUEUEING flag.
 */
void Finalizer_enqueued(JNIEnv *env, w_instance theFinalizer, w_instance enqueuee) {
  unsetFlag(instance2object(enqueuee)->flags, (O_ENQUEUEING));
}


