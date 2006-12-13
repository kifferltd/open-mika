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
* Modifications copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions. Permission is hereby granted to reproduce, modify, and       *
* distribute these modifications under the terms of the Wonka Public      *
* Licence.                                                                *
**************************************************************************/

/* $Id: Dispatcher.c,v 1.6 2006/10/04 14:24:20 cvsroot Exp $ */

#include <string.h>
#include <jni.h>
#include "rudolph.h"
#include "dispatch.h"

#include "exception.h"
#include "hashtable.h"
#include "loading.h"
#include "oswald.h"

#include "registry.h"
#include "platform.h"
#include "awt-classes.h"

#include "Font.h"
#include "canvas.h"

/*
** Positions for the splash screen. If you don't want it centered,
** #define SPLASH_POSITION_HORIZONTAL and/or SPLASH_POSITION_VERTICAL
** in your splash.xpm .
*/
#define LEFT  -1
#define TOP   -1
#define RIGHT  1
#define BOTTOM 1
#define MIDDLE 0

/*
** This is where we define the images to be used for the clear pattern
** and the splash screen. If CLEAR_IMAGE/SPLASH_IMAGE is not defined,
** no code will be generated to clear the screen / draw the "splash" image.
** For example, you could include this code in DEBUG versions only.
*/
#ifdef DEBUG
#define CLEAR_IMAGE  "mika-clear.xpm"
#define SPLASH_IMAGE "mika-splash.xpm"
#endif

#if defined(CLEAR_IMAGE) || defined(SPLASH_IMAGE)
#include "palette.h"
#endif

#ifdef  CLEAR_IMAGE
#include CLEAR_IMAGE
#endif

#ifdef  SPLASH_IMAGE
#include SPLASH_IMAGE

#ifndef SPLASH_POSITION_HORIZONTAL
#define SPLASH_POSITION_HORIZONTAL MIDDLE
#endif

#ifndef SPLASH_POSITION_VERTICAL
#define SPLASH_POSITION_VERTICAL MIDDLE
#endif
#endif

x_monitor tree_lock;

extern char* awt_splash;

/*
** Prototype.
*/

static void do_clearscreen(w_ubyte *scr, int w, int h);
static void do_splashscreen(w_ubyte *scr, int w, int h);

void Dispatcher_init(JNIEnv *env, jobject thisObj, w_instance theEventQueue) {
  static int init = 1;
  w_instance tlock = NULL;
  
  if (init == 1) {
    
    /*
    ** Disable static initializer: 
    */
    
    init = 0;

    /*
    ** Initialize the lock:
    */
    
    if (mustBeInitialized(clazzComponent) == CLASS_LOADING_FAILED) {
      wabort(ABORT_WONKA, "AWT was unable to load class java.awt.Component: %e", exceptionThrown(JNIEnv2w_thread(env)));
    }

    tlock = (w_instance)getStaticReferenceField(clazzComponent, F_Component_lock);
    tree_lock = getMonitor(tlock);
    
    defaultEventQueue = theEventQueue;

    /*
    ** Make sure we have a default Toolkit:
    */
    
    if (mustBeInitialized(clazzToolkit) == CLASS_LOADING_FAILED) {
      wabort(ABORT_WONKA, "AWT was unable to load class java.awt.Toolkit: %e", exceptionThrown(JNIEnv2w_thread(env)));
    }
    
    /*
    ** Make sure we have system colors:
    */ 
    
    if (mustBeInitialized(clazzSystemColor) == CLASS_LOADING_FAILED) {
      wabort(ABORT_WONKA, "AWT was unable to load class java.awt.SystemColor: %e", exceptionThrown(JNIEnv2w_thread(env)));
    }

    /*
    ** Initialize screen:
    */
    
    woempa(9, "initialize video driver ...\n");
    screen = screen_init();
    if(!awt_splash) {
      do_clearscreen(screen->video, screen->width, screen->height);
      do_splashscreen(screen->video, screen->width, screen->height);
      screen_update(0, 0, 0, 0);
    }

    /*
    ** Initialize fonts:
    */

    // woempa(9, "initialize font engine ...\n");
    // Font_init();

    /*
    ** Initialize rootCanvas:
    */

    woempa(9, "initialize component registry ...\n");
    rootCanvas = registry_constructor(screen->width, screen->height);
  }    
}

