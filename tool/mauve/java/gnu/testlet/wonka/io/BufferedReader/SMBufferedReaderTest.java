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


package gnu.testlet.wonka.io.BufferedReader; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import gnu.testlet.wonka.io.Reader.SMExReader;

import java.io.*; // at least the class you are testing ...
import java.lang.reflect.Field;

/**
*  this file contains test for java.io.BufferedReader   <br>
*  as Reader we use SMExReader which is also used to test java.io.Reader <br>
*  --> this class is imported from the gnu.testlet.wonka.io.Reader package <br>
*  This class has a private flag which allows test to be used on JDK <br>
*  so we can debug our tests !
*/
public class SMBufferedReaderTest implements Testlet
{
  protected TestHarness th;
  protected char [] ca="abcde\n\n\tabcde\nsmartmove\n   not empty\r\nmuch more to come...(maybe ! or not)\n\n\n".toCharArray();

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.io.BufferedReader");
       test_BufferedReader();
       test_read();
       test_readLine();
       test_skip();
       test_mark();
       test_markSupported();
       test_reset();
       test_close();
       test_ready();
// [CG 20221220 scrap this, it's totally misconceived
//       test_lock();
     }

/**
* implemented. <br>
*
*/
  public void test_BufferedReader(){
   th.checkPoint("BufferedReader(java.io.Reader)");
    CharArrayReader car = new CharArrayReader(ca);
    BufferedReader br = new BufferedReader(car);
    try { new BufferedReader(null);
    	  th.fail("should throw a NullPointerException");
        }
    catch (NullPointerException ne) { th.check(true); }

   th.checkPoint("BufferedReader(java.io.Reader,int)");
    try { new BufferedReader(car , -1);
    	  th.fail("should throw an IllegalArgumentException");
        }
    catch (IllegalArgumentException ie) { th.check(true); }
    try { new BufferedReader(null,10);
    	  th.fail("should throw a NullPointerException");
        }
    catch (NullPointerException ne) { th.check(true); }

  }


/**
* implemented. <br>
*
*/
  public void test_read(){
    th.checkPoint("read()int");
    CharArrayReader car = new CharArrayReader(ca);
    BufferedReader br = new BufferedReader(car,10);
    try {
    	char c = (char)br.read();
    	th.check( c == 'a' ,"checking value char read --1");
    	c = (char)br.read();
    	th.check( c == 'b' ,"checking value char read --1");
      for (int i=0 ;i < 10 ; i++) { br.read(); }
        int j=0;
        while (br.read() != -1) { if ( j++> 99) break;}
        th.check(j < 100 , "EOF is not returning -1");
    	th.check(br.read() , -1, "expect -1");
    	}
    catch(Exception e){ th.fail("got unwanted exception"+e);}	
    th.checkPoint("read(char[],int,int)int");
    car = new CharArrayReader(ca);
    BufferedReader xar = new BufferedReader(car,40);
    char[] ca1 = new char[15];
    try {	xar.read(null, 0,15);
                th.fail("should throw a NullPointerException");
        }
    catch(NullPointerException ne) { th.check(true); }
    catch(Exception e) { th.fail("got unwanted exception: "+e+" -- 2"); }
    try {	xar.read(ca1, 16,0);
                th.fail("should throw a IndexOutOfBoundsException -- 1");
        }
    catch(IndexOutOfBoundsException ne) { th.check(true); }
    catch(Exception e) { th.fail("got unwanted exception: "+e+" -- 3"); }
    try {	xar.read(ca1, -1,5);
                th.fail("should throw a IndexOutOfBoundsException -- 2");
        }
    catch(IndexOutOfBoundsException ne) { th.check(true); }
    catch(Exception e) { th.fail("got unwanted exception: "+e+" -- 4"); }
    try {	xar.read(ca1, 1,-5);
                th.fail("should throw a IndexOutOfBoundsException -- 3");
        }
    catch(IndexOutOfBoundsException ne) { th.check(true); }
    catch(Exception e) { th.fail("got unwanted exception: "+e+" -- 5"); }
    try {	xar.read(ca1, 10,6);
                th.fail("should throw a IndexOutOfBoundsException -- 4");
        }
    catch(IndexOutOfBoundsException ne) { th.check(true); }
    catch(Exception e) { th.fail("got unwanted exception: "+e+" -- 6"); }
    car = new CharArrayReader(ca);
    br = new BufferedReader(car,10);
    try {
//"abcde\n\n\tabcde\nsmartmove\n   not empty\r\nmuch more to come...(maybe ! or not)\n\n\n"
    	int k = br.read(ca1,0,10);
        th.check(k==10, "checking returnvalue --1");
        th.check( ca1[10] == (char)0 &&ca1[11] == (char)0 && ca1[12] ==  (char)0 &&
        ca1[13] == (char)0 && ca1[14] == (char)0 , "checking nulls");
    	k = br.read(ca1,0,9);
        th.check(k==9, "checking returnvalue --2");
        th.check( new String(ca1,0,10).equals("cde\nsmartb") ,"checking external buffer");
    	ca1 = new char[50];
    	br.read(ca1,1,4);
        th.check( new String(ca1,0,5),"\u0000move" ,"checking external buffer");
    	k = br.read(ca1,1,0);
        th.check(k==0, "checking returnvalue -- 3");
        th.check( new String(ca1,0,5), "\u0000move" ,"checking external buffer");
    	k = br.read(ca1,0,35);
        th.check(k,35, "checking returnvalue -- 4");
        th.check(new String(ca1,0,k) , "\n   not empty\r\nmuch more to come...", "check readed bytes -- 1");
    	k = br.read(ca1,0,9);
        th.check(k, 9, "checking returnvalue -- 5");
        th.check(new String(ca1,0,k) , "(maybe ! ", "check readed bytes -- 2");
    	k = br.read(ca1,0,2);
    	k = br.read(ca1,0,9);
        th.check(k,8, "checking returnvalue -- 6");
        th.check(new String(ca1,0,k) , " not)\n\n\n", "check readed bytes -- 3");
    	k = br.read(ca1,0,2);
        th.check(k,-1, "checking returnvalue -- 7, got k:"+k);
    	}
    catch(Exception e){ th.fail("got unwanted exception"+e);}	


  }

