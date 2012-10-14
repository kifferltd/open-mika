/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
* Modified by Chris Gray 2012.                                            *
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


/************************************************************
 *                                                          *
 *              Rudolph, the red-nosed rendeer              *
 *                                                          *
 ************************************************************
   $Id: rudolph.h,v 1.2 2006/05/14 08:34:30 cvs Exp $
 */

#ifndef _RUDOLPH_H
#define _RUDOLPH_H

#include "oswald.h"
#include "wonka.h"
#include "jni.h"

/*
** Global variables:
*/

extern x_monitor  tree_lock;
extern w_instance defaultFont;
extern w_instance defaultEventQueue;
extern JNIEnv *environment;

/*
** Defines and typedefs:
*/

#define ALIGNMENT_LEFT                  0x00
#define ALIGNMENT_CENTER                0x01
#define ALIGNMENT_RIGHT                 0x02
  // Note: has to match the alignments in java.awt.Label!
  
#define R_WHITE               (r_pixel)(0 - 1)
  // to be system independent
  
#define Z_CHECKBOX            0x02
#define Z_COMPONENT           0x03
#define Z_CONTAINER           0x04
#define Z_IMAGE               0x05
#define Z_FONT                0x06
#define Z_FRAME               0x07
#define Z_SCROLLPANE          0x09

typedef struct r_Bitmap * r_bitmap;  
typedef struct r_Buffer * r_buffer;
typedef struct r_Canvas * r_canvas;
typedef struct r_Char * r_char;
typedef struct r_Component * r_component;
typedef struct r_Event * r_event;
typedef struct r_Font * r_font;
typedef struct r_ImageXpm * r_imageXpm;
typedef struct r_ImagePng * r_imagePng;
typedef struct r_Image * r_image;
typedef struct r_Screen * r_screen;
typedef struct r_XpmColor * r_xpmColor;

typedef unsigned int r_color;
typedef unsigned int r_Tag;

#ifdef AWT_SWAPDISPLAY
  static const int swap_display = 1;
#else 
  static const int swap_display = 0;
#endif

#define MAX_SIZE 1000000

// Disabling this saves a bit of code size, but not much
#define AWT_VIRTUAL_SCREEN_SUPPORT

// Return code values for java.awt.image.BufferedImage.getType()
#define AWT_IMAGE_TYPE_CUSTOM           0
#define AWT_IMAGE_TYPE_INT_RGB          1
#define AWT_IMAGE_TYPE_INT_ARGB         2
#define AWT_IMAGE_TYPE_INT_ARGB_PRE     3
#define AWT_IMAGE_TYPE_INT_BGR          4
#define AWT_IMAGE_TYPE_3BYTE_BGR        5
#define AWT_IMAGE_TYPE_4BYTE_ABGR       6
#define AWT_IMAGE_TYPE_4BYTE_ABGR_PRE   7
#define AWT_IMAGE_TYPE_USHORT_565_RGB   8
#define AWT_IMAGE_TYPE_USHORT_555_RGB   9
#define AWT_IMAGE_TYPE_BYTE_GRAY        10
#define AWT_IMAGE_TYPE_USHORT_GRAY      11
#define AWT_IMAGE_TYPE_BYTE_BINARY      12
#define AWT_IMAGE_TYPE_BYTE_INDEXED     13

#endif
