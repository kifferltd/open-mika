/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
*                                                                         *
* Modifications copyright (c) 2004 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
**************************************************************************/


/*
** $Id: ThreadGroup.c,v 1.7 2005/12/12 21:47:37 cvs Exp $
*/

#include <string.h>

#include "arrays.h"
#include "clazz.h"
#include "core-classes.h"
#include "fields.h"
#include "heap.h"
#include "loading.h"
#include "methods.h"
#include "wstrings.h"
#include "threads.h"
#include "ts-mem.h"

void
ThreadGroup_checkAccess
( JNIEnv *env, w_instance ThreadGroup
) {

  return;

}

