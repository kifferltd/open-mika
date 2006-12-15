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
* Modifications copyright (c) 2005, 2006 by Chris Gray, /k/ Embedded Java *
* Solutions. Permission is hereby granted to reproduce, modify, and       *
* distribute these modifications under the terms of the Wonka Public      *
* Licence.                                                                *
**************************************************************************/


/* $Id: Image.c,v 1.6 2006/10/04 14:24:20 cvsroot Exp $ */

#include <string.h>
#include <stdio.h>

#include "arrays.h"
#include "core-classes.h"
#include "exception.h"
#include "fields.h"

#include "awt-classes.h"
#include "canvas.h"
#include "platform.h"
#include "Component.h"    // Component_getGraphics
#include "core-classes.h" // clazzClassCastExceptioni
#include "arrays.h"
#include "heap.h"
#include "clazz.h"
#include "Image.h"

/*
** Create the r_image from a pixel array.
*/

w_void Image_createImage(JNIEnv *env, jobject thisObject) {
  r_image    image = getWotsitField(thisObject, F_Image_wotsit);

  if(image == NULL) {
    image = allocClearedMem(sizeof(r_Image)); 
    if (image == NULL) {
      woempa(5, "WARNING: could not allocate memory.\n");
      return;
    }
  }

  if(!getReferenceField(thisObject, F_Image_pixels)) {
    
    /*
    ** pixels == null -> release everything.
    */

    image->w = 0;
    image->h = 0;

    if(image->buffer) {
      image->buffer->fw = 0;
      image->buffer->fh = 0;
      if(image->buffer->data) {
        releaseRuBuDa(image->buffer->data);
        image->buffer->data = NULL;
      }
      if(image->buffer->alpha) {
        releaseMem(image->buffer->alpha);
        image->buffer->alpha = NULL;
      }
    }

    setWotsitField(thisObject, F_Image_wotsit, image);

    return;
  }
  
  // Initialize image where possible:
  image->w = getIntegerField(thisObject, F_Image_width);
  image->h = getIntegerField(thisObject, F_Image_height);

  // Allocate buffer:
  if(image->buffer == NULL) {
    image->buffer = allocClearedMem(sizeof(r_Buffer));
  }
  
  if (image->buffer == NULL) {
    releaseMem(image);
    woempa(5, "WARNING: could not allocate memory.\n");
    return;
  }
  else {
    if(image->buffer->data == NULL) {
      image->buffer->data = allocRuBuDa(pixels2bytes(image->w * image->h));
    }
  }
  
  if (image->buffer->data == NULL) {
    releaseMem(image->buffer);
    releaseMem(image);
    woempa(5, "WARNING: could not allocate memory.\n");
    return;
  }
  
  // Initialize buffer:
  image->buffer->fw = image->w;
  image->buffer->fh = image->h;

  w_memcpy(
    (char *)image->buffer->data, 
    (char *)instance2Array_char(getReferenceField(thisObject, F_Image_pixels)), 
    (w_word)pixels2bytes(image->h * image->w)
  );
     
  if(getReferenceField(thisObject, F_Image_alpha)) {

    /*
    ** Image has an alpha layer. Copy it to a native structure.
    */
   
    if(!image->buffer->alpha) {
      image->buffer->alpha = allocMem(image->w * image->h);
    }
    if(image->buffer->alpha) {
      w_memcpy(image->buffer->alpha, (char *)instance2Array_byte(getReferenceField(thisObject, F_Image_alpha)), image->w * image->h);
    }
  }

  setWotsitField(thisObject, F_Image_wotsit, image);
  setIntegerField(thisObject, F_Image_width, image->w);
  setIntegerField(thisObject, F_Image_height, image->h);

  return;
}

/*
** Simply return the graphics object created by the Component from which this image was derived.
*/

