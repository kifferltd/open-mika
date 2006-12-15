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