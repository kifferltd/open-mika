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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.rmi.MarshalException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.activation.ActivationID;
import java.rmi.server.ObjID;
import java.rmi.server.Operation;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RemoteCall;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteRef;

/**
 * Implementation class for activatable rmi references.
 */
public class ActivatableRef implements RemoteRef {
  private static final long serialVersionUID = 1547558010681608306L;

  static final String REF_TYPE = "ActivatableRef";

  private ActivationID activationID;
  private UnicastRef2 ref;


  /**
   * Default non-arg constructor.
   */
  public ActivatableRef(){
  }

  /**
   * Constructor.
   *
   * @param address Host address
   * @param port Portnumber
   * @param id Object identifier
   * @param csf Client socket factory
   */
  public ActivatableRef(ActivationID activationID, String address, int port, ObjID id, RMIClientSocketFactory csf){
    this.activationID = activationID;
    ref = new UnicastRef2(address, port, id, csf);
  }

  /**
   * Returns the class name of the ref type to be serialized onto the stream 'out'.
   *
   * @param out the output stream to which the reference will be serialized
   * @return the class name (without package qualification) of the reference type
   */
  public String getRefClass(ObjectOutput out) {
    return REF_TYPE;
  }

  /**
   * Invoke a method. This form of delegating method invocation to the reference allows
   * the reference to take care of setting up the connection to the remote host, marshaling
   * some representation for the method and parameters, then communicating the method invocation
   * to the remote host. This method either returns the result of a method invocation on the remote
   * object which resides on the remote host or throws a RemoteException if the call failed or an
   * application-level exception if the remote invocation throws an exception. 
   *
   * @param obj the object that contains the RemoteRef (e.g., the RemoteStub for the object.
   * @param method the method to be invoked
   * @param params the parameter list
   * @param opnum a hash that may be used to represent the method
   * @return result of remote method invocation
   * @exception Exception if any exception occurs during remote method invocation
   */
  public Object invoke(Remote obj, Method method, Object[] params, long opnum) throws Exception {
    return ref.invoke(obj, method, params, opnum);
  }

  /**
   * Compares two remote objects for equality. Returns a boolean that indicates whether this remote object is
   * equivalent to the specified Object. This method is used when a remote object is stored in a hashtable.
   *
   * @param obj the Object to compare with
   * @return true if these Objects are equal; false otherwise.
   */
  public boolean remoteEquals(RemoteRef obj) {
    if (obj instanceof ActivatableRef) {
      ActivatableRef activatableRef = (ActivatableRef)obj;

      return ( ref.remoteEquals(activatableRef.ref) ); // TBD Check ActivationID equals?
    }
    return false;
  }

  /**
   * Compares two remote objects for equality. Returns a boolean that indicates whether this remote object is
   * equivalent to the specified Object. This method is used when a remote object is stored in a hashtable.
   *
   * @param obj the Object to compare with
   * @return true if these Objects are equal; false otherwise.
   */
  public int remoteHashCode(){
    return hashCode();
  }

  /**
   * Returns a String that represents the reference of this remote object.
   */
  public String remoteToString(){
    return toString();
  }

  /**
   * The object implements the <code>readExternal</code> method to restore its contents by calling the methods
   * of <code>DataInput</code> for primitive types and <code>readObject</code> for objects, strings and arrays.
   * The <code>readExternal</code> method must read the values in the same sequence and with the same types as
   * were written by <code>writeExternal</code>.
   *
   * @param in the stream to read data from in order to restore the object
   * @exception IOException if I/O errors occur
   * @exception ClassNotFoundException If the class for an object being restored cannot be found
   */
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    activationID = (ActivationID)in.readObject();

    String refType = in.readUTF();


    if ( refType.equals(UnicastRef2.REF_TYPE) ) {
      String name = RemoteRef.packagePrefix + "." + refType;

      try {
        ref = (UnicastRef2)Class.forName(name).newInstance();
        ref.readExternal(in);
      }
      catch(IllegalAccessException ie){
        throw new MarshalException("no access for "+name);
      }
      catch(InstantiationException ie){
        throw new MarshalException("failed to instantiate "+name);
      }
    }
    else {
      if ( !refType.equals("") ) { // Could be a null remote reference
        throw new MarshalException("illegal nested remote reference type " + refType);
      }
    }
  }

  /**
   * The object implements the <code>writeExternal</code> method to save its contents by calling the methods of
   * <code>DataOutput</code> for its primitive values or calling the <code>writeObject</code> method of 
   * <code>ObjectOutput</code> for objects, strings, and arrays.
   *
   * @param out the stream to write the object to 
   * @exception IOException Includes any I/O exceptions that may occur
   */
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeObject(activationID);

    if (ref != null) {
      out.writeUTF(UnicastRef2.REF_TYPE);
      ref.writeExternal(out);
    }
    else {
      out.writeUTF("");
    }
  }


  /**
   * Returns a string representation of this object.
   */
  public String toString() {
    StringBuffer str = new StringBuffer( super.toString() );
    // TBD: Print string conform SUN toString(): port nr. hostname etc.
    return str.toString();
  }


  /**
   ** @deprecated
   */
  public void invoke(RemoteCall call) throws Exception {
    throw new RemoteException("UnsupportedOperation -- deprecated method");
  }

  /**
   ** @deprecated
   */
  public void done(RemoteCall call) throws RemoteException {
    throw new RemoteException("UnsupportedOperation -- deprecated method");
  }

  /**
   * @deprecated
   */
  public RemoteCall newCall(RemoteObject obj, Operation[] op, int opnum, long hash) throws RemoteException {
    throw new RemoteException("UnsupportedOperation -- deprecated method");
  }
}
