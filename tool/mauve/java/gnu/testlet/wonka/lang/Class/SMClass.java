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
 **************************************************************************/

package gnu.testlet.wonka.lang.Class;

import gnu.testlet.*;
import java.lang.reflect.*;
import java.util.ArrayList;

import gnu.testlet.wonka.lang.Class.Classhlp.SMhlpClass;

/**
 * The class java.lang.Class is tested using 3 files <br> - ClassTest.java <br> -
 * reflect.java <br> - SMCLass.java <br>
 * <br>
 * in reflect.java the test for getConstructors and getDeclaredConstructors <br>
 * are commented out because they rely on methods which are not implemented <br>
 * (methods from java.lang.reflect.Constructor) <br>
 * <br> | | --> test files do not test for SecurityExceptions <br> | since there
 * isn't a security manager defined yet<br>
 * a soon as we have spec's, we should write these tests <br> * <br>
 * <br>
 * All methods are tested except getClasses and getDeclaredClasses --> which
 * <br>
 * are not yet by java but they are mentioned in the spec's <br>
 * getSigners is also not tested --> this method is closely linked to the <br>
 * classloader and security manager. We should implement this test if we <br>
 * have the complete spec's of both of them <br>
 * <br>
 * this an appendum to ClassTest.java and reflect.java <br>
 * together these 3 classes should test java.lang.Class completly <br>
 * <br>
 * methods tested: getSigners, getModifiers, isPrimitive <br>
 * <br>
 * special attention: <br>
 * <br> - the functions getClasses and getDeclaredClasses are stated not
 * implemented <br>
 * in the spec's and so they are not tested. ( a test for them might be needed
 * <br>
 * in the future ) --> implemented in JLS 1.2 <br> - getDeclaringClass needs
 * some extra tests <br> - forName needs extratest --> changed JLS 1.2 <br> -
 * getPackage and getProtectionDomain are new in JLS 1.2 <br>
 * <br>
 * the test for getPackage is commented out --> method is not in wonka
 */
public class SMClass implements Testlet, Cloneable {
  static interface MySuperInterface {
    public static final String supervalue1 = "myvalue";

    public static final String supervalue2 = "myvalue";

    public static final String supervalue3 = "myvalue";

    public void supers();
  }

  static interface MyOtherSuperInterface {
    public static final Object supervalue1 = "myvalue";

    public static final Object supervalue2 = "myvalue";

    public static final Object supervalue3 = "myvalue";

    public void other();
  }

  protected TestHarness th;

  static interface MyInterFace extends MySuperInterface, MyOtherSuperInterface {
    public final static String value1 = "myValue";

    public final static String value2 = "myValue";

    public final static String value3 = "myValue";

    public String toString();

    public void myMethod();
  }

  public void test(TestHarness testharness) {
    th = testharness;
    th.setclass("java.lang.Class");
    th.checkPoint("getModifiers()int");
    test_getModifiers();
    th.checkPoint("getSigners()java.lang.Object[]");
    test_getSigners();
    th.checkPoint("isPrimitive()boolean");
    test_isPrimitive();
    test_getPackage();
    test_getProtectionDomain();
    test_forName2ndMethod();
    test_getClasses();
    test_getDeclaredClasses();
    test_getDeclaringClasses();
    test_newInstance();// extra tests -- look in ClassTest
    test_getInterfaces();// extra tests -- lookin ClassTest
    test_getMethods();// extra tests -- look in ClassTest
    test_Interfaces();
  }

  protected Class hc;

  static final private class hlpInClass {
  }

  static protected abstract class hlpInClass2 {
  }

  private class PrivateHelper{}
  
  private void test_Interfaces() {
    Class interfaceClass = MyInterFace.class;
    Field[] fields = interfaceClass.getFields();
    th.check(9, fields.length);
    ArrayList list = getNames(true);
    for (int i = 0; i < fields.length; i++) {
      String field = fields[i].toString();
      th.check(list.remove(field), "problem with " + field);
    }
    th.check(list.isEmpty(), list + " should be empty");
    list = getNames(false);
    fields = interfaceClass.getDeclaredFields();
    for (int i = 0; i < fields.length; i++) {
      String field = fields[i].toString();
      th.check(list.remove(field), "problem with " + field);
    }
    th.check(list.isEmpty(), list + " should be empty");

    list = getMethods(true);
    Method[] methods = interfaceClass.getMethods();
    for (int i = 0; i < methods.length; i++) {
      String name = methods[i].getName();
      th.check(list.remove(name), "problem with " + name);
    }
    th.check(list.isEmpty(), list + " should be empty");

    list = getMethods(false);
    methods = interfaceClass.getDeclaredMethods();
    for (int i = 0; i < methods.length; i++) {
      String name = methods[i].getName();
      th.check(list.remove(name), "problem with " + name);
    }
    th.check(list.isEmpty(), list + " should be empty");
}

