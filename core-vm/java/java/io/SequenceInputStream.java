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
** $Id: SequenceInputStream.java,v 1.2 2006/10/04 14:24:15 cvsroot Exp $
*/

package java.io;

import java.util.Enumeration;

public class SequenceInputStream extends InputStream {

  Enumeration seq;
  InputStream first;
  InputStream second;

  public SequenceInputStream(Enumeration e) {
    seq = e;
  }

  public SequenceInputStream(InputStream s1, InputStream s2) {
    first = s1;
    second = s2;
  }

  public int read() throws IOException {
    int readresult = -1;

    if(first!=null) {
      readresult = first.read();
      if (readresult<0) first = null;
    }

    if(readresult<0 && second!=null) {
      readresult = second.read();
      if (readresult<0) second = null;
    }

    if(readresult<0 && seq!=null && seq.hasMoreElements()) {
      InputStream next = (InputStream)seq.nextElement();
      readresult = next.read();
    }

    return readresult;
  }
  
  
  public int read(byte[] b, int off, int len)
    throws IOException, NullPointerException, ArrayIndexOutOfBoundsException
  {
    if(off<0 || len<0 || off > b.length - len) throw new ArrayIndexOutOfBoundsException();

    int i;
    int nextbyte;

    for(i=0;i<len;++i) {
      nextbyte = read();
      if (nextbyte<0) 

        return -1;

      b[i+off] = (byte)nextbyte;
    }

    return i;

  }

  
  public void close()
    throws IOException
  {
    if (seq != null) {
      while(seq.hasMoreElements()) {
        ((InputStream)seq.nextElement()).close();
      }
      seq = null;
    }
    
    if(first != null) {
      first.close();
      first = null;
    }
    
    if(second != null) {
      second.close();
      second = null;
    }
  }

  public int available() throws IOException{
    //TODO ...
    return 0;
  }

}
