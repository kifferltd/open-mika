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

/* $Id: fileio.c,v 1.1 2005/06/14 08:48:25 cvs Exp $ */

#include <fcntl.h>
#include <unistd.h>
#include "wonka.h"
#include "fontmisc.h"
#include "fntfilio.h"
#include "fileio.h"

#ifdef FONT_COMPRESSION
#include "decompress.h"
#ifdef X_GZIP_FONT_COMPRESSION
#include "dontknowyet.h"
#endif
#endif

#ifndef O_BINARY
#define O_BINARY O_RDONLY
#endif

FontFilePtr
FontFileOpen (char* name)
{
    w_int	fd;
    BufFilePtr	raw;
#ifdef FONT_COMPRESSION
    w_int	len;
    BufFilePtr  cooked;
#endif

    fd = open (name, O_BINARY);
    if (fd < 0)
	return 0;
    raw = BufFileOpenRead (fd);
    if (!raw)
    {
	close (fd);
	return 0;
    }
#ifdef FONT_COMPRESSION
    len = strlen (name);
#ifndef __EMX__
    if (len > 2 && !strcmp (name + len - 2, ".Z")) {
#else
    if (len > 2 && (!strcmp (name + len - 4, ".pcz") || 
		    !strcmp (name + len - 2, ".Z"))) {
#endif
	cooked = BufFilePushCompressed (raw);
	if (!cooked) {
	    BufFileClose (raw, WONKA_TRUE);
	    return 0;
	}
	raw = cooked;
#ifdef X_GZIP_FONT_COMPRESSION
    } else if (len > 3 && !strcmp (name + len - 3, ".gz")) {
	cooked = BufFilePushZIP (raw);
	if (!cooked) {
	    BufFileClose (raw, WONKA_TRUE);
	    return 0;
	}
	raw = cooked;
#endif
    }
#endif    /* FONT_COMPRESSION */
    return (FontFilePtr) raw;
}

w_int
FontFileClose (FontFilePtr	f)
{
    BufFileClose ((BufFilePtr) f, WONKA_TRUE);
    return 0;
}

