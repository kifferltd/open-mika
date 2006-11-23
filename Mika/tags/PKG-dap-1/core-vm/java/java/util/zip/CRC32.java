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
 * $Id: CRC32.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
 */
package java.util.zip;

public class CRC32 implements Checksum {

	private long crc=-1;
	
	public CRC32() {
	
	}
	
	public long getValue(){
	 	return (crc ^ -1);
	}
	
	public void reset(){
		crc=-1;
	}
	
	public void update(int bval) {
        	byte [] b = new byte[1];
        	b[0] = (byte) bval;
        	update(b, 0, 1);		
	}
	public void update(byte[] buf) {
	 	update(buf, 0, buf.length);
	}
	public native void update(byte[] buf, int off, int len);

}
