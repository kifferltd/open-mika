/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2004, 2005, 2006, 2009, 2010 by Chris Gray,         *
* /k/ Embedded Java Solutions. All rights reserved.                       *
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
#include "checks.h"
#include "core-classes.h"
#include "clazz.h"
#include "exception.h"
#include "fastcall.h"
#include "loading.h"
#include "Math.h"
#include "descriptor.h"
#include "fields.h"
#include "heap.h"
#include "ts-mem.h"
#include "methods.h"
#include "wmath.h"
#include "wstrings.h"
#include "threads.h"
#include "chars.h"

/*
** Create an empty string

void String_create_empty(JNIEnv *env, w_instance String) {
  w_string s = registerString(string_empty);
  setWotsitField(String, F_String_wotsit, s);
  woempa(1, "%p new string at %p, empty\n", String, (char *)(String2string(String)));
}
*/

void fast_String_create_empty(w_frame frame) {
  w_instance objectref = (w_instance) frame->jstack_top[-1].c;
  w_string s;

  enterSafeRegion(frame->thread);
  s = registerString(string_empty);
  setWotsitField(objectref, F_String_wotsit, s);
  enterUnsafeRegion(frame->thread);
  frame->jstack_top -= 1;
  woempa(1, "%p new string at %p, empty\n", objectref, String2string(objectref));
}

/*
** Create a string instance just like an existing string
*/

void String_create_String(JNIEnv *env, w_instance String, w_instance Value) {
  if (! Value) {
    throwException(JNIEnv2w_thread(env), clazzNullPointerException, NULL);
  } 
  else {
    w_string s = registerString(String2string(Value));

    setWotsitField(String, F_String_wotsit, s);
  }

}

/*
** Create a string instance from a StringBuffer
*/

void String_create_StringBuffer(JNIEnv *env, w_instance String, w_instance value) {

  w_thread thread = JNIEnv2w_thread(env);
  w_string string;

  if (!value) {
    throwException(thread, clazzNullPointerException, NULL);
  } 
  else {
    string = unicode2String(instance2Array_char(getReferenceField(value, F_StringBuffer_value)), getIntegerField(value, F_StringBuffer_count));
    setWotsitField(String, F_String_wotsit, string);
  }
}

/*
** Create a string instance from an array of Characters
*/

void i_String_create_char(w_thread thread, w_instance String, w_instance charArray, w_int offset, w_int count) {

  w_int length;
  w_string string;
  
  if (!charArray) {
    throwException(thread, clazzNullPointerException, NULL);
  } 
  else {
    length = instance2Array_length(charArray);
    woempa(1, "Array %j length %d offset %d count %d\n", charArray, length, offset, count);
    if (offset < 0 || count < 0 || offset > length - count) {
      throwException(thread, clazzArrayIndexOutOfBoundsException, NULL);
    } 
    else if (count == 0) {
      string = registerString(string_empty);
      setWotsitField(String, F_String_wotsit, string);
    }
    else {
      string = unicode2String(instance2Array_char(charArray) + offset, (w_size)count);
      setWotsitField(String, F_String_wotsit, string);
    }
  }
}

void String_create_char(JNIEnv *env, w_instance thisString, w_instance charArray, w_int offset, w_int count) {
  i_String_create_char(JNIEnv2w_thread(env), thisString, charArray, offset, count);
}

void fast_String_create_char(w_frame frame) {
  enterSafeRegion(frame->thread);
  i_String_create_char(frame->thread, (w_instance)frame->jstack_top[-4].c, (w_instance)frame->jstack_top[-3].c, frame->jstack_top[-2].c, frame->jstack_top[-1].c);
  frame->jstack_top -= 4;
  enterUnsafeRegion(frame->thread);
}

/*
** Create a string instance from an array of ASCII bytes. This is used in some constructors of String
** so the string is freshly created and doesn't have a wotsit set yet.
*/

