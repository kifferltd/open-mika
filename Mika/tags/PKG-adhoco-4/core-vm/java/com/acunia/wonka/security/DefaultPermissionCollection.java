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
 * $Id: DefaultPermissionCollection.java,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
 */

package com.acunia.wonka.security;

import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * The class DefaultPermissionCollection is a catch-all for all subclasses
 * of Permission for which no special optimized collection is defined.
 */
public class DefaultPermissionCollection extends PermissionCollection {

  private Hashtable hashtable;

  private static final int INITIAL_TABSIZE=11;
  /**
   ** Constructor DefaultPermissionCollection() builds an empty Hashtable.
   */
  public DefaultPermissionCollection() {
    hashtable = new Hashtable(INITIAL_TABSIZE);
  }

  /**
   * Method add(Permission) adds the new permission to the hashtable.
   */
  public synchronized void add (Permission permission) throws SecurityException {
    if (super.isReadOnly()) throw new SecurityException("read-only");

    hashtable.put(permission.getName(),permission);

    // System.out.println("Hashtable after adding permission '"+permission+"': "+hashtable);
  }

  /**
   * Method 'implies' tests whether a given permission is implied by any
   * of the Permissions in this collection.  Since we know nothing about
   * the semantics of the Permissions, we iterate over the collection
   * until we find a match (or fail).
   */
  public synchronized boolean implies (Permission permission) {
    Enumeration e = hashtable.elements();

    while (e.hasMoreElements()) {
      Permission p = (Permission)(e.nextElement());
      if (p.implies(permission)) {

        return true;

      }
    }

    return false;

  }

  public Enumeration elements() {
    return hashtable.elements();
  }


}
