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

package java.rmi.registry;

import java.rmi.*;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import wonka.rmi.*;

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

