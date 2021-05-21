/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved. Parts copyright (c) 2006, 2012 by Chris Gray, /k/ Embedded    *
* Java Solutions. All rights reserved.                                    *
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

#ifndef _PIXEL_H
#define _PIXEL_H

#include "rudolph.h"

#define XSIM_HACK_BPP_24 24

typedef u_int32_t r_pixel;

#define pixels2bytes(p) (size_t)((p)*3)

#define bytes2pixels(b) ((b)/3)

#define pixel2red(p)		\
  (int)(((p) >> 16) & 0xff)

#define pixel2green(p)		\
  (int)(((p) >> 8) & 0xff)

#define pixel2blue(p)		\
  (int)((p) & 0xff)

#define rgb2pixel(r, g, b) 	\
  ((((r) & 0xff) << 16) | (((g) & 0xff) << 8) | ((b) & 0xff))

#define color2pixel(c)		\
  ((getIntegerField(c, F_Color_value)) & 0xffffff)

#if __BYTE_ORDER == __LITTLE_ENDIAN
static __inline__ void pixelset(r_pixel c, char *b, w_int w, w_int x, w_int y) {
  r_pixel p = c;

  memcpy(b + pixels2bytes(w * y + x), &p, 3);
}

static __inline__ r_pixel pixelget(char *b, w_int w, w_int x, w_int y) {
  r_pixel p = 0;

  memcpy(&p, b+pixels2bytes(w * y+ x), 3);

  return p;
}
#else
#error Sorry, 24-bit packed-pixel format not yet supported on big-endian CPUs.
#endif

static __inline__ void
pixelcopyline(w_ubyte *dstline, w_ubyte *srcline, int dstx, int srcx, int pixlen) {
    memcpy(dstline + pixels2bytes(dstx), srcline + pixels2bytes(srcx), pixels2bytes(pixlen));
}

// TODO Bit of a guess
#define AWT_IMAGE_TYPE AWT_IMAGE_TYPE_3BYTE_BGR

#endif


