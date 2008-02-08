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


package gnu.testlet.wonka.io.OutputStream; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.*; // at least the class you are testing ...

/**
*  this file contains test for java.io.OutputStream   <br>
*
*/
public class SMOutputStreamTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.io.OutputStream");
       test_write();
       test_flush();
       test_close();
     }


/**
*   not implemented. <br>
*
*/
  public void test_write(){
   th.checkPoint("write(int)void");
    // abstract method
   th.checkPoint("write(byte[])void");
    SMExOutputStream os = new SMExOutputStream();
    String s = "abcdefgh";
    try {
    	os.write(s.getBytes());
    	th.check( s.equals(os.toString()) , "checking bytes are written -- 1");
    	os.write(s.getBytes());
    	th.check( os.toString().equals(s+s) , "checking bytes are written -- 1");
    	}
    catch (Exception e) { th.fail("should not throw any Exception -- 1, got:"+e); }	
    try {
    	os.write(null);
    	th.fail("should throw NullPointerException");
    	}
    catch (NullPointerException ne) { th.check(true); }
    catch (Exception e) { th.fail("should not throw this Exception -- 2, got:"+e); }	
    	
   th.checkPoint("write(byte[],int,int)void");
    os = new SMExOutputStream();
    try {
    	os.write(s.getBytes(),0,5);
    //th.debug(os.toString());
    	os.write(s.getBytes(),5,3);
    	th.check( os.toString().equals(s) , "checking bytes are written");
    	}
    catch (Exception e) { th.fail("should not throw any Exception -- 1, got:"+e); }	
    try {
    	os.write(null,4,5);
    	th.fail("should throw NullPointerException");
    	}
    catch (NullPointerException ne) { th.check(true); }
    catch (Exception e) { th.fail("should not throw this Exception -- 2, got:"+e); }	
    try {
    	os.write(s.getBytes(),-4,5);
    	th.fail("should throw IndexOutOfBoundsException -- 1");
    	}
    catch (IndexOutOfBoundsException ne) { th.check(true); }
    catch (Exception e) { th.fail("should not throw this Exception -- 3, got:"+e); }	
    try {
    	os.write(s.getBytes(),4,-5);
    	th.fail("should throw IndexOutOfBoundsException -- 2");
    	}
    catch (IndexOutOfBoundsException ne) { th.check(true); }
    catch (Exception e) { th.fail("should not throw this Exception -- 4, got:"+e); }	
    try {
    	os.write(s.getBytes(),4,5);
    	th.fail("should throw IndexOutOfBoundsException -- 3");
    	}
    catch (IndexOutOfBoundsException ne) { th.check(true); }
    catch (Exception e) { th.fail("should not throw this Exception -- 5, got:"+e); }	
    try {
    	os.write(s.getBytes(),8,1);
    	th.fail("should throw IndexOutOfBoundsException -- 4");
    	}
    catch (IndexOutOfBoundsException ne) { th.check(true); }
    catch (Exception e) { th.fail("should not throw this Exception -- 6, got:"+e); }	

    th.check( os.toString().equals(s) , "checking no extra bytes are written");

  }

/**
* implemented. <br>
* flush does nothing ...
*/
  public void test_flush(){
    th.checkPoint("flush()void");
    SMExOutputStream os = new SMExOutputStream();
    try {
    	os.flush();
    	th.check(true);
    	}
    catch (Exception e)	{ th.fail("flush does nothing --> throwing exceptions not allowed"); }
  }

/**
* implemented. <br>
* close does nothing ...
*/
  public void test_close(){
    th.checkPoint("close()void");
    SMExOutputStream os = new SMExOutputStream();
    try {
    	os.close();
    	th.check(true);
    	}
    catch (Exception e)	{ th.fail("close does nothing --> throwing exceptions not allowed"); }

  }

}
