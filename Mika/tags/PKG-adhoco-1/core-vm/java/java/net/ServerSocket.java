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


package java.net;

import java.io.*;

public class ServerSocket {

  private static SocketImplFactory factory;

  private SocketImpl socket;
  private InetAddress local;

  public ServerSocket() throws IOException {
    socket = (factory == null ? new PlainSocketImpl() : factory.createSocketImpl());
    //TODO Need more...
  }
  
  public ServerSocket (int port) throws IOException {
  	this(port, 50, null);
  }

  public ServerSocket (int port, int backlog) throws IOException {
  	this(port, backlog, null);
  }

  public ServerSocket (int port, int backlog, InetAddress local) throws IOException {
  	if(port < 0 || port > 65535){
  	  throw new IllegalArgumentException();
  	}
  	
  	InetAddress.listenCheck(port);
  	
  	if (local == null) {
          local = InetAddress.allZeroAddress;
  	}
  	this.local = local;
  	socket = (factory == null ? new PlainSocketImpl() : factory.createSocketImpl());	
  	socket.create(true);
  	socket.bind(local,port);	
  	socket.listen(backlog);
  }

  public Socket accept() throws IOException {
     Socket s = new Socket();
     implAccept(s);
     InetAddress.acceptCheck(s.getInetAddress().getHostAddress(), s.getPort());
     return s;
  }

  protected final void implAccept(Socket s) throws IOException {
      	socket.accept(s.socket);
  }

  public void close() throws IOException {
     socket.close();
  }

  public InetAddress getInetAddress() {
   	return local;
  }

  public int getLocalPort () {
    return socket.localport;
  }

  public synchronized int getSoTimeout() throws IOException {
   	return ((Integer)socket.getOption(SocketOptions.SO_TIMEOUT)).intValue();
  }

  public synchronized void setSoTimeout(int timeout) throws SocketException {
   	if ( timeout < 0) {
   	 	throw new IllegalArgumentException();
   	}
   	socket.setOption(SocketOptions.SO_TIMEOUT, new Integer(timeout));
  }


  public synchronized static void setSocketFactory(SocketImplFactory fact) throws IOException {
   	if (factory != null) {
   		throw new SocketException("factory already set");
   	}
   	
   	InetAddress.factoryCheck();
   	
   	factory = fact;
   	
  }

  public String toString() {
   	return "ServerSocket[address = "+local+"at port = "+socket.localport+"]";
  }
}
