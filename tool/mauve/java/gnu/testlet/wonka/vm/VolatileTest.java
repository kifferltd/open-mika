/**************************************************************************
* Copyright  (c) 2008 by Chris Gray. All rights reserved.                 *
**************************************************************************/


package gnu.testlet.wonka.vm;

import gnu.testlet.*;

public class VolatileTest implements Testlet, Runnable {

  static volatile long static_glare = 0x0123456789abcdefL;
  private static boolean running;
  private static int count0;
  private static int count1;
  private static int count2;

  private VolatileTest parent;
  protected TestHarness th;
  private volatile long instance_glare;
  private boolean test_static;

  public VolatileTest() {
  }

  private VolatileTest(VolatileTest parent, boolean test_static) {
    this.parent = parent;
    this.test_static = test_static;
  }

  public void test(TestHarness harness) {
    parent = this;
    instance_glare = 0x0123456789abcdefL;
    th = harness;
    th.checkPoint("Testing volatile long fields");
    th.setclass("java.lang.Long");
    running = true;
    for (int i = 0; i < 50; ++i) {
      new Thread(new VolatileTest(this, (i % 2) == 0)).start();
    }
    try {
      for (count0 = 0; count0 < 1000000 && count1 < 1000000 && count2 < 1000000; ++count0) {
        long copy = static_glare;
        if (copy != 0xf0123456789abcdeL
         && copy != 0xef0123456789abcdL
         && copy != 0xdef0123456789abcL
         && copy != 0xcdef0123456789abL
         && copy != 0xbcdef0123456789aL
         && copy != 0xabcdef0123456789L
         && copy != 0x9abcdef012345678L
         && copy != 0x89abcdef01234567L
         && copy != 0x789abcdef0123456L
         && copy != 0x6789abcdef012345L
         && copy != 0x56789abcdef01234L
         && copy != 0x456789abcdef0123L
         && copy != 0x3456789abcdef012L
         && copy != 0x23456789abcdef01L
         && copy != 0x123456789abcdef0L
         && copy != 0x0123456789abcdefL
        ) {
          throw new RuntimeException("static_glare = " + Long.toHexString(copy));
        }
        copy = instance_glare;
        if (copy != 0xf0123456789abcdeL
         && copy != 0xef0123456789abcdL
         && copy != 0xdef0123456789abcL
         && copy != 0xcdef0123456789abL
         && copy != 0xbcdef0123456789aL
         && copy != 0xabcdef0123456789L
         && copy != 0x9abcdef012345678L
         && copy != 0x89abcdef01234567L
         && copy != 0x789abcdef0123456L
         && copy != 0x6789abcdef012345L
         && copy != 0x56789abcdef01234L
         && copy != 0x456789abcdef0123L
         && copy != 0x3456789abcdef012L
         && copy != 0x23456789abcdef01L
         && copy != 0x123456789abcdef0L
         && copy != 0x0123456789abcdefL
        ) {
          throw new RuntimeException("instance_glare = " + Long.toHexString(copy));
        }
      }
    }
    finally {
      running = false;
    }
 }

  public void run() {
    int foo = 0;
    if (test_static) {
      while (running) {
        static_glare = 0xf0123456789abcdeL;
        static_glare = 0xef0123456789abcdL;
        static_glare = 0xdef0123456789abcL;
        static_glare = 0xcdef0123456789abL;
        static_glare = 0xbcdef0123456789aL;
        static_glare = 0xabcdef0123456789L;
        static_glare = 0x9abcdef012345678L;
        static_glare = 0x89abcdef01234567L;
        static_glare = 0x789abcdef0123456L;
        static_glare = 0x6789abcdef012345L;
        static_glare = 0x56789abcdef01234L;
        static_glare = 0x456789abcdef0123L;
        static_glare = 0x3456789abcdef012L;
        static_glare = 0x23456789abcdef01L;
        static_glare = 0x123456789abcdef0L;
        static_glare = 0x0123456789abcdefL;
        count1 += 16;
        if (count1 > 1000000) {
          return;
        }
      }
    }
    else {
      while (running) {
        parent.instance_glare = 0xf0123456789abcdeL;
        parent.instance_glare = 0xef0123456789abcdL;
        parent.instance_glare = 0xdef0123456789abcL;
        parent.instance_glare = 0xcdef0123456789abL;
        parent.instance_glare = 0xbcdef0123456789aL;
        parent.instance_glare = 0xabcdef0123456789L;
        parent.instance_glare = 0x9abcdef012345678L;
        parent.instance_glare = 0x89abcdef01234567L;
        parent.instance_glare = 0x789abcdef0123456L;
        parent.instance_glare = 0x6789abcdef012345L;
        parent.instance_glare = 0x56789abcdef01234L;
        parent.instance_glare = 0x456789abcdef0123L;
        parent.instance_glare = 0x3456789abcdef012L;
        parent.instance_glare = 0x23456789abcdef01L;
        parent.instance_glare = 0x123456789abcdef0L;
        parent.instance_glare = 0x0123456789abcdefL;
        count2 += 16;
        if (count2 > 1000000) {
          return;
        }
      }
    }
  }

}
