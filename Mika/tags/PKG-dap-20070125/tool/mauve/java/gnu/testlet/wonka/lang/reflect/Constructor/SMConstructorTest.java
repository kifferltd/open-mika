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


package gnu.testlet.wonka.lang.reflect.Constructor;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.String;

/**
* this file contains testcode for the java.lang.reflect.Constructor class <br>
* in Mauve there is a class newInstance() which performs test on the  <br>
* newInstance() of Constructor.  The testcode is not cleaned up, but will be <br>
* used to perform tests on wonka <br>
* <br>
* Needs tests on:   <br>
* - newInstance
*
*/
public class SMConstructorTest implements Testlet
{
	protected TestHarness th;

	public SMConstructorTest ()throws NullPointerException, IllegalArgumentException, Exception { }		
	private SMConstructorTest (int i)	{ }
	protected SMConstructorTest (long l)	{ }
	public SMConstructorTest (Object o)	{ }
	SMConstructorTest(Object[] o, int k, byte[] b)throws RuntimeException	{ }

  	public void test (TestHarness harness){
		th = harness;
  	th.setclass("java.lang.reflect.Constructor");

		test_newInstance();
		test_getDeclaringClass ();
		test_getExceptionTypes ();
		test_getModifiers ();
		test_getName ();
		test_getParameterTypes ();
		test_equals ();
		test_hashcode ();
		test_toString ();
	}
/**
*not implemented.
*
*/
	protected void test_newInstance(){
	
  }
/**
* implemented.	<br>
* --> might need extra testing
*/
	protected void test_getDeclaringClass ()
	{
		th.checkPoint("getDeclaringClass()Ljava.lang.Class");
		Constructor c=null;
		
		try   {	c.getDeclaringClass();
			th.fail("should throw a NullPointerException");
		      }
		catch (NullPointerException ne) { th.check(true); }
		try   { c = this.getClass().getConstructor(new Class[0]);
			th.check(c.getDeclaringClass().isInstance(this),
				"Classes are the same");
		      }
		catch (	Exception e )	{ th.fail("no exceptions expected"); }
		
        }
/**
* implemented.
*
*/

