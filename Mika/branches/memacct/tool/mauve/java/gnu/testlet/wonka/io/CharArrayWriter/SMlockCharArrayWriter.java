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


package gnu.testlet.wonka.io.CharArrayWriter; //complete the package name ...
import java.io.*;
import gnu.testlet.TestHarness;


/**
*  this file contains a help class used for the java.io.CharArrayWriter test <br>
*  we have this help-class to test if the field lock is used to <br>
*  synchronize the CharArrayWriter methods
*/
class SMlockCharArrayWriter implements Runnable
	
{
	public void run()
	{
		synchronized (wr)
		{
	//th.debug("locked wr");
			wt.set1();
			try {Thread.sleep(150L);}
			catch (InterruptedException ie){}
	//th.debug("lockthread is releasing wr-lock");
			wt.inc();
		}
	}
	
        private SMCharArrayWriterTest wt;
	private TestHarness th;
        private CharArrayWriter wr;
	
        public void setWriter(CharArrayWriter writer) {
        	wr = writer;
        }
        public void setWT(SMCharArrayWriterTest wtest) {
        	wt = wtest;
        }
	public void setTestHarness(TestHarness harness) {
		th = harness;
	}
}