static void i_String_create_byte(w_thread thread, w_instance String, w_instance byteArray, w_int hibyte, w_int offset, w_int count) {

  w_string  string;
  w_sbyte * buffer;
  w_char *  charbuff;
  w_int     length;
  w_byte *  from;
  w_char *  to;
  w_int     i;

  if (!byteArray) {
    throwException(thread, clazzNullPointerException, NULL);
  }
  else {
    buffer = instance2Array_byte(byteArray);
    length = instance2Array_length(byteArray);

    if (offset < 0 || count < 0 || offset > length - count) {
      throwException(thread, clazzArrayIndexOutOfBoundsException, NULL);
    } 
    else {
      if (hibyte < 0) {
        string = utf2String(buffer + offset, (w_size)count);
        setWotsitField(String, F_String_wotsit, string);
      }
      else {
        charbuff = allocMem(count * sizeof(w_char));
        if (!charbuff) {
          wabort(ABORT_WONKA, "Unable to allocate charbuff\n");
        }
        from = buffer + offset;
        to = charbuff;

        if (charbuff) {
          for (i = 0; i < count; ++i) {
            *to++ = (hibyte << 8) | (*from++);
          }
          string = unicode2String(charbuff, (w_size)count);
          setWotsitField(String, F_String_wotsit, string);
          releaseMem(charbuff);
        }
      }
    }
  }

}

void String_create_byte(JNIEnv *env, w_instance thisString, w_instance byteArray, w_int hibyte, w_int offset, w_int count) {
  i_String_create_byte(JNIEnv2w_thread(env), thisString, byteArray, hibyte, offset, count);
}

void fast_String_create_byte(w_frame frame) {
  enterSafeRegion(frame->thread);
  i_String_create_byte(frame->thread, (w_instance)frame->jstack_top[-5].c, (w_instance)frame->jstack_top[-4].c, frame->jstack_top[-3].c, frame->jstack_top[-2].c, frame->jstack_top[-1].c);
  frame->jstack_top -= 5;
  enterUnsafeRegion(frame->thread);
}

void fast_String_toString(w_frame frame) {
  if (!frame->jstack_top[-1].c) {
    enterSafeRegion(frame->thread);
    throwException(frame->thread, clazzNullPointerException, NULL);
    enterUnsafeRegion(frame->thread);
  }
}

w_instance String_toCharArray(JNIEnv *env, w_instance This) {
  w_thread thread = JNIEnv2w_thread(env);
  w_int length;
  w_string this = String2string(This);
  w_instance result;
  
  length = string_length(this);
  woempa(1, "Allocating array of char[%d]\n", length);
  enterUnsafeRegion(thread);
  result = allocArrayInstance_1d(JNIEnv2w_thread(env), atype2clazz[P_char], length);
  enterSafeRegion(thread);
  if (result) {
    if (string_is_latin1(this)) {
      w_size i;
      for (i = 0; i < string_length(this); ++i) {
        instance2Array_char(result)[i] = (w_char)this->contents.bytes[i];
      }
    }
    else {
      w_memcpy(instance2Array_char(result), this->contents.chars, string_length(this) * sizeof(w_char));
    }
  }

  return result;
  
}

w_int String_compareTo(JNIEnv *env, w_instance This, w_instance String) {

  w_string this = String2string(This);
  w_string string;
  w_int result = 0;
  w_int i;

  if (!String) {
    throwException(JNIEnv2w_thread(env), clazzNullPointerException, NULL);
  }
  else {
    string = String2string(String);
    if (string_length(this) == 0) {
      result -= string_length(string);
    }
    else if (string_length(string) == 0) {
      result = string_length(this);
    }
    else if (string != this) {
      for (i = 0; i < (w_int)string_length(this); i++) {

        /*
        ** Calculate possible result based on character compare.
        */
        
        if (string_char(this, i) != string_char(string, i)) {
          result = (w_int)(string_char(this, i) - string_char(string, i));
          break;
        }

        /*
        ** Check that String and This don't differ in length only, otherwise calculate length difference.
        */
        
        if ((i + 1) >= (w_int)string_length(string) || (i + 1) >= (w_int)string_length(this)) {
          result = string_length(this) - string_length(string);
          break;
        }

      }
    }
  }

  return result;
  
}

