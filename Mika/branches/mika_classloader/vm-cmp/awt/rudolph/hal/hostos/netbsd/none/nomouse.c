/**************************************************************************
* Copyright  (c) 2001, 2002 by Acunia N.V. All rights reserved.           *
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


/* $Id: nomouse.c,v 1.1 2005/06/14 08:48:24 cvs Exp $

/* Split mouse routines from none.c into nomouse.c so we can reuse them from
 * a framebuffer driver with no pointing device.   - msmith@cbnco.com
 */

void mouse_set_path(char *s) {
}

void mouse_init(void) {
}

int mouse_poll(int *state, int *x, int *y) {
  return 0;
}

void mouse_flush(void) {
}

void mouse_close(void) {
}

