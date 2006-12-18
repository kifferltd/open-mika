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


/**
 * $Id: PrivilegedActionException.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
 */
package java.security;

import java.io.PrintWriter;
import java.io.PrintStream;

public class PrivilegedActionException extends Exception  {

  private Exception exception;

  public PrivilegedActionException(Exception exception){
        if (exception == null){
                throw new NullPointerException();
        }
        this.exception = exception;
  }

  public Exception getException(){
        return exception;
  }

  public void printStackTrace(){
        exception.printStackTrace();
  }

  public void printStackTrace(PrintStream ps){
        exception.printStackTrace(ps);

  }

  public void printStackTrace(PrintWriter pw) {
        exception.printStackTrace(pw);

  }

}
