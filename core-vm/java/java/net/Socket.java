/**************************************************************************
* Copyright (c) 2007, 2009, 2015, 2016 by Chris Gray, KIFFER Ltd.         *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS *
* OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)   *
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,     *
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING   *
* IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE      *
* POSSIBILITY OF SUCH DAMAGE.                                             *
**************************************************************************/

package java.net;

import java.io.*;

public class Socket {

  private static SocketImplFactory factory=null;

  SocketImpl socket;
  private InetAddress local;
  boolean bound;
  boolean inputShut;
  boolean outputShut;

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

  public void connect(SocketAddress sa, int timeout) throws IOException {
    if (socket == null) {
      throw new SocketException();
    }

    if (!isBound()) {
      bind(null);
    }

    socket.connect(sa, timeout);
  }

  public void connect(SocketAddress sa) throws IOException {
    connect(sa, 0);
  }

  public void bind(SocketAddress sa) throws IOException {
    if (socket == null) {
      throw new SocketException();
    }

    if (sa == null) {
      sa = new InetSocketAddress(InetAddress.getByAddress(new byte[4]), 0);
    }

    try {
      InetSocketAddress isa = (InetSocketAddress)sa;
      socket.bind(isa.getAddress(), isa.getPort());
      bound = true;
    }
    catch (ClassCastException cce) {
      throw new IllegalArgumentException();
    }
    finally {
      if (!bound) {
        close();
      }
    }
  }

  public boolean isBound() {
    try {
      // TODO: how is this supposed to work for non-Plain SocketImpl?
      PlainSocketImpl psi = (PlainSocketImpl)socket;
      return psi != null && psi.localAddress != null && psi.localAddress.getAddress() != null || bound;
    }
    catch (ClassCastException cce) {
      return false;
    }
  }

  public boolean isConnected() {
    return socket != null && socket.getInetAddress() != null;
  }

  public boolean isClosed() {
    return socket == null;
  }

  public synchronized void close() throws IOException {
    if (socket == null) {
      return;
    }
     socket.close();
     local = InetAddress.allZeroAddress;
     socket = null;
  }

  public InetAddress getInetAddress() {
     return socket.address;
  }

  public SocketAddress getRemoteSocketAddress() {
     if(socket == null || socket.address == null) {
       return null;
     }
     return new InetSocketAddress(socket.address, socket.port);
  }

  public InputStream getInputStream() throws IOException {
     if (socket == null) {
       throw new SocketException();
     }
     return socket.getInputStream();
  }

  public InetAddress getLocalAddress() {
System.out.println("isBound(): " + isBound());
    if (!isBound()) {
      try {
System.out.println("not bound: " + InetAddress.getByAddress(new byte[4]));
        return InetAddress.getByAddress(new byte[4]);
      }
      catch(UnknownHostException e){
        e.printStackTrace();
        return null;
      }
    }

    if (local == null) {
      try {
        local = (InetAddress) socket.getOption(SocketOptions.SO_BINDADDR);
System.out.println("socket.getOption(SocketOptions.SO_BINDADDR) returned: " + socket.getOption(SocketOptions.SO_BINDADDR));
      }
      catch(SocketException e){
        e.printStackTrace();
      }
    }
    return local;
  }

  public int getLocalPort() {
     if (!isBound()) {
        return -1;
     }
     return socket.localport;
  }

  public SocketAddress getLocalSocketAddress() {
    if (!isBound()) {
      return null;
    }

    return new InetSocketAddress(getLocalAddress(), socket.localport);
  }

  public OutputStream getOutputStream() throws IOException {
     if (socket == null) {
       throw new SocketException();
     }
     return socket.getOutputStream();
  }

  public int getPort(){
     return socket.port;
  }

  public int getReceiveBufferSize() throws SocketException {
     if (socket == null) {
        throw new SocketException();
     }
     return ((Integer)socket.getOption(SocketOptions.SO_RCVBUF)).intValue();
  }

