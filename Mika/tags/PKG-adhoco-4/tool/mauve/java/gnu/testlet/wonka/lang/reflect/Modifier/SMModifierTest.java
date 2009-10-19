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


package gnu.testlet.wonka.lang.reflect.Modifier;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
//import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
* this file contains testcode for the java.lang.reflect.Modifier class <br>
* together with toString, these cover the whole class Modifier <br>
*   <br>
* Needs tests on:<br>
* -  <br>
*
*/
public class SMModifierTest implements Testlet
{
	protected TestHarness th;
	
  	public void test (TestHarness harness)
  	{
        	th = harness;
        	th.setclass("java.lang.reflect.Modifier");

  		test_constants();
  		test_isAbstract();
  		test_isFinal();
  		test_isInterface ();
  		test_isNative();
  		test_isPrivate();
  		test_isProtected();
  		test_isPublic();
  		test_isStatic();
  		test_isStrict();
  		test_isSynchronized ();
  		test_isTransient();
  		test_isVolatile();
  		test_toString();
 	
  	}
/**
* implemented <br>
*
* the value of these constants are listed in java class libraries p1144
*/
	protected void test_constants ()
	{
		th.checkPoint("checking modifier constants");
		th.check(Modifier.ABSTRACT == 1024);
		th.check(Modifier.FINAL == 16);
		th.check(Modifier.INTERFACE == 512);
		th.check(Modifier.NATIVE == 256);
		th.check(Modifier.PRIVATE == 2);
		th.check(Modifier.PROTECTED ==4 );
		th.check(Modifier.PUBLIC == 1);
		th.check(Modifier.STATIC == 8);
		th.check(Modifier.STRICT == 2048);
		th.check(Modifier.SYNCHRONIZED ==32 );
		th.check(Modifier.TRANSIENT == 128);
		th.check(Modifier.VOLATILE ==64 );
	}

/**
* implemented.
*
*/
	protected void test_isAbstract()
	{
		th.checkPoint("isAbstract(int)boolean");
		th.check(Modifier.isAbstract(1024) , "its number");
		th.check(Modifier.isAbstract(1096) , "its number is included");
		th.check(!Modifier.isAbstract(24) , "not abstract -- 1");
		th.check(!Modifier.isAbstract(1023) , "not abstract -- 2");
	
	
	}

/**
* implemented.
*
*/
	protected void test_isFinal()
	{
		th.checkPoint("isFinal(int)boolean");
		th.check(Modifier.isFinal(16) , "its number");
		th.check(Modifier.isFinal(48) , "its number is included -- 1");
		th.check(Modifier.isFinal(57) , "its number is included -- 2");
		th.check(!Modifier.isFinal(64) , "not final -- 1");
		th.check(!Modifier.isFinal(15) , "not final -- 2");
		
	}

/**
* implemented.
*
*/
	protected void test_isInterface()
	{
		th.checkPoint("isInterface(int)boolean");
		th.check(Modifier.isInterface(512) , "its number");
		th.check(Modifier.isInterface(540) , "its number is included -- 1");
		th.check(Modifier.isInterface(1600) , "its number is included -- 2");
		th.check(!Modifier.isInterface(511) , "not an Interface -- 1");
		th.check(!Modifier.isInterface(1024) , "not an Interface -- 2");
		
	}

/**
* implemented.
*
*/
	protected void test_isNative()
	{
		th.checkPoint("isNative(int)boolean");
		th.check(Modifier.isNative(256),"its number");
		th.check(Modifier.isNative(435),"its number is included -- 1");
		th.check(Modifier.isNative(780),"its number is included -- 2");
		th.check(!Modifier.isNative(513),"not native -- 1");
		th.check(!Modifier.isNative(255),"not native -- 2");
	}

/**
* implemented.
*
*/
	protected void test_isPrivate()
	{
		th.checkPoint("isPrivate(int)boolean");
		th.check(Modifier.isPrivate(2), "its number");
		th.check(Modifier.isPrivate(6), "its number is included -- 1");
		th.check(Modifier.isPrivate(66),"its number is included -- 2");
		th.check(!Modifier.isPrivate(1), "not private -- 1");
		th.check(!Modifier.isPrivate(5), "not private -- 2");
		
	}

/**
* implemented.
*
*/
	protected void test_isProtected()
	{
		th.checkPoint("isProtected(int)boolean");
		th.check(Modifier.isProtected(4),"its number");
		th.check(Modifier.isProtected(6),"its number is included -- 1");
		th.check(Modifier.isProtected(68),"its number is included -- 2");
		th.check(!Modifier.isProtected(64),"not protected -- 1");
		th.check(!Modifier.isProtected(3),"not protected -- 2");
		
	}

/**
* implemented.
*
*/
	protected void test_isPublic()
	{
		th.checkPoint("isPublic(int)boolean");
		th.check(Modifier.isPublic(1),"its number");
		th.check(Modifier.isPublic(3),"its number is included -- 1");
		th.check(Modifier.isPublic(77),"its number is included -- 2");
		th.check(!Modifier.isPublic(2), "not public -- 1");
		th.check(!Modifier.isPublic(888456), "not public --2 ");
		
	}

/**
* implemented.
*
*/
	protected void test_isStatic()
	{
		th.checkPoint("isStatic(int)boolean");
		th.check(Modifier.isStatic(8),"its number");
		th.check(Modifier.isStatic(14),"its number is included -- 1");
		th.check(Modifier.isStatic(25),"its number is included -- 2");
		th.check(!Modifier.isStatic(7),"not static -- 1");
		th.check(!Modifier.isStatic(17),"not static -- 2");
		
	}

/**
* implemented.
*
*/
	protected void test_isStrict()
	{
		th.checkPoint("isStrict(int)boolean");
		th.check(Modifier.isStrict(2048),"its number");
		th.check(Modifier.isStrict(3111),"its number is included -- 1");
		th.check(Modifier.isStrict(6234),"its number is included -- 2");
		th.check(!Modifier.isStrict(2047),"not strict -- 1");
		th.check(!Modifier.isStrict(5000),"not strict -- 2");
		
	}

/**
* implemented.
*
*/
	protected void test_isSynchronized()
	{
		th.checkPoint("isSynchronized(int)boolean");
		th.check(Modifier.isSynchronized(32),"its number");
		th.check(Modifier.isSynchronized(35), "its number is included -- 1");
		th.check(Modifier.isSynchronized(99), "its number is included -- 2");
		th.check(!Modifier.isSynchronized(65),"not synchronized -- 1");
		th.check(!Modifier.isSynchronized(31),"not synchronized -- 2");
		
	}

/**
* implemented.
*
*/
	protected void test_isTransient()
	{
		th.checkPoint("isTransient(int)boolean");
		th.check(Modifier.isTransient(128),"its number");
		th.check(Modifier.isTransient(231),"its number is included -- 1");
		th.check(Modifier.isTransient(1157),"its number is included -- 2");
		th.check(!Modifier.isTransient(127),"not transient -- 1");
		th.check(!Modifier.isTransient(311),"not transient -- 2");
		
	}

/**
* implemented.
*
*/
	protected void test_isVolatile()
	{
		th.checkPoint("isVolatile(int)boolean");
		th.check(Modifier.isVolatile(64),"its number");
		th.check(Modifier.isVolatile(127),"its number is included -- 1");
		th.check(Modifier.isVolatile(320),"its number is included -- 2");
		th.check(!Modifier.isVolatile(319),"not volatile -- 1");
		th.check(!Modifier.isVolatile(63),"not volatile -- 2");
		
	}

/**
* implemented.
*
*/
	protected void test_toString()
	{
		th.checkPoint("toString(int)java.lang.String");
		th.check(Modifier.toString(2047).equals("public protected private abstract static final transient"+
			" volatile synchronized native interface"),"checking toString order");
		th.check(Modifier.toString(4095).equals("public protected private abstract static final transient"+
			" volatile synchronized native strictfp interface"),"checking toString order");
		System.out.println(Modifier.toString(4095));
	}



}