/**
* implemented. <br>
*
*/
  public void test_readLine(){
//"abcde\n\n\tabcde\nsmartmove\n   not empty\r\nmuch more to come...(maybe ! or not)\n\n\n"
    th.checkPoint("readLine()java.lang.String");
    CharArrayReader car = new CharArrayReader(ca);
    BufferedReader br = new BufferedReader(car);
    String s;
    try {
	s = br.readLine();
    	th.check(s,"abcde","got:"+s);
	s = br.readLine();
    	th.check(s,"","got:"+s);
	s = br.readLine();
    	th.check(s,"\tabcde","got:"+s);
	s = br.readLine();
    	th.check(s,"smartmove","got:"+s);
	s = br.readLine();
    	th.check(s,"   not empty","got:"+s);
	s = br.readLine();
    	th.check(s,"much more to come...(maybe ! or not)","got:"+s);
	s = br.readLine();
    	th.check(s,"","got:"+s);
	s = br.readLine();
    	th.check(s,"","got:"+s);
	s = br.readLine();
    	th.check(s , null ,"should return null, got:"+s);
    	car = new CharArrayReader("special things\rhik\noops\n\rtoppie\rg\ntest".toCharArray());
    	br = new BufferedReader(car);
	s = br.readLine();
    	th.check(s,"special things", "got:"+s);
	s = br.readLine();
    	th.check(s,"hik", "got:"+s);
	s = br.readLine();
    	th.check(s,"oops", "got:"+s);
	s = br.readLine();
    	th.check(s,"", "got:"+s);
	s = br.readLine();
    	th.check(s,"toppie", "got:"+s);
    	br.mark(5);
    	br.read();
	s = br.readLine();
    	th.check(s,"", "force fail read() -- got:"+s);
    	br.reset();
    	br.read(new char[1]);
	s = br.readLine();
    	th.check(s,"", "force fail read(char[],int,int) -- got:"+s);
    	br.reset();
	br.skip(1L);
	s = br.readLine();
    	th.check(s,"", "force fail skip() -- got:"+s);
    	br.reset();
	s = br.readLine();
    	th.check(s,"g", "got:"+s);
	s = br.readLine();
    	th.check(s,"test", "got:"+s);
	s = br.readLine();
    	th.check(s, null , "got:"+s);
    	}
    catch(Exception e){
     th.fail("got unwanted exception"+e);
     e.printStackTrace();
    }	
  }

/**
* implemented. <br>
*
*/
  public void test_skip(){
    th.checkPoint("skip(long)long");
    CharArrayReader car = new CharArrayReader(ca);
    BufferedReader br = new BufferedReader(car,10);
    try {
    	br.skip(-1);
    	th.fail("should throw an IllegalArgumentException");
    	}
    catch(IllegalArgumentException ie) { th.check(true); }	
    catch(Exception e){ th.fail("got unwanted exception"+e);}
    try {
        long l = br.skip(10L);
        th.check(l == 10 , "checking return value -- 1");
        l = br.skip((long)ca.length);
        th.check( l == ca.length-10 , "checking return value -- 2");
        l = br.skip(15L);
        th.check( l == 0 , "checking return value -- 3");
    	}
    catch(Exception e){ th.fail("got unwanted exception"+e);}
  }

