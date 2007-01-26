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
