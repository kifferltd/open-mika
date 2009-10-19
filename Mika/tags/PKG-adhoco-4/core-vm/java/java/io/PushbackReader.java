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
** $Id: PushbackReader.java,v 1.2 2006/10/04 14:24:15 cvsroot Exp $
*/

package java.io;

public class PushbackReader extends FilterReader {

  private int pos;
  private char[] chars;

  public PushbackReader(Reader in){
    super(in);
    chars = new char[1];
    pos = 1;
  }

  public PushbackReader(Reader in, int size){
    super(in);
    if(size < 1){
      throw new IllegalArgumentException("buffer size should 1 or greater");
    }
    chars = new char[size];
    pos = size;
  }

  public void close() throws IOException {
    synchronized(lock){
       if(chars != null){
         in.close();
       }
    }
  }

  public void mark(int ignored) throws IOException {
    throw new IOException("mark/reset not supported");
  }

  public boolean markSupported() {
    return false;
  }

  public int read()throws IOException {
    synchronized(lock){
      if(chars == null){
        throw new IOException("Reader is closed");
      }
      if(pos < chars.length){
        return chars[pos++];
      }
      return in.read();
    }
  }

  public int read(char[] buf, int off, int len)throws IOException {
    synchronized(lock){
      if(chars == null){
        throw new IOException("Reader is closed");
      }
      if(buf.length - len < off || off < 0 || len < 0){
        throw new ArrayIndexOutOfBoundsException();
      }
      int rd = chars.length - pos;
      if(rd > 0){
        if (len <= rd){
          System.arraycopy(chars, pos, buf, off, rd);
          pos += len;
          return len;
        }
        System.arraycopy(chars, pos, buf, off, rd);
        off += rd;
        pos += rd;
        len -= rd;
      }
      len = in.read(buf, off, len);
      return (len == -1 ? (rd == 0 ? len : rd) : len + rd);
    }
  }

  public boolean ready() throws IOException{
    synchronized(lock){
      if(chars == null){
        throw new IOException("Reader is closed");
      }
      return (pos < chars.length || in.ready());
    }
  }

  public void reset() throws IOException {
    throw new IOException("mark/reset not supported");
  }

  public void unread(int ch) throws IOException {
    synchronized(lock){
      if(chars == null || pos == 0){
        throw new IOException();
      }
      chars[--pos] = (char)ch;
    }
  }

  public void unread(char[] buf) throws IOException {
     unread(buf, 0, buf.length);
  }

  public void unread(char[] buf, int off, int len) throws IOException {
    synchronized(lock){
      if(buf.length - len < off || off < 0 || len < 0){
        throw new ArrayIndexOutOfBoundsException();
      }
      if(chars == null) {
        throw new IOException("char array is null");
      }
      if((pos - len) < 0) {
        throw new IOException("PushbackBuffer is full");
      }
      pos -= len;
      System.arraycopy(buf, off, chars, pos, len);
    }
  }
}
