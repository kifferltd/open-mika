#ifndef _CALLS_H
#define _CALLS_H

/**************************************************************************
* Copyright  (c) 2001, 2002 by Acunia N.V. All rights reserved.           *
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
* Modifications copyright (c) 2006 by Chris Gray, /k/ Embedded Java       *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: calls.h,v 1.2 2006/10/04 14:24:14 cvsroot Exp $
**
** X86 Architecture
**
** This file can contains the code that will setup a stack for a function call
** with an arbitrary number of arguments.
*/

#include "wonka.h"

w_long _call_static(JNIEnv * env, w_instance clazz, w_slot top, w_methodExec exec);
w_long _call_instance(JNIEnv * env, w_slot top, w_methodExec exec);

#endif /* _CALLS_H */

