/******************************************************************************
* Copyright (c) 2003, 2007 by Chris Gray, /k/ Embedded Java Solutions.        *
* Parts of this file are derived from John R. Hauser's SoftFloat package, see *
* the notice below.                                                           *
===============================================================================

This C source file is part of the SoftFloat IEC/IEEE Floating-point
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
******************************************************************************/

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
Was only called from one other place. It inlined there ...

static wfp_float64 wfp_commonNaNToFloat64( wfp_commonNaNT a ) {

    return
          ( ( (wfp_bits64) a.sign )<<63 )
        | WFP_LIT64( 0x7FF8000000000000 )
        | ( a.high>>12 );

}
*/
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

          return (wfp_float64)( (((wfp_bits64) z.sign)<<63) |
                  WFP_LIT64(0x7FF8000000000000) | (((wfp_bits64) z.high)>>12) ); 

          //return wfp_commonNaNToFloat64( z );
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
  return (a & 0x7fffffff) == 0x7f800000;
  
}

wfp_flag wfp_float64_is_Infinite(wfp_float64 a) {
  return (a == D_POSITIVE_INFINITY) || (a == D_NEGATIVE_INFINITY);
}

/*
-------------------------------------------------------------------------------
Returns an approximation to the square root of the 32-bit significand given
by `a'.  Considered as an integer, `a' must be at least 2^31.  If bit 0 of
`aExp' (the least significant bit) is 1, the integer returned approximates
2^31*sqrt(`a'/2^31), where `a' is considered an integer.  If bit 0 of `aExp'
is 0, the integer returned approximates 2^31*sqrt(`a'/2^30).  In either
case, the approximation returned lies strictly within +/-2 of the exact
value.
-------------------------------------------------------------------------------
*/

static IEEE_INLINE wfp_bits32 wfp_estimateSqrt32( wfp_int16 aExp, wfp_bits32 a ) {

    static const wfp_bits32 sqrtOddAdjustments[] = {
        0x00000004, 0x00000022, 0x0000005D, 0x000000B1,
        0x0000011D, 0x0000019F, 0x00000236, 0x000002E0,
        0x0000039C, 0x00000468, 0x00000545, 0x00000631,
        0x0000072B, 0x00000832, 0x00000946, 0x00000A67,
    };

    static const wfp_bits32 sqrtEvenAdjustments[] = {
        0x00000A2D, 0x000008AF, 0x0000075A, 0x00000629,
        0x0000051A, 0x00000429, 0x00000356, 0x0000029E,
        0x00000200, 0x00000179, 0x00000109, 0x000000AF,
        0x00000068, 0x00000034, 0x00000012, 0x00000002,
    };

    wfp_int8 indexx;
    wfp_bits32 z;

    indexx = ( a>>27 ) & 15;
    if ( aExp & 1 ) {
        z = 0x4000 + ( a>>17 ) - sqrtOddAdjustments[ (short)indexx ];
        z = ( ( a / z )<<14 ) + ( z<<15 );
        a >>= 1;
    }
    else {
        z = 0x8000 + ( a>>17 ) - sqrtEvenAdjustments[ (short)indexx ];
        z = a / z + z;
        z = ( 0x20000 <= z ) ? 0xFFFF8000 : ( z<<15 );
        if ( z <= a ) return (wfp_bits32) ( ( (wfp_sbits32) a )>>1 );
    }
    return ( (wfp_bits32) ( ( ( (wfp_bits64) a )<<31 ) / z ) ) + ( z>>1 );
 
}


