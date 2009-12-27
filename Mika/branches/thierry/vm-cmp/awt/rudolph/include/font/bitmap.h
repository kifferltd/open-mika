/*

Copyright (c) 1990  X Consortium

Permission is hereby granted, free of charge, to any person obtaining
a copy of this software and associated documentation files (the
"Software"), to deal in the Software without restriction, including
without limitation the rights to use, copy, modify, merge, publish,
distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to
the following conditions:

The above copyright notice and this permission notice shall be included
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE X CONSORTIUM BE LIABLE FOR ANY CLAIM, DAMAGES OR
OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
OTHER DEALINGS IN THE SOFTWARE.

Except as contained in this notice, the name of the X Consortium shall
not be used in advertising or otherwise to promote the sale, use or
other dealings in this Software without prior written authorization
from the X Consortium.

*/

/*
 * Author:  Keith Packard, MIT X Consortium
 */

/**************************************************************************
* Portions Copyright  (c) 2002 by Acunia N.V. All rights reserved.        *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
* distribution or disclosure of this software is expressly forbidden.     *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Philips-site 5 box 3        info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
 * $Id: bitmap.h,v 1.1 2005/06/15 09:11:09 cvs Exp $
 */


#ifndef _BITMAP_H_
#define _BITMAP_H_

#include <stdio.h>  /* just for NULL */

/*
 * Internal format used to store bitmap fonts
 */

typedef struct _BitmapExtra {
    Atom*       glyphNames;
    w_int*      sWidths;
    w_word      bitmapsSizes[GLYPHPADOPTIONS];
    FontInfoRec info;
}           BitmapExtraRec, *BitmapExtraPtr;

typedef struct _BitmapFont {
    w_word       version_num;
    w_int        num_chars;
    w_int        num_tables;
    CharInfoPtr  metrics;	/* font metrics, including glyph pointers */
    xCharInfo*   ink_metrics;	/* ink metrics */
    char*        bitmaps;	/* base of bitmaps, useful only to free */
    CharInfoPtr* encoding;	/* array of char info pointers */
    CharInfoPtr  pDefault;	/* default character */
    BitmapExtraPtr bitmapExtra;	/* stuff not used by X server */
}           BitmapFontRec, *BitmapFontPtr;

w_int  bitmapGetGlyphs(
    FontPtr      pFont,
    w_word       count,
    register w_ubyte* chars,
    FontEncoding charEncoding,
    w_word*      glyphCount,	/* RETURN */
    CharInfoPtr* glyphs		/* RETURN */
);

w_int  bitmapGetMetrics(
    FontPtr      pFont,
    w_word       count,
    register w_ubyte* chars,
    FontEncoding charEncoding,
    w_word*      glyphCount,	/* RETURN */
    xCharInfo**  glyphs		/* RETURN */
);

#endif				/* _BITMAP_H_ */
