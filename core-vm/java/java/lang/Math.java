/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2009 by Chris Gray, /k/ Embedded Java Solutions.    *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

package java.lang;

/**
 ** Class <b>java.lang.Math</b> is a little odd.  It is a utility class which
 ** implements basic mathematical functions similar to those found in
 ** the standard libraries of many languages.  The printed documentation 
 ** for the class (<i>The Java Class Libraries</i>) refers the reader to the 
 ** <i>Java Language Specification</i> (1st Ed.!) for the semantics of the 
 ** various methods.  Curiously, the JLS in turn refers to a C library
 ** (<tt>fdlibm</tt>), with a remark to the effect that the C code is to 
 ** be interpreted with Java semantics.  Curious, because
 **   <ul>
 **   <li>the semantics of C are less well-defined than those of Java,
 **   particularly in the area of floating-point arithmetic.  It is
 **   a strange idea to define the semantics of a relatively well-defined
 **   language in terms of a less well-defined language.
 **   <li>in the name of WORA (write once, run anywhere), the semantics
 **   are defined in terms of a reference implementation (the C library
 **   already mentioned).  This seems to imply that it is more important
 **   that the results of a calculation be consistent across implementations
 **   than that they be correct: arguably this is taking WORA a little too
 **   far.
 **   </ul> 
 ** 
 ** <p>The implementations which follow are believed by the authors to be
 ** roughly as correct, mathematically speaking, as the <tt>fdlibm</tt>
 ** code (sometimes more so, sometimes less), based on our own tests; 
 ** we do not however claim that they are bit-compatible with <tt>fdlibm</tt>
 ** or any other library, and here as elsewhere ANY EXPRESS OR IMPLIED 
 ** WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 ** MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 ** <p>We have also tried to avoid including here any material on which
 ** copyright or patent rights might be claimed by any third party:
 **  <ul>
 **  <li>The algorithms for sqrt, sin, cos, tan, and exp are based on 
 **  Newton's approximation and Taylor series from way back when, coded
 **  from scratch.  The string<->number conversions are also straight off
 **  the top of [CG]'s head.
 **  <li>Other algorithms draw on work performed for the United
 **  States Air Force by Col. William A. Whitaker and Lt. Tim Eicholz,
 **  and stated by them to be released into the public domain.
 **  We therefore believe these algorithms (and their expression
 **  here) to be unencumbered.
 **  </ul>
 **
 ** <p>It follows that the Copyright notice above should be read not
 ** as claiming proprietary rights in the expression of these ideas,
 ** but rather as affirming our belief that these ideas are there for
 ** everybody to use.
 **
 ** <p>Finally, a riddle and its answer:
 ** <blockquote>
 **    Q: When is a number not equal to itself? <br>
 **    A: When it's Not a Number.
 ** </blockquote>
 ** <p><author>Chris Gray, ACUNIA VM Architect, August 21 2001</author>
 */
public final class Math {

  static {
    init();
  }

  private static native void init();

 // private static String digits = "0123456789abcdefghijklmnopqrstuvwxyz";

  /**
  ** this constructor is added to prevent the compiler from adding a default constructor.
  ** Since the default constructor is public, it would be possible todo: Math m = new Math();
  ** Such an Object would be useless ...
  */
  private Math(){}

  public static final double E = 2.7182818284590452354;
  public static final double PI = 3.14159265358979323846;

  /**
   ** A double-precision IEE754 number looks like this:
   ** Bit 64   63 .. 52 (implied)  51 .. 0
   **    sign  exp+3ff        0.1  rest of mantissa
   ** Bitmask <tt>d_signbit</tt> isolates the sign bit.
   */
  private static final long d_signbit   = 0x8000000000000000L;

  /**
   ** Bitmask <tt>d_expbits</tt> isolates the exponent field.
   */
  private static final long d_expbits   = 0x7ff0000000000000L;

  /**
   ** The exponent is offset by 1023 (0x3ff).
   */
  private static final long d_offsetbits = 0x3ff0000000000000L;

  /**
   ** Bitmask <tt>d_mantbits</tt> isolates the mantissa field.
   */
  private static final long d_mantbits   = 0x000fffffffffffffL;

  /**
   ** A single-precision IEE754 number looks like this:
   ** Bit 32   31 .. 23 (implied)  22 .. 0
   **    sign  exp+3ff        0.1  rest of mantissa
   ** Bitmask <tt>f_signbit</tt> isolates the sign bit.
   */
  private static final int f_signbit   = 0x80000000;

