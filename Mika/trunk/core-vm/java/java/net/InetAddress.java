/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2007, 2009 by Chris Gray, /k/ Embedded Java         *
* Solutions.  All rights reserved.                                        *
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

/*
** Mika caches DNS lookup results as follows:
** <ul><li>If the <b>security</b> property <tt>inetaddress.cache.ttl</tt> 
**         is defined, we use this as the cache entry lifetime (in seconds).
**     <li>Failing that, if the <b>system</b> property
**         <tt>mika.inetaddress.cache.ttl</tt> property is defined,
**         we use this instead.
**     <li>Otherwise the cache entry lifetime defaults to 86400 seconds
**         (24 hours), in keeping with general DNS practice.
** </ul>
** Wonka does not implement a negative address cache.
*/

package java.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Hashtable;
import java.util.Properties;

public class InetAddress implements Serializable {

  private static final long serialVersionUID = 3286316764910316507L;
  static final int TYPE_LOOPBACK = 1;
  static final int TYPE_IPV4 = 2;
  static final int TYPE_IPV6 = 10;

  private static InetAddress ownAddress;

  /**
   ** Lifetime of positive_cache entries, in milliseconds (0 means infinity).
   */
  static int positive_cache_ttl = GetSecurityProperty.INETADDRESS_CACHE_TTL;

  /**
   ** A subclass of Hashtable used to implement the positive cache.
   */
  static class IAddrPositiveCache extends Hashtable {

    /**
     ** Convert the name (key) to lower case; don't overwrite an immortal
     ** entry with an otherwise identical mortal entry.
     */
    public synchronized Object put(String name, Object value) {
      String namelc = name.toLowerCase();
      IAddrCacheEntry newentry = (IAddrCacheEntry)value;
      InetAddress ia = newentry.addr;
      IAddrCacheEntry existing = (IAddrCacheEntry)super.get(namelc);
      if (existing == null || !existing.addr.equals(ia) || existing.expiryTime < newentry.expiryTime) {
        super.put(namelc, value);
      }

      return existing;
    }

    /**
     ** Convert the name to lower case, and if the value indexed has expired
     ** then remove it and return null.
     */
    public synchronized Object get(String name) {
      String namelc = name.toLowerCase();
      IAddrCacheEntry entry = (IAddrCacheEntry)super.get(namelc);

      if (entry == null) {

        return null;

      }

      if (entry.expiryTime < System.currentTimeMillis()) {
        super.remove(namelc);

        return null;
      }

      return entry;
    }
  }

  /**
   ** Inner class which represents an (InetAddress, long expiryTime) pair.
   */
  static class IAddrCacheEntry {
    InetAddress addr;
    long expiryTime;

    IAddrCacheEntry(InetAddress addr, long expiryTime) {
      this.addr = addr;
      this.expiryTime = expiryTime;
    }

    IAddrCacheEntry(InetAddress addr) {
      this(addr, System.currentTimeMillis() + positive_cache_ttl);
    }

    public String toString() {
      return "IAddrCacheEntry with address " + addr + ", expiry time " + new Date(expiryTime).toString();
    }
  }

  /**
   ** Cache of positive results. Key is the hostname, value is an IAddrCacheEntry.
   */
  static final IAddrPositiveCache positive_cache = new IAddrPositiveCache();

  static final InetAddress loopbackAddress  = new Inet4Address(0x7f000001,"localhost","127.0.0.1", 0);
  static final InetAddress allZeroAddress  = new Inet4Address(0,"0.0.0.0","0.0.0.0", 0);


