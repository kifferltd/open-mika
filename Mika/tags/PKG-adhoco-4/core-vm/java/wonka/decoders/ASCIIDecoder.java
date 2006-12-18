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
