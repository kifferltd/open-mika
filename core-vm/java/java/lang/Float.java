/**************************************************************************
* Copyright (c) 2011, 2023 by KIFFER Ltd. All rights reserved.            *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       *
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    *
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         *
* OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  *
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                              *
**************************************************************************/

package java.lang;

public final class Float extends Number implements Comparable {

  private static final long serialVersionUID = -2671257302660747028L;

  private final float value;

  public static final float MIN_VALUE = 1.40129846432481707e-45f;
  public static final float MAX_VALUE = 3.40282346638528860e+38f;
  public static final float NEGATIVE_INFINITY = (float)(-1.0/0.0);
  public static final float POSITIVE_INFINITY = (float)(+1.0/0.0);
  public static final float NaN = (float)(0.0/0.0);
  public static final Class TYPE = getWrappedClass();

  public Float(float value) {
    this.value = value;
  }

  public Float(double value) {
    this.value = (float)value;
  }

  public Float(String s) throws NumberFormatException {
    this(Math.floatValue(s));
  }

  public String toString() {
    return Math.toString(value);
  }

  /**
   ** Note that equals() needs to return true for two NaNs, false for +/-0.
   */
  public boolean equals(Object obj) {
    if (obj == null) {

      return false;

    }

    try {
      return floatToIntBits(((Float)obj).value) == floatToIntBits(value);
    }
    catch(ClassCastException cce) {

      return false;

    }
  }

  public int compareTo(Float anotherFloat) {
    return compare(value, anotherFloat.value);
  }

  public int compareTo(Object obj) throws ClassCastException {
    return compareTo((Float)obj);
  }

  public static int compare(float one, float two) {
		if (isNaN(one)) {
			return isNaN(two) ? 0 : 1;
		} else if (isNaN(two)) {
			return -1;
		}
		if (one == 0.0 && two == 0.0) {
			int oneL = floatToRawIntBits(one);
			int twoL = floatToRawIntBits(two);
			return oneL > twoL ? 1 : (twoL > oneL ? -1 : 0);
		}
		return one < two ? -1 : (one > two ? 1 : 0);
	}
  
  public int hashCode() {
    return Float.floatToIntBits(value);
  }

  public byte byteValue() {
    return (byte)value;
  }

  public short shortValue() {
    return (short)value;
  }

  public int intValue() {
    return (int)value;
  }

  public long longValue() {
    return (long)value;
  }

  public float floatValue() {
    return value;
  }

  public double doubleValue() {
    return value;
  }

  public static boolean isNaN(float v) {
    return v != v;
  }

  public boolean isNaN() {
    return value != value;
  }

  public static native boolean isInfinite(float v);

  public boolean isInfinite() {
    return Float.isInfinite(this.value);
  }

  public static String toString(float f) {
    return Math.toString(f);
  }

  public static float parseFloat(String s) {
    return Math.floatValue(s);
  }

  public static Float valueOf(String s) throws NullPointerException, NumberFormatException {
    return new Float(Math.floatValue(s));
  }

  public static int floatToIntBits(float value) {
    return Float.floatToRawIntBits(value == value ? value :  Float.NaN);
  }

  public static native int floatToRawIntBits(float value);

  public static native float intBitsToFloat(int bits);

  private static native Class getWrappedClass();
}
