/**************************************************************************
* Copyright (c) 2004, 2005, 2006, 2007, 2009, 2021, 2022 by KIFFER Ltd.   *
* All rights reserved.                                                    *
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

#include "arrays.h"
#include "checks.h"
#include "core-classes.h"
#include "exception.h"
#include "fields.h"
#include "loading.h"
#include "network.h"
#include "methods.h"
#include "wstrings.h"

#ifdef FREERTOS
#include "FreeRTOS_IP.h"
#define w_ntohl FreeRTOS_ntohl
#define w_ntohs FreeRTOS_ntohs
#else
#define w_ntohl ntohl
#define w_ntohs ntohs
#endif

void throwInvalidClassException(w_thread thread, w_instance Class, char * message, int l){
  w_instance ice;

  threadMustBeSafe(thread);

  mustBeInitialized(clazzInvalidClassException);
  w_instance messageString =  getStringInstance(cstring2String(message, l)); 
  w_instance classnameString = getStringInstance(Class2clazz(Class)->dotified);

  enterUnsafeRegion(thread);
  ice = allocInstance(thread,clazzInvalidClassException);

  // TODO: this is messy!
  if(ice){
    if(messageString){
      setReferenceField_unsafe(ice, messageString, F_Throwable_detailMessage);
      deregisterString(messageString);
    }
    if(classnameString){
      setReferenceField_unsafe(ice, classnameString, F_InvalidClassException_classname);
      deregisterString(classnameString);
    }
    throwExceptionInstance(thread,ice);
  }
  enterSafeRegion(thread);
  if(messageString){
    removeLocalReference(thread, messageString);
  }
  if(classnameString){
    removeLocalReference(thread, classnameString);
  }
}

w_instance ObjectInputStream_allocNewInstance(w_thread thread, w_instance this, w_instance Clazz) {
#ifdef JNI
  JNIEnv *env = w_thread2JNIEnv(thread);
#endif
  w_clazz clazz;
  w_instance newInstance;

  if(!Clazz){
    throwNullPointerException(thread);
    return NULL;
  }

  clazz = Class2clazz(Clazz);

  threadMustBeSafe(thread);
  if (mustBeInitialized(clazz) == CLASS_LOADING_FAILED) {
    return NULL;
  }

  enterUnsafeRegion(thread);
  newInstance = allocInstance(thread, clazz);
  enterSafeRegion(thread);

  if(!newInstance){
    return NULL;
  }

  /** if obj is externalizable do
      1. run its no-arg constructor
      2. run the readExternal method. --> done in java.
  */
  if(isAssignmentCompatible(clazz, clazzExternalizable)){
#ifdef JNI
    jmethodID jmid = (*env)->GetMethodID(env, Clazz, "<init>","()V");
    if(jmid){
      w_method method = (w_method) jmid;
      if(method->spec.declaring_clazz == clazz){
        (*env)->CallNonvirtualVoidMethod(env, newInstance, Clazz, jmid);
      }
      else {
        throwInvalidClassException(thread, Clazz, "no-args constructor not available", 33);
      }
    }
    else {
      (*env)->ExceptionClear(env);
      throwInvalidClassException(thread, Clazz, "no-args constructor not available", 33);
    }
#else
    w_method cons = find_method(clazz, "<init>","()V");
    if (cons) {
      w_frame new_frame = pushFrame(thread, cons);
      new_frame->flags |= FRAME_REFLECTION;

      new_frame->jstack_top[0].c = (w_word)newInstance;
      new_frame->jstack_top[0].s = stack_trace;
      new_frame->jstack_top += 1;
      woempa(7, "Calling %M with parameter(%j)\n", cons, newInstance);
      callMethod(new_frame, cons);

      deactivateFrame(new_frame, NULL);
    }
    else {
      throwInvalidClassException(thread, Clazz, "no-args constructor not available", 33);
    }
#endif
  }
  /** else if obj serializable do
      1. run the no-arg constructor of it's first non-serializable superclass.
      2. call readObject or defaultReadObject for each of the ObjectStreamClass.
           --> done in java.
  */
  else if(isAssignmentCompatible(clazz, clazzSerializable)){
    w_clazz super = getSuper(clazz);

    //getSuper returns the super clazz or NULL if super is java.lang.Object

    while(super && isAssignmentCompatible(super, clazzSerializable)){
      super = getSuper(super);
    }

    if(super){
#ifdef JNI
      w_instance superClazz = clazz2Class(super);

      jmethodID jmid = (*env)->GetMethodID(env, superClazz, "<init>","()V");
      if(jmid){
        w_method method = (w_method) jmid;
        if(method->spec.declaring_clazz == super){
          (*env)->CallNonvirtualVoidMethod(env, newInstance, superClazz, jmid);
        }
        else {
          throwInvalidClassException(thread, superClazz, "no-args constructor not available", 33);
        }
      }
      else {
        (*env)->ExceptionClear(env);
        throwInvalidClassException(thread, superClazz, "no-args constructor not available", 33);
      }
#else
      w_method cons = find_method(super, "<init>","()V");
      if (cons) {
        w_frame new_frame = pushFrame(thread, cons);
        new_frame->flags |= FRAME_REFLECTION;
  
        new_frame->jstack_top[0].c = (w_word)newInstance;
        new_frame->jstack_top[0].s = stack_trace;
        new_frame->jstack_top += 1;
        woempa(7, "Calling %M with parameter(%j)\n", cons, newInstance);
        callMethod(new_frame, cons);
  
        deactivateFrame(new_frame, NULL);
      }
      else {
        throwInvalidClassException(thread, Clazz, "no-args constructor not available", 33);
     }
#endif
    }
  }
  /** else throw an Exception */
  else {
    throwInvalidClassException(thread, Clazz, "class is not Serializable", 25);
  }

  return newInstance;

}

