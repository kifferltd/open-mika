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

#endif
