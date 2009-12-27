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
