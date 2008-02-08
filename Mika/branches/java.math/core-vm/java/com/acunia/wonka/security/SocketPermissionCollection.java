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
