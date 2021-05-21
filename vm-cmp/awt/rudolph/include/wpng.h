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



// this is an 32 bit RGBA image
typedef struct w_png_Image {
  w_int width;
  w_int height;
  w_ubyte *data;
} w_png_Image;

typedef struct w_png_Image * w_png_image;

// TODO: memory is now allocated in the system thread because it is 
//       not stored by the application, just copied, and then freed.
//       If one day the w_png_Image is used inside Rudolph, memory 
//       should be allocated in the user thread, so a thread should be
//       passed also.
w_png_image w_png_read(w_ubyte *mem, w_int length);
