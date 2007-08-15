/**************************************************************************
* Copyright (c) 2001 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of other           *
*    contributors may be used to endorse or promote products derived      *
*    from this software without specific prior written permission.        *
*                                                                         *
* THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESS OR IMPLIED          *
* WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF    *
* MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.    *
* IN NO EVENT SHALL PUNCH TELEMATIX OR OTHER CONTRIBUTORS BE LIABLE       *
* FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR            *
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF    *
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR         *
* BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,   *
* WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE    *
* OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN  *
* IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.                           *
**************************************************************************/

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
