/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2009 by /k/ Embedded Java Solutions.                *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

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
    if (wonka.vm.SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
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

