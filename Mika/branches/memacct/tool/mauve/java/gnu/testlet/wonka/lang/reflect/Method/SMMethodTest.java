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


package gnu.testlet.wonka.lang.reflect.Method;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Array;

/**
* this file contains testcode for the java.lang.reflect.Method class <br>
* together with invoke, these cover the whole class Method <br>
* invoke needs to read, checked and be cleaned up  <br>
* <br>
* Needs tests on:<br>
* -  <br>
*
*/
public class SMMethodTest implements Testlet {
  protected TestHarness th;
  protected Method[] mds, ms;
	
  public  void method1() throws ArrayIndexOutOfBoundsException{};
  private static final double method2(int i, long j,char[] ca, String s) {return 1.0;}
  protected synchronized Object method3(Object[] o) throws NullPointerException, Exception {return o;}
  private final native void method4(String s, byte b);
  public String toString() { return "string";}
  
    public void test (TestHarness harness)
    {
      th = harness;
      th.setclass("java.lang.reflect.Method");
      SetupMethods();              
      test_invoke();
      test_getModifiers();
      test_getDeclaringClass();
      test_getExceptionTypes();
      test_getName();
      test_getParameterTypes();
      test_getReturnType();
      test_equals();
      test_hashCode();
      test_toString();  
    }

  public synchronized final void SetupMethods() {
    try {  
      Method [] hms  = this.getClass().getMethods();
      Method [] hmds = this.getClass().getDeclaredMethods();
      String s[];
      int i,j;
      boolean b;

      int l = hmds.length;
      if (l != 18) {
        th.fail("this.getClass().getDeclaredMethods() returned "+l+" methods, expected 19");
      }
      else {
        mds = new Method[l];
        s= new String[l];

        s[0] = "method1";
        s[1] = "method2";
        s[2] = "method3";
        s[3] = "method4";
        s[4] = "toString";
        s[5] = "test";
        s[6] = "test_invoke";
        s[7] = "test_getDeclaringClass";
        s[8] = "test_getExceptionTypes";
        s[9] = "test_getModifiers";
        s[10] = "test_getName";
        s[11] = "test_getParameterTypes";
        s[12] = "test_getReturnType";
        s[13] = "test_hashCode";
        s[14] = "test_equals";
        s[15] = "test_toString";
        s[16] = "SetupMethods";
        s[17] = "hashCode";
  
        for (i=0 ; i < l ; i++) {
          b=false;
          for (j=0 ; j<l ; j++) {
            try {  
              if (s[i].equals(hmds[j].getName())) {
                b=true; mds[i] = hmds[j]; 
              }
            }
            catch (Exception e) {
              th.fail("Sorting declared methods, i = "+i+", j = "+j+", comparing s[i] ("+s[i]+") with "+hmds[j].getName()+" threw "+e);
            }
          }
          if (!b) {
            th.debug("couldn't find declared method "+s[i]);
          }
        }
      }
     
      l = hms.length;
      if (l != 12) {
        th.fail("this.getClass().getMethods() returned "+l+" methods, expected 12");
      }
      else {
        s = new String[l];
        s[0] = "equals";
        s[1] = "getClass";
        s[2] = "hashCode";
        s[3] = "notify";
        s[4] = "notifyAll";
        s[5] = "toString";
        s[6] = "wait()";
        s[7] = "wait(long)";
        s[8] = "wait(long,int)";
        s[9] = "method1";
        s[10] = "test";
        s[11] = "SetupMethods";
        ms = new Method[l];

        for (i=0 ; i < l ; i++) {
          b=false;
          for  (j=0 ; j<l ; j++) {
            try {  
              if (hms[j].toString().lastIndexOf(s[i]) > 0)  {
                b=true;  ms[i] = hms[j]; 
              }
            }
            catch (Exception e) {
              th.fail("Sorting public methods, i = "+i+", j = "+j+", comparing s[i] ("+s[i]+") with hms[j].getName() threw "+e);
            }
          }
          if (!b) {
            th.debug("couldn't find public method "+s[i]);
          }
        }    
      }
    }
    catch (Exception e) {
      th.fail("caught exception during setup, got:"+e);
    }         
  }

/**
* not implemented.  <br>
* --> tests performed in file invoke.java
*/
 	
