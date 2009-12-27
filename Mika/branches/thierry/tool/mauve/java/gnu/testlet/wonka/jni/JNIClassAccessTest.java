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
/**
JNI class access tests: access the data and the functions of a given class through native calls.
We'll try to access the data of various Contianer class instances and compare the results
using the ContainerFunctions' compare-functions. */

public class JNIClassAccessTest extends ContainerFunctions implements Testlet
{

// load the native library//
  static
  {
    System.loadLibrary("JNIClassAccessTest");
  }

/****************************************************************************************
* our variables:
*/
  protected static TestHarness harness;


/****************************************************************************************/
/**
Variable access: Build a SimpleContainer object and access its variables through JNI calls
*/
  private void testClassVariableAccess()
  {
    SimpleContainer container = buildSimpleContainer( 1400,"alfa",false, false, true, 100);

    harness.checkPoint("Direct access class member <primitive int> through native calls");
    harness.check(getClassInteger(container), 1400);
    setClassInteger(container, 1401);
    harness.check(container.number, 1401);

    harness.checkPoint("Direct access class member <String> through native calls");
    harness.check(getClassString(container), "alfa");
    setClassString(container, "beta");
    harness.check(container.name, "beta");



    harness.checkPoint("Direct access member <Array[3]> via native calls");
    checkBoolArray(getClassArray(container), false, false, true, harness);
    setClassArray(container, buildBoolArray(false, true, false));
    checkBoolArray(container.preferences, false, true, false, harness);
    setClassArray(container, false, true, true);
    checkBoolArray(container.preferences, false, true, true, harness);


    harness.checkPoint("Direct access static class member through native calls");
    harness.check(getClassStatic(container), 100);
    setClassStatic(container, 101);
    harness.check(SimpleContainer.common, 101);

  }


// natives for the member access functions: (return or assign the member variable mentioned)
  /// return the int member of a given class
  private native int        getClassInteger(SimpleContainer cs);
  /// set the int member of a given class to the specified value
  private native void       setClassInteger(SimpleContainer cs, int i);
  /// return the string member of a given class
  private native String     getClassString (SimpleContainer cs);
  /// set the string member of a given class to the specified value
  private native void       setClassString (SimpleContainer cs, String s);
  /// return the boolean[3] member of a given class
  private native boolean[]  getClassArray  (SimpleContainer cs);
  /// set the boolean[3] member of a given class to the specified boolean array
  private native void       setClassArray  (SimpleContainer cs, boolean[] bx);
  /// set the boolean[3] member of a given class to the specified 3 booleans
  private native void       setClassArray  (SimpleContainer cs, boolean b0, boolean b1, boolean b2);
  /// return the static int member of a given class
  private native int        getClassStatic (SimpleContainer cs);
  /// set the static int member of a given class to the specified value
  private native void       setClassStatic (SimpleContainer cs, int i);




/****************************************************************************************/
/**
function access: Build a FunctionContainer object and call its get- and set- functions through JNI calls
Check the results by looking at the returned/set member variables
*/
  private void testClassFunctionAccess()
  {
    FunctionContainer container = buildFunctionContainer( 1402,"gamma",true, false, false, 1403, "delta", true, true, true);
    harness.checkPoint("Function access class member <primitive int> through native calls");
    harness.check(getClassInteger(container), 1402);
    setClassInteger(container, 1404);
    harness.check(container.getNumber(), 1404);

    harness.checkPoint("Function access class member <String> through native calls");
    harness.check(getClassString(container), "gamma");
    setClassString(container, "epsilon");
    harness.check(container.getName(), "epsilon");

    harness.checkPoint("Function access member <Array[3]> via native calls");
    checkBoolArray(getClassArray(container), true, false, false, harness);
    setClassArray(container, buildBoolArray(true, false, true));
    checkBoolArray(container.getPreferences(), true, false, true, harness);
    setClassArray(container, true, true, false);
    checkBoolArray(container.getPreferences(), true, true, false, harness);

    harness.checkPoint("Function access class member <self-defined class> through native calls");
    compareSimpleContainer(getClassContainer(container), 1403,"delta", true, true, true, harness);
    setClassContainer(container, buildSimpleContainer(1404,"zeta", false, false, false));
    compareSimpleContainer(container.getInternal(), 1404, "zeta", false, false, false, harness);
  }

// natives for the member access functions: (return or assign the member variable mentioned)
  /// call the container's get-function to return the int member of that container
  private native int              getClassInteger   (FunctionContainer fs);
  /// call the container's set-function to set the int member of that container to the given value
  private native void             setClassInteger   (FunctionContainer fs, int i);
  /// call the container's get-function to return the string member of that container
  private native String           getClassString    (FunctionContainer fs);
  /// call the container's set-function to set the string member of that container to the given value
  private native void             setClassString    (FunctionContainer fs, String s);
  /// call the container's get-function to return the boolean[3] member of that container
  private native boolean[]        getClassArray     (FunctionContainer fs);
  /// call the container's set-function to set the boolean[3] member of that container to the given boolean array
  private native void             setClassArray     (FunctionContainer fs, boolean[] bx);
  /// call the container's set-function to set the boolean[3] member of that container to the 3 given booleans
  private native void             setClassArray     (FunctionContainer fs, boolean b0, boolean b1, boolean b2);
  /// call the container's get-function to return the SimpleContainer member of that container
  private native SimpleContainer  getClassContainer (FunctionContainer fs);
  /// call the container's set-function to set the SimpleContainer member of that container to the given value
  private native void             setClassContainer (FunctionContainer fs, SimpleContainer sc);

/****************************************************************************************/
/**
Static Variable functionalities: Build a series of SimpleContainer anf FunctionContainer instances
and test the passing of the static integer of the SimpleContainer (given and CimpleContainer member of given FunctionContainer)
from one instance to the other.
*/
  private void testClassStaticMemberAccess()
  {
    SimpleContainer   direct  = new SimpleContainer();
    SimpleContainer   member1 = new SimpleContainer();
    SimpleContainer   member2 = new SimpleContainer();
    FunctionContainer member3 = new FunctionContainer();

    SimpleContainer.common = 1405;
    //simple container access
    harness.checkPoint("accessing static member variable through JNI calls");
    harness.check(getClassStatic(member1), 1405);
    setClassStatic(member1, 1406);
    harness.check(SimpleContainer.common, 1406);
    harness.check(getClassStatic(member2), 1406);

    harness.checkPoint("accessing static member from secundary class variable through JNI calls");
    SimpleContainer.common = 1407;
    harness.check(getClassStatic(member3), 1407);
    setClassStatic(member3, 1408);
    harness.check(SimpleContainer.common, 1408);
    harness.check(getClassStatic(member1), 1408);
  }

// natives for the static access functions:
/// get the static integer of the SimpleContainer member of a given Functioncontainer (a call to a class fetched from a class)
  private native int  getClassStatic (FunctionContainer fs);
//private int getClassStatic(FunctionContainer fs) {return getClassStatic(getClassContainer(fs));}
/// set the static integer of the SimpleContainer member of a given Functioncontainer to a given value
  private native void setClassStatic (FunctionContainer fs, int i);
//private void setClassStatic (FunctionContainer fs, int i){setClassStatic(getClassContainer(fs), i);}
/****************************************************************************************/
/**
Static function access: call a the static function of a given MultiFunctionContainer to construct a SimpleContainer
and check the result
*/
  private void testClassStaticFunctionAccess()
  {
    MultiFunctionContainer mfc1 = new MultiFunctionContainer();
    harness.checkPoint("accessing static function through JNI calls");
    compareSimpleContainer(buildSimpleContainerStatic(mfc1,1409,"eta",false, false, true), 1409,"eta",false, false, true, harness);
  }

// natives for the static access functions:
/// Uses the MultiFunctionContainer's static function BuildContainer to construct a SimpleContainer and return it
  private native SimpleContainer  buildSimpleContainerStatic(MultiFunctionContainer mf, int i, String s, boolean b1, boolean b2, boolean b3);

/****************************************************************************************/
/**
Nonvirtual base class function access: call the setVariablesPartial function of a given MultiFunctionContainer's base class
(ConstructionContainer) as opposed to the MultifunctionContainer's own SetVariablesPartial function
*/
  private void testClassNonvirtualFunctionAccess()
  {
    
    MultiFunctionContainer mfc2 = new MultiFunctionContainer();
    
    harness.checkPoint("accessing virtual base class function through JNI calls");

    ConstructionContainer.invert = true;

    buildSimpleContainer(mfc2,1410,"theta",false, true, false);
    compareConstructionContainer(mfc2, 1410,"theta",false, true, false, harness);
    
    buildSimpleContainerNonvirtual(mfc2,1411,"iota",false, true, true);
    compareConstructionContainer(mfc2,-1411,"IOTA",true, false, false, harness);

    ConstructionContainer.invert = false;
  }

// natives for the static access functions:
/// Uses the MultiFunctionContainer's static function BuildContainer to construct a SimpleContainer and return it
  private native void  buildSimpleContainer(ConstructionContainer mf, int i, String s, boolean b1, boolean b2, boolean b3);
  private native void  buildSimpleContainerNonvirtual(ConstructionContainer mf, int i, String s, boolean b1, boolean b2, boolean b3);
/*********************************************************************************************************************
* TestRunner interface : perform the tests described and pass the results to the testharness
*/
  public void test (TestHarness newharness)
	{
		harness = newharness;
		harness.setclass("JNI access members/functions of given classes");
		testClassVariableAccess();
		testClassFunctionAccess();
		testClassStaticMemberAccess();
		testClassStaticFunctionAccess();
		testClassNonvirtualFunctionAccess();
	}

}
