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

import java.io.BufferedInputStream; // at least the class you are testing ...
import java.io.InputStream;
import java.io.IOException;


public class SMExBufferedInputStream extends BufferedInputStream
{
	private int timesclosed=0;

        public  SMExBufferedInputStream(InputStream instream) {
        	super(instream);
        }
        	
        public  SMExBufferedInputStream(InputStream instream,int size) {
        	super(instream,size);
        }

	public String toString() {
		return new String(buf,0,count);
	}

	public byte[] getbuf() {
		return buf;
	}
	
	public int getcount() {
		return count;
	}

	public int getpos() {
		return pos;
	}

	public int getmarkpos() {
		return markpos;
	}

	public int getmarklimit() {
		return marklimit;
	}
	
	public void close() throws IOException {
		timesclosed++;
		super.close();
	}
	
	public int getTC() {
		return timesclosed;
	}
		
	
}
