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


package gnu.testlet.wonka.io.Writer; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.*; // at least the class you are testing ...

/**
*  this file contains test for java.io.Writer   <br>
*
*/
public class SMWriterTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.io.Writer");
       test_Writer();
       test_write();
       test_lock();

     }


/**
* implemented. <br>
*
*/
  public void test_Writer(){
    th.checkPoint("Writer()");
    SMExWriter xw = new SMExWriter();
    th.check( xw == xw.getLock() , "uses itself as lock");

    th.checkPoint("Writer(java.lang.Object)");
    xw = new SMExWriter(this);
    th.check( this == xw.getLock() , "uses this object as lock");
    try {
        new SMExWriter(null);
    	th.fail("should throw NullPointerException");
    	}
    catch(NullPointerException ne) { th.check(true); }	

  }

/**
* implemented. <br>
*
*/
  public void test_write(){
    th.checkPoint("write(int)void");
    SMExWriter xw = new SMExWriter();
    try {
    	xw.write(97);
    	xw.write(98);
    	xw.write(99);
    	xw.write(100);
    	xw.write(101);
    	th.check("abcde".equals(xw.toString()),"checking write");
        }
    catch(Exception e) { th.fail("shouldn't throw any Exception, got:"+e); }

    th.checkPoint("write(char[])void");
    try {
        xw = new SMExWriter();
	xw.write("".toCharArray());
    	th.check( "".equals(xw.toString()),"checking write -- 1");
	xw.write("abcdefgh".toCharArray());
    	th.check( "abcdefgh".equals(xw.toString()),"checking write -- 2");
	xw.write("abcd".toCharArray());
    	th.check( "abcdefghabcd".equals(xw.toString()),"checking write -- 3");
	xw.write("".toCharArray());
    	th.check( "abcdefghabcd".equals(xw.toString()),"checking write -- 4");
        }
    catch(Exception e) { th.fail("shouldn't throw any Exception, got:"+e); }
    try { char []ca = null ;
    	xw.write(ca);
    	th.fail("should throw NullPointerException");
    	}
    catch(NullPointerException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception, got:"+e); }

    th.checkPoint("write(java.lang.String)void");
    try {
        xw = new SMExWriter();
	xw.write("");
    	th.check("".equals(xw.toString()),"checking write -- 1");
	xw.write("abcdefgh");
    	th.check( "abcdefgh".equals(xw.toString()),"checking write -- 2");
	xw.write("abcd");
    	th.check( "abcdefghabcd".equals(xw.toString()),"checking write -- 3");
	xw.write("");
    	th.check( "abcdefghabcd".equals(xw.toString()),"checking write -- 4");
        }
    catch(Exception e) { th.fail("shouldn't throw any Exception, got:"+e); }
    try { String s = null ;
    	xw.write(s);
    	th.fail("should throw NullPointerException");
    	}
    catch(NullPointerException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception, got:"+e); }

    th.checkPoint("write(java.lang.String,int,int)void");
    try {
        xw = new SMExWriter();
	xw.write("",0,0);
//th.debug("length buffer  ="+xw.buffer.length());
    	th.check("".equals(xw.toString()),"checking write -- 1, got:"+xw.toString());
	xw.write("abcdefgh",1,6);
//th.debug("char 0 ="+xw.buffer.charAt(0));
    	th.check( "bcdefg".equals(xw.toString()),"checking write -- 2, got:"+xw.toString());
	xw.write("abcd",4,0);
    	th.check( "bcdefg".equals(xw.toString()),"checking write -- 3, got:"+xw.toString());
	xw.write("a",0,0);
    	th.check( "bcdefg".equals(xw.toString()),"checking write -- 4, got:"+xw.toString());
	xw.write("abcd",2,2);
    	th.check( "bcdefgcd".equals(xw.toString()),"checking write -- 5, got:"+xw.toString());
	xw.write("abcd",0,4);
    	th.check( "bcdefgcdabcd".equals(xw.toString()),"checking write -- 6, got:"+xw.toString());
        }
    catch(Exception e) { th.fail("shouldn't throw any Exception, got:"+e); }
    try { String s = null ;
    	xw.write(s,4,5);
    	th.fail("should throw NullPointerException");
    	}
    catch(NullPointerException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 1, got:"+e); }
    try {
    	xw.write("abcde",4,2);
    	th.fail("should throw an IndexOutOfBoundsException -- 1");
    	}
    catch(IndexOutOfBoundsException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 2, got:"+e); }
    try {
    	xw.write("abcde",-4,2);
    	th.fail("should throw an IndexOutOfBoundsException -- 2");
    	}
    catch(IndexOutOfBoundsException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 3, got:"+e); }
    try {
    	xw.write("abcde",4,-2);
    	th.fail("should throw an IndexOutOfBoundsException -- 3");
    	}
    catch(IndexOutOfBoundsException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 4, got:"+e); }
    try {
    	xw.write("abcde",6,0);
    	th.fail("should throw an IndexOutOfBoundsException -- 4");
    	}
    catch(IndexOutOfBoundsException ne) { th.check(true); }	
    catch(Exception e) { th.fail("shouldn't throw this Exception -- 5, got:"+e); }
}

/**
*   not implemented. <br>
*   not needed
*/
  public void test_lock(){
    th.checkPoint("()");

  }

}
