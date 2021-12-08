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

#include "wonka.h"
#include "core-classes.h"
#include "ts-mem.h"
#include "md5.h"
#include "exception.h"
#include "wstrings.h"
#include "arrays.h"

#include <string.h>

void MD5MessageDigest_md5Clone(w_thread thread, w_instance This, w_instance theClone) {
  w_md5Acc md5 = getWotsitField(This, F_MD5MessageDigest_wotsit);
  w_md5Acc md5Clone = allocMD5Acc(thread);

  setWotsitField(theClone, F_MD5MessageDigest_wotsit, md5Clone);

  // copy values
  md5Clone->len = md5->len;
  md5Clone->total = md5->total;
  memcpy(md5Clone->sums, md5->sums, 4 * sizeof(w_word));
  memcpy(md5Clone->bytes, md5->bytes, 64 * sizeof(w_ubyte));
  
  setWotsitField(theClone, F_MD5MessageDigest_wotsit, md5Clone);
}

void MD5MessageDigest_engineReset(w_thread thread, w_instance This){
  w_md5Acc md5 =  getWotsitField(This, F_MD5MessageDigest_wotsit);

  if(md5 != NULL){
    releaseMem(md5);
  }

  md5 = allocMD5Acc(thread);
  setWotsitField(This, F_MD5MessageDigest_wotsit, md5);
}

void MD5MessageDigest_engineUpdate(w_thread thread, w_instance This, w_instance Input, w_int offset, w_int len){
  w_int length;
  w_ubyte * bytes;
  w_md5Acc md5;

  if(Input == NULL){
    throwException(thread,clazzNullPointerException,NULL);
  }

  length = instance2Array_length(Input);

  if(offset < 0 || len < 0 || offset > length - len){
    throwException(thread,clazzArrayIndexOutOfBoundsException,NULL);
  }

  md5 = getWotsitField(This, F_MD5MessageDigest_wotsit);

  if(md5 == NULL){
    md5 = allocMD5Acc(thread);
    setWotsitField(This, F_MD5MessageDigest_wotsit, md5);
    if(md5 == NULL){
      return;
    }
  }

  bytes = ((w_ubyte*)instance2Array_byte(Input)) + offset;

  processMD5Acc(md5, bytes, (w_size)len);

}

void MD5MessageDigest_finalize(w_thread thread, w_instance This){
  w_md5Acc md5 =  getWotsitField(This, F_MD5MessageDigest_wotsit);

  if(md5 != NULL){
    releaseMem(md5);
  }
}

void MD5MessageDigest_nativeDigest(w_thread thread, w_instance This, w_instance Bytes, w_int off){
  w_int length;
  w_ubyte * bytes;
  w_md5Acc md5;
  w_word words[4];

  if(Bytes == NULL){
    throwException(thread,clazzNullPointerException,NULL);
  }

  length = instance2Array_length(Bytes);

  if(off < 0 || off > length - 16){
    throwException(thread,clazzArrayIndexOutOfBoundsException,NULL);
  }

  md5 = getWotsitField(This, F_MD5MessageDigest_wotsit);

  if(md5 == NULL){
    md5 = allocMD5Acc(thread);
    setWotsitField(This, F_MD5MessageDigest_wotsit, md5);
    if(md5 == NULL){
      return;
    }
  }

  bytes = ((w_ubyte*)instance2Array_byte(Bytes)) + off;

  finishMD5Acc(md5,words);

  w_memcpy(bytes,words,16);

  releaseMem(md5);

  clearWotsitField(This, F_MD5MessageDigest_wotsit);
}