/**
* implemented. <br>
* --> check correct behaviour !!!
*/
  public void test_mark(){
    th.checkPoint("mark(int)void");
    CharArrayReader car = new CharArrayReader(ca);
    BufferedReader br = new BufferedReader(car,10);
    try {
    	br.read();
    	br.mark(1);
    	br.skip(5L);
    	br.mark(-1);
    	th.fail("should throw an IllegalArgumentException");
    	}
    catch(IllegalArgumentException ie) { th.check(true); }	
    catch(Exception e){ th.fail("got unwanted exception"+e);}
    try {	
    	br.skip(5L);
     	br.mark(15);
    	br.skip(11);
    	}
    catch(Exception e){ th.fail("got unwanted exception"+e);}

  }

/**
* implemented. <br>
* always returns true
*/
  public void test_markSupported(){
    th.checkPoint("markSupported()boolean");
    CharArrayReader car = new CharArrayReader(ca);
    BufferedReader br = new BufferedReader(car,10);
    try {
        th.check(br.markSupported() , "always returns true -- 1");
        br.close();
        th.check(br.markSupported() , "always returns true -- 2");
    	}
    catch(Exception e){ th.fail("got unwanted exception"+e);}
  }

/**
* implemented. <br>
*
*/
  public void test_reset(){
    th.checkPoint("reset()void");
    CharArrayReader car = new CharArrayReader(ca);
    BufferedReader br = new BufferedReader(car,10);
    char c;
    try {
    	br.reset();
    	th.fail("should throw an IOException -- 1");
    	}
    catch(IOException ioe){ th.check(true);}
    try {
    	br.mark(3);
    	br.skip(4);
    	}
    catch(Exception e){ th.fail("got unwanted exception"+e);}
    try {
    	br.reset();
    	c = (char) br.read();
    	th.debug( c == 'e' ? "didn't throw the IOException":
    		"reset was executed, got:"+c);
    	}
    catch(IOException ioe){ th.check(true);}
    try {
    	br.mark(1);
    	br.read();
    	br.read();
    	}
    catch(Exception e){ th.fail("got unwanted exception"+e);}
    try {
	br.reset();
    	}
    catch(IOException ioe){ th.check(true);}
    try {
    	br.mark(3);
    	br.read(new char[5],0,5);
    	}
    catch(Exception e){ th.fail("got unwanted exception"+e);}
    try {
    	br.reset();
    	}
    catch(IOException ioe){ th.check(true);}
    car = new CharArrayReader(ca);
    br = new BufferedReader(car,10);
    try {
    	br.mark(5);
    	br.skip(4);
    	br.reset();
    	c = (char) br.read();
    	th.check( c == 'a' , "make sure we read first char -- 1");
    	br.skip(4);
    	br.reset();
    	c = (char) br.read();
    	th.check( c == 'a' , "make sure we can reset as much as we like -- 1");
    	br.skip(4);
    	br.reset();
    	c = (char) br.read();
    	th.check( c == 'a' , "make sure we can reset as much as we like -- 2");
    	br.skip(3);
	br.mark(15);
	br.read(new char[10]);
    	br.reset();
    	c = (char) br.read();
    	th.check( c == 'e' , "make sure reset works if the buffer needs to grow -- 1");
	}
    catch(Exception e){ th.fail("got unwanted exception"+e);}


  }

/**
* implemented. <br>
*
*/
  public void test_ready(){
    th.checkPoint("ready()boolean");
    CharArrayReader car = new CharArrayReader(ca);
    BufferedReader br = new BufferedReader(car,10);
    try {
    	th.check( br.ready() , "always true -- 1");
    	br.read();
    	th.check( br.ready() , "always true -- 2");
    	}
    catch(Exception e){ th.fail("got unwanted exception"+e);}
    br = new BufferedReader(new SMExReader(ca),10);
    try {
    	th.check( !br.ready() , "should be false -- 1");
    	br.read();
    	th.check( br.ready() , "should be true -- 2");
    	}
    catch(Exception e){ th.fail("got unwanted exception"+e);}


  }

/**
*   not implemented. <br>
*
*/
  public void test_close(){
    th.checkPoint("close()void");
    SMExReader car = new SMExReader(ca);
    BufferedReader xar = new BufferedReader(car,10);

    try {
        xar.close();
        xar.close();
        xar.close();
        xar.close();
        th.check(true);
        th.check( car.isClosed(), "make sure resources are released -- 1");
        }
    catch(Exception e) { th.fail("got unwanted exception: "+e+" -- 1"); }
    try { xar.read();
    	  th.fail("should throw IOException after close -- read() 1");
        }
    catch(IOException ioe){th.check(true);}
    try { xar.read(new char[6],1,3);
    	  th.fail("should throw IOException after close -- read() 2");
        }
    catch(IOException ioe){th.check(true);}
    try { xar.readLine();
    	  th.fail("should throw IOException after close -- readLine()");
        }
    catch(IOException ioe){th.check(true);}

    try { xar.skip(3L);
    	  th.fail("should throw IOException after close -- skip()");
        }
    catch(IOException ioe){th.check(true);}
    try { xar.mark(23);
    	  th.fail("should throw IOException after close -- mark()");
        }
    catch(IOException ioe){th.check(true);}
    try { xar.reset();
    	  th.fail("should throw IOException after close -- reset()");
        }
    catch(IOException ioe){th.check(true);}
    try { xar.ready();
    	  th.fail("should throw IOException after close -- ready()");
        }
    catch(IOException ioe){th.check(true);}


  }

