/****************************************************************************
* Copyright (c) 2003 by Chris Gray, trading as /k/ Embedded Java Solutions. *
* All rights reserved.  The contents of this file may not be copied or      *
* distributed in any form without express written consent of the author.    *
****************************************************************************/

/*
** $Id: wmath.c,v 1.1 2005/04/18 17:31:29 cvs Exp $
*/

/*
** To optimize for speed, define IEEE_INLINE to be 'inline'
*/

#define IEEE_INLINE inline

#include "wmath.h"

/*
-------------------------------------------------------------------------------
Floating-point rounding mode, extended double-precision rounding precision,
and exception flags.
-------------------------------------------------------------------------------
*/

wfp_int8 wfp_float_rounding_mode = wfp_float_round_nearest_even;
wfp_int8 wfp_float_exception_flags = 0;

/*
-------------------------------------------------------------------------------
Primitive arithmetic functions, including multi-word arithmetic, and
division and square root approximations.  (Can be specialized to target if
desired.)
-------------------------------------------------------------------------------

w_float wfp_float32_negate(w_float value) {
  if (value & 0x80000000) {
    return value & ~(0x80000000);
  }
  return value | 0x80000000;
}

w_double wfp_float64_negate(w_double value) {
  if (value & 0x8000000000000000LL) {
    return value & ~(0x8000000000000000LL);
  }
  return value | 0x8000000000000000LL;
}
*/

/*
-------------------------------------------------------------------------------
Shifts `a' right by the number of bits given in `count'.  If any nonzero
bits are shifted off, they are ``jammed'' into the least significant bit of
the result by setting the least significant bit to 1.  The value of `count'
can be arbitrarily large; in particular, if `count' is greater than 32, the
result will be either 0 or 1, depending on whether `a' is zero or nonzero.
The result is stored in the location pointed to by `zPtr'.
-------------------------------------------------------------------------------
*/

static IEEE_INLINE void wfp_shift32RightJamming( wfp_bits32 a, wfp_int16 count, wfp_bits32 *zPtr ) {

    wfp_bits32 z;

    if ( count == 0 ) {
        z = a;
    }
    else if ( count < 32 ) {
        z = ( a>>count ) | ( ( a<<( ( - count ) & 31 ) ) != 0 );
    }
    else {
        z = ( a != 0 );
    }
    *zPtr = z;

}

/*
-------------------------------------------------------------------------------
Shifts `a' right by the number of bits given in `count'.  If any nonzero
bits are shifted off, they are ``jammed'' into the least significant bit of
the result by setting the least significant bit to 1.  The value of `count'
can be arbitrarily large; in particular, if `count' is greater than 64, the
result will be either 0 or 1, depending on whether `a' is zero or nonzero.
The result is stored in the location pointed to by `zPtr'.
-------------------------------------------------------------------------------
*/

static IEEE_INLINE void wfp_shift64RightJamming( wfp_bits64 a, wfp_int16 count, wfp_bits64 *zPtr ) {

    wfp_bits64 z;

    if ( count == 0 ) {
        z = a;
    }
    else if ( count < 64 ) {
        z = ( a>>count ) | ( ( a<<( ( - count ) & 63 ) ) != 0 );
    }
    else {
        z = ( a != 0 );
    }
    *zPtr = z;

}

/*
-------------------------------------------------------------------------------
Adds the 128-bit value formed by concatenating `a0' and `a1' to the 128-bit
value formed by concatenating `b0' and `b1'.  Addition is modulo 2^128, so
any carry out is lost.  The result is broken into two 64-bit pieces which
are stored at the locations pointed to by `z0Ptr' and `z1Ptr'.
-------------------------------------------------------------------------------
*/

static IEEE_INLINE void wfp_add128(wfp_bits64 a0, wfp_bits64 a1, wfp_bits64 b0, wfp_bits64 b1, wfp_bits64 *z0Ptr, wfp_bits64 *z1Ptr ) {

    wfp_bits64 z1;

    z1 = a1 + b1;
    *z1Ptr = z1;
    *z0Ptr = a0 + b0 + ( z1 < a1 );

}

/*
-------------------------------------------------------------------------------
Subtracts the 128-bit value formed by concatenating `b0' and `b1' from the
128-bit value formed by concatenating `a0' and `a1'.  Subtraction is modulo
2^128, so any borrow out (carry out) is lost.  The result is broken into two
64-bit pieces which are stored at the locations pointed to by `z0Ptr' and
`z1Ptr'.
-------------------------------------------------------------------------------
*/

static IEEE_INLINE void wfp_sub128(wfp_bits64 a0, wfp_bits64 a1, wfp_bits64 b0, wfp_bits64 b1, wfp_bits64 *z0Ptr, wfp_bits64 *z1Ptr ) {

    *z1Ptr = a1 - b1;
    *z0Ptr = a0 - b0 - ( a1 < b1 );

}

/*
-------------------------------------------------------------------------------
Multiplies `a' by `b' to obtain a 128-bit product.  The product is broken
into two 64-bit pieces which are stored at the locations pointed to by
`z0Ptr' and `z1Ptr'.
-------------------------------------------------------------------------------
*/

static IEEE_INLINE void wfp_mul64To128( wfp_bits64 a, wfp_bits64 b, wfp_bits64 *z0Ptr, wfp_bits64 *z1Ptr ) {

    wfp_bits32 aHigh, aLow, bHigh, bLow;
    wfp_bits64 z0, zMiddleA, zMiddleB, z1;

    aLow = a;
    aHigh = a>>32;
    bLow = b;
    bHigh = b>>32;
    z1 = ( (wfp_bits64) aLow ) * bLow;
    zMiddleA = ( (wfp_bits64) aLow ) * bHigh;
    zMiddleB = ( (wfp_bits64) aHigh ) * bLow;
    z0 = ( (wfp_bits64) aHigh ) * bHigh;
    zMiddleA += zMiddleB;
    z0 += ( ( (wfp_bits64) ( zMiddleA < zMiddleB ) )<<32 ) + ( zMiddleA>>32 );
    zMiddleA <<= 32;
    z1 += zMiddleA;
    z0 += ( z1 < zMiddleA );
    *z1Ptr = z1;
    *z0Ptr = z0;

}

/*
-------------------------------------------------------------------------------
Returns an approximation to the 64-bit integer quotient obtained by dividing
`b' into the 128-bit value formed by concatenating `a0' and `a1'.  The
divisor `b' must be at least 2^63.  If q is the exact quotient truncated
toward zero, the approximation returned lies between q and q + 2 inclusive.
If the exact quotient q is larger than 64 bits, the maximum positive 64-bit
unsigned integer is returned.
-------------------------------------------------------------------------------
*/

static wfp_bits64 wfp_estimateDiv128To64( wfp_bits64 a0, wfp_bits64 a1, wfp_bits64 b ) {

    wfp_bits64 b0, b1;
    wfp_bits64 rem0, rem1, term0, term1;
    wfp_bits64 z;

    if ( b <= a0 ) return WFP_LIT64( 0xFFFFFFFFFFFFFFFF );
    b0 = b>>32;
    z = ( b0<<32 <= a0 ) ? WFP_LIT64( 0xFFFFFFFF00000000 ) : ( a0 / b0 )<<32;
    wfp_mul64To128( b, z, &term0, &term1 );
    wfp_sub128( a0, a1, term0, term1, &rem0, &rem1 );
    while ( ( (wfp_sbits64) rem0 ) < 0 ) {
        z -= WFP_LIT64( 0x100000000 );
        b1 = b<<32;
        wfp_add128( rem0, rem1, b0, b1, &rem0, &rem1 );
    }
    rem0 = ( rem0<<32 ) | ( rem1>>32 );
    z |= ( b0<<32 <= rem0 ) ? 0xFFFFFFFF : rem0 / b0;
    return z;

}

/*
-------------------------------------------------------------------------------
Returns the number of leading 0 bits before the most-significant 1 bit of
`a'.  If `a' is zero, 32 is returned.
-------------------------------------------------------------------------------
*/

IEEE_INLINE static wfp_int8 wfp_countLeadingZeros32( wfp_bits32 a ) {

    static const unsigned char wfp_countLeadingZerosHigh[] = {
        8, 7, 6, 6, 5, 5, 5, 5, 4, 4, 4, 4, 4, 4, 4, 4,
        3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    };

    wfp_int8 shiftCount;

    shiftCount = 0;
    if ( a < 0x10000 ) {
        shiftCount += 16;
        a <<= 16;
    }
    if ( a < 0x1000000 ) {
        shiftCount += 8;
        a <<= 8;
    }
    shiftCount += wfp_countLeadingZerosHigh[ a>>24 ];
    return shiftCount;

}

/*
-------------------------------------------------------------------------------
Raises the exceptions specified by `flags'.  Floating-point traps can be
defined here if desired.  It is currently not possible for such a trap to
substitute a result value.  If traps are not implemented, this routine
should be simply `float_exception_flags |= flags;'.
-------------------------------------------------------------------------------
*/

void wfp_float_raise( wfp_int8 flags ) {

    // TODO lookup current thread and send appropriate message as Throwable...
    //wabort(ABORT_WONKA, "FP exception\n");
    //woempa(9, "Floating point exception 0x%08x.\n", flags);
    wfp_float_exception_flags |= flags;
}

/*
-------------------------------------------------------------------------------
Internal canonical NaN format.
-------------------------------------------------------------------------------
*/

typedef struct {
    wfp_flag sign;
    wfp_bits64 high, low;
} wfp_commonNaNT;

/*
-------------------------------------------------------------------------------
The pattern for a default generated single-precision NaN.
-------------------------------------------------------------------------------
*/

#define wfp_float32_default_nan 0xFFFFFFFF

/*
-------------------------------------------------------------------------------
Returns 1 if the single-precision floating-point value `a' is a NaN;
otherwise returns 0.
-------------------------------------------------------------------------------
*/