/*
-------------------------------------------------------------------------------
Returns the square root of the double-precision floating-point value `a'.
The operation is performed according to the IEC/IEEE Standard for Binary
Floating-Point Arithmetic.
-------------------------------------------------------------------------------

wfp_float64 wfp_float64_sqrt( wfp_float64 a ) {

    wfp_flag aSign;
    wfp_int16 aExp, zExp;
    wfp_bits64 aSig, zSig, doubleZSig;
    wfp_bits64 rem0, rem1, term0, term1;

    aSig = wfp_extractFloat64Frac( a );
    aExp = wfp_extractFloat64Exp( a );
    aSign = wfp_extractFloat64Sign( a );
    if ( aExp == 0x7FF ) {
        if ( aSig ) return wfp_propagateFloat64NaN( a, a );
        if ( ! aSign ) return a;
        wfp_float_raise( wfp_float_flag_invalid );
        return wfp_float64_default_nan;
    }
    if ( aSign ) {
        if ( ( aExp | aSig ) == 0 ) return a;
        wfp_float_raise( wfp_float_flag_invalid );
        return wfp_float64_default_nan;
    }
    if ( aExp == 0 ) {
        if ( aSig == 0 ) return 0;
        wfp_normalizeFloat64Subnormal( aSig, &aExp, &aSig );
    }
    zExp = ( ( aExp - 0x3FF )>>1 ) + 0x3FE;
    aSig |= WFP_LIT64( 0x0010000000000000 );
    zSig = wfp_estimateSqrt32( aExp, (wfp_bits32)aSig>>21 );
    aSig <<= 9 - ( aExp & 1 );
    zSig = wfp_estimateDiv128To64( aSig, WFP_LIT64(0), zSig<<32 ) + ( zSig<<30 );
    if ( ( zSig & 0x1FF ) <= 5 ) {
        doubleZSig = zSig<<1;
        wfp_mul64To128( zSig, zSig, &term0, &term1 );
        wfp_sub128( aSig, WFP_LIT64(0), term0, term1, &rem0, &rem1 );
        while ( (wfp_sbits64) rem0 < 0 ) {
            --zSig;
            doubleZSig -= 2;
            wfp_add128( rem0, rem1, zSig>>63, doubleZSig | 1, &rem0, &rem1 );
        }
        zSig |= ( ( rem0 | rem1 ) != 0 );
    }
    return wfp_roundAndPackFloat64( 0, zExp, zSig );

}
*/

#ifdef NATIVE_MATH
/****************************************************************************/
/* Remaining code by Chris Gray. Do not blame John Hauser for this rubbish! */
/****************************************************************************/

wfp_float64 wfp_float64_sqrt(wfp_float64 arg) {
  wfp_int16 exp;
  wfp_bits64 sig;
  wfp_float64 prev;
  wfp_float64 root;

  if (wfp_float64_is_negative(arg) || wfp_float64_is_NaN(arg)) {

    return D_NAN;

  }

  if (wfp_float64_is_Infinite(arg)) {

    return arg;

  }

  if (wfp_float64_lt(arg, D_TINY)) {

    return wfp_float64_mul(arg, D_ZERO_POINT_FIVE);

  }
 
  exp = wfp_extractFloat64Exp(arg) - 0x3ffLL;
  sig = wfp_extractFloat64Frac(arg);

  exp = (exp >> 1) & 0xfffLL;

  prev = wfp_packFloat64(0, exp + 0x3ffLL, sig);
  root = wfp_float64_mul(wfp_float64_add(wfp_float64_div(arg, prev), prev), D_ZERO_POINT_FIVE);
  if (prev != root) {
    prev = root;
    root = wfp_float64_mul(wfp_float64_add(wfp_float64_div(arg, prev), prev), D_ZERO_POINT_FIVE);
  }
  if (prev != root) {
    prev = root;
    root = wfp_float64_mul(wfp_float64_add(wfp_float64_div(arg, prev), prev), D_ZERO_POINT_FIVE);
  }
  if (prev != root) {
    prev = root;
    root = wfp_float64_mul(wfp_float64_add(wfp_float64_div(arg, prev), prev), D_ZERO_POINT_FIVE);
  }
  if (prev != root) {
    prev = root;
    root = wfp_float64_mul(wfp_float64_add(wfp_float64_div(arg, prev), prev), D_ZERO_POINT_FIVE);
  }
  if (prev != root) {
    prev = root;
    root = wfp_float64_mul(wfp_float64_add(wfp_float64_div(arg, prev), prev), D_ZERO_POINT_FIVE);
  }
  if (prev != root) {
    prev = root;
    root = wfp_float64_mul(wfp_float64_add(wfp_float64_div(arg, prev), prev), D_ZERO_POINT_FIVE);
  }

  return root;
}

