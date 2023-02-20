/**************************************************************************
* Copyright (c) 2020 by KIFFER Ltd. All rights reserved.                  *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       *
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    *
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         *
* OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  *
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                              *
**************************************************************************/

#include "oswald.h"

#include "fifo.h"
#include "oswald.h"

x_size usecs_per_tick;

void x_setup_timers(x_size millis_per_tick) {
  usecs_per_tick = millis_per_tick * 1000;
}

x_long x_time_now_millis() {
  struct timeval tv;
  x_long result;

// TODO could be time to start using clock_gettime(CLOCK_MONOTONIC, ...)?
  gettimeofday(&tv, NULL);
  result = (x_long)tv.tv_sec * 1000;
  result += ((x_long)tv.tv_usec + 500) / 1000;

  return result;
}

void x_now_plus_ticks(x_long ticks, struct timespec *ts)
{
  // volatile 'coz I don't trust gcc any further than I can throw it
  volatile x_long usec; 
  struct timeval now, diff, result;

  usec =  x_ticks2usecs(ticks);

  diff.tv_sec = usec / 1000000;
  diff.tv_usec = usec % 1000000;

// TODO could be time to start using clock_gettime(CLOCK_MONOTONIC, ...)?
  gettimeofday(&now, NULL);
  timeradd(&now, &diff, &result);
  ts->tv_sec = result.tv_sec;
  ts->tv_nsec = result.tv_usec * 1000;
}

x_boolean x_deadline_passed(struct timespec *ts) {
  struct timeval now;

  gettimeofday(&now, NULL);

  return (ts->tv_sec < now.tv_sec)  || ((ts->tv_sec == now.tv_sec) && (ts->tv_nsec <= now.tv_usec));
}

/*
** Functions for time calculations
*/

/*
** Return a number of ticks for a number of seconds.
*/

x_size x_seconds2ticks(x_size seconds) {
  if (usecs_per_tick < 1000000) {
    return (x_size) ((seconds * 1000000) / (x_size) usecs_per_tick);
  }
  return (x_size) ((seconds * 1000) / (x_size) (usecs_per_tick / 1000));
}

/*
** Return the number of milliseconds corresponding to a number of ticks.
** Rounded downwards, can be zero.
*/
x_long x_ticks2millis(x_long ticks) {
  x_long msecs = ticks * (usecs_per_tick / 1000);

  if (msecs / (usecs_per_tick / 1000) != ticks) {
    o4f_abort(O4F_ABORT_OVERFLOW, "overflow converting ticks to millis", 0);
  }

  return msecs;
}

/*
** Return an number of ticks for a number of milliseconds (max = 2000000000).
** The result is always at least 1.
*/
x_size x_millis2ticks(x_size millis) {

  x_size num = millis;
  x_size dem = (x_size) usecs_per_tick;
  while (num > 2000000 && dem > 10) {
    num /= 10;
    dem /= 10;
  }
  x_size ticks = 1000 * num / dem;
 
  return ticks ? ticks : 1;
  
}

/*
** Return the number of microseconds corresponding to a number of ticks.
** Can return zero.
*/
x_long x_ticks2usecs(x_size ticks) {
  return ((x_long)usecs_per_tick * (x_long)ticks);
}

/*
** Return a number of ticks for a number of microseconds.
** The result is always at least 1.
*/
x_size x_usecs2ticks(x_size usecs) {
  return (usecs < usecs_per_tick) ? 1 : ((x_size)(usecs / usecs_per_tick));
}
