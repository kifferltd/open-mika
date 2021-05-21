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

public final class Inet6Address extends InetAddress implements Serializable {

  byte[] ipaddress = new byte[16];

  Inet6Address(){}

  Inet6Address(String name) throws UnknownHostException {
    int idx;
    String tmp = name;
    if(tmp.indexOf(':') != -1) {
      if(tmp.charAt(0) == '[') {
        tmp = tmp.substring(1, tmp.length() - 1);
        name = tmp;
      }
      
      if(tmp.indexOf('.') != -1) {
        
        /* ipv4 address embedded */
        
        idx = tmp.lastIndexOf(':');
        String ipv4 = tmp.substring(idx + 1, tmp.length());
        tmp = tmp.substring(0, idx + 1);

        StringTokenizer st = new StringTokenizer(ipv4, ".");
        
        String ip1 = Integer.toString((Integer.parseInt(st.nextToken()) << 8) + Integer.parseInt(st.nextToken()), 16);
        String ip2 = Integer.toString((Integer.parseInt(st.nextToken()) << 8) + Integer.parseInt(st.nextToken()), 16);
        
        tmp += ip1 + ":" + ip2;
      } 

      if((idx = tmp.indexOf("::")) != -1) {

        /* Need to expand 0's */
        
        StringTokenizer st = new StringTokenizer(tmp, ":");
        int count = st.countTokens();

        String sub = "";

        for(; count < 8; count++) sub += "0:";

        String tmp1 = (tmp.charAt(0) == ':' ? "" : tmp.substring(0, idx + 1));
        String tmp2 = tmp.substring(idx + 2, tmp.length());
        tmp = tmp1 + sub + tmp2;
      }

      StringTokenizer st = new StringTokenizer(tmp, ":");
      int b = 0;
      while(st.hasMoreTokens()) {
        int i = Integer.parseInt(st.nextToken(), 16);
        ipaddress[b++] = (byte)(i >> 8);
        ipaddress[b++] = (byte)(i & 0xff);
      }
    }

    family = InetAddress.TYPE_IPV6;
    hostName = name;
    
    ipAddressString = tmp;
    createInetAddress(name);
  }

  public byte[] getAddress() {
    return ipaddress;
  }

  public boolean isMulticastAddress () {
    return ipaddress[0] == -1;
  }

  public String getCanonicalHostName() {
    return null;
  }

  public boolean isAnyLocalAddress() {
    for (int i = 0; i < 16; i++) {
      if (ipaddress[i] != 0) {
        return false;
      }
    }
    return true;
  }

  public boolean isLinkLocalAddress() {
   return ipaddress[0] == -2 && (ipaddress[1] & 0xc0) == 0x80;
  }

  public boolean isLoopbackAddress() {
    for (int i = 0; i < 15; i++) {
      if (ipaddress[i] != 0) {
        return false;
      }
    }
    return ipaddress[15] == 1;
  }

  public boolean isMCGlobal() {
   return ipaddress[0] == -1 && (ipaddress[1] & 0x0f) == 0x0e;
  }

  public boolean isMCLinkLocal() {
   return ipaddress[0] == -1 && (ipaddress[1] & 0x0f) == 2;
  }

  public boolean isMCNodeLocal() {
   return ipaddress[0] == -1 && (ipaddress[1] & 0x0f) == 1;
  }

  public boolean isMCOrgLocal() {
   return ipaddress[0] == -1 && (ipaddress[1] & 0x0f) == 8;
  }

  public boolean isMCSiteLocal() {
   return ipaddress[0] == -1 && (ipaddress[1] & 0x0f) == 5;
  }

  public boolean isSiteLocalAddress() {
   return ipaddress[0] == -2 && (ipaddress[1] & 0xc0) == 0xc0;
  }

  public boolean isIPvCompatibleAddress() {
    for (int i = 0; i < 12; i++) {
      if (ipaddress[i] != 0) {
        return false;
      }
    }
    return true;
  }

  private static String IPtoIPString(byte[] address) {
    int b = 0;
    String result = "";
    for(int i=0; i<8; i++) {
      result += Integer.toString(((address[b++] & 0xFF) << 8) | (address[b++] & 0xFF), 16);
      if(i != 7) result += ":";
    }

    return result;
  }

  public String getHostAddress() {
    if(ipAddressString == null){
      ipAddressString = IPtoIPString(ipaddress);
    }
    return ipAddressString;
  }

  public String toString () {
    return getHostName() + "/" + getHostAddress();
  }
}

