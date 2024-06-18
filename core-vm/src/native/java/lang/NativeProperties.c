/**************************************************************************
* Copyright (c) 2021, 2022 by KIFFER Ltd. All rights reserved.            *
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

#include "fifo.h"
#include "hashtable.h"
#include "jni.h"
#include "wstrings.h"

w_hashtable prop_hashtable;

w_instance keyArray;

w_instance NativeProperties_init(w_thread thread, w_instance classSystem) {
  char *utf8;
  w_string s;
  w_fifo fifo;
  w_int i;

  prop_hashtable = ht_create("hashtable:native system properties", 17, ht_stringHash, ht_stringCompare, 0, 0);
  woempa(7, "Created prop_hashtable\n");
  s = ascii2String(UNICODE_SUBSETS, strlen(UNICODE_SUBSETS));
  ht_write(prop_hashtable, (w_word)ascii2String("mika.unicode.subsets", 20), (w_word)s);
  woempa(7, "Set %s -> %w\n", "mika.unicode.subsets", s);

  utf8 = getInstallationDir();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("java.home", 9), (w_word)s);
  woempa(7, "Set %s -> %w\n", "java.home", s);

  utf8 = getExtensionDir();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("java.ext.dirs", 13), (w_word)s);
  woempa(7, "Set %s -> %w\n", "java.ext.dirs", s);

  utf8 = getLibraryPath();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("java.library.path", 17), (w_word)s);
  woempa(7, "Set %s -> %w\n", "java.library.path", s);

  s = utf2String(VERSION_STRING, strlen(VERSION_STRING));
  ht_write(prop_hashtable, (w_word)utf2String("mika.version", 12), (w_word)s);
  woempa(7, "Set %s -> %w\n", "mika.version", s);

  s = utf2String(WONKA_INFO, strlen(WONKA_INFO));
  ht_write(prop_hashtable, (w_word)utf2String("mika.vm.options", 15), (w_word)s);
  woempa(7, "Set %s -> %w\n", "mika.vm.options", s);

  s = utf2String(DEFAULT_HEAP_SIZE, strlen(DEFAULT_HEAP_SIZE));
  ht_write(prop_hashtable, (w_word)utf2String("mika.default.heap.size", 22), (w_word)s);
  woempa(7, "Set %s -> %w\n", "mika.default.heap.size", s);

  s = utf2String(AWT_INFO, strlen(AWT_INFO));
  ht_write(prop_hashtable, (w_word)utf2String("mika.awt.options", 16), (w_word)s);
  woempa(7, "Set %s -> %w\n", "mika.awt.options", s);

#ifdef O4P
  s = utf2String(O4P_INFO, strlen(O4P_INFO));
  ht_write(prop_hashtable, (w_word)utf2String("mika.o4p.options", 16), (w_word)s);
  woempa(7, "Set %s -> %w\n", "mika.o4p.options", s);
#endif

#ifdef OSWALD
  s = utf2String(OSWALD_INFO, strlen(OSWALD_INFO));
  ht_write(prop_hashtable, (w_word)utf2String("mika.oswald.options", 19), (w_word)s);
  woempa(7, "Set %s -> %w\n", "mika.oswald.options", s);
#endif

  s = utf2String(BUILD_HOST, strlen(BUILD_HOST));
  ht_write(prop_hashtable, (w_word)utf2String("mika.build.host", 15), (w_word)s);
  woempa(7, "Set %s -> %w\n", "mika.build.host", s);

  utf8 = __DATE__ " " __TIME__;
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("mika.build.time", 15), (w_word)s);
  woempa(7, "Set %s -> %w\n", "mika.build.time", s);

  utf8 = getOSName();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("os.name", 7), (w_word)s);
  woempa(7, "Set %s -> %w\n", "os.name", s);

  utf8 = getOSVersion();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("os.version", 10), (w_word)s);
  woempa(7, "Set %s -> %w\n", "os.version", s);

  utf8 = getOSArch();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("os.arch", 7), (w_word)s);
  woempa(7, "Set %s -> %w\n", "os.arch", s);

  utf8 = getUserName();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("user.name", 9), (w_word)s);
  woempa(7, "Set %s -> %w\n", "user.name", s);

  utf8 = getUserHome();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("user.home", 9), (w_word)s);
  woempa(7, "Set %s -> %w\n", "user.home", s);

  utf8 = getUserDir();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("user.dir", 8), (w_word)s);
  woempa(7, "Set %s -> %w\n", "user.dir", s);

  utf8 = getUserLanguage();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("user.language", 13), (w_word)s);
  woempa(7, "Set %s -> %w\n", "user.language", s);

  utf8 = "Mika " VERSION_STRING;
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("java.runtime.name", 17), (w_word)s);
  woempa(7, "Set %s -> %w\n", "java.runtime.name", s);

  enterUnsafeRegion(thread);
  keyArray = allocArrayInstance_1d(thread, clazzArrayOf_String, prop_hashtable->occupancy);
  enterSafeRegion(thread);
  fifo = ht_list_keys_no_lock(prop_hashtable);
  i = 0;
  while ((s = (w_string)getFifo(fifo))) {
    woempa(1, "keyArray[%d] = %w\n", i, s);
    setArrayReferenceField(keyArray, getStringInstance(s), i);
    ++i;
  }

  return keyArray;
}

w_instance NativeProperties_get(w_thread thread, w_instance classSystem, w_instance nameString) {
  w_string name = String2string(nameString);
  w_string result;

  result = (w_string)ht_read(prop_hashtable, (w_word)name);

  return result ? getStringInstance(result) : NULL;
}


