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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.rmi.AlreadyBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ObjID;

class RMIRegistryRequest extends Thread implements RegistryConstants {

  private Socket s;
  private RegistryImpl registry;

  RMIRegistryRequest(Socket s, RegistryImpl registry){
    super("RMIRegistryRequest Thread "+s);
    this.s = s;
    try {
      s.setSoLinger(true,30000);
    }
    catch(IOException ioe){}
    this.registry = registry;
    this.setDaemon(true);
    this.start();
  }

  public void run(){
    if(RMIConnection.DEBUG < 6) {System.out.println("Starting RegistryRequest to "+registry);}
    OutputStream out = null;
    try {
      out = s.getOutputStream();
    }
    catch(IOException ioe){}
    try {
      RMIConnection.serverSideHandShake(s);
      InputStream in = s.getInputStream();

      DataInputStream din = new DataInputStream(in);
      String address = din.readUTF();
      if(RMIConnection.DEBUG < 6) {System.out.println("Address :"+address);}
      int port = din.readInt();
      if(RMIConnection.DEBUG < 6) {System.out.println("Port :"+port);}

      while(true){
        int type = din.read();
        if(type == -1){
          break;
        }
        if(RMIConnection.DEBUG < 6) {System.out.println("Reading resonse "+Integer.toHexString(type));}
        if(type == RMIConstants.CALL){

          ObjectInputStream oin = new RMIObjectInputStream(new PushbackInputStream(in));
          //ObjID  id = 
            ObjID.read(oin);
          int operation = oin.readInt();
          if(RMIConnection.DEBUG < 6) {System.out.println("Operation: "+operation);}
          long hash = oin.readLong();
          if(RMIConnection.DEBUG < 6) {System.out.println("Hash: "+hash+ " = ?= "+HASH);}

          if(hash != HASH) {
            //check: do we need to handle other request ???
            if(RMIConnection.DEBUG < 9) {System.out.println("REGISTRY REQUEST - unknown hash "+hash);}
            RMIConnection.writeException(out, "unkwown request");
          }
          else {
            switch(operation){
              case BIND:
              case REBIND:
                handleBind(oin, operation == REBIND);
                break;
              case LOOKUP:
                String name = (String)oin.readObject();
                if(RMIConnection.DEBUG < 6) {System.out.println("lookup Argument String: "+name);}
                RMIConnection.writeResponse(out,registry.lookup(name));
                break;

              case LIST:
                if(RMIConnection.DEBUG < 6) {System.out.println("listing registry");}
                RMIConnection.writeResponse(out,registry.list());
                break;

              case UNBIND:
                String remove = (String)oin.readObject();
                if(RMIConnection.DEBUG < 6) {System.out.println("Unbind Argument String: "+remove);}
                registry.unbind(remove);
                RMIConnection.writeVoidResponse(out);
                break;

              default:
                RMIConnection.writeException(out, "unkwown request");
            }
          }
        }
        else if(type == RMIConstants.PING){
          out.write(RMIConstants.PING_ACK);
        }
        else {
          RMIConnection.writeException(out, new RemoteException("invalid request"));
        }
      }
    }
    catch(RemoteException rt){
      RMIConnection.writeException(out, rt);
    }
    catch(Exception e){
      e.printStackTrace();
      RMIConnection.wrapException(out, e);
    }
    finally{
      try {
         s.close();
       } catch(IOException _){}
    }

    if(RMIConnection.DEBUG < 8) {System.out.println("RMIRegistryRequest finished !\n\n");}
  }

  private void handleBind(ObjectInputStream oin, boolean rebind) throws IOException, AlreadyBoundException, ClassNotFoundException {
    String name = (String)oin.readObject();
    if(RMIConnection.DEBUG < 6) {System.out.println("(RE)BIND Argument String: "+name);}
    Remote stub = (Remote)oin.readObject();
    if(RMIConnection.DEBUG < 6) {System.out.println("Argument RemoteStub: "+stub);}
    if(rebind){
      registry.rebind(name, stub);
    }
    else {
      registry.bind(name, stub);
    }
    RMIConnection.writeVoidResponse(s.getOutputStream());
  }
}

