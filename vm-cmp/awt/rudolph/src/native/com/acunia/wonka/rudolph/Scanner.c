/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2005, 2007 by Chris Gray, /k/ Embedded Java         *
* Solutions. All rights reserved.                                         *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

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

