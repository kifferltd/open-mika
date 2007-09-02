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
** $Id: RuleBasedCollator.c,v 1.2 2004/11/30 10:08:10 cvs Exp $
*/

#include "core-classes.h"
#include "hashtable.h"
#include "wstrings.h"
#include "unicode.h"

void RuleBasedCollator_createHashtables(JNIEnv *env, w_instance thisClass) {
  createDecompositionTables();
}

w_int RuleBasedCollator_getCombiningClass(JNIEnv *env, w_instance thisClass, w_char c) {
  return charToCombiningClass(c);
}

w_instance RuleBasedCollator_getDecomposition(JNIEnv *env, w_instance thisClass, w_char c) {
  w_char *decomposition;
  w_string   s;
  w_instance result;

  decomposition = charToDecomposition(c);
  if (decomposition) {

    s = unicode2String(decomposition+1, *decomposition);
    if (!s) {
      wabort(ABORT_WONKA, "Unable to create s\n");
    }
    result = newStringInstance(s);
    deregisterString(s);

    return result;
  }
  else {

    return NULL;

  }
}

w_instance RuleBasedCollator_getCompatibility(JNIEnv *env, w_instance thisClass, w_char c) {
  w_string compatibility;

  compatibility = charToCompatibilityType(c);
  if (compatibility) {

    return newStringInstance(compatibility);

  }
  else {

    return NULL;

  }
}

