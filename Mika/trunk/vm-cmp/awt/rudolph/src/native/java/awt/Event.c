/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2004 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                                    *
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

#include "awt-classes.h"
#include "checks.h"
#include "fields.h"
#include "wonkatime.h"

#include "canvas.h"
#include "registry.h"
#include "Component.h"
#include "Event.h"

r_component latest_entered = NULL;
r_component latest_pressed = NULL;
static w_int click = 0;

inline static int contains(int rx, int ry, int rw, int rh, int px, int py) {
  return ((rx < px) && (px < rx + rw) && (ry < py) && (py < ry + rh));
}

static void addFocusEvent(r_component source, r_event event);
static void addMouseEvent(r_component source, r_event event);
static void addMouseMotionEvent(r_component source, r_event event);

void Event_dispatchEvent(r_canvas canvas, r_event event) {

  static r_event e = NULL;
  r_component source = NULL;
  w_int x, y, w, h, ox, oy;
  static w_int dragging = 0;
    
  /*
  ** Resolve the source component:
  */

  rudolph_lock(); 

  if (canvas != rootCanvas) {
    getAbsoluteCoordinates(canvas->component, &x, &y, &w, &h, &ox, &oy, 0);
  }
  else {
    x = 0;
    y = 0;
  }

  source = Event_getSourceComponent(canvas, event, x, y);
  
  rudolph_unlock();

  if (latest_entered != source) {

    /*
    ** Initialize the event if not already done so:
    */
  
    if (e == NULL) {
      e = allocMem(sizeof(r_Event));
  
      if (e == NULL) {
        woempa(9, "unable to allocate memory for an event -- ignoring\n");
        return;
      }
    }
    
    /*
    ** Take care of "mouse entered" and "mouse exited" events:
    */
    
    if (latest_entered != NULL) {

      // Update the coordinates:
      e->x = event->x;
      e->y = event->y;
 
      // Dispatch a 'mouse exited' event:
      e->tag = R_EVENT_MOUSE_EXITED; 
      e->modifiers = event->modifiers;
      addMouseEvent(latest_entered, e);
      
      // Dispatch a 'focus lost' event:
      // 
      // e->tag = R_EVENT_FOCUS_LOST;      
      // addFocusEvent(latest_entered, e);
      // 
      
    }
    
    if (source != NULL) {
      w_instance component;
      
      // Update the coordinates:
      e->x = event->x;
      e->y = event->y;

      // Dispatch a 'mouse entered' event:
      e->tag = R_EVENT_MOUSE_ENTERED; 
      e->modifiers = event->modifiers;
      addMouseEvent(source, e);
      
      // Dispatch a 'focus lost' event to the current owner.
      e->tag = R_EVENT_FOCUS_LOST;
      component = (w_instance)getStaticReferenceField(clazzComponent, F_Component_focusComponent);
      if(component && component != source->instance) {
        jobject peer = (jobject)getReferenceField(component, F_Component_peer);
        addFocusEvent(getWotsitField(peer, F_DefaultComponent_wotsit), e);
      }
      
      // Dispatch a 'focus gained' event:
      if(component != source->instance) {
        e->tag = R_EVENT_FOCUS_GAINED;
        e->modifiers = event->modifiers;
        addFocusEvent(source, e);
      }
    }
    
  }

  /* 
  ** Should we move on or can we return directly?
  */

  if (source != NULL) {

    /*
    ** Update the static 'latest_entered' field for use above:
    */
    
    latest_entered = source;
    
    if (event->tag == R_EVENT_MOUSE_PRESSED) {
      latest_pressed = source;
      click = 1;
    }

    if (event->tag == R_EVENT_MOUSE_DRAGGED) {
      click = 0;
    }
    
    if (event->tag == R_EVENT_MOUSE_RELEASED && latest_pressed == source) {
      
      /*
      ** Pressed and released on the same component -> send a clicked event.
      */
     
      event->tag = R_EVENT_MOUSE_RELEASED;
      
      if(click) {
        addMouseEvent(source, event);
        event->tag = R_EVENT_MOUSE_CLICKED;
        click = 0;
      }
    }

    /*
    ** Mouse and MouseMotion events:
    */

    if (event->tag == R_EVENT_MOUSE_MOVED || event->tag == R_EVENT_MOUSE_DRAGGED) {
      addMouseMotionEvent(source, event);
      dragging = 1;
    }
    else {
      
      /*
      ** Custom or component specific events:
      */
    
      if (event->tag == R_EVENT_MOUSE_RELEASED && latest_pressed != NULL) {
        addMouseEvent(latest_pressed, event);
        event->tag = R_EVENT_MOUSE_LAST;
        addMouseEvent(latest_pressed, event);
        if(dragging) {
          dragging = 0;
          if(source != latest_pressed) {
            event->tag = R_EVENT_MOUSE_RELEASED_AFTER_DRAG;
            addMouseEvent(source, event);
          }
        }
        latest_pressed = NULL;
      } 
      else {
        /*
        ** A generic mouse event:
        */
        
        addMouseEvent(source, event);
      }
    }
  }
}