w_instance Image_getGraphics (JNIEnv *env, jobject thisImage) {

  w_thread thread = JNIEnv2w_thread(env);
  w_instance fontInstance = NULL;
  w_instance foregroundInstance = NULL;
  w_instance backgroundInstance = NULL;
  r_image image = (r_image)thisImage[F_Image_wotsit];
  r_component component;
  w_instance graphicalCntxt;

  if(getReferenceField(thisImage, F_Image_producer)) {
    throwException(JNIEnv2w_thread(env), clazzClassCastException, NULL);
    return NULL;
  }
  
  if(image == NULL) {
    woempa(9, "image == NULL.. This should never happen...\n");
    return NULL;
  }

  component = image->component;
  if(component == NULL) {
    woempa(9, "component == NULL.. This should never happen...\n");
    return NULL;
  }

  /*
  ** Allocate memory for graphical context:
  */
    
  graphicalCntxt = allocInstance(thread, clazzGraphics);

  if (graphicalCntxt) {
    w_method method = NULL;
    jclass   class_Graphics;
      
    /*
    ** Initialize graphical context:
    */
      
    setWotsitField(graphicalCntxt, F_Graphics_wotsit, component);
    setReferenceField(graphicalCntxt, component->instance, F_Graphics_component);

    class_Graphics = clazz2Class(clazzGraphics);
    method = (*env)->GetMethodID(env, class_Graphics, "<init>", "()V");
    
    if(!method) {
      wabort(ABORT_WONKA,"Graphics has no constructor ???\n");
    }

    (*env)->CallVoidMethod(env, graphicalCntxt, method);
    
    if ((*env)->ExceptionCheck(env)) {
      // wabort(ABORT_WONKA,"Game over 2.\n");
      // Leave the exception for the interpreter to handle...
    }

    Component_getFontInstance(component->parent, &fontInstance);
    setReferenceField(graphicalCntxt, fontInstance, F_Graphics_font);

    Component_getForeground(component->parent, &foregroundInstance, Component_getColor(F_SystemColor_windowText));
    setReferenceField(graphicalCntxt, foregroundInstance, F_Graphics_foreground);

    Component_getBackground(component->parent, &backgroundInstance, Component_getColor(F_SystemColor_window));
    setReferenceField(graphicalCntxt, backgroundInstance, F_Graphics_background);
  }
  else {
    woempa(9, "Unable to allocate graphicalCntxt!\n");
  }
  
  return graphicalCntxt;
  
}

jint Image_convertRGB (JNIEnv *env, w_instance ImageClass, jint r, jint g, jint b) {
  
  /* 
  ** Native so that the AWT classes aren't dependent on a particular Wonka
  ** build.
  */

  return rgb2pixel(r,g,b);
}

w_void Image_setPixel (JNIEnv *env, jobject this_image, jint x, jint y, jint scanlen, jint pixel) {
  w_ubyte *pixels = instance2Array_byte(getReferenceField(this_image, F_Image_pixels));
  pixelset(pixel, pixels, scanlen, x, y);
}

w_void Image_finalize0 (JNIEnv *env, jobject thisImage) {
  r_image image = getWotsitField(thisImage, F_Image_wotsit);

  woempa(5, "called Image_finalize0()\n");

  if (image) {
    clearWotsitField(thisImage, F_Image_wotsit);
    if (image->component) {
      releaseMem(image->component);
    }
  
    if (image->buffer) {
      if (image->buffer->data) {
        releaseRuBuDa(image->buffer->data);
      }
      
      if (image->buffer->alpha) {
        releaseMem(image->buffer->alpha);
      }
    
      releaseMem(image->buffer);
    }
  
    releaseMem(image);
  }
}

w_int Image_verifyCoordinate(r_buffer buffer, w_int x, w_int y) {
  if (x < 0 || y < 0 || x > buffer->fw || y > buffer->fh) {
    return 0;
  }
  else {
    return 1;
  }
}

w_int Image_verifyBounds(r_buffer buffer, r_image image) {
  if ((image->x + image->w > buffer->fw) || (image->y + image->h > buffer->fh)) {
    return 0;
  }
  else {
    return 1;
  }
}


/*
** This function just copies a subimage of an r_image to an r_canvas.  Boundary checks and clippinng are
** performed. Scaling but no flipping is performed. The number of columns and rows copied is determined by
** the number of columns and rows in the intersection of the rectangles (sx1, sy1, sx2, sy2) and
** (dx1, dy1, dx2, dy2).
*/