/**
*   not implemented. <br>
*
*/
  public void test_lock(){
    th.checkPoint("lock(protected)java.lang.Object");
//th.debug("testing locks...");
    SMlockBufferedReader lt = new SMlockBufferedReader();
    CharArrayReader car = new CharArrayReader(ca);
    BufferedReader xr = new BufferedReader(car,100);
    int i = setupLockThread(lt,car);
    try { xr.mark(2);
//th.debug("marked Reader");
     	}
    catch(Exception e) { }
    th.check( i+1 == accesed , "accesed xr before lock was released -- mark");    	
    try { t.join(1000L); }	
    catch(Exception e) { }

//th.debug("next test ...");

    lt = new SMlockBufferedReader();
    i = setupLockThread(lt,car);
    try { xr.reset();
//th.debug("reseted Reader");
    	}
    catch(Exception e) {    }
    th.check( i+1 == accesed , "accesed xr before lock was released -- reset");    	
    try { t.join(1000L); }	
    catch(Exception e) { }

//th.debug("next test ...");

    lt = new SMlockBufferedReader();
    i = setupLockThread(lt,car);
    try { xr.read();
//th.debug("read from Reader");
	}
    catch(Exception e) {    }
    th.check( i+1 == accesed , "accesed xr before lock was released -- read()");    	
    try { t.join(1000L); }	
    catch(Exception e) { }
//th.debug("next test ...");

    lt = new SMlockBufferedReader();
    i = setupLockThread(lt,car);
    try { xr.read(new char[2]);
//th.debug("read from Reader");
	}
    catch(Exception e) {    }
    th.check( i+1 == accesed , "accesed xr before lock was released -- read(char[])");    	
    try { t.join(1000L); }	
    catch(Exception e) { }
//th.debug("next test ...");
    lt = new SMlockBufferedReader();
    i = setupLockThread(lt,car);
    try { xr.read(new char[2],0,1);
//th.debug("read from Reader");
	}
    catch(Exception e) {    }
    th.check( i+1 == accesed , "accesed xr before lock was released -- read(char[],int,int)");    	
    try { t.join(1000L); }	
    catch(Exception e) { }
//th.debug("next test ...");

    lt = new SMlockBufferedReader();
    i = setupLockThread(lt,car);
    try { xr.readLine();
//th.debug("read from Reader");
	}
    catch(Exception e) {    }
    th.check( i+1 == accesed , "accesed xr before lock was released -- readLine()");    	
    try { t.join(1000L); }	
    catch(Exception e) { }
//th.debug("next test ...");

    lt = new SMlockBufferedReader();
    i = setupLockThread(lt,car);
    try { xr.ready();
//th.debug("got ready boolean Reader");
	}
    catch(Exception e) {    }
    th.check( i+1 == accesed , "accesed xr before lock was released -- ready");    	
    try { t.join(1000L); }	
    catch(Exception e) { }
//th.debug("next test ...");
    lt = new SMlockBufferedReader();
    i = setupLockThread(lt,car);
    try { xr.skip(30L);
//th.debug("skipped chars Reader");
	}
    catch(Exception e) {   }
    th.check( i+1 == accesed , "accesed xr before lock was released -- skip");    	
    try { t.join(1000L); }	
    catch(Exception e) { }
//th.debug("next test ...");

    lt = new SMlockBufferedReader();
    i = setupLockThread(lt,car);
    try { xr.close();
//th.debug("closed the Reader");
	}
    catch(Exception e) {    }
    th.check( i+1 == accesed , "accesed xr before lock was released -- close");    	
    try { t.join(1000L); }	
    catch(Exception e) { }
  }    	

  public int setupLockThread(SMlockBufferedReader lt,CharArrayReader car) {

    f1 = false;
    lt.setXReader(car);
    lt.setTestHarness(th);
    lt.setRT(this);
    t = new Thread(lt);
    t.start();
    while (!f1) {
    	Thread.yield();
    }
//th.debug("returning from Setup");
    return accesed;
  }

  protected int accesed=0;

  public void inc() {
        accesed++;
  }

  private Thread t = null;
  private volatile boolean f1=false;

  public void set1(){
  	f1 = true;
  }
  	
}
