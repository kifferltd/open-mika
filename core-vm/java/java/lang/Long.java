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
** $Id: Long.java,v 1.2 2006/03/29 09:27:14 cvs Exp $
*/

package java.lang;

public final class Long extends Number implements Comparable{

  private static final long serialVersionUID = 4290774380558885855L;

  private final long value;

  public static final long MIN_VALUE = 0x8000000000000000L;
  public static final long MAX_VALUE = 0x7fffffffffffffffL;
  public static final Class TYPE = Long.getWrappedClass();

  public Long(long value) {
    this.value = value;
  }
  
  public Long(String s) throws NumberFormatException {
    this(parseLong(s));
  }

  public int hashCode() {
    return (int)(value^(value>>>32));
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
    return value;
  }

  public long longValue() {
    return value;
  }

  public double doubleValue() {
    return value;
  }

  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Long) {
      return value == ((Long)obj).longValue();
    }
    else return false;
  }

  public int compareTo(Long anotherLong) {
    long other = anotherLong.longValue();
    if (value == other) return 0;
    else if (value < other) return -1;
    else return 1;
  }

  public int compareTo(Object obj) throws ClassCastException {
    return compareTo((Long)obj);
  }

  public static Long decode(String s)
                   throws NumberFormatException
  {
    boolean negative = false;
    int index = 0;
    int radix = 10;

    if (s.startsWith("-")) {
      negative = true;
      index += 1;
    }

    if (s.substring(index).equals("0")) {

      return new Long(0);

    }

    if (s.substring(index).startsWith("0x")) {
      radix = 16;
      index += 2;
    }
    else if (s.substring(index).startsWith("#")) {
      radix = 16;
      index += 1;
    }
    else if (s.substring(index).startsWith("0")) {
      radix = 8;
      index += 1;
    }

    String magnitude = s.substring(index);
    if (magnitude.startsWith("-")) {
      throw new NumberFormatException("wrong position of sign");
    }

    return  new Long(parseLong((negative ? "-" : "")+ magnitude,radix));
  }

  public static Long getLong(String nm) {
    return getLong(nm,null);
  }

  public static Long getLong(String nm, long val) {
    Long result = getLong(nm,null);

    if (result == null) {
      result = new Long(val);
    }

    return result;
  }

  public static Long getLong(String nm, Long val) {
    String property = null;

    if (nm != null && nm.length() != 0) {
      property = System.getProperty(nm);
    }

    if (property == null) {

      return val;

    }

    try {
      if (property.startsWith("0x") && !property.startsWith("0x-")) {

        return valueOf(property.substring(2), 16);

      }
      if (property.startsWith("#") && !property.startsWith("#-")) {

        return valueOf(property.substring(1), 16);

      }
      if (property.startsWith("0")) {

        return valueOf(property.substring(1), 8);

      }

      return valueOf(property, 10);

    }
    catch (NumberFormatException e){

      return val;

    }

  }

  public static String toHexString(long j) {
    return Math.toStringUnsigned(j, 16);
  }

  public static String toOctalString(long j) {
    return Math.toStringUnsigned(j, 8);
  }

  public static String toBinaryString(long j) {
    return Math.toStringUnsigned(j, 2);
  }  
  
  public String toString() {
    return Math.toString(value, 10);
  }

  public static String toString(long j) {
    return Math.toString(j, 10);
  }

  public static String toString(long j, int radix) {
    return Math.toString(j, radix);
  }

  public static long parseLong(String s, int radix) 
    throws NumberFormatException
  {
    int index = 0;
    int digit;
    int length;
    boolean negative;
    long result = 0;

    if (s == null) {
      throw new NumberFormatException("cannot parse 'null'");
    }

    length = s.length();

    if (length==0) {
      throw new NumberFormatException("empty");
    }

    if (radix<Character.MIN_RADIX || radix>Character.MAX_RADIX) {
      throw new NumberFormatException("bad radix " + radix);
    }

    if (s.charAt(0)=='-') {
      if (length==1) {
        throw new NumberFormatException("just minus");
      }
      negative = true;
      ++index;
    }
    else {
      negative = false;
    }

    long div = MIN_VALUE / radix;
    long mod = -(MIN_VALUE % radix);

    while(index<length) {
      digit = Character.digit(s.charAt(index),radix);

      if (digit<0) {
        throw new NumberFormatException(s + " : bad digit " + s.charAt(index));
      }

      if (result <= div) {
        if ((result < div) || (digit > mod)) {
          throw new NumberFormatException(s + " : too big");
        }
      }
      result = result * radix - digit;
      ++index;
    }

    if(negative){
      return result;
    }
    if (result == MIN_VALUE){
      throw new NumberFormatException(s + " : too big");
    }
    return -result;
  }

  public static long parseLong(String s) throws NumberFormatException {
    return parseLong(s, 10); 
  }

  public static Long valueOf(String s, int radix) 
    throws NumberFormatException
  {
    return new Long(parseLong(s,radix));
  }

  public static Long valueOf(String s) throws NumberFormatException {
    return new Long(parseLong(s, 10));
  }

  private native static Class getWrappedClass();

}