w_boolean Image_drawImage(r_buffer dst_buffer, r_image image, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2) {

  if (dx1 == 0 && dx2 == image->w - 1 && dy1 == 0 && dy2 == image->h - 1 && dst_buffer->fw == image->buffer->fw &&
      sx1 == 0 && sx2 == image->w - 1 && sy1 == 0 && sy2 == image->h - 1 && image->h <= dst_buffer->fh && !image->buffer->alpha) {

    /*
    ** Image and Graphics have the same size and the image is drawn at (0, 0)
    ** -> Take a shortcut
    */

    woempa(5, "Taking the shortcut\n");

    w_memcpy(dst_buffer->data, image->buffer->data, pixels2bytes(image->w * image->h));

    return (w_boolean)1;
  }
  else 
  if (!Image_verifyCoordinate(image->buffer, sx1, sy1) || !Image_verifyCoordinate(image->buffer, sx2, sy2)) {
    woempa(5, "WARNING: source coordinates (sx1,sy1)=(%d,%d), (sx2,sy2)=(%d,%d) out of source borders\n", sx1,sy1,sx2,sy2);
    return (w_boolean)0;
  }
  else if ((dx1 < dx2 ? dx1 : dx2) > dst_buffer->fw - 1 || (dy1 < dy2 ? dy1 : dy2) > dst_buffer->fh-1 || (dx1 < dx2 ? dx2 : dx1) < 0 || (dy1 < dy2 ? dy2 : dy1) < 0) {
    woempa(5, "WARNING: image position (dx1,dy1)=(%d,%d), (dx2,dy2)=(%d,%d) completely outside target (%d,%d)\n", dx1,dy1,dx2,dy2, dst_buffer->fw, dst_buffer->fh);
    return (w_boolean)0;
  }
  else {
    w_int i, j, k, l;
    w_int width;
    w_int height;
    r_buffer buffer = NULL;
    r_buffer drawbuffer = NULL;
    w_int *rows, *cols;
    
    w_int iw = image->w;           // Image width
    w_int ih = image->h;           // Image height
    w_int nw = abs(dx2 - dx1) + 1; // Destination width
    w_int nh = abs(dy2 - dy1) + 1; // Destination height
    w_int ow;
    w_int oh;
    
    w_ubyte *src;
    w_ubyte *dst;
   
    /*
    ** If some of the coordinates are switched, switch 'em back, we 
    ** don't support flipping images for the time being.
    */
    
    if (sx1 > sx2) {
      w_int tmp = sx1;
      sx1 = sx2;
      sx2 = tmp;
    }
    
    if (sy1 > sy2) {
      w_int tmp = sy1;
      sy1 = sy2;
      sy2 = tmp;
    }

    if (dx1 > dx2) {
      w_int tmp = dx1;
      dx1 = dx2;
      dx2 = tmp;
    }
    
    if (dy1 > dy2) {
      w_int tmp = dy1;
      dy1 = dy2;
      dy2 = tmp;
    }
    
    woempa(5, "calling coordinates: dx1 = %d, dy1 = %d, dx2 = %d, dy2 = %d, sx1 = %d, sy1 = %d, sx2 = %d, sy2=%d \n", dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2);

    /*
    ** Crop & Scale (& flip ??)
    */

    if(sx1 < 0) sx1 = 0;
    if(sy1 < 0) sy1 = 0;
    if(sx2 > iw - 1) sx2 = iw - 1;
    if(sy2 > ih - 1) sy2 = ih - 1;

    ow = abs(sx2 - sx1) + 1; // Source width
    oh = abs(sy2 - sy1) + 1; // Source height

    width  = nw;
    height = nh;

    if(iw != ow || ih != oh) {
      woempa(9, "DRAWIMAGE: Clipping is needed : (%d, %d) -> (%d, %d)\n", iw, ih, ow, oh);
    }
    
    if(nw != ow || nh != oh) {
      woempa(9, "DRAWIMAGE: Scaling is needed : (%d, %d) -> (%d, %d)\n", ow, oh, nw, nh);
      
      buffer = allocClearedMem(sizeof(r_Buffer));
      if(!buffer) {
        return 0;
      }
      
      buffer->data = allocRuBuDa(pixels2bytes(nw * nh));
      if(!buffer->data) {
        releaseMem(buffer);
        return 0;
      }
      
      buffer->fw = nw;
      buffer->fh = nh;
      buffer->vw = nw;
      buffer->vh = nh;
      buffer->ox = 0;
      buffer->oy = 0;
      
      rows = allocMem((oh + 1) * sizeof(w_int));
      if(!rows) {
        releaseRuBuDa(buffer->data);
        releaseMem(buffer);
        return 0;
      }
        
      cols = allocMem((ow + 1) * sizeof(w_int));
      if(!cols) {
        releaseRuBuDa(buffer->data);
        releaseMem(buffer);
        releaseMem(rows);
        return 0;
      }

      for(i=0; i <= ow; i++) cols[i] = (i * nw) / ow;
      for(i=0; i <= oh; i++) rows[i] = (i * nh) / oh;

      for(i=0; i < oh; i++) 
        for(j=0; j < ow; j++) 
          for(k=0; k < (rows[i + 1] - rows[i]); k++) 
            for(l=0; l < (cols[j + 1] - cols[j]); l++)
              pixelset(pixelget(image->buffer->data, image->buffer->fw, j+sx1, i+sy1), 
                       buffer->data, buffer->fw, (cols[j] - cols[0] + l), (rows[i] - rows[0] + k));
      
      if(image->buffer->alpha) {

        woempa(9, "DRAWIMAGE: Has alpha !!\n");
        
        /*
        ** Image has transparency -> also scale/crop the alpha layer.
        */
        
        buffer->alpha = allocMem(nw * nh);
        if(!buffer->alpha) {
          releaseRuBuDa(buffer->data);
          releaseMem(buffer);
          releaseMem(rows);
          releaseMem(cols);
          return 0;
        }

        for(i=0; i < oh; i++) 
          for(j=0; j < ow; j++) 
            for(k=0; k < (rows[i + 1] - rows[i]); k++) 
              for(l=0; l < (cols[j + 1] - cols[j]); l++)
                buffer->alpha[(rows[i] - rows[0] + k) * buffer->fw + cols[j] - cols[0] + l] = 
                  image->buffer->alpha[(i+sy1) * image->buffer->fw + (j+sx1)];
      }

      releaseMem(cols);
      releaseMem(rows);

      sx1 = 0;
      sy1 = 0;
      sx2 = nw - 1;
      sy2 = nh - 1;

    } /* SCALING */

    /*
    ** Check bounds
    */

    if (dx1 < 0) {
      sx1 = sx1 - dx1;
      dx1 = 0;
    }
    if (dy1 < 0) {
      sy1 = sy1 - dy1;
      dy1 = 0;
    }
    if (dx2 > dst_buffer->fw - 1) {
      sx2 = sx2 - (dx2 - dst_buffer->fw - 1) - 2;
      dx2 = dst_buffer->fw - 1;
    }
    if (dy2 > dst_buffer->fh - 1) {
      sy2 = sy2 - (dy2 - dst_buffer->fh - 1) - 2;
      dy2 = dst_buffer->fh - 1;
    }

    woempa(5, "adapted coordinates: dx1 = %d, dy1 = %d, dx2 = %d, dy2 = %d, sx1 = %d, sy1 = %d, sx2 = %d, sy2 = %d \n", dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2);

    width  = sx2 - sx1 + 1;
    height = sy2 - sy1 + 1;

    if(buffer) {
      drawbuffer = buffer; 
    }
    else {
      drawbuffer = image->buffer;
    }

    src = drawbuffer->data + pixels2bytes(sy1 * drawbuffer->fw);
    dst = dst_buffer->data + pixels2bytes(dy1 * dst_buffer->fw);
      
    if(drawbuffer->alpha) {
      r_pixel p1, p2, p3;
      w_ubyte *alpha = drawbuffer->alpha + (sy1 * drawbuffer->fw);

      for (j = sy1; j <= sy2; j++) {
        for (i = sx1; i <= sx2; i++) {
          if(alpha[i] == 255) {
            // Fully opaque.
            pixelset(pixelget(src, 0, i, 0), dst, 0, dx1 + i - sx1, 0);
          }
          else if(alpha[i] == 0) {
            // Do nothing.
          }
          else {
            // Mix'em.
            p1 = pixelget(src, 0, i, 0);
            p2 = pixelget(dst, 0, dx1 + i - sx1, 0);

            p3 = rgb2pixel( 
                (pixel2red(p1) * alpha[i] / 255) + (pixel2red(p2) * (255 - alpha[i]) / 255),
                (pixel2green(p1) * alpha[i] / 255) + (pixel2green(p2) * (255 - alpha[i]) / 255),
                (pixel2blue(p1) * alpha[i] / 255) + (pixel2blue(p2) * (255 - alpha[i]) / 255)
                );
            pixelset(p3, dst, 0, dx1 + i - sx1, 0);
          }
        }

        alpha += drawbuffer->fw;
        src += pixels2bytes(drawbuffer->fw);
        dst += pixels2bytes(dst_buffer->fw);
      }
    }
    else {
      for (j = sy1; j <= sy2; j++) {
        pixelcopyline(dst, src, dx1, sx1, width);
        src += pixels2bytes(drawbuffer->fw);
        dst += pixels2bytes(dst_buffer->fw);
      }
    }

    if(buffer) {
      if(buffer->alpha) releaseMem(buffer->alpha);
      releaseRuBuDa(buffer->data);
      releaseMem(buffer);
    }

    return (w_boolean)1;
  }

}

