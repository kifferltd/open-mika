package wonka.bytecodetest;

import java.math.BigDecimal;
import java.sql.Array;
import java.util.ArrayList;
import java.lang.Math;
import java.io.*;

public class DefinetlyNotTakenClassName {

  static int testNbr = 0;
  static double testPi = 1.1;
  static String testMsg = "asdasdas";
  static float testFl = 0.1f;

  static int statInt = 0;
  
  public DefinetlyNotTakenClassName(String msg, int number, double pi, float fl) {
    wonka.vm.Etc.woempa(7,"Constructor");
    testNbr = number;
    testPi = pi;
    testMsg = msg;
    testFl = fl;
    testInvStatic();
  }
  
  public int testArray(int[] arr){
    assert arr != null;
    int[] newArr = arr;
    return newArr[1];
  }

  public long testLongArr(long[] larr){
    assert larr != null;
    return larr[1];
  }

  public double testDouble(double dbl){
    if(dbl != testPi){
      return 1.1;
    }
    return dbl;
  }

  public float testFloat(float fl) {
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
    int ret = 0;
    for(ret = 0; ret<times; ret++){
      //
    }
    return ret;
  }
}
