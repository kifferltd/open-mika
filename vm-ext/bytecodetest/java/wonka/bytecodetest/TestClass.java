package wonka.bytecodetest;

import java.math.BigDecimal;
import java.sql.Array;
import java.util.ArrayList;
import java.lang.Math;
import java.io.*;

public class TestClass implements TestInterface{

  static int testNbr = 0;
  static double testPi = 1.1;
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
    if(newArr != null){
      if(newArr[1] != 1){
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

  public void testInterface(){
    testNbr = 5;
  }
}
