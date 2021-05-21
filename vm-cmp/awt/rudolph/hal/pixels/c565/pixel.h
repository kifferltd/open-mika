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

#define XSIM_HACK_BPP_16 16

typedef unsigned short r_pixel;

#define pixels2bytes(p) (size_t)((p)*2)

#define bytes2pixels(b) ((b)/2)

#define pixel2red(p)		\
  (int)(((p) >> 8) & 0xf8)

#define pixel2green(p)		\
  (int)(((p) >> 3) & 0xfc)

#define pixel2blue(p)		\
  (int)(((p) << 3) & 0xf8)

#define rgb2pixel(r, g, b) 	\
  ((((r) & 0xf8) << 8) | (((g) & 0xfc) << 3) | (((b) & 0xf8) >> 3))

#define color2pixel(c)		\
  ((((getIntegerField(c, F_Color_value)) & 0xf8) >> 3) | (((getIntegerField(c, F_Color_value)) & 0xfc00) >> 5) | (((getIntegerField(c, F_Color_value)) & 0xf80000) >> 8))

#define pixelset(c, b, w, x, y) \
  *( (r_pixel*) ((b)+pixels2bytes((w)*(y)+x)) ) = (c)

#define pixelget(b, w, x, y) \
  *( (r_pixel*) ((b)+pixels2bytes((w)*(y)+x)) )

static __inline__ void
pixelcopyline(w_ubyte *dstline, w_ubyte *srcline, int dstx, int srcx, int pixlen) {
    w_memcpy(dstline + pixels2bytes(dstx), srcline + pixels2bytes(srcx), pixels2bytes(pixlen));
}

#define AWT_IMAGE_TYPE AWT_IMAGE_TYPE_USHORT_565_RGB

#endif
