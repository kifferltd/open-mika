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
 * $Id: Policy.java,v 1.2 2006/04/18 11:35:28 cvs Exp $
 */
package java.security;

import com.acunia.wonka.security.DefaultPolicy;

public abstract class Policy {

  private static Policy thePolicy;

  static {
    try {
      String policy = Security.securityProps.getProperty("policy.provider");
      thePolicy = policy == null ? new DefaultPolicy() :
        (Policy)Class.forName(policy).newInstance();
    }
    catch(Exception e){
      thePolicy = new DefaultPolicy();
    }
  }

  public Policy() { }

  private static void permissionCheck(String permission) {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      java.security.AccessController.checkPermission(new SecurityPermission(permission));
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkSecurityAccess(permission);
      }
    }
  }

  public static Policy getPolicy() {
    permissionCheck("getPolicy");
    return thePolicy;
  }

  public static void setPolicy (Policy policy) {
    if(policy == null){
      throw new SecurityException("cannot intall a 'null' policy");
    }
    permissionCheck("setPolicy");
    thePolicy = policy;
  }

  public boolean implies(ProtectionDomain domain, Permission permission) {
    return getPermissions(domain).implies(permission);
  }
  
  public PermissionCollection getPermissions(ProtectionDomain domain) {
    if(domain.loader != null) {
      //TODO use loader && principals to generate a permissioncollection
    }
    return domain.getPermissions();
  }
  
  public abstract PermissionCollection getPermissions(CodeSource codesource);

  public abstract void refresh();
}
