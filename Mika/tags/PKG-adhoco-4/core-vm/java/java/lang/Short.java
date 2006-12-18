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
** $Id: Short.java,v 1.2 2006/03/29 09:27:14 cvs Exp $
*/

package java.lang;

public final class Short extends Number implements Comparable {

  private static final long serialVersionUID = 7515723908773894738L;

  private final short value;

  public static final short MIN_VALUE = (short)0x8000;
  public static final short MAX_VALUE = (short)0x7fff;
  public static final Class TYPE = Short.getWrappedClass();

  public Short(short value) {
    this.value = value;
  }
  
  public Short(String s) throws NumberFormatException {
    this.value = parseShort(s);
  }

  public int hashCode() {
    // WRONG ??
    return value;
  }

  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Short) {
      return value == ((Short)obj).shortValue();
    }
    else return false;
  }

  public int compareTo(Short anotherShort) {
    short other = anotherShort.value;
    if (value == other) return 0;
    else if (value < other) return -1;
    else return 1;
  }
  
  public int compareTo(Object obj) throws ClassCastException {
    return compareTo((Short)obj);
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
    return value;
  }

  public byte byteValue() {
    return (byte)value;
  }


  /**
* Use String.valueOf(short);
*
*/	  
  public String toString(){
	  return String.valueOf(this.value);
  }

  public static short parseShort(String s, int radix) throws NumberFormatException
  {
    int i = Integer.parseInt(s, radix);

    if (i < MIN_VALUE || i > MAX_VALUE) throw new NumberFormatException();

    return (short) i;
  }

  public static short parseShort(String s) throws NumberFormatException {
    return Short.parseShort(s, 10); 
  }
/**
*  implemented
* --> returns a Short with value parseShort(s, radix)
*/
  public static Short valueOf(String s, int radix) throws NumberFormatException {
    return new Short(parseShort(s,radix));
	
  }

  public static Short valueOf(String s) throws NumberFormatException {
    return new Short(Short.parseShort(s, 10));
  }

  public static Short decode(String nm)
                   throws NumberFormatException
  {
    int skip = 0;
    int radix = 10;
    String str="";
    if (nm.startsWith("-")) {
      skip = 1;
      str = "-";
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
    return new Short(parseShort(str.concat(nm.substring(skip)),radix));

  }

  public static String toString(short i) {
    return String.valueOf(i);
  }

  private native static Class getWrappedClass();

}
