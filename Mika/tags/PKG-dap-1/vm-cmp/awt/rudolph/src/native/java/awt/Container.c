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
** $Id: Container.c,v 1.1 2005/06/14 16:00:18 cvs Exp $
*/

#include "wstrings.h"     // String2string()
#include "loading.h"      // instance2clazz()

#include "awt-classes.h"
#include "rudolph.h"
#include "canvas.h"
#include "registry.h"
#include "Component.h"
#include "Event.h"

int Container_verifyBounds(r_component dst, r_component src) {
    
  r_canvas canvas = (r_canvas)dst->object;
  w_instance i = (w_instance)src->instance;

  woempa(5, "VERIFY: %d, %d %dx%d -> %dx%d\n", getIntegerField(i, F_Component_x), getIntegerField(i, F_Component_y), getIntegerField(i, F_Component_width), getIntegerField(i, F_Component_height), canvas->buffer->fw, canvas->buffer->fh);

  #ifdef RUNTIME_CHECKS
    if (canvas->buffer->ox + canvas->buffer->vw > canvas->buffer->fw || canvas->buffer->oy + canvas->buffer->vh > canvas->buffer->fh) {
      wabort(ABORT_WONKA, "invalid viewport size:  %d + %d -> %d,  %d + %d -> %d\n", canvas->buffer->ox, canvas->buffer->vw, canvas->buffer->fw, canvas->buffer->oy, canvas->buffer->vh, canvas->buffer->fh);
    }
    
    if ((getIntegerField(dst->instance, F_Component_width) != canvas->buffer->fw) || (getIntegerField(dst->instance, F_Component_height) != canvas->buffer->fh)) {
//      wabort(ABORT_WONKA, "canvas buffer width/height mismatch: %d == %d, %d == %d\n", getIntegerField(dst->instance, F_Component_width), canvas->buffer->fw, getIntegerField(dst->instance, F_Component_height), canvas->buffer->fh);
    } 
  #endif

  if(src->tag == Z_CONTAINER) {
    return WONKA_TRUE;
  } 
  else {
    return ((getIntegerField(i, F_Component_x) <= canvas->buffer->fw) && (getIntegerField(i, F_Component_y) <= canvas->buffer->fh));
  }
}
