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

/* $Id: pcfread.c,v 1.2 2005/06/14 09:46:04 cvs Exp $ */

#include "threads.h"      // currentWonkaThread
#include "ts-mem.h"       // allocMem(), releaseMem()

#include "fontmisc.h"
#include "fontstruct.h"
#include "bufio.h"
#include "fntfilio.h"
#include "bitmap.h"
#include "pcf.h"
#include "utilbitmap.h"
#include "defaults.h"
#include "atom.h"
#include "pcfread.h"

#include "pcfprint.h"

#include <string.h>       // strlen

#ifndef MAX
#define   MAX(a,b)    (((a)>(b)) ? a : b)
#endif


/* Read PCF font files */

static w_word  position;

static w_int
pcfGetLSB32(
    FontFilePtr file
)
{
    w_int         c;

    c = FontFileGetc(file);
    c |= FontFileGetc(file) << 8;
    c |= FontFileGetc(file) << 16;
    c |= FontFileGetc(file) << 24;
    position += 4;
    return c;
}

static w_int
pcfGetINT32(
    FontFilePtr file,
    w_word      format
)
{
    w_int         c;

    if (PCF_BYTE_ORDER(format) == MSBFirst) {
	c = FontFileGetc(file) << 24;
	c |= FontFileGetc(file) << 16;
	c |= FontFileGetc(file) << 8;
	c |= FontFileGetc(file);
    } else {
	c = FontFileGetc(file);
	c |= FontFileGetc(file) << 8;
	c |= FontFileGetc(file) << 16;
	c |= FontFileGetc(file) << 24;
    }
    position += 4;
    return c;
}

static w_int
pcfGetINT16(
    FontFilePtr file,
    w_word      format
)
{
    w_int         c;

    if (PCF_BYTE_ORDER(format) == MSBFirst) {
	c = FontFileGetc(file) << 8;
	c |= FontFileGetc(file);
    } else {
	c = FontFileGetc(file);
	c |= FontFileGetc(file) << 8;
    }
    position += 2;
    return c;
}

#define pcfGetINT8(file, format) (position++, FontFileGetc(file))

static  PCFTablePtr
pcfReadTOC(
    FontFilePtr file,
    w_int*      countp
)
{
    w_word      version;
    PCFTablePtr tables;
    w_int       count;
    w_int       i;

    position = 0;
    version = pcfGetLSB32(file);
    if (version != PCF_FILE_VERSION)
	return (PCFTablePtr) NULL;
    count = pcfGetLSB32(file);
    tables = (PCFTablePtr) allocClearedMem(count * sizeof(PCFTableRec));
    if (!tables)
	return (PCFTablePtr) NULL;
    for (i = 0; i < count; i++) {
	tables[i].type = pcfGetLSB32(file);
	tables[i].format = pcfGetLSB32(file);
	tables[i].size = pcfGetLSB32(file);
	tables[i].offset = pcfGetLSB32(file);
    }
    *countp = count;
    return tables;
}

/*
 * PCF supports two formats for metrics, both the regular
 * jumbo size, and 'lite' metrics, which are useful
 * for most fonts which have even vaguely reasonable
 * metrics
 */

static w_void
pcfGetMetric(
    FontFilePtr file,
    w_word      format,
    xCharInfo*  metric
)
{
    metric->leftSideBearing = pcfGetINT16(file, format);
    metric->rightSideBearing = pcfGetINT16(file, format);
    metric->characterWidth = pcfGetINT16(file, format);
    metric->ascent = pcfGetINT16(file, format);
    metric->descent = pcfGetINT16(file, format);
    metric->attributes = pcfGetINT16(file, format);
}

static w_void
pcfGetCompressedMetric(
    FontFilePtr file,
    w_word      format,
    xCharInfo*  metric
)
{
    metric->leftSideBearing = pcfGetINT8(file, format) - 0x80;
    metric->rightSideBearing = pcfGetINT8(file, format) - 0x80;
    metric->characterWidth = pcfGetINT8(file, format) - 0x80;
    metric->ascent = pcfGetINT8(file, format) - 0x80;
    metric->descent = pcfGetINT8(file, format) - 0x80;
    metric->attributes = 0;
}

/*
 * Position the file to the begining of the specified table
 * in the font file
 */
