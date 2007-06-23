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
** $Id: Integer.java,v 1.2 2006/03/29 09:27:14 cvs Exp $
*/

package java.lang;

public final class Integer extends Number implements Comparable {
  private static final long serialVersionUID = 1360826667806852920L;

  private final int value;

  public static final int MIN_VALUE = 0x80000000;
  public static final int MAX_VALUE = 0x7fffffff;
  public static final Class TYPE = Integer.getWrappedClass();

  public Integer(int value) {
    this.value = value;
  }
  
  public Integer(String s) throws NumberFormatException {
    value = parseInt(s, 10);
  }

  public int hashCode() {
    return value;
  }

  public boolean equals(Object obj) {
    if (obj instanceof Integer) {
      return value == ((Integer)obj).value;
    }
    else return false;
  }

  public int compareTo(Integer anotherInteger) {
    int other = anotherInteger.value;
    if (value == other) return 0;
    else if (value < other) return -1;
    else return 1;
  }
  
  public int compareTo(Object obj) throws ClassCastException {
    return compareTo((Integer)obj);
  }

  public int intValue() {
    return value;
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

  public short shortValue() {
    return (short)value;
  }

  public byte byteValue() {
    return (byte)value;
  }

  public static String toHexString(int i) {
    return Math.toStringUnsigned(i, 16);
  }

  public static String toOctalString(int i) {
    return Math.toStringUnsigned(i, 8);
  }

  public static String toBinaryString(int i) {
    return Math.toStringUnsigned(i, 2);
  }  
  
  public String toString() {
    return Math.toString(value, 10);
  }

  public static int parseInt(String s, int radix) 
    throws NumberFormatException
  {
    long l = Long.parseLong(s, radix);

    if (l<MIN_VALUE || l>MAX_VALUE) {
      throw new NumberFormatException();
    }

    return (int) l;
  }

  public static int parseInt(String s) throws NumberFormatException {
    return Integer.parseInt(s, 10); 
  }

  public static Integer valueOf(String s, int radix) 
    throws NumberFormatException
  {
    return new Integer(Integer.parseInt(s,radix));
  }

  public static Integer valueOf(String s) throws NumberFormatException {
    return new Integer(Integer.parseInt(s, 10));
  }

  public static Integer decode(String s)
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

      return new Integer(0);

    }

    if (s.substring(index).startsWith("0x") || s.substring(index).startsWith("0X")) {
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

    return  new Integer(parseInt((negative ? "-" : "" )+ magnitude,radix));
  }

  public static String toString(int i) {
    return Math.toString(i, 10);
  }

  public static String toString(int i, int radix) {
    return Math.toString(i, radix);
  }

  public static Integer getInteger(String nm) {
    return getInteger(nm,null);
  }

  public static Integer getInteger(String nm, int val) {
    Integer result = getInteger(nm,null);

    if (result == null) {
      result = new Integer(val);
    }

    return result;
  }

  public static Integer getInteger(String nm, Integer val) {
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

  private native static Class getWrappedClass();

}
