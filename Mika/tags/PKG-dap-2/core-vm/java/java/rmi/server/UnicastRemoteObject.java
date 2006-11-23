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

package java.rmi.server;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Hashtable;

import com.acunia.wonka.rmi.ObjIDData;
import com.acunia.wonka.rmi.RMIAcceptThread;
import com.acunia.wonka.rmi.RMIConnection;
import com.acunia.wonka.rmi.UnicastRef;

public class UnicastRemoteObject extends RemoteServer {

  private static final long serialVersionUID = 4974527148936298033L;

  static final Hashtable exports;

  static {
    exports = new Hashtable(13);

    AccessController.doPrivileged(new PrivilegedAction(){
      public Object run(){
        RMIConnection.setupTable(exports);
        return null;
      }
    });
  }
  
  // MODIFIED: Has to be transient
  //private transient int port;
  private transient RMIClientSocketFactory csf;
  private transient RMIServerSocketFactory ssf;

  protected UnicastRemoteObject() throws RemoteException {
    this(0,null,null);
  }
  
  protected UnicastRemoteObject(int port) throws RemoteException {
    this(port,null,null);
  }
  
  protected UnicastRemoteObject(int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf)  throws RemoteException {
    this.csf = (csf == null ? RMISocketFactory.getRMISocketFactory() : csf);
    this.ssf = (ssf == null ? RMISocketFactory.getRMISocketFactory() : ssf);
    exportObject(this, port,this.csf, this.ssf);

    //this.port = ((ObjIDData)exports.get(this)).server.getLocalPort();
  }
  
  public Object clone() throws CloneNotSupportedException {
    try {
      UnicastRemoteObject myClone=(UnicastRemoteObject)super.clone();
      exportObject(myClone);
      return myClone;
    }
    catch (RemoteException e) {
      throw new ServerCloneException("Couldn't clone UnicastRemoteObject: "+this, e);
    }
  }
  
  public static RemoteStub exportObject(Remote obj) throws RemoteException {
    return (RemoteStub)exportObject(obj, 0, null, null);
  }

  public static Remote exportObject(Remote obj, int port) throws RemoteException {
    return exportObject(obj, port, null, null);
  }

  public static Remote exportObject(Remote obj, int port, RMIClientSocketFactory csf, RMIServerSocketFactory ssf)  throws RemoteException {
    csf = (csf == null ? RMISocketFactory.getRMISocketFactory() : csf);
    ssf = (ssf == null ? RMISocketFactory.getRMISocketFactory() : ssf);
    ServerSocket ss = null;
    do {
      try {
        ss = ssf.createServerSocket(port);
      }
      catch(IOException ioe){
        //ask FailureHandler ...
        RMIFailureHandler fh = RMISocketFactory.theFailureHandler;
        if(fh != null){
          if(!fh.failure(ioe)){
            throw new RemoteException("failed to create ServerSocket",ioe);
          }
        }
        //if no FailureHandler we keep trying to create a serversocket !!!
      }
    } while(ss == null);

    if(exports.containsKey(obj)){
      ObjIDData data = (ObjIDData)exports.get(obj);
      try {
        data.server.close();
        new RegisterAction(data.id, null);
      }
      catch(IOException ioe){
        throw new RemoteException("exportObject failed",ioe);
      }
    }
    try {
      Class remoteClass = obj.getClass();
      String name = remoteClass.getName() + "_Stub";
      RemoteStub stub = null;
      Class cl = Class.forName(name,true, remoteClass.getClassLoader());
      ObjID id = new ObjID();

      // MODIFIED: In the remote object that was passed as argument the 
      // unicastRef has to be set as remote reference. Otherwise export of
      // remote classes that inherit from UnicastRemoteObject will not
      // work. They call super(), and in constructor of UnicastRemoteObject
      // exportObject is called.
      // Secondly the API of exportObject specs that the remoteobj passed as
      // argument will be exported. So its internal remoteref must be set.
      RemoteRef ref = new UnicastRef(getAddress(ss), ss.getLocalPort(), id, csf);
      RemoteObject myRemoteObj = (RemoteObject)obj;
      myRemoteObj.ref = ref;

      try {
        Constructor constr = cl.getDeclaredConstructor(new Class[]{java.rmi.server.RemoteRef.class});
        stub = (RemoteStub)constr.newInstance(new Object[]{ref});
      }
      catch(NoSuchMethodException nsme){
        stub = (RemoteStub)cl.newInstance();
        stub.ref = ref;
      }

      ObjIDData idData = new ObjIDData(ss,obj,stub,id);
      exports.put(obj,idData);

      new RegisterAction(id, idData);
      new RMIAcceptThread(ss);
      return stub;
    }
    catch(Exception e){
      throw new RemoteException("export failed", e);
    }
  }

