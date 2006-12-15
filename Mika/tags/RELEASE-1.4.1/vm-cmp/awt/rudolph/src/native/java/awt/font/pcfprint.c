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

/* $Id: pcfprint.c,v 1.1 2005/06/14 08:48:25 cvs Exp $ */


/*
 * Author:  Johan Vandeneede, ACUNIA N.V.
 */


/*
 *  Debug functions for printing out pcf datastructures after reading
 *  pcf tables from file
 */

#include <stdio.h>

#include "wonka.h"
#include "fontmisc.h"
#include "pcf.h"
#include "fontstruct.h"
#include "pcfprint.h"
#include "atom.h"

void  pcfPrintTOC(PCFTablePtr tables, w_int countp) {

  w_int         i;

  printf("table               type  format        size  fileoffset\n");
  printf("-----  -----------------  ------        ----  ----------\n");
  for (i = 0; i < countp; i++) {
    printf("%5d  ", i);
    switch(tables[i].type) {
      case 1:
	printf("%17s  ","PROPERTIES");
        break;
      case 2:
	printf("%17s  ","ACCELERATORS");
	break;
      case 4:
	printf("%17s  ","METRICS");
	break;
      case 8:
	printf("%17s  ","BITMAPS");
	break;
      case 16:
	printf("%17s  ","INK_METRICS");
	break;
      case 32:
	printf("%17s ","BDF_ENCODINGS");
	break;
      case 64:
	printf("%17s  ","SWIDTHS");
	break;
      case 128:
	printf("%17s  ","GLYPH_NAMES");
	break;
      case 256:
	printf("%17s  ","BDF_ACCELERATORS");
	break;
      default:
	printf("%17s  ","UNKNOWN TABLE");
	break;
    }
    printf("%6x  %10d  %10d\n", tables[i].format, tables[i].size, tables[i].offset );
  }
}

void  pcfPrintProperties(
    FontPropPtr props,
    char*       isStringProp,
    w_int       nprops
    )

{
    w_int i;
    char* atomName;
	
    printf("properties table \n");
    printf("property              name numeric value\n");
    printf("-------- ----------------- ------- --------------------\n");

    for (i = 0; i < nprops; i++) {
        atomName=NameForAtom(props[i].name);
        printf("%4d %21s", i, atomName ? atomName : "NULL" );
        if (isStringProp[i]) {
          atomName=NameForAtom(props[i].value);
          printf("     n   %s", atomName ? atomName : "NULL" );
        }
        else
    	  printf("     y   %d", props[i].value );
    	printf("\n");
    }
}


void  pcfPrintMetric (
    xCharInfo*  metric
)
{
    printf("  leftSideBearing   %d\n", metric->leftSideBearing);
    printf("  rightSideBearing  %d\n", metric->rightSideBearing);
    printf("  characterWidth    %d\n", metric->characterWidth);
    printf("  ascent            %d\n", metric->ascent);
    printf("  descent           %d\n", metric->descent);
    printf("  attributes        %d\n", metric->attributes);

}

void pcfPrintMetrics(
   CharInfoPtr  metrics,
   w_int        nmetrics,
   w_int        first,
   w_int        last
   )
{
    w_int i;

    first = ( first < 0 ? 0 : first );
    first = ( first > nmetrics ? nmetrics : first );
    last = ( last < 0 ? 0 : last );
    last = ( last > nmetrics ? nmetrics : last );

    printf("Glyph metrics table (size of the stored bitmaps)  : size %d  \n", nmetrics);
    for (i = first; i < last; i++) {
      printf("Glyph nbr: %d\n", i);
      pcfPrintMetric( &(metrics + i)->metrics);
    }
}


void pcfPrintInkMetrics(
   xCharInfo*   metrics,
   w_int        nmetrics,
   w_int        first,
   w_int        last
   )
{
    w_int i;

    first = ( first < 0 ? 0 : first );
    first = ( first > nmetrics ? nmetrics : first );
    last = ( last < 0 ? 0 : last );
    last = ( last > nmetrics ? nmetrics : last );

    printf("Glyph ink metrics table (the minimum bounding boxes) : size %d  \n", nmetrics);
    for (i = first; i < last; i++) {
      printf("Glyph nbr: %d\n", i);
      pcfPrintMetric( metrics + i);
    }
}

