
/*

Copyright (c) 1991  X Consortium

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

/* $Id: bufio.c,v 1.2 2005/06/14 09:46:04 cvs Exp $ */

/*
 * changelog
 *	07082002 Johan Vandeneede ACUNIA N.V.
 *	- added declarations for all funtions to existing 'bufio.h'
 */


#include <unistd.h>
#include <errno.h>
#include "mika_threads.h"      // currentWonkaThread
#include "ts-mem.h"       // allocMem(), releaseMem()
#include "fontmisc.h"
#include "bufio.h"

#ifdef X_NOT_STDC_ENV
extern w_int errno;
#endif

BufFilePtr
BufFileCreate (
    char*    private,
    w_int    (*io)( BufFilePtr f, w_int c ),
    w_int    (*skip)(BufFilePtr f, w_int count),
    w_int    (*bfclose)( BufFilePtr f, w_int doClose )
)
{
    BufFilePtr	f;

    f = (BufFilePtr) allocClearedMem (sizeof *f);
    if (!f)
	return 0;
    f->private = private;
    f->bufp = f->buffer;
    f->left = 0;
    f->io = io;
    f->skip = skip;
    f->bfclose = bfclose;
    return f;
}

#define FileDes(f)  ((w_int)(long) (f)->private)

static w_int
BufFileRawFill (
    BufFilePtr	f,
    w_int	c
)
{
    w_int	left;

    left = read (FileDes(f), (char *)f->buffer, BUFFILESIZE);
    if (left <= 0) {
	f->left = 0;
	return BUFFILEEOF;
    }
    f->left = left - 1;
    f->bufp = f->buffer + 1;
    return f->buffer[0];
}

static w_int
BufFileRawSkip (
    BufFilePtr	f,
    w_int	count
)
{
    w_int	    curoff;
    w_int	    fileoff;
    w_int	    todo;

    curoff = f->bufp - f->buffer;
    fileoff = curoff + f->left;
    if (curoff + count <= fileoff) {
	f->bufp += count;
	f->left -= count;
    } else {
	todo = count - (fileoff - curoff);
	if (lseek (FileDes(f), todo, 1) == -1) {
	    if (errno != ESPIPE)
		return BUFFILEEOF;
	    while (todo) {
		curoff = BUFFILESIZE;
		if (curoff > todo)
		    curoff = todo;
		fileoff = read (FileDes(f), (char *)f->buffer, curoff);
		if (fileoff <= 0)
		    return BUFFILEEOF;
		todo -= fileoff;
	    }
	}
	f->left = 0;
    }
    return count;
}

static w_int
BufFileRawClose (
    BufFilePtr	f,
    w_int       doClose
)
{
    if (doClose)
	close (FileDes (f));
    return 1;
}

BufFilePtr
BufFileOpenRead (
    w_int	fd
)
{
#ifdef __EMX__
    /* hv: I'd bet WIN32 has the same effect here */
    setmode(fd,O_BINARY);
#endif
    return BufFileCreate ((char *)(long) fd, BufFileRawFill, BufFileRawSkip, BufFileRawClose);
}

static w_int
BufFileRawFlush (
//    w_int	c,
    BufFilePtr	f,
    w_int	c
)
{
    w_int	cnt;

    if (c != BUFFILEEOF)
	*f->bufp++ = c;
    cnt = f->bufp - f->buffer;
    f->bufp = f->buffer;
    f->left = BUFFILESIZE;
    if (write (FileDes(f), (char *)f->buffer, cnt) != cnt)
	return BUFFILEEOF;
    return c;
}

BufFilePtr
BufFileOpenWrite (
    w_int	fd
)
{
    BufFilePtr	f;

#ifdef __EMX__
    /* hv: I'd bet WIN32 has the same effect here */
    setmode(fd,O_BINARY);
#endif
    f = BufFileCreate ((char *)(long) fd, BufFileRawFlush, 0, BufFileFlush);
    f->bufp = f->buffer;
    f->left = BUFFILESIZE;
    return f;
}

w_int
BufFileRead (
    BufFilePtr  f,
    char*       b,
    w_int       n
)
{
    w_int	    c, cnt;
    cnt = n;
    while (cnt--) {
	c = BufFileGet (f);
	if (c == BUFFILEEOF)
	    break;
	*b++ = c;
    }
    return n - cnt - 1;
}

w_int
BufFileWrite (
    BufFilePtr	f,
    char*       b,
    w_int       n
)
{
    w_int	    cnt;
    cnt = n;
    while (cnt--) {
	if (BufFilePut (f, *b++) == BUFFILEEOF)
	    return BUFFILEEOF;
    }
    return n;
}

w_int
BufFileFlush (
    BufFilePtr	f,
    w_int       doClose
)
{
    if (f->bufp != f->buffer)
	(*f->io) (f, BUFFILEEOF);
    return 0;
}

w_int
BufFileClose (
    BufFilePtr	f,
    w_int       doClose
)
{
    (void) (*f->bfclose) (f, doClose);
    releaseMem (f);
    return 0;
}

w_int
BufFileFree (
    BufFilePtr	f
)
{
    releaseMem (f);
    return 0;
}
