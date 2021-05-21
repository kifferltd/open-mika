/**************************************************************************
* Parts copyright (c) 2001 by Punch Telematix. All rights reserved.       *
* Parts copyright (c) 2009 by /k/ Embedded Java Solutions.                *
* All rights reserved.                                                    *
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

/*
** $Id: NewSystemClassLoader.java,v 1.2 2006/02/17 10:53:19 cvs Exp $
*/

package wonka.vm;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AllPermission;
import java.security.PermissionCollection;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

/** The system class loader is a singleton.
** It searches for class and resource files in the system class path,
** e.g. fsroot/system.
*/
public final class NewSystemClassLoader extends ClassLoader {

  /**
   ** The bootstrap classpath, as an array of Zip/JarFile's and/or 
   ** File's (the latter must be directories).
   */
  private static Object[] bootclasspath;

  /**
   ** The one and only instance of SystemClassLoader.
  */
  private static NewSystemClassLoader theSystemClassLoader;

  /**
   ** A PermissionCollection which contains AllPermission.
  */
  /**
   ** The unique instance which represents the the System domain.
  */
  private static ProtectionDomain systemProtectionDomain;

  private static native void setSystemClassLoader(NewSystemClassLoader scl);

  /**
   ** Get the identity of the system class loader.
   ** The instance is created if it does not already exist.
   ** The caller must have RuntimePermission "getClassLoader".
   ** (In fact normally this method should only be called by 
   ** java.lang.ClassLoader).
   **
   ** While creating the SystemClassLoader singleton we also create
   ** the systemProtectionDomain.
   */
  public synchronized static NewSystemClassLoader getInstance() {
    if (theSystemClassLoader == null) {
      PermissionCollection theAllPermission;

      if (wonka.vm.SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
        theAllPermission = new wonka.security.DefaultPermissionCollection();
        theAllPermission.add(new AllPermission());
        systemProtectionDomain = new ProtectionDomain(null, theAllPermission);
      }
      theSystemClassLoader = new NewSystemClassLoader();
      setSystemClassLoader(theSystemClassLoader);
    }
    else {
      if (wonka.vm.SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
        SecurityManager sm = System.getSecurityManager();
        if(sm!=null) {
          sm.checkPermission(new RuntimePermission("getClassLoader"));
        }
      }
    } 

    return theSystemClassLoader;
  }


  /**
  ** A private constructor, so that only getInstance() can create an instance.
  ** Its parent is the bootstrap class loader, represented by `null'.
  **
  ** We parse the bootclasspath into its component jarfiles or directories,
  ** and put them in an array of Object (as JarFile and File respectively).
  */
  private NewSystemClassLoader() {
    super((ClassLoader)null);
    Vector v = new Vector();
    String bcp = getBootclasspath();
    Etc.woempa(9, "SystemClassLoader/<init>: bootclasspath = "+bcp);

    try {
      // just to prevent a bootstrapping problem
      Class.forName("java.util.jar.JarFile");
      new ClassCastException();
    }
    catch (ClassNotFoundException e) {
      e.printStackTrace();
    }

    while (true) {
      int    colon = bcp.indexOf(':');
      String s = colon < 0 ? bcp : bcp.substring(0, colon);
      if (s.endsWith("/")) {
        v.addElement(new File(s));
      }
      else {
        try {
          v.addElement(new JarFile(s, false));
        }
        catch (IOException ioe) {
          Etc.woempa(9, "SystemClassLoader/<init>: unable to create JarFile" + "(" + s + ") : " + ioe);
        }
      }

      if (colon < 0) {

        break;

      }
      bcp = bcp.substring(colon + 1);
    }

    bootclasspath = new Object[v.size()];

    Enumeration e = v.elements();
    int i = 0;
    while (e.hasMoreElements()) {
      bootclasspath[i] = e.nextElement();
      Etc.woempa(9, "SystemClassLoader/<init>: bootclasspath["+i+"]: "+bootclasspath[i]);
      ++i;
    }
  }

  /**
   **Find the class with a given name, by searching bootclasspath.
   */
  protected Class findClass(String dotname)
    throws ClassNotFoundException
  {

    wonka.vm.Etc.woempa(7, "System Class Loader: findClass("+dotname+")");
    String filename = dotname.replace('.','/') + ".class";
    byte[] bytes = null;
    int length = 0;
    InputStream in = null;
    wonka.vm.Etc.woempa(7, "System Class Loader: findClass("+dotname+"): filename = " + filename);

    for (int i = 0; i < bootclasspath.length; ++i) {
      Etc.woempa(7, "SystemClassLoader/findClass: bootclasspath["+i+"]: "+bootclasspath[i]);
      try {
        JarFile jf = (JarFile)bootclasspath[i];
        try {
          wonka.vm.Etc.woempa(7, "System Class Loader: findClass("+dotname+"): seeking in " + jf);
          JarEntry je = jf.getJarEntry(filename);
          wonka.vm.Etc.woempa(7, "System Class Loader: findClass("+dotname+"): seeking " + filename + " in " + jf);
          if (je != null){
            length = (int)je.getSize();
            in = jf.getInputStream(je);
            wonka.vm.Etc.woempa(7, "System Class Loader: findClass("+dotname+"): found " + filename + " in " + jf + ", length = " + length);

            break;
          }
        } catch (IOException e){
	        length = 0;
          e.printStackTrace();
        }
      } catch (ClassCastException cce) {
        File f = new File(bootclasspath[i] + filename);
        wonka.vm.Etc.woempa(7, "System Class Loader: findClass("+dotname+"): seeking file " + f);
        if (f.isFile()) {
          try {
            length = (int)f.length();
            in = new FileInputStream(f);
            wonka.vm.Etc.woempa(7, "System Class Loader: findClass("+dotname+"): found file " + f + ", length = " + length);

            break;
          }
          catch (FileNotFoundException fnfe) {
          }
        }
      }
    }

    if (length > 0) {
      try {
        bytes = new byte[length];
        length = in.read(bytes);
        int j = filename.lastIndexOf('/');
        if(j > 0){
          String packagename = dotname.substring(0,j);
          if (getPackage(packagename) == null) {
            definePackage(packagename,"","","","","","",null);
          }
        }

        return defineClass(dotname,bytes,0,length,systemProtectionDomain);

      }
      catch(IOException ioe) {
      }
    }

    throw new ClassNotFoundException("SystemClassLoader couldn't find "+dotname);

  }