void pcfPrintBitmapOffsets(
   w_word*      offsets,
   w_int        nmetrics,
   w_int        first,
   w_int        last
   )
{
    w_int i;

    first = ( first < 0 ? 0 : first );
    first = ( first > nmetrics ? nmetrics : first );
    last = ( last < 0 ? 0 : last );
    last = ( last > nmetrics ? nmetrics : last );

    printf("Bitmap offsets table : size %d   \n", nmetrics);
    for (i = first; i < last; i++) {
      printf("  offset[%d]=%d \n", i, offsets[i]);
    }
}

void  pcfPrintAccel(
    FontInfoPtr pFontInfo
)
{
    char* truestr="TRUE";
    char* falsestr="FALSE";
    char* ltr="LeftToRight";
    char* rtl="RightToLeft";
    char* btt="BottomToTop";
    char* ttb="TopToBottom";
    char* drawDirection;

    printf("accelerators table \n");
    printf("accelerator              value\n");
    printf("------------------------ -----------------------\n");
    printf("noOverlap                %s\n", pFontInfo->noOverlap ? truestr : falsestr);
    printf("constantMetrics          %s\n", pFontInfo->constantMetrics ? truestr : falsestr);
    printf("terminalFont             %s\n", pFontInfo->terminalFont ? truestr : falsestr);
    printf("constantWidth            %s\n", pFontInfo->constantWidth ? truestr : falsestr);
    printf("inkInside                %s\n", pFontInfo->inkInside ? truestr : falsestr);
    printf("inkMetrics               %s\n", pFontInfo->inkMetrics ? truestr : falsestr);
    switch (pFontInfo->drawDirection) {
      case 0:
    	drawDirection=ltr;
    	break;
      case 1:
    	drawDirection=rtl;
    	break;
      case 2:
    	drawDirection=btt;
    	break;
      case 3:
    	drawDirection=ttb;
    	break;
      default:
    	drawDirection=ltr;
    }
    printf("drawDirection            %s\n", drawDirection);
    printf("anamorphic (not in file) %s\n", pFontInfo->anamorphic ? truestr : falsestr);
    printf("cachable   (not in file) %s\n", pFontInfo->cachable ? truestr : falsestr);
    printf("fontAscent               %d\n", pFontInfo->fontAscent);
    printf("fontDescent              %d\n", pFontInfo->fontDescent);
    printf("padding (internal)       %d\n", pFontInfo->pad);
    printf("maxOverlap               %d\n", pFontInfo->maxOverlap);
    printf("metric maxbounds:        \n");
    pcfPrintMetric(&(pFontInfo->maxbounds));
    printf("metric minbounds:        \n");
    pcfPrintMetric(&(pFontInfo->minbounds));
    printf("metric ink_maxbounds:        \n");
    pcfPrintMetric(&(pFontInfo->ink_maxbounds));
    printf("metric ink_minbounds:        \n");
    pcfPrintMetric(&(pFontInfo->ink_minbounds));
}

void
pcfPrintGlyph (
    char*	pGlyph,
    w_int	width,
    w_int       height
)
{
    w_int   row,col;
    w_int   i;
    char*   pByte = pGlyph;
    char    c;

    for (row = 0; row < height; row++) {
      for (col = 0; col < width; col++) {
        c = *pByte;
        for (i = 7; i > -1; i--) {
          if ( ((c>>i)&1) == 1 )
            printf("OO");
          else
            printf("  ");
        }
        pByte++;
      }
      printf("\n");
    }
	
}

