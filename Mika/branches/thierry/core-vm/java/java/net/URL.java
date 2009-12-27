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

package java.net;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

public final class URL implements java.io.Serializable {

  private static final long serialVersionUID = -7627629688361524110L;

  private static final char OR='|';
  
  private static final Object lock = new Object();
  /**
   ** The factory for URL stream handlers, if one has been set
   ** (using setURLStreamHandlerFactory).
   */
  private static URLStreamHandlerFactory factory;

  /**
   ** Our cache of URL stream handlers, indexed by protocol.
   */
  private static Hashtable handlers = new Hashtable(11);

  /**
   ** Package access so other classes can also see it.
   */
  static boolean verbose = (System.getProperty("mika.verbose", "").indexOf("url") >= 0);

  /**
   ** The protocol, i.e. the part before the first colon (:).
   */
  private String protocol;

  /**
   ** The fragment, i.e. the part after the last hash sign (#).
   */
  private String fragment;

  /**
   ** The host machine, if any (set by the URL stream handler).
   */
  private String host = "";

  /**
   ** The port to be used, or -1 for the default (set by the URL stream handler).
   */
  private int port = -1;

  /**
   ** The query string, if any (set by the URL stream handler).
   */
  private String query;

  /**
   ** The remainder, which may or not be the path to a file (set by the URL stream handler).
   */
  private String path = "";


  private String authority;
  private int hashCode;

  private transient String userInfo;
  private transient URLStreamHandler streamHandler;

  /**
   ** Implement verbosity
   */
  static void debug(String s) {
    if (verbose) {
      System.err.println(s);
    }
  }

  /**
  ** static default access method to verify a NetPermission  ...
  ** should be called by other 'java.net' classes if needed.
  */
  static void netPermissionCheck(String type) {
    if (wonka.vm.SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkPermission(new NetPermission(type));
      }
    }
  }

  /**
   ** Constructor to build a URL from scratch, given its external form.
   ** We split the URL into its three most basic components: the protocol,
   ** the fragment indicator (if present), and "the rest".  We then use the
   ** protocol to identify a URLStreamHandler, and invoke that handler's  
   ** parseURL method to process the "rest" part of the URL.
   **
   ** @param	location	A string reresenting a URL.
   */
  public URL (String location) throws MalformedURLException {
      int colon = location.indexOf(":");
      if (colon < 0) {
        throw new MalformedURLException("missing colon in '" + location + "'");
      }
      int hash = location.lastIndexOf("#");
      if (hash < 0) {
        hash = location.length();
      }
      else {
        fragment = location.substring(hash + 1);
      }

      this.protocol = location.substring(0, colon);
      streamHandler = getHandler(this.protocol);
      streamHandler.parseURL(this, location, colon+1, hash);
      debug("URL: new URL " + this);
  }

  public URL(String protocol, String host, String path) throws MalformedURLException {
    this(protocol,host,-1,path,null);
  }

  public URL(String protocol, String host, int port, String File) throws MalformedURLException {
    this(protocol,host,port,File,null);
  }

  public URL(String protocol, String host, int port, String File, URLStreamHandler handler) throws MalformedURLException {
    if (protocol == null || File == null) {
       throw new NullPointerException();
    }

    if (host == null) {
      host = "";
    }

    if (port < -1 || port > 65535) {
       throw new MalformedURLException("illegal port specified");
    }
    this.protocol = protocol;
    if (handler == null) {
      streamHandler = getHandler(this.protocol);
    }
    else {
       netPermissionCheck("specifyStreamHandler");
       streamHandler = handler;
    }
    this.port = port;
    this.host = host;
    this.path = File;
    debug("URL: new URL " + this);
  }

  public URL(URL context, String spec) throws MalformedURLException {
     this(context,spec, null);
  }