  /**
   ** Bitmask <tt>f_expbits</tt> isolates the exponent field.
   */
  private static final int f_expbits   = 0x7f800000;

  /**
  ** One revolution.
  */
  private static final double twopi  = Math.PI * 2.0;
   
  /**
  ** One right angle.
  */
  private static final double halfpi = Math.PI * 0.5;

  /**
  ** Half a right angle (45 degrees).
  */
  private static final double quarterpi = Math.PI * 0.25;

  /**
  ** The square root of the smallest double-precision number distinguishable
  ** from zero (more or less).  Within this distance of zero all functions
  ** can be treated as linear, 'coz a quadratic term would be negligible.
  */
  private static final double tiny = Double.longBitsToDouble(0x1ff0000000000000L);

  /**
   ** Trying to calculate the exponent of anything bigger than this would be foolish.
  */
  private static final double logbignum = 709.782712893384; // log(Double.MAX_VALUE)

  /**
   ** The (pseudo-)random number generator used by random().
   */
  private static java.util.Random RNG = null;


  /**
  ** The square root algorithm is a pretty dumb Newton-Raphson thang.
  ** Blame this on no one but me [CG].
  */
  public static double sqrt(double arg) {

    /*
     ** Eliminate tiresome cases.
     */
    if ((arg < 0) || (arg != arg)) {

      return Double.NaN;

    }
    if (arg == Double.POSITIVE_INFINITY){
       return arg;
    }

    /*
    ** Eliminate nice easy case.
    */
    if (arg < tiny) {

      return arg * 0.5;

    }
 
    long bits = Double.doubleToLongBits(arg);
    /*
    ** Extract the exponent.  We don't bother to mask out the sign bit,
    ** 'coz we already eliminated negative numbers.
    ** Then make sure the exponent is even (after allowing for the offset
    ** of 0x3ff, which is why the test looks wrong), and define fraction
    ** to be the fractional part and multiplier to be 2 raised to the power
    ** of half the exponent (geddit?).
    */
    long magbits = bits & d_expbits;
    double multiplier = Double.longBitsToDouble(magbits);
    double fraction = Double.longBitsToDouble((bits & d_mantbits) | d_offsetbits);
    if ((magbits & 0x0010000000000000L) == 0) {
        multiplier = multiplier * 0.5;
        fraction = fraction * 2;
    }
    multiplier =  Double.longBitsToDouble((((magbits - d_offsetbits) >> 1) + d_offsetbits) & d_expbits);

    /*
    ** Now comes Sir Isaac's contribution.  The number of iterations may be
    ** shown to be sufficient, using ``proof by vigorous assertion''.
    */
    double root = fraction * 0.5;
    root = (root + fraction/root) * 0.5;
    root = (root + fraction/root) * 0.5;
    root = (root + fraction/root) * 0.5;
    root = (root + fraction/root) * 0.5;
    root = (root + fraction/root) * 0.5;
    root = (root + fraction/root) * 0.5;

    return root * multiplier;
  }

