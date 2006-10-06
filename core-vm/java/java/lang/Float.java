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
**************************************************************************/

/*
** $Id: Float.java,v 1.2 2006/03/27 08:19:05 cvs Exp $
*/

package java.lang;

public final class Float extends Number implements Comparable {

  private static final long serialVersionUID = -2671257302660747028L;

  private final float value;

  public static final float MIN_VALUE = 1.40129846432481707e-45f;
  public static final float MAX_VALUE = 3.40282346638528860e+38f;
  public static final float NEGATIVE_INFINITY = (float)(-1.0/0.0);
  public static final float POSITIVE_INFINITY = (float)(+1.0/0.0);
  public static final float NaN = (float)(0.0/0.0);
  public static final Class TYPE = Float.getWrappedClass();

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
    float other_float = anotherFloat.value;

    if (value < other_float) return -1;

    if (value > other_float) return 1;

    return 0;
  }

  public int compareTo(Object obj) throws ClassCastException {
    return compareTo((Float)obj);
  }

  public static int compare(float one, float two) {
    if(one < two) {
      return -1;
    }
    return one > two ? 1 : 0;    
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

  private native static Class getWrappedClass();

}
