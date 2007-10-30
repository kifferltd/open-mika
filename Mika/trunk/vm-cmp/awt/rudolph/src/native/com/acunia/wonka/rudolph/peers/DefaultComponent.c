/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2007 by Chris Gray, /k/ Embedded Java Solutions.    *
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
#include "canvas.h"
#include "registry.h"
#include "rudolph.h"

#include "Component.h"
#include "Image.h"
#include "Event.h"

/*
** Auxiliary stuff.
*/

w_void invalidateTreeUpwards(r_component component, w_int type) {
  
  /*
  ** Invalidate parent container:
  */
  
  if (component->parent && component->instance && getBooleanField(component->instance, F_Component_visible)) {
    invalidateTreeUpwards(component->parent, type);
    // propagate invalidation through the component tree
  }
 
  /*
  ** Invalidate container:
  */
  
  if (component->refresh < (unsigned int)type) {
    component->refresh = (unsigned int)type;
    
    /*
    ** Note: make sure you don't overwrite component->refresh with 
    **       refresh events that are less important.  A global refresh 
    **       (type = 2) is - obviously - more important then a local 
    **       refresh (type = 1).
    */
  
  }
}            

w_void getRelativeCoordinates(r_component component, int *x, int *y, int *w, int *h) {
  
  *x = getIntegerField(component->instance, F_Component_x);
  *y = getIntegerField(component->instance, F_Component_y);
  *h = getIntegerField(component->instance, F_Component_height);
  *w = getIntegerField(component->instance, F_Component_width);
 
  if ((*x + *w < 0) || (*y + *h < 0) || ((*x) * (*y) > MAX_SIZE) || (*w < 0) || (*h < 0) || ((*w) * (*h) > MAX_SIZE)) {
    // Debug output:
    woempa(8, "coordinates completely out bounds: x = %i, y = %i, w = %i, h = %i\n", *x, *y, *w, *h);
    *x = 0; *y = 0; *h = 0; *w = 0;
  }
}

w_int Component_verifyBounds(r_component component) {

  /*
  ** Checks to see whether we have a valide instance:
  */
  
  int x = getIntegerField(component->instance, F_Component_x);
  int y = getIntegerField(component->instance, F_Component_y);
  int w = getIntegerField(component->instance, F_Component_width);
  int h = getIntegerField(component->instance, F_Component_height);
    
  if (x + w < 0 || y  + h < 0 || w < 1 || h < 1 || (x * y ) > MAX_SIZE || (w * h) > MAX_SIZE) {
    woempa(8, "coordinates completly out of bounds for %k '%p': x = %d, y = %d, w = %d, h = %d\n", (instance2clazz(component->instance)), component, x, y, w, h);
    return 0;
  }
  else {
    return 1;
  }
  
}

w_void Component_getForeground(r_component component, w_instance *colorInstance, w_instance defaultColor) {

  // Remark: don't call this function with a component whose field 'instance' is not itself a component

  if (component && component->instance && getReferenceField(component->instance, F_Component_foreground)) {

    /*
    ** Extract the color from the instance itself:
    */
    
    *colorInstance = getReferenceField(component->instance, F_Component_foreground);
  }
  else if (component && component->parent) {

    /*
    ** Look for the color of the parent component:
    */
    
    Component_getForeground(component->parent, colorInstance, defaultColor);
  }
  else {
    
    /*
    ** No color found, return the default color:
    */
    
    *colorInstance = defaultColor;
  }

}

w_int Component_isLightweight(r_component component) {
  return JNI_FALSE;
  /*
  if (component && component->instance) {
    return getBooleanField(component->instance, F_Component_lightweight);
  }
  else {
    return JNI_FALSE;
  }
  */
}

