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

#include "clazz.h"
#include "core-classes.h"
#include "heap.h"
#include "exception.h"
#include "wonka.h"
#include "wonkatime.h"

w_boolean ReferenceQueue_append(w_thread thread, w_instance this, w_instance reference) {
  if(reference) {
    volatile w_word *flagsptr = instance2flagsptr(reference);
    if((isSet(*flagsptr, O_ENQUEUEABLE))) {
      w_fifo fifo = getWotsitField(this, F_ReferenceQueue_fifo);
      x_monitor lock = getWotsitField(this, F_ReferenceQueue_lock);

      if(!lock || !fifo) {
        return WONKA_FALSE;
      }

      x_monitor_eternal(lock);
      if(putFifo(reference, fifo) == 0) {
        setFlag(*flagsptr, O_BLACK);
        unsetFlag(*flagsptr, O_ENQUEUEABLE);
        setBooleanField(reference, F_Reference_queued, WONKA_TRUE);
        x_monitor_notify(lock);
      }
      x_monitor_exit(lock);
      return WONKA_TRUE;
    }
  }
  return WONKA_FALSE;
}

w_instance ReferenceQueue_poll(w_thread thread, w_instance this) {
  w_fifo fifo = getWotsitField(this, F_ReferenceQueue_fifo);
  x_monitor lock = getWotsitField(this, F_ReferenceQueue_lock);
  w_instance ref;
  w_boolean unsafe;
  if(!lock || !fifo) {
   return NULL;
  }

  unsafe = enterUnsafeRegion(thread);
  x_monitor_eternal(lock);
  ref = (w_instance) getFifo(fifo);
  if(ref) {
    setBooleanField(ref, F_Reference_queued, WONKA_FALSE);
    addLocalReference(thread,ref);
  }
  x_monitor_exit(lock);

  if(!unsafe) {
    enterSafeRegion(thread);
  }
  return ref;
}

w_instance ReferenceQueue_remove(w_thread thread, w_instance this) {
  w_fifo fifo = getWotsitField(this, F_ReferenceQueue_fifo);
  x_monitor lock = getWotsitField(this, F_ReferenceQueue_lock);
  w_instance ref = NULL;
  w_boolean interrupted = FALSE;

  if(!lock || !fifo) {
   return NULL;
  }

  if (thread->flags & WT_THREAD_INTERRUPTED) {
    throwException(thread, clazzInterruptedException, NULL);
    thread->flags &= ~WT_THREAD_INTERRUPTED;
    return NULL;
  }

  while(!interrupted) {
    ref = ReferenceQueue_poll(thread, this);
    if (ref) {
      return ref;
    }

   x_monitor_eternal(lock);
    if(isEmptyFifo(fifo)) {
      x_status status = x_monitor_wait(lock, x_eternal);
      if(status == xs_interrupted) {
        throwException(thread, clazzInterruptedException, NULL);
        thread->flags &= ~WT_THREAD_INTERRUPTED;
        interrupted = TRUE;
      }
    }
    x_monitor_exit(lock);
  }

  return NULL;
}

w_instance ReferenceQueue_removeJ(w_thread thread, w_instance this, w_long waittime) {
  w_fifo fifo = getWotsitField(this, F_ReferenceQueue_fifo);
  x_monitor lock = getWotsitField(this, F_ReferenceQueue_lock);
  w_instance ref =NULL;
  w_boolean interrupted = FALSE;

  if(!lock || !fifo) {
   return NULL;
  }

  if (thread->flags & WT_THREAD_INTERRUPTED) {
    throwException(thread, clazzInterruptedException, NULL);
    thread->flags &= ~WT_THREAD_INTERRUPTED;
    return NULL;
  }

  while(!interrupted) {
    ref = ReferenceQueue_poll(thread, this);
    if(ref) {
      return ref;
    } else {
      w_long now = x_time_now_millis();
      w_long diff;

      x_monitor_eternal(lock);
      if(isEmptyFifo(fifo)) {
       x_status status; 
       if(waittime <= 0) {
          x_monitor_exit(lock);
          return NULL;
        }

        status = x_monitor_wait(lock, waittime != 0 ? x_millis2ticks((w_size)waittime) : x_eternal);
        if(status == xs_interrupted) {
          throwException(thread, clazzInterruptedException, NULL);
          thread->flags &= ~WT_THREAD_INTERRUPTED;
          interrupted = TRUE;
        }
      }
      x_monitor_exit(lock);
      diff = x_time_now_millis() - now;
      waittime -= diff;
    }
  }

  return NULL;
}

void ReferenceQueue_create(w_thread thread, w_instance this) {
  w_fifo fifo = allocFifo(62);
  x_monitor lock =  allocMem(sizeof(x_Monitor));

  if(fifo && lock) {
     x_monitor_create(lock);
     setWotsitField(this, F_ReferenceQueue_lock, lock);
     setWotsitField(this, F_ReferenceQueue_fifo, fifo);
  } else {
    if(fifo) {
      releaseFifo(fifo);
    }
    if(lock) {
      releaseMem(lock);
    }
  }
}

void ReferenceQueue_destructor(w_instance queue) {
  w_fifo fifo = getWotsitField(queue, F_ReferenceQueue_fifo);
  x_monitor lock = getWotsitField(queue, F_ReferenceQueue_lock);

  if(fifo) {
    clearWotsitField(queue, F_ReferenceQueue_fifo);
    releaseFifo(fifo);
  }
  if(lock) {
    clearWotsitField(queue, F_ReferenceQueue_lock);
    x_monitor_delete(lock);
    releaseMem(lock);
  }
}
