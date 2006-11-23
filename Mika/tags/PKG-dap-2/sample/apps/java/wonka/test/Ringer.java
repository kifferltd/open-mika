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
** $Id: Ringer.java,v 1.2 2006/05/16 08:24:09 cvs Exp $
*/

package wonka.test;

class Ringer extends Thread {

  private static final int JUNK_SIZE = 10;

  private Conductor manWithStick;

  private String word;
  private String when;
  private long timeout;

  public Ringer(Conductor c, String whatToSay, String whenToSayIt) {
    super(c.carillon,"Ringer-"+whatToSay+"/"+whenToSayIt);
    manWithStick = c;
    timeout = Carillon.word_timeout;
    word = whatToSay;
    when = whenToSayIt;
  }

  public void run() {
    int hits = 0, misses = 0;
    Object junk;

    try {
      while (hits < Carillon.MAX_HITS && misses < Carillon.MAX_MISSES) {
        synchronized (word) {
          word.wait(timeout);
        if(when.indexOf(manWithStick.nowPlaying)>=0) {
          System.out.print(word + " ");
          hits++;
          misses = 0;
          manWithStick.next();
          word.notifyAll();
        }
        else {
          ++misses;
        }
        }

        if (hits == Carillon.MAX_HITS /* && misses == 0 */ || misses == Carillon.MAX_MISSES) {
          Ringer r = new Ringer(manWithStick,word,when);
          r.start();
        }
      }
    }
    catch (InterruptedException e) {
      System.out.print(this+" was interrupted.");
    }
    catch (Throwable t) {
      t.printStackTrace();
    }
  }

}

