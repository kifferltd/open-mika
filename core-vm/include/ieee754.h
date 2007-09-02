#ifndef _IEEE754_H
#define _IEEE754_H

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
** $Id: ieee754.h,v 1.2 2005/05/23 17:31:50 cvs Exp $
*/

/*
** Floating single and double precision routines. Heavily hacked version from the
** implementation provided by John R. Hauser. See below for more information.
*/

/*
===============================================================================

This C header file is part of the SoftFloat IEC/IEEE Floating-point
Arithmetic Package, Release 2a.

Written by John R. Hauser.  This work was made possible in part by the
International Computer Science Institute, located at Suite 600, 1947 Center
Street, Berkeley, California 94704.  Funding was partially provided by the
National Science Foundation under grant MIP-9311980.  The original version
of this code was written as part of a project to build a fixed-point vector
processor in collaboration with the University of California at Berkeley,
overseen by Profs. Nelson Morgan and John Wawrzynek.  More information
is available through the Web page `http://HTTP.CS.Berkeley.EDU/~jhauser/
arithmetic/SoftFloat.html'.

THIS SOFTWARE IS DISTRIBUTED AS IS, FOR FREE.  Although reasonable effort
has been made to avoid it, THIS SOFTWARE MAY CONTAIN FAULTS THAT WILL AT
TIMES RESULT IN INCORRECT BEHAVIOR.  USE OF THIS SOFTWARE IS RESTRICTED TO
PERSONS AND ORGANIZATIONS WHO CAN AND WILL TAKE FULL RESPONSIBILITY FOR ANY
AND ALL LOSSES, COSTS, OR OTHER PROBLEMS ARISING FROM ITS USE.

Derivative works are acceptable, even for commercial purposes, so long as
(1) they include prominent notice that the work is derivative, and (2) they
include prominent notice akin to these four paragraphs for those parts of
this code that are retained.

===============================================================================
*/

#include "wmath.h"
#include "wonka.h"

/*
** Integer representations. The most convenient type to hold an integer
** of at least as many bits as specified. We make all these types into
** words or double words, so that whatever we do, we are word aligned.
** Any good processor/compiler combo should be faster manipulating words
** than 8 or 16 bit quantities...

typedef int                 flag;
typedef int                 int8;
typedef int                 int16;
typedef int                 int32;
typedef long long           int64;

typedef unsigned int        uint8;
typedef unsigned int        uint32;
typedef unsigned long long  uint64;
*/

/*
** Exact bit count containers for unsigned and signed
** quantities.

typedef unsigned int        bits32;
typedef unsigned long long  bits64;

typedef int                 sbits32;
typedef long long           sbits64;
*/

/*
** Some predefined float and double constants.
#define F_NAN			       (0x7fc00000)

#define F_ZERO                         (0x00000000)
#define F_MINUS_ZERO                   (0x80000000)
#define F_ONE                          (0x3f800000)
#define F_TWO                          (0x40000000)
#define F_ZERO_POINT_SEVEN_FIVE        (0x3f400000)
#define F_ZERO_POINT_FIVE              (0x3f000000)
#define F_FLOAT_MAX_VALUE              (0x7f7fffff)
#define F_FLOAT_MIN_VALUE              (0x00000001)

#define D_POSITIVE_INFINITY	       (0x7ff0000000000000LL)
#define D_NEGATIVE_INFINITY	       (0xfff0000000000000LL)
#define D_NAN			       (0x7ff8000000000000LL)
#define D_NANJIKES		       (0xfff8000000000000LL)

#define D_MINUS_ONE		       (0xbff0000000000000LL)
#define D_ZERO                         (0x0000000000000000LL)
#define D_MINUS_ZERO                   (0x8000000000000000LL)
#define D_ONE                          (0x3ff0000000000000LL)
#define D_TWO                          (0x4000000000000000LL)
#define D_ZERO_POINT_FIVE              (0x3fe0000000000000LL)
#define D_ZERO_POINT_TWO_FIVE          (0x3fd0000000000000LL)
#define D_ONE_TO_THE_POWER_EIGHT       (0x4197d78400000000LL)
#define D_ZERO_POINT_ONE               (0x000000009999999aLL)
#define D_LONG_MIN_VALUE               (0xc3e0000000000000LL)
#define D_LONG_MAX_VALUE               (0x43e0000000000000LL)
#define D_DOUBLE_MAX_VALUE             (0x7fefffffffffffffLL)
#define D_DOUBLE_MIN_VALUE             (0x0000000000000001LL)

#define D_ZERO_MSW                     (0x00000000)
#define D_ZERO_LSW                     (0x00000000)

#define D_ONE_MSW                      (0x3ff00000)
#define D_ONE_LSW                      (0x00000000)

#define D_TWO_MSW                      (0x40000000)
#define D_TWO_LSW                      (0x00000000)
*/

/*
** Some utility macros

//#define float32_negate(x)              ((x) & ~((x) & 0x80000000))
w_float float32_negate(w_float value);
#define float32_is_negative(x)         ((x) & 0x80000000)

//#define float64_negate(x)              ((x) & ~((x) & 0x8000000000000000LL))
w_double float64_negate(w_double value);
#define float64_is_negative(x)         ((x) & 0x8000000000000000LL)
*/

/*
-------------------------------------------------------------------------------
Software IEC/IEEE floating-point types.
-------------------------------------------------------------------------------

typedef bits32 float32;
typedef bits64 float64;
*/

