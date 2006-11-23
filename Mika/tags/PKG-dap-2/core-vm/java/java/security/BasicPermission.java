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
** $Id: BasicPermission.java,v 1.3 2006/02/23 12:32:12 cvs Exp $
*/

package java.security;

import java.io.Serializable;

public abstract class BasicPermission extends Permission implements Serializable {

  public BasicPermission(String name) {
    super(name);
    if (name.equals("")) {
      throw new IllegalArgumentException();
    }
  }

  public BasicPermission(String name, String actions) {
    this(name);
  }

  public boolean equals (Object o) {
    if (o == null)  {
    	return false;
    }
    if (this.getClass().equals(o.getClass())) {
      Permission p = (Permission)o;
      return this.getName().equals(p.getName());
    }
    else return false;
  }

  public int hashCode() {
    return getName().hashCode();
  }

  public String getActions() {
    return "";
  }

  public PermissionCollection newPermissionCollection() {
    return new com.acunia.wonka.security.BasicPermissionCollection();
  }

  public boolean implies (Permission p) {
    if (this.getClass().equals(p.getClass())) {
      String ourname = this.getName();
      if (ourname.equals("*")) {
         return true;
      }
      if (ourname.endsWith(".*")) {
        String name = p.getName();
        return name.length() >= ourname.length() && 
        name.startsWith(ourname.substring(0, ourname.length() - 1));

       }
       return ourname.equals(p.getName());
    }
    return false;
  }
}
