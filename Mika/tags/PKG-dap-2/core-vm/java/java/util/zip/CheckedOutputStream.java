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
 * $Id: CheckedOutputStream.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
 */
package java.util.zip;

import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.IOException;

public class CheckedOutputStream extends FilterOutputStream {
		
	private Checksum checksum;	

	public CheckedOutputStream(OutputStream out, Checksum cs) {
	 	super(out);
	 	if (cs == null) {	 	
			throw new NullPointerException();
		}
		checksum = cs;
	}
	
        public Checksum getChecksum(){
         	return checksum;
        }
	
	public void write(int bval) throws IOException {
		out.write(bval);
		checksum.update(bval);		
	}	
	public void write(byte [] buf, int offset, int len) throws IOException {
		out.write(buf, offset, len);
		checksum.update(buf, offset, len);		
			
	}
}
