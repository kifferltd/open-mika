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
