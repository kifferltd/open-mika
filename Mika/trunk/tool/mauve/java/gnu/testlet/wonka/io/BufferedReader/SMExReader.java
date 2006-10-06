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

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.IOException;
import java.io.Reader;
import java.io.CharArrayReader;

/**
*  this file contains a help class used for the java.io.BufferedReader test <br>
*  we have this help-class which extends BufferedReader to <br>
*  overwrite the private fields of BufferedReader and  make them protected
*/
public class SMExReader extends CharArrayReader
{

        public  SMExReader(char[] ca) {
        	super(ca);
        }

        public Object getLock() {
        	return lock;
        }

        public boolean ready() throws IOException{
        	return false;
        }
	
        public boolean isClosed() {
        	try {	this.skip(0L);
        		return false;
        	    }
        	 catch(IOException ioe) { return true;}
        }
}
