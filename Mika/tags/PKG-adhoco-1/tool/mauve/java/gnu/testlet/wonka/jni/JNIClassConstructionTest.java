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

public class JNIClassConstructionTest extends ContainerFunctions implements Testlet {

// load the native library//
  static
  {
    System.loadLibrary("JNIClassConstructionTest");
  }

/****************************************************************************************
* our variables:
*/
  protected static TestHarness harness;


/****************************************************************************************/
/**
 Ussing JNI FindClass together with NewObject and AllocObject to construct a new container from class name
*/
  private void testClassBuildingDefault()
  {
    SimpleContainer simplereference = buildSimpleContainer(1500, "zero", false, false, true, 1499);
    FunctionContainer functionreference = buildFunctionContainer(1501, "one", false, true, false, 1502, "two", false, true, true);

    // building a default simple container using NewObject(void)
    harness.checkPoint("build default Containers using NewObject(void)");
    compareSimpleContainer(newDefaultSimpleContainer()  , new SimpleContainer(), harness);
    compareFunctionContainer(newDefaultFunctionContainer(), new FunctionContainer(), harness);
    compareSimpleContainer(newClonedSimpleContainer(simplereference)  , new SimpleContainer(), harness);
    compareFunctionContainer(newClonedFunctionContainer(functionreference), new FunctionContainer(), harness);
  }

  /// building a default simple container without initialization using NewObject(void)
  private native SimpleContainer        newDefaultSimpleContainer();
  private native FunctionContainer      newDefaultFunctionContainer();
  private native SimpleContainer        newClonedSimpleContainer(SimpleContainer reference);
  private native FunctionContainer      newClonedFunctionContainer(FunctionContainer reference);


  private void testClassBuildingConstructors()
  {
    ConstructionContainer testcon;

    // building a construction container using NewObject calls to variable constructors
    harness.checkPoint("build ConstructionContainer using NewObject constructor");
    testcon = newConstructedContainer(1510, "ten", false, false, false);
    compareConstructionContainer(testcon, 1510, "ten", false, false, false, harness);
    testcon = newConstructedContainer(1511, "eleven", buildBoolArray(false, false, true));
    compareConstructionContainer(testcon, 1511, "eleven", false, false, true, harness);
    testcon = newConstructedContainer(buildSimpleContainer(1512, "twelve", false, true, false));
    compareConstructionContainer(testcon, 1512, "twelve", false, true, false, harness);

    // building a construction container using AllocObject and init calls
    harness.checkPoint("build ConstructionContainer using AllocObject and constructor");
    testcon = newAllocatedContainer(1513, "thirteen", false, true, true);
    compareConstructionContainer(testcon, 1513, "thirteen", false, true, true, harness);
    testcon = newAllocatedContainer(1514, "fourteen", buildBoolArray(true, false, false));
    compareConstructionContainer(testcon, 1514, "fourteen", true, false, false, harness);
    testcon = newAllocatedContainer(buildSimpleContainer(1515, "fifteen", true, false, true));
    compareConstructionContainer(testcon, 1515, "fifteen", true, false, true, harness);

    // building a construction container using default constructors and data calls
    harness.checkPoint("build ConstructionContainer using NewObject(default) constructor and function calls");
    testcon = newInitialisedContainer(1516, "sixteen", true, true, false);
    compareConstructionContainer(testcon, 1516, "sixteen", true, true, false, harness);
    testcon = newInitialisedContainer(1517, "seventeen", buildBoolArray(true, true, true));
    compareConstructionContainer(testcon, 1517, "seventeen", true, true, true, harness);
    testcon = newInitialisedContainer(buildSimpleContainer(1518, "eightteen", false, false, false));
    compareConstructionContainer(testcon, 1518, "eightteen", false, false, false, harness);
  }

  /// building a construction container usingFindClass and NewObject
  private native ConstructionContainer  newConstructedContainer(int i, String s, boolean b0, boolean b1, boolean b2);
  private native ConstructionContainer  newConstructedContainer(int i, String s, boolean[] bx);
  private native ConstructionContainer  newConstructedContainer(SimpleContainer sc);

  /// building a construction container using FindClass, AllocObject and function calls
  private native ConstructionContainer  newAllocatedContainer(int i, String s, boolean b0, boolean b1, boolean b2);
  private native ConstructionContainer  newAllocatedContainer(int i, String s, boolean[] bx);
  private native ConstructionContainer  newAllocatedContainer(SimpleContainer sc);

  /// building a construction container using FindClass, AllocObject and function calls
  private native ConstructionContainer  newInitialisedContainer(int i, String s, boolean b0, boolean b1, boolean b2);
  private native ConstructionContainer  newInitialisedContainer(int i, String s, boolean[] bx);
  private native ConstructionContainer  newInitialisedContainer(SimpleContainer sc);

