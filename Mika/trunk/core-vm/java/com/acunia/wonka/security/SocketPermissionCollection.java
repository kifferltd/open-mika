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
 * $Id: SocketPermissionCollection.java,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
 */

package com.acunia.wonka.security;

import java.security.Permission;
import java.util.Hashtable;
import java.util.Vector;

public class SocketPermissionCollection extends DefaultPermissionCollection {

  private final static long LIFETIME_MILLIS = 300000;

  private Hashtable cache;
  private Vector ageing_fifo;
  private long next_deletion;

  private static final int INITIAL_TABSIZE=11;
  /**
   ** Constructor SocketPermissionCollection() builds an empty Hashtable.
   */
  public SocketPermissionCollection() {
    cache = new Hashtable(INITIAL_TABSIZE);
    ageing_fifo = new Vector();
    next_deletion = System.currentTimeMillis();
  }


  /**
   * Method 'implies' tests whether a given permission is implied by any
   * of the Permissions in this collection.  Since we know nothing about
   * the semantics of the Permissions, we iterate over the collection
   * until we find a match (or fail).
   */
  public synchronized boolean implies (Permission permission) {
    performPreAgeing();

    Boolean cached_result = (Boolean)cache.get(permission);
    if (cached_result != null) {

      return cached_result.booleanValue();

    }

    ageing_fifo.addElement(permission);
    performPostAgeing();

    if (super.implies(permission)) {
      cache.put(permission, new Boolean(true));

      return true;

    }

    cache.put(permission, new Boolean(false));

    return false;
  }

  /**
   ** A very rough-and-ready ageing algorithm.
   */
  private void performPreAgeing() {
    long now = System.currentTimeMillis();

    while (ageing_fifo.size() > 0 && now > next_deletion) {
      cache.remove(ageing_fifo.elementAt(0));
      ageing_fifo.removeElementAt(0);
      if (ageing_fifo.size() == 0) {
        next_deletion = Long.MAX_VALUE;
      }
      else {
        next_deletion += LIFETIME_MILLIS / ageing_fifo.size();
      }
    }
  }

  private void performPostAgeing() {
    if (next_deletion == Long.MAX_VALUE) {
      next_deletion = System.currentTimeMillis() + LIFETIME_MILLIS;
    }
    else {
      int n = ageing_fifo.size();
      if ( n > 1) {
        next_deletion -= LIFETIME_MILLIS/n - LIFETIME_MILLIS/(n-1);
      }
    }
  }

}
