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
** $Id: OutputStreamWriter.java,v 1.1.1.1 2004/07/12 14:07:45 cvs Exp $
*/

package java.io;

import wonka.decoders.Decoder;

public class OutputStreamWriter extends Writer {

  private static final String default_encoding = GetSystemProperty.FILE_ENCODING;

  private OutputStream out;
  private Decoder decoder;
  private StringBuffer buffer;

  public OutputStreamWriter(OutputStream out) {
    super(out);
    decoder = Decoder.getDefault(default_encoding);
    buffer = new StringBuffer(1024);
    this.out = out;
  }

  public OutputStreamWriter(OutputStream out, String enc) throws UnsupportedEncodingException {
    super(out);
    decoder = Decoder.get(enc);
    buffer = new StringBuffer(1024);
    this.out = out;
  }

  public void close() throws IOException {
    synchronized(lock){
      if(out != null){
        out.write(decoder.cToB(buffer));
        out.close();
        decoder = null;
        buffer = null;
        out = null;
      }
    }
  }

  public void flush() throws IOException {
    synchronized(lock){
      if(out == null){
        throw new IOException("Reader is closed");
      }
      out.write(decoder.cToB(buffer));
      buffer.setLength(0);
      out.flush();
    }
  }

  public String getEncoding(){
    return decoder != null ? decoder.getEncoding() : null;
  }

  public void write(int ch) throws IOException {
    synchronized(lock){
      if(out == null){
        throw new IOException("Reader is closed");
      }
      buffer.append((char)ch);
      if(buffer.length() > 1024){
        out.write(decoder.cToB(buffer));
        buffer.setLength(0);
      }
    }
  }

  public void write(char[] chars, int off, int len) throws IOException {
    synchronized(lock){
      if(out == null){
        throw new IOException("Reader is closed");
      }
      buffer.append(chars, off, len);
      if(buffer.length() > 1024){
        out.write(decoder.cToB(buffer));
        buffer.setLength(0);
      }
    }
  }

  public void write(String str, int off, int len) throws IOException {
    synchronized(lock){
      if(out == null){
        throw new IOException("Reader is closed");
      }
      buffer.append(str.substring(off, off+len));
      if(buffer.length() > 1024){
        out.write(decoder.cToB(buffer));
        buffer.setLength(0);
      }
    }
  }
}
