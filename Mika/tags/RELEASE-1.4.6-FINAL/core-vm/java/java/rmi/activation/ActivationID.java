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

package java.rmi.activation;

import java.io.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RemoteRef;
import java.rmi.server.UID;

public class ActivationID implements Serializable {
  private static final long serialVersionUID = -4608673054848209235L;

  private transient RemoteRef ref;
  private Activator activator;
  private UID uid;


  /**
   *
   */  
  private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
    uid = (UID)in.readObject();

    String name = RemoteRef.packagePrefix + "." + in.readUTF();

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

  /**
   *
   */
  private void writeObject(ObjectOutputStream out) throws IOException, ClassNotFoundException {
    out.writeObject(uid);
    ref.getRefClass(out);
    ref.writeExternal(out);
  }

  /**
   *
   */
  public ActivationID(Activator activator) {
    this.activator = activator;
    uid = new UID();
  }

  /**
   *
   */
  public Remote activate(boolean force) throws ActivationException, UnknownObjectException, RemoteException {
    System.out.println("[ActivationID.activate] Not implemented...");
    return null;
  }
  
  /**
  *
  */
  public int hashCode() {
    // TBD: Activator is not set therefore we can get a nullpointer
    // This is a workaround until activation implementation is finished.
    // return uid.hashCode() ^ activator.hashCode();
    return super.hashCode();
  }
  
  /**
   *
   */
  public boolean equals(Object obj) {
    if(this.getClass().isInstance(obj)){
      ActivationID aid = (ActivationID)obj;
      return uid.equals(aid.uid)
          && activator.equals(aid.activator);
    }
    return false;
  }

}

