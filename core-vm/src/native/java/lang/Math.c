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
*                                                                         *
* Modifications copyright (C) 2007 by Chris Gray, /k/ Embedded Java       *
* Solutions. Permission is hereby granted to distribute these             *
* modifications under the terms of the Wonka Public Licence.              *
*                                                                         *
**************************************************************************/

#include <ctype.h>
#include <stdio.h>
#include <string.h>

#include "chars.h"
#include "ts-mem.h"
#include "wmath.h"
#include "wstrings.h"
#include "core-classes.h"
#include "Math.h"
#include "exception.h"

/* [CG 20070331] None of this seems to be used just now ...

typedef const union {
  unsigned short W[4];
  w_double value;
} math_constant;

#if __BYTE_ORDER == __LITTLE_ENDIAN
#define INIT_MATH_CONSTANT(c)              0, 0, 0, c
#else
#define INIT_MATH_CONSTANT(c)              c, 0, 0, 0
#endif

**
**  Representation of a IEEE754 64 bit double value.
**
**        SHORT_3             SHORT_2            SHORT_1            SHORT_0
**  +------------------+------------------+------------------+------------------+
**  | SEEEEEEEEEEEFFFF | FFFFFFFFFFFFFFFF | FFFFFFFFFFFFFFFF | FFFFFFFFFFFFFFFF |
**  +------------------+------------------+------------------+------------------+ 
**    S|-- exp ---|------------------- fraction ------------------------------|
**    I
**    G
**    N
**
**  A double d = -1^S * (1.FFF FFFFFFFFFFFFFFFF FFFFFFFFFFFFFFFF FFFFFFFFFFFFFFFF) * 2^(EEEEEEEEEEE)
**  
**  and EEEEEEEEEEE = SHORT_3 - VALUE_OF_FFFF_IN_SHORT_3
**

#define FRACTION_BITS_IN_SHORT_3                          4  /. _DOFF ./
#define NBITS                                            (48 + FRACTION_BITS_IN_SHORT_3)
#define _DFRAC                                           ((1 << FRACTION_BITS_IN_SHORT_3)-1)
#define _DMASK                                           (0x7fff&~_DFRAC)
#define _DMAX                                            ((1 << (15 - FRACTION_BITS_IN_SHORT_3)) - 1)
#define _DNAN                                            (0x8000 | _DMAX << FRACTION_BITS_IN_SHORT_3 | 1 << (FRACTION_BITS_IN_SHORT_3 - 1))
#define VALUE_OF_FFFF_IN_SHORT_3                         0x03fe /. _DBIAS ./
#define SIGN_BIT_IN_SHORT_3                              0x8000
#define DIGITS_TO_CONVERT_EACH_PASS                      8
#define MAX_SIGNIFICANT_DIGITS                           32
#define MAX_DIGITS_DISPLAYED_DOUBLE			 16
#define MAX_DIGITS_DISPLAYED_FLOAT			 8 //23 bits in fraction plus hidden 1 --> 24 bits = 16000000
#define DSIGN(x)                                         (((unsigned short *)&(x))[SHORT_3] & SIGN_BIT_IN_SHORT_3)
*/

#define HUGE_EXP                                         (int)(_DMAX * 900L / 1000)
#define SAFE_EXP                                         (_DMAX >> 1)
#define HUGE_RAD                                         (0x4643d0f18fcc1cb2LL)    /* 3.14e30                  */

// static math_constant   HugeVal = {{ INIT_MATH_CONSTANT(_DMAX << FRACTION_BITS_IN_SHORT_3) }};
// static math_constant  Infinite = {{ INIT_MATH_CONSTANT(_DMAX << FRACTION_BITS_IN_SHORT_3) }};
// static math_constant RootOfEps = {{ INIT_MATH_CONSTANT((VALUE_OF_FFFF_IN_SHORT_3 - NBITS / 2) << FRACTION_BITS_IN_SHORT_3) }};
// static math_constant       Big = {{ INIT_MATH_CONSTANT((VALUE_OF_FFFF_IN_SHORT_3 + NBITS / 2) << FRACTION_BITS_IN_SHORT_3) }};
// static math_constant       Nan = {{ INIT_MATH_CONSTANT(_DNAN) }};

