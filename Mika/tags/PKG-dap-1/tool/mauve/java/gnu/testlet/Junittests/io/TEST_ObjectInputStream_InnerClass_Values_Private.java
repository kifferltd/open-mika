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


public class TEST_ObjectInputStream_InnerClass_Values_Private extends Mv_Assert {


  private SAMPLE_ObjectStream_OuterClass outerClass,outerClassAfter;
  private SAMPLE_ObjectStream_A check, original;

  private Object object;

  private ObjectInputStream ois;
  private ByteArrayInputStream bais;

  private byte[] data;

  private final static double DELTA=0.000001;


  public TEST_ObjectInputStream_InnerClass_Values_Private() {
    super();

    data = new byte[SAMPLE_SerialObject.icDataInt.length];
    for (int i=0;i<SAMPLE_SerialObject.icDataInt.length;i++) {
      data[i]=(byte)SAMPLE_SerialObject.icDataInt[i];
    }

    outerClass=SAMPLE_SerialObject.getOuterClass();
    original = outerClass.pric;


  }

  public void setUp() {
    openStream(data);

    try {
      object = ois.readObject();
    }catch(Exception e) {
      fail(e.toString());
    }


    outerClassAfter = (SAMPLE_ObjectStream_OuterClass)object;
    check = outerClassAfter.pric;
  }


  public void tearDown() {
    closeStream();
  }



  public void closeStream() {
    try {
      bais.close();
    }catch(IOException ioe) {
      fail("IO Exception while closing input stream");
    }

  }

  private void openStream(byte [] data) {
    try {
      bais=new ByteArrayInputStream(data);
      ois=new ObjectInputStream(bais);
    }catch(IOException ioe) {
      fail("openStream> "+ioe.toString());
      return;
    }
  }

  protected void runTest()
	{
    	assertEqual( check.getTestByte(), original.getTestByte());
    	assertEqual (check.getTestInt(), original.getTestInt());
    	assertApproximatelyEqual( check.getTestFloat(), original.getTestFloat(), DELTA);
    	assertEqual( check.getTestChar(), original.getTestChar());
    	assertEqual(check.getTestShort(), original.getTestShort());
    	assertEqual(check.getTestLong(), original.getTestLong());
    	assertApproximatelyEqual(check.getTestDouble(), original.getTestDouble(),DELTA);
    	assertTrue("boolean values are not equal",check.getTestBoolean() == original.getTestBoolean());
    	assertTrue(check.getTransientInt()==0);
    	assertEqual(check.getVolatileInt(),original.getVolatileInt());
   	assertTrue(check.getD().toString().equals(original.getD().toString()));
    	assertNull(check.getTransientD());
  	}
}
