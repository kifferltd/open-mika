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


package gnu.testlet.wonka.lang.reflect.AccessibleObject;

import gnu.testlet.*;
import java.lang.reflect.*;
import gnu.testlet.wonka.lang.reflect.AccessibleObject.help.hlpclass1;
import gnu.testlet.wonka.lang.reflect.AccessibleObject.help.hlpclass2;

/**
* 	Written by SmartMove.<br>
* <br>
* The AccessibleObject class is the superclass of Field, Method and <br>
* Constructor.
*
* this class has one constructor, but it is only to be used by the JVM <br>
* protected AccessibleObject()<br>
* there are no test on this constructor <br>
*  <br>
* and the following methods: <br>
* - isAccessible()boolean  <br>
* - setAccessible(boolean)void  <br>
* - setAccessible([Accessible, boolean)void <br>
*              <br>
* ATTENTION : as soon as we have a Security Manager, we should <br>
* write test to test SECURITY EXCEPTIONS
*
*/
public class SMAccessibleObject extends hlpclass2 implements Testlet
{

  protected static TestHarness th;
  	
  public void test(TestHarness testharness) {
    th = testharness;
    th.setclass("java.lang.reflect.AccessibleObject");

    th.checkPoint("isAccessible()boolean");
    test_isAccessible();
    test_setAccessible();
    if (setupReflection()){
      th.checkPoint("access on fields");
      test_fieldAccess();
      th.checkPoint("access on methods");
      test_MethodAccess();
      th.checkPoint("access on constructors");
      test_ConstructorAccess();
    }
    if (setupReflection()){
      th.checkPoint("Static access on fields");
      test_staticFieldAccess(this);
      RefTest.main(null);
      th.checkPoint("Static access on methods");
      test_staticMethodAccess(this);
      th.checkPoint("Static access on constructors");
      test_staticConstructorAccess(this);
    }
  }

/**
* Implemented.	<br>
* <br>
* extra test might be needed  <br>
*/
  public void test_isAccessible(){
    Field f = null;
    hlpclass hc = new hlpclass();
    try {
      f = hc.getClass().getDeclaredField("ipu");
      th.check(!f.isAccessible(), "should be Accessible");
      th.check(f.getInt(hc) == 3 , "access granted");
    }
    catch(Exception e) { th.fail("no Exceptions expected");}	
    try {
      f = hc.getClass().getDeclaredField("ipt");
      th.check(!f.isAccessible(), "shouldn't  be Accessible");
    }
    catch(Exception e) { th.fail("no Exceptions expected");}	
    try	{
      th.check(f.getInt(hc) == 3 , "access granted");
    }					
    catch(Exception e) { th.fail("no Exceptions expected");}	

    try {
      f = hc.getClass().getDeclaredField("ipr");
      th.check(!f.isAccessible(), "shouldn't  be Accessible");
    }
    catch(Exception e) { th.fail("no Exceptions expected");}	
    try	{
      f.getInt(hc);
      th.fail("Exception expected");
    }
    catch(IllegalAccessException e) { th.check(true);}	
    try	{
      f.setAccessible(true);
      th.check(f.getInt(hc) == 3 , "access granted");
    }					
    catch(Exception e) { th.fail("no Exceptions expected");}	
  }

/**
* Implemented.	<br>
*
* extra test might be needed
*/
public void test_setAccessible() {
  th.checkPoint("setAccessible(boolean)void");

  hlpclass hc = new hlpclass();
  Field f = null;
  try	{
    f = hc.getClass().getDeclaredField("ipr");
    f.setAccessible(true);
    th.check(  f.isAccessible(), "should be set to true");
    f.setAccessible(true);
    th.check(  f.isAccessible(), "should still be true");
    f.setAccessible(false);
    th.check( !f.isAccessible(), "should be set to false now");
    f.setAccessible(false);
    th.check( !f.isAccessible(), "should still be false");
  }
  catch	(Exception e) { th.fail("no Exception expected");}

  th.checkPoint("setAccessible(java.lang.reflect.AccessibleObject[],boolean)void");
  		
  Field f1 = null, f2 = null;
  try	{
    f1 = hc.getClass().getDeclaredField("ipt");
    f2 = hc.getClass().getDeclaredField("ipu");
    AccessibleObject[] aoa = new AccessibleObject[3];
    aoa[0] = f; aoa[1] = f1 ; aoa[2] = f2 ;
    Field.setAccessible(aoa, true);
    th.check( f.isAccessible()  , "should be set to true 1");
    th.check( f1.isAccessible() , "should be set to true 2");
    th.check( f2.isAccessible() , "should be set to true 3");
    Field.setAccessible(aoa, false);
    th.check(! f.isAccessible()  , "should be set to false 1");
    th.check(! f1.isAccessible() , "should be set to false 2");
    th.check(! f2.isAccessible() , "should be set to false 3");
  }
  catch	(Exception e) { th.fail("no Exception expected");}
}
	
  protected static Field [] flds;
  protected static Field [] flds1;
  protected static Field [] flds2;
  protected static Field [] fldsthis;

  protected static Method [] mtds;
  protected static Method [] mtds1;
  protected static Method [] mtds2;
  protected static Method [] mtdsthis;

  protected static Constructor [] cons;
  protected static Constructor [] cons1;
  protected static Constructor [] cons2;
  protected static Constructor [] consthis;

