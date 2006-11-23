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


package javax.microedition.io;

import java.io.IOException;
import java.net.*;

class ServerSocketConnection implements StreamConnectionNotifier {

  private ServerSocket server;
  private int timeout;

  ServerSocketConnection(String name, boolean timesout) throws ConnectionNotFoundException {
    int index = name.indexOf(';');
    timeout = timesout ? 60000 : 0;
    if(index != -1){
      //Todo PARSE for options ...
      int timeindex = name.indexOf(";timeout=", index);
      if(timeindex != -1){
        try {
          timeindex += 9;
          int end = name.indexOf(';',timeindex);
          timeout = Integer.parseInt(name.substring(timeindex,(end == -1 ? name.length() : end)));
        }
        catch(NumberFormatException nfe){
          throw new IllegalArgumentException("bad option");
        }
      }
      name = name.substring(0,index);
    }
    if(name.startsWith("//")){
      name = name.substring(2);
    }
    index = name.lastIndexOf(':');
    try {
      if(index == -1){
        server = new ServerSocket(Integer.parseInt(name));
      }
      else {
        int port = Integer.parseInt(name.substring(index+1));
        server = new ServerSocket(port, 50, InetAddress.getByName(name.substring(index)));
      }
      server.setSoTimeout(timeout);
    }
    catch(Exception e){
      throw new ConnectionNotFoundException("unable to create socket due to "+e);
    }
  }

  public void close() throws IOException {
    server = null;
  }

  public StreamConnection acceptAndOpen() throws IOException {
    if(server == null){
      throw new IOException("serversocket connection is closed");
    }
    Socket s = server.accept();
    s.setSoTimeout(timeout);
    return new BasicStreamConnection(new SocketStreamCreator(s));
  }
}