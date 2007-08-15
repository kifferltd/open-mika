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

import java.util.HashSet;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectInput;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectOutput;
import java.io.Serializable;

public class UnserializeTest implements Serializable {

	private class DummyClass implements Serializable {
		int value;
	
		public DummyClass(int value) {
			this.value = value;
		}
	}

	public static void main(String[] argv) {
		System.out.println("Starting");
		new UnserializeTest().test();
		System.out.println("Stopping");
		System.exit(0);
	}
	
	private void test(){
		HashSet h = new HashSet();
		HashSet j = new HashSet();
		for(int i = 0; i<10; i++)
			h.add(new DummyClass(i));
			
		// Unserialize data
		try {
			FileOutputStream fo = new FileOutputStream("tmp");
			ObjectOutput oo = new ObjectOutputStream(fo);
			oo.writeObject(h);
			oo.close();
			fo.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			FileInputStream fi = new FileInputStream("tmp");
			ObjectInput oi = new ObjectInputStream(fi);
			j = (HashSet) oi.readObject();
			oi.close();
			fi.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
