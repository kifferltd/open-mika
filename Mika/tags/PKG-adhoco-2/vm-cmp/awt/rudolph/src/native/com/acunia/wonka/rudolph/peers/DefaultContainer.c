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

/*
** $Id: DefaultContainer.c,v 1.1 2005/06/14 08:48:25 cvs Exp $
*/

#include "fields.h"

#include "awt-classes.h"
#include "rudolph.h"
#include "canvas.h"
#include "registry.h"
#include "Component.h"
#include "Event.h"

#include <stdio.h>

void Container_createPeer(JNIEnv *env, jobject thisPeer, jboolean nested) {

  w_instance containerInstance;
  r_canvas canvas;

  rudolph_lock();
    
  containerInstance = getReferenceField(thisPeer, F_DefaultComponent_component);
  canvas = canvas_constructor(containerInstance);
  if (!canvas) {

    return;

  }
  
  if (!nested) {
    
    /*
    ** Add the canvas to the root canvas in the registry or component tree:
    */
    
    registry_addContainer(rootCanvas, canvas, -1);
  }
  
  woempa(9, "called Container_createPeer(%k, canvas = %p, component = %p)\n", instance2clazz(containerInstance), canvas, canvas->component);
  
  setWotsitField(thisPeer, F_DefaultComponent_wotsit, canvas->component);
  
  rudolph_unlock();
}

void Container_addComponent(JNIEnv *env, jobject thisPeer, jobject componentInstance, jint pos) {
  
  w_instance containerPeer = getReferenceField(componentInstance, F_Component_peer);
  r_component component1 = getWotsitField(thisPeer, F_DefaultComponent_wotsit);
  r_component component2 = getWotsitField(containerPeer, F_DefaultComponent_wotsit);
  r_canvas canvas = (r_canvas)component1->object;

  woempa(9, "called Container_addComponent(%k [%p], %k [%p])\n", instance2clazz(getReferenceField(thisPeer, F_DefaultComponent_component)), canvas, instance2clazz(componentInstance), component2);
  
  /*
  ** Add the component to the registry or component tree:
  */
  
  registry_addComponent(canvas, component2, pos);

  //check_overlap(component2);
  // registry_dump(rootCanvas, 0);
  
  invalidateTreeUpwards(component2, component2->refresh);
}

void Container_addContainer(JNIEnv *env, jobject thisPeer, jobject componentInstance, jint pos) {
  
  w_instance containerPeer = getReferenceField(componentInstance, F_Component_peer);
  r_component component1 = getWotsitField(thisPeer, F_DefaultComponent_wotsit);
  r_component component2 = getWotsitField(containerPeer, F_DefaultComponent_wotsit);
  r_canvas canvas1 = (r_canvas)component1->object;
  r_canvas canvas2 = (r_canvas)component2->object;
      
  woempa(9, "called Container_addContainer(%k [%p], %k [%p])\n", instance2clazz(getReferenceField(thisPeer, F_DefaultComponent_component)), canvas1, instance2clazz(componentInstance), canvas2);

  /* 
  ** Add the container to the registry or component tree:
  */
  
  registry_addContainer(canvas1, canvas2, pos);
  
  //check_overlap(component2);
  invalidateTreeUpwards(component2, component2->refresh);
  
  // registry_dump(rootCanvas, 0);
}

void Container_removeComponent(JNIEnv *env, jobject thisPeer, jobject componentInstance) {
 
  w_instance componentPeer = getReferenceField(componentInstance, F_Component_peer); 
  r_component component = getWotsitField(componentPeer, F_DefaultComponent_wotsit);

  woempa(9, "component instance = %p : component = %p (%k)\n", componentInstance, component, instance2clazz(component->instance));

  /*
  ** Remove the component from the registry or component tree:
  */
   
  registry_delComponent(component);

  /*
  ** We flush the events:
  */
  
  if (component == latest_entered) {
    latest_entered = NULL;
  }

  if (component == latest_pressed) {
    latest_pressed = NULL;
  }

  /*
  ** If the removed component is a container, release its buffer
  ** and those of its children.
  */
  
  if(component->tag == Z_CONTAINER) {
    buffer_release((r_canvas)component->object);
  }
 
}

void Container_scale(JNIEnv *env, jobject thisPeer, jint width, jint height) {

  r_component component = getWotsitField(thisPeer, F_DefaultComponent_wotsit);
  r_canvas canvas = (r_canvas)component->object;
  
  /*
  ** Coalesce rescale request: if the component has been rescaled 
  ** (and thus released) already, we don't have to reallocate it
  ** again.
  */
  
  buffer_release(canvas);
  
  /*
  ** Validate the requested size and update the buffer fields.
  */
  
  if (width < 1 || height < 1 || (height * width) > MAX_SIZE) {

    woempa(10, "warning: refused to scale container '%k' with canvas = %p to size w = %i, h = %i)\n", instance2clazz(getReferenceField(thisPeer, F_DefaultComponent_component)), canvas, width, height);

    /*
    ** Some layout managers and user-defined layoutmanagers might attempt
    ** to scale to java.awt.Container's to invalid sizes; in such case we
    ** release the buffer and we set the buffer size to 0x0.
    */

    canvas->buffer->fw = 0;
    canvas->buffer->fh = 0;
    canvas->buffer->ox = 0;
    canvas->buffer->oy = 0;
    canvas->buffer->vw = 0;
    canvas->buffer->vh = 0;  
  }
  else {
    
    woempa(5, "called Container_scale(instance = %k, canvas = %p, w = %i, h = %i)\n", instance2clazz(getReferenceField(thisPeer, F_DefaultComponent_component)), canvas, width, height);
    
    canvas->buffer->fw = width;
    canvas->buffer->fh = height;
    canvas->buffer->ox = 0;
    canvas->buffer->oy = 0;
    canvas->buffer->vw = width;
    canvas->buffer->vh = height;  
  }
}

