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

package java.io;

public class PrintWriter extends Writer {

  private boolean autoF;
  private boolean error;
  private boolean closed;

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
    if(!closed) {
      flush();
    }
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
