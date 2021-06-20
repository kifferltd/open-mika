/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2007 by Chris Gray, /k/ Embedded Java Solutions.    *
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

#include "core-classes.h"
#include "clazz.h"
#include "arrays.h"
#include "exception.h"
#include "heap.h"
#include "mika_threads.h"
#include "network.h"

w_instance do_word_copy(w_thread thread, w_instance Array, w_instance This){
  w_int length = instance2Array_length(Array) * 4;
  w_instance Bytes;

  threadMustBeSafe(thread);
  enterUnsafeRegion(thread);
  Bytes = allocArrayInstance_1d(thread, atype2clazz[P_byte], length);
  enterSafeRegion(thread);

  if(Bytes) {

    if(length > 0){
      w_word* src = (w_word*) instance2Array_int(Array);
      w_word* dst = (w_word*) instance2Array_byte(Bytes);
      w_int duffs;
      length = length / 4;  // length in words not bytes.

      duffs = (length + 31) / 32; // Note switch must be done on 31 == 0x1f

      switch (length & 0x1f) {
        case  0: do { *dst++ = htonl(*src++);
        case 31:      *dst++ = htonl(*src++);
        case 30:      *dst++ = htonl(*src++);
        case 29:      *dst++ = htonl(*src++);
        case 28:      *dst++ = htonl(*src++);
        case 27:      *dst++ = htonl(*src++);
        case 26:      *dst++ = htonl(*src++);
        case 25:      *dst++ = htonl(*src++);
        case 24:      *dst++ = htonl(*src++);
        case 23:      *dst++ = htonl(*src++);
        case 22:      *dst++ = htonl(*src++);
        case 21:      *dst++ = htonl(*src++);
        case 20:      *dst++ = htonl(*src++);
        case 19:      *dst++ = htonl(*src++);
        case 18:      *dst++ = htonl(*src++);
        case 17:      *dst++ = htonl(*src++);
        case 16:      *dst++ = htonl(*src++);
        case 15:      *dst++ = htonl(*src++);
        case 14:      *dst++ = htonl(*src++);
        case 13:      *dst++ = htonl(*src++);
        case 12:      *dst++ = htonl(*src++);
        case 11:      *dst++ = htonl(*src++);
        case 10:      *dst++ = htonl(*src++);
        case  9:      *dst++ = htonl(*src++);
        case  8:      *dst++ = htonl(*src++);
        case  7:      *dst++ = htonl(*src++);
        case  6:      *dst++ = htonl(*src++);
        case  5:      *dst++ = htonl(*src++);
        case  4:      *dst++ = htonl(*src++);
        case  3:      *dst++ = htonl(*src++);
        case  2:      *dst++ = htonl(*src++);
        case  1:      *dst++ = htonl(*src++);
                } while (--duffs > 0);
      }
    }
    setIntegerField(This, F_ObjectOutputStream_pointer, length);
  }
  return Bytes;
}

w_instance do_long_copy(w_thread thread, w_instance Array, w_instance This){
  w_int length = instance2Array_length(Array) * 8;
  w_instance Bytes;

  threadMustBeSafe(thread);
  enterUnsafeRegion(thread);
  Bytes = allocArrayInstance_1d(thread, atype2clazz[P_byte], length);
  enterSafeRegion(thread);

  if(Bytes) {
    w_long* src = (w_long*) instance2Array_long(Array);
    w_word* dst = (w_word*) instance2Array_byte(Bytes);
    w_int i = 0;

    length = length / 8;
    setIntegerField(This, F_ObjectOutputStream_pointer, length);
    //TODO add duff device ...

    for( ; i < length ; i++){
      w_long l = *src++;
      *dst++ = htonl((w_word)(l>>32));
      *dst++ = htonl((w_word)l);
    }
  }
  return Bytes;

}

w_instance do_short_copy(w_thread thread, w_instance Array, w_instance This){
  w_int length = instance2Array_length(Array) * 2;
  w_instance Bytes;

  threadMustBeSafe(thread);
  enterUnsafeRegion(thread);
  Bytes = allocArrayInstance_1d(thread, atype2clazz[P_byte], length);
  enterSafeRegion(thread);

  if(Bytes) {

    if(length > 0){
      w_ushort* src = (w_ushort*) instance2Array_short(Array);
      w_ushort* dst = (w_ushort*) instance2Array_byte(Bytes);
      w_int duffs;
      length = length / 2;  // length in short not bytes.

      duffs = (length + 31) / 32; // Note switch must be done on 31 == 0x1f

      switch (length & 0x1f) {
        case  0: do { *dst++ = htons(*src++);
        case 31:      *dst++ = htons(*src++);
        case 30:      *dst++ = htons(*src++);
        case 29:      *dst++ = htons(*src++);
        case 28:      *dst++ = htons(*src++);
        case 27:      *dst++ = htons(*src++);
        case 26:      *dst++ = htons(*src++);
        case 25:      *dst++ = htons(*src++);
        case 24:      *dst++ = htons(*src++);
        case 23:      *dst++ = htons(*src++);
        case 22:      *dst++ = htons(*src++);
        case 21:      *dst++ = htons(*src++);
        case 20:      *dst++ = htons(*src++);
        case 19:      *dst++ = htons(*src++);
        case 18:      *dst++ = htons(*src++);
        case 17:      *dst++ = htons(*src++);
        case 16:      *dst++ = htons(*src++);
        case 15:      *dst++ = htons(*src++);
        case 14:      *dst++ = htons(*src++);
        case 13:      *dst++ = htons(*src++);
        case 12:      *dst++ = htons(*src++);
        case 11:      *dst++ = htons(*src++);
        case 10:      *dst++ = htons(*src++);
        case  9:      *dst++ = htons(*src++);
        case  8:      *dst++ = htons(*src++);
        case  7:      *dst++ = htons(*src++);
        case  6:      *dst++ = htons(*src++);
        case  5:      *dst++ = htons(*src++);
        case  4:      *dst++ = htons(*src++);
        case  3:      *dst++ = htons(*src++);
        case  2:      *dst++ = htons(*src++);
        case  1:      *dst++ = htons(*src++);
                } while (--duffs > 0);
      }
    }
    setIntegerField(This, F_ObjectOutputStream_pointer, length);
  }
  return Bytes;
}

