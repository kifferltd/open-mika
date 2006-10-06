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

import java.io.*;
import java.net.*;
import java.rmi.server.*;
import java.rmi.dgc.*;
import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;
import java.lang.reflect.Method;
import java.util.WeakHashMap;

class RMIRequestHandler extends Thread {

  //We use a WeakHashMap to make sure no entries stay around forever ...
  static final WeakHashMap clientHostNames = new WeakHashMap(13);

  private Socket s;

  RMIRequestHandler(Socket s){
    super("RMIRequestHandler Thread "+s);
    if(RMIConnection.DEBUG < 6) {System.out.println("Constructing RMIRequestHandler "+this+" for "+s);}
    this.s = s;
    try {
      s.setSoLinger(true, 30000);
    }
    catch(java.net.SocketException se){}
    this.start();
  }

  public void run(){
    if(RMIConnection.DEBUG < 6) {System.out.println("RMIRequestHandler started "+this);}
    synchronized(clientHostNames){
      clientHostNames.put(this,s.getInetAddress().getHostName());
    }
    try {
      RMIConnection.serverSideHandShake(s);
      InputStream in = s.getInputStream();
      DataOutputStream out = new DataOutputStream(s.getOutputStream());

      DataInputStream din = new DataInputStream(in);
      String address = din.readUTF();
      if(RMIConnection.DEBUG < 6) {System.out.println("Address :"+address); }
      int port = din.readInt();
      if(RMIConnection.DEBUG < 6) {System.out.println("Port :"+port); }
      while(true){
        int type = din.read();
        if(RMIConnection.DEBUG < 6) {System.out.println("Reading resonse "+Integer.toHexString(0xff & type));}
        if(type == RMIConstants.CALL){
          ObjectInputStream oin = new RMIObjectInputStream(new PushbackInputStream(in));
          ObjID  id = ObjID.read(oin);
          if(RMIConnection.DEBUG < 6) {System.out.println("ObjID: "+id);}
          int operation = oin.readInt();
          if(RMIConnection.DEBUG < 6) {System.out.println("Operation: "+operation);}
          long hash = oin.readLong();
          if(RMIConnection.DEBUG < 6) {System.out.println("Hash: "+hash);}

          if(hash == RMIConstants.HASH_DIRTY){
            dirty(oin, operation, out);
          }
          else{
            if(RMIConnection.DEBUG < 6) {System.out.println("Request to invoke a method");}
            ObjIDData data = (ObjIDData) RMIConnection.idData.get(id);
            if(RMIConnection.DEBUG < 6) {System.out.println("Request to invoke a method "+data+", id = "+id);}
            if(data == null){
              RMIConnection.wrapException(out, new NoSuchObjectException("object not found"));
            }
            else {
              Method method = (Method)data.methods.get(new Long(hash));
              if(method == null){
                RMIConnection.wrapException(out, new NoSuchObjectException("method with hash '"+hash+"' not found in object"));
              }
              else {
                dispatch(oin, operation, out, data, method);
              }
            }
          }
        }
        else if(type == RMIConstants.PING){
          out.write(RMIConstants.PING_ACK);
        }
        else {
          if(RMIConnection.DEBUG < 6) {System.out.println("Request to do "+Integer.toHexString(type)+" =?= DgcAck");}
          if(type == -1){
            break;
          }
        }
      }
    }
    catch(Throwable e){
      e.printStackTrace();
    }
    finally  {
      try {
        s.close();
      } catch(Exception _){}
    }
    synchronized(clientHostNames){
      clientHostNames.remove(this);
    }
    if(RMIConnection.DEBUG < 6) {System.out.println("RMIRequestHandler finished execution "+this);}
  }

/**
** still TODO DGC ...
*/
  private void dirty(ObjectInputStream oin, int operation, OutputStream out) throws IOException, ClassNotFoundException {
    out.write(RMIConstants.RETURN_DATA);
    ObjectOutputStream oout = new RMIObjectOutputStream(out);
    if(operation == RMIConstants.OPERATION_DIRTY){
      ObjID[] id = (ObjID[]) oin.readObject();
      if(RMIConnection.DEBUG < 8) {System.out.println("\ndirty is called");}
      if(RMIConnection.DEBUG < 6) {System.out.println("Argument ObjID[]: "+id+" of length "+id.length);}
      if(RMIConnection.DEBUG < 6){
        for(int i = 0 ; i < id.length ; i++){
          System.out.println("Argument ObjID["+i+"]: "+id[i]);
        }
      }

      long sequenceNumber = oin.readLong();
      if(RMIConnection.DEBUG < 6) {System.out.println("sequenceNumber ??? "+sequenceNumber);}
      Lease lease = (Lease)oin.readObject();
      if(RMIConnection.DEBUG < 6) {System.out.println("Argument Lease: "+lease+"\n");}
      oout.write(RMIConstants.RETURN_VALUE);
      new UID().write(oout);
      VMID vmID = lease.getVMID();
      oout.writeObject(new Lease(vmID, lease.getValue()));
    }
    else if(operation == RMIConstants.OPERATION_CLEAN){
      ObjID[] id = (ObjID[]) oin.readObject();
      if(RMIConnection.DEBUG < 8) {System.out.println("\nclean is called");}
      if(RMIConnection.DEBUG < 6) {System.out.println("\nArgument ObjID[]: "+id);}
      long sequenceNumber = oin.readLong();
      if(RMIConnection.DEBUG < 6) {System.out.println("sequenceNumber ??? "+sequenceNumber);}
      VMID vmid = (VMID)oin.readObject();
      if(RMIConnection.DEBUG < 6) {System.out.println("Argument VMID: "+vmid);}
      boolean strong = oin.readBoolean();
      if(RMIConnection.DEBUG < 6) {System.out.println("Argument boolean: "+strong+"\n");}
      oout.write(RMIConstants.RETURN_VALUE);
      new UID().write(oout);
    }
    else {
      oout.write(RMIConstants.EXCEPTION);
      new UID().write(oout);
      oout.writeObject(new RemoteException("invalid operation"));
    }
    oout.flush();
  }

