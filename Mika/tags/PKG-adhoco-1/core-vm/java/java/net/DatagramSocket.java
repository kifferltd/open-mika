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
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      java.security.AccessController.checkPermission(new SocketPermission(addr.getHostAddress(),"accept,connect"));
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkMulticast(addr);
      }
    }
  }

  /**
  ** Special Constructor to create MulticastSockets ...
  */
  DatagramSocket(boolean Multicast){}


  public DatagramSocket() throws SocketException, SecurityException {
  	this(0,null);
  }

  public DatagramSocket(int port) throws SocketException, SecurityException {
  	this(port, null);
  }

  public DatagramSocket(int port, InetAddress laddr) throws SocketException, SecurityException {
    if (laddr == null) {
      laddr = InetAddress.allZeroAddress;
    }
    if(port < 0 || port > 65535) {
      throw new IllegalArgumentException();
    }
    InetAddress.listenCheck(port);
    if(theFactory != null){
      dsocket = theFactory.createDatagramSocketImpl();
    }
    else {
      String s = "java.net."+GetSystemProperty.IMPL_PREFIX+"DatagramSocketImpl";
      try {	
        dsocket = (DatagramSocketImpl) Class.forName(s).newInstance();
      }
      catch(Exception e) {
        dsocket = new PlainDatagramSocketImpl();
      }
    }
  	
    dsocket.create();
    dsocket.bind(port,laddr);  	
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
  ** Disconnects the socket. This does nothing if the socket is not connected.
  **
  */
  public void disconnect() {
  	remoteAddress = null;
  	remoteport = -1;
  }

  /**
  ** The address to which this socket is connected or  null if not connected.
  */
  public InetAddress getInetAddress() {
    return remoteAddress;
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

  /**
  ** closes this socket.
  */
  public void close() {
  	if (dsocket != null) {
      dsocket.close();
      dsocket = null;
    }
  }
  
  public boolean isClosed() {
    return dsocket == null;
  }
}
