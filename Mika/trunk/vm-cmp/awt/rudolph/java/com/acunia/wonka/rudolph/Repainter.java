/**************************************************************************
* Copyright (c) 2004 by Punch Telematix. All rights reserved.             *
*                                                                         *
* Redistribution and use in source and binary forms, with or without      *
* modification, are permitted provided that the following conditions      *
* are met:                                                                *
* 1. Redistributions of source code must retain the above copyright       *
*    notice, this list of conditions and the following disclaimer.        *
* 2. Redistributions in binary form must reproduce the above copyright    *
*    notice, this list of conditions and the following disclaimer in the  *
*    documentation and/or other materials provided with the distribution. *
* 3. Neither the name of Punch Telematix nor the names of                 *
*    other contributors may be used to endorse or promote products        *
*    derived from this software without specific prior written permission.*
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

package com.acunia.wonka.rudolph;

import java.util.*;
import com.acunia.wonka.rudolph.peers.*;

public class Repainter extends Thread {

  private Vector list = new Vector();
  
  private static Repainter rp;

  public static Repainter getInstance() {
    if(rp == null) {
      rp = new Repainter();
      rp.setDaemon(false);
      rp.setPriority(9);
      rp.start();
    }
    return rp;
  }

  private Repainter() {
    super("Dali");
  }

  public synchronized void repaint(DefaultComponent peer) {
    if(!list.contains(peer)) {
      list.add(peer);
    }
    notify();
  }

  public void run() {
    DefaultComponent component;
    boolean keepgoing;

    while(true) {
      try {
        synchronized(this) {
          wait();
          keepgoing = true;
        }
        while(keepgoing) {

          synchronized(list) {
            if(list.size() != 0) {
              component = (DefaultComponent)list.elementAt(0);
              list.remove(0);
            }
            else {
              keepgoing = false;
              break;
            }
          }

          try {
            component.doRepaint();
          }
          catch(Exception t) {
            t.printStackTrace();
          }
        }
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
          System.out.println("Repainter thread threw " + err + "!");
          err.printStackTrace();
        }
        finally {
          System.exit(253);
        }
      }
    }
  }
}