#define D_ONE_OVER_6   0x3fc5555555555555LL    // 1/6
#define D_ONE_OVER_20  0x3fa999999999999aLL    // 1/20
#define D_ONE_OVER_42  0x3f98618618618618LL    // 1/42
#define D_ONE_OVER_72  0x3f8c71c71c71c71cLL    // 1/72
#define D_ONE_OVER_110 0x3f829e4129e4129eLL    // 1/110
#define D_ONE_OVER_156 0x3f7a41a41a41a41aLL    // 1/156

/**
 ** Evaluate sin() using a truncated Taylor series.
 ** Only use this in the range -quarterpi..quarterpi !
 */
static wfp_float64 wfp_float64_sine(wfp_float64 theta) {
  wfp_float64 theta_squared;
  wfp_float64 temp;
  wfp_float64 t1;

  theta_squared = wfp_float64_mul(theta, theta);
  temp = theta_squared;
  t1 = wfp_float64_mul(temp, D_ONE_OVER_156);
  t1 = wfp_float64_mul(theta_squared, t1);
  temp = wfp_float64_sub(theta_squared, t1);
  t1 = wfp_float64_mul(temp, D_ONE_OVER_110);
  t1 = wfp_float64_mul(theta_squared, t1);
  temp = wfp_float64_sub(theta_squared, t1);
  t1 = wfp_float64_mul(temp, D_ONE_OVER_72);
  t1 = wfp_float64_mul(theta_squared, t1);
  temp = wfp_float64_sub(theta_squared, t1);
  t1 = wfp_float64_mul(temp, D_ONE_OVER_42);
  t1 = wfp_float64_mul(theta_squared, t1);
  temp = wfp_float64_sub(theta_squared, t1);
  t1 = wfp_float64_mul(temp, D_ONE_OVER_20);
  t1 = wfp_float64_mul(theta_squared, t1);
  temp = wfp_float64_sub(theta_squared, t1);
  t1 = wfp_float64_mul(temp, D_ONE_OVER_6);
  t1 = wfp_float64_mul(theta, t1);
  temp = wfp_float64_sub(theta, t1);

  return temp;

}

/*
** Reduce arg to the range -pi..pi, and classify into an octant (-4..4).
*/
static wfp_float64 wfp_float64_reduce_angle(wfp_float64 arg) {
  wfp_float64 cycles;
  wfp_float64 roundedcycles;
  wfp_float64 theta;
  wfp_float64 adjust;

  cycles = wfp_float64_div(arg, D_TWO_PI);

  if (wfp_float64_lt(D_LONG_MAX_VALUE, cycles) || wfp_float64_lt(D_LONG_MAX_VALUE, wfp_float64_negate(cycles))) {

    return D_ZERO;

  }

  roundedcycles = wfp_int64_to_float64(wfp_float64_to_int64_round_to_zero(cycles));

  if (wfp_float64_lt(cycles, D_ZERO)) {
    theta = wfp_float64_add(arg, wfp_float64_mul(roundedcycles, D_TWO_PI));
  }
  else if (wfp_float64_lt(D_ZERO, cycles)) {
    theta = wfp_float64_sub(arg, wfp_float64_mul(roundedcycles, D_TWO_PI));
  }

  return theta;
}

/**
 ** Now for the real sin() function.
 **
 ** For small x, sin(x) = x (this also takes care of -0.0 and 0.0).
 ** Otherwise we reduce the argument to the range -pi..pi, and then
 ** subdivide this range into eight octants (half-quadrants).
 ** Within each quadrant, we use either our truncated Taylor series
 ** directly or sqrt(1-y*y), where y is the complementary angle to x
 ** (opposite corner of a right-angled triangle).
 **
 ** N.B. We assume that 'arg' is finite and not a NaN.
 */
