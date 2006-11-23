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
//import java.io.*;


public class TEST_ObjectInputStream_Instanceof extends Mv_Assert {
  private SAMPLE_ObjectStream_B root, b;

  private Object object;

  private ObjectInputStream ois;
  private ByteArrayInputStream bais;

  private byte[] data;




  public TEST_ObjectInputStream_Instanceof() {
    super();

    data = new byte[SAMPLE_SerialObject.dataInt.length];
    for (int i=0;i<SAMPLE_SerialObject.dataInt.length;i++) {
      data[i]=(byte)SAMPLE_SerialObject.dataInt[i];
    }


    root=SAMPLE_SerialObject.getRootObject();


  }

  public void setUp() {
    openStream(data);

    try {
      object = ois.readObject();
    }catch(IOException e) {
      fail(e.toString());
    }catch(ClassNotFoundException cnfe) {
      fail(cnfe.toString());
    }
    closeStream();

    b=(SAMPLE_ObjectStream_B)object;
  }



  public void tearDown() {
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
    assertTrue("read object not an instance of original",
            object instanceof SAMPLE_ObjectStream_B);

    assertTrue("read object not an instance of SAMPLE_ObjectStream_B",
                b.getLeft() instanceof SAMPLE_ObjectStream_B);

    assertTrue("read object not an instance of SAMPLE_ObjectStream_C",
                b.getRight() instanceof SAMPLE_ObjectStream_C);

    assertTrue("read object not an instance of SAMPLE_ObjectStream_A",
                b.getLeft().getLeft() instanceof SAMPLE_ObjectStream_A);

    assertTrue("read object not an instance of SAMPLE_ObjectStream_C",
                b.getLeft().getRight() instanceof SAMPLE_ObjectStream_C);
    }

}