  private ArrayList getMethods(boolean all) {
    ArrayList list = new ArrayList();
    list.add("myMethod");
    list.add("toString");
    if(all) {
      list.add("other");
      list.add("supers");
    }
    return list;    
  }
  
  private ArrayList getNames(boolean all) {
    ArrayList list = new ArrayList();
    list.add("public static final java.lang.String "
        + "gnu.testlet.wonka.lang.Class.SMClass$MyInterFace.value1");
    list.add("public static final java.lang.String "
        + "gnu.testlet.wonka.lang.Class.SMClass$MyInterFace.value2");
    list.add("public static final java.lang.String "
        + "gnu.testlet.wonka.lang.Class.SMClass$MyInterFace.value3");
    if (all) {
      list
          .add("public static final java.lang.String "
              + "gnu.testlet.wonka.lang.Class.SMClass$MySuperInterface.supervalue1");
      list
          .add("public static final java.lang.String "
              + "gnu.testlet.wonka.lang.Class.SMClass$MySuperInterface.supervalue2");
      list
          .add("public static final java.lang.String "
              + "gnu.testlet.wonka.lang.Class.SMClass$MySuperInterface.supervalue3");
      list
          .add("public static final java.lang.Object "
              + "gnu.testlet.wonka.lang.Class.SMClass$MyOtherSuperInterface.supervalue1");
      list
          .add("public static final java.lang.Object "
              + "gnu.testlet.wonka.lang.Class.SMClass$MyOtherSuperInterface.supervalue2");
      list
          .add("public static final java.lang.Object "
              + "gnu.testlet.wonka.lang.Class.SMClass$MyOtherSuperInterface.supervalue3");
    }
    return list;
  }

  /**
   * not implemented. <br>
   * code added to test getClasses function <br>
   * --> needed to get 1.2 tag
   * 
   */
  protected void test_getClasses() {

  }

  /**
   * not implemented. <br>
   * code added to test getDeclaredClasses function <br>
   * --> needed to get 1.2 tag
   * 
   */
  protected void test_getDeclaredClasses() {

  }

  /**
   * not implemented. <br>
   * code added to test getDeclaringClasses function <br>
   * --> needed to get 1.2 tag
   * 
   */
  protected void test_getDeclaringClasses() {
    th.checkPoint("forName(java.lang.String)java.lang.Class");
    try {
      Class.forName("java/lang/String");
      th
          .fail("string should use dots ( not / )should throw ClassNotFoundException");
    } catch (ClassNotFoundException cnfe) {
      th.check(true);
    }

  }

  /**
   * not implemented <br>
   * code added to test forName function <br>
   * --> needed to get 1.2 tag
   * 
   */
  protected void test_forName2ndMethod() {

  }

  /**
   * not implemented <br>
   * code added to test getProtectionDomain function <br>
   * --> needed to get 1.2 tag
   * 
   */
  protected void test_getProtectionDomain() {

  }

  protected void test_getMethods() {
    th.checkPoint("getMethods()java.lang.reflect.Method[]");
    Method[] methods = Testlet.class.getMethods();
    th.check(methods.length, 1);
    th
        .check(methods[0].toString(),
            "public abstract void gnu.testlet.Testlet.test(gnu.testlet.TestHarness)");

  }

  protected void test_getInterfaces() {
    th.checkPoint("getInterfaces()java.lang.Class[]");
    try {
      Class cl = Class
          .forName("gnu.testlet.wonka.lang.Class.ExHelpGetInterfaces");
      Class[] interfaces = cl.getInterfaces();
      th.check(interfaces.length, 2, "checking length");
      if (interfaces.length == 2) {
        th.check(interfaces[0], java.io.Externalizable.class);
        th.check(interfaces[1], java.io.Serializable.class);
      }
    } catch (Exception e) {
      th.fail("unwanted exception " + e);
    }
  }

  /**
   * code added to test getPackage function <br>
   * --> needed to get 1.2 tag
   * 
   * needs some extra tests
   */
  protected void test_getPackage() {
    th.checkPoint("getPackage()java.lang.Package");

    try {
      Class cl = Class.forName("java.lang.Class");
      Package p = cl.getPackage();
      if (p != null)
        th.check(p.getName().equals("java.lang"), "check Package");
      else {
        th.fail("Package should not be null");
        th.fail("Class belongs to java.lang");
      }
    }

    catch (Exception e) {
      th.fail(e.toString());
    }
  }

