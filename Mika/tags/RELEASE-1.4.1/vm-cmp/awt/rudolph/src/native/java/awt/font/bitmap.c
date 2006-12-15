/*

Copyright (c) 1991  X Consortium

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL THE
X CONSORTIUM BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

Except as contained in this notice, the name of the X Consortium shall not be
used in advertising or otherwise to promote the sale, use or other dealings
in this Software without prior written authorization from the X Consortium.

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

/* $Id: bitmap.c,v 1.1 2005/06/14 08:48:25 cvs Exp $ */

#include "wonka.h"
#include "fontmisc.h"
#include "fontstruct.h"
#include "bitmap.h"

w_int
bitmapGetGlyphs(
    FontPtr      pFont,
    w_word       count,
    w_ubyte*     chars,
    FontEncoding charEncoding,
    w_word*      glyphCount,	/* RETURN */
    CharInfoPtr* glyphs		/* RETURN */
    )
{
    BitmapFontPtr  bitmapFont;
    w_word firstCol;
    register w_word numCols;
    w_word firstRow;
    w_word numRows;
    CharInfoPtr *glyphsBase;
    register w_word c;
    register CharInfoPtr pci;
    w_word r;
    CharInfoPtr *encoding;
    CharInfoPtr pDefault;

    bitmapFont = (BitmapFontPtr) pFont->fontPrivate;
    encoding = bitmapFont->encoding;
    pDefault = bitmapFont->pDefault;
    firstCol = pFont->info.firstCol;
    numCols = pFont->info.lastCol - firstCol + 1;
    glyphsBase = glyphs;
    switch (charEncoding) {

    case Linear8Bit:
    case TwoD8Bit:
	if (pFont->info.firstRow > 0)
	    break;
	if (pFont->info.allExist && pDefault) {
	    while (count--) {
		c = (*chars++) - firstCol;
		if (c < numCols)
		    *glyphs++ = encoding[c];
		else
		    *glyphs++ = pDefault;
	    }
	} else {
	    while (count--) {
		c = (*chars++) - firstCol;
		if (c < numCols && (pci = encoding[c]))
		    *glyphs++ = pci;
		else if (pDefault)
		    *glyphs++ = pDefault;
	    }
	}
	break;
    case Linear16Bit:
	if (pFont->info.allExist && pDefault) {
	    while (count--) {
		c = *chars++ << 8;
		c = (c | *chars++) - firstCol;
		if (c < numCols)
		    *glyphs++ = encoding[c];
		else
		    *glyphs++ = pDefault;
	    }
	} else {
	    while (count--) {
		c = *chars++ << 8;
		c = (c | *chars++) - firstCol;
		if (c < numCols && (pci = encoding[c]))
		    *glyphs++ = pci;
		else if (pDefault)
		    *glyphs++ = pDefault;
	    }
	}
	break;

    case TwoD16Bit:
	firstRow = pFont->info.firstRow;
	numRows = pFont->info.lastRow - firstRow + 1;
	while (count--) {
	    r = (*chars++) - firstRow;
	    c = (*chars++) - firstCol;
	    if (r < numRows && c < numCols &&
		    (pci = encoding[r * numCols + c]))
		*glyphs++ = pci;
	    else if (pDefault)
		*glyphs++ = pDefault;
	}
	break;
    }
    *glyphCount = glyphs - glyphsBase;
    return Successful;
}

static CharInfoRec nonExistantChar;

w_int
bitmapGetMetrics(
    FontPtr      pFont,
    w_word       count,
    register w_ubyte* chars,
    FontEncoding charEncoding,
    w_word*      glyphCount,	/* RETURN */
    xCharInfo**  glyphs		/* RETURN */
    )
{
    w_int         ret;
    xCharInfo*    ink_metrics;
    CharInfoPtr   metrics;
    BitmapFontPtr bitmapFont;
    CharInfoPtr	  oldDefault;
    w_word        i;

    bitmapFont = (BitmapFontPtr) pFont->fontPrivate;
    oldDefault = bitmapFont->pDefault;
    bitmapFont->pDefault = &nonExistantChar;
    ret = bitmapGetGlyphs(pFont, count, chars, charEncoding, glyphCount, (CharInfoPtr *) glyphs);
    if (ret == Successful) {
	if (bitmapFont->ink_metrics) {
	    metrics = bitmapFont->metrics;
	    ink_metrics = bitmapFont->ink_metrics;
	    for (i = 0; i < *glyphCount; i++) {
		if (glyphs[i] != (xCharInfo *) & nonExistantChar)
		    glyphs[i] = ink_metrics + (((CharInfoPtr) glyphs[i]) - metrics);
	    }
	}
    }
    bitmapFont->pDefault = oldDefault;
    return ret;
}
