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
