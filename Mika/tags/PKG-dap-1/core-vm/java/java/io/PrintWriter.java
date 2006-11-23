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

package java.io;

public class PrintWriter extends Writer {

  private boolean autoF;
  private boolean error=false;
  private boolean closed=false;

  private static final String line_separator = GetSystemProperty.LINE_SEPARATOR;

  protected Writer out;

  public PrintWriter(OutputStream out){
    this(out,false);
  }

  public PrintWriter(OutputStream out, boolean autoFlush){
    super(out);
    this.out = new OutputStreamWriter(out);
    autoF = autoFlush;
  }

  public PrintWriter(Writer out){
    this(out, false);
  }

  public PrintWriter(Writer out, boolean autoFlush){
    super(out);
    this.out = out;
    autoF = autoFlush;
  }


  public boolean checkError(){
    flush();
    return error;
  }

  public void close(){
    synchronized(lock){
      if(!closed){
        try {
          out.close();

        }
        catch(IOException ioe){
           error = true;
        }
        closed = true;
      }
    }
  }

  public void flush(){
    synchronized(lock){
      try {
        out.flush();
      }
      catch(IOException ioe){
        error = true;
      }
    }
  }

  public void print(boolean b){
    write(String.valueOf(b));
  }

  public void print(char c){
    write(String.valueOf(c));
  }

  public void print(char[] s){
    write(s, 0, s.length);
  }

  public void print(double d){
    write(String.valueOf(d));

  }

  public void print(float f){
    write(String.valueOf(f));
  }

  public void print(int i){
    write(String.valueOf(i));
  }

  public void print(long l){
    write(String.valueOf(l));
  }

  public void print(Object obj){
    write(obj ==  null ? "null" : obj.toString());
  }

  public void print(String s){
    write(s ==  null ? "null" : s);
  }

  public void println(){
    synchronized(lock){
      write(line_separator);
      if(autoF){
        flush();
      }
    }
  }

  public void println(boolean b){
    synchronized(lock){
      print(b);
      println();
    }
  }

  public void println(char c){
    synchronized(lock){
      try {
        out.write(c);
        println();
      }
      catch(IOException ioe){
        error = true;
      }
    }
  }

  public void println(char[] c){
    synchronized(lock){
      try {
        out.write(c);
        println();
      }
      catch(IOException ioe){
        error = true;
      }
    }
  }

  public void println(double d){
    synchronized(lock){
      print(d);
      println();
    }
  }

  public void println(float f){
    synchronized(lock){
      print(f);
      println();
    }
  }

  public void println(int i){
    synchronized(lock){
      print(i);
      println();
    }
  }

  public void println(long l){
    synchronized(lock){
      print(l);
      println();
    }
  }

  public void println(Object obj){
    synchronized(lock){
      print(obj);
      println();
    }
  }

  public void println(String s){
    synchronized(lock){
      print(s);
      println();
    }
  }
  protected void setError(){
    error = true;
  }

  public void write(char[] buf){
    write(buf, 0, buf.length);
  }

  public void write(char[] buf, int off, int len){
    try {
      synchronized(lock){
         out.write(buf, off, len);
      }
    }
    catch(IOException ioe){
      error = true;
    }
  }

  public void write(int ch){
    try {
      synchronized(lock){
         out.write(ch);
      }
    }
    catch(IOException ioe){
      error = true;
    }
  }

  public void write(String s){
    try {
      synchronized(lock){
         out.write(s, 0, s.length());
      }
    }
    catch(IOException ioe){
      error = true;
    }
  }

  public void write(String s, int off, int len){
    try {
      synchronized(lock){
         out.write(s, off, len);
      }
    }
    catch(IOException ioe){
      error = true;
    }
  }
}
