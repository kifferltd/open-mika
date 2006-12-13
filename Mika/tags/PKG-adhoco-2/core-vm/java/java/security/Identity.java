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
 * @version	$Id: Identity.java,v 1.2 2006/04/18 11:35:28 cvs Exp $
 * @deprecated
 */
public abstract class Identity implements Principal, java.io.Serializable {

  private static final long serialVersionUID = 3609922007826600659L;

  protected Identity(){}

  public Identity(String name, IdentityScope scope) throws KeyManagementException{
    throw new DeprecatedMethodError("class java.security.Identity is deprecated");
  }

  public Identity(String name){
    throw new DeprecatedMethodError("class java.security.Identity is deprecated");
  }

  public final String getName(){
    throw new DeprecatedMethodError("class java.security.Identity is deprecated");
  }

  public final IdentityScope getScope(){
    throw new DeprecatedMethodError("class java.security.Identity is deprecated");
  }

  public PublicKey getPublicKey(){
    throw new DeprecatedMethodError("class java.security.Identity is deprecated");
  }

  public void setPublicKey(PublicKey key) throws KeyManagementException {
    throw new DeprecatedMethodError("class java.security.Identity is deprecated");
  }

  public void setInfo(String info){
    throw new DeprecatedMethodError("class java.security.Identity is deprecated");
  }

  public String getInfo(){
    throw new DeprecatedMethodError("class java.security.Identity is deprecated");
  }

  public void addCertificate(Certificate certificate) throws KeyManagementException {
    throw new DeprecatedMethodError("class java.security.Identity is deprecated");
  }

  public void removeCertificate(Certificate certificate) throws KeyManagementException {
    throw new DeprecatedMethodError("class java.security.Identity is deprecated");
  }

  public Certificate[] certificates(){
    throw new DeprecatedMethodError("class java.security.Identity is deprecated");
  }


  public final boolean equals(Object identity){
    throw new DeprecatedMethodError("class java.security.Identity is deprecated");
  }

  protected boolean identityEquals(Identity identity){
    throw new DeprecatedMethodError("class java.security.Identity is deprecated");
  }

  public String toString(){
    throw new DeprecatedMethodError("class java.security.Identity is deprecated");
  }

  public String toString(boolean detailed){
    throw new DeprecatedMethodError("class java.security.Identity is deprecated");
  }

  public int hashCode(){
    throw new DeprecatedMethodError("class java.security.Identity is deprecated");
  }

}
