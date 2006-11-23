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


public class TEST_ObjectOutputStream_InnerClass_Values_Private extends Mv_Assert {


  private SAMPLE_ObjectStream_OuterClass outerClass,outerClassAfter;
  private SAMPLE_ObjectStream_A check, original;

  private Object object;

  private ObjectInputStream ois;
  private ByteArrayInputStream bais;
  private ByteArrayOutputStream baos;
  private ObjectOutputStream oos;

  private byte[] output;


  private final static double DELTA=0.000001;


  public TEST_ObjectOutputStream_InnerClass_Values_Private() {
    super();

    outerClass=SAMPLE_SerialObject.getOuterClass();
    original = outerClass.pric;

  }


  public void setUp() {
    openOutputStream();

    try {
      oos.writeObject(outerClass);
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


    outerClassAfter = (SAMPLE_ObjectStream_OuterClass)object;
    check = outerClassAfter.pric;
  }


  public void tearDown() {
    closeInputStream();
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



  protected void runTest() {
    assertEqual( check.getTestByte(), original.getTestByte());
    assertEqual (check.getTestInt(), original.getTestInt());
    assertApproximatelyEqual( check.getTestFloat(), original.getTestFloat(), DELTA);
    assertEqual( check.getTestChar(), original.getTestChar());
    assertEqual(check.getTestShort(), original.getTestShort());
    assertEqual(check.getTestLong(), original.getTestLong());
    assertApproximatelyEqual(check.getTestDouble(), original.getTestDouble(),DELTA);
    assertTrue("boolean values are not equal",check.getTestBoolean() == original.getTestBoolean());
    assertTrue(check.getTransientInt()==0);
    assertTrue(check.getVolatileInt()==original.getVolatileInt());
    assertTrue(check.getD().toString().equals(original.getD().toString()));
    assertNull(check.getTransientD());
  }
}
