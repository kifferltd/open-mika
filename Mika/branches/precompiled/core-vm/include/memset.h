/**************************************************************************
* Copyright (c) 2001, 2003 by Punch Telematix. All rights reserved.       *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

#ifndef _MEMSET_H
#define _MEMSET_H

#define min(a, b) (a < b ? a : b)
#define max(a, b) (a > b ? a : b)

inline static void *inline_memset(void *address, unsigned int cbyte, unsigned int size) {

  unsigned int mask = cbyte & 0x000000ff;
  unsigned int prebytecopies   = min(4 - ((unsigned int)address % 4), size) % 4;
  unsigned int postbytecopies  = (size - prebytecopies) % 4;
  unsigned int wordcopies = max((int)size - (int)prebytecopies - (int)postbytecopies, 0) >> 2;
  unsigned int duffs = (wordcopies + 63) / 64;
  void *cursor = address;

  /*
  ** Prepare the mask
  */

  mask <<= 8;
  mask |= cbyte;
  mask <<= 8;
  mask |= cbyte;
  mask <<= 8;
  mask |= cbyte;

  /*
  ** Do byte copying (to get aligned)
  */

  for (; prebytecopies; prebytecopies--) {
    *((unsigned char *)cursor)++ = cbyte;
  }
  
  /*
  ** Do word copying
  */

  if(wordcopies) { 
    switch (wordcopies & 0x3f) {
      default:
      case  0: do { *((unsigned int *)cursor)++ = mask;
      case 63:      *((unsigned int *)cursor)++ = mask;
      case 62:      *((unsigned int *)cursor)++ = mask;
      case 61:      *((unsigned int *)cursor)++ = mask;
      case 60:      *((unsigned int *)cursor)++ = mask;
      case 59:      *((unsigned int *)cursor)++ = mask;
      case 58:      *((unsigned int *)cursor)++ = mask;
      case 57:      *((unsigned int *)cursor)++ = mask;
      case 56:      *((unsigned int *)cursor)++ = mask;
      case 55:      *((unsigned int *)cursor)++ = mask;
      case 54:      *((unsigned int *)cursor)++ = mask;
      case 53:      *((unsigned int *)cursor)++ = mask;
      case 52:      *((unsigned int *)cursor)++ = mask;
      case 51:      *((unsigned int *)cursor)++ = mask;
      case 50:      *((unsigned int *)cursor)++ = mask;
      case 49:      *((unsigned int *)cursor)++ = mask;
      case 48:      *((unsigned int *)cursor)++ = mask;
      case 47:      *((unsigned int *)cursor)++ = mask;
      case 46:      *((unsigned int *)cursor)++ = mask;
      case 45:      *((unsigned int *)cursor)++ = mask;
      case 44:      *((unsigned int *)cursor)++ = mask;
      case 43:      *((unsigned int *)cursor)++ = mask;
      case 42:      *((unsigned int *)cursor)++ = mask;
      case 41:      *((unsigned int *)cursor)++ = mask;
      case 40:      *((unsigned int *)cursor)++ = mask;
      case 39:      *((unsigned int *)cursor)++ = mask;
      case 38:      *((unsigned int *)cursor)++ = mask;
      case 37:      *((unsigned int *)cursor)++ = mask;
      case 36:      *((unsigned int *)cursor)++ = mask;
      case 35:      *((unsigned int *)cursor)++ = mask;
      case 34:      *((unsigned int *)cursor)++ = mask;
      case 33:      *((unsigned int *)cursor)++ = mask;
      case 32:      *((unsigned int *)cursor)++ = mask;
      case 31:      *((unsigned int *)cursor)++ = mask;
      case 30:      *((unsigned int *)cursor)++ = mask;
      case 29:      *((unsigned int *)cursor)++ = mask;
      case 28:      *((unsigned int *)cursor)++ = mask;
      case 27:      *((unsigned int *)cursor)++ = mask;
      case 26:      *((unsigned int *)cursor)++ = mask;
      case 25:      *((unsigned int *)cursor)++ = mask;
      case 24:      *((unsigned int *)cursor)++ = mask;
      case 23:      *((unsigned int *)cursor)++ = mask;
      case 22:      *((unsigned int *)cursor)++ = mask;
      case 21:      *((unsigned int *)cursor)++ = mask;
      case 20:      *((unsigned int *)cursor)++ = mask;
      case 19:      *((unsigned int *)cursor)++ = mask;
      case 18:      *((unsigned int *)cursor)++ = mask;
      case 17:      *((unsigned int *)cursor)++ = mask;
      case 16:      *((unsigned int *)cursor)++ = mask;
      case 15:      *((unsigned int *)cursor)++ = mask;
      case 14:      *((unsigned int *)cursor)++ = mask;
      case 13:      *((unsigned int *)cursor)++ = mask;
      case 12:      *((unsigned int *)cursor)++ = mask;
      case 11:      *((unsigned int *)cursor)++ = mask;
      case 10:      *((unsigned int *)cursor)++ = mask;
      case  9:      *((unsigned int *)cursor)++ = mask;
      case  8:      *((unsigned int *)cursor)++ = mask;
      case  7:      *((unsigned int *)cursor)++ = mask;
      case  6:      *((unsigned int *)cursor)++ = mask;
      case  5:      *((unsigned int *)cursor)++ = mask;
      case  4:      *((unsigned int *)cursor)++ = mask;
      case  3:      *((unsigned int *)cursor)++ = mask;
      case  2:      *((unsigned int *)cursor)++ = mask;
      case  1:      *((unsigned int *)cursor)++ = mask;
               } while (--duffs > 0);
    }
  }

  /*
  ** Do byte copying
  */

  for (; postbytecopies; postbytecopies--) {
    *((unsigned char *)cursor)++ = cbyte;
  }

  return address;
  
}

#endif /* _MEMSET_H */