static w_boolean
pcfSeekToType(
    FontFilePtr file,
    PCFTablePtr tables,
    w_int       ntables,
    w_word      type,
    w_word*     formatp,
    w_word*     sizep
)
{
    w_int       i;

    for (i = 0; i < ntables; i++)
	if (tables[i].type == type) {
	    if (position > tables[i].offset)
		return WONKA_FALSE;
	    if (!FontFileSkip(file, tables[i].offset - position))
		return WONKA_FALSE;
	    position = tables[i].offset;
	    *sizep = tables[i].size;
	    *formatp = tables[i].format;
	    return WONKA_TRUE;
	}
    return WONKA_FALSE;
}

static w_boolean
pcfHasType (
    PCFTablePtr tables,
    w_int       ntables,
    w_word      type
)
{
    w_int       i;

    for (i = 0; i < ntables; i++)
	if (tables[i].type == type)
	    return WONKA_TRUE;
    return WONKA_FALSE;
}

/*
 * pcfGetProperties
 *
 * Reads the font properties from the font file, filling in the FontProps rec
 * supplied.  Used to initilialize a table of fonts for selection.
 */

w_boolean
pcfGetInitialProperties(
    FontPropsRec* pFontProps,
    FontFilePtr   file
)
{
    PCFTablePtr pcfTables;
    w_int       ntables;

    FontPropPtr props = 0;
    w_int       nprops;
    char*       isStringProp = 0;
    w_word      format;
    w_int       i;
    w_word      size;
    w_int       string_size;
    char*       strings;


    /* font tables */

    if (!(pcfTables = pcfReadTOC(file, &ntables)))
	goto Bail;

    /* font properties */

    if (!pcfSeekToType(file, pcfTables, ntables, PCF_PROPERTIES, &format, &size))
	goto Bail;
    format = pcfGetLSB32(file);
    if (!PCF_FORMAT_MATCH(format, PCF_DEFAULT_FORMAT))
	goto Bail;
    nprops = pcfGetINT32(file, format);
    props = (FontPropPtr) allocClearedMem(nprops * sizeof(FontPropRec));
    if (!props)
	goto Bail;
    isStringProp = (char *) allocMem(nprops * sizeof(char));
    if (!isStringProp)
	goto Bail;
    for (i = 0; i < nprops; i++) {
	props[i].name = pcfGetINT32(file, format);
	isStringProp[i] = pcfGetINT8(file, format);
	props[i].value = pcfGetINT32(file, format);
    }
    /* pad the property array */
    /*
     * clever here - nprops is the same as the number of odd-units read, as
     * only isStringProp are odd length
     */
    if (nprops & 3)
    {
	i = 4 - (nprops & 3);
	if (!FontFileSkip(file, i))   // jvde  23092002: added test and goto Bail
	  goto Bail;
	position += i;
    }
    string_size = pcfGetINT32(file, format);
    strings = (char *) allocMem(string_size);
    if (!strings) {
	goto Bail;
    }
    FontFileRead(file, strings, string_size);
    position += string_size;
    for (i = 0; i < nprops; i++) {
	props[i].name = MakeAtom(strings + props[i].name,
				 strlen(strings + props[i].name), WONKA_TRUE);
	if (isStringProp[i]) {
	    props[i].value = MakeAtom(strings + props[i].value,
				      strlen(strings + props[i].value), WONKA_TRUE);
	}
    }
    pFontProps->isStringProp = isStringProp;
    pFontProps->props = props;
    pFontProps->nprops = nprops;
    if (strings)
      releaseMem(strings);
    if (pcfTables)
      releaseMem(pcfTables);
    return WONKA_TRUE;
Bail:
    if (pcfTables)
      releaseMem(pcfTables);
    if (isStringProp)
      releaseMem(isStringProp);
    if (props)
      releaseMem(props);
    return WONKA_FALSE;
}

/*
 * pcfGetProperties 
 *
 * Reads the font properties from the font file, filling in the FontInfo rec
 * supplied.  Used by both ReadFont and ReadFontInfo routines.
 */

