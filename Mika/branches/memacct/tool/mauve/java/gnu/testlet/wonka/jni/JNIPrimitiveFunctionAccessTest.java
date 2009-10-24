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

// Tags: JLS1.0

package gnu.testlet.wonka.jni;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

/***********************************************************************************************************************************/
/**********************************************************************************************************************************
for reasons of testing the callNonstatic<type>method functions, we vaave split this file into two parts.
THe main part of the tests are defined in the base class JNIObjectAccessBase. It's functions are called from here.
JNIAccessBase only features the test(harness)-function, calling the base-classes test functions and the <original> versions of the
getMax<type>calls.for the testNonvirtualPrimitives function.
/***********************************************************************************************************************************/
 ;

public class JNIPrimitiveFunctionAccessTest extends JNIObjectFunctionAccessTest implements Testlet
{

// load the native library//
  static
  {
    System.loadLibrary("JNIPrimitiveFunctionAccessTest");
  }

/***********************************************************************************************************************************/
/**
  Call<type>MethodA/V tests for primitive boolean:
 booleanArray calls (overwritten) JNIClassAccessTestPrimitives.countBinary() functions using CallBooleanMethodA and CallBooleanMethodV
 StaticBooleanArray calls (overwritten) JNIClassAccessTestPrimitives.countBinary() functions using CallBooleanMethodA and CallBooleanMethodV
 NonvirtualBooleanArray calls (overwritten) JNIClassAccessBasePrimitives.countBinary() functions
 using CallnonvirtualBooleanMethodA and CallNonvirtualBooleanMethodV
*/
protected void testAVByteFunctions()
{
	// static variable used as first element in class array calls
		varB= (byte)0x40;

  harness.checkPoint("CallByteMethod,");
		harness.verbose("entering byteArray, calls getMaximum(byte...)");
  byte[] barray = byteArray((byte)-0x30, (byte)-0x7d, (byte)0x70);
  harness.check (barray[0],  0x40);
  harness.check (barray[1], -0x7d);
  harness.check (barray[2], -0x7d);

  harness.checkPoint("CallStaticByteMethod");
  barray = staticByteArray((byte)-0x30, (byte)0x41, (byte)-0x7e, (byte)0x70);
  harness.check (barray[0],  0x41);
  harness.check (barray[1], -0x7e);
  harness.check (barray[2], -0x7e);

  harness.checkPoint("CallStaticByteMethod (2)");
  barray = staticByteArray((byte)-0x20, (byte)0x21, (byte)-0x7e, (byte)-0x25);
  harness.check (barray[0],  0x21);
  harness.check (barray[1], -0x7e);
  harness.check (barray[2], -0x7e);

  harness.checkPoint("CallNonvirtualByteMethod");
  barray = nonvirtualByteArray((byte)-0x30, (byte)-0x7f, (byte)0x70);
  harness.check (barray[0], -0x40);
  harness.check (barray[1],  0x7f);
  harness.check (barray[2],  0x7f);
}


native byte[]           byteArray(byte    x1, byte    x2, byte    x3);
native byte[]     staticByteArray(byte    x1, byte    x2, byte    x3, byte    x4);
native byte[] nonvirtualByteArray(byte    x1, byte    x2, byte    x3);

protected void testAVByteFunctionsA()
{
	// static variable used as first element in class array calls
		varB= (byte)0x40;

  harness.checkPoint("CallByteMethodA,");
		harness.verbose("entering byteArrayA, calls getMaximum(byte...)");
  byte[] barray = byteArrayA((byte)-0x30, (byte)-0x7d, (byte)0x70);
  harness.check (barray[0],  0x40);
  harness.check (barray[1], -0x7d);
  harness.check (barray[2], -0x7d);

  harness.checkPoint("CallStaticByteMethodA");
  barray = staticByteArrayA((byte)-0x30, (byte)0x41, (byte)-0x7e, (byte)0x70);
  harness.check (barray[0],  0x41);
  harness.check (barray[1], -0x7e);
  harness.check (barray[2], -0x7e);
  harness.checkPoint("CallStaticByteMethodA (2)");
  barray = staticByteArrayA((byte)-0x20, (byte)0x21, (byte)-0x7e, (byte)-0x25);
  harness.check (barray[0],  0x21);
  harness.check (barray[1], -0x7e);
  harness.check (barray[2], -0x7e);

  harness.checkPoint("CallNonvirtualByteMethodA");
  barray = nonvirtualByteArrayA((byte)-0x30, (byte)-0x7f, (byte)0x70);
  harness.check (barray[0], -0x40);
  harness.check (barray[1],  0x7f);
  harness.check (barray[2],  0x7f);
}


native byte[]           byteArrayA(byte    x1, byte    x2, byte    x3);
native byte[]     staticByteArrayA(byte    x1, byte    x2, byte    x3, byte    x4);
native byte[] nonvirtualByteArrayA(byte    x1, byte    x2, byte    x3);
protected void testAVByteFunctionsV()
{
	// static variable used as first element in class array calls
		varB= (byte)0x40;

  harness.checkPoint("CallByteMethodV");
  byte[] barray = byteArrayV((byte)-0x30, (byte)-0x7d, (byte)0x70);
  harness.check (barray[0],  0x40);
  harness.check (barray[1], -0x7d);
  harness.check (barray[2], -0x7d);

  harness.checkPoint("CallStaticByteMethodV");
  barray = staticByteArrayV((byte)-0x30, (byte)0x41, (byte)-0x7e, (byte)0x70);
  harness.check (barray[0],  0x41);
  harness.check (barray[1], -0x7e);
  harness.check (barray[2], -0x7e);
  harness.checkPoint("CallStaticByteMethodV (2)");
  barray = staticByteArrayV((byte)-0x20, (byte)0x21, (byte)-0x7e, (byte)-0x25);
  harness.check (barray[0],  0x21);
  harness.check (barray[1], -0x7e);
  harness.check (barray[2], -0x7e);

  harness.checkPoint("CallNonvirtualByteMethodV");
  barray = nonvirtualByteArrayV((byte)-0x30, (byte)-0x7f, (byte)0x70);
  harness.check (barray[0], -0x40);
  harness.check (barray[1],  0x7f);
  harness.check (barray[2],  0x7f);
}


native byte[]           byteArrayV(byte    x1, byte    x2, byte    x3);
native byte[]     staticByteArrayV(byte    x1, byte    x2, byte    x3, byte    x4);
native byte[] nonvirtualByteArrayV(byte    x1, byte    x2, byte    x3);



/***********************************************************************************************************************************/
/**
  Call<type>MethodA/V tests for primitive boolean:
 booleanArray calls (overwritten) JNIClassAccessTestPrimitives.countBinary() functions using CallBooleanMethodA and CallBooleanMethodV
 StaticBooleanArray calls (overwritten) JNIClassAccessTestPrimitives.countBinary() functions using CallBooleanMethodA and CallBooleanMethodV
 NonvirtualBooleanArray calls (overwritten) JNIClassAccessBasePrimitives.countBinary() functions
 using CallnonvirtualBooleanMethodA and CallNonvirtualBooleanMethodV
*/
protected void testAVCharFunctions()
{
	// static variable used as first element in class array calls
		varC= (char)0x8080;

  harness.checkPoint("sheck CallCharMethod");
  char[] carray = charArray((char)0x6060, (char)0xfffc, (char)0xfff0);
  harness.check (carray[0], 0x8080);
  harness.check (carray[1], 0xfffc);
  harness.check (carray[2], 0xfffc);

  harness.checkPoint("CallStaticCharMethod");
  carray = staticCharArray((char)0x6060, (char)0x8081, (char)0xfffc, (char)0xfff0);
  harness.check (carray[0], 0x8081);
  harness.check (carray[1], 0xfffc);
  harness.check (carray[2], 0xfffc);

  harness.checkPoint("CallNonvirtualCharMethod");
  carray = nonvirtualCharArray((char)0x6060, (char)0xfffc, (char)0xfff0);
  harness.check (carray[0], 0x7f7f);
  harness.check (carray[1], 0x0003);
  harness.check (carray[2], 0x0003);

}


native char[]           charArray(char   x1, char   x2, char   x3);
native char[]     staticCharArray(char   x1, char   x2, char   x3, char   x4);
native char[] nonvirtualCharArray(char   x1, char   x2, char   x3);

protected void testAVCharFunctionsA()
{
	// static variable used as first element in class array calls
		varC= (char)0x8082;

  harness.checkPoint("sheck CallCharMethodA");
  char[] carray = charArrayA((char)0x6060, (char)0xfffd, (char)0xfff0);
  harness.check (carray[0], 0x8082);
  harness.check (carray[1], 0xfffd);
  harness.check (carray[2], 0xfffd);

  harness.checkPoint("CallStaticCharMethodV");
  carray = staticCharArrayA((char)0x6060, (char)0x8083, (char)0xfffd, (char)0xfff0);
  harness.check (carray[0], 0x8083);
  harness.check (carray[1], 0xfffd);
  harness.check (carray[2], 0xfffd);

  harness.checkPoint("CallNonvirtualCharMethodV");
  carray = nonvirtualCharArrayA((char)0x6060, (char)0xfffd, (char)0xfff0);
  harness.check (carray[0], 0x7f7d);
  harness.check (carray[1], 0x0002);
  harness.check (carray[2], 0x0002);
}


native char[]           charArrayA(char   x1, char   x2, char   x3);
native char[]     staticCharArrayA(char   x1, char   x2, char   x3, char   x4);
native char[] nonvirtualCharArrayA(char   x1, char   x2, char   x3);

protected void testAVCharFunctionsV()
{
	// static variable used as first element in class array calls
		varC= (char)0x8084;

  harness.checkPoint("CallCharMethodV");
  char[] carray = charArrayA((char)0x6060, (char)0xfffe, (char)0xfff0);
  harness.check (carray[0], 0x8084);
  harness.check (carray[1], 0xfffe);
  harness.check (carray[2], 0xfffe);

  harness.checkPoint("CallStaticCharMethodV");
  carray = staticCharArrayV((char)0x6060, (char)0x8085, (char)0xfffe, (char)0xfff0);
  harness.check (carray[0], 0x8085);
  harness.check (carray[1], 0xfffe);
  harness.check (carray[2], 0xfffe);

  harness.checkPoint("CallNonvirtualCharMethodV");
  carray = nonvirtualCharArrayV((char)0x6060, (char)0xfffe, (char)0xfff0);
  harness.check (carray[0], 0x7f7b);
  harness.check (carray[1], 0x0001);
  harness.check (carray[2], 0x0001);
}


native char[]           charArrayV(char   x1, char   x2, char   x3);
native char[]     staticCharArrayV(char   x1, char   x2, char   x3, char   x4);
native char[] nonvirtualCharArrayV(char   x1, char   x2, char   x3);

/***********************************************************************************************************************************/
/**
  Call<type>MethodA/V tests for primitive boolean:
 booleanArray calls (overwritten) JNIClassAccessTestPrimitives.countBinary() functions using CallBooleanMethodA and CallBooleanMethodV
 StaticBooleanArray calls (overwritten) JNIClassAccessTestPrimitives.countBinary() functions using CallBooleanMethodA and CallBooleanMethodV
 NonvirtualBooleanArray calls (overwritten) JNIClassAccessBasePrimitives.countBinary() functions
 using CallnonvirtualBooleanMethodA and CallNonvirtualBooleanMethodV
*/
protected void testAVShortFunctions()
{
	// static variable used as first element in class array calls
		varS= (short)0x4040;

  harness.checkPoint("sheck CallShortMethod");
  short[] sarray = shortArrayA((short)-0x3030, (short)-0x707d, (short)0x7070);
  harness.check (sarray[0], (short) 0x4040);
  harness.check (sarray[1], (short)-0x707d);
  harness.check (sarray[2], (short)-0x707d);

  harness.checkPoint("CallStaticShortMethod");
  sarray = staticShortArrayA((short)-0x3030, (short)0x4041, (short)-0x707e, (short)0x7070);
  harness.check (sarray[0],  0x4041);
  harness.check (sarray[1], -0x707e);
  harness.check (sarray[2], -0x707e);

  harness.checkPoint("CallNonvirtualShortMethod");
  sarray = nonvirtualShortArrayA((short)-0x3030, (short)-0x707f, (short)0x7070);
  harness.check (sarray[0], -0x4040);
  harness.check (sarray[1],  0x707f);
  harness.check (sarray[2],  0x707f);

}


native short[]           shortArray(short   x1, short   x2, short   x3);
native short[]     staticShortArray(short   x1, short   x2, short   x3, short   x4);
native short[] nonvirtualShortArray(short   x1, short   x2, short   x3);

protected void testAVShortFunctionsA()
{
	// static variable used as first element in class array calls
		varS= (short)0x4040;

  harness.checkPoint("sheck CallShortMethodA");
  short[] sarray = shortArrayA((short)-0x3030, (short)-0x707d, (short)0x7070);
  harness.check (sarray[0], (short) 0x4040);
  harness.check (sarray[1], (short)-0x707d);
  harness.check (sarray[2], (short)-0x707d);

  harness.checkPoint("CallStaticShortMethodA");
  sarray = staticShortArrayA((short)-0x3030, (short)0x4041, (short)-0x707e, (short)0x7070);
  harness.check (sarray[0],  0x4041);
  harness.check (sarray[1], -0x707e);
  harness.check (sarray[2], -0x707e);

  harness.checkPoint("CallNonvirtualShortMethodA");
  sarray = nonvirtualShortArrayA((short)-0x3030, (short)-0x707f, (short)0x7070);
  harness.check (sarray[0], -0x4040);
  harness.check (sarray[1],  0x707f);
  harness.check (sarray[2],  0x707f);

}


native short[]           shortArrayA(short   x1, short   x2, short   x3);
native short[]     staticShortArrayA(short   x1, short   x2, short   x3, short   x4);
native short[] nonvirtualShortArrayA(short   x1, short   x2, short   x3);

protected void testAVShortFunctionsV()
{
	// static variable used as first element in class array calls
		varS= (short)0x4040;

  harness.checkPoint("CallShortMethodV");
  harness.verbose("CallShortMethodV");
  short[] sarray = shortArrayV((short)-0x3030, (short)-0x707d, (short)0x7070);
  harness.verbose("sarray.length = "+ sarray.length);
  harness.verbose("sarray[0] = "+ sarray[0]);
  harness.verbose("sarray[1] = "+ sarray[1]);
  harness.check (sarray[0],  0x4040);
  harness.check (sarray[1], -0x707d);
  harness.check (sarray[2], -0x707d);

  harness.checkPoint("CallStaticShortMethodV");
  sarray = staticShortArrayV((short)-0x3030, (short)0x4041, (short)-0x707e, (short)0x7070);
  harness.check (sarray[0],  0x4041);
  harness.check (sarray[1], -0x707e);
  harness.check (sarray[2], -0x707e);

  harness.checkPoint("CallNonvirtualShortMethodV");
  sarray = nonvirtualShortArrayV((short)-0x3030, (short)-0x707f, (short)0x7070);
  harness.check (sarray[0], -0x4040);
  harness.check (sarray[1],  0x707f);
  harness.check (sarray[2],  0x707f);

}


native short[]           shortArrayV(short   x1, short   x2, short   x3);
native short[]     staticShortArrayV(short   x1, short   x2, short   x3, short   x4);
native short[] nonvirtualShortArrayV(short   x1, short   x2, short   x3);

/***********************************************************************************************************************************/
/**
  Call<type>MethodA/V tests for primitive boolean:
 booleanArray calls (overwritten) JNIClassAccessTestPrimitives.countBinary() functions using CallBooleanMethodA and CallBooleanMethodV
 StaticBooleanArray calls (overwritten) JNIClassAccessTestPrimitives.countBinary() functions using CallBooleanMethodA and CallBooleanMethodV
 NonvirtualBooleanArray calls (overwritten) JNIClassAccessBasePrimitives.countBinary() functions
 using CallnonvirtualBooleanMethodA and CallNonvirtualBooleanMethodV
*/
protected void testAVIntFunctions()
{
	// static variable used as first element in class array calls
		varI= 0x40404040;

  harness.checkPoint("CallIntMethod");
  int[] iarray = intArrayA(-0x30303030, -0x7070707e, 0x70707070);
  harness.check (iarray[0],  0x40404040);
  harness.check (iarray[1], -0x7070707e);
  harness.check (iarray[2], -0x7070707e);

  harness.checkPoint("CallStaticIntMethod");
  iarray = staticIntArrayA(-0x30303030, 0x40404041, -0x7070707e, 0x70707070);
  harness.check (iarray[0],  0x40404041);
  harness.check (iarray[1], -0x7070707e);
  harness.check (iarray[2], -0x7070707e);

  harness.checkPoint("CallNonvirtualIntMethod");
  iarray = nonvirtualIntArrayA(-0x30303030, -0x7070707e, 0x70707070);
  harness.check (iarray[0], -0x40404040);
  harness.check (iarray[1],  0x7070707e);
  harness.check (iarray[2],  0x7070707e);
}


native int[]           intArray(int     x1, int     x2, int     x3);
native int[]     staticIntArray(int     x1, int     x2, int     x3, int     x4);
native int[] nonvirtualIntArray(int     x1, int     x2, int     x3);

protected void testAVIntFunctionsA()
{
	// static variable used as first element in class array calls
		varI= 0x40404040;

  harness.checkPoint("CallIntMethodA");
  int[] iarray = intArrayA(-0x30303030, -0x7070707e, 0x70707070);
  harness.check (iarray[0],  0x40404040);
  harness.check (iarray[1], -0x7070707e);
  harness.check (iarray[2], -0x7070707e);

  harness.checkPoint("CallStaticIntMethodA");
  iarray = staticIntArrayA(-0x30303030, 0x40404041, -0x7070707e, 0x70707070);
  harness.check (iarray[0],  0x40404041);
  harness.check (iarray[1], -0x7070707e);
  harness.check (iarray[2], -0x7070707e);

  harness.checkPoint("CallNonvirtualIntMethodA");
  iarray = nonvirtualIntArrayA(-0x30303030, -0x7070707e, 0x70707070);
  harness.check (iarray[0], -0x40404040);
  harness.check (iarray[1],  0x7070707e);
  harness.check (iarray[2],  0x7070707e);
}


native int[]           intArrayA(int     x1, int     x2, int     x3);
native int[]     staticIntArrayA(int     x1, int     x2, int     x3, int     x4);
native int[] nonvirtualIntArrayA(int     x1, int     x2, int     x3);

protected void testAVIntFunctionsV()
{
	// static variable used as first element in class array calls
		varI= 0x40404040;

  harness.checkPoint("CallIntMethodV");
  int[] iarray = intArrayV(-0x30303030, -0x7070707e, 0x70707070);
  harness.check (iarray[0],  0x40404040);
  harness.check (iarray[1], -0x7070707e);
  harness.check (iarray[2], -0x7070707e);

  harness.checkPoint("CallStaticIntMethodV");
  iarray = staticIntArrayV(-0x30303030, 0x40404041, -0x7070707e, 0x70707070);
  harness.check (iarray[0],  0x40404041);
  harness.check (iarray[1], -0x7070707e);
  harness.check (iarray[2], -0x7070707e);

  harness.checkPoint("CallNonvirtualIntMethodV");
  iarray = nonvirtualIntArrayV(-0x30303030, -0x7070707e, 0x70707070);
  harness.check (iarray[0], -0x40404040);
  harness.check (iarray[1],  0x7070707e);
  harness.check (iarray[2],  0x7070707e);
}


native int[]           intArrayV(int     x1, int     x2, int     x3);
native int[]     staticIntArrayV(int     x1, int     x2, int     x3, int     x4);
native int[] nonvirtualIntArrayV(int     x1, int     x2, int     x3);
/***********************************************************************************************************************************/
/**
  Call<type>MethodA/V tests for primitive boolean:
 booleanArray calls (overwritten) JNIClassAccessTestPrimitives.countBinary() functions using CallBooleanMethodA and CallBooleanMethodV
 StaticBooleanArray calls (overwritten) JNIClassAccessTestPrimitives.countBinary() functions using CallBooleanMethodA and CallBooleanMethodV
 NonvirtualBooleanArray calls (overwritten) JNIClassAccessBasePrimitives.countBinary() functions
 using CallnonvirtualBooleanMethodA and CallNonvirtualBooleanMethodV
*/
protected void testAVLongFunctions()
{
	// static variable used as first element in class array calls
		varJ= 0x4040404040404040L;

  harness.checkPoint("CallLongMethod");
  long[] larray = longArrayA( -0x3030303030303030L, -0x707070707070707AL, 0x7070707070707070L);
  harness.check (larray[0],  0x4040404040404040L);
  harness.check (larray[1], -0x707070707070707AL);
  harness.check (larray[2], -0x707070707070707AL);

  harness.checkPoint("CallStaticLongMethod");
  larray = staticLongArrayA( -0x3030303030303030L, 0x4040404040404041L, -0x707070707070707AL, 0x7070707070707070L);
  harness.check (larray[0],  0x4040404040404041L);
  harness.check (larray[1], -0x707070707070707AL);
  harness.check (larray[2], -0x707070707070707AL);

  harness.checkPoint("CallNonvirtualLongMethod");
  larray = nonvirtualLongArrayA( -0x3030303030303030L, -0x707070707070707AL, 0x7070707070707070L);
  harness.check (larray[0], -0x4040404040404040L);
  harness.check (larray[1],  0x707070707070707AL);
  harness.check (larray[2],  0x707070707070707AL);
}


native long[]           longArray(long    x1, long    x2, long    x3);
native long[]     staticLongArray(long    x1, long    x2, long    x3, long    x4);
native long[] nonvirtualLongArray(long    x1, long    x2, long    x3);

protected void testAVLongFunctionsA()
{
	// static variable used as first element in class array calls
		varJ= 0x4040404040404040L;

  harness.checkPoint("CallLongMethodA");
  long[] larray = longArrayA( -0x3030303030303030L, -0x707070707070707AL, 0x7070707070707070L);
  harness.check (larray[0],  0x4040404040404040L);
  harness.check (larray[1], -0x707070707070707AL);
  harness.check (larray[2], -0x707070707070707AL);

  harness.checkPoint("CallStaticLongMethodA");
  larray = staticLongArrayA( -0x3030303030303030L, 0x4040404040404041L, -0x707070707070707AL, 0x7070707070707070L);
  harness.check (larray[0],  0x4040404040404041L);
  harness.check (larray[1], -0x707070707070707AL);
  harness.check (larray[2], -0x707070707070707AL);

  harness.checkPoint("CallNonvirtualLongMethodA");
  larray = nonvirtualLongArrayA( -0x3030303030303030L, -0x707070707070707AL, 0x7070707070707070L);
  harness.check (larray[0], -0x4040404040404040L);
  harness.check (larray[1],  0x707070707070707AL);
  harness.check (larray[2],  0x707070707070707AL);
}


native long[]           longArrayA(long    x1, long    x2, long    x3);
native long[]     staticLongArrayA(long    x1, long    x2, long    x3, long    x4);
native long[] nonvirtualLongArrayA(long    x1, long    x2, long    x3);

protected void testAVLongFunctionsV()
{
	// static variable used as first element in class array calls
		varJ= 0x4040404040404040L;

  harness.checkPoint("CallLongMethodV");
  long[] larray = longArrayV( -0x3030303030303030L, -0x707070707070707AL, 0x7070707070707070L);
  harness.check (larray[0],  0x4040404040404040L);
  harness.check (larray[1], -0x707070707070707AL);
  harness.check (larray[2], -0x707070707070707AL);

  harness.checkPoint("CallStaticLongMethodV");
  larray = staticLongArrayV( -0x3030303030303030L, 0x4040404040404041L, -0x707070707070707AL, 0x7070707070707070L);
  harness.check (larray[0],  0x4040404040404041L);
  harness.check (larray[1], -0x707070707070707AL);
  harness.check (larray[2], -0x707070707070707AL);

  harness.checkPoint("CallNonvirtualLongMethodV");
  larray = nonvirtualLongArrayV( -0x3030303030303030L, -0x707070707070707AL, 0x7070707070707070L);
  harness.check (larray[0], -0x4040404040404040L);
  harness.check (larray[1],  0x707070707070707AL);
  harness.check (larray[2],  0x707070707070707AL);
}


native long[]           longArrayV(long    x1, long    x2, long    x3);
native long[]     staticLongArrayV(long    x1, long    x2, long    x3, long    x4);
native long[] nonvirtualLongArrayV(long    x1, long    x2, long    x3);
/***********************************************************************************************************************************/
/**
  Call<type>MethodA/V tests for primitive boolean:
 booleanArray calls (overwritten) JNIClassAccessTestPrimitives.countBinary() functions using CallBooleanMethodA and CallBooleanMethodV
 StaticBooleanArray calls (overwritten) JNIClassAccessTestPrimitives.countBinary() functions using CallBooleanMethodA and CallBooleanMethodV
 NonvirtualBooleanArray calls (overwritten) JNIClassAccessBasePrimitives.countBinary() functions
 using CallnonvirtualBooleanMethodA and CallNonvirtualBooleanMethodV
*/
protected void testAVFloatFunctions()
{
	// static variable used as first element in class array calls
		varF= 0.75f;

  harness.checkPoint("CallFloatMethod");
  float[] farray = floatArrayA(-.5f, -10.2f, 10.0f);
  harness.check (farray[0],  0.75f);
  harness.check (farray[1], -10.2f);
  harness.check (farray[2], -10.2f);

  harness.checkPoint("CallStaticFloatMethod");
  farray = staticFloatArrayA(-.5f, 0.77f, -10.2f, 10.0f);
  harness.check (farray[0],  0.77f);
  harness.check (farray[1], -10.2f);
  harness.check (farray[2], -10.2f);

  harness.checkPoint("CallNonvirtualFloatMethod");
  farray = nonvirtualFloatArrayA(-.5f, -10.2f, 10.0f);
  harness.check (farray[0], -0.75f);
  harness.check (farray[1],  10.2f);
  harness.check (farray[2],  10.2f);
}


native float[]           floatArray(float   x1, float   x2, float   x3);
native float[]     staticFloatArray(float   x1, float   x2, float   x3, float   x4);
native float[] nonvirtualFloatArray(float   x1, float   x2, float   x3);

protected void testAVFloatFunctionsA()
{
	// static variable used as first element in class array calls
		varF= 0.75f;

  harness.checkPoint("CallFloatMethodA");
  float[] farray = floatArrayA(-.5f, -10.2f, 10.0f);
  harness.check (farray[0],  0.75f);
  harness.check (farray[1], -10.2f);
  harness.check (farray[2], -10.2f);

  harness.checkPoint("CallStaticFloatMethodA");
  farray = staticFloatArrayA(-.5f, 0.77f, -10.2f, 10.0f);
  harness.check (farray[0],  0.77f);
  harness.check (farray[1], -10.2f);
  harness.check (farray[2], -10.2f);

  harness.checkPoint("CallNonvirtualFloatMethodA");
  farray = nonvirtualFloatArrayA(-.5f, -10.2f, 10.0f);
  harness.check (farray[0], -0.75f);
  harness.check (farray[1],  10.2f);
  harness.check (farray[2],  10.2f);
}


native float[]           floatArrayA(float   x1, float   x2, float   x3);
native float[]     staticFloatArrayA(float   x1, float   x2, float   x3, float   x4);
native float[] nonvirtualFloatArrayA(float   x1, float   x2, float   x3);

protected void testAVFloatFunctionsV()
{
	// static variable used as first element in class array calls
		varF= 0.75f;

  harness.checkPoint("CallFloatMethodV");
  float[] farray = floatArrayV(-.5f, -10.2f, 10.0f);
  harness.check (farray[0],  0.75f);
  harness.check (farray[1], -10.2f);
  harness.check (farray[2], -10.2f);

  harness.checkPoint("CallStaticFloatMethodV");
  farray = staticFloatArrayV(-.5f, 0.77f, -10.2f, 10.0f);
  harness.check (farray[0],  0.77f);
  harness.check (farray[1], -10.2f);
  harness.check (farray[2], -10.2f);

  harness.checkPoint("CallNonvirtualFloatMethodV");
  farray = nonvirtualFloatArrayV(-.5f, -10.2f, 10.0f);
  harness.check (farray[0], -0.75f);
  harness.check (farray[1],  10.2f);
  harness.check (farray[2],  10.2f);
}


native float[]           floatArrayV(float   x1, float   x2, float   x3);
native float[]     staticFloatArrayV(float   x1, float   x2, float   x3, float   x4);
native float[] nonvirtualFloatArrayV(float   x1, float   x2, float   x3);
/***********************************************************************************************************************************/
/**
  Call<type>MethodA/V tests for primitive boolean:
 booleanArray calls (overwritten) JNIClassAccessTestPrimitives.countBinary() functions using CallBooleanMethodA and CallBooleanMethodV
 StaticBooleanArray calls (overwritten) JNIClassAccessTestPrimitives.countBinary() functions using CallBooleanMethodA and CallBooleanMethodV
 NonvirtualBooleanArray calls (overwritten) JNIClassAccessBasePrimitives.countBinary() functions
 using CallnonvirtualBooleanMethodA and CallNonvirtualBooleanMethodV
*/
protected void testAVDoubleFunctions()
{
	// static variable used as first element in class array calls
		varD= 0.075;

  harness.checkPoint("CallDoubleMethod");
  double[] darray = doubleArrayA(-.05, -102.2, 100.0);
  harness.check (darray[0],  0.075);
  harness.check (darray[1], -102.2);
  harness.check (darray[2], -102.2);

  harness.checkPoint("CallStaticDoubleMethod");
  darray = staticDoubleArrayA(-.05, 0.077, -102.2, 100.0);
  harness.check (darray[0],  0.077);
  harness.check (darray[1], -102.2);
  harness.check (darray[2], -102.2);

  harness.checkPoint("CallNonvirtualDoubleMethod");
  darray = nonvirtualDoubleArrayA(-.05, -102.2, 100.0);
  harness.check (darray[0], -0.075);
  harness.check (darray[1],  102.2);
  harness.check (darray[2],  102.2);
}

native double[]           doubleArray(double  x1, double  x2, double  x3);
native double[]     staticDoubleArray(double  x1, double  x2, double  x3, double  x4);
native double[] nonvirtualDoubleArray(double  x1, double  x2, double  x3);

protected void testAVDoubleFunctionsA()
{
	// static variable used as first element in class array calls
		varD= 0.075;

  harness.checkPoint("CallDoubleMethodA");
  double[] darray = doubleArrayA(-.05, -102.2, 100.0);
  harness.check (darray[0],  0.075);
  harness.check (darray[1], -102.2);
  harness.check (darray[2], -102.2);

  harness.checkPoint("CallStaticDoubleMethodA");
  darray = staticDoubleArrayA(-.05, 0.077, -102.2, 100.0);
  harness.check (darray[0],  0.077);
  harness.check (darray[1], -102.2);
  harness.check (darray[2], -102.2);

  harness.checkPoint("CallNonvirtualDoubleMethodA");
  darray = nonvirtualDoubleArrayA(-.05, -102.2, 100.0);
  harness.check (darray[0], -0.075);
  harness.check (darray[1],  102.2);
  harness.check (darray[2],  102.2);
}

native double[]           doubleArrayA(double  x1, double  x2, double  x3);
native double[]     staticDoubleArrayA(double  x1, double  x2, double  x3, double  x4);
native double[] nonvirtualDoubleArrayA(double  x1, double  x2, double  x3);


protected void testAVDoubleFunctionsV()
{
	// static variable used as first element in class array calls
		varD= 0.075;

  harness.checkPoint("CallDoubleMethodV");
  double[] darray = doubleArrayV(-.05, -102.2, 100.0);
  harness.check (darray[0],  0.075);
  harness.check (darray[1], -102.2);
  harness.check (darray[2], -102.2);

  harness.checkPoint("CallStaticDoubleMethodV");
  darray = staticDoubleArrayV(-.05, 0.077, -102.2, 100.0);
  harness.check (darray[0],  0.077);
  harness.check (darray[1], -102.2);
  harness.check (darray[2], -102.2);

  harness.checkPoint("CallNonvirtualDoubleMethodV");
  darray = nonvirtualDoubleArrayV(-.05, -102.2, 100.0);
  harness.check (darray[0], -0.075);
  harness.check (darray[1],  102.2);
  harness.check (darray[2],  102.2);

}

native double[]           doubleArrayV(double  x1, double  x2, double  x3);
native double[]     staticDoubleArrayV(double  x1, double  x2, double  x3, double  x4);
native double[] nonvirtualDoubleArrayV(double  x1, double  x2, double  x3);
/***********************************************************************************************************************************/
/**
  Call<type>MethodA/V tests for primitive boolean:
 booleanArray calls (overwritten) JNIClassAccessTestPrimitives.countBinary() functions using CallBooleanMethodA and CallBooleanMethodV
 StaticBooleanArray calls (overwritten) JNIClassAccessTestPrimitives.countBinary() functions using CallBooleanMethodA and CallBooleanMethodV
 NonvirtualBooleanArray calls (overwritten) JNIClassAccessBasePrimitives.countBinary() functions
 using CallnonvirtualBooleanMethodA and CallNonvirtualBooleanMethodV
*/
protected void testAVBooleanFunctions()
{
	// static variable used as first element in class array calls
	varZ= false;

  harness.checkPoint("CallBooleanMethod");
  boolean[] zarray = booleanArray(true,false,true);
  harness.check (zarray[0]); // == true);
  harness.check (zarray[1]); // == true);
  harness.check (!zarray[2]); //  == false);

  harness.checkPoint("CallStaticBooleanMethod");
  zarray = staticBooleanArray(true,false,false,true);
  harness.check (zarray[0]); // == true);
  harness.check (zarray[1]); // == true);
  harness.check (!zarray[2]); //  == false);

  harness.checkPoint("CallNonvirtualBooleanMethod");
  zarray = nonvirtualBooleanArray(true,false,true);
  harness.check (!zarray[0]); // == !true);
  harness.check (!zarray[1]); // == !true);
  harness.check ( zarray[2]); //  == true);
}

native boolean[]           booleanArray(boolean x1, boolean x2, boolean x3);
native boolean[]     staticBooleanArray(boolean x1, boolean x2, boolean x3, boolean x4);
native boolean[] nonvirtualBooleanArray(boolean x1, boolean x2, boolean x3);

protected void testAVBooleanFunctionsA()
{
	// static variable used as first element in class array calls
	varZ= false;

  harness.checkPoint("CallBooleanMethodA");
  boolean[] zarray = booleanArrayA(true,false,true);
  harness.check (zarray[0]); // == true);
  harness.check (zarray[1]); // == true);
  harness.check (!zarray[2]); //  == false);

  harness.checkPoint("CallStaticBooleanMethodA");
  zarray = staticBooleanArrayA(true,false,false,true);
  harness.check (zarray[0]); // == true);
  harness.check (zarray[1]); // == true);
  harness.check (!zarray[2]); //  == false);

  harness.checkPoint("CallNonvirtualBooleanMethodA");
  zarray = nonvirtualBooleanArrayA(true,false,true);
  harness.check (!zarray[0]); // == !true);
  harness.check (!zarray[1]); // == !true);
  harness.check ( zarray[2]); //  == true);
}

native boolean[]           booleanArrayA(boolean x1, boolean x2, boolean x3);
native boolean[]     staticBooleanArrayA(boolean x1, boolean x2, boolean x3, boolean x4);
native boolean[] nonvirtualBooleanArrayA(boolean x1, boolean x2, boolean x3);

protected void testAVBooleanFunctionsV()
{
	// static variable used as first element in class array calls
	varZ= false;

  harness.checkPoint("CallBooleanMethodV");
  boolean[] zarray = booleanArrayV(true,false,true);
  harness.check (zarray[0]); // == true);
  harness.check (zarray[1]); // == true);
  harness.check (!zarray[2]); //  == false);

  harness.checkPoint("CallStaticBooleanMethodV");
  zarray = staticBooleanArrayV(true,false,false,true);
  harness.check (zarray[0]); // == true);
  harness.check (zarray[1]); // == true);
  harness.check (!zarray[2]); //  == false);

  harness.checkPoint("CallNonvirtualBooleanMethodV");
  zarray = nonvirtualBooleanArrayV(true,false,true);
  harness.check (!zarray[0]); // == !true);
  harness.check (!zarray[1]); // == !true);
  harness.check ( zarray[2]); //  == true);
}

native boolean[]           booleanArrayV(boolean x1, boolean x2, boolean x3);
native boolean[]     staticBooleanArrayV(boolean x1, boolean x2, boolean x3, boolean x4);
native boolean[] nonvirtualBooleanArrayV(boolean x1, boolean x2, boolean x3);
/****************************************************************************************
* public functions (derived class):
* will be called automatically insetad of the base class' functions. To access the original functions, use the <Novirtual> option
*/
  public byte   getMaximum(byte   x1) {return absmax(varB,x1);}
  public char   getMaximum(char   x1) {return absmax(varC,x1);}
  public short  getMaximum(short  x1) {return absmax(varS,x1);}
  public int    getMaximum(int    x1) {return absmax(varI,x1);}
  public long   getMaximum(long   x1) {return absmax(varJ,x1);}
  public float  getMaximum(float  x1) {return absmax(varF,x1);}
  public double getMaximum(double x1) {return absmax(varD,x1);}

