/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights     *
* reserved. Parts copyright (c) 2005 by Chris Gray, /k/ Embedded Java     *
* Solutions. All rights reserved.                                         *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

#include "arrays.h"
#include "fields.h"
#include "wstrings.h"

#include "awt-classes.h"
#include "registry.h"
#include "canvas.h"

#include "Component.h"
#include "Font.h"
#include "Image.h"

/*
** Prototypes
*/

w_void r_drawOvalQuarter(r_buffer buffer, w_int x, w_int y, w_int w, w_int h, w_int color, w_int q);
w_void r_fillOvalQuarter(r_buffer buffer, w_int x, w_int y, w_int w, w_int h, w_int color, w_int q);
jboolean Graphics_drawImage1(JNIEnv *env, jobject thisGraphics, jobject image, jint dx1, jint dy1, jobject observer);
jboolean Graphics_drawImage2(JNIEnv *env, jobject thisGraphics, jobject image, jint dx1, jint dy1, jobject color, jobject observer);
jboolean Graphics_drawImage3(JNIEnv *env, jobject thisGraphics, jobject image, jint dx1, jint dy1, jint w, jint h, jobject observer);
jboolean Graphics_drawImage4(JNIEnv *env, jobject thisGraphics, jobject image, jint dx1, jint dy1, jint w, jint h, jobject color, jobject observer);
jboolean Graphics_drawImage5(JNIEnv *env, jobject thisGraphics, jobject image, jint dx1, jint dy1, jint dx2, jint dy2, jint sx1, jint sy1, jint sx2, jint sy2, jobject observer);
jboolean Graphics_drawImage6(JNIEnv *env, jobject thisGraphics, jobject image, jint dx1, jint dy1, jint dx2, jint dy2, jint sx1, jint sy1, jint sx2, jint sy2, jobject color, jobject observer);

/*
** A flag to inform the painter thread that 'something_changed' and that the
** screen should be updated.
*/

extern int something_changed;

/*
** Set the something changed flag and invalidate the tree upwards.
** This is only needed when we are called from a user program,
** not when Rudolph decides it needs to repaint something. 
*/

static inline void Graphics_postUpdate(r_canvas canvas) {

  // Debug output:
  woempa(5, "Graphics_postUpdate of '%k' (instance: %p, component %p, refresh: %d)\n", instance2clazz(canvas->component->instance), canvas->component->instance, canvas->component, canvas->component->refresh);

  if (native_paint == 0) {
      
    /*
    ** Tag parent canvases as being invalid:
    */

    something_changed = 1;
    
    /*
    ** Invalidate the tree:
    */
  
    invalidateTreeUpwards(canvas->component, 1);
  }
}

static inline void clip(w_int x1, w_int y1, w_int w1, w_int h1, w_int *x2, w_int *y2, w_int *w2, w_int *h2) {
  if(*x2 == x1 && *y2 == y1 && *w2 == w1 && *h2 == h1) {
  
    /*
    ** They are the same, do nothing.
    */
    
    return;
  }

  if((*x2 - (x1 + w1 - 1) < 0) && ((*x2 + *w2 - 1) - x1 > 0) && (*y2 - (y1 + h1 - 1) < 0) && ((*y2 + *h2 - 1) - y1 > 0)) {
    
    /*
    ** Get the intersection.
    *
    
    if(x1 < *x2) *x2 = x1;
    if(y1 < *y2) *y2 = y1; 
    if((x1 + w - 1) > (*x2 + *w2 - 1)) *x2 = cx2; 
    if((y1 + h - 1) > (*y2 + *h2 - 1)) *y2 = cy2; 
    */
  }
  else {
    *w2 = 0;
    *h2 = 0;
  }
}

void Graphics_copyArea(JNIEnv *env, jobject thisGraphics, jint x, jint y, jint width, jint height, jint deltax, jint deltay) {

  // Resolve required information:
  r_component component = getWotsitField(thisGraphics, F_Graphics_wotsit);
  r_canvas    canvas = NULL;
  r_buffer    buffer = NULL;
  w_int       sx1, sy1, dx1, dy1, j;

  rudolph_lock();

  // Debug output:
  woempa(5, "  Graphics_copyArea(x = %i, y = %i, width = %i, height = %i, deltax = %i, deltay = %i) 2 \n", x, y, width, height, deltax, deltay);
 
  check_overlap(component);
  buffer = r_getBuffer(component, &canvas, &x, &y, NULL, NULL);
  if(!buffer || !buffer->data) {
    rudolph_unlock();
    return;
  }

  x += getIntegerField(thisGraphics, F_Graphics_ox);
  y += getIntegerField(thisGraphics, F_Graphics_oy);

  sx1 = x;
  sy1 = y;
  dx1 = sx1 + deltax;
  dy1 = sy1 + deltay;
  
  if (dx1 < 0) {
    sx1 = sx1 - dx1;
    width += dx1;
    dx1 = 0;
  }
  if (dy1 < 0) {
    sy1 = sy1 - dy1;
    height += dy1;
    dy1 = 0;
  }
  if (dx1 + width > buffer->fw) {
    width = buffer->fw - dx1;
  }
  if (dy1 + height > buffer->fh) {
    height = buffer->fh - dy1;
  }

  // The actual copying, taking possible overlap of source and destination into account

  /* FIXME: Deal with copy / move operations that don't fall on byte boundaries */

  if (sx1 + width - 1 < dx1 || dx1 + width - 1 < sx1) {       // x-ranges of source ans destination do not overlap!
    for (j = 0; j < height; j++) {
      /*
      pixelcopyline(buffer->data + pixels2bytes((dy1+j)*buffer->fw), 
                    buffer->data + pixels2bytes((sy1+j)*buffer->fw),
                    dx1, sx1, width);
      */
      
      w_memcpy(canvas_getPosition(buffer, dx1, dy1 + j), canvas_getPosition(buffer, sx1, sy1+j), (size_t)pixels2bytes(width));
    }
  }
  else {
    if (sy1 + height - 1 < dy1 || dy1 + height - 1 < sy1) {    // y-ranges of source ans destination do not overlap!
      for (j = 0; j < height; j++) {
        /*
        pixelcopyline(buffer->data + pixels2bytes((dy1+j)*buffer->fw), 
                      buffer->data + pixels2bytes((sy1+j)*buffer->fw),
                      dx1, sx1, width);
        */
        w_memcpy(canvas_getPosition(buffer, dx1, dy1 + j), canvas_getPosition(buffer, sx1, sy1+j), (size_t)pixels2bytes(width));
      }
    }
    else {
      if (sy1 < dy1) {
        for (j = height - 1; j >= 0; j--) {
          /*
          pixelcopyline(buffer->data + pixels2bytes((dy1+j)*buffer->fw), 
                        buffer->data + pixels2bytes((sy1+j)*buffer->fw),
                        dx1, sx1, width);
          */
          w_memcpy(canvas_getPosition(buffer, dx1, dy1 + j), canvas_getPosition(buffer, sx1, sy1+j), (size_t)pixels2bytes(width));
        }
      }
      else {
        if (sy1 > dy1) {
          for (j = 0; j < height; j++) {
            /*
            pixelcopyline(buffer->data + pixels2bytes((dy1+j)*buffer->fw), 
                          buffer->data + pixels2bytes((sy1+j)*buffer->fw),
                          dx1, sx1, width);
            */
            w_memcpy(canvas_getPosition(buffer, dx1, dy1 + j), canvas_getPosition(buffer, sx1, sy1+j), (size_t)pixels2bytes(width));
          }
        }
        else {  // sy1 == dy1
          /* FIXME: may result in some screen corruption */
          for (j = 0; j < height; j++) {
            /*
            pixelcopyline(buffer->data + pixels2bytes((dy1+j)*buffer->fw), 
                          buffer->data + pixels2bytes((sy1+j)*buffer->fw),
                          dx1, sx1, width);*/
            // memmove(canvas_getPosition(buffer, dx1, dy1 + j), canvas_getPosition(buffer, sx1, sy1+j), (size_t)pixels2bytes(width));
          
            w_memcpy(canvas_getPosition(buffer, dx1, dy1 + j), canvas_getPosition(buffer, sx1, sy1+j), (size_t)pixels2bytes(width));
          }
        }
      }
    }
  }    // end actual copying


  if(canvas) Graphics_postUpdate(canvas);        
  
  rudolph_unlock();
}

