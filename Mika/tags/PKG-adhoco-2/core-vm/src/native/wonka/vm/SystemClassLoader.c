/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
*  distribution or disclosure of this software is expressly forbidden.    *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


/*
** $Id: SystemClassLoader.c,v 1.3 2006/06/09 09:51:06 cvs Exp $
*/

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
#include "threads.h"
#include "jni.h"

extern Wonka_InitArgs *system_vm_args;
extern w_boolean getBootstrapFile(char *filename, w_BAR *barptr);

w_instance SystemClassLoader_getBootclasspath(JNIEnv *env, w_instance class) {
  return newStringInstance(cstring2String(bootclasspath, strlen(bootclasspath)));
}

void SystemClassLoader_setSystemClassLoader(JNIEnv *env, w_instance class, w_instance this) {
  setSystemClassLoader(this);
}

w_instance SystemClassLoader_getBootstrapFile(JNIEnv *env, w_instance theSystemClassLoader, w_instance filename) {
  w_thread thread = JNIEnv2w_thread(env);
  w_BAR bar;
  w_instance arrayInstance;
  w_string filename_string;
  char *filename_utf8;
  w_int len;

  filename_string = String2string(filename);
  woempa(7, "filename_string = %w\n", filename_string);
  filename_utf8 = (char *)string2UTF8(filename_string, &len);
  woempa(7, "filename_utf8 = %s\n", filename_utf8 + 2);
  if (getBootstrapFile(filename_utf8 + 2, &bar)) {
    releaseMem(filename_utf8);
    arrayInstance = allocArrayInstance_1d(thread, atype2clazz[P_byte], bar.length);
    memcpy(instance2Array_byte(arrayInstance), bar.buffer, bar.length);

    return arrayInstance;
  }

  releaseMem(filename_utf8);

  return NULL;
}