  private void dispatch(ObjectInputStream oin, int operation, OutputStream out, ObjIDData data, Method method)
    throws IOException, ClassNotFoundException {

    if(RMIConnection.DEBUG < 6) {System.out.println("Invoking method: "+method+"\n\tObjIDData data = "+data );}
    Class[] classes = method.getParameterTypes();
    Object[] params = new Object[classes.length];
    for(int i = 0 ; i < classes.length ; i++){
      if(classes[i].isPrimitive()){
        params[i] = ParameterSet.readPrimitive(classes[i], oin);
      }
      else {
        params[i] = oin.readObject();
        if(RMIConnection.DEBUG < 6) {
          System.out.println("Marshalling "+params[i]+" class = "+(null ==  params[i] ? null : params[i].getClass()) +" <--> "+classes[i]);
        }
      }
    }
    try {
      if(RMIConnection.DEBUG < 6) {System.out.println("invoking method on "+data.impl);}
      Object o = method.invoke(data.impl, params);
      if(RMIConnection.DEBUG < 6) {
        System.out.println("done invoking method on "+data.impl+" got '"+o+"' of "+(o == null ? o : o.getClass()));
      }
      out.write(RMIConstants.RETURN_DATA);
      if(RMIConnection.DEBUG < 6) {System.out.println("SETTING UP STREAM");}
      ObjectOutputStream oout = new RMIObjectOutputStream(out);
      oout.write(RMIConstants.RETURN_VALUE);
      new UID().write(oout);
      if(RMIConnection.DEBUG < 6) {System.out.println("SERIALIZING RESPONSE");}
      Class returntype = method.getReturnType();
      if(returntype == Void.TYPE){
        if(RMIConnection.DEBUG < 6) {System.out.println("Called a void method "+o);}
      }
      else if(returntype.isPrimitive()){
        ParameterSet.writePrimitive(returntype, o, oout);
      }
      else {
        oout.writeObject(o);
      }
      oout.flush();
      if(RMIConnection.DEBUG < 6) {System.out.println("REQUEST IS DONE");}
    }
    catch(IllegalAccessException iae){
      RMIConnection.wrapException(out,iae);
    }
    catch(java.lang.reflect.InvocationTargetException ite){
      RMIConnection.wrapException(out,ite);
    }
  }
}

