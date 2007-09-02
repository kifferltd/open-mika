#ifndef _DATES_H
#define _DATES_H

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
** $Id: dates.h,v 1.2 2006/04/05 08:27:33 cvs Exp $
*/

#include "wonka.h"

/*
** Our Wonka time structure. Note that for time of day elements like
** millis, seconds and minutes, we use midnight 00:00 as a reference.
*/

/*
** The maximum that can be represented in a signed long is
** 9223372036854775807 (0x7FFF FFFF FFFF FFFF)
**
** 1 day is 1000 millis x 3600 seconds x 60 minutes x 24 hours = 5184000000 millis
** That means that we can represent 1779199852 days, counting with milliseconds,
** or more than 4 861 201 years.
**
** So all calculations are normalized back to the date 1 AD with the number of 
** milliseconds passed since that date. For comparing and manipulating 2 dates,
** the result some times can be negative when one is bigger than the other. The type
** that represents the milliseconds that Wonka uses for these calculations is
** w_millis, a signed long long.
**
** To make the difference between 'millis' used as our epoch since 1 AD and the 
** number of milliseconds used in other time calculations, we have given the
** name 'msecs' to the element of the w_Time structure, which carries not the same
** meaning NOR type as 'millis'.
*/

#define MSEC1970         (0x000038831c799000LL)             /* nr of milliseconds in 1970          */

#define MSECS_PER_SECOND                 (1000)
#define MSECS_PER_MINUTE                 (MSECS_PER_SECOND * 60)
#define MSECS_PER_HOUR                   (MSECS_PER_MINUTE * 60)
#define MSECS_PER_DAY                    (w_millis)(MSECS_PER_HOUR * 24)

typedef signed long long                 w_millis;
typedef struct w_Date *                  w_date;

typedef struct w_Date {
  w_long msec;            /* milliseconds passed since midnight                     */
  w_int second;           /* seconds passed since midnight                          */
  w_int minute;           /* minutes passed since midnight                          */
  w_int hour;             /* 24 hour clock, hour 0 is 00.00 AM                      */
  w_int day;              /* the day as in dd/mm/yyyy                               */
  w_int month;            /* the month as in dd/mm/yyyy                             */
  w_int year;             /* the year as in dd/mm/yyyy. Note that it starts at 0001 */
} w_Date;

w_millis date2millis(w_date date);
w_int    millis2date(w_millis millis, w_date date);
w_int    dayOfWeek(w_date date);
w_int    dayOfYear(w_date date);
#endif /* _DATES_H */
