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

#include <string.h>

#include "clazz.h"
#include "arrays.h"
#include "exception.h"
#include "heap.h"
#include "core-classes.h"

void CRC32_update(w_thread thread, w_instance thisCRC32, w_instance byteArray, w_int off, w_int len) {
  
  if (!byteArray) {
        throwException(thread, clazzNullPointerException, NULL);
  }
  else {
    if (off < 0 || len < 0 || off > instance2Array_length(byteArray) - len) {
        throwException(thread, clazzArrayIndexOutOfBoundsException, NULL);
    }
    else {
      w_sbyte * data = instance2Array_byte(byteArray);
      w_word crc = wordFieldPointer(thisCRC32, F_CRC32_crc)[WORD_LSW];
      data += off;
      wordFieldPointer(thisCRC32, F_CRC32_crc)[WORD_LSW] = update_ISO3309_CRC(crc, data, (w_size) len);
    }
  }
}

