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
