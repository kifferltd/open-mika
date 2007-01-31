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
** $Id: CRC32.c,v 1.3 2006/10/04 14:24:17 cvsroot Exp $
*/

#include <string.h>

#include "clazz.h"
#include "arrays.h"
#include "exception.h"
#include "heap.h"
#include "core-classes.h"

void CRC32_update(JNIEnv *env, w_instance thisCRC32, w_instance byteArray, w_int off, w_int len) {
  w_thread thread = JNIEnv2w_thread(env);
  
  if (!byteArray) {
        throwException(thread, clazzNullPointerException, NULL);
  }
  else {
    if (off < 0 || len < 0 || off > instance2Array_length(byteArray) - len) {
        throwException(thread, clazzArrayIndexOutOfBoundsException, NULL);
    }
    else {
      w_sbyte * data = instance2Array_byte(byteArray);
      w_word crc = wordFieldPointer(thisCRC32, F_CRC32_crc)[WORD_LSW];
      data += off;
      wordFieldPointer(thisCRC32, F_CRC32_crc)[WORD_LSW] = update_ISO3309_CRC(crc, data, (w_size) len);
    }
  }
}

