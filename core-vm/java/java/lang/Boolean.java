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
** $Id: Boolean.java,v 1.4 2006/03/27 08:19:05 cvs Exp $
*/

package java.lang;

public final class Boolean implements java.io.Serializable {

  private static final long serialVersionUID = -3665804199014368530L;

  private final boolean value;

  public static final Boolean TRUE = new Boolean(true);
  public static final Boolean FALSE = new Boolean(false);
  public static final Class TYPE = Boolean.getWrappedClass();

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
    return new Boolean(s!=null && s.equalsIgnoreCase("true"));
  }

  public static boolean getBoolean(String nm) {
    String property = null;

    if (nm != null && nm.length() != 0) {
      property = System.getProperty(nm);
    }

    if (property == null) {

      return false;

    }

    return property.equalsIgnoreCase("true");
  }

  private native static Class getWrappedClass();

  public static Boolean valueOf(boolean b) {
    return b ? TRUE : FALSE;
  }

  public static String toString(boolean b) {
    return b ? "true" : "false";
  }
}