#if defined(CLEAR_IMAGE) || defined(SPLASH_IMAGE)
/*
 * Construct a palette from the string array 'colors' with length 'ncolors'.
 * The result is returned as an array of int[ncolors]; this memory is
 * allocMem()ed, and it is the caller's responsibility to releaseMem() it
 * once the data is no longer needed.
 * Each string in 'colors[]' should be one of the following:
 * a) a named colour as defined in palette.h;
 * b) the string "transparent";
 * c) a "#" character followed by six hex digits.
 * In case b the output parameter '*transparent' is set to the index of this
 * string within the 'colors[]' array. If no "transparent" colour is found,
 * '*transparent' is set to -1.
 * Each entry in the output array is a pixel value as generated by rgb2pixel().
 * The entry corresponding to '*transparent' (if any) is zero.
 */
static int *make_palette(int ncolors, char *colors[], int *transparent) {
  int    i;
  char   **p;
  char   *col;
  int *new_palette = allocMem(ncolors * sizeof(int));
  
  *transparent = -1;
  for (i = 0; i < ncolors; ++i) {
    col = colors[i+i+1];
    if (col[0] == '#') {
      int c1 = col[1] > 'a' ? col[1] - 'a' + 10 : col[1] > 'A' ? col[1] - 'A' + 10 : col[1] - '0';
      int c2 = col[2] > 'a' ? col[2] - 'a' + 10 : col[2] > 'A' ? col[2] - 'A' + 10 : col[2] - '0';
      int c3 = col[3] > 'a' ? col[3] - 'a' + 10 : col[3] > 'A' ? col[3] - 'A' + 10 : col[3] - '0';
      int c4 = col[4] > 'a' ? col[4] - 'a' + 10 : col[4] > 'A' ? col[4] - 'A' + 10 : col[4] - '0';
      int c5 = col[5] > 'a' ? col[5] - 'a' + 10 : col[5] > 'A' ? col[5] - 'A' + 10 : col[5] - '0';
      int c6 = col[6] > 'a' ? col[6] - 'a' + 10 : col[6] > 'A' ? col[6] - 'A' + 10 : col[6] - '0';
      int r = c1 * 16 + c2;
      int g = c3 * 16 + c4;
      int b = c5 * 16 + c6;

      woempa(1, "color[%d] = %s = {%d, %d, %d}\n", i, col, r, g, b);
      new_palette[i] = rgb2pixel(r, g, b);
    }
    else if (strcmp(col, "transparent") == 0) {
      *transparent = i;
      new_palette[i] = 0;
    }
    else {
      p = palette;
      while (*p) {
        if (strncmp(col, *p, 32) ==0) {
          w_ubyte *rgb = *(p+1);
          new_palette[i] = rgb2pixel(rgb[0], rgb[1], rgb[2]);
          break;
        }
        p += 2;
      }
    }
    woempa(1, "palette[%d] = %16x\n", i, new_palette[i]);
  }

  return new_palette;
}
#endif

/*
 * Clear the screen by tiling an image to it.
 */
static void do_clearscreen(w_ubyte *scr, int w, int h) {
#ifdef CLEAR_IMAGE
  int x, y;
  int *clear_palette;
  int *clear_image;
  int transparent;
  w_int    i;

  /* Draw a test pattern rather than just blanking the screen; this can
  ** give us some easy clues as to why our new display type isn't working
  ** as it should.
  */

  /*
  ** First we look up the colour names and make an array of colours.
  ** (Note that "transparent" pixels will be treated as black).
  */
  clear_palette = make_palette(clear_ncolors, clear_colors, &transparent);
  /*
  ** Now use this to build a memory image of the clear pattern.
  */
  clear_image = allocMem(clear_width * clear_height * sizeof(int));
  switch(clear_chars_per_pixel) {
  case 1:
    for (x = 0; x < clear_width; ++x) {
      for (y = 0; y < clear_height; ++y) {
        w_ubyte  c = clear_pixels[y][x];
        for (i = 0; i < clear_ncolors; ++i) {
          if (*clear_colors[i+i] == c) {
            clear_image[x + clear_width * y] = clear_palette[i];
            break;
          }
        }
        woempa(1, "clear image[%d, %d] = %16x\n", x, y, clear_image[x + clear_width * y]);
      }
    }
    break;

  case 2:
    for (x = 0; x < clear_width; ++x) {
      for (y = 0; y < clear_height; ++y) {
        w_ubyte c0 = clear_pixels[y][x + x];
        w_ubyte c1 = clear_pixels[y][x + x + 1];
        for (i = 0; i < clear_ncolors; ++i) {
          if (clear_colors[i+i][0] == c0 && clear_colors[i+i][1] == c1) {
            clear_image[x + clear_width * y] = clear_palette[i];
            break;
          }
        }
        woempa(1, "clear image[%d, %d] = %16x\n", x, y, clear_image[x + clear_width * y]);
      }
    }
    break;

  default:
    printf("ERROR - clear pattern has %d chars pre pixel, must be 1 or 2\n", clear_chars_per_pixel);
  }

  /*
  ** Iterate over the whole screen, tiling the clear_image.
  */
  for (y = 0; y < h; y++) {
    for (x = 0; x < w; x++) {
      pixelset(clear_image[(x % clear_width) + clear_width * (y % clear_height)], scr, screen->width, x, y);
    }
  }

  releaseMem(clear_palette);
  releaseMem(clear_image);
#endif 
}

