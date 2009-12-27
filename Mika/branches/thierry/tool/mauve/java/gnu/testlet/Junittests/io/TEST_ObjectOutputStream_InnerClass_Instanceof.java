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


public class TEST_ObjectOutputStream_InnerClass_Instanceof extends Mv_Assert {
  private SAMPLE_ObjectStream_OuterClass outerClassAfter, outerClass;

  private Object object;

  private byte[] output;

  private ObjectInputStream ois;
  private ByteArrayInputStream bais;
  private ByteArrayOutputStream baos;
  private ObjectOutputStream oos;


  public TEST_ObjectOutputStream_InnerClass_Instanceof() {
    super();

  }

  public void setUp() {
    outerClass=SAMPLE_SerialObject.getOuterClass();

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
    assertTrue("read object not an instance of original",
            object instanceof SAMPLE_ObjectStream_OuterClass);
    outerClassAfter=(SAMPLE_ObjectStream_OuterClass)object;
    assertTrue("read object not an instance of SAMPLE_ObjectStream_OuterClass.PublicInnerClass",
              outerClassAfter.puic instanceof SAMPLE_ObjectStream_OuterClass.PublicInnerClass);
  
    outerClassAfter=(SAMPLE_ObjectStream_OuterClass)object;
    assertTrue("read object not an instance of SAMPLE_ObjectStream_OuterClass.PrivateInnerClass",
              outerClassAfter.isInstanceOfPrivateInnerClass(outerClassAfter.pric));
  }
}
