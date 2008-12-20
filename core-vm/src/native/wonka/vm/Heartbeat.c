/**************************************************************************
* Parts copyright (c) 2002, 2003 by Punch Telematix. All rights reserved. *
* Parts copyright (c) 2005, 2008 by Chris Gray, /k/ Embedded Java         *
* Solutions. All rights reserved.                                         *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/
 
#ifdef USE_NANOSLEEP
#include <time.h>
#else
#include <sys/time.h>
#endif

#include "clazz.h"
#include "core-classes.h"
#include "threads.h"

#ifdef UPTIME_LIMIT
#include <stdio.h>

static x_time stop_time;
static w_int  time_remaining;
static x_time next_warning;
#endif

static w_boolean detect_deadlocks;

#ifdef ACADEMIC_LICENCE
#include <stdio.h>
#endif

extern int wonka_killed;
extern x_thread heartbeat_thread;

void Heartbeat_create(JNIEnv *env, w_instance theHeartbeat, w_boolean detectDeadlocks) {
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

w_int Heartbeat_numberNonDaemonThreads(JNIEnv *env, w_instance theClass) {
  
  return nondaemon_thread_count;
}

void clearCheckedFlag(w_word key, w_word value) {
  w_thread t = (w_thread) value;
  unsetFlag(t->flags, WT_THREAD_CHECKED);
}

void printDeadlock(w_thread t0, w_thread t2) {
  w_thread t1 = t0;

  wprintf("- - - DEADLOCK DETECTED - - -\n");
  while (t1 != t2) {
    wprintf("  %t is competing for monitor %p\n", t1, t1->kthread->waiting_on);
    t1 = t1->kthread->waiting_on->owner->xref;
    wprintf("    which is owned by %t\n", t2);
  }
  wprintf("- - - - - - - - - - - - - - -\n");
}

void checkForDeadlocks(w_word key, w_word value) {
  w_thread t0 = (w_thread) value;
  w_thread t1 = t0;
  w_int i;

  if (isNotSet(t0->flags, WT_THREAD_CHECKED)) {
    woempa(7, "checking %t for deadlocks\n", value);
    setFlag(t0->flags, WT_THREAD_CHECKED);
    // upper bound is there just to prevent infinite looping
    for (i = 0; i < thread_hashtable->occupancy; ++i) {
      x_thread xt = t1->kthread;
      if (isSet(xt->flags, TF_COMPETING)) {
        if (xt->waiting_on) {
            woempa(7, "  %t is competing for monitor %p\n", t1, xt->waiting_on);
          if (xt->waiting_on->owner) {
            w_thread t2 = xt->waiting_on->owner->xref;
            woempa(7, "    which is owned by %t\n", t2);
            if (t2 == t0) {
              printDeadlock(t0, t2);
              wabort(ABORT_WONKA, "Deadlock detected involving threads %t, %t", t1, t2);
            }
            else if (isSet(t2->flags, WT_THREAD_CHECKED)) {
              woempa(7, "    which was already checked, so that's all right then\n");
              break;
            }
            else {
              t1 = t2;
            }
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
        setFlag(t1->flags, WT_THREAD_CHECKED);
        break;
      }
    }
  }
  else {
    woempa(7, "not checking %t for deadlocks, is already checked\n", value);
  }
}


extern int dumping_info;

w_boolean Heartbeat_isKilled(JNIEnv *env, w_instance theClass) {
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
    ht_every(thread_hashtable, clearCheckedFlag);
    ht_every(thread_hashtable, checkForDeadlocks);
  }

  // Reset the dumping_info flag with a one-cycle delay (is hack).
  switch(dumping_info) {
    case 1: 
      dumping_info = -1; 
      break;
    case -1: 
      dumping_info = 0;
  }

  return wonka_killed;
}

w_void Heartbeat_setThread(JNIEnv *env, w_instance thisObject, w_instance thread) {
  heartbeat_thread = ((w_thread)getWotsitField(thread, F_Thread_wotsit))->kthread;
}

#ifdef USE_NANOSLEEP
static struct timespec ts;
#endif
static struct timeval before;
static struct timeval now;
static w_boolean inited;

w_long system_time_offset;

void Heartbeat_static_nativesleep(JNIEnv *env, w_instance classHeartbeat, w_long millis) {
  w_thread thread = JNIEnv2w_thread(env);
  long micros = millis * 1000;
  w_long diff;
  w_fifo fifo;

#ifdef USE_NANOSLEEP
  if (!inited) {
    ts.tv_sec = 0;
    ts.tv_nsec = micros * 1000;
  }
  nanosleep(&ts, NULL);
#else
  usleep(micros);
#endif

  if (!inited) {
    gettimeofday(&now, NULL);
    inited = TRUE;
  }
  else {
    before = now;
    gettimeofday(&now, NULL);
    diff = (now.tv_usec - before.tv_usec) / 1000 + (now.tv_sec - before.tv_sec) * 1000;
    if (diff < 0 || diff > 2 * millis) {
      system_time_offset += diff - millis;
      x_adjust_timers(diff);
    }
  }
}

w_long Heartbeat_static_getTimeOffset(JNIEnv *env, w_instance classHeartbeat) {
   return system_time_offset;
}

