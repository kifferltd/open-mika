/**************************************************************************
* Parts copyright (c) 2001, 2002, 2003 by Punch Telematix.                *
* All rights reserved.                                                    *
* Parts copyright (c) 2009, 2010 by Chris Gray, /k/ Embedded Java         *
* Solutions.  All rights reserved.                                        *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix or of /k/ Embedded Java Solutions*
*    nor the names of other contributors may be used to endorse or promote*
*    products derived from this software without specific prior written   *
*    permission.                                                          *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX, /K/ EMBEDDED JAVA SOLUTIONS OR OTHER *
* CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,   *
* EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,     *
* PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR      *
* PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF  *
* LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING    *
* NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS      *
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.            *
**************************************************************************/

package java.lang;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;

import wonka.vm.JDWP;
import wonka.vm.NativeLibrary;
import wonka.vm.SystemClassLoader;

public abstract class ClassLoader {

  /**
   ** The application class loader, used to load the starting class and
   ** (confusingly) the one returned by getSystemClassLoader().
   */
  static ClassLoader applicationClassLoader;

  /**
   ** The extension class loader, used to load installed extensions.
   */
  static ClassLoader extensionClassLoader;

  /**
   ** The real system class loader, i.e. the one which loads from bootclasspath
   ** those classes (and resources) not loaded by the bootstrap class loader.
   */
  static ClassLoader systemClassLoader;

  /**
   ** The default protection domain.  Has a CodeSource of (null,null)
   ** and default Permissions.
   */
  private static ProtectionDomain defaultProtectionDomain;

  /** The default assertion status for this class loader - can be set by 
   ** command line or by the set*AssertionStatus methods of this class.
   ** Package-protected so that it can be read by Class.desiredAssertionStatus().
   ** N.B. The assertion status mechanism is not yet implemented!
   */
  boolean defaultAssertionStatus;

  /** The assertion status which has been specified for various packages. 
   ** Maps String(package name) -> Boolean (assertion status).
   ** Package-protected so that it can be read by Class.desiredAssertionStatus().
   ** N.B. The assertion status mechanism is not yet implemented!
   */
  HashMap packageAssertionStatus;

  /** The assertion status which has been specified for various classes. 
   ** Maps String(package name) -> Boolean (assertion status).
   ** Package-protected so that it can be read by Class.desiredAssertionStatus().
   ** N.B. The assertion status mechanism is not yet implemented!
   */
  HashMap classAssertionStatus;

  /** The packages which have been defined by this ClassLoader.
   ** Only includes those for which a definePackage() was done.
   ** The key is the package name, with as associated data the 
   ** relevant Package object.
   */
  protected final Hashtable packages = new Hashtable();

  /** The code sources of the packages which have been defined by 
   ** this ClassLoader.
   ** Only includes those for which a definePackage() was done.
   ** The key is the Package object, with as associated data the 
   ** relevant CodeSource.
   */
  private HashMap package_sources;

  /**
   ** List of libraries which were loaded by this ClassLoader.
   ** Used so that the NativeLibrary objects will only be finalized and
   ** reclaimed when this ClassLoader becomes unreachable.
   */   
  private Vector loadedLibraries = new Vector(); 

  /**
   ** Every ClassLoader has a parent, except the system ClassLoader,
   ** which has (would have) no parent - or rather its parent is 
   ** the bootstrap class loader, represented as 'null'.
   */
  private ClassLoader parent;

  /**
   ** Our name, as returned by toString()
   */
  private String ownname;

  /**
   * Link to a resource monitor on builds where this feature is enabled.
   */
  private Object resourceMonitor;

  /**
   ** Get a reference to the default protection domain.
   ** A cheap substitute for having a static initialiser.
   */
  static synchronized ProtectionDomain get_defaultProtectionDomain() {
    if (wonka.vm.SecurityConfiguration.ENABLE_SECURITY_CHECKS && defaultProtectionDomain == null) {
      CodeSource cs = new CodeSource(null,null);
      defaultProtectionDomain = new ProtectionDomain(cs,Policy.getPolicy().getPermissions(cs));
    }
    return defaultProtectionDomain;
  }

  /**
   ** Identify the most useful classloader availble - the applicationClassLoader
   ** if already defined, but failing that the extensionClassLoader or
   ** systemClassLoader.  Used to load resources (as opposed to classes)
   ** to avoid an infinite regress involving system.properties
   ** (we use systemClassLoader to load system.properties, so
   ** this needs to be in bootclasspath).
   ** Walks like a hack and quux like hack, so it probably is a hack.
   */
  private static ClassLoader getPertinentClassLoader() {
    ClassLoader loader = applicationClassLoader;
    if (loader == null) {
      loader = extensionClassLoader;
    }
    if (loader == null) {
      systemClassLoader = SystemClassLoader.getInstance();
      loader = systemClassLoader;
    }

    return loader;
  }