/*
-------------------------------------------------------------------------------
Software IEC/IEEE floating-point underflow tininess-detection mode.
-------------------------------------------------------------------------------

extern wfp_int8 float_detect_tininess;

enum {
    float_tininess_after_rounding  = 0,
    float_tininess_before_rounding = 1
};
*/

/*
-------------------------------------------------------------------------------
Software IEC/IEEE floating-point rounding mode.
-------------------------------------------------------------------------------

extern wfp_int8 float_rounding_mode;

enum {
    float_round_nearest_even = 0,
    float_round_to_zero      = 1,
    float_round_down         = 2,
    float_round_up           = 3
};
*/

/*
-------------------------------------------------------------------------------
Software IEC/IEEE floating-point exception flags.
-------------------------------------------------------------------------------

extern wfp_int8 float_exception_flags;

enum {
    float_flag_inexact   =  1,
    float_flag_underflow =  2,
    float_flag_overflow  =  4,
    float_flag_divbyzero =  8,
    float_flag_invalid   = 16
};
*/

/*
-------------------------------------------------------------------------------
Routine to raise any or all of the software IEC/IEEE floating-point
exception flags.
-------------------------------------------------------------------------------

void float_raise( wfp_int8 );

*/
/*
-------------------------------------------------------------------------------
Software IEC/IEEE integer-to-floating-point conversion routines.
-------------------------------------------------------------------------------

float32 wfp_int32_to_float32( wfp_int32 );
float64 wfp_int32_to_float64( wfp_int32 );

float32 wfp_int64_to_float32( wfp_int64 );
float64 wfp_int64_to_float64( wfp_int64 );
*/

/*
-------------------------------------------------------------------------------
Software IEC/IEEE single-precision conversion routines.
-------------------------------------------------------------------------------

wfp_int32 float32_to_int32( wfp_float32 );
wfp_int32 float32_to_int32_round_to_zero( wfp_float32 );
wfp_int64 float32_to_int64( wfp_float32 );
wfp_int64 float32_to_int64_round_to_zero( wfp_float32 );
wfp_float64 float32_to_float64( wfp_float32 );
*/

/*
-------------------------------------------------------------------------------
Software IEC/IEEE single-precision operations.
-------------------------------------------------------------------------------
*/

wfp_float32 float32_round_to_int( wfp_float32 );
wfp_float32 float32_add( wfp_float32, wfp_float32 );
wfp_float32 float32_sub( wfp_float32, wfp_float32 );
wfp_float32 float32_mul( wfp_float32, wfp_float32 );
wfp_float32 float32_div( wfp_float32, wfp_float32 );
wfp_float32 float32_rem( wfp_float32, wfp_float32 );
wfp_float32 float32_sqrt( wfp_float32 );

wfp_flag float32_eq( wfp_float32, wfp_float32 );
wfp_flag float32_le( wfp_float32, wfp_float32 );
wfp_flag float32_lt( wfp_float32, wfp_float32 );
wfp_flag float32_eq_signaling( wfp_float32, wfp_float32 );
wfp_flag float32_le_quiet( wfp_float32, wfp_float32 );
wfp_flag float32_lt_quiet( wfp_float32, wfp_float32 );
wfp_flag float32_is_signaling_nan( wfp_float32 );

/*
-------------------------------------------------------------------------------
Software IEC/IEEE double-precision conversion routines.
-------------------------------------------------------------------------------
*/

wfp_int32 float64_to_int32( wfp_float64 );
wfp_int32 float64_to_int32_round_to_zero( wfp_float64 );
wfp_int64 float64_to_int64( wfp_float64 );
wfp_int64 float64_to_int64_round_to_zero( wfp_float64 );
wfp_float32 float64_to_float32( wfp_float64 );

/*
-------------------------------------------------------------------------------
Software IEC/IEEE double-precision operations.
-------------------------------------------------------------------------------
*/

wfp_float64 float64_round_to_int( wfp_float64 );
wfp_float64 float64_add( wfp_float64, wfp_float64 );
wfp_float64 float64_sub( wfp_float64, wfp_float64 );
wfp_float64 float64_mul( wfp_float64, wfp_float64 );
wfp_float64 float64_div( wfp_float64, wfp_float64 );
wfp_float64 float64_rem( wfp_float64, wfp_float64 );
wfp_float64 float64_sqrt( wfp_float64 );

wfp_flag float64_eq( wfp_float64, wfp_float64 );
wfp_flag float64_le( wfp_float64, wfp_float64 );
wfp_flag float64_lt( wfp_float64, wfp_float64 );
#define float64_gt( f1, f2 ) (!float64_le(f1, f2))

wfp_flag float64_eq_signaling( wfp_float64, wfp_float64 );
wfp_flag float64_le_quiet( wfp_float64, wfp_float64 );
wfp_flag float64_lt_quiet( wfp_float64, wfp_float64 );
wfp_flag float64_is_signaling_nan( wfp_float64 );

#define LIT64(a)     a##LL

/*
** (Added by CG 20010905)
*/
wfp_float32 float32_abs( wfp_float32 );
wfp_float64 float64_abs( wfp_float64 );

#define float64_signBit( float64_val ) ( (float64_val) & 0x8000000000000000LL )

#endif /* _IEEE754_H */
