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
** $Id: StringBufferInputStream.java,v 1.2 2006/03/29 09:27:14 cvs Exp $
*/

package java.io;

/**
** @deprecated
*/
public class StringBufferInputStream extends InputStream {
 
  protected String buffer;

  protected int pos;

  protected int count;

  private char[] chars;

  public StringBufferInputStream(String s) {
    buffer = s;
    pos = 0;
    count = s.length();
    chars = s.toCharArray();
  }

  public int read() {
    if (pos==count) return -1;
    return chars[pos++] & 0xff;
  }
  
  public int read(byte[] b, int off, int len) {
    if (pos==count) return -1;
    int k = (len<count-pos ? len : count-pos);
    for(int i=0;i<k;++i) {
      b[i+off] = (byte)(chars[pos++] & 0xff);
    }
    return k;
  }

  
  public long skip(long n) {
    long k = (n<count-pos ? n : count-pos);

    pos += (int)k;
    return k;
  }

  public int available() {
    return count-pos;
  }

  public void reset() {
    pos = 0;
  }
}
