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

#define XSIM_HACK_BPP_16 16

typedef unsigned short r_pixel;

#define pixels2bytes(p) (size_t)((p)*2)

#define bytes2pixels(b) ((b)/2)

#define pixel2red(p)		\
  (int)(((p) >> 7) & 0xf8)

#define pixel2green(p)		\
  (int)(((p) >> 2) & 0xf8)

#define pixel2blue(p)		\
  (int)(((p) << 3) & 0xf8)

#define rgb2pixel(r, g, b) 	\
  ((((r) & 0xf8) << 7) | (((g) & 0xf8) << 2) | (((b) & 0xf8) >> 3))

#define color2pixel(c)		\
  ((((getIntegerField(c, F_Color_value)) & 0xf8) >> 3) | (((getIntegerField(c, F_Color_value)) & 0xf800) >> 6) | (((getIntegerField(c, F_Color_value)) & 0xf80000) >> 9))

#define pixelset(c, b, w, x, y) \
  *( (r_pixel*) ((b)+pixels2bytes((w)*(y)+x)) ) = (c)

#define pixelget(b, w, x, y) \
  *( (r_pixel*) ((b)+pixels2bytes((w)*(y)+x)) )

static __inline__ void
pixelcopyline(w_ubyte *dstline, w_ubyte *srcline, int dstx, int srcx, int pixlen) {
    w_memcpy(dstline + pixels2bytes(dstx), srcline + pixels2bytes(srcx), pixels2bytes(pixlen));
}

#endif

