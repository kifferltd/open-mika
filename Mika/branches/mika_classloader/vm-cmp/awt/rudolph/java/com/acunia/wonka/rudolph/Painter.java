
/**************************************************************************
* Copyright (c) 2001, 2002, 2003 by Acunia N.V. All rights reserved.      *
*                                                                         *
* This software is copyrighted by and is the sole property of Acunia N.V. *
* and its licensors, if any. All rights, title, ownership, or other       *
* interests in the software remain the property of Acunia N.V. and its    *
* licensors, if any.                                                      *
*                                                                         *
* This software may only be used in accordance with the corresponding     *
* license agreement. Any unauthorized use, duplication, transmission,     *
*  distribution or disclosure of this software is expressly forbidden.    *
*                                                                         *
* This Copyright notice may not be removed or modified without prior      *
* written consent of Acunia N.V.                                          *
*                                                                         *
* Acunia N.V. reserves the right to modify this software without notice.  *
*                                                                         *
*   Acunia N.V.                                                           *
*   Philips-site 5, box 3       info@acunia.com                           *
*   3001 Leuven                 http://www.acunia.com                     *
*   Belgium - EUROPE                                                      *
**************************************************************************/

/*
** $Id: Painter.java,v 1.5 2006/05/27 20:24:51 cvs Exp $
*/

package com.acunia.wonka.rudolph;

public final class Painter implements Runnable {

  private static Painter thePainter;

  private int priority = Thread.MAX_PRIORITY;
  boolean keepOnGoing;
  private Thread thread;
  private Thread shutdown;

  private Painter() {
  }

  public static synchronized Painter getInstance() {
    if (thePainter == null) thePainter = new Painter();
    return thePainter;
  }

  private native void paint(); 

  public void run() {
    while (keepOnGoing) {
      try {
        paint();
        Thread.sleep(20);
      }
      catch (InterruptedException ie) {
      }
      catch (Exception exc) {
        try {
          exc.printStackTrace();
        }
        catch (Exception exc2) {
        }
      }
      catch (Error err) {
        try {
          System.out.println("Painter thread threw " + err + "!");
          err.printStackTrace();
        }
        finally {
          System.exit(253);
        }
      }
    }
  }

  public void start() {
    if(!keepOnGoing) {
      keepOnGoing = true;
      shutdown = new Thread("Painter shutdown hook") {
        public void run() {
          keepOnGoing = false;
        }
      };
      Runtime.getRuntime().addShutdownHook(shutdown);
      thread = new Thread(this, "Picasso");
      thread.setPriority(priority);
      thread.setDaemon(false);
      thread.start();
    }
  }

  public void stop() {
    Runtime.getRuntime().removeShutdownHook(shutdown);
    keepOnGoing = false;
    thread = null;
  }

}

