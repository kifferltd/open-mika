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

import java.io.Serializable;
import java.rmi.server.UID;

public class ActivationGroupID implements Serializable {

  private static final long serialVersionUID = -1648432278909740833L;

  private ActivationSystem system;
  private UID uid;

  public ActivationGroupID(ActivationSystem system) {
    uid = new UID();
    this.system = system;
  }

  public ActivationSystem getSystem() {
    return system;
  }
  
  public int hashCode() {
    return uid.hashCode() ^ system.hashCode();
  }
  
  public boolean equals(Object obj) {
    if(this.getClass().isInstance(obj)){
      ActivationGroupID agid = (ActivationGroupID)obj;
      return system.equals(agid.system)
          && uid.equals(agid.uid);
    }
    return false;
  }

}

