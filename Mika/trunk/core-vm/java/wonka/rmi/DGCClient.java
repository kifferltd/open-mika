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

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.lang.ref.ReferenceQueue;
import java.net.Socket;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.dgc.Lease;
import java.rmi.server.ObjID;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteRef;
import java.rmi.server.UID;
import java.util.Hashtable;
import java.util.TimerTask;
import java.util.Vector;

import wonka.vm.SystemTimer;

/**
** The DGCClient will manage all RemoteObjects recieved by RMI Servers.
**
** the DGCClient will add 1 TimerTask to the SystemTimer for each different RMI Servers it is connectected to. This
** will allow us to renew all Leases to one server in one call. An additional task is added to check the ReferenceQueue
** and to do the clean calls to the server if a RemoteObject is collected.
**
** TODO: make DGCClients use VMID as key.
*/
public class DGCClient {

  private static final ObjID DGCID = new ObjID(2);
  private static final ObjID[] ID_ARRAY = new ObjID[0];
  static final ReferenceQueue QUEUE = new ReferenceQueue();
  private static final Lease LEASE = new Lease(RMIConnection.TheVMID,
                                        Long.getLong("java.rmi.dgc.leaseValue", 10L * 60L * 1000L /* = 10 minutes */).longValue());


  static Hashtable remotes = new Hashtable(11);
  private static Hashtable DGCClients = new Hashtable(7);
  private static long sequenceNumber;

  /** always synchronize the use of the cleanerTask on DGCClients */
  private static DGCClientTask cleanerTask;


  static void registerRemote(Remote obj){
    if(obj instanceof RemoteObject){
      RemoteObject remote = (RemoteObject) obj;
      registerObject(remote.getRef());
    }
  }


  /** all objects registered to DGCClient will be marked as dirty to the DGC */
  /** DGCClient will only keep a WeakReference to this Object and will send a clean call to the server */
  /** the DGCClient is also responseble for renewing the leases */
  synchronized static void registerObject(RemoteRef obj){
    Object o = remotes.get(obj);
    if(o == null){
      if(obj instanceof UnicastRef){
        UnicastRef ref = (UnicastRef) obj;
        String key = ref.address + ref.port;
        DGCClient client = (DGCClient) DGCClients.get(key);
        if(client == null){
          client = new DGCClient(ref.address, ref.port, ref.csf);
        }
        DGCClientReference dgcRef = new DGCClientReference(obj, QUEUE, ref.id);
        remotes.put(dgcRef,client);
        synchronized(DGCClients){
          DGCClients.put(key,client);
          if(cleanerTask == null){
            cleanerTask = new DGCClientTask();
            SystemTimer.scheduleSystemTask(cleanerTask, 750);
          }
        }
        new Registrator(dgcRef, obj, client, ref.id);
      }
      //TODO check what else can be recieved ...
    }
    /** else it is already registered, no extra steps needed */
  }

  private Vector ids;
  private DGCClientTask currentTask;
  private int port;
  private String address;
  private RMIClientSocketFactory csf;
  //private VMID myVMID = RMIConnection.TheVMID;
  private long duration;

  private DGCClient(String addr, int p, RMIClientSocketFactory fact){
    ids = new Vector();
    address = addr;
    port = p;
    csf = fact;
  }

  synchronized void add(ObjID id, Object key)throws IOException {
    Lease lease = reportDirty(new ObjID[]{id});

    if(lease != null){
      long value = lease.getValue();
      ids.add(id);

      //TODO check VMID. what todo if we get different VMID ...

      if(value < duration){
        if(currentTask != null){
          currentTask.cancel();
        }
        if (value < 500){
          /** we don't want to renew the Lease at a high speed rate it can cripple the system */
          value = 500;
        }
        duration = value;
        currentTask = new DGCClientTask(this);
        if(RMIConnection.DEBUG < 7){System.out.println("Starting "+currentTask+" with "+value+" delay");}
        SystemTimer.scheduleOneTimeSystemTask(currentTask, value);
      }
      else if (currentTask == null){
        currentTask = new DGCClientTask(this);
        value -= 500;
        if (value < 500){
          /** we don't want to renew the Lease at a high speed rate it can cripple the system */
          value = 500;
        }
        if(RMIConnection.DEBUG < 7){System.out.println("Starting "+currentTask+" with "+value+" delay");}
        SystemTimer.scheduleOneTimeSystemTask(currentTask, value);
      }
    }
    else {
      remotes.remove(key);
      if(ids.isEmpty()){
        synchronized(DGCClients){
          DGCClients.remove(address+port);
          if(DGCClients.isEmpty() && cleanerTask != null){
            cleanerTask.cancel();
            cleanerTask = null;
          }
        }
      }
    }
  }

