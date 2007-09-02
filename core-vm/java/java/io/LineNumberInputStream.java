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
** $Id: LineNumberInputStream.java,v 1.2 2006/10/04 14:24:15 cvsroot Exp $
*/

package java.io;

public class LineNumberInputStream extends FilterInputStream {

  static final int NEWLINE = 10;
  static final int RETURN = 13;

  private int linecount = 0;
  private int marked_linecount = 0;
  private boolean skipNewLine;

  public LineNumberInputStream(InputStream in) {
    super(in);
  }

  public int read()
    throws IOException
  {
    int nextbyte = in.read();

    if (nextbyte<0) return nextbyte;

    if (nextbyte==NEWLINE) {
      if(skipNewLine){
        skipNewLine = false;
        return read();
      }
      else {
        ++linecount;
      }
    }
    else if (nextbyte==RETURN) {
      skipNewLine = true;
      ++linecount;
      return NEWLINE;
    }
    else  {
      skipNewLine = false;
    }
    return nextbyte;
  }
  
  public int read(byte[] b, int off, int len)
    throws IOException, NullPointerException, ArrayIndexOutOfBoundsException
  {
    if(off<0 || len<0 || off>b.length - len) throw new ArrayIndexOutOfBoundsException();

    int i;
    int nextbyte;

    for(i=0;i<len;++i) {
      nextbyte = read();
      if (nextbyte<0) break;
      b[i+off] = (byte)nextbyte;
    }

    if(i == 0){
      return -1;
    }
    return i;

  }
  
  public long skip(long n)
    throws IOException
  {
    long j;
    int nextbyte;

    for(j=0;j<n;++j) {
      nextbyte = read();
      if (nextbyte<0) break;
    }

    return j;

  }

  public int available()
    throws IOException
  {
    return in.available()/2;
  }

  public void mark(int readLimit)
  {
    marked_linecount = linecount;
    in.mark(readLimit);
  }

  public void reset()
    throws IOException
  {
    in.reset();
    linecount = marked_linecount;
  }

  public int getLineNumber()
  {
    return linecount;
  }

  public void setLineNumber(int lineNumber)
  {
    linecount = lineNumber;
  }
  public boolean markSupported()
  {
    return in.markSupported();
  }

}
