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
** $Id: ClassNotFoundException.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.lang;

import java.io.PrintWriter;
import java.io.PrintStream;

public class ClassNotFoundException extends Exception {

  private static final long serialVersionUID = 9176873029745254542L;

  private Throwable ex;

  public ClassNotFoundException() {
  }

  public ClassNotFoundException(String s) {
    super(s);
  }

  public ClassNotFoundException(String s, Throwable t) {
    super(s);
    ex = t;
  }

  public Throwable getException() {
    return ex;
  }

  public void printStackTrace(){
   	if (ex == null){
   	 	super.printStackTrace();
   	} else {
   	  ex.printStackTrace();
   	}
  }

  public void printStackTrace(PrintStream ps){
   	if (ex == null){
   	 	super.printStackTrace(ps);
   	} else {
   	  ex.printStackTrace(ps);
   	}
  }

  public void printStackTrace(PrintWriter pw){
   	if (ex == null){
   	 	super.printStackTrace(pw);
   	} else {
   	  ex.printStackTrace(pw);
   	}
  }


}

