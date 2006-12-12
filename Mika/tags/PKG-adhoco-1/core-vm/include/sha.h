#ifndef _SHA_H
#define _SHA_H

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
** $Id: sha.h,v 1.2 2004/11/18 22:56:54 cvs Exp $
*/

#include "wonka.h"

typedef struct w_SHA {
  union {
    w_word W[80];
    w_ubyte B[320];
  } buffer;
  w_int buffered;
  void *spare;
  union {
    w_word W[5];
    w_ubyte B[20];
  } signature;
  w_size lo_length;
  w_size hi_length;
} w_SHA;

w_sha allocSha(void);
void processSha(w_sha sha, w_ubyte *data, w_size length);
void finishSha(w_sha sha);
void releaseSha(w_sha sha);

#endif /* _SHA_H */