wfp_flag wfp_float32_is_nan( wfp_float32 a ) {

    return ( 0xFF000000 < (wfp_bits32) ( a<<1 ) );

}

/*
-------------------------------------------------------------------------------
Returns 1 if the single-precision floating-point value `a' is a signaling
NaN; otherwise returns 0.
-------------------------------------------------------------------------------
*/

wfp_flag wfp_float32_is_signaling_nan( wfp_float32 a ) {

    return ( ( ( a>>22 ) & 0x1FF ) == 0x1FE ) && ( a & 0x003FFFFF );

}

/*
-------------------------------------------------------------------------------
Returns the result of converting the single-precision floating-point NaN
`a' to the canonical NaN format.  If `a' is a signaling NaN, the invalid
exception is raised.
-------------------------------------------------------------------------------
*/

IEEE_INLINE static void wfp_float32ToCommonNaN( wfp_commonNaNT *z, wfp_float32 a ) {

    if ( wfp_float32_is_signaling_nan( a ) ) wfp_float_raise( wfp_float_flag_invalid );
    z->sign = a>>31;
    z->low = 0;
    z->high = ( (wfp_bits64) a )<<41;

}

/*
-------------------------------------------------------------------------------
Returns the result of converting the canonical NaN `a' to the single-
precision floating-point format.
-------------------------------------------------------------------------------
*/

IEEE_INLINE static wfp_float32 wfp_commonNaNToFloat32( wfp_commonNaNT a ) {

    return ( ( (wfp_bits32) a.sign )<<31 ) | 0x7FC00000 | ( a.high>>41 );

}

/*
-------------------------------------------------------------------------------
Takes two single-precision floating-point values `a' and `b', one of which
is a NaN, and returns the appropriate NaN result.  If either `a' or `b' is a
signaling NaN, the invalid exception is raised.
-------------------------------------------------------------------------------
*/

static wfp_float32 wfp_propagateFloat32NaN( wfp_float32 a, wfp_float32 b ) {

    wfp_flag aIsNaN, aIsSignalingNaN, bIsNaN, bIsSignalingNaN;

    aIsNaN = wfp_float32_is_nan( a );
    aIsSignalingNaN = wfp_float32_is_signaling_nan( a );
    bIsNaN = wfp_float32_is_nan( b );
    bIsSignalingNaN = wfp_float32_is_signaling_nan( b );
    a |= 0x00400000;
    b |= 0x00400000;
    if ( aIsSignalingNaN | bIsSignalingNaN ) wfp_float_raise( wfp_float_flag_invalid );
    if ( aIsNaN ) {
        return ( aIsSignalingNaN & bIsNaN ) ? b : a;
    }
    else {
        return b;
    }

}

/*
-------------------------------------------------------------------------------
The pattern for a default generated double-precision NaN.
-------------------------------------------------------------------------------
*/

#define wfp_float64_default_nan WFP_LIT64( 0xFFFFFFFFFFFFFFFF )

/*
-------------------------------------------------------------------------------
Returns 1 if the double-precision floating-point value `a' is a NaN;
otherwise returns 0.
-------------------------------------------------------------------------------
*/

IEEE_INLINE static wfp_flag wfp_float64_is_nan( wfp_float64 a ) {

    return ( WFP_LIT64( 0xFFE0000000000000 ) < (wfp_bits64) ( a<<1 ) );

}

/*
-------------------------------------------------------------------------------
Returns 1 if the double-precision floating-point value `a' is a signaling
NaN; otherwise returns 0.
-------------------------------------------------------------------------------
*/

wfp_flag wfp_float64_is_signaling_nan( wfp_float64 a ) {

    return ( ( ( a>>51 ) & 0xFFF ) == 0xFFE ) 
          && ( a & WFP_LIT64( 0x0007FFFFFFFFFFFF ) 
           );

}

IEEE_INLINE static wfp_flag wfp_i_float64_is_signaling_nan( wfp_float64 a ) {

    return ( ( ( a>>51 ) & 0xFFF ) == 0xFFE ) 
          && ( a & WFP_LIT64( 0x0007FFFFFFFFFFFF ) 
           );

}

/*
-------------------------------------------------------------------------------
Returns the result of converting the canonical NaN `a' to the double-
precision floating-point format.
-------------------------------------------------------------------------------
*/

static wfp_float64 wfp_commonNaNToFloat64( wfp_commonNaNT a ) {

    return
          ( ( (wfp_bits64) a.sign )<<63 )
        | WFP_LIT64( 0x7FF8000000000000 )
        | ( a.high>>12 );

}

/*
-------------------------------------------------------------------------------
Takes two double-precision floating-point values `a' and `b', one of which
is a NaN, and returns the appropriate NaN result.  If either `a' or `b' is a
signaling NaN, the invalid exception is raised.
-------------------------------------------------------------------------------
*/

static wfp_float64 wfp_propagateFloat64NaN( wfp_float64 a, wfp_float64 b ) {

    wfp_flag aIsNaN, aIsSignalingNaN, bIsNaN, bIsSignalingNaN;

    aIsNaN = wfp_float64_is_nan( a );
    aIsSignalingNaN = wfp_i_float64_is_signaling_nan( a );
    bIsNaN = wfp_float64_is_nan( b );
    bIsSignalingNaN = wfp_i_float64_is_signaling_nan( b );
    a |= WFP_LIT64( 0x0008000000000000 );
    b |= WFP_LIT64( 0x0008000000000000 );
    if ( aIsSignalingNaN | bIsSignalingNaN ) wfp_float_raise( wfp_float_flag_invalid );
    if ( aIsNaN ) {
        return ( aIsSignalingNaN & bIsNaN ) ? b : a;
    }
    else {
        return b;
    }

}

/*
-------------------------------------------------------------------------------
Normalizes the subnormal single-precision floating-point value represented
by the denormalized significand `aSig'.  The normalized exponent and
significand are stored at the locations pointed to by `zExpPtr' and
`zSigPtr', respectively.
-------------------------------------------------------------------------------
*/

static void wfp_normalizeFloat32Subnormal( wfp_bits32 aSig, wfp_int16 *zExpPtr, wfp_bits32 *zSigPtr ) {

    wfp_int8 shiftCount;

    shiftCount = wfp_countLeadingZeros32( aSig ) - 8;
    *zSigPtr = aSig<<shiftCount;
    *zExpPtr = 1 - shiftCount;

}

/*
-------------------------------------------------------------------------------
Packs the sign `zSign', exponent `zExp', and significand `zSig' into a
single-precision floating-point value, returning the result.  After being
shifted into the proper positions, the three fields are simply added
together to form the result.  This means that any integer portion of `zSig'
will be added into the exponent.  Since a properly normalized significand
will have an integer portion equal to 1, the `zExp' input should be 1 less
than the desired result exponent whenever `zSig' is a complete, normalized
significand.
-------------------------------------------------------------------------------
*/

static IEEE_INLINE wfp_float32 wfp_packFloat32( wfp_flag zSign, wfp_int16 zExp, wfp_bits32 zSig ) {

    return ( ( (wfp_bits32) zSign )<<31 ) + ( ( (wfp_bits32) zExp )<<23 ) + zSig;

}

/*
-------------------------------------------------------------------------------
Returns the number of leading 0 bits before the most-significant 1 bit of
`a'.  If `a' is zero, 64 is returned.
-------------------------------------------------------------------------------
*/

IEEE_INLINE static wfp_int8 wfp_countLeadingZeros64( wfp_bits64 a ) {

    wfp_int8 shiftCount;

    shiftCount = 0;
    if ( a < ( (wfp_bits64) 1 )<<32 ) {
        shiftCount += 32;
    }
    else {
        a >>= 32;
    }
    shiftCount += wfp_countLeadingZeros32( (wfp_bits32)a );
    return shiftCount;

}

/*
-------------------------------------------------------------------------------
Functions and definitions to determine:  (1) whether tininess for underflow
is detected before or after rounding by default, (2) what (if anything)
happens when exceptions are raised, (3) how signaling NaNs are distinguished
from quiet NaNs, (4) the default generated quiet NaNs, and (5) how NaNs
are propagated from function inputs to output.  These details are target-
specific.
-------------------------------------------------------------------------------
*/

/*
-------------------------------------------------------------------------------
Underflow tininess-detection mode, statically initialized to default value.
(The declaration in `softfloat.h' must match the `wfp_int8' type here.)
-------------------------------------------------------------------------------
*/

wfp_int8 wfp_float_detect_tininess = wfp_float_tininess_after_rounding;

/*
-------------------------------------------------------------------------------
Takes an abstract floating-point value having sign `zSign', exponent `zExp',
and significand `zSig', and returns the proper single-precision floating-
point value corresponding to the abstract input.  Ordinarily, the abstract
value is simply rounded and packed into the single-precision format, with
the inexact exception raised if the abstract input cannot be represented
exactly.  However, if the abstract value is too large, the overflow and
inexact exceptions are raised and an infinity or maximal finite value is
returned.  If the abstract value is too small, the input value is rounded to
a subnormal number, and the underflow and inexact exceptions are raised if
the abstract input cannot be represented exactly as a subnormal single-
precision floating-point number.
    The input significand `zSig' has its binary point between bits 30
and 29, which is 7 bits to the left of the usual location.  This shifted
significand must be normalized or smaller.  If `zSig' is not normalized,
`zExp' must be 0; in that case, the result returned is a subnormal number,
and it must not require rounding.  In the usual case that `zSig' is
normalized, `zExp' must be 1 less than the ``true'' floating-point exponent.
The handling of underflow and overflow follows the IEC/IEEE Standard for
Binary Floating-Point Arithmetic.
-------------------------------------------------------------------------------
*/