static w_boolean
pcfGetProperties(
    FontInfoPtr pFontInfo,
    FontFilePtr file,
    PCFTablePtr tables,
    w_int       ntables
)
{
    FontPropPtr props = 0;
    w_int       nprops;
    char*       isStringProp = 0;
    w_word      format;
    w_int       i;
    w_word      size;
    w_int       string_size;
    char*       strings;

    /* font properties */

    if (!pcfSeekToType(file, tables, ntables, PCF_PROPERTIES, &format, &size))
	goto Bail;
    format = pcfGetLSB32(file);
    if (!PCF_FORMAT_MATCH(format, PCF_DEFAULT_FORMAT))
	goto Bail;
    nprops = pcfGetINT32(file, format);
    props = (FontPropPtr) allocClearedMem(nprops * sizeof(FontPropRec));
    if (!props)
	goto Bail;
    isStringProp = (char *) allocMem(nprops * sizeof(char));
    if (!isStringProp)
	goto Bail;
    for (i = 0; i < nprops; i++) {
	props[i].name = pcfGetINT32(file, format);
	isStringProp[i] = pcfGetINT8(file, format);
	props[i].value = pcfGetINT32(file, format);
    }
    /* pad the property array */
    /*
     * clever here - nprops is the same as the number of odd-units read, as
     * only isStringProp are odd length
     */
    if (nprops & 3)
    {
	i = 4 - (nprops & 3);
	if (!FontFileSkip(file, i))   // jvde  07082002: added test and goto Bail
	  goto Bail;
	position += i;
    }
    string_size = pcfGetINT32(file, format);
    strings = (char *) allocMem(string_size);
    if (!strings) {
	goto Bail;
    }
    FontFileRead(file, strings, string_size);
    position += string_size;
    for (i = 0; i < nprops; i++) {
	props[i].name = MakeAtom(strings + props[i].name,
				 strlen(strings + props[i].name), WONKA_TRUE);
	if (isStringProp[i]) {
	    props[i].value = MakeAtom(strings + props[i].value,
				      strlen(strings + props[i].value), WONKA_TRUE);
	}
    }
    if (strings)
      releaseMem(strings);
    pFontInfo->isStringProp = isStringProp;
    pFontInfo->props = props;
    pFontInfo->nprops = nprops;
    return WONKA_TRUE;
Bail:
    if (isStringProp)
      releaseMem(isStringProp);
    if (props)
      releaseMem(props);
    return WONKA_FALSE;
}


/*
 * pcfReadAccel
 *
 * Fill in the accelerator information from the font file; used
 * to read both BDF_ACCELERATORS and old style ACCELERATORS
 */

static w_boolean
pcfGetAccel(
    FontInfoPtr pFontInfo,
    FontFilePtr file,
    PCFTablePtr	tables,
    w_int	ntables,
    w_word	type
    )
{
    w_word      format;
    w_word      size;

    if (!pcfSeekToType(file, tables, ntables, type, &format, &size)) {
      return WONKA_FALSE;
    }
    format = pcfGetLSB32(file);
    if (!PCF_FORMAT_MATCH(format, PCF_DEFAULT_FORMAT) &&
	!PCF_FORMAT_MATCH(format, PCF_ACCEL_W_INKBOUNDS)) {
      return WONKA_FALSE;
    }
    pFontInfo->noOverlap = pcfGetINT8(file, format);
    pFontInfo->constantMetrics = pcfGetINT8(file, format);
    pFontInfo->terminalFont = pcfGetINT8(file, format);
    pFontInfo->constantWidth = pcfGetINT8(file, format);
    pFontInfo->inkInside = pcfGetINT8(file, format);
    pFontInfo->inkMetrics = pcfGetINT8(file, format);
    pFontInfo->drawDirection = pcfGetINT8(file, format);
    pFontInfo->anamorphic = WONKA_FALSE;
    pFontInfo->cachable = WONKA_TRUE;
     /* natural alignment */ pcfGetINT8(file, format);
    pFontInfo->fontAscent = pcfGetINT32(file, format);
    pFontInfo->fontDescent = pcfGetINT32(file, format);
    pFontInfo->maxOverlap = pcfGetINT32(file, format);
    pcfGetMetric(file, format, &pFontInfo->minbounds);
    pcfGetMetric(file, format, &pFontInfo->maxbounds);
    if (PCF_FORMAT_MATCH(format, PCF_ACCEL_W_INKBOUNDS)) {
	pcfGetMetric(file, format, &pFontInfo->ink_minbounds);
	pcfGetMetric(file, format, &pFontInfo->ink_maxbounds);
    } else {
	pFontInfo->ink_minbounds = pFontInfo->minbounds;
	pFontInfo->ink_maxbounds = pFontInfo->maxbounds;
    }
    return WONKA_TRUE;
}

