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

