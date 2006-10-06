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
 * $Id: DefaultPolicy.java,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
 */

package com.acunia.wonka.security;

import java.security.AccessController;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.util.HashMap;
import java.util.Iterator;

public final class DefaultPolicy extends Policy {

  private HashMap collections;

  static final CodeSource DEFAULT_CS = new CodeSource(null,null);

  public DefaultPolicy(){
    collections = (HashMap) AccessController.doPrivileged(new PolicyReader());
    if(collections == null){
      collections = new HashMap(3);
      collections.put(DEFAULT_CS, new PolicyPermissionCollection());
    }
  }

  public PermissionCollection getPermissions(CodeSource codesource) throws SecurityException {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      java.security.AccessController.checkPermission(new java.security.SecurityPermission("getPolicy"));
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkSecurityAccess("getPolicy");
      }
    }

    PolicyPermissionCollection pc = (PolicyPermissionCollection) collections.get(codesource);
    if(pc == null){
      //for now we don't make a special class to find a codesource which implies 'codesource'.
      //it will only make sense if we have a lot of different CodeSources in the HashMap ...
      Iterator it = collections.keySet().iterator();
      int size = collections.size();
      CodeSource implied = null;
      for(int i = 0 ; i < size ; i++){
        CodeSource current = (CodeSource) it.next();
        // System.out.println("" + i + ".  current: " + current + "  codesource: (" + codesource + ") " + current.implies(codesource) + " (" + implied + ") " + current.implies(implied) + " "+ (implied != null ? "" + implied.implies(current) : "<nada>"));
        if(current.implies(codesource) && (implied != null ? implied.implies(current) : true)) {
          implied = current;
        }
      }
      // System.out.println("--> implied : " + implied);
      pc = (PolicyPermissionCollection) collections.get((implied == null ? DEFAULT_CS : implied));
    }
    return (PermissionCollection)pc.clone();
  }

  public void refresh(){
    HashMap newCollections = (HashMap) AccessController.doPrivileged(new PolicyReader());
    if(newCollections != null){
      collections = newCollections;
    }
  }
}

