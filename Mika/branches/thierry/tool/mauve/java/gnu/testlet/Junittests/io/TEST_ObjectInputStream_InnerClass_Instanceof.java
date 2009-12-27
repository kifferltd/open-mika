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


public class TEST_ObjectInputStream_InnerClass_Instanceof extends Mv_Assert {
  private SAMPLE_ObjectStream_OuterClass outerClassAfter;

  private Object object;

  private ObjectInputStream ois;
  private ByteArrayInputStream bais;

  private byte[] data;


  public TEST_ObjectInputStream_InnerClass_Instanceof() {
    super();

    data = new byte[SAMPLE_SerialObject.icDataInt.length];
    for (int i=0;i<SAMPLE_SerialObject.icDataInt.length;i++) {
      data[i]=(byte)SAMPLE_SerialObject.icDataInt[i];
    }



  }
  protected void runTest()
  {
	testInstanceOf();
	testPublicInnerClass();
	testPrivateInnerClass();
  }

  public void setUp() {
     openStream(data);

    try {
      object = ois.readObject();
    }catch(Exception e) {
      fail(e.toString());
    }
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


  public void testInstanceOf() {
    assertTrue("read object not an instance of original",
            object instanceof SAMPLE_ObjectStream_OuterClass);
  }

  public void testPublicInnerClass() {
    outerClassAfter=(SAMPLE_ObjectStream_OuterClass)object;
    assertTrue("read object not an instance of SAMPLE_ObjectStream_OuterClass.PublicInnerClass",
              outerClassAfter.puic instanceof SAMPLE_ObjectStream_OuterClass.PublicInnerClass);
  }

  public void testPrivateInnerClass() {
    outerClassAfter=(SAMPLE_ObjectStream_OuterClass)object;
    assertTrue("read object not an instance of SAMPLE_ObjectStream_OuterClass.PrivateInnerClass",
              outerClassAfter.isInstanceOfPrivateInnerClass(outerClassAfter.pric));
  }
}
