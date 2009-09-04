/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2004, 2005, 2006 by Chris Gray, /k/ Embedded Java   *
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

#include <string.h>
#include "arrays.h"
#include "chars.h"
#include "clazz.h"
#include "core-classes.h"
#include "descriptor.h"
#include "exception.h"
#include "fields.h"
#include "heap.h"
#include "loading.h"
#include "methods.h"
#include "fastcall.h"
#include "ts-mem.h"
#include "wstrings.h"
#include "threads.h"
#include "Math.h"

static void copyChars(w_char * dst, w_char * src, w_size num) {

  w_size duffs;

  if (num) {
    duffs = (num + 31) / 32;
    switch (num & 0x1f) {
      case  0: do { *dst++ = *src++;
      case 31:      *dst++ = *src++;
      case 30:      *dst++ = *src++;
      case 29:      *dst++ = *src++;
      case 28:      *dst++ = *src++;
      case 27:      *dst++ = *src++;
      case 26:      *dst++ = *src++;
      case 25:      *dst++ = *src++;
      case 24:      *dst++ = *src++;
      case 23:      *dst++ = *src++;
      case 22:      *dst++ = *src++;
      case 21:      *dst++ = *src++;
      case 20:      *dst++ = *src++;
      case 19:      *dst++ = *src++;
      case 18:      *dst++ = *src++;
      case 17:      *dst++ = *src++;
      case 16:      *dst++ = *src++;
      case 15:      *dst++ = *src++;
      case 14:      *dst++ = *src++;
      case 13:      *dst++ = *src++;
      case 12:      *dst++ = *src++;
      case 11:      *dst++ = *src++;
      case 10:      *dst++ = *src++;
      case  9:      *dst++ = *src++;
      case  8:      *dst++ = *src++;
      case  7:      *dst++ = *src++;
      case  6:      *dst++ = *src++;
      case  5:      *dst++ = *src++;
      case  4:      *dst++ = *src++;
      case  3:      *dst++ = *src++;
      case  2:      *dst++ = *src++;
      case  1:      *dst++ = *src++;
              } while (--duffs > 0);
    }
  }

}

static void i_ensureCapacity(w_thread thread, w_instance StringBuffer, w_int minimum) {

  w_instance oldbuffer = getReferenceField(StringBuffer, F_StringBuffer_value);
  w_int oldsize = instance2Array_length(oldbuffer);
  w_instance newbuffer;

  threadMustBeSafe(thread);
  woempa(1, "%j: minimum capacity is %d, current is %d\n", StringBuffer, minimum, oldsize);

  if (minimum > oldsize) {
    woempa(1,"Current buffer has length %d, new buffer will have length %d\n", oldsize, (minimum > 2 * oldsize) ? minimum : 2 * oldsize + 2);
    enterUnsafeRegion(thread);
    newbuffer = allocArrayInstance_1d(thread, atype2clazz[P_char], (minimum > 2 * oldsize) ? minimum : 2 * oldsize + 2);
    if (newbuffer) {
      copyChars(instance2Array_char(newbuffer), instance2Array_char(oldbuffer), getIntegerField(StringBuffer, F_StringBuffer_count));
      setReferenceField_unsafe(StringBuffer, newbuffer, F_StringBuffer_value);
      removeLocalReference(thread, newbuffer);
    }
    enterSafeRegion(thread);
  }
}

void StringBuffer_ensureCapacity(JNIEnv *env, w_instance StringBuffer, w_int minimum) {
  i_ensureCapacity(JNIEnv2w_thread(env), StringBuffer, minimum);
}

void StringBuffer_createFromString(JNIEnv *env, w_instance StringBuffer, w_instance String) {
  w_thread thread = JNIEnv2w_thread(env);
  w_string string;
  w_int length;
  w_instance buffer;
  w_char  *charptr;

  if (String) {  
    string = String2string(String);
    length = string_length(string) + 16;
    woempa(1, "Allocating array of char[%d]\n", length);
    enterUnsafeRegion(thread);
    buffer = allocArrayInstance_1d(JNIEnv2w_thread(env), atype2clazz[P_char], length);
    enterSafeRegion(thread);
    if (!buffer) {

      return;

    }
    charptr = instance2Array_char(buffer);
    length -= 16;

    if (string_is_latin1(string)) {
      w_int i;
      for (i = 0; i < length; ++i) {
        *charptr++ =  (w_char)string->contents.bytes[i];
      }
    }
    else {
      copyChars(charptr, string->contents.chars, length);
    }
    setIntegerField(StringBuffer, F_StringBuffer_count, length);
  }
  else {
    string = NULL;
    length = 16;
    woempa(1, "Allocating array of char[%d]\n", length);
    enterUnsafeRegion(thread);
    buffer = allocArrayInstance_1d(JNIEnv2w_thread(env), atype2clazz[P_char], length);
    enterSafeRegion(thread);
    if (!buffer) {

      return;

    }
    charptr = instance2Array_char(buffer);
    length = 0;
  }

  setReferenceField(StringBuffer, buffer, F_StringBuffer_value);
  
}

static w_instance i_StringBuffer_append_String_null(w_thread thread, w_instance thisStringBuffer) {

  w_char *dst;
  w_int count;
  
  woempa(1, "Appending null string to StringBuffer.\n");

  if (exceptionThrown(thread) == NULL) {
    count = getIntegerField(thisStringBuffer, F_StringBuffer_count);
    dst = instance2Array_char(getReferenceField(thisStringBuffer, F_StringBuffer_value)) + count;
  
    *dst++ = 'n';
    *dst++ = 'u';
    *dst++ = 'l';
    *dst++ = 'l';

    setIntegerField(thisStringBuffer, F_StringBuffer_count, count + 4);
  }

  return thisStringBuffer;
   
}

