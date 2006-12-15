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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


/* $Id: Image.h,v 1.1 2005/06/14 08:48:24 cvs Exp $ */

#ifndef _IMAGE
#define _IMAGE

#include "rudolph.h"
 
typedef struct r_XpmColor {
  long value;
  int red;
  int green;
  int blue;
} r_XpmColor;

typedef struct r_Bitmap {
  int x;
  int y;
  int width;
  int height;
  int color;
  unsigned char *data;
  r_component component;
} r_Bitmap;

typedef struct r_ImageXpm {
  int x;
  int y;
  unsigned char *data;
  r_component component;
} r_ImageXpm;

typedef struct r_ImagePng {
  int x;
  int y;
  int width;
  int height;
  int colors;
  r_component component;
} r_ImagePng;

typedef struct r_Image {
  r_component component; 

  char *filename;
   
  int x;
  int y;
  int w;
  int h; 

  r_buffer buffer;   
} r_Image;

w_boolean Image_drawImage(r_buffer dst_buffer, r_image image, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2);
void Image_drawBmpImage(r_buffer buffer, unsigned char *bitmap, int x, int y, int w, int h, int c);
void Image_loadXpmImage(r_image image);
r_image Image_loadPngImage(w_ubyte *data, w_size length);

#endif