  private static void permissionCheck(String permission) {
    if (wonka.vm.SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
      SecurityManager sm = System.theSecurityManager;
      if (sm != null) {
        sm.checkPermission(new RuntimePermission(permission));
      }
    }
  }

  /** Any ClassLoader created using the default constructor will
   ** have as its parent the system ClassLoader.
   */
  protected ClassLoader() throws SecurityException {
    this(applicationClassLoader);
  }

  /**
   ** This new-to-Java-1.2 constructor allows the parent to be
   ** specified by the caller.
   */
  protected ClassLoader(ClassLoader parent) throws SecurityException {
    JDWP.registerClassLoader(this);
    if (getCallingClassLoader() != null || applicationClassLoader != null) {
      if (wonka.vm.SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
        SecurityManager sm = System.theSecurityManager;

        if (sm!=null) {
          sm.checkCreateClassLoader();
        }
      }
    }
    /**
    ** if parent is null we should replace it with the applicationClassLoader
    */
    this.parent = parent == null ? applicationClassLoader : parent;
    this.create();
  }

  private native void create();

  private native boolean checkClassName(String classname);

  /**
   ** As of Java 1.2 this method is no longer abstract.  Instead it
   ** implements a default policy which can be be described as 
   ** "responsible delegation".
   */
  protected synchronized Class loadClass(String classname, boolean resolve) 
    throws ClassNotFoundException 
  {
    if (ownname == null) {
      ownname = toString();
    }

    if (!checkClassName(classname)) {
      throw new ClassNotFoundException(this+": Illegal class name: "+classname);
    }

    Class loaded = _findLoadedClass(classname);
    if (loaded == null) {
      try {
        if (parent == null) {
          loaded = findClass(classname);
        }
        else {
          loaded = parent.loadClass(classname, false);
        }
      }
      catch (ClassNotFoundException e) {
        loaded = findClass(classname);
        // If that throws a ClassNotFoundException then so be it.
      }
    }

    if (resolve) {
      try {
        resolveClass(loaded);
      }
      catch (NullPointerException e) {}
    }

    return loaded;
  }

  /**
   ** If no 'resolve' parameter is supplied then the default is 'false'
   ** (N.B. in Java 1.1 it was 'true'!)
   */
  public synchronized Class loadClass(String classname) throws ClassNotFoundException {
    return loadClass(classname, false);
  }

  /**
   ** The default implementation of findClass() is just a mugtrap: any
   ** real ClassLoader must override it.
   */
  protected Class findClass(String classname) 
    throws ClassNotFoundException
  {
    throw new ClassNotFoundException(classname);
  }

  /** @deprecated Replaced by defineClass(String, byte[], int, int) 
  */
  protected final Class defineClass(byte[] data, int offset, int length) throws ClassFormatError {
    return defineClass(null, data, offset, length);
  }

  /** Turns a bag of bytecodes into a Class and register it in the namespace of this ClassLoader. 
   ** The class is not yet resolved.
   **
   ** (TODO) If the package implied by the class name
   ** already exists and has other signers, throw a SecurityException.
   ** If the ProtectionDomain is null, use the default protection domain.
   */
  protected final Class defineClass(String classname, byte data[], int offset, int length, ProtectionDomain pd)
    throws ClassFormatError
  {
    if (offset < 0 || length < 0 || offset > data.length - length) {
      throw new ArrayIndexOutOfBoundsException();
    }

    if (ownname == null) {
      ownname = toString();
    }

    if (wonka.vm.SecurityConfiguration.ENABLE_SECURITY_CHECKS) {
      int dot = classname.lastIndexOf('.');
      String package_name = dot < 0 ? "" : classname.substring(0, dot);

      ProtectionDomain actual_pd;
      CodeSource actual_source;
      Package    existing_package;
      CodeSource existing_source;

      if (applicationClassLoader == null) {
        // Don't try to get defaultProtectionDomain yet, it won't work
        actual_pd = null;
        actual_source = null;
      }
      else if (pd == null) {
        actual_pd = get_defaultProtectionDomain();
        actual_source = null;
      }
      else {
        actual_pd = pd;
        actual_source = pd.getCodeSource();
      }

// TODO: if destination package has other signers, throw SecurityException
//       Note that the list of signers is defined by the first class to be added to a package.

      synchronized(this) {
        if (package_sources == null) {
          package_sources = new HashMap();
          existing_package = null;
        }
        else {
          existing_package = getPackage(package_name);
        }

        if (existing_package == null) {
          existing_source = null;
        }
        else {
          existing_source = (CodeSource)package_sources.get(existing_package);
          if (existing_source == null) {
            package_sources.put(existing_package, actual_source);
          }
          else {
            if (!existing_source.equals(actual_source)) {
              throw new SecurityException("attempt to create class "+classname+" from "+actual_source+" when package "+package_name+" was already defined from "+existing_source);
            }
          }
        }
      }

      return _defineClass(classname,data,offset,length,actual_pd);

    }
    else {

      return _defineClass(classname, data, offset, length, null);

    }
  }