  synchronized void remove(ObjID id, Object key) throws IOException {
    if(ids.remove(id)){
      if(RMIConnection.DEBUG < 7){System.out.println("DGCClient.remove("+id+", ??) calling reportClean");}
      reportClean(new ObjID[]{id}, csf.createSocket(address,port), false);
      if(ids.isEmpty()){
        if(RMIConnection.DEBUG < 7){System.out.println("DGCClient.remove("+id+", ??) stopping DGCClient");}
        if(currentTask != null){
          currentTask.cancel();
          currentTask = null;
        }
        synchronized(DGCClients){
          DGCClients.remove(address+port);
          if(RMIConnection.DEBUG < 7){System.out.println("DGCClient.remove() cleanup DGCClients" +DGCClients);}
          if(DGCClients.isEmpty() && cleanerTask != null){
            if(RMIConnection.DEBUG < 7){System.out.println("DGCClient.remove() cancelling cleanerTask");}
            cleanerTask.cancel();
            cleanerTask = null;
          }
        }
      }
      else if(RMIConnection.DEBUG < 7){System.out.println("DGCClient.remove("+id+", ??) continuing DGCClient");}
    }
  }

  synchronized void renewLeases() throws IOException {
    ObjID[] o = (ObjID[])ids.toArray(ID_ARRAY);
    if(o == ID_ARRAY){
      /** this means the vector is empty */
      currentTask = null;
      return;
    }
    Lease lease = reportDirty(o);
    if(lease != null){
      /** the lease should be renewed before it expires, so we take half a second to to get it renewed */
      long duration = lease.getValue() - 500;

      if (duration < 500){
        /** we don't want to renew the Lease at a high speed rate it can cripple the system */
        duration = 500;
      }
      this.duration = duration;
      currentTask = new DGCClientTask(this);
      SystemTimer.scheduleOneTimeSystemTask(currentTask, duration);
    }
    else {
      /** we clean up */
      synchronized(DGCClients){
        DGCClients.remove(address+port);
        if(DGCClients.isEmpty() && cleanerTask != null){
          cleanerTask.cancel();
          cleanerTask = null;
        }
      }
      //TODO remove all references to 'this' from the remotes hashtable.
    }
  }

  private void reportClean(Object arg, Socket socket, boolean strong){
    try {
      RMIConnection.handShake(socket);
      if(RMIConnection.DEBUG < 6) {System.out.println("handshake complete");}

      OutputStream out = socket.getOutputStream();
      out.write(RMIConstants.CALL);
      ObjectOutputStream oos = new RMIObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
      DGCID.write(oos);
      oos.writeInt(RMIConstants.OPERATION_CLEAN);
      oos.writeLong(RMIConstants.HASH_DIRTY);
      oos.writeObject(arg);
      /** no need to synchronize the use of the sequenceNumber. The same number can be used to contact different VM's */
      oos.writeLong(sequenceNumber++);
      oos.writeObject(RMIConnection.TheVMID);
      oos.writeBoolean(strong);
      oos.flush();
      if(RMIConnection.DEBUG < 6) {System.out.println("wrote request");}

      InputStream in = socket.getInputStream();
      int rd = in.read();
      if(RMIConnection.DEBUG < 8 && (rd & 0xff) != RMIConstants.RETURN_DATA){
        System.out.println("Oops got :"+(rd & 0xff));
      }
      if(RMIConnection.DEBUG < 6) {System.out.println("reading request");}

      ObjectInputStream ois = new RMIObjectInputStream(new PushbackInputStream(in));
      rd = ois.read();

      if(RMIConnection.DEBUG < 6) {System.out.println("read Header");}

      UID uid = UID.read(ois);
      if(RMIConnection.DEBUG < 6) {System.out.println("UID = "+uid);}
      //Object o = ois.readObject();

      if(RMIConnection.DEBUG < 9 && rd != RMIConstants.RETURN_VALUE){
        /** if o != null the clean called failed. We print out a debug message */
        System.out.println("DGCClient.remove: strong clean operation failed for "+arg);
        /** TODO: do we need to retry ? The Lease will expire anyway */
      }
    }
    catch(Exception _){
      /** TODO: do we need to retry ? The Lease will expire anyway */
    }
  }