w_instance String_getBytes(JNIEnv *env, w_instance This, w_boolean Enc) {

  w_thread thread = JNIEnv2w_thread(env);
  w_instance result = NULL;
  w_string this = String2string(This);
  w_byte *dst;
  w_int i;

  enterUnsafeRegion(thread);
  if (Enc == WONKA_TRUE){
    w_byte * utfbytes = string2UTF8(this, &i);

    if (utfbytes) {
      i -= 2;
      result = allocArrayInstance_1d(thread, atype2clazz[P_byte], i);
      if (result) {
        w_byte * utfsrc = utfbytes + 2;// skip the two first bytes (the length)
        w_int k=0;
        dst = instance2Array_byte(result);
        for( ; k < i ; k++){
          *dst++ = *utfsrc++;
        }
      }
      releaseMem(utfbytes);
    }
  }
  else {
    w_int length = string_length(this);

    result = allocArrayInstance_1d(thread, atype2clazz[P_byte], length);
    if (result) {
      dst = instance2Array_byte(result);
      for (i = 0; i < (w_int)string_length(this); i++) {
        *dst++ = (w_byte)string_char(this, i);
      }
    }
  }
  enterSafeRegion(thread);

  return result;

}

/*
** Compare two strings for equality
*/

static w_boolean i_String_equals(w_thread thread, w_instance thisString, w_instance otherString) {

  if (! otherString || instance2clazz(otherString) != clazzString) {
    return WONKA_FALSE;
  }
  else {
    woempa(1, "Comparing %w to %w.\n", String2string(thisString), String2string(otherString));
    return String2string(thisString) == String2string(otherString);
  }

}

w_boolean String_equals(JNIEnv *env, w_instance thisString, w_instance otherString) {
  return i_String_equals(JNIEnv2w_thread(env), thisString, otherString);
}

void fast_String_equals(w_frame frame) {
  w_instance objectref = (w_instance) frame->jstack_top[-2].c;
  w_instance otherref = (w_instance) frame->jstack_top[-1].c;

  if (objectref) {
    frame->jstack_top[-2].s = 0;
    frame->jstack_top[-2].c = i_String_equals(frame->thread, objectref, otherref);
    frame->jstack_top -= 1;
  }
  else {
    enterSafeRegion(frame->thread);
    throwException(frame->thread, clazzNullPointerException, NULL);
    enterUnsafeRegion(frame->thread);
  }
}

/*
** Compare two strings for equality modulo case
*/

w_boolean String_equalsIgnoreCase(JNIEnv *env, w_instance This, w_instance Other) {

  w_boolean result = WONKA_FALSE;
  w_string this;
  w_string other;
  w_int i;

  /*
  ** First check wether our arguments are not null and are of class String or a subclass, 
  ** if that checks out fine, we go further...
  */

  if (Other && isSuperClass(clazzString, instance2clazz(Other))) {
    this = String2string(This);
    other = String2string(Other);
    /*
    ** Do a quick pointer check first.
    */
    if (this == other) {
      result = WONKA_TRUE;
    }
    else if (string_length(this) == string_length(other)) {
      /*
      ** Preset result to TRUE and compare character by character; when we hit an unequal character,
      ** we set result to FALSE and break...
      */
      result = WONKA_TRUE;
      for (i = 0; i < (w_int)string_length(this); i++) {
        w_char this_char = string_char(this, i);
        w_char that_char = string_char(other, i);
        if ( this_char != that_char && (char2lower(this_char) != char2lower(that_char)) && (char2upper(this_char) != char2upper(that_char))) {
          result = WONKA_FALSE;
          break;
        }
      }
    }
  }
  
  return result;

}

static w_int i_String_hashCode(w_thread thread, w_instance thisString) {
  w_string string = String2string(thisString);

  return string->wonka_hash;
}

w_int String_hashCode(JNIEnv *env, w_instance thisString) {
  return i_String_hashCode(JNIEnv2w_thread(env), thisString);
}

void fast_String_hashCode(w_frame frame) {
  w_instance objectref = (w_instance) frame->jstack_top[-1].c;

  if (objectref) {
    frame->jstack_top[-1].s = 0;
    frame->jstack_top[-1].c = i_String_hashCode(frame->thread, objectref);
  }
  else {
    enterSafeRegion(frame->thread);
    throwException(frame->thread, clazzNullPointerException, NULL);
    enterUnsafeRegion(frame->thread);
  }
}

/*
** Length in unicode characters
*/

w_int String_length(JNIEnv *env, w_instance String) {
  w_string string = String2string(String);
  woempa(1, "%p '%w'\n", String, string);

  return string_length(string);

}

