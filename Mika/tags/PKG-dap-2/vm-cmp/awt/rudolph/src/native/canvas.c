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
* Modifications copyright (c) 2005, 2006 by Chris Gray, /k/ Embedded Java *
* Solutions. Permission is hereby granted to reproduce, modify, and       *
* distribute these modifications under the terms of the Wonka Public      *
* Licence.                                                                *
**************************************************************************/

/*
** $Id: canvas.c,v 1.6 2006/10/04 14:24:20 cvsroot Exp $ 
*/

#include <string.h>

#include "exception.h"
#include "fields.h"
#include "hashtable.h"
#include "methods.h"
#include "wordset.h"

#include "awt-classes.h"
#include "canvas.h" 
#include "registry.h"
#include "Component.h"
#include "Image.h"
 
r_canvas rootCanvas;
int native_paint;

/*
** The RuBuDa (Rudolph Buffer Data) cache is used to supply memory for
** Rudolph buffers. These tend to be rather large, and Mika's unified
** heap is prone to fragmentation, so we often encounter the situation
** where no single free block of N bytes exists, although there are 10N
** bytes free. The largest allocations will tend to be always of the same
** size or a small number of sizes, (full screen, full screen minus menu 
** bar, ...), so we just cache the largest ones which have been allocated
** once and are not currently needed.
*/

/*
** The maximum amount of memory we are allowed to hoard in our cache, as a
** precentage of x_mem_total().
*/
#define RUBUDA_CACHE_MAX_PER_CENT 20

/*
** The maximum amount of memory we are allowed to hoard in our cache, in bytes.
*/
#define RUBUDA_CACHE_MAX_BYTES 4000000

/*
** At runtime we will calculate and use the lesser of the two limits.
*/
static int rubuda_cache_max_bytes;

/*
** The total size of all buffers currently in the cache.
*/
static int rubuda_cache_current_bytes;

/*
** If there is no cached buffer available of the right size, we will use a
** larger one if available. RUBUDA_CACHE_MAX_WASTAGE specifies the maximum
** percentage of a buffer's memory which may be "wasted" in this way.
*/
#define RUBUDA_CACHE_MAX_WASTAGE 10

/*
** The free buffer list is held in a wordset as (address, size) pairs.
** Currently the list is not sorted; we don't expect it to terribly long.
*/
static w_wordset rubuda_cache_free_list;

/*
** Because we sometimes use buffers "wastefully", we need to remember what
** the full size of a buffer is/was. The rubuda_cache_sizes_hashtable is
** used to store this information. We also use its lock to guard the
** rubuda_cache_free_list.
*/
static w_hashtable rubuda_cache_sizes_hashtable;

/*
** The total number of bytes consumed by all the buffers in the hashtable.
** Not used by the algorithm, for statistical purposes only.
*/
#ifdef DEBUG
static int rubuda_cache_hashtable_total;
#endif

/*
** Buffers smaller than rubuda_cache_size_threshold are always allocated
** from the heap and are never cached.  RUBUDA_CACHE_SIZE_THRESHOLD is an
** initial value which may be adjusted upwards at runtime.
*/
#define RUBUDA_CACHE_SIZE_THRESHOLD 65536
static int rubuda_cache_size_threshold;

