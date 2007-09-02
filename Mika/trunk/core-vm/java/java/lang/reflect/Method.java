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
