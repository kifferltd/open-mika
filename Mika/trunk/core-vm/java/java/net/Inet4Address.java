/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2005 by Chris Gray, /k/ Embedded Java Solutions.    *
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
    if (ttl > 0) {
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
    byte[] ip = getAddress();
    return (((ip[0]&0xFF) >223) && ((ip[0]&0xFF)<240));
  }

  public String getCanonicalHostName() {
    return null;
  }

  public boolean isAnyLocalAddress() {
    return false;
  }

  public boolean isLinkLocalAddress() {
    return false;
  }

  public boolean isLoopbackAddress() {
    return false;
  }

  public boolean isMCGlobal() {
    return false;
  }

  public boolean isMCLinkLocal() {
    return false;
  }

  public boolean isMCNodeLocal() {
    return false;
  }

  public boolean isMCOrgLocal() {
    return false;
  }

  public boolean isMCSiteLocal() {
    return false;
  }

  public boolean isSiteLocalAddress() {
    return false;
  }

}