w_instance ObjectInputStream_getCallingClassLoader(w_thread thread, w_instance this) {

  return clazz2loader(Class2clazz(getCallingClazz(thread)->Class));

}

w_instance ObjectInputStream_createAndFillByteArray(w_thread thread, w_instance this, w_int size){
#ifdef JNI
  JNIEnv *env = w_thread2JNIEnv(thread);
#endif
  w_instance Bytes;

  threadMustBeSafe(thread);
  enterUnsafeRegion(thread);
  Bytes = allocArrayInstance_1d(thread, atype2clazz[P_byte], size);
  enterSafeRegion(thread);

  if(Bytes){
#ifdef JNI
    w_instance Class = clazz2Class(clazzObjectInputStream);
    jmethodID jmid =(*env)->GetMethodID(env, Class, "getBytes", "([BI)V");
    if(jmid){
      (*env)->CallNonvirtualVoidMethod(env, this, Class, jmid, Bytes, size);
    }
    else {
      woempa(9, "getBytes([BI)V method not found !!!\n");
    }
#else
    w_method gbs = find_method(clazzObjectInputStream, "getBytes", "([BI)V");
    if (gbs) {
      w_frame new_frame = pushFrame(thread, gbs);
      new_frame->flags |= FRAME_REFLECTION;

      new_frame->jstack_top[0].c = (w_word)size;
      new_frame->jstack_top[0].s = stack_trace;
      new_frame->jstack_top += 1;
      new_frame->jstack_top[0].c = (w_word)Bytes;
      new_frame->jstack_top[0].s = stack_trace;
      new_frame->jstack_top += 1;
      new_frame->jstack_top[0].c = (w_word)this;
      new_frame->jstack_top[0].s = stack_trace;
      new_frame->jstack_top += 1;
      woempa(7, "Calling %M of %j with parameters(%j, %d)\n", gbs, this, Bytes, size);
      callMethod(new_frame, gbs);

      deactivateFrame(new_frame, NULL);
    }
#endif
  }
  return Bytes;
}

void to_word_array(w_instance Array, w_instance Bytes, w_int length){
  if(length > 0){
    w_word* dst = (w_word*) instance2Array_int(Array);
    w_word* src = (w_word*) instance2Array_byte(Bytes);
    w_int duffs = (length + 31) / 32; // Note switch must be done on 31 == 0x1f

    switch (length & 0x1f) {
      case  0: do { *dst++ = w_ntohl(*src++);
      case 31:      *dst++ = w_ntohl(*src++);
      case 30:      *dst++ = w_ntohl(*src++);
      case 29:      *dst++ = w_ntohl(*src++);
      case 28:      *dst++ = w_ntohl(*src++);
      case 27:      *dst++ = w_ntohl(*src++);
      case 26:      *dst++ = w_ntohl(*src++);
      case 25:      *dst++ = w_ntohl(*src++);
      case 24:      *dst++ = w_ntohl(*src++);
      case 23:      *dst++ = w_ntohl(*src++);
      case 22:      *dst++ = w_ntohl(*src++);
      case 21:      *dst++ = w_ntohl(*src++);
      case 20:      *dst++ = w_ntohl(*src++);
      case 19:      *dst++ = w_ntohl(*src++);
      case 18:      *dst++ = w_ntohl(*src++);
      case 17:      *dst++ = w_ntohl(*src++);
      case 16:      *dst++ = w_ntohl(*src++);
      case 15:      *dst++ = w_ntohl(*src++);
      case 14:      *dst++ = w_ntohl(*src++);
      case 13:      *dst++ = w_ntohl(*src++);
      case 12:      *dst++ = w_ntohl(*src++);
      case 11:      *dst++ = w_ntohl(*src++);
      case 10:      *dst++ = w_ntohl(*src++);
      case  9:      *dst++ = w_ntohl(*src++);
      case  8:      *dst++ = w_ntohl(*src++);
      case  7:      *dst++ = w_ntohl(*src++);
      case  6:      *dst++ = w_ntohl(*src++);
      case  5:      *dst++ = w_ntohl(*src++);
      case  4:      *dst++ = w_ntohl(*src++);
      case  3:      *dst++ = w_ntohl(*src++);
      case  2:      *dst++ = w_ntohl(*src++);
      case  1:      *dst++ = w_ntohl(*src++);
              } while (--duffs > 0);
    }
  }
}