static const w_double powers[] = {
  0x4024000000000000LL, /* 1e001 */
  0x4059000000000000LL, /* 1e002 */
  0x40c3880000000000LL, /* 1e004 */
  0x4197d78400000000LL, /* 1e008 */
  0x4341c37937e08000LL, /* 1e016 */
  0x4693b8b5b5056e17LL, /* 1e032 */
  0x4d384f03e93ff9f5LL, /* 1e064 */
  0x5a827748f9301d32LL, /* 1e128 */
  0x75154fdd7f73bf3cLL, /* 1e256 */
};

static const w_double factors[] = {
  0x0000000000000000LL, /* 0.000 */
  0x4197d78400000000LL, /* 1e008 */
  0x4341c37937e08000LL, /* 1e016 */
  0x44ea784379d99db4LL, /* 1e024 */
  0x4693b8b5b5056e17LL, /* 1e032 */
};

static const w_int numPowers = (sizeof(powers) / sizeof(powers[0]) - 1);

#define MAX_LONG_STRING_SIZE          64
#define MAX_INT_STRING_SIZE           32
#define MAX_DOUBLE_STRING_SIZE        64
#define MAX_FLOAT_STRING_SIZE         32

/*
** Tables and constants that are being used as polynomial coefficients
** in calculating the series for cos, sin, tan, exp, ...
*/

static const w_double sincos_c[8] = {
  0xbda93987e26d4e7aLL, /* -0.000000000011470879 */
  0x3e21eeed69c2a98cLL, /*  0.000000002087712071 */
  0xbe927e4fb76db5deLL, /* -0.000000275573192202 */
  0x3efa01a019daaafcLL, /*  0.000024801587292937 */
  0xbf56c16c16c16c2aLL, /* -0.001388888888888893 */
  0x3fa55555555555b4LL, /*  0.041666666666667325 */
  0xbfe0000000000000LL, /* -0.500000000000000000 */
  0x3ff0000000000000LL, /*  1.000000000000000000 */
};

static const w_double sincos_s[8] = {
  0xbd6ae8032efc250fLL, /* -0.000000000000764723 */
  0x3de6125959501a89LL, /*  0.000000000160592578 */
  0xbe5ae64567ea0237LL, /* -0.000000025052108383 */
  0x3ec71de3a544744cLL, /*  0.000002755731921890 */
  0xbf2a01a01a01a030LL, /* -0.000198412698412699 */
  0x3f81111111111127LL, /*  0.008333333333333372 */
  0xbfc5555555555555LL, /* -0.166666666666666667 */
  0x3ff0000000000000LL, /*  1.000000000000000000 */
};