w_int
pcfReadFont(
    FontPtr     pFont,
    FontFilePtr file,
    w_int       bit,
    w_int       byte,
    w_int       glyph,
    w_int       scan
)
{
    w_word      format;
    w_word      size;
    BitmapFontPtr  bitmapFont = 0;
    w_int       i;
    PCFTablePtr tables = 0;
    w_int       ntables;
    w_int       nmetrics;
    w_int       nbitmaps;
    w_int       sizebitmaps;
    w_int       nink_metrics;
    CharInfoPtr metrics = 0;
    xCharInfo*  ink_metrics = 0;
    char*       bitmaps = 0;
    CharInfoPtr* encoding = 0;
    w_int       nencoding;
    w_int       encodingOffset;
    w_word      bitmapSizes[GLYPHPADOPTIONS];
    w_word*     offsets = 0;
    w_boolean	hasBDFAccelerators;

    pFont->info.props = 0;
    if (!(tables = pcfReadTOC(file, &ntables)))
	goto Bail;

//pcfPrintTOC(tables, ntables);	
	
    /* properties */

    if (!pcfGetProperties(&pFont->info, file, tables, ntables))
	goto Bail;

//printf("\n");	
//pcfPrintProperties(pFont->info.props, pFont->info.isStringProp, pFont->info.nprops);

    /* Use the old accelerators if no BDF accelerators are in the file */
    /* if BDF accelerators are present, they are dealt with later cause */
    /* they are in the last table in the file */
    hasBDFAccelerators = pcfHasType (tables, ntables, PCF_BDF_ACCELERATORS);
    if (!hasBDFAccelerators) {
	if (!pcfGetAccel (&pFont->info, file, tables, ntables, PCF_ACCELERATORS))
	    goto Bail;
	else {
//printf("\n");	
//pcfPrintAccel(&(pFont->info));
}
    }

    /* metrics */

    if (!pcfSeekToType(file, tables, ntables, PCF_METRICS, &format, &size)) {
	goto Bail;
    }
    format = pcfGetLSB32(file);
    if (!PCF_FORMAT_MATCH(format, PCF_DEFAULT_FORMAT) &&
	    !PCF_FORMAT_MATCH(format, PCF_COMPRESSED_METRICS)) {
	goto Bail;
    }
    if (PCF_FORMAT_MATCH(format, PCF_DEFAULT_FORMAT))
	nmetrics = pcfGetINT32(file, format);
    else
	nmetrics = pcfGetINT16(file, format);
    metrics = (CharInfoPtr) allocClearedMem(nmetrics * sizeof(CharInfoRec));
    if (!metrics) {
	goto Bail;
    }
    for (i = 0; i < nmetrics; i++)
	if (PCF_FORMAT_MATCH(format, PCF_DEFAULT_FORMAT))
	    pcfGetMetric(file, format, &(metrics + i)->metrics);
	else
	    pcfGetCompressedMetric(file, format, &(metrics + i)->metrics);

//printf("\n");	
//pcfPrintMetrics(metrics, nmetrics, 32, 49);

    /* bitmaps */

    if (!pcfSeekToType(file, tables, ntables, PCF_BITMAPS, &format, &size))
	goto Bail;
    format = pcfGetLSB32(file);
    if (!PCF_FORMAT_MATCH(format, PCF_DEFAULT_FORMAT))
	goto Bail;

    nbitmaps = pcfGetINT32(file, format);
    if (nbitmaps != nmetrics)           /* nbr of bitmaps must be equal to nbr of metrics */
	goto Bail;

    offsets = (w_word *) allocMem(nbitmaps * sizeof(w_word));
    if (!offsets)
	goto Bail;

    for (i = 0; i < nbitmaps; i++)
	offsets[i] = pcfGetINT32(file, format);
	
//printf("\n");	
//pcfPrintBitmapOffsets(offsets, nmetrics, 32, 49);


    /* get the 4 (=GLYPHPADOPTIONS) possible sizes the bitmap data will take up depending on various padding options */
    for (i = 0; i < GLYPHPADOPTIONS; i++)
	bitmapSizes[i] = pcfGetINT32(file, format);
    /* select the bitmap size that is actually used in the file (deduced from 'format')*/
    /* since 'format' must be PCF_DEFAULT_FORMAT, bitmapSizes[0] will be selected */
    sizebitmaps = bitmapSizes[PCF_GLYPH_PAD_INDEX(format)];

//printf("\nbitmapSizes: \n");	
//for (i = 0; i < GLYPHPADOPTIONS; i++){
//  printf("  %d : %d\n", i, bitmapSizes[i]);
//}
//printf("\nSize selected for character bitmaps is %d : %d\n", PCF_GLYPH_PAD_INDEX(format), sizebitmaps);	

    /* guard against completely empty font */
    bitmaps = (char *) allocMem(sizebitmaps ? sizebitmaps : 1);
    if (!bitmaps)
	goto Bail;
	
    /* read 'sizebitmaps' chars from the font file  */
    FontFileRead(file, bitmaps, sizebitmaps);
    position += sizebitmaps;


    /* if bitorder and/or byteorder and/or glyphpadding used in the file is not */
    /* as expected by the current system, adjust the bitmaps to reflect the expected */
    /* characteristics */

    if (PCF_BIT_ORDER(format) != bit)
	BitOrderInvert(bitmaps, sizebitmaps);
    if ((PCF_BYTE_ORDER(format) == PCF_BIT_ORDER(format)) != (bit == byte)) {
	switch (bit == byte ? PCF_SCAN_UNIT(format) : scan) {
	case 1:
	    break;
	case 2:
	    TwoByteSwap(bitmaps, sizebitmaps);
	    break;
	case 4:
	    FourByteSwap(bitmaps, sizebitmaps);
	    break;
	}
    }
    /* glyph padding: how each row in each glyph's bitmap is padded (format&3) */
    /*  0=>bytes, 1=>shorts, 2=>ints */
    /* modify padding of the bitmap rows if it is not padded in the file as expected */
    if (PCF_GLYPH_PAD(format) != glyph) {
	char*       padbitmaps;
	w_int       sizepadbitmaps;
	w_int       old;
	w_int       new;
	xCharInfo*  metric;

	sizepadbitmaps = bitmapSizes[PCF_SIZE_TO_INDEX(glyph)];
//printf("\nSize selected for repadded character bitmaps is %d : %d\n", PCF_SIZE_TO_INDEX(glyph), sizepadbitmaps);	
	padbitmaps = (char *) allocMem(sizepadbitmaps);
	if (!padbitmaps) {
	    goto Bail;
	}
	new = 0;
	for (i = 0; i < nbitmaps; i++) {
	    old = offsets[i];
	    metric = &metrics[i].metrics;
	    offsets[i] = new;
	    new += RepadBitmap(bitmaps + old, padbitmaps + new,
			       PCF_GLYPH_PAD(format), glyph,
			  metric->rightSideBearing - metric->leftSideBearing,
			       metric->ascent + metric->descent);
	}
	if (bitmaps)
	  releaseMem(bitmaps);
	bitmaps = padbitmaps;
    }

    /* copy offsets to metrics[i].bits and then release them */
    for (i = 0; i < nbitmaps; i++)
	metrics[i].bits = bitmaps + offsets[i];
    pFont->info.pad = glyph;
	
    if (offsets)
      releaseMem(offsets);
    offsets = NULL;

//printf("\n");	
//pcfPrintBitmaps(bitmaps, metrics, nbitmaps, glyph, 32, 49);


    /* ink metrics ? */

    ink_metrics = NULL;
    if (pcfSeekToType(file, tables, ntables, PCF_INK_METRICS, &format, &size)) {
	format = pcfGetLSB32(file);
	if (!PCF_FORMAT_MATCH(format, PCF_DEFAULT_FORMAT) &&
		!PCF_FORMAT_MATCH(format, PCF_COMPRESSED_METRICS)) {
	    goto Bail;
	}
	if (PCF_FORMAT_MATCH(format, PCF_DEFAULT_FORMAT))
	    nink_metrics = pcfGetINT32(file, format);
	else
	    nink_metrics = pcfGetINT16(file, format);
	if (nink_metrics != nmetrics)
	    goto Bail;
	ink_metrics = (xCharInfo *) allocClearedMem(nink_metrics * sizeof(xCharInfo));
	if (!ink_metrics)
	    goto Bail;
	for (i = 0; i < nink_metrics; i++)
	    if (PCF_FORMAT_MATCH(format, PCF_DEFAULT_FORMAT))
		pcfGetMetric(file, format, ink_metrics + i);
	    else
		pcfGetCompressedMetric(file, format, ink_metrics + i);
//printf("\n");
//pcfPrintInkMetrics(ink_metrics, nink_metrics, 32, 49);

    }

    /* encoding */
    /* the encodings table is an array of pointers to elements of the bitmaps table */

    if (!pcfSeekToType(file, tables, ntables, PCF_BDF_ENCODINGS, &format, &size))
	goto Bail;
    format = pcfGetLSB32(file);
    if (!PCF_FORMAT_MATCH(format, PCF_DEFAULT_FORMAT))
	goto Bail;

    pFont->info.firstCol = pcfGetINT16(file, format);
    pFont->info.lastCol = pcfGetINT16(file, format);
    pFont->info.firstRow = pcfGetINT16(file, format);
    pFont->info.lastRow = pcfGetINT16(file, format);
    pFont->info.defaultCh = pcfGetINT16(file, format);

    nencoding = (pFont->info.lastCol - pFont->info.firstCol + 1) *
	(pFont->info.lastRow - pFont->info.firstRow + 1);

    encoding = (CharInfoPtr *) allocClearedMem(nencoding * sizeof(CharInfoPtr));
    if (!encoding)
	goto Bail;

    pFont->info.allExist = WONKA_TRUE;
    for (i = 0; i < nencoding; i++) {
	encodingOffset = pcfGetINT16(file, format);
	if (encodingOffset == 0xFFFF) {
	    pFont->info.allExist = WONKA_FALSE;
	    encoding[i] = 0;
	} else
	    encoding[i] = metrics + encodingOffset;
    }

//printf("\n");
//pcfPrintEncodings(&(pFont->info), encoding, 0, 0, 32, 49);


    /* BDF style accelerators (i.e. bounds based on encoded glyphs) */

    if (hasBDFAccelerators) {
	if (!pcfGetAccel (&pFont->info, file, tables, ntables, PCF_BDF_ACCELERATORS))
	    goto Bail;
	else {
//printf("\n");	
//pcfPrintAccel(&(pFont->info));
}

    }

    bitmapFont = (BitmapFontPtr) allocMem(sizeof *bitmapFont);
    if (!bitmapFont)
	goto Bail;

    bitmapFont->version_num = PCF_FILE_VERSION;
    bitmapFont->num_chars = nmetrics;
    bitmapFont->num_tables = ntables;
    bitmapFont->metrics = metrics;
    bitmapFont->ink_metrics = ink_metrics;
    bitmapFont->bitmaps = bitmaps;
    bitmapFont->encoding = encoding;
    bitmapFont->pDefault = (CharInfoPtr) 0;
    if (pFont->info.defaultCh != (w_ushort) NO_SUCH_CHAR) {
	w_word r,
	       c,
	       cols;

	r = pFont->info.defaultCh >> 8;
	c = pFont->info.defaultCh & 0xFF;
	if (pFont->info.firstRow <= r && r <= pFont->info.lastRow &&
		pFont->info.firstCol <= c && c <= pFont->info.lastCol) {
	    cols = pFont->info.lastCol - pFont->info.firstCol + 1;
	    r = r - pFont->info.firstRow;
	    c = c - pFont->info.firstCol;
	    bitmapFont->pDefault = encoding[r * cols + c];
	}
    }

    bitmapFont->bitmapExtra = (BitmapExtraPtr) 0;
    pFont->fontPrivate = (pointer) bitmapFont;
    pFont->get_glyphs = bitmapGetGlyphs;    /* see bitmap.c */
    pFont->get_metrics = bitmapGetMetrics;  /* see bitmap.c */
    pFont->unload_font = pcfUnloadFont;     /* defined below */
    pFont->unload_glyphs = NULL;
    pFont->bit = bit;
    pFont->byte = byte;
    pFont->glyph = glyph;
    pFont->scan = scan;
    if (tables)
      releaseMem(tables);
    return Successful;
Bail:
    if (ink_metrics)
      releaseMem(ink_metrics);
    if (encoding)
      releaseMem(encoding);
    if (bitmaps)
      releaseMem(bitmaps);
    if (offsets)
      releaseMem(offsets);
    if (metrics)
      releaseMem(metrics);
    if (pFont->info.props)
      releaseMem(pFont->info.props);
    if (pFont->info.isStringProp) {
      releaseMem(pFont->info.isStringProp);
      pFont->info.isStringProp = NULL;
    }
    if (bitmapFont)
      releaseMem(bitmapFont);
    if (tables)
      releaseMem(tables);
    return AllocError;
}