w_void Component_getBackground(r_component component, w_instance *colorInstance, w_instance defaultColor) {
  w_thread    thread = currentWonkaThread;
  JNIEnv      *env = w_thread2JNIEnv(thread);
  w_clazz     clazz = instance2clazz(component->instance);
  jclass      component_class = clazz2Class(clazz);
  jthrowable  exception;
  jmethodID   method;

  method = (*env)->GetMethodID(env, component_class, "getBackground", "()Ljava/awt/Color;");

  *colorInstance = (*env)->CallObjectMethod(env, component->instance, method);
  exception = (jthrowable)((*env)->ExceptionOccurred(env));
  if(exception) {
    (*env)->ExceptionClear(env);
    component_class = (*env)->GetObjectClass(env, exception);
    method = (*env)->GetMethodID(env, component_class, "printStackTrace", "()V");
    (*env)->CallVoidMethod(env, exception, method);
  }

  if(!*colorInstance) *colorInstance = defaultColor;

  (*env)->DeleteLocalRef(env, *colorInstance);
}
  
w_instance Component_getColor(int slot) {
  
  return (w_instance)getStaticReferenceField(clazzSystemColor, slot);

}

w_void Component_getFont(r_component component, r_font *font) {

  // Remark: don't call this function with a component whose field 'instance' is not itself a component

  if (component && component->instance && getReferenceField(component->instance, F_Component_font)) {
    w_instance fontInstance = getReferenceField(component->instance, F_Component_font);

    /*
    ** Extract the font from the instance itself:
    */
    
    *font = getWotsitField(fontInstance, F_Font_wotsit);
  }
  else if (component && component->parent) {
    
    /*
    ** Look for the font of the parent component:
    */
    
    Component_getFont(component->parent, font);
  }
  else {
    
    /*
    ** No font found, return the default font:
    */
    
    *font = getWotsitField(defaultFont, F_Font_wotsit);
  }
}

w_void Component_getFontInstance(r_component component, w_instance *fontInstance) {

  // Remark: don't call this function with a component whose field 'instance' is not itself a component

  if (component && component->instance && getReferenceField(component->instance, F_Component_font)) {

    /*
    ** Extract the font from the instance itself:
    */
   
    *fontInstance = getReferenceField(component->instance, F_Component_font);

  }
  else if (component && component->parent) {
  
    /*
    ** Look for the font of the parent component:
    */

    Component_getFontInstance(component->parent, fontInstance);
  }
  else {

    /*
    ** No font found, return the default font:
    */

    *fontInstance = defaultFont;
  }
}

w_void Component_intersect(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2, int *xr, int *yr, int *wr, int *hr) {
 
  int x3 = x1 + w1;
  int y3 = y1 + h1;
  int x4 = x2 + w2;
  int y4 = y2 + h2;
  
  if (x1 < x2) {
    *xr = x2;
  }
  else {
    *xr = x1;
  }
  
  if (y1 < y2) {
    *yr = y2;
  }
  else {
    *yr = y1;
  }
  
  if (x3 < x4) {
    *wr = x3 - *xr;
  }
  else {
    *wr = x4 - *xr;
  }

  if (y3 < y4) {
    *hr = y3 - *yr;
  }
  else {
    *hr = y4 - *yr;
  }

  if (*wr <= 0 || *hr <= 0) {
    *xr = 0;
    *yr = 0;
    *wr = 0;
    *hr = 0;
  }

  /*
  if(x1 < 0) {
    woempa(9, "x1: %d, y1: %d, w1: %d, h1: %d, x2: %d, y2: %d, w2: %d, h2: %d, xr: %d, yr: %d, wr: %d, hr: %d\n",
           x1, y1, w1, h1, x2, y2, w2, h2, *xr, *yr, *wr, *hr);
  }
  */

}

