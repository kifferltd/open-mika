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

