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
