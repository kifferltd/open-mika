/**************************************************************************
* Copyright (c) 2008, 2009, 2010, 2011, 2013, 2018, 2023 by KIFFER Ltd.   *
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
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS *
* OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)   *
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,     *
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING   *
* IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE      *
* POSSIBILITY OF SUCH DAMAGE.                                             *
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
( w_thread thread, w_instance classSystem,
  w_instance filenameString, w_int triggerLevel
) {
  woempa(9, "NOT FUNCTIONAL\n");
}

void 
Etc_static_setAllTriggerLevel
( w_thread thread, w_instance classSystem,
  w_int triggerLevel
) {
  setAllTriggerLevel(triggerLevel);
}

void
Etc_static_woempa
( w_thread thread, w_instance classSystem,
  w_int triggerLevel,
  w_instance theString
) {
  woempa(triggerLevel, "%w\n", String2string ( theString));
}

void
Etc_static_wabort
( w_thread thread, w_instance classSystem,
  w_instance theString
) {
  wabort(ABORT_WONKA, "%w\n", String2string ( theString));
}

void Etc_static_heapCheck(w_thread thread, w_instance classEtc){
#ifdef DEBUG
  woempa(9, "calling heapCheck for Etc\n");
  heapCheck;
#endif //DEBUG

}

void 
Etc_static_memoryCheck ( w_thread thread, w_instance classSystem) {
// change this to TRACE_MEM_ALLOC to enable the check in DEBUG mode
#ifdef TRACE_MEM_ALLOC_BUT_NOT_TODAY
  woempa(7, "calling heapCheck via Etc\n");
  heapCheck;
  woempa(7, "calling lowMemoryCheck via Etc\n");
  lowMemoryCheck;
#endif //DEBUG
}

