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

public final class Boolean implements java.io.Serializable {

  private static final long serialVersionUID = -3665804199014368530L;

  private final boolean value;

  public static final Boolean TRUE = new Boolean(true);
  public static final Boolean FALSE = new Boolean(false);
  public static final Class TYPE = getWrappedClass();

  public Boolean(boolean value) {
    this.value = value;
  }

  public Boolean(String s) {
    this(s!=null && s.equalsIgnoreCase("true"));
  }

  public String toString() {
    return toString(value);
  }

  public boolean equals(Object obj) {
    if ((obj != null) && (obj instanceof Boolean)) {
      return (value == ((Boolean) obj).booleanValue());
    }
    else {
      return false;
    }
  }

  public int hashCode() {
    return (value ? 1231 : 1237);
  }

  public boolean booleanValue() {
    return value;
  }

  public static Boolean valueOf(String s) {
    return (s!=null && s.equalsIgnoreCase("true")) ? TRUE : FALSE;
  }

  public static boolean getBoolean(String nm) {
    String property = null;

    if (nm != null && nm.length() != 0) {
      property = System.systemProperties.getProperty(nm);
    }

    if (property == null) {

      return false;

    }

    return property.equalsIgnoreCase("true");
  }

  public static Boolean valueOf(boolean b) {
    return b ? TRUE : FALSE;
  }

  public static String toString(boolean b) {
    return b ? "true" : "false";
  }

  private static native Class getWrappedClass();
}
