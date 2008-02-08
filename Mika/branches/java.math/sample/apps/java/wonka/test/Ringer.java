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

