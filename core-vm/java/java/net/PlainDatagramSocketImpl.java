/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2007, 2013 by Chris Gray, /k/ Embedded Java         *
* Solutions.  All rights reserved.                                        *
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

import java.io.*;

class PlainDatagramSocketImpl extends DatagramSocketImpl {

  private int timeout; //0 = blocks forever
  private boolean open;


  public PlainDatagramSocketImpl(){}

  /**
   * Create a datagram socket
   */
  protected synchronized void create() throws SocketException {
    fd = new FileDescriptor();
    nativeCreate();
  }

  private native void nativeCreate();

  /**
   * Binds a datagram socket to a local port and address.
   */
  protected synchronized native void bind(int lport, InetAddress laddr) throws SocketException;

  /**
   * Sends a datagram packet. The packet contains the data and the destination address to send the packet to.
   */
  protected native void send(DatagramPacket p) throws IOException;

  /**
   * Peek at the packet to see who it is from.
   */
  protected native int peek(InetAddress i) throws IOException;

  /**
   * Receive the datagram packet.
   */
  protected void receive(DatagramPacket p) throws IOException {
    synchronized(p){
      try {
        SocketUsers.put(this, Thread.currentThread());
        int ip = _receive(p);
        p.setAddress(InetAddress.createInetAddress(ip));
      }
      finally {
        SocketUsers.remove(this);
      }
    }
  }

  private native int _receive(DatagramPacket p) throws IOException;

  /**
   * @deprecated. use setTimeToLive instead.
   * Set the TTL (time-to-live) option.
   */
  protected synchronized void setTTL(byte ttl) throws IOException {
   	setTimeToLive(0x0ff & ttl);
  }

  /**
   * @deprecated. use getTimeToLive instead.
   * Retrieve the TTL (time-to-live) option.
   */
  protected synchronized byte getTTL()throws IOException {
   	return (byte)getTimeToLive();
  }

  /**
   * Join the multicast group.
   */
  protected void join(InetAddress inetaddr) throws IOException {
    join(inetaddr, null);
  }
 
   /**
   * Join the multicast group.
   */
  protected void joinGroup(InetAddress inetaddr, NetworkInterface nw) throws IOException {
    join(inetaddr, (InetAddress) nw.getInetAddresses().nextElement());
  }

  /**
   * Leave the multicast group.
   */
  protected synchronized native void leave(InetAddress inetaddr) throws IOException;

  /**
   * Close the socket.
   */
  protected void close() {
    Thread t = SocketUsers.get(this);
    _close();
    if (t != null) {
      signal(t);
    }
  }

  protected synchronized native void _close();

  /**
   * Set the TTL (time-to-live) option.
   */
  protected synchronized native void setTimeToLive(int ttl) throws IOException;

  /**
   * Retrieve the TTL (time-to-live) option.
   */
  protected synchronized native int getTimeToLive() throws IOException;

  public synchronized Object getOption(int opt) throws SocketException {
    int sock = getSocket();
    if(opt == SO_TIMEOUT){
   	  return new Integer(timeout);
   	}
   	return options(opt, null, sock);
  }

  public synchronized void setOption(int opt, Object value) throws SocketException {
    if(value == null){
      throw new SocketException("a non 'null' option value is required");
    }
    int sock = getSocket();
    if(opt == SO_TIMEOUT){
      if(value instanceof Integer){
        timeout = ((Integer)value).intValue();
        setSoTimeout(sock, timeout);
      }
    }
    else {
      options(opt, value, sock);
    }
  }

  private native int getSocket() throws SocketException;

/*
  SO_BINDADDR = 0x000F;    --> InetAddress
  SO_LINGER = 0x0080;      --> Boolean or Integer
  TCP_NODELAY = 0x0001;    --> Boolean
  IP_MULTICAST_IF = 0x10;  --> InetAddress
  SO_REUSEADDR = 0x04;     --> Integer
  SO_TIMEOUT = 0x1006;     --> Integer
  SO_SNDBUF =0x1001;       --> Integer
  SO_RCVBUF = 0x1002;      --> Integer
*/
  static Object options(int opt, Object value, int sock) throws SocketException {
    try {
      int i=0;
      switch (opt) {
        case SO_BINDADDR:
          if(value == null){//get
            return InetAddress.createInetAddress(getBindAddress(sock));
          }
          break;
        case SO_BROADCAST:
          if(value == null){//get
            return new Boolean(optIntOptions(sock, -1, 7) != 0);
          }
          optIntOptions(sock, ((Boolean)value).booleanValue() ? 1 : 0, 7); //set
          break;
        case SO_LINGER:
          if(value == null){//get
            int res = optLinger(sock, -1);
            if (res == 0){
              return new Boolean(false);
            }
            else {
              return new Integer(res);
            }
          }
          else {//set
            if((value instanceof Boolean) && !((Boolean)value).booleanValue()){
              optLinger(sock, 0);
            }
            else {
              optLinger(sock, ((Integer)value).intValue());
            }
          }
          break;
        case TCP_NODELAY:
          if(value == null){//get
            return new Boolean(optNoDelay(sock, true, true));
          }
          optNoDelay(sock, ((Boolean)value).booleanValue(), false); //set
          break;
        case SO_SNDBUF:
          i++;
        case SO_RCVBUF:
          i++;
       case SO_REUSEADDR:
          if(value == null){//get
            return new Integer(optIntOptions(sock, -1, i));
          }

          /*
           * According to http://java.sun.com/javame/reference/apis/jsr219/java/net/SocketOptions.html
           * this code should handle being passed a Boolean or a Integer
           * also should review other SocketOptions and refactor as required.
           */
         try {
           optIntOptions(sock, ((Boolean)value).booleanValue() ? 1 : 0, i); //set
          } catch (ClassCastException cce) {
           optIntOptions(sock, 1, i); //set
          }
          break;

        case IP_MULTICAST_IF:
          if(value == null){//get
            return InetAddress.createInetAddress(optMulticastIF(sock, null, true));
          }
          optMulticastIF(sock, (InetAddress)value, false); //set
          break;

        default:
          throw new SocketException("no valid option specified");
       }
    } catch(ClassCastException cce){
      throw new SocketException("no valid value specified");
    }
    return null;
  }

  protected native void finalize();

  private native void signal(Thread t);
  // sock is the filedesc of the socket
  private static native int getBindAddress(int sock) throws SocketException ;
  private static native int optLinger(int sock, int value) throws SocketException ; // value == -1 if get is wanted ...
  private static native boolean optNoDelay(int sock, boolean value, boolean set) throws SocketException ;
  private static native int optMulticastIF(int sock, InetAddress value, boolean set) throws SocketException ;
  private static native int optIntOptions(int sock, int value, int opt) throws SocketException ;// value == -1 if get is wanted
  private static native void setSoTimeout(int sock, int t) throws SocketException;
  private synchronized native void join(InetAddress group, InetAddress local) throws IOException;

}
