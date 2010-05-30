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
** $Id: ASCIIDecoder.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package wonka.decoders;

import java.io.*;

public class ASCIIDecoder extends Decoder {

  public byte[] cToB(char[] chars, int off, int len){
    byte[] bytes = new byte[len];
    int o = off+len;
    while(--len >= 0){
      bytes[len] = (byte)chars[--o];
    }
    return bytes;
  }

  public char[] bToC(byte[] bytes, int off, int len){
    char[] chars = new char[len];
    int o = off+len;
    int mask = 0x7f;
    while(--len >= 0){
      chars[len] = (char) (mask & bytes[--o]);
    }
    return chars;
  }

  public int cFromStream(InputStream in, char[] chars, int off, int len) throws IOException {
    byte[] bytes = new byte[len];
    int rd = in.read(bytes,0,len);
    int i = rd;
    int o = off+rd;
    int mask = 0x7f;
    while(--i >= 0){
      chars[--o] = (char)(mask & bytes[i]);
    }
    return rd;
  }

  public int getChar(InputStream in) throws IOException{
    int rd = in.read();
    if(rd > 0){
      rd &= 0x7f;
    }
    return rd;
  }

  public String getEncoding(){
    return "ASCII";
  }
}
