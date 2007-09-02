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
