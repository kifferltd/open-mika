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



package gnu.testlet.wonka.lang.Object;

import gnu.testlet.*;

/**
* This file contains testcode for java.lang.Object <br>
*  <br>
* Together with ObjectTest.java it should cover the whole Object class <br>
* <br>
* Methods tested in this file<br>
*  	- notify()      <br>
*	- notifyAll()  <br>
*	- wait()     <br>
*	-finalize()  <br>
*/
public class SMObjectTest implements Testlet
{
	protected TestHarness th;
	
	public void test(TestHarness testharness)
	{
		th = testharness;
		th.setclass("java.lang.Object");
		th.checkPoint("notify()void");
		test_notify();
		th.checkPoint("notifyAll()void");
		test_notifyAll();
		th.checkPoint("wait()void");
		test_wait();
		th.checkPoint("finalize()void");
		test_finalize();

	}

/**
* not implemented.	<br>
* @PARAM not implemtented
*
*/
	protected void test_notify ()
	{


	}

/**
*  not implemented.	<br>
* @PARAM not implemtented
*
*/
	protected void test_notifyAll ()
	{


	}

/**
* not implemented.	<br>
* @PARAM not implemtented
*
*/
	protected void test_wait ()
	{



	}

/**
*  not implemented.	<br>
* @PARAM not implemtented
*
*/

	protected void test_finalize ()
	{



	}










} 