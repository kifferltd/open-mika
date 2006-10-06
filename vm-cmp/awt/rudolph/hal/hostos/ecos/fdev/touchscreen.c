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


/* $Id: touchscreen.c,v 1.2 2005/06/14 09:46:03 cvs Exp $ */
#include <cyg/kernel/kapi.h>
#include <cyg/io/io.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "ts-mem.h"
#include "rudolph.h"
#include "canvas.h"
#include "Event.h"

//struct for the touchscreen event (is the same as ecos uses)
typedef struct ts_event{
    short button_state;
    short x, y;
    short _unused;
} ts_event;
// touchscreen handler
cyg_io_handle_t ts_handle; 


static char *mouse_path;

/*
** Somewhat ad-hoc function to set the path of the mouse (touchscreen)  device
*/
void mouse_set_path(char *s) {
  char *buf = allocMem(strlen(s) + 1);
  strcpy(buf, s);
  mouse_path = buf;
}

void mouse_init(void){
  const char* name = "/dev/ts";  // configure this name in the ecos configtool
  Cyg_ErrNo error;
  error = cyg_io_lookup(name,&ts_handle);
  if (error != ENOERR){
    if (error == ENOENT){
      wabort(9,"TOUCHSCREEN NOT FOUND\n");
    }
    else{
      wabort(9,"UNKNOWN ERROR WHEN INITIALISING TOUCHSCREEN\n");
    }
  }
}
                                

int mouse_poll(int *state, int *x, int *y) {

  static int ox = 0;
  static int oy = 0;
  static int state1 = 0;  // state 1 == true => mouse pressed
  static int state2 = 0;
  cyg_uint32 lengte = sizeof(struct ts_event);
  ts_event ts;
  ts.button_state = 33;    //NOT A VALID STATE (TO SEE WETHER THERE WAS AN EVENT)
  if (cyg_io_read(ts_handle,&ts,&lengte) != ENOERR){
  wabort(9,"ERROR READING TOUCHSCREEN DEVICE\n");
  }
  
  if(ts.button_state != 33){    //Handle the event
  state2 = state1;
    state1 = (ts.button_state == 4);
  
  // handle coordinates
  if(swap_display) {
          ox = ts.y;
          oy = screen->height - ts.x;
        } 
        else {
          ox = ts.x;
          oy = ts.y;
        }
  *x = ox;
      *y = oy;
  
  // handle the states
  if ((state1 > 0) && (state2 > 0)) {
          *state = R_EVENT_MOUSE_DRAGGED;
          return 1;
         }
     else if ((state1 > 0) && (state2 == 0)) {
          *state = R_EVENT_MOUSE_PRESSED;
          return 1;
      }
  else if ((state1 == 0) && (state2 > 0)) {
          *state = R_EVENT_MOUSE_RELEASED;
    return 1;
      }
  else{
    woempa(9,"Should not get here\n");
    return 0;
  }
  }

  else{      // handle the non-event, just return 0
      return 0;
  }
}

void mouse_flush(void) {

}