  public int getSendBufferSize() throws SocketException {
     if (socket == null) {
       throw new SocketException();
     }
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
     if (socket == null) {
       throw new SocketException();
     }
     if (socket == null) {
       throw new SocketException();
     }
     return ((Integer)socket.getOption(SocketOptions.SO_TIMEOUT)).intValue();
  }

  public boolean getTcpNoDelay() throws SocketException {
     if (socket == null) {
       throw new SocketException();
     }
     return ((Boolean)socket.getOption(SocketOptions.TCP_NODELAY)).booleanValue();
  }

  public void setReceiveBufferSize(int size) throws SocketException {
     if (socket == null) {
       throw new SocketException();
     }
    if (size <= 0) {
       throw new IllegalArgumentException();
    }
    socket.setOption(SocketOptions.SO_RCVBUF, new Integer(size));
  }

  public void setSendBufferSize(int size) throws SocketException {
     if (socket == null) {
       throw new SocketException();
     }
    if (size <= 0) {
       throw new IllegalArgumentException();
    }
    socket.setOption(SocketOptions.SO_SNDBUF, new Integer(size));
  }

  public void setSoLinger(boolean on, int linger) throws SocketException {
     if (socket == null) {
       throw new SocketException();
     }
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
     if (socket == null) {
       throw new SocketException();
     }
    if (timeout < 0) {
      throw new IllegalArgumentException();
    }
    socket.setOption(SocketOptions.SO_TIMEOUT, new Integer(timeout));
  }

  public void setTcpNoDelay(boolean on) throws SocketException {
     if (socket == null) {
       throw new SocketException();
     }
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
     if (socket == null) {
       throw new SocketException();
     }
    if (!inputShut) {
      inputShut = true;
      socket.shutdownInput();
    }
  }

  public boolean isInputShutdown() {
    return inputShut;
  }

  public void shutdownOutput() throws IOException {
     if (socket == null) {
       throw new SocketException();
     }
    if (!outputShut) {
      outputShut = true;
      socket.shutdownOutput();
    }
  }

  public boolean isOutputShutdown() {
    return outputShut;
  }

  public void setKeepAlive(boolean on) throws SocketException {
     if (socket == null) {
       throw new SocketException();
     }
    socket.setOption(SocketOptions.SO_KEEPALIVE, new Boolean(on));
  }

  public boolean getKeepAlive() throws SocketException {
     if (socket == null) {
       throw new SocketException();
     }
     return ((Boolean)socket.getOption(SocketOptions.SO_KEEPALIVE)).booleanValue();
  }

  public void sendUrgentData(int udata) throws IOException {
    if (socket == null) {
       throw new SocketException();
     }
     socket.sendUrgentData(udata);
  }

  public void setOOBInline(boolean on) throws SocketException {
     if (socket == null) {
       throw new SocketException();
     }
    socket.setOption(SocketOptions.SO_OOBINLINE, new Boolean(on));
  }

  public boolean getOOBInline() throws SocketException {
     if (socket == null) {
       throw new SocketException();
     }
     return ((Boolean)socket.getOption(SocketOptions.SO_OOBINLINE)).booleanValue();
  }

  public void setTrafficClass(int tcl) throws SocketException {
     if (socket == null) {
       throw new SocketException();
     }
    socket.setOption(SocketOptions.IP_TOS, new Integer(tcl));
  }

  public int getTrafficClass() throws SocketException {
     if (socket == null) {
       throw new SocketException();
     }
     return ((Integer)socket.getOption(SocketOptions.IP_TOS)).intValue();
  }

  public void setReuseAddress(boolean on) throws SocketException {
     if (socket == null) {
       throw new SocketException();
     }
    socket.setOption(SocketOptions.SO_REUSEADDR, new Boolean(on));
  }

  public boolean getReuseAddress() throws SocketException {
     if (socket == null) {
       throw new SocketException();
     }
     return ((Boolean)socket.getOption(SocketOptions.SO_REUSEADDR)).booleanValue();
  }

}