w_void getAbsoluteCoordinates(r_component component, int *dx, int *dy, int *w, int *h, int *ox, int *oy, int depth) {

  r_canvas canvas = NULL;

  int ix;
  int iy;
  int ih;
  int iw;
  
  int rx;
  int ry;
  int rw;
  int rh;
  
  if(component == rootCanvas->component) {
    rx = 0;
    ry = 0;
    rw = screen->width;
    rh = screen->height;
  } 
  else {
    rx = getIntegerField(component->instance, F_Component_x);      // relative x coordinate
    ry = getIntegerField(component->instance, F_Component_y);      // relative y coordinate
    rw = getIntegerField(component->instance, F_Component_width);  // relative w coordinate
    rh = getIntegerField(component->instance, F_Component_height); // relative h coordinate
  }
  
  if (depth == 0) {
    
    if (component->tag == Z_CONTAINER) {
      canvas = (r_canvas)component->object;

      *w = canvas->buffer->vw;
      *h = canvas->buffer->vh;
      *ox = canvas->buffer->ox;
      *oy = canvas->buffer->oy;
    }
    else {
      
      *w = rw;
      *h = rh;

      *ox = 0;
      *oy = 0;
    }

    *dx = rx;
    *dy = ry;
    
  }
  else {

    canvas = (r_canvas)component->object;

    Component_intersect(canvas->buffer->ox + *ox, canvas->buffer->oy + *oy, canvas->buffer->vw, canvas->buffer->vh, 
                        *ox + *dx, *oy + *dy, *w, *h, &ix, &iy, &iw, &ih);

    if (iw && ih) {
      *ox = ix - *dx;
      *oy = iy - *dy;
      
      *dx += rx - canvas->buffer->ox;
      *dy += ry - canvas->buffer->oy;

      if(rx < 0) {
        iw += rx;
        *ox -= rx;
        *dx -= rx;
      }
      
      if(ry < 0) {
        ih += ry;
        *oy -= ry;
        *dy -= ry;
      }

      if(*dx < 0) *dx = 0;
      if(*dy < 0) *dy = 0;

    }
    
    *w = iw;
    *h = ih;
  }
  
  woempa(6, " %d b. dx = %d, dy = %d, ox = %d, oy = %d, w = %d, h = %d\n", depth, *dx, *dy, *ox, *oy, *w, *h);
  
  if (*h && *w) {
    if (component->parent) {
      getAbsoluteCoordinates(component->parent, dx, dy, w, h, ox, oy, depth + 1);
    }
  }
}

w_int Component_isVisible(r_component component) {
  
  while(component) {
    
    if(component == rootCanvas->component) {
      
      /*
      ** The root canvas is always visible:
      */
      
      return 1;
    }
    
    if(component->instance && !getBooleanField(component->instance, F_Component_visible)) {
      
      /*
      ** The component is explicitly marked as being invisible:
      */
      
      break;
    }
    
    /*
    ** Look at the parent component:
    */

    component = component->parent;
  }

  return 0;
}
  
/*
** Native methods.
*/

w_void Component_createPeer(JNIEnv *env, jobject thisPeer) {
 
  jobject componentInstance = (jobject)getReferenceField(thisPeer, F_DefaultComponent_component);
  r_component component = allocClearedMem(sizeof(r_Component));

  if (component) {
    component->tag = Z_COMPONENT;
    component->instance = componentInstance;
  }
  else {
    woempa(9, "failed to allocate memory for java.awt.Component\n");
  }
  
  // woempa(9, "called Component_constructor(%p, %k)\n", componentInstance, instance2clazz(componentInstance));
  
  setWotsitField(thisPeer, F_DefaultComponent_wotsit, component);
}

