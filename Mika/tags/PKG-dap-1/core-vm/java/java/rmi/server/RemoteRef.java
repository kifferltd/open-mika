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

import java.rmi.RemoteException;
import java.rmi.Remote;
import java.io.Externalizable;
import java.io.ObjectOutput;
import java.lang.reflect.Method;

public interface RemoteRef extends Externalizable {

  public static final String packagePrefix = "com.acunia.wonka.rmi";
  public static final long serialVersionUID = 3632638527362204081L;

  public void done(RemoteCall call) throws RemoteException;
  public String getRefClass(ObjectOutput out);
  public void invoke(RemoteCall call) throws Exception;
  public Object invoke(Remote obj, Method method, Object[] params, long opnum) throws Exception;
  public RemoteCall newCall(RemoteObject obj, Operation[] op, int opnum, long hash) throws RemoteException;
  public boolean remoteEquals(RemoteRef obj);
  public int remoteHashCode();
  public String remoteToString();
  
}

