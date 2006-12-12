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

public class JNIMemberAccessTest extends ContainerFunctions implements Testlet
{

// load the native library//
  static
  {
    System.loadLibrary("JNIMemberAccessTest");
  }

/*********************************************************************************************************************/
/**
* our variables:
*/
  protected static TestHarness harness;

  int     ourInt;
  String  ourString;
  boolean[] ourArray = new boolean[3];
  SimpleContainer ourSimpleContainer = new SimpleContainer();

/*********************************************************************************************************************/
/**
* Tests on accessing the member variables above through a JNI call
*/
  private void testMemberVariableAccess()
  {
    harness.checkPoint("Direct access member <primitive int> via native calls");
    //integer
    ourInt = 1300;
    harness.check(getMemberInteger(), ourInt);

    setMemberInteger(1301);
    harness.check(ourInt, 1301);

    harness.checkPoint("Direct access member <String> via native calls");
    //string
    ourString = "Leopold I";
    harness.check(getMemberString(), ourString);

    setMemberString("Leopold II");
    harness.check (ourString, "Leopold II");

    harness.checkPoint("Direct access member <array> via native calls");
    // boolean array
    ourArray = buildBoolArray(true, true, false);
    boolean[] testarray = getMemberArray();
    checkBoolArray(testarray, ourArray, harness);

    testarray = buildBoolArray(true, false, true);
    setMemberArray(testarray);
    checkBoolArray(ourArray, testarray, harness);

    harness.checkPoint("Direct access member <class SimpleContainer> via native calls");
    // simple container
    ourSimpleContainer = buildSimpleContainer(500, "alfred", buildBoolArray(true, false, false));
    SimpleContainer testcontainer = getMemberSimpleContainer();
    compareSimpleContainer(testcontainer, ourSimpleContainer, harness);

    testcontainer = buildSimpleContainer(501, "Berthold", buildBoolArray(false, true, true));
    setMemberSimpleContainer(testcontainer);
    compareSimpleContainer(ourSimpleContainer, testcontainer, harness);

  }


// natives for the member access functions: (return or assign the member variable mentioned)
/// jni call that returns the member integer ourint
  private native int        getMemberInteger();
/// jni call that sets the member integer ourint to a specified value
  private native void       setMemberInteger(int i);
/// jni call that returns the member string ourstring
  private native String     getMemberString ();
/// jni call that sets the member string ourstring to a specified value
  private native void       setMemberString (String s);
/// jni call that returns the member array ourarray
  private native boolean[]  getMemberArray();
/// jni call that sets the member array ourarray to a specified array of values
  private native void       setMemberArray(boolean[] bx);
/// jni call that returns the member container class ourcontainer
  private native SimpleContainer getMemberSimpleContainer  ();
/// jni call that sets the member container class instance ourcontainer to a specified container contents
  private native void            setMemberSimpleContainer  (SimpleContainer c);



/*********************************************************************************************************************/
/**
* test on calling class functions from within JNI calls. The functions get or set the variables so their output can directly be checked.
*/
  private void testMemberFunctionAccess()
  {
    harness.checkPoint("Direct access function <primitive int> via native calls");
    //integer
    ourInt = 1302;
    harness.check(getInverseInteger(), -1302);

    setInverseInteger(1303);
    harness.check(ourInt, -1303);

    harness.checkPoint("Direct access function <String> via native calls");
    //string
    String teststring = "Albert I";
    String inversestring = inverse(teststring);
    ourString= teststring;
    harness.check(getInverseString(), inversestring);

    teststring = "Leopold III";
    inversestring = inverse(teststring);
    setInverseString(teststring);
    harness.check (ourString, inversestring);

    harness.checkPoint("Direct access function <array> via native calls");
    // boolean array
    boolean[] testarray = buildBoolArray(false, true, false);
    boolean[] inversearray = inverse(testarray);
    ourArray = testarray;
    checkBoolArray(getInverseArray(), inversearray, harness);

    testarray = buildBoolArray(false, false, true);
    inversearray = inverse(testarray);
    setInverseArray(testarray);
    checkBoolArray(ourArray, inversearray, harness);

    harness.checkPoint("Direct access function <class SimpleContainer> via native calls");
    // simple container
    SimpleContainer testcontainer = buildSimpleContainer(502, "Clara", buildBoolArray(false, false, false));
    SimpleContainer inversecontainer = inverse(testcontainer);
    ourSimpleContainer = testcontainer;
    testcontainer = getInverseSimpleContainer();
    compareSimpleContainer(testcontainer, inversecontainer, harness);

    testcontainer = buildSimpleContainer(503, "Dieter", buildBoolArray(true, true, true));
    inversecontainer = inverse(testcontainer);
    setInverseSimpleContainer(testcontainer);
    compareSimpleContainer(ourSimpleContainer, inversecontainer, harness);
  }


/** natives for the member access functions:
(call the 'inverse'-functions on the member variable mentioned to get or set the 'inverse' of the values
*/
  /// call getIntegerInverse and return its value
  private native int        getInverseInteger();
  /// call setIntegerInverse with the given value
  private native void       setInverseInteger(int i);
  /// call getStringInverse and return its value
  private native String     getInverseString ();
  /// call setStringInverse with the given value
  private native void       setInverseString (String s);
  /// call getArrayInverse and return its value
  private native boolean[]  getInverseArray();
  /// call setArrayInverse with the given value
  private native void       setInverseArray(boolean[] bx);
  /// call getContainerInverse and return its value
  private native SimpleContainer getInverseSimpleContainer();
  /// call setContainerInverse with the given value
  private native void            setInverseSimpleContainer(SimpleContainer c);

/** the 'inverse' functions to be called by the JNI interfaces: <br>
 => return the 'inverse' of the value of variable mentioned, <br>
 => assign the variable to the 'inverse' of the given value
*/
  /// return the inverse of outInt
  private int             getIntegerInverse()                    {return inverse(ourInt);   }
  /// set ourInt to the inverse of the given value
  private void            setIntegerInverse(int i)               {ourInt = inverse(i);      }
  /// return the inverse of outString
  private String          getStringInverse()                     {return inverse(ourString);}
  /// set ourString to the inverse of the given value
  private void            setStringInverse(String s)             {ourString = inverse(s);   }
  /// return the inverse of outArray
  private boolean[]       getArrayInverse()                      {return inverse(ourArray); }
  /// set ourArray to the inverse of the given value
  private void            setArrayInverse(boolean[] bx)          {ourArray = inverse(bx);   }
  /// return the inverse of outContainer
  private SimpleContainer getContainerInverse()                  {return inverse(ourSimpleContainer); }
  /// set ourContainer to the inverse of the given value
  private void            setContainerInverse(SimpleContainer c) {ourSimpleContainer = inverse(c);    };

/*********************************************************************************************************************/
/**
* test on calling static class functions from inside JNI function calls
*/
  private void testStaticFunctionAccess()
  {
    //integer
    harness.checkPoint("Direct static function on <primitive int> via native calls");
    int testint = 1304;
    harness.check(getInverse(testint), inverse(testint));

    //string
    harness.checkPoint("Direct static function on <String> via native calls");
    String teststring = "Boudewijn I";
    harness.check(getInverse(teststring), inverse(teststring));

    // boolean array
    harness.checkPoint("Direct static function on <array> via native calls");
    boolean[] testarray = buildBoolArray(true, true, false);
    checkBoolArray(getInverse(testarray), inverse(testarray), harness);

    // simple container
    harness.checkPoint("Direct static function on <class SimpleContainer> via native calls");
    SimpleContainer testcontainer = buildSimpleContainer(504, "Emil", buildBoolArray(true, false, true));
    compareSimpleContainer(getInverse(testcontainer), inverse(testcontainer), harness);
  }

/** natives for the static access functions:
call the 'inverse'-functions on the member variable mentioned and pass the return value if needed
*/

