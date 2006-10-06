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

import java.rmi.Remote;
import java.rmi.NoSuchObjectException;
import java.io.*;
import com.acunia.wonka.rmi.ObjIDData;

public abstract class RemoteObject implements Remote, Serializable {

  private static final long serialVersionUID = -3215090123894869218L;

  protected transient RemoteRef ref;


  public static Remote toStub(Remote obj) throws NoSuchObjectException {
    ObjIDData data = (ObjIDData)UnicastRemoteObject.exports.get(obj);
    if(data == null){
      throw new NoSuchObjectException(obj+" is not exported");
    }
    return data.stub;
  }

  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
    // Modified: Fixed implementation
    String name = RemoteRef.packagePrefix + "." + in.readUTF();

    if (name.length() == 0) {
      ref = (RemoteRef)in.readObject();      
    }
    else {
      try {
        ref = (RemoteRef)Class.forName(name).newInstance();
        ref.readExternal(in);
      }
      catch(IllegalAccessException ie){
        throw new java.rmi.MarshalException("no access for "+name);
      }
      catch(InstantiationException ie){
        throw new java.rmi.MarshalException("failed to instantiate "+name);
      }
    }
  }

  private void writeObject(ObjectOutputStream out) throws IOException, ClassNotFoundException {
    // Modified: Fixed implementation
    if(ref == null){
      throw new java.rmi.MarshalException("RemoteRef ref is null "+this);
    }

    String name = ref.getRefClass(out);

    if((name == null) || (name.length() == 0)) {
      out.writeUTF("");
      out.writeObject(ref);
    }
    else {
      out.writeUTF(name);
      ref.writeExternal(out);
    }
  }

  
  protected RemoteObject() { }
  
  protected RemoteObject(RemoteRef newref) {
    ref = newref;
  }
  
  public boolean equals(Object obj) {
    if(obj instanceof RemoteObject){
      RemoteObject ro = (RemoteObject) obj;
      return ref == null ? ro.ref == null : ref.remoteEquals(ro.ref);
    }
    return false;
  }
  
  public RemoteRef getRef() {
    return ref;
  }
  
  public int hashCode() {
    return (ref == null ? 0 : ref.remoteHashCode());
  }
  
  public String toString() {
    return this.getClass().getName() + " Wrapping Remote ref "+ (ref != null ? ref.remoteToString() : null);
  }
}

