package wonka.bytecodetest;

import java.math.BigDecimal;
import java.sql.Array;
import java.util.ArrayList;
import java.lang.Math;
import java.io.*;
import wonka.bytecodetest.TestClass;
import wonka.bytecodetest.TestWideClass;
import wonka.bytecodetest.TestInterface;

public class Main {
  static String s = " ";
  static int nbr = 0;
  //static double dbl = Math.PI;
  static long l = 0;
  static long[] ll = {1,2};

  public static void main(String[] args) {

    // Tests which do not require any method calls - except that if they fail they will
    // call System.exit(1), which could crash if static method calls don't work ...

    wonka.vm.Etc.woempa(7,"Starting tests");
    double dbl = Math.PI;
    s = "foo";
    l = Long.MAX_VALUE;
    double testdbl = 1.2;
    nbr = 1;
    int dummy = BigDecimal.ROUND_UP;
    int[] array  = {1,2,3};
    nbr = array.length;
    
    TestClass testThings;
    testThings = new TestClass("qwe",1, dbl,0.1f);

    test_invokestatic();

    double pi = testThings.testDouble(testdbl);

    float flo = testThings.testFloat(0.1f);

    int times = testThings.testLoop(150);
    
    int[] nbr2 = testThings.testArray(array);
    
    long[] larr = testThings.testLongArray(ll);

    int[][] anewarray = testThings.testMultiANewArray();

    if(testThings != null && testThings instanceof TestClass){
      wonka.vm.Etc.woempa(7, "Testing instanceof");
    }

    synchronized(testThings){
      testThings.testMonitor(5);
    }

    testThings.testInterface();
    testInvInterface();

    testThings.testStoreAndLoad();
    testThings.testCast();
    TestWideClass wideTest = new TestWideClass();
    wideTest.testWideInt();
    wideTest.testWideFloat();
    wideTest.testWideDouble();
    wideTest.testWideLong();
    wideTest.test_ldc_w();
    // This too assumes that static method calls work, and it triggers initialisation if class System.
    // In case of doubt, comment it out
    wonka.vm.Etc.woempa(7,"all tests ran ok");
    System.exit(1);
  }

  private static void test_invokestatic() {
    s = "bar";
  }

  private static void testInvInterface(){
    TestInterface testInterfaceClass;
    if(l < 1){
      testInterfaceClass = new TestInterfaceClass(s, nbr, 1.1, 0.1f);
    } else {
      testInterfaceClass = new TestClass(s, nbr, 1.1, 0.1f);
    }
    testInterfaceClass.testInterface();
  }

}

