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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Connector {

  private Connector(){}

  public static final int READ = 1;
  public static final int READ_WRITE = 3;
  public static final int WRITE = 2;

  public static Connection open(String name) throws IOException {
    return open(name, READ_WRITE, false);
  }

  public static Connection open(String name, int mode) throws IOException {
    return open(name, mode, false);
  }

  public static Connection open(String name, int mode, boolean times_out) throws IOException {
    int index = name.indexOf(':');
    if(index == -1){
      throw new IllegalArgumentException("no valid connection specified '"+name+"'");
    }
    if (mode < 0 || mode > 3){
      throw new IllegalArgumentException("invalid mode specified "+mode);
    }

    String protocol = name.substring(0,index);
    //supported protocols like datagram, file, http, socket, serversocket ...

    //serversocket
    if(protocol.equals("serversocket")){
      return new ServerSocketConnection(name.substring(index+1),times_out);
    }
    //datagram
    if(protocol.equals("datagram")){
      return new BasicDatagramConnection(name.substring(index+1),times_out);
    }

    StreamCreator creator;
    if(protocol.equals("file")){
      creator = new FileStreamCreator(name.substring(index+1), mode == READ);
    }
    else if(protocol.equals("socket")){
      creator = new SocketStreamCreator(name.substring(index+1),times_out);
    }
    else {
      if(mode != READ_WRITE){
        throw new IllegalArgumentException("invalid mode specified "+mode+" for ContentConnections");
      }
      creator = new URLStreamCreator(name);
      return new BasicStreamConnection(creator);
    }

    switch(mode){
      case READ_WRITE:
        return new BasicStreamConnection(creator);
      case READ:
        return new BasicInputConnection(creator);
      case WRITE:
        return new BasicOutputConnection(creator);
    }
    throw new IllegalArgumentException("invalid mode specified");
  }

  public static DataInputStream openDataInputStream(String name) throws IOException {
    return ((InputConnection)open(name, READ, false)).openDataInputStream();
  }

  public static DataOutputStream openDataOutputStream(String name) throws IOException {
    return ((OutputConnection)open(name, WRITE, false)).openDataOutputStream();
  }

  public static InputStream openInputStream(String name) throws IOException {
    return ((InputConnection)open(name, READ, false)).openInputStream();
  }
  public static OutputStream openOutputStream(String name) throws IOException {
    return ((OutputConnection)open(name, WRITE, false)).openOutputStream();
  }
}
