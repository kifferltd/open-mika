#ifndef _PM_H
#define _PM_H

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
** $Id: pm.h,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
*/

/** File wonka/include/pm.h
**  This is the API for the Persistent Medium (PM).
*/

#if defined(LINUX) || defined(NETBSD)
#include <sys/stat.h>
#include <unistd.h>
#include <fcntl.h>
#endif
#include <errno.h>
#include <stdio.h>
#include "wonka.h"

/// Begin a series of accesses to the PM.
w_boolean openPM(void);

/// Read the whole PM into the Persistent Storage area.
w_boolean readPMall(void);

/// Write the whole PM from the Persistent Storage area.
w_boolean writePMall(void);

/// Write a part of PM from the Persistent Storage area.
w_boolean writePMportion(void* start, w_size length);

/// End a series of accesses to the PM.
w_boolean closePM(void);

/// Erase the current contents of PM and perform a writePMall.
w_boolean clearPM(void);

#endif /* _PM_H */
