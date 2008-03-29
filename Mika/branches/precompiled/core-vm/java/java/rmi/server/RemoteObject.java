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

package java.rmi.server;

import java.rmi.Remote;
import java.rmi.NoSuchObjectException;
import java.io.*;
import wonka.rmi.ObjIDData;

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

