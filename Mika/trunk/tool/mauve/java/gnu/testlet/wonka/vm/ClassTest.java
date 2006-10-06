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



package gnu.testlet.wonka.vm;
import gnu.testlet.*;

public class ClassTest implements Cloneable, java.io.Serializable, Testlet {

  protected TestHarness th;
  public void test(TestHarness harness) {

    th = harness;
    th.setclass("java.lang.Class");
    th.checkPoint("basic class testing ...");
    String s1 = getClass().toString();
    String s2 = (getClass().isInterface() ? "interface " : "class ") + getClass().getName();
    th.check( s1.equals(s2),
          "S1 >>>" + s1 + "<<< S2 >>>" + s2 + "<<<");
    th.check( (new Object()).getClass().toString().equals("class java.lang.Object"));
    th.check( (new java.util.Vector()).getClass().getName().equals("java.util.Vector"));
    th.check( (new Object[3]).getClass().getName().equals("[Ljava.lang.Object;"));
    th.check( (new int[6][7][8]).getClass().getName().equals("[[[I"));
    th.check(!(new Object()).getClass().isInterface());
    th.check(! getClass().isInterface());
    try {
      th.check( Class.forName("java.lang.Cloneable").isInterface());
    }
    catch (Exception e) {
      th.fail("should not throw an Exception -- 1");
    }

    try {
      th.check((new Boolean(true)).getClass().getSuperclass() == Class.forName("java.lang.Object"));
    }
    catch (Exception e) {
      th.fail("should not throw an Exception -- 2");
    }

    th.check((new Object()).getClass().getSuperclass() == null);
    try {	
      Class clss = Class.forName("[[I");
      th.check(clss.getSuperclass() == Class.forName("java.lang.Object"));
    }
    catch ( Exception e ) {
      th.fail("should not throw an Exception -- 3");
    }

    try {	
      Class clss = Class.forName("[D");
      th.check(clss.getSuperclass() == Class.forName("java.lang.Object"));

    }
    catch ( Exception e ){
      th.fail("should not throw an Exception -- 4");
    }

    Class clss[] = getClass().getInterfaces();
    Class clclass = null, clclass1 = null;
    try {
      clclass = Class.forName("java.lang.Cloneable");
      clclass1 = Class.forName("java.io.Serializable");
    }
    catch ( Exception e ) {
      th.fail("should not throw an Exception -- 5");
    }

    th.check(clss != null);
    if ( clss != null) th.check(clss.length == 3);
    if ( clclass != null && clclass1 != null) {
    th.check( (clss[0] == clclass  && clss[1] == clclass1)); }
    try {	
      Class clsss = Class.forName("[[I");
      Class[] ifs = clsss.getInterfaces();
      th.check(ifs.length == 2 ,
        "Length is >>>" + ifs.length + "<<<");
        for (int i = 0; i < ifs.length; i++) {
         th.debug(i + " >>>" + ifs[i].toString() + "<<<");
        }
    }
    catch ( Exception e ) {
      th.fail("should not throw an Exception -- 6");
    }

    try {	
      Class clsss = Class.forName("[D");
      th.check (clsss.getInterfaces().length == 2 );
    }
    catch ( Exception e ) {
      th.fail("should not throw an Exception -- 7");
    }

    Class xclss = getClass();
    Object obj;

    try {
      obj = xclss.newInstance();
      obj = xclss.newInstance();
      obj = xclss.newInstance();
      obj = xclss.newInstance();
    }
    catch ( Exception e ) {
      th.fail("should not throw an Exception -- 8");
    }
    catch ( Error e ) {
      th.fail("should not throw an Error -- 1");
    }

    try {
      obj = Class.forName("java.lang.Object");
      th.check(obj != null , "getting object");
    }
    catch ( Exception e ) {
      th.fail("should not throw an Exception -- 9");
    }

    try {
      Object obj1 = Class.forName("ab.cd.ef");
      th.fail("should throw a ClassNotFoundException");
    }
    catch ( ClassNotFoundException e ) {
    th.check(true);
    }

    try {
      Class obj1 = Class.forName("java.lang.String");
      ClassLoader ldr = obj1.getClassLoader();
      th.check(ldr == null);
    }
    catch (Exception e) {
      th.fail("should not throw an Exception -- 10");
     }
   
    try {

      Class obj1 = Class.forName("java.lang.String");
      th.check(obj1.getComponentType()== null);
      Class obj2 = Class.forName("java.lang.Exception");
      th.check(obj2.getComponentType() == null);
      Class arrclass = Class.forName("[I");
      th.check(arrclass.getComponentType() != null);
      th.check(arrclass.getComponentType().toString().equals("int"));
      arrclass = Class.forName("[[[[I");
      th.check(arrclass.getComponentType() != null);
      th.check( arrclass.getComponentType().toString().equals("class [[[I"));
    }
    catch (Exception e) {
      th.fail("should not throw an Exception -- 11");
    }

    try {
      Class obj1 = Class.forName("java.lang.String");
      th.check( obj1.isInstance("babu")) ;
      Class obj2 = Class.forName("java.lang.Integer");
      th.check( obj2.isInstance(new Integer(10)));
      int arr[]= new int[3];
      Class arrclass = Class.forName("[I");
      th.check( arrclass.isInstance(arr));
      Class cls1 = Class.forName("java.lang.String");
      Class supercls = Class.forName("java.lang.Object"); 
      th.check( supercls.isAssignableFrom(cls1));
      th.check(!cls1.isAssignableFrom(supercls));
      Class cls2 = Class.forName("java.lang.String");
      th.check( cls2.isAssignableFrom( cls1 ));
      arrclass = Class.forName("[I");
      Class arrclass1 = Class.forName("[[[I");
      Class arrclass2 = Class.forName("[[D");
      th.check(arrclass.isArray() && arrclass1.isArray() && arrclass2.isArray());
    }
    catch (Exception e) {
      th.fail("should not throw an Exception -- 11");
    }
  }

}

