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


package gnu.testlet.wonka.io.Serialization;

import gnu.testlet.*;
import java.util.Vector;
import gnu.testlet.*;
import java.io.*;

public class SMObjectInputStreamTest extends Mv_Assert {

  public SMObjectInputStreamTest () {
    super ();
  }

  private ObjectInputStream ois;
  private READ_File file;

  public void setUp() {
  }

  private void setUpFile (String filename) {
    ois = file.open(filename);
  }

  public void tearDown() {
    file.close();
    ois = null;
  }

  public void testBlockdata1() {
    setUpFile("DUMPdata1");

    System.out.println ("If there was an IOException this cannot be printed, got ois "+ois);

    try {
      assertTrue(ois.readBoolean() == false);
      assertTrue(ois.readBoolean() == true);
      assertEqual (10, ois.readInt());
      assertEqual (16, ois.readLong());
      assertEqual (5,  ois.readShort());
    } catch (IOException exc) {
      Assert.problem (exc, "IO problem.");
    }
  }

  public void testBlockdata2() {
    setUpFile("DUMPdata2");

    try {
      assertApproximatelyEqual (1.0, ois.readDouble(), 0.001);
      assertApproximatelyEqual (-1.0, ois.readFloat(), 0.001);
    } catch (IOException exc) {
      Assert.problem (exc, "IO problem.");
    }
  }

  public void testSimpleClass() {
    setUpFile("DUMPSimple");

    try {
      SM_Simple memory = new SM_Simple();
      SM_Simple streamed = (SM_Simple)ois.readObject();
      assertEqual (memory, streamed);
    } catch (IOException exc) {
      Assert.problem (exc, "IO problem.");
    } catch (ClassNotFoundException exc) {
      Assert.problem (exc, "Class not found....");
    }
  }

  public void testClassString() {
    setUpFile("DUMPString");
    try {
      SM_ClassString memory = new SM_ClassString();
      SM_ClassString streamed = (SM_ClassString)ois.readObject();
      assertEqual (memory, streamed);
    } catch (IOException exc) {
      Assert.problem (exc, "IO problem.");
    } catch (ClassNotFoundException exc) {
      Assert.problem (exc, "Class not found....");
    }
  }

  public void testClassArray() {
    setUpFile("DUMPArray");
    try {
      SM_ClassArray memory = new SM_ClassArray();
      SM_ClassArray streamed = (SM_ClassArray)ois.readObject();
      assertEqual (memory, streamed);
    } catch (IOException exc) {
      Assert.problem (exc, "IO problem.");
    } catch (ClassNotFoundException exc) {
      Assert.problem (exc, "Class not found....");
    }
  }

  public void testVector() {
    setUpFile("DUMPVector");
    try {
      Vector streamed = (Vector)ois.readObject();
      assertEqual (streamed.elementAt(0), "Hello world");
      assertEqual (streamed.elementAt(1), new Integer(666));
    } catch (IOException exc) {
      Assert.problem (exc, "IO problem.");
    } catch (ClassNotFoundException exc) {
      Assert.problem (exc, "Class not found....");
    }
  }

  public void testStringBuffer() {
    setUpFile("DUMPStringBuffer");
    try {
      StringBuffer streamed = (StringBuffer)ois.readObject();
      assertEqual (new String(streamed), "Hello");
    } catch (IOException exc) {
      Assert.problem (exc, "IO problem.");
    } catch (ClassNotFoundException exc) {
      Assert.problem (exc, "Class not found....");
    }
  }

  public void testInheritance() {
    setUpFile("DUMPSubclass");
    try {
      ClassSubclass memory = new ClassSubclass();
      ClassSubclass streamed = (ClassSubclass)ois.readObject();
      assertEqual (memory, streamed);
    } catch (IOException exc) {
      Assert.problem (exc, "IO problem.");
    } catch (ClassNotFoundException exc) {
      Assert.problem (exc, "Class not found....");
    }
  }

