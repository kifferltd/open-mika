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
* Modifications copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: Date.c,v 1.3 2004/11/18 23:51:52 cvs Exp $
*/

#include <string.h>

#include "core-classes.h"
#include "clazz.h"
#include "dates.h"
#include "heap.h"
#include "ts-mem.h"
#include "threads.h"
#include "wstrings.h"

#define isLeapYear(y) (!((y)%4)&&(((y)<1582)((y)%100)||!((y)%400)))

void Date_create_ymdhms(JNIEnv *env, w_instance thisDate, w_int year, w_int month, w_int date, w_int hours, w_int minutes, w_int seconds){
  w_date wonkadate;
  w_long ttime;

  wonkadate = allocMem(sizeof(w_Date));
  if (!wonkadate) {
    wabort(ABORT_WONKA, "Unable to allocate wonkadate\n");
  }
  wonkadate->msec = 0;
  wonkadate->second = seconds;
  wonkadate->minute = minutes;
  wonkadate->hour = hours;
  wonkadate->day = date;
  wonkadate->month = month;
  wonkadate->year = year+1900;
  ttime = date2millis(wonkadate);
  releaseMem(wonkadate);

  setLongField(thisDate, F_Date_millisSince1jan1970, ttime);

}