  public static boolean unexportObject(Remote obj, boolean force) throws NoSuchObjectException {
    ObjIDData data = (ObjIDData)exports.get(obj);
    if(data == null){
      throw new NoSuchObjectException("object "+obj+" not found");
    }
    try {
      data.server.close();
      new RegisterAction(data.id, null);
    }
    catch(IOException ioe){
      return false;
    }
    exports.remove(obj);
    return true;
  }
/*
  private static RemoteStub exportObject(Remote obj, ServerSocket ss) throws RemoteException {
    if(exports.containsKey(obj)){
      ObjIDData data = (ObjIDData)exports.get(obj);
      try {
        data.server.close();
        new RegisterAction(id, null);
      }
      catch(IOException ioe){
        throw new RemoteException("exportObject failed",ioe);
      }
    }
    try {
      Class remoteClass = obj.getClass();
      String name = remoteClass.getName() + "_Stub";
      RemoteStub stub = null;
      Class cl = Class.forName(name,true, remoteClass.getClassLoader());
      //ObjID id = new ObjID(0x0L, new UID(0x07f5ea7, System.currentTimeMillis(),(short)0x8000));
      ObjID id = new ObjID();
      try {
        Constructor constr = cl.getDeclaredConstructor(new Class[]{java.rmi.server.RemoteRef.class});
	RemoteRef ref = new UnicastRef(getAddress(ss), ss.getLocalPort(), id);
        stub = (RemoteStub)constr.newInstance(new Object[]{ref});
      }
      catch(NoSuchMethodException nsme){
        stub = (RemoteStub)cl.newInstance();
        stub.ref = new UnicastRef(getAddress(ss), ss.getLocalPort(), id);
      }

      //TODO ... check creation ...
      ObjIDData idData = new ObjIDData(ss,obj,stub,id);
      exports.put(obj,idData);
      new RegisterAction(id, idData);
      new RMIAcceptThread(ss);
      return stub;
    }
    catch(Exception e){
      throw new RemoteException("export failed", e);
    }
  }
*/

  private static String getAddress(ServerSocket ss){
    String address = ss.getInetAddress().getHostAddress();
    if(address.equals("0.0.0.0")){
      try {
        address = InetAddress.getLocalHost().getHostAddress();
      }
      catch(java.net.UnknownHostException uhe){
        address = "127.0.0.1";
      }
    }
    return address;
  }

  static final class RegisterAction implements PrivilegedExceptionAction {

     private ObjID id;
     private ObjIDData data;

     RegisterAction(ObjID id, ObjIDData data) throws RemoteException {
       this.id = id;
       this.data = data;
       try {
         AccessController.doPrivileged(this);
       }
       catch(PrivilegedActionException pae){
         throw (RemoteException)pae.getException();
       }
     }

     public Object run() throws RemoteException {
       if(data == null){
         RMIConnection.deregisterObjIDData(id);
       }
       else {
         RMIConnection.registerObjIDData(id, data);
       }
       return null;
     }
  }
}

