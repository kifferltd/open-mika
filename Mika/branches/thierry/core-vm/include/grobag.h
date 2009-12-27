#ifndef _GROBAG_H
#define _GROBAG_H

/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

/*
** $Id: grobag.h,v 1.2 2005/05/24 10:06:30 cvs Exp $
*/

#include "ts-mem.h"
#include "wonka.h"

#define GROBAG_DUMMY_SIZE 8

/** A Grobag is an expandable array of bytes.
** @field capacity w_size  The current size of the \texttt{contents} array,
**                          in bytes.
** @field occupancy w_size The number of bytes currently in use.
** @field contents char[]  The array where the things are stored.  Its size
**                         below is purely notional: space is reserved using 
**                         ensureGrobagCapacity.
**
** @doc
** Note that all operations on a grobag take a *w_grobag (i.e. a **w_Grobag);
** this is because the w_grobag pointer will be updated when new space is
** allocated.
*/
typedef struct w_Grobag {
  w_size   capacity;
  w_size   occupancy;
  char     contents[GROBAG_DUMMY_SIZE];
} w_Grobag;

/** Reserve space for at least 'newsize' bytes of contents.
**
** If \texttt{*grobag} is currently \texttt{NULL}, allocates a new Grobag.
** Returns WONKA_TRUE if successful, WONKA_FALSE otherwise.
*/
w_boolean ensureGrobagCapacity(w_grobag*, w_size newsize);

/** Append bytes to the end of a grobag, after expanding it
** if necessary. Returns WONKA_TRUE if successful, else WONKA_FALSE.
*/
w_boolean appendToGrobag(w_grobag*, void* what, w_size nbytes);

/** Release the memory used by a Grobag
** (Sets \texttt{*grobag} to \texttt{NULL}.
*/
inline static void releaseGrobag(w_grobag* grobag) {
  releaseMem(*grobag);
  *grobag = NULL;
}

#endif /* _GROBAG_H */