  ///call inverse(int) from within JNI and return the result
  private native int             getInverse(int i);
  ///call inverse(string) from within JNI and return the result
  private native String          getInverse(String s);
  ///call inverse(array) from within JNI and return the result
  private native boolean[]       getInverse(boolean[] bx);
  ///call inverse(container) from within JNI and return the result
  private native SimpleContainer getInverse(SimpleContainer c);

/** the 'static' functions to be called by the JNI interfaces:
 return the 'inverse' of the given integer, string, array or container value
*/
///returns -i
  static int              inverse(int i)         {return (0-i);}
/*
  static byte             inverse(byte i)        {return (byte)(0-i);}
  static short            inverse(short i)       {return (short)(0-i);}
  static char             inverse(char i)        {return (char)(0-i);}
  static long             inverse(long i)        {return (0-i);}
  static double           inverse(double i)      {return (double)(0-i);}
  static float            inverse(float i)       {return (float)(0-i);}
  static boolean          inverse(boolean i)     {return (boolean)(0-i);}
*/
/// return string circled first character moved to the end
  static String           inverse(String s)      {return (s.substring(1) + s.substring(0,1));}
/// return 3-boolean array all values inverted
  static boolean[]        inverse(boolean[] bx)  { boolean[] bi = { !bx[0], !bx[1], !bx[2]};
                                                   return bi;
                                                   }
/// return simplecontainer with inverted integer, string,arrray and static int value
  static SimpleContainer  inverse(SimpleContainer c){ SimpleContainer ci = new SimpleContainer();
                                                      ci.number       = inverse(c.number);
                                                      ci.name         = inverse(c.name);
                                                      ci.preferences  = inverse(c.preferences);
                                                      return ci;
                                                    }

/*********************************************************************************************************************/
/**
* test on accessing the curent class function and the 'super' function it overrides fromwithin jni calls
*/
  private void testSuperclassAccess()
  {
/**we'll define two instances of the function <protected byte getClassVersion()> returning (byte)1 in the base class
and (byte)2 in the derived one. Then we'll call two native functions that respectingly call the base and the
derived function
*/
    byte baseversion  = getBaseVersion();
    byte currentversion = getCurrentVersion();
    harness.check(currentversion == getVersion(), "Derived function returns "+currentversion+" instead of expected "+getVersion() );
    harness.check(baseversion != getVersion(), "Base function call returns derived functin output "+getVersion() );
    harness.check(baseversion == super.getVersion(),"Base function call returns "+baseversion+" instead of expected"+super.getVersion() );
  }

/// call the base classes 'super'function of getVersion() fromwithin jni
  private native byte getBaseVersion();
/// call the current function getVersion fromwithin jni
  private native byte getCurrentVersion();

/// derived function : returns (byte)2 whereas the base-class's function returnes (byte)1
  protected byte getVersion() {return((byte)2); }
/*********************************************************************************************************************/
/**
* TestRunner interface : perform the tests described and pass the results to the testharness
*/
  public void test (TestHarness newharness)
	{
		harness = newharness;
		harness.setclass("JNI member & function access");
		SimpleContainer.common = -1492;
		testMemberVariableAccess();
		testMemberFunctionAccess();
		testStaticFunctionAccess();
		testSuperclassAccess();
	}

}
