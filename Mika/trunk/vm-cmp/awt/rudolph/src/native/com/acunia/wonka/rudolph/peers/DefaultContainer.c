/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2005 by Chris Gray, /k/ Embedded Java Solutions.    *
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

