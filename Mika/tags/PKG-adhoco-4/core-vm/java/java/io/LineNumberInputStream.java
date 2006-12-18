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
** $Id: LineNumberInputStream.java,v 1.2 2006/10/04 14:24:15 cvsroot Exp $
*/

package java.io;

public class LineNumberInputStream extends FilterInputStream {

  static final int NEWLINE = 10;
  static final int RETURN = 13;

  private int linecount = 0;
  private int marked_linecount = 0;
  private boolean skipNewLine;

  public LineNumberInputStream(InputStream in) {
    super(in);
  }

  public int read()
    throws IOException
  {
    int nextbyte = in.read();

    if (nextbyte<0) return nextbyte;

    if (nextbyte==NEWLINE) {
      if(skipNewLine){
        skipNewLine = false;
        return read();
      }
      else {
        ++linecount;
      }
    }
    else if (nextbyte==RETURN) {
      skipNewLine = true;
      ++linecount;
      return NEWLINE;
    }
    else  {
      skipNewLine = false;
    }
    return nextbyte;
  }
  
  public int read(byte[] b, int off, int len)
    throws IOException, NullPointerException, ArrayIndexOutOfBoundsException
  {
    if(off<0 || len<0 || off>b.length - len) throw new ArrayIndexOutOfBoundsException();

    int i;
    int nextbyte;

    for(i=0;i<len;++i) {
      nextbyte = read();
      if (nextbyte<0) break;
      b[i+off] = (byte)nextbyte;
    }

    if(i == 0){
      return -1;
    }
    return i;

  }
  
  public long skip(long n)
    throws IOException
  {
    long j;
    int nextbyte;

    for(j=0;j<n;++j) {
      nextbyte = read();
      if (nextbyte<0) break;
    }

    return j;

  }

  public int available()
    throws IOException
  {
    return in.available()/2;
  }

  public void mark(int readLimit)
  {
    marked_linecount = linecount;
    in.mark(readLimit);
  }

  public void reset()
    throws IOException
  {
    in.reset();
    linecount = marked_linecount;
  }

  public int getLineNumber()
  {
    return linecount;
  }

  public void setLineNumber(int lineNumber)
  {
    linecount = lineNumber;
  }
  public boolean markSupported()
  {
    return in.markSupported();
  }

}
