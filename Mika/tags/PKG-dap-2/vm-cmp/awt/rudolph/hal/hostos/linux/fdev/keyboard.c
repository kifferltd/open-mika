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

/* $Id: keyboard.c,v 1.2 2005/06/14 09:46:03 cvs Exp $ */

#include "ts-mem.h"

#include "rudolph.h"
#include "Event.h"

w_int keyboard_poll(w_int *VK, w_char *keychar, w_int *mod, w_int *pressed) {
  
  *VK = 0;
  *keychar = 0;
  *mod = 0;
  *pressed = 0;

  return 0;
}

w_int keyboard_isMod(w_int VK) {
  return 0;
}
