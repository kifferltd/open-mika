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
** $Id: UnresolvedPermission.java,v 1.2 2006/02/23 12:32:12 cvs Exp $
*/

package java.security;

import java.io.Serializable;

public final class UnresolvedPermission extends Permission implements Serializable{

  private static final long serialVersionUID = -4821973115467008846L;

  private String name;
  private String actions;
  private java.security.cert.Certificate[] certs;

  public UnresolvedPermission (String type, String name, String actions, java.security.cert.Certificate[] certs) {
    super(type);
    this.name=name; //doesn't need stric verification (null is allowed in AllPermission)
    this.actions = actions;//doesn't need stric verification (null is allowed --> AWTPermission, ...)
    this.certs = certs; //TODO verify for illegalArguments ... we allow null, but what of null elements in the array!
  }

  public boolean equals (Object obj) {
      if (!(obj instanceof UnresolvedPermission)) {
       	return false;
      }
      UnresolvedPermission up = (UnresolvedPermission)obj;
      int i,j;

      if (this.certs.length != up.certs.length) {
       	return false;
      }

      boolean found;
      if (this.certs == null) {
       		if (up.certs != null) {
       		 	return false;
       		}
      }
      else {
       if (up.certs == null) {
        	return false;
       }
       for (i = 0; i < this.certs.length; ++i) {
         found = false;
         for (j = 0; j < up.certs.length; ++j) {
          if (this.certs[i].equals(up.certs[j])) {
            found = true;
            break;
          }
         }
         if (!found) {
          return false;
         }
       }
      }
      return this.getName().equals(up.getName())
          && (this.name != null ? this.name.equals(up.name) : up.name == null)
          && (this.actions != null ?  this.actions.equals(up.actions) : up.actions == null);
  }
  
  public boolean implies (Permission perm) {
    return false;
  }

  public int hashCode() {
//    int hash = this.getName().hashCode() * 253 + type.hashCode() * 37 + actions.hashCode();
    int hash = this.getName().hashCode() ^ (name != null ? name.hashCode():0) ^ (actions != null ? actions.hashCode() : 0 );

    for (int i = 0; i < certs.length; ++i) {
      hash ^= certs[i].hashCode(); /*hash * 43 +*/
    }
    return hash;
  }

  public String getActions() {
    return "";
  }

/**
** Returns a string describing this UnresolvedPermission.
** The convention is to specify the class name, the permission name, and the actions, in
** the following format: '(unresolved "ClassName" "name" "actions")'.
*/
  public String toString(){
  	return "(unresolved "+getName()+" "+name+" "+actions+")";

  }

  public PermissionCollection newPermissionCollection() {
   	return new com.acunia.wonka.security.UnresolvedPermissionCollection();
  }
}