void Graphics_drawString(JNIEnv *env, jobject thisGraphics, jobject stringInstance, jint x, jint y) {
  
  r_component component = getWotsitField(thisGraphics, F_Graphics_wotsit);
  w_instance  fontInstance = getReferenceField(thisGraphics, F_Graphics_font);
  r_font      font = getWotsitField(fontInstance, F_Font_wotsit);
  w_instance  color = getReferenceField(thisGraphics, F_Graphics_foreground);
  w_string    string;
  r_buffer    buffer;
  r_canvas    canvas;
 
  w_int cx = getIntegerField(thisGraphics, F_Graphics_cx);
  w_int cy = getIntegerField(thisGraphics, F_Graphics_cy);
  w_int cw = getIntegerField(thisGraphics, F_Graphics_cw);
  w_int ch = getIntegerField(thisGraphics, F_Graphics_ch);
  w_int fw = 0;
  w_int fh = 0;

  rudolph_lock();
  
  if(stringInstance != NULL) {
    string = String2string(stringInstance);
  }
  else {
    rudolph_unlock();
    return;
  }

  x += getIntegerField(thisGraphics, F_Graphics_ox);
  y += getIntegerField(thisGraphics, F_Graphics_oy);
  cx += getIntegerField(thisGraphics, F_Graphics_ox);
  cy += getIntegerField(thisGraphics, F_Graphics_oy);
  
  /*
  ** The given y-coordinate is suposed to specify the position of the text baseline.
  ** The x-coordinate specifies the position of the first character of the string.
  */
  
  // Debug output:
  woempa(5, "  [paint] called Graphics_drawString('%w', x = %i, y = %i)\n", string, x, y);

  check_overlap(component);
  buffer = r_getBuffer(component, &canvas, &x, &y, &fw, &fh);
  if(!buffer || !buffer->data) {
    rudolph_unlock();
    return;
  }

  /* Get the same offsets for cx and cy.. */
  r_getBuffer(component, &canvas, &cx, &cy, &fw, &fh);
  
  cw = (cw <= 0 ? fw : (cw > fw ? fw : cw));
  ch = (ch <= 0 ? fh : (ch > fh ? fh : ch));

  Font_drawStringUnAligned(buffer, font, x, y, cx, cy, cw, ch, string, color2pixel(color));
  
  if(canvas) Graphics_postUpdate(canvas);
  
  rudolph_unlock();
}

static void get_ellipse_point(w_int a2, w_int b2, w_int xFloor, w_int yFloor, w_int *x, w_int *y) {
  w_int xi = b2 * (2 * xFloor + 1);
  w_int yi = a2*(2 * yFloor+1);
  w_int base = b2 * xFloor * xFloor + a2 * yFloor * yFloor - a2 * b2;
  w_int a00 = abs(base);
  w_int a10 = abs(base + xi);
  w_int a01 = abs(base + yi);
  w_int a11 = abs(base + xi + yi);
  w_int min = a00;

  *x = xFloor;
  *y = yFloor;
  if(a10 < min) {
    min = a10;
    *x = xFloor + 1;
    *y = yFloor;
  }
  if(a01 < min) {
    min = a01;
    *x = xFloor;
    *y = yFloor + 1;
  }
  if(a11 < min) {
    min = a11;
    *x = xFloor + 1;
    *y = yFloor + 1;
  }
}

static w_int select_arc(w_int a2, w_int b2, w_int x, w_int y) {
  if(x > 0) {
    if(y > 0)
      return (b2 * x <  a2 * y ? 0 : 1);
    else if(y < 0)
      return (b2 * x > -a2 * y ? 2 : 3);
    else
      return 2;
  }
  else if(x < 0) {
    if(y < 0)
      return(a2 * y <  b2 * x ? 4 : 5);
    else if(y > 0)
      return(a2 * y < -b2 * x ? 6 : 7);
    else
      return 6;
  }
  else {
    return (y > 0 ? 0 : 4);
  }
}

void Graphics_fillArc2(JNIEnv *env, jobject thisGraphics, jint x, jint y, jint w, jint h, jint xb, jint yb, jint xe, jint ye) {
  r_component component = getWotsitField(thisGraphics, F_Graphics_wotsit);
  w_instance color_inst = getReferenceField(thisGraphics, F_Graphics_foreground);
  r_pixel color = color2pixel(color_inst);
  r_buffer buffer = NULL;
  r_canvas canvas = NULL;
  w_int a2 = w * w;
  w_int b2 = h * h;
  w_int x0, y0, x1, y1;
  w_int dx, dy, sqrlen, arc;
  w_int sigma;

  rudolph_lock();
  
  x += getIntegerField(thisGraphics, F_Graphics_ox);
  y += getIntegerField(thisGraphics, F_Graphics_oy);
  
  check_overlap(component);
  buffer = r_getBuffer(component, &canvas, &x, &y, NULL, NULL);
  if (!buffer || !buffer->data) {
    rudolph_unlock();
    return;
  }

  get_ellipse_point(a2, b2, xb, yb, &x0, &y0);
  get_ellipse_point(a2, b2, xe, ye, &x1, &y1);

  dx = x0 - x1;
  dy = y0 - y1;
  sqrlen = dx * dx + dy * dy;

  if(sqrlen == 1 || (sqrlen == 2 && abs(dx) == 1)) {
    drawLine(buffer, x + x0, y + y0, x, y, color);
    drawLine(buffer, x + x1, y + y1, x, y, color);
    rudolph_unlock();
    return;
  }

  arc = select_arc(a2, b2, x0, y0);
  while(TRUE) {
    drawLine(buffer, x + x0, y + y0, x, y, color);

    switch(arc) {
      case 0:
        x0++;
        dx++;
        sigma = b2 * x0 * x0 + a2 * (y0-1) * (y0-1) - a2 * b2;
        if(sigma >= 0) {
          y0--;
          dy--;
        }
        if(b2 * x0 >= a2 * y0) {
          arc = (y0 > 0 ? 1 : 2);
        }
        break;
      case 1: 
        y0--;
        dy--;
        sigma = b2 * x0 * x0 + a2 * y0 * y0 - a2 * b2;
        if(sigma < 0) {
          x0++;
          dx++;
        }
        if(y0 == 0)
          arc = 2;
        break;
      case 2:  
        y0--;
        dy--;
        sigma = b2 * (x0-1) * (x0-1) + a2 * y0 * y0 - a2 * b2;
        if(sigma >= 0) {
          x0--;
          dx--;
        }
        if(b2 * x0 <= -a2 * y0) {
          arc = (x0 > 0 ? 3 : 4);
        }
        break;
      case 3: 
        x0--;
        dx--;
        sigma = b2 * x0 * x0 + a2 * y0 * y0 - a2 * b2;
        if(sigma < 0) {
          y0--;
          dy--;
        }
        if (x0 == 0)
          arc = 4;
        break;
      case 4:  
        x0--;
        dx--;
        sigma = b2 * x0 * x0 + a2 * (y0+1) * (y0+1) - a2 * b2;
        if(sigma >= 0) {
          y0++;
          dy++;
        }
        if(a2 * y0 >= b2 * x0) {
          arc = (y0 < 0 ? 5 : 6);
        }
        break;
      case 5:  
        y0++;
        dy++;
        sigma = b2 * x0 * x0 + a2 * y0 * y0 - a2 * b2;
        if(sigma < 0) {
          x0--;
          dx--;
        }
        if(y0 == 0)
          arc = 6;
        break;
      case 6:  
        y0++;
        dy++;
        sigma = b2 * (x0+1) * (x0+1) + a2 * y0 * y0 - a2 * b2;
        if(sigma >= 0) {
          x0++;
          dx++;
        }
        if(a2 * y0 >= -b2 * x0) {
          arc = (x0 < 0 ? 7 : 8);
        }
        break;
      case 7:  
        x0++;
        dx++;
        sigma = b2 * x0 * x0 + a2 * y0 * y0 - a2 * b2;
        if(sigma < 0) {
          y0++;
          dy++;
        }
        if (x0 == 0)
          arc = 0;
        break;
    }

    sqrlen = dx * dx + dy * dy;
    if (sqrlen <= 1)
      break;
  }
  
  if(canvas) Graphics_postUpdate(canvas);
  
  rudolph_unlock();
}

