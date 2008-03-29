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
import java.util.Vector;
import java.util.Arrays;
import java.io.*;
import gnu.testlet.*;

public class SMObjectOutputStreamTest extends Mv_Assert {

  public SMObjectOutputStreamTest () {
    super ();
  }


    private ByteArrayOutputStream baos;
    private ObjectOutputStream oos;

  public void setUp() {  }

  private void setUpFile () {
    baos = new ByteArrayOutputStream();
    try {
	    oos = new ObjectOutputStream(baos);
    }
    catch(Exception e) { th.debug("got Exception during Setup"); }
  }

  private String toHex (int hexNumber) {
    if (hexNumber == 10) {
      return "A";
    }
    else if (hexNumber == 11) {
      return "B";
    }
    else if (hexNumber == 12) {
      return "C";
    }
    else if (hexNumber == 13) {
      return "D";
    }
    else if (hexNumber == 14) {
      return "E";
    }
    else if (hexNumber == 15) {
      return "F";
    }
    else if (hexNumber < 10) {
      return ""+hexNumber;
    }

    return "X";
  }

  private String toHex (byte b) {
    int i = b;
    if (i < 0) {
      i += 256;
    }

    int part1 = i / 16;
    int part2 = i % 16;

    return toHex(part1) + toHex(part2);
  }

  private void printHex (byte[] bytes) {
    for (int i = 0; i < bytes.length; i++) {
      System.out.print(toHex(bytes[i]) + " ");
    }
  }

  private void printComparison (byte[] one, byte[] other) {
    System.out.println("--- COMPARISON ---");
    int min = Math.min (one.length, other.length);
    for (int i = 0; i < min; i++) {
      System.out.print("" + i + ") " + toHex(one[i]) + " " + ((char)one[i])
                          + " --- " + toHex(other[i]) + " " + ((char)other[i]));
      System.out.print( (one[i] == other[i])  ? "\n" : " *****\n" );
    }

    if (one.length > other.length) {
      for (int i = min; i < one.length; i++) {
        System.out.println("" + i + ") " + toHex(one[i]) + " " + ((char)one[i])
                               + " --- " );
      }
    }
    else if (other.length > one.length) {
      for (int i = min; i < other.length; i++) {
        System.out.println("" + i + ")      --- " + toHex(other[i]) + " " + ((char)other[i]));
      }
    }
    System.out.println("--- END COMPARISON ---");
  }

  public void tearDown() {
    oos = null;
    baos = null;
  }

  public void testBlockdata1() {
    setUpFile();

    try {
      oos.writeBoolean(false);
      oos.writeBoolean(true);
      oos.writeInt(10);
      oos.writeLong(16);
      oos.writeShort(5);
      oos.close();
    } catch (IOException exc) {
      Assert.problem (exc, "IO problem.");
    }
    th.check( Arrays.equals(READ_File.DATABLOCK1,baos.toByteArray()),"checking written data");

  }

  public void testBlockdata2() {
    setUpFile();

    try {
      oos.writeDouble(1.0);
      oos.writeFloat(-1.0f);
      oos.close();
    } catch (IOException exc) {
      Assert.problem (exc, "IO problem.");
    }
    th.check( Arrays.equals(READ_File.DATABLOCK2,baos.toByteArray()),"checking written data");
//    CreateByteArray.printStream( baos.toByteArray() , "testBlockdata2");
  }

  public void testSimpleClass() {
    setUpFile();

    SM_Simple simple = new SM_Simple();
    try {
      oos.writeObject(simple);
      oos.close();
    } catch (IOException exc) {
      Assert.problem (exc, "IO problem.");
    }
    th.check( Arrays.equals(READ_File.Simple,baos.toByteArray()),"checking written data");
  }

  public void testClassString() {
    setUpFile();

    SM_ClassString instance = new SM_ClassString();
    try {
      oos.writeObject(instance);
      oos.close();
    }
    catch (IOException exc) {
      Assert.problem (exc, "IO problem.");
    }
    th.check( Arrays.equals(READ_File.ClassString,baos.toByteArray()),"checking written data");
  }


