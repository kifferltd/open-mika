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
** $Id: CharArrayReader.java,v 1.2 2006/10/04 14:24:14 cvsroot Exp $
*/

package java.io;

public class CharArrayReader extends Reader {

  protected char[] buf;
  protected int count;
  protected int markedPos;
  protected int pos;

  public CharArrayReader(char[] chars) {
    this(chars, 0, chars.length);
  }

  public CharArrayReader(char[] chars, int off, int len) {
    super();
    if(off < 0 || len < 0 || off > chars.length){
      throw new IllegalArgumentException();
    }	
    buf = chars;
    count = chars.length <(len + off) ? chars.length: len+off;
    pos = off;
    markedPos = off;
  }

  public int read() throws IOException {
    synchronized(lock){
      if(buf == null){
    	  throw new IOException("cannot read from a closed Stream");
  	  }
      if(pos >= count){
    	  return -1;
  	  }
      return buf[pos++];
    }
  }

  public int read(char[] chars, int off, int len) throws IOException {
    if(off < 0 || len < 0 || off > chars.length - len){
      throw new ArrayIndexOutOfBoundsException();
    }	
    synchronized(lock){
      if(buf == null){
  	    throw new IOException("cannot read from a closed Stream");
      }
      if(pos >= count){
  	    return -1;
      }	
      int rd = count-pos;
      rd = rd > len ? len : rd;
      System.arraycopy(buf, pos, chars, off, rd);
      pos += rd;
      return rd;
    }
  }

  public long skip(long n) throws IOException {
    if(n <= 0){
      return 0;
    }	
    synchronized(lock){
      if(buf == null){
  	    throw new IOException("cannot read from a closed Stream");
      }	
      if(pos >= count){
  	    return 0;
      }	
      int skip = count-pos;
      skip = skip > n ? (int)n : skip;
      pos += skip; 	
      return skip;
    }
  }

  public boolean ready() throws IOException {
    synchronized(lock){
      if(buf == null){
  	    throw new IOException("cannot read from a closed Stream");
      }
      return pos < count;
    }
  }

  public boolean markSupported() {
    return true;
  }

  public void mark(int readAheadLimit) throws IOException {
    synchronized(lock){
      if(buf == null){
  	    throw new IOException("cannot read from a closed Stream");
      }
      markedPos = pos;
    }
  }

  public void reset() throws IOException {
    synchronized(lock) {
      if(buf == null){
  	    throw new IOException("cannot read from a closed Stream");
      }	
      pos = markedPos;
    }
  }

  public void close(){
    synchronized(lock){
      buf = null;
    }
  }
}
