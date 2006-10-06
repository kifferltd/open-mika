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


package com.acunia.wonka.net.http;

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
      int size = Integer.parseInt(line,16);

      if(size == 0){
        eof = true;
        //read Trailer ...
        while(!"".equals(readLine())){}
        return true;
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
    //System.out.println("ChunkedInputStream: readLine returns '"+buf+"'");
    return buf.toString();
  }
}
