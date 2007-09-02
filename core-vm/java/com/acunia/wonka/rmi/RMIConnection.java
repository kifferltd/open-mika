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

import java.io.*;
import java.net.*;
import java.lang.reflect.Method;
import java.rmi.*;
import java.rmi.dgc.VMID;
import java.rmi.server.*;
import java.util.HashMap;
import java.util.Hashtable;
public class RMIConnection {

  private RMIConnection(){}

  /** integer indicating the debug level -1 = All ; the higher value the less debug you get. Values higher then 10 should not be used */
  static final int DEBUG = 11;

  private static final byte[] headers = {0x4a, 0x52, 0x4d, 0x49, 0x00, 0x02, 0x4b};
  private static final Class REMOTE_CLASS = java.rmi.Remote.class;

  /** the 1 and only VMID for this vm*/
  static final VMID TheVMID = new VMID();

  static final RMIPermission RMIPERMISSION = new RMIPermission();

  static Hashtable exports = new Hashtable(3);
  static final Hashtable idData = new Hashtable(13);

  private native static long getStringHash(String name);

  private static long getMethodHash(Method method){
    StringBuffer name = new StringBuffer(128);
    name.append(method.getName());
    name.append('(');
    Class[] args = method.getParameterTypes();
    for (int i=0 ; i < args.length ; i++){
      name.append(class2String(args[i]));
    }
    name.append(")");
    name.append(class2String(method.getReturnType()));
    long l = getStringHash(name.toString());
    if(DEBUG < 5) {System.out.println("RMIConnection: METHOD NAME for hash '"+name+"' resulted in "+l);}
    return l;
  }

  private static String class2String(Class c){
    String name;
    String prefix = "";
    while(c.isArray()){
      prefix = "[" + prefix;
      c = c.getComponentType();
    }
    if(c.isPrimitive()){
      if(c == Integer.TYPE){
        name = "I";
      }
      else if(c == Long.TYPE){
        name = "J";
      }
      else if(c == Double.TYPE){
        name = "D";
      }
      else if(c == Float.TYPE){
        name = "F";
      }
      else if(c == Short.TYPE){
        name = "S";
      }
      else if(c == Byte.TYPE){
        name = "B";
      }
      else if(c == Character.TYPE){
        name = "C";
      }
      else if(c == Boolean.TYPE){
        name = "Z";
      }
      else {
        name = "V";
      }
    }
    else {
      name = "L"+c.getName()+";";
      //the documentation says the name should be with dots but it only works with the slashes
      name = name.replace('.','/');
    }
    return prefix + name;
  }

/*
** public API needed by java.rmi.server.* classes
*/
  public static String getClientHost() throws ServerNotActiveException {
    String name;
    synchronized(RMIRequestHandler.clientHostNames){
      name = (String)RMIRequestHandler.clientHostNames.get(Thread.currentThread());
    }
    if(name == null){
      throw new ServerNotActiveException("current Thread is not handling any RMI requests");
    }
    return name;
  }

  public static void setupTable(Hashtable map){
    securityCheck();
    exports = map;
  }

  public static void registerObjIDData(ObjID id, ObjIDData data) throws RemoteException {
    securityCheck();
    Class[] interfaces = data.stub.getClass().getInterfaces();
    int length = interfaces.length;
    if(length == 0){
      throw new RemoteException("bad stub: no interfaces implemented");
    }
    HashMap hashes2ID = new HashMap(13);
    boolean remoteOnly = true;
    for(int i = 0 ; i < length ; i++){
      if(!REMOTE_CLASS.equals(interfaces[i])){
        //all interface methods should be public ...
        remoteOnly = false;
        Method[] methods = interfaces[i].getMethods();
        for(int k = 0 ; k < methods.length ; k++){
          if(DEBUG < 5){System.out.println("method "+k+" = "+methods[k]);}
          hashes2ID.put(new Long(getMethodHash(methods[k])), methods[k]);
        }
      }
    }

    if(remoteOnly){
      throw new RemoteException("bad stub: only implements the Remote interface");
    }

    data.methods = hashes2ID;
    idData.put(id, data);
  }

  public static void deregisterObjIDData(ObjID id){
    securityCheck();
    idData.remove(id);
  }

  static void securityCheck(){
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      java.security.AccessController.checkPermission(RMIPERMISSION);
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkPermission(RMIPERMISSION);
      }
    }
  }

/*
** package protected methods contianing functions to handle the RMIStream protocol ...
*/

