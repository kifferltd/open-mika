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

public class RandomAccessFile implements DataOutput, DataInput {

  private FileDescriptor fd;
  private native int createFromString(String name, int nativeMode);

  private byte[] buffer = new byte[64];

  public RandomAccessFile(String name, String mode) throws FileNotFoundException {
    this(new File(name), mode);
  }

  public RandomAccessFile(File file, String mode) throws FileNotFoundException {
    int nativeMode;
    if(mode.equals("r")) {
      nativeMode = 0; 
    } else if (mode.equals("rw")) {
      nativeMode = 1; 
    } else if (mode.equals("rwd")){
      nativeMode = 2;
    } else if (mode.equals("rws")) {
      nativeMode = 3;
    } else {
      throw new IllegalArgumentException(mode);
    }    
    if(createFromString(file.getAbsolutePath(), nativeMode) != 0) {
      throw new FileNotFoundException("" + file + " (" + mode + ")");
    }
  }

  public final FileDescriptor getFD()
    throws IOException {
    return fd;
  }

  public native int read() 
    throws IOException;

  private native int readIntoBuffer(byte[]b, int off, int len);

  public int read(byte[] b, int off, int len) 
    throws IOException, NullPointerException, ArrayIndexOutOfBoundsException {
    
    if(off<0 || len<0 || off > b.length - len)
      throw new ArrayIndexOutOfBoundsException();
    if(len == 0) {
      return 0;
    }
    return readIntoBuffer(b, off, len);
  }
 
  public int read(byte[] b)
    throws IOException, NullPointerException {
    if(b.length == 0) return 0; 
    return readIntoBuffer(b, 0, b.length);
  }

  public final void readFully(byte[] b)
    throws IOException, NullPointerException, ArrayIndexOutOfBoundsException, EOFException {
    int rc;
    int l = 0;

    while (l < b.length) {
      rc = read(b, l, b.length - l);
      if (rc == -1) {
        throw new EOFException();
      }
      l += rc;
    }
  }

  public final void readFully(byte[] b, int off, int len)
    throws IOException, NullPointerException, EOFException {
    int rc;
    int l = 0;

    while (l < len) {
      wonka.vm.Etc.woempa(7, "calling read(" + b + ", " + (off + l) + ", " + (len - l));
      rc = read(b, off + l, len - l);
      if (rc == -1) {
        throw new EOFException();
      }
      l += rc;
    }
  }

  public native int skipBytes(int n)
    throws IOException;

  public native void write(int b)
    throws IOException;

  private native void writeFromBuffer(byte[] b, int off, int len)
    throws IOException;

  public void write(byte[] b)
    throws IOException, NullPointerException {

    if(b==null) throw new NullPointerException();
    writeFromBuffer(b, 0, b.length);
  }

  public void write(byte[] b, int off, int len)
    throws IOException, NullPointerException, ArrayIndexOutOfBoundsException {

    if(off<0 || len<0 || off > b.length - len)
      throw new ArrayIndexOutOfBoundsException();

    writeFromBuffer(b, off,len);
  }

  public native long getFilePointer()
    throws IOException;

  public native void seek(long pos)
    throws IOException;

  public native long length()
    throws IOException;

  public native void setLength(long NewLength)
    throws IOException;

  public native void close()
    throws IOException;

  public final boolean readBoolean() throws IOException {
    this.readFully(buffer, 0, 1);

    if(buffer[0] == 0) 
      return false;
    else
      return true;
  }

  public final byte readByte() throws IOException {
    this.readFully(buffer, 0, 1);
    return buffer[0];
  }

  public final int readUnsignedByte() throws IOException {
    this.readFully(buffer, 0, 1);
    return (0 | buffer[0]);
  }

  public final short readShort() throws IOException {
    this.readFully(buffer, 0, 2);
    return (short)((buffer[0] << 8) | buffer[1]);
  }

  public final int readUnsignedShort() throws IOException {
    this.readFully(buffer, 0, 2);
    return ((buffer[0] << 8) | buffer[1]);
  }