static wfp_float32 wfp_roundAndPackFloat32( wfp_flag zSign, wfp_int16 zExp, wfp_bits32 zSig ) {

    wfp_int8 roundingMode;
    wfp_flag roundNearestEven;
    wfp_int8 roundIncrement, roundBits;
    wfp_flag isTiny;

    roundingMode = wfp_float_rounding_mode;
    roundNearestEven = ( roundingMode == wfp_float_round_nearest_even );
    roundIncrement = 0x40;
    if ( ! roundNearestEven ) {
        if ( roundingMode == wfp_float_round_to_zero ) {
            roundIncrement = 0;
        }
        else {
            roundIncrement = 0x7F;
            if ( zSign ) {
                if ( roundingMode == wfp_float_round_up ) roundIncrement = 0;
            }
            else {
                if ( roundingMode == wfp_float_round_down ) roundIncrement = 0;
            }
        }
    }
    roundBits = zSig & 0x7F;
    if ( 0x000000FD <= (wfp_bits32) zExp ) {
        if (    ( 0xFD < zExp )
             || (    ( zExp == 0xFD )
                  && ( (wfp_sbits32) ( zSig + roundIncrement ) < 0 ) )
           ) {
            wfp_float_raise( wfp_float_flag_overflow | wfp_float_flag_inexact );
            return wfp_packFloat32( zSign, 0xFF, 0 ) - ( roundIncrement == 0 );
        }
        if ( zExp < 0 ) {
            isTiny =
                   ( wfp_float_detect_tininess == wfp_float_tininess_before_rounding )
                || ( zExp < -1 )
                || ( zSig + roundIncrement < 0x80000000 );
            wfp_shift32RightJamming( zSig, - zExp, &zSig );
            zExp = 0;
            roundBits = zSig & 0x7F;
            if ( isTiny && roundBits ) wfp_float_raise( wfp_float_flag_underflow );
        }
    }
    if ( roundBits ) wfp_float_exception_flags |= wfp_float_flag_inexact;
    zSig = ( zSig + roundIncrement )>>7;
    zSig &= ~ ( ( ( roundBits ^ 0x40 ) == 0 ) & roundNearestEven );
    if ( zSig == 0 ) zExp = 0;
    return wfp_packFloat32( zSign, zExp, zSig );

}

/*
-------------------------------------------------------------------------------
Takes an abstract floating-point value having sign `zSign', exponent `zExp',
and significand `zSig', and returns the proper single-precision floating-
point value corresponding to the abstract input.  This routine is just like
`wfp_roundAndPackFloat32' except that `zSig' does not have to be normalized.
Bit 31 of `zSig' must be zero, and `zExp' must be 1 less than the ``true''
floating-point exponent.
-------------------------------------------------------------------------------
*/

static IEEE_INLINE wfp_float32 wfp_normalizeRoundAndPackFloat32( wfp_flag zSign, wfp_int16 zExp, wfp_bits32 zSig ) {

    wfp_int8 shiftCount;

    shiftCount = wfp_countLeadingZeros32( zSig ) - 1;
    return wfp_roundAndPackFloat32( zSign, zExp - shiftCount, zSig<<shiftCount );

}

/*
-------------------------------------------------------------------------------
Returns the fraction bits of the double-precision floating-point value `a'.
-------------------------------------------------------------------------------
*/

static IEEE_INLINE wfp_bits64 wfp_extractFloat64Frac( wfp_float64 a ) {

    return a & WFP_LIT64( 0x000FFFFFFFFFFFFF );

}

/*
-------------------------------------------------------------------------------
Returns the exponent bits of the double-precision floating-point value `a'.
-------------------------------------------------------------------------------
*/

static IEEE_INLINE wfp_int16 wfp_extractFloat64Exp( wfp_float64 a ) {

    return ( a>>52 ) & 0x7FF;

}

/*
-------------------------------------------------------------------------------
Returns the sign bit of the double-precision floating-point value `a'.
-------------------------------------------------------------------------------
*/

static IEEE_INLINE wfp_flag wfp_extractFloat64Sign( wfp_float64 a ) {

    return a>>63;

}

/*
-------------------------------------------------------------------------------
Normalizes the subnormal double-precision floating-point value represented
by the denormalized significand `aSig'.  The normalized exponent and
significand are stored at the locations pointed to by `zExpPtr' and
`zSigPtr', respectively.
-------------------------------------------------------------------------------
*/

static void wfp_normalizeFloat64Subnormal( wfp_bits64 aSig, wfp_int16 *zExpPtr, wfp_bits64 *zSigPtr ) {

    wfp_int8 shiftCount;

    shiftCount = wfp_countLeadingZeros64( aSig ) - 11;
    *zSigPtr = aSig<<shiftCount;
    *zExpPtr = 1 - shiftCount;

}

/*
-------------------------------------------------------------------------------
Packs the sign `zSign', exponent `zExp', and significand `zSig' into a
double-precision floating-point value, returning the result.  After being
shifted into the proper positions, the three fields are simply added
together to form the result.  This means that any integer portion of `zSig'
will be added into the exponent.  Since a properly normalized significand
will have an integer portion equal to 1, the `zExp' input should be 1 less
than the desired result exponent whenever `zSig' is a complete, normalized
significand.
-------------------------------------------------------------------------------
*/

static IEEE_INLINE wfp_float64 wfp_packFloat64( wfp_flag zSign, wfp_int16 zExp, wfp_bits64 zSig ) {

    return ( ( (wfp_bits64) zSign )<<63 ) + ( ( (wfp_bits64) zExp )<<52 ) + zSig;

}

/*
-------------------------------------------------------------------------------
Takes an abstract floating-point value having sign `zSign', exponent `zExp',
and significand `zSig', and returns the proper double-precision floating-
point value corresponding to the abstract input.  Ordinarily, the abstract
value is simply rounded and packed into the double-precision format, with
the inexact exception raised if the abstract input cannot be represented
exactly.  However, if the abstract value is too large, the overflow and
inexact exceptions are raised and an infinity or maximal finite value is
returned.  If the abstract value is too small, the input value is rounded to
a subnormal number, and the underflow and inexact exceptions are raised if
the abstract input cannot be represented exactly as a subnormal double-
precision floating-point number.
    The input significand `zSig' has its binary point between bits 62
and 61, which is 10 bits to the left of the usual location.  This shifted
significand must be normalized or smaller.  If `zSig' is not normalized,
`zExp' must be 0; in that case, the result returned is a subnormal number,
and it must not require rounding.  In the usual case that `zSig' is
normalized, `zExp' must be 1 less than the ``true'' floating-point exponent.
The handling of underflow and overflow follows the IEC/IEEE Standard for
Binary Floating-Point Arithmetic.
-------------------------------------------------------------------------------
*/

static wfp_float64 wfp_roundAndPackFloat64( wfp_flag zSign, wfp_int16 zExp, wfp_bits64 zSig ) {

    wfp_int8 roundingMode;
    wfp_flag roundNearestEven;
    wfp_int16 roundIncrement, roundBits;
    wfp_flag isTiny;

    roundingMode = wfp_float_rounding_mode;
    roundNearestEven = ( roundingMode == wfp_float_round_nearest_even );
    roundIncrement = 0x200;
    if ( ! roundNearestEven ) {
        if ( roundingMode == wfp_float_round_to_zero ) {
            roundIncrement = 0;
        }
        else {
            roundIncrement = 0x3FF;
            if ( zSign ) {
                if ( roundingMode == wfp_float_round_up ) roundIncrement = 0;
            }
            else {
                if ( roundingMode == wfp_float_round_down ) roundIncrement = 0;
            }
        }
    }
    roundBits = zSig & 0x3FF;
    if ( 0x07FD <= (wfp_bits32) zExp ) {
        if (    ( 0x7FD < zExp )
             || (    ( zExp == 0x7FD )
                  && ( (wfp_sbits64) ( zSig + roundIncrement ) < 0 ) )
           ) {
            wfp_float_raise( wfp_float_flag_overflow | wfp_float_flag_inexact );
            return wfp_packFloat64( zSign, 0x7FF, WFP_LIT64(0) ) - ( roundIncrement == 0 );
        }
        if ( zExp < 0 ) {
            isTiny =
                   ( wfp_float_detect_tininess == wfp_float_tininess_before_rounding )
                || ( zExp < -1 )
                || ( zSig + roundIncrement < WFP_LIT64( 0x8000000000000000 ) );
            wfp_shift64RightJamming( zSig, - zExp, &zSig );
            zExp = 0;
            roundBits = zSig & 0x3FF;
            if ( isTiny && roundBits ) wfp_float_raise( wfp_float_flag_underflow );
        }
    }
    if ( roundBits ) wfp_float_exception_flags |= wfp_float_flag_inexact;
    zSig = ( zSig + roundIncrement )>>10;
    zSig &= ~ ( ( ( roundBits ^ 0x200 ) == 0 ) & roundNearestEven );
    if ( zSig == 0 ) zExp = 0;
    return wfp_packFloat64( zSign, zExp, zSig );

}

/*
-------------------------------------------------------------------------------
Takes an abstract floating-point value having sign `zSign', exponent `zExp',
and significand `zSig', and returns the proper double-precision floating-
point value corresponding to the abstract input.  This routine is just like
`wfp_roundAndPackFloat64' except that `zSig' does not have to be normalized.
Bit 63 of `zSig' must be zero, and `zExp' must be 1 less than the ``true''
floating-point exponent.
-------------------------------------------------------------------------------
*/

static IEEE_INLINE wfp_float64 wfp_normalizeRoundAndPackFloat64( wfp_flag zSign, wfp_int16 zExp, wfp_bits64 zSig ) {

    wfp_int8 shiftCount;

    shiftCount = wfp_countLeadingZeros64( zSig ) - 1;
    return wfp_roundAndPackFloat64( zSign, zExp - shiftCount, zSig<<shiftCount );

}

