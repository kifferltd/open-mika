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


/* $Id: rudolph.c,v 1.2 2005/06/14 09:46:04 cvs Exp $ */

#include <X11/Xlib.h>
#include <X11/Xutil.h>
#include <X11/Xos.h>
#include <X11/keysym.h>
#include <X11/cursorfont.h>
#include <stdio.h>

#include "ts-mem.h"

#include "rudolph.h"
#include "canvas.h"
#include "Event.h"
#include "platform.h"

#if !defined(XSIM_HACK_BPP_16) && !defined(XSIM_HACK_BPP_4)
#  error Sorry, but the code in awt/rudolph/hal/hostos/netbsd/xsim/rudolph.c is not generic enough to handle any pixel type yet, only 4 and 16bpp.  So you should hack the aforementioned source file to support your new pixel size, and define a suitable XSIM_HACK_BPP constant, or (better still) take the hack out and make the code in the X simulator a bit more generic.  If this makes you shake your fist at the sky, I hereby promise to buy you a beer for not finishin g the job properly.
#endif

r_screen screen;

Display *display;
Window window;
x_mutex xlock;

static char *displayname;
static XImage *ximage;
static int bitmap_order;
static int color_depth;
static Visual *visual = NULL;
static w_byte *X_data;
static GC gc;
static int red_shift;
static int green_shift;
static int blue_shift;

  // NOTE: a lot of static variables to speed up the simulator
  //       and because X-programming is so 'clumpsy'.

static void x_create_window(void) {
  unsigned long bg_pixel = 0L;
  int pad = 0;
  int xscreen;
  Window root;
  XEvent e;
  XGCValues gcvalues;
  XSetWindowAttributes attr;
  XSizeHints *size_hints;
  XTextProperty windowName;
  XTextProperty *pWindowName = &windowName;
  XTextProperty iconName;
  XTextProperty *pIconName = &iconName;
  XWMHints *wm_hints;
  
  char *window_name = (char *)"Willy Wonka Graphical Emulator";
  char *icon_name = (char *)"Willy Wonka";

  bitmap_order = BitmapBitOrder(display);
  xscreen = DefaultScreen(display);
  color_depth = DisplayPlanes(display, xscreen);

  if (color_depth != 16 && color_depth != 24 && color_depth != 32) { 
    wabort(ABORT_WONKA, "ERROR: unsupported color depth"); 
  }

  root = RootWindow(display, xscreen);

  /* 
  ** Create an XWindow: 
  */

  /*
  ** Define which events we are interested in: 
  */
  
  attr.event_mask = ExposureMask | KeyPressMask | KeyReleaseMask | ButtonPressMask | ButtonReleaseMask | PointerMotionMask;

  window = XCreateWindow(display, root, 0, 0, (unsigned)screen->width, (unsigned)screen->height, 0, color_depth, InputOutput, visual, CWBorderPixel |  CWEventMask, &attr);

  if (window == None) { 
    woempa(10, "ERROR: XCreateWindow() failed\n");
  }
  
  if (!XStringListToTextProperty((char **) &window_name, 1, pWindowName)) {
    pWindowName = NULL;
  }
  
  if (!XStringListToTextProperty((char **) &icon_name, 1, pIconName)) {
    pIconName = NULL;
  }

  if ((size_hints = XAllocSizeHints()) != NULL) {
    
    /* 
    ** The window will not be resizable:
    */
    
    size_hints->flags = PMinSize | PMaxSize;
    size_hints->min_width = size_hints->max_width = screen->width;
    size_hints->min_height = size_hints->max_height = screen->height;
  }

  if ((wm_hints = XAllocWMHints()) != NULL) {
    wm_hints->initial_state = NormalState;
    wm_hints->input = True;
    wm_hints->flags = StateHint | InputHint;
  }

  XSetWMProperties(display, window, pWindowName, pIconName, NULL, 0, size_hints, wm_hints, NULL);

  XMapWindow(display, window);

  gc = XCreateGC(display, window, 0, &gcvalues);

  /* 
  ** Fill window with the specified background color:
  */
  
  bg_pixel = 0;
  XSetForeground(display, gc, bg_pixel);
  XFillRectangle(display, window, gc, 0, 0, (unsigned)screen->width, (unsigned)screen->height);

  /*
  ** Wait for first Expose event to do any drawing, then flush:
  */
  
  do {
    XNextEvent(display, &e);
  }
  while (e.type != Expose || e.xexpose.count);

  XFlush(display);

  /*
  ** Precalc shifts and pad:
  */

  if (color_depth == 16) {
    red_shift = 11;
    green_shift = 5;
    blue_shift = 0;
    pad = 16;
  } 
  else if (color_depth == 24 || color_depth == 32) {
    red_shift = 16;
    green_shift = 8;
    blue_shift = 0;
    pad = 32;
  }
#ifdef XSIM_HACK_BPP_4
  red_shift += 1;
  green_shift += 2;
  blue_shift += 1;
#endif 
  X_data = allocClearedMem ((w_size)(screen->width * screen->height * pad / 8));
  
  if (!X_data) { 
    woempa(10, "ERROR: unable to allocate image memory\n"); 
  }
  
  ximage = XCreateImage(display, visual, (unsigned)color_depth, ZPixmap, 0, (char *)X_data, (unsigned)screen->width, (unsigned)screen->height, pad, 0);

  if (!ximage) {
    woempa(10, "ERROR: XCreateImage() failed\n");
    releaseMem(X_data);
  }
  
  /* 
  ** To avoid testing the bitmap_order every pixel (or doubling the size of
  ** the drawing routine with a giant if-test), we arbitrarily set the byte
  ** order to MSBFirst and let Xlib worry about inverting things on little-
  ** endian machines (like Linux/x86, old VAXen, etc.)--this is not the most
  ** efficient approach (the giant if-test would be better), but in the
  ** interest of clarity, we take the easy way out... 
  */
  
  ximage->byte_order = MSBFirst;
}