/*
** To allocate a RuBuDa buffer, we search the free list for a buffer which
** is big enough but not too big. If none is found, we allocate from heap.
*/
void *allocRuBuDa(w_size bytes) {
  void *buffer = NULL;
  int i;
  int j;
  int n = sizeOfWordset(&rubuda_cache_free_list) / 2;
  int true_size = 0x7fffffff;

  if (!rubuda_cache_size_threshold) {
    woempa(7, "Initialising static variables\n");
    // Note 1: no lock needed, 'cos allocation is always from same thread.(?)
    // Note 2: the first calculation is inaccurate but free from overflow.
    rubuda_cache_max_bytes = (x_mem_total() / 100) * RUBUDA_CACHE_MAX_PER_CENT;
    if (rubuda_cache_max_bytes > RUBUDA_CACHE_MAX_BYTES) {
      rubuda_cache_max_bytes = RUBUDA_CACHE_MAX_BYTES;
    }
    woempa(7, "rubuda_cache_max_bytes = %d\n", rubuda_cache_max_bytes);
    rubuda_cache_size_threshold = RUBUDA_CACHE_SIZE_THRESHOLD;
    woempa(7, "rubuda_cache_size_threshold = %d\n", rubuda_cache_size_threshold);
    rubuda_cache_sizes_hashtable = ht_create("hashtable:rubuda_sizes", 23, NULL, NULL, 0, 0);
  }

  if (bytes < rubuda_cache_size_threshold) {
    woempa(7, "Small buffer, allocating directly from heap\n");

    return allocMem(bytes);

  }

  woempa(7, "Requested size = %d bytes\n", bytes);
  ht_lock(rubuda_cache_sizes_hashtable);
  for (i = 0; i < n; ++i) {
    void *this_buffer = (void*)elementOfWordset(&rubuda_cache_free_list, i * 2);
    int this_size = (int)elementOfWordset(&rubuda_cache_free_list, i * 2 + 1);

    woempa(7, "Cache element[%d]: (%p, %d)\n", i, this_buffer, this_size);
    woempa(7, "  this_size >= bytes : %s\n", this_size >= bytes ? "T" : "F");
    woempa(7, "  (this_size - bytes) / (this_size / 100) < RUBUDA_CACHE_MAX_WASTAGE : %s\n", (this_size - bytes) / (this_size / 100) < RUBUDA_CACHE_MAX_WASTAGE ? "T" : "F");
    woempa(7, "  this_size < true_size : %s\n", this_size < true_size ? "T" : "F");
    if (this_size >= bytes && (this_size - bytes) / (this_size / 100) < RUBUDA_CACHE_MAX_WASTAGE && this_size < true_size) {
      buffer = this_buffer;
      true_size = this_size;
      j = i;
      woempa(7, "Success: buffer = %p, tru_size = %d, index = %d\n", buffer, true_size, j);
    }
  }

  if (buffer) {
    // Careful! removeWordetElementAt() replaces the removed word by the
    // last word in the set, so we need to do this "backwards".
    woempa(7, "Removing element [%d], adding %p->%d to hashtable\n", j, buffer, true_size);
    removeWordsetElementAt(&rubuda_cache_free_list, j * 2 + 1);
    removeWordsetElementAt(&rubuda_cache_free_list, j * 2);
#ifdef DEBUG
    rubuda_cache_hashtable_total += true_size - (int)
#endif
    ht_write_no_lock(rubuda_cache_sizes_hashtable, (w_word)buffer, (w_word)(true_size));
    woempa(7, "rubuda_cache_sizes_hashtable has %d entries totalling %d bytes\n", rubuda_cache_sizes_hashtable->occupancy, rubuda_cache_hashtable_total);
    rubuda_cache_current_bytes -= true_size;
  }
  woempa(7, "rubuda_cache contains %d bytes in %d buffers\n", rubuda_cache_current_bytes, sizeOfWordset(&rubuda_cache_free_list) / 2);
  ht_unlock(rubuda_cache_sizes_hashtable);

  if (!buffer) {
    woempa(7, "No suitable buffer found in cache, allocating from heap.\n");
    buffer = allocMem(bytes);
#ifdef DEBUG
    rubuda_cache_hashtable_total += bytes - (int)
#endif
    ht_write(rubuda_cache_sizes_hashtable, (w_word)buffer, (w_word)bytes);
    woempa(7, "rubuda_cache_sizes_hashtable has %d entries totalling %d bytes\n", rubuda_cache_sizes_hashtable->occupancy, rubuda_cache_hashtable_total);
  }

  woempa(7, "Returning %p\n", buffer);

  return buffer;
}

void releaseRuBuDa(void *buffer) {
  int size = ht_read(rubuda_cache_sizes_hashtable, (w_word)buffer);

  if (!size) {
    woempa(7, "Small buffer, releasing directly to heap\n");
    releaseMem(buffer);

    return;

  }

  ht_lock(rubuda_cache_sizes_hashtable);
  if (size + rubuda_cache_current_bytes > rubuda_cache_max_bytes) {
    void *victim = NULL;
    int victim_size;
    int smallest_so_far = 0x7fffffff;
    int i;
    int j;
    int n = sizeOfWordset(&rubuda_cache_free_list) / 2;

    // See if we could stay within the limit by displacing another buffer
    for (i = 0; i < n; ++i) {
      victim_size = (int)elementOfWordset(&rubuda_cache_free_list, i * 2 + 1);
      if (victim_size < smallest_so_far && size + rubuda_cache_current_bytes - victim_size > rubuda_cache_max_bytes) {
        victim = (void*)elementOfWordset(&rubuda_cache_free_list, i * 2);
        j = i;
      }
    }

    if (victim) {
      ht_erase_no_lock(rubuda_cache_sizes_hashtable, victim);
#ifdef DEBUG
      rubuda_cache_hashtable_total -= victim_size;
      woempa(7, "rubuda_cache_sizes_hashtable has %d entries totalling %d bytes\n", rubuda_cache_sizes_hashtable->occupancy, rubuda_cache_hashtable_total);
#endif
      releaseMem(victim);
      modifyElementOfWordset(&rubuda_cache_free_list, j * 2, buffer);
      modifyElementOfWordset(&rubuda_cache_free_list, j * 2 + 1, size);
      rubuda_cache_current_bytes += size - victim_size;
      woempa(7, "rubuda_cache contains %d bytes in %d buffers\n", rubuda_cache_current_bytes, sizeOfWordset(&rubuda_cache_free_list) / 2);
      if (victim_size > rubuda_cache_size_threshold * 4) {
        rubuda_cache_size_threshold = victim_size / 2;
        woempa(7, "rubuda_cache_size_threshold = %d\n", rubuda_cache_size_threshold);
      }
      ht_unlock(rubuda_cache_sizes_hashtable);

      return;
    }

    woempa(7, "Cache full, releasing directly to heap\n");
    ht_erase_no_lock(rubuda_cache_sizes_hashtable, (w_word)buffer);
#ifdef DEBUG
    rubuda_cache_hashtable_total -= size;
    woempa(7, "rubuda_cache_sizes_hashtable has %d entries totalling %d bytes\n", rubuda_cache_sizes_hashtable->occupancy, rubuda_cache_hashtable_total);
#endif
    releaseMem(buffer);
    ht_unlock(rubuda_cache_sizes_hashtable);

    return;

  }

  addToWordset(&rubuda_cache_free_list, (w_word)buffer);
  addToWordset(&rubuda_cache_free_list, (w_word)size);
  rubuda_cache_current_bytes += size;
  woempa(7, "rubuda_cache contains %d bytes in %d buffers\n", rubuda_cache_current_bytes, sizeOfWordset(&rubuda_cache_free_list) / 2);
  ht_unlock(rubuda_cache_sizes_hashtable);
}

