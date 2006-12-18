#ifndef _GROBAG_H
#define _GROBAG_H

/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
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
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
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
