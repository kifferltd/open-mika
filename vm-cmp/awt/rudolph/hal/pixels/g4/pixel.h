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

#define XSIM_HACK_BPP_4

typedef w_ubyte r_pixel;

#define pixels2bytes(p) (w_size)((p)/2)
#define bytes2pixels(b) ((b)*2)

#ifndef AWT_INVERSEDISPLAY
#  define pixel2red(p) (int)(p & 0xf)
#else
#  define pixel2red(p) (int)(15-(p & 0xf))
#endif
#define pixel2green(p) pixel2red(p)
#define pixel2blue(p) pixel2red(p)

#ifndef AWT_INVERSEDISPLAY
#  define rgb2pixel(r,g,b) (r_pixel)((r+g+b)/48) /* 0-767 ---> 0-15 */
#else
#  define rgb2pixel(r,g,b) (r_pixel)(767-((r+g+b)/48)) /* 0-767 ---> 15-0 */
#endif

#ifdef AWT_INVERSENIBBLES
#  define __pixelmask1 0xf0
#  define __pixelmask2 0x0f
#  define __pixelshift1 4
#  define __pixelshift2 0
#else
#  define __pixelmask1 0x0f
#  define __pixelmask2 0xf0
#  define __pixelshift1 0
#  define __pixelshift2 4
#endif

#define color2pixel(c) rgb2pixel(    \
  (getIntegerField((c), F_Color_value) & 0xff),       \
  ((getIntegerField((c), F_Color_value)>>8) & 0xff),  \
  ((getIntegerField((c), F_Color_value)>>16) & 0xff)  \
)

static __inline__ w_ubyte* 
pixeloff(w_ubyte *b, int w, int x, int y) {
  
  if (x < w)
      return b + pixels2bytes(w)*y + pixels2bytes(x);
  else
      return NULL; /* Cause a crash if this happens */
}

static __inline__  void
pixelset(int c, w_ubyte* b, int w, int x, int y) {
  w_ubyte *p = pixeloff(b, w, x, y);
  int odd = (x & 1);

  /*if (x == 0 || x == (w-1))
      woempa(9, "pixelset(%d, %08x, %d, %d, %d)\n", c, b, w, x, y);*/
  
  *p = (*p & (odd ? __pixelmask1  : __pixelmask2)) |
       (c << (odd ? __pixelshift2 : __pixelshift1));
  
  /*if (x == 0 || x == (w-1))
      woempa(9, "pixelset succeeded\n");*/
}

static __inline__ r_pixel
pixelget(w_ubyte* b, int w, int x, int y) {

  int odd = (x & 1);

  /*if (x == 0 || x == (w-1))
      woempa(9, "pixelget(%08x, %d, %d, %d)\n", b, w, x, y);*/

  return (*((r_pixel*)pixeloff(b, w, x, y)) & (odd ? __pixelmask2 : __pixelmask1)) 
    >> (odd ? __pixelshift2 : __pixelshift1);
}

static __inline__ void
pixelcopyline(w_ubyte *dstline, w_ubyte *srcline, int dstx, int srcx, int pixlen)
{
    dstline += pixels2bytes(dstx);
    srcline += pixels2bytes(srcx);
    
    if ((dstx & 1) == (srcx & 1))
    {
  if (dstx & 1)
  {
      pixelset(
        pixelget(srcline, 1, 1, 0),
        dstline, 1, 0, 0
      );
      pixlen--; dstline++; srcline++;
  }
  //printf("dstline = %p, srcline = %p, length = %d\n", dstline, srcline, pixels2bytes(pixlen));
  w_memcpy(dstline, srcline, pixels2bytes(pixlen));
  if (pixlen & 1)
    pixelset(
            pixelget(srcline+pixels2bytes(pixlen)+1, 1, 0, 0),
      dstline+pixels2bytes(pixlen)+1, 1, 0, 0
    );
    }
    else
    {
  int i;
  
  /* The slow way, for now: need to optimise to load words at a time,
  ** shift them, then store them back again.
  */
  
  for (i = 0; i < pixlen; i++)
    pixelset(
      pixelget(srcline, pixlen, i, 0),
      dstline, pixlen, i, 0
    ); 
    }
}

#define pixelfillbyte(c) ((c) & (c)<<4)

// Not really correct but the closest we could find ...
#define AWT_IMAGE_TYPE AWT_IMAGE_TYPE_BYTE_BINARY

#endif
