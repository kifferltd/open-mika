#ifndef _MATH_H
#define _MATH_H

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
** $Id: Math.h,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
*/

#include "wstrings.h"
#include "wonka.h"

#define FINITE     -1
#define ZERO        0
#define INFINITE    1
#define NAN         2

/*
** w_int2w_string converts a Java integer into a registered Wonka string.
** u_int2w_string does the same but treats the integer as unsigned.
** w_long2w_string converts a Java long into a registered Wonka string.
** u_long2w_string does the same but treats the integer as unsigned.
** w_double2w_string converts a Java long into a registered Wonka string.
*/

w_string w_int2w_string(w_thread, int value, int radix);
w_string u_int2w_string(w_thread, int value, int radix);
w_string w_long2w_string(w_thread, w_long value, int radix);
w_string u_long2w_string(w_thread, w_long value, int radix);
w_string w_double2w_string(w_thread, w_double value, w_boolean isdouble);

w_long parseLong(w_string string, w_int radix);
w_double parseDouble(w_string string);
w_int w_double_test(w_double *px);
w_short w_double_scale(w_double *px, w_int xexp);
w_int w_double_unscale(w_int *exponent, w_double *value);
w_boolean float64_is_NaN(w_double a);
w_boolean float64_is_Infinite(w_double a);
w_boolean float32_is_Infinite(w_float a);
w_boolean float32_is_NaN(w_float a);

#endif /* _MATH_H */
