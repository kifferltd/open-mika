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