void Graphics_drawArc2(JNIEnv *env, jobject thisGraphics, jint x, jint y, jint w, jint h, jint xb, jint yb, jint xe, jint ye) {
  r_component component = getWotsitField(thisGraphics, F_Graphics_wotsit);
  w_instance color_inst = getReferenceField(thisGraphics, F_Graphics_foreground);
  r_pixel color = color2pixel(color_inst);
  r_buffer buffer = NULL;
  r_canvas canvas = NULL;
  w_int a2 = w * w;
  w_int b2 = h * h;
  w_int x0, y0, x1, y1;
  w_int dx, dy, sqrlen, arc;
  w_int sigma;

  rudolph_lock();
  
  x += getIntegerField(thisGraphics, F_Graphics_ox);
  y += getIntegerField(thisGraphics, F_Graphics_oy);
  
  check_overlap(component);
  buffer = r_getBuffer(component, &canvas, &x, &y, NULL, NULL);
  if (!buffer || !buffer->data) {
    rudolph_unlock();
    return;
  }

  get_ellipse_point(a2, b2, xb, yb, &x0, &y0);
  get_ellipse_point(a2, b2, xe, ye, &x1, &y1);

  dx = x0 - x1;
  dy = y0 - y1;
  sqrlen = dx * dx + dy * dy;

  if(sqrlen == 1 || (sqrlen == 2 && abs(dx) == 1)) {
    drawLine(buffer, x + x0, y + y0, x, y, color);
    drawLine(buffer, x + x1, y + y1, x, y, color);
    rudolph_unlock();
    return;
  }

  arc = select_arc(a2, b2, x0, y0);
  while(TRUE) {
    drawPixelClipped(buffer, x + x0, y + y0, color);

    switch(arc) {
      case 0:
        x0++;
        dx++;
        sigma = b2 * x0 * x0 + a2 * (y0-1) * (y0-1) - a2 * b2;
        if(sigma >= 0) {
          y0--;
          dy--;
        }
        if(b2 * x0 >= a2 * y0) {
          arc = (y0 > 0 ? 1 : 2);
        }
        break;
      case 1: 
        y0--;
        dy--;
        sigma = b2 * x0 * x0 + a2 * y0 * y0 - a2 * b2;
        if(sigma < 0) {
          x0++;
          dx++;
        }
        if(y0 == 0)
          arc = 2;
        break;
      case 2:  
        y0--;
        dy--;
        sigma = b2 * (x0-1) * (x0-1) + a2 * y0 * y0 - a2 * b2;
        if(sigma >= 0) {
          x0--;
          dx--;
        }
        if(b2 * x0 <= -a2 * y0) {
          arc = (x0 > 0 ? 3 : 4);
        }
        break;
      case 3: 
        x0--;
        dx--;
        sigma = b2 * x0 * x0 + a2 * y0 * y0 - a2 * b2;
        if(sigma < 0) {
          y0--;
          dy--;
        }
        if (x0 == 0)
          arc = 4;
        break;
      case 4:  
        x0--;
        dx--;
        sigma = b2 * x0 * x0 + a2 * (y0+1) * (y0+1) - a2 * b2;
        if(sigma >= 0) {
          y0++;
          dy++;
        }
        if(a2 * y0 >= b2 * x0) {
          arc = (y0 < 0 ? 5 : 6);
        }
        break;
      case 5:  
        y0++;
        dy++;
        sigma = b2 * x0 * x0 + a2 * y0 * y0 - a2 * b2;
        if(sigma < 0) {
          x0--;
          dx--;
        }
        if(y0 == 0)
          arc = 6;
        break;
      case 6:  
        y0++;
        dy++;
        sigma = b2 * (x0+1) * (x0+1) + a2 * y0 * y0 - a2 * b2;
        if(sigma >= 0) {
          x0++;
          dx++;
        }
        if(a2 * y0 >= -b2 * x0) {
          arc = (x0 < 0 ? 7 : 8);
        }
        break;
      case 7:  
        x0++;
        dx++;
        sigma = b2 * x0 * x0 + a2 * y0 * y0 - a2 * b2;
        if(sigma < 0) {
          y0++;
          dy++;
        }
        if (x0 == 0)
          arc = 0;
        break;
    }

    sqrlen = dx * dx + dy * dy;
    if (sqrlen <= 1)
      break;

  }
  
  if(canvas) Graphics_postUpdate(canvas);
  
  rudolph_unlock();
}

void Graphics_drawLine(JNIEnv *env, jobject thisGraphics, jint x1, jint y1, jint x2, jint y2) {

  r_component component = getWotsitField( thisGraphics, F_Graphics_wotsit);
  w_instance  color = getReferenceField(thisGraphics, F_Graphics_foreground);
  r_canvas    canvas = NULL;
  r_buffer    buffer = NULL;
  w_int       ox = 0;
  w_int       oy = 0;
  
  rudolph_lock();
 
  // Debug output:
  woempa(5, "  [paint] called Graphics_drawLine(x1 = %i, y1 = %i, x2 = %i, y2 = %i)\n", x1, y1, x2, y2);

  check_overlap(component);
  buffer = r_getBuffer(component, &canvas, &ox, &oy, NULL, NULL);
  if(!buffer || !buffer->data) {
    rudolph_unlock();
    return;
  }

  ox += getIntegerField(thisGraphics, F_Graphics_ox);
  oy += getIntegerField(thisGraphics, F_Graphics_oy);
  
  x1 += ox;
  y1 += oy;
  x2 += ox;
  y2 += oy;

  drawLine(buffer, x1, y1, x2, y2, color2pixel(color));
  
  if(canvas) Graphics_postUpdate(canvas);
  
  rudolph_unlock();
}

/*
** Description:
**   Optimized oval algorithm using shift operations instead
**   of multiplications and divisions.
** Algorithm:
**   We avoid the usage of slow sinus and cosinus functions
**   and/or tables, by replacing them with a set of 2
**   lineair functions.  Secondly, by exploiting the symmetry
**   of a cirle we can cut down the number of mathematical
**   operations drastically.
**/

