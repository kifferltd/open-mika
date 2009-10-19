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
** $Id: UTF16Decoder.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package wonka.decoders;

import java.io.*;

public class UTF16Decoder extends Decoder {

  private int state;

  private void setEndianness(InputStream in) throws IOException {
    int b1 = in.read();
    int b2 = in.read();
    if (b1 == 0xfe && b2 == 0xff) {
      state = BIGEND;
    }
    else if (b1 == 0xff && b2 == 0xfe) {
      state = LITTLEEND;
    }
    else {
      throw new UTFDataFormatException("missing BOM");
    }
  }


  public char[] bToC(byte[] bytes, int off, int len){
    if(len < 2){
      return new char[0];
    }
    int c = bytes[off++];
    c = ((c<<8) | (bytes[off++] & 0xff)) & 0xffff;
    if(c == 0xfffe){ //LITTLE END
      return UnicodeDecoder.leBToC(bytes,off,len-2);
    }
    if(c == 0xfeff){ //BIGEND
      return UnicodeDecoder.beBToC(bytes,off, len-2);
    }
    return UnicodeDecoder.beBToC(bytes, off-2, len);
  }

  public int cFromStream(InputStream in, char[] chars, int off, int len) throws IOException {
    if(state == UNDEFINED){
      setEndianness(in);
    }
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
    if(state == BIGEND){
      while(i < rd){
        int ch = bytes[i++]<<8;
        chars[off++] = (char) ((ch | (bytes[i++] & 0xff)) & 0xffff);
      }
    }
    else {
      while(i < rd){
        int ch = bytes[i++] & 0xff;
        chars[off++] = (char) ((ch | (bytes[i++]<<8)) & 0xffff);
      }
    }
    return rd/2;
  }

  public byte[] cToB(char[] chars, int off, int len){
    byte[] bytes = new byte[len*2 + 2];
    len += off;
    bytes[0] = (byte)0xfe;
    bytes[1] = (byte)0xff;
    int o = 2;

    for(int i = off; i < len ; i++){
      int ch = chars[i];
      bytes[o++] = (byte)(ch>>8);
      bytes[o++] = (byte)ch;
    }
    return bytes;
  }

  public int getChar(InputStream in) throws IOException {
    if(state == UNDEFINED){
      setEndianness(in);
    }
    int b1 = in.read();
    if(b1 == -1){
      return -1;
    }
    int b2 = in.read();
    if(b2 == -1){
      return -1;
    }
    if(state == BIGEND){
      return (b1<<8) | b2;
    }
    return (b2<<8) | b1;
  }

  public String getEncoding(){
    return "UTF16";
  }

}
