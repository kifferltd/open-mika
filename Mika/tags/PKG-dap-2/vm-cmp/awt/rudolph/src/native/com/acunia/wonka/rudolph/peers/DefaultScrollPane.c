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
** $Id: DefaultScrollPane.c,v 1.1 2005/06/14 08:48:25 cvs Exp $ 
*/

#include "fields.h"
#include "awt-classes.h"
#include "canvas.h"
#include "Component.h"

void ScrollPane_setViewport(JNIEnv * env, w_instance this, w_instance instance, int x, int y, int w, int h) {
  w_instance peerInstance = getReferenceField(instance, F_Component_peer);
  r_component component = getWotsitField(peerInstance, F_DefaultComponent_wotsit);
  r_canvas canvas = (r_canvas)component->object;
  
  woempa(9, "component '%k' %p: buffer[w = %d, h = %d], viewport[x = %d, y = %d, w = %d, h = %d], canvas %p\n", (instance2clazz(instance)), instance, canvas->buffer->fw, canvas->buffer->fh, x, y, w, h, canvas);
  
  canvas->buffer->ox = x;
  canvas->buffer->oy = y;
  canvas->buffer->vw = w;
  canvas->buffer->vh = h;
}

