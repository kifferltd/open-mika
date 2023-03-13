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

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

/** The jarfile class loader is used twhen the Mika command line contains a -jar flag.
 ** Normally a single instance is created, with the ApplicationClassLoader as parent.
 ** In addition to the normal ClassLoader methods, the jardile class loader extracts
 ** and exposes the contents of the Main-Class: and Class-Path: entries in the manifest
 ** of the jarfile.
 */
public final class JarFileClassLoader extends URLClassLoader {

  private ClassLoader parentClassLoader;
  private JarFile jarfile;
  private Manifest manifest;
  private URL[] internalClassPath;

  /**
  ** Package-private constructor.
  ** TODO: RI will not search jar for classes if jarfile is also on classpath - maybe add a flag for this?
  ** @param jarfile The jarfile to be used.
  ** @param parent  The parent ClassLoader to which the jarfile class loader should delegate.
  */
  JarFileClassLoader (JarFile jarfile, ClassLoader parent) throws SecurityException, IOException {
    super(new java.net.URL[0], parent);
    this.jarfile = jarfile;
    parentClassLoader = parent;

    manifest = jarfile.getManifest();
    String icpString = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
    int icpCursor = 0;
    String elem;
    while (icpString != null) {
      int colon = icpString.indexOf(':', icpCursor);
      if (colon < 0) {
        elem = icpString.substring(icpCursor);
        icpString = null;
      }
      else {
        elem = icpString.substring(icpCursor, colon);
        icpCursor = colon + 1;
      }
      addURL(new URL(elem));
    }
  }

  /**
  ** Package-private convenience constructor.
  ** @param jarpath The path to the jarfile to be used.
  ** @param parent  The parent ClassLoader to which the jarfile class loader should delegate.
  */
  JarFileClassLoader (String jarpath, ClassLoader parent) throws SecurityException, IOException {
    this(new JarFile(jarpath), parent);
  }

  /**
   ** Package-private method to get the Class-Path of the jarfile.
   ** @return The value of the Main-Class: entry in the manifest,
   **         or null if there is none.
   */
  String getClassPath() {
    return manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
  }

  /**
   ** Package-private method to get the Main-Class of the jarfile.
   ** @return The value of the Main-Class: entry in the manifest,
   **         or null if there is none.
   */
  String getMainClass() {
    return manifest.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
  }

  public String toString() {
    return "Jarfile Class Loader";
  }

  /**
   ** Find the class with a given name, by searching the jarfile.
   */
  protected Class findClass(String dotname)
    throws ClassNotFoundException
  {
    String filename = dotname.replace('.','/') + ".class";
    Etc.woempa(7, "Jarfile Class Loader: findClass("+dotname+")");

    ZipEntry ze = jarfile.getEntry(filename);

    if (ze == null) {
      Etc.woempa(7, "Jarfile Class Loader: no matching entry in jarfile, call super.findClass("+dotname+") to search Class-Path");
      return super.findClass(dotname);
    }

    InputStream in = null;
    try {
      in = jarfile.getInputStream(ze);
    } catch (IOException ioe) {
      throw new ClassNotFoundException("Failed to read entry " + filename + " in " + jarfile, ioe);
    }

    Throwable cause;
    try {
      int len = in.available();
      byte[] bytes = new byte[len];
      in.read(bytes,0,len);
      return defineClass(dotname, bytes, 0, bytes.length);
    } catch (IOException ioe1) {
      cause = ioe1;
    } finally {
      try {
        in.close();
      } catch (IOException ioe2) {
      }
    }

    throw new ClassNotFoundException("Failed to read entry " + filename + " in " + jarfile, cause);
  }

  public InputStream getResourceAsStream(String resource){
    while (resource.charAt(0) == '/') {
      resource = resource.substring(1);
    }
    try {
      ZipEntry ze = jarfile.getEntry(resource);
      if(ze != null){
        return jarfile.getInputStream(ze);
      }
    } catch(Exception e){}

    return null;
  }

  // TODO findResource, findResources
  // For these to work nicely we need to be able to handle a JarFile as if it were a direcory ...
}