  /**
   ** If no ProtectionDomain is specified, the default domain is used.
   */
  protected final Class defineClass(String classname, byte data[], int offset, int length) 
    throws ClassFormatError 
  {
    return defineClass(classname,data,offset,length,get_defaultProtectionDomain());
  } 

  /**
   ** The deprecated method defineClass(byte data[], int offset, int length) 
   ** is not provided. :-P
   */

  private native Class _defineClass(String classname, byte data[], int offset, int length, ProtectionDomain pd)
    throws ClassFormatError;

  /** Mutant version used to define Proxy classes. Some checks are by-passed. 
   ** Invoked from java.lang.reflect.Proxy using reflection.
   */
  final Class defineProxyClass(String classname, byte data[], int
offset, int length)
    throws NullPointerException, ArrayIndexOutOfBoundsException,
ClassFormatError
  {
    ProtectionDomain pd = get_defaultProtectionDomain();
    Class c = _defineClass(classname, data, offset, length, pd);
    setProxyFlag(c);

    return c;
  }

  /**
   ** Set a class's "Proxy" flag.
   */
  private static native void setProxyFlag(Class c);


  /**
   ** resolveClass forces all constants in the constant pool of c to be resolved.
   */
  native protected final void resolveClass(Class c) 
    throws NullPointerException;

  /**
   ** findSystemClass tries to load the named class using the system ClassLoader
   */
  protected final Class findSystemClass(String classname) 
    throws ClassNotFoundException
  {
    ClassLoader loader = getPertinentClassLoader();
    if (loader == null) {
      throw new ClassNotFoundException("SystemClassLoader not yet defined");
    }
    return loader.findClass(classname);
  }

  /**
   ** findLoadedClass looks for a class in the cache of classes for which
   ** this ClassLoader was the defining ClassLoader.
   */
  protected final Class findLoadedClass(String classname) {
    if (ownname == null) {
      ownname = toString();
    }

    return _findLoadedClass(classname);
  }

  native private Class _findLoadedClass(String classname);

  /**
   ** findResource returns null: real resource-aware ClassLoaders
   ** override this method.
   */
  protected URL findResource(String resname) {
    return null;
  }

  /**
   ** findResources returns an empty Enumeration: real resource-aware 
   ** ClassLoaders override this method.
   */
  protected Enumeration findResources(String resname) 
    throws IOException
  {
    return new Enumeration() {
      public boolean hasMoreElements() { return false; }
      public Object nextElement() { return null; }
    };
  }

  /**
   ** getResource retrieves the named resource using "responsible
   ** delegation", rather like loadClass (except that there is no
   ** equivalent to "findLoadedClass").
   */
  public URL getResource(String resname) {
    if (ownname == null) {
      ownname = toString();
    }

    URL loaded = null;
    if (parent != null) {
      loaded = parent.getResource(resname);
    }

    if (loaded == null) {
      loaded = findResource(resname);
    }

    return loaded;
  }
  
  /**
   ** getResources retrieves all the resource matching "name"  in a 
   ** responsibly delegating manner.  We first call getResources on
   ** our parent ClassLoader, and then use a Vector to merge this with 
   ** our own resource (if any).  TODO: make this more efficient.
   */
  public final Enumeration getResources(String resname) throws IOException {
    if (ownname == null) {
      ownname = toString();
    }
   
    Vector merged = new Vector();
    if (parent != null) {
      Enumeration enum1 = parent.getResources(resname);
      while (enum1.hasMoreElements()) {
        merged.addElement(enum1.nextElement());
      }
    }

    Enumeration enum2 = findResources(resname);
    while (enum2.hasMoreElements()) {
      merged.addElement(enum2.nextElement());
    }

    return merged.elements();
  }
  
  /** Get all resources using the system class loader.
   * @throws IOException 
   */
  public static Enumeration getSystemResources(String resname) throws IOException {
    ClassLoader loader = getPertinentClassLoader();
    if (loader == null) {
      return null; // Hm, should really be an empty enumeration???
    }
    return loader.getResources(resname);
  }

