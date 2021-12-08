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
#include "sha.h"
#include "exception.h"
#include "wstrings.h"
#include "arrays.h"

#include <string.h>

void ShaMessageDigest_shaClone(w_thread thread, w_instance This, w_instance theClone) {
    w_sha sha = getWotsitField(This, F_ShaMessageDigest_wotsit);
    w_sha shaClone = allocSha();

    setWotsitField(theClone, F_ShaMessageDigest_wotsit, shaClone);

    // copy values
    shaClone->buffered = sha->buffered;
    shaClone->lo_length = sha->lo_length;
    shaClone->hi_length = sha->hi_length;
    memcpy(shaClone->buffer.W, sha->buffer.W, sizeof(w_word) * 80);   
    memcpy(shaClone->signature.W, sha->signature.W, sizeof(w_word) * 5);   
    
    setWotsitField(theClone, F_ShaMessageDigest_wotsit, shaClone);
}

void ShaMessageDigest_engineReset(w_thread thread, w_instance This){
  w_sha sha =  getWotsitField(This, F_ShaMessageDigest_wotsit);

  if(sha != NULL){
    releaseMem(sha);
  }

  sha = allocSha();

  setWotsitField(This, F_ShaMessageDigest_wotsit, sha);

}

void ShaMessageDigest_engineUpdate(w_thread thread, w_instance This, w_instance Input, w_int offset, w_int len){
  w_int length;
  w_ubyte * bytes;
  w_sha sha;

  if(Input == NULL){
    throwException(thread,clazzNullPointerException,NULL);
  }

  length = instance2Array_length(Input);

  if(offset < 0 || len < 0 || offset > length - len){
    throwException(thread,clazzArrayIndexOutOfBoundsException,NULL);
  }

  sha = getWotsitField(This, F_ShaMessageDigest_wotsit);

  if(sha == NULL){
    sha = allocSha();
    setWotsitField(This, F_ShaMessageDigest_wotsit, sha);
    if(sha == NULL){
      return;
    }
  }

  bytes = ((w_ubyte*)instance2Array_byte(Input)) + offset;

  processSha(sha, bytes, (w_size)len);

}

void ShaMessageDigest_finalize(w_thread thread, w_instance This){
  w_sha sha =  getWotsitField(This, F_ShaMessageDigest_wotsit);

  if(sha != NULL){
    releaseMem(sha);
  }
}

void ShaMessageDigest_nativeDigest(w_thread thread, w_instance This, w_instance Bytes, w_int off){
  w_int length;
  w_ubyte * bytes;
  w_sha sha;
  w_word word;

  if(Bytes == NULL){
    throwException(thread,clazzNullPointerException,NULL);
  }

  length = instance2Array_length(Bytes);

  if(off < 0 || off > length - 20){
    throwException(thread,clazzArrayIndexOutOfBoundsException,NULL);
  }

  sha = getWotsitField(This, F_ShaMessageDigest_wotsit);

  if(sha == NULL){
    sha = allocSha();
    setWotsitField(This, F_ShaMessageDigest_wotsit, sha);
    if(sha == NULL){
      return;
    }
  }

  bytes = ((w_ubyte*)instance2Array_byte(Bytes)) + off;

  finishSha(sha);

  /** TODO make sure this still works on a BIG ENDIAN platform */
  word = sha->signature.W[0];
  *bytes++ = (w_ubyte)(word>>24);
  *bytes++ = (w_ubyte)(word>>16);
  *bytes++ = (w_ubyte)(word>>8);
  *bytes++ = (w_ubyte) word;
  word = sha->signature.W[1];
  *bytes++ = (w_ubyte)(word>>24);
  *bytes++ = (w_ubyte)(word>>16);
  *bytes++ = (w_ubyte)(word>>8);
  *bytes++ = (w_ubyte) word;
  word = sha->signature.W[2];
  *bytes++ = (w_ubyte)(word>>24);
  *bytes++ = (w_ubyte)(word>>16);
  *bytes++ = (w_ubyte)(word>>8);
  *bytes++ = (w_ubyte) word;
  word = sha->signature.W[3];
  *bytes++ = (w_ubyte)(word>>24);
  *bytes++ = (w_ubyte)(word>>16);
  *bytes++ = (w_ubyte)(word>>8);
  *bytes++ = (w_ubyte) word;
  word = sha->signature.W[4];
  *bytes++ = (w_ubyte)(word>>24);
  *bytes++ = (w_ubyte)(word>>16);
  *bytes++ = (w_ubyte)(word>>8);
  *bytes++ = (w_ubyte) word;


  releaseMem(sha);

  clearWotsitField(This, F_ShaMessageDigest_wotsit);
}