void Graphics_drawOval(JNIEnv *env, jobject thisGraphics, int x, int y, int w, int h) {
  r_component component = getWotsitField(thisGraphics, F_Graphics_wotsit);
  w_instance color_inst = getReferenceField(thisGraphics, F_Graphics_foreground);
  r_pixel color = color2pixel(color_inst);
  r_buffer buffer = NULL;
  r_canvas canvas = NULL;

  w_int a = w / 2;
  w_int b = h / 2;
  w_int a2 = a * a;
  w_int b2 = b * b;
  w_int fa2 = 4 * a2;
  w_int fb2 = 4 * b2;
  w_int xi, yi;
  w_int sigma;
  
  rudolph_lock();

  x += getIntegerField(thisGraphics, F_Graphics_ox) + w / 2;
  y += getIntegerField(thisGraphics, F_Graphics_oy) + h / 2;
  
  // Debug output:
  woempa(5, "  [paint] called Graphics_drawOval(x = %i, y = %i, w = %i, h = %i)\n", x, y, w, h);

  check_overlap(component);
  buffer = r_getBuffer(component, &canvas, &x, &y, NULL, NULL);
  if(!buffer || !buffer->data) {
    rudolph_unlock();
    return;
  }

  for(xi = 0, yi = b, sigma = 2 * b2 + a2 * (1 - 2 * b); b2 * xi <= a2 * yi; xi++) {
    drawPixelClipped(buffer, x + xi, y + yi, color);
    drawPixelClipped(buffer, x - xi, y + yi, color);
    drawPixelClipped(buffer, x + xi, y - yi, color);
    drawPixelClipped(buffer, x - xi, y - yi, color);
    
    if(sigma >= 0) {
      sigma += fa2 * (1 - yi);
      yi--;
    }
    sigma += b2 * (4 * xi + 6);
  }
  
  for(xi = a, yi = 0, sigma = 2 * a2 + b2 * (1 - 2 * a); a2 * yi <= b2 * xi; yi++) {
    drawPixelClipped(buffer, x + xi, y + yi, color);
    drawPixelClipped(buffer, x - xi, y + yi, color);
    drawPixelClipped(buffer, x + xi, y - yi, color);
    drawPixelClipped(buffer, x - xi, y - yi, color);
    
    if(sigma >= 0) {
      sigma += fb2 * (1 - xi);
      xi--;
    }
    sigma += a2 * (4 * yi + 6);
  }
  
  if(canvas) Graphics_postUpdate(canvas);

  rudolph_unlock();
}

void Graphics_fillOval(JNIEnv *env, jobject thisGraphics, int x, int y, int w, int h) {
  r_component component = getWotsitField(thisGraphics, F_Graphics_wotsit);
  w_instance color_inst = getReferenceField(thisGraphics, F_Graphics_foreground);
  r_pixel color = color2pixel(color_inst);
  r_buffer buffer = NULL;
  r_canvas canvas = NULL;
  
  w_int a = w / 2;
  w_int b = h / 2;
  w_int a2 = a * a;
  w_int b2 = b * b;
  w_int fa2 = 4 * a2;
  w_int fb2 = 4 * b2;
  w_int sigma = 2 * b2 + a2 * (1 - 2 * b);
  w_int xi, yi;
  
  rudolph_lock();

  x += getIntegerField(thisGraphics, F_Graphics_ox) + w / 2;
  y += getIntegerField(thisGraphics, F_Graphics_oy) + h / 2;
  
  // Debug output:
  woempa(5, "  [paint] called Graphics_fillOval(x = %i, y = %i, w = %i, h = %i)\n", x, y, w, h);

  check_overlap(component);
  buffer = r_getBuffer(component, &canvas, &x, &y, NULL, NULL);
  if(!buffer || !buffer->data) {
    rudolph_unlock();
    return;
  }

  for(xi = 0, yi = b; b2 * xi <= a2 * yi; xi++) {
    drawHLineClipped(buffer, x - xi, y + yi, xi * 2, color);
    drawHLineClipped(buffer, x - xi, y - yi, xi * 2, color);
    
    if(sigma >= 0) {
      sigma += fa2 * (1 - yi);
      yi--;
    }
    sigma += b2 * (4 * xi + 6);
  }
  
  sigma = 2 * a2 + b2 * (1 - 2 * a);
  
  for(xi = a, yi = 0; a2 * yi <= b2 * xi; yi++) {
    drawHLineClipped(buffer, x - xi, y + yi, xi * 2, color);
    drawHLineClipped(buffer, x - xi, y - yi, xi * 2, color);
    
    if(sigma >= 0) {
      sigma += fb2 * (1 - xi);
      xi--;
    }
    sigma += a2 * (4 * yi + 6);
  }
  
  if(canvas) Graphics_postUpdate(canvas);
  
  rudolph_unlock();
}

void Graphics_drawPolygon(JNIEnv *env, jobject thisGraphics, jobject xpoints, jobject ypoints, jint n) {

  rudolph_lock();

  if (thisGraphics && xpoints && ypoints) {

    if ((n <= instance2Array_length(xpoints)) && (n <= instance2Array_length(ypoints))) {

      // Resolve required information:
      r_component component = getWotsitField(thisGraphics, F_Graphics_wotsit);
      w_instance  color = getReferenceField(thisGraphics, F_Graphics_foreground);
      r_pixel     col;
      r_canvas    canvas = NULL;
      r_buffer    buffer = NULL;
      w_int       *x = instance2Array_int(xpoints);
      w_int       *y = instance2Array_int(ypoints);
      w_int       i;
      w_int       dx = getIntegerField(thisGraphics, F_Graphics_ox);
      w_int       dy = getIntegerField(thisGraphics, F_Graphics_oy);
     
      col = color2pixel(color);

      // Debug information:
      woempa(5, "  [paint] called drawPolygon(vertices = %i, edges = %i\n", n, n);

      check_overlap(component);
      buffer = r_getBuffer(component, &canvas, &dx, &dy, NULL, NULL);
      if (!buffer || !buffer->data) {
        rudolph_unlock();
        return;
      }

      // Draw polygon:
      for (i = 0; i < n - 1; i++) {
        drawLine(buffer, *(x + i) + dx, *(y + i) + dy, *(x + i + 1) + dx, *(y + i + 1) + dy, col);
      }
      drawLine(buffer, *(x + n-1) + dx, *(y + n-1) + dy, *x + dx, *y + dy, col);
     
      if(canvas) Graphics_postUpdate(canvas);
    }
  }
  
  rudolph_unlock();
}


/*
** An auxilliary function to build a linked list for a given array of values
** values: the buffer of element values
** order: here the ordered list will be stored
** returns: first element in linked list
*/

w_int r_sortIncreasing(w_int* values, w_int* order, w_int size) {
  w_int first, previous, i;
  first = 0;
  order[0]=-1;

  for(i=1; i<size; i++){
    //add next
    if(values[i]<=values[first]){
      order[i]=first;
      first = i;
    }
    else {
      previous = first;
      while(order[previous]>=0 && values[order[previous]]<=values[i]){
        previous = order[previous];
      }
      order[i]=order[previous];
      order[previous]=i;
    }
  }
  return first;
}


/*
** Description:
**   The function fills a polygon area area of the buffer of the given graphics'
**   component with the current foreground color as per java.awt.fillPolygon()
**
**  By default, this function should be completely aware of boundaries clipping
*/

