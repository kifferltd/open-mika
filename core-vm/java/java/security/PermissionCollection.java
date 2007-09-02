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


/**
 * $Id: PermissionCollection.java,v 1.2 2006/04/18 11:35:28 cvs Exp $
 */

package java.security;

import java.io.Serializable;
import java.util.Enumeration;

public abstract class PermissionCollection implements Serializable {

  private boolean readOnly;

  public PermissionCollection() {
  }

  public abstract void add (Permission permission);
  public abstract boolean implies (Permission permission);
  public abstract Enumeration elements();

  public void setReadOnly() {
    this.readOnly = true;
  }

  public boolean isReadOnly() {
    return this.readOnly;
  }

  public String toString() {
    StringBuffer answerBuffer = new StringBuffer(super.toString());
    answerBuffer.append(" (");
    answerBuffer.append(GetSystemProperty.LINE_SEPARATOR);
    for (Enumeration e = elements() ; e.hasMoreElements() ;) {
      answerBuffer.append(e.nextElement().toString());
      answerBuffer.append(GetSystemProperty.LINE_SEPARATOR);
    }
    answerBuffer.append(')');
    answerBuffer.append(GetSystemProperty.LINE_SEPARATOR);
    return answerBuffer.toString();
  }
}
