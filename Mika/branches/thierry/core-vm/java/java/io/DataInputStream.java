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
** $Id: DataInputStream.java,v 1.2 2005/11/29 10:20:16 cvs Exp $
*/

package java.io;

import wonka.vm.ArrayUtil;

public class DataInputStream extends FilterInputStream implements DataInput {

  /**
  ** TODO checks for this condition ...
  */
  private boolean lastWasReturn = false;

  private byte[] buffer = new byte[8];

  public DataInputStream(InputStream input) {
    super(input);
  }

  public final int read(byte[] bytes) throws java.io.IOException {
    return read(bytes, 0, bytes.length);
  }

  public final int read(byte[] bytes, int offset, int length) throws java.io.IOException {
    if(lastWasReturn){
      int rd = in.read();
      if(rd == -1){
        return -1;
      }
      if(rd == '\n'){
        return in.read(bytes, offset, length);
      }
      bytes[offset++] = (byte)rd;
      if(in.available() > 0){
        return in.read(bytes, offset, length-1);
      }
      return 1;
    }
    return in.read(bytes, offset, length);
  }

  public final void readFully(byte[] b) throws IOException {
    readFully(b, 0, b.length);
  }

  public final void readFully(byte[] b, int off, int len) throws IOException {
    int done = 0;
    do {
      int read = in.read(b, off+done, len-done);
      if(read < 0){
  	    throw new EOFException();
      }
      done += read;
    } while(done < len);
  }

  public final int skipBytes(int n) throws IOException {
    int done = 0;
    while(done < n){
      int skipped = (int)in.skip(n-done);
      if(skipped == 0){
  	    throw new EOFException();
      }
      done += skipped;
    }
    return done;
  }

  public final boolean readBoolean() throws IOException {
    int next = in.read();
    if (next == -1){
        throw new EOFException();
    }
    return (next > 0);
  }

  public final byte readByte() throws IOException {
    int next = in.read();
    if (next == -1){
      throw new EOFException();
    }
    return (byte)next;
  }

  public final int readUnsignedByte() throws IOException {
    int next = in.read();
    if (next == -1){
      throw new EOFException();
    }
    return next;
  }

  public final short readShort() throws IOException {
    return (short)readUnsignedShort();
  }

  public final int readUnsignedShort() throws IOException {

    int read = in.read(this.buffer, 0, 2);

    if(read == 1){
      read = in.read(buffer, 1, 1);
    }
    if (read == -1) {
      throw new EOFException();
    }

    return ((this.buffer[0] & 0xff) << 8) | (this.buffer[1] & 0xff);

  }

  public final char readChar() throws IOException {
    return (char) readUnsignedShort();
  }

  public final int readInt() throws IOException {
    readFully(buffer, 0, 4);
    return ArrayUtil.bArrayToI(buffer,0);
  }

  public final long readLong() throws IOException {
    readFully(buffer, 0, 8);
    return ArrayUtil.bArrayToL(buffer,0);
  }
  
  public final float readFloat() throws IOException {
    readFully(buffer, 0, 4);
    return ArrayUtil.bArrayToF(buffer,0);
  }

  public final double readDouble() throws IOException {
    readFully(buffer, 0, 8);
    return ArrayUtil.bArrayToD(buffer,0);
  }

  public final String readLine() throws IOException {
    StringBuffer result = new StringBuffer();
    while(true) {
      int next = in.read();
      if (next == -1){
  	    if(result.length() == 0){
  	      return null;
  	    }
  	    break;
      }
      if (next == '\r') {
        lastWasReturn = true;
  	    break;
      }	
      if (next == '\n') {
        if(!lastWasReturn) {
    	    break;
        }
        continue;
      }	
      result.append((char)(next & 0xff));
      lastWasReturn = false;
    }
    return result.toString();
  }

  public final String readUTF() throws IOException {
    int UTFlength = readUnsignedShort();
    if (UTFlength < 0){
      throw new UTFDataFormatException();
    }
    byte[] bytes = new byte[UTFlength];
    readFully(bytes);
    return new String(bytes,"UTF8");
  }

  public static final String readUTF(DataInput in) throws IOException {
    int UTFlength = in.readUnsignedShort();
    if (UTFlength < 0){
      throw new UTFDataFormatException();
    }
    byte[] bytes = new byte[UTFlength];
    in.readFully(bytes);
    return new String(bytes,"UTF8");
  }
}
