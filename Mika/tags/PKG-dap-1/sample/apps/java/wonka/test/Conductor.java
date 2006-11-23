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

/*
** $Id: Conductor.java,v 1.2 2006/05/16 08:24:09 cvs Exp $
*/

package wonka.test;

import java.util.Vector;

class Conductor extends Thread {

  public char nowPlaying;
  public Carillon carillon;

  private Vector words;
  private String sequence;
  private int slotNumber;
  private int numberOfSlots;
  private int tally;
  private int bucket;

  public Conductor(Carillon carillon, Vector words, String s) {
    super(carillon,"Conductor");
    this.carillon = carillon;
    this.words = words;
    this.sequence = s;
    numberOfSlots = s.length();
    nowPlaying = sequence.charAt(0);
    String theWord = (String)words.elementAt(0);
  }

  synchronized void next() {
    ++slotNumber;

    if(slotNumber==numberOfSlots) {
      System.out.println(++tally);
      slotNumber = 0;
      this.interrupt();
      carillon.checkForTermination(tally);
    }
    nowPlaying = sequence.charAt(slotNumber);

//    String theWord = (String)words.elementAt(slotNumber);
//    synchronized (theWord) {
//      theWord.notifyAll();
//    }
  }

  public void run() {
    String theWord = (String)words.elementAt(0);
    synchronized (theWord) {
      theWord.notifyAll();
    }

    ThreadGroup tg = this.getThreadGroup();
    for(;;) {
      try {
        Thread.sleep(60000);
        System.err.println(tg+": watchdog timer expired! Leaky bucket = "+bucket);
        if (bucket > 10) {
          bucket -= 10;
        }
        else {
          tg.stop();
        }
      }
      catch (InterruptedException e) {
        ++bucket;
      }
    }
  }

}
