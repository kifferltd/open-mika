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

public abstract class DataBuffer {

  public static final int TYPE_BYTE = 0;
  public static final int TYPE_DOUBLE = 1;
  public static final int TYPE_FLOAT = 2;
  public static final int TYPE_INT = 3;
  public static final int TYPE_SHORT = 4;
  public static final int TYPE_UNDEFINED = 5;
  public static final int TYPE_USHORT = 6;

  protected int banks;
  protected int dataType;
  protected int offset;
  protected int[] offsets;
  protected int size;
  
  protected DataBuffer(int dataType, int size, int numBanks, int[] offsets) {
    this.dataType = dataType;
    this.size = size;
    this.banks = numBanks;
    this.offset = offsets[0];
    this.offsets = offsets;
  }

  protected DataBuffer(int dataType, int size, int numBanks, int offset) {
    this(dataType, size, numBanks, new int[]{ offset });
  }
  
  protected DataBuffer(int dataType, int size, int numBanks) {
    this(dataType, size, numBanks, 0);
  }
  
  protected DataBuffer(int dataType, int size) {
    this(dataType, size, 1);
  }
  
  public abstract int getElem(int bank, int i);
  public abstract void setElem(int bank, int i, int val);
  
  public int getDataType() {
    return dataType;
  }
  
  public static int getDataTypeSize(int type) {
    int size = 0;
    switch(type) {
      case TYPE_BYTE:       size = 8;
                            break;
      case TYPE_DOUBLE:     size = 64;
                            break;
      case TYPE_FLOAT:     
      case TYPE_INT:        size = 32;
                            break;
      case TYPE_SHORT:      
      case TYPE_USHORT:     size = 16;
                            break;
    }
    return size;
  }
  
  public int getElem(int i) {
    return 0;
  }
  
  public double getElemDouble(int i) {
    return (double)getElem(i);
  }
  
  public double getElemDouble(int bank, int i) {
    return (double)getElem(bank, i);
  }
  
  public float getElemFloat(int i) {
    return (float)getElem(i);
  }
  
  public float getElemFloat(int bank, int i) {
    return (float)getElem(bank, i);
  }
  
  public int getNumBanks() {
    return banks;
  }
  
  public int getOffset() {
    return offset;
  }
  
  public int[] getOffsets() {
    return offsets;
  }
  
  public int getSize() {
    return size;
  }
  
  public void setElem(int i, int val) {
  }
  
  public void setElemDouble(int i, double val) {
    setElem(i, (int)val);
  }
  
  public void setElemDouble(int bank, int i, double val) {
    setElem(bank, i, (int)val);
  }
  
  public void setElemFloat(int i, float val) {
    setElem(i, (int)val);
  }
  
  public void setElemFloat(int bank, int i, float val) {
    setElem(bank, i, (int)val);
  }
  
}

