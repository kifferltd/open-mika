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

/**
 ** This class represents a Multicastsocket for sending and receiving datagram packets.
 **
 ** UDP broadcasts sends and receives are always enabled on a DatagramSocket.
 */

public class MulticastSocket extends DatagramSocket {

  public MulticastSocket() throws IOException  {
  	this(0);
  }

  /**
  ** a checkListen security check is done.
  */
  public MulticastSocket(int port) throws IOException {
    super(true);
    if(port < 0 || port > 65535) {
      throw new IllegalArgumentException();
    }
    InetAddress.listenCheck(port);
    if(DatagramSocket.theFactory != null){
      dsocket = DatagramSocket.theFactory.createDatagramSocketImpl();
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
    dsocket.setOption(SocketOptions.SO_REUSEADDR, new Integer(1));
    dsocket.bind(port, InetAddress.allZeroAddress);  	
  }

  public InetAddress getInterface() throws SocketException {
  	return (InetAddress)dsocket.getOption(SocketOptions.IP_MULTICAST_IF);
  }

  private static void multicastCheck(InetAddress addr, byte ttl) {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      java.security.AccessController.checkPermission(new SocketPermission(addr.getHostAddress(), "accept,connect"));
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkMulticast(addr,ttl);
      }
    }
  }

  /**
  ** @remark it is allowed to join a group twice if the native socket doesn't return an error
  ** when joining the second time. This happens on Linux.
  */
  public void joinGroup(InetAddress groupAddr) throws IOException {
   	DatagramSocket.multicastCheck(groupAddr);
   	dsocket.join(groupAddr);
  }

  public void leaveGroup(InetAddress groupAddr) throws IOException {
   	DatagramSocket.multicastCheck(groupAddr);
   	dsocket.leave(groupAddr);
  }

  public synchronized void send(DatagramPacket dgram, byte ttl) throws IOException {
   	InetAddress dest  = dgram.getAddress();
   	if (dest.isMulticastAddress()){
      multicastCheck(dest, ttl);
      if (remoteAddress != null && (!remoteAddress.equals(dest) || remoteport != dgram.getPort())) {
  	   //only packets to remoteAddress at remoteport are allowed if we are connected ...	
       throw new IllegalArgumentException("packet has wrong destination ...");	     	
      }		
      byte oldttl = getTTL();
      setTTL(ttl);
      dsocket.send(dgram);	
      setTTL(oldttl);
   	}
   	else {
   	  send(dgram);
   	}
  }

  public void setInterface(InetAddress addr) throws SocketException{
  	dsocket.setOption(SocketOptions.IP_MULTICAST_IF, addr);
  }

  /**
  ** deprecated use getTimeToLive
  */
  public byte getTTL() throws IOException {
    return (byte)dsocket.getTimeToLive();
  }

  /**
  ** deprecated use setTimeToLive
  */
  public void setTTL(byte ttl) throws IOException {
    dsocket.setTimeToLive(0x0ff & ttl);
  }

  public int getTimeToLive() throws IOException {
	  return dsocket.getTimeToLive();
  }

  public void setTimeToLive(int ttl) throws IOException {
	  dsocket.setTimeToLive(ttl);
  }
}
