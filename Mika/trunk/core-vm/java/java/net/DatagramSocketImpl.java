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
