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
** $Id: Adler32.c,v 1.3 2006/10/04 14:24:17 cvsroot Exp $
*/

#include <string.h>

#include "clazz.h"
#include "arrays.h"
#include "exception.h"
#include "heap.h"
#include "core-classes.h"


void Adler32_update(JNIEnv *env, w_instance thisAdler32, w_instance byteArray, w_int off, w_int len) {
  w_thread thread = JNIEnv2w_thread(env);
  w_int sum1 = getIntegerField(thisAdler32, F_Adler32_s1);
  w_int sum2 = getIntegerField(thisAdler32, F_Adler32_s2);
  
    if (!byteArray) {
          throwException(thread, clazzNullPointerException, NULL);
    }
  else {
      if (off < 0 || len < 0 || off > instance2Array_length(byteArray) - len) {
          throwException(thread, clazzArrayIndexOutOfBoundsException, NULL);
      }
          else {
            w_ubyte * data = (w_ubyte *) instance2Array_byte(byteArray);
            w_int loop=0;
            w_int count;  
            
            data += off;
      /**
      ** we want to avoid to do the modulo 65521 after every sum ...
      ** worst case scenario s1 == 65520 and all new bytes are 0xff == 255
      ** max int = 2147483647
      ** we should calculate the modulo thing once every 8000000 times for s1
      ** s2 = s1 + s2 , with s1 = 65520 + n * 255
      ** ==> s2 = (65520 + n * 255) + (65520 + (n-1) * 255 ) + ... + 65520
      ** ==> s2 = (n+1) * 65520 + 255 * (n^2 + n)
      ** ==> 2147483647 = 255 * n^2 + 65775 * n + 65520
      **  n^2 + 258 * n  - 8400000 = 0
      ** --> nmax ~ 2500
      ** ==> to be on the safe side we take n = 2000
      */
               while ( len > 0 ) {
                  
                  loop = (len > 2000 ? 2000 : len);
                  for (count=0 ; count < loop ; count++) {
                     sum1 += *(data++);
                     sum2 += sum1;
                  }
                  sum1 = sum1 % 65521;
                  sum2 = sum2 % 65521;
                  len -= loop;
               }
               setIntegerField(thisAdler32, F_Adler32_s1, sum1);
         setIntegerField(thisAdler32, F_Adler32_s2, sum2);
         }
  }  

}

