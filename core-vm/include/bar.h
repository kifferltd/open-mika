#ifndef _BAR_H
#define  _BAR_H
/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
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
#include "ts-mem.h"

/*
** Byte array reader is a fast version of the byte array device.
** It only implements the read function, and is kept on the stack
**
** Uses fast Duffs memcpy and a special case for one byte (much used)
*/

typedef struct w_BAR {
  w_ubyte *buffer;
  w_int   length;
  w_int   current;
} w_BAR;

/*
** returns 1 if ok
** returns 0 if data exhausted
*/
static inline w_int bar_read(w_bar bar, w_ubyte *bytes, w_int length, w_int *lread) {
  w_int  status = 1;
  w_ubyte *source,*dest;
  w_int duffs;

  dest = bytes;
  source = bar->buffer + bar->current;

  // almost always runs byte per byte, so I have a special case for single byte

  if (bar->current < bar->length) {
    if (length == 1) {
      *dest = *source;
      bar->current += 1;
      *lread += 1;
    }
    else {
      if (length > (bar->length - bar->current)) {
        length = bar->length - bar->current;
      }

      duffs = (length + 7) / 8;
      switch (length % 8) {
        case 0: do { *dest++ = *source++;
        case 7:      *dest++ = *source++;
        case 6:      *dest++ = *source++;
        case 5:      *dest++ = *source++;
        case 4:      *dest++ = *source++;
        case 3:      *dest++ = *source++;
        case 2:      *dest++ = *source++;
        case 1:      *dest++ = *source++;
                } while (--duffs > 0);
      }

      bar->current = source - bar->buffer;
      *lread = dest - bytes;
    }
  }
  else {
    woempa(9, "BAR EXHAUSTED !! \n");
    status = 0;
  }

  return status;
}

/**
 ** Returns number of bytes which can still be read.
 */
static inline w_int bar_avail(w_bar bar) {
  return bar->length - bar->current;
}

/**
 ** Adds 'length' to current offset. Does not check for overrun!
 */
static inline void bar_skip(w_bar bar, w_int length) {
  bar->current += length;
}

/**
 ** Sets current offset to 'offset'.
 */
static inline void bar_seek(w_bar bar, w_int offset) {
  bar->current = offset;
}

#endif