  public final char readChar() throws IOException {
    this.readFully(buffer, 0, 2);
    return (char)((buffer[0] << 8) | buffer[1]);
  }

  public final int readInt() throws IOException {
    this.readFully(buffer, 0, 4);
    return (((buffer[0] & 0xFF) << 24) | ((buffer[1] & 0xFF) << 16) | ((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF));
  }

  public final long readLong() throws IOException {
    this.readFully(buffer, 0, 8);
    return (((long)buffer[0] & 0x0ff) << 56) | (((long)buffer[1] & 0x0ff)<< 48) | 
           (((long)buffer[2] & 0x0ff) << 40) | (((long)buffer[3] & 0x0ff) << 32) |
           (((long)buffer[4] & 0x0ff) << 24) | (((long)buffer[5] & 0x0ff) << 16) |
           (((long)buffer[6] & 0x0ff) <<  8) | (buffer[7] & 0x0ff);
  }

  public final float readFloat() throws IOException {
    return Float.intBitsToFloat(this.readInt());
  }

  public final double readDouble() throws IOException {
    return Double.longBitsToDouble(this.readLong());
  }

  public final void writeBoolean(boolean v) throws IOException {
    if(v) this.write(1); else this.write(0);
  }

  public final void writeByte(int v) throws IOException {
    this.write(v);
  }
  
  public final void writeShort(int v) throws IOException {
    this.write(v >> 8);
    this.write(v & 0x00FF);
  }

  public final void writeChar(int v) throws IOException {
    this.write((v >> 8) & 0x000000FF);
    this.write(v & 0x000000FF);
  }

  public final void writeInt(int v)throws IOException {
    this.write((v >> 24) & 0x000000FF);
    this.write((v >> 16) & 0x000000FF);
    this.write((v >> 8) & 0x000000FF);
    this.write(v & 0x000000FF);
  }

  public final void writeLong(long v) throws IOException {
    this.write((int)((v >> 56) & 0x000000FF));
    this.write((int)((v >> 48) & 0x000000FF));
    this.write((int)((v >> 40) & 0x000000FF));
    this.write((int)((v >> 32) & 0x000000FF));
    this.write((int)((v >> 24) & 0x000000FF));
    this.write((int)((v >> 16) & 0x000000FF));
    this.write((int)((v >> 8) & 0x000000FF));
    this.write((int)(v & 0x000000FF));
  }

  public final void writeFloat(float v) throws IOException {
    this.writeInt(Float.floatToIntBits(v));
  }

  public final void writeDouble(double v) throws IOException {
    this.writeLong(Double.doubleToLongBits(v));
  }

  public final String readLine() throws IOException {
    StringBuffer sb = new StringBuffer();
    long pos = getFilePointer();
    long l = length();
    char ch;

    while (pos < l) {
      ch = (char)(read() & 255);
      if (ch == 13) { // CR
        if (pos < l - 1) {
          if (read() != 10) { // LF
            seek(pos + 1);
          }
        }
        break;
      }
      else if (ch == 10) { // LF
        break;
      }
      sb.append(ch);
      ++pos;
    }

    return sb.toString();
  }

  public final String readUTF() 
    throws IOException {
    int UTFlength = this.readUnsignedShort();
    if (UTFlength < 0){
      throw new UTFDataFormatException();
    }
    byte[] bytes = new byte[UTFlength];
    this.readFully(bytes);
    return new String(bytes,"UTF8");
  }

  public final void writeBytes(String s) throws IOException {
    char c[] = s.toCharArray();
    for(int i=0; i < c.length; i++) {
      write((byte)c[i]);
    }
  }

  public final void writeChars(String s) throws IOException {
    char c[] = s.toCharArray();
    for(int i=0; i < c.length; i++) {
      writeChar(c[i]);
    }
  }

  public final void writeUTF(String str) throws IOException {
    byte []b = str.getBytes("UTF8");
    this.writeShort(b.length);
    this.write(b, 0, b.length);
  }

  protected void finalize() throws Throwable {
    if(fd != null) {
      close();
    }
  }
}
