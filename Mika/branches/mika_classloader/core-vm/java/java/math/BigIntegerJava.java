/**************************************************************************
 * Copyright (c) 2004 by Chris Gray, /k/ Embedded Java Solutions.          *
 *                                                                         *
 * Derived from the BouncyCastle lightweight cryptographic API for J2ME,   *
 * with additions from the Wonka implementation. The BouncyCastle code     *
 * carries the following copyright notice:                                 *
 *   ------------------------------------------------------------------    *
 *   Copyright (c) 2000 The Legion Of The Bouncy Castle                    *
 *   (http://www.bouncycastle.org)                                         *
 *                                                                         *
 *   Permission is hereby granted, free of charge, to any person obtaining *
 *   a copy of this software and associated documentation files (the       *
 *   "Software"), to deal in the Software without restriction, including   *
 *   without limitation the rights to use, copy, modify, merge, publish,   *
 *   distribute, sublicense, and/or sell copies of the Software, and to    *
 *   permit persons to whom the Software is furnished to do so, subject to *
 *   the following conditions:                                             *
 *                                                                         *
 *   The above copyright notice and this permission notice shall be        *
 *   included in all copies or substantial portions of the Software.       *
 *                                                                         *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,       *
 *   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF    *
 *   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.*
 *   IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY  *
 *   CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,  *
 *   TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE     *
 *   SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                *
 *   ------------------------------------------------------------------    *
 * The Wonka code carries the following notice:                            *
 *   ------------------------------------------------------------------    *
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
package java.math;


public class BigIntegerJava {

  private transient static final long IMASK = 0xffffffffL;

  /*
   * * The following fields are required by the serialized form.
   */
  private static int signum; // -1 / 0 / +1

  // Montgomery mult.)

  static int[] makeMagnitude(BigInteger bigi) {
    byte[] bval = bigi.magnitude;
    int i;
    int[] mag;
    int firstSignificant;

    // strip leading zeros
    for (firstSignificant = 0; firstSignificant < bval.length
        && bval[firstSignificant] == 0; firstSignificant++)
      ;

    if (firstSignificant >= bval.length) {
      return new int[0];
    }

    int nInts = (bval.length - firstSignificant + 3) / 4;
    int bCount = (bval.length - firstSignificant) % 4;
    if (bCount == 0)
      bCount = 4;

    mag = new int[nInts];
    int v = 0;
    int magnitudeIndex = 0;
    for (i = firstSignificant; i < bval.length; i++) {
      v <<= 8;
      v |= bval[i] & 0xff;
      bCount--;
      if (bCount <= 0) {
        mag[magnitudeIndex] = v;
        magnitudeIndex++;
        bCount = 4;
        v = 0;
      }
    }

    if (magnitudeIndex < mag.length) {
      mag[magnitudeIndex] = v;
    }

    return mag;
  }

  private final static byte bitCounts[] = { 0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3,
      2, 3, 3, 4, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 1, 2, 2, 3,
      2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5,
      4, 5, 5, 6, 1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4,
      3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5,
      4, 5, 5, 6, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 1, 2, 2, 3,
      2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5,
      4, 5, 5, 6, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 3, 4, 4, 5,
      4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5,
      4, 5, 5, 6, 3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 3, 4, 4, 5,
      4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 4, 5, 5, 6, 5, 6, 6, 7, 5, 6, 6, 7,
      6, 7, 7, 8 };

  private static int bitLength(int indx, int[] mag) {
    int bitLength;

    if (mag.length == 0) {
      return 0;
    } else {
      while (indx != mag.length && mag[indx] == 0) {
        indx++;
      }

      if (indx == mag.length) {
        return 0;
      }

      // bit length for everything after the first int
      bitLength = 32 * ((mag.length - indx) - 1);

      // and determine bitlength of first int
      bitLength += bitLen(mag[indx]);

      if (signum < 0) {
        // Check if magnitude is a power of two
        boolean pow2 = ((bitCounts[mag[indx] & 0xff])
            + (bitCounts[(mag[indx] >> 8) & 0xff])
            + (bitCounts[(mag[indx] >> 16) & 0xff]) + (bitCounts[(mag[indx] >> 24) & 0xff])) == 1;

        for (int i = indx + 1; i < mag.length && pow2; i++) {
          pow2 = (mag[i] == 0);
        }

        bitLength -= (pow2 ? 1 : 0);
      }
    }

    return bitLength;
  }

  //
  // bitLen(val) is the number of bits in val.
  //
  static int bitLen(int w) {
    // Binary search - decision tree (5 tests, rarely 6)
    return (w < 1 << 15 ? (w < 1 << 7 ? (w < 1 << 3 ? (w < 1 << 1 ? (w < 1 << 0 ? (w < 0 ? 32
        : 0)
        : 1)
        : (w < 1 << 2 ? 2 : 3))
        : (w < 1 << 5 ? (w < 1 << 4 ? 4 : 5) : (w < 1 << 6 ? 6 : 7)))
        : (w < 1 << 11 ? (w < 1 << 9 ? (w < 1 << 8 ? 8 : 9) : (w < 1 << 10 ? 10
            : 11)) : (w < 1 << 13 ? (w < 1 << 12 ? 12 : 13) : (w < 1 << 14 ? 14
            : 15))))
        : (w < 1 << 23 ? (w < 1 << 19 ? (w < 1 << 17 ? (w < 1 << 16 ? 16 : 17)
            : (w < 1 << 18 ? 18 : 19)) : (w < 1 << 21 ? (w < 1 << 20 ? 20 : 21)
            : (w < 1 << 22 ? 22 : 23)))
            : (w < 1 << 27 ? (w < 1 << 25 ? (w < 1 << 24 ? 24 : 25)
                : (w < 1 << 26 ? 26 : 27)) : (w < 1 << 29 ? (w < 1 << 28 ? 28
                : 29) : (w < 1 << 30 ? 30 : 31)))));
  }

  /**
   * unsigned comparison on two arrays - note the arrays may start with leading
   * zeros.
   */
  private static int compareTo(int xIndx, int[] x, int yIndx, int[] y) {
    while (xIndx != x.length && x[xIndx] == 0) {
      xIndx++;
    }

    while (yIndx != y.length && y[yIndx] == 0) {
      yIndx++;
    }

    if ((x.length - xIndx) < (y.length - yIndx)) {
      return -1;
    }

    if ((x.length - xIndx) > (y.length - yIndx)) {
      return 1;
    }

    // lengths of magnitudes the same, test the magnitude values

    while (xIndx < x.length) {
      long v1 = (x[xIndx++]) & IMASK;
      long v2 = (y[yIndx++]) & IMASK;
      if (v1 < v2) {
        return -1;
      }
      if (v1 > v2) {
        return 1;
      }
    }

    return 0;
  }

  /**
   * zero out the array x
   */
  static void zero(int[] x) {
    for (int i = 0; i != x.length; i++) {
      x[i] = 0;
    }
  }

  /*
   * public BigInteger modPow(BigInteger exponent, BigInteger m) throws
   * ArithmeticException { int[] zVal = null; int[] yAccum = null; int[] yVal;
   *  // Montgomery exponentiation is only possible if the modulus is odd, //
   * but AFAIK, this is always the case for crypto algo's boolean useMonty =
   * ((m.mag[m.mag.length - 1] & 1) == 1); long mQ = 0; if (useMonty) { mQ =
   * m.getMQuote();
   *  // tmp = this * R mod m BigInteger tmp = this.shiftLeft(32 *
   * m.mag.length).mod(m); zVal = tmp.mag;
   * 
   * useMonty = (zVal.length == m.mag.length);
   * 
   * if (useMonty) { yAccum = new int[m.mag.length + 1]; } }
   * 
   * if (!useMonty) { if (mag.length <= m.mag.length) { //zAccum = new
   * int[m.mag.length * 2]; zVal = new int[m.mag.length];
   * 
   * System.arraycopy(mag, 0, zVal, zVal.length - mag.length, mag.length); }
   * else { // // in normal practice we'll never see this... // BigInteger tmp =
   * this.remainder(m);
   * 
   * //zAccum = new int[m.mag.length * 2]; zVal = new int[m.mag.length];
   * 
   * System.arraycopy(tmp.mag, 0, zVal, zVal.length - tmp.mag.length,
   * tmp.mag.length); }
   * 
   * yAccum = new int[m.mag.length * 2]; }
   * 
   * yVal = new int[m.mag.length];
   *  // // from LSW to MSW // for (int i = 0; i < exponent.mag.length; i++) {
   * int v = exponent.mag[i]; int bits = 0;
   * 
   * if (i == 0) { while (v > 0) { v <<= 1; bits++; }
   *  // // first time in initialise y // System.arraycopy(zVal, 0, yVal, 0,
   * zVal.length);
   * 
   * v <<= 1; bits++; }
   * 
   * while (v != 0) { if (useMonty) { // Montgomery square algo doesn't exist,
   * and a normal // square followed by a Montgomery reduction proved to // be
   * almost as heavy as a Montgomery mulitply. multiplyMonty(yAccum, yVal, yVal,
   * m.mag, mQ); } else { square(yAccum, yVal); remainder(yAccum, m.mag);
   * System.arraycopy(yAccum, yAccum.length - yVal.length, yVal, 0,
   * yVal.length); zero(yAccum); } bits++;
   * 
   * if (v < 0) { if (useMonty) { multiplyMonty(yAccum, yVal, zVal, m.mag, mQ); }
   * else { multiply(yAccum, yVal, zVal); remainder(yAccum, m.mag);
   * System.arraycopy(yAccum, yAccum.length - yVal.length, yVal, 0,
   * yVal.length); zero(yAccum); } }
   * 
   * v <<= 1; }
   * 
   * while (bits < 32) { if (useMonty) { multiplyMonty(yAccum, yVal, yVal,
   * m.mag, mQ); } else { square(yAccum, yVal); remainder(yAccum, m.mag);
   * System.arraycopy(yAccum, yAccum.length - yVal.length, yVal, 0,
   * yVal.length); zero(yAccum); } bits++; } }
   * 
   * if (useMonty) { // Return y * R^(-1) mod m by doing y * 1 * R^(-1) mod m
   * zero(zVal); zVal[zVal.length - 1] = 1; multiplyMonty(yAccum, yVal, zVal,
   * m.mag, mQ); }
   * 
   * return new BigInteger(1, yVal); }
   */
  /**
   * return w with w = x * x - w is assumed to have enough space.
   */
  static int[] square(int[] w, int[] x) {
    long u1, u2, c;

    if (w.length != 2 * x.length) {
      throw new IllegalArgumentException("no I don't think so...");
    }

    for (int i = x.length - 1; i != 0; i--) {
      long v = (x[i] & IMASK);

      u1 = v * v;
      u2 = u1 >>> 32;
      u1 = u1 & IMASK;

      u1 += (w[2 * i + 1] & IMASK);

      w[2 * i + 1] = (int) u1;
      c = u2 + (u1 >> 32);

      for (int j = i - 1; j >= 0; j--) {
        u1 = (x[j] & IMASK) * v;
        u2 = u1 >>> 31; // multiply by 2!
        u1 = (u1 & 0x7fffffff) << 1; // multiply by 2!
        u1 += (w[i + j + 1] & IMASK) + c;

        w[i + j + 1] = (int) u1;
        c = u2 + (u1 >>> 32);
      }
      c += w[i] & IMASK;
      w[i] = (int) c;
      w[i - 1] = (int) (c >> 32);
    }

    u1 = (x[0] & IMASK);
    u1 = u1 * u1;
    u2 = u1 >>> 32;
    u1 = u1 & IMASK;

    u1 += (w[1] & IMASK);

    w[1] = (int) u1;
    w[0] = (int) (u2 + (u1 >> 32) + w[0]);

    return w;
  }

  /**
   * return x with x = y * z - x is assumed to have enough space.
   */
  static int[] multiply(int[] x, int[] y, int[] z) {
    for (int i = z.length - 1; i >= 0; i--) {
      long a = z[i] & IMASK;
      long value = 0;

      for (int j = y.length - 1; j >= 0; j--) {
        value += a * (y[j] & IMASK) + (x[i + j + 1] & IMASK);

        x[i + j + 1] = (int) value;

        value >>>= 32;
      }

      x[i] = (int) value;
    }

    return x;
  }

  /**
   * Calculate mQuote = -m^(-1) mod b with b = 2^32 (32 = word size)
   */
  static long getMQuote(BigInteger bigi) {
    byte[] mag = bigi.magnitude;
    if ((mag[mag.length - 1] & 1) == 0) {
      return -1L; // not for even numbers
    }

    byte[] bytes = { 1, 0, 0, 0, 0 };
    BigInteger b = new BigInteger(1, bytes); // 2^32
    return bigi.negate().mod(b).modInverse(b).longValue();
  }

  /**
   * Montgomery multiplication: a = x * y * R^(-1) mod m <br>
   * Based algorithm 14.36 of Handbook of Applied Cryptography. <br>
   * <li> m, x, y should have length n </li>
   * <li> a should have length (n + 1) </li>
   * <li> b = 2^32, R = b^n </li>
   * <br>
   * The result is put in x <br>
   * NOTE: the indices of x, y, m, a different in HAC and in Java
   */
  static void multiplyMonty(int[] a, int[] x, int[] y, int[] m, long mQuote)
  // mQuote = -m^(-1) mod b
  {
    int n = m.length;
    int nMinus1 = n - 1;
    long y_0 = y[n - 1] & IMASK;

    // 1. a = 0 (Notation: a = (a_{n} a_{n-1} ... a_{0})_{b} )
    for (int i = 0; i <= n; i++) {
      a[i] = 0;
    }

    // 2. for i from 0 to (n - 1) do the following:
    for (int i = n; i > 0; i--) {

      long x_i = x[i - 1] & IMASK;

      // 2.1 u = ((a[0] + (x[i] * y[0]) * mQuote) mod b
      long u = ((((a[n] & IMASK) + ((x_i * y_0) & IMASK)) & IMASK) * mQuote)
          & IMASK;

      // 2.2 a = (a + x_i * y + u * m) / b
      long prod1 = x_i * y_0;
      long prod2 = u * (m[n - 1] & IMASK);
      long tmp = (a[n] & IMASK) + (prod1 & IMASK) + (prod2 & IMASK);
      long carry = (prod1 >>> 32) + (prod2 >>> 32) + (tmp >>> 32);
      for (int j = nMinus1; j > 0; j--) {
        prod1 = x_i * (y[j - 1] & IMASK);
        prod2 = u * (m[j - 1] & IMASK);
        tmp = (a[j] & IMASK) + (prod1 & IMASK) + (prod2 & IMASK)
            + (carry & IMASK);
        carry = (carry >>> 32) + (prod1 >>> 32) + (prod2 >>> 32) + (tmp >>> 32);
        a[j + 1] = (int) tmp; // division by b
      }
      carry += (a[0] & IMASK);
      a[1] = (int) carry;
      a[0] = (int) (carry >>> 32);
    }

    // 3. if x >= m the x = x - m
    if (compareTo(0, a, 0, m) >= 0) {
      subtract(0, a, 0, m);
    }

    // put the result in x
    for (int i = 0; i < n; i++) {
      x[i] = a[i + 1];
    }
  }

  /**
   * return x = x % y - done in place (y value preserved)
   */
  static int[] remainder(int[] x, int[] y) {
    int xyCmp = compareTo(0, x, 0, y);

    if (xyCmp > 0) {
      int[] c;
      int shift = bitLength(0, x) - bitLength(0, y);

      if (shift > 1) {
        c = shiftLeft(y, shift - 1);
      } else {
        c = new int[x.length];

        System.arraycopy(y, 0, c, c.length - y.length, y.length);
      }

      subtract(0, x, 0, c);

      int xStart = 0;
      int cStart = 0;

      for (;;) {
        int cmp = compareTo(xStart, x, cStart, c);

        while (cmp >= 0) {
          subtract(xStart, x, cStart, c);
          cmp = compareTo(xStart, x, cStart, c);
        }

        xyCmp = compareTo(xStart, x, 0, y);

        if (xyCmp > 0) {
          if (x[xStart] == 0) {
            xStart++;
          }

          shift = bitLength(cStart, c) - bitLength(xStart, x);

          if (shift == 0) {
            c = shiftRightOne(cStart, c);
          } else {
            c = shiftRight(cStart, c, shift);
          }

          if (c[cStart] == 0) {
            cStart++;
          }
        } else if (xyCmp == 0) {
          for (int i = xStart; i != x.length; i++) {
            x[i] = 0;
          }
          break;
        } else {
          break;
        }
      }
    } else if (xyCmp == 0) {
      for (int i = 0; i != x.length; i++) {
        x[i] = 0;
      }
    }

    return x;
  }

  /**
   * do a left shift - this returns a new array.
   */
  private static int[] shiftLeft(int[] mag, int n) {
    int nInts = n >>> 5;
    int bitCount = n & 0x1f;
    int magLen = mag.length;
    int newMag[] = null;

    if (bitCount == 0) {
      newMag = new int[magLen + nInts];
      for (int i = 0; i < magLen; i++) {
        newMag[i] = mag[i];
      }
    } else {
      int i = 0;
      int bitCount2 = 32 - bitCount;
      int highBits = mag[0] >>> bitCount2;

      if (highBits != 0) {
        newMag = new int[magLen + nInts + 1];
        newMag[i++] = highBits;
      } else {
        newMag = new int[magLen + nInts];
      }

      int m = mag[0];
      for (int j = 0; j < magLen - 1; j++) {
        int next = mag[j + 1];

        newMag[i++] = (m << bitCount) | (next >>> bitCount2);
        m = next;
      }

      newMag[i] = mag[magLen - 1] << bitCount;
    }

    return newMag;
  }

  /**
   * do a right shift - this does it in place.
   */
  private static int[] shiftRight(int start, int[] mag, int n) {
    int nInts = (n >>> 5) + start;
    int bitCount = n & 0x1f;
    int magLen = mag.length;

    if (nInts != start) {
      int delta = (nInts - start);

      for (int i = magLen - 1; i >= nInts; i--) {
        mag[i] = mag[i - delta];
      }
      for (int i = nInts - 1; i >= start; i--) {
        mag[i] = 0;
      }
    }

    if (bitCount != 0) {
      int bitCount2 = 32 - bitCount;
      int m = mag[magLen - 1];

      for (int i = magLen - 1; i >= nInts + 1; i--) {
        int next = mag[i - 1];

        mag[i] = (m >>> bitCount) | (next << bitCount2);
        m = next;
      }

      mag[nInts] >>>= bitCount;
    }

    return mag;
  }

  /**
   * do a right shift by one - this does it in place.
   */
  private static int[] shiftRightOne(int start, int[] mag) {
    int magLen = mag.length;

    int m = mag[magLen - 1];

    for (int i = magLen - 1; i >= start + 1; i--) {
      int next = mag[i - 1];

      mag[i] = (m >>> 1) | (next << 31);
      m = next;
    }

    mag[start] >>>= 1;

    return mag;
  }

  /**
   * returns x = x - y - we assume x is >= y
   */
  private static int[] subtract(int xStart, int[] x, int yStart, int[] y) {
    int iT = x.length - 1;
    int iV = y.length - 1;
    long m;
    int borrow = 0;

    do {
      m = (x[iT] & IMASK) - (y[iV--] & IMASK) + borrow;

      x[iT--] = (int) m;

      if (m < 0) {
        borrow = -1;
      } else {
        borrow = 0;
      }
    } while (iV >= yStart);

    while (iT >= xStart) {
      m = (x[iT] & IMASK) + borrow;
      x[iT--] = (int) m;

      if (m < 0) {
        borrow = -1;
      } else {
        break;
      }
    }
    return x;
  }
}
