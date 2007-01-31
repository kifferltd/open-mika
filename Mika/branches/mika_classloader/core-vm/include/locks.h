#ifndef _LOCKS_H
#define _LOCKS_H

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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: locks.h,v 1.3 2006/10/04 14:24:14 cvsroot Exp $
*/

#include "oswald.h"
#include "threads.h"

/*
** initLocks is called during initialisation of Wonka.
*/
void initLocks(void);

/*
** EnterMonitor is used to implement the Java entermonitor opcode,
** and on entry to a synchronized method.
** exitMonitor is used to implement the Java exitmonitor opcode, and on
** exit from a synchronized method.
** waitMonitor and notifyMonitor implement methods wait(), notify(), and
** notifyAll() of java.lang.Thread.  (notifyMonitor takes a parameter to
** distinguish notify() from notifyAll()).
** allocMonitor is used to associate a monitor with an instance if 
** none is already allocated.  It returns the new or existing monitor
** associated with the instance.
** getMonitor is used to get monitor with an instance, or to allocate one if 
** none is already allocated.  It returns the new or existing monitor
** associated with the instance.
** releaseMonitor is used to release system resources (and threads) 
** associated with a monitor: the monitor then no longer exists.
** monitorOwner yields the thread (if any) which owns the monitor
** (if any) associated with the instance, or else NULL.
**
** In all cases the w_instance patameter must be non-NULL.
*/
void enterMonitor(w_instance);
void exitMonitor(w_instance);

void waitMonitor(w_instance, x_sleep timeout);
void notifyMonitor(w_instance, int notifyAll);

x_monitor allocMonitor(w_instance);
void releaseMonitor(w_instance);
x_monitor getMonitor(w_instance);

w_thread monitorOwner(w_instance);

#define NOTIFY_ALL                          1
#define NOTIFY                              0

#endif

