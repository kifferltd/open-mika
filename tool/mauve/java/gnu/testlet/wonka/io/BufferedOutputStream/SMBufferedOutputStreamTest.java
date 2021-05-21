/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
* Small modifications by Chris Gray 2011.                                 *
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



package gnu.testlet.wonka.io.BufferedOutputStream; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.*; // at least the class you are testing ...

/**
*  this file contains test for java.io.BufferedOutputStream   <br>
*
*/
public class SMBufferedOutputStreamTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.io.BufferedOutputStream");
       test_BufferedOutputStream();
       test_write();
       test_flush();
       test_flushplus();
     }


/**
* implemented. <br>
*
*/
  public void test_BufferedOutputStream(){
    th.checkPoint("BufferedOutputStream(java.io.OutputStream)");
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    SMExBufferedOutputStream xbuf = new SMExBufferedOutputStream(bos);
    th.check( xbuf.getcount() == 0 , "checking initial count" );
/*
    try {
    	new BufferedOutputStream(null);
    	th.fail("should throw a NullPointerException");
    	}
    catch(NullPointerException ne){ th.check(true); }
*/
    th.checkPoint("BufferedOutputStream(java.io.OutputStream,int)");
    xbuf = new SMExBufferedOutputStream(bos,30);
    th.check( xbuf.getbuf().length == 30 , "checking default size" );
    th.check( xbuf.getcount() == 0 , "checking initial count" );
/*
    try {
    	new BufferedOutputStream(null);
    	th.fail("should throw a NullPointerException");
    	}
    catch(NullPointerException ne){ th.check(true); }
*/
    try {
    	new BufferedOutputStream(bos,0);
    	th.fail("should throw a IllegalArgumentException -- 1");
    	}
    catch(IllegalArgumentException ne){ th.check(true); }
    try {
    	new BufferedOutputStream(bos,-1);
    	th.fail("should throw a IllegalArgumentException -- 2");
    	}
    catch(IllegalArgumentException ne){ th.check(true); }
  }