 	public int hashCode(){
 	  th.check(true);
 	  return -1;
 	}
 	
 	protected void test_invoke(){
    th.checkPoint("invoke(java.lang.Object,java.lang.Object[])java.lang.Object");
 		try {
 		  Method m = Object.class.getMethod("hashCode", null);
 		  th.check(m.invoke(this, new Object[0]), new Integer(-1));
 		}
 		catch(Exception e){
 		  e.printStackTrace();
 		  th.fail("caught unwanted exception "+e);
 		}
 		try {
 		  Method m = Object.class.getMethod("hashCode", new Class[0]);
 		  th.check(m.invoke(this, null), new Integer(-1));
 		}
 		catch(Exception e){
 		  e.printStackTrace();
 		  th.fail("caught unwanted exception "+e);
 		}
 		try {
 		  Class list = Class.forName("java.util.List");
 		  Method add = list.getMethod("add",new Class[]{new Object().getClass()});
 		  java.util.Vector v = new java.util.Vector();
 		  try {
   		  Boolean b = (Boolean)add.invoke(v, new Object[]{this});
 	  	  th.check(b.booleanValue(), "checking return value -- 1");
 		    th.check(v.contains(this), "checking result -- 1");		
 		  }
   		catch(Exception e){
 	  	  e.printStackTrace();
 		    th.fail("caught unwanted exception "+e);
 		  }
 		  v.add(this);
 		  Class alist = Class.forName("java.util.AbstractList");
 		  Method get = list.getMethod("get",new Class[]{Integer.TYPE});
 		  try {
   		  th.check (get.invoke(v, new Object[]{new Integer(0)}), this , "checking return value -- 2");
 		  }
   		catch(Exception e){
 	  	  e.printStackTrace();
 		    th.fail("caught unwanted exception "+e);
 		  }
 		}
 		catch(Exception e){
 		  e.printStackTrace();
 		  th.fail("caught unwanted exception "+e);
 		}
 	} 	

/**
* implemented.
*
*/
 	protected void test_getDeclaringClass()
 	{
 		th.checkPoint("getDeclaringClass()java.lang.Class");
 		th.check(mds[0].getDeclaringClass() == this.getClass()		,"test -- 1");
 		th.check(mds[1].getDeclaringClass() == this.getClass()		,"test -- 2");
 		th.check(mds[2].getDeclaringClass() == this.getClass()		,"test -- 3");
 		th.check(ms[10].getDeclaringClass() == this.getClass()		,"test -- 4");
 		th.check( ms[1].getDeclaringClass() == new Object().getClass() 	,"test -- 5, got:"+
 			ms[1].getDeclaringClass()+"declared ??? :"+ms[1]);
 		th.check( ms[4].getDeclaringClass() == new Object().getClass() 	,"test -- 6");
 		
 	}
 	 	
/**
* implemented.
*
*/
 	protected void test_getExceptionTypes()
 	{
 		th.checkPoint("getExceptionTypes()java.lang.Class[]");
 		Class[] hca = mds[1].getExceptionTypes();
 		th.check( hca != null , "getExceptionTypes gives a non Null value");
 		th.check( hca.length == 0 , "No exceptions thrown by this method");
 		hca = mds[2].getExceptionTypes();
 		th.check(hca.length == 2 , "2 exceptions thrown by this method");
 		th.check(hca[0] == new NullPointerException().getClass(),"checking type -- 1, got"+hca[0]);
		th.check(hca[1] == new Exception().getClass()		,"checking type -- 2, got"+hca[1]);
 	}
 	 	
/**
* implemented.
*
*/
 	protected void test_getModifiers()
 	{
 		th.checkPoint("getModifiers()int");
 		th.check( mds[0].getModifiers() , Modifier.PUBLIC					, "checking Modifiers -- 1");
  	th.check( mds[1].getModifiers() , Modifier.PRIVATE + Modifier.STATIC + Modifier.FINAL	, "checking Modifiers -- 2");
 		th.check( mds[2].getModifiers() , Modifier.PROTECTED + Modifier.SYNCHRONIZED		, "checking Modifiers -- 3");
 		th.check( mds[3].getModifiers() , Modifier.PRIVATE + Modifier.NATIVE +Modifier.FINAL , "checking Modifiers -- 4");
 		th.check( ms[11].getModifiers() , Modifier.PUBLIC + Modifier.FINAL + Modifier.SYNCHRONIZED					,
 		 "checking Modifiers -- 5, got:"+Modifier.toString(ms[0].getModifiers())+" from method "+ms[0].getName());
 		th.check(  ms[1].getModifiers() , Modifier.PUBLIC + Modifier.NATIVE + Modifier.FINAL	, "checking Modifiers -- 6");
 		th.check(  ms[2].getModifiers() , Modifier.PUBLIC , "checking Modifiers -- 7");
 	}
 	 	
/**
* implemented.
*
*/
 	protected void test_getName()
 	{
 		th.checkPoint("getName()java.lang.String");
 		th.check( mds[0].getName().equals("method1")  , "checking names -- 1");
 		th.check( mds[1].getName().equals("method2")  , "checking names -- 2");
 		th.check( mds[2].getName().equals("method3")  , "checking names -- 3");
 		th.check( mds[3].getName().equals("method4")  , "checking names -- 4");
 		th.check(  ms[0].getName().equals("equals")   , "checking names -- 5");
 		th.check(  ms[1].getName().equals("getClass") , "checking names -- 6");
 		th.check(  ms[2].getName().equals("hashCode") , "checking names -- 7");	
 	}
 	 	
/**
* implemented.
*
*/
 	protected void test_getParameterTypes()
 	{
 		th.checkPoint("getParameterTypes()java.lang.Class[]");
 		Class[] hca = mds[0].getParameterTypes();
 		th.check( hca != null     , "getParameterTypes gives a non Null value");
 		th.check( hca.length == 0  , "No parameters taken by this method -- 1");
 		hca = mds[1].getParameterTypes();
 		th.check( hca.length == 4  , " 4 parameters taken by this method -- 2");
 		th.check( hca[0] == Integer.TYPE 	   ,"checking Class type -- 3, got:"+hca[0]+" from "+mds[1]);
 		th.check( hca[1] == Long.TYPE		   ,"checking Class type -- 4, got:"+hca[1]+" from "+mds[1]);
 		th.check( hca[2] , (new char[1]).getClass() ,"checking Class type -- 5, got:"+hca[2]+" from "+mds[1]);
    th.debug(""+new char[1].getClass());
    th.debug(""+new char[2].getClass());
    th.debug(""+new char[3].getClass());
    th.debug(""+new char[4].getClass());
    th.debug(""+new char[5].getClass());



 /*		th.check( hca[3] == "a".getClass()	   ,"checking Class type -- 6, got:"+hca[3]+" from "+mds[1]);
 		hca = ms[0].getParameterTypes();
 		th.check( hca.length == 1  , " 1 parameter  taken by this method -- 7");
 		th.check( hca[0] == new Object().getClass(),"checking Class type -- 7, got:"+hca[0]+" from "+ms[0]);
 		hca = mds[3].getParameterTypes();
 		th.check( hca.length == 2  , " 2 parameters taken by this method -- 4");
 		th.check( hca[1] == Byte.TYPE		   ,"checking Class type -- 8, got:"+hca[1]+" from "+mds[3]);
  		th.check( hca[0] == "a".getClass()	   ,"checking Class type -- 9, got:"+hca[0]+" from "+mds[3]);
   */
 	}
 		
/**
* implemented.
*
*/
 	protected void test_getReturnType()
 	{
 		th.checkPoint("getReturnType()java.lang.Class");
 		th.check( mds[0].getReturnType() == Void.TYPE 		   ,"checking Class type -- 1");
 		th.check( mds[1].getReturnType() == Double.TYPE 	   ,"checking Class type -- 2");
 		th.check( mds[2].getReturnType() == new Object().getClass(),"checking Class type -- 3");
 		th.check(  ms[0].getReturnType() == Boolean.TYPE	   ,"checking Class type -- 4");
 		th.check(  ms[2].getReturnType() == Integer.TYPE	   ,"checking Class type -- 5");
 	}
 		
/**
* implemented.
*
*/
 	protected void test_hashCode()
 	{
 		th.checkPoint("hashCode()int");
 		int j = mds[0].getName().hashCode();
 		int h = mds[0].getDeclaringClass().getName().hashCode();
 		j = h ^ j ;
 		h = mds[0].hashCode();
 		th.check( h == j , "checking hashcode algorithm, got:"+h+", but expected "+j);
 	} 	
 	
/**
* implemented.
*
*/
 	protected void test_equals()
 	{
 		th.checkPoint("equals(java.lang.Object)boolean");
 		Method[] hom = new Object().getClass().getMethods();
 		int l = hom.length;
 		Method [] om = new Method[2];
 		String s[]= new String[l];
 		        s[0] = "toString";
 		        s[1] = "equals";
 		int i,j;
 		boolean b=false;
 		for (i=0 ; i < 2 ; i++) {
 		 	b=false;
 		  	for  (j=0 ; j<l ; j++) {
 		   		if (s[i].equals(hom[j].getName()))
 		              		{b=true; om[i] = hom[j]; }		
 		            	}
 		            	if (!b) th.debug("couldn't find "+s[i]);
 		        }
 		th.check(! om[0].equals(mds[5]) ,  "overwritten Methods are different");
 		th.check(! mds[0].equals(mds[1]) , "different Methods are different");
 		th.check( ms[5].equals(ms[5]) ,  "same Method is equal");
 		th.check( om[1].equals(ms[0]) , "inherited Methods are the same");
 		try { mds[0].equals(null); th.check(true); }
 		catch (Exception e) { th.fail("shouldn't throw exceptions -- "+e); }
 	
 	}
 		
/**
* implemented.
*
*/
 	protected void test_toString()
 	{
 		th.checkPoint("toString()java.lang.String");
 		th.check(mds[0].toString().equals("public void gnu.testlet.wonka.lang.reflect.Method.SMMethodTest.method1() throws java.lang.ArrayIndexOutOfBoundsException"));
		th.check(mds[1].toString().equals("private static final double "+
		"gnu.testlet.wonka.lang.reflect.Method.SMMethodTest.method2(int,long,char[],java.lang.String)"),
			"got:"+mds[1]);
		th.check(mds[2].toString().equals("protected synchronized java.lang.Object gnu.testlet.wonka.lang.reflect."+
		"Method.SMMethodTest.method3(java.lang.Object[]) throws java.lang.NullPointerException,java.lang.Exception"),
			"got:"+mds[2]);
		th.check(mds[3].toString().equals("private final native void gnu.testlet.wonka.lang.reflect.Method.SMMethodTest."+
		"method4(java.lang.String,byte)"), "expected 'private native void gnu.testlet.wonka.lang.reflect.Method.SMMethodTest.method4(java.lang.String,byte)', but got '" + mds[3].toString() + "'");
		th.check(mds[16].toString().equals("public final synchronized void "+
		"gnu.testlet.wonka.lang.reflect.Method.SMMethodTest.SetupMethods()"),
			"got:"+mds[16]);
 	
 	} 	
 	
}