r_canvas canvas_constructor(w_instance canvasInstance) {

  w_thread thread = currentWonkaThread;
  w_instance peerInstance;
  r_buffer buffer;
  r_canvas canvas;
     
  /*
  ** Initialize the off-screen buffer.  We use lazy allocation or
  ** JIT buffer allocation.  That is, we don't allocate memomy for
  ** the buffer now and from start, we mark it as being released.
  ** With Java's layout managers in mind, the container is likely
  ** going to be rescaled anyway.
  */
 
  buffer = allocClearedMem(sizeof(r_Buffer));
  if (!buffer) {

    return NULL;

  }
    
  /*
  ** Allocate memory for the canvas:
  */
  
  canvas = allocMem(sizeof(r_Canvas)); 
  if (!canvas) {
    releaseMem(buffer);

    return NULL;

  }
  
  /*
  ** Initialize the canvas:
  */
  
  canvas->buffer = buffer;
  
  /*
  ** Initialize the canvas specific parts of the component:
  */
  
  peerInstance = getReferenceField(canvasInstance, F_Component_peer);
  canvas->component = getWotsitField(peerInstance, F_DefaultComponent_wotsit);
  canvas->component->tag = Z_CONTAINER;
  canvas->component->object = (unsigned long)canvas;
  canvas->component->refresh = 2;
  removeLocalReference(thread, peerInstance);

  return canvas;

}

inline char *canvas_spaces(const int depth) {

  const char *spaces = "                                    ";  // 40 spaces
  return (char *)(spaces + strlen(spaces) - (2 * depth));

}

w_int canvas_showing(r_component c) {
  while(c) {
    if(c == rootCanvas->component) {
      return 1;
    }
    if(!getBooleanField(c->instance, F_Component_visible)) {
      break;
    }
    if(!c->parent) {
      break;
    }
    c = c->parent;
  }
 
  /*
  if(c && c->tag == Z_CONTAINER) {
    buffer_release((r_canvas)c->object);
  }
  */
  
  return 0;
}

w_int canvas_verifyBounds(r_component dst, r_component src) {

  if (dst && dst->instance && src && src->instance) {
    return Component_verifyBounds(dst) && Component_verifyBounds(src) && Container_verifyBounds(dst, src);
  }
  else if (src && src->instance) {
    return Component_verifyBounds(src);
  }
  else if (dst && dst->instance) {
    return Component_verifyBounds(dst);
  }
  else {
    return 0;
  }

  /*
  ** TODO: for performance's sake, most of these verification results 
  **       can be cached.
  */
  
}

void canvas_copyAll(r_canvas dstCanvas, r_canvas srcCanvas) {

  w_instance instance = srcCanvas->component->instance;
  w_ubyte * src;
  w_ubyte * dst;
  int x;
  int y;
  int w;
  int h;
  int i;
 
  if (!dstCanvas->buffer || !dstCanvas->buffer->data || !srcCanvas->buffer || !srcCanvas->buffer->data) {
    return;
  }

  /*
  ** Copy buffer to the parent buffer:
  */

  x = getIntegerField(instance, F_Component_x);
  y = getIntegerField(instance, F_Component_y);

  Component_intersect(x, y, srcCanvas->buffer->vw, srcCanvas->buffer->vh, 0, 0, dstCanvas->buffer->fw, dstCanvas->buffer->fh, &x, &y, &w, &h);

  src = srcCanvas->buffer->data + pixels2bytes(srcCanvas->buffer->fw * (srcCanvas->buffer->oy + y - getIntegerField(instance, F_Component_y)));
  dst = dstCanvas->buffer->data + pixels2bytes(dstCanvas->buffer->fw * y);
  
  woempa(5, "%d x %d  ->  %d x %d\n", w, h, dstCanvas->buffer->fw, dstCanvas->buffer->fh);
   
  for (i = 0; i < h; i++) {
    pixelcopyline(dst, src, x, (srcCanvas->buffer->ox + x - getIntegerField(instance, F_Component_x)), w);
    src += pixels2bytes(srcCanvas->buffer->fw);
    dst += pixels2bytes(dstCanvas->buffer->fw);
  }
}
    