/*
static const w_double asincos_p[5] = {
  0xbfe64bbdb5e61e65LL, /. -0.69674573447350646411e+0 ./
  0x40244e1764ec3927LL, /.  0.10152522233806463645e+2 ./
  0xc043d82ca9a6da9fLL, /. -0.39688862997504877339e+2 ./
  0x404c9aa7360ad48aLL, /.  0.57208227877891731407e+2 ./
  0xc03b5e55a83a0a62LL, /. -0.27368494524164255994e+2 ./
};

static const w_double asincos_q[6] = {
  0x3ff0000000000000LL, /.  0.10000000000000000000e+1 ./
  0xc037d2e86ef9861fLL, /. -0.23823859153670238830e+2 ./
  0x4062de7c96591c70LL, /.  0.15095270841030604719e+3 ./
  0xc077ddcefc56a848LL, /. -0.38186303361750149284e+3 ./
  0x407a124f101eb843LL, /.  0.41714430248260412556e+3 ./
  0xc06486c03e2b87ccLL, /. -0.16421096714498560795e+3 ./
};

static const w_double tan_p[3] = {
  0xbef2bab72ea2c724LL, /. -0.17861707342254426711e-4 ./
  0x3f6c0e82a63baadfLL, /.  0.34248878235890589960e-2 ./
  0xbfc112b5e54d0900LL, /. -0.13338350006421960681e+0 ./
};

static const w_double tan_q[4] = {
  0x3ea0b774f07678e9LL, /.  0.49819433993786512270e-6 ./
  0xbf346f6499094841LL, /. -0.31181531907010027307e-3 ./
  0x3f9a479ea17e2159LL, /.  0.25663832289440112864e-1 ./
  0xbfdddeb047fbd9d5LL, /. -0.46671683339755294240e+0 ./
};

static const w_double atan_a[8] = {
  0x0000000000000000LL, /. 0.00000000000000000000 ./
  0x3fe0c152382d7366LL, /. 0.52359877559829887308 ./
  0x3ff921fb54442d18LL, /. 1.57079632679489661923 ./
  0x3ff0c152382d7366LL, /. 1.04719755119659774615 ./
  0x3ff921fb54442d18LL, /. 1.57079632679489661923 ./
  0x4000c152382d7366LL, /. 2.09439510239319549231 ./
  0x400921fb54442d18LL, /. 3.14159265358979323846 ./
  0x4004f1a6c638d03fLL, /. 2.61799387799149436538 ./
};

static const w_double atan_p[4] = {
  0xbfeacd7ad9b187bdLL, /. -0.83758299368150059274e+0 ./
  0xc020fd3f5c8d6a63LL, /. -0.84946240351320683534e+1 ./
  0xc034817fb9e2bccbLL, /. -0.20505855195861651981e+2 ./
  0xc02b60a651061ce2LL, /. -0.13688768894191926929e+2 ./
};

static const w_double atan_q[5] = {
  0x3ff0000000000000LL, /. 0.10000000000000000000e+1 ./
  0x402e0c49e14ac710LL, /. 0.15024001160028576121e+2 ./
  0x404dca0a320da3d7LL, /. 0.59578436142597344465e+2 ./
  0x40558a12040b6da5LL, /. 0.86157349597130242515e+2 ./
  0x4044887cbcc495a9LL, /. 0.41066306682575781263e+2 ./
};
*/

static const w_double exp_p[3] = {
  0x3f008b442ae6921eLL, /* 0.31555192765684646356e-4 */
  0x3f7f074bf22a12a6LL, /* 0.75753180159422776666e-2 */
  0x3fd0000000000000LL, /* 0.25000000000000000000e+0 */
};

static const w_double exp_q[4] = {
  0x3ea933630ce50455LL, /* 0.75104028399870046114e-6 */
  0x3f44af0c5c28d4dfLL, /* 0.63121894374398503557e-3 */
  0x3fad172851dfd9ffLL, /* 0.56817302698551221787e-1 */
  0x3fe0000000000000LL, /* 0.50000000000000000000e+0 */
};

static const w_double log_p[3] = {
  0xbfe94415b356bd29LL, /* -0.78956112887491257267e+0 */
  0x4030624a2016afedLL, /* 0.16383943563021534222e+2 */
  0xc05007ff12b3b59aLL, /* -0.64124943423745581147e+2 */
};

static const w_double log_q[3] = {
  0xc041d5804b67ce0fLL, /* -0.35667977739034646171e+2 */
  0x40738083fa15267eLL, /* 0.31203222091924532844e+3 */
  0xc0880bfe9c0d9077LL, /* -0.76949932108494879777e+3 */
};

/*
** Some factors
*/

