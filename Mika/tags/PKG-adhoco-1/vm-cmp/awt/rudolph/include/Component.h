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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


/* $Id: Component.h,v 1.1 2005/06/14 08:48:24 cvs Exp $ */

#ifndef _COMPONENT
#define _COMPONENT

#include "rudolph.h"

#define RF_OVERLAP  0x0000001
#define RF_HIDDEN   0x0000002

typedef struct r_Component {
  r_Tag         tag;
  unsigned long object;
  unsigned int  refresh;
  w_flags       flags;
  w_instance    instance;
  r_component   parent;
  r_component   headChild;
  r_component   tailChild;
  r_component   next;
  r_component   prev;
  w_method      paint_method;
} r_Component;

w_void invalidateTreeUpwards(r_component component, w_int type);
w_void getRelativeCoordinates(r_component component, int *x, int *y, int *w, int *h);
w_void getAbsoluteCoordinates(r_component component, int *x, int *y, int *w, int *h, int *dx, int *dy, int depth);
w_int Component_verifyBounds(r_component component);
w_int Container_verifyBounds(r_component src, r_component dst);
w_void Component_getBackground(r_component component, w_instance *col, w_instance def);
w_void Component_getForeground(r_component component, w_instance *col, w_instance def);
w_void Component_getFontInstance(r_component component, w_instance *fontInstance);
w_instance Component_getColor(int slot);
w_void Component_getFont(r_component component, r_font *font);
w_int Component_isVisible(r_component component);
w_void Component_intersect(int x1, int y1, int w1, int h1, int x2, int y2, int w2, int h2, int *xr, int *yr, int *wr, int *hr);

#endif