/*
-------------------------------------------------------------------------------
Returns the result of converting the double-precision floating-point NaN
`a' to the canonical NaN format.  If `a' is a signaling NaN, the invalid
exception is raised.
-------------------------------------------------------------------------------
*/

static IEEE_INLINE void wfp_float64ToCommonNaN( wfp_commonNaNT *z, wfp_float64 a ) {

    if ( wfp_i_float64_is_signaling_nan( a ) ) wfp_float_raise( wfp_float_flag_invalid );
    z->sign = a>>63;
    z->low = 0;
    z->high = a<<12;

}

/*
-------------------------------------------------------------------------------
Returns the fraction bits of the single-precision floating-point value `a'.
-------------------------------------------------------------------------------
*/

static IEEE_INLINE wfp_bits32 wfp_extractFloat32Frac( wfp_float32 a ) {

    return a & 0x007FFFFF;

}

/*
-------------------------------------------------------------------------------
Returns the exponent bits of the single-precision floating-point value `a'.
-------------------------------------------------------------------------------
*/

static IEEE_INLINE wfp_int16 wfp_extractFloat32Exp( wfp_float32 a ) {

    return ( a>>23 ) & 0xFF;

}

/*
-------------------------------------------------------------------------------
Returns the sign bit of the single-precision floating-point value `a'.
-------------------------------------------------------------------------------
*/

static IEEE_INLINE wfp_flag wfp_extractFloat32Sign( wfp_float32 a ) {

    return a>>31;

}

/*
-------------------------------------------------------------------------------
Returns the result of converting the 32-bit two's complement integer `a'
to the single-precision floating-point format.  The conversion is performed
according to the IEC/IEEE Standard for Binary Floating-Point Arithmetic.
-------------------------------------------------------------------------------
*/

wfp_float32 wfp_int32_to_float32( wfp_int32 a ) {

    wfp_flag zSign;

    if ( a == 0 ) return 0;
    if ( a == (wfp_sbits32) 0x80000000 ) return wfp_packFloat32( 1, 0x9E, 0 );
    zSign = ( a < 0 );
    return wfp_normalizeRoundAndPackFloat32( zSign, 0x9C, zSign ? (wfp_bits32)(- a) : (wfp_bits32)a );

}

/*
-------------------------------------------------------------------------------
Returns the result of converting the 32-bit two's complement integer `a'
to the double-precision floating-point format.  The conversion is performed
according to the IEC/IEEE Standard for Binary Floating-Point Arithmetic.
-------------------------------------------------------------------------------
*/

wfp_float64 wfp_int32_to_float64( wfp_int32 a ) {

    wfp_flag zSign;
    wfp_uint32 absA;
    wfp_int8 shiftCount;
    wfp_bits64 zSig;

    if ( a == 0 ) return 0;
    zSign = ( a < 0 );
    absA = zSign ? - a : a;
    shiftCount = wfp_countLeadingZeros32( absA ) + 21;
    zSig = absA;
    return wfp_packFloat64( zSign, 0x432 - shiftCount, zSig<<shiftCount );

}

/*
-------------------------------------------------------------------------------
Returns the result of converting the 64-bit two's complement integer `a'
to the single-precision floating-point format.  The conversion is performed
according to the IEC/IEEE Standard for Binary Floating-Point Arithmetic.
-------------------------------------------------------------------------------
*/
wfp_float32 wfp_int64_to_float32( wfp_int64 a ) {

    wfp_flag zSign;
    wfp_uint64 absA;
    wfp_int8 shiftCount;

    if ( a == 0 ) return 0;
    zSign = ( a < 0 );
    absA = zSign ? - a : a;
    shiftCount = wfp_countLeadingZeros64( absA ) - 40;
    if ( 0 <= shiftCount ) {
// ORIGINAL        return wfp_packFloat32( zSign, 0x95 - shiftCount, absA<<shiftCount );
        return wfp_packFloat32( zSign, 0x95 - shiftCount, (wfp_bits32)absA<<shiftCount );
    }
    else {
        shiftCount += 7;
        if ( shiftCount < 0 ) {
            wfp_shift64RightJamming( absA, - shiftCount, &absA );
        }
        else {
            absA <<= shiftCount;
        }
// ORIGINAL        return wfp_roundAndPackFloat32( zSign, 0x9C - shiftCount, absA );
        return wfp_roundAndPackFloat32( zSign, 0x9C - shiftCount, (wfp_bits32)absA );
    }

}

/*
-------------------------------------------------------------------------------
Returns the result of converting the 64-bit two's complement integer `a'
to the double-precision floating-point format.  The conversion is performed
according to the IEC/IEEE Standard for Binary Floating-Point Arithmetic.
-------------------------------------------------------------------------------
*/

wfp_float64 wfp_int64_to_float64( wfp_int64 a ) {

    wfp_flag zSign;

    if ( a == 0 ) return 0;
    if ( a == (wfp_sbits64) WFP_LIT64( 0x8000000000000000 ) ) {
        return wfp_packFloat64( 1, 0x43E, WFP_LIT64(0) );
    }
    zSign = ( a < 0 );
    return wfp_normalizeRoundAndPackFloat64( zSign, 0x43C, zSign ? (wfp_bits64)(- a) : (wfp_bits64)a );

}

/*
-------------------------------------------------------------------------------
Returns the result of converting the single-precision floating-point value
`a' to the 32-bit two's complement integer format.  The conversion is
performed according to the IEC/IEEE Standard for Binary Floating-Point
Arithmetic, except that the conversion is always rounded toward zero.
If `a' is a NaN, the largest positive integer is returned.  Otherwise, if
the conversion overflows, the largest integer with the same sign as `a' is
returned.
-------------------------------------------------------------------------------
*/

wfp_int32 wfp_float32_to_int32_round_to_zero( wfp_float32 a ) {

    wfp_flag aSign;
    wfp_int16 aExp, shiftCount;
    wfp_bits32 aSig;
    wfp_int32 z;

    aSig = wfp_extractFloat32Frac( a );
    aExp = wfp_extractFloat32Exp( a );
    aSign = wfp_extractFloat32Sign( a );
    shiftCount = aExp - 0x9E;
    if ( 0 <= shiftCount ) {
        if ( a != 0xCF000000 ) {
            wfp_float_raise( wfp_float_flag_invalid );
            if ( ! aSign || ( ( aExp == 0xFF ) && aSig ) ) return 0x7FFFFFFF;
        }
        return (wfp_sbits32) 0x80000000;
    }
    else if ( aExp <= 0x7E ) {
        if ( aExp | aSig ) wfp_float_exception_flags |= wfp_float_flag_inexact;
        return 0;
    }
    aSig = ( aSig | 0x00800000 )<<8;
    z = aSig>>( - shiftCount );
    if ( (wfp_bits32) ( aSig<<( shiftCount & 31 ) ) ) {
        wfp_float_exception_flags |= wfp_float_flag_inexact;
    }
    if ( aSign ) z = - z;
    return z;

}

/*
-------------------------------------------------------------------------------
Returns 1 if the single-precision floating-point value `a' is equal to
the corresponding value `b', and 0 otherwise.  The comparison is performed
according to the IEC/IEEE Standard for Binary Floating-Point Arithmetic.
-------------------------------------------------------------------------------
*/

wfp_flag wfp_float32_eq( wfp_float32 a, wfp_float32 b ) {

    if (    ( ( wfp_extractFloat32Exp( a ) == 0xFF ) && wfp_extractFloat32Frac( a ) )
         || ( ( wfp_extractFloat32Exp( b ) == 0xFF ) && wfp_extractFloat32Frac( b ) )
       ) {
        if ( wfp_float32_is_signaling_nan( a ) || wfp_float32_is_signaling_nan( b ) ) {
            wfp_float_raise( wfp_float_flag_invalid );
        }
        return 0;
    }
    return ( a == b ) || ( (wfp_bits32) ( ( a | b )<<1 ) == 0 );

}

/*
-------------------------------------------------------------------------------
Returns 1 if the single-precision floating-point value `a' is less than
the corresponding value `b', and 0 otherwise.  The comparison is performed
according to the IEC/IEEE Standard for Binary Floating-Point Arithmetic.
-------------------------------------------------------------------------------
*/

wfp_flag wfp_float32_lt( wfp_float32 a, wfp_float32 b ) {

    wfp_flag aSign, bSign;

    if (    ( ( wfp_extractFloat32Exp( a ) == 0xFF ) && wfp_extractFloat32Frac( a ) )
         || ( ( wfp_extractFloat32Exp( b ) == 0xFF ) && wfp_extractFloat32Frac( b ) )
       ) {
        wfp_float_raise( wfp_float_flag_invalid );
        return 0;
    }
    aSign = wfp_extractFloat32Sign( a );
    bSign = wfp_extractFloat32Sign( b );
    if ( aSign != bSign ) return aSign && ( (wfp_bits32) ( ( a | b )<<1 ) != 0 );
    return ( a != b ) && ( aSign ^ ( a < b ) );

}

/*
-------------------------------------------------------------------------------
Returns the result of converting the double-precision floating-point value
`a' to the 32-bit two's complement integer format.  The conversion is
performed according to the IEC/IEEE Standard for Binary Floating-Point
Arithmetic, except that the conversion is always rounded toward zero.
If `a' is a NaN, the largest positive integer is returned.  Otherwise, if
the conversion overflows, the largest integer with the same sign as `a' is
returned.
-------------------------------------------------------------------------------
*/

