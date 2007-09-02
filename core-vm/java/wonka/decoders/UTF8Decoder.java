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
** $Id: UTF8Decoder.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package wonka.decoders;

import java.io.*;

public class UTF8Decoder extends Decoder {

  /** extra methods for ObjectIn/OutputStream (Thread safe methods) */
  public static native byte[] stringToB(String string);
  public static native String bToString(byte[] bytes, int off, int len);
  /** END */

  private int available;
  private byte[] buf;
  private int pos;

  public UTF8Decoder(){}

  public int getChar(InputStream in) throws IOException {
    int c = readIn(in);
    if(c < 0x80){
      return c;
    }
    else if (c >= 0xe0) {
      int utf8_top_4 = c & 0x0f;
      c = readIn(in);
      if (c < 0) {
        return -1;
      }
      if (c >=0xc0 || c < 0x80) {
        throw new UTFDataFormatException("bad second of triple: "+c);
      }
      int utf8_middle_6 = c & 0x3f;
      c = readIn(in);
      if (c < 0) {
        return -1;
      }
      if (c >=0xc0 || c < 0x80) {
        throw new UTFDataFormatException("bad third of triple: "+c);
      }
      int utf8_bottom_6 = c & 0x3f;
      return (((utf8_top_4 << 6) | utf8_middle_6) << 6) | utf8_bottom_6;
    }
    else if (c >= 0xc0) {
      int utf8_middle_6 = c & 0x3f;
      c = readIn(in);
      if (c < 0) {
        return -1;
      }
      if (c >=0xc0 || c < 0x80) {
        throw new UTFDataFormatException("bad second of duple: "+c);
      }
      int utf8_bottom_6 = c & 0x3f;
      c = (utf8_middle_6 << 6) | utf8_bottom_6;
    }
    throw new UTFDataFormatException("bad start of sequence: "+c);
  }

  public native byte[] cToB(char[] chars, int off, int len);
  public native char[] bToC(byte[] bytes, int off, int len);

  public int cFromStream(InputStream in, char[] chars, int off, int len) throws IOException {
    byte[] oldbuf = buf;
    int avail = available;
    byte[]buffer = oldbuf;
    if(len < 1){
      if(len == 0) return 0;
      throw new ArrayIndexOutOfBoundsException();
    }
    if (len * 3 > avail) {
      buffer = new byte[len * 3];
      if (avail > 0) {
        System.arraycopy(oldbuf, pos, buffer, 0, avail);
      }
      int readlen = in.read(buffer, avail, len * 3 - avail);
      if (readlen > 0) {
        avail += readlen;
      }
    }
    int nread = 0;
    int used = 0;
    while (nread < len){
      if (used >= avail) {
        break;
      }
      int c = buffer[used++] & 0xff;
      if(c >= 0x80){
        if (c >= 0xe0) {
          int utf8_top_4 = c & 0x0f;
          if (used >= avail) {
            used--;
            break;
          }
          c = buffer[used++] & 0xff;
          if (c >=0xc0 || c < 0x80) {
            throw new UTFDataFormatException("bad second of triple: "+c);
          }
          int utf8_middle_6 = c & 0x3f;
          if (used >= avail) {
            used -= 2;
            break;
          }
          c = buffer[used++] & 0xff;
          if (c >=0xc0 || c < 0x80) {
            throw new UTFDataFormatException("bad third of triple: "+c);
          }
          int utf8_bottom_6 = c & 0x3f;
          c = (((utf8_top_4 << 6) | utf8_middle_6) << 6) | utf8_bottom_6;
        }
        else if (c >= 0xc0) {
          int utf8_middle_6 = c & 0x3f;
          if (used >= avail) {
            used--;
            break;
          }
          c = buffer[used++] & 0xff;
          if (c >=0xc0 || c < 0x80) {
            throw new UTFDataFormatException("bad second of duple: "+c);
          }
          int utf8_bottom_6 = c & 0x3f;
          c = (utf8_middle_6 << 6) | utf8_bottom_6;
        }
        else {
          throw new UTFDataFormatException("bad start of sequence: "+c);
        }
      }
      // else c is ASCII, pass straight through
      chars[off++] = (char)c;
      ++nread;
    }

    buf = buffer;
    pos = used;
    available = avail - used;

    if (nread == 0) {
      return -1;
    }
    return nread;
  }

  public String getEncoding(){
    return "UTF8";
  }

  protected Decoder getInstance(){
    return new UTF8Decoder();
  }

  private int readIn(InputStream in) throws IOException {
    if(available > 0){
      available--;
      return buf[pos++] & 0xff;
    }
    else {
      return in.read();
    }
  }
}
