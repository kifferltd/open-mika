/**************************************************************************
* Copyright (c) 2021 by KIFFER Ltd. All rights reserved.                  *
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

// TODO - use a HAL to abstract over OS

#ifndef FREERTOS
#ifdef USE_NANOSLEEP
#include <time.h>
#else
#include <sys/time.h>
#endif
#endif

#include "clazz.h"
#include "core-classes.h"
#include "mika_threads.h"
#include "wstrings.h"

#ifdef UPTIME_LIMIT
#include <stdio.h>

static x_time stop_time;
static w_int  time_remaining;
static x_time next_warning;
#endif

static w_boolean detect_deadlocks;

static w_fifo deadlock_fifo;
static w_int fifo_length;
static w_thread *chain;
static w_int chain_length;
static w_thread *checked;
static w_int checked_length;

static w_long system_time_offset;
static w_boolean collecting;
static w_boolean inited;

#ifdef ACADEMIC_LICENCE
#include <stdio.h>
#endif

extern int wonka_killed;
extern x_thread heartbeat_thread;

void Heartbeat_create(w_thread thread, w_instance theHeartbeat, w_boolean detectDeadlocks) {

#ifdef UPTIME_LIMIT
  stop_time = x_millis2ticks(UPTIME_LIMIT * 1000);
  time_remaining = UPTIME_LIMIT;
  next_warning = 0;
  printf ("/------------------------------------------------------------\\\n");
  printf ("|                      IMPORTANT NOTICE                      |\n");
  printf ("|                                                            |\n");
  printf ("| This copy of Mika is supplied for evaluation purposes and  |\n");
  printf ("| may not be distributed or incorporated into any product.   |\n");
  printf ("| For details of distribution terms contact:                 |\n");
  printf ("|   /k/ Embedded Java Solutions                              |\n");
  printf ("|   Bredestraat 4                                            |\n");
  printf ("|   2000 Antwerpen, Belgium                                  |\n");
  printf ("\\------------------------------------------------------------/\n");
  printf ("\n");
#endif

  detect_deadlocks = detectDeadlocks;
}

w_int Heartbeat_numberNonDaemonThreads(w_thread thread, w_instance theClass) {
  
  return nondaemon_thread_count;
}

static void printDeadlock(w_thread t0, w_thread t2) {
  w_thread t;
  w_int i;

  w_printf("- - - DEADLOCK DETECTED - - -\n");
  for (i = 0; i < chain_length; ++i) {
    t = chain[i];
    w_printf("  %t is competing for monitor %p\n", t, t->kthread->waiting_on);
    t = t->kthread->waiting_on->owner->xref;
    w_printf("    which is owned by %t\n", t);
  }
  w_printf("- - - - - - - - - - - - - - -\n");
}

static void addToList(w_thread t, w_thread *list, w_int *length) {
  w_int i;

  for (i = 0; i < *length; ++i) {
    if (list[i] == t) {
      return;
    }
  }

  woempa(7, "Adding %t to chain at position[%d]\n", t, *length);
  list[*length] = t;
  ++*length;
}

static w_boolean isPresent(w_thread t, w_thread *list, w_int length) {
  w_int i;

  for (i = 0; i < length; ++i) {
    if (list[i] == t) {
      return TRUE;
    }
  }

  return FALSE;
}

static void checkForDeadlock(w_thread t0) {
  w_thread t1 = t0;
  w_int i;
  w_int count = 0;

  chain_length = 0;
  if (!isPresent(t0, checked, checked_length)) {
    woempa(7, "checking %t for deadlocks\n", t0);
    addToList(t0, checked, &checked_length);
    // upper bound is there just to prevent infinite looping
    for (i = 0; i < fifo_length * 2; ++i) {
      x_thread xt = t1->kthread;
      addToList(t1, chain, &chain_length);
      if (isSet(xt->flags, TF_COMPETING)) {
        x_monitor waiting_on = xt->waiting_on;
        if (waiting_on) {
          woempa(7, "  %t is competing for monitor %p\n", t1, waiting_on);
           x_thread owner = waiting_on->owner;
          if (owner) {
            w_thread t2 = owner->xref;
            woempa(7, "    which is owned by %t\n", t2);
            if (t2 && t2 != t1 && isPresent(t2, chain, chain_length)) {
              // Only trigger if we get around the loop twice - this is to
              // prevent false positives due to the graph changing during
              // the scan
              ++count;
              woempa(7, "%t already in chain - count = %d\n", t2, count);
              if (count >= chain_length) {
                printDeadlock(t0, t2);
                if (detect_deadlocks) {
                  wabort(ABORT_WONKA, "Deadlock detected involving threads %t, %t", t1, t2);
                }
                break;
              }
            }
            else {
              count = 0;
            }
              t1 = t2;
          }
          else {
            woempa(7, "    which has no owner\n");
            break;
          }
        }
        else {
          woempa(7, "  funny, %t is flagged TF_COMPETING but waiting_on == NULL\n", t1);
          break;
        }
      }
      else {
        woempa(7, "  %t is not competing for any monitor\n", t1);
        addToList(t1, checked, &checked_length);
        break;
      }
    }
  }
  else {
    woempa(7, "not checking %t for deadlocks, is already checked\n", t0);
    woempa(7, "blocking_all_threads = %s\n", BLOCKED_BY_TEXT);
  }
}


extern int dumping_info;

w_boolean Heartbeat_isKilled(w_thread thread, w_instance theClass) {
#ifdef ACADEMIC_LICENCE
  static x_time next_warning = 0;
  if (x_time_get() >= next_warning) {
    fprintf(stderr, "/-----------------------------------------------------------\\\n");
    fprintf(stderr, "|                    IMPORTANT NOTICE                       |\n");
    fprintf(stderr, "| This copy of Mika is supplied for academic study purposes |\n");
    fprintf(stderr, "| and may not be distributed or incorporated into any       |\n");
    fprintf(stderr, "| commercial product or service.                            |\n");
    fprintf(stderr, "| For commercial licensing terms contact k-sales@kiffer.be |\n");
    fprintf(stderr, "\\-----------------------------------------------------------|\n");
    fprintf(stderr, "\n");
    next_warning += x_millis2ticks(300 * 1000);
  }
#endif

#ifdef UPTIME_LIMIT
  if (x_time_get() >= next_warning) {
    printf ("Time-limited demo version : Mika will terminate in %d seconds\n", time_remaining);
    next_warning += x_millis2ticks(60 * 1000);
    time_remaining -= 60;
  }
  if (x_time_get() >= stop_time) {
    printf ("Uptime limit reached : terminating execution\n");

    return WONKA_TRUE;
  }
#endif

  if (detect_deadlocks) {
    w_thread t;

    deadlock_fifo = ht_list_values(thread_hashtable);
    fifo_length = occupancyOfFifo(deadlock_fifo);
    woempa(7, "Allocating %d words each for chain, checked\n", fifo_length);
    chain = allocMem(fifo_length * sizeof(w_thread));
    checked = allocMem(fifo_length * sizeof(w_thread));
    checked_length = 0;
    while ((t = getFifo(deadlock_fifo))) {
      checkForDeadlock(t);
    }
    releaseMem(chain);
    releaseMem(checked);
    releaseFifo(deadlock_fifo);
    woempa(7, "Released chain, checked\n");
    chain = NULL;
    checked = NULL;
  }

  // Reset the dumping_info flag with a one-cycle delay (is hack).
  switch(dumping_info) {
    case 1: 
      dumping_info = -1; 
// HACK so we can set dumping_info to 1 to trigger a dump
#ifdef FREERTOS
      w_dump_info();
#endif
      break;
    case -1: 
      dumping_info = 0;
  }

  return wonka_killed;
}

w_void Heartbeat_setThread(w_thread thread, w_instance thisObject, w_instance hbthread) {
  heartbeat_thread = ((w_thread)getWotsitField(hbthread, F_Thread_wotsit))->kthread;
}

#ifdef FREERTOS

void Heartbeat_static_nativesleep(w_thread thread, w_instance classHeartbeat, w_long millis) {
  x_thread_sleep(x_millis2ticks(millis));
}

#else

#ifdef USE_NANOSLEEP
static struct timespec ts;
#endif
static struct timeval before;
static struct timeval now;

void Heartbeat_static_nativesleep(w_thread thread, w_instance classHeartbeat, w_long millis) {
  long micros = millis * 1000;
  w_long diff;

#ifdef USE_NANOSLEEP
  ts.tv_sec = 0;
  ts.tv_nsec = micros * 1000;
  nanosleep(&ts, NULL);
#else
  usleep(micros);
#endif

  if (!collecting) {
    return;
  }

  if (!inited) {
    gettimeofday(&now, NULL);
    inited = TRUE;
  }
  else {
    before = now;
    gettimeofday(&now, NULL);
    diff = ((w_long)now.tv_usec - (w_long)before.tv_usec) / 1000LL + ((w_long)now.tv_sec - (w_long)before.tv_sec) * 1000LL;
    if (diff < 0 || diff > 2 * millis) {
#ifdef HAVE_TIMEDWAIT
      system_time_offset += diff - millis;
#endif
    }
  }
}

#endif

void Heartbeat_static_collectTimeOffset(w_thread thread, w_instance classHeartbeat) {
  collecting = TRUE;
}

w_long Heartbeat_static_getTimeOffset(w_thread thread, w_instance classHeartbeat) {
   return system_time_offset;
}