/**
** ask for an object to server (or void request if returns is false). This method uses the CALL scheme.
*/
  static Object requestObject(Socket socket, long hash, int operation, ObjID id, Object arg, boolean returns) throws RemoteException {
    Object o = null;
    int rd = 0;
    try {
      handShake(socket);
      if(DEBUG < 6) {System.out.println("handshake complete");}

      OutputStream out = socket.getOutputStream();
      out.write(RMIConstants.CALL);
      ObjectOutputStream oos = new RMIObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
      id.write(oos);
      oos.writeInt(operation);
      oos.writeLong(hash);
      if(arg instanceof ParameterSet){
        ((ParameterSet)arg).writeData(oos);
      }
      else {
        oos.writeObject(arg);
      }
      oos.flush();
      if(DEBUG < 6) {System.out.println("wrote request");}

      InputStream in = socket.getInputStream();
      rd = in.read();
      if((rd & 0xff) != RMIConstants.RETURN_DATA){
        if(DEBUG < 9) {System.out.println("Oops got :"+(rd & 0xff));}
      }
      if(DEBUG < 6) {System.out.println("reading request");}

      ObjectInputStream ois = new RMIObjectInputStream(new PushbackInputStream(in));
      rd = ois.read();
      if(DEBUG < 6) {System.out.println("read Header");}

      UID uid = UID.read(ois);
      if(DEBUG < 6) {System.out.println("UID = "+uid);}
      if(returns || rd == RMIConstants.EXCEPTION){
        o = ois.readObject();
      }
      else {
        return null;
      }
    }
    catch(Exception e){
      if(DEBUG < 9) {System.out.println("CAUGTH EXCEPTION WHILE CONTACTING SERVER");}
      throw new RemoteException("oops",e);
    }
    if(DEBUG < 6) {System.out.println("verifying object");}

    return verifyObject(o, rd);
  }

/**
** ask for a primitive result to the server and wraps it up (void not allowed). This method uses the CALL scheme.
*/
  static Object requestPrimitive(Socket socket, long hash, int operation, ObjID id, Object arg, Class primitiveClass) throws RemoteException {
    Object o = null;
    int rd = 0;
    try {
      handShake(socket);

      OutputStream out = socket.getOutputStream();
      out.write(RMIConstants.CALL);
      ObjectOutputStream oos = new RMIObjectOutputStream(socket.getOutputStream());
      id.write(oos);
      oos.writeInt(operation);
      oos.writeLong(hash);

      if(arg instanceof ParameterSet){
        ((ParameterSet)arg).writeData(oos);
      }
      else {
        oos.writeObject(arg);
      }
      oos.flush();

      InputStream in = socket.getInputStream();
      rd = in.read();
      if((rd & 0xff) != RMIConstants.RETURN_DATA){
        if(DEBUG < 8) {System.out.println("Oops got :"+(rd & 0xff));}
      }
      ObjectInputStream ois = new RMIObjectInputStream(new PushbackInputStream(in));
      rd = ois.read();

      if(DEBUG < 6) {System.out.println("type is "+(rd & 0xff));}

      UID uid = UID.read(ois);
      if(DEBUG < 6) {System.out.println("UID = "+uid);}
      if(rd == RMIConstants.EXCEPTION){
        o = ois.readObject();
      }
      else {
        return ParameterSet.readPrimitive(primitiveClass, ois);
      }
    }
    catch(Exception e){
      if(DEBUG < 6) {System.out.println("CAUGTH EXCEPTION WHILE CONTACTING SERVER");}
      throw new RemoteException("oops",e);
    }
    return verifyObject(o, rd);
  }

/**
** ping another VM for liveness
*/
  static void pingVM(Socket socket) throws RemoteException {
    int rd = 0;
    try {
      handShake(socket);
      OutputStream out = socket.getOutputStream();
      out.write(RMIConstants.PING);
      InputStream in = socket.getInputStream();
      rd = in.read();
    }
    catch(Exception e){
      if(DEBUG < 9) {System.out.println("CAUGTH EXCEPTION WHILE CONTACTING SERVER");}
      throw new RemoteException("oops",e);
    }
    if((rd & 0xff) != RMIConstants.PING_ACK){
      throw new RemoteException("bad response "+rd);
    }
  }

/**
** implements the DGCAck scheme ...
*/
  static Object sendDGCAck(Socket socket, UID uid) throws RemoteException {
    Object o = null;
    int rd = 0;
    try {
      handShake(socket);
      OutputStream out = socket.getOutputStream();
      out.write(RMIConstants.DGC_ACK);
      uid.write(new DataOutputStream(out));
      out.flush();

      InputStream in = socket.getInputStream();
      rd = in.read();
      if((rd & 0xff) != RMIConstants.RETURN_DATA){
        if(DEBUG < 8) {System.out.println("Oops got :"+(rd & 0xff));}
      }
      ObjectInputStream ois = new RMIObjectInputStream(new PushbackInputStream(in));
      rd = ois.read();
      uid = UID.read(ois);
      if(DEBUG < 6) {System.out.println("UID = "+uid);}
      o = ois.readObject();
    }
    catch(Exception e){
      if(DEBUG < 6) {System.out.println("CAUGTH EXCEPTION WHILE CONTACTING SERVER");}
      throw new RemoteException("oops",e);
    }
    return verifyObject(o, rd);
  }

/**
** writes a void response to out ...
*/
  static void writeVoidResponse(OutputStream out){
    try {
      if(DEBUG < 8) {System.out.println("writing a void response to the stream");}
      out.write(RMIConstants.RETURN_DATA);
      ObjectOutputStream oout = new RMIObjectOutputStream(out);
      oout.write(RMIConstants.RETURN_VALUE);
      new UID().write(oout);
      oout.flush();
    }
    catch(IOException ioe){
      ioe.printStackTrace();
    }
  }

