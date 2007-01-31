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

/* $Id: pcfread.h,v 1.1 2005/06/15 09:11:09 cvs Exp $ */

#ifndef _PCFREAD_H
#define _PCFREAD_H


w_boolean
pcfGetInitialProperties(
    FontPropsRec* pFontProps,
    FontFilePtr   file
);

w_int
pcfReadFont(
    FontPtr     pFont,
    FontFilePtr file,
    w_int       bit,
    w_int       byte,
    w_int       glyph,
    w_int       scan
);

w_int
pcfReadFontInfo(
    FontInfoPtr pFontInfo,
    FontFilePtr file
    );

w_void
pcfUnloadFont(FontPtr     pFont);


#endif  /* _PCFREAD_H */
