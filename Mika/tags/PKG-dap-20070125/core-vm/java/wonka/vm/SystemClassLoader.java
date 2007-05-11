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
*                                                                         *
* Modifications copyright (c) 2005, 2006 by Chris Gray, /k/ Embedded Java *
* Solutions. All rights reserved.                                         *
*                                                                         *
**************************************************************************/

/*
** $Id: SystemClassLoader.java,v 1.5 2006/06/09 09:51:06 cvs Exp $
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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

/** The system class loader is a singleton.
** It searches for class and resource files in the system class path,
** e.g. fsroot/system.
*/
public final class SystemClassLoader extends ClassLoader {

  /**
   ** The bootstrap classpath. Each element of the array contains one of
   ** the following:
   ** - the String bootstrap_zipfile_path, representing the bootstrap zipfile;
   ** - a File, which must be a directory; 
   ** - a JarFile.
   */
  private static Object[] bootclasspath;

  /**
   ** The path to the bootstrap zipfile. For this file we don't create a 
   ** JarFile object, we use the native bootstrap loader instead.
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
      PermissionCollection theAllPermission;

      if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
        theAllPermission = new com.acunia.wonka.security.DefaultPermissionCollection();
        theAllPermission.add(new AllPermission());
        systemProtectionDomain = new ProtectionDomain(null, theAllPermission);
      }
      theSystemClassLoader = new SystemClassLoader();
      setSystemClassLoader(theSystemClassLoader);
    }
    else {
      if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
        java.security.AccessController.checkPermission(new RuntimePermission("getClassLoader"));
      }
      else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
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
    ArrayList list = new ArrayList();
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
      String s = colon < 0 ? bcp : bcp.substring(0, colon);
      if (s.endsWith("/")) {
        File f = new File(s);
        if (f.isDirectory()) {
          list.add(f);
        }
      }
      else {
        try {
          JarFile jf = new JarFile(s, false);
          if (bootstrap_zipfile_path == null) {
            bootstrap_zipfile_path = s;
          }
          list.add(jf);
        }
        catch (IOException ioe) {
//          Etc.woempa(9, "SystemClassLoader/<init>: unable to create JarFile" + "(" + s + ") : " + ioe);
        }
      }

      if (colon < 0) {

        break;

      }
      bcp = bcp.substring(colon + 1);
    }

    bootclasspath = list.toArray();
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
    byte[] bytes = null;
    InputStream in = null;

    for (int i = 0; i < bootclasspath.length; ++i) {
      Object bootclasspath_entry = bootclasspath[i];
      Etc.woempa(9, "Checking bootclasspath[" + i + "] = " + bootclasspath_entry);
      if (bootclasspath_entry == bootstrap_zipfile_path) {
        bytes = getBootstrapFile(filename);
      }
      else {
        try {
          JarFile jf = (JarFile)bootclasspath_entry;
          try {
            JarEntry je = jf.getJarEntry(filename);
            if (je != null){
              int length = (int)je.getSize();
              in = jf.getInputStream(je);
              Etc.woempa(7, "System Class Loader: findClass("+dotname+"): found " + filename + " in " + jf + ", length = " + length);
              bytes = new byte[length];
              length = in.read(bytes);

              break;
            }
            else {
              Etc.woempa(7, "System Class Loader: findClass("+dotname+"): failed top find file " + filename + " in " + jf);
            }
          } catch (IOException e){
            e.printStackTrace();
          }
        } catch (ClassCastException cce) {
          File f = new File(bootclasspath_entry + filename);
          if (f.isFile()) {
            try {
              int length = (int)f.length();
              in = new FileInputStream(f);
              Etc.woempa(7, "System Class Loader: findClass("+dotname+"): found file " + f + ", length = " + length);
              bytes = new byte[length];
              length = in.read(bytes);

              break;
            }
            catch (FileNotFoundException fnfe) {
              Etc.woempa(7, "System Class Loader: findClass("+dotname+"): failed top find file " + f);
            }
            catch(IOException ioe) {
              ioe.printStackTrace();
            }
          }
        }
      }
    }

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

    throw new ClassNotFoundException("SystemClassLoader couldn't find "+dotname);

  }

  /**
   ** Private method to see if a resource is present in a particular element
   ** of bootclasspath.
   */
  private URL tryResource(String name, Object bcpelem) throws MalformedURLException {
    URL url = null;

    if (bcpelem == bootstrap_zipfile_path && getBootstrapFile(name) != null) {
      url = new URL("jar:file:" + ((JarFile)bcpelem).getName() + "!/"+name);
    }
    else {
      try {
        JarFile jf = (JarFile)bcpelem;
        JarEntry je = jf.getJarEntry(name);
        if (je != null) {
          url = new URL("jar:" + jf + "!/"+name);
        }
      } catch (ClassCastException cce) {
        File f = new File(bcpelem + name);
        if (f.isFile()) {
          url = new URL("file:"+f);
        }
      }
    }

    return url;
  }

  /**
   ** Find the (first) resource with a given name, by searching bootclasspath.
   ** If the name is absolute or fsroot-relative, search the filesystem directly.
  */
  protected URL findResource(String name) {
    URL url = null;

    try {
      if (name.startsWith("/") || name.startsWith("{}/")) {
        File f = new File(name);
        if (f.isFile()) {

            return new URL("file:"+f);

        }
      }

      for (int i = 0; i < bootclasspath.length; ++i) {
        url = tryResource(name, bootclasspath[i]);
        if (url != null) {
          break;
        }
      }
    } catch (MalformedURLException e) {
      //e.printStackTrace();
    }

    return url;
  }

  public InputStream getResourceAsStream(String name){
    for (int i = 0; i < bootclasspath.length; ++i) {
      if (bootclasspath[i] == bootstrap_zipfile_path) {
        byte[] bytes = getBootstrapFile(name);
        if (bytes != null) {

          return new java.io.ByteArrayInputStream(bytes);

        }
      }

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
   ** TODO: get this info from wre.jar or our defined classes.
   */
  private void defineStandardPackages() {
    definePackage("com.acunia.device","","","","","","",null);
    definePackage("com.acunia.device.uart","","","","","","",null);
    definePackage("com.acunia.device.serial","","","","","","",null);
    definePackage("com.acunia.resource","","","","","","",null);
    definePackage("com.acunia.wonka.security","","","","","","",null);
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
