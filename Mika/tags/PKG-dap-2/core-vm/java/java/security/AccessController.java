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
 * $Id: AccessController.java,v 1.1.1.1 2004/07/12 14:07:46 cvs Exp $
 */
package java.security;

/** Utitlity class responsible for launching privileged code.
 ** Also contains the method used to take a snapshot of the current context.
 **@author ACUNIA NV
 **@version $Version:$
 */
public final class AccessController {

  static {
    initialize();
  }

  private static native void initialize();

  private static native Object doPrivileged0 (PrivilegedAction action, AccessControlContext context);

  private static native Object doPrivileged0 (PrivilegedExceptionAction action, AccessControlContext context) throws Exception;

  /** Launch some privileged code in the current context.
   */
  public static Object doPrivileged (PrivilegedAction action) {
    return doPrivileged(action, getContext());
  }

  /** Launch some privileged code in another context.
   */
  public static Object doPrivileged (PrivilegedAction action, AccessControlContext context) {
    return doPrivileged0(action, new AccessControlContext(get_calling_domains(), get_inherited_context(), context));
  }

  /** Launch some privileged code in the current context.
   ** The privileged code can throw checked Exceptions.
   */
  public static Object doPrivileged (PrivilegedExceptionAction action) throws PrivilegedActionException {
    try {
      return doPrivileged0(action, getContext());
    }
    catch(Exception e){
      throw new PrivilegedActionException(e);
    }
  }

  /** Launch some privileged code in another context.
   ** The privileged code can throw checked Exceptions.
   */
  public static Object doPrivileged (PrivilegedExceptionAction action, AccessControlContext context) throws PrivilegedActionException {
    try {
      return doPrivileged0(action, new AccessControlContext(get_calling_domains(), get_inherited_context(), context));
    }
    catch(Exception e){
      throw new PrivilegedActionException(e);
    }
  }

 /* Take a snapshot of the current context.
 */
  public static AccessControlContext getContext() {
    return new AccessControlContext(get_calling_domains(), get_inherited_context());
  }

  /** Get the list of ProtectionDomains associated with the current context.
   */
  private static native ProtectionDomain[] get_calling_domains();

  /** Get the AccessControlContext which was inherited by the most recent privileged frame.
   ** (In the worst case this will be the root frame).
   */
  private static native AccessControlContext get_inherited_context();

  public static void checkPermission (Permission perm) throws AccessControlException {
    try {
      getContext().checkPermission(perm);
    }
    catch(AccessControlException e) {
      throw new AccessControlException("Context: " + getContext() + "   Permission: " + perm);
    }
  }

  private AccessController(){}

}