w_void Image_conversion_index(JNIEnv *env, jobject thisImage, jobject srcArray, jobject dstArray, 
                              jobject model, jint x, jint y, jint w, jint h, jint offset, jint scansize) {
  
  w_int      *internal = (w_int *)instance2Array_int(getReferenceField(model, F_IndexColorModel_internal));
  w_ubyte    *src = (w_ubyte *)instance2Array_byte(srcArray);
  w_ubyte    *dst = (w_ubyte *)instance2Array_char(getReferenceField(thisImage, F_Image_pixels));
  w_instance alpha_array = getReferenceField(thisImage, F_Image_alpha);
  
  w_ubyte    *alpha;
  w_int      trans = getIntegerField(model, F_IndexColorModel_trans);
  w_int      width = getIntegerField(thisImage, F_Image_width);
  w_int      i, j;
  w_ubyte    pixel;

  src += offset;
  dst += pixels2bytes(y * width + x);

  for(i=0; i<h; i++) {
    for(j=0; j<w; j++) {
      pixel = *src++;
      *(r_pixel *)dst  = internal[pixel];
      dst += pixels2bytes(1);
    }
    src += scansize - w;
    dst += pixels2bytes(width - (x + w));
  }

  if(trans != -1) {
    if(alpha_array == NULL) {
      alpha_array = (*env)->NewByteArray(env, getIntegerField(thisImage, F_Image_width) * getIntegerField(thisImage, F_Image_height));
      setReferenceField(thisImage, alpha_array, F_Image_alpha);
    }

    src = (w_ubyte *)instance2Array_byte(srcArray);
    src += offset;

    alpha = (w_ubyte *)instance2Array_byte(alpha_array);
    alpha += y * width + x;

    for(i=0; i<h; i++) {
      for(j=0; j<w; j++) {
        pixel = *src++;
        if(pixel == trans) {
          *alpha++ = 0;
        }
        else {
          *alpha++ = 255;
        }
      }
      src += scansize - w;
      alpha += width - (x + w);
    }
  }
}

