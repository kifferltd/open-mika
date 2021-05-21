/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
* Small changes by Chris Gray 2011                                        *
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


package gnu.testlet.wonka.io.ByteArrayInputStream; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.*; // at least the class you are testing ...

/**
*  this file contains test for java.io.ByteArrayInputStream  <br>
*  we have a help-class which extends ByteArrayInputStream and has ways
*  to retrieve the protected fields
*/
public class SMByteArrayInputStreamTest implements Testlet
{
  protected TestHarness th;
  protected byte buffer[] = "this is a test buffer and contains secrets: don't tell them".getBytes();

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.io.ByteArrayInputStream");
       test_ByteArrayInputStream();
       test_read();
       test_skip();
       test_mark();
       test_markSupported();
       test_reset();
       test_available();
       test_close();

    // test on the protected fields
    // not explicitly tested
       test_buf();
       test_count();
       test_fieldmark();
       test_pos();

     }


/**
*   not implemented. <br>
*   we use SMExByteArrayInputStream so we can see what is stored in protected fields
*/
  public void test_ByteArrayInputStream(){
    th.checkPoint("ByteArrayInputStream(byte[])");
    SMExByteArrayInputStream xbin = new SMExByteArrayInputStream(buffer);
    th.check( xbin.get_buf() == buffer , "the stream doesn't use a copy");
    th.check( xbin.get_pos() == 0 , "position is at the begin of the array");
    th.check( xbin.get_count() == buffer.length , "count points to the endindex of the array -- got:"+xbin.get_count()+"length"+buffer.length);
    th.check( xbin.get_mark() == 0 , "mark is at the begin of the array");
    try {	new ByteArrayInputStream((byte[])null);
    		th.fail("should throw NullPointerException");
    	}
    catch (NullPointerException ne) { th.check(true); }

    th.checkPoint("ByteArrayInputStream(byte[],int,int)");
    try {	xbin = new SMExByteArrayInputStream(null,5,10);
		th.fail("should throw NullPointerException");
    	}
    catch (NullPointerException ne) { th.check(true); }

    xbin = new SMExByteArrayInputStream(buffer, 5, 50);    th.check( xbin.get_buf() == buffer , "the stream doesn't use a copy");
    th.check( xbin.get_pos() == 5 , "position is at the begin of the array");
    th.check( xbin.get_count() == 55 , "count points to the endindex of the array -- got:"+xbin.get_count());
    th.check( xbin.get_mark() == 5 , "mark is at the begin of the array");




  }

/**
*   implemented. <br>
*   How can we simulate an IOException ?
*/
  public void test_read(){
    th.checkPoint("read()int");
    ByteArrayInputStream bin = new ByteArrayInputStream(buffer,0,14);
    try {
      th.check( bin.read() , (int) 't');
      th.check( bin.read() , (int) 'h');
      th.check( bin.read() , (int) 'i');
      th.check( bin.read() , (int) 's');
      th.check( bin.read() , (int) ' ');
      th.check( bin.read() , (int) 'i');
      th.check( bin.read() , (int) 's');
      th.check( bin.read() , (int) ' ');
      th.check( bin.read() , (int) 'a');
      th.check( bin.read() , (int) ' ');
      th.check( bin.read() , (int) 't');
      th.check( bin.read() , (int) 'e');
      th.check( bin.read() , (int) 's');
      th.check( bin.read() , (int) 't');
      th.check( bin.read() , -1);
      bin.close();
    }
    catch (IOException e) { th.fail("shouldn't throw an IOException"); }
    th.check(bin.read(), -1 ,"EOF reached");

    th.checkPoint("read(byte[],int,int)int");
    bin = new ByteArrayInputStream(buffer,0,9);
    byte buf[] = new byte[12];
    try {    th.check( bin.read(buf,0,12) == 9); }
    catch (Exception e) { th.fail("should not throw an Exception -- got:"+e); }

    // might be something wrong with the decoding algorithm in the String Constructor
    th.check( (new String(buf)).startsWith("this is a"), "checking contents of buf -- 1 got:"+new String(buf));
    th.check(buf[9] == (byte)0 && buf[10] == (byte)0 && buf[11] == (byte)0 , "checking if rest is untouched");

    bin = new ByteArrayInputStream(buffer,0,40);
    try {    th.check( bin.read(buf,0,12) == 12); }
    catch (Exception e) { th.fail("should not throw an Exception -- got:"+e); }
    th.check( (new String(buf)).equals("this is a te"), "checking contents of buf -- 2, got:"+new String(buf));
    try {    th.check( bin.read(buf,0,12) == 12); }
    catch (Exception e) { th.fail("should not throw an Exception -- got:"+e); }
    th.check( (new String(buf)).equals("st buffer an"), "checking contents of buf -- 3, got:"+new String(buf));
    try {    th.check( bin.read(buf,0,12) == 12); }
    catch (Exception e) { th.fail("should not throw an Exception -- got:"+e); }
    th.check( (new String(buf)).equals("d contains s"), "checking contents of buf -- 4, got:"+new String(buf));
    try {    th.check( bin.read(buf,0,12) == 4); }
    catch (Exception e) { th.fail("should not throw an Exception -- got:"+e); }
    th.check( (new String(buf)).equals("ecrentains s"), "checking contents of buf -- 5, got:"+new String(buf));
    try {    th.check( bin.read(buf,0,12) == -1, "checking returnvlaues"); }
    catch (Exception e) { th.fail("should not throw an Exception -- got:"+e); }
    th.check( (new String(buf)).equals("ecrentains s"), "checking contents of buf -- 6, got:"+new String(buf));


    bin = new ByteArrayInputStream(buffer,0,40);
    try {   	bin.read(buf, -1 ,10);
    		th.fail("should throw an IndexOutOfBoundsException -- 1");
        }
    catch (IndexOutOfBoundsException e) { th.check(true); }
    catch (Exception e) { th.fail("cought wrong exception -- got:"+e); }
/*    try {   	bin.read(buf, 12 ,0);
    		th.fail("should throw an IndexOutOfBoundsException -- 2a");
        }
    catch (IndexOutOfBoundsException e) { th.check(true); }
    catch (Exception e) { th.fail("cought wrong exception -- got:"+e); }
*/  try {   	bin.read(buf, 13 ,0);
    		th.fail("should throw an IndexOutOfBoundsException -- 2b");
        }
    catch (IndexOutOfBoundsException e) { th.check(true); }
    catch (Exception e) { th.fail("cought wrong exception -- got:"+e); }
    try {   	bin.read(buf, 12 ,1);
    		th.fail("should throw an IndexOutOfBoundsException -- 2c");
        }
    catch (IndexOutOfBoundsException e) { th.check(true); }
    catch (Exception e) { th.fail("cought wrong exception -- got:"+e); }
    try {   	bin.read(buf, 2 ,11);
    		th.fail("should throw an IndexOutOfBoundsException -- 3");
        }
    catch (IndexOutOfBoundsException e) { th.check(true); }
    catch (Exception e) { th.fail("cought wrong exception -- got:"+e); }
    try {   	bin.read(buf, 10 ,-1);
    		th.fail("should throw an IndexOutOfBoundsException -- 4");
        }
    catch (IndexOutOfBoundsException e) { th.check(true); }
    catch (Exception e) { th.fail("cought wrong exception -- got:"+e); }

  }

