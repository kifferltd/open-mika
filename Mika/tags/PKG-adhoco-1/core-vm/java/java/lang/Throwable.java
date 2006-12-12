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
*                                                                         *
* Additions copyright (C) 2005 Chris Gray, /k/ Embedded Java Solutions.   *
* Permission is hereby granted to distribute these changes under the      *
* terms of the Wonka Public Licence.                                      *
**************************************************************************/


/*
** $Id: Throwable.java,v 1.7 2006/10/04 14:24:15 cvsroot Exp $
*/

package java.lang;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;

public class Throwable implements Serializable {

  /*
  ** Note: this class is initialized "by hand" before the VM is fully
  ** initialized.  Consequently it must not have a static initializer.
  ** (It can have static variables, and even constant initial values
  ** for those variables, but nothing fancier and certainly no static{}
  ** clause.)
  */

  private static final long serialVersionUID = -3042686055658047285L;
  
  private void writeObject(ObjectOutputStream s) throws IOException {
    getStackTrace();  
  }
  
  private String detailMessage;
  Throwable cause;
  private StackTraceElement[] stackTrace;

  public Throwable() { }

  public Throwable(String message) {
    detailMessage = message;
  }

  public Throwable(Throwable t) {
    detailMessage = t == null ? null : t.toString();    
    cause = t;
  }

  public Throwable(String message, Throwable t) {
    detailMessage = message;
    cause = t;
  }

  public String getMessage() {
    return detailMessage;
  }

  public String getLocalizedMessage() {
    return detailMessage;
  }

  public String toString() {
    String msg = getMessage();
    StringBuffer buf = new StringBuffer(this.getClass().getName());
    return (msg == null ? buf : buf.append(": ").append(msg)).toString();
  }

  public void printStackTrace() {
    printStackTrace(System.err);
  }

  public void setStackTrace(StackTraceElement[] newstack) {
    if(newstack == null) {
      throw new NullPointerException();
    }
    stackTrace = newstack;
  }
  
  public StackTraceElement[] getStackTrace() {
    if(stackTrace == null) {
      synchronized(this) {
        if(stackTrace == null) {
          int len = getStackTraceLength();
          StackTraceElement[] stack = new StackTraceElement[len];
          for(int i=0; i < len; i++) {
            StackTraceElement element = new StackTraceElement();
            nextStackTrace(element);
            stack[i] = element;
          }
          stackTrace = stack;
        }
      }      
    }
    return this.stackTrace;    
  }
  
  private native void nextStackTrace(StackTraceElement element);
  private native int getStackTraceLength();

  public void printStackTrace(PrintStream stream) {
    if(stream == null) {
      try {
         stream = new PrintStream(new FileOutputStream(toString()));
      } catch (Exception e) {}     
    }
    byte[] bytes = new byte[] {'\t','a','t', ' '};
    stream.println(this.toString());
    StackTraceElement[] stack = getStackTrace();
    int l = stack.length;
    try {
      for(int i=0; i < l; i++) {
        stream.write(bytes);
        stream.println(stack[i]);    
      }
      Throwable t = getCause();
      StackTraceElement top = l > 0 ? stack[0] : null;
      while(t != null) {
        stream.print("Caused by: ");
        stream.println(t.toString());
        stack = t.getStackTrace();
        l = stack.length;
        for (int i=0; i < l ; i++) {
          StackTraceElement ste = stack[i];
          stream.write(bytes);
          stream.println(ste);    
          if(ste.equals(top)) {
            stream.println("... "+(l-i-1)+" more");
            break;
          }
        }
        top = l > 0 ? stack[0] : top;
        t = t.getCause();
      }
    } catch (IOException e) {}
  }

  public void printStackTrace(PrintWriter stream) {
    char[] chars = new char[] { '\t', 'a', 't', ' ' };
    stream.println(this.toString());
    StackTraceElement[] stack = getStackTrace();
    int l = stack.length;
    for (int i = 0; i < l; i++) {
      stream.write(chars);
      stream.println(stack[i]);
    }
    Throwable t = this.cause;
    StackTraceElement top = l > 0 ? stack[0] : null;
    while (t != null) {
      stream.print("Caused by: ");
      stream.println(t.toString());
      stack = t.getStackTrace();
      l = stack.length;
      for (int i = 0; i < l; i++) {
        StackTraceElement ste = stack[i];
        stream.write(chars);
        stream.println(ste);
        if (ste.equals(top)) {
          stream.println("... " + (l - i - 1) + " more");
          break;
        }
      }
      top = l > 0 ? stack[0] : top;
      t = t.getCause();
    }
  }

  public Throwable getCause() {
    return cause;
  }
  
  public Throwable initCause(Throwable cause) {
    if (this.cause != null) {
      throw new IllegalStateException("cause was already set "+cause);
    }
    if (cause == this) {
      throw new IllegalArgumentException("cause cannot be 'this'");
    }
    this.cause = cause;
    return this;
  }
  
  public synchronized native Throwable fillInStackTrace();
}
