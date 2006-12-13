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


package gnu.testlet.wonka.io.BufferedInputStream; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.*; // at least the class you are testing ...

public class SMBufferedInputStreamTest implements Testlet
{
  protected TestHarness th;
  protected byte[] b = "smartmove rules!\nWe are testing the BufferedInputStream class".getBytes();

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.io.BufferedInputStream");
       test_BufferedInputStream();
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
  public void test_BufferedInputStream(){
    th.checkPoint("BufferedInputStream(java.io.InputStream)");
    ByteArrayInputStream bis = new ByteArrayInputStream(b);
    SMExBufferedInputStream xbuf = new SMExBufferedInputStream(bis);
    th.check( xbuf.getbuf().length == 2048 , "checking default size" );
    th.check( xbuf.getcount() == 0 , "checking initial count" );
    th.checkPoint("BufferedInputStream(java.io.InputStream,int)");
    xbuf = new SMExBufferedInputStream(bis,30);
    th.check( xbuf.getbuf().length == 30 , "checking default size" );
    th.check( xbuf.getcount() == 0 , "checking initial count" );
    try {
    	new BufferedInputStream(bis,0);
    	th.fail("should throw a IllegalArgumentException -- 1");
    	}
    catch(IllegalArgumentException ne){ th.check(true); }
    try {
    	new BufferedInputStream(bis,-1);
    	th.fail("should throw a IllegalArgumentException -- 2");
    	}
    catch(IllegalArgumentException ne){ th.check(true); }

  }

/**
* implemented. <br>
*
*/
  public void test_read(){
    th.checkPoint("read()int");
    ByteArrayInputStream bis = new ByteArrayInputStream(b);
    SMExBufferedInputStream xbuf = new SMExBufferedInputStream(bis,6);
    char c;
    try {
      c = (char)xbuf.read();
    	th.check( xbuf.toString().equals("smartm") , "checking buffer -- 1" );
    	th.check( xbuf.getcount() , 6 , "checking count -- 1" );
      th.check( xbuf.getpos() , 1 , "checking pos -- 1");
      th.check( c , 's' , "checking return value -- 1");
      checkPos(bis, 'o', "checking pos of in-stream -- 1");
      xbuf.read();
      xbuf.read();
      xbuf.read();
      xbuf.read();
      c = (char)xbuf.read();
    	th.check( xbuf.toString(),"smartm" , "checking buffer -- 2" );
    	th.check( xbuf.getcount() , 6 , "checking count -- 2" );
      th.check( xbuf.getpos() , 6 , "checking pos -- 2");
      th.check( c , 'm' , "checking return value -- 2");
      checkPos(bis, 'o', "checking pos of in-stream -- 2");
      c = (char)xbuf.read();
    	th.check( xbuf.toString(),"ove ru" , "checking buffer -- 3" );
    	th.check( xbuf.getcount() , 6 , "checking count -- 3" );
      th.check( xbuf.getpos() , 1 , "checking pos -- 3");
      th.check( c == 'o' , "checking return value -- 3");
      checkPos(bis, 'l', "checking pos of in-stream -- 3");
      long l =  xbuf.skip(b.length);
      //th.debug("skipped "+l+" bytes, but wanted to skip:"+b.length);
      while(l > 0){
        l =  xbuf.skip(b.length);
        //th.debug("skipped "+l+" bytes, but wanted to skip:"+b.length);
      }
      int i = xbuf.read();
      th.check( i , -1 , "checking return value -- 4");

  	}

    catch(Exception e) { th.fail("got unexpected exception -- 1, got:"+e); }

    th.checkPoint("read(byte[],int,int)int");
    byte [] ba = new byte[10];
    bis = new ByteArrayInputStream(b);
    xbuf = new SMExBufferedInputStream(bis,6);
    int i;
    String s;
    try {
        i = xbuf.read(ba,0,10);
        s = new String(ba);
        th.check( s, "smartmove " , "checking readed bytes -- 1, got:"+s);
    	th.check( xbuf.toString(),"" , "checking buffer -- 1, got:"+xbuf.toString());
    	th.check( xbuf.getcount() , 0 , "checking count -- 1, got:"+xbuf.getcount());
        th.check( xbuf.getpos() , 0 , "checking pos -- 1, got:"+xbuf.getpos());
        th.check( i , 10 , "checking return value -- 1, got:"+i);
        checkPos(bis, 'r', "checking pos of in-stream -- 1");
        i = xbuf.read(ba,5,5);
        s = new String(ba);
        th.check( s, "smartrules" , "checking readed bytes -- 2, got:"+s);
    	th.check( xbuf.toString(),"rules!" , "checking buffer -- 2, got:"+xbuf.toString());
    	th.check( xbuf.getcount() , 6 , "checking count -- 2, got:"+xbuf.getcount());
        th.check( xbuf.getpos() , 5 , "checking pos -- 2, got:"+xbuf.getpos());
        th.check( i , 5 , "checking return value -- 2, got:"+i);
        checkPos(bis, '\n', "checking pos of in-stream -- 2");
        i = xbuf.read(ba,5,5);
        s = new String(ba);
        th.check( s.equals("smart!\nWe ") , "checking readed bytes -- 3, got:"+s);
    	th.check( xbuf.toString().equals("\nWe ar") , "checking buffer -- 3, got:"+xbuf.toString());
    	th.check( xbuf.getcount() == 6 , "checking count -- 3, got:"+xbuf.getcount());
        th.check( xbuf.getpos() == 4 , "checking pos -- 3, got:"+xbuf.getpos());
        th.check( i == 5 , "checking return value -- 3, got:"+i);
        checkPos(bis, 'e', "checking pos of in-stream -- 3");
        i = xbuf.read(ba,3,7);
        s = new String(ba);
        th.check( s.equals("smaare tes") , "checking readed bytes -- 4, got:"+s);
    	th.check( xbuf.toString().equals("e test") , "checking buffer -- 4, got:"+xbuf.toString());
    	th.check( xbuf.getcount() == 6 , "checking count -- 4, got:"+xbuf.getcount());
        th.check( xbuf.getpos() == 5 , "checking pos -- 4, got:"+xbuf.getpos());
        th.check( i == 7 , "checking return value -- 4, got:"+i);
        checkPos(bis, 'i', "checking pos of in-stream -- 4");
        i = xbuf.read(ba,3,7);
        s = new String(ba);
        th.check( s.equals("smating th") , "checking readed bytes -- 5, got:"+s);
    	th.check( xbuf.getcount() == xbuf.getpos() , "checking buffer is empty -- 5");
        th.check( i == 7 , "checking return value -- 5, got:"+i);
        checkPos(bis, 'e', "checking pos of in-stream -- 5");
        i = xbuf.read(ba,0,10);
        s = new String(ba);
        th.check( s.equals("e Buffered") , "checking readed bytes -- 6, got:"+s);
    	th.check( xbuf.getcount() == xbuf.getpos() , "checking buffer is empty -- 6");
        th.check( i == 10 , "checking return value -- 6, got:"+i);
        i = xbuf.read(ba,0,10);
        s = new String(ba);
        th.check( s.equals("InputStrea") , "checking readed bytes -- 7, got:"+s);
    	th.check( xbuf.getcount() == xbuf.getpos() , "checking buffer is empty -- 7");
        th.check( i == 10 , "checking return value -- 7, got:"+i);
        i = xbuf.read(ba,6,4);
        s = new String(ba);
        th.check( s.equals("InputSm cl") , "checking readed bytes -- 8, got:"+s);
    	th.check( xbuf.toString().equals("m clas") , "checking buffer -- 8, got:"+xbuf.toString());
    	th.check( xbuf.getcount() == 6 , "checking count -- 8, got:"+xbuf.getcount());
        th.check( xbuf.getpos() == 4 , "checking pos -- 8, got:"+xbuf.getpos());
        th.check( i == 4 , "checking return value -- 8, got:"+i);
        i = xbuf.read(ba,5,5);
        s = new String(ba);
        th.check( s.equals("Inputasscl") , "checking readed bytes -- 9, got:"+s);
        th.check( i == 3 , "checking return value -- 9, got:"+i);
        i = xbuf.read(ba,5,5);
        s = new String(ba);
        th.check( s.equals("Inputasscl") , "checking readed bytes -- 10, got:"+s);
    	th.check( xbuf.getcount() == xbuf.getpos() , "checking buffer is empty -- 10");
        th.check( i == -1 , "checking return value -- 10, got:"+i);
    	}

    catch(Exception e) {
      th.fail("got unexpected exception -- 1, got:"+e);
      e.printStackTrace();
    }

    try {
    	xbuf.read(null,1,2);
    	th.fail("should throw a NullPointerException");
    	}
    catch(NullPointerException ne){ th.check(true); }
    catch(Exception e) { th.fail("got unexpected exception -- 2, got:"+e); }
    try {
    	xbuf.read(ba,-1,2);
    	th.fail("should throw a IndexOutOfBoundsException -- 1");
    	}
    catch(IndexOutOfBoundsException ne){ th.check(true); }
    catch(Exception e) { th.fail("got unexpected exception -- 3, got:"+e); }
    try {
    	xbuf.read(ba,5,-2);
    	th.fail("should throw a IndexOutOfBoundsException -- 2");
    	}
    catch(IndexOutOfBoundsException ne){ th.check(true); }
    catch(Exception e) { th.fail("got unexpected exception -- 4, got:"+e); }
    try {
    	xbuf.read(ba,11,0);
    	th.fail("should throw an IndexOutOfBoundsException -- 3");
    	}
    catch(IndexOutOfBoundsException ne){ th.check(true); }
    catch(Exception e) { th.fail("got unexpected exception -- 5, got:"+e); }
    try {
    	xbuf.read(ba,5,6);
    	th.fail("should throw a IndexOutOfBoundsException -- 4");
    	}
    catch(IndexOutOfBoundsException ne){ th.check(true); }
    catch(Exception e) { th.fail("got unexpected exception -- 6, got:"+e); }

    th.checkPoint("read(byte[],int,int)int");
    ba = new byte[5];
    bis = new ByteArrayInputStream(b);
    xbuf = new SMExBufferedInputStream(bis,6);
    try {
        i = xbuf.read(ba);
        s = new String(ba);
        th.check( s.equals("smart") , "checking readed bytes -- 1, got:"+s);
    	th.check( xbuf.toString().equals("smartm") , "checking buffer -- 1, got:"+xbuf.toString());
    	th.check( xbuf.getcount() == 6 , "checking count -- 1, got:"+xbuf.getcount());
        th.check( xbuf.getpos() == 5 , "checking pos -- 1, got:"+xbuf.getpos());
        th.check( i == 5 , "checking return value -- 1, got:"+i);
        checkPos(bis, 'o', "checking pos of in-stream -- 1");
    	}
    catch(Exception e) { th.fail("got unexpected exception -- 1, got:"+e); }

    bis = new ByteArrayInputStream(b);
    xbuf = new SMExBufferedInputStream(bis,6);
    try {
        ba = new byte[10];
        i = xbuf.read(ba,0,1);
        i += xbuf.read(ba,1,9);
        s = new String(ba);
        th.check( s, "smartmove " , "checking readed bytes -- 1, got:"+s);
        th.check( i , 10 , "checking return value -- 1, got:"+i);
  	}
    catch(Exception e) {
      th.fail("got unexpected exception -- 3, got:"+e);
      e.printStackTrace();
    }
  }


