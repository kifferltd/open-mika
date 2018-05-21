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
 * $Id: GZIPInputStream.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
 */
package java.util.zip;

import java.io.InputStream;
import java.io.IOException;

public class GZIPInputStream extends InflaterInputStream  {
		
	public static final int GZIP_MAGIC = 0x8b1f;	

	private boolean closed=false;
	
	protected CRC32 crc = new CRC32();
	protected boolean eos = false;
		
	public GZIPInputStream(InputStream instr) throws IOException  {
	 	super(instr,new Inflater(true));
		parseHeader();
	}
	
	public GZIPInputStream(InputStream instr, int readsize) throws IOException {
		super(instr, new Inflater(true), readsize);
		parseHeader();
	}
		
	public void close() throws IOException {
		if (!closed) {
			in.close();
			closed=true;
			inf = null;
		}
	}
		
	public int read(byte [] buffer, int offset, int length) throws IOException {
		int ret = super.read(buffer, offset, length);
		if (ret != -1) {
			crc.update(buffer, offset, ret);
		}
                if (ret == 1 || inf.finished()) {
			if (inf.needsDictionary()) {
			 	throw new IOException();
			}
			int rem = inf.getRemaining();
			byte [] trailer = new byte[8];
			// we need to gather 8 bytes;	
			int off = len - rem;
			rem = (rem > 8 ? 8 : rem);
			if(off >= 0 && off <(buf.length - rem) ){	
				System.arraycopy(buf,len-rem,trailer,0, rem);
			}
			if (rem < 8) {
			 	in.read(trailer, rem , 8-rem);
			}
			long value = ((0x0ff&(char)trailer[0])) + ((0x0ff &(char)trailer[1])<<8) +
				 ((0x0ff&(char)trailer[2])<<16) + ((0x0ff &(char)trailer[3])<<24);
			if ((int)value != (int)crc.getValue()) {
			 	throw new IOException("CRC " + Integer.toHexString((int) value) + " != " + Integer.toHexString((int) crc.getValue()));
			}
			value = (0x0ff&(char)trailer[4]) + ((0x0ff &(char)trailer[5])<<8) +
				 ((0x0ff&(char)trailer[6])<<16) + ((0x0ff &(char)trailer[7])<<24);			
			if ((int)value != inf.getTotalOut()) {
			 	throw new IOException("out " + Integer.toHexString((int) value) + " != " + Integer.toHexString((int) inf.getTotalOut()));
			}
		}
		return ret;
	}
	
	private void parseHeader() throws IOException {
		byte [] header = new byte [10];
		in.read(header, 0, 10);
		int flags = 0x0ff &(char)header[3];
		if (header[0] != 31 || header[1] != -117 || header[2] != 8 || (flags & 0x0e0)!=0) {
		 	throw new IOException("corrupt stream detected");
		}
		if ((flags & 0x04) > 0) {
			in.read(header, 0 ,2);
			int len = (0x0ff&(char)header[0]) + (0x0ff &(char)header[1])*256;
			in.skip(len);
		}
		if ((flags & 0x08) > 0) {
		 	int i=in.read();
		 	while (i > 0) {
		 	 	i = in.read();
		 	}
		}
		if ((flags & 0x010) > 0) {
		 	int i=in.read();
		 	while (i > 0) {
		 	 	i = in.read();
		 	}
		}
		if ((flags & 0x02) > 0) {
		 	in.skip(2);
		}
		
	}
	
}