w_void Component_finalize(JNIEnv *env, jobject thisPeer) {

#ifdef DEBUG
  jobject thisComponent = getReferenceField(thisPeer, F_DefaultComponent_component);
#endif
  r_component component;
  r_component c;
  r_canvas canvas;

  woempa(1, "Finalize (%k, %p)\n", instance2clazz(thisComponent), thisComponent);

  /*
  ** Lock the component tree:
  */
  
  rudolph_lock();
  
  woempa(1, "Locked component tree for (%k, %p)\n", instance2clazz(thisComponent), thisComponent);

  /*
  ** Resolve the component:
  */
  
  component = getWotsitField(thisPeer, F_DefaultComponent_wotsit);

  if(!component) {
    // DB [01/05/2002]: Is this really needed?  If component is NULL, we might have hit a bug?
    woempa(9, "[DB] component is NULL in finalizer!?\n");
    rudolph_unlock();
    return;
  }

  clearWotsitField(thisPeer, F_DefaultComponent_wotsit);

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
  ** If the component is a java.awt.Container, release its off-screen buffers.
  */
  
  if (component->tag == Z_CONTAINER) {
    canvas = (r_canvas)component->object;
 
    if (canvas->buffer && canvas->buffer->data) {
      releaseRuBuDa(canvas->buffer->data);
    }
    
    if (canvas->buffer && canvas->buffer->alpha) {
      releaseMem(canvas->buffer->alpha);
    }
    
    if (canvas->buffer) {
      releaseMem(canvas->buffer);
    }
    
    releaseMem(canvas);
  }  
  
  /*
  ** Unregister component from the component tree:
  */

  registry_delComponent(component);

  /*
  ** Update the child components (if required):
  */

  for (c = component->headChild; c != NULL; c = c->next) {
    c->parent = NULL;
  }

  component->headChild = NULL;
  component->tailChild = NULL;

  /*
  ** Release the component's memory:
  */
  
  releaseMem(component);

  /*
  ** Unlock the component tree:
  */
  
  woempa(1, "Unlocking component tree for (%k, %p)\n", instance2clazz(thisComponent), thisComponent);
  
  rudolph_unlock();
}

w_int Component_getAbsX(JNIEnv *env, jobject thisPeer) {
  w_int dx, dy, w, h, ox, oy;
  r_component component = getWotsitField(thisPeer, F_DefaultComponent_wotsit);

  getAbsoluteCoordinates(component, &dx, &dy, &w, &h, &ox, &oy, 0);

  return dx - ox;
}

w_int Component_getAbsY(JNIEnv *env, jobject thisPeer) {
  w_int dx, dy, w, h, ox, oy;
  r_component component = getWotsitField(thisPeer, F_DefaultComponent_wotsit);

  getAbsoluteCoordinates(component, &dx, &dy, &w, &h, &ox, &oy, 0);

  return dy - oy;
}

jobject Component_createImage(JNIEnv *env, jobject thisPeer, jint width, jint height) {

  jobject thisComponent = getReferenceField(thisPeer, F_DefaultComponent_component);
  r_component component = getWotsitField(thisPeer, F_DefaultComponent_wotsit);
  w_thread thread = currentWonkaThread;
  w_instance imageInstance;
  r_image image;
  w_instance backgroundInstance = NULL;
  w_int i;
  r_pixel color;

  mustBeInitialized(clazzImage);

  if (width < 0 || height < 0) {
    width = 0;
    height = 0;
  }

  /*
  ** Create an image instance:
  */
  
  enterUnsafeRegion(thread);
  imageInstance = allocInstance_initialized(thread, clazzImage);
  enterSafeRegion(thread);
  if (imageInstance == NULL) {
    return NULL;
  }

  /*
  ** Create and initialize an r_image struct:
  */
  
  image = allocMem(sizeof(r_Image));
  if (image == NULL) {
    return NULL;
  }

  image->filename = NULL;
  image->x = 0;
  image->y = 0;
  image->w = width;
  image->h = height;

  /*
  ** Create and intialize an image r_component:
  */
  
  image->component = allocClearedMem(sizeof(r_Component));
  if (image->component == NULL) {
    releaseMem(image);
    return NULL;
  }

  image->component->tag = (r_Tag)Z_IMAGE;
  image->component->object = (unsigned long)image;
  image->component->instance = imageInstance;
  image->component->parent = component;

  /*
  ** Create and initialize an image r_buffer:
  */
  
  image->buffer = allocMem(sizeof(r_Buffer));
  if (image->buffer == NULL) {
    releaseMem(image->component);
    releaseMem(image);
    return NULL;
  }

  image->buffer->fw = width;
  image->buffer->fh = height;
  image->buffer->vw = width;
  image->buffer->vh = height;
  image->buffer->ox = 0;
  image->buffer->oy = 0;
  image->buffer->alpha = NULL;

  /*
  ** Create an image r_buffer data:
  */
  
  image->buffer->data = allocRuBuDa(pixels2bytes((width + 1) * (height + 1)));
  if (image->buffer->data == NULL) {
    releaseMem(image->component);
    releaseMem(image->buffer);
    releaseMem(image);
    return NULL;
  }

  /*
  ** Add the image to the instance:
  */
  
  if (imageInstance) {
    setWotsitField(imageInstance, F_Image_wotsit, image);
    setIntegerField(imageInstance, F_Image_width, image->w);
    setIntegerField(imageInstance, F_Image_height, image->h);
    setReferenceField(imageInstance, thisComponent, F_Image_component);
  }

  /*
  ** Resolve background color and draw background:
  */
 
  Component_getBackground(component, &backgroundInstance, Component_getColor(F_SystemColor_window));

  color = color2pixel(backgroundInstance);
  
  for (i = 0; i < image->h; i++) {
    drawHLine(image->buffer, 0, i, image->w, color);
  }

  woempa(8, "called Component_createImage(%p, %p, %d, %d)\n", env, thisComponent, width, height);

  return (w_instance)imageInstance;
}

