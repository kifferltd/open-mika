#ifndef _CHECKS_H
#define _CHECKS_H

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
**************************************************************************/

/*
** $Id: checks.h,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
*/

#include "wonka.h"

/*
** isAssignmentCompatible(S_clazz, T_clazz) checks that an instance of
** S_Clazz can be assigned to a variable of type T_clazz. Both S_clazz 
** and T_class must already be loaded. Can result in S_clazz becoming
** supersLoaded as a side-effect.
*/
w_boolean isAssignmentCompatible(w_clazz S_clazz, w_clazz T_clazz);

w_boolean implementsInterface (w_clazz thisclass, w_clazz interfaze);

w_boolean sameRuntimePackage(w_clazz clazz1, w_clazz clazz2);

w_boolean isAllowedToCall(w_clazz caller, w_method method, w_boolean is_this);
w_boolean isAllowedToAccess(w_clazz caller, w_field field, w_boolean is_this);

#endif /* _CHECKS_H */
