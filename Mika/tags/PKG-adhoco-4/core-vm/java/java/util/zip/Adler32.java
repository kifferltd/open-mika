/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
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
