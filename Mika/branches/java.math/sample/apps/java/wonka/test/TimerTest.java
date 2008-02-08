/**************************************************************************
* Copyright (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.          *
* All rights reserved.                                                    *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of /k/ Embedded Java Solutions nor the names of     *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL /K/ EMBEDDED JAVA SOLUTIONS OR OTHER CONTRIBUTORS BE  *
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR     *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

package wonka.test;

import java.util.Timer;
import java.util.TimerTask;
import java.lang.InterruptedException;

public class TimerTest {
	private static final int NB = 10;
	int index;
	long i[];
	long j[];

	private class TestTimerTask extends TimerTask {
		public void run() {
			j[TimerTest.this.index] = System.currentTimeMillis();
			System.out.println("delta = " + (j[TimerTest.this.index] - i[TimerTest.this.index]));
			synchronized(TimerTest.this) {
				TimerTest.this.notify();
			}
		}
	}

	public static void main(String[] argv) {
		System.out.println("Starting");
		int delta = 1000; 
		if(argv.length == 1)
			delta = Integer.parseInt(argv[0]);
		new TimerTest().test(delta);
		System.out.println("Stopping");
		System.exit(0);
	}
	
	private void test(int delta) {
		this.i = new long[NB];
		this.j = new long[NB];
		System.out.println(NB+" tries with delta = "+delta+"ms.");
		try {
		  Timer t = new Timer();
			for(index = 0; index < NB; index++) { 
				synchronized(this) {
					// was here : Timer t = new Timer();
					System.out.println("scheduling task # "+index);
					this.i[index] = System.currentTimeMillis();
					t.schedule(new TestTimerTask(), delta);
					this.wait();
				}
			}
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
		
		long res = 0;
		for(index = 0; index < NB; index++) {
			res += this.j[index] - this.i[index];
		}
		System.out.println("average delta = " + res/NB);
	}
}
