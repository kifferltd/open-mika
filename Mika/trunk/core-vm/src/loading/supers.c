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
* Modifications copyright (c) 2004, 2006 by Chris Gray, /k/ Embedded Java *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: supers.c,v 1.7 2006/06/16 20:08:14 cvs Exp $
*/

#include "clazz.h"
#include "checks.h"
#include "constant.h"
#include "exception.h"
#include "loading.h"
#include "threads.h"

/*
 * If NO_FORMAT_CHECKS is defined, no code to check class file format will
 * be generated (leading to a ~4K smaller binary, but less security).
 */
//#define NO_FORMAT_CHECKS

/*
 * If NO_HIERARCHY_CHECKS is defined, no code to check class file format will
 * be generated (leading to a 400 byte smaller binary, but less security).
 */
//#define NO_HIERARCHY_CHECKS

/*
** The maximum depth of a class in the hierarchy. Deliberately exaggerated,
** so that the reallocMem call will normally free up a useful amount of
** memory.
*/
#define MAX_SUPER_CLASSES 256

w_int loadSuperClasses(w_clazz clazz, w_thread thread) {
  w_clazz super;
  w_int   i;
  w_int   n;
  w_int   retcode;

#ifndef NO_FORMAT_CHECKS
  if (isNotSet(clazz->flags, CLAZZ_IS_TRUSTED) && !clazz->temp.super_index) {
    throwException(thread, clazzVerifyError, "Class %k has no superclass", clazz);

    return CLASS_LOADING_FAILED;
  }
#endif

  clazz->supers = allocMem(MAX_SUPER_CLASSES * sizeof(w_clazz));
  if (!clazz->supers) {
    return CLASS_LOADING_FAILED;
  }
  super = clazz;
  n = MAX_SUPER_CLASSES;
  for (i = 0; i < MAX_SUPER_CLASSES; ++i) {
    if (!super->temp.super_index) {
      woempa(1, "Reached top of hierarchy after %d class(es): %k\n", i, super);
      n = i;
      break;
    }

#ifndef NO_FORMAT_CHECKS
    if (isNotSet(super->flags, CLAZZ_IS_TRUSTED) && !isClassConstant(super, super->temp.super_index)) {
      throwException(thread, clazzClassFormatError, "Superclass of %k is not a class constant (is %02x)", super, super->tags[super->temp.super_index]);

      return CLASS_LOADING_FAILED;

    }
#endif

    super = getClassConstant(super, super->temp.super_index);
    if (!super) {
      throwException(thread, clazzLinkageError, "Cannot resolve superclass of %k", clazz);

      return CLASS_LOADING_FAILED;

    }

#ifndef NO_HIERARCHY_CHECKS
    if (super == clazz) {
      throwException(thread, clazzClassCircularityError, "Class %k is its own superclass", clazz);

      return CLASS_LOADING_FAILED;

    }
#endif

    clazz->supers[i] = super;
    woempa(1, "Class %k supers[%d] = %k\n", clazz, i, super);
    if (getClazzState(super) >= CLAZZ_STATE_SUPERS_LOADED) {
      woempa(1, "Class %k is already supersLoaded, has %d superclasses => depth of %k is %d\n", super, super->numSuperClasses, clazz, i + super->numSuperClasses + 1);
      n = i + super->numSuperClasses + 1;
      break;
    }
  }

  if (n == MAX_SUPER_CLASSES) {
      wabort(ABORT_WONKA, "Class %k has too many superclasses", clazz);
  }

  for (i= i + 1; i < n; ++i) {
    woempa(1, "Copying %k (superclass[%d] of %k) as superclass[%d] of %k\n", super->supers[i - n + super->numSuperClasses], i - n + super->numSuperClasses, super, i, clazz);
    clazz->supers[i] = super->supers[i - n + super->numSuperClasses];
  }

  woempa(1, "Class %k has total of %d superclasses\n", clazz, n);
  clazz->supers = reallocMem(clazz->supers, n * sizeof(w_clazz));
  if (!clazz->supers) {
    return CLASS_LOADING_FAILED;
  }
  clazz->numSuperClasses = n;
  super = clazz->supers[0];
#ifndef NO_HIERARCHY_CHECKS
  if (isNotSet(super->flags, CLAZZ_IS_TRUSTED)) {
    if (isSet(clazz->flags, ACC_INTERFACE) && super != clazzObject) {
      throwException(thread, clazzIncompatibleClassChangeError, "Superclass %k of %k is an interface", super, clazz);

      return CLASS_LOADING_FAILED;

    }
    if (isSet(super->flags, ACC_FINAL)) {
      woempa(9, "Violation of J+JVM Constraint 4.1.1, item 2\n");
      throwException(thread, clazzIncompatibleClassChangeError, "Superclass %k of %k is final", super, clazz);

      return CLASS_LOADING_FAILED;

    }
    if (isNotSet(super->flags, ACC_PUBLIC) && !sameRuntimePackage(clazz, super)) {
      woempa(9, "Violation of J+JVM Constraint 4.1.4\n");
      throwException(thread, clazzIncompatibleClassChangeError, "Superclass %k of %k is not accessible", super, clazz);

      return CLASS_LOADING_FAILED;

    }
  }
#endif

  for (i = n - 1; i >= 0; --i) {
    retcode = mustBeSupersLoaded(clazz->supers[i]);
    if (exceptionThrown(thread)) {

      return CLASS_LOADING_FAILED;

    }
  }

  clazz->flags |= super->flags & CLAZZ_HERITABLE_FLAGS;

  return CLASS_LOADING_SUCCEEDED;
}

