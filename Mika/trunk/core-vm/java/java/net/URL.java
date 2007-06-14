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
** $Id: URL.java,v 1.4 2006/10/04 14:24:15 cvsroot Exp $
*/

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
  ** static default access method to verify a NetPermission  ...
  ** should be called by other 'java.net' classes if needed.
  */
  static void netPermissionCheck(String type) {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      java.security.AccessController.checkPermission(new NetPermission(type));
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
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
        throw new MalformedURLException();
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
    this.port = (port == -1 ? streamHandler.getDefaultPort() : port);
    this.host = host;
    this.path = File;
  }

  public URL(URL context, String spec) throws MalformedURLException {
     this(context,spec, null);
  }

/**
** if spec is a relative url it should only contain a path (or path + fragment).
** The given context should be non-null in case of a relative url, otherwise null is allowed
** The specified handler can be null
*/
  public URL(URL context, String spec, URLStreamHandler handler) throws MalformedURLException {
    // we should find out if spec is a relative URL or not
    int hash = spec.lastIndexOf("#");
    int i = spec.indexOf(':', spec.indexOf(']') + 1);
    if(i > -1) { // we have a winner it is a non relative URL ...
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
    else { // we have a relative URL we copy protocol, host and port from ohter
        this.protocol = context.protocol;
        this.host = context.host;
        this.port = context.port;
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
    //System.out.println("Returning Q: " + query);
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
    /* setting the stream handler to null is not a good idea
    if (!protocol.equals(this.protocol)) {
          streamHandler = null;
    }
    */
    this.protocol = protocol;
    this.host = host;
    this.port = port;
    this.path = path;
    this.fragment = ref;
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
    /* setting the stream handler to null is not a good idea
    if (!protocol.equals(this.protocol)) {
          streamHandler = null;
    }
    */
    this.protocol = protocol;
    this.host = host;
    this.port = port;
    this.path = path;
    this.fragment = ref;
    this.authority = authority;
    this.query = query;
    this.userInfo = userInfo;
    //System.out.println("" + protocol + " " + host + " " + port + " " + authority + " " + userInfo + " " + path + " " + query + " " + ref);
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
