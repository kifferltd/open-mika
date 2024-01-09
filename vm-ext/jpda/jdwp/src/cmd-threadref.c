/**************************************************************************
* Copyright (c) 2004, 2006 by KIFFER Ltd. All rights reserved.            *
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

#include <string.h>

#include "core-classes.h"
#include "jdwp.h"
#include "jdwp-protocol.h"
#include "methods.h"
#include "oswald.h"
#include "wonka.h"
#include "wstrings.h"

extern w_thread jdwp_thread;
extern w_size jdwp_global_suspend_count;

extern void Thread_stop0(w_thread thread, w_instance thisThread, w_instance Throwable);
extern void Thread_interrupt(w_thread thread, w_instance thisThread);

#ifdef DEBUG
static const char* thread_reference_command_names[] = {
  /*  0 */ NULL,
  /*  1 */ "Name",
  /*  2 */ "Suspend",
  /*  3 */ "Resume",
  /*  4 */ "Status",
  /*  5 */ "ThreadGroup",
  /*  6 */ "Frames",
  /*  7 */ "FrameCount",
  /*  8 */ "OwnedMonitors",
  /*  9 */ "CurrentContendedMonitor",
  /* 10 */ "Stop",
  /* 11 */ "Interrupt",
  /* 12 */ "SuspendCount",
};

#define THREAD_REFERENCE_MAX_COMMAND 12

#endif

/*
** Check that the given objectref really is a Thread. If it is, return the
** correcponding w_thread, otherwise return NULL.
*/
w_thread jdwp_check_thread_reference(w_instance instance) {
  w_thread thread;

  if (!isProbablyAnInstance(instance)) {
    woempa(7, "%p is not an instance!\n", instance);

    return NULL;
  }

  if (isNotSet(instance2clazz(instance)->flags, CLAZZ_IS_THREAD)) {
    woempa(7, "%j is not a Thread!\n", instance);

    return NULL;
  }

  thread = getWotsitField(instance, F_Thread_wotsit);
  if (!thread || (thread == (w_thread)0xaaaaaaaa) || strncmp(thread->label, "thread", 6) != 0) {
    woempa(7, "%p is not a w_thread!\n", thread);

    return NULL;
  }

  return thread;
}

/*
** Returns the name of the thread with given ID.
*/

w_void jdwp_thr_name(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_thread   thread;
  w_instance instance;
  
  instance = jdwp_get_objectref(cmd->data, &offset);
  woempa(7, "%j\n", instance);
  if (instance) {
    thread = jdwp_check_thread_reference(instance);
    woempa(7, "%t\n", thread);
  
    if (thread) {
      jdwp_put_string(&reply_grobag, thread->name);

      jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
    }
    else {
      jdwp_send_invalid_thread(cmd->id);
    }
  } else {
    jdwp_send_invalid_object(cmd->id);
  }
}


/*
** Suspend a thread. Suspending and resuming threads are counted. This means that when 
** a thread is suspended x times, it should be resumed x times before it will actually 
** run again.
*/

void jdwp_thr_suspend(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_thread   thread;
  w_instance instance;
  
  instance = jdwp_get_objectref(cmd->data, &offset);
  woempa(7, "%j\n", instance);
  if (instance) {
    thread = jdwp_check_thread_reference(instance);
    woempa(7, "%t\n", thread);
  
    if (thread) {
      jdwp_internal_suspend_one(thread);
      jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
    }
    else {
      jdwp_send_invalid_thread(cmd->id);
    }
  }
  else {
    jdwp_send_invalid_object(cmd->id);
  }
}


/*
** Resume a suspended thread.
*/

void jdwp_thr_resume(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_thread   thread;
  w_instance instance;
  
  instance = jdwp_get_objectref(cmd->data, &offset);
  woempa(7, "%j\n", instance);
  if (instance) {
    thread = jdwp_check_thread_reference(instance);
    woempa(7, "%t\n", thread);
  
    if (thread) {
      jdwp_internal_resume_one(thread);
      jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
    }
    else {
      jdwp_send_invalid_thread(cmd->id);
    }
  }
  else {
    jdwp_send_invalid_object(cmd->id);
  }

#ifdef PARALLEL_GC
  x_monitor_eternal(safe_points_monitor);
  x_monitor_notify_all(safe_points_monitor);
  x_monitor_exit(safe_points_monitor);
#endif
}


