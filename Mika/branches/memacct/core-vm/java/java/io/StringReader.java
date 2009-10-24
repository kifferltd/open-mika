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
** $Id: StringReader.java,v 1.3 2006/10/04 14:24:15 cvsroot Exp $
*/

package java.io;

public class StringReader extends Reader {

  private String string;
  private int len;
  private int pos;
  private int markpos;

  public StringReader(String str){
    super();
    len = str.length();
    string = str;
  }

  public void close(){
    synchronized(lock){
      string = null;
    }
  }

  public void mark(int ignored) throws IOException {
    synchronized(lock){
      if(string == null){
        throw new IOException("Reader is closed");
      }
      markpos = pos;
    }
  }

  public boolean markSupported(){
    return true;
  }

  public int read() throws IOException {
    synchronized(lock){
      if(string == null){
        throw new IOException("Reader is closed");
      }
      if(pos >= len){
        return -1;
      }
      return string.charAt(pos++);
    }
  }

  public int read(char[] chars, int off, int len) throws IOException {
    synchronized(lock){
      if(string == null){
        throw new IOException("Reader is closed");
      }
      if(len < 0 || off < 0 || chars.length  - len < off){
        throw new ArrayIndexOutOfBoundsException();
      }
      int rd = this.len - pos;
      if (rd <= 0){
        return -1;
      }
      rd = (rd > len ? len : rd);
      string.getChars(pos, pos + rd, chars, off);
      pos += rd;
      return rd;
    }
  }

  public boolean ready() throws IOException {
    synchronized(lock){
      return (string != null);
    }
  }

  public void reset() throws IOException {
    synchronized(lock){
      if(string == null){
        throw new IOException("Reader is closed");
      }
      pos = markpos;
    }
  }

  public long skip(long skip) throws IOException {
    synchronized(lock){
      if(string == null){
        throw new IOException("Reader is closed");
      }
      if(skip <= 0){
         return 0;
      }
      int rd = this.len -pos;
      rd = (skip > rd ? rd : (int)skip);
      pos += rd;
      return rd;
    }
  }
}