void Graphics_fillPolygon(JNIEnv *env, jobject thisGraphics, jobject xpoints, jobject ypoints, jint size) {
  rudolph_lock();
  /* Check for null instances: */
  if (thisGraphics && xpoints && ypoints) {
    /* Check for buffer overflows: */
    if ((size <= instance2Array_length(xpoints)) && (size <= instance2Array_length(ypoints))) {

      /** Resolve required information: */
      /**********************************/
      //component
      r_component component = getWotsitField(thisGraphics, F_Graphics_wotsit);

      //screen buffer
      r_buffer buffer;
      r_canvas canvas;
      //screen color
      w_instance color = getReferenceField(thisGraphics, F_Graphics_foreground);
      r_pixel pixelColor;
      //screen offsets
      w_int ox = 0;
      w_int oy = 0;

      // corner point arrays (point_x and point_y component
      w_int *pointx = instance2Array_int(xpoints);
      w_int *pointy = instance2Array_int(ypoints);

      // Our own buffers:
      // linked list for points in top-down order
      w_int *nextpoint;
      // linepiece start and stop buffers
      w_int *linestarts;
      w_int *linestops;
      // data buffer for scanline intersection nodes, linked list buffer to order them
      w_int *nodes;
      w_int *nextnode;

      //variables for line-scan loop:
      w_int numberoflines;
      w_int currentpoint, previous, next;
      w_int scanline, scanstart, scanstop;
      w_int i, realline;

      nextpoint =  (w_int*)allocMem((size_t)(size*sizeof(w_int)));
      if (!nextpoint) {
        return;
      }

      linestarts =  (w_int*)allocMem((size_t)(size*sizeof(w_int)));
      if (!linestarts) {
        releaseMem(nextpoint);
        return;
      }

      linestops =  (w_int*)allocMem((size_t)(size*sizeof(w_int)));
      if (!linestops) {
        releaseMem(linestarts);
        releaseMem(nextpoint);
        return;
      }

      nodes =  (w_int*)allocMem((size_t)(size*sizeof(w_int)));
      if (!nodes) {
        releaseMem(linestops);
        releaseMem(linestarts);
        releaseMem(nextpoint);
        return;
      }

      nextnode =  (w_int*)allocMem((size_t)(size*sizeof(w_int)));
      if (!nextnode) {
        releaseMem(nodes);
        releaseMem(linestops);
        releaseMem(linestarts);
        releaseMem(nextpoint);
        return;
      }

      woempa(5, "  [paint] called fillPolygon %i edges\n", size);
      /** initialise screen: color, canvas, buffer, offset*/
      // color
      pixelColor = color2pixel(color);

      check_overlap(component);
      buffer = r_getBuffer(component, &canvas, &ox, &oy, NULL, NULL);
      if(!buffer || !buffer->data) {
        rudolph_unlock();
        return;
      }

      /** Sort the polygon points and initialise the scanline loop */
      numberoflines = 0;
      currentpoint = r_sortIncreasing(pointy, nextpoint, size);
      scanstart = pointy[currentpoint];

      /** loop */
      do{
        // get previous and next linepieces
        previous = (currentpoint>0)? currentpoint-1: size-1;
        next =  (currentpoint<(size-1))? currentpoint+1: 0;

        // scan linepiece from previous point to current
        if(pointy[previous]>pointy[currentpoint]){
          // the linepiece starts here and goes down: add it to linepieces list
          linestarts[numberoflines]=currentpoint;
          linestops[numberoflines++]=previous;
        }
        else if(pointy[previous]<pointy[currentpoint]){
          // the linepiece comes from higher and ends here remove it from list
          for(i=0; (linestarts[i]!=previous || linestops[i]!=currentpoint); i++);
          linestarts[i]=linestarts[--numberoflines];
          linestops[i]=linestops[numberoflines];
        }

        // scan linepiece from current point to next
        if(pointy[next]>pointy[currentpoint]){
          // the linepiece starts here and goes down: add it to boundaries list
          linestarts[numberoflines]=currentpoint;
          linestops[numberoflines++]=next;
        }
        else if(pointy[next]<pointy[currentpoint]){
          // the linepiece comes from higher and ends here remove it from list
          for(i=0; (linestarts[i]!=next || linestops[i]!=currentpoint); i++);
          linestarts[i]=linestarts[--numberoflines];
          linestops[i]=linestops[numberoflines];
        }

        // get subset boundaries:
        scanstop = pointy[nextpoint[currentpoint]];
        realline=scanstart+oy;
        woempa(5, "  drawing polygon subset line %i to %i\n", scanstart, scanstop);
        // look if in screen before drawing
        if(realline<(buffer->fh) && (scanstop+oy)>0) {
          // okay, we start well under the buffers maximum and stop well over the buffers minimum
          // update the scan lines if necessary
          if(realline<0){
            realline = 0;
            scanstart = -oy;
          }
          if((scanstop+oy)>= (buffer->fh)){
            scanstop = (buffer->fh) -1 - oy;
          }
          // for all lines of this linepiece: calculate the intersections with the boundary lines, and paint
          for(scanline=scanstart; scanline<scanstop; scanline++) {
            // get the nodes for the intersections of the scanlines with the current page
            for(i=0; i<numberoflines; i++){
              // calculate the intersection (we directly include the offset)
              nodes[i] = pointx[linestarts[i]] + ox;  // if scanline==start of linepiece, we directly know what the x-offset is
              if(scanline > pointy[linestarts[i]]) {
                nodes[i] += (pointx[linestops[i]]-pointx[linestarts[i]]) * (scanline-pointy[linestarts[i]]) / (pointy[linestops[i]]-pointy[linestarts[i]]);
              }
            }

            // sort them in a linked list
            previous = r_sortIncreasing(nodes, nextnode, numberoflines);
            // draw from node to next node as long as there are
            while(previous>=0){
              next = nextnode[previous];
              // if in bounds, draw the line
              if(nodes[previous]>=buffer->fw || nodes[next]<0) {
                 woempa(5, "  Drawing scanline %i, real line %i line %i to %i out of screen (0->%i) \n", realline, scanline, nodes[previous], nodes[next], buffer->fw);
              }
              else if(nodes[previous]<0 && nodes[next]>=(buffer->fw)) {
                drawHLineClipped(buffer, 0, realline, buffer->fw, pixelColor);          
              }
              else if(nodes[previous]<0){
                drawHLineClipped(buffer, 0, realline, nodes[next] + 1, pixelColor);          
              }
              else if(nodes[next]>=(buffer->fw)){
                drawHLineClipped(buffer, nodes[previous], realline, (buffer->fw) - 1 - nodes[previous], pixelColor);          
              }
              else {
                drawHLineClipped(buffer, nodes[previous], realline, nodes[next] - nodes[previous] + 1, pixelColor);          
              }
              previous = nextnode[next];
            }
            realline++;
          }
        }
        //next point, next scan area
        currentpoint = nextpoint[currentpoint];
        scanstart = scanstop;
      }
      while(nextpoint[currentpoint]>=0);

      // post-update if needed
      if(canvas) Graphics_postUpdate(canvas);

      /** Ok, we're done, now release the buffer memory again*/
      releaseMem(nextpoint);
      releaseMem(linestarts);
      releaseMem(linestops);
      releaseMem(nodes);
      releaseMem(nextnode);

    }
    /* done Check for buffer overflow: */
  }
  /* done Check for null instances: */
  rudolph_unlock();
}

void Graphics_drawRect(r_buffer buffer, int x, int y, int w, int h, int c) {

  rudolph_lock();
  // Debug output:
  woempa(5, "called Graphics_drawRect: x = %i, y = %i, w = %i, h = %i, color = %i\n", x, y, w, h, c);

  // Vertical lines:
  drawLine(buffer, x, y, x, y, c);
  drawLine(buffer, x + w, y, x + w, y + h, c);
  
  // Horizontal lines:
  drawHLineClipped(buffer, x, y, w, c);
  drawHLineClipped(buffer, x, y + h, w, c);

  rudolph_unlock();
}

