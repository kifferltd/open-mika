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

#include "clazz.h"
#include "arrays.h"
#include "exception.h"
#include "heap.h"
#include "core-classes.h"


void Adler32_update(w_thread thread, w_instance thisAdler32, w_instance byteArray, w_int off, w_int len) {
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