/*
** Does nothing on X
*/
void mouse_set_path(char *s) {}

void emulator_init(void) {

  double default_display_exponent;      // whole display system 
  double LUT_exponent;                  // just the lookup table
  double CRT_exponent = 2.2;            // just the monitor
      
  displayname = (char *)NULL;

  LUT_exponent = 1.0;   
    // NOTE: assume no LUT: most PCs
      
  default_display_exponent = LUT_exponent * CRT_exponent;
    // NOTE: the defaults above give 1.0, 1.3, 1.5 and 2.2, respectively:
            
  /*
  ** Open X display:
  */
  
  display = XOpenDisplay(displayname);
  
  /*
  ** Debug output:
  */
  
  if (!display) { 
    wabort(ABORT_WONKA, "ERROR: can't open X display [%s]\n", displayname ? displayname : "default"); 
  }
  
  x_create_window();
}

void mouse_init(void) {
}

int mouse_poll(int *state, int *x, int *y) {
  XEvent e;
  int temp;

  x_mutex_lock(xlock, x_eternal);
  
  if (XCheckWindowEvent(display, window, ExposureMask | ButtonPressMask | ButtonReleaseMask | PointerMotionMask, &e)) {
    if (e.type == ButtonPress) {
      *state = R_EVENT_MOUSE_PRESSED;
      *x = e.xmotion.x;
      *y = e.xmotion.y;
    }
    else if (e.type == ButtonRelease) {
      *state = R_EVENT_MOUSE_RELEASED;
      *x = e.xmotion.x;
      *y = e.xmotion.y;
    }
    else if (e.type == MotionNotify) {
      *state = R_EVENT_MOUSE_MOVED;
      *x = e.xmotion.x;
      *y = e.xmotion.y;
    }
    else if (e.type == KeyPress) {
      woempa(9, "Key Pressed : %d\n", e.xkey.keycode);
    }
    else if (e.type == KeyRelease) {
      woempa(9, "Key Released : %d\n", e.xkey.keycode);
    }
    else if (e.type == Expose) {
      screen_update(0, 0, 0, 0);
    }
   
    if(swap_display && ((e.type == 4) || (e.type == 5) || (e.type == 6))) {
      temp = *x;
      *x = *y;
      *y = screen->height - temp;
    }

    x_mutex_unlock(xlock);
    
    return 1;
  }
  else {

    x_mutex_unlock(xlock);
    
    return 0;
  }
}

