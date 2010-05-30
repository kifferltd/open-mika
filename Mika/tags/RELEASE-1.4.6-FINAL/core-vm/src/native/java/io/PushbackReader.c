/**************************************************************************
* Copyright (c) 2010 by Chris Gray, /k/ Embedded Java Solutions.          *
* All rights reserved.                                   *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of /k/ Embedded Java Solutions nor the names of     *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL /K/ EMBEDDED JAVA SOLUTIONS OR OTHER CONTRIBUTORS BE  *
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR     *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,       *
* EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                      *
**************************************************************************/

#include "core-classes.h"
#include "exception.h"
#include "fields.h"
#include "heap.h"
#include "interpreter.h"
#include "jni.h"
#include "loading.h"
#include "locks.h"
#include "methods.h"
#include "threads.h"

static jclass   class_Reader;
static jmethodID read_method;

w_int PushbackReader_read(JNIEnv *env, w_instance thisPushbackReader) {
  w_thread thread = JNIEnv2w_thread(env);
  x_monitor m;
  w_instance lock = getReferenceField(thisPushbackReader, F_Reader_lock);
  w_instance chars;
  w_instance in;
  w_frame new_frame;
  w_int result;
  w_word *posptr;

  m = getMonitor(lock);
  x_monitor_eternal(m);
  chars = getReferenceField(thisPushbackReader, F_PushbackReader_chars);
  if (!chars) {
    x_monitor_exit(m);
    throwException(thread, clazzIOException, "PushBackReader is closed");

    return -1;
  }

  posptr = wordFieldPointer(thisPushbackReader, F_PushbackReader_pos);
  if (*posptr < instance2Array_length(chars)){
    result = instance2Array_char(chars)[*posptr];
    ++(*posptr);
  }
  else {
    if (!read_method) {
      mustBeInitialized(clazzReader);
      class_Reader = clazz2Class(clazzReader);
      read_method = (*env)->GetMethodID(env, class_Reader, "read", "()I"); 
      woempa(7,"read_method is %M\n",read_method);
    }
    in = getReferenceField(thisPushbackReader, F_PushbackReader_in);
    new_frame = activateFrame(thread, virtualLookup(read_method, instance2clazz(in)), 0, 1, in, stack_trace);
    result = (w_int) new_frame->jstack_top[-1].c;
    deactivateFrame(new_frame, NULL);
    removeLocalReference(thread, in);
  }
  x_monitor_exit(m);
  return result;
}

void PushbackReader_unread(JNIEnv *env, w_instance thisPushbackReader, w_int c) {
  w_thread thread = JNIEnv2w_thread(env);
  x_monitor m;
  w_instance lock = getReferenceField(thisPushbackReader, F_Reader_lock);
  w_instance chars;
  w_word *posptr;
  w_instance in;
  w_frame new_frame;

  m = getMonitor(lock);
  x_monitor_eternal(m);
  chars = getReferenceField(thisPushbackReader, F_PushbackReader_chars);
  posptr = wordFieldPointer(thisPushbackReader, F_PushbackReader_pos);
  if (!chars) {
    x_monitor_exit(m);
    throwException(thread, clazzIOException, "PushBackReader is closed");

    return;
  }

  if (!*posptr) {
    x_monitor_exit(m);
    throwException(thread, clazzIOException, "Pushback buffer is full");

    return;
  }

  --(*posptr);
  instance2Array_char(chars)[*posptr] = (w_char)c;
  x_monitor_exit(m);
}

