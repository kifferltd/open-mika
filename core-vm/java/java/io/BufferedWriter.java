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

/*
** $Id: BufferedWriter.java,v 1.3 2006/10/04 14:24:14 cvsroot Exp $
*/

package java.io;

public class BufferedWriter extends Writer {

  private static final int DEFAULT_BUFFER_SIZE = 8192;
  private int bufsize;
  private int bufcounter;
  private char[] buf;
  private boolean closed;
  private Writer out;
  private String line_separator;

  public BufferedWriter(Writer out) {
    this (out,DEFAULT_BUFFER_SIZE);
  }
  
  public BufferedWriter(Writer out, int size) {
    super(out);

    if (size < 1 ) {
      throw new IllegalArgumentException();
    }

    this.out = out;
    this.bufsize = size;
    this.bufcounter = 0;
    this.buf = new char[this.bufsize];
    line_separator = GetSystemProperty.LINE_SEPARATOR;
  }
  
  public void close() throws IOException {

    synchronized (lock) {
        if (!closed) {
    		closed = true;
    		out.write(buf,0,bufcounter);
    		buf = null;
    		out.close();
   	}
    }	
  }
  
  public void flush() throws IOException {
    if (closed) {
      throw new IOException();
    }
    synchronized (lock) {
    	out.write(buf,0,bufcounter);
    	bufcounter = 0;
    	out.flush();
    }
  }
  
  public void newLine() throws IOException {
    write(line_separator,0,line_separator.length());
  }
  
  public void write(int oneChar) throws IOException {
    synchronized (lock) {
    	if (closed) {
    	  throw new IOException();
    	}
    	if (bufcounter == bufsize) {
    	    out.write(buf);
      		buf[0] = (char)oneChar;
      		bufcounter = 1;
    	}
    	else {
        buf[bufcounter++] = (char)oneChar;
      }
    }
  }
  
  public void write(char[] cbuf, int offset, int count) throws IOException {
    synchronized (lock) {
    	//sanity checks
    	if (closed) throw new IOException();
      if (offset < 0 || count < 0 || offset > cbuf.length - count) throw new ArrayIndexOutOfBoundsException();
	
	    if (count >= bufsize) {
		    if (bufcounter != 0) {
			    out.write(buf,0,bufcounter);
			    bufcounter=0;
		    }
		    out.write(cbuf,offset,count);
	    }    	
      else {
      	if (bufcounter+count<bufsize) {
      		System.arraycopy(cbuf,offset,buf,bufcounter,count);
      	  bufcounter+=count;
      	  if (bufcounter >= bufsize) {
      	   	out.write(buf);
            bufcounter=0;
          }
      	}
      	else {  int help=bufsize-bufcounter;
      		System.arraycopy(cbuf,offset,buf,bufcounter,help);
     	    out.write(buf);
          bufcounter=count-help;
      		System.arraycopy(cbuf,offset+help,buf,0,bufcounter);
      	}	
      }
    }
  }

  public void write(String str, int offset, int count) throws IOException {
    synchronized (lock) {
    	if (closed) throw new IOException();
      if (offset < 0 || count < 0 || offset > str.length() - count) throw new StringIndexOutOfBoundsException("offset = " + offset + ", count = " + count + ", length = " + str.length());
      if (bufcounter+count<bufsize) {
      	str.getChars(offset,offset+count,buf,bufcounter);
        bufcounter+=count;
        if (bufcounter >= bufsize) {
         	out.write(buf);
          bufcounter=0;
        }
      }
      else {
        int help=bufsize-bufcounter;
      	str.getChars(offset,offset+help,buf,bufcounter);
     	  out.write(buf);
     	  count-=help;
     	  offset+=help;
     	  while (count >= bufsize) {
        	str.getChars(offset,offset+bufsize,buf,0);
     	    out.write(buf);
     	    count-=bufsize;
     	    offset+=bufsize;
        }
      	str.getChars(offset,offset+count,buf,0);
        bufcounter=count;
      }	
    }
  }
}
