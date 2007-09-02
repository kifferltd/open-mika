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
** $Id: InputStream.java,v 1.2 2006/10/04 14:24:15 cvsroot Exp $
*/

package java.io;

public abstract class InputStream {

  public InputStream(){}

  public abstract int read() throws IOException;
  

  public int read(byte[] b) 
    throws IOException 
  {
    return read(b, 0, b.length);
  }

  
  public int read(byte [] b, int off, int len) 
    throws IOException 
  {
    if(off<0 || len<0 || off > b.length - len)
	throw new ArrayIndexOutOfBoundsException();

    int i=0;
    int next;
    for(i=0 ; i<len ; i++) {
      next = read();
      if (next<0) 
        break;

      b[i+off] = (byte)next;
    }

    return ((i==0) ? -1 : i);
  }

  
  public long skip(long n)
    throws IOException
  {
    if(n < 0 || n > Integer.MAX_VALUE){
      return 0;
    }

    int m = (int)n;
    long j;
    int next;

    for(j=0;j<m;++j) {
      next = read();
      if (next<0) break;
    }

    return j;
  }


  public int available()
    throws IOException
  {
    return 0;
  }

  public void close() throws IOException {}

  public void mark(int readLimit) {}

  public void reset() throws IOException {
    throw new IOException();
  }

  public boolean markSupported() {
    return false;
  }

}
