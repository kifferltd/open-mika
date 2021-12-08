/**************************************************************************
* Copyright (c) 2021 by KIFFER Ltd. All rights reserved.                  *
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

void fill_bool(w_thread thread, w_instance thisClazz, w_instance array, w_boolean value) {
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
    throwException(thread, clazzNullPointerException, NULL);
  }
}


/*
** Byte
*/

void fill_byte(w_thread thread, w_instance thisClazz, w_instance array, w_sbyte value) {
  if(array) {
    w_int length = instance2Array_length(array);
    w_sbyte *data = instance2Array_byte(array);
    memset(data, (w_int)value & 0xff, length);
  }
  else {
    throwException(thread, clazzNullPointerException, NULL);
  }
}

void fill_byte_range(w_thread thread, w_instance thisClazz, w_instance array, w_int from, w_int to, w_sbyte value) {
  if(array) {
    w_int length = instance2Array_length(array);
    w_sbyte *data = instance2Array_byte(array);
    
    if(from < 0 || to < 0 || from > length || to > length) {
      throwException(thread, clazzArrayIndexOutOfBoundsException, NULL);
    }
    else if(from > to) {
      throwException(thread, clazzIllegalArgumentException, NULL);
    }
    else {
      memset(data + from, (w_int)value & 0xff, to - from);
    }
  }
  else {
    throwException(thread, clazzNullPointerException, NULL);
  }
}

