/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
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
*   Philips site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/


package javax.crypto;

import java.io.FilterInputStream;
import java.io.InputStream;
import java.io.IOException;

public class CipherInputStream extends FilterInputStream {

  private Cipher cipher;
  private byte[] buffer = new byte[512];
  private int index = buffer.length;

  public CipherInputStream(InputStream in, Cipher c){
    super(in);
    cipher = c;
  }

  protected CipherInputStream(InputStream in) {
    super(in);
    cipher = new NullCipher();
  }

  public int available() throws IOException {
    return cipher.getOutputSize(in.available());
  }

  public void close() throws IOException {
    if(cipher != null){
      in.close();
      cipher = null;
    }
  }

  public boolean markSupported() {
    return false;
  }

  public int read() throws IOException {
    if(cipher == null){
      throw new IOException("STREAM IS CLOSED");
    }
    if(index >= buffer.length && !fillBuffer()){
      return -1;
    }
    return buffer[index++];
  }

  public int read(byte[] bytes) throws IOException {
    return read(bytes , 0, bytes.length);
  }

  public int read(byte[] bytes, int offset, int length) throws IOException {
    if(cipher == null){
      throw new IOException("STREAM IS CLOSED");
    }
    if(bytes.length  - length < offset || length < 0 || offset < 0){
      throw new IndexOutOfBoundsException();
    }
    int rd = 0;
    if(buffer.length <= index){
      int cp = buffer.length - index;
      cp = (cp > length ? length : cp);
      System.arraycopy(buffer, index, bytes, offset, cp);
      length -= cp;
      rd += cp;
      index += cp;
    }
    while(length > 0){
      if(!fillBuffer()){
        if(rd == 0){
          return -1;
        }
        break;
      }
      int cp = buffer.length;
      cp = (cp > length ? length : cp);
      System.arraycopy(buffer, index, bytes, offset, cp);
      length -= cp;
      rd += cp;
      index += cp;
    }
    return rd;
  }

  public long skip(long n) throws IOException {
    if(cipher == null){
      throw new IOException("STREAM IS CLOSED");
    }
    if(n <= 0){
      return 0;
    }
    long toSkip = n;
    if(buffer.length <= index){
      int cp = buffer.length - index;
      cp = (cp > toSkip ? (int)toSkip : cp);
      index += cp;
      toSkip -= cp;
    }
    while(n > 0){
      if(!fillBuffer()){
        break;
      }
      int cp = buffer.length;
      cp = (cp > toSkip ? (int)toSkip : cp);
      toSkip -= cp;
      index  += cp;
    }
    return n - toSkip;
  }

  private boolean fillBuffer() throws IOException {
    if(buffer.length < 256){
      buffer = new byte[512];
    }
    int rd = in.read(buffer, 0 , buffer.length);
    if(rd == -1){
      return false;
    }
    try {
      buffer = cipher.update(buffer,0, rd);
      index = 0;
    }
    catch(Exception e){
      throw new IOException("WRAPPING EXCEPTION "+e);
    }
    return true;
  }
}