  /**
   ** Set the signers of a class.
   */
  protected final void setSigners(Class cl, Object[] signers) {
    cl.signers = signers;
  }
  
  /**
   ** Get the identity of the system ClassLoader.
   ** Don't tell this to everybody!
   */
  public static synchronized ClassLoader getSystemClassLoader() {
    ClassLoader result = applicationClassLoader == null ? extensionClassLoader : applicationClassLoader;

    if (result != null) {
      ClassLoader caller = getCallingClassLoader();

      if (!isSystemClassLoader(caller)) {
        permissionCheck("getClassLoader");
      }
    }

    return result;
  }

  /** Get the identity of the parent ClassLoader.
   ** This too is sensitive information: only classes loaded by the
   ** bootstrap class loader or an ancestor of this ClassLoader have
   ** an automatic right to know, anyone else needs special permission.
   */
  public final ClassLoader getParent() {
    ClassLoader caller = getCallingClassLoader();
    if (!isDelegationAncestor(caller)) {
      permissionCheck("getClassLoader");
    }

    return parent;
  }

  static native ClassLoader getCallingClassLoader();

  boolean isDelegationAncestor(ClassLoader putative_ancestor) {
    return putative_ancestor == null || putative_ancestor == this || parent != null && parent.isDelegationAncestor(putative_ancestor);
  }

  // No longer final in 1.2
  /** Get a system resource in the form of an InputStream.
   */
  public static InputStream getSystemResourceAsStream(String resname) {
    ClassLoader systemClassLoader = getPertinentClassLoader();
    if (systemClassLoader == null) {
      return null;
    }
    return systemClassLoader.getResourceAsStream(resname);
  }

  /** Get a resource in the form of an InputStream.
   */
  public InputStream getResourceAsStream(String resname) {
    URL resource = getResource(resname);
    if (resource == null) {

      return null;

    }
    else {
      try {

        return resource.openStream();

      }
      catch (IOException ioe) {

        return null;

      }
    }
  }

  /** Get a resource using the system class loader.
   */
  public static URL getSystemResource(String resname) {
    ClassLoader systemClassLoader = getPertinentClassLoader();
    if (systemClassLoader == null) {
      return null;
    }
    return systemClassLoader.getResource(resname);
  }

  /** Define a package by name.
   ** The name must be unique.
   */
  protected synchronized Package definePackage (String pkgname, 
    String spectitle, String specversion, String specvendor,
    String impltitle, String implversion, String implvendor,
    URL sealbase)
    throws IllegalArgumentException
  {
    if (ownname == null) {
      ownname = toString();
    }

    Package p = (Package)packages.get(pkgname);
    if (p != null) {
      throw new IllegalArgumentException("duplicate package name: "+pkgname);
    }

    p = new Package(pkgname, spectitle, specversion, specvendor, impltitle, implversion, implvendor, sealbase);
    packages.put(pkgname,p);

    return p;
  }

  /** Get the Package object associated with the given package name.
   ** If none found, returns null.
   */
  protected Package getPackage(String pkgname) {
    Package p = (Package)packages.get(pkgname);
    if (p == null) {
      if (parent != null) {
        p = parent.getPackage(pkgname);
      }
    }
    return p;
  }

  /** Get all Package objects known to this ClassLoader and its ancestors,
   ** as an array of all things.
   */
  protected Package[] getPackages() {
    Package[] ancestorpackages = null;
    Enumeration ownpackages = null;
    int total = 0;

    if (parent != null) {
      ancestorpackages = parent.getPackages();
      total = ancestorpackages.length;
    }

    if (packages != null) {
      ownpackages = packages.keys();
      total += packages.size();
    }

    Package[] package_array = new Package[total];
    if (ancestorpackages != null) {
      System.arraycopy(ancestorpackages, 0, package_array, 0, ancestorpackages.length);
    }
    if (ownpackages != null) {
      int i = ancestorpackages.length;
      while(ownpackages.hasMoreElements()) {
        package_array[i++] = (Package)ownpackages.nextElement();
      }
    }

    return package_array;
  }

  /** default behaviour is to return null.
   */
  protected String findLibrary(String libname) {
    return null;
  }

  private static native String getCommandLineClasspath();