void mouse_flush(void) {
  XEvent e;
  int i = 0;

  x_mutex_lock(xlock, x_eternal);
  
  while(XCheckWindowEvent(display, window, PointerMotionMask, &e)) i++;

  if(i) {
    XPutBackEvent(display, &e);
  }
  
  x_mutex_unlock(xlock);
}

extern w_ubyte *awt_args;

r_screen screen_init() {
  w_int width = 400;
  w_int height = 234;
  
  if(awt_args) {
    sscanf(awt_args, "%dx%d", &width, &height);
    woempa(9, "Screen geometry: %d, %d\n", width, height);
  }
  
  screen = allocMem(sizeof(r_Screen));

  if(swap_display) {
    screen->height = width;
    screen->width = height;
  } 
  else {
    screen->height = height;
    screen->width = width;
  }
  
  screen->video = allocMem(pixels2bytes(screen->height * screen->width));

  emulator_init();
  
  screen->height = height;
  screen->width = width;

  xlock = allocMem(sizeof(x_Mutex));
  x_mutex_create(xlock);
  
  return screen;

}

void screen_shutdown(void) {
  if (screen) {
    if (screen->video) {
      releaseMem(screen->video);
    }
    releaseMem(screen);
    screen = NULL;
  }
}

void screen_update(int x1, int y1, int x2, int y2) {
  
  r_pixel rudolph_pixel;
  long xwindow_pixel;
  unsigned char * dest;
  unsigned char r;
  unsigned char g;
  unsigned char b;
  w_int row;
  w_int col;
  w_int height;
  w_int width;
  
  if (ximage == NULL) return;

  dest = ximage->data;  

  if(swap_display) {
    height = screen->width;
    width = screen->height;
  } 
  else {
    height = screen->height;
    width = screen->width;
  }
  
  for (row = 0;  row < height;  row++) {
    for (col = 0;  col < width;  col++) {
      /* 
      ** Resolve the pixels from rudolph's video memory: 
      */
      

#ifdef XSIM_HACK_BPP_4
      if (color_depth == 16)
      {
    int level = pixelget(screen->video, screen->width, col, row);
    *((unsigned short*)dest) = (level<<1) | (level<<5) | (level<<11);
    dest += 2;
      }
#endif
#ifdef XSIM_HACK_BPP_16
      rudolph_pixel = pixelget(screen->video, screen->width, col, row);

      if (color_depth == 16) {
        r = pixel2red(rudolph_pixel) >> 3;
        g = pixel2green(rudolph_pixel) >> 2;
        b = pixel2blue(rudolph_pixel) >> 3;
      } 
      else {
        r = pixel2red(rudolph_pixel);
        g = pixel2green(rudolph_pixel);
        b = pixel2blue(rudolph_pixel);
      }
    
      xwindow_pixel = ((r << red_shift) | (g << green_shift) | (b << blue_shift));

      if (color_depth == 16) {
        *dest++ = ((unsigned char *)&xwindow_pixel)[1];
        *dest++ = ((unsigned char *)&xwindow_pixel)[0];
      } 
      else if (color_depth == 24 || color_depth == 32) {
        *dest++ = ((unsigned char *)&xwindow_pixel)[3];
        *dest++ = ((unsigned char *)&xwindow_pixel)[2];
        *dest++ = ((unsigned char *)&xwindow_pixel)[1];
        *dest++ = ((unsigned char *)&xwindow_pixel)[0];
      }
#endif
    }
  }
 
  x_mutex_lock(xlock, x_eternal);
  
  XPutImage(display, window, gc, ximage, 0, 0, 0, 0, (unsigned)width, (unsigned)height);
  XFlush(display);
  
  x_mutex_unlock(xlock);
}

