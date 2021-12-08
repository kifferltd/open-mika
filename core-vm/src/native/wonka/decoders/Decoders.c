/**************************************************************************
* Copyright (c) 2010, 2021 by KIFFER Ltd. All rights reserved.            *
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

#include "wonka.h"
#include "clazz.h"
#include "core-classes.h"
#include "heap.h"
#include "ts-mem.h"
#include "exception.h"
#include "wstrings.h"
#include "arrays.h"

w_instance UTF8Decoder_bToC(w_thread thread, w_instance Decoder, w_instance byteArray, w_int offset, w_int count){

  w_instance Chars;
  w_char * chars;
  w_byte * bytes;
  w_int length;

  threadMustBeSafe(thread);
  if (!byteArray) {
    throwException(thread, clazzNullPointerException, NULL);
    return NULL;
  }

  bytes = instance2Array_byte(byteArray);
  length = instance2Array_length(byteArray);

  if (offset < 0 || count < 0 || offset > length - count) {
    throwException(thread, clazzArrayIndexOutOfBoundsException, NULL);
    return NULL;
  }

  chars = utf2chars((bytes + offset), &count);

  if (chars) {
    enterUnsafeRegion(thread);
    Chars = allocArrayInstance_1d(thread, atype2clazz[P_char], count);
    enterSafeRegion(thread);
    if (Chars) {
      w_memcpy(instance2Array_byte(Chars), chars, (w_word)(count*2));
    }
    releaseMem(chars);
    return Chars;
  }

  return NULL;

}

w_instance UTF8Decoder_bToString(w_thread thread, w_instance Decoder, w_instance byteArray, w_int offset, w_int count){

  w_instance String;
  w_string string;
  w_byte * bytes;
  w_int length;

  if (!byteArray) {
    throwException(thread, clazzNullPointerException, NULL);
    return NULL;
  }

  bytes = instance2Array_byte(byteArray);
  length = instance2Array_length(byteArray);

  if (offset < 0 || count < 0 || offset > length - count) {
    throwException(thread, clazzArrayIndexOutOfBoundsException, NULL);
    return NULL;
  }
  string = utf2String(bytes + offset, count);

  if (string) {
    String = newStringInstance(string);
    deregisterString(string);
    return String;
  }
  return NULL;
}


w_instance UTF8Decoder_cToB(w_thread thread, w_instance Decoder, w_instance charArray, w_int offset, w_int count){

  w_char * chars;
  w_byte * bytes;
  w_int length;
  w_instance Bytes;
  w_int utf8len;

  threadMustBeSafe(thread);
  if (!charArray){
    throwException(thread, clazzNullPointerException, NULL);
    return NULL;
  }

  chars = instance2Array_char(charArray);
  length = instance2Array_length(charArray);

  if (offset < 0 || count < 0 || offset > length - count){
    throwException(thread, clazzArrayIndexOutOfBoundsException, NULL);
    return NULL;
  }

  bytes = chars2UTF8(chars + offset, count, &utf8len);
  utf8len -= 2;

  if (bytes) {
    enterUnsafeRegion(thread);
    Bytes = allocArrayInstance_1d(thread, atype2clazz[P_byte], utf8len);
    enterSafeRegion(thread);
    if (Bytes) {
      w_memcpy(instance2Array_byte(Bytes), bytes + 2, utf8len);
    }
    releaseMem(bytes);
    return Bytes;
  }

  return NULL;

}

w_instance UTF8Decoder_stringToB(w_thread thread, w_instance Decoder, w_instance String){
  w_byte * bytes;
  w_int length;
  w_instance Bytes;

  threadMustBeSafe(thread);
  if (!String){
    throwException(thread, clazzNullPointerException, NULL);
    return NULL;
  }

  bytes = string2UTF8(String2string(String),&length);

  if (bytes && length >= 2) {
    length -= 2;
    enterUnsafeRegion(thread);
    Bytes = allocArrayInstance_1d(thread, atype2clazz[P_byte], length);
    enterSafeRegion(thread);
    if (Bytes) {
      w_memcpy(instance2Array_byte(Bytes),bytes+2,(w_word)length);
    }
    releaseMem(bytes);
    return Bytes;
  }
  return NULL;
}

void Latin1Decoder_copyArray(w_thread thread, w_instance This, w_instance Bytes, w_int boff, w_instance Chars, w_int coff, w_int count) {

  w_char * chars;
  w_ubyte * bytes;
  w_int clength;
  w_int blength;
  w_int duffs;

  if (!Chars || !Bytes){
    throwException(thread, clazzNullPointerException, NULL);
    return;
  }

  clength = instance2Array_length(Chars);
  blength = instance2Array_length(Bytes);

  if (boff < 0 || count < 0 || boff + count > blength || coff < 0  || coff > clength - count) {
    throwException(thread, clazzArrayIndexOutOfBoundsException, NULL);
    return;
  }

  chars = instance2Array_char(Chars) + coff;
  bytes = instance2Array_byte(Bytes) + boff;

  duffs = (count + 31) / 32;

  if (count) {
    switch (count & 0x1f){
      case  0: do {  *chars++ = *bytes++;
      case 31:       *chars++ = *bytes++;
      case 30:       *chars++ = *bytes++;
      case 29:       *chars++ = *bytes++;
      case 28:       *chars++ = *bytes++;
      case 27:       *chars++ = *bytes++;
      case 26:       *chars++ = *bytes++;
      case 25:       *chars++ = *bytes++;
      case 24:       *chars++ = *bytes++;
      case 23:       *chars++ = *bytes++;
      case 22:       *chars++ = *bytes++;
      case 21:       *chars++ = *bytes++;
      case 20:       *chars++ = *bytes++;
      case 19:       *chars++ = *bytes++;
      case 18:       *chars++ = *bytes++;
      case 17:       *chars++ = *bytes++;
      case 16:       *chars++ = *bytes++;
      case 15:       *chars++ = *bytes++;
      case 14:       *chars++ = *bytes++;
      case 13:       *chars++ = *bytes++;
      case 12:       *chars++ = *bytes++;
      case 11:       *chars++ = *bytes++;
      case 10:       *chars++ = *bytes++;
      case  9:       *chars++ = *bytes++;
      case  8:       *chars++ = *bytes++;
      case  7:       *chars++ = *bytes++;
      case  6:       *chars++ = *bytes++;
      case  5:       *chars++ = *bytes++;
      case  4:       *chars++ = *bytes++;
      case  3:       *chars++ = *bytes++;
      case  2:       *chars++ = *bytes++;
      case  1:       *chars++ = *bytes++;
                  } while(--duffs > 0);
    }
  }

}

w_instance Latin1Decoder_cToB(w_thread thread, w_instance This, w_instance Chars, w_int offset, w_int count){

  w_char * chars;
  w_ubyte * bytes;
  w_int length;
  w_instance Bytes;

  threadMustBeSafe(thread);
  if (!Chars) {
    throwException(thread, clazzNullPointerException, NULL);
    return NULL;
  }

  chars = instance2Array_char(Chars) + offset;
  length = instance2Array_length(Chars);

  if (offset < 0 || count < 0 || offset > length - count) {
    throwException(thread, clazzArrayIndexOutOfBoundsException, NULL);
    return NULL;
  }

  enterUnsafeRegion(thread);
  Bytes = allocArrayInstance_1d(thread, atype2clazz[P_byte], count);
  enterSafeRegion(thread);

  if (Bytes) {
    length = (count + 31) /32; // Use length as duffs count...
    bytes = instance2Array_byte(Bytes);

    if (count) {
      switch(count & 0x1f){
        case  0: do {  *bytes++ = *chars++;
        case 31:       *bytes++ = *chars++;
        case 30:       *bytes++ = *chars++;
        case 29:       *bytes++ = *chars++;
        case 28:       *bytes++ = *chars++;
        case 27:       *bytes++ = *chars++;
        case 26:       *bytes++ = *chars++;
        case 25:       *bytes++ = *chars++;
        case 24:       *bytes++ = *chars++;
        case 23:       *bytes++ = *chars++;
        case 22:       *bytes++ = *chars++;
        case 21:       *bytes++ = *chars++;
        case 20:       *bytes++ = *chars++;
        case 19:       *bytes++ = *chars++;
        case 18:       *bytes++ = *chars++;
        case 17:       *bytes++ = *chars++;
        case 16:       *bytes++ = *chars++;
        case 15:       *bytes++ = *chars++;
        case 14:       *bytes++ = *chars++;
        case 13:       *bytes++ = *chars++;
        case 12:       *bytes++ = *chars++;
        case 11:       *bytes++ = *chars++;
        case 10:       *bytes++ = *chars++;
        case  9:       *bytes++ = *chars++;
        case  8:       *bytes++ = *chars++;
        case  7:       *bytes++ = *chars++;
        case  6:       *bytes++ = *chars++;
        case  5:       *bytes++ = *chars++;
        case  4:       *bytes++ = *chars++;
        case  3:       *bytes++ = *chars++;
        case  2:       *bytes++ = *chars++;
        case  1:       *bytes++ = *chars++;
                    } while(--length > 0);
      }
    }
  }

  return Bytes;

}
