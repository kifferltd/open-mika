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


/* $Id: canvas.h,v 1.2 2006/05/28 15:04:57 cvs Exp $ */

#ifndef _CANVAS_H
#define _CANVAS_H

#include "rudolph.h"
#include "platform.h"

typedef struct r_Buffer { 
  int fw;       // full buffer width
  int fh;       // full buffer height
    // NOTE: we store the "real" width and height here and don't use instance[F_Component_width]
    //       and instance[F_Component_height] as buffers are used by both images and components.
  int ox;       // x offset
  int oy;       // y offset
  int vw;       // viewport width
  int vh;       // viewport height
  w_ubyte * data;
  w_ubyte * alpha;
} r_Buffer;

typedef struct r_Canvas {
  r_component component;
  r_buffer buffer;
} r_Canvas;

extern r_canvas rootCanvas;
extern int native_paint;

void buffer_release(r_canvas canvas);
void buffer_realloc(r_canvas canvas);

r_canvas canvas_constructor(w_instance canvas);
char *canvas_spaces(w_int depth);
void canvas_verifyBuffer(r_buffer buffer);
void canvas_drawCanvas(r_canvas canvas, w_int depth);
void canvas_drawComponent(r_component component, w_int display);
void canvas_repare(r_component component);
void canvas_canvas2screen(r_canvas canvas);
void canvas_paint(r_component component);
void canvas_copyAll(r_canvas parentCanvas, r_canvas canvas);
w_int canvas_showing(r_component);

inline static w_ubyte* canvas_getPosition(r_buffer buffer, int x, int y) {

  #ifdef DEBUG
    if (buffer->data == NULL) {
      woempa(ABORT_WONKA, "buffer check: the buffer %p has been released\n", buffer);
    }
  #endif

  return buffer->data + pixels2bytes((y*buffer->fw)+x);
}

inline static void drawPixelClipped(r_buffer buffer, int x, int y, r_pixel c) {

  #ifdef DEBUG
    if (buffer->data == NULL) {
      woempa(ABORT_WONKA, "buffer check: the buffer %p has been released\n", buffer);
    }
  #endif
    
  if (x >= 0 && x < buffer->fw && y >= 0 && y < buffer->fh) {
    pixelset(c, buffer->data, buffer->fw, x, y);
  }
  else {
    woempa(5, "clipped pixel at location (%i, %i) in buffer of size %ix%i\n", x, y, buffer->fw, buffer->fh);
  }

}

inline static void drawPixel(r_buffer buffer, int x, int y, r_pixel c) {

  #ifdef DEBUG
    if (buffer->data == NULL) {
      woempa(ABORT_WONKA, "buffer check: the buffer %p has been released\n", buffer);
    }
    
    if (!(x >= 0 && x < buffer->fw && y >= 0 && y < buffer->fh)) {
      woempa(ABORT_WONKA, "boundary check: attempt to address location (%i, %i) in buffer of size %ix%i\n", x, y, buffer->fw, buffer->fh);
    }
  #endif
  
  pixelset(c, buffer->data, buffer->fw, x, y);
}

inline static void drawHLine(r_buffer buffer, int x, int y, int w, r_pixel c) {
  
  woempa(3, "drawHLine (%d,%d) len %d size (%d,%d)\n", x, y, w, buffer->fw, buffer->fh);
     
  /* FIXME: Make more efficient use of pixelfillbyte() */
  while (0 < w) {
    w--;
    pixelset(c, buffer->data, buffer->fw, x+w, y);
  }

}

inline static void drawHLineClipped(r_buffer buffer, int x, int y, int w, r_pixel c) {

  if(y >= buffer->fh || y < 0 || x >= buffer->fw) return;
   
  if(x < 0) {
    w += x;
    x = 0;
  }
  
  w = x + w <= buffer->fw ? w : buffer->fw - x;
    
  /* FIXME: Make more efficient use of pixelfillbyte() */
  while (0 < w) {
    w--;
    pixelset(c, buffer->data, buffer->fw, x+w, y);
  }

}  

void drawLine(r_buffer, int, int, int, int, r_pixel);
r_buffer r_getBuffer(r_component component, r_canvas *canvas, w_int *x, w_int *y, w_int *w, w_int *h);

void canvas_overlap(r_component component);

#define check_overlap(a) if(isNotSet(a->flags, RF_OVERLAP)) canvas_overlap(a)

void *allocRuBuDa(w_size bytes);

void releaseRuBuDa(void *buffer);

#endif
