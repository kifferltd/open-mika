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
* Modifications copyright (C) 2004, 2005 by Chris Gray, /k/ Embedded Java *
* Solutions. Permission is hereby granted to distribute these             *
* modifications under the terms of the Wonka Public Licence.              *
*                                                                         *
**************************************************************************/

/*
** $Id: Double.c,v 1.2 2005/04/18 19:08:22 cvs Exp $
*/

#ifdef NATIVE_FP
#include <math.h>
#endif
#include "clazz.h"
#include "descriptor.h"
#include "wmath.h"
#include "Math.h"

w_long Double_static_doubleToRawLongBits(JNIEnv *env, w_instance class, w_double d) {
  union {w_double d; w_long l;} foo;

  foo.d = d;

  return foo.l;
}

w_double Double_static_longToDoubleBits(JNIEnv *env, w_instance class, w_long j) {
  union {w_double d; w_long l;} foo;

  foo.l = j;

  return foo.d;
}

w_boolean Double_static_isNaN(JNIEnv *env, w_instance class, w_double j) {

  return wfp_float64_is_NaN(j);

}

w_boolean Double_static_isInfinite(JNIEnv *env, w_instance class, w_double j) {

  return wfp_float64_is_Infinite(j);

}


w_instance
Double_getWrappedClass(JNIEnv *env, w_instance thisClass) {

  return clazz2Class(clazz_double);
  
}
