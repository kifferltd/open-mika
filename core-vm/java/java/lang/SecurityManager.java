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

/*
** $Id: SecurityManager.java,v 1.2 2006/04/18 11:35:28 cvs Exp $
*/

package java.lang;

import java.io.FileDescriptor; 
import java.io.FilePermission; 
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.SocketPermission;
import java.security.AccessControlContext;
import java.security.Permission;
import java.security.SecurityPermission;
import java.util.PropertyPermission;

/** The default SecurityManager for Wonka.
 ** If USE_SECURITY_MANAGER is set in wonka.vm.SecurityConfiguration
 ** then this is a real security manager which uses the AccessController
 ** to implement all checks: otherwise it is a null security manager which
 ** allows everyone to do everything.
 */
public class SecurityManager {

  /** Field inCheck is set true iff a security check is in progress
   ** AND we call another method of SecurityManager (which might 
   ** have been overridden).  Otherwise we don't bother.  
   ** Note that use of this variable is deprecated.
   */
  protected boolean inCheck = false;
  
  /** This is what one might call ``a workaround''.
  ** The problem is that we may not have an AWT, and in that case
  ** there will be no class java.awt.AWTPermission.  So in our
  ** static initializer we try to load that class, and if the attempt
  ** fails then all calls to checkPrintJobAccess, checkSystemClipboardAccess,
  ** checkAwtEventQueueAccess, and checkTopLevelWindow will fail.
  */
  static private Class awtPermission;

  /** The constructor of awtPermission which takes a single string as argument.
   */
  static private Constructor awtpCon;

  static {
    try {
      awtPermission = Class.forName("java.awt.AWTPermission");
      Class[] params = new Class[1];
      params[0] = String.class;
      awtpCon = awtPermission.getConstructor(params);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** Default constructor, public in Java2.
   ** Performs a security check iff USE_ACCESS_CONTROLLER or USE_SECURITY_MANAGER
   ** is set in wonka.vm.SecurityConfiguration.
   */
  public SecurityManager()
    throws SecurityException
  {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER 
     || wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      java.security.AccessController.checkPermission(new RuntimePermission("createSecurityManager"));
    }
  }

  /** A wrapper for AccessController.checkPermission(perm), or a no-op if
   ** USE_SECURITY_MANAGER is not set in wonka.vm.SecurityConfiguration.
   */
  public void checkPermission(Permission perm) {
    if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      java.security.AccessController.getContext().checkPermission(perm);
    }
  }

