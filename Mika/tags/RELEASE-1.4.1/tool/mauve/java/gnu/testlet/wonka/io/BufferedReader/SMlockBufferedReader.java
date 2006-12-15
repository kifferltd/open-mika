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


package gnu.testlet.wonka.io.BufferedReader; //complete the package name ...
import java.io.*;
import gnu.testlet.TestHarness;


/**
*  this file contains a help class used for the java.io.BufferedReader test <br>
*  we have this help-class to test if the field lock is used to <br>
*  synchronize the BufferedReader methods
*/
class SMlockBufferedReader implements Runnable
	
{
	public void run()
	{
		synchronized (xr)
		{
			rt.set1();
	//th.debug("locked xr");
			try {Thread.sleep(150L);}
			catch (InterruptedException ie){}
	//th.debug("lockthread is releasing xr-lock");
			rt.inc();
						
		}
	}
	
        private SMBufferedReaderTest rt;
	private TestHarness th;
        private CharArrayReader xr;
	
        public void setXReader(CharArrayReader xreader) {
        	xr = xreader;
        }
        public void setRT(SMBufferedReaderTest rtest) {
        	rt = rtest;
        }
	public void setTestHarness(TestHarness harness) {
		th = harness;
	}
}