#define MAX_INTERFACES 1024

static w_boolean addInterface(w_clazz new, w_clazz *if_array, w_int *l) {
  w_int i;

  for (i = 0; i < *l; ++i) {
    if (if_array[i] == new) {
      return WONKA_FALSE;
    }
  }

  if_array[(*l)++] = new;

  return WONKA_TRUE;
}

w_int loadSuperInterfaces(w_clazz clazz, w_thread thread) {
  w_int   i;
  w_int   j;
  w_int   n;
  w_clazz interfaze;
  w_int   retcode;

  n = 0;
  clazz->interfaces = allocMem(MAX_INTERFACES * sizeof(w_clazz));
  if (!clazz->interfaces) {
    return CLASS_LOADING_FAILED;
  }

  /*
  ** We have to do this in two passes, in order to do the Right Thing in
  ** Class/getInterfaces(). First we put the directly inherited interfaces
  ** into clazz->interfaces[], counting them in clazz->numDirectInterfaces.
  ** In this pass we also call loadSuperInterfaces() on the direct super-
  ** interfaces, and perform a number of checks; and we discard duplicates.
  */
  for (i = 0; i < clazz->temp.interface_index_count; ++i) {
#ifndef NO_FORMAT_CHECKS
    if (isNotSet(clazz->flags, CLAZZ_IS_TRUSTED) && !isClassConstant(clazz, clazz->temp.interface_index[i])) {
      throwException(thread, clazzClassFormatError, "Superinterface of %k is not a class constant (is %02x)", clazz, clazz->tags[clazz->temp.interface_index[i]]);

      return CLASS_LOADING_FAILED;

    }
#endif
    interfaze = getClassConstant(clazz, clazz->temp.interface_index[i]);
    if(interfaze == NULL){

      return CLASS_LOADING_FAILED;

    }

#ifndef NO_HIERARCHY_CHECKS
    if (interfaze == clazz) {
      throwException(thread, clazzClassCircularityError, "Class %k is its own superinterface", clazz);

      return CLASS_LOADING_FAILED;

    }

    if (isNotSet(interfaze->flags, ACC_PUBLIC) && !sameRuntimePackage(clazz, interfaze)) {
      woempa(9, "Violation of J+JVM Constraint 4.1.?\n");
      throwException(thread, clazzIncompatibleClassChangeError, "Superinterface %k of %k is not accessible", interfaze, clazz);

      return CLASS_LOADING_FAILED;

    }
#endif

    if (n == MAX_INTERFACES) {
      wabort(ABORT_WONKA, "Class %k has too many superinterfaces", clazz);
    }

    mustBeSupersLoaded(interfaze);
    if (exceptionThrown(thread)) {

      return CLASS_LOADING_FAILED;

    }

#ifndef NO_HIERARCHY_CHECKS
    if (isNotSet(clazz->flags, CLAZZ_IS_TRUSTED) && isNotSet(interfaze->flags, ACC_INTERFACE)) {
      woempa(9, "Violation of J+JVM Constraint 4.1.?, item ? / 4.1.?, item ?\n");
      throwException(thread, clazzIncompatibleClassChangeError, "Superinterface %k of %k is not an interface", interfaze, clazz);

      return CLASS_LOADING_FAILED;

    }
#endif

    if (addInterface(interfaze, clazz->interfaces, &n)) {
      ++clazz->numDirectInterfaces;
      woempa(1, "Added superinterface %k to %k\n", interfaze, clazz);
    }
    else {
      woempa(1, "Ignored duplicate superinterface %k of %k\n", interfaze, clazz);
    }
    if (exceptionThrown(thread)) {
      break;
    }
  }

  /*
  ** In the second pass, we append all non-duplicate interfaces inherited from
  ** the direct superinterfaces.
  */
  for (i = 0; i < clazz->numDirectInterfaces; ++i) {
    interfaze = clazz->interfaces[i];

    for (j = 0; j < interfaze->numInterfaces; ++j) {
      if (addInterface(interfaze->interfaces[j], clazz->interfaces, &n)) {
        woempa(1, "Added supersuperinterface %k to %k\n", interfaze->interfaces[j], clazz);
      }
      else {
        woempa(1, "Ignored duplicate supersuperinterface %k of %k\n", interfaze->interfaces[j], clazz);
      }
    }
    if (exceptionThrown(thread)) {
      break;
    }
  }

  clazz->numInterfaces = n;
  woempa(1, "Class %k has total of %d superinterfaces, of which %d direct\n", clazz, n, clazz->numDirectInterfaces);
  if (clazz->temp.interface_index) {
    releaseMem(clazz->temp.interface_index);
  }
  clazz->temp.interface_index = NULL;
  clazz->interfaces = reallocMem(clazz->interfaces, n * sizeof(w_clazz));
  if (!clazz->interfaces) {
    return CLASS_LOADING_FAILED;
  }

  for (i = n - 1; i >= 0; --i) {
    retcode = mustBeSupersLoaded(clazz->interfaces[i]);
  }

  return CLASS_LOADING_SUCCEEDED;
}

