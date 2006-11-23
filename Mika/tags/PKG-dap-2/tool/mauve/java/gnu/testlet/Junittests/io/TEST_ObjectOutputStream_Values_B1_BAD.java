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

import gnu.testlet.*;
import java.util.Arrays;


public class TEST_ObjectOutputStream_Values_B1_BAD extends Mv_Assert {


  private SAMPLE_ObjectStream_B root, check, original;

  private Object object;

  private ObjectInputStream ois;
  private ByteArrayInputStream bais;
  private ByteArrayOutputStream baos;
  private ObjectOutputStream oos;

  private byte[] output;


  private final static double DELTA=0.000001;


  public TEST_ObjectOutputStream_Values_B1_BAD() {
    super();

    root=SAMPLE_SerialObject.getRootObject();
    original=root;

  }

  public void setUp() {
    openOutputStream();

    try {
      oos.writeObject(original);
      oos.flush();

    }catch(IOException ioe) {
      closeOutputStream();
      fail(ioe.toString());
    }

    output = baos.toByteArray();

    closeOutputStream();


    openInputStream(output);

    try {
      object = ois.readObject();
    }catch(Exception e) {
      fail(e.toString());
    }
    closeInputStream();


    check=(SAMPLE_ObjectStream_B)object;
  }



  public void tearDown() {
  }

  private void closeOutputStream() {
    try {
      baos.close();
    } catch(IOException ioe) {
      fail(ioe.toString());
    }

  }

  private void openOutputStream() {
    try {
      baos = new ByteArrayOutputStream();
      oos = new ObjectOutputStream(baos);
    }catch(IOException ioe) {
      ioe.printStackTrace();
      fail("unexpected io exception occurred while setting up");
    }
  }


  public void closeInputStream() {
    try {
      bais.close();
    }catch(IOException ioe) {
      fail("IO Exception while closing input stream");
    }

  }

  private void openInputStream(byte [] data) {
    try {
      bais=new ByteArrayInputStream(data);
      ois=new ObjectInputStream(bais);
    }catch(IOException ioe) {
      fail("openStream> "+ioe.toString());
      return;
    }
  }




  public void testValues_Byte() {
    assertEqual( check.getTestByte(), original.getTestByte());
  }

  public void testValues_Int() {
    assertEqual (check.getTestInt(), original.getTestInt());
  }

  public void testValues_Float() {
    assertApproximatelyEqual( check.getTestFloat(), original.getTestFloat(), DELTA);
  }

  public void testValues_Char() {
    assertEqual( check.getTestChar(), 'z'); //BAD TEST--> SHOULD FAIL
  }

  public void testValues_Short() {
    assertEqual(check.getTestShort(), original.getTestShort());
  }

  public void testValues_Long() {
    assertEqual(check.getTestLong(), original.getTestLong());
  }

  public void testValues_Double() {
    assertApproximatelyEqual(check.getTestDouble(), original.getTestDouble(),DELTA);
  }

  public void testValues_Boolean() {
    assertTrue("boolean values are not equal",check.getTestBoolean() == original.getTestBoolean());
  }

  public void testValues_TransientInt() {
    assertTrue(check.getTransientInt()==0);
  }

  public void testValues_VolatileInt() {
    assertEqual(check.getVolatileInt(),original.getVolatileInt());
  }



  public void testValues_ArrayString() {
    if (!Arrays.equals(check.getArrayString(),original.getArrayString())) {
       fail("strings don't match in array");
    }
  }


  public void testValues_ArrayByte() {
    if (!Arrays.equals(check.getArrayByte(),original.getArrayByte())) {
      fail("values don't match in byte array");
    }
  }

  public void testValues_ArrayInt() {
    if (!Arrays.equals(check.getArrayInt(),original.getArrayInt())) {
      fail("values don't match in int array");
    }
  }


  protected void runTestt() {
    if (!Arrays.equals(check.getArrayFloat(),original.getArrayFloat())) {
      fail("values don't match in float array");
    }
    if (!Arrays.equals(check.getArrayShort(),original.getArrayShort())) {
      fail("values don't match in short array");
    }
    if (!Arrays.equals(check.getArrayChar(),original.getArrayChar())) {
      fail("values don't match in char array");
    }
    if (!Arrays.equals(check.getArrayLong(),original.getArrayLong())) {
      fail("values don't match in long array");
    }
    if (!Arrays.equals(check.getArrayDouble(),original.getArrayDouble())) {
      fail("values don't match in double array");
    }
    if (!Arrays.equals(check.getArrayBoolean(),original.getArrayBoolean())) {
      fail("values don't match in boolean array");
    }
    assertNull(check.getTransientArrayString());
    assertTrue(check.getD().toString().equals(original.getD().toString()));
    assertNull(check.getTransientD());
  }

}
