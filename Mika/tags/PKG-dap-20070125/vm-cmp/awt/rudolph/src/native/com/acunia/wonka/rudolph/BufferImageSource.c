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


/* $Id: BufferImageSource.c,v 1.1 2005/06/14 08:48:25 cvs Exp $ */

#include <string.h>
#include <stdio.h>

#include "arrays.h"
#include "fields.h"

#include "awt-classes.h"
#include "canvas.h"
#include "Image.h"

w_void BufferImageSource_getImageData(JNIEnv *env, jobject thisObject, jobject imageInstance) { 

  w_instance Array;
  w_thread thread = currentWonkaThread;
  r_image  image = getWotsitField(imageInstance, F_Image_wotsit);
  w_int    w = image->w;
  w_int    h = image->h;
  w_int    length = w * h;
  w_int    *result;
  w_int    x;
  w_int    y;
 
  Array = allocArrayInstance(thread, atype2clazz[P_int], 1, &length);

  if (Array) {
    setReferenceField(thisObject, Array, F_BufferImageSource_pixels);
    result = instance2Array_int(Array);
    for (y = 0; y < h; y++) {
      for (x = 0; x < w; x++) {
        *result = pixelget(image->buffer->data, image->buffer->fw, x, y);
        result++;
      }
    }
// Not needed, surely?   (*env)->DeleteLocalRef(env, Array);
  }
  
  return;

}

