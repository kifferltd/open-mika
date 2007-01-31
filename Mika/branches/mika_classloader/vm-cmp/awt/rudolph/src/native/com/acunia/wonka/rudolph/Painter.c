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
*                                                                         *
* Modifications copyright (c) 2005 by Chris Gray, /k/ Embedded Java       *
* Solutions. Permission is hereby granted to reproduce, modify, and       *
* distribute these modifications under the terms of the Wonka Public      *
* Licence.                                                                *
**************************************************************************/

/* $Id: Painter.c,v 1.5 2006/05/27 20:24:51 cvs Exp $ */

#include "exception.h"
#include "hashtable.h"
#include "loading.h"
#include "oswald.h"
#include "rudolph.h"
#include "registry.h"
#include "dispatch.h"
#include "platform.h"
#include "awt-classes.h"
#include "canvas.h"
#include "registry.h"
#include "Component.h"

static void canvas_blitter(w_word *dst, w_word *src, w_size size) {

  w_int num_words = size / sizeof(w_word);
  w_int duffs = (num_words + 31) / 32;

  if (size) {
    switch (num_words & 0x1f) {
      default:
      case  0: do { *dst++ = *src++;
      case 31:      *dst++ = *src++;
      case 30:      *dst++ = *src++;
      case 29:      *dst++ = *src++;
      case 28:      *dst++ = *src++;
      case 27:      *dst++ = *src++;
      case 26:      *dst++ = *src++;
      case 25:      *dst++ = *src++;
      case 24:      *dst++ = *src++;
      case 23:      *dst++ = *src++;
      case 22:      *dst++ = *src++;
      case 21:      *dst++ = *src++;
      case 20:      *dst++ = *src++;
      case 19:      *dst++ = *src++;
      case 18:      *dst++ = *src++;
      case 17:      *dst++ = *src++;
      case 16:      *dst++ = *src++;
      case 15:      *dst++ = *src++;
      case 14:      *dst++ = *src++;
      case 13:      *dst++ = *src++;
      case 12:      *dst++ = *src++;
      case 11:      *dst++ = *src++;
      case 10:      *dst++ = *src++;
      case  9:      *dst++ = *src++;
      case  8:      *dst++ = *src++;
      case  7:      *dst++ = *src++;
      case  6:      *dst++ = *src++;
      case  5:      *dst++ = *src++;
      case  4:      *dst++ = *src++;
      case  3:      *dst++ = *src++;
      case  2:      *dst++ = *src++;
      case  1:      *dst++ = *src++;
              } while (--duffs > 0);
    }
  }
  
}

static inline w_word *canvas_transpose(w_ubyte *src, w_int width, w_int height) {
  w_ubyte *dst = allocMem(pixels2bytes(width * height));
  w_int  x, y;
  
  if (dst) {
    for (x=0; x < width; x++) {
      for (y=0; y < height; y++) {
        pixelset(pixelget(src, width, x, (height - (y + 1))), dst, height, y, x);
      }
    }
  }
  
  return (w_word *)dst;
}

w_int something_changed = 0;

void Painter_paint(JNIEnv *env, jobject thisObj) {
  r_component component;
  r_canvas canvas;
  w_ubyte *src;
  w_ubyte *dst;
  int i;
  int x;
  int y;
  int w;
  int h;
  int dx;
  int dy;
  w_thread thread = JNIEnv2w_thread(env);
    
  if(x_thread_priority_get(JNIEnv2w_thread(env)->kthread) != 5) {
     x_thread_priority_set(JNIEnv2w_thread(env)->kthread, 5);
  }

    if(rootCanvas->component->refresh) {
      rudolph_lock();
      canvas_drawCanvas(rootCanvas, 0);
      if (exceptionThrown(thread)) {
        // Let Java catch and handle the exception
        rudolph_unlock();

        return;

      }

      if(rootCanvas && rootCanvas->component && rootCanvas->component->tailChild) {
        component = rootCanvas->component->tailChild;
        while(component && component->object) {
          canvas = (r_canvas)component->object;

          if(component->refresh || !Component_isVisible(component) || !Component_verifyBounds(component)) {
            component = component->prev;
            continue;
          }

          /*
          ** Copy canvas to root canvas:
          */

          getAbsoluteCoordinates(component, &x, &y, &w, &h, &dx, &dy, 0);

          src = canvas->buffer->data + pixels2bytes(canvas->buffer->fw * dy);
          dst = rootCanvas->buffer->data + pixels2bytes(y * rootCanvas->buffer->fw);

          for (i = 0; i < h; i++) {
            pixelcopyline(dst, src, x, dx, w);
            dst += pixels2bytes(rootCanvas->buffer->fw);
            src += pixels2bytes(canvas->buffer->fw);
          }

          component = component->prev;
        }
      }

      /*
      ** Copy off-screen buffer to video buffer:
      */

      if (swap_display) {
        w_word *swapped_buffer;
        swapped_buffer = canvas_transpose(rootCanvas->buffer->data, screen->width, screen->height);
        if (swapped_buffer) {
          canvas_blitter((w_word *)screen->video, (w_word *)swapped_buffer, pixels2bytes(screen->width * screen->height));
          releaseMem(swapped_buffer);
        }
      } 
      else {
        canvas_blitter((w_word *)screen->video, (w_word *)rootCanvas->buffer->data, pixels2bytes(screen->width * screen->height));
      }

      something_changed = 0;
      screen_update(0, 0, 0, 0);
      rudolph_unlock();
    }
}

