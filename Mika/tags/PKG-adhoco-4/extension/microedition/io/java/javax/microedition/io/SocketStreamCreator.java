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
