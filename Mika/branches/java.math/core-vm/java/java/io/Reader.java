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

public abstract class Reader {

  protected Object lock;

  protected Reader(){
    this.lock = this;
  }

  protected Reader(Object lock){
    if (lock == null) {
      throw new NullPointerException();
    }
    this.lock = lock;
  }

  public abstract void close() throws IOException;

  /**
  ** real implementations should be synchronized ...
  */
  public abstract int read(char chars[], int off, int len) throws IOException;

  public void mark(int readLimit) throws IOException {
    throw new IOException("mark is not supported by "+this.getClass().getName());
  }

  public boolean markSupported(){
    return false;
  }

  public int read() throws IOException {
    char[] chars = new char[1];
    int rd = read(chars, 0, 1);
    if(rd < 1){
      return -1;
    }else{
      return chars[0];
    }
  }

  public int read(char[] chars) throws IOException {
    return read(chars, 0, chars.length);
  }

  public boolean ready() throws IOException{
    return false;
  }

  public void reset() throws IOException {
    throw new IOException("reset is not supported by "+this.getClass().getName());
  }

  public long skip(long skip) throws IOException {
    if (skip < 0){
      throw new IllegalArgumentException("cannot skip "+skip+" (value must be 0 or greater)");
    }
    synchronized(lock){
      char[] chars = new char[128];
      long remain=skip;
      do{
        int rd = read(chars,0 , (128 > remain ? (int) remain : 128));
        if (rd == -1){
          break;
        }
        remain -= rd;
      }while (remain > 0);
      return skip - remain;
    }
  }
}
