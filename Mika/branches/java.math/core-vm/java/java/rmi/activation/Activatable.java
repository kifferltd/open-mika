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

