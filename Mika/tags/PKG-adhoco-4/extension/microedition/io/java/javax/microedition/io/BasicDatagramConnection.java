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

class BasicDatagramConnection implements DatagramConnection {

  private DatagramSocket dsocket;
  private InetAddress serverAddress;
  private int toPort;

  BasicDatagramConnection(String name, boolean timesout) throws ConnectionNotFoundException {
    int index = name.indexOf(';');
    int timeout = timesout ? 60000 : 0;
    if(index != -1){
      //Todo PARSE for more options ...
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
    if(name.startsWith("//")){
      name = name.substring(2);
    }
    index = name.lastIndexOf(':');
    try {
      if(index == -1){
        dsocket = new DatagramSocket(Integer.parseInt(name));
      }
      else {
        int port = Integer.parseInt(name.substring(index+1));
        name = name.substring(0,index);
        if(name.length() > 0){
          serverAddress = InetAddress.getByName(name);
          dsocket = new DatagramSocket();
          toPort = port;
        }
        else {
          dsocket = new DatagramSocket(port);
        }

      }
      dsocket.setSoTimeout(timeout);
    }
    catch(Exception e){
      e.printStackTrace();
      throw new ConnectionNotFoundException("unable to create socket due to "+e);
    }
  }

  public void close() throws IOException {
    dsocket = null;
  }

  public int getMaximumLength() throws IOException {
    if(dsocket == null){
      throw new IOException("socket is closed");
    }
    return dsocket.getReceiveBufferSize();
  }

  public int getNominalLength() throws IOException {
    return getMaximumLength();
  }

  public Datagram newDatagram(int size) throws IOException {
    return newDatagram(new byte[size], size);
  }

  public Datagram newDatagram(int size, String address) throws IOException {
    return newDatagram(new byte[size], size, address);
  }

  public Datagram newDatagram(byte[] bytes, int size) throws IOException {
    if(dsocket == null){
      throw new IOException("socket is closed");
    }
    BasicDatagram bd = new BasicDatagram(bytes, size);
    if(serverAddress != null){
      bd.dgram.setAddress(serverAddress);
      bd.dgram.setPort(toPort);
    }
    return bd;
  }

  public Datagram newDatagram(byte[] bytes, int size, String address) throws IOException {
    if(dsocket == null){
      throw new IOException("socket is closed");
    }
    return new BasicDatagram(bytes, size, address);
  }

  public void receive(Datagram d) throws IOException {
    if(dsocket == null){
      throw new IOException("socket is closed");
    }
    try {
      BasicDatagram bd = (BasicDatagram)d;
      DatagramPacket dp = bd.dgram;
      dsocket.receive(dp);
      bd.maxSize = dp.getLength() + dp.getOffset();
      bd.pointer = dp.getOffset();
      bd.offset = bd.pointer;
    }
    catch(ClassCastException cce){
      throw new IOException("invalid datagram class");
    }
  }

  public void send(Datagram d) throws IOException {
    if(dsocket == null){
      throw new IOException("socket is closed");
    }
    try {
      BasicDatagram bd = (BasicDatagram)d;
      bd.dgram.setLength(bd.pointer - bd.offset);
      dsocket.send(bd.dgram);
    }
    catch(ClassCastException cce){
      throw new IOException("invalid datagram class");
    }
  }
}