/**
** writes Object response to out ...
*/
  static void writeResponse(OutputStream out, Object response){
    try {
      out.write(RMIConstants.RETURN_DATA);
      ObjectOutputStream oout = new RMIObjectOutputStream(out);
      oout.write(RMIConstants.RETURN_VALUE);
      new UID().write(oout);
      oout.writeObject(response);
      oout.flush();
    }
    catch(IOException ioe){ }
  }

/**
** writes an Exception (with message as message) to out ...
*/
  static void writeException(OutputStream out, String message) {
    try {
      out.write(RMIConstants.RETURN_DATA);
      ObjectOutputStream oout = new RMIObjectOutputStream(out);
      oout.write(RMIConstants.EXCEPTION);
      new UID().write(oout);
      oout.writeObject(new RemoteException(message));
      oout.flush();
    }
    catch(IOException ioe){ }
  }


/**
** writes an RemoteException to out ...
*/
  static void writeException(OutputStream out, RemoteException re) {
    try {
      out.write(RMIConstants.RETURN_DATA);
      ObjectOutputStream oout = new RMIObjectOutputStream(out);
      oout.write(RMIConstants.EXCEPTION);
      new UID().write(oout);
      oout.writeObject(re);
      oout.flush();
    }
    catch(IOException ioe){ }
  }

/**
** wrap an Exception into a RemoteException and then writes it to out ...
*/
  static void wrapException(OutputStream out, Throwable t){
    try {
      out.write(RMIConstants.RETURN_DATA);
      ObjectOutputStream oout = new RMIObjectOutputStream(out);
      oout.write(RMIConstants.EXCEPTION);
      new UID().write(oout);
      oout.writeObject(new RemoteException("RMI invocation fialed",t));
      oout.flush();
    }
    catch(IOException ioe){ }
  }

/**
** initiates the serverside hand shake ...
*/
  static void serverSideHandShake(Socket socket) throws IOException {
    InputStream in = socket.getInputStream();
    byte[] bytes = new byte[7];
    int len = in.read(bytes);

    if(len == -1){
      throw new IOException("no data available");
    }

    while(len < 7){
      int rd = in.read(bytes,len, 7-len);
      if(rd == - 1){
        throw new IOException("header is to short");
      }
      len += rd;
    }

    if(DEBUG < 5) {System.out.println("RESPONSE IS '"+new String(bytes,0,len)+"'");}
    if(!java.util.Arrays.equals(bytes, headers)){
      throw new RemoteException("invalid header");
    }

    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
    out.write(RMIConstants.PROTOCOL_ACK);
    out.writeUTF(socket.getInetAddress().getHostAddress());
    out.writeInt(socket.getPort());
  }

/**
** writes the RMI headers to the stream and handles the client side handshake
*/
  static void handShake(Socket socket) throws IOException {
    OutputStream out = socket.getOutputStream();
    out.write(headers);
    DataInputStream in = new DataInputStream(socket.getInputStream());
    int answer = in.read();
    if(answer != RMIConstants.PROTOCOL_ACK){
      throw new IOException("GAME OVER");
    }

    /** the Server responds by sending the address and the port of the socket connected to the server */
    String address = in.readUTF();
    if(DEBUG < 6) {System.out.println("address = '"+address+"'");}
    int port = in.readInt();
    if(DEBUG < 6) {System.out.println("port = '"+port+"'"); }
    /** we don't check this information, the only thing we could verify is the port */

    DataOutputStream sout = new DataOutputStream(out);
    sout.writeUTF(socket.getInetAddress().getHostAddress());
    sout.writeInt(socket.getPort());
    sout.flush();
  }

/**
** verifies the server response ...
*/
  private static Object verifyObject(Object o, int type) throws RemoteException {
    if(type == RMIConstants.RETURN_VALUE){
      return o;
    }
    else if(type == RMIConstants.EXCEPTION && o instanceof Throwable){
      //check if correct exception is thrown
      if(o instanceof RemoteException){
        if(DEBUG < 9) {System.out.println("REPLY IS A REMOTEEXCEPTION");}
        throw (RemoteException)o;
      }
      if(o instanceof RuntimeException){
        if(DEBUG < 9) {System.out.println("REPLY IS A RUNTIMEEXCEPTION");}
        throw (RuntimeException)o;
      }
      if(o instanceof Exception){
        if(DEBUG < 9) {System.out.println("REPLY IS AN ERROR");}
        throw new UnexpectedException("Unexpected server Exception",(Exception)o);
      }
      if(o instanceof Error){
        if(DEBUG < 9) {System.out.println("REPLY IS AN ERROR");}
        throw new ServerError("Unexpected server Error",(Error)o);
      }

      throw new RemoteException("UnexpectedExeption",(Throwable)o);
    }
    throw new RemoteException("invalid response");
  }
}

