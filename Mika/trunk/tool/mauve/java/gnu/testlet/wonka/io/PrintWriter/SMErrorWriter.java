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


package gnu.testlet.wonka.io.PrintWriter; //complete the package name ...

import java.io.IOException;
import java.io.Writer;

/**
*  this file contains a help class used for the java.io.OutputStream test <br>
*  we have this help-class which extends OutputStream since <br>
*  OutputStream is an abstract class
*/
public class SMErrorWriter extends Writer
{
	private int closed=0;
	private boolean errorOnClose;
        private boolean flushed=false;
        public  SMErrorWriter() {
        	super();
        	errorOnClose=true;
	}
        public  SMErrorWriter(int i) {
        	super();
        	errorOnClose=false;
	}
	
	public boolean isFlushed() {
		return flushed;
	}
	
	public int timesClosed() {
		return closed;
	}
	
	
	public void close() throws IOException {
	 	if ( errorOnClose )
	 		throw new IOException();
	 	closed++;
	}
	public void flush() throws IOException {
	 	flushed=true;
	 	throw new IOException();
	}
	public void write(int i) throws IOException {
	 	throw new IOException();
	}

	public void write(String s) throws IOException {
	 	throw new IOException();
	}

	public void write(String s,int i,int j) throws IOException {
	 	throw new IOException();
	}
	
	public void write(char[] s) throws IOException {
	 	throw new IOException();
	}

	public void write(char[] s,int i,int j) throws IOException {
	 	throw new IOException();
	}
}