void to_long_array(w_instance Array, w_instance Bytes, w_int length){
  w_long* dst = (w_long*) instance2Array_long(Array);
  w_word* src = (w_word*) instance2Array_byte(Bytes);
  w_int i = 0;

  //TODO: add duffs device

  for( ; i < length ; i++){
    w_long l = w_ntohl(*src++);
    *dst++ = (l<<32) | (w_ntohl(*src++));
  }
}

void to_short_array(w_instance Array, w_instance Bytes, w_int length){
  if(length > 0){
    w_ushort* dst = (w_ushort*) instance2Array_short(Array);
    w_ushort* src = (w_ushort*) instance2Array_byte(Bytes);
    w_int duffs = (length + 31) / 32; // Note switch must be done on 31 == 0x1f

    switch (length & 0x1f) {
      case  0: do { *dst++ = w_ntohs(*src++);
      case 31:      *dst++ = w_ntohs(*src++);
      case 30:      *dst++ = w_ntohs(*src++);
      case 29:      *dst++ = w_ntohs(*src++);
      case 28:      *dst++ = w_ntohs(*src++);
      case 27:      *dst++ = w_ntohs(*src++);
      case 26:      *dst++ = w_ntohs(*src++);
      case 25:      *dst++ = w_ntohs(*src++);
      case 24:      *dst++ = w_ntohs(*src++);
      case 23:      *dst++ = w_ntohs(*src++);
      case 22:      *dst++ = w_ntohs(*src++);
      case 21:      *dst++ = w_ntohs(*src++);
      case 20:      *dst++ = w_ntohs(*src++);
      case 19:      *dst++ = w_ntohs(*src++);
      case 18:      *dst++ = w_ntohs(*src++);
      case 17:      *dst++ = w_ntohs(*src++);
      case 16:      *dst++ = w_ntohs(*src++);
      case 15:      *dst++ = w_ntohs(*src++);
      case 14:      *dst++ = w_ntohs(*src++);
      case 13:      *dst++ = w_ntohs(*src++);
      case 12:      *dst++ = w_ntohs(*src++);
      case 11:      *dst++ = w_ntohs(*src++);
      case 10:      *dst++ = w_ntohs(*src++);
      case  9:      *dst++ = w_ntohs(*src++);
      case  8:      *dst++ = w_ntohs(*src++);
      case  7:      *dst++ = w_ntohs(*src++);
      case  6:      *dst++ = w_ntohs(*src++);
      case  5:      *dst++ = w_ntohs(*src++);
      case  4:      *dst++ = w_ntohs(*src++);
      case  3:      *dst++ = w_ntohs(*src++);
      case  2:      *dst++ = w_ntohs(*src++);
      case  1:      *dst++ = w_ntohs(*src++);
              } while (--duffs > 0);
    }
  }
}

