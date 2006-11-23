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
