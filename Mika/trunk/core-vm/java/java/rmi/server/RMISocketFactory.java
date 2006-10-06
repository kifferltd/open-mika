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

package java.rmi.server;

import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;

public abstract class RMISocketFactory implements RMIClientSocketFactory, RMIServerSocketFactory {

  static final RMISocketFactory theDefaultSocketFactory = new com.acunia.wonka.rmi.DefaultRMISocketFactory();
  static RMISocketFactory currentSocketFactory = null;
  static RMIFailureHandler theFailureHandler = null;

  public RMISocketFactory() {}

  public abstract ServerSocket createServerSocket(int port) throws IOException;
  
  public abstract Socket createSocket(String host, int port) throws IOException;
  
  public static RMISocketFactory getDefaultSocketFactory() {
    return theDefaultSocketFactory;
  }
  
  public static RMIFailureHandler getFailureHandler() {
    return theFailureHandler;
  }
  
  public static RMISocketFactory getSocketFactory() {
    return currentSocketFactory;
  }
  
  public static void setFailureHandler(RMIFailureHandler fh) {
    theFailureHandler = fh;
  }
  
  public static void setSocketFactory(RMISocketFactory fac) throws IOException {
    if(currentSocketFactory != null){
      throw new IOException("Factory is already set");
    }

    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      java.security.AccessController.checkPermission(new RuntimePermission("setFactory"));
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkSetFactory();
      }
    }

    currentSocketFactory = fac;
  }

  static RMISocketFactory getRMISocketFactory(){
    return currentSocketFactory == null ? theDefaultSocketFactory : currentSocketFactory;
  }

}

