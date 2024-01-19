package wonka.bytecodetest;

import java.math.BigDecimal;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.Math;
import java.io.*;
import wonka.bytecodetest.TestInterface;

public class TestClass implements TestInterface{

  private int testNbr = 0;
  private double testPi = 1.1;
  static String testMsg = "asdasdas";
  static float testFl = 0.123123123123123f;

  static int statInt = 0;
  
  public TestClass(String msg, int number, double pi, float fl) {
    wonka.vm.Etc.woempa(7,"Constructor");
    testNbr = number;
    testPi = pi;
    testMsg = msg;
    testFl = fl;
    testInvStatic();
  }
  
  public int[] testArray(int[] arr){
    int[] newArr = arr;
    String[] strArr = {"hello","hi"};
    if(newArr != null){
      if(newArr[1] != strArr.length){
        return newArr;
      }
    }
    return arr;
  }

  public long[] testLongArray(long[] larr){
    if(larr != null){
      if(larr[1] != 1){
        return larr;
      }
    }
    return larr;
  }

  public double testDouble(double dbl){
    if(dbl != testPi){
      return 1.1;
    }
    return dbl;
  }

  public float testFloat(float fl) {
    if(fl == testFl){
      return testFl;
    }
    return fl;
  }

  public static int testInvStatic(){
    statInt = 1;
    if (statInt == 1) {
      return statInt;
    }
    return -1;
  }

  public int testLoop(int times){
    int[] ret = new int[times];
    for(int i = 0; i<times; i++){
      ret[i] = times;
    }
    return ret.length;
  }

  public int[][] testMultiANewArray(){
    int[][] multiArr = new int[2][1];
    return multiArr;
  }

  public int testMonitor(int mon){
    if(mon == testNbr){
      return 2;
    }
    return 1;
  }

  public int testStoreAndLoad(){
    int a = 0;
    int b = 1;
    int c = 2;
    int d = 3;
    long e = 4;
    if(d == 3){
      if(e == 4){
        return a;
      }
    }
    return 1;
  }

  public int testCast(){
    Object o = null;
    String message = "";
    String s;
    try {
      message = "casting null to Thread should not throw ClassCastException";
      Thread t = (Thread) o; // OK, can cast null to any ref type
      o = "foo";
      message = "casting a String to String should not throw ClassCastException";
      s = (String) o; // OK
    }
    catch (ClassCastException cce) {
      wonka.vm.Etc.wassert(false, message);
    }
    /*try {
      message = "casting a String to Thread should throw ClassCastException";
      Thread t = (Thread) o;
      wonka.vm.Etc.wassert(false, message);
    }
    catch (ClassCastException cce) {
      return 0;
    }*/
    return 0;
  }

  public void testInterface(){
    
  }
}

class TestInterfaceClass extends TestClass {

  public TestInterfaceClass(String msg, int number, double pi, float fl) {
    super(msg, number, pi, fl);
    //TODO Auto-generated constructor stub
  }

  public void testInterface(){
    if(true){
      return;
    }
  }

}