wfp_int32 wfp_float64_to_int32_round_to_zero( wfp_float64 a ) {

    wfp_flag aSign;
    wfp_int16 aExp, shiftCount;
    wfp_bits64 aSig, savedASig;
    wfp_int32 z;

    aSig = wfp_extractFloat64Frac( a );
    aExp = wfp_extractFloat64Exp( a );
    aSign = wfp_extractFloat64Sign( a );
    if ( 0x41E < aExp ) {
        if ( ( aExp == 0x7FF ) && aSig ) aSign = 0;
        goto invalid;
    }
    else if ( aExp < 0x3FF ) {
        if ( aExp || aSig ) wfp_float_exception_flags |= wfp_float_flag_inexact;
        return 0;
    }
    aSig |= WFP_LIT64( 0x0010000000000000 );
    shiftCount = 0x433 - aExp;
    savedASig = aSig;
    aSig >>= shiftCount;
    z = aSig;
    if ( aSign ) z = - z;
    if ( ( z < 0 ) ^ aSign ) {
 invalid:
        wfp_float_raise( wfp_float_flag_invalid );
        return aSign ? (wfp_sbits32) 0x80000000 : 0x7FFFFFFF;
    }
    if ( ( aSig<<shiftCount ) != savedASig ) {
        wfp_float_exception_flags |= wfp_float_flag_inexact;
    }
    return z;

}

/*
-------------------------------------------------------------------------------
Returns 1 if the double-precision floating-point value `a' is equal to the
corresponding value `b', and 0 otherwise.  The comparison is performed
according to the IEC/IEEE Standard for Binary Floating-Point Arithmetic.
-------------------------------------------------------------------------------
*/

wfp_flag wfp_float64_eq( wfp_float64 a, wfp_float64 b ) {

    if (    ( ( wfp_extractFloat64Exp( a ) == 0x7FF ) && wfp_extractFloat64Frac( a ) )
         || ( ( wfp_extractFloat64Exp( b ) == 0x7FF ) && wfp_extractFloat64Frac( b ) )
       ) {
        if ( wfp_i_float64_is_signaling_nan( a ) || wfp_i_float64_is_signaling_nan( b ) ) {
            wfp_float_raise( wfp_float_flag_invalid );
        }
        return 0;
    }
    return ( a == b ) || ( (wfp_bits64) ( ( a | b )<<1 ) == 0 );

}

/*
-------------------------------------------------------------------------------
Returns 1 if the double-precision floating-point value `a' is less than
the corresponding value `b', and 0 otherwise.  The comparison is performed
according to the IEC/IEEE Standard for Binary Floating-Point Arithmetic.
-------------------------------------------------------------------------------
*/

wfp_flag wfp_float64_lt( wfp_float64 a, wfp_float64 b ) {

    wfp_flag aSign, bSign;

    if (    ( ( wfp_extractFloat64Exp( a ) == 0x7FF ) && wfp_extractFloat64Frac( a ) )
         || ( ( wfp_extractFloat64Exp( b ) == 0x7FF ) && wfp_extractFloat64Frac( b ) )
       ) {
        wfp_float_raise( wfp_float_flag_invalid );
        return 0;
    }
    aSign = wfp_extractFloat64Sign( a );
    bSign = wfp_extractFloat64Sign( b );
    if ( aSign != bSign ) return aSign && ( (wfp_bits64) ( ( a | b )<<1 ) != 0 );
    return ( a != b ) && ( aSign ^ ( a < b ) );

}

/*
-------------------------------------------------------------------------------
Returns the result of converting the double-precision floating-point value
`a' to the 64-bit two's complement integer format.  The conversion is
performed according to the IEC/IEEE Standard for Binary Floating-Point
Arithmetic, except that the conversion is always rounded toward zero.
If `a' is a NaN, the largest positive integer is returned.  Otherwise, if
the conversion overflows, the largest integer with the same sign as `a' is
returned.
-------------------------------------------------------------------------------
*/

wfp_int64 wfp_float64_to_int64_round_to_zero( wfp_float64 a ) {

    wfp_flag aSign;
    wfp_int16 aExp, shiftCount;
    wfp_bits64 aSig;
    wfp_int64 z;

    aSig = wfp_extractFloat64Frac( a );
    aExp = wfp_extractFloat64Exp( a );
    aSign = wfp_extractFloat64Sign( a );
    if ( aExp ) aSig |= WFP_LIT64( 0x0010000000000000 );
    shiftCount = aExp - 0x433;
    if ( 0 <= shiftCount ) {
        if ( 0x43E <= aExp ) {
            if ( a != WFP_LIT64( 0xC3E0000000000000 ) ) {
                wfp_float_raise( wfp_float_flag_invalid );
                if (    ! aSign
                     || (    ( aExp == 0x7FF )
                          && ( aSig != WFP_LIT64( 0x0010000000000000 ) ) )
                   ) {
                    return WFP_LIT64( 0x7FFFFFFFFFFFFFFF );
                }
            }
            return (wfp_sbits64) WFP_LIT64( 0x8000000000000000 );
        }
        z = aSig<<shiftCount;
    }
    else {
        if ( aExp < 0x3FE ) {
            if ( aExp | aSig ) wfp_float_exception_flags |= wfp_float_flag_inexact;
            return 0;
        }
        z = aSig>>( - shiftCount );
        if ( (wfp_bits64) ( aSig<<( shiftCount & 63 ) ) ) {
            wfp_float_exception_flags |= wfp_float_flag_inexact;
        }
    }
    if ( aSign ) z = - z;
    return z;

}

/*
-------------------------------------------------------------------------------
Returns the result of converting the double-precision floating-point value
`a' to the single-precision floating-point format.  The conversion is
performed according to the IEC/IEEE Standard for Binary Floating-Point
Arithmetic.
-------------------------------------------------------------------------------
*/

wfp_float32 wfp_float64_to_float32( wfp_float64 a ) {

    wfp_flag aSign;
    wfp_int16 aExp;
    wfp_bits64 aSig;
    wfp_bits32 zSig;
    wfp_commonNaNT z;

    aSig = wfp_extractFloat64Frac( a );
    aExp = wfp_extractFloat64Exp( a );
    aSign = wfp_extractFloat64Sign( a );
    if ( aExp == 0x7FF ) {
        if ( aSig ) {
          wfp_float64ToCommonNaN( &z, a );
          return wfp_commonNaNToFloat32( z );
        }
        return wfp_packFloat32( aSign, 0xFF, 0 );
    }
    wfp_shift64RightJamming( aSig, 22, &aSig );
    zSig = aSig;
    if ( aExp || zSig ) {
        zSig |= 0x40000000;
        aExp -= 0x381;
    }
    return wfp_roundAndPackFloat32( aSign, aExp, zSig );

}

/*
-------------------------------------------------------------------------------
Returns the result of converting the single-precision floating-point value
`a' to the 64-bit two's complement integer format.  The conversion is
performed according to the IEC/IEEE Standard for Binary Floating-Point
Arithmetic, except that the conversion is always rounded toward zero.  If
`a' is a NaN, the largest positive integer is returned.  Otherwise, if the
conversion overflows, the largest integer with the same sign as `a' is
returned.
-------------------------------------------------------------------------------
*/

wfp_int64 wfp_float32_to_int64_round_to_zero( wfp_float32 a ) {

    wfp_flag aSign;
    wfp_int16 aExp, shiftCount;
    wfp_bits32 aSig;
    wfp_bits64 aSig64;
    wfp_int64 z;

    aSig = wfp_extractFloat32Frac( a );
    aExp = wfp_extractFloat32Exp( a );
    aSign = wfp_extractFloat32Sign( a );
    shiftCount = aExp - 0xBE;
    if ( 0 <= shiftCount ) {
        if ( a != 0xDF000000 ) {
            wfp_float_raise( wfp_float_flag_invalid );
            if ( ! aSign || ( ( aExp == 0xFF ) && aSig ) ) {
                return WFP_LIT64( 0x7FFFFFFFFFFFFFFF );
            }
        }
        return (wfp_sbits64) WFP_LIT64( 0x8000000000000000 );
    }
    else if ( aExp <= 0x7E ) {
        if ( aExp | aSig ) wfp_float_exception_flags |= wfp_float_flag_inexact;
        return 0;
    }
    aSig64 = aSig | 0x00800000;
    aSig64 <<= 40;
    z = aSig64>>( - shiftCount );
    if ( (wfp_bits64) ( aSig64<<( shiftCount & 63 ) ) ) {
        wfp_float_exception_flags |= wfp_float_flag_inexact;
    }
    if ( aSign ) z = - z;
    return z;

}

/*
-------------------------------------------------------------------------------
Returns the result of converting the single-precision floating-point value
`a' to the double-precision floating-point format.  The conversion is
performed according to the IEC/IEEE Standard for Binary Floating-Point
Arithmetic.
-------------------------------------------------------------------------------
*/

wfp_float64 wfp_float32_to_float64( wfp_float32 a ) {

    wfp_flag aSign;
    wfp_int16 aExp;
    wfp_bits32 aSig;
    wfp_commonNaNT z;

    aSig = wfp_extractFloat32Frac( a );
    aExp = wfp_extractFloat32Exp( a );
    aSign = wfp_extractFloat32Sign( a );
    if ( aExp == 0xFF ) {
        if ( aSig ) {
          wfp_float32ToCommonNaN( &z, a );
          return wfp_commonNaNToFloat64( z );
        }
        return wfp_packFloat64( aSign, 0x7FF, WFP_LIT64(0) );
    }
    if ( aExp == 0 ) {
        if ( aSig == 0 ) return wfp_packFloat64( aSign, 0, WFP_LIT64(0) );
        wfp_normalizeFloat32Subnormal( aSig, &aExp, &aSig );
        --aExp;
    }
    return wfp_packFloat64( aSign, aExp + 0x380, ( (wfp_bits64) aSig )<<29 );

}

/*
-------------------------------------------------------------------------------
Returns the result of adding the absolute values of the single-precision
floating-point values `a' and `b'.  If `zSign' is 1, the sum is negated
before being returned.  `zSign' is ignored if the result is a NaN.
The addition is performed according to the IEC/IEEE Standard for Binary
Floating-Point Arithmetic.
-------------------------------------------------------------------------------
*/

