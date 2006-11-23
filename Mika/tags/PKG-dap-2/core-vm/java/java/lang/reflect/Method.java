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
** $Id: Method.java,v 1.2 2006/03/29 09:27:14 cvs Exp $
*/

package java.lang.reflect;

public final class Method extends AccessibleObject implements Member {

  private Method() {}
  
  public native Class getDeclaringClass();
  
  public native String getName();
  
  public native int getModifiers();
  
  public native Class getReturnType();
  
  public native Class[] getParameterTypes();
  
  public native Class[] getExceptionTypes();
  
  public native boolean equals(Object obj);
  
  public String toString(){
    StringBuffer result = new StringBuffer(100);
    result.append(Modifier.toString(getModifiers()));
    if (result.length() > 0) {
      result.append(' ');
    }

    result.append(getClassName(getReturnType()));
    result.append(" ");
    result.append(getClassName(getDeclaringClass()));
    result.append(".");
    result.append(getName());
    result.append("(");
    Class[] classes = getParameterTypes();
    int i;
    int l = classes.length;
    for(i=0; i < l ; i++){
     	result.append(getClassName(classes[i]));
     	result.append(",");
    }
    if ( l !=0 ) result.setLength(result.length()-1);
    result.append(")");
    classes = getExceptionTypes();
    l = classes.length;
    if ( l != 0 ) {
      result.append(" throws ");
      for(i=0; i < l ; i++){
     	result.append(getClassName(classes[i]));
       	result.append(",");
      }
      result.setLength(result.length()-1);
    }

    return new String(result);
  }

  public int hashCode() {
    return getName().hashCode() ^ getDeclaringClass().getName().hashCode();
  }
  
  public Object invoke(Object obj, Object args[])
    throws NullPointerException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
  {
    int modifiers = this.getModifiers();

    if (! Modifier.isStatic(modifiers)) {
      if (obj == null) {
        throw new NullPointerException();
      }
      else if (! this.getDeclaringClass().isAssignableFrom(obj.getClass())) {
        throw new IllegalArgumentException();
      }
    }

    if (args == null) {
      args = new Object[0];
    }

    return invoke0(obj, args);
  }

  private native Object invoke0(Object obj, Object args[])
    throws NullPointerException, IllegalArgumentException, IllegalAccessException, InvocationTargetException;
    
  static String getClassName(Class c) {
        if (c.isArray()) {
       		Class nc = c.getComponentType();
       		if (nc.isArray()){
       			return getClassName(nc)+"[]";
        	}
        	return nc.getName()+"[]";
        }
        else return c.getName();
  }

}