void Graphics_drawRoundRect(JNIEnv *env, jobject thisGraphics, jint x, jint y, jint w, jint h, jint arcWidth, jint arcHeight) {
  r_component component = getWotsitField(thisGraphics, F_Graphics_wotsit);
  w_instance  color_inst = getReferenceField(thisGraphics, F_Graphics_foreground);
  r_pixel     color = color2pixel(color_inst);
  r_buffer    buffer = NULL;
  r_canvas    canvas = NULL;

  rudolph_lock();

  x += getIntegerField(thisGraphics, F_Graphics_ox);
  y += getIntegerField(thisGraphics, F_Graphics_oy);
  
  // Debug output:
  woempa(5, "  [paint] called Graphics_drawRoundRect(x = %i, y = %i, w = %i, h = %i)\n", x, y, w, h);

  check_overlap(component);
  buffer = r_getBuffer(component, &canvas, &x, &y, NULL, NULL);
  if(!buffer || !buffer->data) {
    rudolph_unlock();
    return;
  }

  // Vertical lines:
  drawLine(buffer, x, y + arcHeight / 2, x, y + h - arcHeight / 2, color);
  drawLine(buffer, x + w, y + arcHeight / 2, x + w, y + h - arcHeight / 2, color);
  
  // Horizontal lines:
  drawHLineClipped(buffer, x + arcWidth / 2, y, w - arcWidth + 1, color);
  drawHLineClipped(buffer, x + arcWidth / 2, y + h, w - arcWidth + 1, color);
  
  // Roundings:
  r_drawOvalQuarter(buffer, x, y, arcWidth, arcHeight, color, 2);  
  r_drawOvalQuarter(buffer, x + w - arcWidth + 1, y, arcWidth - 1, arcHeight - 1, color, 1);
  r_drawOvalQuarter(buffer, x, y + h - arcHeight + 1, arcWidth - 1, arcHeight - 1, color, 3);
  r_drawOvalQuarter(buffer, x + w - arcWidth + 1, y + h - arcHeight + 1, arcWidth - 1, arcHeight - 1, color, 4);
  
  if(canvas) Graphics_postUpdate(canvas);
  
  rudolph_unlock();
}

void Graphics_fillRoundRect(JNIEnv *env, jobject thisGraphics, jint x, jint y, jint w, jint h, jint arcWidth, jint arcHeight) {
  r_component component = getWotsitField(thisGraphics, F_Graphics_wotsit);
  w_instance  color_inst = getReferenceField(thisGraphics, F_Graphics_foreground);
  r_pixel     color = color2pixel(color_inst);
  r_buffer    buffer = NULL;
  r_canvas    canvas = NULL;
  w_int       i;

  rudolph_lock();
  
  x += getIntegerField(thisGraphics, F_Graphics_ox);
  y += getIntegerField(thisGraphics, F_Graphics_oy);
  
  // Debug output:
  woempa(5, "  [paint] called Graphics_drawRoundRect(x = %i, y = %i, w = %i, h = %i)\n", x, y, w, h);

  check_overlap(component);
  buffer = r_getBuffer(component, &canvas, &x, &y, NULL, NULL);
  if(!buffer || !buffer->data) {
    rudolph_unlock();
    return;
  }
  
  for(i = 0; i <= arcHeight / 2; i++) {
    drawHLineClipped(buffer, x + arcWidth / 2, y + i, w - arcWidth + 1, color);
    drawHLineClipped(buffer, x + arcWidth / 2, y + h - i, w - arcWidth + 1, color);
  }

  for(i = y + arcHeight / 2; i <= y + h - arcHeight / 2; i++) {
    drawHLineClipped(buffer, x, i, w, color);
  }
  
  // Roundings:
  r_fillOvalQuarter(buffer, x, y, arcWidth, arcHeight, color, 2);  
  r_fillOvalQuarter(buffer, x + w - arcWidth + 1, y, arcWidth - 1, arcHeight - 1, color, 1);
  r_fillOvalQuarter(buffer, x, y + h - arcHeight + 1, arcWidth - 1, arcHeight - 1, color, 3);
  r_fillOvalQuarter(buffer, x + w - arcWidth + 1, y + h - arcHeight + 1, arcWidth - 1, arcHeight - 1, color, 4);
  
  if(canvas) Graphics_postUpdate(canvas);
  rudolph_unlock();
}

/*
** Description:
**   The function fills a rectangular area of the buffer of the given graphics'
**   component with a color specified by 'colorInstance'
**   Given coordinate (x,y) specifies the origin of the rectangle.
**   Its horizontal size is specified by w, and the vertical size is h.
**   In the vertical direction, only the intersection of the given rectangle with
**   the component buffer of the given graphics object is drawn.
**   For restrictions in the horizontal direction, see the docs of 'drawHLine'
*/

void Graphics_fillRect(JNIEnv *env, jobject thisGraphics, jint x, jint y, jint w, jint h) {

  r_component component = getWotsitField(thisGraphics, F_Graphics_wotsit);
  w_instance  color = getReferenceField(thisGraphics, F_Graphics_foreground);
  r_pixel     pixelColor;
  r_canvas    canvas = NULL;
  r_buffer    buffer = NULL;
  w_int       dx = 0;
  w_int       dy = 0;
  w_int       i;
  
  rudolph_lock();
  
  x += getIntegerField(thisGraphics, F_Graphics_ox);
  y += getIntegerField(thisGraphics, F_Graphics_oy);
  
  check_overlap(component);
  buffer = r_getBuffer(component, &canvas, &dx, &dy, NULL, NULL);
  if(!buffer || !buffer->data) {
    rudolph_unlock();
    return;
  }

  // Debug output:
  woempa(5, "  [paint] called Graphics_fillRect(x = %i, y = %i, w = %i, h = %i)\n", x, y, w, h);

  if(component->tag == Z_COMPONENT) {

    /*
    ** Component clipping
    */
    
    w = (getIntegerField(component->instance, F_Component_width) > x + w) ? w : getIntegerField(component->instance, F_Component_width) - x;
    h = (getIntegerField(component->instance, F_Component_height) > y + h) ? h : getIntegerField(component->instance, F_Component_height) - y;
  }
               
  x += dx;
  y += dy;

  /*
  ** Buffer clipping
  */

  if(x >= buffer->fw || y >= buffer->fh) {
    rudolph_unlock();
    return;
  }
  
  if(x < 0) {
    w += x;
    x = 0;
  }
    
  if (y < 0) {
    h += y;
    y = 0;
  }

  w = (buffer->fw >= x + w) ? w : buffer->fw - x; 
  h = (buffer->fh >= y + h) ? h : buffer->fh - y;
  
  pixelColor = color2pixel(color);

  for (i = y; i < y + h; i++) {
    drawHLine(buffer, x, i, w, pixelColor);
  }
    
  if(canvas) Graphics_postUpdate(canvas);

  rudolph_unlock();
}

jint Graphics_getBufferWidth(JNIEnv *env, jobject thisGraphics) {
  r_component component = getWotsitField(thisGraphics, F_Graphics_wotsit);
  r_canvas    canvas;
  w_int       dx;
  w_int       dy;
  w_int       w;
  w_int       h;
  
  if (!component) {
    return 0;
  }
  
  rudolph_lock();
  r_getBuffer(component, &canvas, &dx, &dy, &w, &h);
  rudolph_unlock();
  
  return w;
}

jint Graphics_getBufferHeight(JNIEnv *env, jobject thisGraphics) {
  r_component component = getWotsitField(thisGraphics, F_Graphics_wotsit);
  r_canvas    canvas;
  w_int       dx;
  w_int       dy;
  w_int       w;
  w_int       h;
  
  if (!component) {
    return 0;
  }
  
  rudolph_lock();
  r_getBuffer(component, &canvas, &dx, &dy, &w, &h);
  rudolph_unlock();
  
  return h;
}

/*
** Utility functions for drawImage.
*/

w_void addObserver(JNIEnv *env, jobject image, jobject observer) {
  static jmethodID addObs = NULL;

  if(!addObs) {
    addObs = (*env)->GetMethodID(env, clazz2Class(clazzImage), "addObserver", "(Ljava/awt/image/ImageObserver;)V");
  }

  (*env)->CallVoidMethod(env, image, addObs, observer);
}

w_void prepareImage(JNIEnv *env, jobject image, jobject observer) {
  static jmethodID prepare = NULL;
  static jobject toolkit = NULL;
  
  if(!prepare) {
    prepare = (*env)->GetMethodID(env, clazz2Class(clazzToolkit), "prepareImage", "(Ljava/awt/Image;IILjava/awt/image/ImageObserver;)Z");
    toolkit = (jobject)clazzToolkit->staticFields[F_Toolkit_t];
  }
  
  if(getReferenceField(image, F_Image_producer) && ((getIntegerField(image, F_Image_flags) & (8 + 16 + 32)) == 0)) {
    (*env)->CallBooleanMethod(env, toolkit, prepare, image, -1, -1, observer);
  }
  
}