  /**
   * implemented. <br>
   * this method will perform a weakened test on Interface classes <br>
   * --> an interface should have only public and interface modifiers <br>
   * but is allowed to be abstract <br>
   */
  protected void test_getModifiers() {
    hc = getClass();
    th.check(hc.getModifiers(), Modifier.PUBLIC, "this class should be public");
    th.check(SMClass.hlpInClass2.class.getModifiers(), Modifier.ABSTRACT
        + Modifier.PROTECTED + Modifier.STATIC,
        "test: modifiers of innerclass2 "
            + Modifier.toString(SMClass.hlpInClass2.class.getModifiers()));

    th.check(
            Cloneable.class.getModifiers()
                & (Modifier.PUBLIC + Modifier.INTERFACE),
            Modifier.PUBLIC + Modifier.INTERFACE,
            "test modifiers of interface Cloneable -- should be public + interface and may be ABSTRACT as on JDK, got");
    th.check(String.class.getModifiers(), Modifier.PUBLIC + Modifier.FINAL,
        "test modifiers of String -- public and final");
    th.check(Class.class.getModifiers(), Modifier.PUBLIC + Modifier.FINAL,
        "test modifiers of Class -- public and final");
    th.check(Object.class.getModifiers(), Modifier.PUBLIC,
        "test modifiers of Object -- public ");
    Class ph = new PrivateHelper().getClass();
    th.check(ph.getModifiers(), Modifier.PRIVATE, "modifiers of PrivateHelper");
  }

  /**
   * this method is strongly related to the java.security package (not <br>
   * implemented yet) --> this test should be written if security manager is
   * <br>
   * installed
   */
  protected void test_getSigners() {

    // write code here

  }

  /**
   * this method checks the nine primitive types, Object and SMClass Classes
   * <br>
   * --> Class.isPrimitive()
   */
  protected void test_isPrimitive() {
    th.checkPoint("isPrimitive()boolean");
    hc = Boolean.TYPE;
    th.check(hc.isPrimitive(), "boolean is a primitive type");
    hc = Character.TYPE;
    th.check(hc.isPrimitive(), "char is a primitive type");
    hc = Byte.TYPE;
    th.check(hc.isPrimitive(), "byte is a primitive type");
    hc = Short.TYPE;
    th.check(hc.isPrimitive(), "short is a primitive type");
    hc = Integer.TYPE;
    th.check(hc.isPrimitive(), "int is a primitive type");
    hc = Long.TYPE;
    th.check(hc.isPrimitive(), "long is a primitive type");
    hc = Float.TYPE;
    th.check(hc.isPrimitive(), "float is a primitive type");
    hc = Double.TYPE;
    th.check(hc.isPrimitive(), "double is a primitive type");
    hc = Void.TYPE;
    th.check(hc.isPrimitive(), "void is a primitive type");
    hc = getClass();
    th.check(!hc.isPrimitive(), "SMClass is not a primitive type");
    hc = Object.class;
    th.check(!hc.isPrimitive(), "Object is not a primitive type");

  }

  /**
   * this method has some extra tests <br>
   * --> Class.newInstance()
   */
  protected void test_newInstance() {
    th.checkPoint("newInstance()java.lang.Object");
    Class cl = SMhlpClass.class;
    try {
      cl.newInstance();
      th.fail("should throw an IllegalAccessException -- private constructor");
    } catch (IllegalAccessException ie) {
      th.check(true, "private constructor");
    } catch (Exception e) {
      th
          .fail("should throw an IllegalAccesException -- private constructor, but got "
              + e);
    }
    cl = Boolean.class;
    try {
      cl.newInstance();
      th
          .fail("should throw an InstantiationException -- no constructor with no args");
    } catch (InstantiationException ie) {
      th.check(true, "no constructor with no args");
    } catch (Exception e) {
      th.fail("should throw an InstantiationException, but got " + e);
    }
    cl = Cloneable.class;
    try {
      cl.newInstance();
      th.fail("should throw an InstantiationException -- interface");
    } catch (InstantiationException ie) {
      th.check(true, "interface");
    } catch (Exception e) {
      th.fail("should throw an InstantiationException -- interface, but got "
          + e);
    }
    cl = Boolean.TYPE;
    try {
      cl.newInstance();
      th.fail("should throw an InstantiationException -- primitive type");
    } catch (InstantiationException ie) {
      th.check(true, "primitive type");
    } catch (Exception e) {
      th
          .fail("should throw an InstantiationException -- primitive type, but got "
              + e);
    }
    cl = new int[4].getClass();
    try {
      cl.newInstance();
      th.fail("should throw an InstantiationException -- array type");
    } catch (InstantiationException ie) {
      th.check(true, "array type");
    } catch (Exception e) {
      th.fail("should throw an InstantiationException -- array type, but got "
          + e);
    }

  }

}
