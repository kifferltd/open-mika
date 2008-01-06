/**************************************************************************
* Copyright  (c) 2008 by Chris Gray. All rights reserved.                 *
**************************************************************************/


package gnu.testlet.wonka.vm;

import gnu.testlet.*;

public class VolatileTest implements Testlet, Runnable {

  static volatile long glare = 0x0123456789abcdefL;
  private static boolean running;

  protected TestHarness th;

  public void test(TestHarness harness) {

    th = harness;
    th.checkPoint("Testing volatile long fields");
    th.setclass("java.lang.Long");
    running = true;
    for (int i = 0; i < 50; ++i) {
      new Thread(new VolatileTest()).start();
    }
    for (int count = 0; count < 1000; ++count) {
      long copy = glare;
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
        running = false;
        throw new RuntimeException("glare = " + Long.toHexString(copy));
      }
    }
 }

  public void run() {
    while (running) {
      glare = 0xf0123456789abcdeL;
      glare = 0xef0123456789abcdL;
      glare = 0xdef0123456789abcL;
      glare = 0xcdef0123456789abL;
      glare = 0xbcdef0123456789aL;
      glare = 0xabcdef0123456789L;
      glare = 0x9abcdef012345678L;
      glare = 0x89abcdef01234567L;
      glare = 0x789abcdef0123456L;
      glare = 0x6789abcdef012345L;
      glare = 0x56789abcdef01234L;
      glare = 0x456789abcdef0123L;
      glare = 0x3456789abcdef012L;
      glare = 0x23456789abcdef01L;
      glare = 0x123456789abcdef0L;
      glare = 0x0123456789abcdefL;
    }
  }

}
