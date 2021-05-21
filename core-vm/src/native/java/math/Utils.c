/**************************************************************************
* Copyright (c) 2007 by Chris Gray, /k/ Embedded Java Solutions.          *
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
* 3. Neither the name of /k/ Embedded Java Solutions nor the names of     *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL /K/ EMBEDDED JAVA SOLUTIONS OR OTHER CONTRIBUTORS BE  *
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR     *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

#include "core-classes.h"
//#include "exception.h"
//#include "fields.h"

w_int Utils_static_numberOfLeadingZeros_int(JNIEnv *env, w_instance thisClass, w_int i) {
  w_word j;
  w_int n;

  if (i == 0) {

    return 32;

  }

  j = 0x80000000U;
  n = 0;

  while (((unsigned)i & j) == 0) {
    j >>= 1;
    ++n;
  }

  return n;
}

w_int Utils_static_numberOfLeadingZeros_long(JNIEnv *env, w_instance thisClass, w_long l) {
  w_dword j;
  w_int n;

  if (l == 0) {

    return 64;

  }

  j = 0x8000000000000000LLU;
  n = 0;

  while (((w_dword)l & j) == 0) {
    j >>= 1;
    ++n;
  }

  return n;
}

w_int Utils_static_numberOfTrailingZeros_int(JNIEnv *env, w_instance thisClass, w_int i) {
  w_word j;
  w_int n;

  if (i == 0) {

    return 32;

  }

  j = 1;
  n = 0;

  while (((w_word)i & j) == 0) {
    j <<= 1;
    ++n;
  }

  return n;
}

w_int Utils_static_numberOfTrailingZeros_long(JNIEnv *env, w_instance thisClass, w_long l) {
  w_dword j;
  w_int n;

  if (l == 0) {

    return 64;

  }

  j = 1;
  n = 0;

  while (((w_dword)l & j) == 0) {
    j <<= 1;
    ++n;
  }

  return n;
}

w_int Utils_static_signum_int(JNIEnv *env, w_instance thisClass, w_int i) {
  w_int signum = 0;

  if (i > 0) {
    ++signum;
  }
  else if (i < 0) {
    --signum;
  }

  return signum;
}

w_int Utils_static_signum_long(JNIEnv *env, w_instance thisClass, w_long l) {
  w_int signum = 0;

  if (l > 0) {
    ++signum;
  }
  else if (l < 0) {
    --signum;
  }

  return signum;
}

w_int Utils_static_highestOneBit_int(JNIEnv *env, w_instance thisClass, w_int i) {
  w_int j = i;
  w_int k = 0;

  while (j != 0) {
    k = j;
    j &= j - 1;
  }

printf("highestOneBit(%08x) = %d\n", i, k);
  return k;
}

