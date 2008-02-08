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


package gnu.testlet.wonka.io.ByteArrayOutputStream; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.*; // at least the class you are testing ...

/**
*  this file contains test for java.io.ByteArrayOutputStream   <br>
*  <br>
*  We should have more information on which operation are allowed after a stream is closed <br>
*  write and read are NOT allowed, but what about reset, ... ?
*/
public class SMByteArrayOutputStreamTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.io.ByteArrayOutputStream");
       test_ByteArrayOutputStream();
       test_write();
       test_reset();
       test_size();
       test_toByteArray();
       test_toString();
       test_writeTo();
       test_close();
     }

/**
*  implemented. <br>
*
*/
  public void test_ByteArrayOutputStream(){
    th.checkPoint("ByteArrayOutputStream()");
    SMExByteArrayOutputStream xbout = new SMExByteArrayOutputStream();
    th.check( xbout.get_length() == 32 );
    th.check( xbout.get_count() == 0 );

    th.checkPoint("ByteArrayOutputStream(int)");
    xbout = new SMExByteArrayOutputStream(0);
    th.check( xbout.get_count() == 0 );
    th.check( xbout.get_length() == 0 );
    xbout = new SMExByteArrayOutputStream(1);
    th.check( xbout.get_length() == 1 );
    th.check( xbout.get_count() == 0 );
    xbout = new SMExByteArrayOutputStream(55);
    th.check( xbout.get_length() == 55 );
    xbout = new SMExByteArrayOutputStream(77);
    th.check( xbout.get_length() == 77 );
    try {
    	new ByteArrayOutputStream(-1);
    	th.fail("should throw an IllegalArgumentException");
    }	
    catch (IllegalArgumentException ie) { th.check(true); }
  }

/**
*   implemented. <br>
*
*/
  public void test_write(){
    th.checkPoint("write(int)void");
    SMExByteArrayOutputStream xbout = new SMExByteArrayOutputStream();
    xbout.write((int)'a');
    th.check( xbout.get_count() == 1 );
    xbout.write((int)'b');
    xbout.write((int)'c');
    xbout.write((int)'d');
    th.check( xbout.get_count() == 4 );
    th.check( new String( xbout.get_buf() , 0 , xbout.get_count()).equals("abcd") ,
    	"checking if all byte are written");
    try {
      xbout.close();
      xbout.write(12);
    }
    catch (Exception ieo) { th.fail("close is ignored"); }
    	
    th.checkPoint("write(byte[],int,int)void");
    xbout = new SMExByteArrayOutputStream();
    String s = "abcdefgh";
    xbout.write(s.getBytes(),0,8);
    th.check( xbout.get_count() == 8 );
    th.check( s.equals(new String(xbout.get_buf(),0,xbout.get_count())) ,
     "checking what is written -- 1, got:"+new String(xbout.get_buf(),0,xbout.get_count()));
    xbout.write(s.getBytes(),1,6);
    th.check( xbout.get_count() == 14 );
    th.check( "abcdefghbcdefg".equals(new String(xbout.get_buf(),0,xbout.get_count())) ,
     "checking what is written -- 2, got:"+new String(xbout.get_buf(),0,xbout.get_count()));
    xbout.write(s.getBytes(),3,0);
    th.check( xbout.get_count() == 14 );
    th.check( "abcdefghbcdefg".equals(new String(xbout.get_buf(),0,xbout.get_count())) ,
     "checking what is written -- 3, got:"+new String(xbout.get_buf(),0,xbout.get_count()));

    try { xbout.write(s.getBytes(),3,6);
    	  th.fail("should throw an IndexOutOfBoundsException -- 1");
    	}
    catch (IndexOutOfBoundsException ae) { th.check(true); }
    try { xbout.write(s.getBytes(),-1,5);
    	  th.fail("should throw an IndexOutOfBoundsException -- 2");
    	}
    catch (IndexOutOfBoundsException ae) { th.check(true); }
    try { xbout.write(s.getBytes(),6,-5);
    	  th.fail("should throw an IndexOutOfBoundsException -- 3");
    	}
    catch (IndexOutOfBoundsException ae) { th.check(true); }
    try { xbout.write(s.getBytes(),8,1);
    	  th.fail("should throw an IndexOutOfBoundsException -- 5");
    	}
    catch (IndexOutOfBoundsException ae) { th.check(true); }
    try { xbout.write(null,8,1);
    	  th.fail("should throw a NullPointerException -- 1");
    	}
    catch (NullPointerException ne) { th.check(true); }
    th.check( xbout.get_count() == 14 );
    th.check( "abcdefghbcdefg".equals(new String(xbout.get_buf(),0,xbout.get_count())) ,
     "checking if nothing is added while trting to trigger exceptions, got:"+new String(xbout.get_buf(),0,xbout.get_count()));
    try {
      xbout.close();
      //th.debug("stream closed ...");
    	xbout.write(s.getBytes(),5,1);
    }
    catch (Exception ieo) {
    	th.fail("writting to a closed stream is allowed");

    }
  }

/**
* implemented. <br>
*
*/
  public void test_reset(){
    th.checkPoint("reset()void");
    SMExByteArrayOutputStream xbout = new SMExByteArrayOutputStream();
    String s = "abcdefgh";
    xbout.write(s.getBytes(),0,8);
    xbout.reset();	
    th.check( xbout.get_count() == 0 );
    th.check( xbout.toString().equals("") );
    xbout.write((int)'x');
    th.check(xbout.get_buf()[0] == (byte)'x' ,"writting at 1st pos after a reset");
  }

