/**************************************************************************
* Copyright (c) 2004 Chris Gray, /k/ Embedded Java Solutions.             *
* All rights reserved.                                                    *
**************************************************************************/

package com.acunia.wonka.rudolph;

/**
 ** Very basic beep for generic non-embedded POSIX-like systems.
 */
class DefaultBeep extends BeepImpl {

  /**
   ** Think like a teletype. Sound the BEL.
   */
  public void beep() {
    System.out.write(7);
    System.out.flush();
  }
}