  /** reportDirty will call a strong clean */
  private Lease reportDirty(Object arg) throws IOException {
    Socket socket = null;
    try {
      socket = csf.createSocket(address, port);
      RMIConnection.handShake(socket);
      if(RMIConnection.DEBUG < 6) {System.out.println("handshake complete");}

      OutputStream out = socket.getOutputStream();
      out.write(RMIConstants.CALL);
      ObjectOutputStream oos = new RMIObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
      DGCID.write(oos);
      oos.writeInt(RMIConstants.OPERATION_DIRTY);
      oos.writeLong(RMIConstants.HASH_DIRTY);
      oos.writeObject(arg);
      /** no need to synchronize the use of the sequenceNumber. The same number can be used to contact different VM's */
      oos.writeLong(sequenceNumber++);
      oos.writeObject(LEASE);

      oos.flush();
      if(RMIConnection.DEBUG < 6) {System.out.println("wrote request");}

      InputStream in = socket.getInputStream();
      int rd = in.read();
      if(RMIConnection.DEBUG < 8 && (rd & 0xff) != RMIConstants.RETURN_DATA){
        System.out.println("Oops got :"+(rd & 0xff));
      }
      if(RMIConnection.DEBUG < 6) {System.out.println("reading request");}

      ObjectInputStream ois = new RMIObjectInputStream(new PushbackInputStream(in));
      rd = ois.read();
      if(RMIConnection.DEBUG < 6) {System.out.println("read Header");}

      UID uid = UID.read(ois);
      if(RMIConnection.DEBUG < 6) {System.out.println("UID = "+uid);}
      Object o = ois.readObject();

      if(o instanceof Lease){
        return (Lease)o;
      }
    }
    catch(RemoteException e){
      /** if we get a RemoteException, the communication itself was not bad so we reuse the socket */
      reportClean(arg,socket,true);
    }
    catch(IOException io){
      if(socket != null){
        socket.close();
        /** if we get a IOException, the communication itself was bad so we create a new socket */
        reportClean(arg,csf.createSocket(address, port), true);
      }
    }
    catch(Exception _){
    }
    return null;
  }

  static class DGCClientTask extends TimerTask {

    private boolean started;
    private boolean cleanUp;
    private DGCClient client;

    DGCClientTask(DGCClient client){
      this.client = client;
    }

    DGCClientTask(){
      cleanUp = true;
    }

    public void run(){
      if(cleanUp){

        if(RMIConnection.DEBUG < 8){ System.out.println("DGCClient CleanupTask ...");}

        try {
          DGCClientReference ref = (DGCClientReference)QUEUE.poll();
          if(RMIConnection.DEBUG < 7){ System.out.println("DGCClient CleanupTask poll() returns "+ref);}
          while(ref != null){
            new Registrator((DGCClient)remotes.remove(ref), ref.id, ref);
            ref = (DGCClientReference)QUEUE.poll();
            if(RMIConnection.DEBUG < 7){ System.out.println("DGCClient CleanupTask poll() returns "+ref);}
          }
        }
        catch(Throwable t){
          if(RMIConnection.DEBUG < 9){ System.out.println("DGCClient CleanupTask got Exception "+t);}
        }
      }
      else if(!started){
        if(RMIConnection.DEBUG < 7){ System.out.println("DGCClientTask "+this+": start new Thread");}
        started = true;
        new Thread(this,"DGCClientTask Thread "+this).start();
      }
      else {
        try {
          if(RMIConnection.DEBUG < 8){ System.out.println("DGCClientTask "+this+": renewing Leases");}
          client.renewLeases();
        }
        catch(Throwable t){}
      }
    }
  }

  static class Registrator implements Runnable {

    private Object key;
    /* needed to keep this object alive */
    private Object remote;
    private DGCClient client;
    private ObjID id;
    private boolean remove;

    /** We keep a reference to the RemoteObject so the remoteObject cannot end up i/t ReferenceQueue before this thread stops */
    Registrator(Object key, RemoteRef obj, DGCClient c, ObjID id){
      remote = obj;
      client = c;
      this.key = key;
      this.id = id;
      new Thread(this, "DGCClient$Registrator for "+id).start();
    }

    Registrator(DGCClient c, ObjID id, Object key){
      remove = true;
      client = c;
      this.id = id;
      this.key = key;
      new Thread(this, "DGCClient$Registrator for "+id).start();
    }

    public void run(){
      if(RMIConnection.DEBUG < 7){ System.out.println("DGCClient Registrator for "+client+" remove = "+remove);}
      try {
        if(remove){
          client.remove(id,key);
        }
        else {
          client.add(id,key);
        }
      }
      catch(IOException re){
        if(RMIConnection.DEBUG < 7){ System.out.println("DGCClient Registrator for "+client+" failed "+re);}
        re.printStackTrace();

      }
    }
  }
}