static w_instance i_StringBuffer_append_String(w_thread thread, w_instance thisStringBuffer, w_instance theString) {

  w_char *dst;
  w_int count;
  w_string string = String2string(theString);
  w_int    src_length = string_length(string);
  w_int    i;
 
  woempa(1, "Appending string '%w' to %j.\n", string, thisStringBuffer);

  if (exceptionThrown(thread) == NULL) {
    count = getIntegerField(thisStringBuffer, F_StringBuffer_count);
    dst = instance2Array_char(getReferenceField(thisStringBuffer, F_StringBuffer_value)) + count;
    if (string_is_latin1(string)) {
      w_ubyte *src = string->contents.bytes;
      for (i = 0; i < src_length; ++i) {
        *dst++ = *src++;
       }
     }
    else {
      w_memcpy(dst, string->contents.chars, sizeof(w_char) * src_length);
    }
    setIntegerField(thisStringBuffer, F_StringBuffer_count, count + src_length);
  }

  return thisStringBuffer;
   
}

w_instance StringBuffer_append_String(JNIEnv *env, w_instance thisStringBuffer, w_instance theString) {
  w_thread thread = JNIEnv2w_thread(env);
  w_int curr_length = getIntegerField(thisStringBuffer, F_StringBuffer_count);

  if (theString) {
    w_string string = String2string(theString);
    w_int src_length = string_length(string);

    i_ensureCapacity(thread, thisStringBuffer, src_length + curr_length);
    return i_StringBuffer_append_String(thread, thisStringBuffer, theString);
  }
  else {
    i_ensureCapacity(thread, thisStringBuffer, 4 + curr_length);
    return i_StringBuffer_append_String_null(thread, thisStringBuffer);
  }
}

void fast_StringBuffer_append_String(w_frame frame) {
  w_instance objectref = (w_instance) frame->jstack_top[-2].c;
  w_thread thread = frame->thread;
  w_int curr_length;

  enterSafeRegion(thread);
  if (!objectref) {
    throwException(thread, clazzNullPointerException, NULL);
    enterUnsafeRegion(thread);
  }
  else {
    x_monitor m;

    m = getMonitor(objectref);
    x_monitor_eternal(m);
    curr_length  = getIntegerField(objectref, F_StringBuffer_count);
 
    if (frame->jstack_top[-1].c) {
      w_string string = String2string((w_instance)frame->jstack_top[-1].c);
      w_int    src_length = string_length(string);

      i_ensureCapacity(thread, objectref, src_length + curr_length);
      (void)i_StringBuffer_append_String(thread, objectref, (w_instance)frame->jstack_top[-1].c);
    }
    else {
      i_ensureCapacity(thread, objectref, 4 + curr_length);
      (void)i_StringBuffer_append_String_null(thread, objectref);
    }
    x_monitor_exit(m);
    enterUnsafeRegion(thread);
    frame->jstack_top -= 1;
  }
}

w_instance StringBuffer_substring(JNIEnv *env, w_instance StringBuffer, w_int start, w_int end) {

  w_thread thread = JNIEnv2w_thread(env);
  w_int length = getIntegerField(StringBuffer, F_StringBuffer_count);
  w_string string;
  w_instance result = NULL;
  w_char *src;
  
  if (start < 0 || end < 0 || start > end || start > length || end > length) {
    // printf("start %d end %d length %d\n", start, end, length);
    throwException(thread, clazzStringIndexOutOfBoundsException, NULL);
  }
  else {
    src = instance2Array_char(getReferenceField(StringBuffer, F_StringBuffer_value));
    src += start;
    string = unicode2String(src, (w_size)(end - start));
    if (!string) {
      wabort(ABORT_WONKA, "Unable to create string\n");
    }
    result = getStringInstance(string);
    // string is now registered twice, when once would be enough
    deregisterString(string);
  }

  return result;

}

static w_instance i_StringBuffer_toString(w_instance thisStringBuffer) {

  w_string string;
  w_instance String;
  
  string = unicode2String(instance2Array_char(getReferenceField(thisStringBuffer, F_StringBuffer_value)), (w_size)getIntegerField(thisStringBuffer, F_StringBuffer_count));
  if (!string) {
    wabort(ABORT_WONKA, "Unable to create string\n");
  }
  String = getStringInstance(string);
  // string is now registered twice, when once would be enough
  deregisterString(string);

  return String;

}

w_instance StringBuffer_toString(JNIEnv *env, w_instance thisStringBuffer) {
  return i_StringBuffer_toString(thisStringBuffer);
}

void fast_StringBuffer_toString(w_frame frame) {
  w_instance objectref = (w_instance) frame->jstack_top[-1].c;
  w_instance theString;
  w_thread   thread = frame->thread;

  enterSafeRegion(thread);
  if (objectref) {
    theString = i_StringBuffer_toString(objectref);
    enterUnsafeRegion(thread);
    frame->jstack_top[-1].c = (w_word)theString;
    if (!exceptionThrown(thread)) {
      setFlag(instance2flags(theString), O_BLACK);
      removeLocalReference(thread, theString);
    }
  }
  else {
    throwException(thread, clazzNullPointerException, NULL);
    enterUnsafeRegion(thread);
  }
}

