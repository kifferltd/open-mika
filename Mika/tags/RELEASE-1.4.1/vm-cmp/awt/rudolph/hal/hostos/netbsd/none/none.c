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


/* $Id: none.c,v 1.2 2005/06/14 09:46:04 cvs Exp $ */

#include <stdio.h>

#include "ts-mem.h"

#include "rudolph.h"
#include "canvas.h"
#include "Event.h"

r_screen screen;

/*
** These functions don't do very much when using no AWT device...
*/

extern w_ubyte *awt_args;

r_screen screen_init() {
  w_int width = 400;
  w_int height = 234;
  
  if(awt_args) {
    sscanf(awt_args, "%dx%d", &width, &height);
    woempa(9, "Screen geometry: %d, %d\n", width, height);
  }
  
  screen = allocMem(sizeof(r_Screen));
  screen->height = height;
  screen->width = width;
  screen->video = allocMem(screen->height * screen->width * sizeof(r_pixel));

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

w_int keyboard_isMod(w_int VK) {
  return 0;
}

w_int keyboard_poll(w_int *VK, w_char *keychar, w_int *mod, w_int *pressed) {
  return 0;
}

w_int keyboard_init() {
  return 0;
}

void keyboard_shutdown() {
}

