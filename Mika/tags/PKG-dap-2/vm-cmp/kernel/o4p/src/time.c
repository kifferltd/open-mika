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
** $Id: time.c,v 1.18 2006/10/04 14:24:20 cvsroot Exp $
*/    
 
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

// Used for "time runs backwards" hack
long previous_seconds;
long previous_microseconds;

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
  w_fifo fire_fifo = allocFifo(31);
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

void x_now_plus_ticks(x_size ticks, struct timespec *ts)
{
  // volatile 'coz I don't trust gcc any further than I can throw it
  volatile x_long usec, sec; 
  struct timeval now;

  usec =  usecs_per_tick * ticks;
  sec = usec / 1000000;
  usec %= 1000000;

  x_gettimeofday(&now, NULL);
  ts->tv_sec = now.tv_sec + sec;
  ts->tv_nsec = (now.tv_usec + usec) * 1000;

  while (ts->tv_nsec > 1000000000) {
    ts->tv_nsec -= 1000000000;
    ts->tv_sec++;
  }
}

x_boolean x_deadline_passed(struct timespec *ts) {
  struct timeval now;

  x_gettimeofday(&now, NULL);

  return (ts->tv_sec < now.tv_sec)  || ((ts->tv_sec == now.tv_sec) && (ts->tv_nsec <= now.tv_usec));
}

#endif

x_sleep x_time_get() {
  return o4pe->timer_ticks;
}

/*
** Functions for time calculations, mainly copied from oswald
*/

static const x_size ticks_compensation = 2;

/*
** Return a compensated number of ticks for a number of seconds.
*/

x_size x_seconds2ticks(x_size seconds) {
  return (x_size) ((seconds * 1000) / (x_size) (usecs_per_tick / 1000) - ticks_compensation);
}

/*
** Return the number of seconds corresponding to a number of ticks.
** Uncompensated rounded downwards, , can be zero.
x_size x_ticks2secs(x_size ticks) {
  x_long usecs = ticks * usecs_per_tick;
  return usecs / 1000000;
}
*/

/*
** Return an number of ticks for a number of milliseconds.
** The result is uncompensated, but is always at least 1.
*/
x_size x_millis2ticks(x_size millis) {

  x_size ticks = (millis / (x_size) (usecs_per_tick / 1000));
  
  return ticks ? ticks : 1;
  
}

/*
** Return the number of microseconds corresponding to a number of ticks.
** Uncompensated, can be zero.
*/
x_size x_ticks2usecs(x_size ticks) {
  return (usecs_per_tick * ticks);
}

/*
** Return an number of ticks for a number of microseconds.
** The result is uncompensated, but is always at least 1.
*/
x_size x_usecs2ticks(x_size usecs) {
  return (usecs < usecs_per_tick) ? 1 : ((x_size)(usecs / usecs_per_tick));
}