	protected void test_getExceptionTypes ()
	{
		th.checkPoint("getExceptionTypes()java.lang.Class[]");
		Constructor c=null;
		
		try   {	c.getExceptionTypes();
			th.fail("should throw a NullPointerException");
		      }
		catch (NullPointerException ne) { th.check(true); }
		try   { c = this.getClass().getConstructor(null);
			Class [] clh = c.getExceptionTypes();
			th.check(clh.length == 3, "this constructor throws 3 exceptions");
			th.check(clh[0] == (new NullPointerException().getClass()),
				"clh[0] is a NullPointerException");
			th.check(clh[1] == (new IllegalArgumentException().getClass()),
				"clh[1] is an IllegalArgumentException");
			th.check(clh[2] == (new Exception().getClass()),
				"clh[2] is an Exception");
			clh = new Class[1];  clh[0] = (new Object()).getClass();
			c = this.getClass().getConstructor(clh);
			clh = c.getExceptionTypes();
			th.check( clh.length == 0, "this constructor doesn't throw Exceptions");

		      }
		catch (	Exception e )	{ th.fail("no exceptions expected"); }


        }
/**
* implemented.	<br>
* --> problems might occur if getDeclaredConstructors alters the order of
*     constructors
*/
  protected void test_getModifiers ()
  {
    th.checkPoint("getModifiers()int");    
    Constructor c;
    Constructor[] ca;

    try {
      ca = this.getClass().getDeclaredConstructors();
      if (ca == null) {
        th.fail("this.getClass().getDeclaredConstructors() returned null");
      }
      else {
        int publics = 0;
        int privates = 0;
        int protecteds = 0;
        int defaults = 0;
        if ( ca.length == 5 ) {
          for (int i = 0; i < 5; ++i) {
            if (ca[i].getModifiers() == Modifier.PUBLIC) {
              ++publics;
            }
            if (ca[i].getModifiers() == Modifier.PRIVATE) {
              ++privates;
            }
            if (ca[i].getModifiers() == Modifier.PROTECTED) {
              ++protecteds;
            }
            if (ca[i].getModifiers() == 0) {
              ++defaults;
            }
          }
          th.check(publics, 2, "should be 2 public constructors, got "+publics);  
          th.check(privates, 1, "should be 1 public constructor, got "+privates);  
          th.check(protecteds, 1, "should be 1 protected constructor, got "+protecteds);  
          th.check(defaults, 1, "should be 1 default constructor, got "+defaults);  
        }
        else  { 
          th.fail("Class.getDeclaredConstructors() failed found "+ca.length+" constructors, 5 expected"); 
        }
      }
    }
    catch (  Exception e )  {
      th.fail("no exceptions expected, got "+e+" (1)");
      e.printStackTrace();
    }

    try {
      ca = new Object().getClass().getConstructors();
      th.check(ca != null, "new Object().getClass().getConstructors() returned null");
      th.check(ca.length, 1, "new Object().getClass().getConstructors() should return 1 constructor, got "+ca.length);
      th.check(ca[0].getModifiers() == Modifier.PUBLIC, "new Object().getClass().getConstructors() should yield 1 PUBLIC constructor, but getModifiers() returned "+ca[0].getModifiers());
    }
    catch (  Exception e )  {
      th.fail("no exceptions expected, got "+e+" (2)");
      e.printStackTrace();
    }

  }
/**
* implemented.
*
*/
  protected void test_getName ()
  {
    th.checkPoint("getName()java.lang.String");
    Constructor c;
    try   { c = this.getClass().getConstructor(new Class[0]);

      th.check(c.getName().equals("gnu.testlet.wonka.lang.reflect.Constructor.SMConstructorTest"),
         "wrong constructor name");
      
// Debug Info if needed: System.out.println(c.getName());

      Constructor[] ca = this.getClass().getDeclaredConstructors();
      for (int i=0; i < 4 ; i++)        
      {  th.check(ca[i].getName().equals("gnu.testlet.wonka.lang.reflect.Constructor.SMConstructorTest"),
         "wrong constructor name -- "+(i+1)); }
      ca = new Object().getClass().getConstructors();
      th.check(ca[0].getName().equals("java.lang.Object"),
        "wrong name for Object Constructor");

          }
    catch (  Exception e )  { th.fail("no exceptions expected"); }
    

        }
/**
* implemented.
*
*/
  protected void test_getParameterTypes ()
	{
		th.checkPoint("getParameterTypes()java.lang.Class[]");
		Constructor c=null;
		try   {	c.getParameterTypes();
			th.fail("should throw a NullPointerException");
		      }
		catch (NullPointerException ne) { th.check(true); }
		try   { c = this.getClass().getConstructor(null);
			Class[] clh;
			clh =c.getParameterTypes();
			if (clh != null)
				th.check(clh.length == 0,"Constructor has no parameters");
			else th.fail("getParameterTypes should not return null");

			Constructor[] ca = this.getClass().getDeclaredConstructors();
			clh = ca[1].getParameterTypes();
			th.check(clh.length == 1 , "Constructor takes only one Parameter -- 1");
			th.check(clh[0] == Integer.TYPE, "parameter is an int");			
			clh = ca[2].getParameterTypes();
			th.check(clh.length == 1 , "Constructor takes only one Parameter -- 1");
			th.check(clh[0] == Long.TYPE, "parameter is a long");			
			clh = ca[3].getParameterTypes();
			th.check(clh.length == 1 , "Constructor takes only one Parameter -- 1");
			th.check(clh[0] == new Object().getClass() , "parameter is an Object");			

			clh = new Class[4];
			clh [0] = (new byte [1]).getClass();
			clh [2] = clh [1] = Integer.TYPE;	
			clh [3] = "a".getClass();
	//		for (int i=0;i<4;i++){System.out.println(clh[i]);}

			c = "a".getClass().getConstructor(clh);
			Class[] clh1 = c.getParameterTypes();
			th.check( clh1.length == 4 , "array should have length  1" );
			th.check( clh1[0] ==  (new byte [1]).getClass() , "parameter is a String");
			th.check( clh1[1] == Integer.TYPE , "parameter is a String");
			th.check( clh1[2] == Integer.TYPE , "parameter is a String");
			th.check( clh1[3] == "a".getClass() , "parameter is a String");
		      }
		catch (	Exception e )	{ th.fail("no exceptions expected caught "+e); }
		

        }
/**
* implemented.
*
*/
	protected void test_equals ()
	{
		th.checkPoint("equals(java.lang.Object)boolean");
		Constructor c1=null, c2=null;
		try   {
		  c1 = this.getClass().getConstructor(new Class[0]);
			th.check(! c1.equals(c2), "should return false");
			c2 = this.getClass().getConstructor(null);
			th.check(c2.equals(c1), "constructors are equal");
			c2 = (new Object()).getClass().getConstructor(new Class[0]);
			th.check(!c2.equals(c1), "constructors are not equal -- 1");
			Class[] clh = new Class[1]; clh[0] = (new Object()).getClass();
			c2 = this.getClass().getConstructor(clh);
			th.check(!c2.equals(c1), "constructors are not equal -- 2");
			
		}
		catch (Exception e) {
		  th.fail("no exception expected "+e);	
		  e.printStackTrace();	
		}

  }
/**
* implemented.	<br>
*--> should lookup hashcode Algorithm
*/
	protected void test_hashcode ()
	{
         	th.checkPoint("hashCode()int");
		Constructor c=null;
		try   {	c.hashCode();
			th.fail("should throw NullPointerException");
		      }
		catch (	NullPointerException ne) { th.check(true); }
         	try   { Constructor[] ca = this.getClass().getDeclaredConstructors();
                	int h = ca[0].hashCode();
                	int j = ca[0].getDeclaringClass().getName().hashCode();
                	th.check( h == j , "checking hashCode Algorithm" );
                	th.check( ca[0].hashCode() == h , "hashCodes are Equal -- 1");
                	th.check( ca[0].hashCode() == ca[1].hashCode() , "hashCodes are Equal -- 2");
         	      }	
 		catch (	Exception e )	{ th.fail("no exceptions expected caught "+e); }
        }
/**
* implemented.
*
*/
	protected void test_toString ()
	{
		th.checkPoint("toString()java.lang.String");
		Constructor c=null;
		try   {	c.getParameterTypes();
			th.fail("should throw a NullPointerException");
		      }
		catch (NullPointerException ne) { th.check(true); }
		try   { c = new Object().getClass().getConstructor(null);
			th.check( c.toString().equals("public java.lang.Object()"));
			Constructor[] ca = this.getClass().getDeclaredConstructors();
			th.check(ca[0].toString(),"public gnu.testlet.wonka.lang.reflect.Constructor.SMConstructorTest()"+
			 " throws java.lang.NullPointerException,java.lang.IllegalArgumentException,java.lang.Exception");
			th.check(ca[1].toString(),"private gnu.testlet.wonka.lang.reflect.Constructor.SMConstructorTest(int)");
			th.check(ca[2].toString(),"protected gnu.testlet.wonka.lang.reflect.Constructor.SMConstructorTest(long)");
			th.check(ca[3].toString(),"public gnu.testlet.wonka.lang.reflect.Constructor.SMConstructorTest(java.lang.Object)");
			th.check(ca[4].toString(),"gnu.testlet.wonka.lang.reflect.Constructor.SMConstructorTest"+
			                          "(java.lang.Object[],int,byte[]) throws java.lang.RuntimeException");
						
			Class[] clh = new Class[4];
			clh [0] = (new byte [1]).getClass();
			clh [2] = clh [1] = Integer.TYPE;	
			clh [3] = "a".getClass();
			c = "a".getClass().getConstructor(clh);
			th.check(c.toString(),"public java.lang.String(byte[],int,int,java.lang.String) throws java.io.UnsupportedEncodingException");
//	System.out.println(c);
		      }
		catch (	Exception e )	{
		  th.fail("no exceptions expected caught "+e);
		  e.printStackTrace();
		}
		

        }



}  