r_component Event_getSourceComponent(r_canvas canvas, r_event event, w_int ox, w_int oy) {

  int offset_x = 0;
  int offset_y = 0;
  r_component component;
  r_component source = NULL;
  r_canvas c;
  int x;
  int y;
  int w;
  int h;

  offset_x += ox;
  offset_y += oy;
  
  for (component = canvas->component->tailChild; component != NULL; component = component->prev) {
 
    if (Component_isVisible(component)) {

      // Resolve coordinates of current component:
      getRelativeCoordinates(component, &x, &y, &w, &h);
    
      if (contains(offset_x + x, offset_y + y, w, h, event->x, event->y)) {

        if (component->tag == Z_CONTAINER) {
          
          c = (r_canvas)component->object;
          
          offset_x += (x - c->buffer->ox);
          offset_y += (y - c->buffer->oy);
          
          source = Event_getSourceComponent(c, event, offset_x, offset_y);
          
          if (source == NULL) {
            source = component;
          }
          
          offset_x -= (x - c->buffer->ox);
          offset_y -= (y - c->buffer->oy);
        }
        else {
          source = component;
        }
      }
    }
  }
 
  if (source && !getBooleanField(source->instance, F_Component_eventsEnabled)) {
    return NULL;
  }
  else {
    return source;
  }
}

static void addMouseEvent(r_component source, r_event event) {

  static jmethodID method = NULL;
  w_thread thread = currentWonkaThread;
  JNIEnv *env = w_thread2JNIEnv(thread);
  w_instance mouseEvent;
  w_int x, y, w, h, dx, dy;

  enterUnsafeRegion(thread);
  mouseEvent = allocInstance_initialized(thread, clazzMouseEvent);
  enterSafeRegion(thread);

  if(!mouseEvent || !source) {
    return;
  }

  // Debug output:
  woempa(6, "called addMouseEvent(%p, %p): tag = %i\n", source, event, event->tag);

  if (method == NULL) {
    method = (*env)->GetMethodID(env, clazz2Class(clazzEventQueue), "postNativeEvent", "()V");
  }

  // Resolve relative event coordinates:
  getAbsoluteCoordinates(source, &x, &y, &w, &h, &dx, &dy, 0);

  // Create appropriate java.awt.event.MouseEvent:
  setIntegerField(mouseEvent, F_AWTEvent_id,  event->tag); 
  setIntegerField(mouseEvent, F_MouseEvent_x, event->x - x + dx);  // relative x coordinate
  setIntegerField(mouseEvent, F_MouseEvent_y, event->y - y + dy);  // relative y coordinate
  setIntegerField(mouseEvent, F_MouseEvent_clickCount, 1); 
  setIntegerField(mouseEvent, F_InputEvent_modifiers, event->modifiers); 
  setLongField(mouseEvent, F_InputEvent_timeStamp, (w_long)getNativeSystemTime());
  setReferenceField(mouseEvent, source->instance, F_EventObject_source);

  // Attach MouseEvent to EventQueue instance:
  setReferenceField(defaultEventQueue, mouseEvent, F_EventQueue_nativeAWTEvent);

  // Call method postNativeEvent:
  (*env)->CallVoidMethod(env, defaultEventQueue, method);
}

static void addMouseMotionEvent(r_component source, r_event event) {

  if (source && getReferenceField(source->instance, F_Component_mouseMotionListener)) {

    static jmethodID method = NULL;
    w_thread thread = currentWonkaThread;
    JNIEnv *env = w_thread2JNIEnv(thread);
    w_instance mouseEvent;
    w_int x, y, w, h, dx, dy;
  
    enterUnsafeRegion(thread);
    mouseEvent = allocInstance_initialized(thread, clazzMouseEvent);
    enterSafeRegion(thread);

    if(!mouseEvent) {
      
      return;

    }
  
    // Debug output:
    woempa(9, "called addMouseMotionEvent(%p, %p): tag = %i\n", source, event, event->tag);

    if (method == NULL) {
      method = (*env)->GetMethodID(env, clazz2Class(clazzEventQueue), "postNativeEvent", "()V");
    }

    // Resolve relative event coordinates:
    getAbsoluteCoordinates(source, &x, &y, &w, &h, &dx, &dy, 0);

    // Create appropriate java.awt.event.MouseEvent:
    setIntegerField(mouseEvent, F_AWTEvent_id, event->tag); 
    setIntegerField(mouseEvent, F_MouseEvent_x, event->x - x + dx);  // relative x coordinate
    setIntegerField(mouseEvent, F_MouseEvent_y, event->y - y + dy);  // relative y coordinate
    setIntegerField(mouseEvent, F_InputEvent_modifiers, event->modifiers);
    setLongField(mouseEvent, F_InputEvent_timeStamp, (w_long)getNativeSystemTime());
    setReferenceField(mouseEvent, source->instance, F_EventObject_source);

    // Attach MouseEvent to EventQueue instance:
    setReferenceField(defaultEventQueue, mouseEvent, F_EventQueue_nativeAWTEvent);
 
    // Call method postNativeEvent:
    (*env)->CallVoidMethod(env, defaultEventQueue, method);

  }
}

