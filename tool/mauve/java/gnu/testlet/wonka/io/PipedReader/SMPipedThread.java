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
import java.io.*;
import gnu.testlet.TestHarness;


/**
*  this file contains a help class used for the java.io.PipedReader test <br>
*  we have this help-class to test multithreadedPipe <br>
*
*/
class SMPipedThread implements Runnable {
	
	public void run() {
		//th.debug("starting PipedThread ...");
		if (Alive) {
		 	isAliveTest();		 	
		}		
		else {
		 	blockingTest();
		}
		//th.debug("PipedThread has finished...");
	}
	
	private TestHarness th;
        private PipedReader pr;
        private PipedWriter pw;
	private boolean Alive=true;
        private boolean stop=false;
        private StringBuffer buffer;
        private SMPipedReaderTest prt;

	public SMPipedThread(PipedReader ppr, TestHarness ths) {
	 	pr = ppr;
	 	th = ths;
	}
	
	public SMPipedThread(PipedWriter ppw, TestHarness ths) {
	 	pw = ppw;
	 	th = ths;
	}

	public void stop() {
	 	stop = true;
	}
	
	public void setPRT(SMPipedReaderTest prt) {
	 	this.prt = prt;
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
  			pr = new PipedReader(pw);
  			prt.go();
  			char[] buf = new char[255];
			while (!stop || pr.ready()){
			    rd += pr.read(buf,0,255);
			    //th.debug("read buffer:\n"+new String(buf));
			    buffer.append(buf);
			    buffer.append((char)pr.read());
			    rd++;
			    Thread.yield();
			}
			//th.debug("read "+rd+" characters");			
		}
		catch(IOException ioe) {
			th.fail("IOException in PipedThread while Constructing a PipedReader");
		}		
				
	}

	private void isAliveTest(){
		if (pr == null) {
			try {
			    pr = new PipedReader(pw);
			}
			catch(IOException ioe) {
				th.fail("IOException in PipedThread while Constructing a PipedReader");
			}		
		}
		else {
			try {
			    pw = new PipedWriter(pr);
			}
			catch(IOException ioe) {
				th.fail("IOException in PipedThread while Constructing a PipedWriter");
			}
	        }
	}
}