void fast_String_length(w_frame frame) {
  w_instance objectref = (w_instance) frame->jstack_top[-1].c;

  if (objectref) {
    w_string string = String2string(objectref);

    woempa(1, "%p '%w'\n", objectref, string);
    frame->jstack_top[-1].s = 0;
    frame->jstack_top[-1].c = string_length(string);
  }
  else {
    enterSafeRegion(frame->thread);
    throwException(frame->thread, clazzNullPointerException, NULL);
    enterUnsafeRegion(frame->thread);
  }
}

w_boolean String_regionMatches(JNIEnv *env, w_instance This, w_boolean ic, w_int to, w_instance Other, w_int oo, w_int len) {
  w_thread thread = JNIEnv2w_thread(env);
  w_boolean result = WONKA_TRUE;
  w_string this = String2string(This);
  w_string other;
  w_int i;
  
  if (Other) {
    other = String2string(Other);
    if (to < 0 || oo < 0 || to + len > (w_int)string_length(this) || oo + len > (w_int)string_length(other)) {
      result = WONKA_FALSE;
    }
    else {
      if (ic == WONKA_FALSE) {
        for (i = 0; i < len; i++) {
          if (string_char(this, i + to) != string_char(other, oo + i)) {
            result = WONKA_FALSE;
            break;
          }
        }
      }
      else {
        for (i = 0; i < len; i++) {
          w_char this_char = string_char(this, i + to);
          w_char that_char = string_char(other, oo + i);
          if ((char2upper(this_char) != char2upper(that_char)) && (char2lower(this_char) != char2lower(that_char))) {
            result = WONKA_FALSE;
            break;
          }
        }
      }
    }
  }
  else {
    throwException(thread, clazzNullPointerException, NULL);
  }
  
  return result;
  
}

static w_boolean i_String_startsWith(w_string this, w_string prefix, w_int offset) {
  w_size i;

  if (string_length(prefix) == 0) {
    return TRUE;
  }

  if (prefix == this && offset == 0) {
    return TRUE;
  }

  if (offset < (w_int)string_length(this) && string_length(prefix) <= (string_length(this) - offset)) {
    for (i = 0; i < string_length(prefix); ++i) {
      if (string_char(this, offset + i) != string_char(prefix, i)) {
        return FALSE;
      }
    }
    return TRUE;
  }

  return FALSE;


}

w_boolean String_startsWith(JNIEnv *env, w_instance This, w_instance Prefix, w_int offset) {
  if (!Prefix) {
    throwException(JNIEnv2w_thread(env), clazzNullPointerException, NULL);
    return FALSE;
  }

  return i_String_startsWith(String2string(This), String2string(Prefix), offset);
}

void fast_String_startsWith(w_frame frame) {
  w_instance objectref = (w_instance) frame->jstack_top[-3].c;
  w_instance otherref = (w_instance) frame->jstack_top[-2].c;

  if (objectref && otherref) {
    frame->jstack_top[-3].s = 0;
    frame->jstack_top[-3].c = (w_word)i_String_startsWith(String2string(objectref), String2string(otherref), frame->jstack_top[-1].c);
    frame->jstack_top -= 2;
  }
  else {
    enterSafeRegion(frame->thread);
    throwException(frame->thread, clazzNullPointerException, NULL);
    enterUnsafeRegion(frame->thread);
  }
}

w_boolean String_endsWith(JNIEnv *env, w_instance This, w_instance Suffix) {

  w_string this;
  w_string suffix;
  w_int offset;
  w_size i;
  
  if (!Suffix) {
    throwException(JNIEnv2w_thread(env), clazzNullPointerException, NULL);
    return FALSE;
  }

  this = String2string(This);
  suffix = String2string(Suffix);
  offset = string_length(this) - string_length(suffix);

  if (string_length(suffix) == 0 ) {
    return TRUE;
  }

  if (suffix == this) {
    return TRUE;
  }

  if (offset > 0) {
    for (i = 0; i < string_length(suffix); ++i) {
      if (string_char(this, offset + i) != string_char(suffix, i)) {
        return FALSE;
      }
    }
    return TRUE;
  }

  return FALSE;  

}

/*
** Get one character of a string.  The characters are numbered from 0.
*/

