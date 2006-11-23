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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


/**
 * $Id: JarURLConnection.java,v 1.2 2006/03/29 09:27:14 cvs Exp $
 */

package java.net;

import com.acunia.wonka.net.jar.ImmutableJarEntry;
import com.acunia.wonka.net.jar.ImmutableManifest;

import java.io.IOException;
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
** Base implementation for Jar URL.
**
** Look in com.acunia.wonka.net.jar for more informartion an actual implementation
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
