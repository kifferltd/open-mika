/**************************************************************************
* Copyright (c) 2008, 2009, 2022 by KIFFER Ltd. All rights reserved.      *
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

/** The system class loader is a singleton.
** It searches for class and resource files in the system class path,
** e.g. fsroot/system.
*/
public final class SystemClassLoader extends ClassLoader {

  /**
   ** Each element of the bootstrap classpath contains one of the following:
   ** - the String bootstrap_zipfile_path, representing the bootstrap zipfile;
   ** - a File, which must be a directory; 
   ** - a JarFile.
   */
  private abstract class BootClassPathEntry {
    Object where;

    BootClassPathEntry(Object o) {
      where = o;
    }

    public abstract byte[] getClassBytes(String filename);

    public abstract URL tryResource(String name) throws MalformedURLException;

    public abstract InputStream tryResourceAsStream(String name) throws MalformedURLException;

    public String toString() {
      return "BootClassPathEntry[" + where + "]";
    }
  }

  private class FirstZipBootClassPathEntry  extends BootClassPathEntry {
    FirstZipBootClassPathEntry() {
      super(bootstrap_zipfile_path);
    }

    public byte[] getClassBytes(String filename) {
      Etc.woempa(7, "Calling getBootstrapFile(" + filename + ")");
      return getBootstrapFile(filename);
    }

    public URL tryResource(String name) throws MalformedURLException {
      Etc.woempa(7, "Let's see if getBootstrapFile(" + name + ") returns anything");
      if (getBootstrapFile(name) != null) {
        return new URL("jar:file:" + bootstrap_zipfile_path + "!/"+name);
      }

      return null;
    }

    public InputStream tryResourceAsStream(String name) throws MalformedURLException {
      Etc.woempa(7, "Let's see if getBootstrapFile(" + name + ") returns anything");
      byte[] bytes = getBootstrapFile(name);
      if (bytes != null) {
        return new java.io.ByteArrayInputStream(bytes);
      }

      return null;
    }

    public String toString() {
      return "FirstZipBootClassPathEntry[" + where + "]";
    }
  }

  private class DirectoryBootClassPathEntry  extends BootClassPathEntry {
    DirectoryBootClassPathEntry(File f) {
      super(f);
    }

    public byte[] getClassBytes(String filename) {
      Etc.woempa(7, "Try looking for a file called " + filename + " in " + this);
      File f = new File((File)where, filename);
      byte[] bytes = null;
      if (f.isFile()) {
        try {
          int length = (int)f.length();
          InputStream in = new FileInputStream(f);
          Etc.woempa(7, "System Class Loader: found file " + f + ", length = " + length);
          bytes = new byte[length];
          length = in.read(bytes);
        }
        catch (FileNotFoundException fnfe) {
          Etc.woempa(7, "System Class Loader: failed top find file " + f);
        }
        catch(IOException ioe) {
          ioe.printStackTrace();
        }
      }

      return bytes;
    }

    public URL tryResource(String name) throws MalformedURLException {
      File f = new File((File)where, name);
      Etc.woempa(7, "Try looking for a file called " + name + " in directory " + where);
      if (f.isFile()) {
        return new URL("file:" + f);
      }

      return null;
    }

    public InputStream tryResourceAsStream(String name) throws MalformedURLException {
      Etc.woempa(7, "Try looking for a file called " + name + " in directory " + where);
      File f = new File((File)where, name);
      if (f.isFile()) {
        try {
          return new FileInputStream(f);
        }
        catch (FileNotFoundException fnfe) {}
      }

      return null;
    }

    public String toString() {
      return "DirectoryClassPathEntry[" + where + "]";
    }
  }

  private class JarFileBootClassPathEntry  extends BootClassPathEntry {
    JarFileBootClassPathEntry(JarFile jf) {
      super(jf);
    }

    public byte[] getClassBytes(String filename) {
      JarFile jf = (JarFile)where;
      byte[] bytes = null;
      Etc.woempa(7, "Try to find entry " + filename + " in " + jf);
      try {
        JarEntry je = jf.getJarEntry(filename);
        if (je != null){
          int length = (int)je.getSize();
          InputStream in = jf.getInputStream(je);
          Etc.woempa(7, "System Class Loader: found " + filename + " in " + jf + ", length = " + length);
          bytes = new byte[length];
          length = in.read(bytes);
        }
        else {
          Etc.woempa(7, "System Class Loader: failed to find file " + filename + " in " + jf);
        }
      } catch (IOException e){
        e.printStackTrace();
      }

      return bytes;
    }

    public URL tryResource(String name) throws MalformedURLException {
      JarFile jf = (JarFile)where;
      Etc.woempa(7, "Calling getJarEntry(" +name + ") on " + jf);
      JarEntry je = jf.getJarEntry(name);
      if (je != null) {
        return new URL("jar:file:" + jf.getName() + "!/"+name);
      }

      return null;
    }

    public InputStream tryResourceAsStream(String name) throws MalformedURLException {
      JarFile jf = (JarFile)where;
      Etc.woempa(7, "Calling getJarEntry(" +name + ") on " + jf);
      try {
        JarEntry je = jf.getJarEntry(name);
        if (je != null){
          return jf.getInputStream(je);
        }
      }
      catch (IOException e){}

      return null;
    }

    public String toString() {
      return "JarFileBootClassPathEntry[" + where + "]";
    }
  }

  /**
   ** The bootstrap classpath.
   */
  private static List bootclasspath = new ArrayList();

