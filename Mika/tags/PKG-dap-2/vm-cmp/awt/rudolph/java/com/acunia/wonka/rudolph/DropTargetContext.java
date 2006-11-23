/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/
package com.acunia.wonka.rudolph;

import java.awt.dnd.*;

public class DropTargetContext extends java.awt.dnd.DropTargetContext{
  private com.acunia.wonka.rudolph.peers.DefaultDropTargetContextPeer DDTCPeer;

  public DropTargetContext(DropTarget dropTarget){
    DDTCPeer = new com.acunia.wonka.rudolph.peers.DefaultDropTargetContextPeer(dropTarget);
    java.awt.dnd.peer.DropTargetContextPeer peer = (java.awt.dnd.peer.DropTargetContextPeer) DDTCPeer;
    addNotify(peer);
  }

  public com.acunia.wonka.rudolph.peers.DefaultDropTargetContextPeer getDropTargetContextPeer(){
    return DDTCPeer;
  }

}
