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
import java.io.*;
import gnu.testlet.TestHarness;


/**
*  this file contains a help class used for the java.io.PipedInputStream test <br>
*  we have this help-class to test multithreadedPipe <br>
*
*/
class AcuniaPipedStreamThread implements Runnable {
  
  public void run() {
    //th.debug("starting PipedStreamThread ...");
    if (Alive) {
       isAliveTest();       
    }    
    else {
       blockingTest();
    }
    //th.debug("PipedStreamThread has finished...");
  }
  
  private TestHarness th;
        private PipedInputStream pis;
        private PipedOutputStream pos;
  private boolean Alive=true;
        private boolean stop=false;
        private StringBuffer buffer;
        private AcuniaPipedStreamTest pist;
        boolean connected;

  public AcuniaPipedStreamThread(PipedInputStream ppis, TestHarness ths) {
     pis = ppis;
     th = ths;
  }
  
  public AcuniaPipedStreamThread(PipedOutputStream ppos, TestHarness ths) {
     pos = ppos;
     th = ths;
  }

  public void stop() {
     stop = true;
  }
  
  public void setPRT(AcuniaPipedStreamTest pist) {
     this.pist = pist;
  }
  
  public void setAlive(boolean cnd) {
     Alive = cnd;
  }
  
  public String getBuffer() {
     return new String(buffer);
  }
  
  private void blockingTest(){
    buffer = new StringBuffer();
    int rd = 0;
    try {
      pis = new PipedInputStream(pos);
      pist.go();
      byte[] buf = new byte[357];
      while (!stop || pis.available() > 0){
        buffer.append((char)pis.read());
        int got = pis.read(buf);
        rd += got + 1;
        buffer.append(new String(buf,0,got));
        Thread.yield();
      }
    }
    catch(IOException ioe) {
      th.fail("IOException in PipedStreamThread while Constructing a PipedInputStream");
      pist.go();
    }    
    catch(Throwable t) {
      th.fail(t+"in PipedStreamThread while Constructing a PipedInputStream");
      t.printStackTrace();
      pist.go();
    }    
        
  }

  private void isAliveTest(){
    if (pis == null) {
      //th.debug("constructing a PipedInputStream");
      try {
        pis = new PipedInputStream(pos);
        connected = true;
        th.check(pis.read(), 1 ,"one byte written");
      }
      catch(IOException ioe) {
        th.fail("IOException in PipedStreamThread while Constructing a PipedInputStream");
      }    
    }
    else {
      //th.debug("constructing a PipedOutputStream");
      try {
        pos = new PipedOutputStream(pis);
        connected = true;
        pos.write(1);
      }
      catch(IOException ioe) {
        th.fail("IOException in PipedStreamThread while Constructing a PipedOutputStream");
      }
    }
  }
}
