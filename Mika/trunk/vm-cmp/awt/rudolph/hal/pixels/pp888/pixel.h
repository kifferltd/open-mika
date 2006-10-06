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
*                                                                         *
* Modifications copyright (c) 2006 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
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

#endif