wfp_float64 wfp_float64_sin(wfp_float64 arg) {
  wfp_float64 theta;
  wfp_float64 temp;
  wfp_int32 phase;

  if (wfp_float64_lt(wfp_float64_abs(arg), D_TINY)) {

    return arg;

  }

  theta = wfp_float64_reduce_angle(arg);
  phase = wfp_float64_to_int32_round_to_zero(wfp_float64_div(theta, D_QUARTER_PI));

  switch(phase) {
  case 0:
  case -1:

    return wfp_float64_sine(theta);

  case 1:
    temp = wfp_float64_sine(wfp_float64_sub(D_HALF_PI, theta));
    temp = wfp_float64_sub(D_ONE, wfp_float64_mul(temp, temp));

    return wfp_float64_sqrt(temp);

  case -3:
  case -2:
    temp = wfp_float64_sine(wfp_float64_negate(wfp_float64_add(D_HALF_PI, theta)));
    temp = wfp_float64_sub(D_ONE, wfp_float64_mul(temp, temp));

    return wfp_float64_negate(wfp_float64_sqrt(temp));

  case 2:
    temp = wfp_float64_sine(wfp_float64_sub(theta, D_HALF_PI));
    temp = wfp_float64_sub(D_ONE, wfp_float64_mul(temp, temp));

    return wfp_float64_sqrt(temp);

  default: // 3

    return wfp_float64_sine(wfp_float64_sub(D_PI, theta));
  }
}

/**
 ** The cos() function works the same way as sin(), mutatis mutandis.
 */
wfp_float64 wfp_float64_cos(wfp_float64 arg) {
  wfp_float64 theta;
  wfp_float64 temp;
  wfp_int32 phase;

  if (wfp_float64_lt(wfp_float64_abs(arg), D_TINY)) {

    return D_ONE;

  }

  theta = wfp_float64_reduce_angle(arg);
  phase = wfp_float64_to_int32_round_to_zero(wfp_float64_div(theta, D_QUARTER_PI));

  switch(phase) {
  case 0:
  case -1:
    temp = wfp_float64_sine(theta);
    temp = wfp_float64_sub(D_ONE, wfp_float64_mul(temp, temp));

    return wfp_float64_sqrt(temp);

  case 1:
  case 2:

    return wfp_float64_sine(wfp_float64_sub(D_HALF_PI, theta));

  case -2:
  case -3:

    return wfp_float64_sine(wfp_float64_add(D_HALF_PI, theta));

  default:
    temp = wfp_float64_sine(wfp_float64_sub(D_PI, theta));
    temp = wfp_float64_sub(D_ONE, wfp_float64_mul(temp, temp));

    return wfp_float64_negate(wfp_float64_sqrt(temp));
  }
}

/**
 ** Let's just act dumb for this one.
 */
wfp_float64 wfp_float64_tan(wfp_float64 arg) {
  return wfp_float64_div(wfp_float64_sin(arg), wfp_float64_cos(arg));
}

#define D_MINUS_ZERO_POINT_69674573447350646411  0xbfe64bbdb5e61e65LL // -0.69674573447350646411e+0
#define D_10_POINT_152522233806463645            0x40244e1764ec3927LL //  0.10152522233806463645e+2
#define D_MINUS_39_POINT_688862997504877339      0xc043d82ca9a6da9fLL // -0.39688862997504877339e+2
#define D_57_POINT_208227877891731407            0x404c9aa7360ad48aLL //  0.57208227877891731407e+2
#define D_MINUS_27_POINT_368494524164255994      0xc03b5e55a83a0a62LL // -0.27368494524164255994e+2

#define D_MINUS_23_POINT_823859153670238830      0xc037d2e86ef9861fLL // -0.23823859153670238830e+2
#define D_150_POINT_95270841030604719            0x4062de7c96591c70LL //  0.15095270841030604719e+3
#define D_MINUS_381_POINT_86303361750149284      0xc077ddcefc56a848LL // -0.38186303361750149284e+3
#define D_417_POINT_14430248260412556            0x407a124f101eb843LL //  0.41714430248260412556e+3
#define D_MINUS_164_POINT_21096714498560795      0xc06486c03e2b87ccLL // -0.16421096714498560795e+3

