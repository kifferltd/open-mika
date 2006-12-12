#ifndef _MISC_H
#define _MISC_H

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
** $Id: misc.h,v 1.3 2005/09/18 12:38:21 cvs Exp $
*/

#include <stdio.h>
#include <string.h>
#include "wonka.h"

void initWonka(void);
void *loadModule(char *, char *);
void *lookupModuleSymbol(char *);
extern char *loading_problem;

#if defined(LINUX) || defined(NETBSD)
#ifndef __uClinux__
#include <sys/time.h>

unsigned long timeDifference(struct timeval *start, struct timeval *stop);

#endif
#endif

void hexdump(w_ubyte *data, w_size count);
void dumpfile ( w_ubyte *filename, w_ubyte *data, w_size count);
//void getfile ( w_thread thread, w_ubyte *filename, w_ubyte **data, w_size *count);

#endif /* _MISC_H */
