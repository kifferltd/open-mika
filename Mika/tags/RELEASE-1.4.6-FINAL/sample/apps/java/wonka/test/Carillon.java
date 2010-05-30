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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

class Carillon extends ThreadGroup {

  public final static int MAX_HITS = 20;
  public final static int MAX_MISSES = 100;

  public static long word_timeout = 1000;
  public static long sentence_timeout;

  private static Carillon singleton;

  private Vector words = new Vector();
  private StringBuffer alphabet = new StringBuffer();
  public int iterations;

  private Carillon(String sentence, int iterations) {
    super("Carillon: " + iterations + " of '" + sentence + "'");
    this.iterations = iterations; 

    int i=0;
    Enumeration e = new StringTokenizer(sentence);

    while (e.hasMoreElements()) {
      String nextword = (String)e.nextElement();
      words.addElement(nextword);
      char nextchar = Character.forDigit(i,36);
      alphabet.append(nextchar);

      ++i;
    }
    sentence_timeout = word_timeout * i * 2;
    demo(words,new String(alphabet));
  }

  public void uncaughtException(Thread t, Throwable e) {
    System.err.println("Carillon: Thread "+t+" threw uncaught exception "+e);
    System.err.println("Carillon: terminating");
    singleton = null;
    this.stop();
  }

  public static synchronized Carillon getInstance() {
    return getInstance("the quick brown fox jumps over the lazy dog",Integer.MAX_VALUE);
  }

  public static synchronized Carillon getInstance(String sentence) {
    return getInstance(sentence,Integer.MAX_VALUE);
  }

  public static synchronized Carillon getInstance(String sentence, int iterations) {
    if (singleton==null) {
      singleton = new Carillon(sentence,iterations);
    }
    return singleton;
  }

  public synchronized void checkForTermination(int tally) {
    if (tally >= iterations) {
      ThreadGroup tg = singleton;
      tg.stop();
      singleton = null;
    }
  }


  public void demo(Vector words, String alphabet) {
    Hashtable vocabulary = new Hashtable();
    Conductor maestro = new Conductor(this,words,alphabet);
    Enumeration kees;
    int nwords = words.size();

    for (int i = 0; i < nwords; ++i) {
      String nextword = (String)words.elementAt(i);
      char nextchar = Character.forDigit(i,36);
      StringBuffer currentEntry = (StringBuffer)vocabulary.get(nextword);
      if (currentEntry==null) {
        vocabulary.put(nextword,new StringBuffer(""+nextchar));
      }
      else {
        currentEntry.append(""+nextchar);
      }
    }

    kees = vocabulary.keys();
    while (kees.hasMoreElements()) {
      String nextword = (String)kees.nextElement();
      StringBuffer sequence = (StringBuffer)vocabulary.get(nextword);
      Ringer r = new Ringer(maestro,nextword,new String(sequence));
      r.setPriority((nextword.hashCode() % 5) + 5);
      System.out.println("Starting "+r);
      r.start();
    }
    maestro.start();
  }

  public static void main(String[] args) {
//    getInstance("Multithreaded programming is difficult and error prone. It is easy to make a mistake in synchronization that produces a data race, yet it can be extremely hard to locate this mistake during debugging.");
    if (args.length > 0) {
      String sentence = args[0];

      for (int i = 1; i < args.length; ++i) {
        sentence += " " + args[i];
      }
      getInstance(sentence).setDaemon(true);
    }
    else {
      getInstance().setDaemon(true);
    }
  }
}


