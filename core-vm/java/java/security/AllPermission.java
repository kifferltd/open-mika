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
** $Id: AllPermission.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.security;

import java.io.Serializable;

public final class AllPermission extends Permission implements Serializable {

  private static final long serialVersionUID = -2916474571451318075L;

  private static final String aname = "<all permissions>";
  private static final String action = "<all actions>";

  public AllPermission() {
    super(aname);
  }
  public AllPermission(String name, String actions) {
    super(aname);
  }

  public boolean equals (Object o) {
    return (o instanceof AllPermission);
  }

  public int hashCode() {
    return 1;
  }

  public String getActions() {
    return action;
  }

  public PermissionCollection newPermissionCollection() {
    return new com.acunia.wonka.security.DefaultPermissionCollection();
  }

  public boolean implies (Permission p) {
    return true;
  }
}
