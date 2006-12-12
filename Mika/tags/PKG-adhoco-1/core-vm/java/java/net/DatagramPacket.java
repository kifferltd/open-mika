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

/**
 ** This class represents a datagram packet. 
 **
 ** Datagram packets are used to implement a connectionless packet delivery 
 ** service. Each message is routed from one machine to another based solely 
 ** on information contained within that packet. Multiple packets sent from 
 ** one machine to another might be routed differently, and might arrive in 
 ** any order. 
 */

public final class DatagramPacket {

  private byte[] bytes;
  private int length;
  private int offset;
  private int port;
  private InetAddress address;
  private InetSocketAddress sockaddr;

  /**
  ** Constructs a DatagramPacket useable to revieve ...
  */
  public DatagramPacket (byte[] buf, int offset, int length) throws IllegalArgumentException {
    if (offset<0 || length<0 || offset+length>buf.length) {
      throw new IllegalArgumentException();
    }
    bytes = buf;
    this.length = length;
    this.offset = offset;
    this.address = null;
    this.port = -1;
  }

  /**
  ** Constructs a DatagramPacket useable to revieve ...
  */
  public DatagramPacket (byte[] buf, int length) throws IllegalArgumentException {
    this(buf,0,length);
  }

  /**
  ** Constructs a DatagramPacket to send datagrams ...
  */
  public DatagramPacket(byte[] buf, int offset, int length, InetAddress address, int port)  throws IllegalArgumentException {
    if (offset<0 || length<0 || offset+length>buf.length || port < 0 || port > 65535) {
      throw new IllegalArgumentException();
    }
    if(address == null){
      throw new NullPointerException();
    }
    bytes = buf;
    this.length = length;
    this.offset = offset;
    this.address = address;
    this.port = port;
  }

  /**
  ** Constructs a DatagramPacket to send datagrams ...
  */
  public DatagramPacket(byte[] buf, int length, InetAddress address, int port) throws IllegalArgumentException {
    this(buf,0,length,address,port);
  }

  public DatagramPacket(byte[] bytes, int length, SocketAddress address) throws SocketException {
    this(bytes, 0, length, address);
  }
  
  
  public DatagramPacket(byte[] buf, int offset, int length, 
      SocketAddress address) throws SocketException {
    setSocketAddress(address);
    bytes = buf;
    this.length = length;
    this.offset = offset;
  }

  public void setSocketAddress(SocketAddress address) {
    if(!(address instanceof InetSocketAddress)) {
      throw  new IllegalArgumentException();
    }
    InetSocketAddress iaddr = (InetSocketAddress) address;
    
    this.address = iaddr.getAddress();
    if(address == null) {
      try {
        this.address = InetAddress.getByName(iaddr.getHostName());
      } catch (UnknownHostException e) {
        throw new IllegalArgumentException(e.getMessage());
      }
    }
    this.port = iaddr.getPort();
    this.sockaddr = iaddr;
  }

  /**
  ** returns the address from the remote host
  */
  public synchronized InetAddress getAddress() {
    return address;
  }

   /**
   ** returns the address from the remote host
   */
  public synchronized int getPort() {
    return port;
  }

  /**
   ** getData() returns the data received or the data to be sent.
   ** This is simply a pointer to the buffer originally supplied, so
   ** 1) the real data may start after the start of the buffer returned,
   **    or finish before the end; the caller has to keep track of what
   **    offset and length parameters were passed.
   ** 2) Any changes to the contents of the returned buffer affect the 
   **    DatagramPacket itself.
   */
  public synchronized byte[] getData() {
     return bytes;
  }

  /**
   ** getOffset() retrieves the offset where the data begins in the buffer
   ** returned by getData().
   */
  public synchronized int getOffset() {
    return offset;
  }

  /**
   ** getLength() retrieves the length of the data in the buffer returned
   ** by getData(). Note: if you construct a DatagramPacket to recieve data,  getLength() will
   ** return the maximum number of bytes to be put in the array.
   */
  public synchronized int getLength() {
      return length;
  }

  /**
   ** setAddress(InetAddress iaddr) sets the remote address of this packet.
   */
  public synchronized void setAddress(InetAddress iaddr) {
    if (iaddr == null) {
     	throw new NullPointerException("no null address allowed");
    }
    address = iaddr;
  }
  /**
   ** setPort(int port) sets the remote port of this packet.
   */
  public synchronized void setPort(int port) {
    if (port < 0 || port > 65535) {
      throw new IllegalArgumentException("invalid port number "+port);
    }
    this.port = port;
  }

  /**
   ** setData(byte[] buf) assigns a new buffer to the packet.
   ** The offset is set to zero(*), and the length is set to
   ** the lesser of the old length and the length of the new buffer.
   ** offset, and length to the packet.
   ** (*) Is this right?  Impossible to tell from Sun's %^&* documentation.
   */
  public synchronized void setData(byte[] buf) {
    if(length > buf.length){
      length = buf.length;
    }
    bytes = buf;
    offset = 0;
  }

  public SocketAddress getSocketAddress() {
    if (sockaddr == null) {
      sockaddr = new InetSocketAddress(address, port);
    }
    return sockaddr;
  }
  
  /**
   ** setData(byte[] buf, int offset, int length) assigns a new buffer,
   ** offset, and length to the packet.
   */
  public synchronized void setData(byte[] buf, int off, int len) {
    if (off < 0 || len < 0 || buf.length < off + len) {
      throw new IllegalArgumentException();
    }
    bytes = buf;
    offset = off;
    length = len;
  }

  /**
   ** setLength(int length) sets the length for this packet.
   ** @remark length >= 0 and offset+length <= buffer.length
   */
  public synchronized void setLength(int length) throws IllegalArgumentException {
    if (length<0 || (offset + length)>bytes.length) {
      throw new IllegalArgumentException();
    }
    this.length = length;
  }
}
