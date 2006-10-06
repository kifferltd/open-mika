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

/* $Id: pcfprint.h,v 1.1 2005/06/15 09:11:09 cvs Exp $ */

/*
 * Author:  Johan Vandeneede, ACUNIA N.V.
 */



#ifndef _PCFPRINT_H
#define _PCFPRINT_H

void  pcfPrintTOC(
    PCFTablePtr tables,
    w_int countp
    );

void  pcfPrintProperties(
    FontPropPtr props,
    char*       isStringProp,
    w_int       nprops
    );

void  pcfPrintMetric (
    xCharInfo*  metric
);

void pcfPrintMetrics(
   CharInfoPtr  metrics,
   w_int        nmetrics,
   w_int        first,
   w_int        last
   );

void pcfPrintInkMetrics(
   xCharInfo*   metrics,
   w_int        nmetrics,
   w_int        first,
   w_int        last
   );

void pcfPrintBitmapOffsets(
   w_word*      offsets,
   w_int        nmetrics,
   w_int        first,
   w_int        last
   );

void  pcfPrintAccel(
    FontInfoPtr pFontInfo
);

void
pcfPrintGlyph (
    char*	pGlyph,
    w_int	width,
    w_int       height
);

void
pcfPrintBitmaps(
	char*        bitmaps,
	CharInfoPtr  metrics,
	w_int        nbitmaps,
	w_word       padding,
	w_int        first,
	w_int        last
	);

void  pcfPrintEncodings(
    FontInfoPtr  pFontInfo,
    CharInfoPtr* encodings,
    w_word row1,
    w_word row2,
    w_word col1,
    w_word col2
    );


#endif  /* _PCFPRINT_H */
