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
import java.net.*;

public class RMIAcceptThread extends Thread {

  private ServerSocket ss;

  public RMIAcceptThread(ServerSocket ss){
    super("RMIAcceptThread for "+ss);
    this.ss = ss;
    this.start();
  }

  public void run(){
    try {
      while(true){
        Socket s = ss.accept();
        new RMIRequestHandler(s);
        if(RMIConnection.DEBUG < 5) {System.out.println("ACCECPTED CONNECTION on "+ss);}
      }
    }
    catch(IOException ioe){
      //typically when serversocket gets closed ...
    }
  }
}

