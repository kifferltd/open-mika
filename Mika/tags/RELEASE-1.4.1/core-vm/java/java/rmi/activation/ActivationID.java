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