  private static void useExtDirs(String extdirs) {
    Vector v1;
    Vector v2;
    URL[] urls;
    int i;
    int j;
    int l;
    int sz;
    File f;
    String dirname;
    String filename;
    String[] jars;

    v1 = new Vector();
    i = extdirs.indexOf(':');
    while (i != -1) {
      dirname = extdirs.substring(0,i);
      f = new File(dirname);
      if (f.isDirectory()) {
        v1.add(dirname);
      }
      // non-directories are silently ignored.
      extdirs = extdirs.substring(i+1);
      i = extdirs.indexOf(':');
    }
    f = new File(extdirs);
    if (f.isDirectory()) {
      v1.add(extdirs);
    }

    sz = v1.size();
    v2 = new Vector();
    for (i = 0; i < sz; ++i) {
      dirname = (String)v1.elementAt(i);
      f = new File(dirname);
      jars = f.list();
      l = jars != null ? jars.length : 0;
      for (j = 0; j < l; ++j) {
        filename = dirname + File.separator + jars[j];
        if (filename.endsWith(".jar") || filename.endsWith(".JAR") || filename.endsWith(".zip") || filename.endsWith(".ZIP")) {
          v2.add(filename);
        }
      }
      v2.add(dirname);
    }

    sz = v2.size();
    urls = new URL[sz];
    j = 0;

    for (i=0 ; i < sz ; i++){
      String urlname = (String)v2.get(i);
      urlname = "file:" + urlname;

      try {
        urls[j++] = new URL(urlname);
      }
      catch(java.net.MalformedURLException mue) {
        mue.printStackTrace();
      }
    }

    extensionClassLoader = wonka.vm.ExtensionClassLoader.getInstance(urls, SystemClassLoader.getInstance());
    installExtensionClassLoader(extensionClassLoader);
  }

  private static native void installExtensionClassLoader(ClassLoader cl);

  private static native void installApplicationClassLoader(ClassLoader cl);

  private static URL[] getApplicationClasspath(String classpath) {
    Vector v;
    int i;
    int j;
    int sz;
    URL[] urls;

    v = new Vector();
    i = classpath.indexOf(':');
    while (i != -1) {
      v.add(classpath.substring(0,i));
      classpath = classpath.substring(i+1);
      i = classpath.indexOf(':');
    }
    v.add(classpath);

    sz = v.size();
    urls = new URL[sz];

    j = 0;
    for (i=0 ; i < sz ; i++) {
      String urlname = (String)v.get(i);
      if (urlname.indexOf(':') < 0) {
        if (new File(urlname).isDirectory() && !urlname.endsWith("/")) {
          urlname = urlname + "/";
        }
        urlname = "file:" + urlname;
      }
      try {
        urls[j++] = new URL(urlname);
      }
      catch(java.net.MalformedURLException mue) {
        mue.printStackTrace();
      }
    }

    return urls;
  }

  static void createApplicationClassLoader() {
    String extdirs  = System.systemProperties.getProperty("java.ext.dirs");
    String classpath = getCommandLineClasspath();

    if (extdirs != null && extdirs.trim().length() != 0) {
      useExtDirs(extdirs);
    }
    else {
      extensionClassLoader = null;
    }

    URL[] urls = getApplicationClasspath(classpath);
    ClassLoader parent = extensionClassLoader != null ? extensionClassLoader : SystemClassLoader.getInstance();
    applicationClassLoader = wonka.vm.ApplicationClassLoader.getInstance(urls, parent);
    installApplicationClassLoader(applicationClassLoader);

    if (System.systemProperties != null) {
      System.systemProperties.put("java.class.path", classpath);
    }
  }

  /**
   ** Clear the assertion status settings for all classes and packages,
   ** and the default.
   */
  public void clearAssertionStatus() {
    defaultAssertionStatus = false;
    packageAssertionStatus = null;
    classAssertionStatus = null;
  }

  /**
   ** Set the default assertion status.
   */
  public void setDefaultAssertionStatus(boolean enabled) {
    defaultAssertionStatus = enabled;
  }

  /**
   ** Set the assertion status for a package, by name.
   */
  public void setPackageAssertionStatus(String packageName, boolean enabled) {
    packageAssertionStatus.put(packageName, new Boolean(enabled));
  }

  /**
   ** Set the assertion status for a specific class, by name.
   */
  public void setClassAssertionStatus(String className, boolean enabled) {
    classAssertionStatus.put(className, new Boolean(enabled));
  }

  /**
   ** Package-local method called by Runtime when a library is loaded.
   */
  void registerLibrary(NativeLibrary library) {
    loadedLibraries.add(library);
  }

  /**
   ** Package-private method by which java.lang.SecurityManager can detect whether
   ** a class loader is a system class loader without triggering a security check.
   */
  static boolean isSystemClassLoader(ClassLoader cl) {
    return cl == null || cl == systemClassLoader || cl == extensionClassLoader || cl == applicationClassLoader;
  }

  public native void enableResourceMonitoring(boolean enable);

}
