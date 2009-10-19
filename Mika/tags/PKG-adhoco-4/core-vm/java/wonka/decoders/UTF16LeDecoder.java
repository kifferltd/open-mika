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
** $Id: UTF16LeDecoder.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package wonka.decoders;

import java.io.*;

public class UTF16LeDecoder extends Decoder {

  public char[] bToC(byte[] bytes, int off, int len){
    int l = len / 2;
    char[] chars = new char[l];
    for(int i = 0 ; i < l ; i++){
      int ch = bytes[off++] & 0xff;
      chars[i] = (char) ((ch | bytes[off++]<< 8) & 0xffff);
    }
    return chars;
  }

  public int cFromStream(InputStream in, char[] chars, int off, int len) throws IOException {
    int l = 2*len;
    byte[] bytes = new byte[l];
    int rd = in.read(bytes, 0 , l);
    if(rd == -1){
      return -1;
    }

    if(rd % 2 == 1){
      int b = in.read();
      if(b != -1){
        bytes[rd++] = (byte)b;
      }
      else {
        rd--;
      }
    }
    int i = 0;
    while(i < rd){
      int ch = bytes[i++] & 0xff;
      chars[off++] = (char) ((ch | (bytes[i++])<<8) & 0xffff);
    }
    return rd/2;
  }

  public byte[] cToB(char[] chars, int off, int len){
    byte[] bytes = new byte[2*len];
    len += off;
    int o = 0;
    for(int i= off; i < len ; i++){
      int ch = chars[i];
      bytes[o++] = (byte)ch;
      bytes[o++] = (byte)(ch>>8);
    }
    return bytes;
  }

  public int getChar(InputStream in) throws IOException {
    int b1 = in.read();
    if(b1 == -1){
      return -1;
    }
    int b2 = in.read();
    if(b2 == -1){
      return -1;
    }
    return (b2<<8) | b1;
  }

  public String getEncoding(){
    return "UTF-16LE";
  }

}
