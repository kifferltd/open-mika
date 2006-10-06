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


import java.io.Serializable;


public class SAMPLE_ObjectStream_A implements Serializable {
  //test values
  protected byte testByte=1;
  protected int testInt=1;
  protected float testFloat=1;
  protected char testChar='a';
  protected short testShort = 1;
  protected long testLong = 1;
  protected double testDouble=1.1;
  protected boolean testBoolean=true;
  protected SAMPLE_ObjectStream_D d = new SAMPLE_ObjectStream_D();

  private transient int transientInt=1;
  private transient SAMPLE_ObjectStream_D transientD=new SAMPLE_ObjectStream_D("transient D");
  private volatile int volatileInt=100;


  private SAMPLE_ObjectStream_A right, left;


  public SAMPLE_ObjectStream_A() {
  }


  public SAMPLE_ObjectStream_A(int prefix) {
   setTestByte((byte)(getTestByte()+prefix));
    setTestInt(getTestInt()+prefix);
    setTestFloat(getTestFloat()+prefix);
    setTestChar((char)(getTestChar()+prefix));
    setTestShort((short)(getTestShort()+prefix));
    setTestLong(getTestLong()+prefix);
    setTestDouble(getTestDouble()+prefix);
    d.setString(d.toString()+prefix);
    transientD.setString(transientD.toString()+prefix);

    setTransientInt(getTransientInt()+prefix);
    setVolatileInt(getVolatileInt()+prefix);

    boolean b=false;
    if (prefix%2==0) {
      b=true;
    }
    setTestBoolean(b);

  }


  public void setTestByte(byte testByte) {
    this.testByte=testByte;
  }

  public void setTestInt(int testInt) {
    this.testInt=testInt;
  }

  public void setTestFloat(float testFloat) {
    this.testFloat=testFloat;
  }

  public void setTestChar(char testChar) {
    this.testChar=testChar;
  }


  public void setTestShort(short testShort) {
    this.testShort=testShort;
  }

  public void setTestLong(long testLong) {
    this.testLong=testLong;
  }



  public void setTestDouble(double testDouble) {
    this.testDouble = testDouble;
  }

  public void setTestBoolean(boolean testBoolean) {
    this.testBoolean=testBoolean;
  }


  public byte getTestByte() {
    return testByte;
  }


  public int getTestInt() {
    return testInt;
  }

  public float getTestFloat() {
    return testFloat;
  }

  public char getTestChar() {
    return testChar;
  }

  public short getTestShort() {
    return testShort;
  }

  public long getTestLong() {
    return testLong;
  }

  public double getTestDouble() {
    return testDouble;
  }

  public boolean getTestBoolean() {
    return testBoolean;
  }

  public void setTransientInt(int i) {
    transientInt=i;
  }

  public int getTransientInt() {
    return transientInt;
  }

  public void setVolatileInt(int i) {
    volatileInt=i;
  }

  public SAMPLE_ObjectStream_D getTransientD() {
    return transientD;
  }

  public void setTransientD(SAMPLE_ObjectStream_D transientD) {
    this.transientD=transientD;
  }

  public int getVolatileInt() {
    return volatileInt;
  }

  public SAMPLE_ObjectStream_D getD() {
    return d;
  }

  public void setD(SAMPLE_ObjectStream_D d) {
    this.d=d;
  }

  public String toString() {
    return ("["+testByte+","+testInt+","+testFloat+","+testChar+","+testShort
           +","+testLong+","+testBoolean+","+testDouble+","+ (d == null ? "null" : d.toString())+"]");
  }

  public void setRight(SAMPLE_ObjectStream_A right) {
    this.right=right;
  }

  public void setLeft(SAMPLE_ObjectStream_A left) {
    this.left=left;
  }

  public SAMPLE_ObjectStream_A getRight() {
    return right;
  }

  public SAMPLE_ObjectStream_A getLeft() {
    return left;
  }
}
