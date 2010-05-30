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

package wonka.net.http;

import java.io.*;

public class ChunkedInputStream extends InputStream {

  private InputStream in;
  private int count;
  private boolean eof;

  ChunkedInputStream(InputStream in) throws IOException {
    this.in = in;
    nextChunk(false);
  }

  public void close() throws IOException {
    count=0;
    in.close();
  }

  public int read() throws IOException {
    if(eof){
      return -1;
    }
    if(count == 0 && nextChunk(true)){
      return -1;
    }
    count--;
    int rd = in.read();
    if(rd == -1){
      throw new IOException("unexpected EOF");
    }
    return rd;
  }

  public int read(byte[] bytes, int o, int l) throws IOException {
    if(eof){
      return -1;
    }
    if(count == 0 && nextChunk(true)){
      return -1;
    }
    if(count < l){
      l = count;
    }
    int rd = in.read(bytes,o,l);
    if(rd == -1){
      throw new IOException();
    }

    count -= rd;
    return rd;
  }

  private boolean nextChunk(boolean skip) throws IOException {
    try {
      if(skip){
        in.skip(2);
      }
      String line = readLine();
      int idx = line.indexOf(';');
      if(idx != -1){
        line = line.substring(0,idx);
      }
      int size = Integer.parseInt(line.trim(),16);

      if(size == 0){
        BasicHttpURLConnection.debug("HTTP: end of chunked stream");
        eof = true;
        //read Trailer ...
        while(!"".equals(readLine())){}
        return true;
      }
      else {
        BasicHttpURLConnection.debug("HTTP: Chunk size = " + size);
      }
      count = size;
      return false;
    }
    catch(RuntimeException rt){
      rt.printStackTrace();
      throw new IOException("bad chunck -- 1");
    }
  }

  private String readLine() throws IOException {
    StringBuffer buf = new StringBuffer(64);
    int ch = in.read();
    while(ch != -1){
      if(ch == '\r'){
        ch = in.read();
        if(ch == '\n'){
          break;
        }
        throw new IOException("bad chunck -- 2");
      }
      buf.append((char)ch);
      ch = in.read();
    }
    return buf.toString();
  }
}