 /****************************************************************************************/
/**
 Ussing JNI NewObjectArray together with NewObject and AllocObject to construct a new array of given classes
*/
  private void testClassArrayBuilding()
  {
    int testsize = 3;
    int containernumber;
    ConstructionContainer testconstructed;
    ConstructionContainer[] arrayconstructed;

    harness.checkPoint("using NewObjectArray to construct an array of copies from a template");
    testconstructed = buildConstructionContainer(1520, "twenty", false, false, true);
    arrayconstructed = buildStaticArray(testconstructed, testsize);
    for(int i=0; i<testsize; i++)
      compareConstructionContainer(arrayconstructed[i], testconstructed, harness);

    harness.checkPoint("using NewObjectArray to construct an array of a constructor-built class member");
    testconstructed = buildConstructionContainer(1521, "twenty-one", false, true, false);
    arrayconstructed = buildStaticArray(1521, "twenty-one",false, true, false, testsize);
    for(int i=0; i<testsize; i++)
      compareConstructionContainer(arrayconstructed[i], testconstructed, harness);

/** WARNING, In JDK 1.2 NewObjectArray assigns a POINTER to an object to all instances of the object array Like this a change
in the original object is directly reflected to ALL members of the array, as the example belows showed
The only workaround consists of building an array of allocated memory space, and then initialising the arrays one by one
(as is done in the next example)
*/
    harness.checkPoint("NewObjectArray(template) and SetObjectArrayElement constructs an incrementing array");
    testconstructed = buildConstructionContainer(1522, "twenty-two", false, true, true);
    containernumber = 1522;
    arrayconstructed = buildIncrementalArray(testconstructed, containernumber, testsize);
    for(int i=0; i<testsize; i++)
    {
      containernumber++;
      compareConstructionContainer(arrayconstructed[i], containernumber, "twenty-two",false, true, true, harness);
    }

    harness.checkPoint("NewObjectArray(AllocObject) and SetObjectArrayElement constructs an incrementing array");
    containernumber = 1523;
    arrayconstructed = buildIncrementalArray(containernumber, "twenty-three", true, false, true, testsize);
    for(int i=0; i<testsize; i++)
    {
      containernumber++;
      compareConstructionContainer(arrayconstructed[i], containernumber, "twenty-three", true, false, true, harness);
    }
  }

/** WARNING, In JDK 1.2 NewObjectArray assigns a POINTER to an object to all instances of the object array Like this a change
in the original object is directly reflected to ALL members of the array, as the example belows showed
The only workaround consists of building an array of allocated memory space, and then initialising the arrays one by one
(as is done in the next example)
*/
/// building an array of <size> instances of a constructor container using NewObjectArray with the given template(see the warning)
  private native ConstructionContainer[]  buildStaticArray(ConstructionContainer template, int arraysize);
/// building an array of <size> instances of a constructor container template using NewObject and NewObjectArray(see the warning)
  private native ConstructionContainer[]  buildStaticArray(int i, String s, boolean b0, boolean b1,boolean b2, int arraysize);
/// building an incremental array of containers after a template, using NewObjectArray and apiece-by piece function calls(see the warning)
  private native ConstructionContainer[]  buildIncrementalArray(ConstructionContainer template, int i, int arraysize);
/// building an incremental array of constriction containers using AllocArray and piece-by-piece initialisation (see the warning)
  private native ConstructionContainer[]  buildIncrementalArray(int i, String s, boolean b0, boolean b1,boolean b2,int arraysize);

/****************************************************************************************/
/**
 Ussing JNI ThrowNew and NewObject / Throw to construct and throw exceptions,
*/
  private void testClassExceptionBuilding()
  {
    harness.checkPoint("JNI call throwing a NullPointerException");
    try
    {
      throwsNullPointerException("null pointer exception thrown");
      harness.check(false, "no Exception thrown" );
    }
    catch(Exception e)
    {
      if(e instanceof NullPointerException)
      {
        String errormsg = e.getMessage();
        if(errormsg.indexOf("null pointer exception thrown")>=0)
          harness.check(true,"null pointer exception thrown");
        else
          harness.fail("NullPointrException returned <"+errormsg+"> instead of desired message <null pointer exception thrown>");
      }
      else
        harness.check(false, "error not of type NullPointerException");
    }


    harness.checkPoint("JNI call throwing a ContainerException (built through constructor)");
    try
    {
      throwsConstructedContainerException(1540,"fourty",buildBoolArray(true,true,false),
                                    buildSimpleContainer(1541,"fourty-one",true,false,true));
      harness.check(false, "no Exception thrown" );
    }
    catch(Exception e)
    {
      if(e instanceof ContainerException)
      {
System.out.println("=> ce = (ContainerException)e");
        ContainerException ce = (ContainerException)e;
System.out.println("=> fc = ce.getFunctionContainer()");
        FunctionContainer fc = ce.getFunctionContainer();
System.out.println("=> comparing FunctionContainer()");
        compareFunctionContainer(fc, 1540,"fourty", true, true, false, 1541, "fourty-one", true, false, true, harness);
      }
      else
        harness.check(false, "error not of type ContainerException");
    }

  }


