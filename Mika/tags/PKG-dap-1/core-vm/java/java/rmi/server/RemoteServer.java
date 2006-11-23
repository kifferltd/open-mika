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

import java.io.PrintStream;
import java.io.OutputStream;

public abstract class RemoteServer extends RemoteObject {

  private static final long serialVersionUID = -4100238210092549637L;
  private static PrintStream theLog;

  protected RemoteServer() {}

  protected RemoteServer(RemoteRef ref) {
    super(ref);
  }

  public static String getClientHost() throws ServerNotActiveException {
    return com.acunia.wonka.rmi.RMIConnection.getClientHost();
  }
  
  public static PrintStream getLog() {
    return theLog;
  }

  public static void setLog(OutputStream out) {
    if(out == null){
      theLog = null;
    }
    else {
      theLog = new PrintStream(out);
    }
  }

}

