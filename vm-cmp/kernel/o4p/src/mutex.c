/**************************************************************************
* Copyright (c) 2010, 2023 by Chris Gray, KIFFER Ltd.                     *
* All rights reserved.                                                    *
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
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS *
* OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)   *
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,     *
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING   *
* IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE      *
* POSSIBILITY OF SUCH DAMAGE.                                             *
**************************************************************************/

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

  int rc;

  // TODO can we live with non-recursive mutexes (as opposed to monitors)?
  pthread_mutexattr_t attr;

  rc = pthread_mutex_init(&mutex->mtx_mutex, &o4pe->mutexattr);
  if (rc != 0) {
    return xs_unknown;
  }

  pthread_mutexattr_destroy(&attr);

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

     gettimeofday(&end, NULL);
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
       gettimeofday(&now, NULL);
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
