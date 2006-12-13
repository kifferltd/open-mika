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
* Modifications copyright (c) 2005 by Chris Gray, /k/ Embedded Java       *
* Solutions. Permission is hereby granted to reproduce, modify, and       *
* distribute these modifications under the terms of the Wonka Public      *
* Licence.                                                                *
**************************************************************************/


/* $Id: Rudolph.c,v 1.2 2005/06/14 09:46:04 cvs Exp $ */

#include "fields.h"
#include "wstrings.h"

#include "awt-classes.h"

#include "rudolph.h"
#include "console.h"
#include "platform.h"
#include "varia.h"
#include "canvas.h"

#include "Component.h"
#include "Event.h"

void Rudolph_console(JNIEnv *env, jobject thisInstance, jobject stringInstance) {
  w_string string = String2string(stringInstance);
  char *text = allocMem((string_length(string) + 1) * sizeof(char));
  
  if (text) {
    w_string2c_string(string, text);
  
    console_PutString(text);
  
    releaseMem(text);
  }
}

void Rudolph_click(JNIEnv *env, jobject thisInstance, jobject componentInstance) {
  r_event event;
  int x = 0;
  int y = 0;
  int w = 0;
  int h = 0;
  int dx = 0;
  int dy = 0;
  
  /* Resolve absolute screen coordinates of component: */
  getAbsoluteCoordinates(getWotsitField(componentInstance, F_Component_wotsit), &x, &y, &w, &h, &dx, &dy, 0);

  /* Create and dispatch event #1: */ 
  event = (r_event)allocMem(sizeof(r_Event));
  if (!event) {
    return;
  }

  event->tag = R_EVENT_MOUSE_PRESSED;
  event->x = x + w / 2;
  event->y = y + h / 2;
  Event_dispatchEvent(rootCanvas, event); 
  releaseMem(event);

  x_thread_sleep(10);
    
  /* Create and dispatch event #2: */ 
  event = (r_event)allocMem(sizeof(r_Event));
  if (!event) {
    return;
  }

  event->tag = R_EVENT_MOUSE_RELEASED;
  event->x = x + w / 2;
  event->y = y + h / 2;
  Event_dispatchEvent(rootCanvas, event); 
  releaseMem(event);
}
