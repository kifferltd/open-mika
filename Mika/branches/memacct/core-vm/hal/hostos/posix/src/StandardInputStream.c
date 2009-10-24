/**************************************************************************
* Copyright (c) 2003 by Acunia N.V. All rights reserved.                  *
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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: StandardInputStream.c,v 1.2 2005/01/08 18:48:09 cvs Exp $
*/

#include <unistd.h>
#include <stdio.h>
#include <errno.h>
#include <string.h>
#include <sys/ioctl.h>

#ifdef UNC20
#undef errno
#endif

#include "arrays.h"
#include "core-classes.h"
#include "exception.h"
#include "jni.h"

w_int StandardInputStream_read(JNIEnv *env, w_instance theStandardInputStream, w_instance byte_array_instance, w_int off, w_int len) {
  w_int result = read(0, instance2Array_byte(byte_array_instance) + off, len);
  while (result < 0 && errno == EINTR) {
    result = read(0, instance2Array_byte(byte_array_instance) + off, len);
  }
  if(result == 0) {
    return -1;
  }
  else if(result < 0) {
    throwException(JNIEnv2w_thread(env), clazzIOException, "stdin: read: %s", strerror(errno));
  }

  return result;
}

static char junk[256];

w_long StandardInputStream_skip(JNIEnv *env, w_instance theStandardInputStream, w_long l) {
  w_long result = 0;
  w_long remaining = l;
  w_int  available = 1;

  while(available != 0 && remaining > 256) {
    available = read(0, junk, 256);
    result += available;
    remaining -= available;
  }

  woempa(7, "remaining: %d, eof: %d\n", remaining, (available == 0));
  while(available != 0 && remaining) {
    woempa(7, "remaining: %d\n", remaining);
    available = read(0, junk, remaining);
    result += available;
    remaining -= available;
  }

  return result;
}

w_int StandardInputStream_available(JNIEnv *env, w_instance theStandardInputStream) {
  w_int result;

  if (ioctl(0, FIONREAD, &result) < 0) {
    throwException(JNIEnv2w_thread(env), clazzIOException, "stdin: ioctl(0, FIONREAD, &result) failed");

    return 0;

  }

  return result;
}


