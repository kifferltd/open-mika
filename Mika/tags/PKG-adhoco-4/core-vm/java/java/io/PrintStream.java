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
** $Id: PrintStream.java,v 1.3 2006/10/04 14:24:15 cvsroot Exp $
*/

package java.io;

public class PrintStream extends FilterOutputStream {

  private static final String line_separator = GetSystemProperty.LINE_SEPARATOR;
  private static final byte[] newline_bytes  = line_separator.getBytes();
  private boolean   caughtException;
  private boolean   closed;
  private boolean   autoFlush;
  private String    encoding;

  public PrintStream(OutputStream outputstream) {
    super(outputstream);
    if(outputstream == null){
      throw new NullPointerException();
    }
  }

  public PrintStream(OutputStream outputstream, boolean autoflush,String enc) 
        throws UnsupportedEncodingException { 
    
    this(outputstream);
    this.autoFlush = autoflush;
    this.encoding = enc;
    // just to throw an exception if the encoding is unsupported ...
    "".getBytes(enc);
  }
  
  public PrintStream(OutputStream outputstream, boolean autoflush) {
    this(outputstream);
    autoFlush = autoflush;
  }

  public synchronized void write (int b) {
    try {
      out.write(b);
      // TODO make this use line_separator ...
      if ((char)b =='\n' && autoFlush) {
        flush();
      }
    }
    catch(IOException e) {
      if(e instanceof InterruptedIOException) {
        Thread.currentThread().interrupt();
      }
      else caughtException = true;
    }
  }

  public synchronized void write(byte[] b, int off, int len)
    throws NullPointerException, ArrayIndexOutOfBoundsException
  {
    if(off<0 || len<0 || off > b.length - len)
      throw new ArrayIndexOutOfBoundsException();
    internalwrite(b,off,len);
    if (autoFlush) {
      flush();
    }	
  }

  public void close() {
    if (!closed) {
      flush();
      try {	
        closed=true;
        out.close();	
      }
      catch(IOException e) {
        if(e instanceof InterruptedIOException) {
          Thread.currentThread().interrupt();
        }
        else caughtException = true;
      }
    }
  }

  public synchronized void flush() {
    try {
      out.flush();
    }
    catch(IOException e) {
      if(e instanceof InterruptedIOException) {
        Thread.currentThread().interrupt();
      }
      else caughtException = true;
    }
  }

  protected void setError() {
    caughtException = true;
  }

  public boolean checkError() {
    return caughtException;
  }

//*****************************************************************
// private method internalwrite(byte[] b,int off, int count)
// this is the only place where a byte[] array is written to the field out
//*****************************************************************
  private synchronized void internalwrite(byte[] b, int off, int count) {
    try {
      out.write(b,off,count);
    }
    catch(IOException e) {
      if(e instanceof InterruptedIOException) {
        Thread.currentThread().interrupt();
       }
       else caughtException = true;
    }
  }

//*****************************************************************
// print methods
// all printlmethods pass through print(String s)
//*****************************************************************
  public void print(Object obj) {
    print(String.valueOf(obj));
  } 

  public void print(String s) {
    if(s==null) {
      print("null");
    }
    else try {
      byte[] b;
      if (autoFlush) {
        int flushed = s.lastIndexOf(line_separator);
        if (flushed >= 0) {
          flushed += line_separator.length();
          if (encoding == null) {
            b = s.substring(0, flushed).getBytes();
          }
          else {
            b = s.substring(0, flushed).getBytes(encoding);
          }
          internalwrite(b,0,b.length);
          flush();
          s = s.substring(flushed);
        }
        if (s.lastIndexOf(line_separator) != -1) {
          flush();
        }
      }
      if (encoding == null) {
        b = s.getBytes();
      }
      else {
        b = s.getBytes(encoding);
      }
      internalwrite(b,0,b.length);
    }
    catch(Exception e) {
    }
  }

  public void print(char[] s) throws NullPointerException {
    print(new String(s));
  }

  public void print(boolean b) {
    print(String.valueOf(b));
  }

  public void print(char c) {
    print(String.valueOf(c));
  }

  public void print(int i) {
    print(String.valueOf(i));
  }

  public void print(long l) {
    print(String.valueOf(l));
  }

  public void print(float f) {
    print(String.valueOf(f));
  }

  public void print(double d) {
    print(String.valueOf(d));
  }

//*****************************************************************
// println methods
// all println methods pass through println(String s)
//*****************************************************************
  public void println() {
    println("");
  }

  public void println(Object obj) {
    println(String.valueOf(obj));
  } 

  public void println(String s) {
    if(s==null) {
      println("null");
    }
    else try {
      byte[] b = (encoding == null) ? s.getBytes() : s.getBytes(encoding);
      write(b);
      write(newline_bytes);
    }
    catch(Exception e) {
      System.err.println("Printstream.println(String) threw exception "+e);
    }
  }
 

  public void println(char[] s) 
    throws NullPointerException
  {
      println(new String(s));
  }


  public void println(boolean b) {
    println(String.valueOf(b));
  } 

  public void println(char c) {
    println(String.valueOf(c));
  } 

  public void println(int i) {
    println(String.valueOf(i));
  } 

  public void println(long l) {
    println(String.valueOf(l));
  } 

  public void println(float f) {
    println(String.valueOf(f));
  } 

  public void println(double d) {
    println(String.valueOf(d));
  }
}
