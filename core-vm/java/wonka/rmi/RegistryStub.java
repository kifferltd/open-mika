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

package wonka.rmi;

import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RMIClientSocketFactory;

public class RegistryStub implements java.rmi.registry.Registry, RegistryConstants {

  private String host;
  private int port;
  private RMIClientSocketFactory csf;

  public RegistryStub(String host, int port, RMIClientSocketFactory csf){
    this.host = host;
    this.port = port;
    this.csf  =  csf;
  }

  public void bind(String name, Remote obj) throws AlreadyBoundException, RemoteException {
    ObjIDData data = (ObjIDData)RMIConnection.exports.get(obj);
    if(data == null){
      throw new RemoteException("Object "+obj+" is not exported");
    }
    Socket s = createConnection();
    Object[] params = new Object[2];
    params[0] = name;
    params[1] = data.stub;
    RMIConnection.requestObject(s, HASH, BIND, NULLID, new ParameterSet(params), false);
    if(RMIConnection.DEBUG < 9) {System.out.println("BIND succeeded");}
  }
    
  public String[] list() throws RemoteException {
    Socket s = createConnection();
    if(RMIConnection.DEBUG < 5) {System.out.println("RegistryStub: list --> requestOject");}
    Object o = RMIConnection.requestObject(s, HASH, LIST, NULLID, ParameterSet.NOARGS_SET, true);
    if(RMIConnection.DEBUG < 9) {System.out.println("RegistryStub: list --> requestOject finished");}
    return (String[])o;
  }
  
  public Remote lookup(String name) throws NotBoundException, RemoteException {
    Socket s = createConnection();
    if(RMIConnection.DEBUG < 5) {System.out.println("RegistryStub: lookup --> requestOject");}
    Object o = RMIConnection.requestObject(s, HASH, LOOKUP, NULLID, name, true);
    if(RMIConnection.DEBUG < 9) {System.out.println("RegistryStub: lookup --> requestOject finished "+o+" "+o.getClass());}
    Remote remote = (Remote)o;
    DGCClient.registerRemote(remote);
    return remote;
  }
  
  public void rebind(String name, Remote obj) throws RemoteException {
    ObjIDData data = (ObjIDData)RMIConnection.exports.get(obj);
    if(data == null){
      throw new RemoteException("Object "+obj+" is not exported");
    }
    Socket s = createConnection();
    Object[] params = new Object[2];
    params[0] = name;
    params[1] = data.stub;
    RMIConnection.requestObject(s, HASH, REBIND, NULLID, new ParameterSet(params), false);
    if(RMIConnection.DEBUG < 9) {System.out.println("REBIND Succeeded "+name);}
  }
  
  public void unbind(String name) throws NotBoundException, RemoteException {
    Socket s = createConnection();
    if(RMIConnection.DEBUG < 5) {System.out.println("UNBIND requesting "+name);}
    RMIConnection.requestObject(s, HASH, UNBIND, NULLID, name, false);
    if(RMIConnection.DEBUG < 9) {System.out.println("UNBIND Succeeded "+name);}
    //no clean up needed. (cleanUp will be done when UnexportObject is called in UnicastRemoteObject).
  }

  private Socket createConnection() throws RemoteException {
    try {
      if(csf != null){
        return csf.createSocket(host, port);
      }
      return new Socket(host, port);
    }
    catch(Exception ioe){
      throw new RemoteException("unable to connect", ioe);
    }
  }
}

