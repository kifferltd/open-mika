/**************************************************************************
* Copyright (c) 2007 by Chris Gray, /k/ Embedded Java Solutions.          *
* All rights reserved.                                                    *
**************************************************************************/

#include "fifo.h"
#include "hashtable.h"
#include "jni.h"
#include "wstrings.h"

w_hashtable prop_hashtable;

w_instance keyArray;

w_instance NativeProperties_init(JNIEnv *env, w_instance classSystem) {
  char *utf8;
  w_string s;
  w_fifo fifo;
  w_int i;

  prop_hashtable = ht_create("hashtable:native system properties", 17, ht_stringHash, ht_stringCompare, 0, 0);
  woempa(1, "Created prop_hashtable\n");
  s = cstring2String(UNICODE_SUBSETS, strlen(UNICODE_SUBSETS));
  ht_write(prop_hashtable, (w_word)cstring2String("mika.unicode.subsets", 20), (w_word)s);
  woempa(1, "Set %s -> %w\n", "mika.unicode.subsets", s);

  utf8 = getInstallationDir();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("java.home", 9), (w_word)s);
  woempa(1, "Set %s -> %w\n", "java.home", s);

  utf8 = getExtensionDir();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("java.ext.dirs", 13), (w_word)s);
  woempa(1, "Set %s -> %w\n", "java.ext.dirs", s);

  utf8 = getLibraryPath();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("java.library.path", 17), (w_word)s);
  woempa(1, "Set %s -> %w\n", "java.library.path", s);

  s = utf2String(VERSION_STRING, strlen(VERSION_STRING));
  ht_write(prop_hashtable, (w_word)utf2String("mika.version", 12), (w_word)s);
  woempa(1, "Set %s -> %w\n", "mika.version", s);

  s = utf2String(WONKA_INFO, strlen(WONKA_INFO));
  ht_write(prop_hashtable, (w_word)utf2String("mika.vm.options", 15), (w_word)s);
  woempa(1, "Set %s -> %w\n", "mika.vm.options", s);

  s = utf2String(DEFAULT_HEAP_SIZE, strlen(DEFAULT_HEAP_SIZE));
  ht_write(prop_hashtable, (w_word)utf2String("mika.default.heap.size", 22), (w_word)s);
  woempa(1, "Set %s -> %w\n", "mika.default.heap.size", s);

  s = utf2String(AWT_INFO, strlen(AWT_INFO));
  ht_write(prop_hashtable, (w_word)utf2String("mika.awt.options", 16), (w_word)s);
  woempa(1, "Set %s -> %w\n", "mika.awt.options", s);

#ifdef O4P
  s = utf2String(O4P_INFO, strlen(O4P_INFO));
  ht_write(prop_hashtable, (w_word)utf2String("mika.o4p.options", 16), (w_word)s);
  woempa(1, "Set %s -> %w\n", "mika.o4p.options", s);
#endif

#ifdef OSWALD
  s = utf2String(OSWALD_INFO, strlen(OSWALD_INFO));
  ht_write(prop_hashtable, (w_word)utf2String("mika.oswald.options", 19), (w_word)s);
  woempa(1, "Set %s -> %w\n", "mika.oswald.options", s);
#endif

  s = utf2String(BUILD_HOST, strlen(BUILD_HOST));
  ht_write(prop_hashtable, (w_word)utf2String("mika.build.host", 15), (w_word)s);
  woempa(1, "Set %s -> %w\n", "mika.build.host", s);

  utf8 = __DATE__ " " __TIME__;
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("mika.build.time", 15), (w_word)s);
  woempa(1, "Set %s -> %w\n", "mika.build.time", s);

  utf8 = getOSName();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("os.name", 7), (w_word)s);
  woempa(1, "Set %s -> %w\n", "os.name", s);

  utf8 = getOSVersion();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("os.version", 10), (w_word)s);
  woempa(1, "Set %s -> %w\n", "os.version", s);

  utf8 = getOSArch();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("os.arch", 7), (w_word)s);
  woempa(1, "Set %s -> %w\n", "os.arch", s);

  utf8 = getUserName();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("user.name", 9), (w_word)s);
  woempa(1, "Set %s -> %w\n", "user.name", s);

  utf8 = getUserHome();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("user.home", 9), (w_word)s);
  woempa(1, "Set %s -> %w\n", "user.home", s);

  utf8 = getUserDir();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("user.dir", 8), (w_word)s);
  woempa(1, "Set %s -> %w\n", "user.dir", s);

  utf8 = getUserLanguage();
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("user.language", 13), (w_word)s);
  woempa(1, "Set %s -> %w\n", "user.language", s);

  utf8 = "Mika " VERSION_STRING;
  s = utf2String(utf8, strlen(utf8));
  ht_write(prop_hashtable, (w_word)utf2String("java.runtime.name", 17), (w_word)s);
  woempa(1, "Set %s -> %w\n", "java.runtime.name", s);

  keyArray = allocArrayInstance_1d(JNIEnv2w_thread(env), clazzArrayOf_String, prop_hashtable->occupancy);
  fifo = ht_list_keys_no_lock(prop_hashtable);
  i = 0;
  while ((s = (w_string)getFifo(fifo))) {
    woempa(7, "keyArray[%d] = %w\n", i, s);
    instance2Array_instance(keyArray)[i++] = getStringInstance(s);
  }

  return keyArray;
}

w_instance NativeProperties_get(JNIEnv *env, w_instance classSystem, w_instance nameString) {
  w_string name = String2string(nameString);
  w_string result;

  result = (w_string)ht_read(prop_hashtable, (w_word)name);

  return result ? getStringInstance(result) : NULL;
}

