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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: GetPolicy.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
*/

package java.security;

import wonka.vm.SecurityConfiguration;
import java.security.AccessController;

/*
** Package-visible class used to get the installed Policy  with full privileges.
*/

class GetPolicy implements java.security.PrivilegedAction {

  private Policy policy;

  /**
   ** Constructor needs no parameters..
   */
  public GetPolicy() {
    if (SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      AccessController.doPrivileged(this);
    }
  }

  /**
   ** This gets called by doPrivileged().
   */
  public Object run() {
    policy = Policy.getPolicy();

    return null;
  }

  /**
   ** Return the policy to the caller.
   */
  public Policy get() {
    return policy;
  }

}