  public byte   getMaximum(byte   x1, byte   x2) {return absmax(varB,x1,x2);}
  public char   getMaximum(char   x1, char   x2) {return absmax(varC,x1,x2);}
  public short  getMaximum(short  x1, short  x2) {return absmax(varS,x1,x2);}
  public int    getMaximum(int    x1, int    x2) {return absmax(varI,x1,x2);}
  public long   getMaximum(long   x1, long   x2) {return absmax(varJ,x1,x2);}
  public float  getMaximum(float  x1, float  x2) {return absmax(varF,x1,x2);}
  public double getMaximum(double x1, double x2) {return absmax(varD,x1,x2);}

  public byte   getMaximum(byte   x1, byte   x2, byte   x3) {return absmax(varB,x1,x2,x3);}
  public char   getMaximum(char   x1, char   x2, char   x3) {return absmax(varC,x1,x2,x3);}
  public short  getMaximum(short  x1, short  x2, short  x3) {return absmax(varS,x1,x2,x3);}
  public int    getMaximum(int    x1, int    x2, int    x3) {return absmax(varI,x1,x2,x3);}
  public long   getMaximum(long   x1, long   x2, long   x3) {return absmax(varJ,x1,x2,x3);}
  public float  getMaximum(float  x1, float  x2, float  x3) {return absmax(varF,x1,x2,x3);}
  public double getMaximum(double x1, double x2, double x3) {return absmax(varD,x1,x2,x3);}