  static {
    try {
      ownAddress = new Inet4Address(getLocalName(), 0);
    }
    catch (UnknownHostException uhe) {
      //just to make sure ownAddress will not be null ...
      ownAddress = loopbackAddress;
    }

    IAddrCacheEntry iace1= new IAddrCacheEntry(loopbackAddress, Long.MAX_VALUE);
    IAddrCacheEntry iace2= new IAddrCacheEntry(ownAddress, Long.MAX_VALUE);

    positive_cache.put("localhost", iace1);
    positive_cache.put("127.0.0.1", iace1);
    positive_cache.put(getLocalName(), iace2);
    positive_cache.put(ownAddress.getHostAddress(), iace2);

    Properties hostProperties = new Properties();
    try {
      InputStream hostpropstream = ClassLoader.getSystemResourceAsStream("mika.hosts");
      if (hostpropstream != null) {
        hostProperties.load(hostpropstream);
        Enumeration names = hostProperties.propertyNames();
	while (names.hasMoreElements()) {
          String name = (String)names.nextElement();
	  String addr = hostProperties.getProperty(name);
	  int intaddr = 0;
	  int firstpoint = addr.indexOf('.');
	  int secondpoint = -1;
	  int thirdpoint = -1;
	  if (firstpoint >= 0) {
            secondpoint = addr.indexOf('.', firstpoint + 1);
	  }
	  if (secondpoint >= 0) {
            thirdpoint = addr.indexOf('.', secondpoint + 1);
	  }
	  if (firstpoint >= 0 && secondpoint >= 0 && thirdpoint >= 0) {
            try {
              int octet1 = Integer.parseInt(addr.substring(0, firstpoint));
              int octet2 = Integer.parseInt(addr.substring(firstpoint + 1, secondpoint));
              int octet3 = Integer.parseInt(addr.substring(secondpoint + 1, thirdpoint));
              int octet4 = Integer.parseInt(addr.substring(thirdpoint + 1));
	      if (octet1 >= 0 && octet1 < 256 && octet2 >= 0 && octet2 < 256 && octet3 >= 0 && octet3 < 256 && octet4 >= 0 && octet4 < 256) {
	        intaddr = ((octet1 * 256 + octet2) * 256 + octet3) * 256 + octet4;
                positive_cache.put(name, new IAddrCacheEntry(new Inet4Address(intaddr, name, addr, 0), Long.MAX_VALUE));
	      }
	      else {
                System.err.println("Error in mika.hosts: octet out of range in ip address: " + addr);
	      }
	    }
	    catch (NumberFormatException nfe) {
              System.err.println("Error in mika.hosts: bad digit in ip address: " + addr);
	    }
	  }
	  else {
            System.err.println("Error in mika.hosts: missing '.' in ip address: " + addr);
	  }
	}
      }
    }
    catch (IOException e) {}
  }

/**
** InetAddress hosts static permission checks to avoid code duplication.
** Since all Socket classes use InetAddress this does NOT unneeded class loading ...
*/
  static void permissionCheck(String host) {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      java.security.AccessController.checkPermission(new SocketPermission(host, "resolve"));
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkConnect(host, -1);
      }
    }
  }

  static void factoryCheck() {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      java.security.AccessController.checkPermission(new RuntimePermission("setFactory"));
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkSetFactory();
      }
    }
  }

  static void listenCheck(int port) {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      if (port == 0) {
        java.security.AccessController.checkPermission(new SocketPermission("localhost:1024-","listen"));
      }
      else {
        java.security.AccessController.checkPermission(new SocketPermission("localhost:"+port,"listen"));
      }
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkListen(port);
      }
    }
  }

  static void acceptCheck(String host, int port) {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      java.security.AccessController.checkPermission(new SocketPermission(host+":"+port,"accept"));
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkAccept(host, port);
      }
    }
  }

  static void connectCheck(String host, int port) {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      if (port == -1) {
        java.security.AccessController.checkPermission(new SocketPermission(host,"resolve"));
      }
      else {
        java.security.AccessController.checkPermission(new SocketPermission(host+":"+port,"connect"));
      }
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkConnect(host, port);
      }
    }
  }

  /**
   ** Address family, as dictated by serialized form.
   */
  int family;

  /**
   ** IP address in integer form, as dictated by serialized form.
   */
  int address;

  /**
   ** Host name, as dictated by serialized form.
   */
  String hostName;

  /**
   ** Dotted-string form of 'address'.
   */
  transient String ipAddressString;

  /**
  ** Creates an empty InetAddress used by peek. For all other purposes
  ** a constructor of Inet4Address or Inet6Address is used.
  */
  InetAddress(){}

  /**
   * Compares this object against the specified object
   */
  public boolean equals(Object obj) {
    if(!(obj instanceof InetAddress)){
      return false;
    }
    InetAddress other = (InetAddress)obj;
    return (this.address == other.address);
  }

  /**
   * Returns the raw IP address of this InetAddress object
   */
  public byte[] getAddress() {
   	byte[] octets = new byte[4];
   	octets[3] = (byte)address;
   	octets[2] = (byte)(address>>>8);
   	octets[1] = (byte)(address>>>16);
   	octets[0] = (byte)(address>>>24);
    return octets;
  }

  /**
   * Determines all the hosts IP addresses, given the hostname;
   */
  public static InetAddress[] getAllByName (String host) throws UnknownHostException
  {
    if (host==null || host.length()==0) {
      throw new UnknownHostException();
    }

    //the check is done in getByName
    //permissionCheck(host);

    // FOR NOW, lets do the same thing as for getByName
    InetAddress[] temp = new InetAddress[1];
    temp[0] = getByName(host);
    return temp;
  }

  /**
   * Determine the IP address, given the hostname
   */
  public static InetAddress getByName(String host) throws UnknownHostException {

    if (host==null || host.length() == 0){
      return loopbackAddress;
    }

    try {
      permissionCheck(host);
    }
    catch (IllegalArgumentException iae) {
      throw new UnknownHostException(host);
    }

    return getByNameImpl(host);
  }

  static InetAddress getByNameImpl(String host) throws UnknownHostException {
    synchronized(positive_cache) {
      String hostlc = host.toLowerCase();
      IAddrCacheEntry cached = (IAddrCacheEntry)positive_cache.get(hostlc);
      if(cached != null){

        return cached.addr;

      }
    }

    if(host.indexOf(':') != -1) {
      return new Inet6Address(host);
    }
    else {
      return new Inet4Address(host, positive_cache_ttl);
    }
  }

  /**
   * Returns IP address 'xxx.xxx.xxx.xxx'
   */
  public String getHostAddress() {
    if(ipAddressString == null){
      ipAddressString = intToIPString(address);
    }
    return ipAddressString;
  }

  private static String intToIPString(int address){
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

  static InetAddress createInetAddress(int ip){
    String ipname = intToIPString(ip);
    InetAddress ia = null;

    synchronized (positive_cache) {
      Object cached = positive_cache.get(ipname);
      if(cached != null){
        ia = ((IAddrCacheEntry)cached).addr;
      }
    }

    if(ia == null){
      try {
        return new Inet4Address(ipname, positive_cache_ttl);
      } catch(UnknownHostException uhe){
        //this exception cannot be thrown on a 'real' IP-string, but to be sure ...
        return new Inet4Address(ip,ipname,ipname, 0);
      }
    }
    return ia;
  }


  /**
   * Returns the hostname for this address
   */
  public String getHostName() {
    if(hostName == null){
      if(getHostAddress() != null && lookupName(this)) {
        hostName = ipAddressString;
      }
      else {
        positive_cache.put(hostName, new IAddrCacheEntry(this));
      }
    }
    permissionCheck(hostName);
    return hostName;
  }

  /**
   * Returns the localhost
   */
  public static InetAddress getLocalHost() 
    throws UnknownHostException 
  {
    try {
      permissionCheck(getLocalName());
      return ownAddress;
    }
    catch (SecurityException se) {
      return loopbackAddress;
    }
  }

  /**
   * returns the hashcode for this IP address
   */
  public int hashCode() {
    return address;
  }

  /**
   * Checks if the InetAddress is a multicast address
   * IP Multicast Addresses (Class D) range from 224.0.0.0 to 239.255.255.255
   */ 
  public boolean isMulticastAddress () {
    byte[] ip = getAddress();
    return (((ip[0]&0xFF) >223) && ((ip[0]&0xFF)<240));
  }

  /**
   * InetAddress -> String conversion
   */
  public String toString () {
    String name = "";

    if (hostName != null) {
      name = hostName;
    }
    else if (ipAddressString != null && !ipAddressString.equals(intToIPString(address))) {
      name = ipAddressString;
    }

    return name + "/" + getHostAddress();
  }
/*
  public boolean isIPv6 () {
    return family == TYPE_IPV6;
  }
*/

  /**
  ** should only be called when ipAddressString is not null
  */
  private static synchronized native boolean lookupName(InetAddress ia);

  /**
  ** will find an IP address for the name
  */
  native void createInetAddress(String hostname) throws UnknownHostException;

  /**
  ** will find the name of this host ...
  */
  private static native String getLocalName();

  public static InetAddress getByAddress(byte[] addr) throws UnknownHostException{
    return null;
  }

  public static InetAddress getByAddress(String host, byte[] addr) throws UnknownHostException {
    return null;
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
