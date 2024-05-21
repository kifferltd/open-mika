package wonka.bytecodetest;

import java.math.BigDecimal;
import java.sql.Array;
import java.util.ArrayList;
import java.lang.Math;
import java.io.*;
import wonka.bytecodetest.DefinetlyNotTakenClassName;

public class Main {
  static String s = " ";
  //static int[][] intarr;
  static int nbr = 0;
  static double dbl = 0;
  static long l = 0;
  static long[] ll = {1,2};
  //static TestClass testThings = new TestClass("qweqw", 5, Math.PI, 0.1f);

  public static void main(String[] args) {

    // Tests which do not require any method calls - except that if they fail they will
    // call System.exit(1), which could crash if static method calls don't work ...
    
    /*if (true) {
     throw new IOException("What"); 
    }*/

    wonka.vm.Etc.woempa(7,"Starting tests");
    s = "foo";
    wonka.vm.Etc.woempa(7,s);
    l = Long.MAX_VALUE;
    wonka.vm.Etc.woempa(7,"VAR2");
    double testdbl = 1.2;
    wonka.vm.Etc.woempa(7,"VAR3");
    nbr = 1;
    wonka.vm.Etc.woempa(7,"VAR4");
    int dummy = BigDecimal.ROUND_UP;
    wonka.vm.Etc.woempa(7,"VAR5");
    int[] array  = {1,2,3};
    wonka.vm.Etc.woempa(7,"VAR6");
    nbr = array.length;
    
    wonka.vm.Etc.woempa(7,"Obj");
    //
    DefinetlyNotTakenClassName testThings;
    wonka.vm.Etc.woempa(7, "created at least");
    testThings = new DefinetlyNotTakenClassName("qwe",1,1.1d,0.1f);

    wonka.vm.Etc.woempa(7,"Done initializing variables");
    wonka.vm.Etc.woempa(7,"Testing array");
    int nbr2 = testThings.testArray(array);

    test_invokestatic();
    wonka.vm.Etc.woempa(7,"Testing wide");
    testWide(l);

    nbr = testStuff(s);

    wonka.vm.Etc.woempa(7,"Testing long array");
    long nbr3 = testThings.testLongArr(ll);

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

  private static int testStuff(String inp) {
    System.out.println("in testStuff");

    if (inp == "bar"){
      return 54;
    }
    
    return 0;
  }

  private static long testWide(long nbr){
    if (nbr != Long.MAX_VALUE) System.exit(1);
    return nbr;
  }

}
