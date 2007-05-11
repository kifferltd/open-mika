#ifndef _BAR_H
#define  _BAR_H
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
