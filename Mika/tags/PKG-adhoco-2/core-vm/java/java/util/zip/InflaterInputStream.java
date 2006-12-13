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
 * $Id: InflaterInputStream.java,v 1.5 2006/10/04 14:24:15 cvsroot Exp $
 */
package java.util.zip;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

/**
**	basic InputStream which use the Inflater. <br>	
**	<br>
** 	read class documentation in the {@link Inflater} class	
*/
public class InflaterInputStream extends FilterInputStream {
		
	protected byte [] buf;
	protected int len;
	protected Inflater inf;

	private boolean closed = false;
			
	public InflaterInputStream(InputStream in) {
	 	this(in, new Inflater(), 512);
	}
	public InflaterInputStream(InputStream in, Inflater infl) {	
	 	this(in, infl ,512);
	}
	public InflaterInputStream(InputStream in, Inflater  infl, int bufsize) {
		super(in);
	 	if (infl == null) {	 	
			throw new NullPointerException();
		}
		inf = infl;
		buf = new byte[bufsize];
	}
	
	protected void fill() throws IOException{
		len = in.read(buf, 0, buf.length);
	}	
	
	public int available() throws IOException{
	 	return (closed ? 0 : 1);
	}
	public void close() throws IOException {
		if (!closed) {
			closed = true;			
			in.close();	
                        inf.end();  	
		}
	}
	
	public int read() throws IOException {
   	byte [] b = new byte[1];
   	int ret = read(b,0,1);
   	if (ret != -1) {
   		ret = 0x0ff & (char)b[0];
   	}
   	return ret;			
	}	
  public int read(byte [] buffer, int offset, int length) throws IOException {
    if(offset < 0 || len < 0 || offset > buffer.length - length) {
      throw new ArrayIndexOutOfBoundsException();
    }
    int rd = 0;
    int ret;
    while (length > 0) {
      try {
        ret = inf.inflate(buffer, offset, length);
      }
      catch(DataFormatException dfe) {
        throw new ZipException("stream is corrupted");
      }
      if(ret == 0){
        if (inf.finished()) {
          if(rd == 0) {
            rd--;
          }
          break;                	 		                		
        }
        if (inf.needsInput()) {
          len = in.read(buf,0,buf.length);
          if(len == -1) {
            if(rd == 0) {
              rd--;
            }
            break;
          }
          inf.setInput(buf,0,len);            	 	
        }
        else {
          if (inf.needsDictionary()) {
            rd = -1;
          }
          break;
        }
      }
      length -= ret;
      offset += ret;
      rd += ret;	
    }
    return rd;
	}
	
	public long skip(long n) throws IOException {
	 	long skipped = 0L;
	 	if (n > 0L) {	 	
		 	int count = (n > 1024 ? 1024 : (int)n);;
		 	byte [] buffer = new byte[count];
		 	int rd;
		 	while (n > 0L) {
		 		rd = read(buffer, 0, (count > n ? (int)n : count));
				if (rd != -1) {
				 	skipped += rd;
					n -= rd;			 	
				}
				else {
				 	break;
				}	 		
	 		}
	 	}
	 	return skipped;
	}
}