  public void checkPos( ByteArrayInputStream bis, char b, String s) {
    try {
  	bis.mark(1);
  	char r = (char) bis.read();
  	bis.reset();
  	th.check( (char)r , (char)b ,s);
  	}
    catch(Exception e) { th.debug("checkPos failed, got:"+e);}
  }


/**
* implemented. <br>
*
*/
  public void test_skip(){
    th.checkPoint("skip(long)long");
    ByteArrayInputStream bis = new ByteArrayInputStream(b);
    SMExBufferedInputStream xbuf = new SMExBufferedInputStream(bis,6);
    long l;
    try {
      xbuf.read();
      l = xbuf.skip(4L);
      th.check( xbuf.getpos() , 5 , "checking pos -- 1, got:"+xbuf.getcount());
      th.check( xbuf.toString() ,"smartm" , "checking buffer -- 1, got:"+xbuf.toString());
      th.check( l , 4L , "checking return value -- 1, got:"+l);	
      checkPos(bis, 'o', "checking pos of in-stream -- 1");
      l = xbuf.skip(4L);
      th.check( xbuf.getpos() , 6 , "checking pos -- 2, got:"+xbuf.getcount());
      th.check( xbuf.toString(),"smartm"  , "checking buffer -- 2, got:"+xbuf.toString());
      th.check( l , 1L , "checking return value -- 2");	
      checkPos(bis, 'o', "checking pos of in-stream -- 2");
      l = xbuf.skip(10L);
      th.check( xbuf.getcount() , xbuf.getpos() , "checking buffer is empty -- 3");
      th.check( l , 10L , "checking return value -- 3, got:"+l);	
      checkPos(bis, '\n', "checking pos of in-stream -- 3");
      l = xbuf.skip(-10L);
      th.check( l , 0L , "checking return value -- 4, got:"+l);	
      checkPos(bis, '\n', "checking pos of in-stream -- 4");
      l = xbuf.skip(0L);
      th.check( l , 0L , "checking return value -- 5, got:"+l);	
      checkPos(bis, '\n', "checking pos of in-stream -- 5");
      l = xbuf.skip(b.length);
      th.check( l  , (long)b.length - 16L , "checking return value -- 6, got:"+l);	
      l = xbuf.skip(10);
      th.check( l , 0L , "checking return value -- 6, got:"+l);	
  	}
    catch(Exception e) { th.fail("got unexpected exception -- 1, got:"+e); }

  }

/**
* implemented. <br>
*
*/
  public void test_mark(){
    th.checkPoint("mark(int)void");
    ByteArrayInputStream bis = new ByteArrayInputStream(b);
    SMExBufferedInputStream xbuf = new SMExBufferedInputStream(bis,6);
    byte [] ba = new byte[8];
    try {
      xbuf.read();
      xbuf.mark(1);
      th.check( xbuf.getmarkpos() == 1 ,"checking markpos -- 1, got:"+xbuf.getmarkpos());
      th.check( xbuf.getmarklimit() == 1,"checking marklimit -- 1, got:"+xbuf.getmarklimit());
      th.check(xbuf.getpos() , 1 , "checking position -- 4");
      xbuf.read();
      xbuf.read();
      th.check(xbuf.getpos() , 3 , "checking position -- 2");
      th.check(xbuf.getmarkpos(),   1 ,"checking markpos -- 2, got:"+xbuf.getmarkpos());
      th.check(xbuf.getmarklimit(), 1,"checking marklimit -- 2, got:"+xbuf.getmarklimit());
      xbuf.mark(10);
      th.check( xbuf.getmarkpos() == 3 ,"checking markpos -- 3, got:"+xbuf.getmarkpos());
      th.check( xbuf.getmarklimit() == 10,"checking marklimit -- 3, got:"+xbuf.getmarklimit());
      xbuf.read(ba,0,5);
      th.check( xbuf.getmarkpos() , 0 ,"checking markpos -- 4, got:"+xbuf.getmarkpos());
      th.check( xbuf.getmarklimit() , 10,"checking marklimit -- 4, got:"+xbuf.getmarklimit());
      th.check( xbuf.getbuf().length , 6,"checking buffer length -- 4, got:"+xbuf.getbuf().length);
      th.check( xbuf.getpos() , 5 , "checking pos -- 4, got:"+xbuf.getpos());
    	th.check( xbuf.getcount(), 6 , "checking count -- 4");
      xbuf.read(ba,0,6);
      th.check( xbuf.getmarkpos() < 0 ,"checking markpos -- 4bis, got:"+xbuf.getmarkpos());
      th.check( xbuf.getmarklimit() , 10,"checking marklimit -- 4bis, got:"+xbuf.getmarklimit());
      th.check( xbuf.getbuf().length , 10,"checking buffer length -- 4bis, got:"+xbuf.getbuf().length);
      th.check(xbuf.getpos() , 1 , "checking position -- 4bis");
    	th.check( xbuf.getcount(), 10 , "checking count -- 4bis");
      xbuf.mark(12);
      th.check( xbuf.getmarkpos() , 1 ,"checking markpos -- 5, got:"+xbuf.getmarkpos());
      th.check( xbuf.getmarklimit() , 12,"checking marklimit -- 5, got:"+xbuf.getmarklimit());
      xbuf.skip(13L);
      th.check( xbuf.getmarkpos() , 1 ,"checking markpos -- 6, got:"+xbuf.getmarkpos());
      th.check( xbuf.getmarklimit() , 12,"checking marklimit -- 6, got:"+xbuf.getmarklimit());
    }
    catch(Exception e) {
      th.fail("got unexpected exception -- 1, got:"+e);
      e.printStackTrace();
    }
    xbuf.mark(-2);
    th.check( xbuf.getmarklimit(), -2,"checking marklimit -- 7, got:"+xbuf.getmarklimit());
	
  }

/**
* implemented. <br>
*
*/
  public void test_markSupported(){
    th.checkPoint("markSupported()boolean");
    ByteArrayInputStream bis = new ByteArrayInputStream(b);
    SMExBufferedInputStream xbuf = new SMExBufferedInputStream(bis,6);
    th.check(xbuf.markSupported() ,"should always be true");	
  }

/**
*  implemented. <br>
*
*/
  public void test_reset(){
    th.checkPoint("reset()void");
    ByteArrayInputStream bis = new ByteArrayInputStream(b);
    SMExBufferedInputStream xbuf = new SMExBufferedInputStream(bis,6);
    byte [] ba = new byte[8];

    try {
    	xbuf.reset();
	    th.fail("should throw an IOException -- 1");
	}
    catch(IOException ie){ th.check(true); }
    try {
      xbuf.read();
      xbuf.mark(1);
      xbuf.read();
      xbuf.reset();
      th.check( xbuf.getmarkpos() == 1 ,"checking markpos -- 1, got:"+xbuf.getmarkpos());
      th.check( xbuf.getmarklimit() == 1,"checking marklimit -- 1, got:"+xbuf.getmarklimit());
      th.check( xbuf.getpos() == 1 , "checking pos -- 1, got:"+xbuf.getpos());

      xbuf.read();
      xbuf.read();
    }
    catch(Exception e) { th.fail("got unexpected exception -- 1, got:"+e); }
    try {
    	xbuf.reset();
	
	    th.fail("should throw an IOException -- 2 --> "+(char)xbuf.read());
	  }
    catch(IOException ie){ th.check(true); }

    try {
    	xbuf.mark(10);
    	xbuf.read(ba,0,5);
    	xbuf.reset();
      th.check( xbuf.getpos() , 0 , "checking pos -- 2, got:"+xbuf.getpos());
    	xbuf.read(ba,0,5);
    	xbuf.read(ba,0,6);
    	}
    catch(Exception e) { th.fail("got unexpected exception -- 2, got:"+e); }
    try {
    	xbuf.reset();
	    th.fail("should throw an IOException -- 3");
	  }
    catch(IOException ie){ th.check(true); }
    	
    try {
      bis = new ByteArrayInputStream(b);
    	xbuf = new SMExBufferedInputStream(bis,5);
    	xbuf.mark(12);
   	  xbuf.skip(6L);
    	xbuf.reset();
    	th.check(xbuf.read(), 's');
      th.check( xbuf.getpos() == 1 , "checking pos -- 3, got:"+xbuf.getpos());
     	xbuf.skip(13L);
     	xbuf.skip(13L);
   	}
    catch(Exception e) { th.fail("got unexpected exception -- 3, got:"+e); }
    try {
    	xbuf.reset();
	th.fail("should throw an IOException -- 4");
	}
    catch(IOException ie){ th.check(true); }

  }

/**
* implemented. <br>
*
*/
  public void test_available(){
    th.checkPoint("available()int");
    ByteArrayInputStream bis = new ByteArrayInputStream(b);
    SMExBufferedInputStream xbuf = new SMExBufferedInputStream(bis,6);
    byte [] ba = new byte[8];
    try {
        th.check(xbuf.available() == b.length , "checking returnvalue -- 1, got:"+xbuf.available());
    	xbuf.read(ba,0,5);
        th.check(xbuf.available() == b.length-5 , "checking returnvalue -- 2, got:"+xbuf.available());
    	}
    catch(Exception e) { th.fail("got unexpected exception -- 1, got:"+e); }
  }

/**
* implemented. <br>
*
*/
  public void test_close(){
    th.checkPoint("close()void");
    ByteArrayInputStream bis = new ByteArrayInputStream(b);
    SMExBufferedInputStream xbuf1 = new SMExBufferedInputStream(bis,60);
    SMExBufferedInputStream xbuf = new SMExBufferedInputStream(xbuf1,6);
    byte [] ba = new byte[8];
    try {
    	xbuf.read(ba,0,5);
    	xbuf.close();
    	th.check( xbuf.getbuf() == null , "resources should be released after close");
    	xbuf.close();
    	xbuf.close();
    	xbuf.close();
    	th.check(xbuf1.getTC() == 1, "close does nothing after the first time");
    	}
    catch(Exception e) { th.fail("got unexpected exception -- 1, got:"+e); }
    try {
    	xbuf.reset();
	th.fail("should throw an IOException -- reset");
	}
    catch(IOException ie){ th.check(true); }
    try {
    	xbuf.read();
	th.fail("should throw an IOException -- read 1");
	}
    catch(IOException ie){ th.check(true); }
    try {
    	xbuf.read(new byte[3],1,1);
	th.fail("should throw an IOException -- read 2");
	}
    catch(IOException ie){ th.check(true); }
    try {
    	xbuf.skip(6L);
	th.fail("should throw an IOException -- skip");
	}
    catch(IOException ie){ th.check(true); }
    try {
    	xbuf.available();
	th.fail("should throw an IOException -- available");
	}
    catch(IOException ie){ th.check(true); }



  }

}
