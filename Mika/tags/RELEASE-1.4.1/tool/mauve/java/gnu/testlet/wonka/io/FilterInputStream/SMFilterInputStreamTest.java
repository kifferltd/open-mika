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


package gnu.testlet.wonka.io.FilterInputStream; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.*; // at least the class you are testing ...

/**
*  this file contains test for java.io.FilterInputStream   <br>
*
*/
public class SMFilterInputStreamTest implements Testlet
{
  protected TestHarness th;
  protected byte [] ba = "smartmove rules!\ntesting FilterInputStream".getBytes();

  public void test (TestHarness harness) {
     th = harness;
     th.setclass("java.io.FilterInputStream");
     test_FilterInputStream();
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
  public void test_FilterInputStream(){
    th.checkPoint("FilterInputStream(java.io.InputStream)");
    ByteArrayInputStream bin = new ByteArrayInputStream(ba);
    SMExFilterInputStream xfin = new SMExFilterInputStream(bin);
    th.check(xfin.getIn() == bin , "checking in-stream field");
  }


/**
* implemented. <br>
*
*/
  public void test_read(){
    th.checkPoint("read()int");
    ByteArrayInputStream bin = new ByteArrayInputStream(ba);
    SMExFilterInputStream fin = new SMExFilterInputStream(bin);
    char c;
    try {
        c = (char) fin.read();
        th.check( c == 's' , "checking return value -- 1" );
        c = (char) fin.read();
        th.check( c == 'm' , "checking return value -- 2" );
        c = (char) fin.read();
        th.check( c == 'a' , "checking return value -- 3" );
        c = (char) fin.read();
        th.check( c == 'r' , "checking return value -- 4" );
        fin.skip(100L);
        int i = fin.read();
        th.check( i ==  -1 , "checking return value -- 5" );
        }
    catch(Exception e) { th.fail("got unexpected exception:"+e); }

    th.checkPoint("read(byte[])int");

    bin = new ByteArrayInputStream(ba);
    byte buffer[] = new byte[10];
    SMExFilterInputStream is = new SMExFilterInputStream(bin);
    try {
    	int i = is.read(buffer);
    	th.check("smartmove ".equals(new String(buffer)) , "checking read -- 1");
    	th.check( i == 10 , "check return value -- 1");
    	i = is.read(buffer);
    	th.check("rules!\ntes".equals(new String(buffer)) , "checking read -- 2");
    	th.check( i == 10 , "check return value -- 2");
    	i = is.read(buffer);
    	th.check("ting Filte".equals(new String(buffer)) , "checking read -- 3");
    	th.check( i == 10 , "check return value -- 3");
    	i = is.read(buffer);
    	th.check("rInputStre".equals(new String(buffer)) , "checking read -- 4");
    	th.check( i == 10 , "check return value -- 4");
    	i = is.read(buffer);
    	th.check("amnputStre".equals(new String(buffer)) , "checking read -- 5");
    	th.check( i == 2 , "check return value -- 5");
    	i = is.read(buffer);
    	th.check("amnputStre".equals(new String(buffer)) , "checking read -- 6");
    	th.check( i == -1 , "check return value -- 6");
    	i = is.read(buffer);
    	th.check("amnputStre".equals(new String(buffer)) , "checking read -- 7");
    	th.check( i == -1 , "check return value -- 7");
    	}
    catch (Exception e) { th.fail("should not throw an Exception --1, got:"+e); }
    SMInfoInputStream iin = new SMInfoInputStream();
    fin = new SMExFilterInputStream(iin);
    try {
    	iin.clean();
    	fin.read(null);
    	th.fail("should throw a NullPointerException");
    	}
    catch (NullPointerException e) { th.check(true); }
    catch (Exception e) { th.fail("should not throw an Exception -- 2, got:"+e); }
    th.check(!iin.isMarked() , "checking marked after bad read -- 1");

    th.checkPoint("read(byte[],int,int)int");

    bin = new ByteArrayInputStream(ba);
    is = new SMExFilterInputStream(bin);
    try {
    	int i = is.read(buffer,0,10);
    	th.check("smartmove ".equals(new String(buffer)) , "checking read -- 1");
    	th.check( i == 10 , "check return value -- 1");
    	i = is.read(buffer,1,9);
    	th.check("srules!\nte".equals(new String(buffer)) , "checking read -- 2");
    	th.check( i == 9 , "check return value -- 2");
    	i = is.read(buffer,1,8);
    	th.check("ssting Fie".equals(new String(buffer)) , "checking read -- 3");
    	th.check( i == 8 , "check return value -- 3");
    	i = is.read(buffer,3,0);
    	th.check("ssting Fie".equals(new String(buffer)) , "checking read -- 4");
    	th.check( i == 0 , "check return value -- 4");
    	i = is.read(buffer,0,8);
    	th.check("lterInpuie".equals(new String(buffer)) , "checking read -- 5");
    	th.check( i == 8 , "check return value -- 5");
    	i = is.read(buffer,0,10);
    	th.check("tStreamuie".equals(new String(buffer)) , "checking read -- 6");
    	th.check( i == 7 , "check return value -- 6");
    	i = is.read(buffer);
    	th.check("tStreamuie".equals(new String(buffer)) , "checking read -- 7");
    	th.check( i == -1 , "check return value -- 7");
    	}
    catch (Exception e) { th.fail("should not throw an Exception --1, got:"+e); }
    try {
    	iin.clean();
    	fin.read(null);
    	th.fail("should throw a NullPointerException");
    	}
    catch (NullPointerException e) { th.check(true); }
    catch (Exception e) { th.fail("should not throw an Exception -- 2, got:"+e); }
    th.check(!iin.isMarked() , "checking marked after bad read -- 1");

    try {
    	iin.clean();
    	th.check(fin.read(buffer,-3,1), 1);
    }
    catch (Exception e) { th.fail("should not throw an Exception -- 3, got:"+e); }
    th.check(iin.isMarked() , "checking marked after bad read -- 2");
    try {
    	iin.clean();
    	th.check(fin.read(buffer,3,8), 8);
    }
    catch (Exception e) { th.fail("should not throw an Exception -- 4, got:"+e); }
    try {
    	iin.clean();
    	th.check(fin.read(buffer,3,-1), -1);
    }
    catch (Exception e) { th.fail("should not throw an Exception -- 5, got:"+e); }
  }

/**
* implemented. <br>
*
*/
  public void test_skip(){
    th.checkPoint("skip(long)long");
    ByteArrayInputStream bin = new ByteArrayInputStream(ba);
    SMExFilterInputStream fin = new SMExFilterInputStream(bin);
    long l;
    try {
        l = fin.skip(5L);
        char c = (char) fin.read();
        th.check( l == 5L , "checking return value -- 1");
        th.check( c == 'm' , "checking if really skipped underlying stream");
        l = fin.skip(0L);
        c = (char) fin.read();
        th.check( l == 0L , "checking return value -- 2");
        th.check( c == 'o' , "checking if nothing was skipped underlying stream -- 1");
        l = fin.skip(-5L);
        c = (char) fin.read();
        th.check( l == 0L , "checking return value -- 3");
        th.check( c == 'v' , "checking if nothing was skipped underlying stream -- 2");
        l = fin.skip(15L);
        th.check( l == 15L , "checking return value -- 4");
        l = fin.skip(150L);
        th.check( l ==(long)ba.length - 23L , "checking return value -- 5");
        l = fin.skip(150L);
        th.check( l == 0L , "checking return value -- 5");
        }
    catch(Exception e) { th.fail("got unexpected exception:"+e); }
  }

/**
* implemented. <br>
*
*/
  public void test_mark(){
    th.checkPoint("mark(int)void");
    SMInfoInputStream iin = new SMInfoInputStream();
    SMExFilterInputStream fin = new SMExFilterInputStream(iin);
    try {
    	fin.mark(-10);
    	th.check(iin.isMarked() , "checking if mark is called downstream");
    	th.check(iin.getRAL() == -10 , "checking if readAheadLimit is passed");
    	}
    catch(Exception e) { th.fail("got unexpected exception:"+e); }

  }

/**
* implemented. <br>
*
*/
  public void test_markSupported(){
    th.checkPoint("markSupported()boolean");
    SMInfoInputStream iin = new SMInfoInputStream();
    SMExFilterInputStream fin = new SMExFilterInputStream(iin);
    try {
    	th.check(!fin.markSupported() , "checking if markSupported is asked downstream -- 1");
    	fin.mark(-10);
    	th.check(fin.markSupported() , "checking if markSupported is asked downstream -- 2");
    	}
    catch(Exception e) { th.fail("got unexpected exception:"+e); }

  }

/**
* implemented. <br>
*
*/
  public void test_reset(){
    th.checkPoint("reset()void");
    SMInfoInputStream iin = new SMInfoInputStream();
    SMExFilterInputStream fin = new SMExFilterInputStream(iin);
    try {
    	fin.reset();
    	th.check(iin.isMarked() , "checking if reset is called downstream -- 1");
    	}
    catch(Exception e) { th.fail("got unexpected exception:"+e); }

  }

/**
* implemented. <br>
*
*/
  public void test_available(){
    th.checkPoint("available()int");
    SMInfoInputStream iin = new SMInfoInputStream();
    SMExFilterInputStream fin = new SMExFilterInputStream(iin);
    try {
        th.check(fin.available() == 10 , "checking if available is asked downstream -- 1");
        th.check( iin.isMarked() , "checking if available is asked downstream -- 2");
    	}
    catch(Exception e) { th.fail("got unexpected exception:"+e); }
  }

/**
* implemented. <br>
*
*/
  public void test_close(){
    th.checkPoint("close()void");
    th.checkPoint("available()int");
    SMInfoInputStream iin = new SMInfoInputStream();
    SMExFilterInputStream fin = new SMExFilterInputStream(iin);
    try {
        fin.close();
        th.check( iin.isMarked() , "checking if close is called downstream -- 1");
    	}
    catch(Exception e) { th.fail("got unexpected exception:"+e); }

  }
}