/*
** Drawing images.
*/

jboolean Graphics_drawImage1(JNIEnv *env, jobject thisGraphics, jobject image, jint dx1, jint dy1, jobject observer) {
  return Graphics_drawImage2(env, thisGraphics, image, dx1, dy1, NULL, observer);
}

jboolean Graphics_drawImage2(JNIEnv *env, jobject thisGraphics, jobject image, jint dx1, jint dy1, jobject color, jobject observer) {
  jboolean     result = FALSE;
  r_component  component = getWotsitField(thisGraphics, F_Graphics_wotsit);
  r_image      img;
  r_canvas     canvas;
  r_buffer     buffer;
  w_int        dx = 0;
  w_int        dy = 0;
  w_int        sx = 0;
  w_int        sy = 0;
  w_int        cw;
  w_int        ch;
  w_int        w;
  w_int        h;

  if(!image) {
    return FALSE;
  }

  if(observer) addObserver(env, image, observer);
  
  prepareImage(env, image, observer);
 
  img = getWotsitField(image, F_Image_wotsit);

  if(!img) {
    return FALSE;
  }
  
  w = img->w;
  h = img->h;

  rudolph_lock();
  
  check_overlap(component);
  buffer = r_getBuffer(component, &canvas, &dx, &dy, NULL, NULL);
  
  if(!buffer || !buffer->data) {
    rudolph_unlock();
    return FALSE;
  }

  dx1 += getIntegerField(thisGraphics, F_Graphics_ox) + dx;
  dy1 += getIntegerField(thisGraphics, F_Graphics_oy) + dy;
    
  cw = getIntegerField(thisGraphics, F_Graphics_cw);
  ch = getIntegerField(thisGraphics, F_Graphics_ch);

  if(cw > 0 && ch > 0) {

    /*
    ** setClip is used.
    */
    
    w_int cx = thisGraphics[F_Graphics_cx] + dx;
    w_int cy = thisGraphics[F_Graphics_cy] + dy;
    
    if (dx1 < cx) {
      sx = cx - dx1;
      dx1 = cx;
      w -= sx;
    }
    if (dy1 < cy) {
      sy = cy - dy1;
      dy1 = cy;
      h -= sy;
    }
    if (dx1 + w > cx + cw) {
      w = cx + cw - dx1;
    }
    if (dy1 + h > cy + ch) {
      h = cy + ch - dy1;
    }
  }

  if(image[F_Image_flags] & 0x80000000) {

    /* Restore background */

    w_instance colorInstance;
    r_pixel pixel;
    w_int i;
    
    Component_getBackground(component, &colorInstance, NULL);
    pixel = color2pixel(colorInstance);

    for (i = dy; i < dy + h; i++) {
      drawHLine(buffer, dx1, i, w, pixel);
    }
  }
 
  result = Image_drawImage(buffer, img, dx1, dy1, dx1 + w - 1, dy1 + h - 1, sx, sy, sx + w - 1, sy + h - 1);

  if(canvas) Graphics_postUpdate(canvas);
  
  rudolph_unlock();

  return result;
}

jboolean Graphics_drawImage3(JNIEnv *env, jobject thisGraphics, jobject image, jint dx1, jint dy1, jint w, jint h, jobject observer) {
  return Graphics_drawImage4(env, thisGraphics, image, dx1, dy1, w, h, NULL, observer);
}

jboolean Graphics_drawImage4(JNIEnv *env, jobject thisGraphics, jobject image, jint dx1, jint dy1, jint iw, jint ih, jobject color, jobject observer) {

  jboolean     result = FALSE;
  r_component  component = getWotsitField(thisGraphics, F_Graphics_wotsit);
  r_image      img;
  r_canvas     canvas;
  r_buffer     buffer;
  w_int        dx = 0;
  w_int        dy = 0;
  w_int        sx = 0;
  w_int        sy = 0;
  w_int        cw = thisGraphics[F_Graphics_cw];
  w_int        ch = thisGraphics[F_Graphics_ch];
  w_int        w;
  w_int        h;
 
  if(!image) {
    return FALSE;
  }
  
  if(observer) addObserver(env, image, observer);
  
  prepareImage(env, image, observer);
  
  img = getWotsitField(image, F_Image_wotsit);

  if(!img) return FALSE;

  w = img->w;
  h = img->h;

  rudolph_lock();
  
  check_overlap(component);
  buffer = r_getBuffer(component, &canvas, &dx, &dy, NULL, NULL);
  
  if(!buffer || !buffer->data) {
    rudolph_unlock();
    return FALSE;
  }

  if(iw < 0 && ih < 0) {
    iw = w;
    ih = h;
  }
  else if(iw < 0) {
    iw = (ih * w) / h;
  }
  else if(ih < 0) {
    ih = (iw * h) / w;
  }

  if(cw > 0 && ch > 0) {

    /*
    ** setClip is used.
    */
    
    w_int cx = thisGraphics[F_Graphics_cx];
    w_int cy = thisGraphics[F_Graphics_cy];
    
    if (dx1 < cx) {
      sx = ((cx - dx1) * w) / iw;
      iw -= cx - dx1;
      dx1 = cx;
      w -= sx;
    }
    if (dy1 < cy) {
      sy = ((cy - dy1) * h) / ih;
      ih -= cy - dy1;
      dy1 = cy;
      h -= sy;
    }
    if (dx1 + iw > cx + cw) {
      w_int tmp = iw;
      iw = cx + cw - dx1;
      w = w * iw / tmp;
    }
    if (dy1 + ih > cy + ch) {
      w_int tmp = ih;
      ih = cy + ch - dy1;
      h = h * ih / tmp;
    }
  }
  
  dx1 += getIntegerField(thisGraphics, F_Graphics_ox) + dx;
  dy1 += getIntegerField(thisGraphics, F_Graphics_oy) + dy;

  if(image[F_Image_flags] & 0x80000000) {

    /* Restore background */

    w_instance colorInstance;
    r_pixel pixel;
    w_int i;
    
    Component_getBackground(component, &colorInstance, NULL);
    pixel = color2pixel(colorInstance);

    for (i = dy1; i < dy1 + ih; i++) {
      drawHLine(buffer, dx1, i, iw, pixel);
    }
  }
 
  result = Image_drawImage(buffer, img, dx1, dy1, dx1 + iw - 1, dy1 + ih - 1, sx, sy, sx + w - 1, sy + h - 1);

  if(canvas) Graphics_postUpdate(canvas);
  
  rudolph_unlock();

  return result;
}

jboolean Graphics_drawImage5(JNIEnv *env, jobject thisGraphics, jobject image, jint dx1, jint dy1, jint dx2, jint dy2, jint sx1, jint sy1, jint sx2, jint sy2, jobject observer) {
  return Graphics_drawImage6(env, thisGraphics, image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, NULL, observer);
}

