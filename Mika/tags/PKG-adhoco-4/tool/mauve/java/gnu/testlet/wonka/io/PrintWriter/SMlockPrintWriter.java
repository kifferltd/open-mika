/**************************************************************************
 * Copyright  (c) 2001 by Acunia N.V. All rights reserved.                 *
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
 *   Vanden Tymplestraat 35      info@acunia.com                           *
 *   3000 Leuven                 http://www.acunia.com                     *
 *   Belgium - EUROPE                                                      *
 **************************************************************************/

package gnu.testlet.wonka.io.PrintWriter; // complete the package name ...


/**
 * this file contains a help class used for the java.io.CharArrayWriter test
 * <br>
 * we have this help-class to test if the field lock is used to <br>
 * synchronize the CharArrayWriter methods
 */
class SMlockPrintWriter implements Runnable

{
  public void run() {
    synchronized (wr.getLock()) {
      // th.debug("locked wr");
      wt.set1();
      try {
        Thread.sleep(200L);
      } catch (InterruptedException ie) {
      }
      // th.debug("lockthread is releasing wr-lock");
      wt.inc();
      wr.getLock().notifyAll();
    }
  }

  private SMPrintWriterTest wt;

  private SMExPrintWriter wr;

  public void setWriter(SMExPrintWriter writer) {
    wr = writer;
  }

  public void setWT(SMPrintWriterTest wtest) {
    wt = wtest;
  }
}
