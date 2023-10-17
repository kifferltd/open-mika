package wonka.bytecodetest;

import java.math.BigDecimal;
import java.lang.Math.PI;

public class Main {
  static String s;
  static float f;

  public static void main(String[] args) {
    s = "foo";
    if (s != "foo") System.exit(1);
    f = PI;
    if (f != PI) System.exit(1);
    int dummy = BigDecimal.ROUND_UP;
    test_invokestatic();
    if (s != "bar") System.exit(1);
  }

  private static test_invokestatic() {
    s = "bar";
  }
}
