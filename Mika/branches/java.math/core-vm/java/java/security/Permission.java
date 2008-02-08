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
** $Id: Permission.java,v 1.2 2006/02/27 11:46:39 cvs Exp $
*/

package java.security;

import java.io.Serializable;

public abstract class Permission implements Guard, Serializable{

  private String name;

  public Permission (String name) {
    if (name==null) throw new NullPointerException();
//    if (name=="") throw new IllegalArgumentException();
    this.name = name;
  }

  public final String getName() {
    return this.name;
  }

  public void checkGuard(Object object) throws SecurityException {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      java.security.AccessController.checkPermission((Permission)object);
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkPermission((Permission)object);
      }
    }
  }

  public abstract boolean equals (Object obj);
  
  public abstract boolean implies (Permission perm);

  public abstract int hashCode();

  public abstract String getActions();

  public PermissionCollection newPermissionCollection() {
  // default semantics
    return null;
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append('(').append(this.getClass().getName()).append(' ').append(name);
    String actions = this.getActions();
    if (actions != null && !actions.equals("")) {
      buf.append(' ').append(actions);
    }           
    return buf.append(')').toString();
  }

}
