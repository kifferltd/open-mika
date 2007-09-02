#ifndef _MATH_H
#define _MATH_H

/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
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
