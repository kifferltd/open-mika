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