/*
** Returns the status of the thread with given ID.
*/

w_void jdwp_thr_status(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_thread thread;
  w_instance instance;
  w_int      status;
  w_int      suspend = 0;
  
  instance = jdwp_get_objectref(cmd->data, &offset);
  woempa(7, "%j\n", instance);
  if (instance) {
    thread = jdwp_check_thread_reference(instance);
    woempa(7, "%t\n", thread);
  
    if (thread) {
      switch(thread->state) {
        case wt_ready:
          woempa(7, "thread->state = wt_ready, check with OSwald.\n");
          if ((thread->flags & WT_THREAD_SUSPEND_COUNT_MASK) | jdwp_global_suspend_count) {
            status = jdwp_ts_running;
            suspend = 1;
          }
          else switch(thread->kthread->state) {
            case xt_ready:
              woempa(7, "  OSwald state xt_ready -> jdwp_ts_running\n");
              status = jdwp_ts_running;
              break;
            case xt_mutex:
            case xt_rescheduled:
            case xt_queue:
            case xt_mailbox:
            case xt_semaphore:
            case xt_signals:
            case xt_monitor:
            case xt_block:
            case xt_map:
            case xt_joining:
              woempa(7, "  OSwald state %d -> jdwp_ts_monitor\n", thread->kthread->state);
              status = jdwp_ts_monitor;
              break;
            case xt_waiting:
              woempa(7, "  OSwald state xt_waiting -> jdwp_ts_wait\n");
              status = jdwp_ts_wait;
              break;
            case xt_suspended:
              woempa(7, "  OSwald state xt_suspended -> jdwp_ts_running, suspend = 1\n");
              status = jdwp_ts_running;
              suspend = 1;
              break;
            case xt_sleeping:
              woempa(7, "  OSwald state xt_sleeping -> jdwp_ts_sleeping\n");
             status = jdwp_ts_sleeping;
             break;
           case xt_newborn:
           case xt_dummy:
           case xt_ended:
           case xt_unknown:
           default:
              woempa(7, "  OSwald state %d -> jdwp_ts_zombie\n", thread->kthread->state);
             status = jdwp_ts_zombie;
             break;
          }
          break;
        case wt_sleeping:
          woempa(7, "thread->state = wt_sleeping -> jdwp_ts_sleeping\n");
          status = jdwp_ts_sleeping;
          break;
        case wt_waiting:
          woempa(7, "thread->state = wt_waiting -> jdwp_ts_wait\n");
          status = jdwp_ts_wait;
          break;
        default:
          woempa(7, "thread->state = %d -> jdwp_ts_zombie\n", thread->state);
          status = jdwp_ts_zombie;  
          break;
      }

      woempa(7, "status = %d suspended = %d\n", status, suspend);
      jdwp_put_u4(&reply_grobag, status);
      jdwp_put_u4(&reply_grobag, suspend);
    
      jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
    }
    else {
      jdwp_send_invalid_thread(cmd->id);
    }
  }
  else {
    jdwp_send_invalid_object(cmd->id);
  }
}


/*
** Returns the threadgroup the thread with given ID belongs to.
*/

w_void jdwp_thr_group(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_thread   thread;
  w_instance instance;
  
  instance = jdwp_get_objectref(cmd->data, &offset);
  woempa(7, "%j\n", instance);
  if (instance) {
    thread = jdwp_check_thread_reference(instance);
    woempa(7, "%t\n", thread);
  
    if (thread) {
      woempa(7, "  parent is %j\n", getReferenceField(instance, F_Thread_parent));
      jdwp_put_objectref(&reply_grobag, getReferenceField(instance, F_Thread_parent));
    
      jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
    }
    else {
      jdwp_send_invalid_thread(cmd->id);
    }
  }
  else {
    jdwp_send_invalid_object(cmd->id);
  }
}

#define JDWP_IGNORE_FRAME (FRAME_JNI | FRAME_LOADING | FRAME_CLINIT | FRAME_REFLECTION | FRAME_ROOT)

