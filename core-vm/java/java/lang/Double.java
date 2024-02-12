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
** $Id: Double.java,v 1.2 2006/03/27 08:19:05 cvs Exp $
*/

package java.lang;

public final class Double extends Number implements Comparable {

  private static final long serialVersionUID = -9172774392245257468L;

  private final double value;

  public static final double MIN_VALUE = 5e-324;
  public static final double MAX_VALUE = 1.7976931348623157e+308;
  public static final double NEGATIVE_INFINITY = -1.0/0.0;
  public static final double POSITIVE_INFINITY = +1.0/0.0;
  public static final double NaN = 0.0/0.0;
  public static final Class TYPE = double.class;

  public Double(double value) {
    this.value = value;
  }

  public Double(String s) {
    this(Math.doubleValue(s));
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

  public float floatValue() {
    return (float)value;
  }

  public long longValue() {
    return (long)value;
  }

  public double doubleValue() {
    return value;
  }

  public int hashCode() {
     long v = doubleToLongBits(value);
     return ((int)(v^(v>>>32)));
  }

  /**
   ** Note that equals() needs to return true for two NaNs, false for +/-0.
   */
  public boolean equals ( Object obj)
  {
    if (obj == null) {

      return false;

    }

    try {
      return doubleToLongBits(((Double)obj).value) == doubleToLongBits(value);
    }
    catch (ClassCastException cce) {

      return false;

    }
  }

  public static boolean isNaN(double v) {
    return v != v;
  }

  public boolean isNaN() {
    return value != value;
  }

  public static native boolean isInfinite(double v);

  public boolean isInfinite() {
    return Double.isInfinite(this.value);
  }

  public String toString() {
    return Math.toString(value);
  }

  public static String toString(double d) {
    return Math.toString(d);
  }

  public static double parseDouble(String s) {
    return Math.doubleValue(s);
  }

  public static Double valueOf(String s) {
    return new Double(Math.doubleValue(s));
  }

  public static long doubleToLongBits(double value) {
    return Double.doubleToRawLongBits(value == value ? value : Double.NaN);
  }

  public static native long doubleToRawLongBits(double value);

  public static native double longBitsToDouble(long bits);

  public int compareTo(Double anotherDouble) {
    return compare(value, anotherDouble.value);
  }

  public static int compare(double one, double two) {
	if(isNaN(one)) {
		return isNaN(two) ? 0 : 1;
	} else if(isNaN(two)) {
		return -1;
	}
	if(one == 0.0 && two == 0.0){ 
      long oneL = doubleToRawLongBits(one);
      long twoL = doubleToRawLongBits(two);
      return oneL > twoL ? 1 : (twoL > oneL ? -1 : 0);
	}

    if(one < two) {
      return -1;
    }
    return one > two ? 1 : 0;
  }

  public int compareTo(Object obj) throws ClassCastException {
    return compareTo((Double)obj);
  }

}