/**
* implemented. <br>
*
*/
  public void test_write(){
    th.checkPoint("write(int)void");
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    SMExBufferedOutputStream xbuf = new SMExBufferedOutputStream(bos,6);
    try {
    	xbuf.write(97);
    	xbuf.write(98);
    	th.check( xbuf.getcount() == 2 , "checking count -- 1" );
    	xbuf.write(99);
    	xbuf.write(99);
    	th.check( xbuf.getcount() == 4 , "checking count -- 2" );
    	xbuf.write(98);
    	xbuf.write(97);
    	th.check( xbuf.getcount() == 6 , "checking count -- 3" );
        th.check( xbuf.toString().equals("abccba") , "checking writting in buffer -- 1");
        th.check( bos.toString().equals("") , "nothing written downstream");
    	xbuf.write(97);
        th.check( xbuf.toString().equals("a") , "checking writting in buffer -- 2");
        th.check( bos.toString().equals("abccba") , "now we have written downstream");
    	}
    	
    catch(Exception e) { th.fail("got unexpected exception:"+e); }	
    	
    th.checkPoint("write(byte[],int,int)void");
    bos = new ByteArrayOutputStream();
    xbuf = new SMExBufferedOutputStream(bos,6);
    String s =  "Smartmove!";
    byte [] b = s.getBytes();
    try {
    	xbuf.write(b,0,10);
    	th.check( xbuf.getcount() == 0 , "checking count -- 1" );
        th.check( bos.toString().equals(s) , "checking what is written downstream -- 1");
    	xbuf.write(b,5,3);
    	th.check( xbuf.getcount() == 3 , "checking count -- 2" );
        th.check( xbuf.toString().equals("mov") , "checking writting in buffer -- 1");
        th.check( bos.toString().equals(s) , "checking what is written downstream -- 2");
    	xbuf.write(b,7,3);
    	th.check( xbuf.getcount() == 6 , "checking count -- 3" );
        th.check( xbuf.toString().equals("movve!") , "checking writting in buffer -- 2");
        th.check( bos.toString().equals(s) , "checking what is written downstream -- 3");
    	xbuf.write(b,0,0);
    	th.check( xbuf.getcount() == 6 , "checking count -- 4" );
        th.check( xbuf.toString().equals("movve!") , "checking writting in buffer -- 3");
        th.check( bos.toString().equals(s) , "checking what is written downstream -- 4");
    	xbuf.write(b,0,5);
    	th.check( xbuf.getcount() , 5 , "checking count -- 5, got:"+xbuf.getcount() );
        th.check( bos.toString(), s+"movve!" , "checking what is written downstream -- 5, got:"+bos.toString());
    	bos = new ByteArrayOutputStream();
    	xbuf = new SMExBufferedOutputStream(bos,6);
    	xbuf.write(b,5,3);
    	th.check( xbuf.getcount() == 3 , "checking count -- 6" );
        th.check( xbuf.toString().equals("mov") , "checking writting in buffer -- 4");
        th.check( bos.toString().equals("") , "checking what is written downstream -- 6");
    	xbuf.write(b,0,10);
    	th.check( xbuf.getcount() == 0 , "checking count -- 7" );
        th.check( bos.toString().equals("mov"+s) , "checking what is written downstream -- 7");
    	
    	bos = new ByteArrayOutputStream();
    	xbuf = new SMExBufferedOutputStream(bos,6);
    	xbuf.write(b,0,3);
    	th.check( xbuf.getcount() , 3 , "checking count -- 8" );
      th.check( xbuf.toString() ,"Sma" , "checking writting in buffer -- 8");
      th.check( bos.toString(), "" , "checking what is written downstream -- 8");
    	xbuf.write(b,3,5);
    	th.check( xbuf.getcount() , 5 , "checking count -- 9" );
      th.check( xbuf.toString(), "rtmov" , "checking writting in buffer -- 9");
      th.check( bos.toString(), "Sma" , "checking what is written downstream -- 9");
    	xbuf.write(b,0,9);
    	th.check( xbuf.getcount() , 0 , "checking count -- 10" );
      th.check( xbuf.toString(), "" , "checking writting in buffer -- 10");
      th.check( bos.toString(), "SmartmovSmartmove" , "checking what is written downstream -- 10");
    	
    }
    	
    catch(Exception e) { th.fail("got unexpected exception -- 1, got:"+e); }	
    try {
    	xbuf.write(null,1,2);
    	th.fail("should throw a NullPointerException");
    	}
    catch(NullPointerException ne){ th.check(true); }
    catch(Exception e) { th.fail("got unexpected exception -- 2, got:"+e); }	
    try {
    	xbuf.write(b,-1,2);
    	th.fail("should throw an IndexOutOfBoundsException -- 1");
    	}
    catch(IndexOutOfBoundsException ne){ th.check(true); }
    catch(Exception e) { th.fail("got unexpected exception -- 3, got:"+e); }	
    try {
    	xbuf.write(b,11,0);
    	th.fail("should throw an IndexOutOfBoundsException -- 2");
    	}
    catch(IndexOutOfBoundsException ne){ th.check(true); }
    catch(Exception e) { th.fail("got unexpected exception -- 4, got:"+e); }	
    try {
    	xbuf.write(b,5,-2);
    	th.fail("should throw an IndexOutOfBoundsException -- 3");
    	}
    catch(IndexOutOfBoundsException ne){ th.check(true); }
    catch(Exception e) { th.fail("got unexpected exception -- 5, got:"+e); }	
    try {
    	xbuf.write(b,5,6);
    	th.fail("should throw an IndexOutOfBoundsException -- 4");
    	}
    catch(IndexOutOfBoundsException ne){ th.check(true); }
    catch(Exception e) { th.fail("got unexpected exception -- 6, got:"+e); }	

    th.check( xbuf.getcount() == 0 , "checking count -- 8" );

    th.checkPoint("write(byte[],int,int)void");
    try {
    	b = new byte[3];
    	b[0] = (byte) 97;
    	b[1] = (byte) 98;
    	b[2] = (byte) 99;
    	bos = new ByteArrayOutputStream();
    	xbuf = new SMExBufferedOutputStream(bos,6);
    	xbuf.write(b);
    	th.check( xbuf.getcount() == 3 , "checking count -- special" );
        th.check( xbuf.toString().equals("abc") , "checking writting in buffer -- special");
        th.check( bos.toString().equals("") , "checking what is written downstream -- special");

        }
    catch(Exception e) { th.fail("got unexpected exception -- 6, got:"+e); }	
  }

/**
* implemented. <br>
*
*/
  public void test_flush(){
    th.checkPoint("flush()void");
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    SMExBufferedOutputStream xbuf1 = new SMExBufferedOutputStream(bos,60);
    SMExBufferedOutputStream xbuf = new SMExBufferedOutputStream(xbuf1,6);
    try {
    	byte [] b = new byte[3];
    	b[0] = (byte) 97;
    	b[1] = (byte) 98;
    	b[2] = (byte) 99;
    	xbuf.write(b,0,3);
    	xbuf.flush();
    	th.check( xbuf.getcount() == 0 , "checking count -- 1" );
        th.check( xbuf.toString().equals("") , "checking writting in buffer -- 1");
    	th.check( xbuf1.getcount() == 0 , "checking count -- 2" );
        th.check( xbuf1.toString().equals("") , "checking writting in buffer -- 2");
        th.check( bos.toString().equals("abc") , "checking flusf goes downstream -- 1");


        }
    catch(Exception e) { th.fail("got unexpected exception -- 1, got:"+e); }	

  }
/**
* implemented. <br>
*
*/
  public void test_flushplus(){
    th.checkPoint("flush()void");
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    SMExBufferedOutputStream xbuf1 = new SMExBufferedOutputStream(bos,60);
    SMExBufferedOutputStream xbuf = new SMExBufferedOutputStream(xbuf1,6);
    try {
    	String s =  "Smartmove!";
    	byte [] b = s.getBytes();
    	xbuf.write(b,0,10);
    	xbuf.write(b,5,3);
    	xbuf.write(b,7,3);
    	xbuf.write(b,0,5);
    	xbuf.write(b,5,3);
    	for (int i=0; i < 7 ; i++) { xbuf.write((byte)i);}
        th.check( bos.toString().equals("") , "checking what is written downstream -- 1");
    	}
    catch(Exception e) { th.fail("got unexpected exception -- 1, got:"+e); }	
  }

}
