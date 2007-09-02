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
 * $Id: DeflaterOutputStream.java,v 1.2 2006/10/04 14:24:15 cvsroot Exp $
 */
package java.util.zip;

import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.io.IOException;

/**
*	basic OutputStream for Deflating (compressing) data. <br>
*	<br>
*       read the {@link Deflater} class documentation for more info !
*/
public class DeflaterOutputStream extends FilterOutputStream {
		
	protected byte [] buf;	
	protected Deflater def;
	
        private boolean finished=false;
			
	public DeflaterOutputStream(OutputStream out) {
	 	this(out, new Deflater(),512);
	}
	public DeflaterOutputStream(OutputStream out, Deflater defl) {	
	 	this(out, defl, 512);
	}
	public DeflaterOutputStream(OutputStream out, Deflater  defl, int bufsize) {
		super(out);
	 	if (defl == null) {	 	
			throw new NullPointerException();
		}
		def = defl;
		buf = new byte[bufsize];
	}
	
	protected void deflate() throws IOException{
	     	if (finished) {
		 	throw new IOException("stream has finished");
		}
		int used=0;	     	
	        int size=buf.length;
	        while (! def.needsInput()) {
	        	if (size == used) {
	        	 	out.write(buf, 0, size);
	        	 	used=0;
	        	}
	        	used += def.deflate(buf, used, size - used);
	        }	
       	 	out.write(buf, 0, used);
       	 	used=0;		
	}	
	
	public void close() throws IOException {
		finish();
		out.close();	
	}
	
	public void finish() throws IOException {
	     	if (!finished) {
	     		def.finish();
	     		deflate();
	     		finished = true;
	     	}
	}
	
	public void write(int bval) throws IOException {
        	byte [] b = new byte[1];
        	b[0] = (byte) bval;
        	write(b, 0, 1);		
	
	}	
	
	public void write(byte [] buffer, int offset, int len) throws IOException {
		if (offset < 0 || len < 0 || buffer.length - len < offset ) {
		 	throw new IndexOutOfBoundsException();
		}	
		deflate();
	      	def.setInput(buffer, offset, len);
 	}

}
