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

package wonka.security;

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
