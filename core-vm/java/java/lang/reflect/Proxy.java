/**************************************************************************
* Copyright  (c) 2003 by Acunia N.V. All rights reserved.                 *
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
** $Id: Proxy.java,v 1.3 2006/03/29 09:27:14 cvs Exp $
*/

package java.lang.reflect;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

/**
 This is a factory for dynamic proxy classes. Dynamic proxy classes are
 created on demand, and are immortal(*); a dynamic proxy class is uniquely
 identified by a classloader and the (ordered) list of interfaces which
 it implements.

 <p>
or found using the static method <code>getProxyClass(ClassLoader cl, Class[] interfaces)</code>.
 
 <p>
 (*) There seems to be no way to delete a dynamic proxy, so it will stay in
 the list of known proxies for the lifetime of the VM.
*/

public class Proxy implements java.io.Serializable {

  private static final long serialVersionUID = -2222568056686623797L;

  private static final int CLASS_ACCESS_FLAGS = Modifier.SUPER + Modifier.FINAL + Modifier.PUBLIC;
  private static final int INIT_ACCESS_FLAGS = Modifier.PUBLIC;

/**
 Hashtable used to map classloader x interfaces -> dynamic proxy class.
 The key is an ArrayList in which the first element is the classloader and
 the remaining elements are the interfaces which are implemented; the
 corresponding value is the Class object for the dynamic proxy class.
 We call such an ArrayList a "proxyspec".
 <p>
 Method getProxyClass synchronises on this object while creating a new
 proxy class, so we use this lock to protect other static structures.
*/
  private static Hashtable proxies = new Hashtable();

  private static Method method_defineProxyClass;
  private static Method method_getProxyFlag;
  private static Method method_hashCode;
  private static Method method_equals;
  private static Method method_toString;

  private static class ClassFile {
    private ByteArrayOutputStream s;
    private Vector constants;

    ClassFile() {
      s = new ByteArrayOutputStream();
      constants = new Vector();
    }

  /**
   Write a 16-bit value to the stream in bigendian mode.
  */
    void write16(int w) {
      try {
        s.write((w & 0x0000ff00) >> 8);
        s.write(w & 0x000000ff);
      }
      catch (Exception e) {}
    }

  /**
   Write an array of bytes to the stream.
  */
    void write(byte[] b) {
      try {
        s.write(b);
      }
      catch (Exception e) {}
    }

  /**
   Add an arbitrary constant to the pool.
  */
  int addConstant(byte[] b) {
    constants.add(b);

    return constants.size();
  }

  /**
   Add a UTF8 constant to the pool if necessary, and return the index of the
   new or existing constant. (Remember constants are numbered from 1!).
  <p>
   (TODO: For now, we always add, even if it's a duplicate).
  */
    int getUtf8Constant(String string) {
      int n = constants.size();

      constants.add(utf8Constant(string));

      return n + 1;
    }

  /**
   Add a class constant to the constant pool. The value returned is the index
   (counting from 1) of the class constant.
  */
   int addClassConstant(Class c) {
      int n = constants.size();

      constants.add(new byte[] {7, 0, (byte)(n + 2)});
      constants.add(utf8Constant(c.getName().replace('.','/')));

      return n + 1;
    }

  /**
   Write the contents of the constant pool to the classfile.
  */
    void writeConstants() {
      int n = constants.size();
      write16(n + 1);
      try {
        for (int i = 0; i < n; ++i) {
          write((byte[])constants.elementAt(i));
        }
      }
      catch (Exception e) {
      }
      constants = null;
    }

    byte[] toByteArray() {
      return s.toByteArray();
    }
  }

