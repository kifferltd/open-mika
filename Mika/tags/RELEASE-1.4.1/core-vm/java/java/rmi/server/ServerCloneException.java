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

package java.rmi.server;

import java.io.*;

public class ServerCloneException extends java.lang.CloneNotSupportedException {

  private static final long serialVersionUID = 6617456357664815945L;

  public Exception detail;

  public ServerCloneException(String s) {
    super(s);
  }
  
  public ServerCloneException(String s, Exception ex) {
    super(s);
    detail = ex;
  }

  public String getMessage() {
    String message = super.getMessage();
    if(detail == null){
      return message;
    }
    String detailMessage = detail.getMessage();
    if(message == null){
      return detailMessage;
    }
    if(detailMessage == null){
      return message;
    }
    return message + "\nWrapping message " + detailMessage;
  }
  
  public void printStackTrace() {
    if(detail == null){
      super.printStackTrace();
    }
    else {
      detail.printStackTrace();
    }
  }
  
  public void printStackTrace(PrintStream ps) {
    if(detail == null){
      super.printStackTrace(ps);
    }
    else {
      detail.printStackTrace(ps);
    }
  }
  
  public void printStackTrace(PrintWriter pw) {
    if(detail == null){
      super.printStackTrace(pw);
    }
    else {
      detail.printStackTrace(pw);
    }
  }

}

