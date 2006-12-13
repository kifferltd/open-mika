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
 * $Id: GZIPOutputStream.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
 */
package java.util.zip;

import java.io.OutputStream;
import java.io.IOException;

public class GZIPOutputStream extends DeflaterOutputStream  {
		
	protected CRC32 crc = new CRC32();
	
	private static final byte [] GZIPHeader = { 31, -117, 8, 0, 0, 0, 0, 0, 0, 0 };	
	private boolean finished = false;
	
	public GZIPOutputStream(OutputStream outst) throws IOException {
	 	super(outst, new Deflater(8,true));
	 	out.write(GZIPHeader, 0, GZIPHeader.length);
	}
	public GZIPOutputStream(OutputStream outst, int readsize) throws IOException {
		super(outst, new Deflater(8,true), readsize );
	 	out.write(GZIPHeader, 0, GZIPHeader.length);
	}
	
	public void close() throws IOException {
		finish();
		super.close();
	}

	public void finish() throws IOException {
		if (!finished) {
			super.finish();
			byte [] trailer = new byte [8];
	                int i = 0;
	                int bytes = (int) crc.getValue();
	                trailer[i++] = (byte)bytes;
	                bytes = bytes>>>8;
	                trailer[i++] = (byte)bytes;
	                bytes = bytes>>>8;
	                trailer[i++] = (byte)bytes;
	                bytes = bytes>>>8;
	                trailer[i++] = (byte)bytes;
	                bytes = def.getTotalIn();
	                trailer[i++] = (byte)bytes;
	                bytes = bytes>>>8;
	                trailer[i++] = (byte)bytes;
	                bytes = bytes>>>8;
	                trailer[i++] = (byte)bytes;
	                bytes = bytes>>>8;
	                trailer[i++] = (byte)bytes;
			out.write(trailer, 0 , 8);
			finished = true;
		}
	}
	public void write(byte [] buf, int offset, int len) throws IOException {
		if (finished) {
		 	throw new IOException("stream has finished");
		}
		crc.update(buf, offset, len); // this method will throw exceptions if needed
		super.write(buf, offset, len);
	}
}
