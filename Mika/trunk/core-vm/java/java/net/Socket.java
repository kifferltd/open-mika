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

public class Socket {

  private static SocketImplFactory factory=null;

  SocketImpl socket;   //default acces for communication with serverSocket
  private InetAddress local=null;

  public Socket(){
    socket = getImpl();
  }

  protected Socket(SocketImpl impl) throws SocketException {
  	if (impl == null) {
  	 	throw new NullPointerException();
  	}
  	socket = impl;
  }

  public Socket(String host, int port) throws UnknownHostException, IOException {
  	this(InetAddress.getByName(host), port, null, 0);
  }

  public Socket (InetAddress address, int port) throws IOException {
    this(address, port, null, 0);
  }

  public Socket(String host, int port, InetAddress localAddr, int localPort) throws UnknownHostException, IOException {
  	this(InetAddress.getByName(host), port, localAddr, localPort);
  }


  public Socket(InetAddress address, int port, InetAddress localAddr, int localPort) throws IOException {
    if(port < 0 || port > 65535 || localPort < 0 || localPort > 65535){
      throw new IllegalArgumentException();
    }
  	  	
    if(localAddr == null){
      if(address instanceof Inet6Address) {
        localAddr = new Inet6Address("::0");
      }
      else {
        localAddr = InetAddress.allZeroAddress;
      }
    }
  
    InetAddress.connectCheck(address.getHostAddress(),port);
  
    socket = getImpl();
    socket.create(true);
    socket.bind(localAddr, localPort);
    socket.connect(address,port);  	
  }

  private SocketImpl getImpl() {
	  return factory == null ? new PlainSocketImpl() : factory.createSocketImpl();
  }

  /**
  ** will always create a Stream Socket ...
  **
  */
  public Socket(InetAddress host, int port, boolean stream) throws IOException {
    this(host, port);
  }

  /**
  ** will always create a Stream Socket ...
  **
  */
  public Socket(String host, int port, boolean stream) throws IOException {
    this(host, port);
  }

  public synchronized void close() throws IOException {
 	  socket.close();
  }

  public InetAddress getInetAddress() {
   	return socket.address;
  }

  public InputStream getInputStream() throws IOException {
   	return socket.getInputStream();
  }

  public InetAddress getLocalAddress() {
    if (local == null) {
      try {
      	local = (InetAddress) socket.getOption(SocketOptions.SO_BINDADDR);
      }
      catch(SocketException e){
        e.printStackTrace();
      }
    }
    return local;
  }

  public int getLocalPort() {
   	return socket.localport;
  }

  public OutputStream getOutputStream() throws IOException {
   	return socket.getOutputStream();
  }

  public int getPort(){
   	return socket.port;
  }

  public int getReceiveBufferSize() throws SocketException {
   	return ((Integer)socket.getOption(SocketOptions.SO_RCVBUF)).intValue();
  }

  public int getSendBufferSize() throws SocketException {
  	return ((Integer)socket.getOption(SocketOptions.SO_SNDBUF)).intValue();
  }

  public int getSoLinger() throws SocketException {
    try {
      return ((Integer)socket.getOption(SocketOptions.SO_LINGER)).intValue();
    }
    catch(ClassCastException cce) {
      return -1;
    }
  }

  public int getSoTimeout() throws SocketException {
   	return ((Integer)socket.getOption(SocketOptions.SO_TIMEOUT)).intValue();
  }

  public boolean getTcpNoDelay() throws SocketException {
  	return ((Boolean)socket.getOption(SocketOptions.TCP_NODELAY)).booleanValue();
  }

  public void setReceiveBufferSize(int size) throws SocketException {
    if (size <= 0) {
     	throw new IllegalArgumentException();
    }
    socket.setOption(SocketOptions.SO_RCVBUF, new Integer(size));
  }

  public void setSendBufferSize(int size) throws SocketException {
    if (size <= 0) {
     	throw new IllegalArgumentException();
    }
    socket.setOption(SocketOptions.SO_SNDBUF, new Integer(size));
  }

  public void setSoLinger(boolean on, int linger) throws SocketException {
    if (on) {
      if (linger < 0) {
        throw new IllegalArgumentException();
      }
      socket.setOption(SocketOptions.SO_LINGER, new Integer(linger));	
    }
    else {
      socket.setOption(SocketOptions.SO_LINGER, new Boolean(false));	
    }
  }

  public void setSoTimeout(int timeout) throws SocketException {
    if (timeout < 0) {
      throw new IllegalArgumentException();
    }
    socket.setOption(SocketOptions.SO_TIMEOUT, new Integer(timeout));
  }

  public void setTcpNoDelay(boolean on) throws SocketException {
    socket.setOption(SocketOptions.TCP_NODELAY, new Boolean(on));
  }

  public String toString() {
    return "Socket to "+socket;
  }

 /**
 ** @remark the socketImplFactory can only be set once !
 */
 public static synchronized void setSocketImplFactory(SocketImplFactory fact) throws IOException {
   if (factory != null) {
 	  throw new SocketException();
   }

   InetAddress.factoryCheck();

   factory = fact;  	   	
 }

  public void shutdownInput() throws IOException {
    socket.shutdownInput();
  }

  public void shutdownOutput() throws IOException {
    socket.shutdownOutput();
  }

  public void setKeepAlive(boolean on) throws SocketException {
    socket.setOption(SocketOptions.SO_KEEPALIVE, new Boolean(on));
  }

  public boolean getKeepAlive() throws SocketException {
  	return ((Boolean)socket.getOption(SocketOptions.SO_KEEPALIVE)).booleanValue();
  }
}