void buffer_realloc(r_canvas canvas) {
  int width;
  int height;
  
  if (canvas && canvas->component && canvas->component->instance && canvas->buffer && canvas->buffer->data == NULL) {
   
    width = getIntegerField(canvas->component->instance, F_Component_width);
    height = getIntegerField(canvas->component->instance, F_Component_height);

    if (width < 1 || height < 1 || width * height < 1 || width * height > MAX_SIZE) { 
      width = 0;
      height = 0;
      canvas->buffer->data = NULL;
    }
    else {
  
      /*
      ** Allocate the canvas' buffer:
      */

      canvas->buffer->data = allocRuBuDa(pixels2bytes(width * height));
      if (canvas->buffer->data == NULL) {
        woempa(9, "failed to allocate buffer of size %dx%d (%d bytes) for %k!\n", width, height, pixels2bytes(width * height), instance2clazz(canvas->component->instance));
        wprintf("failed to allocate buffer of size %dx%d (%d bytes) for %k!\n", width, height, pixels2bytes(width * height), instance2clazz(canvas->component->instance));
        return;
      }
    }

    canvas->buffer->fw = width;
    canvas->buffer->fh = height;
    canvas->buffer->ox = 0;
    canvas->buffer->oy = 0;
    canvas->buffer->vw = width;
    canvas->buffer->vh = height;

    /*
    ** Debug output:
    */
      
    woempa(5, "allocated buffer of size %dx%d (%d bytes) for %k!\n", width, height, pixels2bytes(width * height), instance2clazz(canvas->component->instance));
    
    /*
    ** Mark the component as being invalid:
    */
    
    canvas->component->refresh = 2;
  }
}

void buffer_release(r_canvas canvas) {

  r_component component;

  /*
  ** Recursivly do the same thing for all child component:
  */

  if (rootCanvas != canvas && canvas->buffer && canvas->buffer->data) {//&& rootCanvas->component != canvas->component->parent) {

    /*
    ** Release the canvas' buffer :
    */

    releaseRuBuDa(canvas->buffer->data);
    if(canvas->buffer->alpha) releaseMem(canvas->buffer->alpha);
    
    /*
    ** Mark the buffer as being released:
    */
      
    canvas->buffer->data = NULL;
    canvas->buffer->alpha = NULL;

    /*
    ** Debug output:
    */

    woempa(5, "cache: release buffer %p (%dx%d, released %d bytes, %d bytes in use) of container '%k'\n", canvas, canvas->buffer->fw, canvas->buffer->fh, canvas->buffer->fw * canvas->buffer->fh * sizeof(r_pixel), x_mem_total() - x_mem_avail(), (instance2clazz(canvas->component->instance)));

    /*
    ** Mark the component as being invalid:
    */
    
    canvas->component->refresh = 2;
  }
  
  /*
  ** Release buffers from invisible children.
  */
  
  for (component = canvas->component->headChild; component != NULL; component = component->next) {
    if (component->tag == Z_CONTAINER && !getBooleanField(component->instance, F_Component_visible)) {
      buffer_release((r_canvas)component->object);
    }
  }
}

static inline w_int canvas_checkOverlap(r_component component, w_int *x1, w_int *y1, w_int *x2, w_int *y2) {
  w_int cx1 = getIntegerField(component->instance, F_Component_x);
  w_int cy1 = getIntegerField(component->instance, F_Component_y);
  w_int cx2 = cx1 + getIntegerField(component->instance, F_Component_width) - 1;
  w_int cy2 = cy1 + getIntegerField(component->instance, F_Component_height) - 1;

  //w_dump("    checking: %d %d %d %d - %d %d %d %d\n", cx1, cy1, cx2, cy2, *x1, *y1, *x2, *y2);
  
  if(!canvas_showing(component)) {
    return 0;
  }

  if(*x1 == cx1 && *y1 == cy1 && *x2 == cx2 && *y2 == cy2) {
    return 1;
  }

  if((*x1 - cx2 <= 0) && (*x2 - cx1 >= 0) && (*y1 - cy2 <= 0) && (*y2 - cy1 >= 0)) {
    // w_dump("OVERLAP %d %d %d %d - %d %d %d %d\n", cx1, cy1, cx2, cy2, *x1, *y1, *x2, *y2);
    if(cx1 < *x1) *x1 = cx1;
    if(cy1 < *y1) *y1 = cy1;
    if(cx2 > *x2) *x2 = cx2;
    if(cy2 > *y2) *y2 = cy2;
    return 1;
  }
  return 0;
}

void canvas_overlap(r_component component) {
  w_int x1, y1, x2, y2;
  r_component c;

  if(isSet(component->flags, RF_OVERLAP)) {
    return;
  }
  
  setFlag(component->flags, RF_OVERLAP);
  while(component && component->instance) {
    x1 = getIntegerField(component->instance, F_Component_x);
    y1 = getIntegerField(component->instance, F_Component_y);
    x2 = x1 + getIntegerField(component->instance, F_Component_width) - 1;
    y2 = y1 + getIntegerField(component->instance, F_Component_height) - 1;
    
    for(c = component->prev; c != NULL; c = c->prev) {
      if(canvas_checkOverlap(c, &x1, &y1, &x2, &y2)) {
        if(component->refresh != 2) {
          component->refresh = 2;
          invalidateTreeUpwards(component, 2);
          invalidateTreeUpwards(c, 2);
          c->refresh = 2;
        }
        else {
          invalidateTreeUpwards(c, 2);
          c->refresh = 2;
        }
      }
    }
    component = component->parent;
  }
}

