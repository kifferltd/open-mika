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


package gnu.testlet.wonka.io.CharArrayReader; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.CharArrayReader; // at least the class you are testing ...
import java.io.IOException;
/**
*  this file contains a help class used for the java.io.Reader test <br>
*  we have this help-class which extends Reader since <br>
*  Reader is an abstract class
*/
public class SMExCharArrayReader extends CharArrayReader
{
 	private int readcount=0;

        public  SMExCharArrayReader(char[] buffer) {
        	super(buffer);
        }
        public SMExCharArrayReader(char[] buffer, int i, int j) {
             	super(buffer,i,j);
        }

        public Object getLock() {
        	return lock;
        }

        public char[] getbuf() {
        	return buf;
        }
        public int getcount() {
        	return count;
        }
        public int getmpos() {
        	return markedPos;
        }
        public int getpos() {
        	return pos;
        }  	
}
