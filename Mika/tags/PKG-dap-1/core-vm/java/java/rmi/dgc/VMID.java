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

package java.rmi.dgc;

import java.io.Serializable;

public final class VMID implements Serializable {

  private static final long serialVersionUID = -538642295484486218L;

  private byte[] addr;
  private java.rmi.server.UID uid;

  public VMID() {
    uid = new java.rmi.server.UID();
    try {
      addr = java.net.InetAddress.getLocalHost().getAddress();
    }
    catch(Exception e){
      addr = new byte[]{127,0,0,1};
    }
  }
  
  public boolean equals(Object obj) {
    if(obj instanceof VMID){
      VMID id = (VMID)obj;
      return addr == id.addr && uid.equals(id.uid);
    }
    return false;
  }
  
  public int hashCode() {
    return uid.hashCode() ^ addr.hashCode();
  }

  /**
  ** @deprecated
  */
  public static boolean isUnique() {
    return false;
  }
  
  public String toString() {
    return "VMID: " + uid.toString();
  }

}

