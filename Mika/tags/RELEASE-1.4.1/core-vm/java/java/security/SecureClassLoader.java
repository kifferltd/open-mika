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

/**
 *
 * $Id: SecureClassLoader.java,v 1.2 2005/09/12 22:04:51 cvs Exp $
 *
 */
public class SecureClassLoader extends ClassLoader {

  protected SecureClassLoader() throws SecurityException {
  }

  protected SecureClassLoader(ClassLoader parent) throws SecurityException {
    super(parent);
  }

  protected final Class defineClass( String name, byte []b, int off, int len, CodeSource cs) {
    if (cs != null) {
      PermissionCollection pc = getPermissions(cs);
      if (pc == null) {
        return defineClass(name, b, off, len);
      }

      return defineClass(name, b, off, len, new ProtectionDomain(cs, pc));
    }
    else {
      return defineClass(name, b, off, len);
    }
  }

  protected PermissionCollection getPermissions(CodeSource cs){
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      GetPolicy gp = new GetPolicy();

      return gp.get().getPermissions(cs);

    }

    // HACK! HACK! HACK! Just to get out of an initialisation foul-up ...
    Policy policy = Policy.getPolicy();
    if (policy == null) {
      // Policy file not loaded yet
      return null;
    }
    return Policy.getPolicy().getPermissions(cs) ;
  }
}

