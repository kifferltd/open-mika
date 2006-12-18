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
public class JNIObjectFunctionAccessTest extends ContainerFunctions implements Testlet
{

// load the native library//
  static
  {
    System.loadLibrary("JNIObjectFunctionAccessTest");
  }

/****************************************************************************************
* our variables:
*/
  protected static TestHarness harness;  // our harness


  public MultiFunctionContainer cm1 = new MultiFunctionContainer();
  public MultiFunctionContainer cm2 = new MultiFunctionContainer();
  public MultiFunctionContainer cm3 = new MultiFunctionContainer();
  public MultiFunctionContainer cm4 = new MultiFunctionContainer();
  public MultiFunctionContainer cm5 = new MultiFunctionContainer();

  public SimpleContainer cs1 = new SimpleContainer();
  public SimpleContainer cs2 = new SimpleContainer();
  public SimpleContainer cs3 = new SimpleContainer();
  public SimpleContainer cs4 = new SimpleContainer();
  public SimpleContainer cs5 = new SimpleContainer();

  protected boolean varZ;
  protected byte    varB;
  protected char    varC;
  protected short   varS;
  protected int     varI;
  protected long    varJ;
  protected float   varF;
  protected double  varD;


/*********************************************************************************************************************
* TestRunner interface : perform the tests described and pass the results to the testharness
*/

  public void test (TestHarness newharness)
	{
		harness = newharness;
		harness.setclass("JNI class and array construction/ class dependencies");
	
		/// the test classes are so simple that they can be written out in full here without much dismay
    // construct a series of new MultiFunctionContainers objects using NewObjectA/NewObjectV

    harness.checkPoint("series of new builds using NewObjectA ");
    buildFiveContainersA(1700,"ali", true);
    compareAllFunctionContainers(1700,"ali", true);
    harness.checkPoint("series of new builds using NewObjectV ");
    buildFiveContainersV(1701,"Bactar", true);
    compareAllFunctionContainers(1701,"Bactar", true);

    // perform a series of object setting functions that return a series of SimpleContainers pasted into our array
    // The JNI code uses CallObjectMethodA /-V to make a call to MultiFunctionContainer::getBuiltContainerBase
    harness.checkPoint("series of object setting functions using CallObjectMethodA ");
    harness.verbose("series of object setting functions using CallObjectMethodA ");
    harness.verbose("SetFiveSimpleContainersA(1702,<Casim>, true);");
    SetFiveSimpleContainersA(1702,"Casim", true);
    harness.verbose("done building data: checking");
    compareAllSimpleContainers(1702,"Casim", true);
    harness.checkPoint("series of object setting functions using CallObjectMethodV ");
    SetFiveSimpleContainersV(1703,"Dariuz", true);
    compareAllSimpleContainers(1703,"Dariuz", true);

    //idem but this time using CallStaticObjectMethodA /-V to call MultiFunctionContainer::getBuiltContainerBaseStatic
    harness.checkPoint("series of static object setting functions using CallStaticObjectMethodA ");
    SetStaticFiveSimpleContainersA(1704,"Erkan", true);
    compareAllSimpleContainers(1704,"Erkan", true);
    harness.checkPoint("series of static void setting functions using CallStaticObjectMethodV ");
    SetStaticFiveSimpleContainersV(1705,"Feisal", true);
    compareAllSimpleContainers(1705,"Feisal", true);

    //idem but this time using CallNonvirtualObjectMethodA /-V
    //to call the base classe's ConstructionContainer::getBuiltContainerBaseStatic
    harness.checkPoint("series of nonvirtual object setting functions using CallNonvirtualObjectMethodA ");
    SetNonvirtualFiveSimpleContainersA(1706,"Gelareh", true);
    compareAllSimpleContainers(-1706,"elarehG", false);
    harness.checkPoint("series of nonvirtual object setting functions using CallNonvirtualObjectMethodV ");
    SetNonvirtualFiveSimpleContainersV(1707,"Hadji", true);
    compareAllSimpleContainers(-1707,"adjiH", false);

    // perform a series of void setting functions to the MultiFunctionContainers objects using CallVoidMethodA /-V
    harness.checkPoint("series of void setting functions using CallVoidMethodA ");
    SetVoidFiveContainersA(1708,"Ibrahim", true);
    compareAllFunctionContainers(1708,"Ibrahim", true);
    harness.checkPoint("series of void setting functions using CallVoidMethodV ");
    SetVoidFiveContainersV(1709,"Jussuf", true);
    compareAllFunctionContainers(1709,"Jussuf", true);

    //idem but this time using CallStaticVoidMethodA /-V to call MultiFunctionContainer::BuildContainerStatic
    // for all of our functions
    harness.checkPoint("series of static void setting functions using CallStaticVoidMethodA ");
    SetStaticVoidFiveContainersA(1708,"Ibrahim", true);
    compareAllFunctionContainers(1708,"Ibrahim", true);
    harness.checkPoint("series of static void setting functions using CallStaticVoidMethodV ");
    SetStaticVoidFiveContainersV(1709,"Jussuf", true);
    compareAllFunctionContainers(1709,"Jussuf", true);

    //idem but this time using CallNonvirtualVoidMethodA /-V to call the base class
    // ConstructionContainer::BuildContainer for all of our functions
    harness.checkPoint("series of nonvirtual void setting functions using CallNonvirtualVoidMethodA ");
    SetNonvirtualVoidFiveContainersA(1710,"Karim", true);
    compareAllFunctionContainers(-1710,"arimK", false);
    harness.checkPoint("series of nonvirtual void setting functions using CallNonvirtualVoidMethodV ");
    SetNonvirtualVoidFiveContainersV(1711,"Laila", true);
    compareAllFunctionContainers(-1711,"ailaL", false);
  }

