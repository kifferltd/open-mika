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
** $Id: LineNumberReader.java,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
*/

package java.io;

public class LineNumberReader extends BufferedReader {

  private int lineNr = 0;
  private int markline = 0;

  private int lastchar;
  private int markchar;

  public LineNumberReader(Reader in){
    super(in);
  }

  public LineNumberReader(Reader in, int size){
    super(in, size);
  }

  public int getLineNumber(){
    synchronized(lock){
      return lineNr;
    }
  }

  public void mark(int readlimit) throws IOException {
    synchronized(lock){
      super.mark(readlimit);
      markline = lineNr;
      markchar = lastchar;
    }
  }

  public int read() throws IOException {
    synchronized(lock){
      int rd = super.read();
      lastchar = rd;
      if ((lastchar != '\r' && rd == '\n') || rd == '\r'){
        lineNr++;
        // if a line separator is encountered then we return '\n'
        return '\n';
      }
      return rd;
    }
  }

  public int read(char[] chars, int off, int len) throws IOException {
    synchronized(lock){
      int rd = super.read(chars, off, len);
      for ( ; rd > 0; rd--){
        if ((lastchar != '\r' && chars[off] == '\n') || chars[off] == '\r'){
          lineNr++;
        }
        lastchar = chars[off++];
      }
      return rd;
    }
  }

  public String readLine() throws IOException {
    /**
    ** - super.readLine will stop after the lineseparator
    **   so lastchar can be set to 0.
    ** - if lastchar '\r' then we must check if a '\n' follows before we do a readLine
    */
    synchronized(lock){
       if(lastchar == '\r'){
         int rd = super.read();
         if(rd == -1){
           return null;
         }else if(rd != '\n'){
           String remain = super.readLine();
           lineNr++;
           if(remain == null){
             char [] ca = new char[1];
             ca[0] = (char)rd;
             return new String(ca,0,1);
           }
           return ((char)rd)+remain;
         }
       }
       String line = super.readLine();
       if (line != null){
         lineNr++;
         lastchar = 0;
       }
       return line;
    }
  }

  public void reset() throws IOException {
    synchronized(lock){
      super.reset();
      lineNr = markline;
      lastchar = markchar;
    }
  }

  public void setLineNumber(int lineNumber){
    synchronized(lock){
      lineNr = lineNumber;
    }
  }

  public long skip(long count) throws IOException {
    if(count <= 0){
      return 0;
    }
    synchronized(lock){
      char [] chars = new char[2048 > count ? (int)count : 2048];
      long skip = count;
      do {
        // the read will count '\n' and '\r' an "\r\n"
        int rd = read(chars, 0, (chars.length > skip ? (int)skip : chars.length));
        if(rd == -1){
          break;
        }
        skip -= rd;
      }while(skip > 0);
      return count - skip;
    }
  }
}

