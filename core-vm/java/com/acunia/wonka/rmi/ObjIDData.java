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

import java.net.ServerSocket;
import java.rmi.Remote;
import java.rmi.server.RemoteStub;
import java.rmi.server.ObjID;

public final class ObjIDData {

  public final ServerSocket server;
  public final Remote impl;
  public final RemoteStub stub;
  public final ObjID id;

  java.util.HashMap methods;

  public ObjIDData(ServerSocket server, Remote impl, RemoteStub stub, ObjID id){
    this.server = server;
    this.impl = impl;
    this.stub = stub;
    this.id = id;
  }

  public String toString(){
    if(RMIConnection.DEBUG < 8){
      return super.toString() +" server "+server+
        "\n\t Remote impl = "+impl+
        "\n\t RemoteStub stub = "+stub+
        "\n\t ObjID id = "+id+
        "\n\t exported methods: "+methods;
    }
    else {
      return super.toString();
    }
  }
}