void canvas_drawCanvas(r_canvas canvas, w_int depth) {

  r_canvas childCanvas;
  r_component c;

  woempa(5, "Drawing canvas (%d), %J\n", depth, canvas->component->instance);
  
  if(canvas != rootCanvas) {
    if(!(canvas->component && canvas->component->parent && canvas->component->instance && canvas_showing(canvas->component))) {
      return;
    }

    if(!canvas_verifyBounds(canvas->component->parent, canvas->component)) {
      return;
    }
  }

  rudolph_assert_monitor();

  #ifdef DEBUG
    if(canvas != rootCanvas) {
      woempa(5, "Refresh == %d for %j\n", canvas->component->refresh, canvas->component->instance);
    }
    else {
      woempa(5, "Refresh == %d for rootCanvas\n", canvas->component->refresh);
    }
  #endif

  switch(canvas->component->refresh) {
    case 2:

      /*
      ** The off-screen buffer is tagged invalid:
      **   not able to use the cached off-screen buffer - rerender the
      **   canvas instead
      */
   
      /*
      ** Fix the damaged area:
      */
      
      canvas_repare(canvas->component);
   
      /*
      ** Call the Containers's paint() method in Java:
      */
    
      canvas_paint(canvas->component);

      /*
      ** Iterate through the component tree to redraw components (if necessary):
      */

      for (c = canvas->component->tailChild; c != NULL; c = c->prev) {
        // canvas_overlap(c);
        if (canvas_verifyBounds(canvas->component, c)) {
          if (c->tag == Z_CONTAINER) {

            /*
            ** Resolve the child canvas:
            */
          
            childCanvas = (r_canvas) c->object;
          
            /*
            ** Debug output:
            */
           
            #ifdef DEBUG
              if (childCanvas && childCanvas->component && getBooleanField(childCanvas->component->instance, F_Component_visible) && childCanvas->component->refresh == 2) { 
                woempa(5, "%scanvas '%k' (canvas = %p, x = %i, y = %i, w = %i, h = %i) [invalid cache - render]\n", 
                          canvas_spaces(depth), (instance2clazz(childCanvas->component->instance)), childCanvas, 
                          getIntegerField(childCanvas->component->instance, F_Component_x), getIntegerField(childCanvas->component->instance, F_Component_y), 
                          getIntegerField(childCanvas->component->instance, F_Component_width), getIntegerField(childCanvas->component->instance, F_Component_height));
              }
            #endif
            
            /*
            ** Draw child canvas:
            */
          
            canvas_drawCanvas(childCanvas, depth + 1);
          }
          else {
          
            if (c && c->parent && canvas_showing(c)) {
              r_canvas can;
              w_int dummy;

              r_getBuffer(c, &can, &dummy, &dummy, NULL, NULL);

              if (can->buffer && can->buffer->data) {

                /*
                ** Redraw the component:
                */

                /*
                ** Fix the damaged area:
                */

                canvas_repare(c);

                /*
                ** Call the Component's paint() method in Java:
                */

                canvas_paint(c);

                if (can->component->refresh < 1) {
                  can->component->refresh = 1;
                }

                /*
                ** Mark the compent as up-to-date:
                */
                
                c->refresh = 0;
              }
            }
          }
        }     
      }        
      break;
 
    case 1:
      
      /*
      ** The off-screen buffer is still valid:
      **   no need to rerender the canvas - use the cached off-screen
      **   canvas instead
      */
  
      /*
      ** Iterate through the component tree to redraw components (if necessary):
      */
     
      for (c = canvas->component->tailChild; c != NULL; c = c->prev) {
        // canvas_overlap(c);
        if (c->tag == Z_CONTAINER) {
      
          childCanvas = (r_canvas) c->object;
    
          #ifdef DEBUG 
            if (childCanvas && childCanvas->component && childCanvas->component->refresh == 2) {
              woempa(5, "%scanvas '%p' (x = %i, y = %i, w = %i, h = %i) [invalid cache - render]\n", canvas_spaces(depth), childCanvas, 
                        getIntegerField(childCanvas->component->instance, F_Component_x), getIntegerField(childCanvas->component->instance, F_Component_y), 
                        getIntegerField(childCanvas->component->instance, F_Component_width), getIntegerField(childCanvas->component->instance, F_Component_height));
            }
            else { 
              woempa(5, "%scanvas '%p' (x = %i, y = %i, w = %i, h = %i) [valid cache - prune]\n", canvas_spaces(depth), childCanvas, 
                        getIntegerField(childCanvas->component->instance, F_Component_x), getIntegerField(childCanvas->component->instance, F_Component_y), 
                        getIntegerField(childCanvas->component->instance, F_Component_width), getIntegerField(childCanvas->component->instance, F_Component_height)); 
            }
          #endif
        
          if (childCanvas->component->refresh != 0) {  
            canvas_drawCanvas(childCanvas, depth + 1);
          }
        } 
      } 
      break;
  }

  /*
  ** Mark the Container as being refreshed:
  */
    
  canvas->component->refresh = 0;

  for(c = canvas->component->headChild; c != NULL; c = c->next) unsetFlag(c->flags, RF_OVERLAP);
  unsetFlag(canvas->component->flags, RF_OVERLAP);
  
  /*
  ** Visualize the Container's canvas:
  */
  
  if (canvas != rootCanvas && depth != 0) {
    r_canvas parentCanvas = (r_canvas)canvas->component->parent->object;
      
    /*
    ** Debug output:
    */
   
    #ifdef DEBUG
      if (parentCanvas == rootCanvas) { 
        woempa(5, "calling canvas_copyAll(%p = ROOTCANVAS, %p): buffer 1: %p, buffer 2: %p\n", parentCanvas, canvas, parentCanvas->buffer, canvas->buffer); 
      }
      else { 
        woempa(5, "calling canvas_copyAll(%p, %p): buffer 1: %p, buffer 2: %p\n", parentCanvas, canvas, parentCanvas->buffer, canvas->buffer); 
      }
    #endif
    
    /*
    ** Validate the parent buffer:
    */
    
    if (parentCanvas != rootCanvas) {
      buffer_realloc(parentCanvas);
    }
    
    canvas_copyAll(parentCanvas, canvas);
  }
  else {
    canvas_canvas2screen(canvas);
  }       
}

