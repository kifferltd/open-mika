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
**************************************************************************/

/*
** $Id: DefaultWindow.c,v 1.1 2005/06/14 08:48:25 cvs Exp $ 
*/

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

