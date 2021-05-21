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


package gnu.testlet.wonka.io.PipedReader; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.*; // at least the class you are testing ...

/**
*  this file contains test for java.io.PipedReader and PipedWriter ... <br>
*/
public class SMPipedReaderTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.io.PipedWriter");
       test_PipedWriter();
       th.setclass("java.io.PipedReader");
       test_PipedReader();
       test_connect();
       test_close();
       test_ready();
       test_thread();
       test_read_write();
     }

/**
* implemented. <br>
*
*/
  public void test_PipedReader(){
    th.checkPoint("PipedReader(java.io.PipedWriter)");
    try {
     	new PipedWriter(null);
     	th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException npe) {
     	th.check(true , "caught exception ...");
    }
    catch(IOException ieo) { th.fail("Constructor should not throw an IOException here"); }

  }

/**
* implemented. <br>
*
*/
  public void test_PipedWriter(){
    th.checkPoint("PipedWriter(java.io.PipedReader)");
    try {
     	new PipedWriter(null);
     	th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException npe) {
     	th.check(true , "caught exception ...");
    }
    catch(IOException ieo) { th.fail("Constructor should not throw an IOException here"); }

  }

/**
*  implemented. <br>
*
*/
  public void test_connect(){
    th.checkPoint("connect");
    PipedReader pr = new PipedReader();
    PipedWriter pw=null;
    try {
    	pw = new PipedWriter(pr);
    	pw.write("test".toCharArray(),0,4);
    	char[] buf = new char[4];
    	pr.read(buf,0,4);
    	th.check(new String(buf) , "test" , "testing connection");
    }
    catch(IOException ioe) { th.fail("no IOException expected -- 1"); }
    try {
     	new PipedWriter(pr);
     	th.fail("should throw an IOException -- 1");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 1"); }
    try {
     	new PipedReader(pw);
     	th.fail("should throw an IOException -- 2");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 2"); }
    try {
     	pr.connect(new PipedWriter());
     	th.fail("should throw an IOException -- 3");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 3"); }
    try {
     	pw.connect(new PipedReader());
     	th.fail("should throw an IOException -- 4");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 4"); }
    //closed PipedReader/Writer cannot connect
    try {
    	pr = new PipedReader();
    	pr.close();
    	pw = new PipedWriter();
    	pw.close();
    }
    catch(IOException ioe) { th.fail("no IOException expected -- 2"); }
    try {
     	new PipedWriter(pr);
     	th.fail("should throw an IOException -- 5");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 5"); }
    try {
     	new PipedReader(pw);
     	th.fail("should throw an IOException -- 6");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 6"); }
    try {
     	pr.connect(new PipedWriter());
     	th.fail("should throw an IOException -- 7");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 7"); }
    try {
     	pw.connect(new PipedReader());
     	th.fail("should throw an IOException -- 8");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 8"); }
    pr = new PipedReader();
    pw = new PipedWriter();
    try {
     	pr.connect(null);
     	th.fail("should throw a NullPointerException -- 1");
    }
    catch(NullPointerException npe) { th.check(true , "caught NullPointerException -- 1"); }
    catch(IOException ioe) { th.fail("no IOException expected -- 3"); }
    try {
     	pw.connect(null);
     	th.fail("should throw a NullPointerException -- 2");
    }
    catch(NullPointerException npe) { th.check(true , "caught NullPointerException -- 2"); }
    catch(IOException ioe) { th.fail("no IOException expected -- 4"); }



  }

/**
*  implemented. <br>
*
*/
  public void test_ready(){
    th.checkPoint("ready()boolean");
    try {
    	PipedReader pr = new PipedReader();
    	PipedWriter pw = new PipedWriter(pr);
    	th.check(!pr.ready() , "nothing availble -- 1");
    	pw.write("testing".toCharArray(),0,5);
    	th.check(pr.ready() , "things availble");
    	char[] buf = new char[4];
    	pr.read(buf,0,4);
    	th.check(pr.ready() , "one char availble");
    	pr.read();
    	th.check(!pr.ready() , "nothing availble -- 2");
    	
    }
    catch(IOException ioe) { th.fail("no IOException expected -- 1"); }
  }

/**
*  implemented. <br>
*
*/
  public void test_thread(){
    th.checkPoint("read()int");
    PipedReader pr = new PipedReader();
    SMPipedThread pt = new SMPipedThread(pr,th);
    Thread t = new Thread(pt);
    t.start();
    while (t.isAlive()) {
     	Thread.yield();
    }
    try {
        pr.read();
        th.fail("should throw an IOException -- 1");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 1"); }
    th.checkPoint("read(char[],int,int)int");
    pr = new PipedReader();
    pt = new SMPipedThread(pr,th);
    t = new Thread(pt);
    t.start();
    while (t.isAlive()) {
     	Thread.yield();
    }
    try {
        pr.read(new char[3],1,2);
        th.fail("should throw an IOException -- 2");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 2"); }
    PipedWriter pw = new PipedWriter();
    pt = new SMPipedThread(pw,th);
    t = new Thread(pt);
    t.start();
    while (t.isAlive()) {
     	Thread.yield();
    }
    try {
        pw.write(new char[3],1,2);
        th.fail("should throw an IOException -- 3");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 3"); }
    pw = new PipedWriter();
    pt = new SMPipedThread(pw,th);
    t = new Thread(pt);
    t.start();
    while (t.isAlive()) {
     	Thread.yield();
    }
    try {
        pw.write(2);
        th.fail("should throw an IOException -- 4");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 4"); }
  }

/**
*  implemented. <br>
*
*/
  public void test_close(){
    th.checkPoint("close()void");
    PipedReader pr = new PipedReader();
    PipedWriter pw=null;
    try {
    	pw = new PipedWriter(pr);
      pw.close();
      pw.close();
      pr.close();
      pr.close();
      pw.close();
    }
    catch(IOException ioe) { th.fail("no IOException expected -- 1"); }
    try {
     	pr.read();
     	th.fail("should throw an IOException -- 1");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 1"); }
    try {
     	pr.read(new char[2],0,1);
     	th.fail("should throw an IOException -- 2");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 2"); }
    try {
     	pr.ready();
     	th.fail("should throw an IOException -- 3");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 3"); }
    try {
     	pw.write(1);
     	th.fail("should throw an IOException -- 4");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 4"); }
    try {
     	pw.write(new char[2],0,1);
     	th.fail("should throw an IOException -- 5");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 5"); }
    try {
     	pw.flush();
     	th.fail("should throw an IOException -- 6");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 6"); }
  }

/**
*  implemented. <br>
*
*/
  public void test_read_write(){
    th.checkPoint("read/write");
    PipedWriter pw = new PipedWriter();
    SMPipedThread pt = new SMPipedThread(pw,th);
    pt.setAlive(false);
    pt.setPRT(this);
    Thread t = new Thread(pt);
    t.start();
    String s = buildString();
    try {
    	while (!go) {
	     	Thread.yield();
    	}
	pw.write(s.toCharArray());    	
        pt.stop();
    }
    catch(IOException ioe) { th.fail("no IOException expected -- 1"); }
    while (t.isAlive()) {
     	Thread.yield();
    }
    String readedS = pt.getBuffer();
    th.check(readedS, s , "reading is OK ...");
    pw = new PipedWriter();
    PipedReader pr = new PipedReader();
    try {
    	pr.read();
     	th.fail("should throw an IOException -- 1");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 1"); }
    try {
    	pr.read(new char[2],0,1);
     	th.fail("should throw an IOException -- 2");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 2"); }
    try {
    	pw.write(1);
     	th.fail("should throw an NullPointerException -- 3");
    }
    catch(IOException ioe) { th.check(true, "IOException expected -- 2"); }
    try {
    	pw.write(new char[2],0,1);
     	th.fail("should throw an NullPointerException -- 4");
    }
    catch(IOException ioe) { th.check(true,"IOException expected -- 3"); }
    try {
        pr.connect(pw);
        char[] buf = new char[4];
        pw.write(buf,0,4);
        try {
         	pr.read(buf, -1, 0);
         	th.fail("should throw IndexOutOfBoundsException -- 1");		
        }
        catch(IndexOutOfBoundsException ioobe) { th.check(true); }
        try {
         	pr.read(buf, 1, -1);
         	th.fail("should throw IndexOutOfBoundsException -- 2");		
        }
        catch(IndexOutOfBoundsException ioobe) { th.check(true); }
        try {
         	pr.read(buf, 2, 3);
         	th.fail("should throw IndexOutOfBoundsException -- 3");		
        }
        catch(IndexOutOfBoundsException ioobe) { th.check(true); }
        try {
         	pw.write(buf, -1, 2);
         	th.fail("should throw IndexOutOfBoundsException -- 4");		
        }
        catch(IndexOutOfBoundsException ioobe) { th.check(true); }
        try {
         	pw.write(buf, 1, -2);
         	th.fail("should throw IndexOutOfBoundsException -- 5");		
        }
        catch(IndexOutOfBoundsException ioobe) { th.check(true); }
        try {
         	pw.write(buf, 3, 2);
         	th.fail("should throw IndexOutOfBoundsException -- 6");		
        }
        catch(IndexOutOfBoundsException ioobe) { th.check(true); }
        try {
         	pw.write((char[])null, 3, 2);
         	th.fail("should throw NullPointerException -- 1");		
        }
        catch(NullPointerException npe) { th.check(true); }
        try {
         	pr.read((char[])null, 2, 3);
         	th.fail("should throw NullPointerException -- 2");		
        }
        catch(NullPointerException npe) { th.check(true); }

    }
    catch(IOException ioe) { th.fail("no IOException expected -- 4"); }

  }

  private String buildString() {
   	StringBuffer b = new StringBuffer();
   	int j=0;
   	for (int i=0 ; i < 9*32 ; i++) {
		b.append("ACUNIA"+j+" ");   	
		if (j == 9 ) {
		   	b.setCharAt(b.length()-1 , '\n');
		}
   		j = (j+1)%10;
   	}	
   	b.setCharAt(b.length()-1 , '\n');
   	b.setCharAt(0 , '\n');
   	return new String(b);
  }

  private boolean go=false;

  public void go() {
   	go = true;
  }
}
