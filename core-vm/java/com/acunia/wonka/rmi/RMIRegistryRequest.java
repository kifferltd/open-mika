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

