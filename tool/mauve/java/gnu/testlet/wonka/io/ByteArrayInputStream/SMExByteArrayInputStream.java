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



package gnu.testlet.wonka.io.ByteArrayInputStream; //complete the package name ...

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

import java.io.*; // at least the class you are testing ...

/**
*  this file contains a help class used for the java.io.ByteArrayInputStream test <br>
*  we have this help-class which extends ByteArrayInputStream to retrieve <br>
* the protected fields of ByteArrayInputStream
*/
public class SMExByteArrayInputStream extends ByteArrayInputStream
{
        public  SMExByteArrayInputStream(byte[] buffer) {
        	super(buffer);
        }
        public  SMExByteArrayInputStream(byte[] buffer,int offset, int count) {
        	super(buffer, offset, count);
        }

	public byte[] get_buf()  {
        	return buf;
 	}
	public int get_count()  {
        	return count;
 	}
	public int get_mark()  {
        	return mark;
 	}
	public int get_pos()  {
        	return pos;
 	}
        	


}