void canvas_drawComponent(r_component c, w_int display) {

  r_canvas canvas;
  w_int    dummy;
 
  rudolph_assert_monitor();

  if (c && canvas_showing(c)) {

    r_getBuffer(c, &canvas, &dummy, &dummy, NULL, NULL);
    
    if (canvas_verifyBounds(canvas->component, c) && canvas->buffer && canvas->buffer->data) {
    
      /*
      ** Redraw the component:
      */
      
      if (c->tag == Z_CONTAINER) {
        canvas->component->parent->refresh = 2;
        canvas->component->refresh = 2;
      }
      else {

        /*
        ** This is mandatory: leave this out and you'll face update/refresh
        ** problems ... if the canvas you are about to draw in is not up to
        ** date, make sure to update it first.
        **       
        ** if (display && canvas->component->refresh) {
        **   canvas_drawCanvas(canvas, 1);
        ** }
        */
        
        /*
        ** Fix the damaged area:
        */
   
        canvas_repare(c);

        /*
        ** Call the Component's paint() method in Java:
        */
        
        canvas_paint(c);

        /*
        ** Mark the compent as up-to-date:
        */
                
        c->refresh = 0;
         
        /*
        ** Update the screen when needed:
        */
     
        if (display) {
          canvas_canvas2screen(canvas);
        }
         
        if(canvas->component->refresh < 1) {
          canvas->component->refresh = 1;
        }
      }    
    }
  }
}

void canvas_repare(r_component component) {

  r_canvas   canvas;
  r_buffer   buffer;
  w_instance col;
  r_pixel    pixel;
  w_int      x = 0;
  w_int      y = 0;
  w_int      w, h, i;

  if (component->instance == NULL) {

    /*
    ** Do nothing, we are trying to fix the rootCanvas.
    */

    return;
  }
  
  /*
  ** Get the buffer and the region to clear.
  ** (This function also reallocates the buffer if needed.)
  */

  buffer = r_getBuffer(component, &canvas, &x, &y, &w, &h);

  woempa(5, "fixed damaged area: %d, %d, %d, %d, %d, %d of %j\n", x, y, w, h, canvas->buffer->fw, canvas->buffer->fh, component->instance);

  if(!buffer || !buffer->data) {
    woempa(9, "Component %j has no buffer.. Out of memory ??\n", component->instance);

    return;
  }

  /*
  ** Resolve background color:
  */

  Component_getBackground(component, &col, Component_getColor(F_SystemColor_window));

  /*
  ** Redraw background:
  */

  pixel = color2pixel(col);

  if (x < 0) {
    w += x;
    x = 0;
  }

  if (x + w > buffer->fw) {
    w = buffer->fw - x;
  }
  
  if (y < 0) {
    h += y;
    y = 0;
  }

  if (y + h > buffer->fh) {
    h = buffer->fh - y;
  }

  for (i = y; i < y + h; i++) {
    drawHLine(buffer, x, i, w, pixel);
  }
}

void canvas_canvas2screen(r_canvas canvas) {

  r_component component = canvas->component;
  w_ubyte *src;
  w_ubyte *dst;

  int i;
  int x;
  int y;
  int w;
  int h;
  int dx;
  int dy;

  rudolph_assert_monitor();

  if(canvas != rootCanvas) {

    if (canvas_showing(component) && Component_verifyBounds(component)) {

      if(!canvas->buffer || !canvas->buffer->data) {

        /*
        ** No buffer.. Out of memory ??
        */
        
        woempa(9, "Oops!  This buffer %p of canvas %p of component %k == NULL... Out of memory ??\n", canvas->buffer, canvas, (instance2clazz(component->instance)));
        return;
      }
        
      #ifdef DEBUG
        if (canvas->buffer->data == NULL) {
          wabort(ABORT_WONKA, "Oops!  This buffer %p of canvas %p of component %k has been released!\n", canvas->buffer, canvas, (instance2clazz(component->instance)));
        }
      #endif

      /*
      ** Resolve canvas coordinates:
      */

      getAbsoluteCoordinates(component, &x, &y, &w, &h, &dx, &dy, 0);
    
      woempa(5, "called canvas_canvas2screen(%k = %p): x = %i, y = %i, w = %i, h = %i, dx = %i, dy = %i, refresh = %d\n", instance2clazz(component->instance), canvas, x, y, w, h, dx, dy, component->refresh);

      /*
      ** Copy canvas to root canvas:
      */

      if (component->parent != rootCanvas->component) {

        /*
        ** Don't perform this operation when we are a toplevel canvas
        ** (a direct child of the rootCanvas) because the Paint thread
        ** will take care of those. This way we are sure that all the
        ** windows are painted before we update the screen. 
        ** (Avoiding of flickering windows).
        */
        
        src = canvas->buffer->data + pixels2bytes(canvas->buffer->fw * dy);
        dst = rootCanvas->buffer->data + pixels2bytes(y * rootCanvas->buffer->fw);

        for (i = 0; i < h; i++) {
          pixelcopyline(dst, src, x, dx, w);
          dst += pixels2bytes(rootCanvas->buffer->fw);
          src += pixels2bytes(canvas->buffer->fw);
        }
      }
    }
  }
}

