/**************************************************************************
* Parts copyright (c) 2005, 2009, 2016 by Chris Gray, KIFFER Ltd.         *
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
* 3. Neither the name of KIFFER Ltd nor the names of other contributors   *
*    may be used to endorse or promote products derived from this         *
*    software without specific prior written permission.                  *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL KIFFER LTD OR OTHER CONTRIBUTORS BE LIABLE FOR ANY    *
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL      *
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       *
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    *
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         *
* OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  *
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                              *
**************************************************************************/

package java.net;

import java.io.Serializable;
import java.util.StringTokenizer;

public final class Inet4Address extends InetAddress implements Serializable {

  /**
  ** creates an empty InetAddress used by peek
  */
  Inet4Address(){}

  /**
   ** Create an InetAddress object with the given fields.
   ** If ttl > 0 an entry will also be created in the address cache.
   ** @param ip The IP address in integer form
   ** @param name The hostname
   ** @param ipname The IP address in dotted-string form
   ** @param ttl The time-to-live, in seconds
   */
  Inet4Address(int ip, String name, String ipname, int ttl){
    address = ip;
    family = InetAddress.TYPE_IPV4;
    hostName = name;
    ipAddressString = ipname.toLowerCase();
    if (name != null && ttl > 0) {
      long expiry = System.currentTimeMillis() + (1000L * ttl);
      synchronized(positive_cache){
        positive_cache.put(name, new IAddrCacheEntry(this, expiry));
        positive_cache.put(ipAddressString, new IAddrCacheEntry(this, expiry));
      }
    }
  }

  /**
   ** Create an InetAddress object with the given fields.
   ** If ttl > 0 an entry will also be created in the address cache.
   ** @param ipAddress The binary IP address
   ** @param hostname The hostname
   ** @param ttl The time-to-live, in seconds
   */
  Inet4Address(byte[] ipAddress, String hostname, int ttl) throws UnknownHostException {
    this( (((((((ipAddress[0] << 8) | ipAddress[1]) << 8) | ipAddress[2]) << 8) | ipAddress[3]) << 8),
          hostname,
          (ipAddress[0] & 0xff) + "." + (ipAddress[1] & 0xff) + "." + (ipAddress[2] & 0xff) + "." + (ipAddress[3] & 0xff), 
          ttl
        );
  }

  /**
   ** Create an InetAddress object with the given fields.
   ** If ttl > 0 an entry will also be created in the address cache.
   ** @param name The hostname
   ** @param ttl The time-to-live, in seconds
   */
  Inet4Address(String name, int ttl) throws UnknownHostException {
    int char0 = name.charAt(0);
    if(char0 >= '0' && char0 <= '9'){
      StringTokenizer s = new StringTokenizer(name,".");
      if(s.countTokens() == 4){
        try {
          for(int i = 0 ; i < 4 ; i++){
            int octet = Integer.parseInt(s.nextToken());
            if(octet < 0 || octet > 255){
              throw new UnknownHostException();
            }
            address = octet + (address<<8);
          }
        }
        catch(NumberFormatException nfe){
          throw new UnknownHostException();
        }
      }
      family = InetAddress.TYPE_IPV4;
      if (ttl > 0) {
        long expiry = System.currentTimeMillis() + (1000L * ttl);
        synchronized(positive_cache){
          positive_cache.put(getHostAddress(), new IAddrCacheEntry(this, expiry));
          positive_cache.put(name, new IAddrCacheEntry(this, expiry));
        }
      }
    }
    else {
      createInetAddress(name);
      if (ttl > 0) {
        long expiry = System.currentTimeMillis() + (1000L * ttl);
        synchronized(positive_cache){
          // We cache this under both the requested name and the canonical name
          positive_cache.put(name, new IAddrCacheEntry(this, expiry));
          positive_cache.put(this.getHostName(), new IAddrCacheEntry(this, expiry));
          positive_cache.put(this.getHostAddress(), new IAddrCacheEntry(this, expiry));
        }
      }
    }
  }
  
  public byte[] getAddress() {
   	byte[] octets = new byte[4];
   	octets[3] = (byte)address;
   	octets[2] = (byte)(address>>>8);
   	octets[1] = (byte)(address>>>16);
   	octets[0] = (byte)(address>>>24);
    return octets;
  }

  public boolean isMulticastAddress () {
    final int firstByte = getAddress()[0] & 0xff;
    return firstByte > 223 && firstByte < 240;
  }

  public boolean isAnyLocalAddress() {
    return address == 0;
  }

  public boolean isLinkLocalAddress() {
    // According to the Android sources:
    // The reference implementation does not return true for loopback
    // addresses even though RFC 3484 says to do so
    final byte[] bytes = getAddress();
    final int firstByte = bytes[0] & 0xff;
    final int secondByte = bytes[1] & 0xff;
    return firstByte == 169 && secondByte == 254;
  }

  public boolean isLoopbackAddress() {
    byte[] ip = getAddress();
    return ip[0] == 127;
  }

  public boolean isMCGlobal() {
    if (!isMulticastAddress()) {
      return false;
    }
    byte[] ip = getAddress();
    switch(ip[0]) {
    // must be 0xe0 -- 0xef or isMulticastAddress() would have returned false
    case (byte) 0xe0:
      return !(ip[1] == 0 && ip[2] == 0);
    case (byte) 0xef:
      return false;
    default:
      return true;
    }
  }

  public boolean isMCLinkLocal() {
    byte[] ip = getAddress();
    return ip[0] == 0xe0 && ip[1] == 00 && ip[2] == 0;
  }

  public boolean isMCOrgLocal() {
    byte[] ip = getAddress();
    return ip[0] == 0xef && (ip[1] & 0xfc) == 0xc0;
  }

  public boolean isMCSiteLocal() {
    byte[] ip = getAddress();
    return ip[0] == 0xef && ip[1] == 0xff;
  }

  public boolean isSiteLocalAddress() {
    final byte[] bytes = getAddress();
    final int firstByte = bytes[0] & 0xff;
    final int secondByte = bytes[1] & 0xff;
    // 10/8
    if (firstByte == 10) {
      return true;
    }
    // 172.16/12
    if (firstByte == 172 && (secondByte & 0xf0) == 0x10) {
      return true;
    }
    // 192.168/16
    if (firstByte == 192 && secondByte == 168) {
      return true;
    }
    return false;
  }

}

