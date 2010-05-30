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



package gnu.testlet.wonka.io.FilterInputStream; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.*; // at least the class you are testing ...

/**
*  this file contains a help class used for the java.io.FilterInputStream test <br>
*  we have this help-class which extends FilterInputStream to <br>
*  acces the protected Field (InputStream) in
*/
public class SMInfoInputStream extends InputStream
{
	private boolean marked=false;
	private int ralimit = 10;
        public int off=0;
        public int len=0;
        public byte[] buf=null;

        public  SMInfoInputStream() {
        	super();
        }

        public int read(){
        	marked=true;

        	return 97;
        }

        public boolean markSupported() {
        	return marked;
        }

        public void mark(int ral) {
        	marked=true;
        	ralimit=ral;
        }

        public void close() {
        	marked=true;
        }

        public int available() {
        	marked=true;
        	return ralimit;
        }
        public void reset() {
        	marked=true;
        }

        public boolean isMarked() {
        	return marked;
        }
        	
        public int getRAL() {
        	return ralimit;
        }

        public int read(byte[] buffer) {
        	marked=true;
        	buf=buffer;
        	off=-1;
        	len=-1;
        	return 6;
        }

        public int read(byte[] buffer,int o,int l) {
        	marked=true;
        	buf=buffer;
        	off=o;
        	len=l;
        	return l;
        }

        public void clean() {
        	marked=false;
        	buf=null;
        	off=0;
        	len=0;
        }

}
