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

import java.rmi.MarshalledObject;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public abstract class ActivationGroup extends UnicastRemoteObject implements ActivationInstantiator {

  private static final long serialVersionUID = -7696947875314805420L;

  private ActivationGroupID groupID;
  private ActivationMonitor monitor;
  private long incarnation;

  protected ActivationGroup(ActivationGroupID groupID) throws RemoteException {
    this.groupID = groupID;
  }
  
  public static ActivationGroup createGroup(ActivationGroupID id, ActivationGroupDesc desc, long incarnation) throws ActivationException {
    System.out.println("[ActivationGroup.createGroup] Not implemented...");
    return null;
  }
  
  public static ActivationGroupID currentGroupID() {
    System.out.println("[ActivationGroup.currentGroupID] Not implemented...");
    return null;
  }
  
  public static void setSystem(ActivationSystem system) throws ActivationException {
    System.out.println("[ActivationGroup.setSystem] Not implemented...");
  }
  
  public static ActivationSystem getSystem() throws ActivationException {
    System.out.println("[ActivationGroup.getSystem] Not implemented...");
    return null;
  }
  
  public abstract void activeObject(ActivationID id, Remote obj) throws ActivationException, UnknownObjectException, RemoteException;

  protected void activeObject(ActivationID id, MarshalledObject mobj) throws ActivationException, UnknownObjectException, RemoteException {
    monitor.activeObject(id, mobj);
  }
  
  protected void inactiveGroup() throws UnknownGroupException, RemoteException {
    monitor.inactiveGroup(groupID, incarnation);
    //TODO remove the current ActivationGroup.
  }

  public boolean inactiveObject(ActivationID id) throws ActivationException, UnknownObjectException, RemoteException {
    monitor.inactiveObject(id);
    //the object was not exported so return false ???
    return false;
  }


}

