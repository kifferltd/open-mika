package wonka.bytecodetest;

import java.math.BigDecimal;
import java.lang.Math;

public class Main {
  static String s;
  static double d;

  public static void main(String[] args) {
    s = "foo";
    if (s != "foo") System.exit(1);
    d = Math.PI;
    if (d != Math.PI) System.exit(1);
    int dummy = BigDecimal.ROUND_UP;
    test_invokestatic();
    test_putfield(s);
    test_putfield_long(d);
    if (s != "bar") System.exit(1);
  }

  private static void test_invokestatic() {
    s = "bar";
  }

  private static void test_putfield(String s) {
    s = "bar";
    s = 1;
  }

  private static void test_putfield_long(long l){
    long hello = l;
    hello = hello + 5;
  }
}