  private int thisipr = 4;
  public int thisipu = 1;
  protected int thisipt = 2;
  int thisidef = 3;

  public SMAccessibleObject(){}
  protected SMAccessibleObject(int i) {}
  SMAccessibleObject(float f) {}
  private SMAccessibleObject(Object o) {}


  public void publicThisMethod(){}
  protected void protectedThisMethod(){}
  void defaultThisMethod(){}
  private void privateThisMethod(){}


  public boolean setupReflection(){
    try {
      Class [] o = new Class[0];
      Class [] i = new Class[1];
      i[0] = Integer.TYPE;
      Class [] f = new Class[1];
      f[0] = Float.TYPE;
      Class [] b = new Class[1];
      b[0] = Object.class;
    	
      Class c = new hlpclass().getClass();
      flds = new Field[4];
      flds[0] = c.getDeclaredField("ipu");
      flds[1] = c.getDeclaredField("ipt");
      flds[2] = c.getDeclaredField("idef");
      flds[3] = c.getDeclaredField("ipr");
      mtds = new Method[4];
      mtds[0] = c.getDeclaredMethod("publicMethod",o);
      mtds[1] = c.getDeclaredMethod("protectedMethod",o);
      mtds[2] = c.getDeclaredMethod("defaultMethod",o);
      mtds[3] = c.getDeclaredMethod("privateMethod",o);
      cons = new Constructor[4];
      cons[0] = c.getDeclaredConstructor(o);
      cons[1] = c.getDeclaredConstructor(i);
      cons[2] = c.getDeclaredConstructor(f);
      cons[3] = c.getDeclaredConstructor(b);
    	
      c = new hlpclass1().getClass();
      flds1 = new Field[4];
      flds1[0] = c.getDeclaredField("ipu");
      flds1[1] = c.getDeclaredField("ipt");
      flds1[2] = c.getDeclaredField("idef");
      flds1[3] = c.getDeclaredField("ipr");
      mtds1 = new Method[4];
      mtds1[0] = c.getDeclaredMethod("publicMethod",o);
      mtds1[1] = c.getDeclaredMethod("protectedMethod",o);
      mtds1[2] = c.getDeclaredMethod("defaultMethod",o);
      mtds1[3] = c.getDeclaredMethod("privateMethod",o);
      cons1 = new Constructor[4];
      cons1[0] = c.getDeclaredConstructor(o);
      cons1[1] = c.getDeclaredConstructor(i);
      cons1[2] = c.getDeclaredConstructor(f);
      cons1[3] = c.getDeclaredConstructor(b);
    	
      c = new hlpclass2().getClass();
      flds2 = new Field[5];
      flds2[0] = c.getDeclaredField("ipu");
      flds2[1] = c.getDeclaredField("ipt");
      flds2[2] = c.getDeclaredField("idef");
      flds2[3] = c.getDeclaredField("ipr");
      flds2[4] = c.getDeclaredField("ispt");
      mtds2 = new Method[5];
      mtds2[0] = c.getDeclaredMethod("publicMethod",o);
      mtds2[1] = c.getDeclaredMethod("protectedMethod",o);
      mtds2[2] = c.getDeclaredMethod("defaultMethod",o);
      mtds2[3] = c.getDeclaredMethod("privateMethod",o);
      mtds2[4] = c.getDeclaredMethod("protectedStaticMethod",o);
      cons2 = new Constructor[4];
      cons2[0] = c.getDeclaredConstructor(o);
      cons2[1] = c.getDeclaredConstructor(i);
      cons2[2] = c.getDeclaredConstructor(f);
      cons2[3] = c.getDeclaredConstructor(b);
    	
      c = this.getClass();
      fldsthis = new Field[4];
      fldsthis[0] = c.getDeclaredField("thisipu");
      fldsthis[1] = c.getDeclaredField("thisipt");
      fldsthis[2] = c.getDeclaredField("thisidef");
      fldsthis[3] = c.getDeclaredField("thisipr");	
      mtdsthis = new Method[4];
      mtdsthis[0] = c.getDeclaredMethod("publicThisMethod",o);
      mtdsthis[1] = c.getDeclaredMethod("protectedThisMethod",o);
      mtdsthis[2] = c.getDeclaredMethod("defaultThisMethod",o);
      mtdsthis[3] = c.getDeclaredMethod("privateThisMethod",o);
      consthis = new Constructor[4];
      consthis[0] = c.getDeclaredConstructor(o);
      consthis[1] = c.getDeclaredConstructor(i);
      consthis[2] = c.getDeclaredConstructor(f);
      consthis[3] = c.getDeclaredConstructor(b);
      return true;
    }catch(Exception e){
      th.fail("failed to setup reflecftion");
      e.printStackTrace();
      return false;
    }
  }
		
