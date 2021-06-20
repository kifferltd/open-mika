/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

/*
** $Id: Arrays.c,v 1.2 2004/11/18 23:51:52 cvs Exp $
*/

#include "core-classes.h"
#include "clazz.h"
#include "heap.h"
#include "ts-mem.h"
#include "mika_threads.h"
#include "wstrings.h"
#include "arrays.h"
#include "exception.h"

#include <string.h>

/*
** Boolean
*/

void fill_bool(JNIEnv *env, w_instance thisClazz, w_instance array, w_boolean value) {
  if(array) {
    w_int length = instance2Array_length(array);
    w_ubyte *data = instance2Array_byte(array);
    if(value) {
      memset(data, 0xFF, (length + 7) / 8);
    }
    else {
      memset(data, 0x00, (length + 7) / 8);
    }
  }
  else {
    throwException(JNIEnv2w_thread(env), clazzNullPointerException, NULL);
  }
}


/*
** Byte
*/

void fill_byte(JNIEnv *env, w_instance thisClazz, w_instance array, w_sbyte value) {
  if(array) {
    w_int length = instance2Array_length(array);
    w_sbyte *data = instance2Array_byte(array);
    memset(data, (w_int)value & 0xff, length);
  }
  else {
    throwException(JNIEnv2w_thread(env), clazzNullPointerException, NULL);
  }
}

void fill_byte_range(JNIEnv *env, w_instance thisClazz, w_instance array, w_int from, w_int to, w_sbyte value) {
  if(array) {
    w_int length = instance2Array_length(array);
    w_sbyte *data = instance2Array_byte(array);
    
    if(from < 0 || to < 0 || from > length || to > length) {
      throwException(JNIEnv2w_thread(env), clazzArrayIndexOutOfBoundsException, NULL);
    }
    else if(from > to) {
      throwException(JNIEnv2w_thread(env), clazzIllegalArgumentException, NULL);
    }
    else {
      memset(data + from, (w_int)value & 0xff, to - from);
    }
  }
  else {
    throwException(JNIEnv2w_thread(env), clazzNullPointerException, NULL);
  }
}