  /**
   ** Evaluate sin() using a truncated Taylor series.
   ** Only use this in the range -quarterpi..quarterpi !
   */
  private static final double sine(double theta) {
    double temp;
    temp = theta * theta;
    temp = theta * theta * (1.0 - (temp / 156.0));
    temp = theta * theta * (1.0 - (temp / 110.0));
    temp = theta * theta * (1.0 - (temp / 72.0));
    temp = theta * theta * (1.0 - (temp / 42.0));
    temp = theta * theta * (1.0 - (temp / 20.0));
    temp = theta * (1.0 - (temp / 6.0));
    return temp;
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
   ** Here also, only [CG] is to blame.
   */
  public static double sin(double arg) {
    if (arg > -tiny && arg < tiny || arg != arg) {

      return arg;

    }

    double theta = arg % twopi;
    int phase;
    if (theta <= - Math.PI) {
        theta += twopi;
    }
    else if (theta > Math.PI) {
        theta -= twopi;
    }
    phase = (int)(theta / quarterpi);

    double temp;
    switch(phase) {
    case 0:
    case -1:

      return sine(theta);

    case 1:
      temp = sine(halfpi - theta);

      return sqrt(1.0 - temp * temp);

    case -3:
    case -2:
      temp = sine(-halfpi - theta);

      return -sqrt(1.0 - temp * temp);

    case 2:
      temp = sine(theta - halfpi);

      return sqrt(1.0 - temp * temp);
    default:

      return sine(Math.PI - theta);
    }
  }

  /**
   ** The cos() function works the same way as sin(), mutatis mutandis.
   */
  public static double cos(double arg) {
    if (arg > -tiny && arg < tiny) {

      return 1.0;

    }

    if (arg != arg) {

        return arg;

    }

    double theta = arg % twopi;
    int phase;
    if (theta <= - Math.PI) {
        theta += twopi;
    }
    else if (theta > Math.PI) {
        theta -= twopi;
    }
    phase = (int)(theta / quarterpi);

    double temp;
    switch(phase) {
    case 0:
    case -1:
      temp = sine(theta);

      return sqrt(1.0 - temp * temp);

    case 1:
    case 2:

      return -sine(theta - halfpi);

    case -2:
    case -3:

      return sine(halfpi + theta);

    default:
      temp = sine(Math.PI - theta);

      return -sqrt(1.0 - temp * temp);
    }
  }

  /**
   ** This is what they tort me at skule, anyway.
   */
  public static double tan(double arg) {
    if (arg > -tiny && arg < tiny) {

      return arg;

    }

    return sin(arg) / cos(arg);
  }


  /**
   ** Implementation of asin uses a rational polynomial.
   ** This and asin() itself are based on Whitaker and Eicholz's work.
   */
  private static double arcrat(double x) {
    double denom;
    double numer;

    numer = - 0.69674573447350646411e+0 * x;
    numer = (numer + 0.10152522233806463645e+2) * x;
    numer = (numer - 0.39688862997504877339e+2) * x;
    numer = (numer + 0.57208227877891731407e+2) * x;
    numer = (numer - 0.27368494524164255994e+2) * x;
    denom = x;
    denom = (denom - 0.23823859153670238830e+2) * x;
    denom = (denom + 0.15095270841030604719e+3) * x;
    denom = (denom - 0.38186303361750149284e+3) * x;
    denom = (denom + 0.41714430248260412556e+3) * x ;
    denom = denom - 0.16421096714498560795e+3;

    return numer/denom;
  }

  /**
   ** The asin() method proper first eliminates trivial or awkward cases,
   ** then finds a way to apply arcrat() to a value in [0.0,0.5]. 
   ** If the absolute value of the argument is in [0.5,1.0] then
   ** we use recursion, which is lazy: someone should take the time
   ** to eliniate this (e.g., the compiler 9->).
   */
  public static double asin(double arg) {
    double absval;
    double sign;

    if (arg > -tiny && arg < tiny || arg != arg) {

      return arg;

    }

    if (arg > 0) {
      if (arg > 1) {

        return Double.NaN;

      }

      if (arg > 0.5) {

        return halfpi - 2.0 * asin(sqrt((1.0 - arg) * 0.5));

      } 

      absval = arg;
      sign = 1.0;
    }
    else {
      if (arg < -1) {

        return Double.NaN;

      }

      if (arg < -0.5) {

        return 2.0 * asin(sqrt((1.0 + arg) * 0.5)) - halfpi;

      }

      absval = -arg;
      sign = -1.0;
    }

    return sign * (absval + (absval * arcrat(arg * arg)));
  }

  /**
   ** Lazy version.
   */
  public static double acos(double arg) {

    return halfpi - asin(arg);

  }

  /**
   ** Implementation of atan uses a rational polynomial.
   ** This and arctan() itself are based on Whitaker and Eicholz's work.
   */
  private static double tancrat(double x) {
    double denom;
    double numer;

    numer = - 0.83758299368150059274e+0 * x;
    numer = (numer - 0.84946240351320683534e+1) * x;
    numer = (numer - 0.20505855195861651981e+2) * x;
    numer = (numer - 0.13688768894191926929e+2) * x;
    denom = x;
    denom = (denom + 0.15024001160028576121e+2) * x;
    denom = (denom + 0.59578436142597344465e+2) * x;
    denom = (denom + 0.86157349597130242515e+2) * x;
    denom = denom + 0.41066306682575781263e+2;

    return numer/denom;
  }

  /**
   ** Similarly to asin(), atan() first narrows the range (to [0.0,1.0])
   ** and then invokes tancrat().
   */
  public static double atan(double arg) {
    double x = arg;
    boolean flipped = false;
    double sign = 1.0;
    double temp;

    if (arg == 0.0){
       return arg;
    }

    if (arg != arg) {

      return arg;

    }

    if (arg < 0.0) {
      x = -arg;
      sign = -1.0;
    }

    if (x > 1.0) {
      x = 1.0/x;
      flipped = true;
    }
    
    if (x < tiny) {
      temp = x;
    }
    else {
      temp = x + x * tancrat(x * x);
    }

    if (flipped) {
      temp = halfpi - temp;
    }

    return sign * temp;
  }

  /**
   ** The atan2() method is basically a wrapper around atan(), except
   ** that for negative arguments it sometimes returns a result in a
   ** different quadrant.
   */
  public static double atan2(double y, double x) {
    if (x != x || y != y) {

      return Double.NaN;

    }

    if (x == 0.0) {

      return y == 0.0 ? Double.NaN : y > 0 ? halfpi : -halfpi;

    }

    if (y == 0.0){
       if (x > 0){
          return y;
       }
       long l = Double.doubleToLongBits(PI) | Double.doubleToLongBits(y);
       return Double.longBitsToDouble(l);
    }

    if (Double.isInfinite(y)) {
      if (Double.isInfinite(x)) {

        return (y > 0 ? 1.0 : -1.0) * (x > 0 ? quarterpi : Math.PI - quarterpi);

      }
      else {

        return y > 0 ? halfpi : -halfpi;

      }
    }

    double temp = atan(y/x);

    if (x < 0.0) {
      if (y < 0.0) {
        temp = temp - Math.PI;
      } 
      else {
        temp = temp + Math.PI;
      }
    }

    return temp;
  }

  /**
   ** Our log is based on a simple Taylor series -- couldn't get
   ** the rational polynomial jazz to work on this one.
   */
  public static double log(double arg) {
    if (arg < 0.0 || arg != arg) {

      return Double.NaN;

    }

    if (arg < Double.longBitsToDouble(1L)) {

      return Double.NEGATIVE_INFINITY;

    }

    if (arg == Double.POSITIVE_INFINITY) {

    return arg;

    }

    /*
    ** Extract the exponent and fractional part.
    */
    long bits = Double.doubleToLongBits(arg);
    int exponent = (int)((bits - d_offsetbits) >> 52);
    double fraction = Double.longBitsToDouble((bits & d_mantbits) | d_offsetbits);

    /*
    ** Now we use the Taylor series for ln((x+1)/(x-1)):
    ** <pre>
    **     / x+1 \     /  1      1       1         \
    **  ln |-----| = 2*| --- + ----- + ----- + ... |    x >= 1
    **     \ x-1 /     \  x    3*x^3   5*x^5       /
    ** </pre>
    */
    double temp = 0.0;
    if (fraction > 1.0) {
      double x = (1 + fraction) / (fraction - 1);
      double square = 1.0 / (x * x);
      temp = 1.0/21.0;
      temp = (temp * square) + 1.0/19.0;
      temp = (temp * square) + 1.0/17.0;
      temp = (temp * square) + 1.0/15.0;
      temp = (temp * square) + 1.0/13.0;
      temp = (temp * square) + 1.0/11.0;
      temp = (temp * square) + 1.0/9.0;
      temp = (temp * square) + 1.0/7.0;
      temp = (temp * square) + 1.0/5.0;
      temp = (temp * square) + 1.0/3.0;
      temp = (temp * square) + 1.0;
      temp = 2.0 * temp / x;
    }

    /*
    ** Finally, add in ln(2) times the exponent.
    */ 
    return temp + 0.6931471805599453 * exponent;
  }
  /**
   ** For the exp() method we just use a schoolbook Taylor series.
   ** I'm sure there are faster ways to do this ...
   */
  public static double exp(double arg) {
      if (arg != arg) {

      return arg;

      }

      if (arg > logbignum) {

      return Double.POSITIVE_INFINITY;


      }

      if (arg == Double.NEGATIVE_INFINITY) {

      return 0.0;

      }

      if (arg > -tiny && arg < tiny) {

      return 1.0;

      }

      int exponent = (int)((arg / 0.6931471805599453) + (arg > 0 ? + 0.5 : -0.5));
      double difference = arg - (exponent *  0.6931471805599453);

      double temp = 1.0 + (difference / 12.0);
      temp = 1.0 + (difference / 11.0) * temp;
      temp = 1.0 + (difference / 10.0) * temp;
      temp = 1.0 + (difference / 9.0) * temp;
      temp = 1.0 + (difference / 8.0) * temp;
      temp = 1.0 + (difference / 7.0) * temp;
      temp = 1.0 + (difference / 6.0) * temp;
      temp = 1.0 + (difference / 5.0) * temp;
      temp = 1.0 + (difference / 4.0) * temp;
      temp = 1.0 + (difference / 3.0) * temp;
      temp = 1.0 + (difference / 2.0) * temp;
      temp = 1.0 + difference * temp;

      return Double.longBitsToDouble(((long)exponent + 0x3ff) << 52) * temp;
  }


  /**
   ** This looks more complicated than it is because of a lot of special-casing.
   ** Once all that is out of the way, we either use a multiply-and-square
   ** algorithm if b is an integer, or exp(log(a)*b) otherwise.  
   */
  public static double pow(double a, double b) {
    if (b > -tiny && b < tiny) {

      return 1.0;

    }

    if (b == 1.0) {

      return a;

    }

    if (b != b || a != a) {

        return Double.NaN;

    }

    if (Double.isInfinite(b)) {
      if (abs(a) == 1.0) {

        return Double.NaN;

      }
      else {

        return ((abs(a) > 1.0 && b > 0.0) || (abs(a) < 1.0 && b < 0.0)) ? Double.POSITIVE_INFINITY : 0.0;
      }
    }

    if (Double.isInfinite(a)) {
      if (b > 0.0) {

        return a;

      }
      else {

        return 0.0;

      }
    }

    long ib = (long)floor(b);

    if (ib == b) {
        double temp = 1.0;
        long j = ib < 0 ? -ib : ib;
        long k;
        for (k = 1; k <= j; k <<= 1);
        for (;;) {
        if ((k & j) != 0) {
          temp = temp * a;
        }
        k >>= 1;
        if (k > 0) {
          temp = temp * temp;
        }
        else {
          break;
        }
      }
      if (ib < 0) {
        temp = 1.0 / temp;
      }

      return temp;
    }
    else {
    /*
    ** Note that if b is non-integer and a is negative then log(a) will 
    ** return NaN, so we don't have to test for that case.
    */

      return exp(log(a) * b);

    }
  }

  /**
   ** Create an instance of Random and use this for all subsequent calls.
   */
  public static synchronized double random() {

    if (RNG == null) {
      RNG = new java.util.Random();
    }

    return RNG.nextDouble();

  }

  public static double toDegrees(double d)
  {
    if ( d == -0.0 || d ==  0.0 || d != d || Double.isInfinite(d)) {
      return d;
    }
    d = d * 180.0 / Math.PI;

    return d;
  }

  public static double toRadians(double d)
  {
    if ( d == -0.0 || d ==  0.0 || d != d || Double.isInfinite(d)) {
      return d;
    }
    d = d * Math.PI / 180.0;

    return d;
  }
  		
  /**
   ** A very simple abs().
   */
  public static int abs(int arg) {

    return (arg < 0) ? -arg : arg;

  }

  /**
   ** Another very simple abs().
   */
  public static long abs(long arg) {

    return (arg < 0L) ? -arg : arg;

  }

  /**
   ** A not-so-simple abs() (we need to return 0.0 for abs(-0.0)).
   */
  public static float abs(float arg) {

    return Float.intBitsToFloat(Float.floatToIntBits(arg) & ~f_signbit);

  }

  /**
   ** Another not-so-simple abs().
   */
  public static double abs(double arg) {

    return Double.longBitsToDouble(Double.doubleToLongBits(arg) & ~d_signbit);

  }

  /**
   ** A very simple max().
   */
  public static int max(int a, int b) {

    return (a > b) ? a : b;

  }

  /**
   ** Another very simple max().
   */
  public static long max(long a, long b) {

    return (a > b) ? a : b;

  }

  /**
   ** This max() is not so simple, because we have to ensure that
   ** max(0.0,-0.0) is 0.0.
   */
  public static float max(float a, float b) {
    int abits = Float.floatToIntBits(a);
    int bbits = Float.floatToIntBits(a);

    if (abits == 0 && b <= 0.0f) {

      return a;

    }

    if (bbits == 0 && a <= 0.0f) {

      return b;

    }

    return (a > b || a != a) ? a : b;

  }

  /**
   ** Another not-so-simple max().
   */
  public static double max(double a, double b) {
    long abits = Double.doubleToLongBits(a);
    long bbits = Double.doubleToLongBits(a);

    if (abits == 0L && b <= 0.0f) {

      return a;

    }

    if (bbits == 0L && a <= 0.0f) {

      return b;

    }


    return (a > b || a != a) ? a : b;

  }

  /**
   ** A very simple min().
   */
  public static int min(int a, int b) {

    return (a < b || a != a) ? a : b;

  }

  /**
   ** Another very simple min().
   */
  public static long min(long a, long b) {

    return (a < b || a != a) ? a : b;

  }

  /**
   ** A not-so-simple min().
   */
  public static float min(float a, float b) {
    int abits = Float.floatToIntBits(a);
    int bbits = Float.floatToIntBits(a);

    if (abits == f_signbit && b >= 0.0f) {

      return a;

    }

    if (bbits == f_signbit && a >= 0.0f) {

        return b;

    }


    return (a < b || a != a) ? a : b;

  }

  /**
   ** Another not-so-simple min().
   */
    public static double min(double a, double b) {
    long abits = Double.doubleToLongBits(a);
    long bbits = Double.doubleToLongBits(a);

    if (abits == d_signbit && b >= 0.0f) {

      return a;

    }

    if (bbits == d_signbit && a >= 0.0f) {

      return b;

    }


    return (a < b || a!= a) ? a : b;

  }

  /**
   ** If arg is already an integer, floor(arg) == arg.
   ** All numbers bigger than 2^52 are considered to be integers
   ** (how would we know if they weren't?).  For smaller numbers
   ** we can cast to a long and back to truncate towards zero,
   ** and then adjust as necessary.
   */
  public static double floor(double arg) {
    if ((abs(arg) > 0x0010000000000000L) || arg != arg || (arg == ((long)arg))) {

      return arg;

    }

    if (arg < 0.0) {
      return ((long)arg) - 1.0;

    }

    return ((long)arg);
  }

  /**
   ** See floor() above.
   ** Ha, floor above and ceiling below.  This is good stuff.
   */
  public static double ceil(double arg) {
    if ((abs(arg) > 0x0010000000000000L) || arg != arg || arg == ((long)arg)) {

      return arg;

    }

    if (arg < 0.0) {

      arg = ((long)(arg));
      return (arg == 0.0 ? -0.0 : arg);

    }

    return ((long)arg) + 1.0;
  }

  /**
   ** Simple enough, but look out for the case of infinite y, finite x.
   */
  public static double IEEEremainder(double x, double y) {
    if (Double.isInfinite(y) && ! Double.isInfinite(x)) {

      return x;

    }

    return x - y * rint(x / y);
  }

  /**
   ** The integer closest to arg (if two are equally close, take the even one).
   */
  public static double rint(double arg) {
    double f = floor(arg);
    double c = ceil(arg);
    double n;

    if (arg == f) {
      n = arg;
    }
    else if (arg - f > c - arg) {
      n = c;
    }
    else if (arg - f < c - arg) {
      n = f;
    }
    else if ((f % 2.0) == 0.0) {
      n = f;
    }
    else {
      n = c;
    }

    return n;
  }

  /**
   ** Defined to be (int)floor(arg + 0.5f).
   */
  public static int round(float arg) {

    return (int)floor(arg + 0.5f);

  }

  /**
   ** Same story.
   */
  public static long round(double arg) {

    return (long)floor(arg + 0.5d);

  }

 /**
  ** Implementation of Float.valueOf() and Float.parseFloat().
  ** Package access, so can only be seen within java.lang .
  */
  static float floatValue(String s) 
    throws NumberFormatException
  {
    s = s.trim();
    int length = s.length();

    if(length == 0){
      throw new NumberFormatException();
    }

    double result = 0.0f;
    boolean negative = false;
    boolean expnegative = false;
    int index = 0;
    String wholepart;
    String fractpart;
    String exppart;

    try {
      if (s.charAt(index) == '-') {
        negative = true;
        ++index;
      }
      else if (s.charAt(index) == '+') {
        ++index;
      }

      if((3 + index == length) &&s.regionMatches(true, index, "NaN", 0, 3)) {
        return Float.NaN;
      }
      if((8 + index == length) &&s.regionMatches(true, index, "Infinity", 0, 8)) {
        return negative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
      }
      
      if("dDfF".indexOf(s.charAt(length-1)) != -1){
         --length;
      }
      
      int point = s.indexOf('.', index);
      int e;

      if (point >= 0) {
        e = s.indexOf('e', point+1);
        if (e < 0) {
          e = s.indexOf('E', point+1);
        }
        wholepart = s.substring(index, point);
        if (e < 0) {
          fractpart = s.substring(point+1, length);
          exppart = "0";
          expnegative = false;
        }
        else {
          fractpart = s.substring(point+1, e);
          exppart = s.substring(e+1, length);
          if (exppart.charAt(0) == '-') {
            expnegative = true;
            exppart = exppart.substring(1);          }
          else if (exppart.charAt(0) == '+') {
            exppart = exppart.substring(1);
          }
        }
      }
      else {
        e = s.indexOf('e', index);
        if (e < 0) {
          e = s.indexOf('E', index);
        }
        if (e < 0) {
          wholepart = s.substring(index, length);
          fractpart = "";
          exppart = "0";
          expnegative = false;
        }
        else {
          wholepart = s.substring(index, e);
          fractpart = "";
          exppart = s.substring(e+1, length);
          if (exppart.charAt(0) == '-') {
            expnegative = true;
            exppart = exppart.substring(1);
          }
          else if (exppart.charAt(0) == '+') {
            exppart = exppart.substring(1);
          }
        }
      }

      if (fractpart.length() > 18) {
        fractpart = fractpart.substring(0, 18);
      }
    }
    catch (IndexOutOfBoundsException ioobe) {
      throw new NumberFormatException();
    }

    if (wholepart.length() == 0 && fractpart.length() == 0) {
      throw new NumberFormatException();
    }

    if (wholepart.length() == 0) {
      wholepart = "0";
    }

    if (fractpart.length() == 0) {
      fractpart = "0";
    }

    if (negative && wholepart.equals("0") && fractpart.equals("0")) {
      return -0.0f;
    }

    result = (Long.parseLong(fractpart) / pow(10.0, fractpart.length()));
    result += Long.parseLong(wholepart);
    int exponent = Integer.parseInt(exppart);
    if (exponent != 0) {
      result *= pow(10.0, expnegative ? -exponent : exponent);
    }
    return (float)(negative ? -result : result);
  }

 /**
  ** Implementation of Double.valueOf() and Double.parseDouble().
  ** Package access, so can only be seen within java.lang .
  */
  static double doubleValue(String s) 
    throws NumberFormatException
  {
    s = s.trim();
    int length = s.length();
    if(length == 0){
      throw new NumberFormatException();
    }


    double result = 0.0d;
    boolean negative = false;
    boolean expnegative = false;
    int index = 0;
    String wholepart;
    String fractpart;
    String exppart;



    try {
      if (s.charAt(index) == '-') {
        negative = true;
        ++index;
      }
      else if (s.charAt(index) == '+') {
        ++index;
      }

      if((3 + index == length) &&s.regionMatches(true, index, "NaN", 0, 3)) {
        return Float.NaN;
      }
      if((8 + index == length) &&s.regionMatches(true, index, "Infinity", 0, 8)) {
        return negative ? Float.NEGATIVE_INFINITY : Float.POSITIVE_INFINITY;
      }
      
      if("dDfF".indexOf(s.charAt(length-1)) != -1){
        --length;
     }

      int point = s.indexOf('.', index);
      int e;

      if (point >= 0) {
      e = s.indexOf('e', point+1);
        if (e < 0) {
          e = s.indexOf('E', point+1);
        }
        wholepart = s.substring(index,point);
        if (e < 0) {
          fractpart = s.substring(point+1, length);
          exppart = "0";
          expnegative = false;
        }
        else {
          fractpart = s.substring(point+1, e);
          exppart = s.substring(e+1, length);
          if (exppart.charAt(0) == '-') {
            expnegative = true;
            exppart = exppart.substring(1);
          }
          else if (exppart.charAt(0) == '+') {
            exppart = exppart.substring(1);
          }
        }
      }
      else {
        e = s.indexOf('e', index);
        if (e < 0) {
          e = s.indexOf('E', index);
        }
        if (e < 0) {
          wholepart = s.substring(index, length);
          fractpart = "";
          exppart = "0";
          expnegative = false;
        }
        else {
          wholepart = s.substring(index, e);
          fractpart = "";
          exppart = s.substring(e+1, length);
          if (exppart.charAt(0) == '-') {
            expnegative = true;
            exppart = exppart.substring(1);
          }
          else if (exppart.charAt(0) == '+') {
            exppart = exppart.substring(1);
          }
        }
      }

      if (fractpart.length() > 18) {
        fractpart = fractpart.substring(0, 18);
      }
    }
    catch (IndexOutOfBoundsException ioobe) {
      throw new NumberFormatException();
    }

    if (wholepart.length() == 0 && fractpart.length() == 0) {
      throw new NumberFormatException();
    }

    if (wholepart.length() == 0) {
    wholepart = "0";
    }

    if (fractpart.length() == 0) {
    fractpart = "0";
    }

    if (negative && wholepart.equals("0") && fractpart.equals("0")) {
    return -0.0d;
    }

    result = Long.parseLong(wholepart);
    result += Long.parseLong(fractpart) / pow(10.0, fractpart.length());
    int exponent = Integer.parseInt(exppart);
    if (exponent != 0) {
    result *= pow(10.0, expnegative ? -exponent : exponent);
    }

    return negative ? -result : result;
}

  /**
   ** Package-private method to convert a long to a string.
   */
  static String toString(long arg, int radix) {
    if (arg == 0) {
      return "0";
    }

    if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX) {
      radix = 10;
    }

    char[] buffer = new char[65];
    boolean negative = true;
    int length = 0;
   
    if (arg > 0) {
      negative = false;
      arg = -arg;
    }

    while (arg < 0) {
      buffer[64 - length] = Character.forDigit((int)(-(arg % radix)), radix);
      arg /= radix;
      ++length;
    }

    if (negative) {
      buffer[64 - length] = '-';
      ++length;
    }

    return new String(buffer, 65 - length, length);

  }

