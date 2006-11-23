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
