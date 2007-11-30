/**************************************************************************
* Copyright  (c) 2001, 2002 by Acunia N.V. All rights reserved.           *
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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: Class.java,v 1.9 2006/04/18 11:35:28 cvs Exp $
*/

package java.lang;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.io.InputStream;
import java.net.URL;
import java.security.ProtectionDomain;
import wonka.vm.SystemClassLoader;

  /*
  ** Representation of a Java type.
  **
  ** Note: this class is initialized "by hand" before the VM is fully
  ** initialized.  Consequently it must not have a static initializer.
  ** (It can have static variables, and even constant initial values
  ** for those variables, but nothing fancier and certainly no static{}
  ** clause.)
  */
public final class Class implements java.io.Serializable {

  private static final long serialVersionUID = 3206093459760846163L;
  
  /** The ClassLoader which defined this Class.
   */
  private final ClassLoader loader;

  /** The protection domain to which this class belongs.
   */
  private final ProtectionDomain domain;

  /** The signers of this Class.
   ** Package-accessible so that ClassLoader can set it.
   ** TODO: how do we stop reflection from screwing with this?
   */
  Object[]    signers;

  /**
   ** List of prefixes which denote packages that require special handling.
   */
  private static String restricted_packages;
;

  /**
   ** Get the list of restricted packages.  Doesn't work yet (crashes during
   ** bootstrapping -- FIXME).
   */
  private static synchronized String getRestrictedPackages() {
    if (restricted_packages == null){
      restricted_packages = java.security.Security.getProperty("package.access");
      if (restricted_packages == null) {
        restricted_packages = "com.acunia.wonka";
      }
    }

    return restricted_packages;
  }

  /** Private constructor to disallow ``new Class()''.
   ** We set the blank finals here to keep the compiler happy 
   ** (in reality they are set up by native code).
   */
  private Class(){
    loader = null;
    domain = null;
    signers = null;
  }

  /** Get the name of this package this class is in.
   */
  private synchronized String getPackageName() {
    String classname = getName();
    int lastdot = classname.lastIndexOf('.');
    if (lastdot < 0) {

      return "";

    }
    else {

      return classname.substring(0,lastdot);

    }
  }


  /** Find and initialize the class with the given name, using the
   ** ``current'' class loader (the one that loaded the calling class).
   ** Can also throw LinkageError or ExceptionInInitializerError.
   */
  public static Class forName(String classname)
    throws ClassNotFoundException
  {
    Class theClass = null;

    try {
      theClass = forName_S(classname);
    } catch (NoClassDefFoundError e) {
      throw new ClassNotFoundException("class loader threw NoClassDefFoundError", e);
    }

    return theClass;
  }

  /** Find and optionally initialize the class with the given name, using
   ** the given class loader.
   ** Can also throw LinkageError or ExceptionInInitializerError.
   */
  public static Class forName(String classname, boolean initialize, ClassLoader loader)
    throws ClassNotFoundException
  {
    if (loader == null && ClassLoader.getCallingClassLoader() != null) {
      if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
        java.security.AccessController.checkPermission(new RuntimePermission("getClassLoader"));
      }
      else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
          sm.checkPermission(new RuntimePermission("getClassLoader"));
        }
      }
    }

    Class theClass = null;

    try {
      theClass = forName_SZCL(classname, initialize, loader);
    } catch (NoClassDefFoundError e) {
      throw new ClassNotFoundException(loader + " threw NoClassDefFoundError", e);
    }

    return theClass;
  }

  /** Native code portion of forName(String).
   */
  private static native Class forName_S(String className)
    throws ClassNotFoundException;

  /** Native code portion of forName(String, Boolean, ClassLoader).
   */
  private static native Class forName_SZCL(String className, boolean initialize, ClassLoader loader)
    throws ClassNotFoundException;

