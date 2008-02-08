/**************************************************************************
* Copyright  (c) 2001, 2002 by Acunia N.V. All rights reserved.           *
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


package gnu.testlet.wonka.io.PipedStreams;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.*; // at least the class you are testing ...

/**
*  this file contains test for java.io.PipedInputStream and PipedOutputStream ... <br>
*/
public class AcuniaPipedStreamTest implements Testlet
{
  protected TestHarness th;

  public void test (TestHarness harness)
    {
       th = harness;
       th.setclass("java.io.PipedOutputStream");
       test_PipedOutputStream();
       th.setclass("java.io.PipedInputStream");
       test_PipedInputStream();
       test_connect();
       test_close();
       test_available();
       test_thread();
       test_read_write();
     }

/**
* implemented. <br>
*
*/
  public void test_PipedInputStream(){
    th.checkPoint("PipedInputStream(java.io.PipedOutputStream)");
    try {
       new PipedOutputStream(null);
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
  public void test_PipedOutputStream(){
    th.checkPoint("PipedOutputStream(java.io.PipedInputStream)");
    try {
       new PipedOutputStream(null);
       th.fail("should throw a NullPointerException");
    }
    catch(NullPointerException npe) {
       th.check(true , "caught exception ...");
    }
    catch(IOException ieo) { th.fail("Constructor should not throw an IOException here"); }

    th.checkPoint("flush()void");
    try {
       new PipedOutputStream().flush();
       th.check(true, "unconnected stream be flushed");
    }
    catch(IOException ieo) { th.fail("unconnected stream be flushed, but got Exception"); }
  }

/**
*  implemented. <br>
*
*/
  public void test_connect(){
    th.checkPoint("connect()void");
    PipedInputStream pis = new PipedInputStream();
    PipedOutputStream pos=null;
    try {
      String test = "The quick brown ferret slunk through the twisty pipe.";
      pos = new PipedOutputStream(pis);
      pos.write(test.getBytes());
      byte[] buf = new byte[test.length()];
      pis.read(buf);
      th.check(new String(buf) , test, test);
    }
    catch(IOException ioe) { ioe.printStackTrace(); th.fail("no IOException expected -- 1"); }
    try {
       new PipedOutputStream(pis);
       th.fail("should throw an IOException -- 1");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 1"); }
    try {
       new PipedInputStream(pos);
       th.fail("should throw an IOException -- 2");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 2"); }
    try {
       pis.connect(new PipedOutputStream());
       th.fail("should throw an IOException -- 3");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 3"); }
    try {
       pos.connect(new PipedInputStream());
       th.fail("should throw an IOException -- 4");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 4"); }

    pis = new PipedInputStream();
    pos = new PipedOutputStream();
    try {
       pis.connect(null);
       th.fail("should throw a NullPointerException -- 1");
    }
    catch(NullPointerException npe) { th.check(true , "caught NullPointerException -- 1"); }
    catch(IOException ioe) { th.fail("no IOException expected -- 3"); }
    try {
       pos.connect(null);
       th.fail("should throw a NullPointerException -- 2");
    }
    catch(NullPointerException npe) { th.check(true , "caught NullPointerException -- 2"); }
    catch(IOException ioe) { th.fail("no IOException expected -- 4"); }



  }

/**
*  implemented. <br>
*
*/
  public void test_available(){
    th.checkPoint("available()int");
    try {
      PipedInputStream pis = new PipedInputStream();
      PipedOutputStream pos = new PipedOutputStream(pis);
      th.check(pis.available() == 0 , "nothing available -- 1");
      pos.write("testing".getBytes(),0,7);
      int avail = pis.available();
      th.check(avail  > 0, avail+" bytes available");
      byte[] buf = new byte[4];
      pis.read(buf,0,4);
        avail = pis.available();
        if (avail != 3) {
          th.fail("pis.available() returned "+avail+", expected 3");
        }
        else {
        th.check(true , "three bytes available");
      }
  pis.read();
      pis.read();
      pis.read();
      th.check(pis.available() == 0, "nothing available -- 2");
      
    }
    catch(IOException ioe) { th.fail("no IOException expected -- 1"); }
  }

/**
*  implemented. <br>
*
*/
  public void test_thread(){
    th.checkPoint("read()int");
    PipedInputStream pis = new PipedInputStream();
    AcuniaPipedStreamThread pt = new AcuniaPipedStreamThread(pis,th);
    Thread t = new Thread(pt);
    try {
        pis.read();
        th.fail("should throw an IOException -- 1");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 1"); }
    t.start();


    while (t.isAlive()) {
       Thread.yield();
    }
    try {
      th.check(pis.read(), 1 , "checking first read");
    }
    catch(IOException ioe) { th.fail("no IOException expected -- 1"); }
    try {
        pis.read();
        th.fail("should throw an IOException -- 2");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 2"); }

    th.checkPoint("read(byte[],int,int)int");

    pis = new PipedInputStream();
    pt = new AcuniaPipedStreamThread(pis,th);
    t = new Thread(pt);
    try {
        pis.read(new byte[3],1,2);
        th.fail("should throw an IOException -- 3");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 3"); }
    t.start();
    while (t.isAlive()) {
       Thread.yield();
    }
    try {
      th.check(pis.read(new byte[1],0,1), 1 , "checking first read");
    }
    catch(IOException ioe) { th.fail("no IOException expected -- 2"); }
    try {
        pis.read(new byte[3],1,2);
        th.fail("should throw an IOException -- 4");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 4"); }

    th.checkPoint("write(byte[],int,int)void");
    PipedOutputStream pos = new PipedOutputStream();
    pt = new AcuniaPipedStreamThread(pos,th);
    t = new Thread(pt);

    t.start();


    while (!pt.connected) {
       Thread.yield();
    }

    try {
        byte[] b = new byte[]{1};
        pos.write(b,0,1);
    }
    catch(IOException ioe) { th.fail("no IOException expected -- 3 "+ioe); }

    while (t.isAlive()) {
       Thread.yield();
    }
    try {
        pos.write(new byte[3],1,2);
        th.fail("should throw an IOException -- 5");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 5"); }

    th.checkPoint("write(int)void");
    pos = new PipedOutputStream();
    pt = new AcuniaPipedStreamThread(pos,th);
    t = new Thread(pt);

    t.start();

    while (!pt.connected) {
       Thread.yield();
    }

    try {
      pos.write(1);
    }
    catch(IOException ioe) { th.fail("no IOException expected -- 4"); }


    while (t.isAlive()) {
       Thread.yield();
    }
    try {
        pos.write(2);
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
    PipedInputStream pis = new PipedInputStream();
    PipedOutputStream pos=null;
    try {
      pos = new PipedOutputStream(pis);
        pos.close();
        pos.close();
        pis.close();
        pis.close();
        pos.close();
    }
    catch(IOException ioe) { th.fail("no IOException expected -- 1"); }
    try {
       pis.read();
       th.fail("should throw an IOException -- 1");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 1"); }
    try {
       pis.read(new byte[2],0,1);
       th.fail("should throw an IOException -- 2");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 2"); }
    try {
       th.check(pis.available(),0);
    }
    catch(IOException ioe) { th.fail("caught IOException -- 3"); }
    try {
       pos.write(1);
       th.fail("should throw an IOException -- 4");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 4"); }
    try {
       pos.write(new byte[2],0,1);
       th.fail("should throw an IOException -- 5");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 5"); }
    try {
       pos.flush();
       th.check(true, "should throw not IOException -- 6");
    }
    catch(IOException ioe) { th.fail("caught IOException -- 6"); }
  }

/**
*  implemented. <br>
*
*/
  public void test_read_write(){
    th.checkPoint("read/write");
    PipedOutputStream pos = new PipedOutputStream();
    AcuniaPipedStreamThread pt = new AcuniaPipedStreamThread(pos,th);
    pt.setAlive(false);
    pt.setPRT(this);
    Thread t = new Thread(pt);
    t.start();
    String s = buildString();
    try {
      while (!go) {
         Thread.yield();
      }
      pos.write(s.getBytes());
      pt.stop();
    }
    catch(IOException ioe) { th.fail("no IOException expected -- 1"); }
    while (t.isAlive()) {
       Thread.yield();
    }
    String readedS = pt.getBuffer();
    th.check(readedS, s , "reading is OK ... -- 1");
    th.check(readedS.length(), s.length() , "reading is OK ... -- 2");
    pos = new PipedOutputStream();
    PipedInputStream pis = new PipedInputStream();
    try {
      pis.read();
       th.fail("should throw an IOException -- 1");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 1"); }
    try {
      pis.read(new byte[2],0,1);
       th.fail("should throw an IOException -- 2");
    }
    catch(IOException ioe) { th.check(true , "caught IOException -- 2"); }
    try {
      pos.write(1);
       th.fail("should throw an NullPointerException -- 3");
    }
    catch(IOException ioe) { th.check(true, "caught IOException"); }
    try {
      pos.write(new byte[2],0,1);
       th.fail("should throw an NullPointerException -- 4");
    }
    catch(IOException ioe) { th.check(true, "caught IOException"); }
    try {
        pis.connect(pos);
        byte[] buf = new byte[4];
        pos.write(buf,0,4);
        try {
           pis.read(buf, -1, 0);
           th.fail("should throw IndexOutOfBoundsException -- 1");    
        }
        catch(IndexOutOfBoundsException ioobe) { th.check(true); }
        try {
           pis.read(buf, 1, -1);
           th.fail("should throw IndexOutOfBoundsException -- 2");    
        }
        catch(IndexOutOfBoundsException ioobe) { th.check(true); }
        try {
           pis.read(buf, 2, 3);
           th.fail("should throw IndexOutOfBoundsException -- 3");    
        }
        catch(IndexOutOfBoundsException ioobe) { th.check(true); }
        try {
           pos.write(buf, -1, 2);
           th.fail("should throw IndexOutOfBoundsException -- 4");    
        }
        catch(IndexOutOfBoundsException ioobe) { th.check(true); }
        try {
           pos.write(buf, 1, -2);
           th.fail("should throw IndexOutOfBoundsException -- 5");    
        }
        catch(IndexOutOfBoundsException ioobe) { th.check(true); }
        try {
           pos.write(buf, 3, 2);
           th.fail("should throw IndexOutOfBoundsException -- 6");    
        }
        catch(IndexOutOfBoundsException ioobe) { th.check(true); }
        try {
           pos.write((byte[])null, 3, 2);
           th.fail("should throw NullPointerException -- 1");    
        }
        catch(NullPointerException npe) { th.check(true); }
        try {
           pis.read((byte[])null, 2, 3);
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
     //th.debug("Test string:\n'"+b+"'");
     return new String(b);

  }

  private boolean go=false;

  public void go() {
     go = true;
  }
}