static void addFocusEvent(r_component source, r_event event) {
  w_thread thread = currentWonkaThread;

  if(source && event->tag == R_EVENT_FOCUS_GAINED) {
    setStaticReferenceField(clazzComponent, F_Component_focusComponentPrev, getStaticReferenceField(clazzComponent, F_Component_focusComponent), thread);
    setStaticReferenceField(clazzComponent, F_Component_focusComponent, source->instance, thread);
  }

  if (source && (getReferenceField(source->instance, F_Component_focusListener) || isAssignmentCompatible(instance2clazz(source->instance), clazzWindow))) {

    static jmethodID method = NULL;
    w_thread thread = currentWonkaThread;
    JNIEnv *env = w_thread2JNIEnv(thread);
    w_instance focusEvent;

    // Debug output:
    woempa(9, "called addFocusEvent(%p, %p)\n", source, event);
 
    enterUnsafeRegion(thread);
    focusEvent = allocInstance_initialized(thread, clazzFocusEvent);
    enterSafeRegion(thread);
 
    if (!focusEvent) {

      return;

    }

    if (method == NULL) {
      method = (*env)->GetMethodID(env, clazz2Class(clazzEventQueue), "postNativeEvent", "()V");
    }

    // Create appropriate java.awt.event.FocusEvent:
    setIntegerField(focusEvent, F_AWTEvent_id,  event->tag);
    setReferenceField(focusEvent, source->instance, F_EventObject_source);

    // Attach FocusEvent to EventQueue instance:
    setReferenceField(defaultEventQueue, focusEvent, F_EventQueue_nativeAWTEvent);

    // Call method postNativeEvent:
    (*env)->CallVoidMethod(env, defaultEventQueue, method);
  }  
}

void Event_addKeyEvent(w_int VK, w_int keychar, w_int mod, w_int pressed, w_instance source) {

  if (source /*&& getReferenceField(source, F_Component_keyListener)*/) {

    static jmethodID method = NULL;
    w_thread thread = currentWonkaThread;
    JNIEnv *env = w_thread2JNIEnv(thread);
    w_instance keyEvent;

    enterUnsafeRegion(thread);
    keyEvent = allocInstance_initialized(thread, clazzKeyEvent);
    enterSafeRegion(thread);

    if(!keyEvent) {
      
      return;

    }
  
    // Debug output:
    woempa(9, "called Event_addKeyEvent(%d, %d, %d, %d, %p)\n", VK, keychar, mod, pressed, source);

    if (method == NULL) {
      method = (*env)->GetMethodID(env, clazz2Class(clazzEventQueue), "postNativeEvent", "()V");
    }

    // Create appropriate java.awt.event.MouseEvent:
    setIntegerField(keyEvent, F_AWTEvent_id, pressed);

	if(pressed == R_EVENT_KEY_TYPED)
	{ 
		// proveo: need modification of all keyTyped methods of all java.awt peer classes
		//setIntegerField(keyEvent, F_KeyEvent_keyCode, 0);

		setIntegerField(keyEvent, F_KeyEvent_keyCode, VK);
	}
	else
	{
		setIntegerField(keyEvent, F_KeyEvent_keyCode, VK);
	}

	if((VK >= 65) && (mod & 2))
	{
		if(mod & 1)
		{
			setCharacterField(keyEvent, F_KeyEvent_keyChar, keychar-96+32);
		}
		else
		{
			setCharacterField(keyEvent, F_KeyEvent_keyChar, keychar-96);
		}
	}
	else
	{	
		setCharacterField(keyEvent, F_KeyEvent_keyChar, keychar);
	}

    setIntegerField(keyEvent, F_InputEvent_modifiers, mod);
    setLongField(keyEvent, F_InputEvent_timeStamp, (w_long)getNativeSystemTime());
    setReferenceField(keyEvent, source, F_EventObject_source);

    // Attach KeyEvent to EventQueue instance:
    setReferenceField(defaultEventQueue, keyEvent, F_EventQueue_nativeAWTEvent);
 
    // Call method postNativeEvent:
    (*env)->CallVoidMethod(env, defaultEventQueue, method);
  }
}