static const w_double           ln2 = 0x3fe62e42fefa39efLL;    /* 0.69314718055994530942   */
static const w_double          loge = 0x3fdbcb7b1526e50eLL;    /* 0.43429448190325182765   */
static const w_double        rthalf = 0x3fe6a09e667f3bcdLL;    /* 0.70710678118654752440   */
static const w_double        exp_c1 = 0x3fe62e4000000000LL;    /* 22713.0 / 32768.0        */
static const w_double        exp_c2 = 0x3eb7f7d1cf79abcaLL;    /* 1.428606820309417232e-6  */
static const w_double        hugexp = 0x409cc80000000000LL;    /* huge exp (w_double)1842  */
static const w_double        invln2 = 0x3ff71547652b82feLL;    /* 1.4426950408889634074    */
static const w_double          fold = 0x3fd126145e9ecd56LL;    /* 0.26794919243112270647   */
static const w_double         sqrt3 = 0x3ffbb67ae8584caaLL;    /* 1.73205080756887729353   */
static const w_double       sqrt3m1 = 0x3fe76cf5d0b09955LL;    /* 0.73205080756887729353   */
static const w_double    sin_cos_c1 = 0x3ff921fb00000000LL;    /* 3294198.0 / 2097152.0    */
static const w_double    sin_cos_c2 = 0x3e95110b4611a626LL;    /* 3.1391647865048132117e-7 */
static const w_double        log_c1 = 0x3fe62e4000000000LL;    /* 22713.0 / 32768.0        */
static const w_double        log_c2 = 0x3eb7f7d1cf79abcaLL;    /* 1.4286068203094173e-06   */
static const w_double       twobypi = 0x3fe45f306dc9c883LL;    /* 0.63661977236758134308   */
static const w_double         twopi = 0x401921fb54442d18LL;    /* 6.28318530717958647693   */
static const w_double         piby2 = 0x3ff921fb54442d18LL;    /* 1.57079632679489661923   */
static const w_double         piby4 = 0x3fe921fb54442d18LL;    /* 0.78539816339744830962   */
//static const w_double         sqrt2 = 0x3ff6a09e667f3bcdLL;    /* 1.41421356237309505      */
//static const w_double       sqrt_f1 = 0xbfc9679a430cc50aLL;    /*-0.1984742                */
//static const w_double       sqrt_f2 = 0x3fec2cf81b2f306fLL;    /* 0.8804894                */
//static const w_double       sqrt_f3 = 0x3fd454af195d9f18LL;    /* 0.3176687                */
//static const w_double ten_e_minus_3 = 0x3f50624dd2f1a9fcLL;    /* 10e-3                    */
//static const w_double       ten_e_7 = 0x416312d000000000LL;    /* 10e7                     */
//static const w_double       	ten = 0x4024000000000000LL;    /* 10                       */

#ifdef NATIVE_MATH
void fast_Math_static_sqrt(w_frame frame) {
  union {w_double d; w_word w[2];} double_x;

  double_x.w[0] = frame->jstack_top[-2].c;
  double_x.w[1] = frame->jstack_top[-1].c;
  if ((double_x.d != D_ZERO) && (double_x.d != D_MINUS_ZERO) && (double_x.d != D_POSITIVE_INFINITY)) {
    double_x.d = wfp_float64_sqrt(double_x.d);
  }
  frame->jstack_top[-2].c = double_x.w[0];
  frame->jstack_top[-1].c = double_x.w[1];
}
 
void fast_Math_static_sin(w_frame frame) {
  union {w_double d; w_word w[2];} double_x;

  double_x.w[0] = frame->jstack_top[-2].c;
  double_x.w[1] = frame->jstack_top[-1].c;
  if (wfp_float64_is_Infinite(double_x.d)) {
    double_x.d = D_NAN;
  }
  else if (!wfp_float64_is_NaN(double_x.d)) {
    double_x.d = wfp_float64_sin(double_x.d);
  }
  frame->jstack_top[-2].c = double_x.w[0];
  frame->jstack_top[-1].c = double_x.w[1];
}
 
