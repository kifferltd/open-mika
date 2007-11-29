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

package java.rmi.activation;

import java.io.*;

public class ActivationException extends Exception {

  private static final long serialVersionUID = -4320118837291406071L;

  public Throwable detail;

  public ActivationException() {
    super();
  }
  
  public ActivationException(String s) {
    super(s);
  }
  
  public ActivationException(String s, Throwable ex) {
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