w_int
pcfReadFontInfo(
    FontInfoPtr pFontInfo,
    FontFilePtr file
    )
{
    PCFTablePtr tables;
    w_int       ntables;
    w_word      format;
    w_word      size;
    w_int       nencoding;
    w_boolean	hasBDFAccelerators;

    pFontInfo->isStringProp = NULL;
    pFontInfo->props = NULL;

    if (!(tables = pcfReadTOC(file, &ntables)))
	goto Bail;

    /* properties */

    if (!pcfGetProperties(pFontInfo, file, tables, ntables))
	goto Bail;

    /* Use the old accelerators if no BDF accelerators are in the file */

    hasBDFAccelerators = pcfHasType (tables, ntables, PCF_BDF_ACCELERATORS);
    if (!hasBDFAccelerators)
	if (!pcfGetAccel (pFontInfo, file, tables, ntables, PCF_ACCELERATORS))
	    goto Bail;

    /* encoding */

    if (!pcfSeekToType(file, tables, ntables, PCF_BDF_ENCODINGS, &format, &size))
	goto Bail;
    format = pcfGetLSB32(file);
    if (!PCF_FORMAT_MATCH(format, PCF_DEFAULT_FORMAT))
	goto Bail;

    pFontInfo->firstCol = pcfGetINT16(file, format);
    pFontInfo->lastCol = pcfGetINT16(file, format);
    pFontInfo->firstRow = pcfGetINT16(file, format);
    pFontInfo->lastRow = pcfGetINT16(file, format);
    pFontInfo->defaultCh = pcfGetINT16(file, format);

    nencoding = (pFontInfo->lastCol - pFontInfo->firstCol + 1) *
	(pFontInfo->lastRow - pFontInfo->firstRow + 1);

    pFontInfo->allExist = WONKA_TRUE;
    while (nencoding--) {
	if (pcfGetINT16(file, format) == 0xFFFF)
	    pFontInfo->allExist = WONKA_FALSE;
    }

    /* BDF style accelerators (i.e. bounds based on encoded glyphs) */

    if (hasBDFAccelerators)
	if (!pcfGetAccel (pFontInfo, file, tables, ntables, PCF_BDF_ACCELERATORS))
	    goto Bail;
    if (tables)
      releaseMem(tables);
    return Successful;
Bail:
    if (pFontInfo->props)
      releaseMem (pFontInfo->props);
    if (pFontInfo->isStringProp);
      releaseMem (pFontInfo->isStringProp);
    if (tables)
      releaseMem(tables);
    return AllocError;
}

w_void
pcfUnloadFont(FontPtr     pFont)
{
    BitmapFontPtr  bitmapFont;

    if (pFont) {
      bitmapFont = (BitmapFontPtr) pFont->fontPrivate;
      if (bitmapFont) {
        if (bitmapFont->ink_metrics)
          releaseMem(bitmapFont->ink_metrics);
        if (bitmapFont->encoding)
          releaseMem(bitmapFont->encoding);
        if (bitmapFont->bitmaps)
          releaseMem(bitmapFont->bitmaps);
        if (bitmapFont->metrics)
          releaseMem(bitmapFont->metrics);
        releaseMem(bitmapFont);
      }
      if (pFont->info.isStringProp) {
        releaseMem(pFont->info.isStringProp);
        pFont->info.isStringProp = NULL;
      }
      /*
      if (pFont->info.props)
        releaseMem(pFont->info.props);
      */
      if (pFont->devPrivates)
        releaseMem(pFont->devPrivates);
      releaseMem(pFont);
    }
}