static wfp_float32 wfp_addFloat32Sigs( wfp_float32 a, wfp_float32 b, wfp_flag zSign ) {

    wfp_int16 aExp, bExp, zExp;
    wfp_bits32 aSig, bSig, zSig;
    wfp_int16 expDiff;

    aSig = wfp_extractFloat32Frac( a );
    aExp = wfp_extractFloat32Exp( a );
    bSig = wfp_extractFloat32Frac( b );
    bExp = wfp_extractFloat32Exp( b );
    expDiff = aExp - bExp;
    aSig <<= 6;
    bSig <<= 6;
    if ( 0 < expDiff ) {
        if ( aExp == 0xFF ) {
            if ( aSig ) return wfp_propagateFloat32NaN( a, b );
            return a;
        }
        if ( bExp == 0 ) {
            --expDiff;
        }
        else {
            bSig |= 0x20000000;
        }
        wfp_shift32RightJamming( bSig, expDiff, &bSig );
        zExp = aExp;
    }
    else if ( expDiff < 0 ) {
        if ( bExp == 0xFF ) {
            if ( bSig ) return wfp_propagateFloat32NaN( a, b );
            return wfp_packFloat32( zSign, 0xFF, 0 );
        }
        if ( aExp == 0 ) {
            ++expDiff;
        }
        else {
            aSig |= 0x20000000;
        }
        wfp_shift32RightJamming( aSig, - expDiff, &aSig );
        zExp = bExp;
    }
    else {
        if ( aExp == 0xFF ) {
            if ( aSig | bSig ) return wfp_propagateFloat32NaN( a, b );
            return a;
        }
        if ( aExp == 0 ) return wfp_packFloat32( zSign, 0, ( aSig + bSig )>>6 );
        zSig = 0x40000000 + aSig + bSig;
        zExp = aExp;
        goto roundAndPack;
    }
    aSig |= 0x20000000;
    zSig = ( aSig + bSig )<<1;
    --zExp;
    if ( (wfp_sbits32) zSig < 0 ) {
        zSig = aSig + bSig;
        ++zExp;
    }
 roundAndPack:
    return wfp_roundAndPackFloat32( zSign, zExp, zSig );

}

/*
-------------------------------------------------------------------------------
Returns the result of subtracting the absolute values of the single-
precision floating-point values `a' and `b'.  If `zSign' is 1, the
difference is negated before being returned.  `zSign' is ignored if the
result is a NaN.  The subtraction is performed according to the IEC/IEEE
Standard for Binary Floating-Point Arithmetic.
-------------------------------------------------------------------------------
*/

static wfp_float32 wfp_subFloat32Sigs( wfp_float32 a, wfp_float32 b, wfp_flag zSign ) {

    wfp_int16 aExp, bExp, zExp;
    wfp_bits32 aSig, bSig, zSig;
    wfp_int16 expDiff;

    aSig = wfp_extractFloat32Frac( a );
    aExp = wfp_extractFloat32Exp( a );
    bSig = wfp_extractFloat32Frac( b );
    bExp = wfp_extractFloat32Exp( b );
    expDiff = aExp - bExp;
    aSig <<= 7;
    bSig <<= 7;
    if ( 0 < expDiff ) goto aExpBigger;
    if ( expDiff < 0 ) goto bExpBigger;
    if ( aExp == 0xFF ) {
        if ( aSig | bSig ) return wfp_propagateFloat32NaN( a, b );
        wfp_float_raise( wfp_float_flag_invalid );
        return wfp_float32_default_nan;
    }
    if ( aExp == 0 ) {
        aExp = 1;
        bExp = 1;
    }
    if ( bSig < aSig ) goto aBigger;
    if ( aSig < bSig ) goto bBigger;
    return wfp_packFloat32( wfp_float_rounding_mode == wfp_float_round_down, 0, 0 );
 bExpBigger:
    if ( bExp == 0xFF ) {
        if ( bSig ) return wfp_propagateFloat32NaN( a, b );
        return wfp_packFloat32( zSign ^ 1, 0xFF, 0 );
    }
    if ( aExp == 0 ) {
        ++expDiff;
    }
    else {
        aSig |= 0x40000000;
    }
    wfp_shift32RightJamming( aSig, - expDiff, &aSig );
    bSig |= 0x40000000;
 bBigger:
    zSig = bSig - aSig;
    zExp = bExp;
    zSign ^= 1;
    goto normalizeRoundAndPack;
 aExpBigger:
    if ( aExp == 0xFF ) {
        if ( aSig ) return wfp_propagateFloat32NaN( a, b );
        return a;
    }
    if ( bExp == 0 ) {
        --expDiff;
    }
    else {
        bSig |= 0x40000000;
    }
    wfp_shift32RightJamming( bSig, expDiff, &bSig );
    aSig |= 0x40000000;
 aBigger:
    zSig = aSig - bSig;
    zExp = aExp;
 normalizeRoundAndPack:
    --zExp;
    return wfp_normalizeRoundAndPackFloat32( zSign, zExp, zSig );

}

/*
-------------------------------------------------------------------------------
Returns the result of adding the single-precision floating-point values `a'
and `b'.  The operation is performed according to the IEC/IEEE Standard for
Binary Floating-Point Arithmetic.
-------------------------------------------------------------------------------
*/

wfp_float32 wfp_float32_add( wfp_float32 a, wfp_float32 b ) {

    wfp_flag aSign, bSign;

    aSign = wfp_extractFloat32Sign( a );
    bSign = wfp_extractFloat32Sign( b );
    if ( aSign == bSign ) {
        return wfp_addFloat32Sigs( a, b, aSign );
    }
    else {
        return wfp_subFloat32Sigs( a, b, aSign );
    }

}

/*
-------------------------------------------------------------------------------
Returns the result of subtracting the single-precision floating-point values
`a' and `b'.  The operation is performed according to the IEC/IEEE Standard
for Binary Floating-Point Arithmetic.
-------------------------------------------------------------------------------
*/

wfp_float32 wfp_float32_sub( wfp_float32 a, wfp_float32 b ) {
    wfp_flag aSign, bSign;

    aSign = wfp_extractFloat32Sign( a );
    bSign = wfp_extractFloat32Sign( b );
    if ( aSign == bSign ) {
        return wfp_subFloat32Sigs( a, b, aSign );
    }
    else {
        return wfp_addFloat32Sigs( a, b, aSign );
    }

}

/*
-------------------------------------------------------------------------------
Returns the result of multiplying the single-precision floating-point values
`a' and `b'.  The operation is performed according to the IEC/IEEE Standard
for Binary Floating-Point Arithmetic.
-------------------------------------------------------------------------------
*/

wfp_float32 wfp_float32_mul( wfp_float32 a, wfp_float32 b ) {

    wfp_flag aSign, bSign, zSign;
    wfp_int16 aExp, bExp, zExp;
    wfp_bits32 aSig, bSig;
    wfp_bits64 zSig64;
    wfp_bits32 zSig;

    aSig = wfp_extractFloat32Frac( a );
    aExp = wfp_extractFloat32Exp( a );
    aSign = wfp_extractFloat32Sign( a );
    bSig = wfp_extractFloat32Frac( b );
    bExp = wfp_extractFloat32Exp( b );
    bSign = wfp_extractFloat32Sign( b );
    zSign = aSign ^ bSign;
    if ( aExp == 0xFF ) {
        if ( aSig || ( ( bExp == 0xFF ) && bSig ) ) {
            return wfp_propagateFloat32NaN( a, b );
        }
        if ( ( bExp | bSig ) == 0 ) {
            wfp_float_raise( wfp_float_flag_invalid );
            return wfp_float32_default_nan;
        }
        return wfp_packFloat32( zSign, 0xFF, 0 );
    }
    if ( bExp == 0xFF ) {
        if ( bSig ) return wfp_propagateFloat32NaN( a, b );
        if ( ( aExp | aSig ) == 0 ) {
            wfp_float_raise( wfp_float_flag_invalid );
            return wfp_float32_default_nan;
        }
        return wfp_packFloat32( zSign, 0xFF, 0 );
    }
    if ( aExp == 0 ) {
        if ( aSig == 0 ) return wfp_packFloat32( zSign, 0, 0 );
        wfp_normalizeFloat32Subnormal( aSig, &aExp, &aSig );
    }
    if ( bExp == 0 ) {
        if ( bSig == 0 ) return wfp_packFloat32( zSign, 0, 0 );
        wfp_normalizeFloat32Subnormal( bSig, &bExp, &bSig );
    }
    zExp = aExp + bExp - 0x7F;
    aSig = ( aSig | 0x00800000 )<<7;
    bSig = ( bSig | 0x00800000 )<<8;
    wfp_shift64RightJamming( ( (wfp_bits64) aSig ) * bSig, 32, &zSig64 );
    zSig = zSig64;
    if ( 0 <= (wfp_sbits32) ( zSig<<1 ) ) {
        zSig <<= 1;
        --zExp;
    }
    return wfp_roundAndPackFloat32( zSign, zExp, zSig );

}

/*
-------------------------------------------------------------------------------
Returns the result of dividing the single-precision floating-point value `a'
by the corresponding value `b'.  The operation is performed according to the
IEC/IEEE Standard for Binary Floating-Point Arithmetic.
-------------------------------------------------------------------------------
*/