w_instance do_boolean_copy(w_thread thread, w_instance Array, w_instance This){
  w_int length = instance2Array_length(Array);
  w_instance Bytes;

  threadMustBeSafe(thread);
  enterUnsafeRegion(thread);
  Bytes = allocArrayInstance_1d(thread, atype2clazz[P_byte], length);
  enterSafeRegion(thread);

  if(Bytes) {
    w_ubyte* src = (w_ubyte*) instance2Array_byte(Array);
    w_ubyte* dst = (w_ubyte*) instance2Array_byte(Bytes);
    w_int byteidx = length / 8;
    w_int bitidx = length % 8;
    w_ubyte bytevalue;
    w_int i = 0;

    setIntegerField(This, F_ObjectOutputStream_pointer, length);

    // TODO optimize this part...

    for( ; i < byteidx ; i++){
      bytevalue = *src++;
      *dst++ =  bytevalue     & 0x01;
      *dst++ = (bytevalue>>1) & 0x01;
      *dst++ = (bytevalue>>2) & 0x01;
      *dst++ = (bytevalue>>3) & 0x01;
      *dst++ = (bytevalue>>4) & 0x01;
      *dst++ = (bytevalue>>5) & 0x01;
      *dst++ = (bytevalue>>6) & 0x01;
      *dst++ = (bytevalue>>7) & 0x01;
    }
    if(bitidx){
       w_int s = bitidx;
      //TODO ... check if this works !!!

      bytevalue = *src++;
      switch(bitidx & 0x7){
        case 7: *dst++ = (bytevalue) & 0x01;
                s--;
        case 6: *dst++ = (bytevalue>>(bitidx-s--)) & 0x01;
        case 5: *dst++ = (bytevalue>>(bitidx-s--)) & 0x01;
        case 4: *dst++ = (bytevalue>>(bitidx-s--)) & 0x01;
        case 3: *dst++ = (bytevalue>>(bitidx-s--)) & 0x01;
        case 2: *dst++ = (bytevalue>>(bitidx-s--)) & 0x01;
        case 1: *dst++ = (bytevalue>>(bitidx-s--)) & 0x01;
      }
    }
  }
  return Bytes;
}

w_instance ObjectOutputStream_primitiveArrayToBytes(JNIEnv *env, w_instance thisOOStream, w_instance Object) {
  w_thread thread = JNIEnv2w_thread(env);
  w_clazz clazz;
  w_clazz type;
  w_instance Bytes;

  if(Object == NULL){
    throwNullPointerException(thread);
    return NULL;
  }

  clazz = instance2clazz(Object);

  if(clazz->dims == 0){
    woempa(9,"What do you mean this is not an ArrayClass %k\n",clazz);
    throwException(thread, clazzInternalError, "trying to serialize a primitive Array, but %k in not an ArrayClass",clazz);
    return NULL;
  }

  type = clazz->previousDimension;

  if (type == clazz_byte) {
    w_int length = instance2Array_length(Object);

    setIntegerField(thisOOStream, F_ObjectOutputStream_pointer, length);
    return Object;
  }
  else if (type == clazz_int || type == clazz_float) {
    Bytes = do_word_copy(thread, Object, thisOOStream);
  }
  else if (type == clazz_double || type == clazz_long) {
    Bytes = do_long_copy(thread, Object, thisOOStream);

  }
  else if (type == clazz_char || type == clazz_short) {
    Bytes = do_short_copy(thread, Object, thisOOStream);
  }
  else if (type == clazz_boolean) {
    Bytes = do_boolean_copy(thread, Object, thisOOStream);
  }
  else {
    woempa(9,"What do you mean this is not a primitive ArrayClass %k\n",clazz);
    throwException(thread, clazzInternalError, "trying to serialize a primitive Array, but %k in not a primitive ArrayClass",clazz);
    return NULL;
  }
  return Bytes;
}

