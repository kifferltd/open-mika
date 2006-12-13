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
 * $Id: ProtectionDomain.java,v 1.2 2006/04/18 11:35:28 cvs Exp $
 */
package java.security;

/** The protection domain to which a class belongs.
 ** Encapsulates a CodeSource and a PermissionCollection.
 **@author ACUNIA NV
 **@version $Version:$
 */
public class ProtectionDomain {

  /** The source from whence this code came.
   */
  private CodeSource codeSource;

  /** The permissions which are granted it.
   */
  private PermissionCollection permissions;

  ClassLoader loader;
  private Principal[] principals;

  /** Standard constructor.
   */
  public ProtectionDomain (CodeSource cs, PermissionCollection permissions) {
    this.codeSource = cs;
    if(permissions != null){
      permissions.setReadOnly();
      this.permissions = permissions;
    }
  }
  
  public ProtectionDomain(CodeSource cs, PermissionCollection permissions,
      ClassLoader classloader, Principal[] principals) {
    
    this(cs, permissions);
    this.loader = classloader; 
    this.principals = principals;    
  }

  /** Access method for the encapsulated CodeSource.
   */
  public final CodeSource getCodeSource() {
    return codeSource;
  }

  /** Access method for the encapsulated PermissionCollection.
   */
  public final PermissionCollection getPermissions() {
    return permissions;
  }

  /** Is the given permission implied by this domain's permisisons?.
   */
  public boolean implies (Permission permission) {
    return (permissions == null ? false : permissions.implies(permission));
  }

  /** String representation of this PermissionCollection.
   */
  public String toString() {
    String answer = this.getClass().getName()+" with codesource "+this.codeSource;
    return answer;
  }

  /**
   * @return Returns a classloader.
   */
  public final ClassLoader getClassLoader() {
    return loader;
  }

  /**
   * @return Returns the principals.
   */
  public final Principal[] getPrincipals() {
    return principals;
  }

}
