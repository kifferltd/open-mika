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

public final class Byte extends Number implements Comparable {

  private static final long serialVersionUID = -7183698231559129828L;

  public static final byte MAX_VALUE = +127;
  public static final byte MIN_VALUE = -128;
  public static final Class TYPE = getWrappedClass();

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
    return ByteCache.byteFactory(parseByte(str.concat(nm.substring(skip)),radix));

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
    return ByteCache.byteFactory(parseByte(str));
  }

  public static Byte valueOf(String str, int radix) throws NumberFormatException
  {
    return ByteCache.byteFactory(parseByte(str,radix));
  }

  /**
  * We cache a Byte instance for every possible value of byte.
  */
  private static class ByteCache {

    static Byte[] cache;

    static {
      cache = new Byte[256];
      for (int i = 0; i < 256; ++i) {
        cache[i] = new Byte((byte)i);
      }
    }

    static Byte byteFactory(byte value) {
      return cache[value & 0xff];
    }
  }

  private static native Class getWrappedClass();
}
