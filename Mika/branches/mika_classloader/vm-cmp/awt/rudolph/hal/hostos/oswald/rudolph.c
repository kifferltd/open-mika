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


/* $Id: rudolph.c,v 1.1 2005/12/12 22:00:09 cvs Exp $ */

#include "rudolph.h"
#include "canvas.h"
#include "Event.h"

r_screen screen;

void getTouchScreenInput(w_int*, w_int*, w_int*);

r_screen screen_init(void) {

  screen->height = 234;
  screen->width = 400;
  screen->video = (w_ubyte *)0x80000000;

  return screen;
            
}

void screen_shutdown(void) { }

void mouse_init(void) {
}

int mouse_poll(int *state, int *x, int *y) {

  static int prev = 0;
  static int next = 0;  

  getTouchScreenInput(&next, x, y);

  if (next == prev) {
    *state = R_EVENT_MOUSE_MOVED;
  }  
  else if (next == 1) {
    *state = R_EVENT_MOUSE_PRESSED;
  }
  else {
    *state = R_EVENT_MOUSE_RELEASED;
  }
  
  prev = next;
  
  return *x && *y;

}

void mouse_flush(void) {
}