w_int mustBeSupersLoaded(w_clazz clazz) {
  w_thread thread = currentWonkaThread;
  w_int    state = getClazzState(clazz);
  w_int    result = CLASS_LOADING_DID_NOTHING;
  x_status monitor_status;

#ifdef RUNTIME_CHECKS
  if (state < CLAZZ_STATE_LOADED) {
    wabort(ABORT_WONKA, "%K must be loaded before it can be SupersLoaded\n", clazz);
  }
#endif

  if (state == CLAZZ_STATE_BROKEN) {
  // TODO - is the right thing to throw?
    throwException(thread, clazzNoClassDefFoundError, "%k : %w", clazz, clazz->failure_message);
  }

  if (state >= CLAZZ_STATE_SUPERS_LOADED) {

    return CLASS_LOADING_DID_NOTHING;

  }

  threadMustBeSafe(thread);

  x_monitor_eternal(clazz->resolution_monitor);
  clazz->resolution_thread = thread;
  state = getClazzState(clazz);

  while(state == CLAZZ_STATE_LOADING) {
    monitor_status = x_monitor_wait(clazz->resolution_monitor, CLASS_STATE_WAIT_TICKS);
    if (monitor_status == xs_interrupted) {
      x_monitor_eternal(clazz->resolution_monitor);
    }
    state = getClazzState(clazz);
  }

  if (state == CLAZZ_STATE_LOADED) {
    if (clazz->loader) {
      woempa(1, "Need to load supers of class %k using loader %j\n", clazz, clazz->loader);
    }
    else {
      woempa(1, "Need to load supers of class %k using bootstrap class loader\n", clazz);
    }

    if (isSet(clazz->flags, ACC_FINAL) && isSet(clazz->flags, ACC_ABSTRACT)) {
      woempa(9, "%K: Violation of J+JVM Constraint 4.1.1, item 3\n", clazz);
      throwException(currentWonkaThread, clazzIncompatibleClassChangeError, "Class %k is both FINAL and ABSTRACT", clazz);
      setClazzState(clazz, CLAZZ_STATE_BROKEN);
      x_monitor_notify_all(clazz->resolution_monitor);
      x_monitor_exit(clazz->resolution_monitor);

      return CLASS_LOADING_FAILED;

    }

    setClazzState(clazz, CLAZZ_STATE_SUPERS_LOADING);
    x_monitor_exit(clazz->resolution_monitor);

    woempa(1, "Class %k super_index is %d\n", clazz, clazz->temp.super_index);
    if (clazz->temp.super_index) {
      result = loadSuperClasses(clazz, thread);
    }
    else {
      woempa(1, "Class %k super_index is 0, has no superclasses\n", clazz);
    }

    if (result != CLASS_LOADING_FAILED && clazz->temp.interface_index_count) {
      result = loadSuperInterfaces(clazz, thread);
    }

    if (result == CLASS_LOADING_FAILED) {
      x_monitor_eternal(clazz->resolution_monitor);
      setClazzState(clazz, CLAZZ_STATE_BROKEN);
      x_monitor_notify_all(clazz->resolution_monitor);
      x_monitor_exit(clazz->resolution_monitor);

      return result;

    }

    x_monitor_eternal(clazz->resolution_monitor);
    setClazzState(clazz, CLAZZ_STATE_SUPERS_LOADED);
    x_monitor_notify_all(clazz->resolution_monitor);
  }
  else if (state == CLAZZ_STATE_BROKEN) {
    x_monitor_exit(clazz->resolution_monitor);

    return CLASS_LOADING_FAILED;

  }

  x_monitor_exit(clazz->resolution_monitor);

  return result;
}

