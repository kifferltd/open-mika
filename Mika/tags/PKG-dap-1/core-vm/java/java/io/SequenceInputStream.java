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
** $Id: SequenceInputStream.java,v 1.2 2006/10/04 14:24:15 cvsroot Exp $
*/

package java.io;

import java.util.Enumeration;

public class SequenceInputStream extends InputStream {

  Enumeration seq;
  InputStream first;
  InputStream second;

  public SequenceInputStream(Enumeration e) {
    seq = e;
  }

  public SequenceInputStream(InputStream s1, InputStream s2) {
    first = s1;
    second = s2;
  }

  public int read() throws IOException {
    int readresult = -1;

    if(first!=null) {
      readresult = first.read();
      if (readresult<0) first = null;
    }

    if(readresult<0 && second!=null) {
      readresult = second.read();
      if (readresult<0) second = null;
    }

    if(readresult<0 && seq!=null && seq.hasMoreElements()) {
      InputStream next = (InputStream)seq.nextElement();
      readresult = next.read();
    }

    return readresult;
  }
  
  
  public int read(byte[] b, int off, int len)
    throws IOException, NullPointerException, ArrayIndexOutOfBoundsException
  {
    if(off<0 || len<0 || off > b.length - len) throw new ArrayIndexOutOfBoundsException();

    int i;
    int nextbyte;

    for(i=0;i<len;++i) {
      nextbyte = read();
      if (nextbyte<0) 

        return -1;

      b[i+off] = (byte)nextbyte;
    }

    return i;

  }

  
  public void close()
    throws IOException
  {
    if (seq != null) {
      while(seq.hasMoreElements()) {
        ((InputStream)seq.nextElement()).close();
      }
      seq = null;
    }
    
    if(first != null) {
      first.close();
      first = null;
    }
    
    if(second != null) {
      second.close();
      second = null;
    }
  }

  public int available() throws IOException{
    //TODO ...
    return 0;
  }

}
