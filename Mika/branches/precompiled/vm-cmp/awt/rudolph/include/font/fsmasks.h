/* $XConsortium: fsmasks.h,v 1.5 94/04/17 20:11:08 dpw Exp $ */
/*
 * Copyright 1990, 1991 Network Computing Devices;
 * Portions Copyright 1987 by Digital Equipment Corporation 
 *
 * Permission to use, copy, modify, distribute, and sell this software and
 * its documentation for any purpose is hereby granted without fee, provided
 * that the above copyright notice appear in all copies and that both that
 * copyright notice and this permission notice appear in supporting
 * documentation, and that the names of Network Computing Devices or Digital
 * not be used in advertising or publicity pertaining to distribution
 * of the software without specific, written prior permission.
 * Network Computing Devices and Digital make no representations 
 * about the suitability of this software for any purpose.  It is provided 
 * "as is" without express or implied warranty.
 *
 * NETWORK COMPUTING DEVICES AND DIGITAL DISCLAIM ALL WARRANTIES WITH
 * REGARD TO THIS SOFTWARE, INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS, IN NO EVENT SHALL NETWORK COMPUTING DEVICES
 * OR DIGITAL BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL
 * DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
 * PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS
 * ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE OF
 * THIS SOFTWARE.
 */

/*

Portions Copyright (c) 1987, 1994  X Consortium

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

/* $Id: fsmasks.h,v 1.1 2005/06/15 09:11:09 cvs Exp $ */

/*
 * masks & values used by the font lib and the font server
 */

#ifndef _FSMASKS_H_
#define _FSMASKS_H_

//#include <X11/Xmd.h>

/* font format macros */
#define BitmapFormatByteOrderMask       (1L << 0)
#define BitmapFormatBitOrderMask        (1L << 1)
#define BitmapFormatImageRectMask       (3L << 2)
#define BitmapFormatScanlinePadMask     (3L << 8)
#define BitmapFormatScanlineUnitMask    (3L << 12)

#define BitmapFormatByteOrderLSB        (0)
#define BitmapFormatByteOrderMSB        (1L << 0)
#define BitmapFormatBitOrderLSB         (0)
#define BitmapFormatBitOrderMSB         (1L << 1)

#define BitmapFormatImageRectMin        (0L << 2)
#define BitmapFormatImageRectMaxWidth   (1L << 2)
#define BitmapFormatImageRectMax        (2L << 2)

#define BitmapFormatScanlinePad8        (0L << 8)
#define BitmapFormatScanlinePad16       (1L << 8)
#define BitmapFormatScanlinePad32       (2L << 8)
#define BitmapFormatScanlinePad64       (3L << 8)

#define BitmapFormatScanlineUnit8       (0L << 12)
#define BitmapFormatScanlineUnit16      (1L << 12)
#define BitmapFormatScanlineUnit32      (2L << 12)
#define BitmapFormatScanlineUnit64      (3L << 12)

#define BitmapFormatMaskByte            (1L << 0)
#define BitmapFormatMaskBit             (1L << 1)
#define BitmapFormatMaskImageRectangle  (1L << 2)
#define BitmapFormatMaskScanLinePad     (1L << 3)
#define BitmapFormatMaskScanLineUnit    (1L << 4)

typedef w_word fsBitmapFormat;
typedef w_word fsBitmapFormatMask;

#endif	/* _FSMASKS_H_ */