jboolean Graphics_drawImage6(JNIEnv *env, jobject thisGraphics, jobject image, jint dx1, jint dy1, jint dx2, jint dy2, jint sx1, jint sy1, jint sx2, jint sy2, jobject color, jobject observer) {

  jboolean     result = FALSE;
  r_component  component = getWotsitField(thisGraphics, F_Graphics_wotsit);
  r_image      img;
  r_canvas     canvas;
  r_buffer     buffer;
  w_int        dx = 0;
  w_int        dy = 0;
  w_int        cw = thisGraphics[F_Graphics_cw];
  w_int        ch = thisGraphics[F_Graphics_ch];
  w_int        w;
  w_int        h;
 
  if(!image) {
    return FALSE;
  }
  
  if(observer) addObserver(env, image, observer);
  
  prepareImage(env, image, observer);
  
  img = getWotsitField(image, F_Image_wotsit);
  
  if(!img) return FALSE;
  
  w = img->w;
  h = img->h;

  if(!w || !h) return TRUE;
  
  rudolph_lock();
  
  check_overlap(component);
  buffer = r_getBuffer(component, &canvas, &dx, &dy, NULL, NULL);
  
  if(!buffer || !buffer->data) {
    rudolph_unlock();
    return FALSE;
  }

  if(sx2 < 0 && sy2 < 0) {
    sx2 = w - 1;
    sy2 = h - 1;
  }
  else if(sx2 < 0) {
    sx2 = (sy2 * w) / h;
  }
  else if(sy2 < 0) {
    sy2 = (sx2 * h) / w;
  }

  if(sx2 >= w) {
    if(sx1 == sx2) {
      rudolph_unlock();
      return TRUE;
    }
    dx2 = dx1 + ((dx2 - dx1) * (w - 1)) / (sx2 - sx1);
    sx2 = w - 1;
  }
  
  if(sy2 >= h) {
    if(sy1 == sy2) {
      rudolph_unlock();
      return TRUE;
    }
    dy2 = dy1 + ((dy2 - dy1) * (h - 1)) / (sy2 - sy1); 
    sy2 = h - 1;
  }

  if(dx2 < 0 && dy2 < 0) {
    dx2 = dx1 + w - 1;
    dy2 = dy1 + h - 1;
  }
  else if(dx2 < 0) {
    dx2 = dx1 + ((dy2 - dy1) * w) / h;
  }
  else if(dy2 < 0) {
    dy2 = dx2 + ((dx2 - dx1) * h) / w;
  }

  if(dx1 == dx2 || dy1 == dy2) {
    rudolph_unlock();
    return TRUE;
  }
  
  if(cw > 0 && ch > 0) {

    /*
    ** setClip is used.
    */
    
    w_int cx = thisGraphics[F_Graphics_cx];
    w_int cy = thisGraphics[F_Graphics_cy];
    
   if (dx1 < cx) {
      sx1 += ((cx - dx1) * (sx2 - sx1)) / (dx2 - dx1); 
      dx1 = cx;
    }
    if (dy1 < cy) {
      sy1 += ((cy - dy1) * (sy2 - sy1)) / (dy2 - dy1);
      dy1 = cy;
    }
    if (dx2 > cx + cw - 1) {
      sx2 -= ((dx2 - cx - cw + 1) * (sx2 - sx1)) / (dx2 - dx1);
      dx2 = cx + cw - 1;
    }
    if (dy2 > cy + ch - 1) {
      sy2 -= ((dy2 - cy - ch + 1) * (sy2 - sy1)) / (dy2 - dy1);
      dy2 = cy + ch - 1;
    }
  }

  if(sx2 > 0 && sy2 > 0) {
    dx1 += getIntegerField(thisGraphics, F_Graphics_ox) + dx;
    dy1 += getIntegerField(thisGraphics, F_Graphics_oy) + dy;
    dx2 += getIntegerField(thisGraphics, F_Graphics_ox) + dx;
    dy2 += getIntegerField(thisGraphics, F_Graphics_oy) + dy;

    if(image[F_Image_flags] & 0x80000000) {

      /* Restore background */

      w_instance colorInstance;
      r_pixel pixel;
      w_int i;

      Component_getBackground(component, &colorInstance, NULL);
      pixel = color2pixel(colorInstance);

      for (i = dy1; i < dy2; i++) {
        drawHLine(buffer, dx1, i, dx2 - dx1 + 1, pixel);
      }
    }

    /*
    ** 'x - 1' and 'y - 1' are to compensate for a bug in jdk...
    ** They seem to have a problem with going from x1 and x2 to x and width...
    */
    
    result = Image_drawImage(buffer, img, dx1, dy1, dx2 - 1, dy2 - 1, sx1, sy1, sx2 - 1, sy2 - 1);

    if(canvas) Graphics_postUpdate(canvas);
  }
  
  rudolph_unlock();

  return result;
}


/*
** Draw a quarter of an oval. This should be replaced once drawArc is doing the
** right thing.
*/

w_void r_drawOvalQuarter(r_buffer buffer, w_int x, w_int y, w_int w, w_int h, w_int color, w_int q) {
  w_int a = w / 2;
  w_int b = h / 2;
  w_int a2 = a * a;
  w_int b2 = b * b;
  w_int fa2 = 4 * a2;
  w_int fb2 = 4 * b2;
  w_int sigma = 2 * b2 + a2 * (1 - 2 * b);
  w_int xi, yi;
  
  x += w / 2;
  y += h / 2;
  
  for(xi = 0, yi = b; b2 * xi <= a2 * yi; xi++) {
    if(q == 4) drawPixelClipped(buffer, x + xi, y + yi, color);
    if(q == 3) drawPixelClipped(buffer, x - xi, y + yi, color);
    if(q == 1) drawPixelClipped(buffer, x + xi, y - yi, color);
    if(q == 2) drawPixelClipped(buffer, x - xi, y - yi, color);
    
    if(sigma >= 0) {
      sigma += fa2 * (1 - yi);
      yi--;
    }
    sigma += b2 * (4 * xi + 6);
  }
  
  sigma = 2 * a2 + b2 * (1 - 2 * a);
  
  for(xi = a, yi = 0; a2 * yi <= b2 * xi; yi++) {
    if(q == 4) drawPixelClipped(buffer, x + xi, y + yi, color);
    if(q == 3) drawPixelClipped(buffer, x - xi, y + yi, color);
    if(q == 1) drawPixelClipped(buffer, x + xi, y - yi, color);
    if(q == 2) drawPixelClipped(buffer, x - xi, y - yi, color);
    
    if(sigma >= 0) {
      sigma += fb2 * (1 - xi);
      xi--;
    }
    sigma += a2 * (4 * yi + 6);
  }
}

/*
** Draw a quarter of an oval and fill it. This should be replaced once fillArc is doing the
** right thing.
*/

w_void r_fillOvalQuarter(r_buffer buffer, w_int x, w_int y, w_int w, w_int h, w_int color, w_int q) {
  w_int a = w / 2;
  w_int b = h / 2;
  w_int a2 = a * a;
  w_int b2 = b * b;
  w_int fa2 = 4 * a2;
  w_int fb2 = 4 * b2;
  w_int sigma = 2 * b2 + a2 * (1 - 2 * b);
  w_int xi, yi;
  
  x += w / 2;
  y += h / 2;
  
  for(xi = 0, yi = b; b2 * xi <= a2 * yi; xi++) {
    if(q == 4) drawLine(buffer, x, y, x + xi, y + yi, color);
    if(q == 3) drawLine(buffer, x, y, x - xi, y + yi, color);
    if(q == 1) drawLine(buffer, x, y, x + xi, y - yi, color);
    if(q == 2) drawLine(buffer, x, y, x - xi, y - yi, color);
    
    if(sigma >= 0) {
      sigma += fa2 * (1 - yi);
      yi--;
    }
    sigma += b2 * (4 * xi + 6);
  }
  
  sigma = 2 * a2 + b2 * (1 - 2 * a);
  
  for(xi = a, yi = 0; a2 * yi <= b2 * xi; yi++) {
    if(q == 4) drawLine(buffer, x, y, x + xi, y + yi, color);
    if(q == 3) drawLine(buffer, x, y, x - xi, y + yi, color);
    if(q == 1) drawLine(buffer, x, y, x + xi, y - yi, color);
    if(q == 2) drawLine(buffer, x, y, x - xi, y - yi, color);
    
    if(sigma >= 0) {
      sigma += fb2 * (1 - xi);
      xi--;
    }
    sigma += a2 * (4 * yi + 6);
  }
}

