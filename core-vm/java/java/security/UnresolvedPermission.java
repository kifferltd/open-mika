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
