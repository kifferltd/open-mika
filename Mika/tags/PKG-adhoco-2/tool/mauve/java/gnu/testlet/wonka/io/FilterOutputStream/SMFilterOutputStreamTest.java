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

package gnu.testlet.wonka.io.FilterOutputStream; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.*; // at least the class you are testing ...

/**
*  this file contains test for java.io.FilterOutputStream   <br>
*
*/
public class SMFilterOutputStreamTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.io.FilterOutputStream");
       test_FilterOutputStream();
       test_flush();
       test_write();
       test_close();
     }


/**
* implemented. <br>
*
*/
  public void test_FilterOutputStream(){
    th.checkPoint("FilterOutputStream(java.io.OutputStream)");
    //nothing much to check since null argument are not verified
  }


/**
* implemented. <br>
*
*/
  public void test_flush(){
    th.checkPoint("flush()void");
    SMInfoOutputStream ios = new SMInfoOutputStream();
    FilterOutputStream fos =new FilterOutputStream(ios);
    try {
        fos.flush();
        th.check(ios.isMarked() , "check if flush is called downstream");
    	}
    catch(Exception e) { th.fail("got unexpected Exception:"+e); }
  }

/**
* implemented. <br>
*
*/
  public void test_write(){
    th.checkPoint("write(int)void");
    SMInfoOutputStream ios = new SMInfoOutputStream();
    FilterOutputStream fos =new FilterOutputStream(ios);
    try {
    	fos.write(23);
    	th.check(ios.isMarked() ,"checking write -- 1");
    	th.check(ios.off == 23  ,"checking write -- 2");
    	}
    catch(Exception e) { th.fail("got unexpected Exception:"+e); }
    ios.clean();

    th.checkPoint("write(byte[])void");
    byte[] buf="abcd".getBytes();
    try {
    	fos.write(buf);
    	th.check(ios.isMarked() ,"checking write -- 1");
    	}
    catch(Exception e) { th.fail("got unexpected Exception -- 1, got:"+e); }
    try {
    	ios.clean();
        fos.write(null);
        th.fail("shoild throw a NullPointerException:");
    	}
    catch(NullPointerException e) { th.check(true); }
    catch(Exception e) { th.fail("got unexpected Exception -- 2, got:"+e); }
    th.check(!ios.isMarked() ,"checking marked after failed write -- 1");

    ios.clean();

    th.checkPoint("write(byte[],int,int)void");
    try {
    	fos.write(buf,1,2);
    	th.check(ios.isMarked() ,"checking write -- 1");
    	}
    catch(Exception e) { th.fail("got unexpected Exception -- 1, got:"+e); }
    try {
    	ios.clean();
        fos.write(null,1,2);
        th.fail("shoild throw a NullPointerException:");
    	}
    catch(NullPointerException e) { th.check(true); }
    catch(Exception e) { th.fail("got unexpected Exception -- 2, got:"+e); }
    th.check(!ios.isMarked() ,"checking marked after failed write -- 1");

    try {
    	ios.clean();
	fos.write(buf,-1,2);
    	th.fail("should throw IndexOutOfBoundsException -- 1");
    	}
    catch (IndexOutOfBoundsException ne) { th.check(true); }
    catch (Exception e) { th.fail("should not throw this Exception -- 3, got:"+e); }	
    th.check(!ios.isMarked() ,"checking marked after failed write -- 2");

    try {
    	ios.clean();
	fos.write(buf,3,-2);
    	th.fail("should throw IndexOutOfBoundsException -- 2");
    	}
    catch (IndexOutOfBoundsException ne) { th.check(true); }
    catch (Exception e) { th.fail("should not throw this Exception -- 4, got:"+e); }	
    th.check(!ios.isMarked() ,"checking marked after failed write -- 3");

    try {
    	ios.clean();
	fos.write(buf,5,0);
    	th.fail("should throw IndexOutOfBoundsException -- 3");
    	}
    catch (IndexOutOfBoundsException ne) { th.check(true); }
    catch (Exception e) { th.fail("should not throw this Exception -- 5, got:"+e); }	
    th.check(!ios.isMarked() ,"checking marked after failed write -- 4");

    try {
    	ios.clean();
	fos.write(buf,3,2);
    	th.fail("should throw IndexOutOfBoundsException -- 4");
    	}
    catch (IndexOutOfBoundsException ne) { th.check(true); }
    catch (Exception e) { th.fail("should not throw this Exception -- 6, got:"+e); }	
    th.check(!ios.isMarked() ,"checking marked after failed write -- 5");

  }

/**
* implemented. <br>
*
*/
  public void test_close(){
    th.checkPoint("close()void");
    SMInfoOutputStream ios = new SMInfoOutputStream();
    FilterOutputStream fos =new FilterOutputStream(ios);
    try {
        fos.close();
        th.check(ios.isMarked() , "check if close is called downstream");
    	}
    catch(Exception e) { th.fail("got unexpected Exception:"+e); }

  }
}
