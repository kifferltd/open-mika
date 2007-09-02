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
