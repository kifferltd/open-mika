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

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ActivationSystem extends Remote {

  public static final int SYSTEM_PORT = 1098;
 
  public ActivationID registerObject(ActivationDesc desc) 
    throws ActivationException, UnknownGroupException, RemoteException;
    
  public void unregisterObject(ActivationID id) 
    throws ActivationException, UnknownObjectException, RemoteException;
    
  public ActivationGroupID registerGroup(ActivationGroupDesc desc) 
    throws ActivationException, RemoteException;
    
  public ActivationMonitor activeGroup(ActivationGroupID id, ActivationInstantiator group, long incarnation) 
    throws UnknownGroupException, ActivationException, RemoteException;
    
  public void unregisterGroup(ActivationGroupID id) 
    throws ActivationException, UnknownGroupException, RemoteException;
    
  public void shutdown() 
    throws RemoteException;
    
  public ActivationDesc setActivationDesc(ActivationID id, ActivationDesc desc) 
    throws ActivationException, UnknownObjectException, UnknownGroupException, RemoteException;
    
  public ActivationGroupDesc setActivationGroupDesc(ActivationGroupID id, ActivationGroupDesc desc) 
    throws ActivationException, UnknownGroupException, RemoteException;
    
  public ActivationDesc getActivationDesc(ActivationID id) 
    throws ActivationException, UnknownObjectException, RemoteException;
    
  public ActivationGroupDesc getActivationGroupDesc(ActivationGroupID id) 
    throws ActivationException,  UnknownGroupException, RemoteException;
}