/**
** if spec is a relative url it should only contain a path (or path + fragment).
** The given context should be non-null in case of a relative url, otherwise null is allowed
** The specified handler can be null, as can the context.
*/
  public URL(URL context, String spec, URLStreamHandler handler) throws MalformedURLException {
    // we should find out if spec is a relative URL or not
    int hash = spec.lastIndexOf("#");
    int i = spec.indexOf(':');
    int j = spec.indexOf('/');

    // RFC 1630 allowed relative URLs to begin with the scheme name if this
    // was the same as for the base URL. Work around this.
    if (context != null && i == context.protocol.length() && spec.startsWith(context.protocol) && j != i + 1) {
      spec = spec.substring(i + 1);
      j -= i + 1;
      i = -1;
    }

    if (i > 0 && (j < 0 || i < j)) { // absolute URL
        protocol = spec.substring(0,i);
      if (handler == null) {
        streamHandler = getHandler(this.protocol);
      }
      else {
         netPermissionCheck("specifyStreamHandler");
         streamHandler = handler;
      }
      streamHandler.parseURL(this,spec, i+1, hash >= i ? hash : spec.length());

      if (host == null) {
        host = "";
      }

      if (path == null) {
         throw new MalformedURLException();
      }

      if (hash != -1) {
        fragment = spec.substring(hash+1);
      }
    }
    else { // relative URL 
      if (spec.startsWith("//")) {
        this.protocol = context.protocol;
      }
      else {
        this.protocol = context.protocol;
        this.userInfo = context.userInfo;
        this.authority = context.authority;
        this.host = context.host;
        this.port = context.port;
      }
        if (handler == null) {
          streamHandler = getHandler(this.protocol);
      }
      else {
         netPermissionCheck("specifyStreamHandler");
         streamHandler = handler;
      }

      if ( hash != -1) {
         path = context.path + spec.substring(0,hash);
         fragment = spec.substring(hash+1);
      }
      else {
         path = context.path + spec;
      }
    }      
  }

  public boolean equals(Object o) {
    if (!(o instanceof URL)){
      return false;
    }
    return streamHandler.equals(this,(URL)o);
  }

  public final Object getContent()throws IOException {
    return openConnection().getContent();
  }

  public final Object getContent(Class[] classes)throws IOException {
    return openConnection().getContent(classes);
  }

  public String getAuthority(){
    return this.authority;
  }

  public String getProtocol() {
    return this.protocol;
  }

  public String getHost() {
    return this.host;
  }

  public String getPath() {
    return this.path;
  }

  public int getPort() {
    return this.port;
  }

  public String getFile() {
    return this.path + (query != null ? "?" + query : "");
  }

  public String getRef() {
    return this.fragment;
  }

  public String getQuery() {
    return this.query;
  }

  public String getUserInfo(){
    return this.userInfo;
  }

  public int hashCode() {
    if (hashCode == 0) {
      hashCode = streamHandler.hashCode(this);
    }
    return hashCode;
  }

  public static void setURLStreamHandlerFactory(URLStreamHandlerFactory newfact) {
     synchronized(lock) {
       factory = newfact;
       debug("URL: set URLStreamHandlerFactory to " + newfact);
     }
  }

  public URLConnection openConnection() throws IOException {
    return streamHandler.openConnection(this);
  }

  public final InputStream openStream() throws IOException {
    return openConnection().getInputStream();
  }

  public boolean sameFile(URL url) {
     if(url == null){
       return false;
     }
     return streamHandler.sameFile(this, url);
  }

  public String toExternalForm() {
    return streamHandler.toExternalForm(this);
  }

  public String toString () {
     return streamHandler.toExternalForm(this);
  }

  protected void set(String protocol, String host, int port, String path, String ref){
    if (path == null) {
       throw new NullPointerException();
    }
    if (host == null) {
      host = "";
    }
    if (port <-1 || port > 65535) {
       throw new IllegalArgumentException();
    }
    this.protocol = protocol;
    this.host = host;
    this.port = port;
    this.path = path;
    this.fragment = ref;
    debug("URL: set " + this + " to protocol '" + protocol + "', host '" + host + "', port '" + port + "', path '" + path + "', fragment '" + ref + "'");
  }

  protected void set(String protocol, String host, int port, String authority, String userInfo, String path, String query, String ref){
    if (path == null) {
       throw new NullPointerException();
    }
    if (host == null) {
      host = "";
    }
    if (port <-1 || port > 65535) {
       throw new IllegalArgumentException();
    }
    this.protocol = protocol;
    this.host = host;
    this.port = port;
    this.path = path;
    this.fragment = ref;
    this.authority = authority;
    this.query = query;
    this.userInfo = userInfo;
    debug("URL: set " + this + " to protocol '" + protocol + "', host '" + host + "', port '" + port + "', authority '" + authority + "', user info '" + userInfo + "', path '" + path + "', query '" + query + "', fragment '" + ref + "'");
  }

// package protected methods to update an URL.
  void setHost(String host) {
    if (host == null) {
      host = "";;
    }
    hashCode=0;
    this.host = host;
  }

  void setProtocol(String protocol) {
    if (!protocol.equals(this.protocol)) {
          streamHandler = null;
      hashCode=0;
      this.protocol = protocol;
    }
  }

  void setUserInfo(String userinfo) {
    hashCode=0;
    this.userInfo = userinfo;
  }

  void setAuthority(String auth) {
    hashCode=0;
    this.authority = auth;
  }

  void setFile(String path) {
    if (path == null) {
       throw new NullPointerException();
    }
    hashCode=0;
    this.path = path;
  }

  void setRef(String ref) {
    this.fragment = ref;
  }

  void setQuery(String query) {
    this.query = query;
    //System.out.println("Q: " + query);
  }

  void setPort(int port) {
    if (port <-1 || port > 65535) {
       throw new IllegalArgumentException();
    }
    this.port = port;
  }

  public int getDefaultPort() {
    return  this.streamHandler.getDefaultPort();
  }
  
  /**
   ** Find the URLStreamHandler which is associated with a given protocol.
   ** We cache the ones we already found in a Hashtable, so we never try
   ** to create the same URLStreamHandler twice.
   */
  private static URLStreamHandler getHandler(String protocol) throws MalformedURLException {
    URLStreamHandler handler = null;
    handler = (URLStreamHandler)handlers.get(protocol);    

    if (handler != null) {
      return handler;
    }

    
    synchronized(lock) {
    
      if (factory != null) {
        handler = factory.createURLStreamHandler(protocol);
        if (handler != null) {
          handlers.put(protocol, handler);
          debug("URL: created handler " + handler + " for protocol '" + protocol + "' using " + factory);
          return handler;
       }
      }

      String pkgs = GetSystemProperty.PROTOCOL_HANDLER_PKGS;
      String name;
      while (pkgs.length() > 0 ) {
        int i = pkgs.indexOf(OR);
        if(i == -1) {
          name = pkgs;
          pkgs = "";
        }
        else {
          name = pkgs.substring(0,i);
          pkgs = pkgs.substring(i+1);
        }
        name = name+"."+protocol+".Handler";
        try {
          handler = (URLStreamHandler)Class.forName(name, true , ClassLoader.getSystemClassLoader()).newInstance();
          handlers.put(protocol, handler);
          debug("URL: created handler " + handler + " for protocol '" + protocol + "'");
          return handler;
        }
        catch(Exception e){
          /*if we catch an Exception here we should continue looking ...*/
        }        
      }
    }  
    throw new MalformedURLException("no Handler found for the "+protocol+" protocol");    
  }
}
