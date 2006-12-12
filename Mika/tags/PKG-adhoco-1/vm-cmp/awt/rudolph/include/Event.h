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


/* $Id: Event.h,v 1.1 2005/06/14 08:48:24 cvs Exp $ */

#ifndef _EVENT
#define _EVENT

#include "rudolph.h"

/* 
** These value match the "static final int"'s in 
** java.awt.event.MouseEvent: 
*/

#define R_EVENT_KEY_FIRST       400
#define R_EVENT_KEY_TYPED       400
#define R_EVENT_KEY_PRESSED     401 
#define R_EVENT_KEY_RELEASED    402
#define R_EVENT_KEY_LAST        402

#define R_EVENT_MOUSE_FIRST     500
#define R_EVENT_MOUSE_CLICKED   500
#define R_EVENT_MOUSE_PRESSED   501 
#define R_EVENT_MOUSE_RELEASED  502
#define R_EVENT_MOUSE_MOVED     503
#define R_EVENT_MOUSE_ENTERED   504
#define R_EVENT_MOUSE_EXITED    505
#define R_EVENT_MOUSE_DRAGGED   506
#define R_EVENT_MOUSE_LAST      506
#define R_EVENT_MOUSE_RELEASED_AFTER_DRAG  510

#define R_EVENT_FOCUS_GAINED    1004
#define R_EVENT_FOCUS_LOST      1005

extern r_component latest_entered;
extern r_component latest_pressed;

typedef struct r_Event {
  r_Tag tag;
  int x;
  int y;
  int modifiers;
} r_Event;

r_component Event_getSourceComponent(r_canvas canvas, r_event event, w_int x, w_int y);
void Event_dispatchEvent(r_canvas canvas, r_event event);

void Event_addKeyEvent(w_int VK, w_int keychar, w_int mod, w_int pressed, w_instance source);
void Event_addMouseEvent(r_component source, r_event event);
void Event_addMouseMotionEvent(r_component source, r_event event);
void Event_addActionEvent(r_component source, r_event event);
void Event_addFocusEvent(r_component source, r_event event);

#endif
