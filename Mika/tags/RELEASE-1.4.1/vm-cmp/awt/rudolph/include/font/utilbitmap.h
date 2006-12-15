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

/*
 * Author:  Johan Vandeneede, ACUNIA N.V.
 */

/* $Id: utilbitmap.h,v 1.1 2005/06/15 09:11:09 cvs Exp $ */

#ifndef UTILBITMAP_H
#define UTILBITMAP_H

/* Utility functions for reformating font bitmaps */


/*
 *	Invert bit order within each BYTE of an array.
 */
void
BitOrderInvert(
    register w_ubyte* buf,
    register w_int    nbytes
);
/*
 *	Invert byte order within each 16-bits of an array.
 */
void
TwoByteSwap(
    register w_ubyte* buf,
    register w_int nbytes
);
/*
 *	Invert byte order within each 32-bits of an array.
 */
void
FourByteSwap(
    register w_ubyte* buf,
    register w_int    nbytes
);
/*
 *	Repad a bitmap
 */
int
RepadBitmap (
    char*	pSrc,
    char*       pDst,
    w_word	srcPad,
    w_word      dstPad,
    w_int	width,
    w_int       height
);

#endif				/* UTILBITMAP_H */
