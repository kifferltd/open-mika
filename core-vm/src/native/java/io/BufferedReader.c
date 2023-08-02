/**************************************************************************
* Copyright (c) 2021, 2023 by KIFFER Ltd. All rights reserved.            *
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

#include <stdio.h>

#include "core-classes.h"
#include "arrays.h"
#include "exception.h"
#include "fields.h"
#include "interpreter.h"
#include "heap.h"
#include "jni.h"
#include "locks.h"
#include "methods.h"
#include "mika_threads.h"

static w_method updateBuffer_method;

w_int BufferedReader_locateEnd(w_thread thread, w_instance this, w_instance Array, w_int start, w_int stop) {

  if(Array){
    w_int length = instance2Array_length(Array);
    w_char* chars  = instance2Array_char(Array);
    if(length >= stop && start >= 0){
      while(start < stop){
        if(chars[start] == 10 || chars[start] == 13){
          woempa(1, "found end-of-line character %02x at position[%d]\n", chars[start], start);
          return start;
        }
        start++;
      }
    }
  }

  woempa(1, "found no end-of-line character\n");

  return -1;
}

w_int BufferedReader_read(w_thread thread, w_instance thisBufferedReader) {
  w_int result = -1;
  x_monitor m;
  w_instance lock = getReferenceField(thisBufferedReader, F_Reader_lock);
  w_instance buf;
  w_frame new_frame;

  m = getMonitor(lock);
  x_monitor_eternal(m);
  buf = getReferenceField(thisBufferedReader, F_BufferedReader_buf);

  if (!buf) {
    x_monitor_exit(m);
    throwException(thread, clazzIOException, "BufferedReader is closed");

    return -1;
  }

  w_word *posptr = wordFieldPointer(thisBufferedReader, F_BufferedReader_pos);
  w_int count = getIntegerField(thisBufferedReader, F_BufferedReader_count);
  w_boolean updated;

  if (count <= (w_int)*posptr) {
    if (!updateBuffer_method) {
      updateBuffer_method = find_method(clazzBufferedReader, "updateBuffer", "()Z");
      woempa(7,"updateBuffer_method is %M\n",updateBuffer_method);
    }
    new_frame = activateFrame(thread, updateBuffer_method, 0, 1, thisBufferedReader, stack_trace);
    updated = (w_boolean) GET_SLOT_CONTENTS(new_frame->jstack_top - 1);
    deactivateFrame(new_frame, NULL);
    removeLocalReference(thread, thisBufferedReader);
    if (!updated) {
      x_monitor_exit(m);

      return -1;
    }
  }
  // The buf array may very well have changed as a result of updateBuffer!
  buf = getReferenceField(thisBufferedReader, F_BufferedReader_buf);
  result = instance2Array_char(buf)[*posptr];
  ++(*posptr);
  x_monitor_exit(m);

  return result;
}