/**
*   implemented. <br>
*
*/
  public void test_skip(){
    th.checkPoint("skip(long)long");
    ByteArrayInputStream bin = new ByteArrayInputStream(buffer, 25 , 30);
    th.check( bin.skip(-20) == 0 );
    th.check( bin.skip(0) == 0 );
    th.check( bin.skip(20) == 20 );
    th.check( bin.skip(20) == 10 );
    th.check( bin.skip(20) == 0 );
  }

/**
*   implemented. <br>
*   --> this function take a parameter, but it is compltely ignored <br>
*/
  public void test_mark(){
    th.checkPoint("mark(int)void");
    SMExByteArrayInputStream xbin = new SMExByteArrayInputStream(buffer, 25 , 30);
    try {
    xbin.skip(10);
    xbin.mark(2132234);
    th.check( xbin.get_mark() == 35 );
    xbin.read();
    xbin.mark(-2132234);
    xbin.read();
    th.check( xbin.get_mark() == 36 );
    xbin.mark(0);
    th.check( xbin.get_mark() == 37 );
    xbin.read();
    xbin.mark(657);
    th.check( xbin.get_mark() == 38 );
    }
    catch (Exception e) { th.fail("should't throw exceptions");}
  }

/**
*   implemented. <br>
*
*/
  public void test_markSupported(){
    th.checkPoint("markSupported()boolean");
    ByteArrayInputStream bin = new ByteArrayInputStream(buffer, 25 , 30);
    th.check(bin.markSupported());
  }

/**
*  implemented. <br>
*
*/
  public void test_reset(){
    th.checkPoint("reset()void");
    SMExByteArrayInputStream xbin = new SMExByteArrayInputStream(buffer, 25 , 30);
    try {
      xbin.skip(10);
      xbin.reset();
      th.check( xbin.get_mark() == xbin.get_pos());
      xbin = new SMExByteArrayInputStream(buffer);
      xbin.skip(30);
      xbin.reset();
      th.check( xbin.get_mark() == xbin.get_pos());
      xbin.skip(30);
      xbin.mark(30);
      xbin.skip(30);
      xbin.reset();
      th.check( xbin.get_mark() == xbin.get_pos());
      xbin.close();
      xbin.reset();
    }
    catch (Exception e) { th.fail("shouldn't throw an exception"); }

  }

/**
*   implemented. <br>
*
*/
  public void test_available(){
    th.checkPoint("available()int");
    ByteArrayInputStream bin = new ByteArrayInputStream(buffer, 25 , 30);
    th.check( bin.available() == 30 , "test -- 1 got:"+bin.available());
    bin.skip(25);
    th.check( bin.available() == 5 , "test -- 2 got:"+bin.available());
    bin.skip(15);
    th.check( bin.available() == 0 , "test -- 3 got:"+bin.available());
    bin = new ByteArrayInputStream(buffer);
    th.check( bin.available() == 59 , "test -- 4 got:"+bin.available());
  }

/**
*   not implemented. <br>
*
*/
  public void test_buf(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_count(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_fieldmark(){
    th.checkPoint("()");

  }

/**
*   not implemented. <br>
*
*/
  public void test_pos(){
    th.checkPoint("()");

  }


/**
* implemented. <br>
*
*/
  public void test_close(){
    th.checkPoint("close()void");
    ByteArrayInputStream bin = new ByteArrayInputStream(buffer, 25 , 30);
    try {
      bin.close(); 	
      th.check (bin.available(), 30 , "nothing availbale anymore");
    }
    catch (IOException e) { th.check(true); }
    bin = new ByteArrayInputStream(buffer, 25 , 30);
    try {
      bin.close();
    	th.check(bin.skip(4L) , 4 ,"nothing skipped");
    }
    catch (IOException e) { th.check(true); }
    bin = new ByteArrayInputStream(buffer, 25 , 30);
    try {
      bin.close();
    	th.check(bin.read(), 32 ,"nothing read -- 1");
    }
    catch (IOException e) { th.check(true); }
    try {
      bin.close();
    	th.check(bin.read(new byte[10],1,2), 2 ,"nothing read -- 2");
    }
    catch (IOException e) { th.check(true); }
  }
}
