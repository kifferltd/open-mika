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
      int lc = lastchar;
      lastchar = rd;
      if ((rd == '\n' && lc != '\r') || rd == '\r'){
        lineNr++;
        // if a line separator is encountered then we return '\n'
        return '\n';
      } else if(rd == '\n') {
        return read();
      }
      return rd;
    }
  }

  public int read(char[] chars, int off, int len) throws IOException {
    synchronized(lock){
      int rd = super.read(chars, off, len);
      for (int i = rd; i > 0; i--){
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

