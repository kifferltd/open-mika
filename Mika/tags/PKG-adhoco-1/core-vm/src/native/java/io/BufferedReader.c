/**************************************************************************
* Copyright  (c) 2001, 2002 by Acunia N.V. All rights reserved.           *
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
*   Philips Site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: BufferedReader.c,v 1.1.1.1 2004/07/12 14:07:48 cvs Exp $
*/

#include <stdio.h>

#include "core-classes.h"
#include "arrays.h"

w_int BufferedReader_locateEnd(JNIEnv *env, w_instance this, w_instance Array, w_int start, w_int stop) {

  if(Array){
    w_int length = instance2Array_length(Array);
    w_char* chars  = instance2Array_char(Array);
    if(length >= stop && start >= 0){
      while(start < stop){
        if(chars[start] == 10 || chars[start] == 13){
          woempa(1, "found end-of-line character %02x at position[%d]\n", chars[start], start);
          return start;
        }
        start++;
      }
    }
  }

  woempa(1, "found no end-of-line character\n");

  return -1;
}

