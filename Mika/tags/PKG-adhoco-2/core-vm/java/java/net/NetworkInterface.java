/**
 * Copyright  (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.
 * All rights reserved.
 *
 * $Id: NetworkInterface.java,v 1.2 2006/10/04 14:24:15 cvsroot Exp $
 */
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
    addresses = new InetAddress[]{InetAddress.createInetAddress(ip)};
  }

  public String getName() {
    return name;
  }

  public Enumeration getInetAddresses() {
     return new AddrEnum(addresses); 
  }


  public String getDisplayName() {
    return displayName;
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
    System.out.println(name);
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
