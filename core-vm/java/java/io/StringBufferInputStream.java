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
** $Id: StringBufferInputStream.java,v 1.2 2006/03/29 09:27:14 cvs Exp $
*/

package java.io;

/**
** @deprecated
*/
public class StringBufferInputStream extends InputStream {
 
  protected String buffer;

  protected int pos;

  protected int count;

  private char[] chars;

  public StringBufferInputStream(String s) {
    buffer = s;
    pos = 0;
    count = s.length();
    chars = s.toCharArray();
  }

  public int read() {
    if (pos==count) return -1;
    return chars[pos++] & 0xff;
  }
  
  public int read(byte[] b, int off, int len) {
    if (pos==count) return -1;
    int k = (len<count-pos ? len : count-pos);
    for(int i=0;i<k;++i) {
      b[i+off] = (byte)(chars[pos++] & 0xff);
    }
    return k;
  }

  
  public long skip(long n) {
    long k = (n<count-pos ? n : count-pos);

    pos += (int)k;
    return k;
  }

  public int available() {
    return count-pos;
  }

  public void reset() {
    pos = 0;
  }
}
