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

/*
** $Id: Scanner.c,v 1.2 2006/06/01 13:22:19 cvs Exp $
*/

#include "platform.h"
#include "awt-classes.h"
#include "wstrings.h"

#include "canvas.h"
#include "Component.h"
#include "Event.h"

// TODO: put this in a header file "somewhere".
extern void mouse_set_path(char *s);

void Scanner_init(JNIEnv *env, jobject thisObj, w_instance mouseString) {
  w_string mouse_string;
  char *mouse_chars;

  mouse_string = String2string(mouseString);
  mouse_chars = string2UTF8(mouse_string, NULL);
  mouse_set_path(mouse_chars + 2);

  woempa(9, "initialize mouse driver ...\n");
  mouse_init();

  woempa(9, "initialize keyboard driver ...\n");
  keyboard_init();
}

static r_Event Event;
static r_event event = &Event;

static w_instance source;

void Scanner_poll(JNIEnv *env, jobject thisObj, jobject root) {
  
  static int down = 0;
  static int ox = 0;
  static int oy = 0;
  
  int state = 0;
  int x = 0;
  int y = 0;

  int     VK = 0;
  w_char  keychar = 0;
  int     mod = 0;
  int     pressed = 0;

  r_canvas canvas;

  /*
  ** Check the root.
  */

  if(!root) {
    canvas = rootCanvas;
  }
  else {
    r_component component = getWotsitField(root, F_Component_wotsit);
    canvas = (r_canvas)component->object;
  }
  
  while(keyboard_poll(&VK, &keychar, &mod, &pressed)) {
    /*
    ** Received a keyboard event.
    */


    if(pressed) {
      /*
      ** Get the component with focus.
      */
      source = getStaticReferenceField(clazzComponent, F_Component_focusComponent);
      Event_addKeyEvent(VK, (w_int)keychar, mod, R_EVENT_KEY_PRESSED, source);
      if(!keyboard_isMod(VK)) { 
        switch(VK) {
            case 27: // VK_ESCAPE
                Event_addKeyEvent(0, (w_int)keychar, mod, R_EVENT_KEY_TYPED, source);
                break;

            default:
                Event_addKeyEvent(VK, (w_int)keychar, mod, R_EVENT_KEY_TYPED, source);
         }
      }
    }
    else {
      Event_addKeyEvent(VK, (w_int)keychar, mod, R_EVENT_KEY_RELEASED, source);
    }
        
  }

  if (mouse_poll(&state, &x, &y)) {
      
    woempa(5, "received touchscreen event at (%i, %i): %i [P:%d, R:%d, D:%d]\n", 
           x, y, state, R_EVENT_MOUSE_PRESSED, R_EVENT_MOUSE_RELEASED, R_EVENT_MOUSE_DRAGGED);
      
    if (state == R_EVENT_MOUSE_RELEASED) {

      if (down == 1) {

        down = 0;

        event->tag = R_EVENT_MOUSE_RELEASED;
        event->x = x;
        event->y = y;
        event->modifiers = mod;
        Event_dispatchEvent(canvas, event);
      }
    }
    else if (state == R_EVENT_MOUSE_PRESSED) {

      if (down == 0) {

        down = 1;

        event->tag = R_EVENT_MOUSE_PRESSED;
        event->x = x;
        event->y = y;
        event->modifiers = mod;

        Event_dispatchEvent(canvas, event);
      }
    }
    else {

      if (abs(ox - x) > 5 || abs(oy - y) > 5) {

        if (down) {
          event->tag = R_EVENT_MOUSE_DRAGGED;

          event->x = x;
          event->y = y;
          event->modifiers = mod;

          Event_dispatchEvent(canvas, event);
        }
        else {
          event->tag = R_EVENT_MOUSE_MOVED;

          event->x = x;
          event->y = y;
          event->modifiers = mod;

          // Event_dispatchEvent(canvas, event);
        }

        ox = event->x;
        oy = event->y;
      }
    }
  }

  mouse_flush();
}

void Scanner_drain(JNIEnv *env, jobject thisClass) {
  int     VK;
  w_char  keychar;
  int     mod;
  int     pressed;
  int     state;
  int     x;
  int     y;

  while(keyboard_poll(&VK, &keychar, &mod, &pressed) || mouse_poll(&state, &x, &y));
}

/*
** Runs keyboard shutdown routine (regardless of whether the keyboard was
** initialized successfully). Could run other device shutdown routines if
** we had to, but at the moment it doesn't look like we do.
*/
void Scanner_shutdown(JNIEnv *env, jobject thisobj) {
  screen_shutdown();
  keyboard_shutdown();
}