  native void buildFiveContainersA(int i, String s, boolean b);
  native void buildFiveContainersV(int i, String s, boolean b);
	
	native void SetFiveSimpleContainersA(int i, String s, boolean b);	
	native void SetFiveSimpleContainersV(int i, String s, boolean b);	
	native void SetStaticFiveSimpleContainersA(int i, String s, boolean b);	
	native void SetStaticFiveSimpleContainersV(int i, String s, boolean b);	
	native void SetNonvirtualFiveSimpleContainersA(int i, String s, boolean b);	
	native void SetNonvirtualFiveSimpleContainersV(int i, String s, boolean b);	
	
	native void SetVoidFiveContainersA(int i, String s, boolean b);	
	native void SetVoidFiveContainersV(int i, String s, boolean b);	
	native void SetStaticVoidFiveContainersA(int i, String s, boolean b);	
	native void SetStaticVoidFiveContainersV(int i, String s, boolean b);	
	native void SetNonvirtualVoidFiveContainersA(int i, String s, boolean b);	
	native void SetNonvirtualVoidFiveContainersV(int i, String s, boolean b);	
		
/****************************************************************************************
* helper function: Check if all containers are graduately set to the given values
* as wanted in the -A, -V JNI- functions
*/
  protected void compareAllFunctionContainers(int i, String s, boolean b)
  {
    compareConstructionContainer(cm1, i,"",!b,!b,!b, harness);
    compareConstructionContainer(cm2, i, s,!b,!b,!b, harness);
    compareConstructionContainer(cm3, i, s, b,!b,!b, harness);
    compareConstructionContainer(cm4, i, s, b, b,!b, harness);
    compareConstructionContainer(cm5, i, s, b, b, b, harness);
  }
  protected void compareAllSimpleContainers(int i, String s, boolean b)
  {
    compareSimpleContainer(cs1, i,"",!b,!b,!b, harness);
    compareSimpleContainer(cs2, i, s,!b,!b,!b, harness);
    compareSimpleContainer(cs3, i, s, b,!b,!b, harness);
    compareSimpleContainer(cs4, i, s, b, b,!b, harness);
    compareSimpleContainer(cs5, i, s, b, b, b, harness);
  }


/****************************************************************************************
* Static functions for the CallMethodA/V tests of the JNIPrimitiveObjectAccessTest derived class
* :  of a series of numbers, return the absolute maximum
*/

