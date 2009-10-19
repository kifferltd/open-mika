/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2007, 2009 by /k/ Embedded Java Solutions.          *
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
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

package java.net;

import java.io.IOException;

public class DatagramSocket {

  private DatagramPacket peekGarbage;
  DatagramSocketImpl dsocket;
  InetAddress remoteAddress;
  int remoteport=-1;

  static DatagramSocketImplFactory theFactory;

  public static void setDatagramSocketImplFactory(DatagramSocketImplFactory factory) throws IOException {
   if (theFactory != null) {
 	  throw new SocketException();
   }

   InetAddress.factoryCheck();

   theFactory = factory;  	   	
  }

  static void multicastCheck(InetAddress addr) {
    if (wonka.vm.SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkMulticast(addr);
      }
    }
  }

  /**
  ** Special Constructor to create MulticastSockets ...
  */
  DatagramSocket(boolean Multicast){
  }

  public DatagramSocket(SocketAddress saddr) throws SocketException {
    this(saddr == null ? 0 : ((InetSocketAddress)saddr).port, saddr == null ? null : ((InetSocketAddress)saddr).addr);
  }

  public DatagramSocket() throws SocketException, SecurityException {
  	this(0,null);
  }

  public DatagramSocket(int port) throws SocketException, SecurityException {
    if(port < 0 || port > 65535) {
      throw new IllegalArgumentException();
    }
    InetAddress.listenCheck(port);
    if(theFactory != null){
      dsocket = theFactory.createDatagramSocketImpl();
    }
    else {
      String s = GetSystemProperty.DATAGRAM_SOCKET_IMPL;
      try {	
        dsocket = (DatagramSocketImpl) Class.forName(s).newInstance();
      }
      catch(Exception e) {
        dsocket = new PlainDatagramSocketImpl();
      }
    }
  	
    dsocket.create();
    dsocket.bind(port, InetAddress.allZeroAddress);  	
  }

  public DatagramSocket(int port, InetAddress laddr) throws SocketException, SecurityException {
    if(port < 0 || port > 65535) {
      throw new IllegalArgumentException();
    }
    InetAddress.listenCheck(port);
    if(theFactory != null){
      dsocket = theFactory.createDatagramSocketImpl();
    }
    else {
      String s = GetSystemProperty.DATAGRAM_SOCKET_IMPL;
      try {	
        dsocket = (DatagramSocketImpl) Class.forName(s).newInstance();
      }
      catch(Exception e) {
        dsocket = new PlainDatagramSocketImpl();
      }
    }
  	
    dsocket.create();
    dsocket.bind(port, laddr == null ? InetAddress.allZeroAddress : laddr);  	
  }

  public void setReuseAddress (boolean on) throws SocketException {
    if (dsocket == null) {
      throw new SocketException("DatagramSocket is closed");
    }
    dsocket.setOption(SocketOptions.SO_REUSEADDR, new Boolean(on));
  }

  public void setTrafficClass (int tc) throws SocketException {
    throw new SocketException("not yet implemented");
  }

  public void bind(SocketAddress bindAddr) throws SocketException {
    if(dsocket == null) {
      throw new SocketException("DatagramSocket is closed");
    }
    
    if (remoteAddress != null) {
      throw new SocketException("Datagram socket is already connected");
    }

    if (bindAddr == null) {
      // TODO: we should bind to an ephemeral port and a valid local address
      throw new SocketException("Bind datagram socket to null address not yet implemented");
    }
    else {
      try {
        InetSocketAddress isa = (InetSocketAddress)bindAddr;
        InetAddress address = isa.getAddress();
        int port = isa.getPort();
        if (wonka.vm.SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
          SecurityManager sm = System.getSecurityManager();
          if (sm != null) {
            sm.checkListen(port);
          }
        }
        dsocket.bind(port, address);
      }
      catch (ClassCastException cce) {
        throw new IllegalArgumentException();
      }
    }
  }


  /**
   * * Connects the datagramsocket to a remote address. * Connecting a socket
   * will bypass security checks if you send packets to the connected socket. *
   * All other destination are not allowed ...
   */
  public void connect(InetAddress address, int port) throws SecurityException {
    if (address == null || port < 0 || port > 65535) {
      throw new IllegalArgumentException();
    }

    if (this.dsocket != null) {

      if (address.isMulticastAddress()) {
        multicastCheck(address);
      } else {
        InetAddress.connectCheck(address.getHostAddress(), port);
      }
      remoteAddress = address;
      remoteport = port;
    }
  }

  /**
   * * Connects the datagramsocket to a remote address. * Connecting a socket
   * will bypass security checks if you send packets to the connected socket. *
   * All other destination are not allowed ...
   */
  public void connect(SocketAddress sa) throws SecurityException {
    InetSocketAddress isa;
    try {
      isa = (InetSocketAddress)sa;
    }
    catch (ClassCastException cce) {
      throw new IllegalArgumentException();
    }

    connect(isa.getAddress(), isa.getPort());
  }

  /**
  ** Disconnects the socket. This does nothing if the socket is not connected.
  **
  */
  public void disconnect() {
  	remoteAddress = null;
  	remoteport = -1;
  }

  public boolean getBroadcast() throws SocketException {
    if (dsocket == null) {
      throw new SocketException("DatagramSocket is closed");
    }
    return ((Boolean) dsocket.getOption(SocketOptions.SO_BROADCAST)).booleanValue();
  }

  public void setBroadcast(boolean on) throws SocketException {
    if (dsocket == null) {
      throw new SocketException("DatagramSocket is closed");
    }
    dsocket.setOption(SocketOptions.SO_BROADCAST, new Boolean(on));
  }

  /**
  ** The address to which this socket is connected or  null if not connected.
  */
  public InetAddress getInetAddress() {
    return remoteAddress;
  }

  /**
  ** The local address to which this socket is bound or  null if not bound.
  */
  public SocketAddress getLocalSocketAddress() {
    InetAddress localAddress = getLocalAddress();
    if (localAddress == null) {
      return null;
    }
    int localport = getLocalPort();

    return new InetSocketAddress(localAddress, localport);
  }

  /**
  ** The remote address to which this socket is connected or  null if not connected.
  */
  public SocketAddress getRemoteSocketAddress() {
    if (remoteAddress == null) {
      return null;
    }

    return new InetSocketAddress(remoteAddress, remoteport);
  }

  /**
  ** The port off the socket t o which this socket is connected or -1 if this socket is not connected.
  */
  public int getPort() {
    return remoteport;
  }

  /**
  ** Sends a datagram packet from this socket.
  */
  public void send(DatagramPacket p) throws IOException {
    if(dsocket == null) {
      throw new IOException("DatagramSocket is closed");
    }
    
    if (remoteAddress == null) { // we are not connected all Packets are allowed to be send unless ...
      InetAddress addr = p.getAddress();	
      if (addr.isMulticastAddress()){
        multicastCheck(addr);
      }
      else{
        InetAddress.connectCheck(addr.getHostAddress(), p.getPort());
      }
    }
    else if (!remoteAddress.equals(p.getAddress()) || remoteport != p.getPort()) {
  	  //only packets to remoteAddress at remoteport are allowed ...	
       throw new IllegalArgumentException("packet has wrong destination");	     	
    }		
    dsocket.send(p);	
  }

  /**
  ** Receives a datagram packet from this socket.
  ** @remark if the incoming packet contians more bytes than the buffer of the packet can handle, then they are lost.
  */
  public synchronized void receive(DatagramPacket p) throws IOException {
    if(dsocket == null) {
      throw new IOException("DatagramSocket is closed");
    }
    //we stay in receive until we receive a packet that is allowed to be received ...
    if (remoteAddress == null) { // we are not connected all Packets received are allowed  unless ...
       while (true) {
	       dsocket.receive(p);
	       try {
	         InetAddress.acceptCheck(p.getAddress().getHostName(), p.getPort());
	         break;
	       } catch(SecurityException se){}
	     }
    }
    else {
      if(p == null){
        throw new NullPointerException();
      }
      //peek is used to make sure p doesn't get corrupted when an error occurs
      // or contain information that should not been seen for security reasons
      if(peekGarbage == null){
        peekGarbage = new DatagramPacket(new byte[2],2);
      }
      InetAddress inAddr = new InetAddress();
      while (remoteport != (dsocket.peek(inAddr)) || !inAddr.equals(remoteAddress)) {
        //only packets to remoteAddress at remoteport are allowed ...	
        dsocket.receive(peekGarbage);
      }
      dsocket.receive(p);
    }		
  }

  /**
   * * gets the local address to which the socket is bound.
   */
  public InetAddress getLocalAddress() {
    if (dsocket != null) {
      try {
        InetAddress addr = (InetAddress) dsocket
            .getOption(SocketOptions.SO_BINDADDR);
        InetAddress.connectCheck(addr.getHostName(), -1);
        return addr;
      } catch (SocketException e) {
      } catch (SecurityException se) {
      }
    }
    return InetAddress.loopbackAddress;
  }

  /**
  ** The port number on the local host to which this socket is bound.
  */
  public int getLocalPort() {
    return dsocket == null ? -1 : dsocket.localPort;
  }

  /**
  **   set the socket timeout values.
  **   @throws throws an IllegalArgumentException if timeout < 0
  */
  public synchronized void setSoTimeout(int timeout) throws SocketException {
   	if (dsocket == null) {
      throw new SocketException("DatagramSocket is closed");
    }
    if (timeout < 0 ) {
   		throw new IllegalArgumentException();
   	}
   	dsocket.setOption(SocketOptions.SO_TIMEOUT , new Integer(timeout));
  }

  /**
  ** returns the timeout value of this socket.
  */
  public synchronized int getSoTimeout() throws SocketException {
    if (dsocket == null) {
      throw new SocketException("DatagramSocket is closed");
    }  
    return ((Integer) dsocket.getOption(SocketOptions.SO_TIMEOUT)).intValue();
  }

  public int getTrafficCless() {
    // TODO ...
    return 0;
  }

  /**
  ** This method tries to set the 'send' buffersize.  There is no guarantee this call will have
  ** an effect on the native socket ...
  */
  public synchronized void setSendBufferSize(int size) throws SocketException {
    if (dsocket == null) {
      throw new SocketException("DatagramSocket is closed");
    }
  	if (size <= 0 ) {
  	 	throw new IllegalArgumentException("size should be > 0, but got "+size);
  	}
  	dsocket.setOption(SocketOptions.SO_SNDBUF , new Integer(size));
  }

  /**
  ** returns the size of the 'send' buffers of this socket.
  */
  public synchronized int getSendBufferSize() throws SocketException {
    if (dsocket == null) {
      throw new SocketException("DatagramSocket is closed");
    }
  	return ((Integer) dsocket.getOption(SocketOptions.SO_SNDBUF)).intValue();
  }

  /**
  ** This method tries to set the 'recieve' buffersize.  There is no guarantee this call will have
  ** an effect on the native socket ...
  */
  public synchronized void setReceiveBufferSize(int size) throws SocketException {
    if (dsocket == null) {
      throw new SocketException("DatagramSocket is closed");
    }
  	if (size <= 0 ) {
  	 	throw new IllegalArgumentException("size should be > 0, but got "+size);
  	}
  	dsocket.setOption(SocketOptions.SO_RCVBUF , new Integer(size));
  }

  /**
  ** returns the size of the 'recieve' buffers of this socket.
  */
  public synchronized int getReceiveBufferSize() throws SocketException {
    if (dsocket == null) {
      throw new SocketException("DatagramSocket is closed");
    }
  	return ((Integer) dsocket.getOption(SocketOptions.SO_RCVBUF)).intValue();
  }

  public boolean getReuseAddress() throws SocketException {
    if (dsocket == null) {
      throw new SocketException("DatagramSocket is closed");
    }
    return ((Boolean) dsocket.getOption(SocketOptions.SO_REUSEADDR)).booleanValue();
  }

  /**
  ** closes this socket.
  */
  public void close() {
    disconnect();
  	if (dsocket != null) {
      dsocket.close();
      dsocket = null;
    }
  }
  
  public boolean isBound() {
    return getLocalAddress() != null;
  }

  public boolean isConnected() {
    return remoteAddress != null;
  }

  public boolean isClosed() {
    return dsocket == null;
  }
}
