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


public class TEST_ObjectOutputStream_Values_C1 extends Mv_Assert {


  private SAMPLE_ObjectStream_B root;
  private SAMPLE_ObjectStream_C check, original;

  private Object object;

  private ObjectInputStream ois;
  private ByteArrayInputStream bais;
  private ByteArrayOutputStream baos;
  private ObjectOutputStream oos;

  private byte[] output;


  private final static double DELTA=0.000001;


  public TEST_ObjectOutputStream_Values_C1() {
    super();

    root=SAMPLE_SerialObject.getRootObject();
    original=(SAMPLE_ObjectStream_C)(root.getRight());

  }

  public void setUp() {
    openOutputStream();

    try {
      oos.writeObject(root);
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
    }    closeInputStream();


    check=(SAMPLE_ObjectStream_C)((SAMPLE_ObjectStream_B)object).getRight();
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
