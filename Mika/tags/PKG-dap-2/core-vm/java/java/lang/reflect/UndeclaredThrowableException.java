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
** $Id: UndeclaredThrowableException.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/


package java.lang.reflect;

import java.io.PrintWriter;
import java.io.PrintStream;

public class UndeclaredThrowableException extends RuntimeException {

  private Throwable undeclaredThrowable;

  public UndeclaredThrowableException(Throwable undeclaredThrowable) {
    super();
    this.undeclaredThrowable = undeclaredThrowable;
  }

  public UndeclaredThrowableException(Throwable undeclaredThrowable, String detail) {
    super(detail);
    this.undeclaredThrowable = undeclaredThrowable;
  }

  public Throwable getUndeclaredThrowable() {
    return undeclaredThrowable;
  }

  public void printStackTrace(){
   	if (undeclaredThrowable == null){
   	 	super.printStackTrace();
   	} else {
   	  	System.err.println("*** UndeclaredThrowableException: wrapping '"+undeclaredThrowable+"' "
   	  	                   +"printing out stack trace:");
   	  	undeclaredThrowable.printStackTrace();
   	}
  }
  public void printStackTrace(PrintStream ps){
   	if (undeclaredThrowable == null){
   	 	super.printStackTrace(ps);
   	} else {
   	  	ps.println("*** UndeclaredThrowableException: wrapping '"+undeclaredThrowable+"' "
   	  	           +"printing out stack trace:");
   	  	undeclaredThrowable.printStackTrace(ps);
   	}
  }

  public void printStackTrace(PrintWriter pw){
   	if (undeclaredThrowable == null){
   	 	super.printStackTrace(pw);
   	} else {
   	  	pw.println("*** UndeclaredThrowableException: wrapping '"+undeclaredThrowable+"' "
   	  	           +"printing out stack trace:");
   	  	undeclaredThrowable.printStackTrace(pw);
   	}
  }
}
