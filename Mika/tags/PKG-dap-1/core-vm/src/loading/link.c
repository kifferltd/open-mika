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
** $Id: link.c,v 1.4 2006/05/24 10:31:55 cvs Exp $
*/

/*
#include <string.h>

#include "checks.h"
#include "descriptor.h"
#include "fields.h"
#include "hashtable.h"
#include "methods.h"
#include "reflection.h"
#include "threads.h"
*/
#include "clazz.h"
#include "constant.h"
#include "exception.h"
#include "loading.h"
#include "wonka.h"

/*
** The ``linked'' state is the last state before class initialisation.
** Currently not much work is done here: however this is probably the
** appropriate moment to invoke a bytecode verifier.
*/

static w_int linkClazz(w_clazz clazz) {
  w_int    result = CLASS_LOADING_DID_NOTHING;

  // nothing to do ...
  result = CLASS_LOADING_SUCCEEDED;

  return result;
}

w_int mustBeLinked(w_clazz clazz) {
  w_thread thread = currentWonkaThread;
  w_int    state = getClazzState(clazz);
  w_int    result = CLASS_LOADING_DID_NOTHING;
  x_status monitor_status;

#ifdef RUNTIME_CHECKS
  threadMustBeSafe(thread);

  if (state < CLAZZ_STATE_LOADED) {
    wabort(ABORT_WONKA, "%K must be loaded before it can be Linked\n", clazz);
  }

  if (exceptionThrown(thread)) {
    woempa(9, "Eh? Exception '%e' already pending in mustBeLinked(%K)\n", exceptionThrown(thread), clazz);
  }
#endif

  if (state == CLAZZ_STATE_BROKEN) {
  // TODO - is the right thing to throw?
    throwException(thread, clazzNoClassDefFoundError, "%k : %w", clazz, clazz->failure_message);

    return CLASS_LOADING_FAILED;

  }

  if (state >= CLAZZ_STATE_LINKED) {

    return CLASS_LOADING_DID_NOTHING;

  }

  result = mustBeReferenced(clazz);

  if (result == CLASS_LOADING_FAILED) {

    return CLASS_LOADING_FAILED;

  }

  x_monitor_eternal(clazz->resolution_monitor);
  state = getClazzState(clazz);

#ifdef RUNTIME_CHECKS
  if(state < CLAZZ_STATE_REFERENCED) {
    wabort(ABORT_WONKA,"INVALID CLAZZ STATE %d for %K",state,clazz);
  }
#endif

  while(state == CLAZZ_STATE_LINKING) {
    monitor_status = x_monitor_wait(clazz->resolution_monitor, CLASS_STATE_WAIT_TICKS);
    if (monitor_status == xs_interrupted) {
      x_monitor_eternal(clazz->resolution_monitor);
    }
    state = getClazzState(clazz);
  }

  if (state == CLAZZ_STATE_REFERENCED) {
    woempa(1, "Linking %K\n", clazz);
    setClazzState(clazz, CLAZZ_STATE_LINKING);
    x_monitor_exit(clazz->resolution_monitor);

    result = linkClazz(clazz);

    x_monitor_eternal(clazz->resolution_monitor);
    if (result == CLASS_LOADING_FAILED) {
      setClazzState(clazz, CLAZZ_STATE_BROKEN);
      x_monitor_notify_all(clazz->resolution_monitor);
      x_monitor_exit(clazz->resolution_monitor);

      return result;

    }

    if(exceptionThrown(thread)) {
      setClazzState(clazz, CLAZZ_STATE_BROKEN);
      result = CLASS_LOADING_FAILED;
    }
    else {
      setClazzState(clazz, CLAZZ_STATE_LINKED);
    }
    x_monitor_notify_all(clazz->resolution_monitor);
  }
  else if (state == CLAZZ_STATE_BROKEN) {
    x_monitor_exit(clazz->resolution_monitor);

    return CLASS_LOADING_FAILED;

  }

  x_monitor_exit(clazz->resolution_monitor);

  return result;
}