  static {
    try {
      Class class_bytearray = Class.forName("[B");
      method_defineProxyClass = ClassLoader.class.getDeclaredMethod("defineProxyClass", new Class[] {String.class, class_bytearray, int.class, int.class});
      method_hashCode = Object.class.getDeclaredMethod("hashCode", null);
      method_equals = Object.class.getDeclaredMethod("equals", new Class[] {Object.class});
      method_toString = Object.class.getDeclaredMethod("toString", null);
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

/**
 The InvocationHandler for this instance.
*/
  protected InvocationHandler h;

/**
 When an instance of a dynamic proxy class is created, an InvocationHandler
 is specified. (So the InvocationHandler belongs to the instance, not the
 class).
*/
  protected Proxy(InvocationHandler h){
  // TODO: checks?
    this.h = h;
  }

/**
 You can ask an instance  of a dynamic proxy class for its InvocationHandler.
*/
  public static InvocationHandler getInvocationHandler(Object object) throws IllegalArgumentException {
    if(object instanceof Proxy){
      return ((Proxy)object).h;
    }
    throw new IllegalArgumentException("'"+object+"' is not a Proxy");
  }

/**
 The namespace beginning "$Proxy" is reserved for dynamic proxy classes.
 We make up the name as follows:
<pre>
   $Proxy$$&lt;hash&gt;$$&lt;cl&gt;$$&lt;i1&gt;$$&lt;i2&gt;...
</pre>
 where &lt;hash&gt; is the hashcode of the ArrayList used as the key to the
 hashtable of known proxies, as a hex string; &lt;cl&gt; is the name of the
 class of the classloader, and &lt;i1&gt; etc. are the names of the interfaces.
*/
  private static String proxyname(Package pkg, ArrayList proxyspec) {
   StringBuffer s = new StringBuffer();
   if (pkg != null) {
     s.append(pkg.getName());
     s.append('.');
   }
   s.append("$Proxy$$");
   s.append(Integer.toHexString(proxyspec.hashCode()));
   s.append("$$");
   s.append(proxyspec.get(0).getClass().getName().replace('.','_'));
   int l = proxyspec.size();
   for (int i = 1; i < l; ++i) {
     s.append("$$");
     s.append(((Class)proxyspec.get(i)).getName().replace('.','_'));
   }

   return s.toString();
  }

/**
 Write a UTF8 constant: the tag is 1, followed by the length in bytes and the
 UTF8-encoded characters.
*/
  static byte[] utf8Constant(String string) {
    ByteArrayOutputStream temp = new ByteArrayOutputStream();
    try {
      byte[] b = string.getBytes("UTF8");
      temp.write(1);
      temp.write((b.length & 0x0000ff00) >> 8);
      temp.write(b.length & 0x000000ff);
      temp.write(b);
    }
    catch (Throwable t) {}

    return temp.toByteArray();
  }

/**
*/
  private static String descriptor(Class c) {
    if (c == void.class) {
      return "V";
    }
    else if (c == boolean.class) {
      return "Z";
    }
    else if (c == byte.class) {
      return "B";
    }
    else if (c == short.class) {
      return "S";
    }
    else if (c == char.class) {
      return "C";
    }
    else if (c == int.class) {
      return "I";
    }
    else if (c == float.class) {
      return "F";
    }
    else if (c == long.class) {
      return "J";
    }
    else if (c == double.class) {
      return "D";
    }
    else if (c.isArray()) {
      return "["+descriptor(c.getComponentType());
    }
    else {
      return "L"+c.getName().replace('.','/')+";";
    }
  }

/**
 Create a dynamic proxy class. We write a simple classfile in memory, and
 pass it to classloader.defineProxyClass().
*/
  private static Class makeproxy(Package pkg, ArrayList proxyspec) {
    int numInterfaces = proxyspec.size() - 1;
    int i;
    String name = proxyname(pkg, proxyspec);
    ClassFile classfile = new ClassFile();

// Build the constant pool. Remember where we put the class constant for the
// superclass java.lang.reflect.Proxy (super_index) and the superinterfaces
// (itf_index[]), the strings "<init>", "(Ljava/lang/reflect/InvocationHandler;)V",
// and "Code".
    classfile.addConstant(new byte[] {7, 0, 2});
    classfile.addConstant(utf8Constant(name.replace('.','/')));
    int super_index = classfile.addClassConstant(java.lang.reflect.Proxy.class);
    int init_index = classfile.getUtf8Constant("<init>");
    int init_desc_index = classfile.getUtf8Constant("(Ljava/lang/reflect/InvocationHandler;)V");
    int init_nat_index = classfile.addConstant(new byte[] {12, 0, (byte)init_index, 0, (byte)init_desc_index});
    int init_method_index = classfile.addConstant(new byte[] {10, 0, (byte)super_index, 0, (byte)init_nat_index});
    int code_tag_index = classfile.addConstant(utf8Constant("Code"));
    int[] itf_index = new int[numInterfaces];
    for (i = 0; i < numInterfaces; ++i) {
      itf_index[i] = classfile.addClassConstant((Class)proxyspec.get(i + 1));
    }

// OK, let's write out the class file.
// magic number, minor version, major version
    classfile.write16(0xcafe);
    classfile.write16(0xbabe);
    classfile.write16(0);
    classfile.write16(45);
// constant pool
    classfile.writeConstants();
// access flags
    classfile.write16(CLASS_ACCESS_FLAGS);
// this class
    classfile.write16(1);
// super class
    classfile.write16(super_index);
// interfaces
    classfile.write16(numInterfaces);
    for (i = 0; i < numInterfaces; ++i) {
      classfile.write16(itf_index[i]);
    }
// fields (there ain't no fields)
    classfile.write16(0);
// methods : <init>(Ljava/lang/reflect/InvocationHandler;)V
// has one attribute, namely its Code.
    classfile.write16(1);
    classfile.write16(INIT_ACCESS_FLAGS);
    classfile.write16(init_index);
    classfile.write16(init_desc_index);
    classfile.write16(1);  // no. attributes
    classfile.write16(code_tag_index);
    classfile.write16(0);  // MS half of attribute length
    classfile.write16(18); // LS half of attribute length
    classfile.write16(2);  // max stack
    classfile.write16(2);  // max locals
    classfile.write16(0);  // MS half of code length
    classfile.write16(6);  // LS half of code length
                           // Code: aload0
                           //       aload1
                           //       invokespecial java.lang.reflect.Proxy(Ljava/lang/reflect/InvocationHandler;)V
                           //       vreturn
    classfile.write(new byte[] {0x2a, 0x2b, (byte)0xb7, 0, (byte)init_method_index, (byte)0xb1});
    classfile.write16(0);  // exception table length
    classfile.write16(0);  // code attributes (no attributes)
// class attributes
    classfile.write16(0);
// That's it, folks ...

    ClassLoader cl = (ClassLoader)proxyspec.get(0);
    if (cl == null) {
      cl = ClassLoader.getSystemClassLoader();
    }
    byte[] b = classfile.toByteArray();
    Class c = null;
    try {
      method_defineProxyClass.setAccessible(true);
      c = (Class)method_defineProxyClass.invoke(cl, new Object[] {name, b, new Integer(0), new Integer(b.length)});
      method_defineProxyClass.setAccessible(false);
    }
    catch (IllegalAccessException e) {
      e.printStackTrace();
    }
    catch (InvocationTargetException e) {
      e.printStackTrace();
    }

    return c;
  }

  public static Class getProxyClass(ClassLoader loader, Class[] interfaces) throws IllegalArgumentException {
    ArrayList al = new ArrayList();
    Package pkg = null;

    // Check the 'interfaces' array:
    for (int i = 0; i < interfaces.length; ++i) {
      Class itf = interfaces[i];
      // must contain only interfaces, not classes or primitive types.
      if (!itf.isInterface()) {
        throw new IllegalArgumentException(itf + " is not an interface");
      }

      // no duplicates
      if (al.contains(itf)) {
        throw new IllegalArgumentException(itf + " is duplicate interface");
      }

      // must be visible through specified class loader
      try {
        if (Class.forName(itf.getName(), false, loader) != itf) {
          throw new IllegalArgumentException(itf + " is not visible using " + loader);
        }
      }
      catch (Exception e) {
        throw new IllegalArgumentException(itf + " is not visible using " + loader + ": " + e);
      }

      if (!Modifier.isPublic(itf.getModifiers())) {
        if (pkg == null) {
          pkg = itf.getPackage();
        }
        else if (itf.getPackage() != pkg) {
          throw new IllegalArgumentException(itf + " is not in same package as " + interfaces[0]);
      }
    }

      // TODO: No two interfaces may each have a method with the same name and parameter signature but different return type.

      al.add(itf);
    }

    ArrayList proxyspec = new ArrayList(interfaces.length + 1);
    proxyspec.add(loader);
    int l = interfaces.length;
    for (int i = 0; i < l; ++i) {
      proxyspec.add(interfaces[i]);
    }
    synchronized(proxies) {
      Class proxy = (Class)proxies.get(proxyspec);
      if (proxy != null) {
        return proxy;
      }
      proxy = makeproxy(pkg, proxyspec);
      proxies.put(proxyspec, proxy);
      return proxy;
    }
  }

  public static native boolean isProxyClass(Class clazz);

  public static Object newProxyInstance(ClassLoader loader, Class[] interfaces, InvocationHandler h) throws IllegalArgumentException {
    Class proxy = getProxyClass(loader, interfaces);
    try {
      Constructor constructor = proxy.getConstructor(new Class[] { InvocationHandler.class });
      return constructor.newInstance(new Object[] { h });
    }
    catch (java.lang.NoSuchMethodException nsme) {
      throw new java.lang.UnknownError(proxy + " has no constructor taking an InvocationHandler as argument : " + nsme);
    }
    catch (java.lang.InstantiationException ie) {
      throw new java.lang.UnknownError(proxy + " is abstract (???) : " + ie);
    }
    catch (java.lang.IllegalAccessException iae) {
      throw new java.lang.UnknownError(proxy + " constructor is not public : " + iae);
    }
    catch (InvocationTargetException ite) {
      throw new java.lang.UnknownError(proxy + "constructor failed : " + ite);
    }
  }

  public int hashCode() {
    try {
      return ((Integer)h.invoke(this, method_hashCode, null)).intValue();
    }
    catch (RuntimeException re) {
      throw re;
    }
    catch (Error e) {
      throw e;
    }
    catch (Throwable t) {
      throw new UndeclaredThrowableException(t);
    }
  }

  public boolean equals(Object o) {
    try {
      return ((Boolean)h.invoke(this, method_equals, new Object[] {o})).booleanValue();
    }
    catch (RuntimeException re) {
      throw re;
    }
    catch (Error e) {
      throw e;
    }
    catch (Throwable t) {
      throw new UndeclaredThrowableException(t);
    }
  }

  public String toString() {
    try {
      return (String)h.invoke(this, method_toString, null);
    }
    catch (RuntimeException re) {
      throw re;
    }
    catch (Error e) {
      throw e;
    }
    catch (Throwable t) {
      throw new UndeclaredThrowableException(t);
    }
  }

}
