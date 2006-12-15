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
			 	throw new IOException();
			}
			value = (0x0ff&(char)trailer[4]) + ((0x0ff &(char)trailer[5])<<8) +
				 ((0x0ff&(char)trailer[6])<<16) + ((0x0ff &(char)trailer[7])<<24);			
			if ((int)value != inf.getTotalOut()) {
			 	throw new IOException();
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
