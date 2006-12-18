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


/* $Id: PNGImageSource.c,v 1.1 2005/06/14 08:48:25 cvs Exp $ */

#include "arrays.h"        // Array stuff
#include "fields.h"

#include "awt-classes.h"
#include "wpng.h"          // PNG reader

w_void PNGImageSource_readImage(JNIEnv *env, jobject thisObject, jbyteArray imagedata) {
  
  w_thread     thread = currentWonkaThread;
  w_png_image  p;
  w_ubyte      *start = instance2Array_byte(imagedata);
  w_int        *result;
  w_int        length = instance2Array_length(imagedata);
  w_instance   Array;

  // return rgba image
  p = w_png_read(start, (signed) length);

  if(p == NULL || p->data == NULL) {
    woempa(10, "WARNING: could not load PNG image.\n");
    return;
  }

  // Initialize PNGImageSource where possible:
  setIntegerField(thisObject, F_PNGImageSource_width, p->width);
  setIntegerField(thisObject, F_PNGImageSource_height, p->height);

  length = p->width * p->height;
  Array = allocArrayInstance(thread, atype2clazz[P_int], 1, &length);

  if (Array) {
    setReferenceField(thisObject, Array, F_PNGImageSource_pixels);
    result = instance2Array_int(Array);
    w_memcpy(result, p->data, (w_word)(p->width * p->height * 4));
// Not needed    (*env)->DeleteLocalRef(env, Array);
  }
  else {
    woempa(10, "WARNING: could not allocate array to hold image data.\n");
  }
  
  // Release memory:
  releaseMem(p->data);
  releaseMem(p);  
  
  return;
}