/**
 ** Implementation of asin uses a rational polynomial.
 ** This and asin() itself are based on Whitaker and Eicholz's work.
 */
static wfp_float64 wfp_float64_arcrat(wfp_float64 x) {
  wfp_float64 numerator;
  wfp_float64 denominator;

  numerator = D_MINUS_ZERO_POINT_69674573447350646411;
  numerator = wfp_float64_mul(numerator, x);
  numerator = wfp_float64_add(numerator, D_10_POINT_152522233806463645);
  numerator = wfp_float64_mul(numerator, x);
  numerator = wfp_float64_add(numerator, D_MINUS_39_POINT_688862997504877339);
  numerator = wfp_float64_mul(numerator, x);
  numerator = wfp_float64_add(numerator, D_57_POINT_208227877891731407);
  numerator = wfp_float64_mul(numerator, x);
  numerator = wfp_float64_add(numerator, D_MINUS_27_POINT_368494524164255994);
  numerator = wfp_float64_mul(numerator, x);

  denominator = x;
  denominator = wfp_float64_add(denominator, D_MINUS_23_POINT_823859153670238830);
  denominator = wfp_float64_mul(denominator, x);
  denominator = wfp_float64_add(denominator, D_150_POINT_95270841030604719);
  denominator = wfp_float64_mul(denominator, x);
  denominator = wfp_float64_add(denominator, D_MINUS_381_POINT_86303361750149284);
  denominator = wfp_float64_mul(denominator, x);
  denominator = wfp_float64_add(denominator, D_417_POINT_14430248260412556);
  denominator = wfp_float64_mul(denominator, x);
  denominator = wfp_float64_add(denominator, D_MINUS_164_POINT_21096714498560795);

  return wfp_float64_div(numerator, denominator);
}

  /**
   ** The asin() method proper finds a way to apply arcrat() to a value in [0.0,0.5]. 
 **
 ** N.B. We assume that 'x' is finite and not a NaN.
   */
wfp_float64 wfp_float64_asin(wfp_float64 x) {
  wfp_float64 absval;
  wfp_float64 temp;

  if (wfp_float64_lt(x, D_MINUS_ONE)) {

    return D_NAN;

  }

  if (wfp_float64_eq(x, D_MINUS_ONE)) {

    return D_MINUS_HALF_PI;

  }

  if (wfp_float64_lt(x, D_MINUS_ZERO_POINT_FIVE)) {
    // return 2.0 * asin(sqrt((1.0 + x) * 0.5)) - halfpi;
    temp = wfp_float64_add(x, D_ONE);
    temp = wfp_float64_mul(temp, D_ZERO_POINT_FIVE);
    temp = wfp_float64_sqrt(temp);
    temp = wfp_float64_asin(temp);
    temp = wfp_float64_mul(temp, D_TWO);
    temp = wfp_float64_sub(temp, D_HALF_PI);

    return temp;
  }

  if (wfp_float64_lt(x, D_MINUS_TINY)) {
    // return -(-x + (-x * arcrat(-x * -x)));
    absval = wfp_float64_negate(x);
    temp = wfp_float64_mul(absval, absval);
    temp = wfp_float64_arcrat(temp);
    temp = wfp_float64_mul(temp, absval);
    temp = wfp_float64_add(temp, absval);

    return wfp_float64_negate(temp);
  }

  if (wfp_float64_lt(x, D_TINY)) {

    return x;

  }

  if (wfp_float64_lt(x, D_ZERO_POINT_FIVE) || x == D_ZERO_POINT_FIVE) {
    // return x + (x * arcrat(x * x));
    temp = wfp_float64_mul(x, x);
    temp = wfp_float64_arcrat(temp);
    temp = wfp_float64_mul(temp, x);
    temp = wfp_float64_add(temp, x);

    return temp;
  }

  if (wfp_float64_lt(x, D_ONE)) {
    // return halfpi - 2.0 * asin(sqrt((1.0 - x) * 0.5));
    temp = wfp_float64_sub(D_ONE, x);
    temp = wfp_float64_mul(temp, D_ZERO_POINT_FIVE);
    temp = wfp_float64_sqrt(temp);
    temp = wfp_float64_asin(temp);
    temp = wfp_float64_mul(temp, D_TWO);
    temp = wfp_float64_sub(D_HALF_PI, temp);

    return temp;
  }

  if (wfp_float64_eq(x, D_ONE)) {

    return D_HALF_PI;

  }

  // if we get here, x > 1.0
  return D_NAN;
}