void fast_Math_static_cos(w_frame frame) {
  union {w_double d; w_word w[2];} double_x;

  double_x.w[0] = frame->jstack_top[-2].c;
  double_x.w[1] = frame->jstack_top[-1].c;
  if (wfp_float64_is_Infinite(double_x.d)) {
    double_x.d = D_NAN;
  }
  else if (!wfp_float64_is_NaN(double_x.d)) {
    double_x.d = wfp_float64_cos(double_x.d);
  }
  frame->jstack_top[-2].c = double_x.w[0];
  frame->jstack_top[-1].c = double_x.w[1];
}
 
void fast_Math_static_tan(w_frame frame) {
  union {w_double d; w_word w[2];} double_x;

  double_x.w[0] = frame->jstack_top[-2].c;
  double_x.w[1] = frame->jstack_top[-1].c;
  if (wfp_float64_is_Infinite(double_x.d)) {
    double_x.d = D_NAN;
  }
  else if (!wfp_float64_is_NaN(double_x.d)) {
    double_x.d = wfp_float64_tan(double_x.d);
  }
  frame->jstack_top[-2].c = double_x.w[0];
  frame->jstack_top[-1].c = double_x.w[1];
}
 
void fast_Math_static_asin(w_frame frame) {
  union {w_double d; w_word w[2];} double_x;

  double_x.w[0] = frame->jstack_top[-2].c;
  double_x.w[1] = frame->jstack_top[-1].c;
  if (wfp_float64_is_Infinite(double_x.d)) {
    double_x.d = D_NAN;
  }
  else if (!wfp_float64_is_NaN(double_x.d)) {
    double_x.d = wfp_float64_asin(double_x.d);
  }
  frame->jstack_top[-2].c = double_x.w[0];
  frame->jstack_top[-1].c = double_x.w[1];
}
 
void fast_Math_static_atan(w_frame frame) {
  union {w_double d; w_word w[2];} double_x;

  double_x.w[0] = frame->jstack_top[-2].c;
  double_x.w[1] = frame->jstack_top[-1].c;
  if (wfp_float64_is_Infinite(double_x.d)) {
    // Following looks reasonable, but seems that Sun return NaN
    // double_x.d = wfp_float64_is_negative(double_x.d) ? D_MINUS_HALF_PI : D_HALF_PI;
    double_x.d = D_NAN;
  }
  else if (!wfp_float64_is_NaN(double_x.d)) {
    double_x.d = wfp_float64_atan(double_x.d);
  }
  frame->jstack_top[-2].c = double_x.w[0];
  frame->jstack_top[-1].c = double_x.w[1];
}

void fast_Math_static_log(w_frame frame) {
  union {w_double d; w_word w[2];} double_x;

  double_x.w[0] = frame->jstack_top[-2].c;
  double_x.w[1] = frame->jstack_top[-1].c;
  if (wfp_float64_is_Infinite(double_x.d)) {
    double_x.d = wfp_float64_is_negative(double_x.d) ? D_NAN : double_x.d;
  }
  else if (!wfp_float64_is_NaN(double_x.d)) {
    double_x.d = wfp_float64_log(double_x.d);
  }
  frame->jstack_top[-2].c = double_x.w[0];
  frame->jstack_top[-1].c = double_x.w[1];
}

void fast_Math_static_exp(w_frame frame) {
  union {w_double d; w_word w[2];} double_x;

  double_x.w[0] = frame->jstack_top[-2].c;
  double_x.w[1] = frame->jstack_top[-1].c;
  if (wfp_float64_is_Infinite(double_x.d)) {
    double_x.d = wfp_float64_is_negative(double_x.d) ? D_ZERO : double_x.d;
  }
  else if (!wfp_float64_is_NaN(double_x.d)) {
    double_x.d = wfp_float64_exp(double_x.d);
  }
  frame->jstack_top[-2].c = double_x.w[0];
  frame->jstack_top[-1].c = double_x.w[1];
}

#endif
 

