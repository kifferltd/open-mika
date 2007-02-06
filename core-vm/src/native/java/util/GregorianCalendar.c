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
** $Id: GregorianCalendar.c,v 1.3 2006/04/05 08:27:33 cvs Exp $
*/

#include <string.h>

#include "arrays.h"
#include "core-classes.h"
#include "dates.h"
#include "fields.h"

//#define MSEC1970         (0x00003883219fec00LL)             /* nr of milliseconds in 1970          */
//#define MSEC1970         (0x000038831C799000LL)             /* nr of milliseconds in 1970          */

static const w_byte DaysInMonth[]     = {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
static const w_byte DaysInMonthLeap[] = {0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

w_boolean checkLeapYear(w_int year) {

  if (year % 4 != 0) {
    return WONKA_FALSE;
  }
  if (year < 1582) {
    return WONKA_TRUE;
  }
  if (year % 100 != 0) {
    return WONKA_TRUE;
  }
  if (year % 400 != 0) {
    return WONKA_FALSE;
  }
  else {
    return WONKA_TRUE;
  }

}

/**
* Class:     java_util_GregorianCalendar
* Method:    setfields
* Signature: (I)V
*/
void GregorianCalendar_setfields (JNIEnv * env, w_instance this, w_int offset){
      	
 	w_date wdate;
 	w_long ttime;
  w_instance flds = getReferenceField(this, F_Calendar_fields);
  w_int * ip = instance2Array_int(flds);

  wdate = allocMem(sizeof(w_Date));
  if (!wdate) {
    wabort(ABORT_WONKA, "Unable to allocate wdate\n");
  }

	ttime = getLongField(this, F_Calendar_time);
 	ttime += MSEC1970 +(w_millis)offset;  	
 	
 	millis2date(ttime, wdate);
  *(ip+clazzCalendar->staticFields[F_Calendar_MONTH])=wdate->month-1;
  *(ip+clazzCalendar->staticFields[F_Calendar_YEAR])=wdate->year;
  *(ip+clazzCalendar->staticFields[F_Calendar_DATE])=wdate->day;
  *(ip+clazzCalendar->staticFields[F_Calendar_HOUR_OF_DAY])=wdate->hour;
  *(ip+clazzCalendar->staticFields[F_Calendar_MINUTE])=wdate->minute%60;
  *(ip+clazzCalendar->staticFields[F_Calendar_SECOND])=wdate->second%60;
  *(ip+clazzCalendar->staticFields[F_Calendar_MILLISECOND])=wdate->msec%1000;
  *(ip+clazzCalendar->staticFields[F_Calendar_DAY_OF_YEAR])=dayOfYear(wdate);
  *(ip+clazzCalendar->staticFields[F_Calendar_DAY_OF_WEEK])=dayOfWeek(wdate);

	releaseMem(wdate);	
}
/**
* Class:     java_util_GregorianCalendar
* Method:    settime
* Signature: (I)V
*/

void GregorianCalendar_settime (JNIEnv * env, w_instance this, w_int fdw, w_int datecase){

  // fdw == First_Day_Of_Week
  //Sunday = 1, Monday = 2, ... Saturday = 7
  // let's check if we can calculate the time ...
  w_date wdate;
  w_long ttime;
  w_instance flds = getReferenceField(this, F_Calendar_fields);
  w_int * ip =  instance2Array_int(flds);
  w_int dow=0;
  w_byte * mp;

  wdate = allocMem(sizeof(w_Date));
  if (!wdate) {
    return;
  }
  wdate->year = *(ip+clazzCalendar->staticFields[F_Calendar_YEAR]);

	
  switch (datecase) {	
    case 1:
      wdate->month = *(ip+clazzCalendar->staticFields[F_Calendar_MONTH]);
      wdate->day = *(ip+clazzCalendar->staticFields[F_Calendar_DATE]);
      break;
    case 2:
      wdate->month = *(ip+clazzCalendar->staticFields[F_Calendar_MONTH]);
      wdate->day = 1;
      dow = dayOfWeek(wdate);
      if (dow == -1) break;
      wdate->day = (*(ip+clazzCalendar->staticFields[F_Calendar_WEEK_OF_MONTH]))*7+
          (*(ip+clazzCalendar->staticFields[F_Calendar_DAY_OF_WEEK])- fdw + 1)%7 -((dow-fdw)%7);
      break;
    case 3:
      wdate->month = *(ip+clazzCalendar->staticFields[F_Calendar_MONTH]) + 1;
      wdate->day = 1;
      dow = dayOfWeek(wdate);
      wdate->month--;
      if (dow == -1) break;
      wdate->day = (*(ip+clazzCalendar->staticFields[F_Calendar_DAY_OF_WEEK])- dow + 1)%7;
      dow = *(ip+clazzCalendar->staticFields[F_Calendar_DAY_OF_WEEK_IN_MONTH]);
      if (dow == 0) break;
      if (dow > 0) {
       	wdate->day += ((dow-1)*7); 	
      }
      else {
        mp = (w_byte *)  (checkLeapYear(wdate->year) ? &DaysInMonthLeap : &DaysInMonth);
        while (wdate->day <mp[wdate->month%12]-7) {
          wdate->day += 7;
        }
        while (++dow <0 && wdate->day > 7) {
          wdate->day -= 7;
        }
      }
      break;
    case 4:
      wdate->day = *(ip+clazzCalendar->staticFields[F_Calendar_DAY_OF_YEAR]);
      wdate->month = 0;
      break;
    case 5:
      wdate->month = 0;
      wdate->day = 1;		
      dow = dayOfWeek(wdate);
      if (dow == -1) break;
      wdate->day = (*(ip+clazzCalendar->staticFields[F_Calendar_WEEK_OF_YEAR])-1)*7+
      (*(ip+clazzCalendar->staticFields[F_Calendar_DAY_OF_WEEK])- fdw + 1)%7-((dow-fdw)%7);     	
  }

  if (wdate->month < 0) {
    wdate->year += wdate->month / 12 -1;
    wdate->month = (wdate->month % 12) + 13;
  } else {
    wdate->year += wdate->month / 12;
    wdate->month = (wdate->month % 12)+1;
  }

  // we don't check these values ...
  wdate->msec = *(ip+clazzCalendar->staticFields[F_Calendar_HOUR_OF_DAY]);
  wdate->msec = *(ip+clazzCalendar->staticFields[F_Calendar_MINUTE]) + (60LL * wdate->msec);
  wdate->msec = *(ip+clazzCalendar->staticFields[F_Calendar_SECOND]) + (60LL * wdate->msec);
  wdate->msec = *(ip+clazzCalendar->staticFields[F_Calendar_MILLISECOND]) + (1000LL * wdate->msec);
  ttime = date2millis(wdate);
  //printf("DEBUG got time %x %x year = %i m %i d %i\n", ((w_int *)&ttime)[1],((w_int *)&ttime)[0],wdate->year, wdate->month, wdate->day);
  ttime -= MSEC1970;
  //printf("DEBUG got time %x %x year = %i m %i d %i\n", ((w_int *)&ttime)[1],((w_int *)&ttime)[0],wdate->year, wdate->month, wdate->day);
  releaseMem(wdate);
 	
  setLongField(this, F_Calendar_time, ttime);
}

