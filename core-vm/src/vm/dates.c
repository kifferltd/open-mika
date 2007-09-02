/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
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

/*
** $Id: dates.c,v 1.3 2006/01/03 23:37:27 cvs Exp $
*/

#include <string.h>

#include "dates.h"

/*
** The body of the routines in this file come from the book
** "Practical Algorithms for Programmers", by Andrew Binstock and John Rex,
** Addison Wesley. 
*/

static const w_byte DaysInMonth[]     = {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
static const w_byte DaysInMonthLeap[] = {0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

static w_boolean isLeapYear(w_int year) {

  /*
  ** The most simple check first.
  */

  if (year % 4 != 0) {
    return WONKA_FALSE;
  }

  /*
  ** All years divisible by 4 and that are before 1582 are leap years, since 1582
  ** is the year where Poppy Greg put in effect a compensation scheme...
  */

  if (year < 1582) {
    return WONKA_TRUE;
  }
  
  /*
  ** If the year is divisible by 4 but not by 100, it's a leap year.
  */
  
  if (year % 100 != 0) {
    return WONKA_TRUE;
  }
  
  /*
  ** If it's divisible by 100 and not by 400, it's not a leap year, if it's divisble
  ** by 400, it's a leap year (cfr. 2000 is a leap year).
  */
  
  if (year % 400 != 0) {
    return WONKA_FALSE;
  }
  else {
    return WONKA_TRUE;
  }

}

static w_boolean isValidDate(w_date date) {

  w_int daysInMonth;
  
  if (isLeapYear(date->year)) {
    daysInMonth = DaysInMonthLeap[date->month];
  }
  else {
    daysInMonth = DaysInMonth[date->month];
  }
  
  if (date->msec < 0 || date->msec > MSECS_PER_DAY) {
    return WONKA_FALSE;
  }

  if (date->second < 0 || date->second > 86400) {
    return WONKA_FALSE;
  }

  if (date->minute < 0 || date->minute > 1440) {
    return WONKA_FALSE;
  }

  if (date->hour < 0 || date->hour > 24) {
    return WONKA_FALSE;
  }

  if (date->day < 1 || date->day > daysInMonth) {
    return WONKA_FALSE;
  }

  if (date->month < 1 || date->month > 12) {
    return WONKA_FALSE;
  }

  if (date->year < 1) {
    return WONKA_FALSE;
  }

  return WONKA_TRUE;

}

static w_int date2days(w_date date) {

  w_int days;
  w_int months = date->month;
  w_int years = date->year - 1;
  w_byte const *daysInMonth;

  /*
  ** Get all the days, without taking leap years and stuff into account ...
  */
  
  days = 365 * years;
  
  /*
  ** ... now account for all leap years ...
  */
  
  days += years / 4;
  
  /*
  ** ... now back out all the century years that are not leap years; these are all
  ** the century years that are not evenly divisible by 400: 1700, 1800, 1900, ...
  */
  
  days -= years / 100;
  days += years / 400;

  /*
  ** ... before 1582 all century years are leap years, so adjust for this. If the
  ** year is > 1582, then just add 12 days for the 12 century years that appear
  ** before 1582, otherwise calculate it ...
  */

  if (years > 1582) {
    days += 12;
  }  
  else {
    days += years / 100;
    days -= years / 400;
  }
  
  /*
  ** ... now add the days that have elapsed in the current year so far ...
  */
  
  if (isLeapYear(date->year)) {
    daysInMonth = DaysInMonthLeap;
  }
  else {
    daysInMonth = DaysInMonth;
  }

  if (months > 0) {
    while (--months) {
      days += daysInMonth[months];
    }
  }

  /*
  ** ... add the number of days we are in the month ...
  */
  
  days += date->day;  
  
  /*
  ** ... compensate the 10 day deletion when the change was made to the Gregorian
  ** calendar. This change reflects a jump from October 4 to October 15 in 1582.
  ** A full 10 days are missing. This date corresponds to 577737 days since
  ** 01/01/0001 and we are done!
  */
  
  if (days > 577737) {
    days -= 10;
  }
  
  return days;
  
}

w_millis date2millis(w_date date) {

  w_int days = date2days(date);
  w_millis millis;
  
  millis = (w_millis)(days * MSECS_PER_DAY);
  millis += date->msec;
  
  /*
  //  we started the day so we delete those milliseconds ...
  */
  millis -= (w_millis)MSECS_PER_DAY;

  return millis;
  
}

/*
** Return the day of the week, 1 = Sunday, 2 = Monday, ...
** When in error, it returns -1.
*/

w_int dayOfWeek(w_date date) {

  w_int days = date2days(date);
  w_int day;
  
  if (days < 0) {
    return -1;
  }
  
  day = (w_int)((days+5) %7);
  
  day += 1;
 
  return day;
  
}

/*
** Return the day in the year. When the date is January 1, it returns 1,
** and when the date is December 31, it returns either 365 or 365 depending
** on wether it's a leap year or not.
*/

w_int dayOfYear(w_date date) {

  w_Date jan1;
  w_int day;

  jan1.msec = 0;
  jan1.second = 0;
  jan1.minute = 0;
  jan1.hour = 0;
  jan1.day = 1;
  jan1.month = 1;
  jan1.year = date->year;
  
  day = date2days(date);
  day -= date2days(&jan1);
  day += 1;
  
  return day;
  
}

/*
** Calculate the difference between two dates. The fields of the date structure then
** get the meaning of the difference and are NOT an absolute date. E.g. result->year
** will hold the number of years difference between the two date, result->day the number
** of days difference between the two dates ... All these results are positive, even when
** date_1 is more recent than date_2. The only difference is that this function will return
** -1 when date_1 is more recent than date_2, and will return 1 when date_1 is predating
** date_2. When both are exactly the same up to the number of milliseconds, a 0 is
** returned. Note that the number of years is based 365 day years and the months
** difference is based on the months of 30 days length, which should be
** obvious since we are measuring relative time.

** [CG 20060101] dateDifference() is not used by Wonka.

static w_int dateDifference(w_date date_1, w_date date_2, w_date result) {

  w_int returns = 1;
  w_millis millis_1;
  w_int days_1;
  w_millis millis_2;
  w_int days_2;
  w_millis millis_diff = 0LL;
  w_int days_diff = 0;
  
  days_1 = date2days(date_1);
  days_2 = date2days(date_2);
  
  millis_1 = days_1 * MSECS_PER_DAY;
  millis_1 += date_1->msec;
  millis_2 = days_2 * MSECS_PER_DAY;
  millis_2 += date_2->msec;

  if (days_1 == days_2) {
    if (millis_1 == millis_2) {
      memset(result, 0x00, sizeof(w_Date));
      return 0;
    }
  }
  else if (millis_1 > millis_2) {
    returns = -1;
    millis_diff = millis_1 - millis_2;
    days_diff = days_1 - days_2;
  }
  else {
    returns = 1;
    millis_diff = millis_2 - millis_1;
    days_diff = days_2 - days_1;
  }

  /.
  .. Make millis_diff only the result of real milliseconds difference
  .. between the two dates and not whole days/months/years.
  ./

  millis_diff -= days_diff * MSECS_PER_DAY;

  result->year = (w_int)(days_diff / 365);
  days_diff -= result->year * 365;
  result->month = (w_int)(days_diff / 30);
  days_diff -= result->month * 30;
  result->day = days_diff;
  result->hour = (w_int)(millis_diff / MSECS_PER_DAY);
  millis_diff -= (w_millis)(result->hour * MSECS_PER_DAY);
  result->minute = (w_int)(millis_diff / MSECS_PER_MINUTE);
  millis_diff -= (w_millis)(result->minute * MSECS_PER_MINUTE);
  result->second = (w_int)(millis_diff / MSECS_PER_SECOND);
  millis_diff -= (w_millis)(result->second * MSECS_PER_SECOND);
  result->msec = (w_int)millis_diff;

  return returns;
  
}
*/

w_int millis2date(w_millis millis, w_date date) {

  w_int days = (w_int)(millis / MSECS_PER_DAY);
  w_int day;
  w_int month;
  w_int year;
  w_int i;
  w_byte const *daysInMonth;

  /*
  ** Adjust millis for non-whole days only.
  */
  
  millis -= days * MSECS_PER_DAY;

  /*
  ** When we have a number of days past 1582, we adjust for the 10 days lost
  ** from Thursday, October 4, 1582 to Friday, October 15 1582 which jumped
  ** 10 days.
  */

  if (days > 577737) {
    days += 10;
  }
  
  year = days / 365;
  days = days % 365;

  /*
  ** 'year' now holds the number of elapsed year, so add 1 for the current year.
  */

//  year += 1;
  days++;
  //woempa(9, "DATE: year = %i and days = %i\n",year,days);
  /*
  ** Prior to 1700, all years evenly divisible by 4 are leap.
  */

  if (year < 1700) {
    days -= year / 4;
  }
  else {
    days -= year / 4;      /* deduct the leap years            */
    days += year / 100;    /* add in century years             */
    days -= year / 400;    /* deduct years / 400               */
    days -= 12;            /* deduct century years before 1700 */
  }

  //woempa(9, "leap year correction: year = %i and days = %u\n",year,(unsigned int)days);

  /*
  ** We now make sure that days left is > 0 
  */

  while (days <= 0) {
    w_boolean lpy = isLeapYear(year);
    year -= 1;
    days +=( lpy ? 366 : 365 );
  }
  //woempa(9, "second day correction: year = %i and days = %i\n",year,(signed int)days);

  /*
  ** 'year' now holds the number of elapsed year, so add 1 for the current year.
  */
  
  year += 1;
  
  /*
  ** Now deduct the days in each month, starting from January to find each month and
  ** day of the month, while adjusting for leap years.
  */
  
  day = days;
  month = 0;
  
  if (isLeapYear(year)) {
    daysInMonth = DaysInMonthLeap;
  }
  else {
    daysInMonth = DaysInMonth;
  }
  
  for (i = 1; i < 13; i++) {
    month = i;
    if (day <= daysInMonth[i]) {
      break;
    }
    else {
      day -= daysInMonth[i];
    }
  }
  
  date->year = year;
  date->month = month;
  date->day = day;

  /*
  ** We have in millis the number of milliseconds that don't fit in a whole day;
  ** fill up the rest of the structure.
  */

  date->hour = (w_int)(millis / MSECS_PER_HOUR);
  millis -= (w_millis)(date->hour * MSECS_PER_HOUR);
  date->minute = (w_int)(millis / MSECS_PER_MINUTE);
  millis -= (w_millis)(date->minute * MSECS_PER_MINUTE);
  date->second = (w_int)(millis / MSECS_PER_SECOND);
  millis -= (w_millis)(date->second * MSECS_PER_SECOND);
  date->msec = (w_int)millis;

  if (isValidDate(date)) {
    return 0;
  }
  else {
    return -1;
  }
      
}

