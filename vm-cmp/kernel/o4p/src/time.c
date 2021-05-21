/**************************************************************************
* Copyright (c) 2008, 2009, 2016, 2018 by Chris Gray, KIFFER Ltd.         *
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
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR        *
* ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR                *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT       *
* OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;         *
* OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF           *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

#include "oswald.h"

#ifdef USE_NANOSLEEP
#include <time.h>
#else
#ifdef LINUX
#include <sys/time.h>
#endif
#endif


#include "fifo.h"
#include "wordset.h"
#include "oswald.h"

x_size usecs_per_tick;

#ifndef HAVE_TIMEDWAIT
/*
 * The set of all threads which are waiting for time to elapse
 */
static w_wordset sleeping_threads;

/*
 * A mutex to protect sleeping_threads
 */
static x_Mutex sleeping_threads_Mutex;

/*
 * What to do when a thread's sleep timer expires.
 */
static void fire_timeout(x_thread thread);

/*
 * The thread which deals with timing issues.
 */
x_Thread timer_Thread;

/*
 * Its entry function.
 */
void timer_entry_function(void* ptr) {
  int i;
  int number_sleeping_threads;
  int missed_ticks = 0;
  w_fifo fire_fifo = allocFifo(30);
  x_thread thread;
#ifdef USE_NANOSLEEP
  struct timespec ts;
  long micros = x_ticks2usecs(1);

  // Try to compensate for inherent "lateness" of nanosleep().
  // Note: we shouldn't do this if a real-time scheduler is being used.
  if (micros >= HOST_TIMER_GRANULARITY) {
    micros -= HOST_TIMER_GRANULARITY - 1;
  }
  ts.tv_sec = 0;
  ts.tv_nsec = micros * 1000;
#endif

  while(1) {
#ifdef USE_NANOSLEEP
    nanosleep(&ts, NULL);
#else
    usleep(x_ticks2usecs(1));
#endif
    o4pe->timer_ticks++;
    if (x_mutex_lock(&sleeping_threads_Mutex, x_no_wait) == xs_success) {
      number_sleeping_threads = sizeOfWordset(&sleeping_threads);
      for (i = 0; i < number_sleeping_threads; ++i) {
        thread = (x_thread)elementOfWordset(&sleeping_threads, i);
        if (thread) {
          thread->sleep_ticks -= missed_ticks + 1; 
          if (thread->sleep_ticks < 0) {
            putFifo(thread, fire_fifo);
            modifyElementOfWordset(&sleeping_threads, i, 0);
          }
        }
      }
      while (sleeping_threads && removeFromWordset(&sleeping_threads, 0));
      x_mutex_unlock(&sleeping_threads_Mutex);
      missed_ticks = 0;
    }
    else {
     //printf("Missed a tick!\n");
      ++missed_ticks;
    }

    while ((thread = getFifo(fire_fifo))) {
      fire_timeout(thread);
    }
  }
}

/*
 * Add a thread to the set of sleeping_threads. The thread's sleep time and 
 * state flags must be set up before this is called (the timer thread could
 * run before control is returned to the caller). Returns TRUE normally,
 * FALSE if insufficient memory.
 */
w_boolean join_sleeping_threads(x_thread thread) {
  w_boolean success = TRUE;
  unsetFlag(thread->flags, TF_TIMEOUT);

  loempa(2, "Adding thread %p to sleeping_threads\n", thread);
  x_mutex_lock(&sleeping_threads_Mutex, x_eternal);
  if (!isInWordset(&sleeping_threads, (w_word)thread)) {
    success = addToWordset(&sleeping_threads, (w_word)thread);
  }
  x_mutex_unlock(&sleeping_threads_Mutex);

  return success;
}

/*
 * Remove a thread from the set of sleeping_threads. May also be called if
 * the thread is not in sleeping_threads, in which case this function does
 * nothing.
 */
void leave_sleeping_threads(x_thread thread) {
  loempa(2, "Removing thread %p from sleeping_threads\n", thread);
  x_mutex_lock(&sleeping_threads_Mutex, x_eternal);
  if (removeFromWordset(&sleeping_threads, (w_word)thread))
  while (removeFromWordset(&sleeping_threads, (w_word)thread)) {
  }
  x_mutex_unlock(&sleeping_threads_Mutex);
}

static void fire_timeout(x_thread thread) {
  pthread_cond_t *cond = thread->sleeping_on_cond;
  pthread_mutex_t *mutex = thread->sleeping_on_mutex;
  int rc;

  if (cond && mutex) {
    pthread_mutex_lock(mutex);
    setFlag(thread->flags, TF_TIMEOUT);
    thread->sleeping_on_cond = NULL;
    thread->sleeping_on_mutex = NULL;
    thread->sleep_ticks = 0; 
    loempa(2, "Timer expired for thread %p -> notifying\n", thread);
    rc = pthread_cond_broadcast(cond);
    pthread_mutex_unlock(mutex);
    if (rc != 0) {
      o4p_abort(O4P_ABORT_PTHREAD_RETCODE, "pthread_cond_broadcast()", rc);
    }
  }
}

#define TIMER_THREAD_STACK_SIZE 65536
/*
 * Set up the timing thread.
 */
void x_setup_timers(x_size millis) {
  x_thread timer_thread = &timer_Thread;

  usecs_per_tick = millis * 1000;
  x_mutex_create(&sleeping_threads_Mutex);
  x_thread_create(timer_thread, timer_entry_function, NULL, NULL, TIMER_THREAD_STACK_SIZE, NUM_PRIORITIES - 1, 0);
}

#else
void x_setup_timers(x_size millis) {
  usecs_per_tick = millis * 1000;
}
#endif

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
//printf("diff sec = %d, usec = %d\n", diff.tv_sec, diff.tv_usec);

// TODO could be time to start using clock_gettime(CLOCK_MONOTONIC, ...)?
  gettimeofday(&now, NULL);
//printf("now  sec = %d, usec = %d\n", now.tv_sec, now.tv_usec);
  timeradd(&now, &diff, &result);
//printf("result = %d %d\n", result.tv_sec, result.tv_usec);
  ts->tv_sec = result.tv_sec;
  ts->tv_nsec = result.tv_usec * 1000;
//printf("   -> %d %d\n", ts->tv_sec, ts->tv_nsec);
}

x_boolean x_deadline_passed(struct timespec *ts) {
  struct timeval now;

  gettimeofday(&now, NULL);

  return (ts->tv_sec < now.tv_sec)  || ((ts->tv_sec == now.tv_sec) && (ts->tv_nsec <= now.tv_usec));
}

x_sleep x_time_get() {
  return o4pe->timer_ticks;
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
    o4p_abort(O4P_ABORT_OVERFLOW, "overflow converting ticks to millis", 0);
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
//printf("%d 000 / %d\n", num, dem);
  while (num > 2000000 && dem > 10) {
    num /= 10;
    dem /= 10;
//printf("%d 000 / %d\n", num, dem);
  }
  x_size ticks = 1000 * num / dem;
//printf("%d\n", ticks);
 
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
** Return an number of ticks for a number of microseconds.
** The result is always at least 1.
*/
x_size x_usecs2ticks(x_size usecs) {
  return (usecs < usecs_per_tick) ? 1 : ((x_size)(usecs / usecs_per_tick));
}