/*
 * Draw the "splash" image on top of the background drawn by do_clearscreen().
 */
static void do_splashscreen(w_ubyte *scr, int w, int h) {
#ifdef SPLASH_IMAGE
  int x, y, xoff, yoff, minx, maxx, miny, maxy;
  int *splash_palette;
  int transparent;
  w_int    i;

  /*
   * Calculate the offset required to justify the picture.
   */
  if (SPLASH_POSITION_HORIZONTAL == LEFT) {
    xoff = 0;
  }
  else if (SPLASH_POSITION_HORIZONTAL == RIGHT) {
    xoff = w - splash_width;
  }
  else {
    xoff = (w / 2) - (splash_width / 2);
  }
  if (SPLASH_POSITION_VERTICAL == TOP) {
   yoff = 0;
  }
  else if (SPLASH_POSITION_VERTICAL == BOTTOM) {
   yoff = h - splash_height;
  }
  else {
    yoff = (h / 2) - (splash_height / 2);
  }

  /*
   * Crop as necessary, so we don't write outside the frame buffer and cause
   * a kernel panic ...
   */
  if (xoff < 0) {
    minx = -xoff;
  }
  else {
    minx = 0;
  }
  if (splash_width + xoff > w) {
    maxx = w - xoff;
  }
  else {
    maxx = splash_width;
  }
  if (yoff < 0) {
    miny = -yoff;
  }
  else {
    miny = 0;
  }
  if (splash_height + yoff > h) {
    maxy = h - yoff;
  }
  else {
    maxy = splash_height;
  }
  woempa(1, "w = %d, h = %d, splash_width = %d, splash_height = %d\n", w, h, splash_width, splash_height);
  woempa(1, "xoff = %d, yoff = %d, minx = %d, maxx=%d, miny = %d, maxy = %d\n", xoff, yoff, minx, maxx, miny, maxy);

  /*
  ** As for the clear pattern, first look up the colours in our palette.
  */
  splash_palette = make_palette(splash_ncolors, splash_colors, &transparent);
  woempa(1, "transparent = %d\n", transparent);

  /*
  ** We don't tile this image, so no need for a temporary copy in memory.
  */
  switch(splash_chars_per_pixel) {
  case 1:
    for (y = miny; y < maxy; y++) {
      for (x = minx; x < maxx; x++) {
        w_ubyte  c = splash_pixels[y][x];
        for (i = 0; i < splash_ncolors; ++i) {
          if (*splash_colors[i+i] == c) {
            if (i != transparent) {
              pixelset(splash_palette[i], scr, w, x + xoff, y + yoff);
	    }
	    break;
          }
        }
      }
    }
    break;

  case 2:
    for (y = 0; y < splash_height; ++y) {
      for (x = 0; x < splash_width; ++x) {
        w_ubyte c0 = splash_pixels[y][x + x];
        w_ubyte c1 = splash_pixels[y][x + x + 1];
        for (i = 0; i < splash_ncolors; ++i) {
          if (splash_colors[i+i][0] == c0 && splash_colors[i+i][1] == c1) {
            if (i != transparent) {
              pixelset(splash_palette[i], scr, w, x + xoff, y + yoff);
	    }
	    break;
          }
        }
      }
    }
    break;

  default:
    printf("ERROR - splash pattern has %d chars pre pixel, must be 1 or 2\n", splash_chars_per_pixel);
  }
  
  releaseMem(splash_palette);
#endif
}

void init_awt(void) {

  /*
  ** Wonka Native Interface:
  */
  
  collectAWTFixups();
  loadAWTClasses();
}

void init_module(void) {
  woempa(9, "initializing AWT module\n");
  init_awt();
}

void clean_module(void) {
  woempa(9, "cleaning AWT module\n");
}

