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

#include "core-classes.h"
#include "ts-mem.h"
#include "exception.h"
#include "arrays.h"

w_int ArrayUtil_bArrayToI(w_thread thread, w_instance Class, w_instance bytes, w_int off){
  w_int length;
  w_ubyte * buffer;

  if(bytes == NULL){
    throwException(thread, clazzNullPointerException, NULL);
    return 0;
  }

  length = instance2Array_length(bytes);

  if(off < 0 || off > length - 4){
    throwException(thread, clazzArrayIndexOutOfBoundsException, NULL);
    return 0;
  }

  buffer = (instance2Array_byte(bytes)) + off;

  return (w_int)((buffer[0] << 24) | (buffer[1] << 16) | (buffer[2] << 8) | buffer[3]);
}

w_long ArrayUtil_bArrayToL(w_thread thread, w_instance Class, w_instance bytes, w_int off){
  w_int length;
  w_long result;
  w_ubyte * buffer;

  if(bytes == NULL){
    throwException(thread, clazzNullPointerException, NULL);
    return 0;
  }

  length = instance2Array_length(bytes);

  if(off < 0 || off > length - 8){
    throwException(thread, clazzArrayIndexOutOfBoundsException, NULL);
    return 0;
  }

  buffer = (instance2Array_byte(bytes)) + off;

  result = buffer[0];
  result = (result << 8) | buffer[1];
  result = (result << 8) | buffer[2];
  result = (result << 8) | buffer[3];
  result = (result << 8) | buffer[4];
  result = (result << 8) | buffer[5];
  result = (result << 8) | buffer[6];
  result = (result << 8) | buffer[7];

  return result;
}

void ArrayUtil_iInBArray(w_thread thread, w_instance Class, w_int i, w_instance bytes, w_int off){
  w_ubyte * buffer;
  w_int length;

  if(bytes == NULL){
    throwException(thread, clazzNullPointerException, NULL);
    return;
  }

  length = instance2Array_length(bytes);

  if(off < 0 || off > length - 4){
    throwException(thread, clazzArrayIndexOutOfBoundsException, NULL);
    return;
  }

  buffer = instance2Array_byte(bytes) + off;

  buffer[0] = (i >> 24);
  buffer[1] = (i >> 16);
  buffer[2] = (i >>  8);
  buffer[3] = i;
}

void ArrayUtil_lInBArray(w_thread thread, w_instance Class, w_long l, w_instance bytes, w_int off){
  w_ubyte * buffer;
  w_int length;
  w_word word;

  if(bytes == NULL){
    throwException(thread, clazzNullPointerException, NULL);
    return;
  }

  length = instance2Array_length(bytes);

  if(off < 0 || off > length - 8){
    throwException(thread, clazzArrayIndexOutOfBoundsException, NULL);
    return;
  }

  buffer = instance2Array_byte(bytes) + off;

  word = l;
  buffer[4] = (word >> 24);
  buffer[5] = (word >> 16);
  buffer[6] = (word >>  8);
  buffer[7] = word;

  word = l >> 32;
  buffer[0] = (word >> 24);
  buffer[1] = (word >> 16);
  buffer[2] = (word >>  8);
  buffer[3] = word;
}

w_float ArrayUtil_bArrayToF(w_thread thread, w_instance Class, w_instance bytes, w_int off){
  w_int result = ArrayUtil_bArrayToI(thread, Class, bytes, off);
  w_float* f = (w_float*)&result;
  return *f;
}

w_double ArrayUtil_bArrayToD(w_thread thread, w_instance Class, w_instance bytes, w_int off){
  w_long result = ArrayUtil_bArrayToL(thread, Class, bytes, off);
  w_double* d = (w_double*)&result;
  return *d;
}

void ArrayUtil_fInBArray(w_thread thread, w_instance Class, w_float f, w_instance bytes, w_int off){
  w_int* i = (w_int*) &f;
  ArrayUtil_iInBArray(thread, Class, *i, bytes, off);
}

void ArrayUtil_dInBArray(w_thread thread, w_instance Class, w_double d, w_instance bytes, w_int off){
  w_long* l = (w_long*)&d;
  ArrayUtil_lInBArray(thread, Class, *l, bytes, off);

}

