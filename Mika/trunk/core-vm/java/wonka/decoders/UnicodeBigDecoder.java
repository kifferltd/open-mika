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

/**
 * UnicodeBigDecoder:
 *
 * @author ruelens
 *
 * created: Jan 25, 2007
 */
public class UnicodeBigDecoder extends UnicodeDecoder {

  /* (non-Javadoc)
   * @see wonka.decoders.Decoder#bToC(byte[], int, int)
   */
  public char[] bToC(byte[] bytes, int off, int len) {
    if(len < 2){
      return new char[0];
    }
    int c = bytes[off++];
    c = ((c<<8) | (bytes[off++] & 0xff)) & 0xffff;
    if(c == 0xfeff){ //BIGEND
      return leBToC(bytes,off,len-2);
    }
    return beBToC(bytes, off-2, len);
  }

  /* (non-Javadoc)
   * @see wonka.decoders.Decoder#cFromStream(java.io.InputStream, char[], int, int)
   */
  public int cFromStream(InputStream in, char[] chars, int off, int len)
      throws IOException {
    if(state == Decoder.BIGEND) {
      return super.cFromStream(in, chars, off, len);
    }
    int rd = super.cFromStream(in, chars, off, len);
    if(state != Decoder.BIGEND) {
      throw new IOException();
    }
    return rd;
  }

  /* (non-Javadoc)
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

  /* (non-Javadoc)
   * @see wonka.decoders.Decoder#getChar(java.io.InputStream)
   */
  public int getChar(InputStream in) throws IOException {
    if(state == Decoder.BIGEND) {
      return super.getChar(in);
    }
    int rd = super.getChar(in);
    if(state != Decoder.BIGEND) {
      throw new IOException();
    }
    return rd;
  }

  /* (non-Javadoc)
   * @see wonka.decoders.Decoder#getEncoding()
   */
  public String getEncoding() {
    return "UnicodeBig";
  }

}
