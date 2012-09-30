package java.awt;

class Synchronizer {
  static {
    clinit();
  }

  private static native void clinit();

  static final native void staticLockAWT();

  static final native void staticUnlockAWT();

}

