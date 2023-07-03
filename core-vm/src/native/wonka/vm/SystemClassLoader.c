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

#include "argument.h"
#include "bar.h"
#include "clazz.h"
#include "core-classes.h"
#include "heap.h"
#include "loading.h"
#include "checks.h"
#include "ts-mem.h"
#include "oswald.h"
#include "wstrings.h"
#include "mika_threads.h"
#include "jni.h"

extern Wonka_InitArgs *system_vm_args;

w_instance SystemClassLoader_getBootclasspath(w_thread thread, w_instance class) {
  return getStringInstance(cstring2String(bootclasspath, strlen(bootclasspath)));
}

void SystemClassLoader_setSystemClassLoader(w_thread thread, w_instance class, w_instance this) {
  setSystemClassLoader(this);
}

w_instance SystemClassLoader_getBootstrapFile(w_thread thread, w_instance theSystemClassLoader, w_instance filename) {
  w_bar bar;
  w_instance arrayInstance;
  w_string filename_string;
  char *filename_utf8;
  w_int len;

  threadMustBeSafe(thread);
  filename_string = String2string(filename);
  woempa(7, "filename_string = %w\n", filename_string);
  filename_utf8 = (char *)string2UTF8(filename_string, &len);
  woempa(7, "filename_utf8 = %s\n", filename_utf8 + 2);
  bar = getBootstrapFile(filename_utf8 + 2);
  releaseMem(filename_utf8);
  if (bar) {
    enterUnsafeRegion(thread);
    arrayInstance = allocArrayInstance_1d(thread, atype2clazz[P_byte], bar->length);
    enterSafeRegion(thread);
    // TODO: bar_read returns 1 on success, 0 on failure; last param is bytes read
    bar_read(bar,instance2Array_byte(arrayInstance), bar->length, NULL);
    // WAS : memcpy(instance2Array_byte(arrayInstance), bar->buffer, bar->length);

    return arrayInstance;
  }

  releaseMem(filename_utf8);

  return NULL;
}

