/**************************************************************************
* Copyright (c) 2006 by Chris Gray, /k/ Embedded Java Solutions.          *
* All rights reserved.                                                    *
**************************************************************************/


/*
** $Id: JDWP.java,v 1.1 2006/10/04 14:24:15 cvsroot Exp $
*/

package wonka.vm;

public final class JDWP extends Thread {

  private static JDWP theJDWP;

  private static native boolean isRunning();

  public static synchronized JDWP getInstance() {
    if (theJDWP == null) {
      theJDWP = new JDWP();
    }

    while (!isRunning()) {
      try {
        Thread.sleep(1000);
      }
      catch (InterruptedException ie) {
      }
    }

    return theJDWP;
  }

  private JDWP() {
    super("JDWP");
    setPriority(10);
    setDaemon(true);
    start();
  }

  public native void run();

}

