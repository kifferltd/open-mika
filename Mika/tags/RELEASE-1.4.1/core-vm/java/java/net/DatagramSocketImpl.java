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

public abstract class DatagramSocketImpl implements SocketOptions {

  protected int localPort;
  protected FileDescriptor fd;

  /**
   * default constructor ...
   */
   public DatagramSocketImpl(){
   }

  /**
   * Create a datagram socket
   */
  protected abstract void create() throws SocketException;

  /**
   * Binds a datagram socket to a local port and address.
   */
  protected abstract void bind(int lport, InetAddress laddr) throws SocketException;

  /**
   * Sends a datagram packet. The packet contains the data and the destination address to send the packet to.
   */
  protected abstract void send(DatagramPacket p) throws IOException;

  /**
   * Peek at the packet to see who it is from.
   */
  protected abstract int peek(InetAddress i) throws IOException;

  /**
   * Receive the datagram packet.
   */
  protected abstract void receive(DatagramPacket p) throws IOException;

  /**
   * @deprecated. use setTimeToLive instead.
   * Set the TTL (time-to-live) option.
   */
  protected abstract void setTTL(byte ttl) throws IOException;

  /**
   * @deprecated. use getTimeToLive instead.
   * Retrieve the TTL (time-to-live) option.
   */
  protected abstract byte getTTL() throws IOException;

  /**
   * Set the TTL (time-to-live) option.
   */
  protected abstract void setTimeToLive(int ttl) throws IOException;


  /**
   * Retrieve the TTL (time-to-live) option.
   */
  protected abstract int getTimeToLive() throws IOException;


  /**
   * Join the multicast group.
   */
  protected abstract void join(InetAddress inetaddr) throws IOException;

  /**
   * Leave the multicast group.
   */
  protected abstract void leave(InetAddress inetaddr) throws IOException;

  /**
   * Close the socket.
   */
  protected abstract void close();

  /**
   * Get the local port.
   */
  protected int getLocalPort() {
    return localPort;
  }

  /**
   * Get the datagram socket file descriptor
   */
  protected FileDescriptor getFileDescriptor() {
    return fd;
  }
}
