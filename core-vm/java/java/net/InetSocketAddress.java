/**************************************************************************
* Copyright (c) 2006, 2007 by Chris Gray, /k/ Embedded Java Solutions.    *
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
* 3. Neither the name of /k/ Embedded Java Solutions nor the names of     *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL /K/ EMBEDDED JAVA SOLUTIONS OR OTHER CONTRIBUTORS BE  *
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR     *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

package java.net;


/**
 * InetSocketAddress: new since 1.4 
 *
 * @author ruelens,cgray
 * @since 1.4
 *
 * created: Mar 28, 2006
 */
public class InetSocketAddress extends SocketAddress {  
  private static final long serialVersionUID = 5076001401234631237L;

  int port;
  InetAddress addr;
  String hostname;

  public InetSocketAddress(int port) throws IllegalArgumentException {
    this(InetAddress.allZeroAddress, port);
  }

  public InetSocketAddress(InetAddress address, int port) throws IllegalArgumentException {
    this.port = checkPort(port);
    this.hostname = address.getHostName();    
    if (this.hostname != null) {
      try {
        this.addr = InetAddress.getByName(hostname);
      } catch (Exception e) {
      }
    }
    else if (address == null) {
      try {
        this.addr = InetAddress.getByAddress(null, new byte[4]);
      }
      catch (UnknownHostException uhe) {
      }
    }
    else {
      this.addr = address;
    }
  }

  public InetSocketAddress(String hostname, int port) throws IllegalArgumentException {
    this.port = checkPort(port);
    this.hostname = hostname;
    try {
      this.addr = InetAddress.getByName(hostname);
    }
    catch (NullPointerException npe) {
      throw new IllegalArgumentException();
    }
    catch (Exception e) {
    }
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

  private int checkPort(int port) {
    if (port >= 0 && port < 0x10000) {
      return port;
    }
    throw new IllegalArgumentException();
  }

  private InetAddress checkAddress(InetAddress addr) {

    return addr;
  }

}
