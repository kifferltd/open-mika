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
* Modifications copyright (c) 2005, 2006 by Chris Gray, /k/ Embedded Java *
* Solutions. Permission is hereby granted to reproduce, modify, and       *
* distribute these modifications under the terms of the Wonka Public      *
* Licence.                                                                *
**************************************************************************/

/* $Id: touchscreen.c,v 1.3 2006/05/14 08:34:30 cvs Exp $ */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <fcntl.h>
#include <sys/time.h>
#include <unistd.h>
#include <errno.h>
#include <sys/ioctl.h>
#include <unistd.h>
#include <stdio.h>
#include <fcntl.h>

#include "ts-mem.h"
#include "rudolph.h"
#include "canvas.h"
#include "Event.h"
#include "asyncio.h"
#include "network.h"

typedef struct {
  short pressure;  /* 0 = up, 1 = down */
  short xpos;
  short ypos;
  short pad;
} ts_event;

static ts_event ts;
static char *mouse_path;
static int mfd = -1;
static int cached_event = 0;
extern w_boolean awt_zoom;
extern w_boolean awt_virtual;
#ifdef AWT_VIRTUAL_SCREEN_SUPPORT
extern w_int awt_real_width;
extern w_int awt_real_height;
extern w_int awt_virtual_width;
extern w_int awt_virtual_height;
#endif

void mouse_flush_internal(void);

static int retry_counter = 0;
static int last_attempt = 1;

/*
** Somewhat ad-hoc function to set the path of the mouse (touchscreen)  device
*/

void mouse_set_path(char *s) {
  char *buf = allocMem(strlen(s) + 1);
  strcpy(buf, s);
  mouse_path = buf;
}

void mouse_init(void) {

  if(mfd > 0) return;
  
  woempa(9, "called mouse_init()\n");
    
  mfd = open(mouse_path, O_NONBLOCK);
  if (mfd == -1) {
    if(last_attempt) w_dump("error: failed to initialize mouse or touchscreen (%s)\n", mouse_path);
    last_attempt = 0;
  }
  else {
    woempa(9, "succesfully initialized mouse or touchscreen\n");
    last_attempt = 1;
  }
  //if(x_async_register(mfd)) {
  //  wabort(ABORT_WONKA, "error: failed set the mouse or touchscreen to asynchronous IO (%s)\n", mouse_path);
  //}
}

void mouse_close(void) {
  if(mfd > 0) {
    x_async_unregister(mfd);
    close(mfd);
    mfd = -1;
  }
}

int mouse_poll(int *state, int *x, int *y) {

  static int ox = 0;
  static int oy = 0;
  static int dragcount = 0;
  static int state1 = 0;
  static int state2 = 0;
  struct timeval tv;
  int result;

  if(mfd <= 0) {
    retry_counter++;
    if(retry_counter % 64 == 0) mouse_init();
    if(mfd <= 0) 
      return 0;
  }
 
  tv.tv_sec = 0;
  tv.tv_usec = 0;
 
  if(!cached_event) {
    ts.pressure = 0;
    ts.xpos = 0;
    ts.ypos = 0;
  }

  if(1) {
   
    if(!cached_event) {

      result = x_read(mfd, &ts, sizeof(ts));
      if(result == - 1) {
        if(errno == EINVAL || errno == EFAULT) { mouse_close(); return 0; }
        if(x_async_block(mfd, x_usecs2ticks(50000))) {
          return 0;
        }
        result = x_read(mfd, &ts, sizeof(ts));
        if(result == -1) {
          if(errno == EINVAL || errno == EFAULT) { mouse_close(); }
          return 0;
        }
      }
    }

    cached_event = 0;
  
    /*
    ** Update the states:
    */
    
    state2 = state1;
    state1 = (ts.pressure > 0);
   
    /*
    ** Handle the coordinates:
    */
        
    if (state1) {
      if(swap_display) {
        ox = ts.ypos;
        oy = screen->height - ts.xpos;
      } 
      else {
        ox = ts.xpos;
        oy = ts.ypos;
      }
    }

    if(awt_zoom) {
      *x = ox / 2;
      *y = oy / 2;
    }
#ifdef AWT_VIRTUAL_SCREEN_SUPPORT
    else if (awt_virtual) {
      *x = ox * awt_virtual_width / awt_real_width;
      *y = oy * awt_virtual_height / awt_real_height;
    }
#endif
    else {
      *x = ox;
      *y = oy;
    }
    
    
    /*
    ** Map the states on Java states:
    */
    
    if (state1 == 0) {
      dragcount = 0;
      *state = R_EVENT_MOUSE_RELEASED;

      return 1;
    }
    else if (state2 == 0) {
      dragcount = 0;
      *state = R_EVENT_MOUSE_PRESSED;

      return 1;
    }
    else {
      mouse_flush_internal();

      dragcount++;

      if(dragcount > 5) {
        *state = R_EVENT_MOUSE_DRAGGED;
        return 1;
      }
      
      return 0;
    }
  }
  else {
    return 0;
  }        
}

void mouse_flush_internal(void) {
  int result = 0;
  
  if(mfd <= 0) {
    retry_counter++;
    if(retry_counter % 64 == 0) mouse_init();
    if(mfd <= 0) 
      return;
  }
  
  do {
    result = x_read(mfd, &ts, sizeof(ts));
    if(result > 0)  {
      cached_event = 1;
      if(ts.pressure == 0) {
        return;
      }
    }
    else if(errno == EINVAL || errno == EFAULT) 
    { 
      mouse_close(); 
    }
  } while(result > 0); 
}

void mouse_flush(void) {
}

