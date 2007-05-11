/**************************************************************************
* Copyright  (c) 2001, 2002 by Acunia N.V. All rights reserved.           *
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
*   Philips Site 5, bus 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: Field.java,v 1.2 2006/03/29 09:27:14 cvs Exp $
*/

package java.lang.reflect;

public final class Field extends AccessibleObject implements Member {

  private Field() {}
  
  public native Class getDeclaringClass();
  
  public native String getName();
  
  public native int getModifiers();
  
  public native Class getType();
  
  public native boolean equals(Object obj);
  
  public int hashCode() {
    return getName().hashCode() ^ getDeclaringClass().getName().hashCode();
  }
  
  public String toString() {
    StringBuffer result = new StringBuffer();
    int mods = getModifiers();

    if ((mods & Modifier.PUBLIC) != 0) {
      result.append("public ");
    }

    if ((mods & Modifier.PROTECTED) != 0) {
      result.append("protected ");
    }

    if ((mods & Modifier.PRIVATE) != 0) {
      result.append("private ");
    }

    if ((mods & Modifier.STATIC) != 0) {
      result.append("static ");
    }

    if ((mods & Modifier.FINAL) != 0) {
      result.append("final ");
    }

    if ((mods & Modifier.VOLATILE) != 0) {
      result.append("volatile ");
    }
  
    if ((mods & Modifier.TRANSIENT) != 0) {
      result.append("transient ");
    }

    result.append(getType().getName());
    result.append(' ');
    result.append(getDeclaringClass().getName());
    result.append('.');
    result.append(getName());

    return new String(result);
  }
  
  public native Object get(Object obj) 
    throws NullPointerException, IllegalArgumentException, IllegalAccessException;
    
  public native boolean getBoolean(Object obj)
    throws NullPointerException, IllegalArgumentException, IllegalAccessException;

  public native byte getByte(Object obj)
    throws NullPointerException, IllegalArgumentException, IllegalAccessException;

  public native char getChar(Object obj)
    throws NullPointerException, IllegalArgumentException, IllegalAccessException;

  public native short getShort(Object obj)
    throws NullPointerException, IllegalArgumentException, IllegalAccessException;

  public native int getInt(Object obj)
    throws NullPointerException, IllegalArgumentException, IllegalAccessException;

  public native float getFloat(Object obj)
    throws NullPointerException, IllegalArgumentException, IllegalAccessException;

  public native double getDouble(Object obj)
    throws NullPointerException, IllegalArgumentException, IllegalAccessException;

  public native long getLong(Object obj)
    throws NullPointerException, IllegalArgumentException, IllegalAccessException;

  public native void set(Object obj, Object value) 
    throws NullPointerException, IllegalArgumentException, IllegalAccessException;
    
  public native void setBoolean(Object obj, boolean z)
    throws NullPointerException, IllegalArgumentException, IllegalAccessException;

  public native void setByte(Object obj, byte b)
    throws NullPointerException, IllegalArgumentException, IllegalAccessException;

  public native void setChar(Object obj, char c)
    throws NullPointerException, IllegalArgumentException, IllegalAccessException;

  public native void setShort(Object obj, short s)
    throws NullPointerException, IllegalArgumentException, IllegalAccessException;

  public native void setInt(Object obj, int i)
    throws NullPointerException, IllegalArgumentException, IllegalAccessException;

  public native void setFloat(Object obj, float f)
    throws NullPointerException, IllegalArgumentException, IllegalAccessException;

  public native void setDouble(Object obj, double d)
    throws NullPointerException, IllegalArgumentException, IllegalAccessException;

  public native void setLong(Object obj, long l)
    throws NullPointerException, IllegalArgumentException, IllegalAccessException;

}
