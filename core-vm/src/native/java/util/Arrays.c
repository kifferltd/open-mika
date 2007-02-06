/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
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
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: Arrays.c,v 1.2 2004/11/18 23:51:52 cvs Exp $
*/

#include "core-classes.h"
#include "clazz.h"
#include "heap.h"
#include "ts-mem.h"
#include "threads.h"
#include "wstrings.h"
#include "arrays.h"
#include "exception.h"

#include <string.h>

/*
** Boolean
*/

void fill_bool(JNIEnv *env, w_instance thisClazz, w_instance array, w_boolean value) {
  if(array) {
    w_int length = instance2Array_length(array);
    w_ubyte *data = instance2Array_byte(array);
    if(value) {
      memset(data, 0xFF, (length + 7) / 8);
    }
    else {
      memset(data, 0x00, (length + 7) / 8);
    }
  }
  else {
    throwException(JNIEnv2w_thread(env), clazzNullPointerException, NULL);
  }
}


/*
** Byte
*/

void fill_byte(JNIEnv *env, w_instance thisClazz, w_instance array, w_sbyte value) {
  if(array) {
    w_int length = instance2Array_length(array);
    w_sbyte *data = instance2Array_byte(array);
    memset(data, (w_int)value & 0xff, length);
  }
  else {
    throwException(JNIEnv2w_thread(env), clazzNullPointerException, NULL);
  }
}

void fill_byte_range(JNIEnv *env, w_instance thisClazz, w_instance array, w_int from, w_int to, w_sbyte value) {
  if(array) {
    w_int length = instance2Array_length(array);
    w_sbyte *data = instance2Array_byte(array);
    
    if(from < 0 || to < 0 || from > length || to > length) {
      throwException(JNIEnv2w_thread(env), clazzArrayIndexOutOfBoundsException, NULL);
    }
    else if(from > to) {
      throwException(JNIEnv2w_thread(env), clazzIllegalArgumentException, NULL);
    }
    else {
      memset(data + from, (w_int)value & 0xff, to - from);
    }
  }
  else {
    throwException(JNIEnv2w_thread(env), clazzNullPointerException, NULL);
  }
}

