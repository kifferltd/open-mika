/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved.                                                               *
* Parts copyright (c) 2004 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

#include "ts-mem.h"
#include "wonka.h"

/*
** If 'pedantic' is WONKA_TRUE, Wonka will detect errors such as AbstractMethodError
** even if the offending method is never called at runtime.
*/
w_boolean pedantic;

w_instance                    SystemClassLoader;

/*
#ifdef LINUX
#define UART0 0
#define BAUD_115200 0
void InitCom0(int p, int s) {
  PutString ((char*)"BANG BANG-A-BANG\r\n");
}

#endif
*/

w_word *crc_table;

/*
** Calculate the CCITT CRC16 checksum. The code is taken from Appendix C, of the document,
** prENV 278/4/7/011-2, B/TPEG "TPEG Specification - Part 2, Syntax, Semantics and Framing Structure
** Version 1.2 (November 1999)."
**
** This document can be found on the EBU web site, http://www.ebu.ch
**
** The polynomial used is: x^16 + x^12 + x^5 + 1
*/

#define CCITT_swap(a)           (((a) << 8) | ((a) >> 8))

w_ushort CCITT_16(w_ubyte *data, w_size length) {

  w_ushort crc = 0xffff;
  w_size i;
  
  for (i = 0; i < length; i++) {
    crc = (w_ushort)(CCITT_swap(crc) ^ (w_ushort)(*data++));
    crc ^= ((w_ubyte)(crc) >> 4);
    crc = (w_ushort)(crc ^ (CCITT_swap((w_ubyte)(crc)) << 4) ^ ((w_ubyte)(crc) << 5));
  }
  
  return crc ^ 0xffff;

}

/*
** The CRC32 code (taken from RFC2083: PNG Specification)
*/

/* 
** Make the table for a fast CRC. 
*/
w_void make_ISO3309_CRC_table(w_void) {
  w_word c;
  w_int n, k;
  crc_table = allocMem(256 * sizeof(w_word));
  for (n = 0; n < 256; n++) {
    c = n;
    for (k = 0; k < 8; k++) {
      if (c & 1) {
        c = 0xedb88320L ^ (c >> 1);
      }
      else {
        c = c >> 1;
      }
      crc_table[n] = c;
    }
  }
}

/*
** Release the table
*/
w_void release_ISO3309_CRC_table(w_void) {
  woempa(1, "Releasing ISO3309 CRC table at %p\n", crc_table);
  releaseMem(crc_table);
}

/* 
** Update a running CRC with the bytes buf[0..len-1]--the CRC
** should be initialized to all 1's, and the transmitted value
** is the 1's complement of the final running CRC (see the
** crc() routine below)).
*/
w_word update_ISO3309_CRC(w_word crc, w_ubyte *buf, w_size len) {
  w_word c = crc;
  w_size n;

  for (n = 0; n < len; n++) {
    c = crc_table[(c ^ buf[n]) & 0xff] ^ (c >> 8);
  }
  return c;
}

/* 
** Return the CRC of the bytes buf[0..len-1]. 
*/

w_word CCITT_32(w_ubyte *buf, w_size len) {
  return update_ISO3309_CRC(0xffffffffL, buf, len) ^ 0xffffffffL;
}

int w_strcmp(const char *p1, const char *p2) {

  unsigned char c1;
  unsigned char c2;
  unsigned char *s1 = (unsigned char *) p1;
  unsigned char *s2 = (unsigned char *) p2;
         
  do {
    c1 = (unsigned char) *s1++;
    c2 = (unsigned char) *s2++;
    if (c1 == '\0') {
      return c1 - c2;
    }
  } while (c1 == c2);

  return c1 - c2;

}

#ifdef USE_W_MEMCPY
/*
** Our own internal version of memcpy. Does not return a pointer and
** is properly unrolled AND it guarantees alignment since it works
** on bytes; this function can thus replace safely all occurences of
** memcpy and memmove that don't use the return value of the 'normal'
** functions.
*/
                                       
inline static void bytecopy(void * adst, const void * asrc, w_size length) {

  w_ubyte * dst = adst;
  const w_ubyte * src = asrc;
  w_int duffs = (length + 31) / 32;

  switch (length & 0x1f) {
    default:
    case  0: do { *dst++ = *src++;
    case 31:      *dst++ = *src++;
    case 30:      *dst++ = *src++;
    case 29:      *dst++ = *src++;
    case 28:      *dst++ = *src++;
    case 27:      *dst++ = *src++;
    case 26:      *dst++ = *src++;
    case 25:      *dst++ = *src++;
    case 24:      *dst++ = *src++;
    case 23:      *dst++ = *src++;
    case 22:      *dst++ = *src++;
    case 21:      *dst++ = *src++;
    case 20:      *dst++ = *src++;
    case 19:      *dst++ = *src++;
    case 18:      *dst++ = *src++;
    case 17:      *dst++ = *src++;
    case 16:      *dst++ = *src++;
    case 15:      *dst++ = *src++;
    case 14:      *dst++ = *src++;
    case 13:      *dst++ = *src++;
    case 12:      *dst++ = *src++;
    case 11:      *dst++ = *src++;
    case 10:      *dst++ = *src++;
    case  9:      *dst++ = *src++;
    case  8:      *dst++ = *src++;
    case  7:      *dst++ = *src++;
    case  6:      *dst++ = *src++;
    case  5:      *dst++ = *src++;
    case  4:      *dst++ = *src++;
    case  3:      *dst++ = *src++;
    case  2:      *dst++ = *src++;
    case  1:      *dst++ = *src++;
            } while (--duffs > 0);
  }

}

