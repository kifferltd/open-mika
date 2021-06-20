/**************************************************************************
* Copyright (c) 2001, 2002 by Punch Telematix. All rights reserved.       *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

#ifndef _LOCKS_H
#define _LOCKS_H

#include "oswald.h"
#include "mika_threads.h"

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

