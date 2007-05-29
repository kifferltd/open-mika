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