  public void test_fieldAccess(){
      try{
 	th.check(fldsthis[0].getInt(this),1, "access on same class -- 1");
      }
      catch (IllegalAccessException e) { th.check(false);}	
      try{
 	th.check(fldsthis[1].getInt(this), 2, "access on same class -- 2");
      }
      catch (IllegalAccessException e) { th.check(false);}	
      try{
 	th.check(fldsthis[2].getInt(this), 3, "access on same class -- 3");
      }
      catch (IllegalAccessException e) { th.check(false);}	
      try{
 	th.check(fldsthis[3].getInt(this),4, "access on same class -- 4");
      }
      catch (IllegalAccessException e) { th.check(false);}	

      try{
         fldsthis[0].setAccessible(false);
 	th.check(fldsthis[0].getInt(this),1, "access on same class -- 5");
      }
      catch (IllegalAccessException e) { th.check(false);}	

      hlpclass hc = new hlpclass();
      try{
 	th.check(flds[0].getInt(hc),3,"access on class in same package -- 1");
      }
      catch (IllegalAccessException e) { th.check(false);}	
      try{
 	th.check(flds[1].getInt(hc), 3,"access on class in same package -- 2");
      }
      catch (IllegalAccessException e) { th.check(false);}	
      try{
 	th.check(flds[2].getInt(hc), 4,"access on class in same package -- 3");
      }
      catch (IllegalAccessException e) { th.check(false);}	
      try{
 	flds[3].getInt(hc);
 	th.fail("access on class in same package -- 4");
      }
      catch (IllegalAccessException e) { th.check(true);}	
      try{
 	flds[3].setAccessible(true);
 	th.check(flds[3].getInt(hc), 3,"access on class in same package -- 5");
      }
      catch (IllegalAccessException e) { th.check(false);}	

      hlpclass2 hc2 = new hlpclass2();
      try{
 	th.check(flds2[0].getInt(hc2),1,"access on superclass in other package -- 1");
      }
      catch (IllegalAccessException e) { th.check(false);}	
      try{
 	flds2[1].getInt(hc2);
 	th.fail("access on superclass in other package -- 2");
      }
      catch (IllegalAccessException e) { th.check(true);}	
      try{
 	th.check(flds2[1].getInt(this),2,"access on superclass in other package -- 3");
      }
      catch (IllegalAccessException e) { th.check(false,"no exception wanted");}	
      try{
 	flds2[2].getInt(hc2);
 	th.fail("access on superclass in other package -- 4");
      }
      catch (IllegalAccessException e) { th.check(true);}	
      try{
 	flds2[3].getInt(hc2);
 	th.fail("access on superclass in other package -- 5");
      }
      catch (IllegalAccessException e) { th.check(true);}	
      try{
 	flds2[1].setAccessible(true);
 	th.check(flds2[1].getInt(hc2), 2,"access on superclass in other package -- 6");
      }
      catch (IllegalAccessException e) { th.check(false);}	
      try{
 	flds2[2].setAccessible(true);
 	th.check(flds2[2].getInt(hc2), 3,"access on superclass in other package -- 7");
      }
      catch (IllegalAccessException e) { th.check(false);}	
      try{
 	flds2[3].setAccessible(true);
 	th.check(flds2[3].getInt(hc2), 4,"access on superclass in other package -- 8");
      }
      catch (IllegalAccessException e) { th.check(false);}	
      try{
 	flds2[4].getInt(this);
 	th.fail("access on superclass in other package -- 9");
      }
      catch (IllegalAccessException e) { th.check(true,"IllegalAccessException was wanted");}	
      try{
 	flds2[4].getInt(hc2);
 	th.fail("access on superclass in other package -- 10");
      }
      catch (IllegalAccessException e) { th.check(true,"IllegalAccessException was wanted");}	
      try{
         flds2[4].setAccessible(true);
 	th.check(flds2[4].getInt(this), 5,"access on superclass in other package -- 11");
      }
      catch (IllegalAccessException e) { th.check(false,"IllegalAccessException was not wanted");}	


      hlpclass1 hc1 = new hlpclass1();
      try{
 	th.check(flds1[0].getInt(hc1),1,"access on class in other package -- 1");
      }
      catch (IllegalAccessException e) { th.check(false);}	
      try{
         flds1[1].getInt(hc1);
         th.fail("access on class in other package -- 2");
      }
      catch (IllegalAccessException e) { th.check(true);}	
      try{
 	flds1[2].getInt(hc1);
 	th.fail("access on class in other package -- 3");
      }
      catch (IllegalAccessException e) { th.check(true);}	
      try{
 	flds1[3].getInt(hc1);
 	th.fail("access on class in other package -- 4");
      }
      catch (IllegalAccessException e) { th.check(true);}	
      try{
 	flds1[1].setAccessible(true);
 	th.check(flds1[1].getInt(hc1), 2,"access on class in other package -- 5");
      }
      catch (IllegalAccessException e) { th.check(false);}	
      try{
 	flds1[2].setAccessible(true);
 	th.check(flds1[2].getInt(hc1), 3,"access on class in other package -- 6");
      }
      catch (IllegalAccessException e) { th.check(false);}	
      try{
 	flds1[3].setAccessible(true);
 	th.check(flds1[3].getInt(hc1), 4,"access on class in other package -- 7");
      }
      catch (IllegalAccessException e) { th.check(false);}	
  }
 		