/**
* implemented. <br>
*
*/
  public void test_size(){
    th.checkPoint("size()int");
    SMExByteArrayOutputStream xbout = new SMExByteArrayOutputStream();
    String s = "abcdefgh";
    xbout.write(s.getBytes(),0,8);
    th.check( xbout.size() == 8 );
    xbout.reset();	
    th.check( xbout.size() == 0 );
    xbout.write((int)'x');
    th.check( xbout.size() == 1 );
  }

/**
* implemented. <br>
*
*/
  public void test_toByteArray(){
    th.checkPoint("toByteArray()byte[]");
    SMExByteArrayOutputStream xbout = new SMExByteArrayOutputStream();
    String s = "abcdefgh";
    th.check( xbout.toByteArray() != null , "should be an empty array");
    th.check( xbout.toByteArray().length == 0 , "length of array is 0");
    xbout.write(s.getBytes(),0,8);
    th.check( xbout.get_buf() != xbout.toByteArray ());
    th.check( s.equals(new String(xbout.toByteArray())) , "should give only the bytes written");
    xbout.write(s.getBytes(),1,6);
    th.check( "abcdefghbcdefg".equals(new String(xbout.toByteArray())) , "should give only the bytes written");
    xbout.reset();
    th.check( xbout.toByteArray().length == 0 , "length of array is 0 after reset");
  }

/**
* implemented. <br>
*
*/
  public void test_toString(){
    th.checkPoint("toString()java.lang.String");
    SMExByteArrayOutputStream xbout = new SMExByteArrayOutputStream();
    String s = "abcdefgh";
    th.check( xbout.toString() != null , "should be an empty array");
    th.check( "".equals(xbout.toString()), "should return empty string -- 1");
    xbout.write(s.getBytes(),0,8);
    th.check( s.equals(xbout.toString()) , "should give only the bytes written");
    xbout.write(s.getBytes(),1,6);
    th.check( "abcdefghbcdefg".equals(xbout.toString()) , "should give only the bytes written");
    xbout.reset();
    th.check( "".equals(xbout.toString()), "should return empty string -- 2");
    th.checkPoint("toString(java.lang.String)java.lang.String");
    xbout = new SMExByteArrayOutputStream();
    try {
    th.check( xbout.toString("8859_1") != null , "should be an empty array");
    th.check( "".equals(xbout.toString("8859_1")), "should return empty string -- 1");
    xbout.write(s.getBytes(),0,8);
    th.check( s.equals(xbout.toString("8859_1")) , "should give only the bytes written");
    xbout.write(s.getBytes(),1,6);
    th.check( "abcdefghbcdefg".equals(xbout.toString("8859_1")) , "should give only the bytes written");
    xbout.reset();
    th.check( "".equals(xbout.toString("8859_1")), "should return empty string -- 2");
    }
    catch (IOException ioe) { th.fail("should not throw an IOException, got:"+ioe); }
    catch (Exception e) { th.fail("got unexpected exception -- 1,got: "+e);}
    try {	xbout.toString(null);
    		th.fail("should throw NullPointerException");
        }
    catch (NullPointerException ne) { th.check(true); }
    catch (Exception e) { th.fail("got unexpected exception -- 2,got: "+e);}
    try {	xbout.toString("this is not a good encoding string");
    		th.fail("should throw UnsupportedEncodingException");
        }
    catch (UnsupportedEncodingException ne) { th.check(true); }
  }

/**
* implemented. <br>
*
*/
  public void test_writeTo(){
    th.checkPoint("writeTo(java.io.OutputStream)void");
    SMExByteArrayOutputStream xbout1 = new SMExByteArrayOutputStream();
    SMExByteArrayOutputStream xbout2 = new SMExByteArrayOutputStream();
    String s = "abcdefgh";
    try { xbout1.writeTo(null);
          th.fail("should throw a NullPointerException -- 1");
        }
    catch (NullPointerException ne) { th.check(true); }
    catch (IOException ioe) { th.fail("should not throw an IOException -- 1, got:"+ioe); }

    try {
    xbout1.writeTo(xbout2);
    th.check("".equals(xbout2.toString()) , "making sure empty to empty works");
    xbout1.write(s.getBytes(),0,8);
    xbout1.write(s.getBytes(),0,8);
    xbout1.writeTo(xbout2);
    th.check("abcdefghabcdefgh".equals(xbout2.toString()) , "making sure writeTo an empty stream works");
    xbout1.writeTo(xbout2);
    th.check("abcdefghabcdefghabcdefghabcdefgh".equals(xbout2.toString()) , "making sure writeTo does not overwrite the stream");
    xbout2.close();
    }
    catch (IOException ioe) { th.fail("should not throw an IOException -- 2, got:"+ioe); }
    try {
    	xbout1.writeTo(xbout2);
    }
    catch (IOException ioe) {
      th.fail("close() is ignored");
    }

    try { xbout1.writeTo(null);
          th.fail("should throw a NullPointerException -- 2");
        }
    catch (NullPointerException ne) { th.check(true); }
    catch (IOException ioe) { th.fail("should not throw an IOException -- 3, got:"+ioe); }

  }

/**
*   not implemented. <br>
*   the only thing close does is to set an internal flag (= a private member) <br>
*   this flag is used to determine if a write is allowed or IOException <br>
*   needs to be thrown.  This is tested in the write methods ... <br>
*   no tests needed
*/
  public void test_close(){
    th.checkPoint("close()void");

  }

}
