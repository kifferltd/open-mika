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
#include <string.h>  /* memcpy */

#define XSIM_HACK_BPP_8 8

typedef w_ubyte r_pixel;

#define pixels2bytes(p) (size_t)((p))

#define bytes2pixels(b) ((b))

/* return 3/3/2 bit r, g or b component of 8 bit pixelval*/
#define pixel2red(p)                    (((p) >> 5) & 0x07)

#define pixel2green(p) 	                (((p) >> 2) & 0x07)

#define pixel2blue(p)                   ((p) & 0x03)


/* create 8 bit 3/3/2 format pixel from RGB triplet*/
#define rgb2pixel(r, g, b)	\
  (((r) & 0xe0) | (((g) & 0xe0) >> 3) | (((b) & 0xc0) >> 6))

/* create 8 bit 3/3/2 format pixel from RGB colorval (0x00BBGGRR)*/
#define color2pixel(c)	\
  ((((getIntegerField(c, F_Color_value)) & 0xc0) >> 6) | (((getIntegerField(c, F_Color_value)) & 0xe000) >> 11) | (((getIntegerField(c, F_Color_value)) & 0xe00000) >> 16))

#define pixelset(c, b, w, x, y) \
  *( (r_pixel*) ((b)+pixels2bytes((w)*(y)+x)) ) = (c)

#define pixelget(b, w, x, y) \
  *( (r_pixel*) ((b)+pixels2bytes((w)*(y)+x)) )

static __inline__ void
pixelcopyline(w_ubyte *dstline, w_ubyte *srcline, int dstx, int srcx, int pixlen) {
  w_memcpy(dstline + pixels2bytes(dstx), srcline + pixels2bytes(srcx), pixels2bytes(pixlen));
}

#endif
