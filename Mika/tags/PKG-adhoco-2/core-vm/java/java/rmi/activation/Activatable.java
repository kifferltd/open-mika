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

package java.rmi.activation;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;
import java.rmi.MarshalledObject;
import java.rmi.server.RemoteServer;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

public abstract class Activatable extends RemoteServer {

  private static final long serialVersionUID = -3120617863591563455L;

  private ActivationID id;

  protected Activatable(String location, MarshalledObject data, boolean restart, int port) throws ActivationException, RemoteException {
    
    System.out.println("[Activatable] Not implemented...");
  }
  
  protected Activatable(String location, MarshalledObject data, boolean restart, int port,
    RMIClientSocketFactory csf, RMIServerSocketFactory ssf) throws ActivationException, RemoteException {
    
    System.out.println("[Activatable] Not implemented...");
  }
  
  protected Activatable(ActivationID id, int port) throws RemoteException {
    System.out.println("[Activatable] Not implemented...");
  }
  
  protected Activatable(ActivationID id, int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf) throws RemoteException {
    System.out.println("[Activatable] Not implemented...");
  }
  
  protected ActivationID getID() {
    return id;
  }
  
  public static Remote register(ActivationDesc desc) throws UnknownGroupException, ActivationException, RemoteException {
    System.out.println("[Activatable.register] Not implemented...");
    return null;
  }
  
  public static boolean inactive(ActivationID id) throws UnknownObjectException, ActivationException, RemoteException {
    System.out.println("[Activatable.inactive] Not implemented...");
    return false;
  }
  
  public static void unregister(ActivationID id) throws UnknownObjectException,  ActivationException, RemoteException {
    System.out.println("[Activatable.unregister] Not implemented...");
  }
  
  public static ActivationID exportObject(Remote obj, String location, MarshalledObject data, boolean restart, int port) 
    throws ActivationException, RemoteException {
    
    System.out.println("[Activatable.exportObject] Not implemented...");
    return null;
  }
  
  public static ActivationID exportObject(Remote obj, String location, MarshalledObject data, boolean restart, int port, 
                                          RMIClientSocketFactory csf, RMIServerSocketFactory ssf) 
    throws ActivationException, RemoteException {
    
    System.out.println("[Activatable.exportObject] Not implemented...");
    return null;
  }
  
  public static Remote exportObject(Remote obj, ActivationID id, int port) throws RemoteException {
    System.out.println("[Activatable.exportObject] Not implemented...");
    return null;
  }
  
  public static Remote exportObject(Remote obj, ActivationID id, int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf) throws RemoteException {
    System.out.println("[Activatable.exportObject] Not implemented...");
    return null;
  }
  
  public static boolean unexportObject(Remote obj, boolean force) throws NoSuchObjectException {
    System.out.println("[Activatable.unexportObject] Not implemented...");
    return false;
  }
  
}

