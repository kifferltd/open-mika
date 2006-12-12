/**************************************************************************
* Copyright  (c) 2002 by Acunia N.V. All rights reserved.                 *
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


/* $Id: */

#ifndef _PALETTE_H
#define _PALETTE_H

/*
** Crude but it works. We should probably generate this from an X11-style
** colour database or something: for now, feel free to add your own fave
** colours by hand.
*/

static char _white[] = {255, 255, 255};
static char _black[] = {0, 0, 0};
static char _silver[] = {192,192,192};
static char _gray[] = {128,128,128};
static char _maroon[] = {128,0,0};
static char _red[] = {255,0,0};
static char _purple[] = {128,0,128};
static char _fuchsia[] = {255,0,255};
static char _green[] = {0,128,0};
static char _lime[] = {0,255,0};
static char _olive[] = {128,128,0};
static char _yellow[] = {255,255,0} ;
static char _navy[] = {0,0,128};
static char _blue[] = {0,0,255};
static char _teal[] = {0,128,128};
static char _aqua[] = {0,255,255};
static char _acunia_red[] = {204, 0, 51};

static char *palette[] = {
  "white", _white,
  "black", _black,
  "silver", _silver,
  "gray", _gray,
  "maroon", _maroon,
  "red", _red,
  "purple", _purple,
  "fuchsia", _fuchsia,
  "green", _green,
  "lime", _lime,
  "olive", _olive,
  "yellow", _yellow,
  "navy", _navy,
  "blue", _blue,
  "teal", _teal,
  "aqua", _aqua,
  "acunia red", _acunia_red,
  NULL
};


#endif /* _PALETTE_H */

