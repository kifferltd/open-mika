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
      property = System.systemProperties.getProperty(nm);
    }

    if (property == null) {

      return val;

    }

    try {
      return decode(property);
    }
    catch (NumberFormatException e){

      return val;

    }

  }

  private native static Class getWrappedClass();

}
