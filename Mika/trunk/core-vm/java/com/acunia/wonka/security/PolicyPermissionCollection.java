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

/**
 * $Id: PolicyPermissionCollection.java,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
 */

package com.acunia.wonka.security;

import java.security.Permission;
import java.security.PermissionCollection;
import com.acunia.wonka.security.DefaultPermissionCollection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * The class Permissions represents a collection of PermissionCollection's
 * (a super-collection).  One PermissionObject is present for each subclass
 * of java.security.Permission which has at least one instance in the
 * super-collection.
 */
final class PolicyPermissionCollection extends PermissionCollection implements Cloneable {

  private Hashtable collections;
  private boolean allPermissions=false;

  private static final int INITIAL_TABSIZE=11;


  /**
   ** Constructor Permissions() builds an empty Hashtable.
   */
  PolicyPermissionCollection() {
    collections = new Hashtable(INITIAL_TABSIZE);
  }

  /**
   * Method add(Permission) adds the specified Permission to the
   * collection appropriate to the class of the Permission.  If no
   * collection yet exists for this class then one is created.
   */
  public synchronized void add (Permission permission) throws SecurityException {
    if (super.isReadOnly()) throw new SecurityException("read-only");

    Class pc = permission.getClass();
    PermissionCollection c = (PermissionCollection)collections.get(pc);

    if (c == null) {
      c = permission.newPermissionCollection();
      if (c == null) {
        c = new DefaultPermissionCollection();
      }
      collections.put(pc,c);

      if (pc.getName().equals("java.security.AllPermission")) {
         	allPermissions=true;
      }
    }

    c.add(permission);
  }

  /**
   * Method 'implies' tests whether a given permission is implied by any
   * of the Permissions in this collection.   We first identify the
   * PermissionCollection appropriate to this subclass of Permission,
   * and delegate to that.
   */
  public boolean implies (Permission permission) {
    Class pc = permission.getClass();
    PermissionCollection c = (PermissionCollection)collections.get(pc);
    if (c == null || allPermissions) {
      return allPermissions;
    }
    return c.implies(permission);
  }

  /**
  */
  public Enumeration elements() {
  	Vector v = new Vector();
  	Enumeration cols = collections.elements();
  	Enumeration pers;
  	while (cols.hasMoreElements()) {
  		pers = ((PermissionCollection)cols.nextElement()).elements();
  		while (pers.hasMoreElements()) {
  		 	v.add(pers.nextElement());
  		}
  	}
  	return v.elements();
  }

  protected Object clone(){
    try {
      PolicyPermissionCollection ppc = (PolicyPermissionCollection) super.clone();
      ppc.collections = (Hashtable) collections.clone();
      return ppc;
    }
    catch(CloneNotSupportedException cnse){
      return null;
    }
  }
}
