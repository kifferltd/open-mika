/**
 * Copyright  (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.
 * All rights reserved.
 *
 * $Id: StackTraceElement.java,v 1.3 2006/04/10 15:45:48 cvs Exp $
 */
package java.lang;

import java.io.Serializable;

/**
 * StackTraceElement:
 *
 * @author ruelens
 *
 * created: Mar 27, 2006
 */
public final class StackTraceElement implements Serializable {
  private static final long serialVersionUID = 6992337162326171013L;

  private String declaringClass;
  private String methodName;
  private String fileName;
  private int lineNumber;
  private transient boolean nativeM;
  
  StackTraceElement(){}
  
  public String getFileName() {
    return fileName;
  }
  
  public int getLineNumber() {
    return lineNumber;
  }

  public String getClassName() {
    return declaringClass;
  }
  
  public String getMethodName() {
    return methodName;
  }

  public boolean isNativeMethod() {
    return nativeM; 
  }  

  public String toString() {
     StringBuffer buf = new StringBuffer(declaringClass).append('.').append(methodName);
     if(nativeM) {
       buf.append("(Native Method)");
       
     } else {
       buf.append('(');
       buf.append(fileName != null ? fileName : "Unknown Source");
       if(lineNumber >= 0) {
         buf.append(':').append(lineNumber);
       }
       buf.append(')');
     }
     return buf.toString();
  }  
}
