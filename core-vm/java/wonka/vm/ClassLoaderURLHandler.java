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
** $Id: ClassLoaderURLHandler.java,v 1.2 2006/05/16 08:24:41 cvs Exp $
*/

package wonka.vm;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.jar.Manifest;
import java.util.jar.JarFile;

public class ClassLoaderURLHandler {

  private static final Manifest emptyMan = new Manifest();

  private static boolean verbose = false;

  private static void debug(String s) {
    if (verbose) {
      System.err.println(s);
    }
  }

  public static ClassLoaderURLHandler[] createClassLoaderURLHandlers(URL[] urls){
    int length = urls.length;
    ClassLoaderURLHandler[] handlers = new ClassLoaderURLHandler[length];
    for(int i = 0 ; i < length ; i++){
      debug("Loading: URL [" + i + "] = " + urls[i]);
      handlers[i] = createClassLoaderURLHandler(urls[i]);
      debug("Loading: URL handler [" + i + "] = " + handlers[i]);
    }
    return handlers;
  }

  public static ClassLoaderURLHandler createClassLoaderURLHandler(URL url){
    try {
      String protocol = url.getProtocol();
      if(protocol.equals("file")){
        File f = new File(url.getFile());
        if(f.isDirectory()){
          return new CLFileHandler(f);
        }
        else if(f.isFile()){
          //is this an archive file !!!
          URL jUrl =  new URL("jar:"+url+"!/");
          return new CLJarHandler(new JarFile(f), jUrl);
        }
      }
      else if(protocol.equals("jar")){
        JarURLConnection jfc = (JarURLConnection)url.openConnection();
        if(jfc.getJarEntry() == null){
          return new CLJarHandler(jfc.getJarFile(), url);
        }
        else {
          return new CLUrlHandler(url);
        }
      }
      else if(protocol.equals("http")){
        return new CLHttpHandler(url);
      }
      else {
        return new CLUrlHandler(url);
      }
    }
    catch(Exception e){
      /* no valid url! */
      e.printStackTrace();
    }

    return new ClassLoaderURLHandler();
  }

  public byte[] getByteArray(String resource){
    return null;
  }

  public InputStream getInputStream(String resource){
    return null;
  }

  public URL getURL(String resource){
    return null;
  }

  public Manifest getManifest(){
    return emptyMan;
  }



}