  public boolean countBinary(boolean x1) {return binary(varZ,x1); }
  public boolean countBinary(boolean x1,boolean x2) {return binary(varZ,x1,x2); }
  public boolean countBinary(boolean x1,boolean x2,boolean x3) {return binary(varZ,x1,x2,x3); }

/*********************************************************************************************************************
* TestRunner interface : perform the tests described and pass the results to the testharness
*/
  public void test (TestHarness newharness)
	{
		harness = newharness;
		harness.setclass("JNI class and array construction/ class dependencies");
		harness.verbose("byte function access normal");
		testAVByteFunctions();
		harness.verbose("byte function access -a");
		testAVByteFunctionsA();
		harness.verbose("byte function access -v");
		testAVByteFunctionsV();

		harness.verbose("char  function access normal");
		testAVCharFunctions();
		harness.verbose("char  function access -a");
		testAVCharFunctionsA();
	  harness.verbose("char  function access -v");
		testAVCharFunctionsV();
		
		harness.verbose("short  function access normal");
		testAVShortFunctions();
		harness.verbose("short  function access -a");
		testAVShortFunctionsA();
	  harness.verbose("short  function access -v");
		testAVShortFunctionsV();
		
		harness.verbose("int  function access normal");
		testAVIntFunctions();
		harness.verbose("int  function access -a");
		testAVIntFunctionsA();
		harness.verbose("int  function access -v");
		testAVIntFunctionsV();
		
		harness.verbose("long  function access normal");
		testAVLongFunctions();
		harness.verbose("long  function access -a");
		testAVLongFunctionsA();
		harness.verbose("long  function access -v");
		testAVLongFunctionsV();
		
		harness.verbose("float  function access normal");
		testAVFloatFunctions();
		harness.verbose("float  function access -a");
		testAVFloatFunctionsA();
		harness.verbose("float  function access -v");
		testAVFloatFunctionsV();
		
		harness.verbose("double  function access normal");
		testAVDoubleFunctions();
		harness.verbose("double  function access -a");
		testAVDoubleFunctionsA();
		harness.verbose("double  function access -v");
		testAVDoubleFunctionsV();
		
		harness.verbose("Boolean function access normal");
		testAVBooleanFunctions();
		harness.verbose("Boolean function access -a");
		testAVBooleanFunctionsA();
		harness.verbose("Boolean function access -v");
		testAVBooleanFunctionsV();

	}

}
