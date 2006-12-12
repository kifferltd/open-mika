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
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

package java.awt.image;

public final class DataBufferByte extends DataBuffer {

  private byte[][] data;

  public DataBufferByte(byte[][] dataArray, int size) {
    this(dataArray, size, new int[]{0});
  }
  
  public DataBufferByte(byte[][] dataArray, int size, int[] offsets) {
    super(DataBuffer.TYPE_BYTE, size, dataArray.length, offsets);
    data = dataArray;
  }
  
  public DataBufferByte(byte[] dataArray, int size) {
    this(dataArray, size, 0);
  }
  
  public DataBufferByte(byte[] dataArray, int size, int offset) {
    super(DataBuffer.TYPE_BYTE, size, 1, offset);
    data = new byte[1][0];
    data[1] = dataArray;
  }
  
  public DataBufferByte(int size) {
    this(size, 1);
  }
  
  public DataBufferByte(int size, int numBanks) {
    super(DataBuffer.TYPE_BYTE, size, numBanks);
    data = new byte[numBanks][size];
  }

  public byte[][] getBankData() {
    return data;
  }

  public byte[] getData() {
    return data[1];
  }
  
  public byte[] getData(int bank) {
    return data[bank];
  }
  
  public int getElem(int i) {
    return getElem(1, i);
  }
  
  public int getElem(int bank, int i) {
    return data[bank][i];
  }
  
  public void setElem(int i, int val) {
    setElem(1, i, val);
  }
  
  public void setElem(int bank, int i, int val) {
    data[bank][i] = (byte)val;
  }
  
}