  /**
   ** Private method to see if a resource is present in a particular element
   ** of bootclasspath.
   */
  private URL tryResource(String name, Object bcpelem) {
    URL url = null;

    try {
      JarFile jf = (JarFile)bcpelem;
      JarEntry je = jf.getJarEntry(name);
      if (je != null) {
        try {
          url = new URL("jar:" + jf + "!/"+name);
        } catch (MalformedURLException e) {
          e.printStackTrace();
        }
      }
    } catch (ClassCastException cce) {
      File f = new File(bcpelem + name);
      if (f.isFile()) {
        try {
          url = new URL("file:"+f);
        } catch (MalformedURLException e) {
          Etc.woempa(10, "SystemClassLoader: exception = "+e+", file = "+f);
          //e.printStackTrace();
        }
      }
    }

    return url;
  }

  /**
   ** Find the (first) resource with a given name, by searching bootclasspath.
  */
  protected URL findResource(String name) {
    URL url = null;

    for (int i = 0; i < bootclasspath.length; ++i) {
      url = tryResource(name, bootclasspath[i]);
      if (url != null) {
        break;
      }
    }

    return url;
  }

  public InputStream getResourceAsStream(String name){
    for (int i = 0; i < bootclasspath.length; ++i) {
      try {
        JarFile jf = (JarFile)bootclasspath[i];
        try {
          JarEntry je = jf.getJarEntry(name);
          if (je != null){
            return jf.getInputStream(je);
          }
        }
        catch (IOException e){}
      }
      catch (ClassCastException cce) {
        File f = new File(bootclasspath[i] + name);
        if (f.isFile()) {
          try {
            return new FileInputStream(f);
          }
          catch (FileNotFoundException fnfe) {}
        }
      }
    }

    return null;
  }


  /**
   ** Find all resources with a given name, by searching bootclasspath.
  */
  protected Enumeration findResources (String name)
    throws IOException
  {
    URL url = null;
    Vector v = new Vector();

    for (int i = 0; i < bootclasspath.length; ++i) {
      url = tryResource(name, bootclasspath[i]);
      if (url != null) {
        v.addElement(url);;
      }
    }

    return v.elements();
  }

  protected synchronized Class loadClass(String name, boolean resolve) 
    throws ClassNotFoundException 
  {
    Class loaded = findLoadedClass(name);
    if (loaded == null) {
      loaded = findClass(name);
    }

    if (resolve) {
      try {
        resolveClass(loaded);
      }
      catch (NullPointerException e) {}
    }

    return loaded;
  }

  public String toString() {
    return ("System Class Loader");
  }

  private static native String getBootclasspath();

  /**
   ** Enter the standard packages into the `packages' hashtable.
   ** TODO: get this info from mcl.jar or our defined classes.
   */
  private void defineStandardPackages() {
    definePackage("com.acunia.device","","","","","","",null);
    definePackage("com.acunia.device.uart","","","","","","",null);
    definePackage("com.acunia.device.serial","","","","","","",null);
    definePackage("wonka.resource","","","","","","",null);
    definePackage("wonka.security","","","","","","",null);
    definePackage("java.lang","","","","","","",null);
    definePackage("java.lang.ref","","","","","","",null);
    definePackage("java.lang.reflect","","","","","","",null);
    definePackage("java.io","","","","","","",null);
    definePackage("java.util","","","","","","",null);
    definePackage("java.net","","","","","","",null);
    definePackage("java.awt.event","","","","","","",null);
    definePackage("java.awt","","","","","","",null);
    definePackage("java.security","","","","","","",null);
    definePackage("java.security.cert","","","","","","",null);
    definePackage("java.math","","","","","","",null);
    definePackage("java.text","","","","","","",null);
    definePackage("java.rmi","","","","","","",null);
    definePackage("javax.comm","","","","","","",null);
    definePackage("wonka.vm","","","","","","",null);
  }

  /** Get a list of packages defined by the bootstrap class loader.
   */
  protected Package[] getPackages() {
    synchronized (this) {
      if (packages.size() == 0) {
        defineStandardPackages();
      }
    }

    Enumeration ownpackages = packages.keys();
    Package[] package_array = new Package[packages.size()];
    int i = 0;
    while(ownpackages.hasMoreElements()) {
      package_array[i++] = (Package)ownpackages.nextElement();
    }

    return package_array;
  }

  /** Find a Package defined by the system or bootstrap class loader.
   */
  protected Package getPackage(String pkgname) {
    synchronized (this) {
      if (packages.size() == 0) {
        defineStandardPackages();
      }
    }

    return (Package)packages.get(pkgname);
  }

}
