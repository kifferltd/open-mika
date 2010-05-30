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


package gnu.testlet.Junittests.io;    

import java.io.*;


public class SAMPLE_ObjectStream_B extends SAMPLE_ObjectStream_A {

  protected String [] arrayString={"test","another teststring","......"};
  protected int [] arrayInt={1,2,3,4,5,6,7,8,9,0,Integer.MAX_VALUE,Integer.MIN_VALUE};

  protected byte[] arrayByte={-128,6,2,8,4,8,127,1,2};
  protected float[] arrayFloat={(float)8.7,(float)5.2,(float)1.5,(float)78.3,
                                (float)32.99};
  protected char[] arrayChar={'S','m','a','r','t','M','o','v','e'};
  protected short[] arrayShort={9,123,67,9,4656,98,-987};
  protected long[] arrayLong={0,98654,Long.MIN_VALUE,Long.MAX_VALUE};
  protected double[] arrayDouble={44.65556,32.45,654.5,-45.3};
  protected boolean[] arrayBoolean={true,false,true,true,false};

  protected transient String [] transientArrayString = {"transient:","this",
            "array ", "should ","be ", "null!!"};

  public SAMPLE_ObjectStream_B() {
  }


  public SAMPLE_ObjectStream_B(int prefix) {
    super(prefix);

    for(int i=0;i<arrayString.length;i++) {
      arrayString[i]+=String.valueOf(prefix);
    }
    for(int i=0;i<transientArrayString.length;i++) {
      transientArrayString[i]+=String.valueOf(prefix);
    }
    for(int i=0;i<arrayInt.length-2;i++) {
      arrayInt[i]+=prefix;
    }
    for(int i=0;i<arrayByte.length-2;i++) {
      arrayByte[i]+=prefix;
    }
    for(int i=0;i<arrayFloat.length-2;i++) {
      arrayFloat[i]+=prefix;
    }
    for(int i=0;i<arrayChar.length-2;i++) {
      arrayChar[i]+=prefix;
    }
    for(int i=0;i<arrayShort.length-2;i++) {
      arrayShort[i]+=prefix;
    }
    for(int i=0;i<arrayLong.length-2;i++) {
      arrayLong[i]+=prefix;
    }
    for(int i=0;i<arrayDouble.length-2;i++) {
      arrayDouble[i]+=prefix;
    }

    for(int i=0;i<arrayBoolean.length;i++) {
      if (prefix%2==0) {
        arrayBoolean[i]=!arrayBoolean[i];
      }
    }

    for(int i=0;i<transientArrayString.length;i++) {
      transientArrayString[i]+=String.valueOf(prefix);
    }

  }


  public void setArrayString(String [] arrayString) {
    this.arrayString=arrayString;
  }

  public void setArrayInt(int [] arrayInt) {
    this.arrayInt=arrayInt;
  }


  public String[] getArrayString() {
    return arrayString;
  }

  public int[] getArrayInt() {
    return arrayInt;
  }

  public byte[] getArrayByte() {
    return arrayByte;
  }

  public float[] getArrayFloat() {
    return arrayFloat;
  }

  public char[] getArrayChar() {
    return arrayChar;
  }

  public short[] getArrayShort() {
    return arrayShort;
  }

  public long[] getArrayLong() {
    return arrayLong;
  }

  public double[] getArrayDouble() {
    return arrayDouble;
  }

  public boolean[] getArrayBoolean() {
    return arrayBoolean;
  }


  public String[] getTransientArrayString () {
    return transientArrayString;
  }


  public String toString() {
    StringBuffer buffer = new StringBuffer();

    buffer.append("arrayString: ");
    if (arrayString != null) {
      buffer.append("[");
      for (int i = 0; i < arrayString.length; i++) {
        buffer.append (arrayString[i]);
        if (i < arrayString.length-1) {
          buffer.append (" | ");
        }
      }
      buffer.append("]");
    }
    else {
      buffer.append("null");
    }
    buffer.append("\n");


    buffer.append("arrayInt: ");
    if (arrayInt != null) {
      buffer.append("[");
      for (int i = 0; i < arrayInt.length; i++) {
        buffer.append (arrayInt[i]);
        if (i < arrayInt.length-1) {
          buffer.append (" | ");
        }
      }
      buffer.append("]");
    }
    else {
      buffer.append("null");
    }
    buffer.append("\n");

    return super.toString() + " - " + new String(buffer);
  }
}
