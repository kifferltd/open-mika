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

/**
 *
 * @version	$Id: Signer.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
 * @deprecated
 */
public abstract class Signer extends Identity {

  private static final long serialVersionUID = -1763464102261361480L;

  protected Signer(){}

  public Signer(String name){
    throw new DeprecatedMethodError("class java.security.Signer is deprecated");
  }

  public Signer(String name, IdentityScope scope) throws KeyManagementException{
    throw new DeprecatedMethodError("class java.security.Signer is deprecated");
  }

  public PrivateKey getPrivateKey(){
    throw new DeprecatedMethodError("class java.security.Signer is deprecated");
  }

  public final void setKeyPair(KeyPair pair) throws InvalidParameterException, KeyException {
    throw new DeprecatedMethodError("class java.security.Signer is deprecated");
  }

  public String toString(){
    throw new DeprecatedMethodError("class java.security.Signer is deprecated");
  }
}
