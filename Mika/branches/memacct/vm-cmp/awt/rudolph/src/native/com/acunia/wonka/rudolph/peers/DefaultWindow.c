/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Punch Telematix. All rights reserved. *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

#include "fields.h"

#include "awt-classes.h"
#include "canvas.h"
#include "Component.h"
#include "registry.h"

void Window_toFront(JNIEnv * env, w_instance thisPeer) {

#ifdef DEBUG
  jobject windowInstance = (jobject)getReferenceField(thisPeer, F_DefaultComponent_component);
#endif
  r_component component;
    
  rudolph_lock();

  component = getWotsitField(thisPeer, F_DefaultComponent_wotsit);  
  
  if((component == NULL) || (rootCanvas == NULL) || ((rootCanvas->component == NULL) || (rootCanvas->component->tailChild == NULL)))
  {
    rudolph_unlock();      
    return;
  }
  
  woempa(9, "called Window_toFront(windowInstance = %p): component = %p\n", windowInstance, component);
  
  if (rootCanvas->component->headChild == component) {
    rudolph_unlock();
    return;
  }

  if (component->prev) {
    component->prev->next = component->next;
  }
 
  if (component->next) {
    component->next->prev = component->prev;
  }

  /*
  ** Update rootCanvas' tailchild (if required):
  */

  if (rootCanvas->component->tailChild == component) {
    rootCanvas->component->tailChild = rootCanvas->component->tailChild->prev;
  }
  
  if (rootCanvas->component->headChild) {
    rootCanvas->component->headChild->prev = component;
    component->next = rootCanvas->component->headChild;
    component->prev = NULL;
    rootCanvas->component->headChild = component;
  }
 
  /*
  ** Redraw the components with a lower z-order:
  */
 
  if (rootCanvas->component->headChild) {
    canvas_drawCanvas((r_canvas)rootCanvas->component->headChild->object, 0);
  }
 
  rootCanvas->component->refresh = 2;

  rudolph_unlock(); 
}

void Window_toBack(JNIEnv * env, w_instance thisPeer) {

#ifdef DEBUG
  jobject windowInstance = (jobject)getReferenceField(thisPeer, F_DefaultComponent_component);
#endif
  r_component component;
  
  rudolph_lock();

  component = getWotsitField(thisPeer, F_DefaultComponent_wotsit);

  woempa(9, "called Window_toBack(windowInstance = %p): component = %p\n", windowInstance, component);
  
  if (rootCanvas->component->tailChild == component) {
    rudolph_unlock();
    return;
  }
  
  if (component->prev) {
    component->prev->next = component->next;
  }
 
  if (component->next) {
    component->next->prev = component->prev;
  }
  
  /*
  ** Update rootCanvas' headchild (if required):
  */

  if (rootCanvas->component->headChild == component) {
    rootCanvas->component->headChild = rootCanvas->component->headChild->next;
  }
  
  if (rootCanvas->component->tailChild) {
    rootCanvas->component->tailChild->next = component;
    component->prev = rootCanvas->component->tailChild;
    component->next = NULL;
    rootCanvas->component->tailChild = component;
  }
  
  /*
  ** Redraw the components with a higher z-order:
  */

  if (component->next) {
    canvas_canvas2screen((r_canvas)component->next->object);
  }
 
  rootCanvas->component->refresh = 2;

  rudolph_unlock();  
}

void Window_disposePeer(JNIEnv *env, jobject thisPeer) {
  
  jobject windowInstance;
  r_component component;
  
  rudolph_lock();

  windowInstance = (jobject)getReferenceField(thisPeer, F_DefaultComponent_component);
  component = getWotsitField(thisPeer, F_DefaultComponent_wotsit);
  
  woempa(9, "called Window_disposePeer(windowInstance = %p): component = %p\n", windowInstance, component);

  /*
  ** Remove the component from the registry or component tree:
  */
  
  registry_delComponent(component);
  
  /*
  ** Redraw the components with a lower z-order:
  */
  
  
  if (rootCanvas->component && rootCanvas->component->headChild) {
    canvas_drawCanvas((r_canvas)rootCanvas->component->headChild->object, 0); 
  }
  
  rootCanvas->component->refresh = 2;

  rudolph_unlock();
}

void Window_relocatePeer(JNIEnv *env, jobject thisPeer) {
  
  r_component c;
  r_canvas canvas;
  
  rudolph_lock();
  
  c = getWotsitField(thisPeer, F_DefaultComponent_wotsit);
  
  woempa(9, "called Window_relocatePeer(thisPeer = %p): component = %p\n", thisPeer, c);
  
  /*
  ** Redraw the components with a lower z-order:
  */
  
  for (c = rootCanvas->component->headChild; c != NULL; c = c->next) {
    canvas = (r_canvas)c->object;
    if (getBooleanField(c->instance, F_Component_visible)) {
      if (canvas->buffer->data) {
        canvas_copyAll(rootCanvas, canvas);
      }
    }
  }

  canvas_canvas2screen(rootCanvas);
  
  // if (rootCanvas->component && rootCanvas->component->headChild) {
  //   canvas_drawCanvas((r_canvas)rootCanvas->component->headChild->object, 0); 
  // }  
  
  rootCanvas->component->refresh = 2;

  rudolph_unlock();
}

