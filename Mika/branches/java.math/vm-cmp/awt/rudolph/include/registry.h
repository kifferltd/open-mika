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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


/* $Id: registry.h,v 1.1 2005/06/14 08:48:24 cvs Exp $ */

#ifndef _REGISTRY_H
#define _REGISTRY_H

#include "rudolph.h"

/*
** Debug functions:
*/

inline char *registry_spaces(w_int depth);
w_void registry_dump(r_canvas canvas, w_int depth);

/*
** Canvas creation functions:
*/

r_canvas registry_constructor(int w, int h);

/*
** Global registry functions:
*/

w_void registry_addComponent(r_canvas canvas, r_component component, w_int pos);
w_void registry_addContainer(r_canvas canvas, r_canvas panel, w_int pos);

/*
** Unregister function:
*/

w_void registry_delComponent(r_component);

inline static void rudolph_lock(void) {
  x_monitor_enter(tree_lock, x_eternal);
}

inline static void rudolph_unlock(void) {
  x_monitor_exit(tree_lock);
}

#ifdef DEBUG
inline static void rudolph_assert_monitor(void) {
w_thread thread = currentWonkaThread;
  if (tree_lock->owner != thread->kthread) {
    woempa(9, "BUG: we don't own the big rudolph lock (BRL) and yet we are using the component tree?\n");
    abort();
  }
}
#else
inline static void rudolph_assert_monitor(void) {
}
#endif

#endif
