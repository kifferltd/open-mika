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

w_long RMIConnection_getStringHash(w_thread thread, w_instance ThisClass, w_instance TheString) {
  w_long suid = 0;

  if(TheString == NULL){
    throwException(thread,clazzNullPointerException,NULL);
  }
  else {
    w_word length;
    w_byte * utf8 = string2UTF8(String2string(TheString), &length);

    woempa(10, "'%w' with length %i (=?= %d)\n",String2string(TheString),length,(w_int)*(utf8+1));

    if(utf8) {
      w_sha sha = allocSha();
      processSha(sha, utf8, length);
      finishSha(sha);
      releaseMem(utf8);

      suid = sha->signature.B[4];
      suid <<= 8;  suid |= sha->signature.B[5];
      suid <<= 8;  suid |= sha->signature.B[6];
      suid <<= 8;  suid |= sha->signature.B[7];
      suid <<= 8;  suid |= sha->signature.B[0];
      suid <<= 8;  suid |= sha->signature.B[1];
      suid <<= 8;  suid |= sha->signature.B[2];
      suid <<= 8;  suid |= sha->signature.B[3];
    }
  }
  return suid;

}
