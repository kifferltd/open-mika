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
** $Id: SocketPermission.java,v 1.2 2005/09/27 07:39:19 cvs Exp $
*/

package java.net;
import java.security.Permission;


public final class SocketPermission extends Permission implements java.io.Serializable {

  private static final long serialVersionUID = -7204263841984476862L;

  private static final String DNS_CHARS = "abcdefghijklmnopqrstuvwxyz-0123456789";
  private static final int ACCEPT = 1;
  private static final int CONNECT = 2;
  private static final int LISTEN = 4;
  private static final int RESOLVE = 8;

  private String action;
  private String host_suffix;
  private boolean has_wild_prefix;
  private boolean numeric;
  private int lo_port;
  private int hi_port;
  private int bitmap;

  public SocketPermission(String host, String action){
    super("".equals(host) ? "localhost":host);
    if ("".equals(host)) {
     	host="localhost";
    }
    int colon = host.indexOf(':');
    if (colon < 0) {
      parseHostName(host);
      lo_port = 0;
      hi_port = 65535;
    }
    else {
      parseHostName(host.substring(0,colon));
      String ranges = host.substring(colon+1);
      int hyphen = ranges.indexOf('-');
      if (hyphen < 0) {
        lo_port = Integer.parseInt(ranges);
        hi_port = lo_port;
      }
      else {
        String s = ranges.substring(0,hyphen);
        lo_port = ("".equals(s) ? 0 : Integer.parseInt(s));
        s = ranges.substring(hyphen+1);
        hi_port = ("".equals(s) ? 65535 : Integer.parseInt(s));
      }
    }
    if (hi_port < lo_port || lo_port < 0 || hi_port > 65535) {
     	throw new IllegalArgumentException("bad port Numbers specified");
    }
    parseActions(action);
  }

  /**
   ** Analyse a hostname.  The name may be either a numeric IP address,
   ** a DNS-compliant hostname, or ``*.'' followed by a valid suffix
   ** of a hostname.
   */
  private void parseHostName(String name) throws IllegalArgumentException {
    int     dotcount;
    int     octet = 0;

    if (name.equals("")) {
     	throw new IllegalArgumentException("bad hostname specified");
    }

    if (name.charAt(0) == '*') {
      if (name.length() > 3 && (name.charAt(1) != '.' || Character.isDigit(name.charAt(2)))) {
        throw new IllegalArgumentException("Attempt to use wildcard in numeric IP address");
      }
      int start = (name.length() > 1 ? 2 : 1);
      host_suffix = name.substring(start).toLowerCase();
      has_wild_prefix = true;
      numeric = false;
    }
    else {
      host_suffix = name.toLowerCase();
      has_wild_prefix = false;
      numeric = Character.isDigit(name.charAt(0));
    }
    // Sanity check ... numeric version only covers IPv4!
    dotcount = 0;
    for (int i = 0; i < host_suffix.length(); ++i) {
      char c = host_suffix.charAt(i);
      if (c == '.') {
        if (numeric) {
          if (++dotcount > 3) {
            throw new IllegalArgumentException("Too many dots in hostname");
          }
          octet = 0;
        }
      }
      else if (numeric) {
        if (!Character.isDigit(c)) {
          throw new IllegalArgumentException("Non-digit '"+c+"'in numeric IP address");
        }
        octet = octet * 10 + Character.digit(c,10);
        if (octet > 255 || octet < 0) {
          throw new IllegalArgumentException("Illegal octet '"+octet+"'in numeric IP address");
        }
      }
      else if (DNS_CHARS.indexOf(c) < 0) {
        throw new IllegalArgumentException("Bad character '"+c+"'in hostname");
      }
    }
  }

  /**
   ** Analyse the action.  Result is a bitmap.
   */
  private void parseActions(String s) {
    String sx = s.toLowerCase();
    while (sx != "") {
      int i = sx.indexOf(',');
      String s0;

      if (i<0) {
        s0 = sx.trim();
        sx = "";
      }
      else {
        s0 = sx.substring(0,i).trim();
        sx = sx.substring(i+1);
      }

      if (s0.equals("accept")) {
        bitmap |= ACCEPT | RESOLVE;
      }
      else if (s0.equals("connect")) {
        bitmap |= CONNECT | RESOLVE;
      }
      else if (s0.equals("listen")) {
        bitmap |= LISTEN | RESOLVE;
      }
      else if (s0.equals("resolve")) {
        bitmap |= RESOLVE;
      }
      else {
     	throw new IllegalArgumentException("bad action specified -->"+s0);
      }
    }
    if (bitmap == 0) {
     	throw new IllegalArgumentException("no actions specified");
    }
    StringBuffer buf = new StringBuffer();
    if ((bitmap & CONNECT) > 0) {
    	buf.append("connect,");
    }
    if ((bitmap & LISTEN) > 0) {
	    buf.append("listen,");
    }
    if ((bitmap & ACCEPT) > 0) {
	    buf.append("accept,");
    }
    if ((bitmap & RESOLVE) > 0) {
	    buf.append("resolve");
    }
    action = buf.toString();

  }