#define D_MINUS_ZERO_POINT_83758299368150059274 0xbfeacd7ad9b187bdLL // -0.83758299368150059274e+0
#define D_MINUS_8_POINT_4946240351320683534     0xc020fd3f5c8d6a63LL // -0.84946240351320683534e+1
#define D_MINUS_20_POINT_505855195861651981     0xc034817fb9e2bccbLL // -0.20505855195861651981e+2
#define D_MINUS_13_POINT_688768894191926929     0xc02b60a651061ce2LL // -0.13688768894191926929e+2

#define D_15_POINT_024001160028576121           0x402e0c49e14ac710LL // 0.15024001160028576121e+2
#define D_59_POINT_578436142597344465           0x404dca0a320da3d7LL // 0.59578436142597344465e+2
#define D_86_POINT_157349597130242515           0x40558a12040b6da5LL // 0.86157349597130242515e+2
#define D_41_POINT_066306682575781263           0x4044887cbcc495a9LL // 0.41066306682575781263e+2

  /**
   ** Implementation of atan uses a rational polynomial.
   ** This and arctan() itself are based on Whitaker and Eicholz's work.
   */
static wfp_float64 wfp_float64_tancrat(wfp_float64 x) {
  wfp_float64 numerator;
  wfp_float64 denominator;

  numerator = wfp_float64_mul(x, D_MINUS_ZERO_POINT_83758299368150059274);
  numerator = wfp_float64_add(numerator, D_MINUS_8_POINT_4946240351320683534);
  numerator = wfp_float64_mul(numerator, x);
  numerator = wfp_float64_add(numerator, D_MINUS_20_POINT_505855195861651981);
  numerator = wfp_float64_mul(numerator, x);
  numerator = wfp_float64_add(numerator, D_MINUS_13_POINT_688768894191926929);
  numerator = wfp_float64_mul(numerator, x);

  denominator = x;
  denominator = wfp_float64_add(denominator, D_15_POINT_024001160028576121);
  denominator = wfp_float64_mul(denominator, x);
  denominator = wfp_float64_add(denominator, D_59_POINT_578436142597344465);
  denominator = wfp_float64_mul(denominator, x);
  denominator = wfp_float64_add(denominator, D_86_POINT_157349597130242515);
  denominator = wfp_float64_mul(denominator, x);
  denominator = wfp_float64_add(denominator, D_41_POINT_066306682575781263);

  return wfp_float64_div(numerator, denominator);
}

/**
 ** Similarly to asin(), atan() first narrows the range (to [0.0,1.0])
 ** and then invokes tancrat().
 **
 ** N.B. We assume that 'x' is finite and not a NaN.
 */
wfp_float64 wfp_float64_atan(wfp_float64 x) {
  wfp_float64 absval;
  wfp_float64 temp;

  if (wfp_float64_lt(x, D_MINUS_ONE)) {
    // return -x + -x * tancrat(-x * -x) - halfpi;
    absval = wfp_float64_negate(x);
    temp = wfp_float64_mul(absval, absval);
    temp = wfp_float64_tancrat(temp);
    temp = wfp_float64_mul(temp, absval);
    temp = wfp_float64_add(temp, absval);
    temp = wfp_float64_sub(temp, D_HALF_PI);

    return temp;
  }

  if (wfp_float64_lt(x, D_MINUS_TINY)) {
    // return -(-x + -x * tancrat(-x * -x));
    absval = wfp_float64_negate(x);
    temp = wfp_float64_mul(absval, absval);
    temp = wfp_float64_tancrat(temp);
    temp = wfp_float64_mul(temp, absval);
    temp = wfp_float64_add(temp, absval);

    return wfp_float64_negate(temp);
  }

  if (wfp_float64_lt(x, D_TINY)) {

    return x;

  }

  if (wfp_float64_lt(x, D_ONE)) {
    // return x + x * tancrat(x * x));
    temp = wfp_float64_mul(x, x);
    temp = wfp_float64_tancrat(temp);
    temp = wfp_float64_mul(temp, x);
    temp = wfp_float64_add(temp, x);

    return temp;
  }

  // if we get here, x > 1.0
  // return halfpi - (x + x * tancrat(x * x));
  temp = wfp_float64_mul(x, x);
  temp = wfp_float64_tancrat(temp);
  temp = wfp_float64_mul(temp, x);
  temp = wfp_float64_add(temp, x);
  temp = wfp_float64_sub(D_HALF_PI, temp);

  return temp;
}