  static long absmax(long x1, long x2)
  {
    if(x1>0 && x2>0)
    {
      if(x1>x2)
        return x1;
      else
        return x2;
    }
    else if (x1>0)
    {
      if(x1>(-x2))
        return x1;
      else
        return x2;
    }
    else if (x2>0)
    {
      if(x2<(-x1))
        return x1;
      else
        return x2;
    }
    else
    {
      if(x1<x2)
        return x1;
      else
        return x2;
    }
  }

  static int    absmax(int   x1, int   x2) {return   (int)absmax((long)x1, (long)x2);}
  static short  absmax(short x1,short  x2) {return (short)absmax((long)x1, (long)x2);}
  static byte   absmax(byte  x1, byte  x2) {return (byte)absmax((long)x1, (long)x2);}

  static long   absmax(long  x1, long  x2, long  x3) {return absmax(x1,absmax(x2,x3));}
  static int    absmax(int   x1, int   x2, int   x3) {return absmax(x1,absmax(x2,x3));}
  static short  absmax(short x1, short x2, short x3) {return absmax(x1,absmax(x2,x3));}
  static byte   absmax(byte  x1, byte  x2, byte  x3) {return absmax(x1,absmax(x2,x3));}

  static long   absmax(long  x1, long  x2, long  x3, long  x4) {return absmax(x1,absmax(x2,x3,x4));}
  static int    absmax(int   x1, int   x2, int   x3, int   x4) {return absmax(x1,absmax(x2,x3,x4));}
  static short  absmax(short x1, short x2, short x3, short x4) {return absmax(x1,absmax(x2,x3,x4));}
  static byte   absmax(byte  x1, byte  x2, byte  x3, byte  x4) {return absmax(x1,absmax(x2,x3,x4));}

/*
  static byte   absmax(byte  x1, byte  x2)
  {
    byte result =  (byte)absmax((long)x1, (long)x2);
   	System.out.println("absmax("+x1+", "+x2+") = "+ result);	
    return result;
  }
  static byte   absmax(byte  x1, byte  x2, byte  x3) //{return absmax(x1,absmax(x2,x3));}
  {
    byte result =  (byte)absmax((long)x1, (long)x2, (long)x3);
   	System.out.println("absmax("+x1+", "+x2+", "+x3+") = "+ result);	
    return result;
  }
  static byte   absmax(byte  x1, byte  x2, byte  x3, byte  x4) //{return absmax(x1,absmax(x2,x3,x4));}
  {
    byte result =  (byte)absmax((long)x1, (long)x2, (long)x3, (long)x4);
   	System.out.println("absmax("+x1+", "+x2+", "+x3+", "+x4+") = "+ result);	
    return result;
  }
*/
/****************************************************************************************
* float and double
*/
  static double absmax(double x1, double x2)
  {
    if(x1>0 && x2>0)
    {
      if(x1>x2)
        return x1;
      else
        return x2;
    }
    else if (x1>0)
    {
      if(x1>(-x2))
        return x1;
      else
        return x2;
    }
    else if (x2>0)
    {
      if(x2<(-x1))
        return x1;
      else
        return x2;
    }
    else
    {
      if(x1<x2)
        return x1;
      else
        return x2;
    }
  }

