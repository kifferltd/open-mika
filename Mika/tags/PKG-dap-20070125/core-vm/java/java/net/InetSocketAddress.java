/**
 * Copyright  (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.
 * All rights reserved.
 *
 * $Id: InetSocketAddress.java,v 1.1 2006/03/29 09:27:14 cvs Exp $
 */
package java.net;


/**
 * InetSocketAddress: new since 1.4 
 *
 * @author ruelens
 *
 * created: Mar 28, 2006
 */
public class InetSocketAddress extends SocketAddress {  
  private static final long serialVersionUID = 5076001401234631237L;

  int port;
  InetAddress addr;
  String hostname;

  public InetSocketAddress(int port) {
    this(InetAddress.allZeroAddress, port);
  }

  public InetSocketAddress(InetAddress address, int port) {
    this.port = port;
    this.addr = address;
    this.hostname = address.getHostName();    
  }

  public InetSocketAddress(String hostname, int port) {
    this.port = port;
    this.hostname = hostname;
  }
  
  public final InetAddress getAddress() {
    return addr;
  }

  public final String getHostName() {
    return hostname;
  }

  public final int getPort() {
    return port;
  } 
  
  public final boolean isUnresolved() {
    return addr == null;
  }
  
  public final int hashCode() {
    return port ^ hostname.hashCode();
  }
  
  public final boolean equals(Object obj) {
    if (obj instanceof InetSocketAddress) {
      InetSocketAddress sock = (InetSocketAddress)obj;
      return port == sock.port &&
        (addr != null && sock.addr != null 
           ? addr.equals(sock.addr) 
           : hostname.equals(sock.hostname));       
    }
    return false;
  }
  
  public String toString() {
    return (addr == null ? hostname : addr.toString()) +":"+port;
  }
}