/*
**  Returns the call stack of a thread. The thread has to be suspended, and the frameID is only 
**  valid as long as the thread is suspended. The first frame is the current one, the next is 
**  its caller, etc...
*/

w_void jdwp_thr_frames(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_thread   thread;
  w_instance instance;
  w_int      start;
  w_int      length;
  w_frame    cursor;
  w_frame    top;
  w_int      count = 0;
  jdwp_Location location;
  w_frame    frame;
  w_method   method;
  w_long     pc;
  
  instance = jdwp_get_objectref(cmd->data, &offset);
  woempa(7, "%j\n", instance);
  if (instance) {
    thread = jdwp_check_thread_reference(instance);
    woempa(7, "%t\n", thread);
  
    if (thread) {
      start = jdwp_get_u4(cmd->data, &offset);
      length = jdwp_get_u4(cmd->data, &offset);
      woempa(7, "Request is for %d frames starting with frame %d\n", length, start);

      if(thread->kthread->state == xt_suspended || isSet(thread->flags, WT_THREAD_SUSPEND_COUNT_MASK) || jdwp_global_suspend_count) {
        cursor = thread->top;
        woempa(7, "  top frame is at %p\n", cursor);
      
        while(cursor  && cursor->method && count < start) {
          count += isNotSet(cursor->flags, JDWP_IGNORE_FRAME);
          cursor = cursor->previous;
        }
      
        top = cursor;
        woempa(7, "  skipped %d frames, now at %p\n", count, cursor);

        /* 
        ** Count the remaining frames.
        */
      
        count = 0;

        while(cursor  && cursor->method && (length == -1 ? 1 : (count < length))) {
          count += isNotSet(cursor->flags, JDWP_IGNORE_FRAME);
          cursor = cursor->previous;
        }

        woempa(7, "  will give back %d frames\n", count);
        jdwp_put_u4(&reply_grobag, count);

        cursor = top;
        count = 0;

        while(cursor  && cursor->method && (length == -1 ? 1 : (count < length))) {
          if (isNotSet(cursor->flags, JDWP_IGNORE_FRAME)) {
            frame = cursor;
      
            woempa(7, "    frame at %p (%m)\n", frame, frame->method);
            jdwp_put_frame(&reply_grobag, frame);
  
            method = cursor->method;
            pc = frame->current - method->exec.code;

            location.method = method;
            location.pc = pc;

            jdwp_put_location(&reply_grobag, &location);
      
            count++;
          }
          cursor = cursor->previous;
        }

        jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
      }
      else {
        jdwp_send_thread_not_suspended(cmd->id);
      }
    }
    else {
      jdwp_send_invalid_thread(cmd->id);
    }
  }
  else {
    jdwp_send_invalid_object(cmd->id);
  }
}


/*
** Count the number of frames on this thread's stack, but only if it's suspended.
** The returned number of frames is valid as long as the thread is suspended.
*/

w_void jdwp_thr_countframes(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_thread   thread;
  w_instance instance;
  w_frame    cursor;
  w_int      count = 0;
  
  instance = jdwp_get_objectref(cmd->data, &offset);
  woempa(7, "%j\n", instance);
  if (instance) {
    thread = jdwp_check_thread_reference(instance);
    woempa(7, "%t\n", thread);
  
    if (thread) {
      if(thread->kthread->state == xt_suspended || isSet(thread->flags, WT_THREAD_SUSPEND_COUNT_MASK) || jdwp_global_suspend_count) {
        cursor = thread->top;
        while(cursor) {
          count += isNotSet(cursor->flags, JDWP_IGNORE_FRAME);;
          cursor = cursor->previous;
        }
        jdwp_put_u4(&reply_grobag, count);
        jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
      }
      else {
        jdwp_send_thread_not_suspended(cmd->id);
      }
    }
    else {
      jdwp_send_invalid_thread(cmd->id);
    }
  }
  else {
    jdwp_send_invalid_object(cmd->id);
  }
}


/*
** Return the number of times a given thread has been suspended. In other words:
** The number of times it should be resumed before it starts running again.
*/

