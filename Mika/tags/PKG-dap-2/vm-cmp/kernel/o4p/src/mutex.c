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
* Modifications for Mika(TM) Copyright (c) 2004, 2005, 2006 by Chris Gray,*
* /k/ Embedded Java Solutions, Antwerp, Belgium. All rights reserved.     *
*                                                                         *
**************************************************************************/


/*
** $Id: mutex.c,v 1.5 2006/04/21 13:15:49 cvs Exp $
*/    
 
#include <sys/time.h>

#include "oswald.h"

/*
 * Prototype:
 *   x_status x_mutex_create(x_mutex mutex);
 * Description:
 *   Creates a mutex.
 * Implementation:
 *   It is just mapped to the mutex function provided by POSIX Threads.
 */
x_status x_mutex_create(x_mutex mutex) {

  int status;

  status = pthread_mutex_init(&mutex->mtx_mutex, NULL);
  if (status != 0) {
    return xs_unknown;
  }

  return xs_success;
}

/*
 * Prototype:
 *   x_status x_mutex_delete(x_mutex mutex);
 * Description:
 *   Deletes the specified mutex.
 * Implementation:
 *   It is just mapped to the mutex function provided by POSIX Threads.
 */
x_status x_mutex_delete(x_mutex mutex) {

  x_status rval = xs_success;
  int status;

  status = pthread_mutex_destroy(&mutex->mtx_mutex);
  if (status != 0) {
    rval = xs_unknown;
  }

  return rval;
}

/*
 * Prototype:
 *   x_status x_mutex_lock(x_mutex mutex, x_sleep timeout);
 * Description:
 *   Tries to lock the specified mutex within the specified time.
 * Implementation:
 *   Basically, it is just mapped to the mutex function provided
 *   by POSIX Threads. 
 */
x_status x_mutex_lock(x_mutex mutex, x_sleep timeout) {

  x_status rval = xs_unknown;
  int retcode;

  if (timeout == x_eternal) {
    retcode = pthread_mutex_lock(&mutex->mtx_mutex);
    if (retcode != 0) {
      o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_mutex_lock()", retcode);
    }
    rval = xs_success;
  }
  else if (timeout == x_no_wait) {
    retcode = pthread_mutex_trylock(&mutex->mtx_mutex);
    if (retcode == 0) { 
      rval = xs_success;
    }
    else if (retcode == EBUSY) {
      rval = xs_no_instance;
    }
    else {
      o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_mutex_trylock()", retcode);
    }
  }
  else {
     struct timeval end, now;
     long usec, sec;
     struct timespec one_tick_ts;

     one_tick_ts.tv_sec = 0;
     one_tick_ts.tv_nsec = 1000 * x_ticks2usecs(1);

     usec = x_ticks2usecs(timeout);
     sec = usec / 1000000;
     usec -= sec * 1000000;

     x_gettimeofday(&end, NULL);
     end.tv_sec += sec;
     end.tv_usec += usec;

     while (end.tv_usec > 1000000) {
       end.tv_usec -= 1000000;
       end.tv_sec++;
     }

     do {
       retcode = pthread_mutex_trylock(&mutex->mtx_mutex);
       if (retcode == 0)
         break;

       nanosleep(&one_tick_ts, NULL);
       x_gettimeofday(&now, NULL);
     } while (retcode == EBUSY || (now.tv_sec > end.tv_sec || (now.tv_sec == end.tv_sec && now.tv_usec > end.tv_usec)));

     if (retcode == 0) {
       rval = xs_success;
     }
     else if (retcode == EBUSY) {
       rval = xs_no_instance;
     }
     else {
        o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_mutex_trylock()", retcode);
     }
  }

  return rval;
}

/*
 * Prototype:
 *   x_status x_mutex_unlock(x_mutex mutex);
 * Description:
 *   Unlocks the specified mutex.
 * Implementation:
 *   It is just mapped to the mutex function provided by POSIX Threads.
 */
x_status x_mutex_unlock(x_mutex mutex) {
  x_status rval = xs_success;
  int retcode = pthread_mutex_unlock(&mutex->mtx_mutex);

  if (retcode) {
    o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_mutex_unlock()", retcode);
  }

  return rval;
}
