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


#ifndef _PLATFORM_H
#define _PLATFORM_H

#include "rudolph.h"
#include "pixel.h" /* from awt/rudolph/hal/pixel/xxx */

typedef struct r_Screen {
  w_ubyte * video;
  int height;
  int width;
} r_Screen;

extern r_screen screen;

/*
** Screen driver prototypes:
*/
r_screen screen_init(void);
void screen_shutdown(void);
void screen_update(int, int, int, int);

/*
** Poll driver prototypes:
*/
void mouse_init(void);
void mouse_flush(void);
int mouse_poll(int *state, int *x, int *y);
int keyboard_init(void);
void keyboard_shutdown(void);
int keyboard_poll(int *VK, w_char *keychar, int *mod, int *pressed);
int keyboard_isMod(int VK);
#endif