wfp_float32 wfp_float32_div( wfp_float32 a, wfp_float32 b ) {

    wfp_flag aSign, bSign, zSign;
    wfp_int16 aExp, bExp, zExp;
    wfp_bits32 aSig, bSig, zSig;

    aSig = wfp_extractFloat32Frac( a );
    aExp = wfp_extractFloat32Exp( a );
    aSign = wfp_extractFloat32Sign( a );
    bSig = wfp_extractFloat32Frac( b );
    bExp = wfp_extractFloat32Exp( b );
    bSign = wfp_extractFloat32Sign( b );
    zSign = aSign ^ bSign;
    if ( aExp == 0xFF ) {
        if ( aSig ) return wfp_propagateFloat32NaN( a, b );
        if ( bExp == 0xFF ) {
            if ( bSig ) return wfp_propagateFloat32NaN( a, b );
            wfp_float_raise( wfp_float_flag_invalid );
            return wfp_float32_default_nan;
        }
        return wfp_packFloat32( zSign, 0xFF, 0 );
    }
    if ( bExp == 0xFF ) {
        if ( bSig ) return wfp_propagateFloat32NaN( a, b );
        return wfp_packFloat32( zSign, 0, 0 );
    }
    if ( bExp == 0 ) {
        if ( bSig == 0 ) {
            if ( ( aExp | aSig ) == 0 ) {
                wfp_float_raise( wfp_float_flag_invalid );
                return wfp_float32_default_nan;
            }
            wfp_float_raise( wfp_float_flag_divbyzero );
            return wfp_packFloat32( zSign, 0xFF, 0 );
        }
        wfp_normalizeFloat32Subnormal( bSig, &bExp, &bSig );
    }
    if ( aExp == 0 ) {
        if ( aSig == 0 ) return wfp_packFloat32( zSign, 0, 0 );
        wfp_normalizeFloat32Subnormal( aSig, &aExp, &aSig );
    }
    zExp = aExp - bExp + 0x7D;
    aSig = ( aSig | 0x00800000 )<<7;
    bSig = ( bSig | 0x00800000 )<<8;
    if ( bSig <= ( aSig + aSig ) ) {
        aSig >>= 1;
        ++zExp;
    }
    zSig = ( ( (wfp_bits64) aSig )<<32 ) / bSig;
    if ( ( zSig & 0x3F ) == 0 ) {
        zSig |= ( (wfp_bits64) bSig * zSig != ( (wfp_bits64) aSig )<<32 );
    }
    return wfp_roundAndPackFloat32( zSign, zExp, zSig );

}

/*
-------------------------------------------------------------------------------
Rounds the double-precision floating-point value `a' to an integer, and
returns the result as a double-precision floating-point value.  The
operation is performed according to the IEC/IEEE Standard for Binary
Floating-Point Arithmetic.
-------------------------------------------------------------------------------
*/

wfp_float64 wfp_float64_round_to_int( wfp_float64 a ) {

    wfp_flag aSign;
    wfp_int16 aExp;
    wfp_bits64 lastBitMask, roundBitsMask;
    wfp_int8 roundingMode;
    wfp_float64 z;

    aExp = wfp_extractFloat64Exp( a );
    if ( 0x433 <= aExp ) {
        if ( ( aExp == 0x7FF ) && wfp_extractFloat64Frac( a ) ) {
            return wfp_propagateFloat64NaN( a, a );
        }
        return a;
    }
    if ( aExp < 0x3FF ) {
        if ( (wfp_bits64) ( a<<1 ) == 0 ) return a;
        wfp_float_exception_flags |= wfp_float_flag_inexact;
        aSign = wfp_extractFloat64Sign( a );
        switch ( wfp_float_rounding_mode ) {
         case wfp_float_round_nearest_even:
            if ( ( aExp == 0x3FE ) && wfp_extractFloat64Frac( a ) ) {
                return wfp_packFloat64( aSign, 0x3FF, WFP_LIT64(0) );
            }
            break;
         case wfp_float_round_down:
            return aSign ? WFP_LIT64( 0xBFF0000000000000 ) : 0;
         case wfp_float_round_up:
            return
            aSign ? WFP_LIT64( 0x8000000000000000 ) : WFP_LIT64( 0x3FF0000000000000 );
        }
        return wfp_packFloat64( aSign, 0, WFP_LIT64(0) );
    }
    lastBitMask = 1;
    lastBitMask <<= 0x433 - aExp;
    roundBitsMask = lastBitMask - 1;
    z = a;
    roundingMode = wfp_float_rounding_mode;
    if ( roundingMode == wfp_float_round_nearest_even ) {
        z += lastBitMask>>1;
        if ( ( z & roundBitsMask ) == 0 ) z &= ~ lastBitMask;
    }
    else if ( roundingMode != wfp_float_round_to_zero ) {
        if ( wfp_extractFloat64Sign( z ) ^ ( roundingMode == wfp_float_round_up ) ) {
            z += roundBitsMask;
        }
    }
    z &= ~ roundBitsMask;
    if ( z != a ) wfp_float_exception_flags |= wfp_float_flag_inexact;
    return z;

}

/*
-------------------------------------------------------------------------------
Returns the result of adding the absolute values of the double-precision
floating-point values `a' and `b'.  If `zSign' is 1, the sum is negated
before being returned.  `zSign' is ignored if the result is a NaN.
The addition is performed according to the IEC/IEEE Standard for Binary
Floating-Point Arithmetic.
-------------------------------------------------------------------------------
*/

static wfp_float64 wfp_addFloat64Sigs( wfp_float64 a, wfp_float64 b, wfp_flag zSign ) {

    wfp_int16 aExp, bExp, zExp;
    wfp_bits64 aSig, bSig, zSig;
    wfp_int16 expDiff;

    aSig = wfp_extractFloat64Frac( a );
    aExp = wfp_extractFloat64Exp( a );
    bSig = wfp_extractFloat64Frac( b );
    bExp = wfp_extractFloat64Exp( b );
    expDiff = aExp - bExp;
    aSig <<= 9;
    bSig <<= 9;
    if ( 0 < expDiff ) {
        if ( aExp == 0x7FF ) {
            if ( aSig ) return wfp_propagateFloat64NaN( a, b );
            return a;
        }
        if ( bExp == 0 ) {
            --expDiff;
        }
        else {
            bSig |= WFP_LIT64( 0x2000000000000000 );
        }
        wfp_shift64RightJamming( bSig, expDiff, &bSig );
        zExp = aExp;
    }
    else if ( expDiff < 0 ) {
        if ( bExp == 0x7FF ) {
            if ( bSig ) return wfp_propagateFloat64NaN( a, b );
            return wfp_packFloat64( zSign, 0x7FF, WFP_LIT64(0) );
        }
        if ( aExp == 0 ) {
            ++expDiff;
        }
        else {
            aSig |= WFP_LIT64( 0x2000000000000000 );
        }
        wfp_shift64RightJamming( aSig, - expDiff, &aSig );
        zExp = bExp;
    }
    else {
        if ( aExp == 0x7FF ) {
            if ( aSig | bSig ) return wfp_propagateFloat64NaN( a, b );
            return a;
        }
        if ( aExp == 0 ) return wfp_packFloat64( zSign, 0, ( aSig + bSig )>>9 );
        zSig = WFP_LIT64( 0x4000000000000000 ) + aSig + bSig;
        zExp = aExp;
        goto roundAndPack;
    }
    aSig |= WFP_LIT64( 0x2000000000000000 );
    zSig = ( aSig + bSig )<<1;
    --zExp;
    if ( (wfp_sbits64) zSig < 0 ) {
        zSig = aSig + bSig;
        ++zExp;
    }
 roundAndPack:
    return wfp_roundAndPackFloat64( zSign, zExp, zSig );

}

/*
-------------------------------------------------------------------------------
Returns the result of subtracting the absolute values of the double-
precision floating-point values `a' and `b'.  If `zSign' is 1, the
difference is negated before being returned.  `zSign' is ignored if the
result is a NaN.  The subtraction is performed according to the IEC/IEEE
Standard for Binary Floating-Point Arithmetic.
-------------------------------------------------------------------------------
*/

static wfp_float64 wfp_subFloat64Sigs( wfp_float64 a, wfp_float64 b, wfp_flag zSign ) {

    wfp_int16 aExp, bExp, zExp;
    wfp_bits64 aSig, bSig, zSig;
    wfp_int16 expDiff;

    aSig = wfp_extractFloat64Frac( a );
    aExp = wfp_extractFloat64Exp( a );
    bSig = wfp_extractFloat64Frac( b );
    bExp = wfp_extractFloat64Exp( b );
    expDiff = aExp - bExp;
    aSig <<= 10;
    bSig <<= 10;
    if ( 0 < expDiff ) goto aExpBigger;
    if ( expDiff < 0 ) goto bExpBigger;
    if ( aExp == 0x7FF ) {
        if ( aSig | bSig ) return wfp_propagateFloat64NaN( a, b );
        wfp_float_raise( wfp_float_flag_invalid );
        return wfp_float64_default_nan;
    }
    if ( aExp == 0 ) {
        aExp = 1;
        bExp = 1;
    }
    if ( bSig < aSig ) goto aBigger;
    if ( aSig < bSig ) goto bBigger;
    return wfp_packFloat64( wfp_float_rounding_mode == wfp_float_round_down, 0, WFP_LIT64(0) );
 bExpBigger:
    if ( bExp == 0x7FF ) {
        if ( bSig ) return wfp_propagateFloat64NaN( a, b );
        return wfp_packFloat64( zSign ^ 1, 0x7FF, WFP_LIT64(0) );
    }
    if ( aExp == 0 ) {
        ++expDiff;
    }
    else {
        aSig |= WFP_LIT64( 0x4000000000000000 );
    }
    wfp_shift64RightJamming( aSig, - expDiff, &aSig );
    bSig |= WFP_LIT64( 0x4000000000000000 );
 bBigger:
    zSig = bSig - aSig;
    zExp = bExp;
    zSign ^= 1;
    goto normalizeRoundAndPack;
 aExpBigger:
    if ( aExp == 0x7FF ) {
        if ( aSig ) return wfp_propagateFloat64NaN( a, b );
        return a;
    }
    if ( bExp == 0 ) {
        --expDiff;
    }
    else {
        bSig |= WFP_LIT64( 0x4000000000000000 );
    }
    wfp_shift64RightJamming( bSig, expDiff, &bSig );
    aSig |= WFP_LIT64( 0x4000000000000000 );
 aBigger:
    zSig = aSig - bSig;
    zExp = aExp;
 normalizeRoundAndPack:
    --zExp;
    return wfp_normalizeRoundAndPackFloat64( zSign, zExp, zSig );

}

