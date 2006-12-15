/**************************************************************************
* Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
*  distribution or disclosure of this software is expressly forbidden.    *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Vanden Tymplestraat 35      info@acunia.com                           *
*   3000 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
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
