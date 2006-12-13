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

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;

public class RegistryImpl extends Thread implements Registry {

  private java.util.HashMap map = new java.util.HashMap(31);
  private ServerSocket server;
  private RMIClientSocketFactory csf;

  public RegistryImpl(int port) throws RemoteException {
    super("RegistryImplThread on port:"+port);
    try {
      server = new ServerSocket(port);
    }
    catch(IOException ioe){
      throw new RemoteException("failed to create ServerSocket",ioe);
    }
    this.setDaemon(true);
    this.start();
  }

  public RegistryImpl(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf) throws RemoteException {
    super("RegistryImplThread on port:"+port);
    try {
      server = ssf != null ? ssf.createServerSocket(port) : new ServerSocket(port);
    }
    catch(IOException ioe){
      throw new RemoteException("failed to create ServerSocket",ioe);
    }
    this.csf = csf;
    this.setDaemon(true);
    this.start();
  }

  public void run(){
    try {
      while(true){
        Socket s = server.accept();
        new RMIRegistryRequest(s, this);
        if(RMIConnection.DEBUG < 6) {System.out.println("HANDLING INCOMING REQUEST");}
      }
    }
    catch(Exception e){
      e.printStackTrace();
      try {
        server.close();
      }
      catch(IOException ioe){}
    }
    if(RMIConnection.DEBUG < 7) {System.out.println("Registry Thread Stopped\n");}
  }

  public synchronized void bind(String name, Remote obj) throws AlreadyBoundException {
    if(name == null || map.containsKey(name)){
      throw new AlreadyBoundException("'"+name+"' is not valid");
    }
    DGCClient.registerRemote(obj);
    map.put(name,obj);
  }

  public synchronized String[] list() {
    int size = map.size();
    String[] strings = new String[size];
    if(size > 0){
      map.keySet().toArray(strings);
    }
    return strings;
  }

  public synchronized Remote lookup(String name) throws NotBoundException {
    if(name == null || !map.containsKey(name)){
      throw new NotBoundException("'"+name+"' is not valid");
    }
    return (Remote)map.get(name);
  }

  public synchronized void rebind(String name, Remote obj) throws RemoteException {
    if(name == null){
      throw new RemoteException("name is null");
    }
    map.put(name,obj);
    DGCClient.registerRemote(obj);
    if(RMIConnection.DEBUG < 9) {System.out.println("REBIND '"+name+"' "+map);}
  }

  public synchronized void unbind(String name) throws NotBoundException {
    Object obj = map.remove(name);
    if(obj == null){
      if(RMIConnection.DEBUG < 9) {System.out.println("FAILED to UNBIND '"+name+"'");}
      throw new NotBoundException("'"+name+"' is not valid");
    }
    if(RMIConnection.DEBUG < 9) {System.out.println("Succeeded to UNBIND '"+name+"' "+map);}

  }
}

