/*

Copyright (c) 1993  X Consortium

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
** $Id: bufio.h,v 1.1 2005/06/15 09:11:09 cvs Exp $
*/

#ifndef BUFIO_H
#define BUFIO_H

#define BUFFILESIZE	8192
#define BUFFILEEOF	-1

typedef w_ubyte BufChar;

typedef struct _buffile* BufFilePtr;

typedef struct _buffile {
    BufChar* bufp;
    w_int    left;
    BufChar  buffer[BUFFILESIZE];
    w_int    (*io)(BufFilePtr f, w_int c );
    w_int    (*skip)(BufFilePtr f, w_int count);
    w_int    (*bfclose)( BufFilePtr f, w_int doClose);
    char*    private;
} BufFileRec;

BufFilePtr
BufFileCreate (
    char*   private,
    w_int   (*io)( BufFilePtr f, w_int c ),
    w_int    (*skip)( BufFilePtr f, w_int count ),
    w_int   (*bfclose)( BufFilePtr f ,w_int doClose )
);

BufFilePtr
BufFileOpenRead (
    w_int	fd
);

BufFilePtr
BufFileOpenWrite (
    w_int	fd
);

w_int	
BufFileClose (
    BufFilePtr	f,
    w_int       doClose
);
w_int
BufFileRead (
    BufFilePtr  f,
    char*       b,
    w_int	n
);

w_int
BufFileWrite (
    BufFilePtr  f,
    char*       b,
    w_int       n
);

w_int	
BufFileFlush (
    BufFilePtr	f,
    w_int       doClose
);

w_int
BufFileFree (
    BufFilePtr	f
);

#define BufFileGet(f)	((f)->left-- ? *(f)->bufp++ : (*(f)->io) (f,0))
#define BufFilePut(f,c)	(--(f)->left ? *(f)->bufp++ = (c) : (*(f)->io) (f,c))
#define BufFileSkip(f,c)    ((*(f)->skip) (f, c))


#endif            /* BUFIO_H */

