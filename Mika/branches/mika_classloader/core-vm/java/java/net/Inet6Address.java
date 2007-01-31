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


/*
** $Id: Inet6Address.java,v 1.3 2006/03/29 09:27:14 cvs Exp $ 
*/

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
    
    /*
    if(char0 >= '0' && char0 <= '9'){
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
    */
    addressCache = tmp;
    createInetAddress(name);
  }

  public byte[] getAddress() {
    return ipaddress;
  }

  public boolean isMulticastAddress () {
    return false;
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
    if(addressCache == null){
      addressCache = IPtoIPString(ipaddress);
    }
    return addressCache;
  }

  public String toString () {
    return getHostName() + "/" + getHostAddress();
  }
}