void to_boolean_array(w_instance Array, w_instance Bytes, w_int length){
  w_ubyte* dst = (w_ubyte*) instance2Array_byte(Array);
  w_ubyte* src = (w_ubyte*) instance2Array_byte(Bytes);
  w_int byteidx = length / 8;
  w_int bitidx = length % 8;
  w_ubyte bytevalue;
  w_int i = 0;

  // TODO optimize this part...

  for( ; i < byteidx ; i++){
    bytevalue = (*src++ & 0x1);
    bytevalue |= (*src++ & 0x1)<<1;
    bytevalue |= (*src++ & 0x1)<<2;
    bytevalue |= (*src++ & 0x1)<<3;
    bytevalue |= (*src++ & 0x1)<<4;
    bytevalue |= (*src++ & 0x1)<<5;
    bytevalue |= (*src++ & 0x1)<<6;
    bytevalue |= (*src++ & 0x1)<<7;

    *dst++ = bytevalue;
  }
  if(bitidx){
    w_int s = bitidx;
    bytevalue = 0;
    switch(bitidx & 0x7){
      case 7: bytevalue = (*src++ & 0x1);
              s--;
      case 6: bytevalue |= (*src++ & 0x1)<<(bitidx-s--);
      case 5: bytevalue |= (*src++ & 0x1)<<(bitidx-s--);
      case 4: bytevalue |= (*src++ & 0x1)<<(bitidx-s--);
      case 3: bytevalue |= (*src++ & 0x1)<<(bitidx-s--);
      case 2: bytevalue |= (*src++ & 0x1)<<(bitidx-s--);
      case 1: bytevalue |= (*src++ & 0x1)<<(bitidx-s--);
    }
    *dst = bytevalue;
  }
}

w_instance ObjectInputStream_createPrimitiveArray(w_thread thread, w_instance this, w_instance Clazz, w_int length) {
  w_clazz type;
  w_clazz aclazz;
  w_instance Bytes;
  w_instance Array;

  threadMustBeSafe(thread);
  if(Clazz == NULL){
    woempa(9, "Class is NULL\n");
    throwNullPointerException(thread);
    return NULL;
  }

  type = Class2clazz(Clazz);

  aclazz = type->nextDimension;

  if(!aclazz || mustBeInitialized(aclazz) == CLASS_LOADING_FAILED) { //remove this check if possible ...
    woempa(9,"What do you mean the array clazz of %k is not yet defined\n",type);
    throwException(thread, clazzInternalError, "trying to deserialize a primitive Array, but no arrayclass defined");
    return NULL;
  }

  if(length < 0){
    throwException(thread, clazzStreamCorruptedException, "negative array size on stream");
    woempa(9, "length is negative\n");
    return NULL;
  }

  enterUnsafeRegion(thread);
  Array = allocArrayInstance_1d(thread, aclazz, length);
  enterSafeRegion(thread);

  if(!Array){
    return NULL;
  }

#ifdef JNI
  JNIEnv *env = w_thread2JNIEnv(thread);
#endif

  if (type == clazz_byte) {
#ifdef JNI
    w_instance Class = clazz2Class(clazzObjectInputStream);
    jmethodID jmid =(*env)->GetMethodID(env, Class, "getBytes", "([BI)V");
    if(jmid){
      (*env)->CallNonvirtualVoidMethod(env, this, Class, jmid, Array, length);
    }
#else
    w_method gbs = find_method(clazzObjectInputStream, "getBytes", "([BI)V");
    if (gbs) {
      w_frame new_frame = pushFrame(thread, gbs);
      new_frame->flags |= FRAME_REFLECTION;

      new_frame->jstack_top[0].c = (w_word)length;
      new_frame->jstack_top[0].s = stack_trace;
      new_frame->jstack_top += 1;
      new_frame->jstack_top[0].c = (w_word)Array;
      new_frame->jstack_top[0].s = stack_trace;
      new_frame->jstack_top += 1;
      new_frame->jstack_top[0].c = (w_word)this;
      new_frame->jstack_top[0].s = stack_trace;
      new_frame->jstack_top += 1;
      woempa(7, "Calling %m of %j with parameters(%j, %d)\n", gbs, this, Array, length);
      callMethod(new_frame, gbs);

      deactivateFrame(new_frame, NULL);
    }
#endif
  }
  else if (type == clazz_int || type == clazz_float) {
    Bytes = ObjectInputStream_createAndFillByteArray(thread, this, length * 4);
    if(Bytes){
      to_word_array(Array, Bytes, length);
    }
  }
  else if (type == clazz_double || type == clazz_long) {
    Bytes = ObjectInputStream_createAndFillByteArray(thread, this, length * 8);
    if(Bytes){
      to_long_array(Array, Bytes, length);
    }
  }
  else if (type == clazz_char || type == clazz_short) {
    Bytes = ObjectInputStream_createAndFillByteArray(thread, this, length * 2);
    if(Bytes){
      to_short_array(Array, Bytes, length);
    }
  }
  else if (type == clazz_boolean) {
    Bytes = ObjectInputStream_createAndFillByteArray(thread, this, length);
    if(Bytes){
      to_boolean_array(Array, Bytes, length);
    }
  }
  else {
    woempa(9,"What do you mean this is not a primitive Class %k\n",type);
    throwException(thread, clazzInternalError, "trying to deserialize a primitive Array, but %k in not a primitive type",type);
    return NULL;
  }
  return Array;
}