  public void testUnderscore() {
    setUpFile("DUMPUnderscore");
    try {
      SM_ClassUnderscore memory = new SM_ClassUnderscore();
      SM_ClassUnderscore streamed = (SM_ClassUnderscore)ois.readObject();
      assertEqual (memory, streamed);
    } catch (IOException exc) {
      Assert.problem (exc, "IO problem.");
    } catch (ClassNotFoundException exc) {
      Assert.problem (exc, "Class not found....");
    }
  }

  public void testSimpleOuterClass() {
    setUpFile("DUMPOuterClass");
    try {
      SAMPLE_SimpleOuterClass memory = new SAMPLE_SimpleOuterClass();
      SAMPLE_SimpleOuterClass streamed = (SAMPLE_SimpleOuterClass)ois.readObject();
      assertEqual (memory, streamed);
    } catch (IOException exc) {
      Assert.problem (exc, "IO problem.");
    } catch (ClassNotFoundException exc) {
      Assert.problem (exc, "Class not found....");
    }
  }

  public void testBlockdatalong() {
    setUpFile("DUMPBlockdatalong");
    try {
      for (int i = 0; i < 1022; i++) {
        int zero = ois.readByte();
      }
      assertEqual(0x01020304, ois.readInt());

      try {
        ois.readByte();
        fail("Should never be here!");
      }
      catch (Exception exc) {
        assertTrue(true);
      }
    }
    catch (IOException exc) {
      Assert.problem (exc, "IO Problem.");
    }
  }

  public void testStringBufferArray() {
    setUpFile("DUMP_SBA");
    try {
      Object o = ois.readObject();
      System.out.println("CLASSNAME: " + o.getClass().getName());
      StringBuffer[] stringbufferArray = (StringBuffer[])o;
      assertTrue(stringbufferArray.length == 2);
      if (stringbufferArray[0] == null) {
        System.out.println("It fucking is null.");
      }
      System.out.println("CLASSNAME: " + stringbufferArray[0].getClass().getName());
      assertTrue(stringbufferArray[0].toString().equals(""));
      assertTrue(stringbufferArray[1].toString().equals(""));
    }
    catch (IOException exc) {
      Assert.problem (exc, "IO Problem.");
    }
    catch (ClassNotFoundException exc) {
      Assert.problem (exc, "Class not found....");
    }
    catch (RuntimeException exc) {
      exc.printStackTrace();
      Assert.problem (exc, "Other problem....");
    }
  }

  public void testCreateByteArray() {
    setUpFile("DUMP_CreateByteArray");
    try {
      Object o = ois.readObject();
      th.check(o instanceof CreateByteArray, "got "+o);
    } catch (IOException exc) {
      Assert.problem (exc, "IO problem.");
    } catch (ClassNotFoundException exc) {
      Assert.problem (exc, "Class not found....");
    }
  }



  public void runTest() {
	
    	file = new READ_File();
/*    	
    	th.setclass("java.io.ObjectInputStream");
    	th.checkPoint("testStringBufferArray");
	testStringBufferArray();
    	th.checkPoint("testBlockdatalong");
	testBlockdatalong();
    	th.checkPoint("testBlockData1");
	testBlockdata1();
    	th.checkPoint("testBlockData2");
	testBlockdata2();
    	th.checkPoint("testSimpleClass");
	testSimpleClass();
    	th.checkPoint("testClassString");
	testClassString();
    	th.checkPoint("testClassArray");
	testClassArray();
    	th.checkPoint("testVector");
	testVector();
    	th.checkPoint("testStringBuffer");
       	testStringBuffer();
    	th.checkPoint("testInheritance");
       	testInheritance();
    	th.checkPoint("testUnderscore");
       	testUnderscore();
    	th.checkPoint("testSimpleOuterClass");
       	testSimpleOuterClass();
*/    	
    	th.checkPoint("CreateByteArray(<clinit>)");
       	testCreateByteArray();

  }
}