void canvas_paint(r_component component) {

  w_thread    thread = currentWonkaThread;
  JNIEnv      *env = w_thread2JNIEnv(thread);
  jobject     instance;
  w_clazz     clazz;
  jclass      componentpeer_class;
  w_instance  graphics = NULL;
  jthrowable  exception;
  r_canvas    canvas;

  /*
  ** Check whether the component is visible:
  */

  if (!(component->instance && getBooleanField(component->instance, F_Component_visible))) {
    removeLocalReference(thread, instance);
    return;
  }

  instance = (jobject)getReferenceField(component->instance, F_Component_peer);
  clazz = instance2clazz(instance);
  componentpeer_class = clazz2Class(clazz);
//  if (!component->paint_method) {
    component->paint_method = (*env)->GetMethodID(env, componentpeer_class, "paint", "(Ljava/awt/Graphics;)V");
//  }

  /*
  ** Check whether the method is non-empty:
  */
  
  if (component->paint_method == NULL || component->paint_method->exec.code_length <= 1) {
    removeLocalReference(thread, instance);
    return;
  }

  /*
  ** If the buffer has no memory, skip painting.
  */

  canvas = (r_canvas)component->object;
  if (component->instance && component->tag == Z_CONTAINER && (!canvas->buffer || !canvas->buffer->data)) {
    woempa(9, "Canvas has no buffer... No more memory ??\n");
    removeLocalReference(thread, instance);
  
    return;
  }

  graphics = Component_getGraphics(env, instance);

  if (graphics) {           
    native_paint = 1;
    (*env)->CallVoidMethod(env, instance, component->paint_method, graphics);
    exception = (jthrowable)((*env)->ExceptionOccurred(env));
    if(exception) {
      (*env)->ExceptionClear(env);
      {
        jclass     exception_class = (*env)->GetObjectClass(env, exception);
        jmethodID  method = (*env)->GetMethodID(env, exception_class, "printStackTrace", "()V");
        if(method) {
          (*env)->CallVoidMethod(env, exception, method);
        }
        else {
          wabort(ABORT_WONKA, "An exception (%K) without a printStackTrace()V method ???", Class2clazz(exception_class));
        }
      }
    }
    native_paint = 0;
    (*env)->DeleteLocalRef(env, graphics);
  }
  
  removeLocalReference(thread, instance);
  
}

r_buffer r_getBuffer(r_component component, r_canvas *canvas, w_int *x, w_int *y, w_int *w, w_int *h) {
  r_buffer buffer = NULL;
 
  rudolph_assert_monitor();

  if (component->tag == Z_COMPONENT) {
    if(!component->parent) {
    //if(!canvas_showing(component)) {
      
      /*
      ** This component is not a part of the component tree.
      */
      
      *x = 0;
      *y = 0;
      if(w) *w = 0;
      if(h) *h = 0;
      return NULL;
    }
    
    *canvas = (r_canvas)component->parent->object;
    if(!(*canvas)->buffer || !(*canvas)->buffer->data) {
      buffer_realloc((r_canvas)(*canvas));
    }
    buffer = ((r_canvas)*canvas)->buffer;
    *x += getIntegerField(component->instance, F_Component_x);
    *y += getIntegerField(component->instance, F_Component_y);
    if(w != NULL) *w = getIntegerField(component->instance, F_Component_width);
    if(h != NULL) *h = getIntegerField(component->instance, F_Component_height);
  }
  else if (component->tag == Z_IMAGE) {
    *canvas = NULL;
    buffer = ((r_image)(component->object))->buffer;
    if(w != NULL) *w = buffer->fw;
    if(h != NULL) *h = buffer->fh;
  }
  else if (component->tag == Z_CONTAINER) {

    *canvas = (r_canvas)component->object;
    
    if(canvas_showing(component)) {
      if(!(*canvas)->buffer || !(*canvas)->buffer->data) {
        buffer_realloc((r_canvas)(*canvas));
      }
      buffer = ((r_canvas)*canvas)->buffer;
      if(w != NULL) *w = buffer->fw;
      if(h != NULL) *h = buffer->fh;
    }
    else {
      buffer = ((r_canvas)*canvas)->buffer;
      if(w != NULL) *w = 0;
      if(h != NULL) *h = 0;
    }
  }
    
  return buffer;
}

