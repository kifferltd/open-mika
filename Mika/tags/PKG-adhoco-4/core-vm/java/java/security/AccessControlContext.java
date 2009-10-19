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
 * $Id: AccessControlContext.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
 */

package java.security;

import java.util.Iterator;
import java.util.HashSet;

/** An AccessControlContext is a set of ProtectionDomains.
 **@author ACUNIA NV
 **@version $Version:$
*/
public final class AccessControlContext {

  /**
   ** We store the ProtectionDomain's in a HashSet, ignoring duplicates.
   */
  HashSet domains;

  /** The first time the hashcode is requested we cache it for later use.
   */
  int hashcode;

  /** We also cache the last permission for which checkPermission() succeeded.
   */
  Permission o_k_perm;

  /** DomainCombiner
  */
  private DomainCombiner domainCombiner;
  /** The main public constructor.
   */
  public AccessControlContext (ProtectionDomain[] context) {
    domains = new HashSet(context.length*4/3+4);
    for (int i = 0; i < context.length; ++i) {
      domains.add(context[i]);
    }
  }

  public AccessControlContext (AccessControlContext context, DomainCombiner combiner) {
    Security.permissionCheck("createAccessControlContext");
    domains = (HashSet) context.domains.clone();
    domainCombiner = combiner;
  }

  /** Our special constructor.
   ** The second parameter is used to pre-populate the HashSet
   ** with the ProtectionDomains of an existing context.
   */
  AccessControlContext (ProtectionDomain[] context, AccessControlContext acc) {

    if (acc == null) {
      domains = new HashSet();
    }
    else {
      if(domainCombiner != null){
        domains = new HashSet();
        context = domainCombiner.combine(context, (ProtectionDomain[])acc.domains.toArray(new ProtectionDomain[acc.domains.size()]));
      }
      else {
        domains = (HashSet)acc.domains.clone();
      }
    }

    ProtectionDomain pd;
    for (int i = 0; i < context.length; ++i) {
      pd = context[i];
      if (!domains.contains(pd)) {
        domains.add(pd);
      }
    }
  }

  /** Another special constructor.
   ** The second and third parameters are used to pre-populate the HashSet
   ** with the ProtectionDomains of existing contexts.
   */
  AccessControlContext (ProtectionDomain[] context, AccessControlContext acc1, AccessControlContext acc2) {
    if (acc1 == null) {
      domains = new HashSet();
    }
    else {
      domains = (HashSet)acc1.domains.clone();
    }

    ProtectionDomain pd;

    if (acc2 != null) {
      Iterator acc2enum = acc2.domains.iterator();
      while (acc2enum.hasNext()) {
        pd = (ProtectionDomain)acc2enum.next();
        if (!domains.contains(pd)) {
          domains.add(pd);
        }
      }
    }

    for (int i = 0; i < context.length; ++i) {
      pd = context[i];
      if (!domains.contains(pd)) {
        domains.add(pd);
      }
    }
  }

  /** Check that the given Permission is implied by every domain in this context.
   */
  public void checkPermission (Permission perm) throws AccessControlException {
    if (perm.equals(o_k_perm)) {
      return;
    }

    Iterator pdenum = domains.iterator();
    while(pdenum.hasNext()) {
      ProtectionDomain pd = (ProtectionDomain)pdenum.next();
      if (!pd.implies(perm)) {
        throw new AccessControlException("Not implied by "+pd, perm);
      }
    }
    o_k_perm = perm;
  }


  /** Two AccessControlContexts are considered equal if they contain the same elements (not necessarily in the same order).
   */
  public boolean equals(Object o) {
     if(!(o instanceof AccessControlContext)){
      return false;
    }
    AccessControlContext that = (AccessControlContext)o;
    if (this.domains.size() != that.domains.size()) {

      return false;

    }

    Iterator pdenum = this.domains.iterator();
    while(pdenum.hasNext()) {
      ProtectionDomain pd = (ProtectionDomain)pdenum.next();
      if (!that.domains.contains(pd)) {

        return false;

      }
    }

    return true;
  }

  public DomainCombiner getDomainCombiner(){
    Security.permissionCheck("getDomainCombiner");
    return domainCombiner;
  }

  public int hashCode() {
    if (hashcode == 0) {
      Iterator pdenum = this.domains.iterator();
      while(pdenum.hasNext()) {
        ProtectionDomain pd = (ProtectionDomain)pdenum.next();
        hashcode ^= pd.hashCode();
      }
    }
    return hashcode;
  }

}