static w_char i_String_charAt(w_thread thread, w_instance thisString, w_int idx) {
  w_string string = String2string(thisString);

  if (idx< 0 || idx>= (w_int)string_length(string)) {
    woempa(7, "asked for charAt(%d), length of `%w' is %d\n", idx, string, string_length(string));
    throwException(thread, clazzStringIndexOutOfBoundsException, NULL);
    return 0;
  }
  
  return string_char(string, idx); 
 
}

w_char String_charAt(JNIEnv *env, w_instance thisString, w_int idx) {
  return i_String_charAt(JNIEnv2w_thread(env), thisString, idx);
}

void fast_String_charAt(w_frame frame) {
  w_instance objectref = (w_instance) frame->jstack_top[-2].c;

  if (objectref) {
    w_int idx = frame->jstack_top[-1].c;
    w_string string = String2string(objectref);
    if (idx >= 0 && idx < (w_int)string_length(string)) {
      frame->jstack_top[-2].s = 0;
      frame->jstack_top[-2].c = string_char(string, idx); 
      frame->jstack_top -= 1;
    }
    else {
      woempa(7, "asked for charAt(%d), length of `%w' is %d\n", idx, string, string_length(string));
      enterSafeRegion(frame->thread);
      throwException(frame->thread, clazzStringIndexOutOfBoundsException, NULL);
      enterUnsafeRegion(frame->thread);
    }
  }
  else {
    enterSafeRegion(frame->thread);
    throwException(frame->thread, clazzNullPointerException, NULL);
    enterUnsafeRegion(frame->thread);
  }
}

void String_getChars(JNIEnv *env, w_instance This, w_int srcBegin, w_int srcEnd, w_instance Dst, w_int dstBegin) {
  w_thread thread = JNIEnv2w_thread(env);
  w_string this;
  w_char *dst;
  w_int length;

  this = String2string(This);
  if (Dst == NULL) {
    throwException(thread, clazzNullPointerException, NULL);
  }
  else if (srcBegin < 0 || srcBegin > srcEnd || srcEnd > (w_int)string_length(this) || dstBegin < 0 || dstBegin + srcEnd - srcBegin > instance2Array_length(Dst)) {
    throwException(thread, clazzIndexOutOfBoundsException, NULL);
  }
  else {
    if ((srcEnd - srcBegin) > 0) {
      dst = instance2Array_char(Dst) + dstBegin;
      length = srcEnd - srcBegin;
      if (string_is_latin1(this)) {
        w_int i;
        for (i = srcBegin; i < srcEnd; ++i) {
          dst[i - srcBegin] = (w_char)this->contents.bytes[i];
        }
      }
      else {
        w_memcpy(dst, this->contents.chars + srcBegin, length * sizeof(w_char));
      }
    }
  }
  
} 


void String_copyBytes(JNIEnv *env, w_instance Source, w_int srcBegin, w_int srcEnd, w_instance dstByteArray, w_int dstBegin) {
  w_string source = String2string(Source);
  w_sbyte *destination;
  w_int length;
  w_int i;
  
  destination = instance2Array_byte(dstByteArray);
  destination += dstBegin;
  length = srcEnd - srcBegin;
  
  if (string_is_latin1(source)) {
    w_memcpy(destination, source->contents.bytes + srcBegin, length * sizeof(w_sbyte));
  }
  else {
    for (i = 0; i < length; i++) {
      *destination++ = (w_byte)string_char(source, i);
    }
  }
}


static w_int i_String_indexOf_char(w_thread thread, w_instance thisString, w_int ch, w_int offset) {
  w_string string;
  w_int result = -1;
  w_int length;

  string = String2string(thisString);
  length = string_length(string);
  offset = offset < 0 ? 0 : offset;
  woempa(1, "Searching %slatin1 string '%w' for %d starting at offset %d\n", string_is_latin1(string) ? "" : "non-", string, ch, offset);

  for (  ; offset < length; offset++) {
    w_char cx = string_char(string, offset);
    woempa(1, "Char at offset %d is %d\n", offset, cx);
    if ((w_char)ch == cx) {
      result = offset;
      break;
    }
  }

  return result;
  
}

w_int String_indexOf_char(JNIEnv *env, w_instance thisString, w_int ch, w_int offset) {
  return i_String_indexOf_char(JNIEnv2w_thread(env), thisString, ch, offset);
}

