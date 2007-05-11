#ifndef _WMATH_H
#define _WMATH_H

/****************************************************************************
* Copyright (c) 2005 by Chris Gray, trading as /k/ Embedded Java Solutions. *
* All rights reserved.  The contents of this file may not be copied or      *
* distributed in any form without express written consent of the author.    *
****************************************************************************/

/*
** $Id: wmath.h,v 1.1 2005/04/18 17:31:29 cvs Exp $
*/

#include <float.h>
#include <math.h>

/*
 * Integer representations.
 */
typedef int                 wfp_flag;
typedef int                 wfp_int32;
typedef long long           wfp_int64;

/*
-------------------------------------------------------------------------------
Software IEC/IEEE floating-point types.
-------------------------------------------------------------------------------
*/
typedef float  wfp_float32;
typedef double wfp_float64;

extern wfp_float32 F_NAN;
extern wfp_float64 D_NAN;

/*
** Some predefined float and double constants.
*/
#define F_ZERO             (0.0f)
#define F_ONE              (1.0f)
#define F_TWO              (2.0f)
#define F_FLOAT_MAX_VALUE  (FLT_MAX)
#define F_MINUS_ZERO       (-0.0f)

#define D_ZERO             (0.0)
#define D_ONE              (1.0)
#define D_TWO              (2.0)
#define D_DOUBLE_MAX_VALUE (DBL_MAX)
#define D_MINUS_ZERO       (-0.0)

/*

#define F_ZERO_POINT_SEVEN_FIVE        (0x3f400000)
#define F_ZERO_POINT_FIVE              (0x3f000000)
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
*/
#define w_float32_negate(x)              (-(x))
//w_float float32_negate(w_float value);
#define w_float32_is_negative(x)         ((x) < 0.0)

#define w_float64_negate(x)              (-(x))
//w_double float64_negate(w_double value);
#define w_float64_is_negative(x)         ((x) < 0.0)

/*
-------------------------------------------------------------------------------
integer-to-floating-point conversion routines.
-------------------------------------------------------------------------------
*/

static inline wfp_float32 wfp_int32_to_float32( wfp_int32 i) {
  return (float)i;
}

static inline wfp_float64 wfp_int32_to_float64( wfp_int32 i) {
  return (double)i;
}

static inline wfp_float32 wfp_int64_to_float32( wfp_int64 j) {
  return (float)j;
}

static inline wfp_float64 wfp_int64_to_float64( wfp_int64 j) {
  return (double)j;
}

/*
-------------------------------------------------------------------------------
Software IEC/IEEE single-precision conversion routines.
-------------------------------------------------------------------------------
*/

// TODO: make a distinction between round_to_zero and not?

static inline wfp_int32 wfp_float32_to_int32( wfp_float32 f) {
  return (int) f;
}

static inline wfp_int32 wfp_float32_to_int32_round_to_zero( wfp_float32 f) {
  return (int) f;
}

static inline wfp_int64 wfp_float32_to_int64( wfp_float32 f) {
  return (long long int) f;
}

static inline wfp_int64 wfp_float32_to_int64_round_to_zero( wfp_float32 f) {
  return (long long int) f;
}

static inline wfp_float64 wfp_float32_to_float64( wfp_float32 f) {
  return (double) f;
}

/*
-------------------------------------------------------------------------------
Software IEC/IEEE single-precision operations.
-------------------------------------------------------------------------------

static inline wfp_flag wfp_float32_eq( wfp_float32 f1, wfp_float32 f2) {
  return !isnanf(f1) && !isnanf(f2) && f1 == f2;
}

static inline wfp_flag wfp_float32_lt( wfp_float32 f1, wfp_float32 f2) {
  return !isnanf(f1) && !isnanf(f2) && f1 < f2;
}

static inline wfp_float32 wfp_float32_add( wfp_float32 f1, wfp_float32 f2) {
  return (isnanf(f1) || isnanf(f2)) ? nanf(NULL) : f1 + f2;
}

static inline wfp_float32 wfp_float32_sub( wfp_float32 f1, wfp_float32 f2) {
  return (isnanf(f1) || isnanf(f2)) ? nanf(NULL) : f1 - f2;
}

static inline wfp_float32 wfp_float32_mul( wfp_float32 f1, wfp_float32 f2) {
  return (isnanf(f1) || isnanf(f2)) ? nanf(NULL) : f1 * f2;
}

static inline wfp_float32 wfp_float32_div( wfp_float32 f1, wfp_float32 f2) {
  return (isnanf(f1) || isnanf(f2)) ? nanf(NULL) : f1 / f2;
}
*/

#define wfp_float32_eq(f1,f2) (!isnan(f1) && !isnan(f2) && (f1) == (f2))

#define wfp_float32_lt(f1,f2) (!isnan(f1) && !isnan(f2) && (f1) < (f2))