  /**
   ** Package-private method to convert an int to a string, unsigned.
   ** Only works for radix = 2, 8 or 16.
   */
  static String toStringUnsigned(int arg, int radix) {
    if (arg >= 0) {

      return toString(arg, radix);

    }

    char[] buffer = new char[32];
    int shift;
    int mask;
    int length = 0;
   
    switch(radix) {
      case 2:
        shift = 1;
        mask  = 1;
        break;

      case 8:
        shift = 3;
        mask  = 7;
        break;

      default: /* 16 */
        shift = 4;
        mask  = 15;
      }

    while (arg != 0) {
      buffer[31 - length] = Character.forDigit((arg & mask), radix);
      arg >>>= shift;
      ++length;
    }

    return new String(buffer, 32 - length, length);
  }


  /**
   ** Package-private method to convert a long to a string, unsigned.
   ** Only works for radix = 2, 8 or 16.
   */
  static String toStringUnsigned(long arg, int radix) {
    if (arg >= 0) {

      return toString(arg, radix);

    }

    char[] buffer = new char[64];
    int shift;
    int mask;
    int length = 0;
   
    switch(radix) {
      case 2:
        shift = 1;
        mask  = 1;
        break;

      case 8:
        shift = 3;
        mask  = 7;
        break;

      default: /* 16 */
        shift = 4;
        mask  = 15;
      }

    while (arg != 0) {
      buffer[63 - length] = Character.forDigit((int)(arg & mask), radix);
      arg >>>= shift;
      ++length;
    }

    return new String(buffer, 64 - length, length);
  }