void
pcfPrintBitmaps(
	char*        bitmaps,
	CharInfoPtr  metrics,
	w_int        nbitmaps,
	w_word       padding,
	w_int        first,
	w_int        last
	)
{
    w_int i;
    w_int width;
    w_int widthBytes;
    xCharInfo m;

    first = ( first < 0 ? 0 : first );
    first = ( first > nbitmaps ? nbitmaps : first );
    last = ( last < 0 ? 0 : last );
    last = ( last > nbitmaps ? nbitmaps : last );

    printf("Bitmaps table : size %d   \n", nbitmaps);
    printf("--------------------------\n");
    for (i = first; i < last; i++) {
      m = metrics[i].metrics;
      /*  calculate nbr of storage bytes for each row of this character */
      width = m.rightSideBearing - m.leftSideBearing;
      switch (padding) {
        case 1:	
          widthBytes = (width+7)>>3;
          break;
        case 2:
          widthBytes = ((width+15)>>4)<<1;
          break;
        case 4:	
          widthBytes = ((width+31)>>5)<<2;
          break;
        case 8:	
          widthBytes = ((width+63)>>6)<<3;
          break;
        default:
          widthBytes = ((width+31)>>5)<<2;
          break;
      }
      pcfPrintGlyph(metrics[i].bits, widthBytes, m.ascent + m.descent);
    }
}

void  pcfPrintEncodings(
    FontInfoPtr  pFontInfo,
    CharInfoPtr* encodings,
    w_word row1,
    w_word row2,
    w_word col1,
    w_word col2
    )
{
    w_word r, c;
    w_word ncols = pFontInfo->lastCol - pFontInfo->firstCol + 1;
    CharInfoPtr pDefault = 0;

    printf("Encodings table\n");
    printf("range (%d,%d) - (%d,%d) | allExist = %s | defaultCh = %d\n",
           pFontInfo->firstRow, pFontInfo->firstCol,
           pFontInfo->lastRow, pFontInfo->lastCol,
           pFontInfo->allExist ? "TRUE" : "FALSE",
           pFontInfo->defaultCh   );
    printf("------------------------------------------------------------------------\n");


    /* (w_ushort) NO_SUCH_CHAR) is supposed to be max w_ushort   */
    if (pFontInfo->defaultCh == (w_ushort) NO_SUCH_CHAR) {
      /* let's for now automatically use the first character in the bitmaps */
      pFontInfo->defaultCh = 0;

      /* we should however create a custom CharInfoPtr with its own metrics and bitmap: */
      /* pDefault = (CharInfoPtr) allocClearedMeme (currentWonkaThread, sizeof(CharInfoRec));                         */
      /* if (!pDefault) ...                                                             */
      /* else define_custom_default_char                                                */

      /* or use the static CharInfoRec nonExistantChar that is defined in bitmap.c      */
    }

    r = pFontInfo->defaultCh >> 8;
    c = pFontInfo->defaultCh & 0xFF;
    if (pFontInfo->firstRow <= r && r <= pFontInfo->lastRow &&
    	pFontInfo->firstCol <= c && c <= pFontInfo->lastCol) {
          r = r - pFontInfo->firstRow;
	  c = c - pFontInfo->firstCol;
	  pDefault = encodings[r * ncols + c];
    }

    printf("  default glyph encoding and bitmap table address\n");
    printf("  0x%02x%02x: %x \n\n",r,c, (w_word)pDefault);


    printf("  requested glyph encodings and bitmap table addresses\n");
    for  ( r=row1; r<row2+1; r++) {
      for ( c=col1; c<col2+1; c++) {
	if (pFontInfo->firstRow <= r && r <= pFontInfo->lastRow &&
		pFontInfo->firstCol <= c && c <= pFontInfo->lastCol) {
          printf("  0x%02x%02x: %x \n",(r - pFontInfo->firstRow),(c - pFontInfo->firstCol), (w_word)(encodings[(r - pFontInfo->firstRow)*ncols + (c - pFontInfo->firstCol)]));
        }
        else {
          printf("  0x%02x%02x: character not in font \n",(r - pFontInfo->firstRow),(c - pFontInfo->firstCol));
        }
      }
    }
}

