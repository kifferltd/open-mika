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
** $Id: ExceptionInInitializerError.java,v 1.2 2006/10/04 14:24:15 cvsroot Exp $
*/

package java.lang;

import java.io.PrintWriter;
import java.io.PrintStream;

public class ExceptionInInitializerError extends LinkageError {

  private static final long serialVersionUID = 1521711792217232256L;

  public ExceptionInInitializerError() {
  }

  public ExceptionInInitializerError(String s) {
    super(s);
  }

  public ExceptionInInitializerError(Throwable t) {
    super (t == null ? null : t.toString());
    cause = t;
  }

  public Throwable getException() {

    return cause;

  }
}

