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


package java.security;

import wonka.vm.DeprecatedMethodError;
import java.util.Enumeration;

/**
 *
 * @version	$Id: IdentityScope.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
 * @deprecated
 */
public abstract class IdentityScope extends Identity {

  private static final long serialVersionUID = -2337346281189773310L;

  protected IdentityScope(){
  }

  public IdentityScope(String name){
    throw new DeprecatedMethodError("class java.security.IdentityScope is deprecated");
  }

  public IdentityScope(String name, IdentityScope scope) throws KeyManagementException{}

  public static IdentityScope getSystemScope(){
    throw new DeprecatedMethodError("class java.security.IdentityScope is deprecated");
  }

  protected static void setSystemScope(IdentityScope scope){
    throw new DeprecatedMethodError("class java.security.IdentityScope is deprecated");
  }

  public abstract int size();
  public abstract Identity getIdentity(String name);

  public Identity getIdentity(Principal principal){
    throw new DeprecatedMethodError("class java.security.IdentityScope is deprecated");
  }

  public abstract Identity getIdentity(PublicKey key);
  public abstract void addIdentity(Identity identity) throws KeyManagementException;
  public abstract void removeIdentity(Identity identity) throws KeyManagementException;
  public abstract Enumeration identities();

  public String toString(){
    throw new DeprecatedMethodError("class java.security.IdentityScope is deprecated");
  }
}
