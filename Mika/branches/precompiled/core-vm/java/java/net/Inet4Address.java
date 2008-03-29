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

  Inet4Address(int ip, String name, String ipname){
    address = ip;
    family = InetAddress.TYPE_IPV4;
    hostName = name;
    addressCache = ipname.toLowerCase();
    synchronized(positive_cache){
      positive_cache.put(name, new IAddrCacheEntry(this));
      positive_cache.put(addressCache, new IAddrCacheEntry(this));
    }
  }

  /**
  ** calling this constructor will add the address to the addressCache !
  */
  Inet4Address(String name) throws UnknownHostException {
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
      synchronized(positive_cache){
        positive_cache.put(getHostAddress(), new IAddrCacheEntry(this));
        positive_cache.put(name, new IAddrCacheEntry(this));
      }
    }
    else {
      createInetAddress(name);
      synchronized(positive_cache){
        positive_cache.put(this.getHostName(), new IAddrCacheEntry(this));
        positive_cache.put(this.getHostAddress(), new IAddrCacheEntry(this));
      }
    }
  }
  
/*
  private static String intToIPString(int address) {
      StringBuffer result = new StringBuffer(16);
      result.append((address>>>24 & 0x0ff));
      result.append('.');
      result.append((address>>>16 & 0x0ff));
      result.append('.');
      result.append((address>>>8 & 0x0ff));
      result.append('.');
      result.append((address & 0x0ff));
      return result.toString();
  }
*/
  
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

