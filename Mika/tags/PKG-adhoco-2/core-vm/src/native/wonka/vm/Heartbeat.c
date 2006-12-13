/**************************************************************************
* Copyright (c) 2002, 2003 by Acunia N.V. All rights reserved.            *
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
*                                                                         *
* Modifications copyright (c) 2005 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: Heartbeat.c,v 1.4 2005/09/30 14:49:13 cvs Exp $
*/
#include "clazz.h"
#include "core-classes.h"
#include "threads.h"

#ifdef UPTIME_LIMIT
#include <stdio.h>

static x_time stop_time;
static w_int  time_remaining;
static x_time next_warning;
#endif

#ifdef ACADEMIC_LICENCE
#include <stdio.h>
#endif

extern int wonka_killed;
extern x_thread heartbeat_thread;

void Heartbeat_create(JNIEnv *env, w_instance theHeartbeat) {
#ifdef UPTIME_LIMIT
  stop_time = x_millis2ticks(UPTIME_LIMIT * 1000);
  time_remaining = UPTIME_LIMIT;
  next_warning = 0;
  printf ("/------------------------------------------------------------\\\n");
  printf ("|                      IMPORTANT NOTICE                      |\n");
  printf ("|                                                            |\n");
  printf ("| This copy of Mika is supplied for evaluation purposes and  |\n");
  printf ("| may not be distributed or incorporated into any product.   |\n");
  printf ("| For details of distribution terms contact:                 |\n");
  printf ("|   /k/ Embedded Java Solutions                              |\n");
  printf ("|   Bredestraat 4                                            |\n");
  printf ("|   2000 Antwerpen, Belgium                                  |\n");
  printf ("\\------------------------------------------------------------/\n");
  printf ("\n");
#endif
}

w_int Heartbeat_numberNonDaemonThreads(JNIEnv *env, w_instance theClass) {
  
  return nondaemon_thread_count;
}

extern int dumping_info;

w_boolean Heartbeat_isKilled(JNIEnv *env, w_instance theClass) {
#ifdef ACADEMIC_LICENCE
  static x_time next_warning = 0;
  if (x_time_get() >= next_warning) {
    fprintf(stderr, "/-----------------------------------------------------------\\\n");
    fprintf(stderr, "|                    IMPORTANT NOTICE                       |\n");
    fprintf(stderr, "| This copy of Mika is supplied for academic study purposes |\n");
    fprintf(stderr, "| and may not be distributed or incorporated into any       |\n");
    fprintf(stderr, "| commercial product or service.                            |\n");
    fprintf(stderr, "| For commercial licensing terms contact k-sales@kiffer.be |\n");
    fprintf(stderr, "\\-----------------------------------------------------------|\n");
    fprintf(stderr, "\n");
    next_warning += x_millis2ticks(300 * 1000);
  }
#endif

#ifdef UPTIME_LIMIT
  if (x_time_get() >= next_warning) {
    printf ("Time-limited demo version : Mika will terminate in %d seconds\n", time_remaining);
    next_warning += x_millis2ticks(60 * 1000);
    time_remaining -= 60;
  }
  if (x_time_get() >= stop_time) {
    printf ("Uptime limit reached : terminating execution\n");

    return WONKA_TRUE;
  }
#endif

  // Reset the dumping_info flag with a one-cycle delay (is hack).
  switch(dumping_info) {
    case 1: 
      dumping_info = -1; 
      break;
    case -1: 
      dumping_info = 0;
  }

  return wonka_killed;
}

w_void Heartbeat_setThread(JNIEnv *env, w_instance thisObject, w_instance thread) {
  heartbeat_thread = ((w_thread)getWotsitField(thread, F_Thread_wotsit))->kthread;
}