#define wfp_float32_add(f1,f2) ((isnan(f1) || isnan(f2)) ? F_NAN : (f1) + (f2))

#define wfp_float32_sub(f1,f2) ((isnan(f1) || isnan(f2)) ? F_NAN : (f1) - (f2))

#define wfp_float32_mul(f1,f2) ((isnan(f1) || isnan(f2)) ? F_NAN : (f1) * (f2))

#define wfp_float32_div(f1,f2) ((isnan(f1) || isnan(f2)) ? F_NAN : (f1) / (f2))

/*
-------------------------------------------------------------------------------
Software IEC/IEEE double-precision conversion routines.
-------------------------------------------------------------------------------
*/

static inline wfp_int32 wfp_float64_to_int32_round_to_zero( wfp_float64 d) {
  return (int)d;
}

static inline wfp_int64 wfp_float64_to_int64_round_to_zero( wfp_float64 d) {
  return (long long int)d;
}

static inline wfp_float32 wfp_float64_to_float32( wfp_float64 d) {
  return (float)d;
}


/*
-------------------------------------------------------------------------------
Software IEC/IEEE double-precision operations.
-------------------------------------------------------------------------------

static inline wfp_flag wfp_float64_eq( wfp_float64 d1, wfp_float64 d2) {
  return !isnan(d1) && !isnan(d2) && d1 == d2;
}

static inline wfp_flag wfp_float64_lt( wfp_float64 d1, wfp_float64 d2) {
  return !isnan(d1) && !isnan(d2) && d1 < d2;
}
*/

#define wfp_float64_eq(d1,d2) (!isnan(d1) && !isnan(d2) && d1 == d2)

#define wfp_float64_lt(d1,d2) (!isnan(d1) && !isnan(d2) && d1 < d2)

static inline wfp_float64 wfp_float64_round_to_int( wfp_float64 d) {
  wfp_int64 j = (wfp_int64)d;
  return (wfp_float64)j;
}

#define wfp_float64_add(d1,d2) ((isnan(d1) || !isnan(d2)) ? D_NAN : d1 + d2)

#define wfp_float64_sub(d1,d2) ((isnan(d1) || !isnan(d2)) ? D_NAN : d1 - d2)

#define wfp_float64_mul(d1,d2) ((isnan(d1) || !isnan(d2)) ? D_NAN : d1 * d2)

#define wfp_float64_div(d1,d2) ((isnan(d1) || !isnan(d2)) ? D_NAN : d1 / d2)

#define wfp_float32_abs(f) (isnan(f) ? F_NAN : fabsf(f))

#define wfp_float64_abs(d) (isnan(d) ? D_NAN : fabs(d))

/*
static inline wfp_float64 wfp_float64_add( wfp_float64 d1, wfp_float64 d2) {
  return (isnan(d1) || !isnan(d2)) ? nan(NULL) : d1 + d2;
}

static inline wfp_float64 wfp_float64_sub( wfp_float64 d1, wfp_float64 d2) {
  return (isnan(d1) || !isnan(d2)) ? nan(NULL) : d1 - d2;
}

static inline wfp_float64 wfp_float64_mul( wfp_float64 d1, wfp_float64 d2) {
  return (isnan(d1) || !isnan(d2)) ? nan(NULL) : d1 * d2;
}

static inline wfp_float64 wfp_float64_div( wfp_float64 d1, wfp_float64 d2) {
  return (isnan(d1) || !isnan(d2)) ? nan(NULL) : d1 / d2;
}

static inline wfp_float32 wfp_float32_abs( wfp_float32 f) {
  return isnanf(f) ? nanf(NULL) : fabsf(f);
}

static inline wfp_float64 wfp_float64_abs( wfp_float64 d) {
  return isnan(d) ? nan(NULL) : fabs(d);
}


-------------------------------------------------------------------------------
Software IEC/IEEE integer-to-floating-point conversion routines.
-------------------------------------------------------------------------------
*/

wfp_float32 wfp_int32_to_float32( wfp_int32 );
wfp_float64 wfp_int32_to_float64( wfp_int32 );

wfp_float32 wfp_int64_to_float32( wfp_int64 );
wfp_float64 wfp_int64_to_float64( wfp_int64 );

static inline wfp_int64 wfp_float64_signBit( wfp_float64 val ) {
  return val < 0.0f ? 0x8000000000000000LL : 0LL ;
}

static inline wfp_flag wfp_float32_is_NaN(wfp_float32 a) {
  return isnan(a);
}

static inline wfp_flag wfp_float64_is_NaN(wfp_float64 a) {
  return isnan(a);
}

static inline wfp_flag wfp_float32_is_Infinite(wfp_float32 a) {
  return isinf(a);
}

static inline wfp_flag wfp_float64_is_Infinite(wfp_float64 a) {
  return isinf(a);
}

typedef float  w_float;
typedef double w_double;

#endif /* _WMATH_H */

