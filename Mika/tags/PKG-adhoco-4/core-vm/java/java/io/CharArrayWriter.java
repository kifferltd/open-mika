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
** $Id: CharArrayWriter.java,v 1.2 2006/10/04 14:24:14 cvsroot Exp $
*/

package java.io;

public class CharArrayWriter extends Writer {

  protected char[] buf;
  protected int count=0;
  private static final int defaultsize = 32;

  public CharArrayWriter() {
  	buf = new char[defaultsize];
  }
  
  public CharArrayWriter(int size) {
  	if (size < 0) throw new IllegalArgumentException();
  	buf = new char[size];
  }
  
  public void close() {
  }
  
  public void flush() {
  }
  
  public void reset() {
    count =0;
  }
  
  public int size() {
    return count;
  }
  
  public char[] toCharArray() {
    synchronized (lock) {
	    int c = count;
	    char [] ca = new char[c];
    	System.arraycopy(buf, 0, ca, 0, c);
    	return ca;
    }
  }
  
  public String toString() {
    synchronized (lock) {
     	return new String(buf,0,count);
    }
  }

  public void write(int oneChar) {
    synchronized (lock) {
      int c = count;
    	if (c == buf.length) resize(2*c+1,c);
      	buf[count++]=(char) oneChar;
    }
  }

  public void write(char[] buf, int offset, int count){
    if (offset < 0 || count < 0 || offset > buf.length - count) throw new ArrayIndexOutOfBoundsException();
    synchronized (lock) {
      int c = this.count;
    	if (c+count > this.buf.length) resize(count+c,c);
    	System.arraycopy(buf, offset, this.buf, c, count);
    	this.count += count;
    }
  }

  public void write(String str, int offset, int count) {
    if (offset < 0 || count < 0 || offset > str.length() - count) throw new StringIndexOutOfBoundsException();
    synchronized (lock) {
      int c = this.count;
    	if (c+count > this.buf.length) resize(count+c,c);
    	str.getChars(offset, offset+count, buf, c);
    	this.count += count;
    }
  }

  public void writeTo(Writer out) throws IOException {
    synchronized (lock) {
      	out.write(buf,0,count);
    }
  }
/**
* do not use resize when the object is not locked !!!
*/
  private void resize(int newsize, int count) {
    char[] temp = new char[newsize];
    System.arraycopy(buf, 0, temp, 0, count);

    buf = temp;

  }
}