/*
-------------------------------------------------------------------------------
Returns the result of adding the double-precision floating-point values `a'
and `b'.  The operation is performed according to the IEC/IEEE Standard for
Binary Floating-Point Arithmetic.
-------------------------------------------------------------------------------
*/

wfp_float64 wfp_float64_add( wfp_float64 a, wfp_float64 b ) {

    wfp_flag aSign, bSign;

    aSign = wfp_extractFloat64Sign( a );
    bSign = wfp_extractFloat64Sign( b );
    if ( aSign == bSign ) {
        return wfp_addFloat64Sigs( a, b, aSign );
    }
    else {
        return wfp_subFloat64Sigs( a, b, aSign );
    }

}

/*
-------------------------------------------------------------------------------
Returns the result of subtracting the double-precision floating-point values
`a' and `b'.  The operation is performed according to the IEC/IEEE Standard
for Binary Floating-Point Arithmetic.
-------------------------------------------------------------------------------
*/

wfp_float64 wfp_float64_sub( wfp_float64 a, wfp_float64 b ) {

    wfp_flag aSign, bSign;

    aSign = wfp_extractFloat64Sign( a );
    bSign = wfp_extractFloat64Sign( b );
    if ( aSign == bSign ) {
        return wfp_subFloat64Sigs( a, b, aSign );
    }
    else {
        return wfp_addFloat64Sigs( a, b, aSign );
    }

}

/*
-------------------------------------------------------------------------------
Returns the result of multiplying the double-precision floating-point values
`a' and `b'.  The operation is performed according to the IEC/IEEE Standard
for Binary Floating-Point Arithmetic.
-------------------------------------------------------------------------------
*/

wfp_float64 wfp_float64_mul( wfp_float64 a, wfp_float64 b ) {

    wfp_flag aSign, bSign, zSign;
    wfp_int16 aExp, bExp, zExp;
    wfp_bits64 aSig, bSig, zSig0, zSig1;

    aSig = wfp_extractFloat64Frac( a );
    aExp = wfp_extractFloat64Exp( a );
    aSign = wfp_extractFloat64Sign( a );
    bSig = wfp_extractFloat64Frac( b );
    bExp = wfp_extractFloat64Exp( b );
    bSign = wfp_extractFloat64Sign( b );
    zSign = aSign ^ bSign;
    if ( aExp == 0x7FF ) {
        if ( aSig || ( ( bExp == 0x7FF ) && bSig ) ) {
            return wfp_propagateFloat64NaN( a, b );
        }
        if ( ( bExp | bSig ) == 0 ) {
            wfp_float_raise( wfp_float_flag_invalid );
            return wfp_float64_default_nan;
        }
        return wfp_packFloat64( zSign, 0x7FF, WFP_LIT64(0) );
    }
    if ( bExp == 0x7FF ) {
        if ( bSig ) return wfp_propagateFloat64NaN( a, b );
        if ( ( aExp | aSig ) == 0 ) {
            wfp_float_raise( wfp_float_flag_invalid );
            return wfp_float64_default_nan;
        }
        return wfp_packFloat64( zSign, 0x7FF, WFP_LIT64(0) );
    }
    if ( aExp == 0 ) {
        if ( aSig == 0 ) return wfp_packFloat64( zSign, 0, WFP_LIT64(0) );
        wfp_normalizeFloat64Subnormal( aSig, &aExp, &aSig );
    }
    if ( bExp == 0 ) {
        if ( bSig == 0 ) return wfp_packFloat64( zSign, 0, WFP_LIT64(0) );
        wfp_normalizeFloat64Subnormal( bSig, &bExp, &bSig );
    }
    zExp = aExp + bExp - 0x3FF;
    aSig = ( aSig | WFP_LIT64( 0x0010000000000000 ) )<<10;
    bSig = ( bSig | WFP_LIT64( 0x0010000000000000 ) )<<11;
    wfp_mul64To128( aSig, bSig, &zSig0, &zSig1 );
    zSig0 |= ( zSig1 != 0 );
    if ( 0 <= (wfp_sbits64) ( zSig0<<1 ) ) {
        zSig0 <<= 1;
        --zExp;
    }
    return wfp_roundAndPackFloat64( zSign, zExp, zSig0 );

}

/*
-------------------------------------------------------------------------------
Returns the result of dividing the double-precision floating-point value `a'
by the corresponding value `b'.  The operation is performed according to
the IEC/IEEE Standard for Binary Floating-Point Arithmetic.
-------------------------------------------------------------------------------
*/

wfp_float64 wfp_float64_div( wfp_float64 a, wfp_float64 b ) {

    wfp_flag aSign, bSign, zSign;
    wfp_int16 aExp, bExp, zExp;
    wfp_bits64 aSig, bSig, zSig;
    wfp_bits64 rem0, rem1;
    wfp_bits64 term0, term1;

    aSig = wfp_extractFloat64Frac( a );
    aExp = wfp_extractFloat64Exp( a );
    aSign = wfp_extractFloat64Sign( a );
    bSig = wfp_extractFloat64Frac( b );
    bExp = wfp_extractFloat64Exp( b );
    bSign = wfp_extractFloat64Sign( b );
    zSign = aSign ^ bSign;
    if ( aExp == 0x7FF ) {
        if ( aSig ) return wfp_propagateFloat64NaN( a, b );
        if ( bExp == 0x7FF ) {
            if ( bSig ) return wfp_propagateFloat64NaN( a, b );
            wfp_float_raise( wfp_float_flag_invalid );
            return wfp_float64_default_nan;
        }
        return wfp_packFloat64( zSign, 0x7FF, WFP_LIT64(0) );
    }
    if ( bExp == 0x7FF ) {
        if ( bSig ) return wfp_propagateFloat64NaN( a, b );
        return wfp_packFloat64( zSign, 0, WFP_LIT64(0) );
    }
    if ( bExp == 0 ) {
        if ( bSig == 0 ) {
            if ( ( aExp | aSig ) == 0 ) {
                wfp_float_raise( wfp_float_flag_invalid );
                return wfp_float64_default_nan;
            }
            wfp_float_raise( wfp_float_flag_divbyzero );
            return wfp_packFloat64( zSign, 0x7FF, WFP_LIT64(0) );
        }
        wfp_normalizeFloat64Subnormal( bSig, &bExp, &bSig );
    }
    if ( aExp == 0 ) {
        if ( aSig == 0 ) return wfp_packFloat64( zSign, 0, WFP_LIT64(0) );
        wfp_normalizeFloat64Subnormal( aSig, &aExp, &aSig );
    }
    zExp = aExp - bExp + 0x3FD;
    aSig = ( aSig | WFP_LIT64( 0x0010000000000000 ) )<<10;
    bSig = ( bSig | WFP_LIT64( 0x0010000000000000 ) )<<11;
    if ( bSig <= ( aSig + aSig ) ) {
        aSig >>= 1;
        ++zExp;
    }
    zSig = wfp_estimateDiv128To64( aSig, WFP_LIT64(0), bSig );
    if ( ( zSig & 0x1FF ) <= 2 ) {
        wfp_mul64To128( bSig, zSig, &term0, &term1 );
        wfp_sub128( aSig, WFP_LIT64(0), term0, term1, &rem0, &rem1 );
        while ( (wfp_sbits64) rem0 < 0 ) {
            --zSig;
            wfp_add128( rem0, rem1, WFP_LIT64(0), bSig, &rem0, &rem1 );
        }
        zSig |= ( rem1 != 0 );
    }
    return wfp_roundAndPackFloat64( zSign, zExp, zSig );

}

wfp_float32 wfp_float32_abs( wfp_float32 x ) {
  return wfp_extractFloat32Sign(x) ? wfp_float32_negate(x) : x;
}

wfp_float64 wfp_float64_abs( wfp_float64 x ) {
  return wfp_extractFloat64Sign(x) ? wfp_float64_negate(x) : x;
}

wfp_flag wfp_float32_is_NaN(wfp_float32 a) {
    return (((a >> 20) & 0x7fc) == 0x7fc);	
}

// there is a difference between the NaNs of javac and jikes
// jikes : fff8 0000 0000 0000
// javac : 7ff8 0000 0000 0000 follows the spec
wfp_flag wfp_float64_is_NaN(wfp_float64 a) {
	return ( (( a >> 48) &  0x7ff8) == 0x7ff8 ) ;
}

wfp_flag wfp_float32_is_Infinite(wfp_float32 a) {

	/*
  a = wfp_float32_is_negative(a) ? wfp_float32_negate(a) : a;
  
  return (a == 0x7f800000);
  */
	return (a & 0x7fffffff) == 0x7f800000;
  
}

wfp_flag wfp_float64_is_Infinite(wfp_float64 a) {

	/*
	wprintf("a = %08x%08x\n", a >> 32, a & WFP_LIT64(0x00000000ffffffff));
  a = wfp_float64_is_negative(a) ? wfp_float64_negate(a) : a;
	wprintf("a = %08x%08x\n", a >> 32, a & WFP_LIT64(0x00000000ffffffff));
  
  return (a == WFP_LIT64(0x7f80000000000000));
  */
  return (a == D_POSITIVE_INFINITY) || (a == D_NEGATIVE_INFINITY);
}


