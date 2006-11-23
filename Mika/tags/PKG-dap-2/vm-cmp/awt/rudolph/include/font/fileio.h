/**************************************************************************
* Copyright  (c) 2002 by Acunia N.V. All rights reserved.                 *
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
 * Author:  Johan Vandeneede, ACUNIA N.V.
 */

/* $Id: fileio.h,v 1.1 2005/06/15 09:11:09 cvs Exp $ */

#ifndef _FILEIO_H
#define _FILEIO_H

FontFilePtr
FontFileOpen (char* name);

w_int
FontFileClose (FontFilePtr f);


#endif  /* _FILEIO_H */