#define D_OFFSETBITS 0x3ff0000000000000LL
#define D_MANTBITS   0x000fffffffffffffLL

/**
 ** Our log is based on a simple Taylor series -- couldn't get
 ** the rational polynomial jazz to work on this one.
 **
 ** N.B. We assume that 'arg' is finite and not a NaN.
 */
wfp_float64 wfp_float64_log(wfp_float64 arg) {
  wfp_int32 exponent;
  wfp_float64 fraction;
  wfp_float64 temp1;
  wfp_float64 temp2;
  wfp_float64 square;

  if (wfp_float64_lt(arg, D_MINUS_ZERO)) {

    return D_NAN;

  }

  if (wfp_float64_lt(arg, D_DOUBLE_MIN_VALUE)) {

    return D_NEGATIVE_INFINITY;

  }

  /*
  ** Extract the exponent and fractional part.
  */
  exponent = (arg - D_OFFSETBITS) >> 52;
  fraction = (arg & D_MANTBITS) | D_OFFSETBITS;

  /*
  ** Now we use the Taylor series for ln((x+1)/(x-1)):
  ** <pre>
  **     / x+1 \     /  1      1       1         \
  **  ln |-----| = 2*| --- + ----- + ----- + ... |    x >= 1
  **     \ x-1 /     \  x    3*x^3   5*x^5       /
  ** </pre>
  */


  if (wfp_float64_eq(fraction, D_ONE)) {
    temp2 = D_ZERO;
  }
  else {
    temp1 = wfp_float64_add(fraction, D_ONE);
    temp2 = wfp_float64_sub(fraction, D_ONE);
    temp1 = wfp_float64_div(temp1, temp2);
    square = wfp_float64_mul(temp1, temp1);
    square = wfp_float64_div(D_ONE, square);
    temp2 = D_ONE_OVER_21;
    temp2 = wfp_float64_mul(temp2, square);
    temp2 = wfp_float64_add(temp2, D_ONE_OVER_19);
    temp2 = wfp_float64_mul(temp2, square);
    temp2 = wfp_float64_add(temp2, D_ONE_OVER_17);
    temp2 = wfp_float64_mul(temp2, square);
    temp2 = wfp_float64_add(temp2, D_ONE_OVER_15);
    temp2 = wfp_float64_mul(temp2, square);
    temp2 = wfp_float64_add(temp2, D_ONE_OVER_13);
    temp2 = wfp_float64_mul(temp2, square);
    temp2 = wfp_float64_add(temp2, D_ONE_OVER_11);
    temp2 = wfp_float64_mul(temp2, square);
    temp2 = wfp_float64_add(temp2, D_ONE_OVER_9);
    temp2 = wfp_float64_mul(temp2, square);
    temp2 = wfp_float64_add(temp2, D_ONE_OVER_7);
    temp2 = wfp_float64_mul(temp2, square);
    temp2 = wfp_float64_add(temp2, D_ONE_OVER_5);
    temp2 = wfp_float64_mul(temp2, square);
    temp2 = wfp_float64_add(temp2, D_ONE_OVER_3);
    temp2 = wfp_float64_mul(temp2, square);
    temp2 = wfp_float64_add(temp2, D_ONE);
    temp2 = wfp_float64_mul(temp2, D_TWO);
    temp2 = wfp_float64_div(temp2, temp1);
  }

  /*
  ** Finally, add in ln(2) times the exponent.
  */ 
  temp1 = wfp_float64_mul(wfp_int32_to_float64(exponent), D_LN_2);

  return wfp_float64_add(temp1, temp2);
}