w_void Image_conversion_direct(JNIEnv *env, jobject thisImage, jobject srcArray, jobject dstArray, 
                              jobject model, jint x, jint y, jint w, jint h, jint offset, jint scansize) {
  
  w_instance alpha_array = getReferenceField(thisImage, F_Image_alpha);
  w_word     *src = (w_word *)instance2Array_byte(srcArray);
  w_ubyte    *dst = (w_ubyte *)instance2Array_char(dstArray);
  w_ubyte    *alpha;
  w_int      width = getIntegerField(thisImage, F_Image_width);
  w_int      i, j;
  w_word     pixel;

  src += offset;
  dst += pixels2bytes(y * width + x);

  if(alpha_array == NULL) {
    alpha_array = (*env)->NewByteArray(env, getIntegerField(thisImage, F_Image_width) * getIntegerField(thisImage, F_Image_height));
    setReferenceField(thisImage, alpha_array, F_Image_alpha);
  }
    
  alpha = (w_ubyte *)instance2Array_byte(alpha_array);
  alpha += y * width + x;
    
  for(i=0; i<h; i++) {
    for(j=0; j<w; j++) {
      pixel = *src++;
      *(r_pixel *)dst = rgb2pixel((((pixel & getIntegerField(model, F_DirectColorModel_rmask)) >> getIntegerField(model, F_DirectColorModel_rmaskpos)) << 8) >> getIntegerField(model, F_DirectColorModel_rmaskbits),
                                  (((pixel & getIntegerField(model, F_DirectColorModel_gmask)) >> getIntegerField(model, F_DirectColorModel_gmaskpos)) << 8) >> getIntegerField(model, F_DirectColorModel_gmaskbits),
                                  (((pixel & getIntegerField(model, F_DirectColorModel_bmask)) >> getIntegerField(model, F_DirectColorModel_bmaskpos)) << 8) >> getIntegerField(model, F_DirectColorModel_bmaskbits));
      *alpha++ = (((pixel & getIntegerField(model, F_DirectColorModel_amask)) >> getIntegerField(model, F_DirectColorModel_amaskpos)) << 8) >> getIntegerField(model, F_DirectColorModel_amaskbits);
      dst += pixels2bytes(1);
    }
    src += scansize - w;
    dst += pixels2bytes(width - (x + w));
  }
}

w_void IndexColorModel_fill_internal(JNIEnv *env, jobject model) {
  w_ubyte *reds = (w_ubyte *)instance2Array_byte(getReferenceField(model, F_IndexColorModel_reds));
  w_ubyte *greens = (w_ubyte *)instance2Array_byte(getReferenceField(model, F_IndexColorModel_greens));
  w_ubyte *blues = (w_ubyte *)instance2Array_byte(getReferenceField(model, F_IndexColorModel_blues));
  w_int   *internal = (w_int *)instance2Array_int(getReferenceField(model, F_IndexColorModel_internal));
  w_int   i, size = getIntegerField(model, F_IndexColorModel_size);

  for(i=0; i < size; i++) {
    internal[i] = rgb2pixel(reds[i], greens[i], blues[i]);
  }

  return;
}

