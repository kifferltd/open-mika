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

import java.io.*;
import java.net.*;

class SocketStreamCreator extends StreamCreator {

  private Socket socket;

  SocketStreamCreator(Socket socket){
    this.socket = socket;
  }

  SocketStreamCreator(String name, boolean timesout) throws ConnectionNotFoundException {
    int index = name.indexOf(';');
    int timeout = timesout ? 60000 : 0;
    if(index != -1){
      //Todo PARSE for options ...
      int idx = name.indexOf(";timeout=", index);
      if(idx != -1){
        try {
          idx += 9;
          int end = name.indexOf(';',idx);
          timeout = Integer.parseInt(name.substring(idx,(end == -1 ? name.length() : end)));
        }
        catch(NumberFormatException nfe){
          throw new IllegalArgumentException("bad option");
        }
      }
      name = name.substring(0,index);
    }
    index = name.lastIndexOf(':');
    try {
      int port = Integer.parseInt(name.substring(index+1));
      name = name.substring(0,index);
      if(name.startsWith("//")){
        name = name.substring(2);
      }
      socket = new Socket(InetAddress.getByName(name),port);
      socket.setSoTimeout(timeout);
    }
    catch(Exception e){
      throw new ConnectionNotFoundException("unable to create socket");
    }
  }

  public void close(){
    closed = true;
    socket = null;
  }

  protected InputStream getInputStream() throws IOException {
    return socket.getInputStream();
  }

  protected OutputStream getOutputStream() throws IOException {
    return socket.getOutputStream();
  }

}