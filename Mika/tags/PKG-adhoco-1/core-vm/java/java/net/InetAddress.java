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
** $Id: InetAddress.java,v 1.6 2006/05/25 08:55:16 cvs Exp $ 
*/

/*
** 
** For a discussion of the positive address cache built into InetAddress, see
** <a href="http://www.limewire.org/pipermail/codepatch/2004-February/000310.html>
** http://www.limewire.org/pipermail/codepatch/2004-February/000310.html</a>.
** 
** Wonka's behaviour is as follows:
** <ul><li>If the <b>security</b> property <tt>inetaddress.cache.ttl</tt> 
**         is defined, we use this as the cache entry lifetime (in seconds).
**     <li>Failing that, if the <b>system</b> property
**         <tt>wonka.inetaddress.cache.ttl</tt> property is defined,
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
      return "IAddrCacheEntry with address " + addr + ", expiry time " + new java.util.Date(expiryTime).toString();
    }
  }

  /**
   ** Cache of positive results. Key is the hostname, value is an IAddrCacheEntry.
   */
  static final IAddrPositiveCache positive_cache = new IAddrPositiveCache();

  /**
   ** Lifetime of positive_cache entries, in milliseconds (0 means infinity).
   */
  static long positive_cache_ttl;

  static final InetAddress loopbackAddress  = new Inet4Address(0x7f000001,"localhost","127.0.0.1");
  static final InetAddress allZeroAddress  = new Inet4Address(0,"0.0.0.0","0.0.0.0");


  static {
    try {
      ownAddress = new Inet4Address(getLocalName());
    }
    catch (UnknownHostException uhe) {
      //just to make sure ownAddress will not be null ...
      ownAddress = loopbackAddress;
    }

    IAddrCacheEntry iace1= new IAddrCacheEntry(loopbackAddress, Long.MAX_VALUE);
    IAddrCacheEntry iace2= new IAddrCacheEntry(ownAddress, Long.MAX_VALUE);

    positive_cache.put("loopback", iace1);
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
                positive_cache.put(name, new IAddrCacheEntry(new Inet4Address(intaddr, name, addr), Long.MAX_VALUE));
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


  //dictated by serialized form ...
  int family;
  int address;
  String hostName;

  //since all InetAddresses get cached and the address is used as a key we might as well cache it.
  transient String addressCache;

  /**
  ** creates an empty InetAddress used by peek
  */
  InetAddress(){}

  InetAddress(int ip, String name, String ipname, int type){
    address = ip;
    family = type;
    hostName = name;
    addressCache = ipname;
    IAddrCacheEntry iace = new IAddrCacheEntry(this);
    synchronized(positive_cache){
      positive_cache.put(name, iace);
      positive_cache.put(ipname, iace);
    }
  }

  /**
  ** calling this constructor will add the address to the addressCache !
  */
  InetAddress(String name) throws UnknownHostException {
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
      family = TYPE_IPV4;
      synchronized(positive_cache){
        positive_cache.put(name,  new IAddrCacheEntry(this));
      }
    }
    else {
      createInetAddress(name);
      synchronized(positive_cache){
        positive_cache.put(this.getHostName(),  new IAddrCacheEntry(this));
        positive_cache.put(this.getHostAddress(),  new IAddrCacheEntry(this));
      }
    }
  }

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
      return new Inet4Address(host);
    }
  }

  /**
   * Returns IP address 'xxx.xxx.xxx.xxx'
   */
  public String getHostAddress() {
    if(addressCache == null){
      addressCache = intToIPString(address);
    }
    return addressCache;
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
        return new Inet4Address(ipname);
      } catch(UnknownHostException uhe){
        //this exception cannot be thrown on a 'real' IP-string, but to be sure ...
        return new Inet4Address(ip,ipname,ipname);
      }
    }
    return ia;
  }


  /**
   * Returns the hostname for this address
   */
  public String getHostName() {
    if(hostName == null){
      if(getHostAddress() != null && lookupName()) {
        hostName = addressCache;
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
    return getHostName() + "/" + getHostAddress();
  }
/*
  public boolean isIPv6 () {
    return family == TYPE_IPV6;
  }
*/

  /**
  ** should only be called when addressCache is not null
  */
  private native boolean lookupName();

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
