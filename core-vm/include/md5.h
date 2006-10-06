#ifndef _MD5_H
#define _MD5_H

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

#include "wonka.h"

typedef struct w_MD5Acc *     w_md5Acc;

typedef struct w_MD5Acc {
  w_word sums[4];
  w_ubyte bytes[64];
  w_size len;
  w_size total;
  w_thread thread;
} w_MD5Acc;

w_md5Acc allocMD5Acc(w_thread thread);
void releaseMD5Acc(w_md5Acc acc);
void processMD5Acc(w_md5Acc acc, w_ubyte *data, w_size size);
void finishMD5Acc(w_md5Acc acc, w_word signature[]);

#endif /* _MD5_h */
