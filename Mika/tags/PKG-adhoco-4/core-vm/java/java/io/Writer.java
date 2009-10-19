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

package java.io;

public abstract class Writer {

  protected Object lock;


  protected Writer(){
    lock = this;
  }

  protected Writer(Object lock){
    if (lock == null){
      throw new NullPointerException("lock should be non null");
    }
    this.lock = lock;
  }

  public abstract void close() throws IOException;
  public abstract void flush() throws IOException;

  /**
  ** Note: an implementation of this method should use the 'lock' object
  ** to synchronize with other methods ...
  */
  public abstract void write(char[] cbuf, int off, int len) throws IOException;

  public void write(char[] buf) throws IOException {
     write(buf, 0, buf.length);
  }

  public void write(int c) throws IOException {
     char[] buf = new char[1];
     buf[0] = (char) c;
     write(buf,0,1);
  }

  public void write(String string) throws IOException {
    write(string, 0, string.length());
  }

  public void write(String string, int off, int len) throws IOException {
     if (len < 0) {
       throw new IndexOutOfBoundsException();
     }
     char[] buf = new char[len];
     string.getChars(off,off+len, buf, 0);
     write(buf, 0, len);
  }

}
