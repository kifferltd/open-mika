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

package java.rmi.registry;

import java.rmi.*;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import com.acunia.wonka.rmi.*;

public final class LocateRegistry {

  private LocateRegistry(){}

  public static Registry createRegistry(int port) throws RemoteException {
    return new RegistryImpl(port);
  }
  
  public static Registry createRegistry(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf)  throws RemoteException {
    return new RegistryImpl(port, csf, ssf);
  }
  
  public static Registry getRegistry() throws RemoteException {
    return new RegistryStub("localhost",Registry.REGISTRY_PORT,null);
  }
  
  public static Registry getRegistry(int port) throws RemoteException {
    return new RegistryStub("localhost",port,null);
  }
  
  public static Registry getRegistry(String host) throws RemoteException {
    return new RegistryStub(host,Registry.REGISTRY_PORT,null);
  }
  
  public static Registry getRegistry(String host, int port) throws RemoteException {
    return new RegistryStub(host,port,null);
  }
  
  public static Registry getRegistry(String host, int port, RMIClientSocketFactory csf)  throws RemoteException {
    return new RegistryStub(host,port,csf);
  }

}
