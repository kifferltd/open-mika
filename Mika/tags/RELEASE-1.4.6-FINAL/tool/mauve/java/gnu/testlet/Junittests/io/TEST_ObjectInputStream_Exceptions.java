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

import gnu.testlet.Mv_Assert;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;


public class TEST_ObjectInputStream_Exceptions extends Mv_Assert {

  private ObjectInputStream ois;
  private ByteArrayInputStream bais;

  private byte[] data, optionalData, writeAbortedData;


  public TEST_ObjectInputStream_Exceptions() {
    
    super();
    data = new byte[SAMPLE_SerialObject.dataInt.length];
    for (int i=0;i<SAMPLE_SerialObject.dataInt.length;i++) {
      data[i]=(byte)SAMPLE_SerialObject.dataInt[i];
    }
    optionalData=new byte[SAMPLE_SerialObject.optionalDataInt.length];
    for (int i=0;i<SAMPLE_SerialObject.optionalDataInt.length;i++) {
      optionalData[i]=(byte)SAMPLE_SerialObject.optionalDataInt[i];
    }
    writeAbortedData = new byte[SAMPLE_SerialObject.writeAbortedDataInt.length];
    for (int i=0;i<SAMPLE_SerialObject.writeAbortedDataInt.length;i++) {
      writeAbortedData[i]=(byte)SAMPLE_SerialObject.writeAbortedDataInt[i];
    }
  }

  protected void runTest()
  {
	th.checkPoint("EOFException");
	testEOFException();
	th.checkPoint("ClassNotFoundException");
	testClassNotFoundException();
	th.checkPoint("StreamCorreptedException");	
	testStreamCorruptedException();
	th.checkPoint("InvalidClassException");	
	testInvalidClassException();
	th.checkPoint("OptionalDataException");	
	testOptionalDataException();
  }

  public void setUp() {

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


  public void testEOFException() {
    openStream(data);

    try {
      for(;;) {
        ois.readObject();
      }
    }catch(EOFException eofe) {
      closeStream();
      return;
    }catch(Exception e) {
      closeStream();
      fail(e.toString());
    }

    closeStream();
    fail("EOFException expected, but not thrown");
  }



  public void testClassNotFoundException() {
    byte [] data1 = (byte[])data.clone();
    //mess up class name
    data1[9] = (byte)'z';
    openStream(data1);

    try {
      ois.readObject();
    }catch(ClassNotFoundException cnfe) {
      closeStream();
      return;
    }catch(IOException ioe) {
      closeStream();
      fail(ioe.toString());
      return;
    }

    fail("ClassNotFoundException expected");
  }



  public void testStreamCorruptedException() {
    byte [] data1 = (byte[])data.clone();
    data1[4]=(byte)0xFF;
    openStream(data1);

    try {
      ois.readObject();
    }catch(StreamCorruptedException sce) {
      closeStream();
      return;
    }catch(Exception e) {
      System.out.println("Exception");
      closeStream();
      //e.printStackTrace();
      fail(e.toString());
      return;
    }

    closeStream();
    fail("StreamCorruptedException expected");
  }



  public void testInvalidClassException() {
    byte [] data1 = (byte[])data.clone();
    //mess up serialVersionUID
    //data1[38]=(byte)0xFF;
    //data1[39]=(byte)0xFF;
    //data1[40]=(byte)0xFF;
    data1[55]=(byte)0xFF;
    openStream(data1);

    try {
      ois.readObject();
    }catch(InvalidClassException ice) {
      closeStream();
      return;
    }catch(Exception e) {
      closeStream();
      fail(e.toString());
      return;
    }

    closeStream();
    fail("InvalidClassException expected");
  }



  public void testOptionalDataException() {
    openStream(optionalData);

    try {
      ois.readObject();
    }catch(OptionalDataException ode) {
      closeStream();
      return;
    }catch(Exception e) {
      closeStream();
      fail(e.toString());
    }

    closeStream();
    fail("OptionalDataException expected");
  }


}