void fast_String_indexOf_char(w_frame frame) {
  w_instance objectref = (w_instance) frame->jstack_top[-3].c;

  if (objectref) {
    frame->jstack_top[-3].s = 0;
    frame->jstack_top[-3].c = (w_word)i_String_indexOf_char(frame->thread, objectref, frame->jstack_top[-2].c, frame->jstack_top[-1].c);
    frame->jstack_top -= 2;
  }
  else {
    enterSafeRegion(frame->thread);
    throwException(frame->thread, clazzNullPointerException, NULL);
    enterUnsafeRegion(frame->thread);
  }
}

w_int String_indexOf_String(JNIEnv *env, w_instance thisString, w_instance otherString, w_int fromIndex) {
  w_thread thread = JNIEnv2w_thread(env);
  w_string string;
  w_string other;
  w_int where;
  w_int max;
  w_int result = -1;
  w_size length;
  w_boolean this_is_latin1;
  w_boolean other_is_latin1;
  w_int s;
  w_int o;

  if (!otherString) {
    throwException(thread, clazzNullPointerException, NULL);
  }
  else {
    string = String2string(thisString);
    other = String2string(otherString);
    length = string_length(other);
    this_is_latin1 = string_is_latin1(string);
    other_is_latin1 = string_is_latin1(other);
    woempa(1,"findstring '%w' in '%w' starting at %d\n",other,string,fromIndex);
    
    if (string_length(other) == 0) {
    	return  fromIndex;
    }

    if (fromIndex < (w_int)string_length(string)) {
      where = fromIndex<0?0:fromIndex;
      max = string_length(string) - string_length(other);
      while (where <= max) {
        woempa(1,"trying offset %d\n",where);
        if (this_is_latin1 && other_is_latin1) {
          if (memcmp(string->contents.bytes + where, other->contents.bytes, length * sizeof(w_ubyte)) == 0) {
            woempa(1,"match at offset %d\n",where);

            return where;

          }
        }
        else if (!this_is_latin1 && !other_is_latin1) {
          if (memcmp(string->contents.chars + where, other->contents.chars, length * sizeof(w_char)) == 0) {
            woempa(1,"match at offset %d\n",where);

            return where;

          }
        }
        else {
          for (s = where, o = 0; o < (w_int)string_length(other); o++, s++) {
            woempa(1, "A = %d, B = %d\n", string_char(string, s), string_char(other, o)); 
            if (string_char(string, s) != string_char(other, o)) {
              woempa(1,"mismatch after %d chars\n",o);
              break;
            }
          }
          if (o == (w_int)string_length(other)) {
            woempa(1,"match at offset %d\n",where);

            return where;

          }
        }
        where += 1;
      }
    }
  }
  
  woempa(1, "No match found.\n");

  return result;

}

w_int String_lastIndexOf_char(JNIEnv *env, w_instance thisString, w_int ch, w_int offset) {
  w_string string = String2string(thisString);
  w_int i;
  w_int result = -1;

  string = String2string(thisString);

  if (0 <= offset) {
    offset = (offset < (w_int)string_length(string)) ? offset : (w_int)string_length(string) - 1;
    for (i = offset; i >= 0; i--) {
      if (ch == (w_int)string_char(string, i)) {
        result = i;
        break;
      }
    }
  }

  woempa(1, "Looked for last index of '%c', offset %d in %w, result %d.\n", (char)ch, offset, string, result);

  return result;
  
}

w_int String_lastIndexOf_String(JNIEnv *env, w_instance thisString, w_instance otherString, w_int offset) {
  w_thread thread = JNIEnv2w_thread(env);
  w_string string;
  w_string other;
  w_int where;
  w_int max;
  w_int s;
  w_int o;
  w_int result = -1;

  if (!otherString) {
    throwException(thread, clazzNullPointerException, NULL);
  }
  else if (0 <= offset) {
    string = String2string(thisString);
    other = String2string(otherString);
    woempa(1, "%w <-> %w starting at %d.\n", other, string, offset);
    offset = (offset < (w_int)string_length(string)) ? offset : (w_int)string_length(string);
    max = string_length(string) - string_length(other);
    if (string_length(other) == 0) {
    	return  offset;
    }
    if (max < 0) return -1;
    where = ( max < offset ?  max : offset);
    while (where+1) {
      woempa(1, "trying offset %d.\n", where);
      for (s = where, o = 0; o < (w_int)string_length(other); o++, s++) {
        if (string_char(string, s) != string_char(other, o)) {
          woempa(1, "mismatch after %d chars\n", o);
          break;
        }
      }
      if (o == (w_int)string_length(other)) {
        result = where;
        break;
      }
      where -= 1;
    }

    woempa(1, "Result: %w <-> %w starting at %d, result %d.\n", other, string, offset, result);

  }

  return result;

}