  public void test_MethodAccess(){
    Object [] o = new Object[0];
    try {
            th.check( mtdsthis[0].invoke(this,o), null , "access on same class -- 1");
    } catch (IllegalAccessException e) { th.check(false);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
    try {
            th.check( mtdsthis[1].invoke(this,o), null , "access on same class -- 2");
    } catch (IllegalAccessException e) { th.check(false);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
    try {
            th.check( mtdsthis[2].invoke(this,o), null , "access on same class -- 3");
    } catch (IllegalAccessException e) { th.check(false);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
    try {
            th.check( mtdsthis[3].invoke(this,o), null , "access on same class -- 4");
    } catch (IllegalAccessException e) { th.check(false);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
    try {
            mtdsthis[3].setAccessible(false);
            th.check( mtdsthis[3].invoke(this,o), null , "access on same class -- 5");
    } catch (IllegalAccessException e) { th.fail("access on same class -- 5 "+e);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}

    hlpclass hc = new hlpclass();
    try {
            th.check( mtds[0].invoke(hc,o), null , "access on class in same package -- 1");
    } catch (IllegalAccessException e) { th.check(false);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
    try {
            th.check( mtds[1].invoke(hc,o), null , "access on class in same package -- 2");
    } catch (IllegalAccessException e) { th.check(false);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
    try {
            th.check( mtds[2].invoke(hc,o), null , "access on class in same package -- 3");
    } catch (IllegalAccessException e) { th.check(false);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
    try {
            mtds[3].invoke(hc,o);
            th.fail("access on class in same package -- 4");
    } catch (IllegalAccessException e) { th.check(true);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
    try {
            mtds[3].setAccessible(true);
            th.check( mtds[3].invoke(hc,o), null , "access on class in same package -- 5");
    } catch (IllegalAccessException e) { th.fail("access on class in same package -- 5 "+e);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}

    hlpclass2 hc2 = new hlpclass2();
    try {
            th.check( mtds2[0].invoke(hc2,o), null , "access on superclass in other package -- 1");
    } catch (IllegalAccessException e) { th.check(false);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
    try {
            mtds2[1].invoke(hc2,o);
            th.fail("access on superclass in other package -- 2");
    } catch (IllegalAccessException e) { th.check(true);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
    try {
            th.check( mtds2[1].invoke(this,o), null , "access on superclass in other package -- 3");
    } catch (IllegalAccessException e) { th.check(false);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
    try {
            mtds2[2].invoke(hc2,o);
            th.fail("access on superclass in other package -- 4");
    } catch (IllegalAccessException e) { th.check(true);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
    try {
            mtds2[3].invoke(hc2,o);
            th.fail("access on superclass in other package -- 5");
    } catch (IllegalAccessException e) { th.check(true);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
    try {
            mtds2[1].setAccessible(true);
            th.check( mtds2[1].invoke(hc2,o), null , "access on superclass in other package -- 6");
    } catch (IllegalAccessException e) { th.fail("access on superclass in other package -- 6 "+e);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
    try {
            mtds2[2].setAccessible(true);
            th.check( mtds2[2].invoke(hc2,o), null , "access on class in same package -- 7");
    } catch (IllegalAccessException e) { th.fail("access on class in same package -- 7 "+e);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
    try {
            mtds2[3].setAccessible(true);
            th.check( mtds2[3].invoke(hc2,o), null , "access on class in same package -- 8");
    } catch (IllegalAccessException e) { th.fail("access on class in same package -- 8 "+e);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
    try {
            mtds2[4].invoke(null,o);
            th.fail("access on superclass in other package -- 9");
    } catch (IllegalAccessException e) { th.check(true);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
    try {
            mtds2[4].setAccessible(true);
            th.check( mtds2[4].invoke(null,o), null , "access on class in same package -- 10");
    } catch (IllegalAccessException e) { th.fail("access on class in same package -- 10 "+e);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}

    hlpclass1 hc1 = new hlpclass1();
    try {
            th.check( mtds1[0].invoke(hc1,o), null , "access on class in other package -- 1");
    } catch (IllegalAccessException e) { th.check(false);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
    try {
            mtds1[1].invoke(hc1,o);
            th.fail("access on class in other package -- 2");
    } catch (IllegalAccessException e) { th.check(true);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
    try {
            mtds1[2].invoke(hc1,o);
            th.fail("access on class in other package -- 3");
    } catch (IllegalAccessException e) { th.check(true);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
    try {
            mtds1[3].invoke(hc1,o);
            th.fail("access on class in other package -- 4");
    } catch (IllegalAccessException e) { th.check(true);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
    try {
            mtds1[1].setAccessible(true);
            th.check( mtds1[1].invoke(hc1,o), null , "access on class in other package -- 5");
    } catch (IllegalAccessException e) { th.fail("access on class in other package -- 5 "+e);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
    try {
            mtds1[2].setAccessible(true);
            th.check( mtds1[2].invoke(hc1,o), null , "access on class in other package -- 6");
    } catch (IllegalAccessException e) { th.fail("access on class in other package -- 6 "+e);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
    try {
            mtds1[3].setAccessible(true);
            th.check( mtds1[3].invoke(hc1,o), null , "access on class in other package -- 7");
    } catch (IllegalAccessException e) { th.fail("access on class in other package -- 7 "+e);}
    catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
  }
 		
  public void test_ConstructorAccess(){
    Object [] o = new Object[0];
    Object [] i = new Object[1]; i[0] = new Integer(5);
    Object [] f = new Object[1]; f[0] = new Float(5.0f);
    Object [] b = new Object[1]; b[0] = new Object();

    try {
            th.check( consthis[0].newInstance(o).getClass(), this.getClass() , "access on same class -- 1");
    } catch (IllegalAccessException e) { th.check(false);}
    catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
    try {
            th.check( consthis[1].newInstance(i).getClass(), this.getClass() , "access on same class -- 2");
    } catch (IllegalAccessException e) { th.check(false);}
    catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
    try {
            th.check( consthis[2].newInstance(f).getClass(), this.getClass() , "access on same class -- 3");
    } catch (IllegalAccessException e) { th.check(false);}
    catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
    try {
            th.check( consthis[3].newInstance(b).getClass(), this.getClass() , "access on same class -- 4");
    } catch (IllegalAccessException e) { th.check(false);}
    catch(Exception t) { th.check(false,"unwanted Exception: "+t);}

    Object c = new hlpclass();
    try {
            th.check( cons[0].newInstance(o).getClass(), c.getClass() , "access on class in same package -- 1");
    } catch (IllegalAccessException e) { th.check(false);}
    catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
    try {
            th.check( cons[1].newInstance(i).getClass(), c.getClass() , "access on class in same package -- 2");
    } catch (IllegalAccessException e) { th.check(false);}
    catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
    try {
            th.check( cons[2].newInstance(f).getClass(), c.getClass() , "access on class in same package -- 3");
    } catch (IllegalAccessException e) { th.check(false);}
    catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
    try {
            cons[3].newInstance(b);
            th.fail("access on class in same package -- 4");
    } catch (IllegalAccessException e) { th.check(true);}
    catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
    try {
            cons[3].setAccessible(true);
            th.check( cons[3].newInstance(b).getClass(), c.getClass() , "access on class in same package -- 5");
    } catch (IllegalAccessException e) { th.fail("access on class in same package -- 5 "+e);}
    catch(Exception t) { th.check(false,"unwanted Exception: "+t);}

    c = new hlpclass2();
    try {
            th.check( cons2[0].newInstance(o).getClass(), c.getClass() , "access on superclass in other package -- 1");
    } catch (IllegalAccessException e) { th.check(false);}
    catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
    try {
            cons2[1].newInstance(i);
            th.fail("access on superclass in other package -- 2");
    } catch (IllegalAccessException e) { th.check(true);}
    catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
    try {
            cons2[2].newInstance(f);
            th.fail("access on superclass in other package -- 3");
    } catch (IllegalAccessException e) { th.check(true);}
    catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
    try {
            cons2[3].newInstance(b);
            th.fail("access on superclass in other package -- 4");
    } catch (IllegalAccessException e) { th.check(true);}
    catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
    try {
            cons2[1].setAccessible(true);
            th.check( cons2[1].newInstance(i).getClass(), c.getClass() , "access on superclass in other package -- 5");
    } catch (IllegalAccessException e) { th.fail("access on superclass in other package -- 5 "+e);}
    catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
    try {
            cons2[2].setAccessible(true);
            th.check( cons2[2].newInstance(f).getClass(), c.getClass() , "access on superclass in other package -- 6");
    } catch (IllegalAccessException e) { th.fail("access on superclass in other package -- 6 "+e);}
    catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
    try {
            cons2[3].setAccessible(true);
            th.check( cons2[3].newInstance(b).getClass(), c.getClass() , "access on superclass in other package -- 7");
    } catch (IllegalAccessException e) { th.fail("access on superclass in other package -- 7 "+e);}
    catch(Exception t) { th.check(false,"unwanted Exception: "+t);}

    c = new hlpclass1();
    try {
            th.check( cons1[0].newInstance(o).getClass(), c.getClass() , "access on class in other package -- 1");
    } catch (IllegalAccessException e) { th.check(false);}
    catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
    try {
            cons1[1].newInstance(i);
            th.fail("access on class in other package -- 2");
    } catch (IllegalAccessException e) { th.check(true);}
    catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
    try {
            cons1[2].newInstance(f);
            th.fail("access on class in other package -- 3");
    } catch (IllegalAccessException e) { th.check(true);}
    catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
    try {
            cons1[3].newInstance(b);
            th.fail("access on class in other package -- 4");
    } catch (IllegalAccessException e) { th.check(true);}
    catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
    try {
            cons1[1].setAccessible(true);
            th.check( cons1[1].newInstance(i).getClass(), c.getClass() , "access on class in other package -- 5");
    } catch (IllegalAccessException e) { th.fail("access on class in other package -- 5 "+e);}
    catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
    try {
            cons1[2].setAccessible(true);
            th.check( cons1[2].newInstance(f).getClass(), c.getClass() , "access on class in other package -- 6");
    } catch (IllegalAccessException e) { th.fail("access on class in other package -- 6 "+e);}
    catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
    try {
            cons1[3].setAccessible(true);
            th.check( cons1[3].newInstance(b).getClass(), c.getClass() , "access on class in other package -- 7");
    } catch (IllegalAccessException e) { th.fail("access on class in other package -- 7 "+e);}
    catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
  }

		
 public static void test_staticFieldAccess(SMAccessibleObject object){
     try{
	th.check(fldsthis[0].getInt(object),1, "access on same class -- 1");
     }
     catch (IllegalAccessException e) { th.check(false);}	
     try{
	th.check(fldsthis[1].getInt(object), 2, "access on same class -- 2");
     }
     catch (IllegalAccessException e) { th.check(false);}	
     try{
	th.check(fldsthis[2].getInt(object), 3, "access on same class -- 3");
     }
     catch (IllegalAccessException e) { th.check(false);}	
     try{
	th.check(fldsthis[3].getInt(object),4, "access on same class -- 4");
     }
     catch (IllegalAccessException e) { th.check(false);}	

     try{
        fldsthis[0].setAccessible(false);
	th.check(fldsthis[0].getInt(object),1, "access on same class -- 5");
     }
     catch (IllegalAccessException e) { th.check(false);}	

     hlpclass hc = new hlpclass();
     try{
	th.check(flds[0].getInt(hc),3,"access on class in same package -- 1");
     }
     catch (IllegalAccessException e) { th.check(false);}	
     try{
	th.check(flds[1].getInt(hc), 3,"access on class in same package -- 2");
     }
     catch (IllegalAccessException e) { th.check(false);}	
     try{
	th.check(flds[2].getInt(hc), 4,"access on class in same package -- 3");
     }
     catch (IllegalAccessException e) { th.check(false);}	
     try{
	flds[3].getInt(hc);
	th.fail("access on class in same package -- 4");
     }
     catch (IllegalAccessException e) { th.check(true);}	

     hlpclass2 hc2 = new hlpclass2();
     try{
	th.check(flds2[0].getInt(hc2),1,"access on superclass in other package -- 1");
     }
     catch (IllegalAccessException e) { th.check(false);}	
     try{
	flds2[1].getInt(hc2);
	th.fail("access on superclass in other package -- 2");
     }
     catch (IllegalAccessException e) { th.check(true,"exception wanted");}	
     try{
	th.check(flds2[1].getInt(object),object.ipt,"access on superclass in other package -- 3");
     }
     catch (IllegalAccessException e) { th.check(false,"no exception wanted -- 3");}	
     try{
	flds2[2].getInt(hc2);
	th.fail("access on superclass in other package -- 4");
     }
     catch (IllegalAccessException e) { th.check(true);}	
     try{
	flds2[3].getInt(hc2);
	th.fail("access on superclass in other package -- 5");
     }
     catch (IllegalAccessException e) { th.check(true);}	
     try{
	flds2[1].setAccessible(true);
	th.check(flds2[1].getInt(hc2), 2,"access on superclass in other package -- 6");
     }
     catch (IllegalAccessException e) { th.check(false);}	
     try{
	flds2[2].setAccessible(true);
	th.check(flds2[2].getInt(hc2), 3,"access on superclass in other package -- 7");
     }
     catch (IllegalAccessException e) { th.check(false);}	
     try{
	flds2[3].setAccessible(true);
	th.check(flds2[3].getInt(hc2), 4,"access on superclass in other package -- 8");
     }
     catch (IllegalAccessException e) { th.check(false);}	
     try{
	flds2[4].getInt(object);
	th.fail("access on superclass in other package -- 9");
     }
     catch (IllegalAccessException e) { th.check(true,"IllegalAccessException was wanted");}	
     try{
	flds2[4].getInt(hc2);
	th.fail("access on superclass in other package -- 10");
     }
     catch (IllegalAccessException e) { th.check(true,"IllegalAccessException was wanted");}	
     try{
        flds2[4].setAccessible(true);
	th.check(flds2[4].getInt(object), 5,"access on superclass in other package -- 11");
     }
     catch (IllegalAccessException e) { th.check(false,"IllegalAccessException was not wanted");}	


     hlpclass1 hc1 = new hlpclass1();
     try{
	th.check(flds1[0].getInt(hc1),1,"access on class in other package -- 1");
     }
     catch (IllegalAccessException e) { th.check(false);}	
     try{
        flds1[1].getInt(hc1);
        th.fail("access on class in other package -- 2");
     }
     catch (IllegalAccessException e) { th.check(true);}	
     try{
	flds1[2].getInt(hc1);
	th.fail("access on class in other package -- 3");
     }
     catch (IllegalAccessException e) { th.check(true);}	
     try{
	flds1[3].getInt(hc1);
	th.fail("access on class in other package -- 4");
     }
     catch (IllegalAccessException e) { th.check(true);}	
     try{
	flds1[1].setAccessible(true);
	th.check(flds1[1].getInt(hc1), 2,"access on class in other package -- 5");
     }
     catch (IllegalAccessException e) { th.check(false);}	
     try{
	flds1[2].setAccessible(true);
	th.check(flds1[2].getInt(hc1), 3,"access on class in other package -- 6");
     }
     catch (IllegalAccessException e) { th.check(false);}	
     try{
	flds1[3].setAccessible(true);
	th.check(flds1[3].getInt(hc1), 4,"access on class in other package -- 7");
     }
     catch (IllegalAccessException e) { th.check(false);}	
 }
 		
 public static void test_staticMethodAccess(SMAccessibleObject object){
        Object [] o = new Object[0];
        try {
                th.check( mtdsthis[0].invoke(object,o), null , "access on same class -- 1");
        } catch (IllegalAccessException e) { th.check(false);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
        try {
                th.check( mtdsthis[1].invoke(object,o), null , "access on same class -- 2");
        } catch (IllegalAccessException e) { th.check(false);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
        try {
                th.check( mtdsthis[2].invoke(object,o), null , "access on same class -- 3");
        } catch (IllegalAccessException e) { th.check(false);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
        try {
                th.check( mtdsthis[3].invoke(object,o), null , "access on same class -- 4");
        } catch (IllegalAccessException e) { th.check(false);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
        try {
                mtdsthis[3].setAccessible(false);
                th.check( mtdsthis[3].invoke(object,o), null , "access on same class -- 5");
        } catch (IllegalAccessException e) { th.fail("access on same class -- 5 "+e);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}

        hlpclass hc = new hlpclass();
        try {
                th.check( mtds[0].invoke(hc,o), null , "access on class in same package -- 1");
        } catch (IllegalAccessException e) { th.check(false);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
        try {
                th.check( mtds[1].invoke(hc,o), null , "access on class in same package -- 2");
        } catch (IllegalAccessException e) { th.check(false);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
        try {
                th.check( mtds[2].invoke(hc,o), null , "access on class in same package -- 3");
        } catch (IllegalAccessException e) { th.check(false);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
        try {
                mtds[3].invoke(hc,o);
                th.fail("access on class in same package -- 4");
        } catch (IllegalAccessException e) { th.check(true);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
        try {
                mtds[3].setAccessible(true);
                th.check( mtds[3].invoke(hc,o), null , "access on class in same package -- 5");
        } catch (IllegalAccessException e) { th.fail("access on class in same package -- 5 "+e);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}

        hlpclass2 hc2 = new hlpclass2();
        try {
                th.check( mtds2[0].invoke(hc2,o), null , "access on superclass in other package -- 1");
        } catch (IllegalAccessException e) { th.check(false);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
        try {
                mtds2[1].invoke(hc2,o);
                th.fail("access on superclass in other package -- 2");
        } catch (IllegalAccessException e) { th.check(true);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
        try {
                mtds2[1].setAccessible(false);
                th.check(mtds2[1].invoke(object,o), null , "access on superclass in other package -- 3");
        } catch (IllegalAccessException e) { th.check(false,"unwanted IllegalAccessException -- 3");}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException -- 3");}
        try {
                mtds2[2].invoke(hc2,o);
                th.fail("access on superclass in other package -- 4");
        } catch (IllegalAccessException e) { th.check(true);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
        try {
                mtds2[3].invoke(hc2,o);
                th.fail("access on superclass in other package -- 5");
        } catch (IllegalAccessException e) { th.check(true);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
        try {
                mtds2[1].setAccessible(true);
                th.check( mtds2[1].invoke(hc2,o), null , "access on superclass in other package -- 6");
        } catch (IllegalAccessException e) { th.fail("access on superclass in other package -- 6 "+e);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
        try {
                mtds2[2].setAccessible(true);
                th.check( mtds2[2].invoke(hc2,o), null , "access on class in same package -- 7");
        } catch (IllegalAccessException e) { th.fail("access on class in same package -- 7 "+e);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
        try {
                mtds2[3].setAccessible(true);
                th.check( mtds2[3].invoke(hc2,o), null , "access on class in same package -- 8");
        } catch (IllegalAccessException e) { th.fail("access on class in same package -- 8 "+e);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
        try {
                mtds2[4].invoke(null,o);
                th.fail("access on superclass in other package -- 9");
        } catch (IllegalAccessException e) { th.check(true);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
        try {
                mtds2[4].setAccessible(true);
                th.check( mtds2[4].invoke(null,o), null , "access on class in same package -- 10");
        } catch (IllegalAccessException e) { th.fail("access on class in same package -- 10 "+e);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}

        hlpclass1 hc1 = new hlpclass1();
        try {
                th.check( mtds1[0].invoke(hc1,o), null , "access on class in other package -- 1");
        } catch (IllegalAccessException e) { th.check(false);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
        try {
                mtds1[1].invoke(hc1,o);
                th.fail("access on class in other package -- 2");
        } catch (IllegalAccessException e) { th.check(true);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
        try {
                mtds1[2].invoke(hc1,o);
                th.fail("access on class in other package -- 3");
        } catch (IllegalAccessException e) { th.check(true);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
        try {
                mtds1[3].invoke(hc1,o);
                th.fail("access on class in other package -- 4");
        } catch (IllegalAccessException e) { th.check(true);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
        try {
                mtds1[1].setAccessible(true);
                th.check( mtds1[1].invoke(hc1,o), null , "access on class in other package -- 5");
        } catch (IllegalAccessException e) { th.fail("access on class in other package -- 5 "+e);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
        try {
                mtds1[2].setAccessible(true);
                th.check( mtds1[2].invoke(hc1,o), null , "access on class in other package -- 6");
        } catch (IllegalAccessException e) { th.fail("access on class in other package -- 6 "+e);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}
        try {
                mtds1[3].setAccessible(true);
                th.check( mtds1[3].invoke(hc1,o), null , "access on class in other package -- 7");
        } catch (IllegalAccessException e) { th.fail("access on class in other package -- 7 "+e);}
        catch(InvocationTargetException i) { th.check(false,"unwanted InvocationTargetException");}

}
 		
 public void test_staticConstructorAccess(SMAccessibleObject object){
        Object [] o = new Object[0];
        Object [] i = new Object[1]; i[0] = new Integer(5);
        Object [] f = new Object[1]; f[0] = new Float(5.0f);
        Object [] b = new Object[1]; b[0] = new Object();

        try {
                th.check( consthis[0].newInstance(o).getClass(), object.getClass() , "access on same class -- 1");
        } catch (IllegalAccessException e) { th.check(false);}
        catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
        try {
                th.check( consthis[1].newInstance(i).getClass(), object.getClass() , "access on same class -- 2");
        } catch (IllegalAccessException e) { th.check(false);}
        catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
        try {
                th.check( consthis[2].newInstance(f).getClass(), object.getClass() , "access on same class -- 3");
        } catch (IllegalAccessException e) { th.check(false);}
        catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
        try {
                th.check( consthis[3].newInstance(b).getClass(), object.getClass() , "access on same class -- 4");
        } catch (IllegalAccessException e) { th.check(false);}
        catch(Exception t) { th.check(false,"unwanted Exception: "+t);}

        Object c = new hlpclass();
        try {
                th.check( cons[0].newInstance(o).getClass(), c.getClass() , "access on class in same package -- 1");
        } catch (IllegalAccessException e) { th.check(false);}
        catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
        try {
                th.check( cons[1].newInstance(i).getClass(), c.getClass() , "access on class in same package -- 2");
        } catch (IllegalAccessException e) { th.check(false);}
        catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
        try {
                th.check( cons[2].newInstance(f).getClass(), c.getClass() , "access on class in same package -- 3");
        } catch (IllegalAccessException e) { th.check(false);}
        catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
        try {
                cons[3].newInstance(b);
                th.fail("access on class in same package -- 4");
        } catch (IllegalAccessException e) { th.check(true);}
        catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
        try {
                cons[3].setAccessible(true);
                th.check( cons[3].newInstance(b).getClass(), c.getClass() , "access on class in same package -- 5");
        } catch (IllegalAccessException e) { th.fail("access on class in same package -- 5 "+e);}
        catch(Exception t) { th.check(false,"unwanted Exception: "+t);}

        c = new hlpclass2();
        try {
                th.check( cons2[0].newInstance(o).getClass(), c.getClass() , "access on superclass in other package -- 1");
        } catch (IllegalAccessException e) { th.check(false);}
        catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
        try {
                cons2[1].newInstance(i);
                th.fail("access on superclass in other package -- 2");
        } catch (IllegalAccessException e) { th.check(true);}
        catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
        try {
                cons2[2].newInstance(f);
                th.fail("access on superclass in other package -- 3");
        } catch (IllegalAccessException e) { th.check(true);}
        catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
        try {
                cons2[3].newInstance(b);
                th.fail("access on superclass in other package -- 4");
        } catch (IllegalAccessException e) { th.check(true);}
        catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
        try {
                cons2[1].setAccessible(true);
                th.check( cons2[1].newInstance(i).getClass(), c.getClass() , "access on superclass in other package -- 5");
        } catch (IllegalAccessException e) { th.fail("access on superclass in other package -- 5 "+e);}
        catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
        try {
                cons2[2].setAccessible(true);
                th.check( cons2[2].newInstance(f).getClass(), c.getClass() , "access on superclass in other package -- 6");
        } catch (IllegalAccessException e) { th.fail("access on superclass in other package -- 6 "+e);}
        catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
        try {
                cons2[3].setAccessible(true);
                th.check( cons2[3].newInstance(b).getClass(), c.getClass() , "access on superclass in other package -- 7");
        } catch (IllegalAccessException e) { th.fail("access on superclass in other package -- 7 "+e);}
        catch(Exception t) { th.check(false,"unwanted Exception: "+t);}

        c = new hlpclass1();
        try {
                th.check( cons1[0].newInstance(o).getClass(), c.getClass() , "access on class in other package -- 1");
        } catch (IllegalAccessException e) { th.check(false);}
        catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
        try {
                cons1[1].newInstance(i);
                th.fail("access on class in other package -- 2");
        } catch (IllegalAccessException e) { th.check(true);}
        catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
        try {
                cons1[2].newInstance(f);
                th.fail("access on class in other package -- 3");
        } catch (IllegalAccessException e) { th.check(true);}
        catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
        try {
                cons1[3].newInstance(b);
                th.fail("access on class in other package -- 4");
        } catch (IllegalAccessException e) { th.check(true);}
        catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
        try {
                cons1[1].setAccessible(true);
                th.check( cons1[1].newInstance(i).getClass(), c.getClass() , "access on class in other package -- 5");
        } catch (IllegalAccessException e) { th.fail("access on class in other package -- 5 "+e);}
        catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
        try {
                cons1[2].setAccessible(true);
                th.check( cons1[2].newInstance(f).getClass(), c.getClass() , "access on class in other package -- 6");
        } catch (IllegalAccessException e) { th.fail("access on class in other package -- 6 "+e);}
        catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
        try {
                cons1[3].setAccessible(true);
                th.check( cons1[3].newInstance(b).getClass(), c.getClass() , "access on class in other package -- 7");
        } catch (IllegalAccessException e) { th.fail("access on class in other package -- 7 "+e);}
        catch(Exception t) { th.check(false,"unwanted Exception: "+t);}
 }

  public static class RefTest {

    private static final long MyValue = -2767605614048989439L;
    private long value = -2123605614048989439L;

    public static void main(String[] args) {
      try {
        SMAccessibleObject.th.checkPoint("extra tests");
        RefTest rf = new RefTest();
        Class cl = rf.getClass();
        Field myVal = cl.getDeclaredField("MyValue");
        SMAccessibleObject.th.check(myVal.getLong(null),MyValue, "checking static field");

        myVal = cl.getDeclaredField("value");
        SMAccessibleObject.th.check(myVal.getLong(rf),rf.value, "checking instance field");
      }
      catch(Exception e){
        SMAccessibleObject.th.fail("extra test failed due to "+e);
      }
    }
  }
}
