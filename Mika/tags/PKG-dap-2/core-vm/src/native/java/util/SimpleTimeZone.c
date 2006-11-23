/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
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
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
*                                                                         *
* Modifications copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: SimpleTimeZone.c,v 1.3 2004/11/18 23:51:52 cvs Exp $
*/

#include <string.h>

#include "core-classes.h"
#include "clazz.h"
#include "dates.h"
#include "heap.h"
#include "ts-mem.h"
#include "threads.h"
#include "wstrings.h"
#include "arrays.h"

#define MSEC1970         (0x000038831c799000LL)             /* nr of milliseconds in 1970          */

/**
* Class:     java_util_SimpleTimeZone
* Method:    setfields
* Signature: ([IJ)V
*/

void SimpleTimeZone_getfields (JNIEnv * env, w_instance this, w_instance Array, w_long ttime){
      	
  w_date wdate;
  w_int * ip =  instance2Array_int(Array);

  wdate = allocMem(sizeof(w_Date));
  if (!wdate) {
    wabort(ABORT_WONKA, "Unable to allocate wdate\n");
  }

  millis2date(ttime + MSEC1970, wdate);

  ip[0] = wdate->year;
  ip[1] = wdate->month-1;
  ip[2] = wdate->day;
  ip[3] = wdate->msec%1000;
  ip[4] = dayOfWeek(wdate);

  releaseMem(wdate);

}