inline static void shortcopy(void * adst, const void * asrc, w_size length) {

  w_ushort * dst = adst;
  const w_ushort * src = asrc;
  w_int duffs = (length + 31) / 32;

  switch (length & 0x1f) {
    default:
    case  0: do { *dst++ = *src++;
    case 31:      *dst++ = *src++;
    case 30:      *dst++ = *src++;
    case 29:      *dst++ = *src++;
    case 28:      *dst++ = *src++;
    case 27:      *dst++ = *src++;
    case 26:      *dst++ = *src++;
    case 25:      *dst++ = *src++;
    case 24:      *dst++ = *src++;
    case 23:      *dst++ = *src++;
    case 22:      *dst++ = *src++;
    case 21:      *dst++ = *src++;
    case 20:      *dst++ = *src++;
    case 19:      *dst++ = *src++;
    case 18:      *dst++ = *src++;
    case 17:      *dst++ = *src++;
    case 16:      *dst++ = *src++;
    case 15:      *dst++ = *src++;
    case 14:      *dst++ = *src++;
    case 13:      *dst++ = *src++;
    case 12:      *dst++ = *src++;
    case 11:      *dst++ = *src++;
    case 10:      *dst++ = *src++;
    case  9:      *dst++ = *src++;
    case  8:      *dst++ = *src++;
    case  7:      *dst++ = *src++;
    case  6:      *dst++ = *src++;
    case  5:      *dst++ = *src++;
    case  4:      *dst++ = *src++;
    case  3:      *dst++ = *src++;
    case  2:      *dst++ = *src++;
    case  1:      *dst++ = *src++;
            } while (--duffs > 0);
  }

}

inline static void wordcopy(void * adst, const void * asrc, w_size length) {

  w_word * dst = adst;
  const w_word * src = asrc;
  w_int duffs = (length + 31) / 32;

  switch (length & 0x1f) {
    default:
    case  0: do { *dst++ = *src++;
    case 31:      *dst++ = *src++;
    case 30:      *dst++ = *src++;
    case 29:      *dst++ = *src++;
    case 28:      *dst++ = *src++;
    case 27:      *dst++ = *src++;
    case 26:      *dst++ = *src++;
    case 25:      *dst++ = *src++;
    case 24:      *dst++ = *src++;
    case 23:      *dst++ = *src++;
    case 22:      *dst++ = *src++;
    case 21:      *dst++ = *src++;
    case 20:      *dst++ = *src++;
    case 19:      *dst++ = *src++;
    case 18:      *dst++ = *src++;
    case 17:      *dst++ = *src++;
    case 16:      *dst++ = *src++;
    case 15:      *dst++ = *src++;
    case 14:      *dst++ = *src++;
    case 13:      *dst++ = *src++;
    case 12:      *dst++ = *src++;
    case 11:      *dst++ = *src++;
    case 10:      *dst++ = *src++;
    case  9:      *dst++ = *src++;
    case  8:      *dst++ = *src++;
    case  7:      *dst++ = *src++;
    case  6:      *dst++ = *src++;
    case  5:      *dst++ = *src++;
    case  4:      *dst++ = *src++;
    case  3:      *dst++ = *src++;
    case  2:      *dst++ = *src++;
    case  1:      *dst++ = *src++;
            } while (--duffs > 0);
  }

}

void w_memcpy(void * adst, const void * asrc, w_size length) {

  w_ubyte *  dst = adst;
  const w_ubyte *  src = asrc;
  w_int      duffs;

  if (length) {
    if (src < dst && dst < src + length) {
      dst += length;
      src += length;
      duffs = (length + 31) / 32;
      switch (length & 0x1f) {
        default:
        case  0: do { *--dst = *--src;
        case 31:      *--dst = *--src;
        case 30:      *--dst = *--src;
        case 29:      *--dst = *--src;
        case 28:      *--dst = *--src;
        case 27:      *--dst = *--src;
        case 26:      *--dst = *--src;
        case 25:      *--dst = *--src;
        case 24:      *--dst = *--src;
        case 23:      *--dst = *--src;
        case 22:      *--dst = *--src;
        case 21:      *--dst = *--src;
        case 20:      *--dst = *--src;
        case 19:      *--dst = *--src;
        case 18:      *--dst = *--src;
        case 17:      *--dst = *--src;
        case 16:      *--dst = *--src;
        case 15:      *--dst = *--src;
        case 14:      *--dst = *--src;
        case 13:      *--dst = *--src;
        case 12:      *--dst = *--src;
        case 11:      *--dst = *--src;
        case 10:      *--dst = *--src;
        case  9:      *--dst = *--src;
        case  8:      *--dst = *--src;
        case  7:      *--dst = *--src;
        case  6:      *--dst = *--src;
        case  5:      *--dst = *--src;
        case  4:      *--dst = *--src;
        case  3:      *--dst = *--src;
        case  2:      *--dst = *--src;
        case  1:      *--dst = *--src;
                } while (--duffs > 0);
      }
    }
    else {
      if (! ((length & 0x3) | ((w_size) dst & 0x3) | ((w_size) src & 0x3))) {
        wordcopy(adst, asrc, length >> 2);
      }
      else if (! ((length & 0x1) | ((w_size) dst & 0x1) | ((w_size) src & 0x1))) {
        shortcopy(adst, asrc, length >> 1);
      }
      else {
        bytecopy(adst, asrc, length);
      }
    }
  }

}
#endif

