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


package gnu.testlet.wonka.io.InputStream; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.*; // at least the class you are testing ...

/**
*  this file contains test for java.io.InputStream   <br>
*
*/
public class SMInputStreamTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.io.InputStream");
       test_read();
       test_skip();
       test_mark();
       test_markSupported();
       test_reset();
       test_available();
       test_close();
     }


/**
* implemented. <br>
*
*/
  public void test_read(){
    th.checkPoint("read()int");
    //abstract method
    th.checkPoint("read(byte[])int");
    byte buffer[] = new byte[10];
    SMExInputStream is = new SMExInputStream();
    try {
    	is.read(buffer);
    	th.check("aaaaaaaaaa".equals(new String(buffer)) , "checking read");
    	}
    catch (Exception e) { th.fail("should not throw an Exception --1, got:"+e); }
    int rc=is.getRC();
    try {
    	is.read(null);
    	th.fail("should throw a NullPointerException");
    	}
    catch (NullPointerException e) { th.check(true); }
    catch (Exception e) { th.fail("should not throw an Exception -- 2, got:"+e); }
    th.check( is.readed(rc) , "read was called --> not expected");	
    th.checkPoint("read(byte[],int,int)int");
    buffer = new byte[10];
    for (int i=0; i < 10; i++)
    { buffer[i] = (byte)'b'; }
    try {
    	is.read(buffer,0,1);
    //th.debug(new String(buffer));
    	is.read(buffer,3,1);
    //th.debug(new String(buffer));
    	is.read(buffer,5,5);
    	th.check("abbabaaaaa".equals(new String(buffer)) , "checking read");
    	}
    catch (Exception e) { th.fail("should not throw an Exception -- 1, got:"+e); }

    rc=is.getRC();
    try {
    	is.read(null,3,1);
        th.fail("should throw a NullpointerException");
        }
    catch (NullPointerException ne) { th.check(true); }
    catch (Exception e) { th.fail("should not throw an Exception -- 2, got:"+e); }
    try {
    	is.read(buffer,-3,1);
        th.fail("should throw an IndexOutOfBoundsException -- 1");
        }
    catch (IndexOutOfBoundsException ne) { th.check(true); }
    catch (Exception e) { th.fail("should not throw this Exception -- 3, got:"+e); }
    try {
    	is.read(buffer,3,8);
        th.fail("should throw an IndexOutOfBoundsException -- 1");
        }
    catch (IndexOutOfBoundsException ne) { th.check(true); }
    catch (Exception e) { th.fail("should not throw this Exception -- 4, got:"+e); }
    try {
    	is.read(buffer,3,-1);
        th.fail("should throw an IndexOutOfBoundsException -- 1");
        }
    catch (IndexOutOfBoundsException ne) { th.check(true); }
    catch (Exception e) { th.fail("should not throw this Exception -- 5, got:"+e); }
    try {
    	is.read(buffer,11,1);
        th.fail("should throw an IndexOutOfBoundsException -- 1");
        }
    catch (IndexOutOfBoundsException ne) { th.check(true); }
    catch (Exception e) { th.fail("should not throw this Exception -- 6, got:"+e); }

    th.check( is.readed(rc) , "read was called --> not expected");	
  }

/**
*   not implemented. <br>
*
*/
  public void test_skip(){
    th.checkPoint("skip(long)long");
    SMExInputStream is = new SMExInputStream();
    try {
    	th.check(is.skip(120L)==120L ,"checking return value -- 1");
    	th.check(is.skip(0x0ffffffff00000004L) , 0L ,"checking return value -- 2");
        //long l = 0x0ffffffff00000004L ;
        //th.debug("l = "+l+", casted l ="+((int)l));
        }
    catch (Exception e) { th.fail("should not throw an Exception, got:"+e); }
    try {
       	th.check(is.skip(-120L)==0L ,"checking return value -- 3");
    	
    	}
    catch (Exception e) { th.fail("should not throw an Exception, got:"+e); }
  }

/**
* implemented. <br>
*
*/
  public void test_mark(){
    th.checkPoint("mark(int)void");
    SMExInputStream is = new SMExInputStream();
    try {
    	is.mark(30);
        th.check( is.read() == 97 );
        }
    catch (Exception e) { th.fail("should not throw an Exception, got:"+e); }

  }

/**
* implemented. <br>
*
*/
  public void test_markSupported(){
    th.checkPoint("markSupported()boolean");
    SMExInputStream is = new SMExInputStream();
    try {
    	th.check(!is.markSupported(), "always returns false -- 1");
    	is.read();
    	is.mark(20);
    	th.check(!is.markSupported(), "always returns false -- 2");
        }
    catch (Exception e) { th.fail("should not throw an Exception, got:"+e); }
  }

/**
* implemented. <br>
*
*/
  public void test_reset(){
    th.checkPoint("reset()void");
    SMExInputStream is = new SMExInputStream();
    try { is.reset();
    	  th.fail("should throw IOException");
        }
    catch (IOException ioe) { th.check(true); }

  }

/**
* implemented. <br>
*
*/
  public void test_available(){
    th.checkPoint("available()int");
    SMExInputStream is = new SMExInputStream();
    try { th.check(is.available()== 0); }
    catch (IOException ioe) { th.fail("should not throw an IOException, got:"+ioe); }
  }

/**
* implemented. <br>
* close does nothing, but we test it to make sure it does nothing
*/
  public void test_close(){
    th.checkPoint("close()void");
    SMExInputStream is = new SMExInputStream();
    try {
    	is.close();
        th.check( is.read() == 97 );
        }
    catch (Exception e) { th.fail("should not throw an Exception, got:"+e); }

  }

}
