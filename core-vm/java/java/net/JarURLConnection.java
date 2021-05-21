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

import wonka.net.jar.ImmutableJarEntry;
import wonka.net.jar.ImmutableManifest;

import java.io.IOException;
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
** Base implementation for Jar URL.
**
** Look in wonka.net.jar for more informartion an actual implementation
*/
public abstract class JarURLConnection extends URLConnection {

  /**
   ** The connection to the jarfile URL, if is has been established.
   */
  protected URLConnection jarFileURLConnection;

  /**
   ** The name of the underlying jarfile (portion before `!/').
   */
  private String file_name;

  /**
   ** The URL of the underlying jarfile (portion before `!/').
   */
  private URL file_url;

  /**
   ** The name of the file within the jar (portion after `!/'), if present
   ** (else null).
   */
  private String entry_name;

  /**
   ** Constructor to be used by subclasses to initialize this JarURLConnection.
   ** jarFileURLConnection field remains untouched.  A subclass could open a 
   ** connection to the 'jarfile' by asking an url with getJarFileURL to 
   ** construct a URLConnection ...
   */
  protected JarURLConnection (URL url) throws MalformedURLException {
    super(url);
    String temp = url.getFile();
    int slashbang = temp.lastIndexOf("!/");
    if (slashbang < 0) {
      entry_name = null;
      slashbang = temp.length() - 2;
    }
    else if (slashbang == temp.length() - 2) {
      entry_name = null;
    }
    else {
      entry_name = temp.substring(slashbang + 2);
    }
    file_name = temp.substring(0, slashbang);
    file_url = new URL(file_name);           
  }

  public Attributes getAttributes() throws IOException {
     JarEntry je = getJarEntry();
     if (je == null){
        return null;
     }
     return  je.getAttributes();

  }

  public Certificate[] getCertificates() throws IOException {
     JarEntry je = getJarEntry();
     if (je == null){
        return null;
     }
     return je.getCertificates();
  }

  /**
   ** Return the name of the entry within the JarFile (or null if none was
   ** specified).
   */
  public String getEntryName() {
    return entry_name;  
  }

  /**
   ** Return an immutable copy of the JarEntry within the JarFile 
   ** (or null if none was specified).
   */
  public JarEntry getJarEntry() throws IOException {
    JarEntry je = getJarFile().getJarEntry(entry_name);

    return je == null ? null : new ImmutableJarEntry(je);
  }

  /**
   ** Return the underlying JarFile, as an immutable object.
   */
  public abstract JarFile getJarFile() throws IOException;

  /**
   ** Return the URL for the underlying JarFile.  At least I think that's 
   ** what we're supposed to do ...
   */
  public URL getJarFileURL() {
     return file_url;
  }

  public Attributes getMainAttributes() throws IOException{
     Manifest man = getManifest();
     if (man == null){
        return null;
     }
    return man.getMainAttributes();
  }

  /**
   ** Return the Manifest of the underlying JarFile, as an immutable object.
   */
  public Manifest getManifest() throws IOException {
    Manifest m = getJarFile().getManifest();

    return m == null ? null : new ImmutableManifest(m);
  }

}