  /** A wrapper for AccessController.checkPermission(perm).
   */
  public void checkPermission(Permission perm, Object context) {
    if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      try {
        java.security.AccessControlContext acc = (AccessControlContext)context;
        acc.checkPermission(perm);
      } catch (ClassCastException cce) {
        throw new SecurityException("Not an AccessControlContext: "+context);
      }
    }
  }

  /** Get the security context (i.e., AccessControlContext) currently applicable.
   */
  public Object getSecurityContext() {
    return java.security.AccessController.getContext();
  }

  /** Get the thread group in which new threads should be instantiated.
   ** Used by the constructors of java.lang.Thread which do not specifiy the ThreadGroup.
   ** This implementation just returns the ThreadGroup of the current thread.
   ** Override this if you feel that new threads should be instantiated somewhere else (e.g. in a Realm ...).
   */
  public ThreadGroup getThreadGroup() {
    return Thread.currentThread().getThreadGroup();
  }
 
  /** The Class[] returned contains one element for every method on the stack, starting with the method which called  getClassContext().
   ** Provided for use by user-defined SecurityManager's, we don't use this internally.
   ** Looks at all frames, not just up to the most recent doPrivileged.
   ** Includes native methods, including invoke().
   */
  protected native Class[] getClassContext();
  
  /** The Class[] returned contains one element for every method on the stack, starting with the method which called  getClassContext(), up to the most recent privileged frame.
   ** Includes native methods, including invoke().
   */
  private native Class[] getNonPrivilegedClassContext();
  
  /** Gets the stack depth of the most recent call to a method of the given class.
   ** (But does it include this call? Who cares...).
   ** Returns -1 if not found.
   ** Looks at all frames, not just up to the most recent doPrivileged.
   ** Deprecated.
   */
  protected int classDepth(String name) {
    System.err.println("SecurityManager method classDepth() is deprecated!");
    Class [] classes = getClassContext();
    try {
      Class cl = Class.forName(name);

      for (int i = 0; i < classes.length; ++i) {
        if (classes[i] == cl) {

          return i;

        }
      }
    } catch (ClassNotFoundException cnfe) {
      // Fall through, return -1
    }

    return -1;
  }
  
  /** Get the class loader of the topmost class on the stack that was not loaded by the system class loader or bootstrap class loader.
   ** Only looks at frames up to the most recent doPrivileged, but does include native methods, including invoke().
   ** Always returns null if caller has AllPermission.
   ** Deprecated.
   */
  protected ClassLoader currentClassLoader() {
    System.err.println("SecurityManager method currentClassLoader() is deprecated!");
    Class clc = currentLoadedClass();

    return clc == null ? null : clc.getClassLoader();
  }
  
  /** Get the topmost class on the stack that was not loaded by the system class loader or bootstrap class loader.
   ** Only looks at frames up to the most recent doPrivileged, but does include native methods, including invoke().
   ** Always returns null if caller has AllPermission.
   ** Deprecated.
   */
  protected Class currentLoadedClass() {
    System.err.println("SecurityManager method currentLoadedClass() is deprecated!");
    Class [] classes = getNonPrivilegedClassContext();
    ClassLoader scl = ClassLoader.getSystemClassLoader();

    try {
      inCheck = true;
      checkPermission(new java.security.AllPermission());
      inCheck = false;

      return null;
    }
    catch (SecurityException se) {
      inCheck = false;
      for (int i = 0; i < classes.length; ++i) {
        ClassLoader cl = classes[i].getClassLoader();
        if (cl != null && cl != scl) {
  
          return classes[i];
  
        }
      }
    }

    return null;
  }
  
  /** Get the depth on the stack of the topmost class that was not loaded by the system class loader or bootstrap class loader.
   ** Only looks at frames up to the most recent doPrivileged, but does include native methods, including invoke().
   ** Always returns null if caller has AllPermission.
   ** Deprecated.
   */
  protected int classLoaderDepth() {
    System.err.println("SecurityManager method classLoaderDepth() is deprecated!");
    if (currentLoadedClass() == null) {

      return -1;

    }

    Class [] classes = getNonPrivilegedClassContext();
    ClassLoader scl = ClassLoader.getSystemClassLoader();

    try {
      inCheck = true;
      checkPermission(new java.security.AllPermission());
      inCheck = false;

      return -1;
    }
    catch (SecurityException se) {
      inCheck = false;
      for (int i = 0; i < classes.length; ++i) {
        ClassLoader cl = classes[i].getClassLoader();
        if (cl != null && cl != scl) {
  
          return i;
  
        }
      }
    }

    return -1;
  }
  
  /** See whether the given class is in the stack.
   ** Deprecated.
   */
  protected boolean inClass(String name) {
    System.err.println("SecurityManager method inClass() is deprecated!");
    return classDepth(name) >= 0;
  }

  /** See whether any non-system class is in the stack.
   ** See currentClassLoader().
   ** Deprecated.
   */
  protected boolean inClassLoader() {
    System.err.println("SecurityManager method inClassLoader() is deprecated!");
    return currentClassLoader() != null;
  }
  
  /** Get the value of the inCheck field.  Deprecated.
   */
  public synchronized boolean getInCheck()
  {
    System.err.println("SecurityManager method getInCheck() is deprecated!");
    return inCheck;
  }
  
  /** Checks whether the caller has permission to create a ClassLoader.
   ** If you override this, you should  include a call to super.checkCreateClassLoader().
   */
  public  void checkCreateClassLoader()
    throws SecurityException
  {
    inCheck = true;
    checkPermission(new RuntimePermission("createClassLoader"));
    inCheck = false;
  }
    
  /** Checks whether the caller has permission to modify Thread t.
   ** The check is only applied if t belongs to the system ThreadGroup.
   ** If you override this, you should  include a call to super.checkAccess().
   ** (You should probably also allow access to any thread which has
   ** RuntimePermission("modifyThread"), so that system code can still create threads).
   */
  public void checkAccess(Thread t)
    throws SecurityException
  {
    if (t.getThreadGroup().getParent() != null) {

      return;

    }

    inCheck = true;
    checkPermission(new RuntimePermission("modifyThread"));
    inCheck = false;
  }

  /** Checks whether the caller has permission to modify ThreadGroup g.
   ** The check is only applied if t belongs to the system ThreadGroup.
   ** If you override this, you should  include a call to super.checkAccess().
   ** (You should probably also allow access to any thread which has
   ** RuntimePermission("modifyThreadGroup"), so that system code can still create threads).
   */
  public void checkAccess(ThreadGroup g)
    throws SecurityException
  {
    if (g.getParent() != null) {

      return;

    }

    inCheck = true;
    checkPermission(new RuntimePermission("modifyThreadGroup"));
    inCheck = false;
  }

  /** Checks whether the caller has access to the declared members of ``cl''.
   ** If you override this, you should  include a call to super.checkMemberAccess().
   */
  public void checkMemberAccess(Class cl, int mtype)
    throws SecurityException
  {
    if (mtype != java.lang.reflect.Member.PUBLIC && cl.getClassLoader() != currentClassLoader()) {
      inCheck = true;
      checkPermission(new RuntimePermission("accessDeclaredMembers."+cl.getName()));
      inCheck = false;
    }
  }

  /** Checks whether the caller is allowed to exit the VM.
   ** If you override this, you should  include a call to super.checkExit().
   ** The `status' argument is not used.
   */
  public void checkExit(int status)
    throws SecurityException
  {
    inCheck = true;
    checkPermission(new RuntimePermission("exitVM"));
    inCheck = false;
  }

  /** Checks whether the caller is allowed to execute the specified command.
   ** If you override this, you should  include a call to super.checkExecute().
   ** Note the implicit assumption that all commands are the names of files.
   */
  public void checkExec(String cmd)
    throws SecurityException
  {
    inCheck = true;
    checkPermission(new FilePermission(cmd, "execute"));
    inCheck = false;
  }

  /** Checks whether the caller has permission to use IP Multicast.
   ** If you override this, you should  include a call to super.checkMulticast().
   */
  public void checkMulticast(InetAddress addr)
    throws SecurityException
  {
    inCheck = true;
    checkPermission(new SocketPermission(addr.getHostAddress(), "accept,connect"));
    inCheck = false;
  }

  /** Checks whether the caller has permission to use IP Multicast.
   ** If you override this, you should  include a call to super.checkMulticast().
   ** Note: parameter `ttl' is not used (what's it there for?)
   */
  public void checkMulticast(InetAddress addr, byte ttl)
    throws SecurityException
  {
    inCheck = true;
    checkPermission(new SocketPermission(addr.getHostAddress(), "accept,connect"));
    inCheck = false;
  }

  /** Checks whether the caller has permission to read the given system property.
   ** If you override this, you should  include a call to super.checkPropertyAccess().
   */
  public void checkPropertyAccess(String propname)
    throws SecurityException
  {
    inCheck = true;
    checkPermission(new PropertyPermission(propname, "read"));
    inCheck = false;
  }

  /** Checks whether the caller has permission to read or modify the system properties as a whole.
   ** If you override this, you should  include a call to super.checkPropertiesAccess().
   */
  public void checkPropertiesAccess()
    throws SecurityException
  {
    inCheck = true;
    checkPermission(new PropertyPermission("*", "read,write"));
    inCheck = false;
  }

  /** Checks whether the caller has the SecurityPermission called ``target''.
   ** If you override this, you should include a call to super.checkPropertiesAccess().
   */
  public void checkSecurityAccess(String target)
    throws SecurityException
  {
    inCheck = true;
    checkPermission(new SecurityPermission(target));
    inCheck = false;
  }

  /** Checks whether the caller has permission to load library ``libname''.
   ** If you override this, you should include a call to super.checkLink().
   */
  public void checkLink(String libname)
    throws SecurityException
  {
    inCheck = true;
    checkPermission(new RuntimePermission("loadLibrary."+libname));
    inCheck = false;
  }

  /** Checks whether the caller has permission to read the file descriptor `fd'.
   ** If you override this, you should include a call to super.checkRead().
   ** Parameter `fd' is not used.
   */
  public void checkRead(FileDescriptor fd)
    throws SecurityException
  {
    inCheck = true;
    checkPermission(new RuntimePermission("readFileDescriptor"));
    inCheck = false;
  }

  /** Checks whether the caller has permission to read the file ``file''.
   ** If you override this, you should include a call to super.checkRead().
   */
  public void checkRead(String file)
    throws SecurityException
  {
    inCheck = true;
    checkPermission(new FilePermission(file,"read"));
    inCheck = false;
  }

  /** Checks whether the given context has permission to read the file ``file''.
   */
  public void checkRead(String file, Object context)
    throws SecurityException
  {
    inCheck = true;
    checkPermission(new FilePermission(file,"read"), context);
    inCheck = false;
  }

  /** Checks whether the caller has permission to write to the file `fd'.
   ** If you override this, you should include a call to super.checkWrite().
   ** Parameter `fd' is not used in the default version.
   */
  public void checkWrite(FileDescriptor fd)
    throws SecurityException
  {
    inCheck = true;
    checkPermission(new RuntimePermission("writeFileDescriptor"));
    inCheck = false;
  }

  /** Checks whether the caller has permission to write to the file ``file''.
   ** If you override this, you should include a call to super.checkWrite().
   */
  public void checkWrite(String file)
    throws SecurityException
  {
    inCheck = true;
    checkPermission(new FilePermission(file,"write"));
    inCheck = false;
  }

  public void checkDelete(String file)  
    throws SecurityException
  {
    inCheck = true;
    checkPermission(new FilePermission(file,"delete"));
    inCheck = false;
  }

  /** Checks whether the caller has permission to connect to the given port.
   ** If you override this, you should include a call to super.checkConnect().
   */
  public void checkConnect(String host, int port)
    throws SecurityException
  {
    inCheck = true;
    if (port == -1) {
      checkPermission(new SocketPermission(host,"resolve"));
    }
    else {
      checkPermission(new SocketPermission(host+":"+port,"connect"));
    }
    inCheck = false;
  }

  /** Checks whether the given context has permission to connect to the given port.
   ** If you override this, you should include a call to super.checkConnect().
   */
  public void checkConnect(String host, int port, Object context)
    throws SecurityException
  {
    inCheck = true;
    if (port == -1) {
      checkPermission(new SocketPermission(host,"resolve"), context);
    }
    else {
      checkPermission(new SocketPermission(host+":"+port,"connect"), context);
    }
    inCheck = false;
  }

  /** Checks whether the caller has permission to listen to the given port.
   ** If you override this, you should include a call to super.checkListen().
   */
  public void checkListen(int port)
    throws SecurityException
  {
    inCheck = true;
    if (port == 0) {
      checkPermission(new SocketPermission("localhost:1024-","listen"));
    }
    else {
      checkPermission(new SocketPermission("localhost:"+port,"listen"));
    }
    inCheck = false;
  }

  /** Checks whether the caller has permission to accept incoming connections.
   ** If you override this, you should include a call to super.checkAccept().
   */
  public void checkAccept(String host, int port)
    throws SecurityException
  {
    inCheck = true;
    checkPermission(new SocketPermission(host+":"+port,"accept"));
    inCheck = false;
  }

  /** Checks whether the caller has permission to initiate a print job.
   ** If you override this, you should include a call to super.checkPrintJobAccess().
   */
  public void checkPrintJobAccess()
    throws SecurityException
  {
    if (awtPermission != null) {
      Object[] params = new Object[1];
      params[0] = new String("queuePrintJob");
      Permission perm;
      try {
        perm = (Permission)awtpCon.newInstance(params);
      }
      catch (Exception e) {
        e.printStackTrace();
        throw new SecurityException("Failed to create an AWTPermission? "+e);
      }
      inCheck = true;
      checkPermission(perm);
      inCheck = false;
    }
    else {
      throw new SecurityException("No AWT present?");
    }
  }

  /** Checks whether the caller has permission to access the AWT system clipboard.
   ** If you override this, you should include a call to super.checkSystemClipboardAccess().
   */
  public void checkSystemClipboardAccess()
    throws SecurityException
  {
    if (awtPermission != null) {
      Object[] params = new Object[1];
      params[0] = new String("accessClipboard");
      Permission perm;
      try {
        perm = (Permission)awtpCon.newInstance(params);
      }
      catch (Exception e) {
        e.printStackTrace();
        throw new SecurityException("Failed to create an AWTPermission? "+e);
      }
      inCheck = true;
      checkPermission(perm);
      inCheck = false;
    }
    else {
      throw new SecurityException("No AWT present?");
    }
  }

  /** Checks whether the caller has permission to access the AWT event queue.
   ** If you override this, you should include a call to super.checkAwtEventQueueAccess().
   */
  public void checkAwtEventQueueAccess()
    throws SecurityException
  {
    if (awtPermission != null) {
      Object[] params = new Object[1];
      params[0] = new String("accessEventQueue");
      Permission perm;
      try {
        perm = (Permission)awtpCon.newInstance(params);
      }
      catch (Exception e) {
        e.printStackTrace();
        throw new SecurityException("Failed to create an AWTPermission? "+e);
      }
      inCheck = true;
      checkPermission(perm);
      inCheck = false;
    }
    else {
      throw new SecurityException("No AWT present?");
    }
  }

  /** Checks whether the caller has permission to set the socket factories used by the java.net code.
   ** If you override this, you should include a call to super.checkSetFactory().
   */
  public void checkSetFactory()
    throws SecurityException
  {
    inCheck = true;
    checkPermission(new RuntimePermission("setFactory"));
    inCheck = false;
  }

  /** Checks whether the caller is trusted to bring up the specified window.
   ** If you override this, you should include a call to super.checkTopLevelWindow().
   ** Note that this method anomolously returns `true' if the caller is
   ** trusted, `false' otherwise.  The parameter (`window') is not used.
   */
  public boolean checkTopLevelWindow(Object window)
    throws SecurityException
  {
    if (awtPermission != null) {
      Object[] params = new Object[1];
      params[0] = new String("topLevelWindowPermission");
      Permission perm;
      try {
        perm = (Permission)awtpCon.newInstance(params);
      }
      catch (Exception e) {
        e.printStackTrace();

        return false;

      }

      inCheck = true;
      try {
        checkPermission(perm);
        inCheck = false;

        return true;

      } catch (SecurityException se) {
        inCheck = false;

        return false;

      }
    }
    else {

      return false;

    }
  }

  /** Checks whether the caller has access to the given package.
   ** If you override this, you should include a call to super.checkPackageAccess().
   */
  public void checkPackageAccess(String packageName)
    throws SecurityException
  {
    String restricteds = java.security.Security.getProperty("package.definition");
    if (restricteds == null) {
      System.err.println("Hm, we don't seem to have a security property `package.definition'.  This is broken, someone please fix it.");
      restricteds = "java,wonka,com.acunia.wonka";
    }

    int comma = restricteds.indexOf(',');
    String arestricted;
    inCheck = true;
    while (comma >= 0) {
      arestricted = restricteds.substring(0,comma);
      if (packageName == arestricted || packageName.startsWith(arestricted) && packageName.charAt(arestricted.length()) == '.') {
        checkPermission(new RuntimePermission("accessClassInPackage."+packageName));
      }
      restricteds = restricteds.substring(comma+1);
      comma = restricteds.indexOf(',');
    }
    arestricted = restricteds;
    if (packageName == arestricted || packageName.startsWith(arestricted) && packageName.charAt(arestricted.length()) == '.') {
      checkPermission(new RuntimePermission("accessClassInPackage."+packageName));
    }
      inCheck = false;
  }

  /** Checks whether the caller has permission to define classes in the given package.
   ** If you override this, you should include a call to super.checkPackageDefinition().
   */
  public void checkPackageDefinition(String packageName)
    throws SecurityException
  {
    String restricteds = java.security.Security.getProperty("package.definition");
    if (restricteds == null) {
      System.err.println("Hm, we don't seem to have a security property `package.definition'.  This is broken, someone please fix it.");
    }

    int comma = restricteds.indexOf(',');
    String arestricted;
    inCheck = true;
    while (comma >= 0) {
      arestricted = restricteds.substring(0,comma);
      if (packageName == arestricted || packageName.startsWith(arestricted) && packageName.charAt(arestricted.length()) == '.') {
        checkPermission(new RuntimePermission("defineClassInPackage."+packageName));
    inCheck = true;
      }
      restricteds = restricteds.substring(comma+1);
      comma = restricteds.indexOf(',');
    }
    arestricted = restricteds;
    if (packageName == arestricted || packageName.startsWith(arestricted) && packageName.charAt(arestricted.length()) == '.') {
      checkPermission(new RuntimePermission("defineClassInPackage."+packageName));
    }
      inCheck = false;
  }
}
