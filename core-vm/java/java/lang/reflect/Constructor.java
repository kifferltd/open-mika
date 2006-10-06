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
** $Id: Constructor.java,v 1.2 2006/03/29 09:27:14 cvs Exp $
*/

package java.lang.reflect;

public final class Constructor extends AccessibleObject implements Member {

  public native Class getDeclaringClass();

  public native String getName();
	
  public native int getModifiers();

  public native Class[] getParameterTypes();
  	
  public native Class[] getExceptionTypes();

  public boolean equals(Object obj) {
      return (obj instanceof Constructor)
        && (this.toString().equals(obj.toString()));
  }

  private Constructor() {}
  
  public int hashCode() {
    return getName().hashCode();
  }
  
  public String toString() {
    StringBuffer result = new StringBuffer(100);
    result.append(Modifier.toString(getModifiers()));

    if (result.length() > 0) {
      result.append(' ');
    }

    result.append(getName());

    Class[] args = getParameterTypes();
    result.append('(');
    for (int i = 0; i < args.length; ++i) {
      if (i > 0) {
        result.append(',');
      }
      result.append(Method.getClassName(args[i]));
    }
    result.append(')');
    args = getExceptionTypes();
    int l = args.length;
    if ( l != 0 ) {
      result.append(" throws ");
      for(int i=0; i < l ; i++){
     	result.append(Method.getClassName(args[i]));
       	result.append(",");
      }
      result.setLength(result.length()-1);
    }
    return result.toString();
  }
  
  public Object newInstance(Object initargs[])
    throws InstantiationException, IllegalArgumentException, 
    IllegalAccessException, InvocationTargetException
  {
    if (initargs == null) {
      //throw new IllegalArgumentException();
      initargs = new Object[0];
    }

    if (Modifier.isAbstract(this.getModifiers())) {
      throw new InstantiationException();
    }

    return newInstance0(initargs);
  }
    
  private native Object newInstance0(Object initargs[])
    throws InstantiationException, IllegalArgumentException, 
    IllegalAccessException, InvocationTargetException;
    
}
