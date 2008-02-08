/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
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