  /**
   ** The absolute path to the bootstrap zipfile. For this file we don't 
   ** create a JarFile object, we use the native bootstrap loader instead.
   */
  private static String bootstrap_zipfile_path;

  /**
   ** The one and only instance of SystemClassLoader.
  */
  private static SystemClassLoader theSystemClassLoader;

  /**
   ** A PermissionCollection which contains AllPermission.
  */
  /**
   ** The unique instance which represents the the System domain.
  */
  private static ProtectionDomain systemProtectionDomain;

  /**
   ** Set 'scl' as the system class loader. May only be called once!
   */
  private static native void setSystemClassLoader(SystemClassLoader scl);

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
  public synchronized static SystemClassLoader getInstance() {
    if (theSystemClassLoader == null) {
      // just for fun
      Iterator iter = bootclasspath.iterator();

      PermissionCollection theAllPermission;

      if (wonka.vm.SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
        theAllPermission = new wonka.security.DefaultPermissionCollection();
        theAllPermission.add(new AllPermission());
        systemProtectionDomain = new ProtectionDomain(null, theAllPermission);
      }
      theSystemClassLoader = new SystemClassLoader();
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
  private SystemClassLoader() {
    super((ClassLoader)null);
    String bcp = getBootclasspath();
    Etc.woempa(9, "bcp = " + bcp);

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
      File f = new File(colon < 0 ? bcp : bcp.substring(0, colon));
      if (!f.exists()) {
        Etc.woempa(7, "SystemClassLoader: " + f + " does not exist, ignoring it");
        continue;
      }

      if (f.isDirectory()) {
        Etc.woempa(7, "SystemClassLoader: adding directory " + f + " to bootclasspath");
        bootclasspath.add(new DirectoryBootClassPathEntry(f));
      }
      else {
        try {
          if (bootstrap_zipfile_path == null) {
            bootstrap_zipfile_path = f.getAbsolutePath();
            Etc.woempa(7, "SystemClassLoader: adding bootstrap file " + f + " to bootclasspath");
            bootclasspath.add(new FirstZipBootClassPathEntry());
          }
          else {
            JarFile jf = new JarFile(f, false);
            Etc.woempa(7, "SystemClassLoader: adding jarfile " + f + " to bootclasspath");
            bootclasspath.add(new JarFileBootClassPathEntry(jf));
          }
        }
        catch (IOException ioe) {
          Etc.woempa(9, "SystemClassLoader/<init>: unable to create JarFile" + "(" + f + ") : " + ioe);
        }
      }

      if (colon < 0) {

        break;

      }
      bcp = bcp.substring(colon + 1);
    }

  }

  /**
   ** Extract one file from the bootstrap zipfile, as an array of bytes.
   */
  protected native byte[] getBootstrapFile(String filename);

  /**
   **Find the class with a given name, by searching bootclasspath.
   */
  protected Class findClass(String dotname)
    throws ClassNotFoundException
  {
    String filename = dotname.replace('.','/') + ".class";
    Etc.woempa(7, "System Class Loader: findClass("+dotname+")");
    byte[] bytes = null;
    InputStream in = null;
    Iterator iter = bootclasspath.iterator();

    while (iter.hasNext()) {
      BootClassPathEntry bootclasspath_entry = (BootClassPathEntry)iter.next();
      Etc.woempa(7, "findClass(" + filename + "): checking " + bootclasspath_entry);
      bytes  = bootclasspath_entry.getClassBytes(filename);

      if (bytes != null) {
        int j = filename.lastIndexOf('/');
        if(j > 0){
          String packagename = dotname.substring(0,j);
          if (getPackage(packagename) == null) {
            definePackage(packagename,"","","","","","",null);
          }
        }

        return defineClass(dotname, bytes, 0, bytes.length, systemProtectionDomain);

      }
    }

    throw new ClassNotFoundException("SystemClassLoader couldn't find "+dotname);

  }

  /**
   ** Find the (first) resource with a given name, by searching bootclasspath.
   ** If the name is absolute or fsroot-relative, search the filesystem directly.
  */
  protected URL findResource(String name) {
    URL url = null;

    if (name.startsWith("/") || name.startsWith("{}/")) {
      File f = new File(name);
      if (f.isFile()) {
        try {
          return new URL("file:"+f);
        } catch (MalformedURLException e) {
          //e.printStackTrace();
        }
      }
    }

    Iterator iter = bootclasspath.iterator();
    while (iter.hasNext()) {
      try {
        BootClassPathEntry bootclasspath_entry = (BootClassPathEntry)iter.next();
        url = bootclasspath_entry.tryResource(name);
        if (url != null) {
          break;
        }
      } catch (MalformedURLException e) {
        //e.printStackTrace();
      }
    }

    return url;
  }

  public InputStream getResourceAsStream(String name){
    Etc.woempa(7, "getResourceAsStream(" + name + ")");

    Iterator iter = bootclasspath.iterator();
    while (iter.hasNext()) {
      BootClassPathEntry bootclasspath_entry = (BootClassPathEntry)iter.next();
      Etc.woempa(7, "Checking " + bootclasspath_entry);
      try {
        return bootclasspath_entry.tryResourceAsStream(name);
      } catch (MalformedURLException e) {
        //e.printStackTrace();
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

    Iterator iter = bootclasspath.iterator();
    while (iter.hasNext()) {
      BootClassPathEntry bootclasspath_entry = (BootClassPathEntry)iter.next();
      url = bootclasspath_entry.tryResource(name);
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
    definePackage("java.util.jar","","","","","","",null);
    definePackage("java.util.zip","","","","","","",null);
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
