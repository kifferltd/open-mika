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