w_instance String_toUpperCase(JNIEnv *env, w_instance This, w_instance Locale) {

  w_string this = String2string(This);
  w_instance result = This;
  w_int i;
  w_char *buffer;
  w_char *dst;
  w_string string;

  buffer = allocMem(string_length(this) * sizeof(w_char));
  if (buffer) {
    dst = buffer;
    for (i = 0; i < (w_int)string_length(this); i++) {
      *dst++ = char2upper(string_char(this, i));
    }
    string = unicode2String(buffer, string_length(this));
    if (string) {
      result = newStringInstance(string);
    // string is now registered twice, which is once too many
      deregisterString(string);
    }
    releaseMem(buffer);
  }
    
  return result;

}

w_instance String_toLowerCase(JNIEnv *env, w_instance This, w_instance Locale) {

  w_string this = String2string(This);
  w_instance result = This;
  w_int i;
  w_char * buffer;
  w_char * dst;
  w_string string;
  
  buffer = allocMem(string_length(this) * sizeof(w_char));
  if (buffer) {
    dst = buffer;
    for (i = 0; i < (w_int)string_length(this); i++) {
      *dst++ = char2lower(string_char(this, i));
    }
    string = unicode2String(buffer, string_length(this));
    if (string) {
      result = newStringInstance(string);
      deregisterString(string);
    }
    releaseMem(buffer);
  }
    
  return result;

}

w_instance String_replace(JNIEnv *env, w_instance This, w_char oldChar, w_char newChar) {

  w_string this;
  w_instance Result = This;
  w_string result;
  w_char * buffer;
  w_int i;

  this = String2string(This);
  buffer = allocMem(string_length(this) * sizeof(w_char));
  if (buffer) {
    for (i = 0; i < (w_int)string_length(this); i++) {
      w_char ch = string_char(this, i);
      if (ch == oldChar) {
        buffer[i] = newChar;
      }
      else {
        buffer[i] = ch;
      }
    }
    result = unicode2String(buffer, string_length(this));
    if (result) {
      Result = newStringInstance(result);
      deregisterString(result);
    }
    releaseMem(buffer);
  }

  return Result;

}

w_instance String_concat(JNIEnv *env, w_instance This, w_instance String) {

  w_thread thread = JNIEnv2w_thread(env);
  w_string this;
  w_string string;
  w_instance Result = NULL;
  w_string result;
  w_char * buffer;

  if (!String) {
    throwException(thread, clazzNullPointerException, NULL);

    return NULL;

  }

  this = String2string(This);
  string = String2string(String);
  if (string_length(string)) {
    buffer = allocMem((string_length(string) + string_length(this)) * sizeof(w_char));
    if (buffer) {
      if (string_is_latin1(this)) {
        w_size i;
        for (i = 0; i < string_length(this); ++i) {
          buffer[i] = (w_char)this->contents.bytes[i];
        }
      }
      else {
        w_memcpy(buffer, this->contents.chars, string_length(this) * sizeof(w_char));
      }

      if (string_is_latin1(string)) {
        w_size i;
        for (i = 0; i < string_length(string); ++i) {
          buffer[i + string_length(this)] = (w_char)string->contents.bytes[i];
        }
      }
      else {
        w_memcpy(buffer + string_length(this), string->contents.chars, string_length(string) * sizeof(w_char));
      }
      result = unicode2String(buffer, string_length(this) + string_length(string));
      if (result) {
        Result = newStringInstance(result);
        deregisterString(result);
      }
      releaseMem(buffer);
    }
  }
  else {
    Result = This;
  }
  
  return Result;

}

