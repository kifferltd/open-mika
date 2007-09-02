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

#include "wonkatime.h"
#include <unistd.h>
#include <sys/time.h>

/*
 * Result of the previous call to getNativeSystemTime. We don't allow time to go backwards!
 */
static volatile w_long previous_time;
/*
 * Unless, that is, the update_counter goes backwards too.
 * To understand why, imagine that a clock interrupt occurs between the call to gettimeofday()
 * and the succeeding 'if' statement. Then a thread switch might occur, and another thread 
 * might enter this routine and call gettimeofday(). This other thread would presumably get
 * a timeval later than ours, and also later than that stored in previous_time; so it will
 * go ahead and update previous_time and return normally. Now when our thread reaches its 'if'
 * statement it will see the new value of previous_time and get terribly upset. By reading the
 * time_update_counter before calling gettimeofday(), and updating it after updating 
 * previous_time, we are able to detect and ignore this apparent anomaly.
 */
static volatile w_int time_update_counter;


w_long getNativeSystemTime(void) {
  w_long result;
  w_long counter;
  struct timeval now;
  
  counter = time_update_counter;
//  [CG 20040331] You can put this voodoo in, but it slows you down ...
//  usleep(0);
  gettimeofday(&now, NULL);
  result = now.tv_sec;
  result *= 1000;
  result += (now.tv_usec + 500) / 1000;
  if (result < previous_time && counter > time_update_counter) {
//    printf ("getNativeSystemTime(): going backwards! Was %lld, now %lld\n", previous_time, result);
    result = previous_time;
  }
  else {
    previous_time = result;
    time_update_counter = counter + 1;
  }

  return result;
}

