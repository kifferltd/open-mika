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

#include <stdlib.h>
#include <string.h>
#include "clazz.h"
#include "core-classes.h"
#include "wstrings.h"
#include "mika_threads.h"
#include "wonka.h"
#include "exception.h"

void 
Etc_static_setTriggerLevel
( JNIEnv *env, w_instance classSystem,
  w_instance filenameString, w_int triggerLevel
) {
  woempa(9, "NOT FUNCTIONAL\n");
}

void 
Etc_static_setAllTriggerLevel
( JNIEnv *env, w_instance classSystem,
  w_int triggerLevel
) {
  setAllTriggerLevel(triggerLevel);
}

void
Etc_static_woempa
( JNIEnv *env, w_instance classSystem,
  w_int triggerLevel,
  w_instance theString
) {
  woempa(triggerLevel, "%w\n", String2string ( theString));
}

void Etc_static_heapCheck(JNIEnv *env, w_instance classSystem){
#ifdef DEBUG
  woempa(9, "calling heapCheck for Etc\n");
  heapCheck;
#endif //DEBUG

}

