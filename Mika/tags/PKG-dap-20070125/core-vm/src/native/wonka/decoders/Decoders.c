/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: Decoders.c,v 1.4 2006/10/04 14:24:17 cvsroot Exp $
*/

#include "wonka.h"
#include "clazz.h"
#include "core-classes.h"
#include "heap.h"
#include "ts-mem.h"
#include "exception.h"
#include "wstrings.h"
#include "arrays.h"

w_instance UTF8Decoder_bToC(JNIEnv *env, w_instance Decoder, w_instance byteArray, w_int offset, w_int count){

  w_thread thread = JNIEnv2w_thread(env);
  w_instance Chars;
  w_char * chars;
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

  chars = utf2chars((bytes + offset), &count);

  if (chars) {
    Chars = allocArrayInstance_1d(thread, atype2clazz[P_char], count);
    if (Chars) {
      w_memcpy(instance2Array_byte(Chars), chars, (w_word)(count*2));
    }
    releaseMem(chars);
    return Chars;
  }

  throwOutOfMemoryError(thread);
  return NULL;

}

w_instance UTF8Decoder_bToString(JNIEnv *env, w_instance Decoder, w_instance byteArray, w_int offset, w_int count){

  w_thread thread = JNIEnv2w_thread(env);
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


w_instance UTF8Decoder_cToB(JNIEnv *env, w_instance Decoder, w_instance charArray, w_int offset, w_int count){

  w_thread thread = JNIEnv2w_thread(env);
  w_char * chars;
  w_byte * bytes;
  w_int length;
  w_instance Bytes;
  w_int utf8len;

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
    Bytes = allocArrayInstance_1d(thread, atype2clazz[P_byte], utf8len);
    if (Bytes) {
      w_memcpy(instance2Array_byte(Bytes), bytes + 2, utf8len);
    }
    releaseMem(bytes);
    return Bytes;
  }

  throwOutOfMemoryError(thread);
  return NULL;

}

w_instance UTF8Decoder_stringToB(JNIEnv *env, w_instance Decoder, w_instance String){
  w_thread thread = JNIEnv2w_thread(env);
  w_byte * bytes;
  w_int length;
  w_instance Bytes;

  if (!String){
    throwException(thread, clazzNullPointerException, NULL);
    return NULL;
  }

  bytes = string2UTF8(String2string(String),&length);

  if (bytes && length >= 2) {
    length -= 2;
    Bytes = allocArrayInstance_1d(thread, atype2clazz[P_byte], length);
    if (Bytes) {
      w_memcpy(instance2Array_byte(Bytes),bytes+2,(w_word)length);
    }
    releaseMem(bytes);
    return Bytes;
  }
  return NULL;
}

void Latin1Decoder_copyArray(JNIEnv *env, w_instance This, w_instance Bytes, w_int boff, w_instance Chars, w_int coff, w_int count) {

  w_char * chars;
  w_ubyte * bytes;
  w_int clength;
  w_int blength;
  w_int duffs;

  if (!Chars || !Bytes){
    throwException(JNIEnv2w_thread(env), clazzNullPointerException, NULL);
    return;
  }

  clength = instance2Array_length(Chars);
  blength = instance2Array_length(Bytes);

  if (boff < 0 || count < 0 || boff + count > blength || coff < 0  || coff > clength - count) {
    throwException(JNIEnv2w_thread(env), clazzArrayIndexOutOfBoundsException, NULL);
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

w_instance Latin1Decoder_cToB(JNIEnv *env, w_instance This, w_instance Chars, w_int offset, w_int count){

  w_thread thread = JNIEnv2w_thread(env);
  w_char * chars;
  w_ubyte * bytes;
  w_int length;
  w_instance Bytes;

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

  Bytes = allocArrayInstance_1d(thread, atype2clazz[P_byte], count);

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
