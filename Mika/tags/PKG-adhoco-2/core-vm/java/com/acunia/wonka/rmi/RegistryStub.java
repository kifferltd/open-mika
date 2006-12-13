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

package com.acunia.wonka.rmi;

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