static w_instance i_String_substring(w_thread thread, w_instance This, w_int offset, int endIndex) {

  w_string this;
  w_instance SubString = NULL;
  w_string subString = NULL;
  w_char  *buffer;
  w_size   length;

  this = String2string(This);
  length = string_length(this);
  woempa(1, "String '%w' (length %d) offset %d endIndex %d\n", this, length, offset, endIndex);

  if (offset < 0 || offset > (w_int)length || offset > endIndex || endIndex > (w_int)length) {
    throwException(thread, clazzStringIndexOutOfBoundsException, NULL);
  }
  else {
    buffer = allocMem((endIndex - offset) * sizeof(w_char));
    if (buffer) {
      if (string_is_latin1(this)) {
        w_int i;
        for (i = offset; i < endIndex; ++i) {
          buffer[i - offset] = (w_char)this->contents.bytes[i];
        }
      }
      else {
        w_memcpy(buffer, this->contents.chars + offset, (endIndex - offset) * sizeof(w_char));
      }
      subString = unicode2String(buffer, (w_size)(endIndex - offset));
      releaseMem(buffer);
    }
    if (subString) {
      SubString = newStringInstance(subString);
      deregisterString(subString);
    woempa(1, "Result is %w\n", subString);
    } 
  }

  return SubString;

}

w_instance String_substring(JNIEnv *env, w_instance thisString, w_int offset, int endIndex) {
  return i_String_substring(JNIEnv2w_thread(env), thisString, offset, endIndex);
}

void fast_String_substring(w_frame frame) {
  w_instance objectref = (w_instance) frame->jstack_top[-3].c;
  w_thread thread = frame->thread;

  enterSafeRegion(thread);
  if (objectref) {
    w_instance subString = i_String_substring(frame->thread, objectref, frame->jstack_top[-2].c, frame->jstack_top[-1].c);
    enterUnsafeRegion(thread);
    frame->jstack_top[-3].c = (w_word)subString;
    if (subString) {
      setFlag(instance2flags(subString), O_BLACK);
      removeLocalReference(thread, subString);
    }
    frame->jstack_top -= 2;
  }
  else {
    throwException(thread, clazzNullPointerException, NULL);
    enterUnsafeRegion(thread);
  }
}

/*
**
*/

w_instance String_intern(JNIEnv *env, w_instance thisString) {
  w_thread thread = JNIEnv2w_thread(env);
  w_instance resultString;

  threadMustBeSafe(thread);
  ht_lock(string_hashtable);
  resultString = internString(thread, thisString);
  ht_unlock(string_hashtable);

  return resultString;
}

/*
**
*/

w_instance String_trim(JNIEnv *env, w_instance This) {

  w_string this = String2string(This);
  w_instance Result = This;
  w_string result;
  w_size i;
  w_int leading;
  w_int trailing;
  w_char * buffer = NULL;

  /*
  ** First check if we have leading and trailing whitespace and a string which is
  ** not empty.
  */

  if (string_length(this) > 0 && (string_char(this, 0) <= 0x0020 || string_char(this, string_length(this) - 1) <= 0x0020)) {
    buffer = allocMem(string_length(this) * sizeof(w_char));
    if (buffer) {
      if (string_is_latin1(this)) {
        for (i = 0; i < string_length(this); ++i) {
          buffer[i] = (w_char)this->contents.bytes[i];
        }
      }
      else {
        w_memcpy(buffer, this->contents.chars, string_length(this) * sizeof(w_char));
      }
      leading = 0;
      trailing = 0;
      for (i = 0; i < string_length(this); i++) {
        if (buffer[i] > 0x0020) {
          break;
        }
        leading += 1;
      }
      for (i = string_length(this) - 1; i >= 0; i--) {
        if (buffer[i] > 0x0020) {
          break;
        }
        trailing += 1;
      }
      if (leading != (w_int)string_length(this)) {
        result = unicode2String(buffer + leading, string_length(this) - trailing - leading);
        if (result) {
          Result = newStringInstance(result);
          deregisterString(result);
        }
      }
      else {
        Result = getStringInstance(string_empty);
      }
      releaseMem(buffer);
    }
  }

  return Result;

}

w_instance String_static_valueOf_char(JNIEnv *env, w_instance stringClass, w_char c) {
  w_string string;
  w_instance result = NULL;

  string = unicode2String(&c, 1);
  if (string) {
    result = newStringInstance(string);
    // string is now registered twice, which is once too many
    deregisterString(string);
  }
  return result;
}

