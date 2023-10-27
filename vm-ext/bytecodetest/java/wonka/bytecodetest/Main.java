package wonka.bytecodetest;

import java.math.BigDecimal;
import java.lang.Math;

public class Main {
  static String s;
  static long l;

  String myString;

  Main() {}

  Main(String str) {
    this();
    myString = str;
  }

  public static void main(String[] args) {

    // Tests which do not require any method calls - except that if they fail they will
    // call System.exit(1), which could crash if static method calls don't work ...
    s = "foo";
    if (s != "foo") System.exit(1);
    l = Long.MAX_VALUE;
    if (l != Long.MAX_VALUE) System.exit(1);
    int dummy = BigDecimal.ROUND_UP;
    test_invokestatic();
    if (s != "bar") System.exit(1);

    test_object_creation();
    // This too assumes that static method calls work, and it triggers initialisation if class System.
    // In case of doubt, comment it out
    // System.out.println("all tests ran ok");
  }

  private static void test_invokestatic() {
    s = "bar";
  }

  private static void test_object_creation() {
    // Here we test opcodes new and invokespecial.

    Main m = new Main();
    if (!(m instanceof Main)) System.exit(1);

    m = new Main("quux");
    if (m.myString != "quux") System.exit(1);
  }
}