w_instance Component_getGraphics(JNIEnv *env, jobject thisPeer) {

  w_thread thread = JNIEnv2w_thread(env);
  w_instance graphicalCntxt = NULL;
  w_instance fontInstance = NULL;
  w_instance foregroundInstance = NULL;
  w_instance backgroundInstance = NULL;
  
  r_component component = getWotsitField(thisPeer, F_DefaultComponent_wotsit);

  if(component == NULL) {
    return NULL;
  }

  mustBeInitialized(clazzGraphics);

  // while(iterator && iterator != rootCanvas->component) iterator = iterator->parent;
  
  // if (canvas_showing(component)) {

  // if (iterator) {
  if (1) {
  
    /*
    ** Allocate memory for graphical context:
    */
    
    enterUnsafeRegion(thread);
    graphicalCntxt = allocInstance_initialized(thread, clazzGraphics);
    enterSafeRegion(thread);

    if (graphicalCntxt) {
      
      /*
      ** Initialize graphical context:
      */
      
      setWotsitField(graphicalCntxt, F_Graphics_wotsit, component);
      setReferenceField(graphicalCntxt, component->instance, F_Graphics_component);

      Component_getFontInstance(component, &fontInstance);
      
      setReferenceField(graphicalCntxt, fontInstance, F_Graphics_font);

      Component_getForeground(component, &foregroundInstance, Component_getColor(F_SystemColor_windowText));
      setReferenceField(graphicalCntxt, foregroundInstance, F_Graphics_foreground);

      Component_getBackground(component, &backgroundInstance, Component_getColor(F_SystemColor_window));
      setReferenceField(graphicalCntxt, backgroundInstance, F_Graphics_background);

    }
    else {
      woempa(9, "Unable to allocate graphicalCntxt!\n");
    }
  
    return graphicalCntxt;
  }
  else {
    return NULL;
  }
  
}

w_void Component_tag(JNIEnv *env, jobject thisPeer, jint type, jboolean render) {

  r_component component = getWotsitField(thisPeer, F_DefaultComponent_wotsit);
  
  // Debug output:
  woempa(5, "called Component_tag(type = '%k', type = %i, render = %i\n", instance2clazz(getReferenceField(thisPeer, F_DefaultComponent_component)), type, render);

  /*
  ** Invalidation process:
  **  - type = 0 : up-to-date, no update needed
  **  - type = 1 : local update, fast, use the cached version of container
  **  - type = 2 : global update, slow, re-render all parent containers on path to rootCanvas
  */
  
  /*
  ** Invalidate parent components in component-tree (bottom-up):
  */

  canvas_overlap(component);

  if(component->refresh == 2) {
    type = 2;
  }
  
  invalidateTreeUpwards(component, type);

  if (render && type == 1) {
    canvas_drawComponent(component, 1);
  }

}
