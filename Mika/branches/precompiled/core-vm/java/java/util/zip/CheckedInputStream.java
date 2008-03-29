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


/**
 * $Id: CheckedInputStream.java,v 1.2 2006/02/17 10:53:19 cvs Exp $
 */
package java.util.zip;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

public class CheckedInputStream extends FilterInputStream {
	
	private Checksum checksum;
	
	public CheckedInputStream(InputStream in, Checksum cs) {
		super(in);
		if (cs == null) {
		 	throw new NullPointerException();
		}
		checksum = cs;
	}
	
	public Checksum getChecksum() {
	 	return checksum;
	}

	public int read() throws IOException {
		int rd = in.read();
		if (rd != -1) {
			checksum.update(rd);
		}
		return rd;
	}
	
	public int read(byte [] buf, int offset, int len) throws IOException {
		int rd = in.read(buf, offset, len);
		if (rd != -1) {
			checksum.update(buf, offset, rd);
		}
		return rd;
		
	}
	
	public long skip(long n) throws IOException {
	 	if (n < 0) {
	 	 	throw new IllegalArgumentException();
	 	}
	 	long skipped = 0;
	 	int count=(n > 1024 ? 1024 : (int)n);;
	 	byte [] buf = new byte[count];
	 	int rd;
	 	while (n > 0) {
	 		rd = in.read(buf, 0, count);
			if (rd != -1) {
			 	skipped += rd;
				checksum.update(buf, 0, rd);
				n -= rd;			 	
			}
			else {
			 	break;
			}	 		
	 	}
	 	return skipped;
	}
}
