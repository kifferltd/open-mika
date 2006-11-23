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
** $Id: Byte.java,v 1.2 2006/03/29 09:27:14 cvs Exp $
*/

package java.lang;

public final class Byte extends Number implements Comparable {

  private static final long serialVersionUID = -7183698231559129828L;

  public static final byte MAX_VALUE = +127;
  public static final byte MIN_VALUE = -128;
  public static final Class TYPE = Byte.getWrappedClass();

  private final byte value;

  public Byte(byte value) {
    this.value = value;
  }
  
  public Byte(String str) throws NumberFormatException {
    this.value = Byte.parseByte(str);
  }
  
  public byte byteValue() {
    return this.value;
  }
  
  public static Byte decode(String nm)
                   throws NumberFormatException
  {
    int skip = 0;
    int radix = 10;
    String str="";
    // note: if nm == null, startswith will throw a NullPointerException
    if (nm.startsWith("-")) {
      skip = 1;
      str="-";
    }

    if (nm.substring(skip).startsWith("0x")) {
      radix = 16;
      skip += 2;
      if (nm.substring(skip).startsWith("-")) {
      	throw new NumberFormatException("wrong position of sign");
      }
    }
    else if (nm.substring(skip).startsWith("#")) {
      radix = 16;
      skip += 1;
      if (nm.substring(skip).startsWith("-")) {
      	throw new NumberFormatException("wrong position of sign");
      }
    }
    else if (nm.substring(skip).startsWith("0")) {
      radix = 8;
      skip += 1;
      if (nm.substring(skip).startsWith("-")) {
       	throw new NumberFormatException("wrong position of sign");
      }
    }
    return new Byte(parseByte(str.concat(nm.substring(skip)),radix));

  }

  
  public double doubleValue() {
    return this.value;
  }
  
  public boolean equals(Object obj) {
    return (obj instanceof Byte) && (this.value == ((Byte)obj).intValue());
  }
  
  public float floatValue() {
    return this.value;
  }
  
  public int hashCode() {
    return this.value;// same as Integer
  }
  
  public int intValue() {
    return this.value;
  }
  
  public long longValue() {
    return this.value;
  }

  public static byte parseByte(String s, int radix) throws NumberFormatException
  {
    long j = Long.parseLong(s, radix);

    if ((j < MIN_VALUE) || (j > MAX_VALUE)) {
      throw new NumberFormatException();
    }

    return (byte) j;
  }

  
  public static byte parseByte(String str) throws NumberFormatException
  {
    return parseByte(str,10);
  }

  
  public short shortValue() {
    return this.value;
  }

  public int compareTo (Byte anotherByte) {
    int answer = 0;
    if (this.byteValue() != anotherByte.byteValue()) {
      answer = ((this.value > anotherByte.byteValue()) ? 1 : -1 );
    }
    return answer;
  }

  public int compareTo (Object o) {
// I know, this can be done without test, but this is more readable
    if (o instanceof Byte) {
      return compareTo ((Byte)o);
    }
    else {
      throw new ClassCastException ("Can only compare Byte with Byte");
    }
  }
  
  public String toString() {
    return String.valueOf(this.value);
  }
  
  public static String toString(byte value) {
    return String.valueOf(value);
  }

  public static Byte valueOf(String str) throws NumberFormatException
  {
    return new Byte(str);
  }

  public static Byte valueOf(String str, int radix) throws NumberFormatException
  {
    return new Byte(parseByte(str,radix));
  }

  private native static Class getWrappedClass();

}
