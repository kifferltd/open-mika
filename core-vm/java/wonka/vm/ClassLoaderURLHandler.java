/**************************************************************************
* Copyright (c) 2022 by KIFFER Ltd. All rights reserved.                  *
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
* DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE       *
* GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS           *
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER    *
* IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR         *
* OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF  *
* ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                              *
**************************************************************************/

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

  public byte[] getByteArray(String resource) throws IOException {
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