  public boolean equals (Object obj) {
    if (!( obj instanceof SocketPermission)){
      return false;
    }
    SocketPermission p = (SocketPermission) obj;
    if(!this.has_wild_prefix){
      try {
        return !p.has_wild_prefix
            && this.lo_port == p.lo_port
            && this.hi_port == p.hi_port
            && this.action.equals(p.action)
            && InetAddress.getByNameImpl(host_suffix).equals(InetAddress.getByNameImpl(p.host_suffix));
      }
      catch(UnknownHostException ukhe){
      }
    }
    return this.getName().equals(p.getName())
        && this.action.equals(p.action);
  }

  public int hashCode() {
    if(!has_wild_prefix){
      try {
        return InetAddress.getByNameImpl(host_suffix).hashCode() ^ lo_port ^ (hi_port<<16);
      }
      catch(UnknownHostException ukhe){
      }
    }
    return this.getName().hashCode();// ^ action.hashCode();

  }

  public String getActions() {
    return action;
  }

  public boolean implies (Permission p) {
    if (!(p instanceof SocketPermission)){
     	return false;
    }
    SocketPermission that = (SocketPermission)p;
    return
      (that.bitmap & (~this.bitmap)) == 0
      &&
      that.lo_port >= this.lo_port
      &&
      that.hi_port <= this.hi_port
      && nameImplies(this,that)
    ;
  }

  /**
   ** This is where we check the implications of a hostname.
   ** This stuff is tricky and security-sensitive, so read very carefully ...
   **	 one of this four cases should be true ...
   **	 1. this permission is initialized with a numeric IP adress and one of p's adresses is equal to it
   **	 2. this object is *.wathever and p is something.whatever
   **  3. if this objects hostname IP adresses equals one of p's IP adresses
   **	 4. if this canonical name equals p's canonical name
   **
   */
  private boolean nameImplies(SocketPermission a, SocketPermission b) {
    String b_as_numeric = b.host_suffix;
    String b_as_hostname = b.host_suffix;

    // Quick checks to eliminate the simplest cases:
    // -1- If both are non-wild and equal, fine.    (checks for case 4) ...
    if (!a.has_wild_prefix && !b.has_wild_prefix && a.host_suffix.equals(b.host_suffix)) {

      return true;

    } 
    // -2- If both are wild, a must be a suffix of b.
    if (a.has_wild_prefix && b.has_wild_prefix) {

      return b.host_suffix.endsWith(a.host_suffix);

    } 

    // -3- A non-wild name can never imply a wild one.
    if (!a.has_wild_prefix && b.has_wild_prefix) {

      return false;

    }

    // O.K., now for the serious stuff.
    // First we canonicalize b.  If reverse DNS lookup fails, the game is up.
    try {
      if (b.numeric) {
        b_as_hostname = InetAddress.getByNameImpl(b.host_suffix).getHostName();
      }
      else if (!b.has_wild_prefix) {
        b_as_numeric = InetAddress.getByNameImpl(b.host_suffix).getHostAddress();
        b_as_hostname = InetAddress.getByNameImpl(b.host_suffix).getHostName();
        if (b_as_hostname != b.host_suffix) {

          return false;

        }
      }
    }
    catch (UnknownHostException e) {

      return false;

    }

    // What if both are non-wild, but the names are not identical?
    // Then a must be a hostname, and either:
    // -4a- b is numeric and is a possible translation of a, or
    // -4b- b is a hostname and its IP address is a possible translation of a.
    if (!a.has_wild_prefix && !b.has_wild_prefix) {
      if (a.numeric) {

        return false;

      }

      InetAddress[] translations;

      try {
        translations = InetAddress.getAllByName(a.host_suffix);
      }
      catch (UnknownHostException e) {

        return false;

      }

      for (int i = 0; i < translations.length; ++i) {
        if (translations[i].getHostAddress().equals(b_as_numeric)) {

          return true;

        }
      }

      return false;

    }

    // So a is wild and b is not.
    // -5- The canonical hostname of b must match in the obvious way.
    return b_as_hostname.endsWith(a.host_suffix);
  }

  public java.security.PermissionCollection newPermissionCollection() {
    return new com.acunia.wonka.security.SocketPermissionCollection();
  }

}