  static float  absmax(float  x1, float  x2) {return (float)absmax((double)x1, (double)x2);}
  static float  absmax(float  x1, float  x2, float  x3) {return absmax(x1,absmax(x2,x3));}
  static double absmax(double x1, double x2, double x3) {return absmax(x1,absmax(x2,x3));}
  static float  absmax(float  x1, float  x2, float  x3, float  x4) {return absmax(x1,absmax(x2,x3,x4));}
  static double absmax(double x1, double x2, double x3, double x4) {return absmax(x1,absmax(x2,x3,x4));}

/****************************************************************************************
* special case unsigned 16-bit char:
*/
  static char   absmax(char  x1, char  x2)                      {return ((x1>x2)?x1:x2);}
  static char   absmax(char  x1, char  x2, char  x3)            {return absmax(x1,absmax(x2,x3));}
  static char   absmax(char  x1, char  x2, char  x3, char  x4)  {return absmax(x1,absmax(x2,x3,x4));}

/****************************************************************************************
* Special case: boolean
*/
  static boolean binary(boolean x1, boolean x2)
  {
    if(x1==x2)
      return false;
    else
      return true;
  }

  static boolean binary(boolean x1, boolean x2, boolean x3) {return binary(x1, binary(x2,x3));}
  static boolean binary(boolean x1, boolean x2, boolean x3, boolean x4) {return binary(x1, binary(x2,x3,x4));}

/****************************************************************************************
* public functions (base class): will be overwritten in the derived class, only to be called from there using the <Novirtual> option
*/
  public byte   getMaximum(byte   x1)                          {return (byte) (0 - absmax(varB,x1));}
  public short  getMaximum(short  x1)                          {return (short)(0 - absmax(varS,x1));}
  public int    getMaximum(int    x1)                          {return -absmax(varI,x1);}
  public long   getMaximum(long   x1)                          {return -absmax(varJ,x1);}
  public float  getMaximum(float  x1)                          {return -absmax(varF,x1);}
  public double getMaximum(double x1)                          {return -absmax(varD,x1);}

  public byte   getMaximum(byte   x1, byte   x2)               {return (byte) (0 -absmax(varB,x1,x2));}
  public short  getMaximum(short  x1, short  x2)               {return (short)(0 -absmax(varS,x1,x2));}
  public int    getMaximum(int    x1, int    x2)               {return -absmax(varI,x1,x2);}
  public long   getMaximum(long   x1, long   x2)               {return -absmax(varJ,x1,x2);}
  public float  getMaximum(float  x1, float  x2)               {return -absmax(varF,x1,x2);}
  public double getMaximum(double x1, double x2)               {return -absmax(varD,x1,x2);}

  public byte   getMaximum(byte   x1, byte   x2, byte   x3)    {return (byte) (0 - absmax(varB,x1,x2,x3));}
  public short  getMaximum(short  x1, short  x2, short  x3)    {return (short)(0 - absmax(varS,x1,x2,x3));}
  public int    getMaximum(int    x1, int    x2, int    x3)    {return -absmax(varI,x1,x2,x3);}
  public long   getMaximum(long   x1, long   x2, long   x3)    {return -absmax(varJ,x1,x2,x3);}
  public float  getMaximum(float  x1, float  x2, float  x3)    {return -absmax(varF,x1,x2,x3);}
  public double getMaximum(double x1, double x2, double x3)    {return -absmax(varD,x1,x2,x3);}

  public char   getMaximum(char   x1)                           {return (char)(0xffff-absmax(varC,x1));}
  public char   getMaximum(char   x1, char   x2)                {return (char)(0xffff-absmax(varC,x1,x2));}
  public char   getMaximum(char   x1, char   x2, char   x3)     {return (char)(0xffff-absmax(varC,x1,x2,x3));}

  public boolean countBinary(boolean x1)                        {return !binary(varZ,x1);}
  public boolean countBinary(boolean x1,boolean x2)             {return !binary(varZ,x1,x2);}
  public boolean countBinary(boolean x1,boolean x2,boolean x3)  {return !binary(varZ,x1,x2,x3);}




}