  /// call to a JNI c-function that uses ThrowNew to throw a NullPointerException
  private native void throwsNullPointerException(String s) throws NullPointerException;
  /// call to a JNI c-function that uses ThrowNew to build and throw a ContainerException
  private native void throwsConstructedContainerException(int i, String s, boolean[] bx, SimpleContainer sc) throws ContainerException;


/****************************************************************************************/
/**
 Using JNI ExceptionOccurred to handle Java exceptions in JNI function calls
*/
  private void testClassExceptionHandling()
  {
    harness.checkPoint("JNI handling a java function that throws a ContainerException");

    FunctionContainer  fc = handlesException();
    if (fc != null)
      compareFunctionContainer( fc, 1550, "fifty", false, false, true, 1551, "fifty-one", false, true, false, harness);
    else
      harness.fail("Unable to catch ContainerException");

  }

  /// calls the java throwsError-function that throws a ContainerError. Catches the error and returns its container data
  private native FunctionContainer handlesException();

  /// throws a ContainerException. when called from within a JNI function, it is upon the JNI code to detect and handle the exception
  void throwsError() throws ContainerException
  {
    //does nothing but throwing a container exception
    FunctionContainer fc = buildFunctionContainer(1550, "fifty", false, false, true, 1551, "fifty-one", false, true, false);
    throw (new ContainerException(fc) );
  }


/****************************************************************************************/
/**
 Testing the specific dependency functions IsAssignableFrom(), IsInstanceOf(), IsSameObject() and GetSuperClass()
*/
  private void testClassDependencies()
  {
    harness.checkPoint("testing <IsAssignableForm()> JNI function");
    harness.check( testAssignable("gnu/testlet/wonka/jni/JNIClassConstructionTest", "gnu/testlet/wonka/jni/JNIClassConstructionTest"),"assignable to itself");

    harness.check( testAssignable("gnu/testlet/wonka/jni/JNIClassConstructionTest", "gnu/testlet/wonka/jni/ContainerFunctions"),"assignable to extends base class");
    harness.check( testAssignable("gnu/testlet/wonka/jni/JNIClassConstructionTest", "gnu/testlet/Testlet"),"assignable to implements class");
    harness.check(!testAssignable("gnu/testlet/wonka/jni/JNIClassConstructionTest", "gnu/testlet/TestHarness"),"not assignable to complete stranger");

    harness.checkPoint("testing <IsInstanceOf()> JNI function");
    ContainerException ex = new ContainerException("error detecting instance");
    harness.check( testInstanceOf(ex, "gnu/testlet/wonka/jni/ContainerException"),"instance of its own class");
    harness.check( testInstanceOf(ex, "java/lang/Exception"),"instance of its base class");
    harness.check( testInstanceOf(null, "gnu/testlet/wonka/jni/ContainerException"),"null should be instance of all classes");
    harness.check(!testInstanceOf(ex, "gnu/testlet/wonka/jni/SimpleContainer"),"instance of other class");

    SimpleContainer sc1 = buildSimpleContainer(1611, "jikes", true, true, true);
    SimpleContainer sc2 = sc1;
    harness.checkPoint("testing <IsSameObject()>' JNI function");
    harness.check( testSame(sc1, sc1),"IsSameObject with itself");
    harness.check( testSame(sc1, sc2),"IsSameObject with equal name of itself");
    harness.check(!testSame(sc1, buildSimpleContainer(1612, "kaboom", false, false, false) ),"IsSameObject new constructed (should fail)" );
    harness.check(!testSame(sc1, buildSimpleContainer(1611, "jikes", true, true, true) ),"IsSameObject with new constructed of same value (should fail)" );
    harness.check(!testSame(sc1, null),"IsSameObject with null (should fail)");
    harness.check(!testSame(null, sc1),"IsSameObject with null (should fail)");
    harness.check( testSame(null, null),"null IsSameObject with null (should be true)");
  }

  /// return class1 is assignable to class 2
  private native boolean testAssignable(String class1, String class2);
  /// return exception is instance of to class 1
  private native boolean testInstanceOf(ContainerException c, String class1);
  /// return container i is same as container j
  private native boolean testSame(SimpleContainer i, SimpleContainer j);

/*********************************************************************************************************************/
/**
* TestRunner interface : perform the tests described and pass the results to the testharness
*/
  public void test (TestHarness newharness)
	{
		harness = newharness;
		harness.setclass("JNI class and array construction/ class dependencies");
		
    harness.verbose("Start tests dependencies");
		testClassDependencies();

    harness.verbose("Start tests building");
		testClassBuildingDefault();
		testClassBuildingConstructors();
    harness.verbose("Start tests array building");
		testClassArrayBuilding();
		
    harness.verbose("Start tests exception throwing");
		testClassExceptionBuilding();
    harness.verbose("Start tests exception catching and dispatching");
		testClassExceptionHandling();
	}

}