w_void jdwp_thr_suspend_count(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_thread   thread;
  w_instance instance;
  w_int      count;
  
  instance = jdwp_get_objectref(cmd->data, &offset);
  woempa(7, "%j\n", instance);
  if (instance) {
    thread = jdwp_check_thread_reference(instance);
    woempa(7, "%t\n", thread);
  
    if (thread) {
      count = (thread->flags & WT_THREAD_SUSPEND_COUNT_MASK) >> WT_THREAD_SUSPEND_COUNT_SHIFT;
      jdwp_put_u4(&reply_grobag, (w_word)count);
      jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
    }
    else {
      jdwp_send_invalid_thread(cmd->id);
    }
  }
  else {
    jdwp_send_invalid_object(cmd->id);
  }
  
  return;
}


/*
** Stop the given thread as if by java.lang.Thread.stop().
*/

w_void jdwp_thr_stop(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_thread   thread;
  w_instance instance;
  w_instance throwable;
  
  instance = jdwp_get_objectref(cmd->data, &offset);
  woempa(7, "%j\n", instance);
  if (instance) {
    thread = jdwp_check_thread_reference(instance);
    woempa(7, "%t\n", thread);
  
    if (thread) {
      throwable = jdwp_get_objectref(cmd->data, &offset);
      if (instance) {
        Thread_stop0(jdwp_thread, instance, throwable);
        jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
      }
      else {
        jdwp_send_invalid_object(cmd->id);
      }
    }
    else {
      jdwp_send_invalid_thread(cmd->id);
    }
  }
  else {
    jdwp_send_invalid_object(cmd->id);
  }
  
  return;
}

/*
** Interrupt the given thread as if by java.lang.Thread.interrupt().
*/

w_void jdwp_thr_interrupt(jdwp_command_packet cmd) {
  w_size offset = 0;
  w_thread   thread;
  w_instance instance;
  
  instance = jdwp_get_objectref(cmd->data, &offset);
  woempa(7, "%j\n", instance);
  if (instance) {
    thread = jdwp_check_thread_reference(instance);
    woempa(7, "%t\n", thread);
  
    if (thread) {
      Thread_interrupt(jdwp_thread, instance);
      jdwp_send_reply(cmd->id, &reply_grobag, jdwp_err_none);
    }
    else {
      jdwp_send_invalid_thread(cmd->id);
    }
  }
  else {
    jdwp_send_invalid_object(cmd->id);
  }
  
  return;
}


/*
** The dispatcher for the 'Thread reference' command set.
*/

w_void dispatch_threadref(jdwp_command_packet cmd) {

  woempa(7, "Thread Reference Command = %s\n", cmd->command > 0 && cmd->command <= THREAD_REFERENCE_MAX_COMMAND ? thread_reference_command_names[cmd->command] : "unknown");
  switch((jdwp_threadref_cmd)cmd->command) {
    case jdwp_threadref_name:
      jdwp_thr_name(cmd);
      break;
    case jdwp_threadref_suspend:
      jdwp_thr_suspend(cmd);
      break;
    case jdwp_threadref_resume:
      jdwp_thr_resume(cmd);
      break;
    case jdwp_threadref_status:
      jdwp_thr_status(cmd);
      break;
    case jdwp_threadref_threadGroup:
      jdwp_thr_group(cmd);
      break;
    case jdwp_threadref_frames:
      jdwp_thr_frames(cmd);
      break;
    case jdwp_threadref_frameCount:
      jdwp_thr_countframes(cmd);
      break;
    case jdwp_threadref_suspendCount:
      jdwp_thr_suspend_count(cmd);
      break; 
    case jdwp_threadref_ownedMonitors:
// TODO: we could implement this if the dispatchers for synchronized methods
// would push the monitor onto the aux stack (but how to map back to object?)
    case jdwp_threadref_currentContendedMonitor:
// TODO: could probably hack this one too
      jdwp_send_not_implemented(cmd->id);
      break;
    case jdwp_threadref_stop:
      jdwp_thr_stop(cmd);
      break; 
    case jdwp_threadref_interrupt:
      jdwp_thr_interrupt(cmd);
      break; 
    default:
      jdwp_send_not_implemented(cmd->id);
  }
  
}

