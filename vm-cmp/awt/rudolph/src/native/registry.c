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
** $Id: registry.c,v 1.4 2006/05/28 15:04:57 cvs Exp $ 
*/

#include <string.h>

#include "ts-mem.h"
#include "wstrings.h"
#include "threads.h"
#include "oswald.h"
#include "loading.h"
#include "awt-classes.h"
#include "dispatch.h"
#include "registry.h"
#include "platform.h"
#include "canvas.h"
#include "Component.h"

/*
** The registry is responsible for maintaining the internal component
** tree.  We should see how we can "merge" the component tree that is
** kept at the Java level (see java.awt.Component and Container) with
** this one in C.  [TODO]
*/

inline char *registry_spaces(w_int depth) {
  const char *spaces = "                                        ";  // 40 spaces
  return (char *)(spaces + strlen(spaces) - (2 * depth));
}

w_void w_dump_awt_tree(void) {
  registry_dump(rootCanvas, 0);
}
    
w_void registry_dump(r_canvas canvas, w_int depth) {

  r_component component;
  r_canvas c;

  if (canvas && canvas->component) {

    /*
    ** Mark the beginning of the component tree:
    */
    
    if (canvas == rootCanvas) { 
      w_dump("---------------- component tree -----------------\n");
    }

    /*
    ** Dump the component tree:
    */

    for (component = canvas->component->headChild; component != NULL; component = component->next) {
      if(component->tag == Z_CONTAINER) {
        c = (r_canvas)component->object;
        w_dump("%scontainer '%k' (%p) of size %dx%d (%d bytes), refresh %d, visible: %s, valid: %s\n", registry_spaces(depth), instance2clazz((w_instance)component->instance), component, c->buffer->fw, c->buffer->fh, (c->buffer->data ? c->buffer->fw * c->buffer->fh * sizeof(r_pixel) : 0), component->refresh, getBooleanField(component->instance, F_Component_visible) ? "y" : "n", getBooleanField(component->instance, F_Component_valid) ? "y": "n");
        registry_dump(c, depth + 1);
      }
      else {
        w_dump("%sregular component '%k' (%p) (bounds: %d, %d, %d, %d - visible: %s, valid: %s)\n", registry_spaces(depth), instance2clazz(component->instance), component, getIntegerField(component->instance, F_Component_x), getIntegerField(component->instance, F_Component_y), getIntegerField(component->instance, F_Component_width), getIntegerField(component->instance, F_Component_height), getBooleanField(component->instance, F_Component_visible) ? "y" : "n", getBooleanField(component->instance, F_Component_valid) ? "y" : "n");
      }
    }
    
    /*
    ** Mark the end of the component tree:
    */
    
    if (canvas == rootCanvas) {
      w_dump("-------------------------------------------------\n");
    }
  }
}

r_canvas registry_constructor(int w, int h) {
  r_canvas canvas;
  r_buffer buffer;
  
  /*
  ** Allocate memory for off-screen buffer:
  */
  
  buffer = allocMem(sizeof(r_Buffer));
  if (!buffer) {
    return NULL;
  }

  buffer->data = allocRuBuDa((pixels2bytes(w * h)));
  if (!buffer->data) {
    releaseMem(buffer);
    return NULL;
  }

  buffer->fw = w;
  buffer->fh = h;
  buffer->vw = w;
  buffer->vh = h;
  buffer->ox = 0;
  buffer->oy = 0;
  
  /*
  ** Initialize canvas:
  */
  
  canvas = (r_canvas)allocClearedMem(sizeof(r_Canvas)); 
  if (!canvas) {
    releaseRuBuDa(buffer->data);
    releaseMem(buffer);
    return NULL;
  }

  canvas->buffer = buffer;
    
  /*
  ** Link in component tree:
  */
  canvas->component = allocClearedMem(sizeof(r_Component));
  if (!canvas->component) {
    releaseMem(canvas);
    releaseRuBuDa(buffer->data);
    releaseMem(buffer);
    return NULL;
  }

  canvas->component->tag = Z_CONTAINER;
  canvas->component->object = (unsigned long) canvas;
  canvas->component->refresh = 0;

  canvas->buffer = buffer;  

  return canvas;
}

w_void registry_addContainer(r_canvas parentCanvas, r_canvas panel, w_int pos) {  

  woempa(5, "Add Container : %p (child: %p)\n", panel->component, panel->component->headChild);

  /*
  ** Link component in component tree:
  */
  
  panel->component->tag = Z_CONTAINER;
  panel->component->refresh = 2;
  panel->component->object = (unsigned long) panel;
  panel->component->parent = parentCanvas->component;
  panel->component->next = NULL;

  if (parentCanvas->component->headChild == NULL) {
    panel->component->prev = NULL;
    parentCanvas->component->headChild = panel->component;
    parentCanvas->component->tailChild = panel->component;
  }
  else {
    if(pos != -1) {
      r_component iter = parentCanvas->component->headChild;
      for(; pos > 0 && iter != NULL; pos--) iter = iter->next;
      if(iter) {
        panel->component->prev = iter->prev;
        iter->prev = panel->component;
        panel->component->next = iter;
        if(panel->component->prev) {
          panel->component->prev->next = panel->component;
        }
        else {
          parentCanvas->component->headChild = panel->component;
        }
      }
      else {
        pos = -1;
      }
    }
    if(pos == -1) {
      parentCanvas->component->tailChild->next = panel->component;
      panel->component->prev = parentCanvas->component->tailChild;
      parentCanvas->component->tailChild = panel->component;
    }
  }
}

w_void registry_addComponent(r_canvas canvas, r_component component, w_int pos) {

  woempa(5, "Add Component : %p\n", component);

  /*
  ** Link component in component tree:
  */
  
  component->parent = canvas->component;
  component->headChild = NULL;
  component->tailChild = NULL;
  component->next = NULL;

  if (canvas->component->headChild == NULL) {
    component->prev = NULL;
    canvas->component->headChild = component;
    canvas->component->tailChild = component;
  }
  else {
    if(pos != -1) {
      r_component iter = canvas->component->headChild;
      for(; pos > 0 && iter != NULL; pos--) iter = iter->next;
      if(iter) {
        component->prev = iter->prev;
        iter->prev = component;
        component->next = iter;
        if(component->prev) {
          component->prev->next = component;
        }
        else {
          canvas->component->headChild = component;
        }
      }
      else {
        pos = -1;
      }
    }
    if(pos == -1) {
      component->prev = canvas->component->tailChild;
      canvas->component->tailChild->next = component;
      canvas->component->tailChild = component;
    }
  }
}

w_void registry_delComponent(r_component component) {

  /*
  ** Update the parent's headchild (if required):
  */

  if (component->parent && component->parent->headChild == component) {
    component->parent->headChild = component->next;
  }

  /*
  ** Update parent's tailchild (if required):
  */
  
  if (component->parent && component->parent->tailChild == component) {
    component->parent->tailChild = component->prev;
  }
  
  /*
  ** Update the next and previous (if required):
  */
  
  if (component->prev) {
    component->prev->next = component->next;
  }

  if (component->next) {
    component->next->prev = component->prev;
  }

  component->parent = NULL;
  component->next = NULL;
  component->prev = NULL;

}
