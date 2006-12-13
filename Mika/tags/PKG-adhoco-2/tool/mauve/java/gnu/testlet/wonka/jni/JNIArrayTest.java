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

/**********************************************************************************************************************************
for reasons of testing the callNonstatic<type>method functions, this class has become a base class for the JNIObjectAccessTest.
This means that it features allmost all of the functions, but the loading of the library and the actual calling of the functions in the
test(harness) calls is done by the derived class.
Also the getMax<type>-functions are returning maximum positives instead of absolute maximums. THey are designed to be overwritten
/***********************************************************************************************************************************/
public class JNIArrayTest extends ContainerFunctions implements Testlet
{

// load the native library//
  static
  {
    System.loadLibrary("JNIArrayTest");
  }

// variable:
TestHarness harness;
/*********************************************************************************************************************/
/**
 <type> byte array functions for primitive byte
*/

private void testByteArrayFunctions()
{
  byte[] refarray = new byte[6];
  byte[] buildarray = new byte[6];
  //array building

  harness.checkPoint("NewByteArray()");
  byte current = (byte)0x40;
  for(int i=0; i<6; i++)
  {
    refarray[i]=current;
    current = (byte)(current+ 0x10);
  }
  buildarray = buildArray(6,(byte)0x40,(byte)0x10);
  compare(buildarray, refarray);

  // array length
  harness.checkPoint("GetArrayLength");
  harness.check(getArrayLength(refarray),6);


  // scan contents of array
  harness.checkPoint("Accessing elements of byte array");
  harness.check(scanArrayElements(refarray,(byte)0),4,"GetByteArrayElements/ReleaseByteArrayElements");
  harness.check(scanArrayRegionComplete(refarray,(byte)0),4,"GetByteArrayRegion: complete region");
  harness.check(scanArrayRegionOneByOne(refarray,(byte)0),4,"GetByteArrayRegion: one by one");

  for(int i=0; i<3; i++)
  {
    refarray[i]     = (byte)(0x10 * i);
    refarray[i+3]   = (byte)(0x10 * i);
    buildarray[i]   = (byte)(0x10 * i);
    buildarray[i+3] = (byte)(0x10 * i);
  }
  harness.checkPoint("changing elements of byte array");
  //replace values 0 by values 0x40
  refarray[0]=(byte)0x40;
  refarray[3]=(byte)0x40;
  changeArrayElements(buildarray, (byte)0, (byte)0x40);
  compare(buildarray, refarray,"GetByteArrayElements/ReleaseByteArrayElements");


  //replace values 0 by values 0x40
  refarray[2]=(byte)0x60;
  refarray[5]=(byte)0x60;
  changeArrayRegionComplete(buildarray, (byte)0x20, (byte)0x60);
  compare(buildarray, refarray,"GetByteArrayRegion: complete region");

  //replace values 0 by values 0x40
  refarray[0]=(byte)0x70;
  refarray[3]=(byte)0x70;
  changeArrayRegionOneByOne(buildarray, (byte)0x40, (byte)0x70);
  compare(buildarray, refarray,"GetByteArrayRegion: one by one");

}

private void testByteArrayCritical()
{
  byte[] refarray   =  buildArray(6,(byte)0x40,(byte)0x10);
  byte[] buildarray =  buildArray(6,(byte)0x40,(byte)0x10);

  harness.checkPoint("Accessing elements of byte array, critical");
  harness.check(scanArrayCritical(refarray,(byte)0),4,"GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical");

  harness.checkPoint("changing elements of byte array, critical");
  //replace values 0x50 by values 0x10
  refarray[1]=(byte)0x10;
  changeArrayCritical(buildarray, (byte)0x50, (byte)0x10);
  compare(buildarray, refarray,"GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical");
}
/** natives: */
/// build using GetByteArrayElements/ReleaseByteArrayElements
native private byte[] buildArray(int len, byte start, byte increment);
/// get array length
native private int getArrayLength(byte[] array);

/// scan using GetByteArrayElements/ReleaseByteArrayElements
native private int scanArrayElements(byte[] array, byte max);
/// scan using GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical
native private int scanArrayCritical(byte[] array, byte max);
/// scan using GetByteArrayRegion covering the complete region
native private int scanArrayRegionComplete(byte[] array, byte max);
/// scan using GetByteArrayRegion one byte at a time
native private int scanArrayRegionOneByOne(byte[] array, byte max);

/// change content using GetByteArrayElements/ReleaseByteArrayElements
native private void changeArrayElements(byte[]array, byte toreplace, byte newvalue);
/// change contents using GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical
native private void changeArrayCritical(byte[]array, byte toreplace, byte newvalue);
/// change contents using ReleaseByteArrayRegion one buffer for the complete region
native private void changeArrayRegionComplete(byte[]array, byte toreplace, byte newvalue);
/// change contents using ReleaseByteArrayRegion one element at a time
native private void changeArrayRegionOneByOne(byte[]array, byte toreplace, byte newvalue);

/*********************************************************************************************************************/
/**
 <type> array functions for primitive short
*/

private void testShortArrayFunctions()
{
  short[] refarray = new short[6];
  short[] buildarray = new short[6];
  //array building

  harness.checkPoint("NewShortArray()");
  short current = (short)0x4040;
  for(int i=0; i<6; i++)
  {
    refarray[i]=current;
    current = (short)(current+ 0x1010);
  }
  buildarray = buildArray(6,(short)0x4040,(short)0x1010);
  compare(buildarray, refarray);

  // array length
  harness.checkPoint("GetArrayLength");
  harness.check(getArrayLength(refarray),6);

  // scan contents of array
  harness.checkPoint("Accessing elements of short array");
  harness.check(scanArrayElements(refarray,(short)0),4,"GetShortArrayElements/ReleaseShortArrayElements");
  harness.check(scanArrayRegionComplete(refarray,(short)0),4,"GetShortArrayRegion: complete region");
  harness.check(scanArrayRegionOneByOne(refarray,(short)0),4,"GetShortArrayRegion: one by one");

  for(int i=0; i<3; i++)
  {
    refarray[i]     = (short)(0x1010 * i);
    refarray[i+3]   = (short)(0x1010 * i);
    buildarray[i]   = (short)(0x1010 * i);
    buildarray[i+3] = (short)(0x1010 * i);
  }
  harness.checkPoint("changing elements of short array");
  //replace values 0 by values 0x40
  refarray[0]=(short)0x4040;
  refarray[3]=(short)0x4040;
  changeArrayElements(buildarray, (short)0, (short)0x4040);
  compare(buildarray, refarray,"GetShortArrayElements/ReleaseShortArrayElements");

  //replace values 0 by values 0x40
  refarray[2]=(short)0x6060;
  refarray[5]=(short)0x6060;
  changeArrayRegionComplete(buildarray, (short)0x2020, (short)0x6060);
  compare(buildarray, refarray,"GetShortArrayRegion: complete region");

  //replace values 0 by values 0x40
  refarray[0]=(short)0x7070;
  refarray[3]=(short)0x7070;
  changeArrayRegionOneByOne(buildarray, (short)0x4040, (short)0x7070);
  compare(buildarray, refarray,"GetShortArrayRegion: one by one");
}

private void testShortArrayCritical()
{
  short[] refarray   =  buildArray(6,(short)0x4040,(short)0x1010);
  short[] buildarray =  buildArray(6,(short)0x4040,(short)0x1010);

  harness.checkPoint("Accessing elements of array, critical");
  harness.check(scanArrayCritical(refarray,(short)0),4,"GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical");

  harness.checkPoint("changing elements of array, critical");
  //replace values 0x50 by values 0x10
  refarray[1]=(short)0x1010;
  changeArrayCritical(buildarray, (short)0x5050, (short)0x1010);
  compare(buildarray, refarray,"GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical");
}

/** natives: */
/// build using GetShortArrayElements/ReleaseShortArrayElements
native private short[] buildArray(int len, short start, short increment);
/// get array length
native private int getArrayLength(short[] array);

/// scan using GetShortArrayElements/ReleaseShortArrayElements
native private int scanArrayElements(short[] array, short max);
/// scan using GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical
native private int scanArrayCritical(short[] array, short max);
/// scan using GetShortArrayRegion covering the complete region
native private int scanArrayRegionComplete(short[] array, short max);
/// scan using GetShortArrayRegion one short at a time
native private int scanArrayRegionOneByOne(short[] array, short max);

/// change content using GetShortArrayElements/ReleaseShortArrayElements
native private void changeArrayElements(short[]array, short toreplace, short newvalue);
/// change contents using GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical
native private void changeArrayCritical(short[]array, short toreplace, short newvalue);
/// change contents using ReleaseShortArrayRegion one buffer for the complete region
native private void changeArrayRegionComplete(short[]array, short toreplace, short newvalue);
/// change contents using ReleaseShortArrayRegion one element at a time
native private void changeArrayRegionOneByOne(short[]array, short toreplace, short newvalue);


/*********************************************************************************************************************/
/**
 <type> array functions for primitive int
*/

private void testIntArrayFunctions()
{
  int[] refarray = new int[6];
  int[] buildarray = new int[6];
  //array building

  harness.checkPoint("NewIntArray()");
  int current = 0x40404040;
  for(int i=0; i<6; i++)
  {
    refarray[i]=current;
    current = (current+ 0x10101010);
  }
  buildarray = buildArray(6,0x40404040,0x10101010);
  compare(buildarray, refarray);

  // array length
  harness.checkPoint("GetArrayLength integer array");
  harness.check(getArrayLength(refarray),6);

  // scan contents of array
  harness.checkPoint("Accessing elements of integer array");
  harness.check(scanArrayElements(refarray,0),4,"GetIntArrayElements/ReleaseIntArrayElements");
  harness.check(scanArrayRegionComplete(refarray,0),4,"GetIntArrayRegion: complete region");
  harness.check(scanArrayRegionOneByOne(refarray,0),4,"GetIntArrayRegion: one by one");

  for(int i=0; i<3; i++)
  {
    refarray[i]     = 0x10101010 * i;
    refarray[i+3]   = 0x10101010 * i;
    buildarray[i]   = 0x10101010 * i;
    buildarray[i+3] = 0x10101010 * i;
  }
  harness.checkPoint("changing elements of integer array");
  //replace values 0 by values 0x40
  refarray[0]=0x40404040;
  refarray[3]=0x40404040;
  changeArrayElements(buildarray, 0, 0x40404040);
  compare(buildarray, refarray,"GetIntArrayElements/ReleaseIntArrayElements");

  //replace values 0 by values 0x40
  refarray[2]=0x60606060;
  refarray[5]=0x60606060;
  changeArrayRegionComplete(buildarray, 0x20202020, 0x60606060);
  compare(buildarray, refarray,"GetIntArrayRegion: complete region");

  //replace values 0 by values 0x40
  refarray[0]=0x70707070;
  refarray[3]=0x70707070;
  changeArrayRegionOneByOne(buildarray, 0x40404040, 0x70707070);
  compare(buildarray, refarray,"GetIntArrayRegion: one by one");
}

private void testIntArrayCritical()
{
  int[] refarray   =  buildArray(6,0x40404040,0x10101010);
  int[] buildarray =  buildArray(6,0x40404040,0x10101010);

  harness.checkPoint("Accessing elements of array, critical");
  harness.verbose("Accessing elements of array, critical");
  harness.check(scanArrayCritical(refarray,0),4,"GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical");

  harness.checkPoint("changing elements of array, critical");
  harness.verbose("changing elements of array, critical");
  //replace values 0x50 by values 0x10
  refarray[1]=0x10101010;
  changeArrayCritical(buildarray, 0x50505050, 0x10101010);
  compare(buildarray, refarray,"GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical");
}

/** natives: */
/// build using GetIntArrayElements/ReleaseIntArrayElements
native private int[] buildArray(int len, int start, int increment);
/// get array length
native private int getArrayLength(int[] array);

/// scan using GetIntArrayElements/ReleaseIntArrayElements
native private int scanArrayElements(int[] array, int max);
/// scan using GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical
native private int scanArrayCritical(int[] array, int max);
/// scan using GetIntArrayRegion covering the complete region
native private int scanArrayRegionComplete(int[] array, int max);
/// scan using GetIntArrayRegion one int at a time
native private int scanArrayRegionOneByOne(int[] array, int max);

/// change content using GetIntArrayElements/ReleaseIntArrayElements
native private void changeArrayElements(int[]array, int toreplace, int newvalue);
/// change contents using GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical
native private void changeArrayCritical(int[]array, int toreplace, int newvalue);
/// change contents using ReleaseIntArrayRegion one buffer for the complete region
native private void changeArrayRegionComplete(int[]array, int toreplace, int newvalue);
/// change contents using ReleaseIntArrayRegion one element at a time
native private void changeArrayRegionOneByOne(int[]array, int toreplace, int newvalue);

/*********************************************************************************************************************/
/**
 <type> array functions for primitive long
*/

private void testLongArrayFunctions()
{
  long[] refarray = new long[6];
  long[] buildarray = new long[6];
  //array building

  harness.checkPoint("NewLongArray()");
  long current = 0x4040404040404040L;
  for(int i=0; i<6; i++)
  {
    refarray[i]=current;
    current = (current+ 0x1010101010101010L);
  }
  buildarray = buildArray(6,0x4040404040404040L,0x1010101010101010L);
  compare(buildarray, refarray);

  // array length
  harness.checkPoint("GetArrayLength long array");
  harness.check(getArrayLength(refarray),6);

  // scan contents of array
  harness.checkPoint("Accessing elements of long array");
  harness.check(scanArrayElements(refarray,0L),4,"GetLongArrayElements/ReleaseLongArrayElements");
  harness.check(scanArrayRegionComplete(refarray,0L),4,"GetLongArrayRegion: complete region");
  harness.check(scanArrayRegionOneByOne(refarray,0L),4,"GetLongArrayRegion: one by one");

  for(int i=0; i<3; i++)
  {
    refarray[i]     = 0x1010101010101010L * i;
    refarray[i+3]   = 0x1010101010101010L * i;
    buildarray[i]   = 0x1010101010101010L * i;
    buildarray[i+3] = 0x1010101010101010L * i;
  }
  harness.checkPoint("changing elements of long array");
  //replace values 0 by values 0x40
  refarray[0]=0x4040404040404040L;
  refarray[3]=0x4040404040404040L;
  changeArrayElements(buildarray, 0, 0x4040404040404040L);
  compare(buildarray, refarray,"GetLongArrayElements/ReleaseLongArrayElements");

  //replace values 0 by values 0x40
  refarray[2]=0x6060606060606060L;
  refarray[5]=0x6060606060606060L;
  changeArrayRegionComplete(buildarray, 0x2020202020202020L, 0x6060606060606060L);
  compare(buildarray, refarray,"GetLongArrayRegion: complete region");

  //replace values 0 by values 0x40
  refarray[0]=0x7070707070707070L;
  refarray[3]=0x7070707070707070L;
  changeArrayRegionOneByOne(buildarray, 0x4040404040404040L, 0x7070707070707070L);
  compare(buildarray, refarray,"GetLongArrayRegion: one by one");
}

private void testLongArrayCritical()
{
  long[] refarray   =  buildArray(6,0x4040404040404040L,0x1010101010101010L);
  long[] buildarray =  buildArray(6,0x4040404040404040L,0x1010101010101010L);

  harness.checkPoint("Accessing elements of array, critical");
  harness.check(scanArrayCritical(refarray,0L),4,"GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical");

  harness.checkPoint("changing elements of array, critical");
  //replace values 0x50 by values 0x10
  refarray[1]=0x1010101010101010L;
  changeArrayCritical(buildarray, 0x5050505050505050L, 0x1010101010101010L);
  compare(buildarray, refarray,"GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical");
}

/** natives: */
/// build using GetLongArrayElements/ReleaseLongArrayElements
native private long[] buildArray(int len, long start, long increment);
/// get array length
native private int getArrayLength(long[] array);

/// scan using GetLongArrayElements/ReleaseLongArrayElements
native private int scanArrayElements(long[] array, long max);
/// scan using GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical
native private int scanArrayCritical(long[] array, long max);
/// scan using GetLongArrayRegion covering the complete region
native private int scanArrayRegionComplete(long[] array, long max);
/// scan using GetLongArrayRegion one long at a time
native private int scanArrayRegionOneByOne(long[] array, long max);

/// change content using GetLongArrayElements/ReleaseLongArrayElements
native private void changeArrayElements(long[]array, long toreplace, long newvalue);
/// change contents using GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical
native private void changeArrayCritical(long[]array, long toreplace, long newvalue);
/// change contents using ReleaseLongArrayRegion one buffer for the complete region
native private void changeArrayRegionComplete(long[]array, long toreplace, long newvalue);
/// change contents using ReleaseLongArrayRegion one element at a time
native private void changeArrayRegionOneByOne(long[]array, long toreplace, long newvalue);



/*********************************************************************************************************************/
/**
 <type> array functions for primitive float
*/

private void testFloatArrayFunctions()
{
  float[] refarray = new float[6];
  float[] buildarray = new float[6];
  //array building

  harness.checkPoint("NewFloatArray()");
  float current = 0.61f;
  for(int i=0; i<6; i++)
  {
    refarray[i]=current;
    current = (current+ 0.11f);
  }
  buildarray = buildArray(6,0.61f,0.11f);
  compare(buildarray, refarray);

  // array length
  harness.checkPoint("GetArrayLength float array");
  harness.check(getArrayLength(refarray),6);

  // scan contents of array
  harness.checkPoint("Accessing elements of float array");
  harness.check(scanArrayElements(refarray,1.0f),2,"GetFloatArrayElements/ReleaseFloatArrayElements");
  harness.check(scanArrayRegionComplete(refarray,1.0f),2,"GetFloatArrayRegion: complete region");
  harness.check(scanArrayRegionOneByOne(refarray,1.0f),2,"GetFloatArrayRegion: one by one");

  for(int i=0; i<3; i++)
  {
    refarray[i]     = 0.11f * i;
    refarray[i+3]   = 0.11f * i;
    buildarray[i]   = 0.11f * i;
    buildarray[i+3] = 0.11f * i;
  }
  harness.checkPoint("changing elements of float array");
  //replace values 0 by values 0.40
  refarray[0]=0.44f;
  refarray[3]=0.44f;
  changeArrayElements(buildarray, 0, 0.44f);
  compare(buildarray, refarray,"GetFloatArrayElements/ReleaseFloatArrayElements");

  //replace values 0 by values 0.40
  refarray[2]=0.66f;
  refarray[5]=0.66f;
  changeArrayRegionComplete(buildarray, 0.22f, 0.66f);
  compare(buildarray, refarray,"GetFloatArrayRegion: complete region");

  //replace values 0 by values 0.40
  refarray[0]=0.77f;
  refarray[3]=0.77f;
  changeArrayRegionOneByOne(buildarray, 0.44f, 0.77f);
  compare(buildarray, refarray,"GetFloatArrayRegion: one by one");
}

private void testFloatArrayCritical()
{
  float[] refarray   =  buildArray(6, 0.61f, 0.11f);
  float[] buildarray =  buildArray(6, 0.61f, 0.11f);

  harness.checkPoint("Accessing elements of array, critical");
  harness.check(scanArrayCritical(refarray, 1.0f),2,"GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical");

  harness.checkPoint("changing elements of array, critical");
  //replace values 0x50 by values 0x10
  refarray[1]=0.22f;
  changeArrayCritical(buildarray, 0.72f, 0.22f);
  compare(buildarray, refarray,"GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical");
}

/** natives: */
/// build using GetFloatArrayElements/ReleaseFloatArrayElements
native private float[] buildArray(int len, float start, float increment);
/// get array length
native private int getArrayLength(float[] array);

/// scan using GetFloatArrayElements/ReleaseFloatArrayElements
native private int scanArrayElements(float[] array, float max);
/// scan using GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical
native private int scanArrayCritical(float[] array, float max);
/// scan using GetFloatArrayRegion covering the complete region
native private int scanArrayRegionComplete(float[] array, float max);
/// scan using GetFloatArrayRegion one float at a time
native private int scanArrayRegionOneByOne(float[] array, float max);

/// change content using GetFloatArrayElements/ReleaseFloatArrayElements
native private void changeArrayElements(float[]array, float toreplace, float newvalue);
/// change contents using GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical
native private void changeArrayCritical(float[]array, float toreplace, float newvalue);
/// change contents using ReleaseFloatArrayRegion one buffer for the complete region
native private void changeArrayRegionComplete(float[]array, float toreplace, float newvalue);
/// change contents using ReleaseFloatArrayRegion one element at a time
native private void changeArrayRegionOneByOne(float[]array, float toreplace, float newvalue);


/*********************************************************************************************************************/
/**
 <type> array functions for primitive double
*/

private void testDoubleArrayFunctions()
{
  double[] refarray = new double[6];
  double[] buildarray = new double[6];
  //array building

  harness.checkPoint("NewDoubleArray()");
  double current = 0.6006;
  for(int i=0; i<6; i++)
  {
    refarray[i]=current;
    current = (current+ 0.1001);
  }
  buildarray = buildArray(6, 0.6006, 0.1001);
  compare(buildarray, refarray);

  // array length
  harness.checkPoint("GetArrayLength double array");
  harness.check(getArrayLength(refarray),6);

  // scan contents of array
  harness.checkPoint("Accessing elements of double array");
  harness.check(scanArrayElements(refarray, 1.0),2,"GetDoubleArrayElements/ReleaseDoubleArrayElements");
  harness.check(scanArrayRegionComplete(refarray, 1.0),2,"GetDoubleArrayRegion: complete region");
  harness.check(scanArrayRegionOneByOne(refarray, 1.0),2,"GetDoubleArrayRegion: one by one");

  for(int i=0; i<3; i++)
  {
    refarray[i]     = 0.10011 * i;
    refarray[i+3]   = 0.10011 * i;
    buildarray[i]   = 0.10011 * i;
    buildarray[i+3] = 0.10011 * i;
  }
  harness.checkPoint("changing elements of double array");
  //replace values 0 by values 0.40
  refarray[0]=0.400456;
  refarray[3]=0.400456;
  changeArrayElements(buildarray, 0.0, 0.400456);
  compare(buildarray, refarray,"GetDoubleArrayElements/ReleaseDoubleArrayElements");

  //replace values 0 by values 0.40
  refarray[2]=0.600456;
  refarray[5]=0.600456;
  changeArrayRegionComplete(buildarray, 0.20022, 0.600456);
  compare(buildarray, refarray,"GetDoubleArrayRegion: complete region");

  //replace values 0 by values 0.40
  refarray[0]=0.700456;
  refarray[3]=0.700456;
  changeArrayRegionOneByOne(buildarray, 0.400456, 0.700456);
  compare(buildarray, refarray,"GetDoubleArrayRegion: one by one");
}

private void testDoubleArrayCritical()
{
  double[] refarray   =  buildArray(6, 0.601, 0.101);
  double[] buildarray =  buildArray(6, 0.601, 0.101);

  harness.checkPoint("Accessing elements of array, critical");
  harness.check(scanArrayCritical(refarray, 1.0),2,"GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical");

  harness.checkPoint("changing elements of array, critical");
  //replace values 0x50 by values 0x10
  refarray[1]=0.2202;
  changeArrayCritical(buildarray, 0.702, 0.2202);
  compare(buildarray, refarray,"GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical");
}

/** natives: */
/// build using GetDoubleArrayElements/ReleaseDoubleArrayElements
native private double[] buildArray(int len, double start, double increment);
/// get array length
native private int getArrayLength(double[] array);

/// scan using GetDoubleArrayElements/ReleaseDoubleArrayElements
native private int scanArrayElements(double[] array, double max);
/// scan using GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical
native private int scanArrayCritical(double[] array, double max);
/// scan using GetDoubleArrayRegion covering the complete region
native private int scanArrayRegionComplete(double[] array, double max);
/// scan using GetDoubleArrayRegion one double at a time
native private int scanArrayRegionOneByOne(double[] array, double max);

/// change content using GetDoubleArrayElements/ReleaseDoubleArrayElements
native private void changeArrayElements(double[]array, double toreplace, double newvalue);
/// change contents using GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical
native private void changeArrayCritical(double[]array, double toreplace, double newvalue);
/// change contents using ReleaseDoubleArrayRegion one buffer for the complete region
native private void changeArrayRegionComplete(double[]array, double toreplace, double newvalue);
/// change contents using ReleaseDoubleArrayRegion one element at a time
native private void changeArrayRegionOneByOne(double[]array, double toreplace, double newvalue);



/*********************************************************************************************************************/
/**
 special case: <type> array functions for primitive boolean
*/

private void testBooleanArrayFunctions()
{
  harness.checkPoint("NewBooleanArray()");
  boolean[] refarray = {true, true, true, true, false, false};
  boolean[] buildarray = buildArray(4,2);
  compare(buildarray, refarray);

  // array length
  harness.checkPoint("GetArrayLength");
  harness.check(getArrayLength(refarray),6);

  // scan contents of array
  harness.checkPoint("Accessing elements of boolean array");
  refarray[0] = false; //{false, true, true, true, false, false};
  buildarray[0] = false; //{false, true, true, true, false, false};
  harness.check(scanArrayElements(refarray, false),3,"GetBooleanArrayElements/ReleaseBooleanArrayElements");
  harness.check(scanArrayRegionComplete(refarray, false),3,"GetBooleanArrayRegion: complete region");
  harness.check(scanArrayRegionOneByOne(refarray, false),3,"GetBooleanArrayRegion: one by one");


  harness.checkPoint("changing elements of boolean array");
  shiftleft(refarray, true);  //{true, true, true, false, false, true};
  changeArrayElements(buildarray, true);
  compare(buildarray, refarray,"GetBooleanArrayElements/ReleaseBooleanArrayElements");

  shiftleft(refarray, true);  //{true, true, false, false, true, true};
  changeArrayRegionComplete(buildarray, true);
  compare(buildarray, refarray,"GetBooleanArrayRegion: complete region");

  shiftleft(refarray, false);  //{true, false, false, true, true, false};
  changeArrayRegionOneByOne(buildarray, false);
  compare(buildarray, refarray,"GetBooleanArrayRegion: one by one");

}

private void testBooleanArrayCritical()
{
  boolean[] refarray    = {false, true, true, true, false, false};
  boolean[] buildarray  = {false, true, true, true, false, false};

  harness.checkPoint("Accessing elements of boolean array, critical");
  harness.check(scanArrayCritical(refarray, false),3,"GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical");

  harness.checkPoint("changing elements of boolean array, critical");
  shiftleft(refarray, true);  //{true, true, true, false, false, true};
  changeArrayCritical(buildarray, true);
  compare(buildarray, refarray,"GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical");
}

/** auxilliary function for boolean */
private void  shiftleft(boolean[]refarray, boolean nextvalue)
{
  for(int i=1; i<refarray.length; i++)
    refarray[i-1] = refarray[i];
  refarray[(refarray.length - 1)] = nextvalue;
}
/** natives: */
/// build using GetBooleanArrayElements/ReleaseBooleanArrayElements
native private boolean[] buildArray(int trues, int falses);
/// get array length
native private int getArrayLength(boolean[] array);

///return numbers of occurrences GetBooleanArrayElements/ReleaseBooleanArrayElements
native private int scanArrayElements(boolean[] array, boolean valuetoscan);
/// scan using GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical
native private int scanArrayCritical(boolean[] array, boolean valuetoscan);
/// scan using GetBooleanArrayRegion covering the complete region
native private int scanArrayRegionComplete(boolean[] array, boolean valuetoscan);
/// scan using GetBooleanArrayRegion one double at a time
native private int scanArrayRegionOneByOne(boolean[] array, boolean valuetoscan);

/// invert first of double occurrences using GetBooleanArrayElements/ReleaseBooleanArrayElements
native private void changeArrayElements(boolean[] array, boolean nextvalue);
/// invert first of double occurrences using GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical
native private void changeArrayCritical(boolean[] array, boolean nextvalue);
/// invert first of double occurrences using ReleaseBooleanArrayRegion one buffer for the complete region
native private void changeArrayRegionComplete(boolean[] array, boolean nextvalue);
/// invert first of double occurrences using ReleaseBooleanArrayRegion one element at a time
native private void changeArrayRegionOneByOne(boolean[] array, boolean nextvalue);



/*********************************************************************************************************************/
/**
 special case: <type> array functions for complex class objects (type SimpleContainer)
*/

private void testObjectArrayFunctions()
{
  // (newObjectarray is tested later on in object construction)

  // create main working array
  SimpleContainer[] testarray = new SimpleContainer[6];
  int number = 1200;
  for (int i=0; i<6; i++)
    testarray[i] = buildSimpleContainer(number+i, "test"+i, false, false, false);

  // array length
  harness.checkPoint("GetArrayLength object array");
  harness.check(getArrayLength(testarray),6);

  // scan contents of array using getObjectArrayElement/SetObjectArrayelement
  harness.checkPoint("Accessing elements of object array");
  harness.check(scanArrayElements(testarray, 1203),2);
  // (no other accessing methods are available)

  harness.checkPoint("changing elements of object array");
  //replace using getObjectArrayElement/SetObjectArrayelement
  changeArrayElements(testarray, 1201, buildSimpleContainer(1210,"new value",false, false, true));
  compareSimpleContainer(testarray[0], 1200, "test0"    , false, false, false, harness);
  compareSimpleContainer(testarray[1], 1210, "new value", false, false,  true, harness);
  compareSimpleContainer(testarray[2], 1202, "test2"    , false, false, false, harness);
  //(no other accessing methods are available )
}
/** native functins for object arrays*/
///get array length
native private int getArrayLength(SimpleContainer[] array);
///access array using GetObjectArrayElement/SetObjectArrayElement
native private int scanArrayElements(SimpleContainer[] array, int maximum);
///access array using GetObjectArrayElement/SetObjectArrayElement
native private void changeArrayElements(SimpleContainer[] array, int toreplace, SimpleContainer newvalue);





/*********************************************************************************************************************
* help and test functions
*/

/// build a string from a given array
private String buildString(boolean[] testarray)
{
  String notation = "Z"+testarray.length + "<";
  if(testarray.length >0)
    notation+=(testarray[0]==true)?"1":"0";
  for(int i=1; i<testarray.length; i++)
    notation+=(testarray[i]==true)?",1":",0";
  notation += ">";
  return notation;
}
/// build a string from a given array
private String buildString(byte[] testarray)
{
  String notation = "B"+testarray.length + "<";
  if(testarray.length >0)
    notation+=Integer.toHexString((int)(testarray[0]));
  for(int i=1; i<testarray.length; i++)
    notation+=","+Integer.toHexString((int)(testarray[i]));
  notation += ">";
  return notation;
}
/// build a string from a given array
private String buildString(short[] testarray)
{
  String notation = "S"+testarray.length + "<";
  if(testarray.length >0)
    notation+=Integer.toHexString((int)(testarray[0]));
  for(int i=1; i<testarray.length; i++)
    notation+=","+Integer.toHexString((int)(testarray[i]));
  notation += ">";
  return notation;
}
/// build a string from a given array
private String buildString(int[] testarray)
{
  String notation = "I"+testarray.length + "<";
  if(testarray.length >0)
    notation+=Integer.toHexString(testarray[0]);
  for(int i=1; i<testarray.length; i++)
    notation+=","+Integer.toHexString(testarray[i]);
  notation += ">";
  return notation;
}
/// build a string from a given array
private String buildString(long[] testarray)
{
  String notation = "J"+testarray.length + "<";
  if(testarray.length >0)
    notation+=Long.toHexString(testarray[0]);
  for(int i=1; i<testarray.length; i++)
    notation+=","+Long.toHexString(testarray[i]);
  notation += ">";
  return notation;
}
/// build a string from a given array
private String buildString(float[] testarray)
{
  String notation = "F"+testarray.length + "<";
  if(testarray.length >0)
    notation+=Float.toString(testarray[0]);
  for(int i=1; i<testarray.length; i++)
    notation+=","+Float.toString(testarray[i]);
  notation += ">";
  return notation;
}
/// build a string from a given array
private String buildString(double[] testarray)
{
  String notation = "D"+testarray.length + "<";
  if(testarray.length >0)
    notation+=Double.toString(testarray[0]);
  for(int i=1; i<testarray.length; i++)
    notation+=","+Double.toString(testarray[i]);
  notation += ">";
  return notation;
}

/// compare a given array to a reference string representation
private void compare(boolean[] testarray, String reference){harness.check(buildString(testarray), reference);}
private void compare(byte[] testarray, String reference){harness.check(buildString(testarray), reference);}
private void compare(short[] testarray, String reference){harness.check(buildString(testarray), reference);}
private void compare(int[] testarray, String reference){harness.check(buildString(testarray), reference);}
private void compare(long[] testarray, String reference){harness.check(buildString(testarray), reference);}
private void compare(float[] testarray, String reference){harness.check(buildString(testarray), reference);}
private void compare(double[] testarray, String reference){harness.check(buildString(testarray), reference);}

private void compare(boolean[] testarray, String reference, String note){harness.check(buildString(testarray), reference, note);}
private void compare(byte[] testarray, String reference, String note){harness.check(buildString(testarray), reference, note);}
private void compare(short[] testarray, String reference, String note){harness.check(buildString(testarray), reference, note);}
private void compare(int[] testarray, String reference, String note){harness.check(buildString(testarray), reference, note);}
private void compare(long[] testarray, String reference, String note){harness.check(buildString(testarray), reference, note);}
private void compare(float[] testarray, String reference, String note){harness.check(buildString(testarray), reference, note);}
private void compare(double[] testarray, String reference, String note){harness.check(buildString(testarray), reference, note);}

/// compare a given array to a reference array
private void compare(boolean[] testarray, boolean[] reference)
                    {harness.check(buildString(testarray), buildString(reference));}
private void compare(byte[] testarray, byte[] reference)
                    {harness.check(buildString(testarray), buildString(reference));}
private void compare(short[] testarray, short[] reference)
                    {harness.check(buildString(testarray), buildString(reference));}
private void compare(int[] testarray, int[] reference)
                    {harness.check(buildString(testarray), buildString(reference));}
private void compare(long[] testarray, long[] reference)
                    {harness.check(buildString(testarray), buildString(reference));}
private void compare(float[] testarray, float[] reference)
                    {harness.check(buildString(testarray), buildString(reference));}
private void compare(double[] testarray, double[] reference)
                    {harness.check(buildString(testarray), buildString(reference));}

private void compare(boolean[] testarray, boolean[] reference, String note)
                    {harness.check(buildString(testarray), buildString(reference), note);}
private void compare(byte[] testarray, byte[] reference, String note)
                    {harness.check(buildString(testarray), buildString(reference), note);}
private void compare(short[] testarray, short[] reference, String note)
                    {harness.check(buildString(testarray), buildString(reference), note);}
private void compare(int[] testarray, int[] reference, String note)
                    {harness.check(buildString(testarray), buildString(reference), note);}
private void compare(long[] testarray, long[] reference, String note)
                    {harness.check(buildString(testarray), buildString(reference), note);}
private void compare(float[] testarray, float[] reference, String note)
                    {harness.check(buildString(testarray), buildString(reference), note);}
private void compare(double[] testarray, double[] reference, String note)
                    {harness.check(buildString(testarray), buildString(reference), note);}
/*********************************************************************************************************************
* TestRunner interface : perform the tests described and pass the results to the testharness
*/

  public void test (TestHarness newharness)
	{
		harness = newharness;
		harness.setclass("JNI class and array construction/ class dependencies");
		
		testByteArrayFunctions();
		testByteArrayCritical();
//		newharness.fail("GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical causes Wonka to exit");
		
		testShortArrayFunctions();
		testShortArrayCritical();
		//newharness.fail("GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical causes Wonka to exit");
		
		testIntArrayFunctions();
		testIntArrayCritical();
		//newharness.fail("GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical causes Wonka to exit");
		
		testLongArrayFunctions();
		testLongArrayCritical();
		//newharness.fail("GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical causes Wonka to exit");
		
		testFloatArrayFunctions();
		testFloatArrayCritical();
		//newharness.fail("GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical causes Wonka to exit");
		
		testDoubleArrayFunctions();
		testDoubleArrayCritical();
		//newharness.fail("GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical causes Wonka to exit");
				
		testBooleanArrayFunctions();
		testBooleanArrayCritical();
		//newharness.fail("GetPrimitiveArrayCritical/ReleasePrimitiveArrayCritical causes Wonka to exit");
		
		testObjectArrayFunctions();
	}




}