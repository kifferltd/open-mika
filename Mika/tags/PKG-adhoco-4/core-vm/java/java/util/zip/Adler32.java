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

/*
** $Id: Adler32.java,v 1.2 2006/02/17 10:53:19 cvs Exp $
*/

package java.util.zip;

/**
*         Adler-32 is composed of two sums accumulated per byte: s1 is
*         the sum of all bytes, s2 is the sum of all s1 values. Both sums
*         are done modulo 65521. s1 is initialized to 1, s2 to zero.  The
*         Adler-32 checksum is stored as s2*65536 + s1 in most-
*         significant-byte first (network) order.
*/	
public class Adler32 implements Checksum {

	private int s1=1;
	private int s2;
	
	
	public Adler32() { }
	
	public long getValue(){
	 	return s2 * 65536L + s1;
	}
	
	public void reset(){
		s1=1;
		s2=0;
	}
	
	public void update(int bval) {
		s1 = ((0x0FF & bval) + s1) % 65521;
		s2 = (s2 + s1) % 65521;	
		
	}
	public void update(byte[] buf) {
	 	update(buf, 0, buf.length);
	}
	/**
	* this method is not that hard to do in java code, but it is very likely that large byte arrays are passed
	* to this method --> so it can be justified to get the job done in native code ...
	*/
	public native void update(byte[] buf, int off, int len);

}
