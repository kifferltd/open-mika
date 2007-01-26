/**
 * Copyright  (c) 2007 by Chris Gray, /k/ Embedded Java Solutions.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of /k/ Embedded Java Solutions nor the names of other contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL /K/
 * EMBEDDED SOLUTIONS OR OTHER CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * $Id: $
 */
package wonka.decoders;

import java.io.IOException;
import java.io.InputStream;
import java.io.UTFDataFormatException;

/**
 * UnicodeDecoder:
 *
 * @author ruelens
 *
 * created: Jan 25, 2007
 */
public class UnicodeDecoder extends Decoder {

  protected int state;

  /**
   * @see wonka.decoders.Decoder#bToC(byte[], int, int)
   */
  public char[] bToC(byte[] bytes, int off, int len) {
    if(len < 2){
      return new char[0];
    }
    int c = bytes[off++];
    c = ((c<<8) | (bytes[off++] & 0xff)) & 0xffff;
    if(c == 0xfffe){ //LITTLE END
      return leBToC(bytes,off,len-2);
    }
    if(c == 0xfeff){ //BIGEND
      return beBToC(bytes,off, len-2);
    }
    return beBToC(bytes, off-2, len);
  }

  static char[] beBToC(byte[] bytes, int off, int len) {
    int l = (len/2);
    char[] chars = new char[l];

    for(int i = 0 ; i < l ; i++){
      int ch = bytes[off++]<<8;
      chars[i] = (char)((ch | (bytes[off++] & 0xff)) & 0xffff);
    }
    return chars;
  }


  static char[] leBToC(byte[] bytes, int off, int len) {
    int l = (len/2);
    char[] chars = new char[l];

    for(int i = 0 ; i < l ; i++){
      int ch = bytes[off++] & 0xff;
      chars[i] = (char)((ch | (bytes[off++]<<8)) & 0xffff);
    }
    return chars;
  }


  /**
   * @see wonka.decoders.Decoder#cFromStream(java.io.InputStream, char[], int, int)
   */
  public int cFromStream(InputStream in, char[] chars, int off, int len)
      throws IOException {
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

  /**
   * @see wonka.decoders.Decoder#cToB(char[], int, int)
   */
  public byte[] cToB(char[] chars, int off, int len) {
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

  /**
   * @see wonka.decoders.Decoder#getChar(java.io.InputStream)
   */
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

  /**
   * @see wonka.decoders.Decoder#getEncoding()
   */
  public String getEncoding() {
    return "Unicode";
  }

  protected Decoder getInstance() {
    try {
      return (Decoder)this.getClass().newInstance();
    } catch (Exception e) {
    }
    return this;
  }

}
