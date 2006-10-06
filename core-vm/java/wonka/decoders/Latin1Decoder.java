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
** $Id: Latin1Decoder.java,v 1.1.1.1 2004/07/12 14:07:47 cvs Exp $
*/

package wonka.decoders;

import java.io.*;

public class Latin1Decoder extends Decoder {

  public native byte[] cToB(char[] chars, int off, int len);

  public char[] bToC(byte[] bytes, int off, int len){
    char[] chars = new char[len];
    copyArray(bytes, off, chars, 0, len);
    return chars;
  }

  private native void copyArray(byte[] src, int srcOff, char[] dst, int dstOff, int len);

  public int cFromStream(InputStream in, char[] chars, int off, int len) throws IOException {
    byte[] bytes = new byte[len];
    int rd = in.read(bytes,0,len);
    if(rd > 0){
      copyArray(bytes, 0, chars, off, rd);
    }
    return rd;
  }

  public int getChar(InputStream in) throws IOException{
    return in.read();
  }

  public String getEncoding(){
    return "ISO8859_1";
  }

}