  /**
   ** Package-private method to convert a float to a string.
   */
  static String toString(float arg) {
    if (arg != arg) {

      return "NaN";

    }

    int bits = Float.floatToIntBits(arg);
    int signbit = bits & f_signbit;

    if (bits == signbit) {

      return (signbit == 0) ? "0.0" : "-0.0";

    }

    if (Double.isInfinite(arg)) {

      return (signbit == 0) ? "Infinity" : "-Infinity";

    }

    float absvalue = abs(arg);

    if (absvalue >= 0.001 && absvalue < 10000000.0) {

      return ((signbit == 0) ? "" : "-") + MathHelper.toString_internal(absvalue);

    }
    else {
      long exponent = (((bits & f_expbits) >> 23) - 0x7f) * 30103L / 100000L;

      float scaled = absvalue / (float)pow(10.0f, exponent);
      while (scaled < 1.0f) {
        exponent -= 1;
        scaled *= 10.0f;
      }
      while (scaled >= 10.0f) {
        exponent += 1;
        scaled *= 0.1f;
      }

      return ((signbit == 0L) ? "" : "-") + MathHelper.toString_internal(scaled) + "E" + exponent;

    }
  }

  /**
   ** Package-private method to convert a double to a string.
   */
  static String toString(double arg) {
    if (arg != arg) {

      return "NaN";

    }

    long bits = Double.doubleToLongBits(arg);
    long signbit = bits & d_signbit;

    if (bits == signbit) {

      return (signbit == 0L) ? "0.0" : "-0.0";

    }

    if (Double.isInfinite(arg)) {

      return (signbit == 0L) ? "Infinity" : "-Infinity";

    }

    double absvalue = abs(arg);

    if (absvalue >= 0.001 && absvalue < 10000000.0) {

      return ((signbit == 0L) ? "" : "-") + MathHelper.toString_internal(absvalue);

    }
    else {
      long exponent = (((bits & d_expbits) >> 52) - 0x3ff) * 30103L / 100000L;

      double scaled = absvalue / pow(10.0d, exponent);
      while (scaled < 1.0) {
        exponent -= 1;
        scaled *= 10.0;
      }
      while (scaled >= 10.0) {
        exponent += 1;
        scaled *= 0.1;
      }

      return ((signbit == 0L) ? "" : "-") + MathHelper.toString_internal(scaled) + "E" + exponent;

    }
  }
}