/**
 ** For the exp() method we also use a schoolbook Taylor series.
 **
 ** N.B. We assume that 'arg' is finite and not a NaN.
 */
wfp_float64 wfp_float64_exp(wfp_float64 arg) {
  wfp_int64 exponent;
  wfp_float64 difference;
  wfp_float64 temp1;
  wfp_float64 temp2;

  if (wfp_float64_lt(D_LOG_BIG_NUM, arg)) {

    return D_POSITIVE_INFINITY;

  }

  if (wfp_float64_lt(D_MINUS_TINY, arg) && wfp_float64_lt(arg, D_TINY)) {

    return D_ONE;

  }

  temp1 = wfp_float64_div(arg, D_LN_2);
  temp2 = wfp_float64_lt(arg, D_MINUS_ZERO) ? D_ZERO_POINT_FIVE : D_MINUS_ZERO_POINT_FIVE;
  temp1 = wfp_float64_add(temp1, temp2); // arg / log(2) + (arg < 0 ? -0.5 : 0.5);
  exponent = wfp_float64_to_int64_round_to_zero(temp1);

  temp1 = wfp_float64_mul(temp1, D_LN_2);
  difference = wfp_float64_sub(arg, wfp_int64_to_float64(exponent)); // arg - (exponent * log(2))

printf("exponent = %lld, x = %g, difference = %g\n", exponent, temp1, difference);
  temp1 = wfp_float64_div(difference, D_TWELVE);
  temp2 = wfp_float64_add(temp1, D_ONE);
  temp1 = wfp_float64_div(difference, D_ELEVEN);
  temp1 = wfp_float64_mul(temp1, temp2);
  temp2 = wfp_float64_add(temp1, D_ONE);
  temp1 = wfp_float64_div(difference, D_TEN);
  temp1 = wfp_float64_mul(temp1, temp2);
  temp2 = wfp_float64_add(temp1, D_ONE);
  temp1 = wfp_float64_div(difference, D_NINE);
  temp1 = wfp_float64_mul(temp1, temp2);
  temp2 = wfp_float64_add(temp1, D_ONE);
  temp1 = wfp_float64_div(difference, D_EIGHT);
  temp1 = wfp_float64_mul(temp1, temp2);
  temp2 = wfp_float64_add(temp1, D_ONE);
  temp1 = wfp_float64_div(difference, D_SEVEN);
  temp1 = wfp_float64_mul(temp1, temp2);
  temp2 = wfp_float64_add(temp1, D_ONE);
  temp1 = wfp_float64_div(difference, D_SIX);
  temp1 = wfp_float64_mul(temp1, temp2);
  temp2 = wfp_float64_add(temp1, D_ONE);
  temp1 = wfp_float64_div(difference, D_FIVE);
  temp1 = wfp_float64_mul(temp1, temp2);
  temp2 = wfp_float64_add(temp1, D_ONE);
  temp1 = wfp_float64_div(difference, D_FOUR);
  temp1 = wfp_float64_mul(temp1, temp2);
  temp2 = wfp_float64_add(temp1, D_ONE);
  temp1 = wfp_float64_div(difference, D_THREE);
  temp1 = wfp_float64_mul(temp1, temp2);
  temp2 = wfp_float64_add(temp1, D_ONE);
  temp1 = wfp_float64_div(difference, D_TWO);
  temp1 = wfp_float64_mul(temp1, temp2);
  temp2 = wfp_float64_add(temp1, D_ONE);
  temp1 = wfp_float64_mul(difference, temp2);
  temp2 = wfp_float64_add(temp1, D_ONE);

  temp1 = (exponent  + 0x3ff) << 52;

  return wfp_float64_mul(temp1, temp2);
}

#endif

