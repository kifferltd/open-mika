/**************************************************************************
* Copyright (c) 2007, 2009, 2022 by Chris Gray, KIFFER Ltd.               *
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
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS *
* OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)   *
* HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,     *
* STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING   *
* IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE      *
* POSSIBILITY OF SUCH DAMAGE.                                             *
**************************************************************************/

package java.net;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * NetworkInterface:
 *
 * @author ruelens
 *
 * created: Mar 28, 2006
 */
public final class NetworkInterface {

  /**
   * AddrEnum:
   *
   * @author ruelens
   *
   * created: Mar 28, 2006
   */
  private static class AddrEnum implements Enumeration {

    private InetAddress[] addr;
    private int current;
    private int next;

    /**
     * @param addresses the array to enumerate over;
     * 
     */
    public AddrEnum(InetAddress[] addresses) {
      this.addr = addresses;
      next = -1;
      current = -1;
    }

    /**
     * @see java.util.Enumeration#hasMoreElements()
     */
    public boolean hasMoreElements() {
      if(next == current) {
        while(++next < addr.length) {
          try {
            InetAddress ad = addr[next];
            InetAddress.connectCheck(ad.hostName,-1);
            break;
          } catch (SecurityException se){}
        }
      }
      return next < addr.length;
    }

    /**
     * @see java.util.Enumeration#nextElement()
     */
    public Object nextElement() throws NoSuchElementException {
      if(!hasMoreElements()) {
        throw new NoSuchElementException();
      }
      current = next;
      return addr[current];
    }

  }

  private String name;
  private InetAddress[] addresses;
  private String displayName; 

  private NetworkInterface(String name, int ip) {
    this.name = name;
    this.displayName = name;
    addresses = new InetAddress[]{InetAddress.createFromInteger(ip)};
  }

  public String getName() {
    return name;
  }

  public Enumeration getInetAddresses() {
     if (addresses == null) {
       return new Vector(0).elements();
     }

     return new AddrEnum(addresses); 
  }


  public String getDisplayName() {
    return displayName != null && !"".equals(displayName) ? displayName : name;
  }

  public boolean equals(Object obj) {
    if (obj instanceof NetworkInterface) {
      NetworkInterface ni = (NetworkInterface) obj;
      return this.name.equals(ni.name) &&
       Arrays.equals(this.addresses, ni.addresses);
    }
    return false;
  }

  public int hashCode() {
    int hc = 9753113;
    for(int i = 0 ; i < addresses.length ; i++) {
      hc ^= addresses[i].hashCode(); 
    }
    return name.hashCode() ^ hc;
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer().append(name).append(':');
    for(int i = 0 ; i < addresses.length ; i++) {
      (i>0 ? buffer.append(',') : buffer).append(addresses[i]);
    }
    return buffer.toString();
  }

  public static NetworkInterface getByName(String name) throws SocketException {
    int ip = getAddressDevice(name);    
    if(ip != -1) {
      return new NetworkInterface(name,ip);
    }
    return null;      
  }

  /*
   * TODO: rework !
   */ 
  public static NetworkInterface getByInetAddress(InetAddress addr) throws SocketException {
    Enumeration enumeration = getNetworkInterfaces();
    while (enumeration.hasMoreElements()) {
      NetworkInterface element = (NetworkInterface) enumeration.nextElement();
      InetAddress[] addresses = element.addresses;
      for(int i=0 ; i < addresses.length ; i++) {
        if(addr.equals(addresses[i])) {
          return element;
        }
      }      
    }
    return null;
  }

  public static Enumeration getNetworkInterfaces() throws SocketException {
    Vector list = new Vector();
    nativeGetInterfaces(list);
    return list.elements();
  }

  static final void addToList(Vector list,int ip, String name) {
    list.add(new NetworkInterface(name, ip));
  }
  
  private static native void nativeGetInterfaces(Vector list);
  private static native int getAddressDevice(String device);
}
