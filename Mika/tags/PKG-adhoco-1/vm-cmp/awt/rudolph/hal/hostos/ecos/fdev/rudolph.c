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


/* $Id: rudolph.c,v 1.2 2005/06/14 09:46:03 cvs Exp $ */
#include <cyg/kernel/kapi.h>
#include <cyg/io/io.h>
#include <cyg/hal/lcd_support.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "ts-mem.h"
#include "rudolph.h"
#include "canvas.h"
#include "Event.h"

r_screen screen;

void screen_shutdown(void) { }

r_screen screen_init(void) {
    struct lcd_info screen_info;
  woempa(9, "called screen_init()\n");
  
  // initialise the lcd screen for the compaq ipaq
  lcd_init(16);
  // get the info about the screen
  lcd_getinfo(&screen_info);

      woempa(9, "--------------------------------------------------------\n");
     woempa(9, "framebuffer screen specifications:\n");
      woempa(9, "   WIDTH %3d, HEIGHT: %3d, bits per pixel: %3d\n", screen_info.width, screen_info.height, screen_info.bpp);
      woempa(9, "   memory      : %p\n", screen_info.fb);
      woempa(9, "--------------------------------------------------------\n");

    /*
    ** Initialze screen structure:
    */

    screen = allocMem(sizeof(r_Screen));

    if(swap_display){
      screen->height = screen_info.width;
      screen->width = screen_info.height;
    } 
    else{
      screen->height = screen_info.height;
      screen->width = screen_info.width;
    }
    screen->video = (w_ubyte *)screen_info.fb;  // cast to avoid warnings at compile time 

    woempa(9, "finished screen_init()\n");
    
    return screen;
}

