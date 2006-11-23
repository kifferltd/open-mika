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
*                                                                         *
* Modifications Copyright (c) 2005 by Chris Gray, /k/ Embedded Java       *
* Solutions. Permission is hereby granted to copy and distribute this     *
* code under the terms of the Wonka Public Licence.                       *
*                                                                         *
**************************************************************************/


/*
** $Id: Inet4Address.java,v 1.3 2006/03/29 09:27:14 cvs Exp $ 
*/

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

