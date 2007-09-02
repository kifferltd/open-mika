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


package javax.microedition.io;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class BasicDatagram implements Datagram {

  void debug(){
    System.out.println("\nBasicDatagram: pointer "+pointer+", offset "+offset+", maxSize "+maxSize);
    System.out.println("Content '"+new String(buffer, offset, pointer - offset)+"'");
    System.out.println("to "+dgram.getAddress()+", port "+dgram.getPort());
    System.out.println("DatagramPacket: offset "+dgram.getOffset()+", length "+dgram.getLength());
    System.out.println("Content '"+new String(dgram.getData(), dgram.getOffset(), dgram.getLength())+"'\n");
  }

  private Input in;
  private Output out;

  private DataInputStream dataIn;
  private DataOutputStream dataOut;

  int pointer = 0;
  int offset = 0;
  byte[] buffer;
  int maxSize;

  DatagramPacket dgram;

  BasicDatagram(byte[] buf, int size){
    dgram = new DatagramPacket(buf,size);
    in = new Input();
    dataIn = new DataInputStream(in);
    out = new Output();
    dataOut = new DataOutputStream(out);
    buffer = buf;
    maxSize = size;
  }

  BasicDatagram(byte[] buf, int size, String name) throws IOException {
    if(name.startsWith("//")){
      name = name.substring(2);
    }
    int index = name.lastIndexOf(':');
    try {
      int port = Integer.parseInt(name.substring(index+1));
      dgram = new DatagramPacket(buf,size, InetAddress.getByName(name.substring(index)),port);
    }
    catch(Exception e){
      throw new IOException("invalid name");
    }
    in = new Input();
    dataIn = new DataInputStream(in);
    out = new Output();
    dataOut = new DataOutputStream(out);
    buffer = buf;
    maxSize = size;
  }

  public String getAddress(){
    InetAddress addr = dgram.getAddress();
    return "//"+(addr == null ? "" : addr.getHostName())+":"+dgram.getPort();
  }

  public byte[] getData(){
    return buffer;
  }
  public int getLength(){
    return maxSize;
  }

  public int getOffset(){
    return offset;
  }

  public void reset(){
    maxSize = buffer.length;
    dgram.setData(buffer, 0, maxSize);
    offset = 0;
    pointer = 0;
  }

  public void setAddress(String name) throws java.io.IOException{
    if(name.startsWith("//")){
      name = name.substring(2);
    }
    int index = name.lastIndexOf(':');
    try {
      int port = Integer.parseInt(name.substring(index+1));
      dgram.setAddress(InetAddress.getByName(name.substring(index)));
      dgram.setPort(port);
    }
    catch(RuntimeException rt){
      throw new IOException("invalid name");
    }
  }

  public void setAddress(Datagram d){
    try {
      setAddress(d.getAddress());
    }
    catch(IOException ioe){}
  }

  public void setData(byte[] data, int offset, int length){
    dgram.setData(data, offset, length);
    buffer = data;
    this.offset = offset;
    pointer = offset;
    maxSize = length;
  }

  public void setLength(int length){
    dgram.setLength(length);
    maxSize = length;
  }

  class Input extends InputStream {

    public int read() throws IOException {
      if(pointer >= maxSize){
        return -1;
      }
      try {
        return ((char)buffer[pointer++]);
      }
      catch(Exception e){
        throw new IOException();
      }
    }

    public int read(byte[] buf, int off, int len) throws IOException {
      if(off<0 || len<0 || off > buf.length - len) {
        throw new ArrayIndexOutOfBoundsException();
      }
      if(pointer >= maxSize){
        return -1;
      }
      int length = maxSize - pointer;
      length = len > length ? length : len;
      System.arraycopy(buffer,pointer,buf,off,length);
      pointer += length;
      return length;
    }
  }

  class Output extends OutputStream {

    public void write(int b) throws IOException {
      if(pointer >= maxSize){
        throw new IOException("Buffer is full");
      }
      try {
        buffer[pointer++] = (byte)b;
      }
      catch(Exception e){
        throw new IOException();
      }
    }

    public void write(byte[] buf, int off, int len) throws IOException {
      if(off<0 || len<0 || off > buf.length - len) {
        throw new ArrayIndexOutOfBoundsException();
      }
      if(pointer >= maxSize){
        throw new IOException("Buffer is full");
      }
      int length = maxSize - pointer;
      length = len > length ? length : len;
      System.arraycopy(buf,off,buffer,pointer,length);
      pointer += length;
      if (length != len){
        throw new IOException("Buffer is full");
      }
    }
  }

  //DataInput API ...
  public void readFully(byte[] b) throws IOException {
    dataIn.readFully(b);
  }

  public void readFully(byte[] b, int off, int len) throws IOException {
    dataIn.readFully(b,off,len);
  }

  public int skipBytes(int n) throws IOException {
    return dataIn.skipBytes(n);
  }

  public boolean readBoolean() throws IOException {
    return dataIn.readBoolean();
  }

  public byte readByte() throws IOException {
    return dataIn.readByte();
  }

  public int readUnsignedByte() throws IOException {
    return dataIn.readUnsignedByte();
  }

  public short readShort() throws IOException {
    return dataIn.readShort();
  }

  public int readUnsignedShort() throws IOException {
    return dataIn.readUnsignedShort();
  }

  public char readChar() throws IOException {
    return dataIn.readChar();
  }

  public int readInt() throws IOException {
    return dataIn.readInt();
  }

  public long readLong() throws IOException {
    return dataIn.readLong();
  }

  public float readFloat() throws IOException {
    return dataIn.readFloat();
  }

  public double readDouble() throws IOException {
    return dataIn.readDouble();
  }

  public String readLine() throws IOException {
    return dataIn.readLine();
  }

  public String readUTF() throws IOException {
    return dataIn.readUTF();
  }

  //DataOutput API
  public void write(int b) throws IOException {
    dataOut.write(b);
  }

  public void write(byte[] b) throws IOException {
    dataOut.write(b);
  }

  public void write(byte[] b, int off, int len) throws IOException {
    dataOut.write(b,off,len);
  }

  public void writeBoolean(boolean v) throws IOException {
    this.dataOut.writeBoolean(v);
  }

  public void writeByte(int v) throws IOException {
    dataOut.writeByte(v);
  }

  public void writeShort(int v) throws IOException {
    dataOut.writeShort(v);
  }

  public void writeChar(int v) throws IOException {
    dataOut.writeChar(v);
  }

  public void writeInt(int v) throws IOException {
    dataOut.writeInt(v);
  }

  public void writeLong(long v) throws IOException {
    dataOut.writeLong(v);
  }

  public void writeFloat(float v) throws IOException {
    dataOut.writeFloat(v);
  }

  public void writeDouble(double v) throws IOException {
    dataOut.writeDouble(v);
  }

  public void writeBytes(String s) throws IOException {
    dataOut.writeBytes(s);
  }

  public void writeChars(String s) throws IOException {
    dataOut.writeChars(s);
  }

  public void writeUTF(String s) throws IOException {
    dataOut.writeUTF(s);
  }
}
