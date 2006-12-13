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


package gnu.testlet.wonka.io.Reader; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.Reader; // at least the class you are testing ...
import java.io.IOException;

/**
*  this file contains a help class used for the java.io.Reader test <br>
*  we have this help-class which extends Reader since <br>
*  Reader is an abstract class
*/
public class SMExReader extends Reader
{
 	private int readcount=0;
 	private int maxcount;
 	private boolean closed=false;

        public  SMExReader() {
        	super();
        	maxcount=3;
        }
        public  SMExReader(int mx) {
        	super();
        	maxcount=mx;
        }

        public SMExReader(Object o) {
             	super(o);
        	maxcount=3;
        }
        public void close() throws IOException {
        	closed=true;
        	//abstract method from Reader
        }

        public int read(char[] buf, int off, int count) throws IOException {
        	if ( off < 0 || count < 0 || buf.length < off +count )
        		throw new ArrayIndexOutOfBoundsException();
        	int i;
        	for (i=off; i < off + count; i++) {
        		readcount++;
        		if  (maxcount < readcount) break;
        		buf[i] = 'a';
        	}
        	if (i == off) return -1;
        	return i-off;
        }
        public int getRC() {
        	return readcount;
        }
        public boolean isClosed() {
        	return closed;
        }

        public Object getLock() {
        	return lock;
        }        	
}
