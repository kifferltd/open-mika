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

