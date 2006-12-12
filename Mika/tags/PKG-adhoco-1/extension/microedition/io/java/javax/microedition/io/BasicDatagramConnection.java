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