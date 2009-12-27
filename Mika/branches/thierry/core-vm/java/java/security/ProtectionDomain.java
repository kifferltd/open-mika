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