  public void testClassArray() {
    setUpFile();

    SM_ClassArray instance = new SM_ClassArray();
    try {
      oos.writeObject(instance);
      oos.close();
    }
    catch (IOException exc) {
      Assert.problem (exc, "IO problem.");
    }
    th.check( Arrays.equals(READ_File.ClassArray,baos.toByteArray()),"checking written data");

    printComparison(READ_File.ClassArray,baos.toByteArray());
  }

  public void testVector() {
    setUpFile();

    Vector instance = new Vector();
    instance.add("Hello world");
    instance.add(new Integer(666));
    try {
      oos.writeObject(instance);
      oos.close();
    }
    catch (IOException exc) {
      Assert.problem (exc, "IO problem.");
    }
    th.check( Arrays.equals(READ_File.Vector,baos.toByteArray()),"checking written data");
  }

  public void testStringBuffer() {
    setUpFile();

    StringBuffer instance = new StringBuffer("Hello");
    try {
      oos.writeObject(instance);
      oos.close();
    }
    catch (IOException exc) {
      Assert.problem (exc, "IO problem");
    }
    th.check( Arrays.equals(READ_File.StringBuffer,baos.toByteArray()),"checking written data");

    printComparison(READ_File.StringBuffer,baos.toByteArray());
  }

  public void testInheritance() {
    setUpFile();

    ClassSubclass instance = new ClassSubclass();
    try {
      oos.writeObject(instance);
      oos.close();
    }
    catch (IOException exc) {
      Assert.problem (exc, "IO problem");
    }
    th.check( Arrays.equals(READ_File.ClassSubClass,baos.toByteArray()),"checking written data");
  }

  public void testUnderscore() {
    setUpFile();

    SM_ClassUnderscore instance = new SM_ClassUnderscore();
    try {
      oos.writeObject(instance);
      oos.close();
    }
    catch (IOException exc) {
      Assert.problem (exc, "IO problem");
    }
    th.check( Arrays.equals(READ_File.SM_ClassUnderscore,baos.toByteArray()),"checking written data");
  }

  public void testSimpleOuterClass() {
    setUpFile();

    SAMPLE_SimpleOuterClass instance = new SAMPLE_SimpleOuterClass();
    try {
      oos.writeObject(instance);
      oos.close();
    }
    catch (IOException exc) {
      Assert.problem (exc, "IO problem");
    }
    th.check( Arrays.equals(READ_File.SAMPLE_SimpleOuterClass,baos.toByteArray()),"checking written data");
  }

  public void testBlockdatalong() {
    setUpFile();
    try {
      byte[] blockdatabytes = new byte[1022];
      oos.write(blockdatabytes);
      oos.writeInt(0x01020304);
      oos.close();
    }
    catch (IOException exc) {
      Assert.problem (exc, "IO problem");
    }
    th.check( Arrays.equals(READ_File.BLOCKDATALONG,baos.toByteArray()),"checking written data");
  }


  public void testStringBufferArray() {
    setUpFile();
    try {
      StringBuffer[] stringbufferArray = { new StringBuffer(), new StringBuffer() };
      oos.writeObject(stringbufferArray);
      oos.close();
    }
    catch (IOException exc) {
      Assert.problem (exc, "IO problem");
    }
    th.check( Arrays.equals(READ_File.StringBufferArray,baos.toByteArray()),"checking written data");
    printComparison(READ_File.StringBufferArray,baos.toByteArray());
  }

  public void runTest() {
   	th.setclass("java.io.ObjectOutputStream");
   	th.checkPoint("testStringBufferArray;");
   	testStringBufferArray();
   	th.checkPoint("testBlockdatalong();");
   	testBlockdatalong();
   	th.checkPoint("testBlockdata1();");
   	testBlockdata1();
   	th.checkPoint("testBlockdata2();");
   	testBlockdata2();
//   	th.checkPoint("testSimpleClass();");
//   	testSimpleClass();
   	th.checkPoint("testClassString();");
   	testClassString();
   	th.checkPoint("testClassArray();");
   	testClassArray();
   	th.checkPoint("testVector();");
   	testVector();
   	th.checkPoint("testStringBuffer();");
   	testStringBuffer();
   	th.checkPoint("testInheritance();");
   	testInheritance();
   	th.checkPoint("testUnderscore();");
   	testUnderscore();
   	th.checkPoint("testSimpleOuterClass();");
   	testSimpleOuterClass();
  }
}
