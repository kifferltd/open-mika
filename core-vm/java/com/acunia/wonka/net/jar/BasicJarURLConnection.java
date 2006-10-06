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

package com.acunia.wonka.net.jar;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.jar.*;
import java.net.URL;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.security.Permission;

/**
** Basic implementation of JarURLConnection.
** a Jar url has the following structure 'jar:<url>[!/[entryname]] (where [abc...] means optional)
** the included url says where the jarfile is located (and how you could retrieve it (protocol to use));
** most common cases the url is a file- or http-url.  Since the JarFile class(the constructors) expects the jarfile to be on
** a local disk.  This implies that getJarFile() will throw an IOException.  Use getInputStream() instead to get a InputStream
** to retrieve the bytes of an Entry (or a the whole JarFile if no entry specified).
**
** the jar url contains a other url. This could be another JAR url allowing nested definitions. (this didn't work on blackdown though)!
*/
public class BasicJarURLConnection extends JarURLConnection {

  private static WeakHashMap jarFileCache = new WeakHashMap();

  private synchronized JarFile addJarToCache(String name) throws IOException {
    JarFile jar = getCachedJarFile(name);
    if(jar == null){
      jar = new JarFile(name);
      jarFileCache.put(jar,name);
    }
    return jar;
  }

  private synchronized JarFile getCachedJarFile(String name){
    Iterator it = jarFileCache.entrySet().iterator();
    while(it.hasNext()){
      Map.Entry entry = (Map.Entry) it.next();
      if(name.equals(entry.getValue())){
        return (JarFile)entry.getKey();
      }
    }
    return null;
  }

  private JarFile jar;
  private byte [] bytes;
  private ImmutableJarEntry je;
  private ImmutableManifest man;
  
  public BasicJarURLConnection(URL url) throws MalformedURLException {
    super(url);
  }

  public synchronized void connect() throws IOException {
    if (!connected) {
      URL jarurl = getJarFileURL();
      jarFileURLConnection = jarurl.openConnection();
      String name = getEntryName();
      if (jarurl.getProtocol().equals("file")) {
        String filename = jarurl.getFile();
        jar = getCachedJarFile(filename);
        if(jar == null){
          jar = addJarToCache(filename);
        }
        man = new ImmutableManifest(jar.getManifest());
      	if (name == null)
          je = null;
        else
        {
          JarEntry jeTemp = jar.getJarEntry(name);
          if (jeTemp == null)
            throw new FileNotFoundException(jarurl+"!/"+name+" does not exist");
          else
            je = new ImmutableJarEntry(jeTemp);
        }
      }
      else {
        JarInputStream jin = new JarInputStream(jarFileURLConnection.getInputStream(),true);
        je = new ImmutableJarEntry(jin.getNextJarEntry());
        if (name == null) {
          man = new ImmutableManifest(jin.getManifest());
          je = null;
        }
        else {
          while(je != null) {
            if (name.equals(je.getName())) {
              man = new ImmutableManifest(jin.getManifest());
              setBytes(jin);
              break;
            }
            je = new ImmutableJarEntry(jin.getNextJarEntry());
          }
          if (je == null) {
            throw new IOException("no entry found with name "+name+" in JAR archive "+jarurl);
          }
        }
      }
      connected = true;
     }  
  }    

  public Permission getPermission() throws IOException {
    if (!connected) {
      connect();
    }
    return jarFileURLConnection.getPermission();
  }

  public InputStream getInputStream() throws IOException {
    if (!connected) {
      connect();
    }
    if (jar != null){
      if (getEntryName() != null) {
        return  jar.getInputStream(je);
       }
       return new FileInputStream(getJarFileURL().getFile());
    }  
    if (getEntryName() != null) {
      return new ByteArrayInputStream(bytes);
    }
    return jarFileURLConnection.getInputStream();    
  }
  
  public JarFile getJarFile() throws IOException {
    if (!connected) {
      connect();
    }
    if(jar == null) {
      throw new IOException("jar file not on local disk");
    }
    return jar;
  }

  private void setBytes(InputStream jin) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    bytes = new byte[1024];
    int i = jin.read(bytes,0,1024);
    while(i != -1) {
      baos.write(bytes,0,i);
      i = jin.read(bytes,0,1024);
    }
    bytes = baos.toByteArray();
  }

//Inherited from JarURLConnection but overridden

  public JarEntry getJarEntry() throws IOException {
    if (!connected) {
      connect();
    }
    return je;

  }

  public Manifest getManifest() throws IOException {
    if (!connected) {
      connect();
    }
    return man;
  }
}