// IS HACK
// [CG 20010510] IS OBSOLETE HACK
// [CG 20010603] REMOVED OBSOLOETE HACK :)
// => IS NO MORE HACK

  /**
   ** Create a new instance of this class.
   ** The default (no-args) constructor is used.
   ** Can also throw ExceptionInIntializerError or SecurityException.
   */
  public Object newInstance()
    throws InstantiationException, IllegalAccessException
  {
    if (isPrimitive() || isArray() || Modifier.isAbstract(getModifiers())) {
      throw new InstantiationException();
    }

    access_checks(Member.PUBLIC); 

    return newInstance0();
  }

  /** Native portion of newInstance().
   */
  private native Object newInstance0() throws InstantiationException, IllegalAccessException;

  /** Do the same checks as for the `instanceof' operator.
   ** obj is null: return false.
   ** this Class is array: return true iff obj is or can be widened to this Class.
   ** this Class is interface: return true iff class (or any superclass) of obj implements this interface.
   ** else (normal class): return true iff obj is of this Class or a subclass.
   */
  public native boolean isInstance(Object obj);

  /** Test whether this Class is the same as, or a superclass or superinterface of, Class cls.
   ** (Put differently: can Class cls be converted to this Class via an 
   ** identity or a widening conversion.  See JLS2 5.1.1, 5.1.4).
   */
  public native boolean isAssignableFrom(Class cls);

  /** Is this Class an interface?
   */
  public native boolean isInterface();

  /** Is this Class an array?
   */
  public native boolean isArray();

  /** Is this Class a primitive?
   */
  public native boolean isPrimitive();

  /** Get the unadorned, dotted fully qualified name of this Class.
   ** If an array class, return something like ``[B'' or ``[Ljava.lang.Object;''.
   */
  public native String getName();

  /** Get the ClassLoader which loaded this Class.
   ** Returns null if the bootstrap class loader was used.
   ** Performs a checkPermission ("getClassLoader") if the caller's
   ** class loader is not this Class's class loader or a delegation ancestor 
   ** thereof (class loader `null' is everybody's ancestor).
   */
  public ClassLoader getClassLoader() {
    ClassLoader cl  = ClassLoader.getCallingClassLoader();
    if (cl != null && !cl.isDelegationAncestor(loader)) {
      if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
        java.security.AccessController.checkPermission(new RuntimePermission("getClassLoader"));
      }
      else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
          sm.checkPermission(new RuntimePermission("getClassLoader"));
        }
      }
    }
    return loader;
  }

  /** Get this Class's superclass.
   ** Returns null if this Class is java.lang.Object, an interface,
   ** or a primitive type.  If this Class is an array then return
   ** java.lang.Object.
   */  
  public native Class getSuperclass();

  /** Get the Package this Class belongs to.
   ** If this Class was loaded by the bootstrap classloader,
   ** get the info from the system class loader.
   ** Otherwise, ask the classloader which loaded us.
   */
  public Package getPackage(){
    ClassLoader cl = loader == null ? SystemClassLoader.getInstance() : loader;

    return cl.getPackage(getPackageName());
  }

  /** Get the package this Class belongs to: the Class must have been
   ** loaded by the bootstrap class loader.
  private Package getBootstrapPackage(String name) {
     throw new UnsupportedOperationException("getPackage() in Class is not yet implemented for classes loaded by bootstrap loader");
  }
   */

  /** If this Class is a member of another Class, return that Class.
   ** Otherwise return null.
   ** Always returns null for a primitive or array Class.
   */
  public native Class getDeclaringClass();

  /** Return an array of Class objects, one for every Class which is a public member of this Class.
   ** The array includes all directly-declared public inner classes, but also
   ** all public classes inherited via a superclass or interface.
   ** Always returns a zero-length array for a primitive or array Class.
   **
   ** Should perform access checks for this class and every superclass (TODO).
   **
   ** Currently not implemented, throws Exception !
   */
   public Class[] getClasses() {
     access_checks(Member.PUBLIC); // TODO - also for superclasses!
     return getClasses0();
   }

   private native Class[] getClasses0();

  /** Return an array of Class objects, one for every Class which is a declared in this Class as a member.
   ** Always returns a zero-length array for a primitive or array Class.
   */
  public Class[] getDeclaredClasses()
    throws SecurityException {
     access_checks(Member.DECLARED);
     return getDeclaredClasses0();
  }

  private native  Class[] getDeclaredClasses0();

  /** Get this Class's superinterfaces.
   ** this is an interface: return all interfaces it directly extends
   **                       (in the same order as in the .class file). (*)
   ** otherwise: return all interfaces it directly implements
   **                       (in the same order as in the .class file). (*)
   ** (*) The spec says the same order as in the class's declaration, but
   ** we can't really know that ...
   ** If this Class does not extend/implement any interfaces, returns an
   ** array of length 0.
   */  
  public native Class[] getInterfaces();

  /** Get the component type of this Class.
   ** If this class is not an array class, returns null.
   */
  public native Class getComponentType();

  /** Get the modifiers of this class.
   ** Returns a bitmask which can be analysed using methods of
   ** java.lang.reflect.Modifier. The spec says to return something
   ** for public, protected, private, final, static, abstract, and
   ** interface, but currently we never return ``static'' (and for
   ** inner classes we only look at the flags at the very front of 
   ** the .class file, not any funky attributes further on).
   ** Probably needs further work ...
   */
  public native int getModifiers();

  /** Get the signers of this Class, if any.
   ** For a primitive type, will always return null.
   */
  public Object [] getSigners() {
    return signers;
  }

  /** Internal method to get all constructors of type `mtype'.
   ** `mtype' may be Memeber.PUBLIC or Member.DECLARED.
   */
  private native Constructor[] get_constructors (int mtype);

  /** Internal method to get the constructor with the given parameter types and type `mtype'.
   ** `mtype' may be Member.PUBLIC or Member.DECLARED.
   */
  private native Constructor get_one_constructor (Class[] parameterTypes, int mtype);

  /** Internal method to get all fields of type `mtype'.
   ** `mtype' may be Member.PUBLIC or Member.DECLARED.
   */
  private native Field[] get_fields (int mtype);

  /** Internal method to get the field with the given name and type `mtype'.
   ** `mtype' may be Member.PUBLIC or Member.DECLARED.
   */
  private native Field get_one_field (String fieldname, int mtype);

  /** Internal method to get all methods of type `mtype'.
   ** `mtype' may be Member.PUBLIC or Member.DECLARED.
   */
  private native Method[] get_methods (int mtype);

  /** Internal method to get the method with the given name, parameters and type `mtype'.
   ** `mtype' may be Member.PUBLIC or Member.DECLARED.
   */
  private native Method get_one_method (String methodname, Class[] parameterTypes, int mtype);

  /** Perform the typical access check: if there is a security manager,
   ** then invoke its sm.checkMemberAccess() method, and if this Class
   ** is in a package, call checkPackageAccess() as well.  The parameter
   ** passed to sm.checkMemberAccess() is determined by check_type, which
   ** should be either Member.PUBLIC or Member.DECLARED.
   */
  private void access_checks(int check_type) 
    throws SecurityException
  {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      if ((check_type != java.lang.reflect.Member.PUBLIC) && (loader != ClassLoader.getCallingClassLoader())) {
        java.security.AccessController.checkPermission(new RuntimePermission("accessDeclaredMembers."+getName()));
      }

      String restricteds = getRestrictedPackages();
      //String restricteds = "java,com.acunia.wonka";
      int comma = restricteds.indexOf(',');
      String arestricted;
      String pname = getPackageName();
      while (comma >= 0) {
        arestricted = restricteds.substring(0,comma);
        if (pname == arestricted || pname.startsWith(arestricted) && pname.charAt(arestricted.length()) == '.') {
          java.security.AccessController.checkPermission(new RuntimePermission("accessClassInPackage."+pname));
        }
        restricteds = restricteds.substring(comma+1);
        comma = restricteds.indexOf(',');
      }
      arestricted = restricteds;
      if (pname == arestricted || pname.startsWith(arestricted) && pname.charAt(arestricted.length()) == '.') {
        java.security.AccessController.checkPermission(new RuntimePermission("accessClassInPackage."+pname));
      }
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkMemberAccess(this,check_type);
        String pname = getPackageName();
        if (pname != "") {
          sm.checkPackageAccess(pname);
        }
      }
    }
  }
  
  /** Return an array containing all this Class's public constructors.
   ** Returns a zero-length array if this class has no public constructors,
   ** or is primitive or array.
   **
   ** If there is a security manager, call its checkMemberAccess(Member.PUBLIC),
   ** and if this Class is in a package call checkPackageAccess() too.
   */
  public Constructor[] getConstructors() 
    throws SecurityException
  {
    if (isPrimitive() || isArray()) {

      return new Constructor[0];

    }

    access_checks(Member.PUBLIC);

    return get_constructors(Member.PUBLIC);
  }

  /** Return an array containing all this Class's declared constructors.
   ** (This includes the default constructor if any).
   ** Returns a zero-length array if this class has no declared constructors,
   ** or is primitive or array or interface.
   **
   ** If there is a security manager, call its checkMemberAccess(Member.DECLARED),
   ** and if this Class is in a package call checkPackageAccess() too.
   */
  public Constructor[] getDeclaredConstructors() 
    throws SecurityException
  {
    if (isPrimitive() || isArray()) {

      return new Constructor[0];

    }

    access_checks(Member.DECLARED);

    return get_constructors(Member.DECLARED);
  }

  /** Get this Class's public constructor with the given parameter types.
   ** Throws NoSuchMethodException if there ain't no such animal.
   **
   ** If there is a security manager, call its checkMemberAccess(Member.PUBLIC),
   ** and if this Class is in a package call checkPackageAccess() too.
   */
  public Constructor getConstructor(Class[] parameterTypes) 
    throws NoSuchMethodException, SecurityException
  {
    if (isPrimitive() || isArray()) {

      throw new NoSuchMethodException();

    }

    access_checks(Member.PUBLIC);

    Class[] pt = parameterTypes == null ? new Class[0] : parameterTypes;

    return get_one_constructor(pt, Member.PUBLIC);
  }

  /** Get this Class's declared constructor with the given parameter types.
   ** Throws NoSuchMethodException if there ain't no such animal.
   **
   ** If there is a security manager, call its checkMemberAccess(Member.DECLARED),
   ** and if this Class is in a package call checkPackageAccess() too.
   */
  public Constructor getDeclaredConstructor(Class[] parameterTypes) 
    throws NoSuchMethodException, SecurityException
  {
    if (isPrimitive() || isArray()) {

      throw new NoSuchMethodException();

    }
    access_checks(Member.DECLARED);

    Class[] pt = parameterTypes == null ? new Class[0] : parameterTypes;

    return get_one_constructor(pt, Member.DECLARED);
  }


  /** Return an array containing all this Class's public fields.
   ** If this Class is a class, includes fields of superclasses;
   ** if this Class is an interface, includes fields of superinterfaces.
   ** Returns a zero-length array if this class has no accessible public fields,
   ** or is primitive or array.
   **
   ** If there is a security manager, call its checkMemberAccess(Member.PUBLIC),
   ** and if this Class is in a package call checkPackageAccess() too.
   */
  public Field[] getFields() 
    throws SecurityException
  {
    if (isPrimitive() || isArray()) {

      return new Field[0];

    }

    access_checks(Member.PUBLIC);

    return get_fields(Member.PUBLIC);
  }

  /** Return an array containing all this Class's declared fields.
   ** Returns a zero-length array if this class has no accessible declared fields,
   ** or is primitive or array.
   **
   ** If there is a security manager, call its checkMemberAccess(Memeber.DECLARED),
   ** and if this Class is in a package call checkPackageAccess() too.
   */
  public Field[] getDeclaredFields() 
    throws SecurityException
  {
    if (isPrimitive() || isArray()) {

      return new Field[0];

    }

    access_checks(Member.DECLARED);

    return get_fields(Member.DECLARED);
  }

  /** Get this Class's public field with the given name.
   ** Includes all fields of any interfaces this Class extends or implements,
   ** and so on recursively for each of its superclasses.
   ** Throws NoSuchFieldException if this Class has no public field of that name.
   **
   ** If there is a security manager, call its checkMemberAccess(Member.PUBLIC),
   ** and if this Class is in a package call checkPackageAccess() too.
   */
  public Field getField(String fieldname) 
    throws SecurityException, NoSuchFieldException
  {
    if (isPrimitive() || isArray()) {

      throw new NoSuchFieldException();

    }

    access_checks(Member.PUBLIC);

    return get_one_field(fieldname, Member.PUBLIC);
  }

  /** Get this Class's declared field with the given name.
   ** Throws NoSuchFieldException if this Class has no declared field of that name.
   **
   ** If there is a security manager, call its checkMemberAccess(Memeber.DECLARED),
   ** and if this Class is in a package call checkPackageAccess() too.
   */
  public Field getDeclaredField(String fieldname) 
    throws SecurityException, NoSuchFieldException
  {
    if (isPrimitive() || isArray()) {

      throw new NoSuchFieldException("class "+this+" is a"+
        (isPrimitive() ? " primitive" : "n array") 
        + " class");

    }
    access_checks(Member.DECLARED);

    return get_one_field(fieldname, Member.DECLARED);
  }

  /** Return an array containing all this Class's public methods.
   ** Does not include <clinit>.
   ** If this Class is a class, includes methods of superclasses;
   ** if this Class is an interface, includes methods of superinterfaces.
   ** Returns a zero-length array if this class has no accessible public methods,
   ** or is primitive or array.
   **
   ** If there is a security manager, call its checkMemberAccess(Memeber.PUBLIC),
   ** and if this Class is in a package call checkPackageAccess() too.
   */
  public Method[] getMethods() 
    throws SecurityException
  {
    if (isPrimitive() || isArray()) {

      return new Method[0];

    }

    access_checks(Member.PUBLIC);

    return get_methods(Member.PUBLIC);
  }

  /** Return an array containing all this Class's declared methods.
   ** Does not include <clinit>.
   ** Returns a zero-length array if this class has no accessible declared methods,
   ** If there is a security manager, call its checkMemberAccess(Memeber.DECLARED),
   ** and if this Class is in a package call checkPackageAccess() too.
   */
  public Method[] getDeclaredMethods() 
    throws SecurityException
  {
    if (isPrimitive() || isArray()) {

      return new Method[0];

    }

    access_checks(Member.DECLARED);

    return get_methods(Member.DECLARED);
  }


  /** Get this Class's public method with the given name and parameter types.
   ** Throws NoSuchMethodException if there ain't no such animal,
   ** or if the method name is <init> or <clinit> (get real).
   ** The spec says to search first this Class, then its superclasses,
   ** then its superinterfaces.  It also says that for each class or
   ** interface that we check, if there are multiple methods with the
   ** same name and parameters but different return types, then we should
   ** take the one with the most specific return type.  I don't grok that:
   ** surely there cannot be two methods in one class which differ only
   ** in their return type??? TODO: check JLS 8.2 and 8.4.
   **
   ** If there is a security manager, call its checkMemberAccess(Memeber.PUBLIC),
   ** and if this Class is in a package call checkPackageAccess() too.
   */
  public Method getMethod(String methodname, Class[] parameterTypes) 
    throws SecurityException, NoSuchMethodException
  {
    if (isPrimitive() || isArray() || methodname==null || methodname.equals("<init>") || methodname.equals("<clinit>")) {

      throw new NoSuchMethodException();

    }

    access_checks(Member.PUBLIC);

    Class[] pt = parameterTypes == null ? new Class[0] : parameterTypes;

    return get_one_method(methodname, pt, Member.PUBLIC);
  }

  /** Get this Class's public method with the given name and parameter types.
   ** Throws NoSuchMethodException if there ain't no such animal,
   ** or if the method name is <init> or <clinit> (get real).
   **
   ** If there is a security manager, call its checkMemberAccess(Memeber.DECLARED),
   ** and if this Class is in a package call checkPackageAccess() too.
   */
  public Method getDeclaredMethod(String methodname, Class[] parameterTypes) 
    throws SecurityException, NoSuchMethodException
  {
    access_checks(Member.DECLARED);

    Class[] pt = parameterTypes == null ? new Class[0] : parameterTypes;

    return get_one_method(methodname, pt, Member.DECLARED);
  }



  /** As getResourceAsStream(String), but returns a URL instead of an InputStream.
   */
  public URL getResource(String name) {
    ClassLoader cl = getClassLoader();
    String slashed;
    if (!name.startsWith("/")) {
      if (!name.startsWith("{}/")) {
        String packagename = getPackageName();
        slashed = (packagename == "" ? name :
          getPackageName().replace('.','/')+"/"+name);
      } else {
        slashed = name.substring(3);
      }
    } else {
      slashed = name.substring(1);
    }

    if (cl==null) {
      return ClassLoader.getSystemResource(slashed);
    }
    else {
      return cl.getResource(slashed);
    }
  }

  /** Find the resource with a given name, using the namespace of this Class.
   ** If the name begins with `/' or `{}/', leave it be: otherwise prefix it with
   ** the name of this Class's package, with all `.' replaced by `/'.
   ** Then pass the resulting mess to this Class's class loader.
   ** (The spec doesn't say what happens if this Class is not part of a
   ** package, so I took the liberty of prefixing ``./'').
   **
   ** If this Class's class loader is null, punt to getSystemResourceAsStream().
   ** The result is returned in the form of an InputStream.
   */
  public InputStream getResourceAsStream(String name) {
    ClassLoader cl = getClassLoader();
    String slashed;
    if (!name.startsWith("/")) {
      if (!name.startsWith("{}/")) {
        String packagename = getPackageName();
        slashed = (packagename == "" ? name :
          getPackageName().replace('.','/')+"/"+name);
      } else {
        slashed = name.substring(3);
      }
    } else {
      slashed = name.substring(1);
    }

    if (cl==null) {
      return ClassLoader.getSystemResourceAsStream(slashed);
    }
    else {
      return cl.getResourceAsStream(slashed);
    }
  }

  /** Get this Class's ProtectionDomain.
   ** If there is a SecurityManager, call its checkPermission with 
   ** a RuntimePermission("getProtectionDomain") first.
   */
  public ProtectionDomain getProtectionDomain() {
    if (wonka.vm.SecurityConfiguration.USE_ACCESS_CONTROLLER) {
      java.security.AccessController.checkPermission(new RuntimePermission("getProtectionDomain"));
    }
    else if (wonka.vm.SecurityConfiguration.USE_SECURITY_MANAGER) {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkPermission(new RuntimePermission("getProtectionDomain"));
      }
    }

    return domain;
  }

  /**
   ** Get the most specific assertion status from the class loader's data.
   ** Mainly for use by the VM itself, cannot reliably be used by applications.
   */
  public boolean desiredAssertionStatus() {
    Object foo = loader.classAssertionStatus.get(getName());

    if (foo == null) {
      foo = loader.packageAssertionStatus.get(getPackageName());

      if (foo == null) {
        return loader.defaultAssertionStatus;    
      }
    }

    return ((Boolean)foo).booleanValue();

  } 

  /** Create the string representation of a Class.
   ** Class is primitive -> same as get getName()
   ** Class is interface -> ``interface foo.bar.Baz''.
   ** else -> ``class foo.bar.Baz''.
   */
  public String toString() {
    String name = getName();

    if (isPrimitive()) {

      return name;

    }
    else if (isInterface()) {

      return "interface "+name;

    }
    else {

      return "class "+name;

    }
  }
  
}