/*
** Description:
**   A line algorithm using shift operators instead of
**   multiplications and divisions!
** Algorithm:
**   Given two line endpoints, we are trying to find the
**   'in-between' points on a pixel grid.  Briefly, our
**   algorithm determines subsequent points from the start
**   point by making a decision between the two next
**   available points by determining which is closer to the
**   ideal point.
** Remark:
**   Once we are in the for-loop the calculation of a pixel
**   requires 2.5 additions (average)! :)
*/

enum {
  r_top = 0x1, 
  r_bottom = 0x2, 
  r_right = 0x4, 
  r_left = 0x8
};

int cohen_sutherland_code(int x, int y, r_buffer buffer) {
  int c = 0;
  
  if (y > buffer->fh - 1) {
    c |= r_top;
  }
  else if (y < 0) {
    c |= r_bottom;
  }
  if (x > buffer->fw - 1) {
    c |= r_right;
  }
  else if (x < 0) {
    c |= r_left;
  }
  
  return c;
}

void drawLine(r_buffer buffer, int x1, int y1, int x2, int y2, r_pixel color) {
 
  int dx;
  int dy;
  int bx1;
  int by1;
  int bx2;
  int by2;
  int x;
  int y;
  int stop;
  int code;
  int code0;
  int code1; 
  int xIncr;
  int yIncr;
  
  /*
  ** Take a handy short-cut when we have to draw a horizontal 
  ** line:
  */
    
  if (y1 == y2) {
    drawHLineClipped(buffer, (x1 < x2 ? x1 : x2), y1, abs(x2 - x1), color);
    return;
  }
  
  /*
  ** Initialize the temporaries:
  */
  
  bx1 = 0;
  by1 = 0;
  bx2 = buffer->fw - 1;
  by2 = buffer->fh - 1;

  x = 0;
  y = 0;
  
  stop = 0;
  
  /*
  ** Clip the line:
  */
  
  code0 = cohen_sutherland_code(x1, y1, buffer);
  code1 = cohen_sutherland_code(x2, y2, buffer);

  while (!stop) {
    
    /*
    ** The line is completly inside our buffer.
    */
    
    if (!(code0 | code1)) {
      stop = 1;
      continue;
    }


    /*
    ** The line is completly outside our buffer.
    */
    
    if (code0 & code1) {
      stop = 2;
      continue;
    }

    /*
    ** We have to clip the line:
    */
   
    code = code0 ? code0 : code1;
    
    if (code & r_top) {
      x = x1 + (x2 - x1) * (by2 - y1) / (y2 - y1);
      y = by2;
    } 
    else if (code & r_bottom) {
      x = x1 + (x2 - x1) * (by1 - y1) / (y2 - y1);
      y = by1;
    } 
    else if (code & r_right) {
      x = bx2;
      y = y1 + (y2 - y1) * (bx2 - x1) / (x2 - x1);
    }
    else {
      x = bx1;
      y = y1 + (y2 - y1) * (bx1 - x1) / (x2 - x1);
    }

    /*
    ** Update our coordinates and iterate:
    */

    if (code == code0) {
      x1 = x;
      y1 = y;
      code0 = cohen_sutherland_code(x1, y1, buffer);
    } 
    else {
      x2 = x;
      y2 = y;
      code1 = cohen_sutherland_code(x2, y2, buffer);
    }
  }

  /*
  ** Render the line:
  */
 
  if (stop == 1) {
 
    /*
    ** Initialize the components of the algorithm that are not
    ** affected by the slope or direction of the line.
    */
    
    dx = abs(x2 - x1);
    dy = abs(y2 - y1);

    woempa(5, "called r_drawLine(%p, %i, %i, %i, %i, %d)\n", buffer, x1, y1, x2, y2, color);

    /*
    ** Determine 'directions' to increment x and y (regardless of
    ** decision):
    */
    
    if (x1 > x2) { 
      xIncr = -1;
    } 
    else { 
      xIncr = 1; 
    }

    if (y1 > y2) { 
      yIncr = -1; 
    }
    else {
      yIncr = 1;
    }

    /* 
    ** Determine variable and initiate appropriate line drawing
    ** routine:
    */
    
    if (dx >= dy) {
      int dPr = dy << 1;
      int dPru = dPr - (dx << 1);
      int p = dPr - dx;
  
      for (; dx >= 0; dx--) {
        drawPixel(buffer, x1, y1, color);
        if (p > 0) {
          x1 += xIncr;
          y1 += yIncr;
          p += dPru;
        }
        else {
          x1 += xIncr;
          p += dPr;
        }
      }
    }
    else {
      int dPr = dx << 1;
      int dPru = dPr - (dy << 1);
      int p = dPr - dy;
  
      for (; dy >= 0; dy--) {
        drawPixel(buffer, x1, y1, color);
        if (p > 0) {
          x1 += xIncr;
          y1 += yIncr;
          p += dPru;
        }
        else {
          y1 += yIncr;
          p += dPr;
        }
      }
    }
  }
}

