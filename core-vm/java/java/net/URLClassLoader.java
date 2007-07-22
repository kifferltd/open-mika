/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

package java.net;

import java.security.SecureClassLoader;
import java.security.PermissionCollection;
import java.security.CodeSource;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;
import wonka.vm.ClassLoaderURLHandler;

/**
 *
 * $Id: URLClassLoader.java,v 1.2 2006/05/16 08:30:38 cvs Exp $
 *
 */
public class URLClassLoader extends SecureClassLoader{

  URL [] urls;
  ClassLoaderURLHandler[] handlers;

  private static boolean verbose = true;

  private static void debug(String s) {
    if (verbose) {
      System.err.println(s);
    }
  }

  private static final Manifest emptyMan = new Manifest();

  private static void permissionCheck() {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
    // Note: we assume that java.net is a restricted package.
      java.security.AccessController.checkPermission(new RuntimePermission("accessClassInPackage.java.net"));
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkPackageAccess("java.net");
      }
    }
  }

  public URLClassLoader(URL[] urls) throws SecurityException {
    debug("Loading: create URL handlers for " + this);
    handlers = ClassLoaderURLHandler.createClassLoaderURLHandlers(urls);
    this.urls = urls;
  }

  public URLClassLoader(URL[] urls, ClassLoader parent) throws SecurityException {
    super(parent);
    debug("Loading: create URL handlers for " + this);
    handlers = ClassLoaderURLHandler.createClassLoaderURLHandlers(urls);
    this.urls = urls;
  }

  public URLClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) throws SecurityException {
    super(parent);
    debug("Loading: create URL handlers for " + this);
    handlers = ClassLoaderURLHandler.createClassLoaderURLHandlers(urls);
    this.urls = urls;
  }

  public static URLClassLoader newInstance(final URL[] urls) throws SecurityException{
    permissionCheck();
    //TODO: do as a privileged action
    return new URLClassLoader(urls);
  }

  public static URLClassLoader newInstance(final URL[] urls, ClassLoader parent) throws SecurityException{
    permissionCheck();
    //TODO: do as a privileged action
    return new URLClassLoader(urls, parent);
  }

  protected void addURL(URL url){
    debug("Loading: add URL handler to " + this);
    ClassLoaderURLHandler handler = ClassLoaderURLHandler.createClassLoaderURLHandler(url);
    debug("Loading: new URL handler = " + handler);
    int length = handlers.length;
    ClassLoaderURLHandler[] newhandlers = new ClassLoaderURLHandler[length+1];
    System.arraycopy(handlers, 0, newhandlers, 0, length);
    newhandlers[length] = handler;
    handlers = newhandlers;
    URL[] newurls = new URL[urls.length+1];
    System.arraycopy(urls, 0, newurls, 0, urls.length);
    newurls[urls.length] = url;
    urls = newurls;
  }

  protected Package definePackage(String pname, Manifest man, URL url) throws IllegalArgumentException {
    Attributes attr = man.getMainAttributes();
    String specTitle = attr.getValue("Specification-Title");
    String specVersion  = attr.getValue("Specification-Version");
    String specVendor = attr.getValue("Specification-Vendor");
    String implTitle  = attr.getValue("Implementation-Title");
    String implVersion  = attr.getValue("Implementation-Version");
    String implVendor   = attr.getValue("Implementation-Vendor");
    return definePackage(pname, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, url);
  }

  protected Class findClass(String name) throws ClassNotFoundException {
    String filename = name.replace('.','/') + ".class";
    int l = handlers.length;
    for(int i = 0 ; i < l ; i++){
      ClassLoaderURLHandler handler = handlers[i];
      byte[] bytes = handler.getByteArray(filename);
      if(bytes != null){
        URL url = urls[i];
        int idx = name.lastIndexOf('.');
        String pname = name.substring(0,idx == -1 ? 0 : idx);

        // TODO: get manifest right
        if (getPackage(pname) == null){
          definePackage(pname, handler.getManifest() , url);
        }
        if(url.getProtocol().equals("jar")) {
          String u = ("" + url).substring(4);
          u = u.substring(0, u.indexOf('!'));
          try {
            url = new URL(u);
          }
          catch(Exception e) {
          }
        }
        return defineClass(name, bytes, 0, bytes.length, new CodeSource(url, null));
      }
    }
    throw new ClassNotFoundException(name);
  }

  public URL findResource(String name){
    int l = handlers.length;
    for(int i = 0 ; i < l ; i++){
      ClassLoaderURLHandler handler = handlers[i];

      URL url = handler.getURL(name);
      if(url != null){
        return url;
      }
    }
    return null;
  }

  public Enumeration findResources(String name) throws IOException{
  // TODO; wasn't there something about leading slash?
    int l = handlers.length;
    Vector v = new Vector(l);
    for(int i = 0 ; i < l ; i++){
      ClassLoaderURLHandler handler = handlers[i];

      URL url = handler.getURL(name);
      if(url != null){
        v.add(url);
      }
    }
    return v.elements();
  }

  protected PermissionCollection getPermissions(CodeSource codes){
        PermissionCollection pc = super.getPermissions(codes);
        //ToDo add extra permissions to allow usage of the URL's
        return pc;
  }

/**
** returns a clone of the original URL array (for securiry reasons).
*/
  public URL[] getURLs(){
    return (URL[])urls.clone();
  }

}

